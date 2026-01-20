package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.CardLibrary;
import com.example.kuroyale.model.Deck;

public class TankRushChallenge implements Challenge {

    @Override
    public String getName() {
        return "Tank Rush";
    }

    @Override
    public String getDescription() {
        return "• Can only use high-HP units: Giant, Knight, Valkyrie, Mini P.E.K.K.A, Barbarians\n" +
                "• No spells or buildings\n" +
                "• Objective: Win normally\n" +
                "• NOTE: A default deck will be provided for this challenge.";
    }

    @Override
    public int getReward() {
        return 300;
    }

    @Override
    public String validateDeck(Deck deck) {
        // Always valid because we provide the deck
        return null;
    }

    @Override
    public void onGameStart(GameController controller) {
        Deck fixedDeck = new Deck();
        fixedDeck.addCard(CardLibrary.getCardByName("Giant"));
        fixedDeck.addCard(CardLibrary.getCardByName("Knight"));
        fixedDeck.addCard(CardLibrary.getCardByName("Valkyrie"));
        fixedDeck.addCard(CardLibrary.getCardByName("Mini P.E.K.K.A"));
        fixedDeck.addCard(CardLibrary.getCardByName("Barbarians"));

        controller.setDeck(fixedDeck);
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

    @Override
    public boolean isDeckProvided() {
        return true;
    }
}
