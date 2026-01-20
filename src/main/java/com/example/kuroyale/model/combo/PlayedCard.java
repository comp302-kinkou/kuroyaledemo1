package com.example.kuroyale.model;

// Represents a card that was played, along with its timestamp.
// Used for tracking card plays to detect combos.

public class PlayedCard {
    private final Card card;
    private final long timestamp; // milliseconds since epoch
    private final boolean isPlayer; // true if player played it, false if computer

    public PlayedCard(Card card, long timestamp, boolean isPlayer) {
        this.card = card;
        this.timestamp = timestamp;
        this.isPlayer = isPlayer;
    }

    public Card getCard() {
        return card;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    // Checks if this card was played within the specified time window (in milliseconds) from the given reference timestamp.
    public boolean isWithinTimeWindow(long referenceTimestamp, long windowMs) {
        long timeDiff = Math.abs(referenceTimestamp - this.timestamp);
        return timeDiff <= windowMs;
    }

    @Override
    public String toString() {
        return "PlayedCard{" +
                "card=" + (card != null ? card.getName() : "null") +
                ", timestamp=" + timestamp +
                ", isPlayer=" + isPlayer +
                '}';
    }
}