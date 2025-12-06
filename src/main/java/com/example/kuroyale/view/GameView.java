package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.controller.GameLoop;
import com.example.kuroyale.model.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.List;

public class GameView {

    private ClashRoyaleFX mainApp;
    private GameController controller;
    private Canvas canvas;
    private GraphicsContext gc;
    private GameLoop gameLoop;
    private AnimationTimer animationTimer;

    private Card selectedCard = null;
    private int selectedHandIndex = -1;

    // UI Elements
    private Label elixirLabel;
    private ProgressBar elixirBar;
    private HBox handBox;
    private Label messageLabel;
    private Label timerLabel;
    private Button btnPause;

    private static final double TILE_SIZE = 20.0; // Scale factor

    public GameView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
    }

    public Parent getView() {
        BorderPane root = new BorderPane();

        // --- Top: Status / Exit ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnExit = new Button("Exit Game");
        btnExit.setOnAction(e -> {
            stopGame();
            mainApp.showMainMenu();
        });

        btnPause = new Button("Pause");
        btnPause.setOnAction(e -> {
            controller.togglePause();
            updatePauseButton();
        });

        messageLabel = new Label("Select a card and click on arena to spawn!");
        timerLabel = new Label("Time: 3:00");
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        topBar.getChildren().addAll(btnExit, btnPause, messageLabel, spacer, timerLabel);
        root.setTop(topBar);

        // --- Center: Arena Canvas ---
        // Arena is 18x32. Let's scale it up.
        double canvasWidth = 18 * TILE_SIZE;
        double canvasHeight = 32 * TILE_SIZE;

        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseClicked(this::handleCanvasClick);

        VBox centerBox = new VBox(canvas);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: #333;"); // Dark background behind arena
        root.setCenter(centerBox);

        // --- Bottom: HUD (Elixir & Hand) ---
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: #ddd;");

        // Elixir
        HBox elixirBox = new HBox(10);
        elixirBox.setAlignment(Pos.CENTER_LEFT);
        elixirLabel = new Label("Elixir: 5");
        elixirBar = new ProgressBar(0.5);
        elixirBar.setPrefWidth(200);
        elixirBox.getChildren().addAll(elixirLabel, elixirBar);

        // Hand
        handBox = new HBox(10);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPrefHeight(100);

        bottomBox.getChildren().addAll(elixirBox, handBox);
        root.setBottom(bottomBox);

        // Start Game Loop
        startGame();

        return root;
    }

    private void startGame() {
        controller.startGame();
        updateHandView();
        updatePauseButton();

        // Create GameLoop for game state updates
        gameLoop = new GameLoop(controller);
        gameLoop.start();

        // Create AnimationTimer for rendering (view concern)
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameLoop.update(now); // Update game state
                render();              // Render graphics
                updateHUD();           // Update HUD
            }
        };
        animationTimer.start();
    }

    private void stopGame() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void updatePauseButton() {
        if (controller.isPaused()) {
            btnPause.setText("Resume");
        } else {
            btnPause.setText("Pause");
        }
    }

    private void updateHUD() {
        ElixirManager em = controller.getElixirManager();
        elixirLabel.setText("Elixir: " + em.getElixir());
        elixirBar.setProgress(em.getExactElixir() / 10.0);

        // Update Timer
        double time = controller.getGameTime();
        int minutes = (int) time / 60;
        int seconds = (int) time % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, seconds));

        if (time <= 0) {
            messageLabel.setText("GAME OVER!");
        }

        // Check if we need to refresh hand (e.g. after playing a card)
        // For simplicity, we can refresh every frame or check a flag.
        // Let's just refresh if the hand size changes or card changes?
        // Actually, let's just re-render the hand buttons if needed.
        // Optimization: Only update if changed. But for now, let's just update
        // text/disable state.

        // Re-populating HBox every frame is bad. Let's do it only when card played.
        // We can check if handBox is empty (init) or if we played a card.
        if (handBox.getChildren().isEmpty()) {
            updateHandView();
        }
    }

    private void updateHandView() {
        handBox.getChildren().clear();
        Deck deck = controller.getDeck();
        List<Card> hand = deck.getHand();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            Button btnCard = new Button(card.getName() + "\n(" + card.getElixirCost() + ")");
            btnCard.setPrefSize(80, 80);

            int index = i;
            btnCard.setOnAction(e -> {
                selectedCard = card;
                selectedHandIndex = index;
                messageLabel.setText("Selected: " + card.getName());
            });

            // Highlight selected
            if (selectedHandIndex == i) {
                btnCard.setStyle("-fx-border-color: blue; -fx-border-width: 3px;");
            }

            handBox.getChildren().add(btnCard);
        }

        // Next Card
        Card next = deck.getNextCard();
        if (next != null) {
            Label nextLabel = new Label("Next:\n" + next.getName());
            handBox.getChildren().add(nextLabel);
        }
    }

    private void handleCanvasClick(MouseEvent e) {
        if (selectedCard == null || selectedHandIndex == -1) {
            messageLabel.setText("Select a card first!");
            return;
        }

        double x = e.getX() / TILE_SIZE;
        double y = e.getY() / TILE_SIZE;

        // Validate placement based on card type
        if (!isValidPlacement(selectedCard, x, y)) {
            return; // Error message already set by isValidPlacement
        }

        if (controller.playCard(selectedCard, x, y)) {
            // Success
            controller.getDeck().playCard(selectedHandIndex);
            selectedCard = null;
            selectedHandIndex = -1;
            messageLabel.setText("Card played!");
            updateHandView(); // Refresh hand
        } else {
            messageLabel.setText("Not enough Elixir!");
        }
    }

    /**
     * Validates card placement based on card type and position
     */
    private boolean isValidPlacement(Card card, double x, double y) {
        Arena arena = controller.getArena();

        // Check bounds
        if (x < 0 || x >= arena.getWidth() || y < 0 || y >= arena.getHeight()) {
            messageLabel.setText("Out of bounds!");
            return false;
        }

        // Spell cards can be placed anywhere on the map
        if (card.getType().equals("SPELL")) {
            return true;
        }

        // Troops and Buildings must be on player side (bottom half: y > 16)
        if (y < 16) {
            messageLabel.setText("Cannot place on enemy side!");
            return false;
        }

        // Buildings cannot be placed on bridges
        if (card.getType().equals("BUILDING") && arena.isOnBridge(x, y)) {
            messageLabel.setText("Cannot place building on bridge!");
            return false;
        }

        return true;
    }

    private void render() {
        // Clear
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw Background
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw River
        double riverY = controller.getArena().getRiverY() * TILE_SIZE;
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, riverY - 10, canvas.getWidth(), 20); // 20px wide river

        // Draw Bridges
        gc.setFill(Color.BROWN);
        for (Arena.Bridge bridge : controller.getArena().getBridges()) {
            // Draw bridge at its stored position
            gc.fillRect(bridge.x * TILE_SIZE, riverY - 10, bridge.width * TILE_SIZE, 20);
        }

        // Draw Towers
        for (Tower tower : controller.getArena().getTowers()) {
            if (tower.isDestroyed())
                continue;

            gc.setFill(tower.isPlayer() ? Color.BLUE : Color.RED);
            double size = tower.getRange() > 7.0 ? 2.0 : 1.5; // King bigger than Princess
            size *= TILE_SIZE;

            gc.fillRect(tower.getX() * TILE_SIZE - size / 2, tower.getY() * TILE_SIZE - size / 2, size, size);

            // Health bar
            gc.setFill(Color.GREEN);
            gc.fillRect(tower.getX() * TILE_SIZE - size / 2, tower.getY() * TILE_SIZE - size / 2 - 5,
                    size * (tower.getHealth() / 2500.0), 3);
        }

        // Draw Buildings
        for (Building building : controller.getActiveBuildings()) {
            if (building.isDestroyed())
                continue;

            gc.setFill(building.isPlayer() ? Color.PURPLE : Color.ORANGE);
            double size = 1.2 * TILE_SIZE;
            gc.fillRect(building.getX() * TILE_SIZE - size / 2, building.getY() * TILE_SIZE - size / 2, size, size);

            // Health bar
            gc.setFill(Color.LIGHTGREEN);
            double healthRatio = building.getHealth() / building.getMaxHealth();
            gc.fillRect(building.getX() * TILE_SIZE - size / 2, building.getY() * TILE_SIZE - size / 2 - 5,
                    size * healthRatio, 3);
        }

        // Draw Units
        for (Unit unit : controller.getActiveUnits()) {
            gc.setFill(unit.isPlayer() ? Color.CYAN : Color.MAGENTA);
            double size = 0.8 * TILE_SIZE;
            gc.fillOval(unit.getX() * TILE_SIZE - size / 2, unit.getY() * TILE_SIZE - size / 2, size, size);
        }
    }
}
