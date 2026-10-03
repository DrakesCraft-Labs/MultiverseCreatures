package com.Chagui68.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the Head Slime gelatin immunity window.
 *
 * It used to be a plain {@code Set<UUID>} plus a scheduled removal 200 ticks later, so eating a
 * second gelatin did not extend the window — the first timer ended it early — and the entry
 * survived a logout until that timer fired. It is now a deadline per player.
 */
class HeadSlimeImmunityTest {

    @Test
    void activeHeadSlimesHaveABoundedAutomaticPopulation() {
        assertTrue(HeadSlime.hasCapacity(11, 12));
        assertFalse(HeadSlime.hasCapacity(12, 12));
        assertFalse(HeadSlime.hasCapacity(99, 12));
    }

    @Test
    void invalidCapacityConfigurationStillAllowsOnlyOneHeadSlime() {
        assertEquals(1, HeadSlime.effectiveMaxActive(0));
        assertEquals(1, HeadSlime.effectiveMaxActive(-4));
        assertTrue(HeadSlime.hasCapacity(0, 0));
        assertFalse(HeadSlime.hasCapacity(1, 0));
    }

    @Test
    @DisplayName("Verify a player is not immune before eating anything")
    void testNotImmuneByDefault() {
        assertFalse(HeadSlime.isImmune(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Verify eating gelatin opens an immunity window that expires on its own")
    void testImmunityExpires() {
        UUID player = UUID.randomUUID();
        HeadSlime.grantImmunity(player, 200);

        long now = System.currentTimeMillis();
        assertTrue(HeadSlime.isImmune(player), "Immunity must apply immediately");
        assertTrue(HeadSlime.isImmune(player, now + 9_000L), "Still inside the 10 s window");
        assertFalse(HeadSlime.isImmune(player, now + 11_000L), "Window must be over");
        assertFalse(HeadSlime.isImmune(player), "An expired window must be dropped");
    }

    @Test
    @DisplayName("Verify a second gelatin refreshes the deadline instead of being ignored")
    void testSecondGelatinRefreshes() {
        UUID player = UUID.randomUUID();
        HeadSlime.grantImmunity(player, 200);

        long now = System.currentTimeMillis();
        // The first window ends at ~10 s; a longer window must replace it, not coexist with it.
        assertFalse(HeadSlime.isImmune(player, now + 11_000L));
        HeadSlime.grantImmunity(player, 400);
        assertTrue(HeadSlime.isImmune(player, now + 15_000L),
                "The refresh must extend the deadline, not leave the first one in place");
    }

    @Test
    @DisplayName("Verify clearing immunity takes effect immediately")
    void testClearImmunity() {
        UUID player = UUID.randomUUID();
        HeadSlime.grantImmunity(player, 200);
        HeadSlime.clearImmunity(player);

        assertFalse(HeadSlime.isImmune(player));
    }

    @Test
    @DisplayName("Verify a null player is never immune")
    void testNullPlayer() {
        assertFalse(HeadSlime.isImmune(null));
        HeadSlime.grantImmunity(null, 200);
        HeadSlime.clearImmunity(null);
    }

    @Test
    @DisplayName("Verify disabling the plugin drops every window")
    void testClearAllImmunity() {
        UUID player = UUID.randomUUID();
        HeadSlime.grantImmunity(player, 200);
        HeadSlime.clearAllImmunity();

        assertFalse(HeadSlime.isImmune(player));
    }
}
