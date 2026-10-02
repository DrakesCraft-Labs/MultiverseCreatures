package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DioInvocationStructureTest {

    @Test
    @DisplayName("Four yellow candles sit right next to the throne, on the ground layer")
    void candlesSurroundTheThrone() {
        assertEquals(4, DioInvocationStructure.CANDLE_OFFSETS.length);
        for (int[] offset : DioInvocationStructure.CANDLE_OFFSETS) {
            assertEquals(0, offset[1]);
            assertEquals(1, Math.abs(offset[0] - 2) + Math.abs(offset[2] - 2));
        }
    }

    @Test
    @DisplayName("The pillars stand on the four corners of the 5x5 square")
    void pillarsOnTheCorners() {
        assertEquals(4, DioInvocationStructure.CORNER_OFFSETS.length);
        for (int[] corner : DioInvocationStructure.CORNER_OFFSETS) {
            assertTrue((corner[0] == 0 || corner[0] == 4) && (corner[1] == 0 || corner[1] == 4));
        }
    }

    @Test
    @DisplayName("The clock is offered on top of the throne, at the centre of the square")
    void throneIsTheCentre() {
        Location throne = DioInvocationStructure.getThroneLocation(new Location(null, 10, 50, -20));
        assertEquals(12.5, throne.getX(), 1e-9);
        assertEquals(51.0, throne.getY(), 1e-9);
        assertEquals(-17.5, throne.getZ(), 1e-9);
        List<Location> tops = DioInvocationStructure.getPillarTops(new Location(null, 0, 60, 0));
        assertEquals(4, tops.size());
        for (Location top : tops) assertEquals(62.6, top.getY(), 1e-9, "the light starts above the head");
    }

    @Test
    @DisplayName("Any skull or head, standing or on a wall, crowns a pillar; nothing else does")
    void headsCrownThePillars() {
        assertTrue(DioInvocationStructure.isHead(Material.PLAYER_HEAD));
        assertTrue(DioInvocationStructure.isHead(Material.WITHER_SKELETON_WALL_SKULL));
        assertFalse(DioInvocationStructure.isHead(Material.LANTERN));
        assertFalse(DioInvocationStructure.isHead(Material.GOLD_BLOCK));
    }
}
