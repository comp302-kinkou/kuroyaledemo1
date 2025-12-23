package com.example.kuroyale.controller;

import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Deck;
import com.example.kuroyale.model.CardLibrary;
import java.util.Random;

public class ComputerOpponent {
    private GameController gameController;
    private Deck deck;
    private double actTimer;
    private Random random;
    private static final double ACT_INTERVAL = 3.0; // Try to play a card every 3 seconds

    public ComputerOpponent(GameController gameController) {
        this.gameController = gameController;
        this.deck = new Deck();
        this.random = new Random();
        initializeDeck();
    }

    private void initializeDeck() {
        // give computer some random cards or a fixed deck
        // For simplicity, let's just use all available cards like the player for now
        // or a subset.
        for (Card card : CardLibrary.getAllCards()) {
            if (deck.getCards().size() >= 8)
                break;
            if (!deck.getCards().contains(card)) {
                deck.addCard(card);
            }
        }
        deck.initializeGameDeck();
    }

    public void update(double deltaTime) {
        if (!gameController.isGameRunning() || gameController.isPaused()) {
            return;
        }

        actTimer += deltaTime;

        if (actTimer >= ACT_INTERVAL) {
            playRandomCard();
            // Reset timer with some randomness so it's not perfectly periodic
            actTimer = -random.nextDouble(); // between -1.0 and 0.0 delay
        }
    }

    private void playRandomCard() {
        // Try to play a card
        // 1. Pick a card from hand
        int handSize = deck.getHand().size();
        if (handSize == 0)
            return;

        int cardIndex = random.nextInt(handSize);
        Card card = deck.getCardInHand(cardIndex);

        if (card == null)
            return;

        // 2. Check elixir (We need access to computer's elixir manager)
        // We will expose a method in GameController for this or pass it in.
        // For now, let's assume GameController handles the check in playCard,
        // but we should check beforehand to avoid spamming failed attempts?
        // Actually, playCard returns boolean, so we can just try.
        // However, we need to know the cost.

        if (gameController.getComputerElixirManager().getElixir() < card.getElixirCost()) {
            return; // Not enough elixir
        }

        // 3. Pick a valid position
        // Enemy is at the TOP (Y < 16 usually, assuming 0 is top)
        // Wait, Arena.java says:
        // Player Towers (Bottom): Y ~ 26-30
        // Enemy Towers (Top): Y ~ 2-6
        // River is at Y=16.
        // So Enemy side is 0 <= Y <= 16.
        // Player side is 16 <= Y <= 32.

        double x, y;

        if (card.getType().equals("SPELL")) {
            // Spells anywhere
            x = random.nextDouble() * gameController.getArena().getWidth();
            y = random.nextDouble() * gameController.getArena().getHeight();
        } else {
            // Troops/Buildings on own side (Top)
            x = random.nextDouble() * (gameController.getArena().getWidth() - 2) + 1; // padding
            y = random.nextDouble() * (gameController.getArena().getRiverY() - 2) + 1; // padding

            // Avoid bridges for buildings?
            if (card.getType().equals("BUILDING")) {
                while (gameController.getArena().isOnBridge(x, y)) {
                    x = random.nextDouble() * (gameController.getArena().getWidth() - 2) + 1;
                    y = random.nextDouble() * (gameController.getArena().getRiverY() - 2) + 1;
                }
            }
        }

        // 4. Play
        // Pass false for isPlayer
        System.out.println("Computer trying to play " + card.getName() + " at " + x + ", " + y);
        boolean success = gameController.playCard(card, x, y, false);

        if (success) {
            deck.playCard(cardIndex);
            System.out.println("Computer played " + card.getName());
        }
    }
}
