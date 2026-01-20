package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Manages combo detection and tracking.
// Tracks the last 10 cards played and detects combos within a 5-second window.
public class ComboManager {
    private static final int MAX_TRACKED_CARDS = 10;
    private static final long COMBO_TIME_WINDOW_MS = 5000; // 5 seconds

    // Tracks recently played cards (most recent last)
    private LinkedList<PlayedCard> recentCards;

    public ComboManager() {
        this.recentCards = new LinkedList<>();
    }

    /**
     * Records a card play for combo detection.
     * @param card The card that was played
     * @param timestamp The time when the card was played (milliseconds)
     * @param isPlayer Whether the player (true) or computer (false) played the card
     */
    public void recordCardPlay(Card card, long timestamp, boolean isPlayer) {
        if (card == null) {
            return;
        }

        PlayedCard playedCard = new PlayedCard(card, timestamp, isPlayer);
        recentCards.add(playedCard);

        // Keep only the last MAX_TRACKED_CARDS cards
        while (recentCards.size() > MAX_TRACKED_CARDS) {
            recentCards.removeFirst();
        }
    }

    /**
     * Gets all cards played within the last 5 seconds from the given timestamp.
     * @param referenceTimestamp The timestamp to check from (usually current time)
     * @return List of PlayedCard objects within the time window
     */
    public List<PlayedCard> getCardsInTimeWindow(long referenceTimestamp) {
        List<PlayedCard> cardsInWindow = new ArrayList<>();
        
        for (PlayedCard playedCard : recentCards) {
            if (playedCard.isWithinTimeWindow(referenceTimestamp, COMBO_TIME_WINDOW_MS)) {
                cardsInWindow.add(playedCard);
            }
        }
        
        return cardsInWindow;
    }

    // Gets all recently tracked cards (up to MAX_TRACKED_CARDS) and returns List of all tracked PlayedCard objects
    public List<PlayedCard> getRecentCards() {
        return new ArrayList<>(recentCards);
    }

    // Clears all tracked card history. Should be called when a new game starts.
    public void reset() {
        recentCards.clear();
    }

    // Gets the combo time window in milliseconds.
    public static long getComboTimeWindow() {
        return COMBO_TIME_WINDOW_MS;
    }
}
