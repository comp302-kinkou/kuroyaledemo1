package com.example.kuroyale.model.quest;

/**
 * Enum defining the 15 possible daily quest types.
 * Each quest has a description, target value, and gold reward.
 */
public enum QuestType {
    // Match-based quests
    PLAY_MATCHES("Play Matches", "Play %d matches", 3, 50),
    WIN_MATCHES("Win Matches", "Win %d matches", 2, 75),
    WIN_STREAK("Win Streak", "Win %d matches in a row", 2, 100),
    
    // Card deployment quests
    DEPLOY_TROOPS("Deploy Troops", "Deploy %d troop cards", 10, 40),
    DEPLOY_SPELLS("Cast Spells", "Cast %d spell cards", 5, 40),
    DEPLOY_BUILDINGS("Place Buildings", "Place %d building cards", 3, 40),
    
    // Combat quests
    DESTROY_TOWERS("Destroy Towers", "Destroy %d enemy towers", 3, 60),
    DEAL_DAMAGE("Deal Damage", "Deal %d total damage", 5000, 50),
    SPELL_DAMAGE("Spell Damage", "Deal %d spell damage", 1000, 60),
    
    // Combo quests
    TRIGGER_COMBOS("Trigger Combos", "Trigger %d card combos", 3, 80),
    
    // Challenge quests
    COMPLETE_CHALLENGE("Complete Challenge", "Complete any challenge", 1, 100),
    EARN_STARS("Earn Stars", "Earn %d total stars in challenges", 3, 75),
    
    // Resource quests
    EARN_GOLD("Earn Gold", "Earn %d gold", 200, 50),
    UPGRADE_CARD("Upgrade Card", "Upgrade any card", 1, 60),
    
    // Time-based quest
    DAILY_LOGIN("Daily Login", "Log in and play", 1, 25);

    private final String displayName;
    private final String descriptionFormat;
    private final int targetValue;
    private final int goldReward;

    QuestType(String displayName, String descriptionFormat, int targetValue, int goldReward) {
        this.displayName = displayName;
        this.descriptionFormat = descriptionFormat;
        this.targetValue = targetValue;
        this.goldReward = goldReward;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return String.format(descriptionFormat, targetValue);
    }

    public int getTargetValue() {
        return targetValue;
    }

    public int getGoldReward() {
        return goldReward;
    }
}
