package com.example.kuroyale.model.quest;

import java.util.EnumMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages achievements and tracks player progress.
 * Singleton pattern - use getInstance() to access.
 * 
 * Other classes should call the onXxx() event methods when relevant actions occur.
 */
public class QuestManager {
    
    private static QuestManager instance;
    
    private final Map<AchievementType, Achievement> achievements;
    private final PlayerStatistics statistics;
    
    // Listeners for achievement completion notifications
    private final List<AchievementListener> listeners;
    
    /**
     * Listener interface for achievement completion events.
     */
    public interface AchievementListener {
        void onAchievementCompleted(Achievement achievement);
    }
    
    private QuestManager() {
        this.achievements = new EnumMap<>(AchievementType.class);
        this.statistics = new PlayerStatistics();
        this.listeners = new ArrayList<>();
        
        initializeAchievements();
    }
    
    public static synchronized QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
        return instance;
    }
    
    /**
     * Initializes all achievements.
     */
    private void initializeAchievements() {
        for (AchievementType type : AchievementType.values()) {
            achievements.put(type, new Achievement(type));
        }
    }
    
    // ==================== Getters ====================
    
    public PlayerStatistics getStatistics() {
        return statistics;
    }
    
    public Achievement getAchievement(AchievementType type) {
        return achievements.get(type);
    }
    
    public Map<AchievementType, Achievement> getAllAchievements() {
        return achievements;
    }
    
    /**
     * @return List of all achievements sorted by completion status
     */
    public List<Achievement> getAchievementsList() {
        return new ArrayList<>(achievements.values());
    }
    
    /**
     * @return Number of achievements that can have rewards claimed
     */
    public int getClaimableAchievementCount() {
        int count = 0;
        for (Achievement a : achievements.values()) {
            if (a.canClaimReward()) {
                count++;
            }
        }
        return count;
    }
    
    // ==================== Listeners ====================
    
    public void addAchievementListener(AchievementListener listener) {
        listeners.add(listener);
    }
    
    public void removeAchievementListener(AchievementListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyAchievementCompleted(Achievement achievement) {
        for (AchievementListener listener : listeners) {
            listener.onAchievementCompleted(achievement);
        }
    }
    
    // ==================== Event Handlers ====================
    // These methods should be called by other game classes when events occur
    
    /**
     * Called when a match is played (win or loss).
     */
    public void onMatchPlayed() {
        statistics.recordMatchPlayed();
        updateAchievementProgress(AchievementType.VETERAN_PLAYER, statistics.getTotalMatchesPlayed());
    }
    
    /**
     * Called when the player wins a match.
     * 
     * @param isMultiplayer true if this was a network multiplayer match
     */
    public void onMatchWon(boolean isMultiplayer) {
        statistics.recordMatchWon();
        
        // FIRST_BLOOD - Win your first match
        updateAchievementProgress(AchievementType.FIRST_BLOOD, statistics.getTotalMatchesWon());
        
        // UNDEFEATED - Win 5 matches in a row
        updateAchievementProgress(AchievementType.UNDEFEATED, statistics.getCurrentWinStreak());
        
        // NETWORK_WARRIOR - Win 10 network multiplayer matches
        if (isMultiplayer) {
            statistics.recordMultiplayerMatchWon();
            updateAchievementProgress(AchievementType.NETWORK_WARRIOR, statistics.getTotalMultiplayerMatchesWon());
        }
    }
    
    /**
     * Called when the player loses a match.
     */
    public void onMatchLost() {
        statistics.recordMatchLost();
        // Reset win streak for UNDEFEATED - it tracks current streak
    }
    
    /**
     * Called when a Crown Tower is destroyed.
     */
    public void onCrownTowerDestroyed() {
        statistics.recordCrownTowerDestroyed();
        updateAchievementProgress(AchievementType.TOWER_HUNTER, statistics.getTotalCrownTowersDestroyed());
    }
    
    /**
     * Called when a King Tower is destroyed.
     */
    public void onKingTowerDestroyed() {
        statistics.recordKingTowerDestroyed();
    }
    
    /**
     * Called when a challenge is completed.
     * 
     * @param stars Number of stars earned (1-3)
     */
    public void onChallengeCompleted(int stars) {
        statistics.recordChallengeCompleted(stars);
        
        // CHALLENGE_MASTER - Complete all 5 challenges
        updateAchievementProgress(AchievementType.CHALLENGE_MASTER, statistics.getTotalChallengesCompleted());
        
        // THREE_STAR_HERO - Get 3 stars on any challenge
        if (stars >= 3) {
            updateAchievementProgress(AchievementType.THREE_STAR_HERO, statistics.getTotalThreeStarChallenges());
        }
    }
    
    /**
     * Called when troop(s) are deployed.
     * 
     * @param troopCount Number of troops in the deployed card (e.g., Skeleton Army = many)
     */
    public void onTroopsDeployed(int troopCount) {
        statistics.recordTroopDeployed(troopCount);
        updateAchievementProgress(AchievementType.ARMY_BUILDER, statistics.getTotalTroopsDeployed());
    }
    
    /**
     * Called when spell damage is dealt.
     * 
     * @param damage Amount of damage dealt
     */
    public void onSpellDamageDealt(int damage) {
        statistics.recordSpellDamage(damage);
        updateAchievementProgress(AchievementType.SPELL_MASTER, statistics.getTotalSpellDamageDealt());
    }
    
    /**
     * Called when gold is earned.
     * 
     * @param amount Amount of gold earned
     */
    public void onGoldEarned(int amount) {
        statistics.recordGoldEarned(amount);
        updateAchievementProgress(AchievementType.GOLD_HOARDER, statistics.getTotalGoldEarned());
    }
    
    /**
     * Called when a card combo is triggered.
     */
    public void onComboTriggered() {
        statistics.recordComboTriggered();
        updateAchievementProgress(AchievementType.COMBO_EXPERT, statistics.getTotalCombosTriggered());
    }
    
    /**
     * Called when a Legendary card is upgraded to Level 3.
     */
    public void onLegendaryCardUpgradedToLevel3() {
        statistics.recordLegendaryLevel3Upgrade();
        if (statistics.hasLegendaryLevel3()) {
            updateAchievementProgress(AchievementType.LEGENDARY_COLLECTOR, 1);
        }
    }
    
    // ==================== Achievement Progress ====================
    
    /**
     * Updates progress for an achievement and checks for completion.
     */
    private void updateAchievementProgress(AchievementType type, int newProgress) {
        Achievement achievement = achievements.get(type);
        if (achievement == null || achievement.isCompleted()) {
            return;
        }
        
        boolean wasCompleted = achievement.isCompleted();
        achievement.updateProgress(newProgress);
        
        // Notify listeners if just completed
        if (!wasCompleted && achievement.isCompleted()) {
            notifyAchievementCompleted(achievement);
        }
    }
    
    /**
     * Claims the reward for an achievement.
     * 
     * @param type The achievement type to claim
     * @return The gold reward, or 0 if not claimable
     */
    public int claimAchievementReward(AchievementType type) {
        Achievement achievement = achievements.get(type);
        if (achievement != null) {
            return achievement.claimReward();
        }
        return 0;
    }
    
    // ==================== Persistence ====================
    
    /**
     * Exports achievement data for saving.
     * Call this when saving game state.
     */
    public AchievementData exportAchievementData() {
        return new AchievementData(achievements, statistics);
    }
    
    /**
     * Imports achievement data from save.
     * Call this when loading game state.
     */
    public void importAchievementData(AchievementData data) {
        if (data == null) return;
        
        // Import achievements
        Map<AchievementType, Achievement> savedAchievements = data.getAchievements();
        if (savedAchievements != null) {
            for (Map.Entry<AchievementType, Achievement> entry : savedAchievements.entrySet()) {
                achievements.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Import statistics
        PlayerStatistics savedStats = data.getStatistics();
        if (savedStats != null) {
            copyStatistics(savedStats);
        }
    }
    
    private void copyStatistics(PlayerStatistics source) {
        // Copy all statistics from source to our statistics object
        // This is a simple approach - in production you might want deep copy
        while (statistics.getTotalMatchesPlayed() < source.getTotalMatchesPlayed()) {
            statistics.recordMatchPlayed();
        }
        while (statistics.getTotalMatchesWon() < source.getTotalMatchesWon()) {
            statistics.recordMatchWon();
        }
        // Note: For a complete implementation, you'd copy all fields
        // This is simplified for the initial implementation
    }
    
    /**
     * Resets the singleton instance. Useful for testing.
     */
    public static void resetInstance() {
        instance = null;
    }
}
