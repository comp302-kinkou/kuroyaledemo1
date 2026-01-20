package com.example.kuroyale.model.combo;

// Represents a visual effect for a combo that was triggered.
// Tracks the position, timing, and display information for combo animations.
public class ComboVisualEffect {
    private final double x;
    private final double y;
    private final long startTime;
    private final long duration; // Duration in milliseconds
    private final String comboName;
    private final ComboType comboType;
    
    // Animation properties
    private double scale = 1.0;
    private double alpha = 1.0;
    
    public ComboVisualEffect(double x, double y, String comboName, ComboType comboType, long startTime) {
        this.x = x;
        this.y = y;
        this.comboName = comboName;
        this.comboType = comboType;
        this.startTime = startTime;
        this.duration = 2000; // 2 seconds display time
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public String getComboName() {
        return comboName;
    }
    
    public ComboType getComboType() {
        return comboType;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getDuration() {
        return duration;
    }
    
    /**
     * Checks if this visual effect is still active at the given time.
     * @param currentTime Current time in milliseconds
     * @return true if the effect is still active, false if it has expired
     */
    public boolean isActive(long currentTime) {
        long elapsed = currentTime - startTime;
        return elapsed < duration;
    }
    
    /**
     * Gets the current animation scale based on elapsed time.
     * Creates a pulsing effect.
     * @param currentTime Current time in milliseconds
     * @return Scale value (1.0 to 1.5)
     */
    public double getScale(long currentTime) {
        long elapsed = currentTime - startTime;
        double progress = (double) elapsed / duration;
        
        // Pulse effect: scale from 1.0 to 1.5 and back
        if (progress < 0.3) {
            // Growing phase
            double phaseProgress = progress / 0.3;
            return 1.0 + (0.5 * phaseProgress);
        } else {
            // Shrinking phase
            double phaseProgress = (progress - 0.3) / 0.7;
            return 1.5 - (0.5 * phaseProgress);
        }
    }
    
    /**
     * Gets the current alpha (opacity) based on elapsed time.
     * Fades out towards the end.
     * @param currentTime Current time in milliseconds
     * @return Alpha value (0.0 to 1.0)
     */
    public double getAlpha(long currentTime) {
        long elapsed = currentTime - startTime;
        double progress = (double) elapsed / duration;
        
        // Fade out in the last 30% of duration
        if (progress > 0.7) {
            double fadeProgress = (progress - 0.7) / 0.3;
            return 1.0 - fadeProgress;
        }
        return 1.0;
    }
}