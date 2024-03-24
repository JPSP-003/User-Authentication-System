import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Color;

public class ClientGUI extends JFrame {
    private JPanel chatPanel;
    private JTextField messageField;
    private JTextField nameField;
    private PrintWriter writer;
    private String userName;
    private Random random;
    private Map<String, Color> userColors;

    public ClientGUI() {
        setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        add(messageField, BorderLayout.SOUTH);

        nameField = new JTextField("Your Name");
        nameField.addActionListener(e -> setUserName());
        add(nameField, BorderLayout.NORTH);

        random = new Random();
        userColors = new HashMap<>();

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
        String message = messageField.getText();
        Color userColor = getUserColor(userName);
        writer.println(userName + ": " + message);
        appendChatBubble("You", message, userColor);
        messageField.setText("");
    }

    private void setUserName() {
        userName = nameField.getText();
        nameField.setEditable(false);
        nameField.setFocusable(false);
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
            new ClientGUI().setVisible(true);
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
}

class ChatBubble extends JPanel {
    public ChatBubble(String user, String message, Color userColor) {
        JTextArea textArea = new JTextArea(user + ": " + message);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        Font font = new Font("Helvetica", Font.PLAIN, 16);
        textArea.setFont(font);
        textArea.setForeground(new Color(255, 255, 255)); // Bubble color
        textArea.setBackground(userColor); // Set the text color based on the user

        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}