package com.example.kuroyale.model;

import com.example.kuroyale.controller.GameController;

import com.example.kuroyale.model.persistence.GameData;
import com.example.kuroyale.model.persistence.PersistenceManager;
import com.example.kuroyale.model.persistence.PlayerProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class UpgradeCardTest {

    private GameController gameController;
    private PlayerProfile playerProfile;
    private Card testCard;
    private static final String SAVE_FILE = "test_upgrade_savegame.dat";

    @BeforeEach
    public void setUp() {
        // Reset game state by saving a clean state and forcing a reload
        GameData cleanData = new GameData();
        PersistenceManager.getInstance().save(cleanData, SAVE_FILE);

        // Force GameController to reload from the clean file
        gameController = GameController.getInstance();
        gameController.setSaveFileName(SAVE_FILE);
        gameController.loadGame();

        // Setup PlayerProfile with known state
        playerProfile = gameController.getPlayerProfile();
        playerProfile.setTotalGold(1000); // Start with enough gold

        // Get a specific test card (e.g., Common card like "Skeletons")
        testCard = CardLibrary.getCardByName("Skeletons");
    }

    @AfterEach
    public void tearDown() {
        // Clean up the save file
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testUpgradeCard_NullCard_ReturnsFalse() {
        boolean result = gameController.upgradeCard(null);
        assertFalse(result, "Should return false for null card");
    }

    @Test
    public void testUpgradeCard_InsufficientGold_ReturnsFalse() {
        // Setup card progression to confirm costs
        CardProgression progression = gameController.getCardProgression(testCard);
        int cost = progression.getUpgradeCost();

        // Set gold to less than cost
        playerProfile.setTotalGold(cost - 1);

        boolean result = gameController.upgradeCard(testCard);

        assertFalse(result, "Should return false when gold is insufficient");
        assertEquals(cost - 1, playerProfile.getTotalGold(), "Gold should not change on failure");
        assertEquals(1, progression.getLevel(), "Level should not change on failure");
    }

    @Test
    public void testUpgradeCard_Success() {
        // Get initial state
        CardProgression progression = gameController.getCardProgression(testCard);
        int initialLevel = progression.getLevel();
        int upgradeCost = progression.getUpgradeCost();
        int initialGold = playerProfile.getTotalGold();

        assertTrue(initialGold >= upgradeCost, "Setup should provide enough gold");

        // Execute upgrade
        boolean result = gameController.upgradeCard(testCard);

        // Verify effects
        assertTrue(result, "Upgrade should succeed");
        assertEquals(initialLevel + 1, progression.getLevel(), "Level should increase by 1");
        assertEquals(initialGold - upgradeCost, playerProfile.getTotalGold(), "Gold should decrease by cost");
        assertEquals(upgradeCost, progression.getTotalGoldSpent(), "Gold spent should be tracked");
    }

    @Test
    public void testUpgradeCard_MaxLevel_ReturnsFalse() {
        // Find a card and upgrade it to max level
        CardProgression progression = gameController.getCardProgression(testCard);

        // Level 1 -> 2
        gameController.upgradeCard(testCard);
        // Level 2 -> 3 (Max)
        gameController.upgradeCard(testCard);

        assertEquals(3, progression.getLevel(), "Card should be at max level 3");

        // Try to upgrade again (Level 3 -> 4 should fail)
        playerProfile.addGold(10000); // Ensure plenty of gold
        boolean result = gameController.upgradeCard(testCard);

        assertFalse(result, "Should not upgrade beyond max level");
        assertEquals(3, progression.getLevel(), "Level should remain at 3");
    }
}
