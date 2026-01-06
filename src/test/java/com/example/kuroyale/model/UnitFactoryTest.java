package com.example.kuroyale.model;

import com.example.kuroyale.controller.UnitFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnitFactoryTest {

    private Card testCard;

    @BeforeEach
    public void setUp() {

        testCard = new Card(
                "TestUnit",
                3,
                "TROOP",
                5.0,
                100.0, // Damage
                1.0, // Hit Speed
                1.0, // Speed
                1000.0, // Health
                "image_path", // Image Path
                TransportType.GROUND,
                TargetType.GROUND);
    }

    // Test Case 1: Create Unit Without Progression (Null)
    @Test
    public void testCreateUnitWithoutProgression() {
        Unit unit = UnitFactory.createUnit(testCard, 10, 10, true, null);

        assertNotNull(unit);
        assertEquals(1000.0, unit.getHealth(), 0.001, "Health should match base card health");
        assertEquals(100.0, unit.getDamage(), 0.001, "Damage should match base card damage");
    }

    // Test Case 2: Create Unit With Level 1 Progression
    @Test
    public void testCreateUnitWithLevel1Progression() {
        CardProgression progression = new CardProgression("TestUnit", CardRarity.COMMON);
        // Default level is 1

        Unit unit = UnitFactory.createUnit(testCard, 10, 10, true, progression);

        assertEquals(1000.0, unit.getHealth(), 0.001, "Level 1 health should be 1.0x");
        assertEquals(100.0, unit.getDamage(), 0.001, "Level 1 damage should be 1.0x");
    }

    // Test Case 3: Create Unit With Level 3 Progression
    @Test
    public void testCreateUnitWithLevel3Progression() {
        CardProgression progression = new CardProgression("TestUnit", CardRarity.COMMON);
        progression.upgrade(); // Lv 2
        progression.upgrade(); // Lv 3

        Unit unit = UnitFactory.createUnit(testCard, 10, 10, true, progression);

        // Expected: 1000 * 1.2 = 1200
        // Expected: 100 * 1.2 = 120, rounded

        assertEquals(1200.0, unit.getHealth(), 0.001, "Level 3 health should be 1.2x");
        assertEquals(120.0, unit.getDamage(), 0.001, "Level 3 damage should be 1.2x");
    }
}
