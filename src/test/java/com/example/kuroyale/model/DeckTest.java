package com.example.kuroyale.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    private Deck deck;

    @BeforeEach
    public void setUp() {
        deck = new Deck();
    }

    @Test
    public void testRepOkOnEmpty() {
        // Test Case 1: Verify Rep Invariant on a fresh, empty deck.
        // Input: New Deck().
        // Expected Output: repOk() returns true.

        // RI: cards != null, size <= 8
        assertTrue(deck.repOk(), "Empty deck should satisfy rep invariant");
    }

    @Test
    public void testAddCardSuccess() {
        // Test Case 2: Verify adding a valid card to a non-full deck.
        // Input: A valid Card object.
        // Expected Output: addCard() returns true, size increments, RI holds.

        Card c1 = new Card("Knight", 3, "TROOP", 2.0, 100, 1.0, 1.0, 1000, "");

        // Requires: c1 != null
        assertTrue(deck.addCard(c1));

        // Effects: cards size increases
        assertEquals(1, deck.getCards().size());
        assertTrue(deck.getCards().contains(c1));
        assertTrue(deck.repOk());
    }

    @Test
    public void testAddCardFull() {
        // Test Case 3: Verify boundary condition when adding to a full deck.
        // Input: Deck with 8 cards, attempt to add 9th card.
        // Expected Output: addCard() returns false, size remains 8, RI holds.

        // Fill deck with 8 cards
        for (int i = 0; i < 8; i++) {
            deck.addCard(new Card("Card" + i, 3, "TROOP", 2.0, 100, 1.0, 1.0, 1000, ""));
        }
        assertTrue(deck.repOk());
        assertEquals(8, deck.getCards().size());

        // Try to add 9th card
        Card c9 = new Card("Extra", 3, "TROOP", 2.0, 100, 1.0, 1.0, 1000, "");
        assertFalse(deck.addCard(c9), "Should return false when deck is full");
        assertEquals(8, deck.getCards().size(), "Size should remain 8");
        assertTrue(deck.repOk());
    }

    @Test
    public void testPlayCardEffect() {
        // Test Case 4: Verify playCard functionality and side effects.
        // Input: Full initialized deck, play card at index 0.
        // Expected Output: Card moves from hand to drawPile, nextCard moves to hand,
        // new nextCard drawn.

        setupFullDeck();
        deck.initializeGameDeck();

        Card firstInHand = deck.getCardInHand(0);
        Card nextOriginal = deck.getNextCard();

        assertNotNull(firstInHand);
        assertNotNull(nextOriginal);

        // Execute playCard(0)
        // Requires: 0 <= index < 4
        deck.playCard(0);

        // 1. Old firstInHand should be at bottom of drawPile (not easily visible via
        // public API, but inferred)
        // 2. Old 'nextCard' should now be in hand at index 0
        assertEquals(nextOriginal, deck.getCardInHand(0), "Next card should move to hand");

        // 3. New 'nextCard' should be drawn from pile
        assertNotEquals(nextOriginal, deck.getNextCard(), "Should have a new next card");

        assertTrue(deck.repOk());
    }

    @Test
    public void testPlayCardInvalidIndex() {
        // Test Case 5: Verify playCard with invalid indices.
        // Input: playCard(-1) and playCard(10).
        // Expected Output: No change in state (Hand, DrawPile, NextCard remain same),
        // RI holds.

        setupFullDeck();
        deck.initializeGameDeck();

        Card initialFirst = deck.getCardInHand(0);
        Card initialNext = deck.getNextCard();
        int initialDrawSize = deck.getCards().size() - 5; // Total - Hand(4) - Next(1)

        // Invalid Low
        deck.playCard(-1);
        assertEquals(initialFirst, deck.getCardInHand(0), "Hand should not change on invalid index");
        assertEquals(initialNext, deck.getNextCard(), "Next card should not change");

        // Invalid High
        deck.playCard(10);
        assertEquals(initialFirst, deck.getCardInHand(0), "Hand should not change on invalid index");
        assertEquals(initialNext, deck.getNextCard(), "Next card should not change");

        assertTrue(deck.repOk());
    }

    @Test
    public void testPlayCardCycleSequence() {
        // Test Case 6: Verify preservation of card order over multiple plays (FIFO
        // Cycle).
        // Context: Full Deck (8 cards).
        // Input: Play 5 cards sequentially.
        // Expected Output: The first card played should eventually return to be the
        // 'Next' card.

        // Setup distinct cards to track order
        deck = new Deck();
        Card[] specificCards = new Card[8];
        for (int i = 0; i < 8; i++) {
            specificCards[i] = new Card("Unique" + i, 3, "TROOP", 1, 1, 1, 1, 1, "");
            deck.addCard(specificCards[i]);
        }

        deck.initializeGameDeck();
        Card firstPlayed = deck.getCardInHand(0); // This is the card we will play

        // Play 1st card
        deck.playCard(0);
        // State: firstPlayed is now at bottom of drawPile.
        // DrawPile size: 3 cards.

        // To get 'firstPlayed' back to 'Next' position, we need to exhaust the current
        // drawPile.
        // Current DrawPile has 3 cards.
        // We need to play 3 more cards to pull those 3 from DrawPile into Hand/Next.

        deck.playCard(0);
        deck.playCard(0);
        deck.playCard(0);

        // After 4 plays total:
        // The original DrawPile (3 cards) should be fully consumed and moved to
        // hand/active.

        assertEquals(firstPlayed, deck.getNextCard(),
                "First played card should cycle back to become NextCard after 4 plays");

        assertTrue(deck.repOk());
    }

    private void setupFullDeck() {
        for (int i = 0; i < 8; i++) {
            deck.addCard(new Card("C" + i, 3, "TROOP", 2.0, 100, 1.0, 1.0, 1000, ""));
        }
    }
}
