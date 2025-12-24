package com.example.kuroyale.controller;

import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.Unit;

public class UnitFactory {

    // Constructor with no progression
    public static Unit createUnit(Card card, double x, double y, boolean isPlayer) {
        return createUnit(card, x, y, isPlayer, null);
    }

    // Creates a unit from a card, applying level-based stat bonuses if progression is provided
    // New constructor with progression: either null or a certain progression object depending on the call
    public static Unit createUnit(Card card, double x, double y, boolean isPlayer, CardProgression progression) {
        double health = card.getHealth();
        double damage = card.getDamage();

        // Apply level bonuses if progression is provided
        if (progression != null) {
            health = progression.applyLevelBonus(health);
            damage = progression.applyLevelBonus(damage);
            // Round damage to nearest integer
            damage = Math.round(damage);
        }

        return new Unit(
                card.getName(),
                x,
                y,
                isPlayer,
                health,
                damage,
                card.getRange(),
                card.getSpeed(),
                card.getHitSpeed(),
                card.getTransportType(),
                card.getTargetType());
    }
}
