package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Deck;

public class BudgetBattleChallenge implements Challenge {

    @Override
    public String getName() {
        return "Budget Battle";
    }

    @Override
    public String getDescription() {
        return "• Can only use cards costing 3 Elixir or less\n" +
                "• Deck must have 8 valid cards\n" +
                "• Objective: Win normally";
    }

    @Override
    public int getReward() {
        return 250;
    }

    @Override
    public String validateDeck(Deck deck) {
        if (deck.getCards().size() != 8) {
            return "Deck must have 8 cards.";
        }

        for (Card card : deck.getCards()) {
            if (card.getElixirCost() > 3) {
                return "Card costs more than 3 Elixir: " + card.getName();
            }
        }
        return null;
    }

    @Override
    public void onGameStart(GameController controller) {
    }

    @Override
    public int calculateStars(GameController controller) {
        if (!"WIN".equals(controller.getGameResult())) {
            return 0;
        }

        double timeRemaining = controller.getGameTime();
        boolean fastWin = timeRemaining > 60;

        boolean perfectWin = controller.getArena().getTowers().stream()
                .filter(t -> t.isPlayer())
                .allMatch(t -> t.getHealth() == t.getMaxHealth());

        if (perfectWin)
            return 3;
        if (fastWin)
            return 2;
        return 1;
    }
}
