package com.example.kuroyale.model.challenge;

import com.example.kuroyale.controller.GameController;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardLibrary;
import com.example.kuroyale.model.Deck;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SwarmMasterChallenge implements Challenge {

    @Override
    public String getName() {
        return "Swarm Master";
    }

    @Override
    public String getDescription() {
        return "• You can only use swarm troop cards (Skeletons, Goblins, Spear Goblins, Archers, Minions, Minion Horde, Barbarians)\n"
                +
                "• Must include at least 5 swarm cards in your deck\n" +
                "• Objective: Win normally";
    }

    @Override
    public int getReward() {
        return 250;
    }

    @Override
    public String validateDeck(Deck deck) {
        int swarmCount = 0;
        for (Card card : deck.getCards()) {
            if (CardLibrary.isSwarmCard(card.getName())) {
                swarmCount++;
            } else if (card.getType().equals("TROOP")) {
                // Technically "only use swarm troop cards", implies other troops are banned.
                // Assuming Spells/Buildings are allowed if they don't contradict "only use
                // swarm troop cards"?
                // "You can only use swarm troop cards (list...)" usually implies NO OTHER
                // TROOPS.
                return "Deck contains non-swarm troop: " + card.getName();
            }
        }

        if (swarmCount < 5) {
            return "Deck must contain at least 5 swarm cards. Found: " + swarmCount;
        }

        return null;
    }

    @Override
    public void onGameStart(GameController controller) {
        // No special game state changes needed
    }

    @Override
    public int calculateStars(GameController controller) {
        if (!"WIN".equals(controller.getGameResult())) {
            return 0; // Failed
        }

        // 1 Star: Complete
        // 2 Stars: Win quickly (e.g., < 2 mins used, so > 60s remaining)
        // 3 Stars: Perfect (e.g., No tower damage? or very fast)

        double timeRemaining = controller.getGameTime();
        // Total time is 180s.
        // Win within 2 mins meant used < 120s, so remaining > 60s.

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
