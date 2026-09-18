package com.Chagui68.ritual;

import org.bukkit.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossInvocationStructureTest {

    @Test
    @DisplayName("Verify Sentinel invocation circle center is at (2, 0, 2)")
    void testCenterLocation() {
        Location origin = new Location(null, 50, 10, -50);
        Location center = BossInvocationStructure.getCenterLocation(origin);

        assertEquals(52.0, center.getX(), 0.001);
        assertEquals(10.0, center.getY(), 0.001);
        assertEquals(-48.0, center.getZ(), 0.001);
    }

    @Test
    @DisplayName("Verify containsCandle correctly validates 12 candle ring positions")
    void testContainsCandle() {
        Location origin = new Location(null, 0, 64, 0);

        // Edges have 3 candles each (total 12)
        // Top edge: (1,0,0), (2,0,0), (3,0,0)
        assertTrue(BossInvocationStructure.containsCandle(origin, new Location(null, 1, 64, 0)));
        assertTrue(BossInvocationStructure.containsCandle(origin, new Location(null, 2, 64, 0)));
        assertTrue(BossInvocationStructure.containsCandle(origin, new Location(null, 3, 64, 0)));

        // Center must be empty (where Echo Shard is dropped)
        assertFalse(BossInvocationStructure.containsCandle(origin, new Location(null, 2, 64, 2)));

        // Outside ring must return false
        assertFalse(BossInvocationStructure.containsCandle(origin, new Location(null, 5, 64, 5)));
        assertFalse(BossInvocationStructure.containsCandle(origin, new Location(null, -1, 64, 0)));
    }
}
