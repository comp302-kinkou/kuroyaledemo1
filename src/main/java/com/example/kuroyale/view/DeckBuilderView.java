package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardLibrary;
import com.example.kuroyale.model.Deck;
import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.CardRarity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class DeckBuilderView {

    private ClashRoyaleFX mainApp;
    private GameController controller;
    private ObservableList<Card> availableCards;
    private ObservableList<Card> currentDeckCards;
    private Label cardCountLabel;

    public DeckBuilderView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
        this.availableCards = FXCollections.observableArrayList();
        this.currentDeckCards = FXCollections.observableArrayList();
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top: Title
        Label titleLabel = new Label("Deck Builder");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox topBox = new HBox(titleLabel);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // Center: Two lists
        VBox leftBox = new VBox(10);
        Label lblAvailable = new Label("Available Cards");
        ListView<Card> listAvailable = new ListView<>(availableCards);
        listAvailable.setCellFactory(new CardCellFactory());
        leftBox.getChildren().addAll(lblAvailable, listAvailable);
        leftBox.setAlignment(Pos.CENTER);

        VBox rightBox = new VBox(10);
        Label lblDeck = new Label("Your Deck");

        // Card count label
        cardCountLabel = new Label("0/8 cards selected");
        cardCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red; -fx-font-weight: bold;");

        ListView<Card> listDeck = new ListView<>(currentDeckCards);
        listDeck.setCellFactory(new CardCellFactory());
        rightBox.getChildren().addAll(lblDeck, cardCountLabel, listDeck);
        rightBox.setAlignment(Pos.CENTER);

        // Buttons in the middle
        VBox centerButtons = new VBox(20);
        centerButtons.setAlignment(Pos.CENTER);
        centerButtons.setPadding(new Insets(0, 10, 0, 10));

        Button btnAdd = new Button("Add >");
        btnAdd.setOnAction(e -> {
            Card selected = listAvailable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (currentDeckCards.size() < 8) {
                    currentDeckCards.add(selected);
                    availableCards.remove(selected);
                    updateCardCount();
                } else {
                    showAlert("Deck Full", "You can only have 8 cards in your deck.");
                }
            }
        });

        Button btnRemove = new Button("< Remove");
        btnRemove.setOnAction(e -> {
            Card selected = listDeck.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentDeckCards.remove(selected);
                availableCards.add(selected);
                updateCardCount();
            }
        });

        centerButtons.getChildren().addAll(btnAdd, btnRemove);

        HBox centerContent = new HBox(10, leftBox, centerButtons, rightBox);
        centerContent.setAlignment(Pos.CENTER);

        // Allow lists to grow and have min width
        javafx.scene.layout.HBox.setHgrow(leftBox, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.HBox.setHgrow(rightBox, javafx.scene.layout.Priority.ALWAYS);
        leftBox.setMinWidth(250);
        rightBox.setMinWidth(250);

        root.setCenter(centerContent);

        // Bottom: Save and Back
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        Button btnSave = new Button("Save Deck");
        btnSave.setStyle("-fx-font-weight: bold; -fx-base: #90ee90;");
        btnSave.setOnAction(e -> saveDeck());

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> mainApp.showMainMenu());

        bottomBox.getChildren().addAll(btnSave, btnBack);
        root.setBottom(bottomBox);

        // Initialize Data
        loadData();

        return root;
    }

    private void loadData() {
        // Start with empty deck
        currentDeckCards.clear();

        // Load all available cards
        availableCards.setAll(CardLibrary.getAllCards());

        updateCardCount();
    }

    private void updateCardCount() {
        int count = currentDeckCards.size();
        cardCountLabel.setText(count + "/8 cards selected");

        // Update color based on count
        if (count == 8) {
            cardCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green; -fx-font-weight: bold;");
        } else if (count > 0) {
            cardCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            cardCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void saveDeck() {
        if (currentDeckCards.size() != 8) {
            showAlert("Invalid Deck", "Deck must have exactly 8 cards.");
            return;
        }

        Deck newDeck = new Deck();
        for (Card c : currentDeckCards) {
            newDeck.addCard(c);
        }
        controller.setDeck(newDeck);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Deck saved successfully!");
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Custom Cell Factory to display Card info nicely with rarity and level
    private class CardCellFactory implements Callback<ListView<Card>, ListCell<Card>> {
        @Override
        public ListCell<Card> call(ListView<Card> param) {
            return new ListCell<Card>() {
                @Override
                protected void updateItem(Card item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        CardProgression progression = controller.getCardProgression(item);
                        CardRarity rarity = CardLibrary.getCardRarity(item.getName());
                        int level = progression != null ? progression.getLevel() : 1;
                        String levelStars = getLevelStars(level);

                        String text = item.getName() + " (" + item.getElixirCost() + ") - " + item.getType();
                        if (CardLibrary.isSwarmCard(item.getName())) {
                            text += " (Swarm)";
                        }
                        text += " | Level " + level + " " + levelStars;
                        if (rarity != null) {
                            text += " [" + rarity.getDisplayName() + "]";
                        }
                        setText(text);

                        // Apply rarity border color
                        String rarityColor = getRarityBorderColor(rarity);
                        setStyle("-fx-border-color: " + rarityColor + "; -fx-border-width: 2px; -fx-padding: 5px;");
                    }
                }
            };
        }
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
}