package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Deck;

public interface Challenge {
    String getName();

    String getDescription();

    int getReward();

    /**
     * Strategy Design Pattern Implementation.
     * <p>
     * This interface acts as the Strategy in the Strategy Pattern. It defines a
     * family of algorithms
     * (challenges) that can be selected and exchanged at runtime, allowing the game
     * to switch
     * between different game modes/rulesets dynamically.
     * </p>
     * 
     * Validates if the deck meets the challenge requirements
     * 
     * @param deck The player's deck to validate
     * @return null if valid, error message string if invalid
     */
    String validateDeck(Deck deck);

    /**
     * Called when the game starts with this challenge active
     */
    void onGameStart(GameController controller);

    /**
     * Calculate stars earned based on game result
     * 
     * @param controller check game state/stats
     * @return 0 if lost/incomplete, 1-3 stars
     */
    int calculateStars(GameController controller);

    /**
     * Helper to check if a specific modifier applies
     */
    default int getModifiedCost(Card card) {
        return card.getElixirCost();
    }

    /**
     * Whether this challenge provides its own fixed deck
     */
    default boolean isDeckProvided() {
        return false;
    }
}
