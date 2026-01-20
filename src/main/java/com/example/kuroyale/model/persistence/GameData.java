package com.example.kuroyale.model.persistence;

import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.quest.AchievementData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;

    private PlayerProfile playerProfile;
    private QuestData questData;
    private List<CardProgression> cardProgressions;
<<<<<<< HEAD
    private ChallengeData challengeData;
    private AchievementData achievementData;

    public GameData() {
        this.playerProfile = new PlayerProfile();
        this.cardProgressions = new ArrayList<>();
        this.questData = new QuestData();
        this.challengeData = new ChallengeData();
        this.achievementData = new AchievementData();
=======
    private ChallengeData challengeData; // Flexible map for challenge persistence

    public GameData() {
        this.playerProfile = new PlayerProfile();
        this.questData = new QuestData();
        this.cardProgressions = new java.util.ArrayList<>();
        this.challengeData = new ChallengeData();
>>>>>>> c0638b4 (Refine persistence implementation and fix compilation errors)
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public QuestData getQuestData() {
        return questData;
    }

    public void setQuestData(QuestData questData) {
        this.questData = questData;
    }

    public List<CardProgression> getCardProgressions() {
        return cardProgressions;
    }

    public void setCardProgressions(List<CardProgression> cardProgressions) {
        this.cardProgressions = cardProgressions;
    }

    public ChallengeData getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
    }

    public AchievementData getAchievementData() {
        return achievementData;
    }

    public void setAchievementData(AchievementData achievementData) {
        this.achievementData = achievementData;
    }
}
