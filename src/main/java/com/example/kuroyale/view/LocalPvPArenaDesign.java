package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Arena;
import com.example.kuroyale.model.Tower;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LocalPvPArenaDesign {

    private ClashRoyaleFX mainApp;
    private GameController controller;
    private Arena arena;

    // Current player designing (1 or 2)
    private int currentPlayer = 1;

    // Temporary storage for both players' designs
    private List<Tower> player1Towers = new ArrayList<>();
    private List<Tower> player2Towers = new ArrayList<>();
    private List<Tower> tempTowers = new ArrayList<>();
    private List<Arena.Bridge> tempBridges = new ArrayList<>();

    private Canvas canvas;
    private ToggleGroup toolsGroup;
    private String currentTool = "NONE"; // KING, PRINCESS
    private Label titleLabel;

    // Fixed dimensions for display mapping
    private double scale = 20.0; // Pixels per game unit
    private double canvasWidth;
    private double canvasHeight;

    public LocalPvPArenaDesign(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
        this.arena = controller.getArena();

        this.canvasWidth = arena.getWidth() * scale;
        this.canvasHeight = arena.getHeight() * scale;

        loadBridges();
    }

    private void loadBridges() {
        // Fixed bridges for Local PvP (same as multiplayer)
        tempBridges.clear();
        tempBridges.add(new Arena.Bridge("Bridge 1", 5.0, 2.0));
        tempBridges.add(new Arena.Bridge("Bridge 2", 11.0, 2.0));
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. Top Controls (Tools)
        HBox toolsBox = new HBox(10);
        toolsBox.setAlignment(Pos.CENTER);
        toolsBox.setPadding(new Insets(10));

        toolsGroup = new ToggleGroup();

        ToggleButton btnKing = new ToggleButton("Place King Tower");
        btnKing.setToggleGroup(toolsGroup);
        btnKing.setUserData("KING");

        ToggleButton btnPrincess = new ToggleButton("Place Princess Tower");
        btnPrincess.setToggleGroup(toolsGroup);
        btnPrincess.setUserData("PRINCESS");

        Button btnClear = new Button("Clear All");
        btnClear.setOnAction(e -> clearDesign());

        toolsGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                currentTool = "NONE";
            } else {
                currentTool = (String) newVal.getUserData();
            }
        });

        toolsBox.getChildren().addAll(btnKing, btnPrincess, btnClear);

        // Title showing current player
        titleLabel = new Label("Player 1 Arena Design");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        VBox topBox = new VBox(10, titleLabel, toolsBox);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // 2. Center: Canvas
        canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setOnMouseClicked(this::handleMapClick);

        // Wrap canvas in a box to center it
        VBox canvasContainer = new VBox(canvas);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        root.setCenter(canvasContainer);

        // 3. Bottom: Actions
        HBox actionsBox = new HBox(20);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(10));

        Button btnNext = new Button("Next");
        btnNext.setStyle("-fx-font-weight: bold; -fx-background-color: lightgreen;");
        btnNext.setOnAction(e -> handleNext());

        Button btnBack = new Button("Cancel");
        btnBack.setOnAction(e -> {
            controller.resetGameMode(); // Clean up
            mainApp.showMainMenu();
        });

        Label instructions = new Label("Place: 1 King, 2 Princess. Bridges are FIXED for this match.");

        VBox bottomBox = new VBox(10, instructions, actionsBox);
        bottomBox.setAlignment(Pos.CENTER);
        actionsBox.getChildren().addAll(btnNext, btnBack);
        root.setBottom(bottomBox);

        render();

        return root;
    }

    private void clearDesign() {
        tempTowers.clear();
        render();
    }

    private void handleMapClick(MouseEvent e) {
        if (currentTool.equals("NONE"))
            return;

        double gameX = e.getX() / scale;
        double gameY = e.getY() / scale;

        // Constraints - Player side is bottom (y > riverY)
        boolean isBottomSide = gameY > arena.getRiverY();

        if (currentTool.equals("KING") || currentTool.equals("PRINCESS")) {
            if (!isBottomSide) {
                showAlert("Invalid Side", "You can only place towers on your side (bottom)!");
                return;
            }

            // Check counts
            long kingCount = tempTowers.stream().filter(t -> t.isPlayer() && isKing(t)).count();
            long princessCount = tempTowers.stream().filter(t -> t.isPlayer() && !isKing(t)).count();

            if (currentTool.equals("KING")) {
                if (kingCount >= 1) {
                    showAlert("Limit Reached", "You already have a King Tower.");
                    return;
                }
                tempTowers.add(new Tower("KING", gameX, gameY, true));
            } else {
                if (princessCount >= 2) {
                    showAlert("Limit Reached", "You already have 2 Princess Towers.");
                    return;
                }
                tempTowers.add(new Tower("PRINCESS", gameX, gameY, true));
            }
        }

        render();
    }

    private boolean isKing(Tower t) {
        return "KING".equals(t.getType());
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // River
        double riverY = arena.getRiverY() * scale;
        gc.setFill(Color.BLUE);
        gc.fillRect(0, riverY - 10, canvasWidth, 20); // River width approx

        // Grid lines
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, canvasWidth, canvasHeight);

        // Draw Bridges
        gc.setFill(Color.BROWN);
        for (Arena.Bridge b : tempBridges) {
            gc.fillRect(b.x * scale, riverY - 10, b.width * scale, 20);
        }

        // Draw Towers
        for (Tower t : tempTowers) {
            if ("KING".equals(t.getType()))
                gc.setFill(Color.GOLD); // King
            else
                gc.setFill(Color.MAGENTA); // Princess

            double size = 1.0 * scale; // 1 unit size
            gc.fillOval((t.getX() * scale) - size / 2, (t.getY() * scale) - size / 2, size, size);
        }

        // Draw Mirror Preview (Enemy towers will be here)
        for (Tower t : tempTowers) {
            double mirrorX = t.getX();
            double mirrorY = (arena.getHeight()) - t.getY();
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            double size = 1.0 * scale;
            gc.strokeOval((mirrorX * scale) - size / 2, (mirrorY * scale) - size / 2, size, size);
        }
    }

    private void handleNext() {
        // Validate
        long kingCount = tempTowers.stream().filter(t -> "KING".equals(t.getType())).count();
        long princessCount = tempTowers.stream().filter(t -> "PRINCESS".equals(t.getType())).count();

        if (kingCount != 1 || princessCount != 2) {
            showAlert("Incomplete Design", "You must place 1 King Tower and 2 Princess Towers.");
            return;
        }

        if (currentPlayer == 1) {
            // Save Player 1's towers
            player1Towers.clear();
            for (Tower t : tempTowers) {
                player1Towers.add(new Tower(t.getType(), t.getX(), t.getY(), true));
            }

            // Switch to Player 2
            currentPlayer = 2;
            titleLabel.setText("Player 2 Arena Design");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

            // Clear for Player 2
            tempTowers.clear();
            render();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Player 1 Arena Saved");
            alert.setHeaderText(null);
            alert.setContentText("Player 1's arena saved! Now Player 2, design your arena.");
            alert.showAndWait();

        } else {
            // Save Player 2's towers
            player2Towers.clear();
            for (Tower t : tempTowers) {
                player2Towers.add(new Tower(t.getType(), t.getX(), t.getY(), true));
            }

            // Serialize and send to controller
            saveBothLayouts();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Both Arenas Ready");
            alert.setHeaderText(null);
            alert.setContentText("Both arenas saved! Starting Local PvP game...");
            alert.showAndWait();

            // Start the game
            controller.startLocalPvPGame();
            mainApp.showGameView();
        }
    }

    private void saveBothLayouts() {
        // Serialize Player 1's towers
        StringBuilder sb1 = new StringBuilder();
        for (Tower t : player1Towers) {
            sb1.append(t.getType()).append(",").append(t.getX()).append(",").append(t.getY()).append(";");
        }

        // Serialize Player 2's towers
        StringBuilder sb2 = new StringBuilder();
        for (Tower t : player2Towers) {
            sb2.append(t.getType()).append(",").append(t.getX()).append(",").append(t.getY()).append(";");
        }

        // Send to controller
        controller.setLocalPvPPlayer1Towers(sb1.toString());
        controller.setLocalPvPPlayer2Towers(sb2.toString());

        // Confirm arena design to set isArenaSaved flag
        controller.confirmArenaDesign();

        System.out.println("Both tower layouts saved for Local PvP!");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
