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

    public void showArenaDesigner() {
        ArenaDesignView view = new ArenaDesignView(this);
        Scene scene = new Scene(view.getView(), 500, 850);
        primaryStage.setScene(scene);
    }

    public void showGameView() {
        GameView view = new GameView(this);
        Scene scene = new Scene(view.getView(), 600, 800); // Larger for game
        primaryStage.setScene(scene);
    }

    public void showChallengeSelection() {
        ChallengeSelectionView view = new ChallengeSelectionView(this);
        primaryStage.setScene(view.createScene());
    }

    public void showCardUpgrade() {
        CardUpgradeView view = new CardUpgradeView(this);
        Scene scene = new Scene(view.getView(), 1000, 700);
        primaryStage.setScene(scene);
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

    public void showLocalPvPDeckSelection() {
        LocalPvPDeckSelection view = new LocalPvPDeckSelection(this);
        Scene scene = new Scene(view.getView(), 800, 600);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
