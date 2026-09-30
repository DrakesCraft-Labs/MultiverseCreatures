package com.Chagui68.testsupport;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The recording sink stands in for a chunk while the terrain tests run, so its bookkeeping has to
 * be exact: a key that two positions share makes a test read a block that belongs to a neighbour.
 * That is not hypothetical — an earlier version of this class packed 25 bits per coordinate where
 * 21 belonged, and the arena's flat floor started reading twenty blocks tall.
 */
class RecordingTerrainTest {

    @Test
    @DisplayName("Position keys round-trip, negative coordinates included")
    void keysRoundTrip() {
        int[] values = {0, 1, -1, 15, -15, 255, -256, 2048, -2048, 1_000_000, -1_000_000};
        for (int x : values) {
            for (int z : values) {
                for (int y : new int[]{-64, -1, 0, 1, 5, 63, 320}) {
                    long key = RecordingTerrain.key(x, y, z);
                    assertEquals(x, RecordingTerrain.keyX(key), "x of " + x + "," + y + "," + z);
                    assertEquals(y, RecordingTerrain.keyY(key), "y of " + x + "," + y + "," + z);
                    assertEquals(z, RecordingTerrain.keyZ(key), "z of " + x + "," + y + "," + z);
                }
            }
        }
    }

    @Test
    @DisplayName("Different positions never share a key")
    void keysAreUnique() {
        Set<Long> keys = new HashSet<>();
        for (int x = -40; x <= 40; x++) {
            for (int z = -40; z <= 40; z++) {
                for (int y = 0; y <= 40; y++) {
                    assertTrue(keys.add(RecordingTerrain.key(x, y, z)),
                            "two positions share a key at " + x + "," + y + "," + z);
                }
            }
        }
    }

    @Test
    @DisplayName("A column's height is tracked per column, not per key")
    void topFollowsTheColumn() {
        RecordingTerrain terrain = RecordingTerrain.window(0, 0, 16);
        terrain.column(3, 4, 1, 9, Material.DEEPSLATE);
        terrain.set(3, 20, 4, Material.OBSIDIAN);
        assertEquals(20, terrain.topY(3, 4));
        assertEquals(Material.DEEPSLATE, terrain.at(3, 1, 4));
        assertEquals(Material.AIR, terrain.at(3, 10, 4));
        assertEquals(Integer.MIN_VALUE, terrain.topY(3, 5), "an untouched column has no height");
        assertEquals(0, terrain.dropped(), "every write was inside the window");
    }

    @Test
    @DisplayName("Writes outside the window are counted, not kept")
    void outsideWritesAreDropped() {
        RecordingTerrain terrain = RecordingTerrain.window(0, 0, 16);
        terrain.set(1, 5, 1, Material.STONE);
        terrain.set(16, 5, 1, Material.STONE);
        terrain.set(1, 5, -1, Material.STONE);
        terrain.set(1, 400, 1, Material.STONE);
        assertEquals(1, terrain.size());
        assertEquals(3, terrain.dropped(), "the three out-of-window writes must be counted");
        assertNotEquals(0, terrain.dropped());
    }
}
