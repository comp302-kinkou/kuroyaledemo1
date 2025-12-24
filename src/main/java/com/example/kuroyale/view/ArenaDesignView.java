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

public class ArenaDesignView {

    private ClashRoyaleFX mainApp;
    private GameController controller;
    private Arena arena;

    // Temporary storage for design
    private List<Tower> tempTowers = new ArrayList<>();
    private List<Arena.Bridge> tempBridges = new ArrayList<>();

    private Canvas canvas;
    private ToggleGroup toolsGroup;
    private String currentTool = "NONE"; // BRIDGE, KING, PRINCESS

    // Fixed dimensions for display mapping
    private double scale = 20.0; // Pixels per game unit
    private double canvasWidth;
    private double canvasHeight;

    public ArenaDesignView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
        this.arena = controller.getArena();

        this.canvasWidth = arena.getWidth() * scale;
        this.canvasHeight = arena.getHeight() * scale;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. Top Controls (Tools)
        HBox toolsBox = new HBox(10);
        toolsBox.setAlignment(Pos.CENTER);
        toolsBox.setPadding(new Insets(10));

        toolsGroup = new ToggleGroup();

        ToggleButton btnBridge = new ToggleButton("Place Bridge");
        btnBridge.setToggleGroup(toolsGroup);
        btnBridge.setUserData("BRIDGE");

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

        toolsBox.getChildren().addAll(btnBridge, btnKing, btnPrincess, btnClear);
        root.setTop(toolsBox);

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

        Button btnSave = new Button("Save & Exit");
        btnSave.setStyle("-fx-font-weight: bold; -fx-background-color: lightgreen;");
        btnSave.setOnAction(e -> saveAndExit());

        Button btnBack = new Button("Cancel");
        btnBack.setOnAction(e -> mainApp.showMainMenu());

        Label instructions = new Label("Place: 1 King, 2 Princess, 1-3 Bridges on YOUR side (Bottom).");

        VBox bottomBox = new VBox(10, instructions, actionsBox);
        bottomBox.setAlignment(Pos.CENTER);
        actionsBox.getChildren().addAll(btnSave, btnBack);
        root.setBottom(bottomBox);

        // Initial render
        clearDesign(); // Reset temp lists
        render();

        return root;
    }

    private void clearDesign() {
        tempTowers.clear();
        tempBridges.clear();
        render();
    }

    private void handleMapClick(MouseEvent e) {
        if (currentTool.equals("NONE"))
            return;

        double gameX = e.getX() / scale;
        double gameY = e.getY() / scale;

        // Constraints
        boolean isBottomSide = gameY > arena.getRiverY(); // Player side is bottom > 16.0

        if (currentTool.equals("BRIDGE")) {
            // Bridge must be ON the river (approx)
            // Let's allow clicking anywhere near riverY, and snap to riverY
            if (Math.abs(gameY - arena.getRiverY()) > 3.0) {
                showAlert("Invalid Position", "Bridges must be placed on the river!");
                return;
            }
            if (tempBridges.size() >= 3) {
                showAlert("Limit Reached", "Max 3 bridges allowed.");
                return;
            }
            // Add Bridge
            // Make sure it doesn't overlap too much? Simplified for now.
            tempBridges.add(new Arena.Bridge("Bridge " + (tempBridges.size() + 1), gameX - 1.0, 2.0)); // centered width

        } else if (currentTool.equals("KING") || currentTool.equals("PRINCESS")) {
            if (!isBottomSide) {
                showAlert("Invalid Side", "You can only place towers on your side!");
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

        // Grid lines (optional) or Bounds
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, canvasWidth, canvasHeight);

        // Draw Bridges
        gc.setFill(Color.BROWN);
        for (Arena.Bridge b : tempBridges) {
            gc.fillRect(b.x * scale, riverY - 10, b.width * scale, 20);
        }

        // Draw Towers
        for (Tower t : tempTowers) {
            if (t.getHealth() >= 4000)
                gc.setFill(Color.GOLD); // King
            else
                gc.setFill(Color.MAGENTA); // Princess

            double size = 1.0 * scale; // 1 unit size
            gc.fillOval((t.getX() * scale) - size / 2, (t.getY() * scale) - size / 2, size, size);
        }

        // Draw Mirror Preview (Enemy)
        for (Tower t : tempTowers) {
            double mirrorX = t.getX();

            double mirrorY = (arena.getHeight()) - t.getY();

            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            double size = 1.0 * scale;
            gc.strokeOval((mirrorX * scale) - size / 2, (mirrorY * scale) - size / 2, size, size);
        }
    }

    private void saveAndExit() {
        // Validate
        long kingCount = tempTowers.stream().filter(t -> t.getHealth() >= 4000).count();
        long princessCount = tempTowers.stream().filter(t -> t.getHealth() < 4000).count();

        if (kingCount != 1 || princessCount != 2) {
            showAlert("Incomplete Design", "You must place 1 King Tower and 2 Princess Towers.");
            return;
        }

        if (tempBridges.size() < 1 || tempBridges.size() > 3) {
            showAlert("Invalid Bridges", "You must have between 1 and 3 bridges.");
            return;
        }

        // Update Actual Arena
        arena.clearTowers();
        arena.clearBridges();

        // Add Player Towers
        for (Tower t : tempTowers) {
            arena.addTower(t);
        }

        // Add Enemy Towers (Mirrored)
        for (Tower t : tempTowers) {
            double mirrorY = arena.getHeight() - t.getY();
            // Assuming Type based on HP again
            String type = (t.getHealth() >= 4000) ? "KING" : "PRINCESS";
            arena.addTower(new Tower(type, t.getX(), mirrorY, false));
        }

        // 3. Reconstruct the Bridges
        for (Arena.Bridge b : tempBridges) {
            arena.addBridge(b.name, b.x);
        }

        controller.confirmArenaDesign();

        showAlert("Saved", "Arena design saved successfully!");
        mainApp.showMainMenu();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
