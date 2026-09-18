package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RitualStructureTest {

    @Test
    @DisplayName("Verify center of Overworld ritual is at (3, 0, 3) with radius 5")
    void testCenterLocation() {
        Location origin = new Location(null, 100, 70, 200);
        Location center = RitualStructure.getCenterLocation(origin);

        assertEquals(103.0, center.getX(), 0.001);
        assertEquals(70.0, center.getY(), 0.001);
        assertEquals(203.0, center.getZ(), 0.001);
        assertEquals(5.0, RitualStructure.getRadius(), 0.001);
    }

    @Test
    @DisplayName("Verify Overworld ritual has exactly 12 candles on Y=1 surrounding center")
    void testCandleLocationsCountAndElevation() {
        Map<Location, Material> candleMap = RitualStructure.getCandleLocations();
        assertEquals(12, candleMap.size(), "Ritual structure must define exactly 12 candle locations");

        for (Location loc : candleMap.keySet()) {
            assertEquals(1, loc.getBlockY(), "All candles in ritual structure must be placed on layer Y=1");
            int dx = Math.abs(loc.getBlockX() - 3);
            int dz = Math.abs(loc.getBlockZ() - 3);
            assertTrue(dx > 0 || dz > 0, "No candle can be placed at the center (3, 1, 3)");
        }
    }
}
