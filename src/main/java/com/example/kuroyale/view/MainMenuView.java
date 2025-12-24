package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuView {

    private ClashRoyaleFX mainApp;

    public MainMenuView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Clash Royale Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label goldLabel = new Label(
                "Gold: " + com.example.kuroyale.model.challenge.ChallengeManager.getInstance().getTotalGold());
        goldLabel.setStyle(
                "-fx-font-size: 16px; -fx-text-fill: gold; -fx-font-weight: bold; -fx-effect: dropshadow(one-pass-box, black, 2, 0.0, 1, 1);");

        Button btnDeckBuilder = new Button("Build Deck");
        btnDeckBuilder.setOnAction(e -> mainApp.showDeckBuilder());
        btnDeckBuilder.setMaxWidth(200);

        Button btnArenaDesigner = new Button("Design Arena");
        btnArenaDesigner.setOnAction(e -> mainApp.showArenaDesigner());
        btnArenaDesigner.setMaxWidth(200);

        Button btnStartGame = new Button("Start Game");
        btnStartGame.setOnAction(e -> {
            if (GameController.getInstance().isGameReady()) {
                mainApp.showGameView();
            } else {
                showAlert("Not Ready", "Please build a deck and design the arena before starting!");
            }
        });
        btnStartGame.setMaxWidth(200);

        Button btnChallengeMode = new Button("Challenge Mode");
        btnChallengeMode.setOnAction(e -> mainApp.showChallengeSelection());
        btnChallengeMode.setMaxWidth(200);

        Button btnExit = new Button("Exit");
        btnExit.setOnAction(e -> System.exit(0));
        btnExit.setMaxWidth(200);

        root.getChildren().addAll(titleLabel, goldLabel, btnDeckBuilder, btnArenaDesigner, btnStartGame,
                btnChallengeMode,
                btnExit);
        return root;
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
