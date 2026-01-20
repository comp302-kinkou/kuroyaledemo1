package com.example.kuroyale.model;

import java.io.Serializable;

public class CardProgression implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ADT Overview: Mutable, represents the progression state of a card in the
     * game.
     * Manages level, total gold spent, and rarity.
     * 
     * Abstraction Function:
     * AF(c) = A card progression p where
     * p.name = c.cardName
     * p.level = c.level
     * p.rarity = c.rarity
     * p.goldSpent = c.totalGoldSpent
     * 
     * Rep Invariant:
     * cardName != null
     * rarity != null
     * 1 <= level <= 3
     * totalGoldSpent >= 0
     */

    private String cardName;
    private int level; // 1, 2, or 3
    private CardRarity rarity;
    private int totalGoldSpent;

    public CardProgression(String cardName, CardRarity rarity) {
        this.cardName = cardName;
        this.rarity = rarity;
        this.level = 1; // All cards start at Level 1
        this.totalGoldSpent = 0;
        assert repOk();
    }

    public CardProgression(String cardName, CardRarity rarity, int level) {
        this.cardName = cardName;
        this.rarity = rarity;
        this.level = Math.max(1, Math.min(3, level)); // Clamp between 1 and 3
        this.totalGoldSpent = 0;
        assert repOk();
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

    /**
     * Checks if the representation invariant holds.
     * 
     * @return true if the rep is valid, false otherwise.
     */
    public boolean repOk() {
        if (cardName == null)
            return false;
        if (rarity == null)
            return false;
        if (level < 1 || level > 3)
            return false;
        if (totalGoldSpent < 0)
            return false;
        return true;
    }

    // Upgrades the card to the next level if possible.
    public boolean upgrade() {
        if (canUpgrade()) {
            level++;
            assert repOk();
            return true;
        }
        return false;
    }

    public void addGoldSpent(int amount) {
        this.totalGoldSpent += amount;
        assert repOk();
    }

    public int getTotalGoldSpent() {
        return totalGoldSpent;
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
            case 1:
                return 1.0;
            case 2:
                return 1.1;
            case 3:
                return 1.2;
            default:
                return 1.0;
        }
    }

    // Applies level-based stat bonuses to a base value.
    public double applyLevelBonus(double baseValue) {
        return baseValue * getStatMultiplier();
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
