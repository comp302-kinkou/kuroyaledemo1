package com.example.kuroyale.model;

// Enum representing the rarity tiers for cards in the game.
public enum CardRarity {
    COMMON("Common", "Gray/White"),
    RARE("Rare", "Blue"),
    EPIC("Epic", "Purple"),
    LEGENDARY("Legendary", "Orange/Gold");

    private final String displayName;
    private final String borderColor;

    CardRarity(String displayName, String borderColor) {
        this.displayName = displayName;
        this.borderColor = borderColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBorderColor() {
        return borderColor;
    }        

    // Gets the upgrade cost for a specific level transition.
    public int getUpgradeCost(int fromLevel, int toLevel) {
        if (fromLevel == 1 && toLevel == 2) {
            return getLevel1To2Cost();
        } else if (fromLevel == 2 && toLevel == 3) {
            return getLevel2To3Cost();
        }
        return 0;
    }

    // Gets the cost to upgrade from Level 1 to Level 2.
    public int getLevel1To2Cost() {
        switch (this) {
            case COMMON: return 200;
            case RARE: return 400;
            case EPIC: return 800;
            case LEGENDARY: return 1500;
            default: return 0;
        }
    }

    // Gets the cost to upgrade from Level 2 to Level 3.
    public int getLevel2To3Cost() {
        switch (this) {
            case COMMON: return 500;
            case RARE: return 1000;
            case EPIC: return 2000;
            case LEGENDARY: return 4000;
            default: return 0;
        }
    }
}