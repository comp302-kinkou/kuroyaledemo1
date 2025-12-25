package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardLibrary;
import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.CardRarity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CardUpgradeView {

    private ClashRoyaleFX mainApp;
    private GameController controller;
    private GridPane cardGrid;
    private Label goldLabel;

    public CardUpgradeView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top: Title and Gold Display
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10, 0, 20, 0));

        Label titleLabel = new Label("Card Upgrade & Evolution");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        goldLabel = new Label("Gold: " + controller.getPlayerProfile().getTotalGold());
        goldLabel.setStyle(
                "-fx-font-size: 18px; -fx-text-fill: gold; -fx-font-weight: bold; -fx-effect: dropshadow(one-pass-box, black, 2, 0.0, 1, 1);");

        topBox.getChildren().addAll(titleLabel, goldLabel);
        root.setTop(topBox);

        // Center: Card Grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        cardGrid = new GridPane();
        cardGrid.setHgap(15);
        cardGrid.setVgap(15);
        cardGrid.setPadding(new Insets(10));
        cardGrid.setAlignment(Pos.CENTER);

        loadCards();

        scrollPane.setContent(cardGrid);
        root.setCenter(scrollPane);

        // Bottom: Back Button
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> mainApp.showMainMenu());
        btnBack.setStyle("-fx-font-size: 14px; -fx-padding: 10 20 10 20;");

        bottomBox.getChildren().add(btnBack);
        root.setBottom(bottomBox);

        return root;
    }

    private void loadCards() {
        cardGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        int colsPerRow = 7; // 7 cards per row for better layout

        for (Card card : CardLibrary.getAllCards()) {
            VBox cardBox = createCardBox(card);
            cardGrid.add(cardBox, col, row);

            col++;
            if (col >= colsPerRow) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createCardBox(Card card) {
        VBox cardBox = new VBox(5);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(10));
        cardBox.setMinWidth(120);
        cardBox.setMaxWidth(120);

        CardProgression progression = controller.getCardProgression(card);
        CardRarity rarity = CardLibrary.getCardRarity(card.getName());

        // Determine border color based on rarity
        String borderColor = getRarityBorderColor(rarity);
        cardBox.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 3px; -fx-border-radius: 5px; "
                + "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5px;");

        // Card Name
        Label nameLabel = new Label(card.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // Card Level with Stars
        String levelStars = getLevelStars(progression.getLevel());
        Label levelLabel = new Label("Level " + progression.getLevel() + " " + levelStars);
        levelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFD700;");

        // Rarity Badge
        Label rarityLabel = new Label(rarity.getDisplayName());
        rarityLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + borderColor + "; -fx-font-weight: bold;");

        // Upgrade Button
        Button upgradeBtn = new Button("Upgrade");
        upgradeBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5 10 5 10;");
        upgradeBtn.setDisable(!progression.canUpgrade());

        upgradeBtn.setOnAction(e -> showUpgradeDialog(card, progression, rarity));

        cardBox.getChildren().addAll(nameLabel, levelLabel, rarityLabel, upgradeBtn);

        return cardBox;
    }

    private String getRarityBorderColor(CardRarity rarity) {
        if (rarity == null)
            return "#808080"; // Gray default

        switch (rarity) {
            case COMMON:
                return "#C0C0C0"; // Gray/White
            case RARE:
                return "#4169E1"; // Blue
            case EPIC:
                return "#9370DB"; // Purple
            case LEGENDARY:
                return "#FF8C00"; // Orange/Gold
            default:
                return "#808080";
        }
    }

    private String getLevelStars(int level) {
        switch (level) {
            case 1:
                return "★";
            case 2:
                return "★★";
            case 3:
                return "★★★";
            default:
                return "";
        }
    }

    private void showUpgradeDialog(Card card, CardProgression progression, CardRarity rarity) {
        if (!progression.canUpgrade()) {
            showAlert("Cannot Upgrade", "This card is already at maximum level (Level 3).");
            return;
        }

        int currentLevel = progression.getLevel();
        int nextLevel = currentLevel + 1;
        int upgradeCost = progression.getUpgradeCost();
        int playerGold = controller.getPlayerProfile().getTotalGold();

        // Calculate current and next level stats
        double currentHP = card.getHealth() * progression.getStatMultiplier();
        double nextHP = card.getHealth() * getStatMultiplierForLevel(nextLevel);
        double currentDMG = card.getDamage() * progression.getStatMultiplier();
        double nextDMG = card.getDamage() * getStatMultiplierForLevel(nextLevel);

        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Upgrade Card");
        dialog.setHeaderText("Upgrade " + card.getName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(400);

        // Current Stats
        Label currentStatsLabel = new Label("Current Level " + currentLevel + " Stats:");
        currentStatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label currentHPLabel = new Label("HP: " + String.format("%.0f", currentHP));
        Label currentDMGLabel = new Label("DMG: " + String.format("%.0f", currentDMG));

        VBox currentStatsBox = new VBox(5, currentStatsLabel, currentHPLabel, currentDMGLabel);
        currentStatsBox.setPadding(new Insets(10));
        currentStatsBox.setStyle("-fx-background-color: rgba(200, 200, 200, 0.2); -fx-background-radius: 5px;");

        // Next Level Stats Preview
        Label nextStatsLabel = new Label("Next Level " + nextLevel + " Stats:");
        nextStatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
        Label nextHPLabel = new Label("HP: " + String.format("%.0f", nextHP) + " (+"
                + String.format("%.0f", nextHP - currentHP) + ")");
        nextHPLabel.setStyle("-fx-text-fill: #4CAF50;");
        Label nextDMGLabel = new Label("DMG: " + String.format("%.0f", nextDMG) + " (+"
                + String.format("%.0f", nextDMG - currentDMG) + ")");
        nextDMGLabel.setStyle("-fx-text-fill: #4CAF50;");

        VBox nextStatsBox = new VBox(5, nextStatsLabel, nextHPLabel, nextDMGLabel);
        nextStatsBox.setPadding(new Insets(10));
        nextStatsBox.setStyle("-fx-background-color: rgba(76, 175, 80, 0.2); -fx-background-radius: 5px;");

        // Upgrade Cost
        Label costLabel = new Label("Upgrade Cost: " + upgradeCost + " gold");
        costLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gold;");

        // Player Gold
        Label goldLabel = new Label("Your Gold: " + playerGold);
        goldLabel.setStyle("-fx-font-size: 14px;");

        // Warning if not enough gold
        if (playerGold < upgradeCost) {
            Label warningLabel = new Label("Insufficient gold! You need " + (upgradeCost - playerGold) + " more gold.");
            warningLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            content.getChildren().add(warningLabel);
        }

        content.getChildren().addAll(currentStatsBox, nextStatsBox, costLabel, goldLabel);

        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType upgradeButtonType = new ButtonType("Upgrade", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(upgradeButtonType, cancelButtonType);

        // Disable upgrade button if not enough gold
        Button upgradeButton = (Button) dialog.getDialogPane().lookupButton(upgradeButtonType);
        if (playerGold < upgradeCost) {
            upgradeButton.setDisable(true);
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == upgradeButtonType) {
                if (controller.upgradeCard(card)) {
                    showAlert("Success", card.getName() + " upgraded to Level " + nextLevel + "!");
                    updateGoldDisplay();
                    loadCards(); // Refresh the card grid
                } else {
                    showAlert("Error", "Failed to upgrade card. Please check your gold balance.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private double getStatMultiplierForLevel(int level) {
        switch (level) {
            case 1:
                return 1.0;
            case 2:
                return 1.1;
            case 3:
                return 1.2;
            default:
                return 1.0;
        }
    }

    private void updateGoldDisplay() {
        goldLabel.setText("Gold: " + controller.getPlayerProfile().getTotalGold());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}