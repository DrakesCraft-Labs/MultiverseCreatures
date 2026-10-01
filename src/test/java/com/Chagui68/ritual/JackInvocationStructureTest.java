package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JackInvocationStructureTest {

    @Test
    @DisplayName("The core, candles and base accept the blocks the ritual documents, and nothing else")
    void materials() {
        assertTrue(JackInvocationStructure.isValidCore(Material.RESPAWN_ANCHOR));
        assertTrue(JackInvocationStructure.isValidCore(Material.LODESTONE));
        assertFalse(JackInvocationStructure.isValidCore(Material.STONE));

        assertTrue(JackInvocationStructure.isValidCandle(Material.CYAN_CANDLE));
        assertTrue(JackInvocationStructure.isValidCandle(Material.LIGHT_BLUE_CANDLE));
        assertFalse(JackInvocationStructure.isValidCandle(Material.RED_CANDLE));

        assertTrue(JackInvocationStructure.isValidBase(Material.CRYING_OBSIDIAN));
        assertTrue(JackInvocationStructure.isValidBase(Material.POLISHED_BLACKSTONE_BRICKS));
        assertFalse(JackInvocationStructure.isValidBase(Material.DIRT));
    }

    @Test
    @DisplayName("Four candles surround the core and four pillars mark the corners of the 5x5")
    void layout() {
        assertEquals(4, JackInvocationStructure.CANDLE_OFFSETS.length);
        for (int[] candle : JackInvocationStructure.CANDLE_OFFSETS) {
            assertEquals(1, Math.abs(candle[0] - 2) + Math.abs(candle[2] - 2), "candles sit next to the core");
        }
        assertEquals(4, JackInvocationStructure.CORNER_OFFSETS.length);
        for (int[] corner : JackInvocationStructure.CORNER_OFFSETS) {
            assertTrue((corner[0] == 0 || corner[0] == 4) && (corner[1] == 0 || corner[1] == 4));
        }
    }

    @Test
    @DisplayName("Only the four candle spots count as candles")
    void containsCandle() {
        Location origin = new Location(null, 100, 10, 100);
        assertTrue(JackInvocationStructure.containsCandle(origin, new Location(null, 102, 10, 101)));
        assertTrue(JackInvocationStructure.containsCandle(origin, new Location(null, 103, 10, 102)));
        assertFalse(JackInvocationStructure.containsCandle(origin, new Location(null, 102, 10, 102)), "the core");
        assertFalse(JackInvocationStructure.containsCandle(origin, new Location(null, 100, 10, 100)), "a pillar");
    }
}
