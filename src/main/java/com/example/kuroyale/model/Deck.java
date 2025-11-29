package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();
    private List<Card> hand = new ArrayList<>();
    private List<Card> drawPile = new ArrayList<>();
    private Card nextCard;

    public boolean addCard(Card card) {
        if (cards.size() >= 8) {
            // System.out.println("Deck full! (Max 8 cards)");
            return false;
        }
        cards.add(card);
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
