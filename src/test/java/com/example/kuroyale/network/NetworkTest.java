package com.example.kuroyale.network;

import com.example.kuroyale.protocol.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetworkTest {

    private NetworkManager serverManager; // We can't use Singleton for both in one process easily without Refactoring.
    // Actually, NetworkManager is a Singleton! This makes testing Host AND Client
    // in the same JVM problematic
    // unless we create a separate "TestNetworkManager" or refactor NetworkManager
    // to not be a strict singleton for testing,
    // or we just test the socket logic and Message serialization manually here.

    // To test properly, I will simulate the other end using raw Sockets, because
    // standard NetworkManager is a Singleton.
    // I will test "NetworkManager as Client" vs "Test Server" and "NetworkManager
    // as Host" vs "Test Client".

    @AfterEach
    public void tearDown() {
        NetworkManager.getInstance().disconnect();
    }

    @Test
    public void testMessageSerialization() throws IOException, ClassNotFoundException {
        // Test simple serialization logic (Validation of Message class)
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

        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertEquals(original.getData(), deserialized.getData());
    }

    @Test
    public void testNetworkManagerConnectsToHost() throws Exception {
        int testPort = 9999;
        BlockingQueue<Message> serverReceivedMessages = new LinkedBlockingQueue<>();

        // 1. Start a simple dummy server thread
        Thread serverThread = new Thread(() -> {
            try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(testPort)) {
                java.net.Socket clientSocket = serverSocket.accept();
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(clientSocket.getInputStream());
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    serverReceivedMessages.offer((Message) obj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Wait for server to be ready
        Thread.sleep(500);

        // 2. Use NetworkManager to connect
        NetworkManager.getInstance().connect("127.0.0.1", testPort);

        // 3. Send a message
        Message testMsg = new Message(Message.MessageType.JOIN_REQUEST, 2, "TestPlayer");
        NetworkManager.getInstance().sendMessage(testMsg);

        // 4. Verify server received it
        Message received = serverReceivedMessages.poll(5, TimeUnit.SECONDS);
        assertNotNull(received, "Server should receive the message");
        assertEquals(Message.MessageType.JOIN_REQUEST, received.getType());
        assertEquals("TestPlayer", received.getData());
    }
}
