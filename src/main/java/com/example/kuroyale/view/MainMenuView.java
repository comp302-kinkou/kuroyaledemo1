package com.example.kuroyale.view;

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

        Button btnDeckBuilder = new Button("Build Deck");
        btnDeckBuilder.setOnAction(e -> mainApp.showDeckBuilder());
        btnDeckBuilder.setMaxWidth(200);

        Button btnArenaDesigner = new Button("Design Arena");
        btnArenaDesigner.setOnAction(e -> mainApp.showArenaDesigner());
        btnArenaDesigner.setMaxWidth(200);

        Button btnStartGame = new Button("Start Game");
        btnStartGame.setOnAction(e -> mainApp.showGameView());
        btnStartGame.setMaxWidth(200);

        Button btnExit = new Button("Exit");
        btnExit.setOnAction(e -> System.exit(0));
        btnExit.setMaxWidth(200);

        root.getChildren().addAll(titleLabel, btnDeckBuilder, btnArenaDesigner, btnStartGame, btnExit);
        return root;
    }
}
