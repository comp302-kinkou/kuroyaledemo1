package com.example.kuroyale.model;

public class Card {
    private String name;
    private int elixirCost;
    private String type; // TROOP, SPELL, BUILDING
    private double range;
    private double damage;
    private double hitSpeed;
    private double speed;
    private double health;
    private String imagePath;

    public Card(String name, int elixirCost, String type, double range, double damage, double hitSpeed, double speed,
            double health, String imagePath) {
        this.name = name;
        this.elixirCost = elixirCost;
        this.type = type;
        this.range = range;
        this.damage = damage;
        this.hitSpeed = hitSpeed;
        this.speed = speed;
        this.health = health;
        this.imagePath = imagePath;
    }

    // Simplified constructor for backward compatibility (optional, or update calls)
    public Card(String name, int elixirCost) {
        this(name, elixirCost, "TROOP", 1.0, 100, 1.0, 1.0, 500, "");
    }

    public String getName() {
        return name;
    }

    public int getElixirCost() {
        return elixirCost;
    }

    public String getType() {
        return type;
    }

    public double getRange() {
        return range;
    }

    public double getDamage() {
        return damage;
    }

    public double getHitSpeed() {
        return hitSpeed;
    }

    public double getSpeed() {
        return speed;
    }

    public double getHealth() {
        return health;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return name + " (cost " + elixirCost + ")";
    }
}
