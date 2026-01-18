package com.example.kuroyale.network;

import com.example.kuroyale.protocol.Message;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkManager {
    private static NetworkManager instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private Consumer<Message> messageHandler;
    private boolean isRunning = false;
    private boolean isHost = false;
    private int localPlayerId = 0; // 1 = Host, 2 = Client

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void startHost(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        this.isHost = true;
        this.localPlayerId = 1;

        // Run acceptance in a separate thread to not block UI if needed,
        // but typically we might want to wait for connection in a lobby thread.
        // For simplicity here, we assume this is called when "Host" is clicked and we
        // wait for one connection.
        // A better approach for UI is doing this async.
        new Thread(() -> {
            try {
                System.out.println("Host started, waiting for client on port " + port);
                socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());
                setupStreams();
                startListening();
                // Notify via handler that connection is established if needed, or wait for Join
                // Request
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close(); // Stop listening for more clients once one connects
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void connect(String ip, int port) throws IOException {
        this.isHost = false;
        this.localPlayerId = 2;
        socket = new Socket(ip, port);
        setupStreams();
        startListening();
        System.out.println("Connected to host at " + ip + ":" + port);
    }

    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void startListening() {
        isRunning = true;
        listenerThread = new Thread(() -> {
            try {
                while (isRunning && !socket.isClosed()) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        if (messageHandler != null) {
                            // Run on JavaFX thread if interacting with UI?
                            // Usually safer to let controller handle Platform.runLater
                            messageHandler.accept(msg);
                        }
                    }
                }
            } catch (EOFException | java.net.SocketException e) {
                System.out.println("Connection closed: " + e.getMessage());
                handleDisconnect();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true); // Ensure thread dies if app closes
        listenerThread.start();
    }

    public synchronized void sendMessage(Message msg) {
        if (out != null) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                handleDisconnect();
            }
        }
    }

    public void setMessageHandler(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    public boolean isHost() {
        return isHost;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnect() {
        // Create a synthetic DISCONNECT message
        if (messageHandler != null && isRunning) {
            Message disconnectMsg = new Message(Message.MessageType.DISCONNECT, -1, "Connection Lost");
            messageHandler.accept(disconnectMsg);
        }
        isRunning = false;
    }
}
