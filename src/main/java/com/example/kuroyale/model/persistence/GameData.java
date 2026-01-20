package com.example.kuroyale.model.persistence;

import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.quest.AchievementData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;

    private PlayerProfile playerProfile;
    private List<CardProgression> cardProgressions;
    private QuestData questData;
    private ChallengeData challengeData;
    private AchievementData achievementData;

    public GameData() {
        this.playerProfile = new PlayerProfile();
        this.cardProgressions = new ArrayList<>();
        this.questData = new QuestData();
        this.challengeData = new ChallengeData();
        this.achievementData = new AchievementData();
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public List<CardProgression> getCardProgressions() {
        return cardProgressions;
    }

    public void setCardProgressions(List<CardProgression> cardProgressions) {
        this.cardProgressions = cardProgressions;
    }

    public QuestData getQuestData() {
        return questData;
    }

    public void setQuestData(QuestData questData) {
        this.questData = questData;
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
