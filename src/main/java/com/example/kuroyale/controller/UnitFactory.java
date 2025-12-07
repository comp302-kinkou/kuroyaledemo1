package com.example.kuroyale.controller;

import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Unit;

public class UnitFactory {

    public static Unit createUnit(Card card, double x, double y, boolean isPlayer) {
        // In a real game, we might have subclasses for different unit types
        // For now, we use the generic Unit class with stats from the Card

        return new Unit(
                card.getName(),
                x,
                y,
                isPlayer,
                card.getHealth(),
                card.getDamage(),
                card.getRange(),
                card.getSpeed(),
                card.getHitSpeed(),
                card.getTransportType(),
                card.getTargetType());
    }
}
