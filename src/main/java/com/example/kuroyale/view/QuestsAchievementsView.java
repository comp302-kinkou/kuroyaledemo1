package com.example.kuroyale.view;

import com.example.kuroyale.model.quest.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * UI for Quests & Achievements screen.
 */
public class QuestsAchievementsView {
    
    private ClashRoyaleFX mainApp;
    
    public QuestsAchievementsView(ClashRoyaleFX mainApp) {
        this.mainApp = mainApp;
    }
    
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #1a252f);");
        
        Label titleLabel = new Label("Quests & Achievements");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.GOLD);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab achievementsTab = new Tab("Achievements");
        achievementsTab.setContent(createAchievementsContent());
        
        Tab questsTab = new Tab("Daily Quests");
        questsTab.setContent(createQuestsPlaceholder());
        
        tabPane.getTabs().addAll(achievementsTab, questsTab);
        
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnBack.setOnAction(e -> mainApp.showMainMenu());
        
        root.getChildren().addAll(titleLabel, tabPane, btnBack);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        return root;
    }
    
    private ScrollPane createAchievementsContent() {
        VBox achievementsList = new VBox(10);
        achievementsList.setPadding(new Insets(15));
        achievementsList.setStyle("-fx-background-color: #34495e;");
        
        QuestManager qm = QuestManager.getInstance();
        
        for (AchievementType type : AchievementType.values()) {
            Achievement achievement = qm.getAchievement(type);
            HBox row = createAchievementRow(achievement);
            achievementsList.getChildren().add(row);
        }
        
        ScrollPane scrollPane = new ScrollPane(achievementsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #34495e; -fx-background-color: #34495e;");
        
        return scrollPane;
    }
    
    private HBox createAchievementRow(Achievement achievement) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(12));
        row.setAlignment(Pos.CENTER_LEFT);
        
        boolean isUnlocked = achievement.isUnlocked();
        boolean isCompleted = achievement.isCompleted();
        boolean canClaim = achievement.canClaimReward();
        
        String bgColor;
        if (achievement.isRewardClaimed()) {
            bgColor = "#27ae60";
        } else if (isCompleted) {
            bgColor = "#f39c12";
        } else if (isUnlocked) {
            bgColor = "#3498db";
        } else {
            bgColor = "#7f8c8d";
        }
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");
        
        Label statusLabel = new Label(isCompleted ? "✓" : (isUnlocked ? "◐" : "🔒"));
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setMinWidth(30);
        
        VBox textBox = new VBox(3);
        Label nameLabel = new Label(achievement.getDisplayName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        
        String descText = isUnlocked ? achievement.getDescription() : achievement.getType().getLockedHint();
        Label descLabel = new Label(descText);
        descLabel.setFont(Font.font("Arial", 11));
        descLabel.setTextFill(Color.LIGHTGRAY);
        
        textBox.getChildren().addAll(nameLabel, descLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        
        VBox progressBox = new VBox(3);
        progressBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label progressLabel = new Label(achievement.getProgressText());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        progressLabel.setTextFill(Color.WHITE);
        
        ProgressBar progressBar = new ProgressBar(achievement.getProgressPercentage());
        progressBar.setPrefWidth(80);
        progressBar.setStyle("-fx-accent: #2ecc71;");
        
        progressBox.getChildren().addAll(progressLabel, progressBar);
        
        VBox rewardBox = new VBox(3);
        rewardBox.setAlignment(Pos.CENTER);
        rewardBox.setMinWidth(80);
        
        Label rewardLabel = new Label("+" + achievement.getGoldReward() + " Gold");
        rewardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        rewardLabel.setTextFill(Color.GOLD);
        
        if (canClaim) {
            Button claimBtn = new Button("Claim!");
            claimBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
            claimBtn.setOnAction(e -> {
                int reward = QuestManager.getInstance().claimAchievementReward(achievement.getType());
                if (reward > 0) {
                    mainApp.showQuestsAchievements();
                }
            });
            rewardBox.getChildren().addAll(rewardLabel, claimBtn);
        } else if (achievement.isRewardClaimed()) {
            Label claimedLabel = new Label("Claimed");
            claimedLabel.setTextFill(Color.LIGHTGREEN);
            rewardBox.getChildren().add(claimedLabel);
        } else {
            rewardBox.getChildren().add(rewardLabel);
        }
        
        row.getChildren().addAll(statusLabel, textBox, progressBox, rewardBox);
        return row;
    }
    
    private VBox createQuestsPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(50));
        placeholder.setStyle("-fx-background-color: #34495e;");
        
        Label label = new Label("Daily Quests");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.setTextFill(Color.WHITE);
        
        Label comingSoon = new Label("Coming Soon!");
        comingSoon.setFont(Font.font("Arial", 14));
        comingSoon.setTextFill(Color.LIGHTGRAY);
        
        placeholder.getChildren().addAll(label, comingSoon);
        return placeholder;
    }
}
