package com.example.kuroyale.model.persistence;

import java.io.Serializable;

public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name = "Player";
    private int gold = 1000;

    // Lifetime Statistics
    private int totalMatchesPlayed;
    private int totalWins;
    private int totalLosses;
    private int totalTowersDestroyed;
    private int totalDamageDealt;

    public PlayerProfile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalGold() {
        return gold;
    }

    public void setTotalGold(int gold) {
        this.gold = gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public boolean spendGold(int amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    // Statistics Methods
    public void incrementMatchesPlayed() {
        this.totalMatchesPlayed++;
    }

    public void incrementWins() {
        this.totalWins++;
    }

    public void incrementLosses() {
        this.totalLosses++;
    }

    public void addTowersDestroyed(int count) {
        this.totalTowersDestroyed += count;
    }

    public void addDamageDealt(int amount) {
        this.totalDamageDealt += amount;
    }

    // Getters for statistics
    public int getTotalMatchesPlayed() {
        return totalMatchesPlayed;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public int getTotalTowersDestroyed() {
        return totalTowersDestroyed;
    }

    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }
}
