package com.example.kuroyale.model.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.kuroyale.model.persistence.ChallengeData;

public class ChallengeManager {
    private static ChallengeManager instance;
    private final List<Challenge> challenges;
    private final Map<String, Boolean> unlockedChallenges;
    private final Map<String, Boolean> completedChallenges;
    private final Map<String, Integer> starsEarned;
    private int totalGold;

    private ChallengeManager() {
        challenges = new ArrayList<>();
        unlockedChallenges = new HashMap<>();
        completedChallenges = new HashMap<>();
        starsEarned = new HashMap<>();

        initializeChallenges();
    }

    public static synchronized ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }
        return instance;
    }

    private void initializeChallenges() {
        // Add challenges in order
        challenges.add(new SwarmMasterChallenge());
        challenges.add(new SpellBarrageChallenge());
        challenges.add(new NoBuildingsChallenge());
        challenges.add(new BudgetBattleChallenge());
        challenges.add(new TankRushChallenge());

        // Unlock first challenge by default
        if (!challenges.isEmpty()) {
            unlockChallenge(challenges.get(0).getName());
        }
    }

    public List<Challenge> getAllChallenges() {
        return challenges;
    }

    public boolean isUnlocked(String challengeName) {
        return unlockedChallenges.getOrDefault(challengeName, false);
    }

    public boolean isCompleted(String challengeName) {
        return completedChallenges.getOrDefault(challengeName, false);
    }

    public int getStars(String challengeName) {
        return starsEarned.getOrDefault(challengeName, 0);
    }

    public void unlockChallenge(String challengeName) {
        unlockedChallenges.put(challengeName, true);
    }

    public int getTotalGold() {
        return totalGold;
    }

    public void completeChallenge(String challengeName, int stars) {
        // Award gold if first time completion
        if (!isCompleted(challengeName)) {
            for (Challenge c : challenges) {
                if (c.getName().equals(challengeName)) {
                    totalGold += c.getReward();
                    break;
                }
            }
        }

        completedChallenges.put(challengeName, true);

        // Update stars if new record is higher
        int currentStars = getStars(challengeName);
        if (stars > currentStars) {
            starsEarned.put(challengeName, stars);
        }

        // Unlock next challenge
        for (int i = 0; i < challenges.size() - 1; i++) {
            if (challenges.get(i).getName().equals(challengeName)) {
                unlockChallenge(challenges.get(i + 1).getName());
                break;
            }
        }
    }

    public ChallengeData exportData() {
        ChallengeData data = new ChallengeData();
        data.setUnlockedChallenges(new HashMap<>(unlockedChallenges));
        data.setCompletedChallenges(new HashMap<>(completedChallenges));
        data.setStarsEarned(new HashMap<>(starsEarned));
        // Note: bestTimes is not currently tracked in ChallengeManager but added to
        // Data model for future proofing
        return data;
    }

    public void importData(ChallengeData data) {
        if (data == null)
            return;
        this.unlockedChallenges.clear();
        this.unlockedChallenges.putAll(data.getUnlockedChallenges());

        this.completedChallenges.clear();
        this.completedChallenges.putAll(data.getCompletedChallenges());

        this.starsEarned.clear();
        this.starsEarned.putAll(data.getStarsEarned());
    }
}
