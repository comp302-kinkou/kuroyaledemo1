package com.example.kuroyale.network;

import com.example.kuroyale.protocol.Message;
import java.io.IOException;

public class ManualNetworkTest {

    public static void main(String[] args) {
        System.out.println("Starting Manual Network Verification...");

        testMessageSerialization();
        testHostClientConnection();
    }

    private static void testMessageSerialization() {
        System.out.println("1. Testing Message Serialization...");
        try {
            Message original = new Message(Message.MessageType.CARD_PLAYED, 1, "Knight,5.0,3.0");

            // Serialize
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bos);
            out.writeObject(original);
            out.close();

            // Deserialize
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(bis);
            Message deserialized = (Message) in.readObject();

            boolean match = original.getType() == deserialized.getType() &&
                    original.getPlayerId() == deserialized.getPlayerId() &&
                    original.getData().equals(deserialized.getData());

            if (match) {
                System.out.println("   [PASS] Serialization successful.");
            } else {
                System.out.println("   [FAIL] Serialization mismatch.");
            }
        } catch (Exception e) {
            System.out.println("   [FAIL] Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testHostClientConnection() {
        System.out.println("2. Testing Host-Client Connection...");
        int port = 9999;

        // Start Server Mock
        new Thread(() -> {
            try (java.net.ServerSocket server = new java.net.ServerSocket(port)) {
                java.net.Socket socket = server.accept();
                // Vital: Create ObjectOutputStream FIRST and flush to send the serialization
                // header.
                // If both sides create ObjectInputStream first, they both block waiting for the
                // header -> Deadlock.
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
                out.flush();
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    Message m = (Message) obj;
                    System.out.println("   [SERVER] Received: " + m.getType() + " " + m.getData());
                    if ("TestMsg".equals(m.getData())) {
                        System.out.println("   [PASS] Connection and Data verified.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        try {
            Thread.sleep(1000); // Wait for server to bind
            NetworkManager.getInstance().connect("127.0.0.1", port);
            NetworkManager.getInstance().sendMessage(new Message(Message.MessageType.PING, 2, "TestMsg"));

            // Give time for transmission
            Thread.sleep(1000);
            NetworkManager.getInstance().disconnect();
        } catch (Exception e) {
            System.out.println("   [FAIL] Client Exception: " + e.getMessage());
        }
    }
}
