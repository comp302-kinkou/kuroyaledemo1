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

    // Combo effect modifiers (stack multiplicatively)
    private double damageMultiplier = 1.0;
    private double speedMultiplier = 1.0;
    private double healthMultiplier = 1.0;
    private double rangeBoost = 0.0;
    private double flatHealthBoost = 0.0;

    // Base stats (stored to apply multipliers correctly)
    private double baseDamage;
    private double baseSpeed;
    private double baseHealth;
    private double baseRange;

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
        
        // Store base stats
        this.baseDamage = damage;
        this.baseSpeed = speed;
        this.baseHealth = health;
        this.baseRange = range;
    }

    public void moveTowards(double tx, double ty, double deltaTime) {
        double dx = tx - x;
        double dy = ty - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.1) {
            double currentSpeed = getSpeed(); // Use getter to get modified speed
            double moveDist = currentSpeed * deltaTime;
            if (moveDist > distance)
                moveDist = distance;

            x += (dx / distance) * moveDist;
            y += (dy / distance) * moveDist;
        }
    }

    public boolean isInRange(double tx, double ty, double radius) {
        double dx = tx - x;
        double dy = ty - y;
        double currentRange = getRange(); // Use getter to get modified range
        return (dx * dx + dy * dy) <= (currentRange + radius) * (currentRange + radius);
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
        // Return base damage with multiplier applied
        return baseDamage * damageMultiplier;
    }

    public double getSpeed() {
        // Return base speed with multiplier applied
        return baseSpeed * speedMultiplier;
    }

    public double getRange() {
        // Return base range with boost applied
        return baseRange + rangeBoost;
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

    // Combo effect application methods
    public void applyDamageMultiplier(double multiplier) {
        this.damageMultiplier *= multiplier;
    }

    public void applySpeedMultiplier(double multiplier) {
        this.speedMultiplier *= multiplier;
    }

    public void applyHealthMultiplier(double multiplier) {
        // Apply to current health proportionally
        double healthRatio = health / baseHealth;
        this.healthMultiplier *= multiplier;
        this.baseHealth *= multiplier;
        this.health = baseHealth * healthRatio;
    }

    public void addHealth(double amount) {
        this.flatHealthBoost += amount;
        this.health += amount;
        this.baseHealth += amount;
    }

    public void addRange(double amount) {
        this.rangeBoost += amount;
    }
}