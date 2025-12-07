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
    private TransportType transportType;
    private TargetType targetType;

    public Card(String name, int elixirCost, String type, double range, double damage, double hitSpeed, double speed,
            double health, String imagePath, TransportType transportType, TargetType targetType) {
        this.name = name;
        this.elixirCost = elixirCost;
        this.type = type;
        this.range = range;
        this.damage = damage;
        this.hitSpeed = hitSpeed;
        this.speed = speed;
        this.health = health;
        this.imagePath = imagePath;
        this.transportType = transportType;
        this.targetType = targetType;
    }

    // Simplified constructor for backward compatibility (defaults to GROUND/GROUND)
    public Card(String name, int elixirCost) {
        this(name, elixirCost, "TROOP", 1.0, 100, 1.0, 1.0, 500, "", TransportType.GROUND, TargetType.GROUND);
    }

    // Backward compatibility for existing full constructor (defaults to
    // GROUND/GROUND)
    public Card(String name, int elixirCost, String type, double range, double damage, double hitSpeed, double speed,
            double health, String imagePath) {
        this(name, elixirCost, type, range, damage, hitSpeed, speed, health, imagePath, TransportType.GROUND,
                TargetType.GROUND);
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

    public TransportType getTransportType() {
        return transportType;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    @Override
    public String toString() {
        return name + " (cost " + elixirCost + ")";
    }
}
