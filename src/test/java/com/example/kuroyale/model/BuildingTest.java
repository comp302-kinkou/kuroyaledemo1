package com.example.kuroyale.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BuildingTest {

    private Building defensiveBuilding;
    private Building spawnerBuilding;
    private Building elixirCollector;

    @BeforeEach
    public void setUp() {
        // Initialize different types of buildings for testing

        // Defensive Building: Cannon (Lifetime 30s)
        defensiveBuilding = new Building("Cannon", 10, 10, true, 1000, 150, 5, 1.0, 30.0, "DEFENSIVE",
                TransportType.GROUND, TargetType.GROUND);

        // Spawner Building: Barbarian Hut (Lifetime 60s, Spawns every 10s)
        spawnerBuilding = new Building("Barbarian Hut", 5, 5, true, 2000, 0, 0, 0, 60.0, "SPAWNER",
                TransportType.GROUND, TargetType.NONE);
        // Manually set spawner properties since they aren't in constructor for generic
        // Building (often set by factory)
        spawnerBuilding.setSpawnerProperties("Barbarian", 10.0);

        // Special: Elixir Collector (Lifetime 70s, Generates every 8.5s - default in
        // code is 10s but let's test default)
        // Note: Default elixir generation interval in Building.java is 10.0s
        elixirCollector = new Building("Elixir Collector", 2, 2, true, 1000, 0, 0, 0, 70.0, "SPECIAL",
                TransportType.GROUND, TargetType.NONE);
    }

    /**
     * Test Case 1: Lifetime Expiry
     * Verifies that a building is destroyed when its lifetime is exceeded.
     */
    @Test
    public void testLifetimeExpiry() {
        // Initial state
        assertFalse(defensiveBuilding.isDestroyed(), "Building should not be destroyed initially");
        assertEquals(0.0, defensiveBuilding.getTimeAlive(), 0.001);

        // Advance time by half lifetime
        defensiveBuilding.update(15.0);
        assertFalse(defensiveBuilding.isDestroyed(), "Building should be alive after 15s");
        assertEquals(15.0, defensiveBuilding.getTimeAlive(), 0.001);

        // Advance time to exactly lifetime
        defensiveBuilding.update(15.0);
        assertTrue(defensiveBuilding.isDestroyed(), "Building should be destroyed after reaching lifetime");
    }

    /**
     * Test Case 2: Spawner Logic
     * Verifies that a spawner building triggers spawns at correct intervals.
     */
    @Test
    public void testSpawnerLogic() {
        assertTrue(spawnerBuilding.isSpawner(), "Should be identified as a spawner");

        // Advance time by 9.9 seconds (should not spawn yet)
        spawnerBuilding.update(9.9);
        assertFalse(spawnerBuilding.shouldSpawn(), "Should not spawn before interval");

        // Advance time by 0.2 seconds (total 10.1s) - should trigger spawn
        spawnerBuilding.update(0.2);
        assertTrue(spawnerBuilding.shouldSpawn(), "Should spawn after interval passed");

        // After check, it resets internally (based on implementation pattern usually)
        // OR the method `shouldSpawn` resets it. Let's check the code assumption for
        // `shouldSpawn`
        // Building.java: if (timeSinceLastSpawn >= interval) { timeSinceLastSpawn = 0;
        // return true; }

        // So checking it again immediately should be false
        assertFalse(spawnerBuilding.shouldSpawn(), "Should consume spawn flag immediately");
    }

    /**
     * Test Case 3: Elixir Generation Logic
     * Verifies that an Elixir Collector generates elixir at correct intervals.
     */
    @Test
    public void testElixirGeneration() {
        assertTrue(elixirCollector.isElixirCollector(), "Should be identified as elixir collector");

        // Default interval is 10.0s
        // Update by 5.0s
        elixirCollector.update(5.0);
        assertFalse(elixirCollector.shouldGenerateElixir(), "Should not generate elixir yet");

        // Update by another 5.0s (Total 10.0s)
        elixirCollector.update(5.0);
        assertTrue(elixirCollector.shouldGenerateElixir(), "Should generate elixir at 10s");

        // Should reset after generating
        assertFalse(elixirCollector.shouldGenerateElixir(), "Should reset timer after generation");
    }
}
