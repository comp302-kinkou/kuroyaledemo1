package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.controller.GameLoop;
import com.example.kuroyale.model.*;
import com.example.kuroyale.model.challenge.Challenge;
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
    private int selectedByPlayer = 0; // Track which player selected the card (1 or 2)

    // UI Elements
    private Label elixirLabel;
    private ProgressBar elixirBar;
    private Label player2ElixirLabel;
    private ProgressBar player2ElixirBar;
    private HBox handBox;
    private HBox player2HandBox;
    private Label messageLabel;
    private Label timerLabel;
    private Label turnIndicatorLabel;
    private Button btnPause;
    private Button btnSpeed;
    private Label playerScoreLabel;
    private Label enemyScoreLabel;
    private Label comboCounterLabel;

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
            controller.resetGameMode(); // Reset PvP state when exiting
            mainApp.showMainMenu();
        });

        btnPause = new Button("Pause");
        btnPause.setOnAction(e -> {
            controller.togglePause();
            updatePauseButton();
        });

        btnSpeed = new Button("1x");
        btnSpeed.setOnAction(e -> {
            toggleSpeed();
        });

        // Score labels - different for Local PvP
        if (controller.isLocalPvP()) {
            playerScoreLabel = new Label("Player 1: 0");
            playerScoreLabel.setStyle(
                    "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: cyan; -fx-padding: 0 10 0 0;");

            enemyScoreLabel = new Label("Player 2: 0");
            enemyScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: orange;");
        } else {
            playerScoreLabel = new Label("Player: 0");
            playerScoreLabel.setStyle(
                    "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: cyan; -fx-padding: 0 10 0 0;");

            enemyScoreLabel = new Label("Enemy: 0");
            enemyScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: orange;");
        }

        // Turn indicator for local PvP
        turnIndicatorLabel = new Label("");
        turnIndicatorLabel.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1c40f; -fx-padding: 0 10 0 10;");
        if (controller.isLocalPvP()) {
            turnIndicatorLabel.setText("Player " + controller.getCurrentPlayerTurn() + "'s Turn");
        }

        messageLabel = new Label("Select a card and click on arena to spawn!");
        timerLabel = new Label("Time: 3:00");
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        comboCounterLabel = new Label("Combos: 0");
        comboCounterLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gold;");

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Layout: [Player Score] [Buttons...] [Turn Indicator] [Message] [Spacer]
        // [Combo Counter] [Timer] [Enemy Score]
        topBar.getChildren().addAll(playerScoreLabel, btnExit, btnPause, btnSpeed, turnIndicatorLabel, messageLabel,
                spacer, comboCounterLabel, timerLabel,
                enemyScoreLabel);
        topBar.setStyle("-fx-background-color: #222;"); // Darker top bar

        // Challenge Overlay / Info OR Player 2 UI for Local PvP
        VBox topContainer = new VBox(topBar);

        if (controller.isLocalPvP()) {
            // Player 2 UI at the top (upside down gameplay)
            VBox player2UI = new VBox(10);
            player2UI.setPadding(new Insets(10));
            player2UI.setStyle("-fx-background-color: #f0e68c;"); // Light yellow for Player 2

            // Player 2 Elixir
            HBox player2ElixirBox = new HBox(10);
            player2ElixirBox.setAlignment(Pos.CENTER);
            player2ElixirLabel = new Label("Player 2 Elixir: 5");
            player2ElixirLabel.setStyle("-fx-font-weight: bold;");
            player2ElixirBar = new ProgressBar(0.5);
            player2ElixirBar.setPrefWidth(200);
            player2ElixirBox.getChildren().addAll(player2ElixirLabel, player2ElixirBar);

            // Player 2 Hand
            player2HandBox = new HBox(10);
            player2HandBox.setAlignment(Pos.CENTER);
            player2HandBox.setPrefHeight(100);

            player2UI.getChildren().addAll(player2ElixirBox, player2HandBox);
            topContainer.getChildren().add(player2UI);
        } else if (controller.getActiveChallenge() != null) {
            Challenge c = controller.getActiveChallenge();
            Label challengeLabel = new Label("CHALLENGE: " + c.getName() + " - " + c.getDescription().split("\n")[0]);
            challengeLabel.setStyle(
                    "-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
            challengeLabel.setMaxWidth(Double.MAX_VALUE);
            challengeLabel.setAlignment(Pos.CENTER);
            topContainer.getChildren().add(0, challengeLabel);
        }

        root.setTop(topContainer);

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

        // Elixir displays
        if (controller.isLocalPvP()) {
            // Only Player 1 elixir at bottom in local PvP
            HBox elixirBox = new HBox(10);
            elixirBox.setAlignment(Pos.CENTER);
            elixirLabel = new Label("Player 1 Elixir: 5");
            elixirLabel.setStyle("-fx-font-weight: bold;");
            elixirBar = new ProgressBar(0.5);
            elixirBar.setPrefWidth(200);
            elixirBox.getChildren().addAll(elixirLabel, elixirBar);
            bottomBox.getChildren().add(elixirBox);
        } else {
            // Single elixir display for normal mode
            HBox elixirBox = new HBox(10);
            elixirBox.setAlignment(Pos.CENTER_LEFT);
            elixirLabel = new Label("Elixir: 5");
            elixirBar = new ProgressBar(0.5);
            elixirBar.setPrefWidth(200);
            elixirBox.getChildren().addAll(elixirLabel, elixirBar);
            bottomBox.getChildren().add(elixirBox);
        }

        // Hand (Player 1 hand in local PvP, or single hand in normal mode)
        handBox = new HBox(10);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPrefHeight(100);

        bottomBox.getChildren().add(handBox);
        root.setBottom(bottomBox);

        // Start Game Loop
        startGame();

        return root;
    }

    private void startGame() {
        controller.startGame();
        updateHandView();
        updatePauseButton();
        updateSpeedButton();

        // Create GameLoop for game state updates
        gameLoop = new GameLoop(controller);
        gameLoop.start();

        // Create AnimationTimer for rendering (view concern)
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameLoop.update(now); // Update game state
                render(); // Render graphics
                updateHUD(); // Update HUD
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
        // Update Elixir displays
        if (controller.isLocalPvP()) {
            // Update Player 1 elixir
            ElixirManager em1 = controller.getElixirManager();
            elixirLabel.setText("Player 1 Elixir: " + em1.getElixir() + "/10");
            elixirBar.setProgress(em1.getExactElixir() / 10.0);

            // Update Player 2 elixir
            ElixirManager em2 = controller.getPlayer2ElixirManager();
            player2ElixirLabel.setText("Player 2 Elixir: " + em2.getElixir() + "/10");
            player2ElixirBar.setProgress(em2.getExactElixir() / 10.0);

            // Highlight current player's elixir
            int currentTurn = controller.getCurrentPlayerTurn();
            if (currentTurn == 1) {
                elixirLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                player2ElixirLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
            } else {
                elixirLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
                player2ElixirLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22;");
            }

            // Update turn indicator
            turnIndicatorLabel.setText("Player " + currentTurn + "'s Turn");
        } else {
            // Normal mode - single elixir
            ElixirManager em = controller.getElixirManager();
            elixirLabel.setText("Elixir: " + em.getElixir() + "/10");
            elixirBar.setProgress(em.getExactElixir() / 10.0);
        }

        // Update Timer
        double time = controller.getGameTime();
        int minutes = (int) time / 60;
        int seconds = (int) time % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, seconds));

        if (time <= 0 || "WIN".equals(controller.getGameResult()) || "LOSS".equals(controller.getGameResult())
                || "DRAW".equals(controller.getGameResult())) {
            showGameOverOverlay();
        }

        // Update Crown Score
        int playerCrowns = 0;
        int enemyCrowns = 0;
        for (Tower tower : controller.getArena().getTowers()) {
            if (tower.isDestroyed()) {
                if (tower.isPlayer()) {
                    enemyCrowns++;
                } else {
                    playerCrowns++;
                }
            }
        }

        // Update labels based on mode
        if (controller.isLocalPvP()) {
            playerScoreLabel.setText("Player 1: " + playerCrowns);
            enemyScoreLabel.setText("Player 2: " + enemyCrowns);
        } else {
            playerScoreLabel.setText("Player: " + playerCrowns);
            enemyScoreLabel.setText("Enemy: " + enemyCrowns);
        }

        // Update combo counter
        comboCounterLabel.setText("Combos: " + controller.getComboManager().getUniqueComboCount());

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
        if (controller.isLocalPvP() && player2HandBox != null) {
            player2HandBox.getChildren().clear();
        }

        if (controller.isLocalPvP()) {
            // Update both players' hands using SEPARATE PvP decks
            updatePlayerHand(controller.getLocalPvPPlayer1Deck(), handBox, 1);
            updatePlayerHand(controller.getPlayer2Deck(), player2HandBox, 2);
        } else {
            // Normal mode - just one hand
            updatePlayerHand(controller.getDeck(), handBox, 1);
        }
    }

    private void updatePlayerHand(Deck deck, HBox targetHandBox, int playerNumber) {
        if (deck == null || targetHandBox == null)
            return;

        List<Card> hand = deck.getHand();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            CardProgression progression = controller.getCardProgression(card);
            CardRarity rarity = CardLibrary.getCardRarity(card.getName());

            // Build button text with level indicator
            String levelStars = getLevelStars(progression != null ? progression.getLevel() : 1);
            Button btnCard = new Button(card.getName() + "\n(" + card.getElixirCost() + ")\n" + levelStars);
            btnCard.setPrefSize(80, 90);

            // Apply rarity border color
            String rarityColor = getRarityBorderColor(rarity);
            String baseStyle = "-fx-border-color: " + rarityColor + "; -fx-border-width: 2px; -fx-font-size: 10px;";

            int index = i;
            int currentPlayer = playerNumber;
            btnCard.setOnAction(e -> {
                // Only allow selection if it's this player's turn in PvP
                if (controller.isLocalPvP() && controller.getCurrentPlayerTurn() != currentPlayer) {
                    messageLabel
                            .setText("Not your turn! It's Player " + controller.getCurrentPlayerTurn() + "'s turn.");
                    return;
                }
                selectedCard = card;
                selectedHandIndex = index;
                selectedByPlayer = currentPlayer; // Track which player selected this card

                // DEBUG
                System.out.println("[DEBUG] Card selected by Player " + currentPlayer + ": " + card.getName() +
                        " at index " + index + " (current turn: " + controller.getCurrentPlayerTurn() + ")");

                messageLabel.setText("Selected: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ")");
            });

            // Highlight selected with blue border, otherwise use rarity color
            if (selectedHandIndex == i && selectedCard == card) {
                btnCard.setStyle(baseStyle + " -fx-border-color: blue; -fx-border-width: 3px;");
            } else {
                btnCard.setStyle(baseStyle);
            }

            targetHandBox.getChildren().add(btnCard);
        }

        // Next Card
        Card next = deck.getNextCard();
        if (next != null) {
            CardProgression nextProgression = controller.getCardProgression(next);
            String nextLevelStars = getLevelStars(nextProgression != null ? nextProgression.getLevel() : 1);
            Label nextLabel = new Label("Next:\n" + next.getName() + "\n" + nextLevelStars);
            nextLabel.setStyle("-fx-font-size: 11px; -fx-text-alignment: center;");
            targetHandBox.getChildren().add(nextLabel);
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

        // Get the correct deck for the player who selected the card
        Deck currentDeck;
        boolean isPlayer1;
        if (controller.isLocalPvP()) {
            // Use selectedByPlayer to get the right deck (important!)
            currentDeck = (selectedByPlayer == 1) ? controller.getLocalPvPPlayer1Deck() : controller.getPlayer2Deck();
            isPlayer1 = (selectedByPlayer == 1);
        } else {
            currentDeck = controller.getDeck();
            isPlayer1 = true;
        }

        if (controller.playCard(selectedCard, x, y, isPlayer1)) {
            // Success
            currentDeck.playCard(selectedHandIndex);
            selectedCard = null;
            selectedHandIndex = -1;
            selectedByPlayer = 0; // Reset

            if (controller.isLocalPvP()) {
                // Switch turn after successful card play
                controller.switchTurn();
                messageLabel.setText("Player " + controller.getCurrentPlayerTurn() + "'s turn - select a card!");
            } else {
                messageLabel.setText("Card played!");
            }

            updateHandView(); // Refresh hand to show next player's cards
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

        // In local PvP, Player 1 plays on bottom (y > 16), Player 2 plays on top (y <
        // 16)
        if (controller.isLocalPvP()) {
            int currentPlayer = controller.getCurrentPlayerTurn();
            if (currentPlayer == 1) {
                // Player 1 plays on bottom half
                if (y < 16) {
                    messageLabel.setText("Player 1 must play on bottom half!");
                    return false;
                }
            } else {
                // Player 2 plays on top half
                if (y > 16) {
                    messageLabel.setText("Player 2 must play on top half!");
                    return false;
                }
            }
        } else {
            // Normal mode: Troops and Buildings must be on player side (bottom half: y >
            // 16)
            if (y < 16) {
                messageLabel.setText("Cannot place on enemy side!");
                return false;
            }
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

        // Draw Effects
        for (Effect effect : controller.getActiveEffects()) {
            double alpha = effect.getDuration() / effect.getMaxDuration();
            // Yellow explosion
            gc.setFill(Color.rgb(255, 255, 0, alpha * 0.7));
            double r = effect.getRadius() * TILE_SIZE;
            gc.fillOval(effect.getX() * TILE_SIZE - r, effect.getY() * TILE_SIZE - r, r * 2, r * 2);
        }

        // Draw Combo Visuals
        long currentTime = System.currentTimeMillis();
        for (com.example.kuroyale.model.combo.ComboVisualEffect visual : controller.getActiveComboVisuals()) {
            double x = visual.getX() * TILE_SIZE;
            double y = visual.getY() * TILE_SIZE;
            double scale = visual.getScale(currentTime);
            double alpha = visual.getAlpha(currentTime);
            
            // Draw "COMBO!" text with animation
            gc.save();
            gc.setGlobalAlpha(alpha);
            gc.setFill(Color.GOLD);
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            
            // Scale the text
            double fontSize = 20 * scale;
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, fontSize));
            
            // Draw "COMBO!" text
            String comboText = "COMBO!";
            gc.fillText(comboText, x, y - 20);
            gc.strokeText(comboText, x, y - 20);
            
            // Draw combo name below
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, fontSize * 0.7));
            gc.setFill(Color.WHITE);
            gc.fillText(visual.getComboName(), x, y + 5);
            
            gc.restore();
        }
    }

    private void toggleSpeed() {
        if (controller.getTimeScale() == 1.0) {
            controller.setTimeScale(2.0);
        } else {
            controller.setTimeScale(1.0);
        }
        updateSpeedButton();
    }

    private void updateSpeedButton() {
        double scale = controller.getTimeScale();
        if (scale == 1.0) {
            btnSpeed.setText("1x");
        } else {
            btnSpeed.setText("2x");
        }
    }

    private void showGameOverOverlay() {
        if (((BorderPane) canvas.getParent().getParent()).getCenter() instanceof VBox) {
            VBox currentCenter = (VBox) ((BorderPane) canvas.getParent().getParent()).getCenter();
            // Simple check if we already replaced the game view with our overlay (which has
            // styled background)
            if (currentCenter.getStyle().contains("rgba(0, 0, 0, 0.8)")) {
                return;
            }
        }

        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Fill space
        overlay.setPadding(new Insets(40));

        String result = controller.getGameResult();
        if (result == null)
            result = "DRAW";

        String titleText = "DRAW";
        String color = "#ecf0f1";

        if (controller.isLocalPvP()) {
            // Local PvP - determine which player won
            if ("WIN".equals(result)) {
                // Player 1 wins (player side won)
                titleText = "PLAYER 1 WINS!";
                color = "#27ae60";
            } else if ("LOSS".equals(result)) {
                // Player 2 wins (enemy side won, which is Player 2 in PvP)
                titleText = "PLAYER 2 WINS!";
                color = "#e67e22";
            }
        } else {
            // Normal mode
            if ("WIN".equals(result)) {
                titleText = "VICTORY!";
                color = "#f1c40f";
            } else if ("LOSS".equals(result)) {
                titleText = "DEFEAT";
                color = "#e74c3c";
            }
        }

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        overlay.getChildren().add(titleLabel);

        // Challenge Results
        if (controller.getActiveChallenge() != null && "WIN".equals(result)) {
            int stars = controller.getActiveChallenge().calculateStars(controller);
            Label starsLabel = new Label("Stars: " + "⭐".repeat(stars));
            starsLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: gold;");

            Label rewardLabel = new Label("Reward: " + controller.getActiveChallenge().getReward() + " Gold");
            rewardLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

            overlay.getChildren().addAll(starsLabel, rewardLabel);
        }

        Button exitButton = new Button("Return to Menu");
        exitButton.setStyle("-fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white;");
        exitButton.setOnAction(e -> {
            stopGame();
            mainApp.showMainMenu();
        });

        overlay.getChildren().add(exitButton);

        // Replace the center with our overlay
        ((BorderPane) canvas.getParent().getParent()).setCenter(overlay);

        stopGame();
    }
}