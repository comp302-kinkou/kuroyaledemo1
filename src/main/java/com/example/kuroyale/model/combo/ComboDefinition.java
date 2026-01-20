package com.example.kuroyale.model.combo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Defines a combo with its trigger conditions and effects.
// Each combo type has specific card requirements and applies specific bonuses.
public class ComboDefinition {
    private final ComboType comboType;
    private final Set<String> requiredCardNames1; // First set of cards that can trigger
    private final Set<String> requiredCardNames2; // Second set of cards that can trigger
    private final boolean orderMatters; // Whether the order of cards matters
    private final ComboEffect effect;

    public ComboDefinition(ComboType comboType, Set<String> requiredCardNames1, 
                          Set<String> requiredCardNames2, boolean orderMatters, ComboEffect effect) {
        this.comboType = comboType;
        this.requiredCardNames1 = requiredCardNames1;
        this.requiredCardNames2 = requiredCardNames2;
        this.orderMatters = orderMatters;
        this.effect = effect;
    }

    public ComboType getComboType() {
        return comboType;
    }

    public Set<String> getRequiredCardNames1() {
        return requiredCardNames1;
    }

    public Set<String> getRequiredCardNames2() {
        return requiredCardNames2;
    }

    public boolean doesOrderMatter() {
        return orderMatters;
    }

    public ComboEffect getEffect() {
        return effect;
    }

    /**
     * Checks if two cards match this combo's trigger conditions.
     * @param card1Name Name of the first card
     * @param card2Name Name of the second card
     * @return true if the cards trigger this combo
     */
    public boolean matches(String card1Name, String card2Name) {
        // Special case: Spell Synergy requires two DIFFERENT spells
        if (comboType == ComboType.SPELL_SYNERGY) {
            boolean card1IsSpell = requiredCardNames1.contains(card1Name);
            boolean card2IsSpell = requiredCardNames1.contains(card2Name);
            return card1IsSpell && card2IsSpell && !card1Name.equals(card2Name);
        }
        
        if (orderMatters) {
            // Order matters: card1 must be in set1, card2 must be in set2
            return (requiredCardNames1.contains(card1Name) && requiredCardNames2.contains(card2Name));
        } else {
            // Order doesn't matter: either combination works
            return (requiredCardNames1.contains(card1Name) && requiredCardNames2.contains(card2Name)) ||
                   (requiredCardNames1.contains(card2Name) && requiredCardNames2.contains(card1Name));
        }
    }

    // Creates all combo definitions for the game.
    public static ComboDefinition[] createAllComboDefinitions() {
        return new ComboDefinition[]{
            // 1. Tank + Support: Giant/Knight + ranged troop (Musketeer, Archers, Spear Goblins, Wizard)
            new ComboDefinition(
                ComboType.TANK_SUPPORT,
                new HashSet<>(Arrays.asList("Giant", "Knight")),
                new HashSet<>(Arrays.asList("Musketeer", "Archers", "Spear Goblins", "Wizard")),
                true, // Order matters: tank first, then support
                new ComboEffect(ComboEffectType.DAMAGE_BOOST, 0.15, "ranged") // +15% damage to ranged unit
            ),

            // 2. Spell Synergy: Any spell + different spell
            new ComboDefinition(
                ComboType.SPELL_SYNERGY,
                new HashSet<>(Arrays.asList("Zap", "Arrows", "Fireball", "Rocket")),
                new HashSet<>(Arrays.asList("Zap", "Arrows", "Fireball", "Rocket")),
                false, // Order doesn't matter, but must be different spells
                new ComboEffect(ComboEffectType.ELIXIR_REFUND, 1, null) // Refund 1 elixir
            ),

            // 3. Swarm Attack: Two swarm cards
            new ComboDefinition(
                ComboType.SWARM_ATTACK,
                new HashSet<>(Arrays.asList("Skeletons", "Goblins", "Spear Goblins", "Archers", 
                                           "Minions", "Minion Horde", "Barbarians")),
                new HashSet<>(Arrays.asList("Skeletons", "Goblins", "Spear Goblins", "Archers", 
                                           "Minions", "Minion Horde", "Barbarians")),
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.SPEED_BOOST, 0.10, "swarm") // +10% speed to swarm units
            ),

            // 4. Building Defense: Two building cards
            new ComboDefinition(
                ComboType.BUILDING_DEFENSE,
                new HashSet<>(Arrays.asList("Cannon", "Tesla", "Mortar", "Bomb Tower", "Inferno Tower",
                                           "Tombstone", "Goblin Hut", "Barbarian Hut", "Elixir Collector")),
                new HashSet<>(Arrays.asList("Cannon", "Tesla", "Mortar", "Bomb Tower", "Inferno Tower",
                                           "Tombstone", "Goblin Hut", "Barbarian Hut", "Elixir Collector")),
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.HEALTH_BOOST, 0.20, "building") // +20% HP to buildings
            ),

            // 5. Air Assault: Minions + Minion Horde
            new ComboDefinition(
                ComboType.AIR_ASSAULT,
                new HashSet<>(Arrays.asList("Minions")),
                new HashSet<>(Arrays.asList("Minion Horde")),
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.DAMAGE_BOOST, 0.15, "air") // +15% damage to air units
            ),

            // 6. Royal Combo: Knight + Archers
            new ComboDefinition(
                ComboType.ROYAL_COMBO,
                new HashSet<>(Arrays.asList("Knight")),
                new HashSet<>(Arrays.asList("Archers")),
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.FLAT_HEALTH_BOOST, 100, "Knight") // +100 HP to Knight
            ),

            // 7. Siege Mode: Mortar + defensive building
            new ComboDefinition(
                ComboType.SIEGE_MODE,
                new HashSet<>(Arrays.asList("Mortar")),
                new HashSet<>(Arrays.asList("Cannon", "Tesla", "Bomb Tower", "Inferno Tower")),
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.RANGE_BOOST, 2.0, "Mortar") // +2 tiles range to Mortar
            ),

            // 8. Rush Attack: Hog Rider + low-cost card (1-2 Elixir)
            new ComboDefinition(
                ComboType.RUSH_ATTACK,
                new HashSet<>(Arrays.asList("Hog Rider")),
                new HashSet<>(Arrays.asList("Skeletons", "Goblins", "Spear Goblins", "Zap")), // 1-2 elixir cards
                false, // Order doesn't matter
                new ComboEffect(ComboEffectType.SPEED_BOOST, 0.20, "Hog Rider") // +20% speed to Hog Rider
            )
        };
    }
}
