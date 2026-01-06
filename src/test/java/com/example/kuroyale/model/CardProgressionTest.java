package com.example.kuroyale.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardProgressionTest {

    private CardProgression progression;
    private CardRarity rarity;

    @BeforeEach
    public void setUp() {
        // Mock a rarity with known costs
        // Common: L1->L2 = 5, L2->L3 = 10 (example values based on likely enum logic,
        // but we can trust the class under test to pull from actual enum effectively.
        // Let's rely on actual CardRarity enum logic if available, or just test
        // behavior)
        rarity = CardRarity.COMMON;
        progression = new CardProgression("Knight", rarity);
    }

    /**
     * Test Case 1: Initial State and RepOk
     * Verifies that the object starts in a valid state according to the ADT specs.
     */
    @Test
    public void testInitialState() {
        assertNotNull(progression.getCardName());
        assertEquals(1, progression.getLevel(), "Initial level should be 1");
        assertEquals(0, progression.getTotalGoldSpent(), "Initial gold spent should be 0");
        assertTrue(progression.repOk(), "Rep invariant should hold after initialization");
    }

    /**
     * Test Case 2: Upgrade Success
     * Verifies that upgrading increments level and maintains RepOk.
     */
    @Test
    public void testUpgradeSuccess() {
        assertTrue(progression.canUpgrade(), "Should be able to upgrade from level 1");

        // Perform upgrade
        boolean result = progression.upgrade();

        assertTrue(result, "Upgrade should return true");
        assertEquals(2, progression.getLevel(), "Level should be 2 after upgrade");
        assertTrue(progression.repOk(), "Rep invariant should hold after upgrade");
    }

    /**
     * Test Case 3: Upgrade Fail Max Level
     * Verifies that upgrading fails when max level (3) is reached.
     */
    @Test
    public void testUpgradeFailMaxLevel() {
        // Upgrade to 2
        progression.upgrade();
        // Upgrade to 3
        progression.upgrade();

        assertEquals(3, progression.getLevel());
        assertFalse(progression.canUpgrade(), "Should not be able to upgrade at level 3");

        // Try to upgrade again
        boolean result = progression.upgrade();

        assertFalse(result, "Upgrade should return false at max level");
        assertEquals(3, progression.getLevel(), "Level should remain at 3");
        assertTrue(progression.repOk());
    }

    /**
     * Test Case 4: Add Gold Spent
     * Verifies that gold spent accumulates correctly and maintains RepOk
     * (non-negative).
     */
    @Test
    public void testAddGoldSpent() {
        progression.addGoldSpent(100);
        assertEquals(100, progression.getTotalGoldSpent());
        assertTrue(progression.repOk());

        progression.addGoldSpent(50);
        assertEquals(150, progression.getTotalGoldSpent());
        assertTrue(progression.repOk());
    }

    /**
     * Test Case 5: Upgrade Cost Logic
     * Verifies that upgrade costs are retrieved correctly based on level.
     */
    @Test
    public void testUpgradeCost() {
        // At Level 1, cost should be for L1->L2
        int cost1 = progression.getUpgradeCost();
        assertTrue(cost1 > 0, "Upgrade cost should be positive");

        progression.upgrade();
        // At Level 2, cost should be for L2->L3
        int cost2 = progression.getUpgradeCost();
        assertTrue(cost2 > 0, "Upgrade cost should be positive");
        assertTrue(cost2 > cost1, "Higher level upgrade usually costs more (Common rarity check)");

        progression.upgrade();
        // At Level 3 (Max), cost logic might return 0 or remain valid but unused.
        // Spec says: if level is not 1 or 2, return 0.
        assertEquals(0, progression.getUpgradeCost(), "Upgrade cost should be 0 at max level");
    }
}
