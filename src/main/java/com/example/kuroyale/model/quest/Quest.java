package com.example.kuroyale.model.quest;

import java.io.Serializable;

/**
 * Represents a daily quest that resets every 24 hours.
 * Tracks progress toward a target and can be claimed when complete.
 */
public class Quest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final QuestType type;
    private int currentProgress;
    private boolean rewardClaimed;
    private long assignedTimestamp; // When this quest was assigned

    public Quest(QuestType type) {
        this.type = type;
        this.currentProgress = 0;
        this.rewardClaimed = false;
        this.assignedTimestamp = System.currentTimeMillis();
    }

    public QuestType getType() {
        return type;
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }

    public String getDescription() {
        return type.getDescription();
    }

    public int getTargetValue() {
        return type.getTargetValue();
    }

    public int getGoldReward() {
        return type.getGoldReward();
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void addProgress(int amount) {
        if (!rewardClaimed) {
            this.currentProgress = Math.min(currentProgress + amount, type.getTargetValue());
        }
    }

    public void setProgress(int progress) {
        if (!rewardClaimed) {
            this.currentProgress = Math.min(progress, type.getTargetValue());
        }
    }

    public boolean isCompleted() {
        return currentProgress >= type.getTargetValue();
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public boolean canClaimReward() {
        return isCompleted() && !rewardClaimed;
    }

    public int claimReward() {
        if (canClaimReward()) {
            rewardClaimed = true;
            return type.getGoldReward();
        }
        return 0;
    }

    public double getProgressPercentage() {
        return (double) currentProgress / type.getTargetValue();
    }

    public String getProgressText() {
        return currentProgress + " / " + type.getTargetValue();
    }

    public long getAssignedTimestamp() {
        return assignedTimestamp;
    }

    public void setAssignedTimestamp(long timestamp) {
        this.assignedTimestamp = timestamp;
    }

    /**
     * Checks if this quest has expired (24 hours since assignment).
     */
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        long twentyFourHours = 24 * 60 * 60 * 1000L;
        return (now - assignedTimestamp) >= twentyFourHours;
    }

    /**
     * Gets the remaining time in milliseconds until this quest expires.
     */
    public long getRemainingTimeMs() {
        long now = System.currentTimeMillis();
        long twentyFourHours = 24 * 60 * 60 * 1000L;
        long elapsed = now - assignedTimestamp;
        return Math.max(0, twentyFourHours - elapsed);
    }

    /**
     * Gets remaining time as formatted string (HH:MM:SS).
     */
    public String getRemainingTimeFormatted() {
        long remaining = getRemainingTimeMs();
        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (remaining % (60 * 1000)) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
