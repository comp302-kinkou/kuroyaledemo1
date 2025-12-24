package com.example.kuroyale.model.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChallengeData implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Boolean> unlockedChallenges;
    private Map<String, Boolean> completedChallenges;
    private Map<String, Integer> starsEarned;
    private Map<String, Long> bestTimes; // Time in seconds or milliseconds

    public ChallengeData() {
        this.unlockedChallenges = new HashMap<>();
        this.completedChallenges = new HashMap<>();
        this.starsEarned = new HashMap<>();
        this.bestTimes = new HashMap<>();
    }

    public Map<String, Boolean> getUnlockedChallenges() {
        return unlockedChallenges;
    }

    public void setUnlockedChallenges(Map<String, Boolean> unlockedChallenges) {
        this.unlockedChallenges = unlockedChallenges;
    }

    public Map<String, Boolean> getCompletedChallenges() {
        return completedChallenges;
    }

    public void setCompletedChallenges(Map<String, Boolean> completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    public Map<String, Integer> getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(Map<String, Integer> starsEarned) {
        this.starsEarned = starsEarned;
    }

    public Map<String, Long> getBestTimes() {
        return bestTimes;
    }

    public void setBestTimes(Map<String, Long> bestTimes) {
        this.bestTimes = bestTimes;
    }
}
