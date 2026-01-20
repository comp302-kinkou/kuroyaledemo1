package com.example.kuroyale.model.quest;

/**
 * Enum defining the 12 permanent achievement types.
 * Each achievement has a name, description, target value, and gold reward.
 */
public enum AchievementType {

    FIRST_BLOOD(
        "First Blood",
        "Win your first match",
        1,
        500
    ),

    TOWER_HUNTER(
        "Tower Hunter",
        "Destroy 50 Crown Towers total",
        50,
        750
    ),
    
    CHALLENGE_MASTER(
        "Challenge Master",
        "Complete all 5 challenges",
        5,
        1500
    ),

    THREE_STAR_HERO(
        "Three-Star Hero",
        "Get 3 stars on any challenge",
        1,
        600
    ),
    
    LEGENDARY_COLLECTOR(
        "Legendary Collector",
        "Upgrade a Legendary card to Level 3",
        1,
        1000
    ),
    
    NETWORK_WARRIOR(
        "Network Warrior",
        "Win 10 network multiplayer matches",
        10,
        800
    ),

    ARMY_BUILDER(
        "Army Builder",
        "Deploy 100 swarm troops total",
        100,
        700
    ),
    
    SPELL_MASTER(
        "Spell Master",
        "Deal 10,000 damage with spells total",
        10000,
        800
    ),
    
    GOLD_HOARDER(
        "Gold Hoarder",
        "Accumulate 5,000 total gold earned",
        5000,
        500
    ),
    
    VETERAN_PLAYER(
        "Veteran Player",
        "Play 50 matches",
        50,
        600
    ),
    
    COMBO_EXPERT(
        "Combo Expert",
        "Trigger 25 card combos",
        25,
        750
    ),
    
    UNDEFEATED(
        "Undefeated",
        "Win 5 matches in a row",
        5,
        1000
    );
    
    private final String displayName;
    private final String description;
    private final int targetValue;
    private final int goldReward;
    
    AchievementType(String displayName, String description, int targetValue, int goldReward) {
        this.displayName = displayName;
        this.description = description;
        this.targetValue = targetValue;
        this.goldReward = goldReward;
    }
    
    /**
     * @return The display name of this achievement
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return The description of how to earn this achievement
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return The target value needed to complete this achievement
     */
    public int getTargetValue() {
        return targetValue;
    }
    
    /**
     * @return The gold reward for completing this achievement
     */
    public int getGoldReward() {
        return goldReward;
    }
    
    /**
     * @return The ??? shown when the achievement is locked
     */
    public String getLockedHint() {
        return "???";
    }
}
