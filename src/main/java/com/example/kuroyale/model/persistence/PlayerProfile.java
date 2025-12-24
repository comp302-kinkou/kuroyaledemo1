package com.example.kuroyale.model.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int totalGold;
    private Map<String, Integer> lifetimeStats;

    public PlayerProfile() {
        this.name = "Player";
        this.totalGold = 0;
        this.lifetimeStats = new HashMap<>();
        initializeStats();
    }

    public PlayerProfile(String name) {
        this.name = name;
        this.totalGold = 0;
        this.lifetimeStats = new HashMap<>();
        initializeStats();
    }

    private void initializeStats() {
        lifetimeStats.put("matchesPlayed", 0);
        lifetimeStats.put("wins", 0);
        lifetimeStats.put("losses", 0);
        lifetimeStats.put("towersDestroyed", 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public void addGold(int amount) {
        this.totalGold += amount;
    }

    public boolean spendGold(int amount) {
        if (totalGold >= amount) {
            totalGold -= amount;
            return true;
        }
        return false;
    }

    public int getStat(String key) {
        return lifetimeStats.getOrDefault(key, 0);
    }

    public void incrementStat(String key, int amount) {
        lifetimeStats.put(key, getStat(key) + amount);
    }

    public Map<String, Integer> getLifetimeStats() {
        return lifetimeStats;
    }
}
