package com.example.kuroyale.model.quest;

/**
 * Enum defining the 15 possible daily quest types.
 * Each quest has a description, target value, and gold reward.
 */
public enum QuestType {
    // 1. Win 3 matches
    WIN_MATCHES("Win Matches", "Win %d matches", 3, 250),
    
    // 2. Destroy 5 Crown Towers
    DESTROY_TOWERS("Destroy Towers", "Destroy %d Crown Towers", 5, 200),
    
    // 3. Play 10 spell cards
    PLAY_SPELLS("Play Spells", "Play %d spell cards", 10, 150),
    
    // 4. Deploy 15 troop cards
    DEPLOY_TROOPS("Deploy Troops", "Deploy %d troop cards", 15, 175),
    
    // 5. Spend 100 total Elixir
    SPEND_ELIXIR("Spend Elixir", "Spend %d total Elixir", 100, 100),
    
    // 6. Win a match without losing a Crown Tower
    PERFECT_WIN("Perfect Win", "Win a match without losing a Crown Tower", 1, 300),
    
    // 7. Play 5 building cards
    PLAY_BUILDINGS("Play Buildings", "Play %d building cards", 5, 150),
    
    // 8. Deal 3000 damage with spells
    SPELL_DAMAGE("Spell Damage", "Deal %d damage with spells", 3000, 200),
    
    // 9. Win using only common cards
    COMMON_ONLY_WIN("Common Only Win", "Win using only common cards", 1, 250),
    
    // 10. Complete 2 challenges
    COMPLETE_CHALLENGES("Complete Challenges", "Complete %d challenges", 2, 300),
    
    // 11. Win a network multiplayer match
    WIN_MULTIPLAYER("Win Multiplayer", "Win a network multiplayer match", 1, 200),
    
    // 12. Play 20 cards in a single match
    PLAY_CARDS_SINGLE_MATCH("Card Master", "Play %d cards in a single match", 20, 150),
    
    // 13. Win 2 matches in a row
    WIN_STREAK("Win Streak", "Win %d matches in a row", 2, 300),
    
    // 14. Destroy an enemy King Tower
    DESTROY_KING("Destroy King", "Destroy an enemy King Tower", 1, 350),
    
    // 15. Win a PvP match
    WIN_PVP("Win PvP", "Win a PvP match", 1, 200);

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
        if (descriptionFormat.contains("%d")) {
            return String.format(descriptionFormat, targetValue);
        }
        return descriptionFormat;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public int getGoldReward() {
        return goldReward;
    }
}
