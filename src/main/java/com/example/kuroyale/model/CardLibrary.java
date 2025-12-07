package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardLibrary {

        private static final List<Card> allCards = new ArrayList<>();

        static {
                // ============ TROOP CARDS (15) ============

                // Single-Target Troops
                // Knight: Ground, Ground
                allCards.add(new Card("Knight", 3, "TROOP", 1.0, 75, 1.1, 2.0, 600, "/knight.png", TransportType.GROUND,
                                TargetType.GROUND));
                // Musketeer: Ground, Air & Ground
                allCards.add(new Card("Musketeer", 4, "TROOP", 6.5, 100, 1.1, 2.0, 340, "/musketeer.png",
                                TransportType.GROUND,
                                TargetType.AIR_AND_GROUND));
                // Mini P.E.K.K.A: Ground, Ground
                allCards.add(new Card("Mini P.E.K.K.A", 4, "TROOP", 1.0, 325, 1.8, 1.0, 600, "/minipekka.png",
                                TransportType.GROUND, TargetType.GROUND));
                // Giant: Ground, Buildings
                allCards.add(new Card("Giant", 5, "TROOP", 1.0, 126, 1.5, 0.5, 2000, "/giant.png", TransportType.GROUND,
                                TargetType.BUILDINGS));
                // Hog Rider: Ground, Buildings
                allCards.add(new Card("Hog Rider", 4, "TROOP", 1.0, 160, 1.5, 3.0, 800, "/hogrider.png",
                                TransportType.GROUND,
                                TargetType.BUILDINGS));

                // AoE Troops
                // Bomber: Ground, Ground
                allCards.add(new Card("Bomber", 3, "TROOP", 5.0, 100, 1.9, 2.0, 150, "/bomber.png",
                                TransportType.GROUND,
                                TargetType.GROUND));
                // Valkyrie: Ground, Ground
                allCards.add(new Card("Valkyrie", 4, "TROOP", 1.0, 120, 1.5, 2.0, 880, "/valkyrie.png",
                                TransportType.GROUND,
                                TargetType.GROUND));
                // Wizard: Ground, Air & Ground
                allCards.add(new Card("Wizard", 5, "TROOP", 5.0, 130, 1.7, 2.0, 340, "/wizard.png",
                                TransportType.GROUND,
                                TargetType.AIR_AND_GROUND));

                // Swarm Troops (Multiple Units Per Card) - Spawning logic handles count, here
                // assume properties for individual unit
                // Skeletons: Ground, Ground
                allCards.add(new Card("Skeletons", 1, "TROOP", 1.0, 30, 1.0, 3.5, 30, "/skeletons.png",
                                TransportType.GROUND,
                                TargetType.GROUND));
                // Goblins: Ground, Ground
                allCards.add(new Card("Goblins", 2, "TROOP", 1.0, 50, 1.0, 3.0, 80, "/goblins.png",
                                TransportType.GROUND,
                                TargetType.GROUND));
                // Spear Goblins: Ground, Air & Ground
                allCards.add(new Card("Spear Goblins", 2, "TROOP", 5.5, 24, 1.0, 3.0, 52, "/speargoblins.png",
                                TransportType.GROUND, TargetType.AIR_AND_GROUND));
                // Archers: Ground, Air & Ground
                allCards.add(new Card("Archers", 3, "TROOP", 5.5, 40, 1.0, 2.0, 125, "/archers.png",
                                TransportType.GROUND,
                                TargetType.AIR_AND_GROUND));
                // Minions: Air, Air & Ground
                allCards.add(new Card("Minions", 3, "TROOP", 2.5, 40, 1.0, 3.5, 90, "/minions.png", TransportType.AIR,
                                TargetType.AIR_AND_GROUND));
                // Minion Horde: Air, Air & Ground
                allCards.add(new Card("Minion Horde", 5, "TROOP", 2.5, 40, 1.0, 3.5, 90, "/minionhorde.png",
                                TransportType.AIR,
                                TargetType.AIR_AND_GROUND));
                // Barbarians: Ground, Ground
                allCards.add(new Card("Barbarians", 5, "TROOP", 1.0, 75, 1.0, 3.0, 300, "/barbarians.png",
                                TransportType.GROUND,
                                TargetType.GROUND));

                // ============ BUILDING CARDS (9) ============

                // Defensive Buildings - Ground by default, target logic varies but usually Air
                // & Ground for defensive buildings
                // except Cannon and Bomb Tower (Ground only)
                // Cannon: Ground Only
                allCards.add(new Card("Cannon", 3, "BUILDING", 5.5, 60, 1.0, 0, 400, "/cannon.png",
                                TransportType.NONE,
                                TargetType.GROUND));
                // Tesla: Air & Ground
                allCards.add(new Card("Tesla", 4, "BUILDING", 5.5, 64, 1.0, 0, 400, "/tesla.png", TransportType.NONE,
                                TargetType.AIR_AND_GROUND));
                // Mortar: Ground only (usually siege)
                allCards.add(new Card("Mortar", 4, "BUILDING", 11.0, 108, 5.0, 0, 600, "/mortar.png",
                                TransportType.NONE,
                                TargetType.GROUND));
                // Bomb Tower: Ground only
                allCards.add(new Card("Bomb Tower", 5, "BUILDING", 6.0, 100, 1.0, 0, 900, "/bombtower.png",
                                TransportType.NONE, TargetType.GROUND));
                // Inferno Tower: Air & Ground
                allCards.add(new Card("Inferno Tower", 5, "BUILDING", 6.0, 20, 1.0, 0, 800, "/infernotower.png",
                                TransportType.NONE, TargetType.AIR_AND_GROUND));

                // Spawner Buildings - Don't attack directly usually, but let's leave defaults
                allCards.add(new Card("Tombstone", 3, "BUILDING", 0, 0, 0, 0, 200, "/tombstone.png",
                                TransportType.NONE,
                                TargetType.GROUND));
                allCards.add(new Card("Goblin Hut", 5, "BUILDING", 0, 0, 0, 0, 700, "/goblinhut.png",
                                TransportType.NONE,
                                TargetType.GROUND));
                allCards.add(new Card("Barbarian Hut", 7, "BUILDING", 0, 0, 0, 0, 1100, "/barbarianhut.png",
                                TransportType.NONE, TargetType.GROUND));

                // Special Building
                allCards.add(new Card("Elixir Collector", 5, "BUILDING", 0, 0, 0, 0, 640, "/elixircollector.png",
                                TransportType.NONE, TargetType.NONE));

                // ============ SPELL CARDS (4) ============
                // Spells target everything usually
                allCards.add(new Card("Zap", 2, "SPELL", 2.5, 80, 0, 0, 0, "/zap.png", TransportType.NONE,
                                TargetType.AIR_AND_GROUND));
                allCards.add(new Card("Arrows", 3, "SPELL", 4.0, 115, 0, 0, 0, "/arrows.png", TransportType.NONE,
                                TargetType.AIR_AND_GROUND));
                allCards.add(new Card("Fireball", 4, "SPELL", 2.5, 325, 0, 0, 0, "/fireball.png", TransportType.NONE,
                                TargetType.AIR_AND_GROUND));
                allCards.add(new Card("Rocket", 6, "SPELL", 2.0, 700, 0, 0, 0, "/rocket.png", TransportType.NONE,
                                TargetType.AIR_AND_GROUND));
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
