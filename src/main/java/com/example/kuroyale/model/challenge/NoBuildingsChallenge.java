package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Deck;

public class NoBuildingsChallenge implements Challenge {

    @Override
    public String getName() {
        return "No Buildings Allowed";
    }

    @Override
    public String getDescription() {
        return "• Cannot use any building cards\n" +
                "• Deck must contain only troops and spells\n" +
                "• Objective: Win normally";
    }

    @Override
    public int getReward() {
        return 200;
    }

    @Override
    public String validateDeck(Deck deck) {
        for (Card card : deck.getCards()) {
            if ("BUILDING".equals(card.getType())) {
                return "Deck cannot contain buildings: " + card.getName();
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
        // 2 Stars: Win in 2 mins or less (< 120s used) -> timeRemaining > 60s
        boolean fastWin = timeRemaining > 60;

        // 3 Stars: Perfect Win OR Very Fast Win (90s or less used -> timeRemaining >
        // 90s)
        boolean veryFastWin = timeRemaining > 90;

        boolean perfectWin = controller.getArena().getTowers().stream()
                .filter(t -> t.isPlayer())
                .allMatch(t -> t.getHealth() == t.getMaxHealth());

        if (perfectWin || veryFastWin)
            return 3;
        if (fastWin)
            return 2;
        return 1;
    }
}
