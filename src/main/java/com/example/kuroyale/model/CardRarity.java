package com.example.kuroyale.model;

public enum CardRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY;

    public String getDisplayName() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public int getLevel1To2Cost() {
        switch (this) {
            case COMMON:
                return 200;
            case RARE:
                return 400;
            case EPIC:
                return 800;
            case LEGENDARY:
                return 1500;
            default:
                return 0;
        }
    }

    public int getLevel2To3Cost() {
        switch (this) {
            case COMMON:
                return 500;
            case RARE:
                return 1000;
            case EPIC:
                return 2000;
            case LEGENDARY:
                return 4000;
            default:
                return 0;
        }
    }
}