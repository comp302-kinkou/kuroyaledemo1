package com.example.kuroyale.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClashRoyaleFX extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Clash Royale Demo");

        showMainMenu();
        primaryStage.show();
    }

    public void showMainMenu() {
        MainMenuView view = new MainMenuView(this);
        Scene scene = new Scene(view.getView(), 400, 400);
        primaryStage.setScene(scene);
    }

    public void showDeckBuilder() {
        DeckBuilderView view = new DeckBuilderView(this);
        Scene scene = new Scene(view.getView(), 800, 600);
        primaryStage.setScene(scene);
    }

    public void showArenaDesigner(boolean isMultiplayer) {
        ArenaDesignView view = new ArenaDesignView(this, isMultiplayer);
        Scene scene = new Scene(view.getView(), 500, 850);
        primaryStage.setScene(scene);
    }

    public void showArenaDesigner() {
        showArenaDesigner(false); // Default (from Main Menu)
    }

    public void showGameView() {
        GameView view = new GameView(this);
        Scene scene = new Scene(view.getView(), 900, 950); // Increased size for Player 1 hand visibility
        primaryStage.setScene(scene);
    }

    public void showChallengeSelection() {
        ChallengeSelectionView view = new ChallengeSelectionView(this);
        primaryStage.setScene(view.createScene());
    }

    public void showCardUpgrade() {
        try {
            CardUpgradeView view = new CardUpgradeView(this);
            Scene scene = new Scene(view.getView(), 1000, 700);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error showing card upgrade view: " + e.getMessage());
            e.printStackTrace();
            // Show error to user
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Card Upgrade view");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void showLobby() {
        LobbyView view = new LobbyView(this);
        Scene scene = new Scene(view.getView(), 600, 600);
        primaryStage.setScene(scene);
    }

    public void showQuestsAchievements() {
        QuestsAchievementsView view = new QuestsAchievementsView(this);
        Scene scene = new Scene(view.getView(), 600, 700);
        primaryStage.setScene(scene);
    }

    public void showPlayerStats() {
        PlayerStatsView view = new PlayerStatsView(this);
        Scene scene = new Scene(view.getView(), 600, 700);
        primaryStage.setScene(scene);
    }

    public void showLocalPvPDeckSelection() {
        LocalPvPDeckSelection view = new LocalPvPDeckSelection(this);
        Scene scene = new Scene(view.getView(), 800, 600);
        primaryStage.setScene(scene);
    }

    public void showLocalPvPArenaDesign() {
        LocalPvPArenaDesign view = new LocalPvPArenaDesign(this);
        Scene scene = new Scene(view.getView(), 500, 850);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}