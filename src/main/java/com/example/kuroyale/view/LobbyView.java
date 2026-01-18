package com.example.kuroyale.view;

import com.example.kuroyale.network.NetworkManager;
import com.example.kuroyale.protocol.Message;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class LobbyView {

    private ClashRoyaleFX app;
    private NetworkManager networkManager;
    private Label statusLabel;
    private Button startButton;

    public LobbyView(ClashRoyaleFX app) {
        this.app = app;
        this.networkManager = NetworkManager.getInstance();
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("Multiplayer Lobby");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Host Section
        VBox hostBox = new VBox(10);
        hostBox.setStyle(
                "-fx-border-color: #ecf0f1; -fx-border-width: 1px; -fx-padding: 15px; -fx-background-color: #34495e;");
        hostBox.setAlignment(Pos.CENTER);

        Label hostTitle = new Label("Host Game");
        hostTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TextField hostPortField = new TextField("8080");
        hostPortField.setPromptText("Port");
        hostPortField.setMaxWidth(100);

        Button hostButton = new Button("Start Host");
        hostButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        hostBox.getChildren().addAll(hostTitle, new Label("Port:"), hostPortField, hostButton);

        // Join Section
        VBox joinBox = new VBox(10);
        joinBox.setStyle(
                "-fx-border-color: #ecf0f1; -fx-border-width: 1px; -fx-padding: 15px; -fx-background-color: #34495e;");
        joinBox.setAlignment(Pos.CENTER);

        Label joinTitle = new Label("Join Game");
        joinTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        HBox connectionBox = new HBox(10);
        connectionBox.setAlignment(Pos.CENTER);

        TextField ipField = new TextField("127.0.0.1");
        ipField.setPromptText("IP Address");

        TextField joinPortField = new TextField("8080");
        joinPortField.setPromptText("Port");
        joinPortField.setPrefWidth(80);

        connectionBox.getChildren().addAll(ipField, joinPortField);

        Button joinButton = new Button("Connect");
        joinButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        joinBox.getChildren().addAll(joinTitle, connectionBox, joinButton);

        // Status Area
        statusLabel = new Label("Status: Not Connected");
        statusLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 14px;");

        startButton = new Button("Start Match");
        startButton.setDisable(true); // Enabled when connected
        startButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 16px;");
        startButton.setOnAction(e -> startGame());

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> app.showMainMenu());

        root.getChildren().addAll(titleLabel, hostBox, joinBox, statusLabel, startButton, backButton);

        // Actions
        hostButton.setOnAction(e -> {
            try {
                int port = Integer.parseInt(hostPortField.getText());
                networkManager.startHost(port);
                statusLabel.setText("Status: Hosting on port " + port + "... Waiting for player.");
                setupNetworkHandler();
                hostButton.setDisable(true);
                joinButton.setDisable(true);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Invalid Port");
            } catch (IOException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        joinButton.setOnAction(e -> {
            try {
                String ip = ipField.getText();
                int port = Integer.parseInt(joinPortField.getText());
                statusLabel.setText("Status: Connecting...");
                networkManager.connect(ip, port);
                statusLabel.setText("Status: Connected to " + ip + ":" + port);
                setupNetworkHandler();
                hostButton.setDisable(true);
                joinButton.setDisable(true);
                startButton.setDisable(false); // Enable for joiner too? Or wait for host?
                // Usually Host starts, but for simplicity allow both to click for now
                // or better: Wait for "START" message if client.
                if (!networkManager.isHost()) {
                    startButton.setText("Waiting for Host to Start...");
                    startButton.setDisable(true);
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Invalid Port");
            } catch (IOException ex) {
                statusLabel.setText("Error: Connection Failed - " + ex.getMessage());
            }
        });

        return root;
    }

    private void setupNetworkHandler() {
        networkManager.setMessageHandler(msg -> {
            Platform.runLater(() -> {
                handleMessage(msg);
            });
        });

        // If Host, we might want to enable start button once a client connects.
        // Currently NetworkManager doesn't explicitly notify "client connected" event
        // separate from messages.
        // We can add a simple ping or just assume if we receive anything or if the
        // socket is live.
        // For now, let's enable Start button for Host immediately (waiting for
        // connection happens in background).
        // Actually, we need to know when client connects to enable start.
        // NetworkManager implementation printed "Client connected", but didn't
        // callback.
        // Let's improve NetworkManager to send a local "Connected" event or just rely
        // on manual coordination for now.
        // Improvement: Host sends "Ping" after accept, Client replies.

        if (networkManager.isHost()) {
            // Polling or waiting for first message?
            // Let's Just enable Start button for Host. If they click it before client
            // connects, the message send will fail/queue.
            statusLabel.setText("Status: Hosting... (Wait for client to join)");

            // In a real lobby, we'd wait for a JOIN message.
            // Let's assume the Client sends a JOIN_REQUEST on connect.
        } else {
            // Client just connected. Send Join Request.
            networkManager.sendMessage(new Message(Message.MessageType.JOIN_REQUEST, 2, "Player 2"));
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case JOIN_REQUEST:
                statusLabel.setText("Status: Client Connected!");
                if (networkManager.isHost()) {
                    startButton.setDisable(false);
                    // Reply with Accept
                    networkManager.sendMessage(new Message(Message.MessageType.JOIN_ACCEPT, 1, "Player 1"));
                }
                break;
            case JOIN_ACCEPT:
                statusLabel.setText("Status: Joined! Waiting for host to start.");
                break;
            case START_MATCH:
                app.showGameView();
                break;
            case DISCONNECT:
                statusLabel.setText("Status: Opponent Disconnected");
                startButton.setDisable(true);
                break;
            default:
                break;
        }
    }

    private void startGame() {
        // Send START_MATCH to opponent
        networkManager.sendMessage(new Message(Message.MessageType.START_MATCH, 1, null));
        app.showGameView();
    }
}
