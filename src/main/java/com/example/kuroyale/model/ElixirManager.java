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

    public int getElixir() {
        return (int) currentElixir;
    }

    public double getExactElixir() {
        return currentElixir;
    }
}
