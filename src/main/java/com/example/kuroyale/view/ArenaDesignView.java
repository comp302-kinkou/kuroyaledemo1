package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Arena;
import com.example.kuroyale.model.Tower;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ArenaDesignView {

    private ClashRoyaleFX mainApp;
    private GameController controller;

    public ArenaDesignView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.controller = GameController.getInstance();
    }

    public Parent getView() {
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
        updateArenaDisplay(arenaDisplay);

        Button btnCreate = new Button("Save Arena Layout");
        btnCreate.setOnAction(e -> {
            try {
                int count = Integer.parseInt(txtBridges.getText());
                if (count < 1 || count > 3) {
                    showAlert("Invalid Input", "Please enter a number between 1 and 3.");
                    return;
                }

                Arena arena = controller.getArena();
                arena.getBridges().clear();
                for (int i = 1; i <= count; i++) {
                    arena.addBridge("Bridge " + i);
                }
                updateArenaDisplay(arenaDisplay);
                showAlert("Success", "Arena layout saved!");

            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });

        Button btnBack = new Button("Back to Menu");
        btnBack.setOnAction(e -> mainApp.showMainMenu());

        root.getChildren().addAll(titleLabel, bridgeInputBox, btnCreate, arenaDisplay, btnBack);
        return root;
    }

    private void updateArenaDisplay(TextArea display) {
        Arena arena = controller.getArena();

        StringBuilder sb = new StringBuilder();

        // ===== Arena info =====
        sb.append("Arena Size: ")
                .append(arena.getWidth())
                .append(" x ")
                .append(arena.getHeight())
                .append("\n");
        sb.append("River Y: ")
                .append(arena.getRiverY())
                .append("\n\n");

        // ===== Towers =====
        sb.append("TOWERS:\n");
        if (arena.getTowers().isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (Tower t : arena.getTowers()) {
                sb.append("  - ")
                        .append(t.toString())
                        .append("\n");
            }
        }

        // ===== Bridges =====
        sb.append("\nBRIDGES:\n");
        if (arena.getBridges().isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (Arena.Bridge b : arena.getBridges()) {
                sb.append("  - ")
                        .append(b.name)
                        .append(" (x=")
                        .append(b.x)
                        .append(", width=")
                        .append(b.width)
                        .append(")\n");
            }
        }

        display.setText(sb.toString());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
