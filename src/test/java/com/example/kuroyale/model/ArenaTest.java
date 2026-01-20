package com.example.kuroyale.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArenaTest {

    @Test
    public void testFixedBridges() {
        Arena arena = new Arena();
        arena.setupFixedBridges();

        assertEquals(2, arena.getBridges().size(), "Should always have 2 bridges");

        Arena.Bridge b1 = arena.getBridges().get(0);
        Arena.Bridge b2 = arena.getBridges().get(1);

        assertEquals(5.0, b1.x, 0.001);
        assertEquals(11.0, b2.x, 0.001);
    }
}
