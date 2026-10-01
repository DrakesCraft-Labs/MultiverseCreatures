package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The walking bosses (Kinger, NIX, JackStar and the Sentinel's charge) step up one block at most,
 * never pass through a wall, and fall off ledges instead of hanging over them.
 */
class BossWalkTest {

    @Test
    @DisplayName("Level ground keeps the feet where they are; one block up is a step")
    void levelGroundAndSteps() {
        assertEquals(6.0, BossArena.nextFeetY(6.0, 6.0), 1e-9);
        assertEquals(7.0, BossArena.nextFeetY(6.0, 7.0), 1e-9);
        assertEquals(6.5, BossArena.nextFeetY(6.0, 6.5), 1e-9, "a slab is a step too");
    }

    @Test
    @DisplayName("Anything taller than a step is a wall, not something to climb")
    void wallsBlock() {
        assertTrue(Double.isNaN(BossArena.nextFeetY(6.0, 8.0)));
    }

    @Test
    @DisplayName("A ledge is fallen off gradually, and a bottomless drop still falls")
    void fallsGradually() {
        assertEquals(6.0 - BossArena.FALL_PER_TICK, BossArena.nextFeetY(6.0, 1.0), 1e-9);
        assertEquals(5.8, BossArena.nextFeetY(6.0, 5.8), 1e-9);
        assertEquals(6.0 - BossArena.FALL_PER_TICK, BossArena.nextFeetY(6.0, Double.NaN), 1e-9,
                "with no floor in reach the walker must not hang in the air");
    }

    @Test
    @DisplayName("Settling repeatedly lands exactly on the floor and stays there")
    void settlingConverges() {
        double feet = 12.3;
        for (int tick = 0; tick < 40; tick++) {
            feet = BossArena.nextFeetY(feet, 6.0);
        }
        assertEquals(6.0, feet, 1e-9);
    }
}
