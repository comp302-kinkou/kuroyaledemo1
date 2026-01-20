package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.challenge.Challenge;
import com.example.kuroyale.model.challenge.ChallengeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChallengeSelectionView {
    private final ClashRoyaleFX mainApp;
    private final VBox challengesLayout;

    public ChallengeSelectionView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.challengesLayout = new VBox(20);
        challengesLayout.setPadding(new Insets(20));
        challengesLayout.setAlignment(Pos.TOP_CENTER);
        challengesLayout.setStyle("-fx-background-color: #2c3e50;");
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();

        // Header
        // Header
        Label headerLabel = new Label("CHALLENGE MODE");
        headerLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

        Label goldLabel = new Label("Gold: " + ChallengeManager.getInstance().getTotalGold());
        goldLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: gold; -fx-padding: 0 0 0 50;");

        HBox headerBox = new HBox(headerLabel, goldLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle(
                "-fx-background-color: #34495e; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        // Star Criteria Legend
        Label criteriaLabel = new Label("⭐ Win  |  ⭐⭐ Win < 2 min  |  ⭐⭐⭐ Perfect Win or Win < 90s");
        criteriaLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px; -fx-padding: 5 0 0 0;");

        VBox topContainer = new VBox(10, headerBox, criteriaLabel);
        topContainer.setAlignment(Pos.CENTER);
        topContainer.setStyle("-fx-background-color: #2c3e50;");

        root.setTop(topContainer);

        // Content
        refreshChallenges();
        ScrollPane scrollPane = new ScrollPane(challengesLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2c3e50; -fx-border-color: transparent;");
        root.setCenter(scrollPane);

        // Footer / Back
        Button backButton = new Button("Back to Menu");
        backButton.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        backButton.setMinWidth(200);
        backButton.setOnAction(e -> {
            mainApp.showMainMenu();
        });

        HBox footerBox = new HBox(backButton);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20));
        footerBox.setStyle("-fx-background-color: #34495e;");
        root.setBottom(footerBox);

        return new Scene(root, 1200, 800);
    }

    private void refreshChallenges() {
        challengesLayout.getChildren().clear();
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> challenges = manager.getAllChallenges();

        for (Challenge challenge : challenges) {
            challengesLayout.getChildren().add(createChallengeCard(challenge, manager));
        }
    }

    private HBox createChallengeCard(Challenge challenge, ChallengeManager manager) {
        HBox card = new HBox(20);
        boolean unlocked = manager.isUnlocked(challenge.getName());
        boolean completed = manager.isCompleted(challenge.getName());
        int stars = manager.getStars(challenge.getName());

        card.setStyle(
                "-fx-background-color: " + (unlocked ? "#34495e" : "#2c3440") + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(800);

        // Icon/Status section
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setMinWidth(100);

        Label statusIcon = new Label(unlocked ? (completed ? "✅" : "⚔️") : "🔒");
        statusIcon.setStyle("-fx-font-size: 40px;");

        Label starsLabel = new Label("⭐".repeat(stars));
        starsLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 20px;");

        statusBox.getChildren().addAll(statusIcon);
        if (completed)
            statusBox.getChildren().add(starsLabel);

        // Info Section
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label((unlocked ? "" : "[LOCKED] ") + challenge.getName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: "
                + (unlocked ? "#ecf0f1" : "#7f8c8d") + ";");

        Label descLabel = new Label(challenge.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

        Label rewardLabel = new Label("Reward: " + challenge.getReward() + " Gold");
        rewardLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(nameLabel, descLabel, rewardLabel);

        // Action Section
        VBox actionBox = new VBox();
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        if (unlocked) {
            Button startButton = new Button("Start");
            startButton.setStyle(
                    "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 5;");
            startButton.setMinWidth(100);
            startButton.setOnAction(e -> startChallenge(challenge));

            Button testButton = new Button("Test");
            testButton.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 5;");
            testButton.setMinWidth(100);
            testButton.setOnAction(e -> startChallengeTest(challenge));

            VBox buttonsBox = new VBox(10, startButton, testButton);
            buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            actionBox.getChildren().add(buttonsBox);
        }

        card.getChildren().addAll(statusBox, infoBox, actionBox);
        return card;
    }

    private void startChallenge(Challenge challenge) {
        GameController controller = GameController.getInstance();

        if (!controller.isGameReady()) {
            showAlert("Not Ready", "Please build a deck and design the arena before starting a challenge!");
            return;
        }

        // Validate Deck
        String error = challenge.validateDeck(controller.getDeck());
        if (error != null) {
            showAlert("Invalid Deck", "Your deck does not meet the challenge requirements:\n\n" + error);
            return;
        }

        // Start Game
        controller.startChallenge(challenge, false);
        mainApp.showGameView();
    }

    private void startChallengeTest(Challenge challenge) {
        GameController controller = GameController.getInstance();

        if (!controller.isGameReady()) {
            showAlert("Not Ready", "Please build a deck and design the arena before starting a challenge!");
            return;
        }

        // Validate Deck
        String error = challenge.validateDeck(controller.getDeck());
        if (error != null) {
            showAlert("Invalid Deck", "Your deck does not meet the challenge requirements:\n\n" + error);
            return;
        }

        // Start Game in Test Mode
        controller.startChallenge(challenge, true);
        mainApp.showGameView();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
