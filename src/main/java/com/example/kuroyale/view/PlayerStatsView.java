package com.example.kuroyale.view;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.persistence.GameData;
import com.example.kuroyale.model.persistence.PersistenceManager;
import com.example.kuroyale.model.persistence.PlayerProfile;
import com.example.kuroyale.model.CardProgression;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

/**
 * UI for displaying player statistics and profile information.
 * Shows lifetime stats, card progression summary, and challenge progress.
 */
public class PlayerStatsView {

    private ClashRoyaleFX mainApp;
    private GameData gameData;

    private static final String SAVE_FILE = "savegame.dat";

    public PlayerStatsView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
        this.gameData = PersistenceManager.getInstance().load(SAVE_FILE);
        if (this.gameData == null) {
            this.gameData = new GameData(); // Default if no save exists
        }
    }

    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        // Title
        Label title = new Label("📊 Player Statistics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.GOLD);

        // Back Button
        Button backButton = new Button("← Back to Menu");
        backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10 20; -fx-background-radius: 5;");
        backButton.setOnAction(e -> mainApp.showMainMenu());

        // Create sections
        VBox profileSection = createProfileSection();
        VBox statsSection = createLifetimeStatsSection();
        VBox cardProgressSection = createCardProgressSection();
        VBox challengeSection = createChallengeProgressSection();

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(15, profileSection, statsSection, cardProgressSection, challengeSection);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(title, scrollPane, backButton);
        return root;
    }

    private VBox createProfileSection() {
        VBox section = createSection("👤 Player Profile");
        PlayerProfile profile = gameData.getPlayerProfile();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Player Name - Editable
        Label nameLabel = new Label("Player Name:");
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setFont(Font.font("Arial", 14));

        TextField nameField = new TextField(profile.getName());
        nameField.setStyle("-fx-background-color: #3a3a5a; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 5 10; -fx-background-radius: 5;");
        nameField.setPrefWidth(200);

        Button saveNameButton = new Button("💾 Save");
        saveNameButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-padding: 5 15; -fx-background-radius: 5;");

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", 12));

        saveNameButton.setOnAction(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                statusLabel.setText("❌ Name cannot be empty!");
                statusLabel.setTextFill(Color.RED);
            } else if (newName.length() > 20) {
                statusLabel.setText("❌ Name too long (max 20 chars)");
                statusLabel.setTextFill(Color.RED);
            } else {
                profile.setName(newName);
                boolean saved = PersistenceManager.getInstance().save(gameData, SAVE_FILE);
                if (saved) {
                    statusLabel.setText("✅ Name saved!");
                    statusLabel.setTextFill(Color.LIGHTGREEN);
                } else {
                    statusLabel.setText("❌ Failed to save!");
                    statusLabel.setTextFill(Color.RED);
                }
            }
        });

        HBox nameRow = new HBox(10, nameField, saveNameButton, statusLabel);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        grid.add(nameLabel, 0, 0);
        grid.add(nameRow, 1, 0);

        // Gold Balance (read-only)
        addStatRow(grid, 1, "💰 Gold Balance:", String.format("%,d", profile.getTotalGold()));

        section.getChildren().add(grid);
        return section;
    }

    private VBox createLifetimeStatsSection() {
        VBox section = createSection("🏆 Lifetime Statistics");
        PlayerProfile profile = gameData.getPlayerProfile();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;
        addStatRow(grid, row++, "Matches Played:", String.valueOf(profile.getTotalMatchesPlayed()));
        addStatRow(grid, row++, "Wins:", String.valueOf(profile.getTotalWins()));
        addStatRow(grid, row++, "Losses:", String.valueOf(profile.getTotalLosses()));

        // Win Rate
        int totalMatches = profile.getTotalMatchesPlayed();
        double winRate = totalMatches > 0 ? (profile.getTotalWins() * 100.0 / totalMatches) : 0;
        addStatRow(grid, row++, "Win Rate:", String.format("%.1f%%", winRate));

        addStatRow(grid, row++, "Towers Destroyed:", String.valueOf(profile.getTotalTowersDestroyed()));
        addStatRow(grid, row++, "Total Damage Dealt:", String.format("%,d", profile.getTotalDamageDealt()));

        section.getChildren().add(grid);
        return section;
    }

    private VBox createCardProgressSection() {
        VBox section = createSection("🃏 Card Progression");

        // Get card progressions from GameController (which has all 28 cards
        // initialized)
        List<CardProgression> progressions = new ArrayList<>(GameController.getInstance().getAllCardProgressions());

        if (progressions.isEmpty()) {
            Label noCards = new Label("No card progression data available yet.");
            noCards.setTextFill(Color.LIGHTGRAY);
            section.getChildren().add(noCards);
            return section;
        }

        // Summary stats
        int totalCards = progressions.size();
        int level1Cards = 0, level2Cards = 0, level3Cards = 0;
        int totalGoldSpent = 0;

        for (CardProgression cp : progressions) {
            switch (cp.getLevel()) {
                case 1:
                    level1Cards++;
                    break;
                case 2:
                    level2Cards++;
                    break;
                case 3:
                    level3Cards++;
                    break;
            }
            totalGoldSpent += cp.getTotalGoldSpent();
        }

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;
        addStatRow(grid, row++, "Total Cards:", String.valueOf(totalCards) + " / 28");
        addStatRow(grid, row++, "Level 1 Cards:", String.valueOf(level1Cards));
        addStatRow(grid, row++, "Level 2 Cards:", String.valueOf(level2Cards));
        addStatRow(grid, row++, "Level 3 Cards (Max):", String.valueOf(level3Cards));
        addStatRow(grid, row++, "💰 Total Gold Spent:", String.format("%,d", totalGoldSpent));

        section.getChildren().add(grid);

        // Expandable card list
        TitledPane cardList = new TitledPane();
        cardList.setText("View All Cards");
        cardList.setExpanded(false);
        cardList.setStyle("-fx-background-color: #2a2a4a;");

        VBox cardListContent = new VBox(5);
        cardListContent.setPadding(new Insets(10));
        for (CardProgression cp : progressions) {
            HBox cardRow = new HBox(10);
            cardRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(cp.getCardName());
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setMinWidth(150);

            Label levelLabel = new Label("Lv." + cp.getLevel());
            levelLabel.setTextFill(getLevelColor(cp.getLevel()));
            levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            Label rarityLabel = new Label("[" + cp.getRarity().name() + "]");
            rarityLabel.setTextFill(Color.LIGHTGRAY);

            cardRow.getChildren().addAll(nameLabel, levelLabel, rarityLabel);
            cardListContent.getChildren().add(cardRow);
        }
        cardList.setContent(cardListContent);
        section.getChildren().add(cardList);

        return section;
    }

    private VBox createChallengeProgressSection() {
        VBox section = createSection("⚔️ Challenge Progress");

        var challengeData = gameData.getChallengeData();
        if (challengeData == null) {
            Label noData = new Label("No challenge data available yet.");
            noData.setTextFill(Color.LIGHTGRAY);
            section.getChildren().add(noData);
            return section;
        }

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        var unlocked = challengeData.getUnlockedChallenges();
        var completed = challengeData.getCompletedChallenges();
        var stars = challengeData.getStarsEarned();
        var times = challengeData.getBestTimes();

        int unlockedCount = unlocked != null ? (int) unlocked.values().stream().filter(b -> b).count() : 0;
        int completedCount = completed != null ? (int) completed.values().stream().filter(b -> b).count() : 0;
        int totalStars = stars != null ? stars.values().stream().mapToInt(Integer::intValue).sum() : 0;

        int row = 0;
        addStatRow(grid, row++, "Challenges Unlocked:", String.valueOf(unlockedCount));
        addStatRow(grid, row++, "Challenges Completed:", String.valueOf(completedCount));
        addStatRow(grid, row++, "⭐ Total Stars:", String.valueOf(totalStars));

        section.getChildren().add(grid);

        // Challenge details
        if (stars != null && !stars.isEmpty()) {
            TitledPane detailsPane = new TitledPane();
            detailsPane.setText("Challenge Details");
            detailsPane.setExpanded(false);
            detailsPane.setStyle("-fx-background-color: #2a2a4a;");

            VBox detailsContent = new VBox(5);
            detailsContent.setPadding(new Insets(10));

            for (var entry : stars.entrySet()) {
                String challengeName = entry.getKey();
                int starCount = entry.getValue();
                Long bestTime = times != null ? times.get(challengeName) : null;

                HBox challengeRow = new HBox(10);
                challengeRow.setAlignment(Pos.CENTER_LEFT);

                Label nameLabel = new Label(challengeName);
                nameLabel.setTextFill(Color.WHITE);
                nameLabel.setMinWidth(150);

                Label starsLabel = new Label("⭐".repeat(starCount));
                starsLabel.setMinWidth(60);

                Label timeLabel = new Label(bestTime != null ? formatTime(bestTime) : "N/A");
                timeLabel.setTextFill(Color.LIGHTGRAY);

                challengeRow.getChildren().addAll(nameLabel, starsLabel, timeLabel);
                detailsContent.getChildren().add(challengeRow);
            }

            detailsPane.setContent(detailsContent);
            section.getChildren().add(detailsPane);
        }

        return section;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #2a2a4a; -fx-background-radius: 10;");

        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.LIGHTBLUE);

        section.getChildren().add(sectionTitle);
        return section;
    }

    private void addStatRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setTextFill(Color.LIGHTGRAY);
        labelNode.setFont(Font.font("Arial", 14));

        Label valueNode = new Label(value);
        valueNode.setTextFill(Color.WHITE);
        valueNode.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private Color getLevelColor(int level) {
        switch (level) {
            case 1:
                return Color.LIGHTGRAY;
            case 2:
                return Color.LIGHTBLUE;
            case 3:
                return Color.GOLD;
            default:
                return Color.WHITE;
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
