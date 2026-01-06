package com.example.kuroyale.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UpgradeCardCostTest {

    @Test
    public void testCommonUpgradeCostLevel1to2() {
        // Verify that a Common card upgrading from Level 1 to 2 costs 200 gold.
        CardProgression card = new CardProgression("Knight", CardRarity.COMMON, 1);
        assertEquals(200, card.getUpgradeCost(), "Common card upgrade from Level 1 to 2 should cost 200 gold.");
    }

    @Test
    public void testLegendaryUpgradeCostLevel2to3() {
        // Verify that a Legendary card upgrading from Level 2 to 3 costs 4000 gold.
        CardProgression card = new CardProgression("Mega Knight", CardRarity.LEGENDARY, 2);
        assertEquals(4000, card.getUpgradeCost(), "Legendary card upgrade from Level 2 to 3 should cost 4000 gold.");
    }

    @Test
    public void testMaxLevelUpgradeCost() {
        // Verify that passing currentLevel = 3 returns 0 (handling the max level edge
        // case).
        CardProgression card = new CardProgression("Maxed Card", CardRarity.COMMON, 3);
        assertEquals(0, card.getUpgradeCost(), "Upgrade cost should be 0 for max level (3) cards.");
    }
}
