package com.example.kuroyale.model.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.kuroyale.model.quest.Quest;
import java.time.LocalDateTime;

public class QuestData implements Serializable {
    private static final long serialVersionUID = 2L; // Updated version for new field

    private List<String> dailyQuests; // IDs or Descriptions of active quests (legacy)
    private Map<String, Integer> questProgress; // QuestID -> Progress count
    private long lastQuestResetTimestamp;
    private Map<String, Boolean> achievements;
    private Map<String, Integer> achievementProgress;
    
    // New: Store actual Quest objects for full state persistence
    private List<Quest> quests;

    public QuestData() {
        this.dailyQuests = new ArrayList<>();
        this.questProgress = new HashMap<>();
        this.lastQuestResetTimestamp = 0;
        this.achievements = new HashMap<>();
        this.achievementProgress = new HashMap<>();
        this.quests = new ArrayList<>();
    // Placeholder for Quest logic
    private LocalDateTime lastResetTime;

    public QuestData() {
        this.lastResetTime = LocalDateTime.now();
    }

    public LocalDateTime getLastResetTime() {
        return lastResetTime;
    }

    public void setDailyQuests(List<String> dailyQuests) {
        this.dailyQuests = dailyQuests;
    }
    
    public List<Quest> getQuests() {
        return quests;
    }
    
    public void setQuests(List<Quest> quests) {
        this.quests = quests != null ? quests : new ArrayList<>();
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
    public void setLastResetTime(LocalDateTime time) {
        this.lastResetTime = time;
    }
}
