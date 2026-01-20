package com.example.kuroyale.model.combo;

import com.example.kuroyale.model.Card;

/**
 * Represents a combo that was detected.
 * Contains the combo type, the cards that triggered it, and the effect to apply.
 */
public class DetectedCombo {
    private final ComboType comboType;
    private final Card card1;
    private final Card card2;
    private final ComboEffect effect;
    private final long detectionTime;

    public DetectedCombo(ComboType comboType, Card card1, Card card2, ComboEffect effect, long detectionTime) {
        this.comboType = comboType;
        this.card1 = card1;
        this.card2 = card2;
        this.effect = effect;
        this.detectionTime = detectionTime;
    }

    public ComboType getComboType() {
        return comboType;
    }

    public Card getCard1() {
        return card1;
    }

    public Card getCard2() {
        return card2;
    }

    public ComboEffect getEffect() {
        return effect;
    }

    public long getDetectionTime() {
        return detectionTime;
    }

    @Override
    public String toString() {
        return "DetectedCombo{" +
                "comboType=" + comboType.getDisplayName() +
                ", card1=" + (card1 != null ? card1.getName() : "null") +
                ", card2=" + (card2 != null ? card2.getName() : "null") +
                '}';
    }
}