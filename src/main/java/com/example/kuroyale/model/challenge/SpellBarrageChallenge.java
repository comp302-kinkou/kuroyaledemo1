package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.Deck;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SpellBarrageChallenge implements Challenge {
    private static final Set<String> REQUIRED_SPELLS = new HashSet<>(Arrays.asList(
            "Zap", "Arrows", "Fireball", "Rocket"));

    @Override
    public String getName() {
        return "Spell Barrage";
    }

    @Override
    public String getDescription() {
        return "• Your deck must contain all 4 spell cards (Zap, Arrows, Fireball, Rocket)\n" +
                "• Objective: Win the match\n" +
                "• Spells cost 1 less Elixir (minimum 1)";
    }

    @Override
    public int getReward() {
        return 300;
    }

    @Override
    public String validateDeck(Deck deck) {
        Set<String> presentSpells = new HashSet<>();
        for (Card card : deck.getCards()) {
            if (REQUIRED_SPELLS.contains(card.getName())) {
                presentSpells.add(card.getName());
            }
        }

        if (!presentSpells.containsAll(REQUIRED_SPELLS)) {
            return "Deck must contain all 4 spells: Zap, Arrows, Fireball, Rocket.";
        }
        return null;
    }

    @Override
    public void onGameStart(GameController controller) {
        // Modifiers applied via getModifiedCost
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

        // Check for perfect win (full health towers)
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
    public int getModifiedCost(Card card) {
        if ("SPELL".equals(card.getType())) {
            return Math.max(1, card.getElixirCost() - 1);
        }
        return card.getElixirCost();
    }
}
