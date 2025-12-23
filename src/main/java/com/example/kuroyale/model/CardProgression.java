package com.example.kuroyale.model;

public class CardProgression {
    private String cardName;
    private int level; // 1, 2, or 3
    private CardRarity rarity;

    public CardProgression(String cardName, CardRarity rarity) {
        this.cardName = cardName;
        this.rarity = rarity;
        this.level = 1; // All cards start at Level 1
    }

    public CardProgression(String cardName, CardRarity rarity, int level) {
        this.cardName = cardName;
        this.rarity = rarity;
        this.level = Math.max(1, Math.min(3, level)); // Clamp between 1 and 3
    }

    public String getCardName() {
        return cardName;
    }

    public int getLevel() {
        return level;
    }

    public CardRarity getRarity() {
        return rarity;
    }

    // Upgrades the card to the next level if possible.
    public boolean upgrade() {
        if (level < 3) {
            level++;
            return true;
        }
        return false;
    }

    // Checks if the card can be upgraded further.
    public boolean canUpgrade() {
        return level < 3;
    }

    // Gets the cost to upgrade to the next level.
    public int getUpgradeCost() {
        if (level == 1) {
            return rarity.getLevel1To2Cost();
        } else if (level == 2) {
            return rarity.getLevel2To3Cost();
        }
        return 0;
    }

    // Calculates the stat multiplier for the current level.
    public double getStatMultiplier() {
        switch (level) {
            case 1: return 1.0;
            case 2: return 1.1;
            case 3: return 1.2;
            default: return 1.0;
        }
    }

    // Applies level-based stat bonuses to a base value.
    public double applyLevelBonus(double baseValue) {
        return baseValue * getStatMultiplier();
    }
}