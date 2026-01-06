package com.example.kuroyale.model;

public class Unit {
    private String name;
    private double x, y;
    private double health;
    private double damage;
    private double range;
    private double speed;
    private double hitSpeed;
    private boolean isPlayer; // true if player's unit
    private long lastAttackTime;

    // Movement target
    private double targetX, targetY;
    private boolean hasTarget;

    // New fields
    private TransportType transportType;
    private TargetType targetType;

    public Unit(String name, double x, double y, boolean isPlayer, double health, double damage, double range,
            double speed, double hitSpeed, TransportType transportType, TargetType targetType) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.health = health;
        this.damage = damage;
        this.range = range;
        this.speed = speed;
        this.hitSpeed = hitSpeed;
        this.lastAttackTime = 0;
        this.hasTarget = false;
        this.transportType = transportType;
        this.targetType = targetType;
    }

    public void moveTowards(double tx, double ty, double deltaTime) {
        double dx = tx - x;
        double dy = ty - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.1) {
            double moveDist = speed * deltaTime;
            if (moveDist > distance)
                moveDist = distance;

            x += (dx / distance) * moveDist;
            y += (dy / distance) * moveDist;
        }
    }

    public boolean isInRange(double tx, double ty, double radius) {
        double dx = tx - x;
        double dy = ty - y;
        return (dx * dx + dy * dy) <= (range + radius) * (range + radius);
    }

    public void takeDamage(double amount) {
        health -= amount;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean canAttack(long currentTime) {
        return (currentTime - lastAttackTime) >= (hitSpeed * 1000);
    }

    public void attack(long currentTime) {
        this.lastAttackTime = currentTime;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public double getHealth() {
        return health;
    }

    public double getDamage() {
        return damage;
    }

    public String getName() {
        return name;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public TargetType getTargetType() {
        return targetType;
    }
}
