package com.example.kuroyale.model.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> dailyQuests; // IDs or Descriptions of active quests
    private Map<String, Integer> questProgress; // QuestID -> Progress count
    private long lastQuestResetTimestamp;
    private Map<String, Boolean> achievements;
    private Map<String, Integer> achievementProgress;

    public QuestData() {
        this.dailyQuests = new ArrayList<>();
        this.questProgress = new HashMap<>();
        this.lastQuestResetTimestamp = 0;
        this.achievements = new HashMap<>();
        this.achievementProgress = new HashMap<>();
    }

    public List<String> getDailyQuests() {
        return dailyQuests;
    }

    public void setDailyQuests(List<String> dailyQuests) {
        this.dailyQuests = dailyQuests;
    }

    public int getQuestProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public void updateQuestProgress(String questId, int progress) {
        questProgress.put(questId, progress);
    }

    public long getLastQuestResetTimestamp() {
        return lastQuestResetTimestamp;
    }

    public void setLastQuestResetTimestamp(long lastQuestResetTimestamp) {
        this.lastQuestResetTimestamp = lastQuestResetTimestamp;
    }

    public boolean isAchievementCompleted(String achievementId) {
        return achievements.getOrDefault(achievementId, false);
    }

    public void completeAchievement(String achievementId) {
        achievements.put(achievementId, true);
    }

    public int getAchievementProgress(String achievementId) {
        return achievementProgress.getOrDefault(achievementId, 0);
    }

    public void updateAchievementProgress(String achievementId, int progress) {
        achievementProgress.put(achievementId, progress);
    }
}
