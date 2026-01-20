package com.example.kuroyale.model;

public class ElixirManager {
    private double currentElixir;
    private int maxElixir = 10;
    private double regenerationRate = 0.5; // Elixir per second (approx 2.8s for 1 elixir usually, but let's speed it up
                                           // or stick to standard)
    // Standard is 1 elixir every 2.8 seconds -> ~0.35 per sec. Let's use 0.5 for
    // faster testing.

    public ElixirManager() {
        this.currentElixir = 5; // Start with 5
    }

    public void update(double deltaTime) {
        if (currentElixir < maxElixir) {
            currentElixir += regenerationRate * deltaTime;
            if (currentElixir > maxElixir) {
                currentElixir = maxElixir;
            }
        }
    }

    public boolean spendElixir(int amount) {
        if (currentElixir >= amount) {
            currentElixir -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Spends elixir and tracks it for quests (player only method).
     */
    public boolean spendElixirWithTracking(int amount) {
        if (spendElixir(amount)) {
            com.example.kuroyale.model.quest.QuestManager.getInstance()
                .addQuestProgress(com.example.kuroyale.model.quest.QuestType.SPEND_ELIXIR, amount);
            return true;
        }
        return false;
    }

    /**
     * Spends elixir regardless of current amount. Used for syncing remote player
     * moves.
     */
    public void forceSpendElixir(int amount) {
        currentElixir -= amount;
        // Optional: Clamp to 0 if we don't want negative elixir on client side,
        // effectively treating it as "if remote says they played, they played".
        // But allowing negative helps track if we are severely desynced.
    }

    // Adds elixir (e.g., from combo refunds).
    // Caps at maxElixir.
    public void addElixir(int amount) {
        currentElixir += amount;
        if (currentElixir > maxElixir) {
            currentElixir = maxElixir;
        }
    }

    public int getElixir() {
        return (int) currentElixir;
    }

    public double getExactElixir() {
        return currentElixir;
    }

    public void setRegenerationRate(double rate) {
        this.regenerationRate = rate;
    }
}
