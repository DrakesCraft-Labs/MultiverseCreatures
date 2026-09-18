package com.Chagui68.ritual;

import org.bukkit.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NixInvocationStructureTest {

    @Test
    @DisplayName("Verify scaffold candle offsets surround anvil at distance 1")
    void testCandleOffsetsSymmetry() {
        assertEquals(4, NixInvocationStructure.CANDLE_OFFSETS.length, "Scaffold must have exactly 4 red candles");

        for (int[] offset : NixInvocationStructure.CANDLE_OFFSETS) {
            int dx = Math.abs(offset[0] - 2);
            int dy = Math.abs(offset[1]);
            int dz = Math.abs(offset[2] - 2);

            assertEquals(0, dy, "Candles must be on the ground layer (Y=0)");
            assertEquals(1, dx + dz, "Each candle must be Manhattan distance 1 from the anvil at (2, 0, 2)");
        }
    }

    @Test
    @DisplayName("Verify corner gallows form symmetric 5x5 bounding square")
    void testCornerGallowsSymmetry() {
        assertEquals(4, NixInvocationStructure.CORNER_OFFSETS.length, "Scaffold must have 4 corner gallows");

        for (int[] corner : NixInvocationStructure.CORNER_OFFSETS) {
            int x = corner[0];
            int z = corner[1];
            assertTrue((x == 0 || x == 4) && (z == 0 || z == 4),
                    "Corner offsets must be at the perimeter bounds of the 5x5 area");
        }
    }

    @Test
    @DisplayName("Verify anvil center location is correctly computed")
    void testAnvilCenterCalculation() {
        Location origin = new Location(null, 10, 50, -20);
        Location anvilLoc = NixInvocationStructure.getAnvilLocation(origin);

        assertEquals(12.5, anvilLoc.getX(), 0.001);
        assertEquals(50.5, anvilLoc.getY(), 0.001);
        assertEquals(-17.5, anvilLoc.getZ(), 0.001);
    }

    @Test
    @DisplayName("Verify corner gallows locations are elevated to Y+1.8")
    void testCornerGallowsLocations() {
        Location origin = new Location(null, 0, 60, 0);
        List<Location> gallows = NixInvocationStructure.getCornerGallowsLocations(origin);

        assertEquals(4, gallows.size());
        for (Location loc : gallows) {
            assertEquals(61.8, loc.getY(), 0.001, "Gallows particle origin must be elevated near skull height");
        }
    }

    @Test
    @DisplayName("Verify containsCandle correctly identifies candles and rejects other blocks")
    void testContainsCandle() {
        Location origin = new Location(null, 100, 10, 100);

        // Valid candles at (2, 0, 1), (2, 0, 3), (1, 0, 2), (3, 0, 2)
        assertTrue(NixInvocationStructure.containsCandle(origin, new Location(null, 102, 10, 101)));
        assertTrue(NixInvocationStructure.containsCandle(origin, new Location(null, 102, 10, 103)));
        assertTrue(NixInvocationStructure.containsCandle(origin, new Location(null, 101, 10, 102)));
        assertTrue(NixInvocationStructure.containsCandle(origin, new Location(null, 103, 10, 102)));

        // Invalid: center anvil position
        assertFalse(NixInvocationStructure.containsCandle(origin, new Location(null, 102, 10, 102)));
        // Invalid: corner gallows position
        assertFalse(NixInvocationStructure.containsCandle(origin, new Location(null, 100, 10, 100)));
        // Invalid: outside structure
        assertFalse(NixInvocationStructure.containsCandle(origin, new Location(null, 105, 10, 105)));
    }
}
