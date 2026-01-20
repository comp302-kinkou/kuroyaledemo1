package com.example.kuroyale.model.quest;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Data transfer object for persisting achievement state.
 * Used for saving/loading achievement progress.
 */
public class AchievementData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<AchievementType, Achievement> achievements;
    private PlayerStatistics statistics;
    
    public AchievementData() {
        this.achievements = new EnumMap<>(AchievementType.class);
        this.statistics = new PlayerStatistics();
    }
    
    public AchievementData(Map<AchievementType, Achievement> achievements, PlayerStatistics statistics) {
        this.achievements = new EnumMap<>(achievements);
        this.statistics = statistics;
    }
    
    public Map<AchievementType, Achievement> getAchievements() {
        return achievements;
    }
    
    public void setAchievements(Map<AchievementType, Achievement> achievements) {
        this.achievements = achievements;
    }
    
    public PlayerStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(PlayerStatistics statistics) {
        this.statistics = statistics;
    }
}
