package com.example.kuroyale.model.combo;

import com.example.kuroyale.model.Card;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// Manages combo detection and tracking.
// Tracks the last 10 cards played and detects combos within a 5-second window.
public class ComboManager {
    private static final int MAX_TRACKED_CARDS = 10;
    private static final long COMBO_TIME_WINDOW_MS = 5000; // 5 seconds

    // Tracks recently played cards (most recent last)
    private LinkedList<PlayedCard> recentCards;
    
    // All combo definitions
    private final ComboDefinition[] allComboDefinitions;
    
    // Tracks recently triggered combos to prevent duplicates within the same time window
    // Key format: "ComboType:Card1Name:Card2Name" (normalized)
    private Set<String> recentlyTriggeredCombos;
    
    // Tracks when each combo was last triggered (for cleanup)
    private List<ComboTriggerRecord> comboTriggerHistory;

    // Tracks unique combo types triggered in the current match (for counter and rewards)
    private Set<ComboType> uniqueCombosTriggered;

    public ComboManager() {
        this.recentCards = new LinkedList<>();
        this.allComboDefinitions = ComboDefinition.createAllComboDefinitions();
        this.recentlyTriggeredCombos = new HashSet<>();
        this.comboTriggerHistory = new ArrayList<>();
        this.uniqueCombosTriggered = new HashSet<>();
    }

    /**
     * Records a card play for combo detection and checks for combos.
     * 
     * @param card The card that was played
     * @param timestamp The time when the card was played (milliseconds)
     * @param isPlayer Whether the player (true) or computer (false) played the card
     * @return List of detected combos (empty if none detected)
     */
    public List<DetectedCombo> recordCardPlay(Card card, long timestamp, boolean isPlayer) {
        if (card == null) {
            return new ArrayList<>();
        }

        PlayedCard playedCard = new PlayedCard(card, timestamp, isPlayer);
        recentCards.add(playedCard);

        // Keep only the last MAX_TRACKED_CARDS cards
        while (recentCards.size() > MAX_TRACKED_CARDS) {
            recentCards.removeFirst();
        }

        // Clean up old combo triggers (older than time window)
        cleanupOldComboTriggers(timestamp);

        // Detect combos with the newly played card
        return detectCombos(card, timestamp);
    }

    /**
     * Gets all cards played within the last 5 seconds from the given timestamp.
     * 
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

    /**
     * Gets all recently tracked cards (up to MAX_TRACKED_CARDS).
     * 
     * @return List of all tracked PlayedCard objects
     */
    public List<PlayedCard> getRecentCards() {
        return new ArrayList<>(recentCards);
    }

    /**
     * Detects combos involving the newly played card.
     * Checks all cards within the time window against all combo definitions.
     * 
     * @param newlyPlayedCard The card that was just played
     * @param currentTimestamp Current timestamp
     * @return List of detected combos
     */
    private List<DetectedCombo> detectCombos(Card newlyPlayedCard, long currentTimestamp) {
        List<DetectedCombo> detectedCombos = new ArrayList<>();
        
        // Get all cards within the time window
        List<PlayedCard> cardsInWindow = getCardsInTimeWindow(currentTimestamp);
        
        if (cardsInWindow.size() < 2) {
            return detectedCombos; // Need at least 2 cards for a combo
        }

        // Check the newly played card against all other cards in the window
        String newCardName = newlyPlayedCard.getName();
        
        for (PlayedCard otherPlayedCard : cardsInWindow) {
            Card otherCard = otherPlayedCard.getCard();
            if (otherCard == null) {
                continue;
            }
            
            // Skip if it's the same card play (same timestamp means same play)
            // We want to match with different card plays
            if (otherPlayedCard.getTimestamp() == currentTimestamp) {
                continue; // This is the same card play, skip it
            }
            
            String otherCardName = otherCard.getName();
            
            // Check against all combo definitions
            for (ComboDefinition definition : allComboDefinitions) {
                if (definition.matches(newCardName, otherCardName)) {
                    // Check if this combo was already triggered recently
                    String comboKey = createComboKey(definition.getComboType(), newCardName, otherCardName);
                    
                    if (!recentlyTriggeredCombos.contains(comboKey)) {
                        // New combo detected!
                        DetectedCombo detected = new DetectedCombo(
                            definition.getComboType(),
                            newlyPlayedCard,
                            otherCard,
                            definition.getEffect(),
                            currentTimestamp
                        );
                        detectedCombos.add(detected);
                        
                        // Mark as triggered
                        recentlyTriggeredCombos.add(comboKey);
                        comboTriggerHistory.add(new ComboTriggerRecord(comboKey, currentTimestamp));

                        // Track unique combo type for match counter
                        uniqueCombosTriggered.add(definition.getComboType());
                    }
                }
            }
        }
        
        return detectedCombos;
    }

    // Creates a normalized key for a combo to prevent duplicate triggers.
    // Ensures the same combo with the same cards can't trigger twice in the same window.
    private String createComboKey(ComboType comboType, String card1Name, String card2Name) {
        // Normalize card names (alphabetically) so order doesn't matter for the key
        String first = card1Name.compareTo(card2Name) < 0 ? card1Name : card2Name;
        String second = card1Name.compareTo(card2Name) < 0 ? card2Name : card1Name;
        return comboType.name() + ":" + first + ":" + second;
    }

    // Cleans up combo trigger records that are older than the time window.
    // This allows the same combo to trigger again after the window expires.
    private void cleanupOldComboTriggers(long currentTimestamp) {
        comboTriggerHistory.removeIf(record -> {
            long age = currentTimestamp - record.getTimestamp();
            if (age > COMBO_TIME_WINDOW_MS) {
                recentlyTriggeredCombos.remove(record.getComboKey());
                return true; // Remove this record
            }
            return false;
        });
    }

    // Clears all tracked card history. Should be called when a new game starts.
    public void reset() {
        recentCards.clear();
        recentlyTriggeredCombos.clear();
        comboTriggerHistory.clear();
        uniqueCombosTriggered.clear();
    }

    // Gets the number of unique combos triggered in the current match and returns the count of unique combo types triggered.
    public int getUniqueComboCount() {
        return uniqueCombosTriggered.size();
    }

    // Gets the set of unique combo types triggered in the current match and returns the set of ComboType enums that have been triggered.
    public Set<ComboType> getUniqueCombosTriggered() {
        return new HashSet<>(uniqueCombosTriggered);
    }

    // Gets the combo time window in milliseconds.
    public static long getComboTimeWindow() {
        return COMBO_TIME_WINDOW_MS;
    }

    // Internal class to track when a combo was triggered.
    private static class ComboTriggerRecord {
        private final String comboKey;
        private final long timestamp;

        public ComboTriggerRecord(String comboKey, long timestamp) {
            this.comboKey = comboKey;
            this.timestamp = timestamp;
        }

        public String getComboKey() {
            return comboKey;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}