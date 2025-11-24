import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ClashRoyaleFX extends Application {

    private Stage primaryStage;
    private Deck deck;
    private Arena arena;
    private List<Card> allCards;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.deck = new Deck();
        this.allCards = new ArrayList<>();
        initializeCards();

        primaryStage.setTitle("Clash Royale Demo");
        showMainMenu();
        primaryStage.show();
    }

    private void initializeCards() {
        allCards.add(new Card("Knight", 3));
        allCards.add(new Card("Archer", 2));
        allCards.add(new Card("Fireball", 4));
        allCards.add(new Card("Giant", 5));
    }

    private void showMainMenu() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Clash Royale Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button btnDeckBuilder = new Button("Build Deck");
        btnDeckBuilder.setOnAction(e -> showDeckBuilder());
        btnDeckBuilder.setMaxWidth(200);

        Button btnArenaDesigner = new Button("Design Arena");
        btnArenaDesigner.setOnAction(e -> showArenaDesigner());
        btnArenaDesigner.setMaxWidth(200);

        Button btnStartGame = new Button("Start Game");
        btnStartGame.setOnAction(e -> showGameView());
        btnStartGame.setMaxWidth(200);

        Button btnExit = new Button("Exit");
        btnExit.setOnAction(e -> primaryStage.close());
        btnExit.setMaxWidth(200);

        root.getChildren().addAll(titleLabel, btnDeckBuilder, btnArenaDesigner, btnStartGame, btnExit);
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showDeckBuilder() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Deck Builder");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Available Cards
        ListView<String> cardsListView = new ListView<>();
        for (Card card : allCards) {
            cardsListView.getItems().add(card.toString());
        }
        cardsListView.setMaxHeight(150);

        // Current Deck Display
        TextArea deckDisplay = new TextArea();
        deckDisplay.setEditable(false);
        deckDisplay.setMaxHeight(100);
        updateDeckDisplay(deckDisplay);

        Button btnAdd = new Button("Add Selected Card");
        btnAdd.setOnAction(e -> {
            int selectedIndex = cardsListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                boolean added = deck.addCard(allCards.get(selectedIndex));
                if (added) {
                    updateDeckDisplay(deckDisplay);
                } else {
                    showAlert("Deck Full", "You can only have 4 cards in your deck.");
                }
            } else {
                showAlert("No Selection", "Please select a card to add.");
            }
        });

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> showMainMenu());

        root.getChildren().addAll(titleLabel, new Label("Available Cards:"), cardsListView, btnAdd,
                new Label("Your Deck:"), deckDisplay, btnBack);
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
    }

    private void updateDeckDisplay(TextArea display) {
        display.setText(deck.toString());
    }

    private void showArenaDesigner() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Arena Designer");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox bridgeInputBox = new HBox(10);
        bridgeInputBox.setAlignment(Pos.CENTER);
        Label lblBridges = new Label("Number of Bridges (1-3):");
        TextField txtBridges = new TextField("2");
        txtBridges.setMaxWidth(50);
        bridgeInputBox.getChildren().addAll(lblBridges, txtBridges);

        TextArea arenaDisplay = new TextArea();
        arenaDisplay.setEditable(false);
        if (arena != null) {
            arenaDisplay.setText(arena.toString());
        } else {
            arenaDisplay.setText("Arena not created yet.");
        }

        Button btnCreate = new Button("Create Arena");
        btnCreate.setOnAction(e -> {
            try {
                int count = Integer.parseInt(txtBridges.getText());
                if (count < 1 || count > 3) {
                    showAlert("Invalid Input", "Please enter a number between 1 and 3.");
                    return;
                }
                arena = new Arena();
                for (int i = 1; i <= count; i++) {
                    arena.addBridge("Bridge " + i);
                }
                arenaDisplay.setText(arena.toString());
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> showMainMenu());

        root.getChildren().addAll(titleLabel, bridgeInputBox, btnCreate, arenaDisplay, btnBack);
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showGameView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Game Arena");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        if (deck.getCards().isEmpty()) {
            root.getChildren().add(new Label("Error: Deck is empty! Go back and build a deck."));
        } else if (arena == null) {
            root.getChildren().add(new Label("Error: Arena not designed! Go back and design an arena."));
        } else {
            TextArea gameInfo = new TextArea();
            gameInfo.setEditable(false);
            gameInfo.setText("Game Started!\n\n" + deck.toString() + "\n" + arena.toString());
            root.getChildren().add(gameInfo);
            root.getChildren().add(new Label("(Gameplay visualization would go here)"));
        }

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> showMainMenu());

        root.getChildren().add(0, titleLabel); // Add title at the top
        root.getChildren().add(btnBack);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
