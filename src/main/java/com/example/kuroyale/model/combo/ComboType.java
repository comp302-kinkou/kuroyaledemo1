package com.example.kuroyale.model.combo;

// Enum representing the different types of combos in the game.
public enum ComboType {
    TANK_SUPPORT("Tank + Support", "Gold glow around ranged unit"),
    SPELL_SYNERGY("Spell Synergy", "Sparkle effect when spell is cast"),
    SWARM_ATTACK("Swarm Attack", "Speed lines appear on units"),
    BUILDING_DEFENSE("Building Defense", "Shield icon appears on buildings"),
    AIR_ASSAULT("Air Assault", "Lightning effect around flying units"),
    ROYAL_COMBO("Royal Combo", "Crown icon appears above Knight"),
    SIEGE_MODE("Siege Mode", "Range indicator circle expands"),
    RUSH_ATTACK("Rush Attack", "Dust trail behind Hog Rider");

    private final String displayName;
    private final String visualDescription;

    ComboType(String displayName, String visualDescription) {
        this.displayName = displayName;
        this.visualDescription = visualDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVisualDescription() {
        return visualDescription;
    }
}