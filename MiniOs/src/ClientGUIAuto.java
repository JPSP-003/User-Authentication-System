import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Color;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ClientGUIAuto extends JFrame {
    private JPanel chatPanel;
    private JTextField messageField;
    private PrintWriter writer;
    private String userName;
    private Random random;
    private Map<String, Color> userColors;
    private LevenshteinDistance distance;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionListModel;
    private List<String> commonWords;

    public ClientGUIAuto() {
        setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        add(messageField, BorderLayout.SOUTH);

        random = new Random();
        userColors = new HashMap<>();
        distance = new LevenshteinDistance();
        suggestionListModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionListModel);
        suggestionList.setVisibleRowCount(5);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        commonWords = Arrays.asList("hello", "world", "java", "example", "autocorrect", "feature");

        messageField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showSuggestions(messageField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showSuggestions(messageField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showSuggestions(messageField.getText());
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    String selectedSuggestion = suggestionList.getSelectedValue();
                    if (selectedSuggestion != null) {
                        messageField.setText(selectedSuggestion);
                        suggestionListModel.clear();
                        suggestionList.setVisible(false);
                    }
                }
            }
        });

        add(new JScrollPane(suggestionList), BorderLayout.NORTH);

        try {
            Socket socket = new Socket("localhost", 1278);
            writer = new PrintWriter(socket.getOutputStream(), true);

            Thread serverThread = new Thread(new ServerListener(socket));
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String inputMessage = messageField.getText();
        Color userColor = getUserColor(userName);
        writer.println(userName + ": " + inputMessage);
        appendChatBubble("You", inputMessage, userColor);
        messageField.setText("");
    }

    private String autocorrectWord(String word) {
        int minDistance = Integer.MAX_VALUE;
        String suggestedWord = word;

        for (String commonWord : commonWords) {
            int currentDistance = distance.apply(word.toLowerCase(), commonWord);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                suggestedWord = commonWord;
            }
        }

        return suggestedWord;
    }

    private void setUserName() {
        userName = messageField.getText();
        messageField.setEditable(false);
        messageField.setFocusable(false);
        messageField.requestFocus();
    }

    private Color getUserColor(String user) {
        if (userColors.containsKey(user)) {
            return userColors.get(user);
        } else {
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            userColors.put(user, color);
            return color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUIAuto().setVisible(true);
        });
    }

    class ServerListener implements Runnable {
        private Socket socket;
        private BufferedReader reader;

        public ServerListener(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    StringTokenizer tokenizer = new StringTokenizer(serverMessage, ":");
                    String user = tokenizer.nextToken();
                    String message = tokenizer.nextToken();

                    if (!user.equals(userName)) {
                        Color userColor = getUserColor(user);
                        appendChatBubble(user.equals(userName) ? "You" : user, message, userColor);
                    }
                }

                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendChatBubble(String user, String message, Color userColor) {
        SwingUtilities.invokeLater(() -> {
            ChatBubble bubble = new ChatBubble(user, message, userColor);
            chatPanel.add(bubble);
            chatPanel.revalidate();
            chatPanel.repaint();
        });
    }

    private void showSuggestions(String inputWord) {
        suggestionListModel.clear();

        for (String commonWord : commonWords) {
            if (commonWord.startsWith(inputWord.toLowerCase())) {
                suggestionListModel.addElement(commonWord);
            }
        }

        suggestionList.setVisible(!suggestionListModel.isEmpty());
    }
}

class ChatBubble extends JPanel {
    public ChatBubble(String user, String message, Color userColor) {
        JTextArea textArea = new JTextArea(user + ": " + message);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        Font font = new Font("Helvetica", Font.PLAIN, 16);
        textArea.setFont(font);
        textArea.setBackground(new Color(255, 222, 173)); // Bubble color
        textArea.setForeground(userColor); // Set the text color based on the user

        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}