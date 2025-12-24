package com.example.kuroyale.controller;

public class GameLoop {
    
    private GameController gameController;
    private boolean isRunning;
    private long lastUpdateTime;
    
    public GameLoop(GameController gameController) {
        this.gameController = gameController;
        this.isRunning = false;
        this.lastUpdateTime = 0;
    }
    
    /**
     * Updates the game state based on delta time.
     * @param currentTimeNanos Current time in nanoseconds (from JavaFX AnimationTimer)
     */
    public void update(long currentTimeNanos) {
        if (!isRunning) {
            return;
        }
        
        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTimeNanos;
            return;
        }
        
        double deltaTime = (currentTimeNanos - lastUpdateTime) / 1e9; // Convert to seconds
        lastUpdateTime = currentTimeNanos;
        
        gameController.update(deltaTime);
    }
    
    // Starts the game loop
    public void start() {
        isRunning = true;
        lastUpdateTime = 0;
    }
    
    // Stops the game loop
    public void stop() {
        isRunning = false;
        lastUpdateTime = 0;
    }
    
    // Checks if the game is running or not
    public boolean isRunning() {
        return isRunning;
    }
}
