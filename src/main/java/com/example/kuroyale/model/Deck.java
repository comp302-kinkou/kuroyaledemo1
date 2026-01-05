package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of cards in the game.
 * ADT Overview: Mutable, represents a collection of Card objects with a hand
 * subset.
 * 
 * Abstraction Function:
 * AF(c) = A deck d where
 * d.allCards = c.cards
 * d.hand = c.hand
 * d.drawPile = c.drawPile
 * d.next = c.nextCard
 * 
 * Rep Invariant:
 * cards != null
 * hand != null
 * drawPile != null
 * cards.size() <= 8
 * hand.size() <= 4
 * hand elements are subset of cards
 */
public class Deck {
    private List<Card> cards = new ArrayList<>();
    private List<Card> hand = new ArrayList<>();
    private List<Card> drawPile = new ArrayList<>();
    private Card nextCard;

    /**
     * Checks if the representation invariant holds.
     * 
     * @return true if the rep is valid, false otherwise.
     */
    public boolean repOk() {
        if (cards == null || hand == null || drawPile == null)
            return false;
        if (cards.size() > 8)
            return false;
        if (hand.size() > 4)
            return false;
        // Check if hand cards are in main deck
        for (Card c : hand) {
            if (!cards.contains(c))
                return false;
        }
        return true;
    }

    /**
     * Adds a card to the deck.
     * 
     * Requires: card is not null
     * Modifies: this.cards
     * Effects: If cards.size() < 8, adds card to cards and returns true.
     * Otherwise returns false.
     */
    public boolean addCard(Card card) {
        if (cards.size() >= 8) {
            // System.out.println("Deck full! (Max 8 cards)");
            return false;
        }
        cards.add(card);
        // assert repOk();
        return true;
    }

    public void clear() {
        cards.clear();
        hand.clear();
        drawPile.clear();
        nextCard = null;
    }

    public void initializeGameDeck() {
        if (cards.size() < 8) {
            System.out.println("Warning: Deck has fewer than 8 cards.");
        }
        drawPile.clear();
        drawPile.addAll(cards);
        Collections.shuffle(drawPile);

        hand.clear();
        for (int i = 0; i < 4 && !drawPile.isEmpty(); i++) {
            hand.add(drawPile.remove(0));
        }

        if (!drawPile.isEmpty()) {
            nextCard = drawPile.remove(0);
        }
        // assert repOk();
    }

    public Card getCardInHand(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.get(index);
        }
        return null;
    }

    public Card getNextCard() {
        return nextCard;
    }

    /**
     * Plays a card from the hand.
     * 
     * Requires: handIndex larger or equal to 0 and smaller than hand.size()
     * Modifies: this.hand, this.drawPile, this.nextCard
     * Effects: Removes the card at handIndex from hand.
     * Adds the played card to the bottom of drawPile.
     * Moves nextCard to hand at handIndex.
     * If it is available, draws a new nextCard from drawPile.
     */
    public void playCard(int handIndex) {
        if (handIndex >= 0 && handIndex < hand.size()) {
            Card played = hand.get(handIndex);

            // Add played card back to bottom of draw pile (or discard pile if we had one,
            // but CR cycles)
            drawPile.add(played);

            // Move next card to hand
            if (nextCard != null) {
                hand.set(handIndex, nextCard);
            }

            // Draw new next card
            if (!drawPile.isEmpty()) {
                nextCard = drawPile.remove(0);
            }
        }
        // assert repOk();
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Card> getHand() {
        return hand;
    }

    @Override
    public String toString() {
        return "Deck size: " + cards.size();
    }
}
