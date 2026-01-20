package com.example.kuroyale.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArenaTest {

    @Test
    public void testRandomizeBridgesDeterministic() {
        Arena arena1 = new Arena();
        Arena arena2 = new Arena();
        long seed = 12345L;

        arena1.randomizeBridges(seed);
        arena2.randomizeBridges(seed);

        assertEquals(arena1.getBridges().size(), arena2.getBridges().size(), "Bridge count should be identical");

        for (int i = 0; i < arena1.getBridges().size(); i++) {
            assertEquals(arena1.getBridges().get(i).x, arena2.getBridges().get(i).x, 0.001,
                    "Bridge " + i + " position should be identical");
        }
    }

    @Test
    public void testRandomizeBridgesDifferentSeeds() {
        Arena arena1 = new Arena();
        Arena arena2 = new Arena();

        arena1.randomizeBridges(11111L);
        arena2.randomizeBridges(99999L); // High chance to be different

        // Note: small chance they are same if RNG aligns, but unlikely for double
        // positions
        if (arena1.getBridges().size() == arena2.getBridges().size()) {
            boolean allSame = true;
            for (int i = 0; i < arena1.getBridges().size(); i++) {
                if (Math.abs(arena1.getBridges().get(i).x - arena2.getBridges().get(i).x) > 0.001) {
                    allSame = false;
                    break;
                }
            }
            assertFalse(allSame, "Different seeds should likely produce different bridges");
        }
    }
}
