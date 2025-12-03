package com.example.kuroyale.model;

public class Tower {
    private String type; // "KING", "PRINCESS"
    private double x, y;
    private double health;
    private double maxHealth;
    private double range;
    private double damage;
    private double hitSpeed;
    private long lastAttackTime;
    private boolean isPlayer; // true if belongs to player, false if enemy

    public Tower(String type, double x, double y, boolean isPlayer) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.lastAttackTime = 0;

        if (type.equals("KING")) {
            this.maxHealth = 4000;
            this.range = 7.0;
            this.damage = 100;
            this.hitSpeed = 1.0;
        } else { // PRINCESS
            this.maxHealth = 2500;
            this.range = 7.5;
            this.damage = 80;
            this.hitSpeed = 0.8;
        }
        this.health = this.maxHealth;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getHealth() {
        return health;
    }

    public double getRange() {
        return range;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void takeDamage(double amount) {
        this.health -= amount;
        if (this.health < 0)
            this.health = 0;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public boolean canAttack(long currentTime) {
        return (currentTime - lastAttackTime) >= (hitSpeed * 1000); // hitSpeed in seconds
    }

    public void attack(long currentTime) {
        this.lastAttackTime = currentTime;
    }

    public double getDamage() {
        return damage;
    }

    public String getType() {
        return type;
    }
    // getType method

    public double getMaxHealth() {
        return maxHealth;
    }

    // getMaxHealth method
    @Override
    public String toString() {
        return type + " Tower @ (" + x + ", " + y + "), HP=" + health + "/" + maxHealth + ", player=" + isPlayer;
    }
}
