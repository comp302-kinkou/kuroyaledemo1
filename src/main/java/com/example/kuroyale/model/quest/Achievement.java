package com.example.kuroyale.model.quest;

import java.io.Serializable;

/**
 * Represents a permanent achievement with progress tracking.
 * Achievements are one-time rewards for reaching milestones.
 * They track lifetime statistics and remain locked until conditions are met.
 */
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final AchievementType type;
    private int currentProgress;
    private boolean completed;
    private boolean rewardClaimed;
    
    /**
     * Creates a new achievement of the specified type.
     * 
     * @param type The type of achievement
     */
    public Achievement(AchievementType type) {
        this.type = type;
        this.currentProgress = 0;
        this.completed = false;
        this.rewardClaimed = false;
    }
    
    /**
     * @return The type of this achievement
     */
    public AchievementType getType() {
        return type;
    }
    
    /**
     * @return The display name of this achievement
     */
    public String getDisplayName() {
        return type.getDisplayName();
    }
    
    /**
     * @return The description of this achievement
     */
    public String getDescription() {
        return type.getDescription();
    }
    
    /**
     * @return The current progress towards completing this achievement
     */
    public int getCurrentProgress() {
        return currentProgress;
    }
    
    /**
     * @return The target value needed to complete this achievement
     */
    public int getTargetValue() {
        return type.getTargetValue();
    }
    
    /**
     * @return The gold reward for completing this achievement
     */
    public int getGoldReward() {
        return type.getGoldReward();
    }
    
    /**
     * @return True if the achievement is completed (progress >= target)
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * @return True if the reward has been claimed
     */
    public boolean isRewardClaimed() {
        return rewardClaimed;
    }
    
    /**
     * @return True if the achievement has been unlocked (progress > 0 or completed)
     */
    public boolean isUnlocked() {
        return currentProgress > 0 || completed;
    }
    
    /**
     * @return True if the reward can be claimed (completed but not yet claimed)
     */
    public boolean canClaimReward() {
        return completed && !rewardClaimed;
    }
    
    /**
     * Updates the progress for this achievement.
     * If the progress reaches or exceeds the target, marks as completed.
     * 
     * @param progress The new progress value
     */
    public void updateProgress(int progress) {
        this.currentProgress = progress;
        checkCompletion();
    }
    
    /**
     * Increments the progress by the specified amount.
     * 
     * @param amount The amount to add to current progress
     */
    public void incrementProgress(int amount) {
        this.currentProgress += amount;
        checkCompletion();
    }
    
    /**
     * Checks if the achievement should be marked as completed.
     */
    private void checkCompletion() {
        if (!completed && currentProgress >= type.getTargetValue()) {
            completed = true;
        }
    }
    
    /**
     * Claims the reward for this achievement.
     * 
     * @return The gold reward amount, or 0 if already claimed or not completed
     */
    public int claimReward() {
        if (canClaimReward()) {
            rewardClaimed = true;
            return type.getGoldReward();
        }
        return 0;
    }
    
    /**
     * @return The progress as a percentage (0.0 to 1.0)
     */
    public double getProgressPercentage() {
        if (type.getTargetValue() == 0) {
            return completed ? 1.0 : 0.0;
        }
        return Math.min(1.0, (double) currentProgress / type.getTargetValue());
    }
    
    /**
     * @return A formatted progress string (e.g., "5/50" or "Complete!")
     */
    public String getProgressText() {
        if (completed) {
            return rewardClaimed ? "Claimed" : "Complete!";
        }
        return currentProgress + "/" + type.getTargetValue();
    }
    
    @Override
    public String toString() {
        return String.format("Achievement[%s: %s (%s)]", 
            type.getDisplayName(), 
            getProgressText(),
            completed ? "COMPLETED" : "IN_PROGRESS");
    }
}
