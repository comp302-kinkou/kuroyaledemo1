package com.example.kuroyale.model;

public class Effect {
    private double x, y;
    private double duration; // seconds
    private double maxDuration;
    private String type; // "EXPLOSION", "SPELL_AREA", etc.
    private double radius;

    public Effect(double x, double y, double duration, String type, double radius) {
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.maxDuration = duration;
        this.type = type;
        this.radius = radius;
    }

    public void update(double deltaTime) {
        duration -= deltaTime;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDuration() {
        return duration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public String getType() {
        return type;
    }

    public double getRadius() {
        return radius;
    }
}
