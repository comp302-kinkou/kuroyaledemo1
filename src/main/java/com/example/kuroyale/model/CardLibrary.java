package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardLibrary {

    private static final List<Card> allCards = new ArrayList<>();

    static {
        // ============ TROOP CARDS (15) ============

        // Single-Target Troops
        allCards.add(new Card("Knight", 3, "TROOP", 1.0, 75, 1.1, 2.0, 600, "/knight.png"));
        allCards.add(new Card("Musketeer", 4, "TROOP", 6.5, 100, 1.1, 2.0, 340, "/musketeer.png"));
        allCards.add(new Card("Mini P.E.K.K.A", 4, "TROOP", 1.0, 325, 1.8, 1.0, 600, "/minipekka.png"));
        allCards.add(new Card("Giant", 5, "TROOP", 1.0, 126, 1.5, 0.5, 2000, "/giant.png"));
        allCards.add(new Card("Hog Rider", 4, "TROOP", 1.0, 160, 1.5, 3.0, 800, "/hogrider.png"));

        // AoE Troops
        allCards.add(new Card("Bomber", 3, "TROOP", 5.0, 100, 1.9, 2.0, 150, "/bomber.png"));
        allCards.add(new Card("Valkyrie", 4, "TROOP", 1.0, 120, 1.5, 2.0, 880, "/valkyrie.png"));
        allCards.add(new Card("Wizard", 5, "TROOP", 5.0, 130, 1.7, 2.0, 340, "/wizard.png"));

        // Swarm Troops (Multiple Units Per Card)
        allCards.add(new Card("Skeletons", 1, "TROOP", 1.0, 30, 1.0, 3.5, 30, "/skeletons.png")); // spawns 4
        allCards.add(new Card("Goblins", 2, "TROOP", 1.0, 50, 1.0, 3.0, 80, "/goblins.png")); // spawns 3
        allCards.add(new Card("Spear Goblins", 2, "TROOP", 5.5, 24, 1.0, 3.0, 52, "/speargoblins.png")); // spawns 3
        allCards.add(new Card("Archers", 3, "TROOP", 5.5, 40, 1.0, 2.0, 125, "/archers.png")); // spawns 2
        allCards.add(new Card("Minions", 3, "TROOP", 2.5, 40, 1.0, 3.5, 90, "/minions.png")); // spawns 3
        allCards.add(new Card("Minion Horde", 5, "TROOP", 2.5, 40, 1.0, 3.5, 90, "/minionhorde.png")); // spawns 6
        allCards.add(new Card("Barbarians", 5, "TROOP", 1.0, 75, 1.0, 3.0, 300, "/barbarians.png")); // spawns 4

        // ============ BUILDING CARDS (9) ============

        // Defensive Buildings
        allCards.add(new Card("Cannon", 3, "BUILDING", 5.5, 60, 1.0, 0, 400, "/cannon.png"));
        allCards.add(new Card("Tesla", 4, "BUILDING", 5.5, 64, 1.0, 0, 400, "/tesla.png"));
        allCards.add(new Card("Mortar", 4, "BUILDING", 11.0, 108, 5.0, 0, 600, "/mortar.png")); // Range 4.5-11
        allCards.add(new Card("Bomb Tower", 5, "BUILDING", 6.0, 100, 1.0, 0, 900, "/bombtower.png"));
        allCards.add(new Card("Inferno Tower", 5, "BUILDING", 6.0, 20, 1.0, 0, 800, "/infernotower.png")); // DMG ramps
                                                                                                           // to 400

        // Spawner Buildings
        allCards.add(new Card("Tombstone", 3, "BUILDING", 0, 0, 0, 0, 200, "/tombstone.png"));
        allCards.add(new Card("Goblin Hut", 5, "BUILDING", 0, 0, 0, 0, 700, "/goblinhut.png"));
        allCards.add(new Card("Barbarian Hut", 7, "BUILDING", 0, 0, 0, 0, 1100, "/barbarianhut.png"));

        // Special Building
        allCards.add(new Card("Elixir Collector", 5, "BUILDING", 0, 0, 0, 0, 640, "/elixircollector.png"));

        // ============ SPELL CARDS (4) ============
        allCards.add(new Card("Zap", 2, "SPELL", 2.5, 80, 0, 0, 0, "/zap.png"));
        allCards.add(new Card("Arrows", 3, "SPELL", 4.0, 115, 0, 0, 0, "/arrows.png"));
        allCards.add(new Card("Fireball", 4, "SPELL", 2.5, 325, 0, 0, 0, "/fireball.png"));
        allCards.add(new Card("Rocket", 6, "SPELL", 2.0, 700, 0, 0, 0, "/rocket.png"));
    }

    public static List<Card> getAllCards() {
        return Collections.unmodifiableList(allCards);
    }

    public static Card getCardByName(String name) {
        for (Card card : allCards) {
            if (card.getName().equalsIgnoreCase(name)) {
                return card;
            }
        }
        return null;
    }
}
