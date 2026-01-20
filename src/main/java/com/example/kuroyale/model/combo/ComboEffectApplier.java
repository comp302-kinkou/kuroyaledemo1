package com.example.kuroyale.model.combo;

import com.example.kuroyale.model.Building;
import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardLibrary;
import com.example.kuroyale.model.Unit;
import java.util.List;

// Utility class for applying combo effects to units and buildings.
public class ComboEffectApplier {

    /**
     * Applies a combo effect to the appropriate units/buildings based on the effect target.
     * 
     * @param detectedCombo The detected combo with its effect
     * @param activeUnits List of all active units in the game
     * @param activeBuildings List of all active buildings in the game
     * @param isPlayer Whether the combo was triggered by the player
     */
    public static void applyComboEffect(DetectedCombo detectedCombo, 
                                       List<Unit> activeUnits, 
                                       List<Building> activeBuildings,
                                       boolean isPlayer) {
        ComboEffect effect = detectedCombo.getEffect();
        ComboEffectType effectType = effect.getEffectType();
        String target = effect.getTarget();
        double value = effect.getValue();

        // Special case: Elixir refund is handled separately (not applied to units/buildings)
        if (effectType == ComboEffectType.ELIXIR_REFUND) {
            return; // Elixir refund is handled in GameController
        }

        // Apply effects based on target type
        switch (target) {
            case "ranged":
                applyToRangedUnits(activeUnits, isPlayer, effectType, value);
                break;
            case "swarm":
                applyToSwarmUnits(activeUnits, isPlayer, effectType, value);
                break;
            case "air":
                applyToAirUnits(activeUnits, isPlayer, effectType, value);
                break;
            case "building":
                applyToBuildings(activeBuildings, isPlayer, effectType, value);
                break;
            default:
                // Specific card name (e.g., "Knight", "Hog Rider", "Mortar")
                applyToSpecificCard(activeUnits, activeBuildings, isPlayer, target, effectType, value);
                break;
        }
    }

    // Applies effect to ranged units (Musketeer, Archers, Spear Goblins, Wizard).
    private static void applyToRangedUnits(List<Unit> activeUnits, boolean isPlayer, 
                                          ComboEffectType effectType, double value) {
        String[] rangedUnits = {"Musketeer", "Archers", "Spear Goblins", "Wizard"};
        
        for (Unit unit : activeUnits) {
            if (unit.isPlayer() == isPlayer && !unit.isDead()) {
                for (String rangedName : rangedUnits) {
                    if (unit.getName().equals(rangedName)) {
                        applyEffectToUnit(unit, effectType, value);
                        break;
                    }
                }
            }
        }
    }

    // Applies effect to swarm units.
    private static void applyToSwarmUnits(List<Unit> activeUnits, boolean isPlayer, 
                                         ComboEffectType effectType, double value) {
        for (Unit unit : activeUnits) {
            if (unit.isPlayer() == isPlayer && !unit.isDead()) {
                if (CardLibrary.isSwarmCard(unit.getName())) {
                    applyEffectToUnit(unit, effectType, value);
                }
            }
        }
    }

    // Applies effect to air units (Minions, Minion Horde).
    private static void applyToAirUnits(List<Unit> activeUnits, boolean isPlayer, 
                                       ComboEffectType effectType, double value) {
        for (Unit unit : activeUnits) {
            if (unit.isPlayer() == isPlayer && !unit.isDead()) {
                if (unit.getTransportType() == com.example.kuroyale.model.TransportType.AIR) {
                    applyEffectToUnit(unit, effectType, value);
                }
            }
        }
    }

    // Applies effect to all buildings.
    private static void applyToBuildings(List<Building> activeBuildings, boolean isPlayer, 
                                        ComboEffectType effectType, double value) {
        for (Building building : activeBuildings) {
            if (building.isPlayer() == isPlayer && !building.isDestroyed()) {
                applyEffectToBuilding(building, effectType, value);
            }
        }
    }

    // Applies effect to a specific card by name.
    private static void applyToSpecificCard(List<Unit> activeUnits, List<Building> activeBuildings, 
                                           boolean isPlayer, String cardName, 
                                           ComboEffectType effectType, double value) {
        // Check units
        for (Unit unit : activeUnits) {
            if (unit.isPlayer() == isPlayer && !unit.isDead() && unit.getName().equals(cardName)) {
                applyEffectToUnit(unit, effectType, value);
            }
        }
        
        // Check buildings
        for (Building building : activeBuildings) {
            if (building.isPlayer() == isPlayer && !building.isDestroyed() && 
                building.getName().equals(cardName)) {
                applyEffectToBuilding(building, effectType, value);
            }
        }
    }

    // Applies a combo effect to a unit.
    private static void applyEffectToUnit(Unit unit, ComboEffectType effectType, double value) {
        switch (effectType) {
            case DAMAGE_BOOST:
                unit.applyDamageMultiplier(1.0 + value);
                break;
            case SPEED_BOOST:
                unit.applySpeedMultiplier(1.0 + value);
                break;
            case HEALTH_BOOST:
                unit.applyHealthMultiplier(1.0 + value);
                break;
            case FLAT_HEALTH_BOOST:
                unit.addHealth(value);
                break;
            case RANGE_BOOST:
                unit.addRange(value);
                break;
            default:
                break;
        }
    }

    // Applies a combo effect to a building.
    private static void applyEffectToBuilding(Building building, ComboEffectType effectType, double value) {
        switch (effectType) {
            case DAMAGE_BOOST:
                building.applyDamageMultiplier(1.0 + value);
                break;
            case HEALTH_BOOST:
                building.applyHealthMultiplier(1.0 + value);
                break;
            case RANGE_BOOST:
                building.addRange(value);
                break;
            default:
                break;
        }
    }
}