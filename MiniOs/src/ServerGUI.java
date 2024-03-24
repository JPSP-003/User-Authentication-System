import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerGUI extends JFrame {
    private JTextArea chatArea;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public ServerGUI() {
        setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        clients = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(1278);
            chatArea.append("Server started. Listening on port 1234...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                chatArea.append("New client connected: " + clientSocket + "\n");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    chatArea.append("Received message from client: " + clientMessage + "\n");

                    // Process the client message here

                    // Send the message to all connected clients
                    for (ClientHandler client : clients) {
                        client.writer.println(clientMessage);
                    }
                }

                // Client disconnected
                chatArea.append("Client disconnected: " + clientSocket + "\n");
                reader.close();
                writer.close();
                clientSocket.close();
                clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}