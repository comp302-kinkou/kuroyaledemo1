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

    private boolean isMultiplayerMode = false;

    public ArenaDesignView(ClashRoyaleFX mainApp, boolean isMultiplayerMode) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
        this.arena = controller.getArena();
        this.isMultiplayerMode = isMultiplayerMode;

        this.canvasWidth = arena.getWidth() * scale;
        this.canvasHeight = arena.getHeight() * scale;

        loadCurrentDesign();
    }

    private void loadCurrentDesign() {
        tempTowers.clear();
        tempBridges.clear();

        // Load existing towers from arena
        for (Tower t : arena.getTowers()) {
            if (t.isPlayer()) {
                tempTowers.add(new Tower(t.getType(), t.getX(), t.getY(), true));
            }
        }

        // Load bridges
        if (isMultiplayerMode) {
            // Requirement: 2 Bridges already present in default place
            tempBridges.add(new Arena.Bridge("Bridge 1", 5.0, 2.0));
            tempBridges.add(new Arena.Bridge("Bridge 2", 11.0, 2.0));
        } else {
            // Load existing bridges from arena (Singleplayer)
            for (Arena.Bridge b : arena.getBridges()) {
                tempBridges.add(new Arena.Bridge(b.name, b.x, b.width));
            }
        }
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

        // Disable bridge tool in multiplayer
        if (isMultiplayerMode) {
            btnBridge.setDisable(true);
            btnBridge.setVisible(false); // Hide it to be cleaner? Or just disable.
            // Let's hide it to avoid confusion
            btnBridge.setManaged(false);
        }

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

        // Return to appropriate view
        Button btnBack = new Button("Cancel");
        btnBack.setOnAction(e -> {
            if (isMultiplayerMode) {
                mainApp.showLobby();
            } else {
                mainApp.showMainMenu();
            }
        });

        Label instructions = new Label(
                isMultiplayerMode ? "Place: 1 King, 2 Princess. Bridges are FIXED for this match."
                        : "Place: 1 King, 2 Princess, 1-3 Bridges on YOUR side (Bottom).");

        VBox bottomBox = new VBox(10, instructions, actionsBox);
        bottomBox.setAlignment(Pos.CENTER);
        actionsBox.getChildren().addAll(btnSave, btnBack);
        root.setBottom(bottomBox);

        render();

        return root;
    }

    private void clearDesign() {
        tempTowers.clear();
        // In multiplayer, do NOT clear bridges
        if (!isMultiplayerMode) {
            tempBridges.clear();
        }
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
            // Should be disabled but double check
            if (isMultiplayerMode)
                return;

            // Bridge must be ON the river (approx)
            if (Math.abs(gameY - arena.getRiverY()) > 3.0) {
                showAlert("Invalid Position", "Bridges must be placed on the river!");
                return;
            }
            if (tempBridges.size() >= 3) {
                showAlert("Limit Reached", "Max 3 bridges allowed.");
                return;
            }

            // Check if new bridge would overlap with existing bridges
            double newBridgeX = gameX - 1.0;
            double newBridgeWidth = 2.0;
            if (bridgeOverlapsBridges(newBridgeX, newBridgeWidth)) {
                showAlert("Invalid Placement",
                        "Bridges cannot overlap with each other! Please choose a different location.");
                return;
            }

            tempBridges.add(new Arena.Bridge("Bridge " + (tempBridges.size() + 1), newBridgeX, newBridgeWidth));

        } else if (currentTool.equals("KING") || currentTool.equals("PRINCESS")) {
            if (!isBottomSide) {
                showAlert("Invalid Side", "You can only place towers on your side!");
                return;
            }

            // Check if placement is on the river (river zone is approximately ±1 unit from
            // riverY)
            double riverZoneHalfWidth = 1.0; // River spans 1 unit above and below riverY
            if (Math.abs(gameY - arena.getRiverY()) <= riverZoneHalfWidth) {
                showAlert("Invalid Position", "Towers cannot be placed on the river!");
                return;
            }

            // Check counts
            long kingCount = tempTowers.stream().filter(t -> t.isPlayer() && isKing(t)).count();
            long princessCount = tempTowers.stream().filter(t -> t.isPlayer() && !isKing(t)).count();

            // Check if new tower would overlap with existing towers
            if (towerOverlapsTowers(gameX, gameY)) {
                showAlert("Invalid Placement",
                        "Towers cannot overlap with each other! Please choose a different location.");
                return;
            }

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

    /**
     * Checks if a new bridge would overlap with any existing bridge.
     * Bridges are rectangles - they overlap if their x-ranges intersect.
     */
    private boolean bridgeOverlapsBridges(double newBridgeX, double newBridgeWidth) {
        double newLeft = newBridgeX;
        double newRight = newBridgeX + newBridgeWidth;

        for (Arena.Bridge existing : tempBridges) {
            double existingLeft = existing.x;
            double existingRight = existing.x + existing.width;

            // Check if ranges overlap (both are on the river, so only x matters)
            if (newLeft < existingRight && newRight > existingLeft) {
                return true; // Overlap detected
            }
        }
        return false;
    }

    /**
     * Checks if a new tower would overlap with any existing tower.
     * Towers are circles - they overlap if distance between centers < sum of radii.
     */
    private boolean towerOverlapsTowers(double newX, double newY) {
        double towerRadius = 0.5; // Tower size is 1.0 unit, radius is 0.5
        double minDistance = towerRadius * 2; // Two towers overlap if closer than their combined radii

        for (Tower existing : tempTowers) {
            double dx = newX - existing.getX();
            double dy = newY - existing.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                return true; // Overlap detected
            }
        }
        return false;
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
            if ("KING".equals(t.getType()))
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
        long kingCount = tempTowers.stream().filter(t -> "KING".equals(t.getType())).count();
        long princessCount = tempTowers.stream().filter(t -> "PRINCESS".equals(t.getType())).count();

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

        // In multiplayer, DO NOT clear/update bridges if we didn't touch them (which we
        // couldn't)
        // But tempBridges holds the synced bridges anyway.
        // Safer to just allow overwrite IF tempBridges matches synced state, but
        // simpler:
        // just blindly overwrite since tempBridges started as copy and couldn't be
        // changed.
        // Wait, if we overwrite, we might lose precision? No, copies are fine.

        // Actually, if we are in multiplayer, let's explicitly NOT touch bridges in the
        // arena object
        // to be absolutely safe against drift, although re-adding same values is fine.
        if (!isMultiplayerMode) {
            arena.clearBridges();
            for (Arena.Bridge b : tempBridges) {
                arena.addBridge(b.name, b.x);
            }
        }
        // If multiplayer, bridges are untouched in `arena` object.

        // Add Player Towers
        for (Tower t : tempTowers) {
            arena.addTower(t);
        }

        // Add Enemy Towers (Mirrored)
        for (Tower t : tempTowers) {
            double mirrorY = arena.getHeight() - t.getY();
            // Use explicit type
            String type = t.getType();
            arena.addTower(new Tower(type, t.getX(), mirrorY, false));
        }

        controller.confirmArenaDesign();

        showAlert("Saved", "Arena design saved successfully!");
        if (isMultiplayerMode) {
            mainApp.showLobby();
        } else {
            mainApp.showMainMenu();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
