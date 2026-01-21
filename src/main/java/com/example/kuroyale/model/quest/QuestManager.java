package com.example.kuroyale.model.quest;

import java.util.EnumMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages achievements and daily quests.
 * <p>
 * <b>Singleton Pattern:</b> This class ensures only one instance exists to
 * track global
 * player progress and achievements.
 * </p>
 * <p>
 * <b>Observer Pattern:</b> Uses the Observer pattern to notify listeners
 * ({@link AchievementListener})
 * when achievements are completed.
 * </p>
 */
public class QuestManager {

    private static QuestManager instance;

    private final Map<AchievementType, Achievement> achievements;
    private final PlayerStatistics statistics;
    private final List<AchievementListener> listeners;

    // Daily Quests
    private static final int DAILY_QUEST_COUNT = 3;
    private List<Quest> dailyQuests;
    private long lastQuestResetTime;
    private final Random random;

    public interface AchievementListener {
        void onAchievementCompleted(Achievement achievement);
    }

    private QuestManager() {
        this.achievements = new EnumMap<>(AchievementType.class);
        this.statistics = new PlayerStatistics();
        this.listeners = new ArrayList<>();
        this.dailyQuests = new ArrayList<>();
        this.random = new Random();
        this.lastQuestResetTime = 0;

        initializeAchievements();
        checkAndRefreshDailyQuests();
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
            // Track multiplayer win quests
            addQuestProgress(QuestType.WIN_MULTIPLAYER, 1);
        }

        // Track for daily quests
        addQuestProgress(QuestType.WIN_MATCHES, 1);
        addQuestProgress(QuestType.WIN_STREAK, 1);
    }

    /**
     * Called when the player loses a match.
     */
    public void onMatchLost() {
        statistics.recordMatchLost();
        // Reset win streak for UNDEFEATED - it tracks current streak
        // Reset WIN_STREAK quest progress on loss
        updateQuestProgress(QuestType.WIN_STREAK, 0);
    }

    /**
     * Called when a Crown Tower is destroyed.
     */
    public void onCrownTowerDestroyed() {
        statistics.recordCrownTowerDestroyed();
        updateAchievementProgress(AchievementType.TOWER_HUNTER, statistics.getTotalCrownTowersDestroyed());
        // Track for daily quests
        addQuestProgress(QuestType.DESTROY_TOWERS, 1);
    }

    /**
     * Called when a King Tower is destroyed.
     */
    public void onKingTowerDestroyed() {
        statistics.recordKingTowerDestroyed();
        // Track for DESTROY_KING quest
        addQuestProgress(QuestType.DESTROY_KING, 1);
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

        // Track for daily quests
        addQuestProgress(QuestType.COMPLETE_CHALLENGES, 1);
    }

    /**
     * Called when troop(s) are deployed.
     * 
     * @param troopCount Number of troops in the deployed card (e.g., Skeleton Army
     *                   = many)
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
        // Track for daily quests
        addQuestProgress(QuestType.SPELL_DAMAGE, damage);
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
            int reward = achievement.claimReward();
            if (reward > 0) {
                // Add gold to player's profile
                com.example.kuroyale.controller.GameController.getInstance()
                        .getPlayerProfile().addGold(reward);
                // Auto-save after claiming
                com.example.kuroyale.controller.GameController.getInstance().saveGame();
            }
            return reward;
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
        if (data == null)
            return;

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

    // ==================== Daily Quest Management ====================

    /**
     * Checks if quests need to be refreshed and generates new ones if needed.
     */
    public void checkAndRefreshDailyQuests() {
        boolean needsRefresh = false;

        if (dailyQuests.isEmpty()) {
            needsRefresh = true;
        } else {
            // Check if any quest is expired (24+ hours old)
            for (Quest quest : dailyQuests) {
                if (quest.isExpired()) {
                    needsRefresh = true;
                    break;
                }
            }
        }

        if (needsRefresh) {
            generateNewDailyQuests();
        }
    }

    /**
     * Generates 3 new random daily quests.
     */
    private void generateNewDailyQuests() {
        dailyQuests.clear();
        lastQuestResetTime = System.currentTimeMillis();

        QuestType[] allTypes = QuestType.values();
        List<QuestType> availableTypes = new ArrayList<>();
        for (QuestType type : allTypes) {
            availableTypes.add(type);
        }

        // Pick 3 random unique quest types
        for (int i = 0; i < DAILY_QUEST_COUNT && !availableTypes.isEmpty(); i++) {
            int index = random.nextInt(availableTypes.size());
            QuestType selectedType = availableTypes.remove(index);
            dailyQuests.add(new Quest(selectedType));
        }

        System.out.println("New daily quests generated!");
    }

    /**
     * Gets the current daily quests.
     */
    public List<Quest> getDailyQuests() {
        checkAndRefreshDailyQuests();
        return new ArrayList<>(dailyQuests);
    }

    /**
     * Updates progress for quests of a specific type.
     */
    public void updateQuestProgress(QuestType type, int progress) {
        for (Quest quest : dailyQuests) {
            if (quest.getType() == type && !quest.isRewardClaimed()) {
                quest.setProgress(progress);
            }
        }
    }

    /**
     * Adds progress to quests of a specific type.
     */
    public void addQuestProgress(QuestType type, int amount) {
        for (Quest quest : dailyQuests) {
            if (quest.getType() == type && !quest.isRewardClaimed()) {
                quest.addProgress(amount);
            }
        }
    }

    /**
     * Claims reward for a daily quest.
     */
    public int claimQuestReward(int questIndex) {
        if (questIndex >= 0 && questIndex < dailyQuests.size()) {
            Quest quest = dailyQuests.get(questIndex);
            int reward = quest.claimReward();
            if (reward > 0) {
                // Add gold to player's profile
                com.example.kuroyale.controller.GameController.getInstance()
                        .getPlayerProfile().addGold(reward);
                // Auto-save after claiming
                com.example.kuroyale.controller.GameController.getInstance().saveGame();
            }
            return reward;
        }
        return 0;
    }

    /**
     * Gets count of claimable daily quests.
     */
    public int getClaimableQuestCount() {
        int count = 0;
        for (Quest quest : dailyQuests) {
            if (quest.canClaimReward()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Imports daily quests from save data.
     */
    public void importDailyQuests(List<Quest> quests, long resetTime) {
        if (quests != null && !quests.isEmpty()) {
            this.dailyQuests = new ArrayList<>(quests);
            this.lastQuestResetTime = resetTime;
        }
        checkAndRefreshDailyQuests();
    }

    /**
     * Exports daily quests for saving.
     */
    public List<Quest> exportDailyQuests() {
        return new ArrayList<>(dailyQuests);
    }

    public long getLastQuestResetTime() {
        return lastQuestResetTime;
    }
}
