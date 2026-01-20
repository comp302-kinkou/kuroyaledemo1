package com.example.kuroyale.model.quest;

import java.io.Serializable;

/**
 * Tracks lifetime player statistics for achievement progress.
 * All statistics are cumulative and persist across sessions.
 */
public class PlayerStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Match statistics
    private int totalMatchesPlayed;
    private int totalMatchesWon;
    private int currentWinStreak;
    private int longestWinStreak;
    
    // Tower statistics
    private int totalCrownTowersDestroyed;
    private int totalKingTowersDestroyed;
    
    // Card statistics
    private int totalTroopsDeployed;
    private int totalSpellsPlayed;
    private int totalBuildingsPlayed;
    private int totalCardsPlayed;
    
    // Damage statistics
    private int totalSpellDamageDealt;
    
    // Economy statistics
    private int totalGoldEarned;
    private int totalElixirSpent;
    
    // Challenge statistics
    private int totalChallengesCompleted;
    private int totalThreeStarChallenges;
    
    // Multiplayer statistics
    private int totalMultiplayerMatchesWon;
    
    // Combo statistics
    private int totalCombosTriggered;
    
    // Card upgrade tracking
    private boolean hasLegendaryLevel3;
    
    public PlayerStatistics() {
        // Initialize all to 0/false (default values)
    }
    
    // ==================== Match Statistics ====================
    
    public int getTotalMatchesPlayed() {
        return totalMatchesPlayed;
    }
    
    public int getTotalMatchesWon() {
        return totalMatchesWon;
    }
    
    public int getCurrentWinStreak() {
        return currentWinStreak;
    }
    
    public int getLongestWinStreak() {
        return longestWinStreak;
    }
    
    public void recordMatchPlayed() {
        totalMatchesPlayed++;
    }
    
    public void recordMatchWon() {
        totalMatchesWon++;
        currentWinStreak++;
        if (currentWinStreak > longestWinStreak) {
            longestWinStreak = currentWinStreak;
        }
    }
    
    public void recordMatchLost() {
        currentWinStreak = 0;
    }
    
    // ==================== Tower Statistics ====================
    
    public int getTotalCrownTowersDestroyed() {
        return totalCrownTowersDestroyed;
    }
    
    public int getTotalKingTowersDestroyed() {
        return totalKingTowersDestroyed;
    }
    
    public void recordCrownTowerDestroyed() {
        totalCrownTowersDestroyed++;
    }
    
    public void recordKingTowerDestroyed() {
        totalKingTowersDestroyed++;
    }
    
    // ==================== Card Statistics ====================
    
    public int getTotalTroopsDeployed() {
        return totalTroopsDeployed;
    }
    
    public int getTotalSpellsPlayed() {
        return totalSpellsPlayed;
    }
    
    public int getTotalBuildingsPlayed() {
        return totalBuildingsPlayed;
    }
    
    public int getTotalCardsPlayed() {
        return totalCardsPlayed;
    }
    
    public void recordTroopDeployed(int count) {
        totalTroopsDeployed += count;
        totalCardsPlayed++;
    }
    
    public void recordSpellPlayed() {
        totalSpellsPlayed++;
        totalCardsPlayed++;
    }
    
    public void recordBuildingPlayed() {
        totalBuildingsPlayed++;
        totalCardsPlayed++;
    }
    
    // ==================== Damage Statistics ====================
    
    public int getTotalSpellDamageDealt() {
        return totalSpellDamageDealt;
    }
    
    public void recordSpellDamage(int damage) {
        totalSpellDamageDealt += damage;
    }
    
    // ==================== Economy Statistics ====================
    
    public int getTotalGoldEarned() {
        return totalGoldEarned;
    }
    
    public int getTotalElixirSpent() {
        return totalElixirSpent;
    }
    
    public void recordGoldEarned(int amount) {
        totalGoldEarned += amount;
    }
    
    public void recordElixirSpent(int amount) {
        totalElixirSpent += amount;
    }
    
    // ==================== Challenge Statistics ====================
    
    public int getTotalChallengesCompleted() {
        return totalChallengesCompleted;
    }
    
    public int getTotalThreeStarChallenges() {
        return totalThreeStarChallenges;
    }
    
    public void recordChallengeCompleted(int stars) {
        totalChallengesCompleted++;
        if (stars >= 3) {
            totalThreeStarChallenges++;
        }
    }
    
    // ==================== Multiplayer Statistics ====================
    
    public int getTotalMultiplayerMatchesWon() {
        return totalMultiplayerMatchesWon;
    }
    
    public void recordMultiplayerMatchWon() {
        totalMultiplayerMatchesWon++;
    }
    
    // ==================== Combo Statistics ====================
    
    public int getTotalCombosTriggered() {
        return totalCombosTriggered;
    }
    
    public void recordComboTriggered() {
        totalCombosTriggered++;
    }
    
    // ==================== Card Upgrade Tracking ====================
    
    public boolean hasLegendaryLevel3() {
        return hasLegendaryLevel3;
    }
    
    public void recordLegendaryLevel3Upgrade() {
        hasLegendaryLevel3 = true;
    }
}
