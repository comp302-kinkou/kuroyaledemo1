package com.example.kuroyale.model;

public class Building {
    private String name;
    private double x, y;
    private double health, maxHealth;
    private double damage;
    private double range;
    private double hitSpeed;
    private double lifetime; // in seconds
    private double timeAlive; // tracks age
    private boolean isPlayer;
    private boolean destroyed;
    private String buildingType; // DEFENSIVE, SPAWNER, SPECIAL
    private long lastAttackTime;

    // For spawner buildings
    private String spawnedUnitType;
    private double spawnInterval;
    private double timeSinceLastSpawn;

    // For Elixir Collector
    private double elixirGenerationInterval = 10.0; // 1 elixir every 10s
    private double timeSinceLastElixir = 0.0;

    public Building(String name, double x, double y, boolean isPlayer, double health,
            double damage, double range, double hitSpeed, double lifetime,
            String buildingType) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.range = range;
        this.hitSpeed = hitSpeed;
        this.lifetime = lifetime;
        this.timeAlive = 0.0;
        this.buildingType = buildingType;
        this.destroyed = false;
        this.lastAttackTime = 0;
        this.timeSinceLastSpawn = 0.0;
    }

    public void updateLifetime(double deltaTime) {
        timeAlive += deltaTime;
    }

    public boolean isExpired() {
        return timeAlive >= lifetime;
    }

    public void destroy() {
        this.destroyed = true;
    }

    public void takeDamage(double amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            destroyed = true;
        }
    }

    public boolean canAttack(long currentTime) {
        return (currentTime - lastAttackTime) >= (hitSpeed * 1000);
    }

    public void attack(long currentTime) {
        this.lastAttackTime = currentTime;
    }

    public void updateSpawnTimer(double deltaTime) {
        timeSinceLastSpawn += deltaTime;
    }

    public boolean shouldSpawn() {
        if (timeSinceLastSpawn >= spawnInterval) {
            timeSinceLastSpawn = 0.0;
            return true;
        }
        return false;
    }

    public void updateElixirTimer(double deltaTime) {
        timeSinceLastElixir += deltaTime;
    }

    public boolean shouldGenerateElixir() {
        if (timeSinceLastElixir >= elixirGenerationInterval) {
            timeSinceLastElixir = 0.0;
            return true;
        }
        return false;
    }

    public void setSpawnerProperties(String unitType, double interval) {
        this.spawnedUnitType = unitType;
        this.spawnInterval = interval;
    }

    // Type checks
    public boolean isDefensive() {
        return buildingType.equals("DEFENSIVE");
    }

    public boolean isSpawner() {
        return buildingType.equals("SPAWNER");
    }

    public boolean isElixirCollector() {
        return buildingType.equals("SPECIAL") && name.equals("Elixir Collector");
    }

    // Getters
    public String getName() {
        return name;
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

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public double getTimeAlive() {
        return timeAlive;
    }

    public double getLifetime() {
        return lifetime;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public String getSpawnedUnitType() {
        return spawnedUnitType;
    }
}
