package com.example.kuroyale.model.combo;

// Represents the effect that a combo applies.
public class ComboEffect {
    private final ComboEffectType effectType;
    private final double value; // Percentage boost (0.15 = 15%) or flat value (100 = +100 HP)
    private final String target; // Which unit/card this affects (e.g., "ranged", "Knight", "air", etc.)

    public ComboEffect(ComboEffectType effectType, double value, String target) {
        this.effectType = effectType;
        this.value = value;
        this.target = target;
    }

    public ComboEffectType getEffectType() {
        return effectType;
    }

    public double getValue() {
        return value;
    }

    public String getTarget() {
        return target;
    }
}