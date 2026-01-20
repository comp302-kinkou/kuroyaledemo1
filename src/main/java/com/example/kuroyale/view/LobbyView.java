package com.example.kuroyale.view;

import com.example.kuroyale.network.NetworkManager;
import com.example.kuroyale.protocol.Message;
import com.example.kuroyale.controller.GameController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Random;

public class LobbyView {

    private ClashRoyaleFX app;
    private NetworkManager networkManager;
    private GameController controller;
    private Label statusLabel;
    private Button startButton;
    private Button readyButton;
    private boolean isLocalReady = false;
    private boolean isRemoteReady = false;

    // Persist host/join state across view reloads (if backing out to deck builder)
    // This could be moved to a robust storage, but static here is a simple hack for
    // now
    // or rely on NetworkManager.isConnected()

    public LobbyView(ClashRoyaleFX app) {
        this.app = app;
        this.networkManager = NetworkManager.getInstance();
        this.controller = GameController.getInstance();
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("Multiplayer Lobby");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Preparation Section
        HBox prepBox = new HBox(15);
        prepBox.setAlignment(Pos.CENTER);

        Button btnDeck = new Button("Build Deck");
        btnDeck.setOnAction(e -> app.showDeckBuilder());

        Button btnArena = new Button("Design Arena");
        btnArena.setOnAction(e -> app.showArenaDesigner(true));

        prepBox.getChildren().addAll(btnDeck, btnArena);

        // Connection Panels
        VBox connectionPanel = new VBox(20);
        connectionPanel.setAlignment(Pos.CENTER);

        if (networkManager.isConnected() || networkManager.isHost()) {
            // Already connected/hosting - show active lobby state
            Label connectedLabel = new Label("Session Active");
            connectedLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 16px;");
            connectionPanel.getChildren().add(connectedLabel);

            // Re-hook handler because view was recreated
            setupNetworkHandler();
        } else {
            // Host Section
            VBox hostBox = new VBox(10);
            hostBox.setStyle(
                    "-fx-border-color: #ecf0f1; -fx-border-width: 1px; -fx-padding: 15px; -fx-background-color: #34495e;");
            hostBox.setAlignment(Pos.CENTER);
            Label hostTitle = new Label("Host Game");
            hostTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            TextField hostPortField = new TextField("8080");
            hostPortField.setMaxWidth(100);
            Button hostButton = new Button("Start Host");
            hostButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

            hostButton.setOnAction(e -> {
                try {
                    int port = Integer.parseInt(hostPortField.getText());
                    networkManager.startHost(port);
                    statusLabel.setText("Hosting on port " + port + "... Waiting.");
                    setupNetworkHandler();

                    // Host generates seed immediately
                    long seed = new Random().nextLong();
                    controller.setMultiplayerSeed(seed);
                    // We will send this seed to client when they join

                    refreshUIState(connectionPanel, root);
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            });
            hostBox.getChildren().addAll(hostTitle, new Label("Port:"), hostPortField, hostButton);

            // Join Section
            VBox joinBox = new VBox(10);
            joinBox.setStyle(
                    "-fx-border-color: #ecf0f1; -fx-border-width: 1px; -fx-padding: 15px; -fx-background-color: #34495e;");
            joinBox.setAlignment(Pos.CENTER);
            Label joinTitle = new Label("Join Game");
            joinTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            TextField ipField = new TextField("127.0.0.1");
            TextField joinPortField = new TextField("8080");
            Button joinButton = new Button("Connect");
            joinButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

            joinButton.setOnAction(e -> {
                try {
                    String ip = ipField.getText();
                    int port = Integer.parseInt(joinPortField.getText());
                    networkManager.connect(ip, port);
                    statusLabel.setText("Connected to " + ip + ":" + port);
                    setupNetworkHandler();
                    refreshUIState(connectionPanel, root);
                } catch (Exception ex) {
                    statusLabel.setText("Connection Failed: " + ex.getMessage());
                }
            });
            joinBox.getChildren().addAll(joinTitle, new Label("IP:"), ipField, new Label("Port:"), joinPortField,
                    joinButton);

            connectionPanel.getChildren().addAll(hostBox, joinBox);
        }

        // Status & Controls
        statusLabel = new Label("Status: Not Connected");
        statusLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 14px;");

        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);

        readyButton = new Button("Ready");
        readyButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px;");
        readyButton.setDisable(true); // Enable only when connected
        readyButton.setOnAction(e -> toggleReady());

        startButton = new Button("Start Match");
        startButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 16px;");
        startButton.setDisable(true);
        startButton.setOnAction(e -> startGame());

        controlBox.getChildren().addAll(readyButton, startButton);

        Button backButton = new Button("Main Menu");
        backButton.setOnAction(e -> app.showMainMenu());

        root.getChildren().addAll(titleLabel, prepBox, connectionPanel, statusLabel, controlBox, backButton);

        // Initial State check
        if (networkManager.isConnected() || networkManager.isHost()) {
            readyButton.setDisable(false);
            statusLabel.setText("Connected. Please prepare deck/arena and click Ready.");
            // If we are host, we might have seed already. If client, waiting for seed.
        }

        return root;
    }

    private void refreshUIState(VBox connectionPanel, VBox root) {
        connectionPanel.getChildren().clear();
        Label connected = new Label("Connected");
        connected.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 16px;");
        connectionPanel.getChildren().add(connected);

        readyButton.setDisable(false);
    }

    private void toggleReady() {
        if (!controller.isDeckReady()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not Ready");
            alert.setContentText("You must build a valid Deck before playing!");
            alert.showAndWait();
            return;
        }

        if (!controller.isArenaReady()) {
            if (controller.getArena().getTowers().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Not Ready");
                alert.setContentText("You must design your Arena (place Towers)!");
                alert.showAndWait();
                return;
            }
        }

        // Serialize and Send Tower Layout
        StringBuilder sb = new StringBuilder();
        for (com.example.kuroyale.model.Tower t : controller.getArena().getTowers()) {
            if (t.isPlayer()) { // Only send my towers
                sb.append(t.getType()).append(",").append(t.getX()).append(",").append(t.getY()).append(";");
            }
        }
        networkManager.sendMessage(
                new Message(Message.MessageType.TOWER_LAYOUT, networkManager.getLocalPlayerId(), sb.toString()));

        isLocalReady = true;
        readyButton.setDisable(true); // Lock in
        readyButton.setText("Ready!");
        readyButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        networkManager
                .sendMessage(new Message(Message.MessageType.PLAYER_READY, networkManager.getLocalPlayerId(), null));
        checkStartCondition();
    }

    private void checkStartCondition() {
        if (isLocalReady && isRemoteReady) {
            statusLabel.setText("Both Players Ready! Waiting for Host...");
            if (networkManager.isHost()) {
                startButton.setDisable(false);
            }
        } else {
            statusLabel.setText(
                    "Waiting for players to be Ready... (Local: " + isLocalReady + ", Remote: " + isRemoteReady + ")");
        }
    }

    private void setupNetworkHandler() {
        networkManager.setMessageHandler(msg -> {
            Platform.runLater(() -> handleMessage(msg));
        });

        // If client, send join request immediately
        if (!networkManager.isHost() && networkManager.isConnected()) {
            // networkManager.sendMessage(new Message(Message.MessageType.JOIN_REQUEST, 2,
            // "Player 2"));
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case JOIN_REQUEST:
                statusLabel.setText("Client Connected!");
                if (networkManager.isHost()) {
                    networkManager.sendMessage(new Message(Message.MessageType.JOIN_ACCEPT, 1, "Player 1"));
                    // Send Seed
                    long seed = controller.getMultiplayerSeed();
                    networkManager.sendMessage(new Message(Message.MessageType.BRIDGE_SEED, 1, String.valueOf(seed)));
                }
                break;
            case JOIN_ACCEPT:
                statusLabel.setText("Joined! Waiting for seed...");
                break;
            case BRIDGE_SEED:
                try {
                    long seed = Long.parseLong(msg.getData().toString());
                    controller.setMultiplayerSeed(seed);
                    statusLabel.setText("Synced with Host.");
                } catch (Exception e) {
                    System.err.println("Failed to parse bridge seed: " + msg.getData());
                }
                break;
            case TOWER_LAYOUT:
                controller.setOpponentTowers((String) msg.getData());
                System.out.println("Received Opponent Tower Layout.");
                break;
            case PLAYER_READY:
                isRemoteReady = true;
                checkStartCondition();
                break;
            case START_MATCH:
                controller.startMultiplayerGame();
                app.showGameView();
                break;
            case DISCONNECT:
                statusLabel.setText("Opponent Disconnected");
                isRemoteReady = false;
                startButton.setDisable(true);
                break;
            default:
                break;
        }
    }

    private void startGame() {
        networkManager.sendMessage(new Message(Message.MessageType.START_MATCH, 1, null));
        controller.startMultiplayerGame();
        app.showGameView();
    }
}
