package com.Chagui68.ritual.terrain;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the real column writer and checks what a fight actually gets: a flat, empty arena, ground
 * a boss can walk on around it, lava that can never run anywhere, a sealed world and a closed edge.
 */
class WastelandTest {

    private static final long SEED = 1234L;
    private static final Wasteland LAND = new Wasteland(SEED);

    /** One column's blocks, by height. */
    private static TreeMap<Integer, Material> column(Wasteland land, int x, int z) {
        TreeMap<Integer, Material> blocks = new TreeMap<>();
        land.write(x, z, (from, to, material) -> {
            for (int y = from; y <= to; y++) blocks.put(y, material);
        });
        return blocks;
    }

    @Test
    @DisplayName("The arena is flat at FLOOR_Y, paved, and nothing stands on it or around it")
    void theArenaIsFlatAndClear() {
        for (int x = -90; x <= 90; x += 3) {
            for (int z = -90; z <= 90; z += 3) {
                double r = Math.hypot(x, z);
                if (r > Wasteland.ARENA_RADIUS + 40) continue;
                TreeMap<Integer, Material> blocks = column(LAND, x, z);
                int top = LAND.surfaceY(x, z);
                assertEquals(top, (int) blocks.lastKey(), "something stands near the arena at " + x + "," + z);
                if (r <= Wasteland.ARENA_RADIUS) {
                    assertEquals(Wasteland.FLOOR_Y, top, "the arena floor is not flat at " + x + "," + z);
                }
            }
        }
    }

    @Test
    @DisplayName("Around the arena a boss can walk anywhere: no step is taller than one block")
    void theSurroundingsAreWalkable() {
        for (int x = -110; x <= 110; x++) {
            for (int z = -110; z <= 110; z += 2) {
                if (Math.hypot(x, z) > Wasteland.HAZARD_RADIUS) continue;
                assertTrue(Math.abs(LAND.surfaceY(x + 1, z) - LAND.surfaceY(x, z)) <= 1, "a wall at " + x + "," + z);
                assertTrue(Math.abs(LAND.surfaceY(x, z + 1) - LAND.surfaceY(x, z)) <= 1, "a wall at " + x + "," + z);
            }
        }
    }

    @Test
    @DisplayName("No lava within the hazard radius: the fight starts on safe ground")
    void noLavaNearTheArena() {
        for (int x = -110; x <= 110; x += 2) {
            for (int z = -110; z <= 110; z += 2) {
                if (Math.hypot(x, z) > Wasteland.HAZARD_RADIUS) continue;
                assertFalse(column(LAND, x, z).containsValue(Material.LAVA), "lava at " + x + "," + z);
            }
        }
    }

    @Test
    @DisplayName("Lava is always walled in by rock or more lava, sideways and below")
    void lavaIsContained() {
        Map<Long, TreeMap<Integer, Material>> cache = new HashMap<>();
        int lava = 0;
        for (int x = -400; x <= 400; x += 7) {
            for (int z = -400; z <= 400; z += 7) {
                TreeMap<Integer, Material> here = cached(cache, x, z);
                for (Map.Entry<Integer, Material> block : here.entrySet()) {
                    if (block.getValue() != Material.LAVA) continue;
                    int y = block.getKey();
                    assertTrue(y <= Wasteland.LAVA_Y, "lava above the lava level at " + x + "," + y + "," + z);
                    assertNotNull(here.get(y - 1), "lava over air at " + x + "," + y + "," + z);
                    for (int[] side : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        assertNotNull(cached(cache, x + side[0], z + side[1]).get(y),
                                "lava at " + x + "," + y + "," + z + " flows out sideways");
                    }
                    lava++;
                }
            }
        }
        assertTrue(lava > 0, "the wasteland has no lava at all");
    }

    @Test
    @DisplayName("Every column is sealed with bedrock and solid from there to its surface")
    void theWorldIsSealed() {
        for (int x = -740; x <= 740; x += 37) {
            for (int z = -740; z <= 740; z += 37) {
                TreeMap<Integer, Material> blocks = column(LAND, x, z);
                assertEquals(Material.BEDROCK, blocks.get(Wasteland.BEDROCK_Y));
                assertEquals(Wasteland.BEDROCK_Y, (int) blocks.firstKey(), "something under the bedrock");
                for (int y = Wasteland.BEDROCK_Y; y <= LAND.surfaceY(x, z); y++) {
                    assertNotNull(blocks.get(y), "a hole at " + x + "," + y + "," + z);
                }
                assertTrue(blocks.lastKey() < 320, "the column leaves the world at " + x + "," + z);
            }
        }
    }

    @Test
    @DisplayName("A mountain range closes the battlefield before the border")
    void theEdgeIsClosed() {
        for (int along = -700; along <= 700; along += 50) {
            for (int[] edge : new int[][]{{along, 745}, {along, -745}, {745, along}, {-745, along}}) {
                assertTrue(LAND.surfaceY(edge[0], edge[1]) >= Wasteland.FLOOR_Y + 20,
                        "the range is missing at " + edge[0] + "," + edge[1]);
            }
        }
    }

    @Test
    @DisplayName("Every kind of hazard and structure appears on the battlefield")
    void everyFeatureAppears() {
        Set<Material> seen = EnumSet.noneOf(Material.class);
        for (int x = -500; x <= 500; x += 2) {
            for (int z = -500; z <= 500; z += 2) {
                int surface = LAND.surfaceY(x, z);
                column(LAND, x, z).forEach((y, material) -> {
                    if (y >= surface) seen.add(material);
                });
            }
        }
        for (Material expected : new Material[]{Material.LAVA, Material.MAGMA_BLOCK, Material.SOUL_FIRE,
                Material.FIRE, Material.OBSIDIAN, Material.BONE_BLOCK, Material.CHISELED_POLISHED_BLACKSTONE,
                Material.SOUL_SAND, Material.NETHERRACK, Material.BLACKSTONE, Material.CRYING_OBSIDIAN}) {
            assertTrue(seen.contains(expected), expected + " never appears");
        }
    }

    @Test
    @DisplayName("The same seed builds the same column, another seed a different battlefield")
    void seedsMatter() {
        Wasteland same = new Wasteland(SEED);
        Wasteland other = new Wasteland(SEED + 1);
        int differences = 0;
        for (int x = -600; x <= 600; x += 40) {
            for (int z = -600; z <= 600; z += 40) {
                assertEquals(column(LAND, x, z), column(same, x, z));
                if (LAND.surfaceY(x, z) != other.surfaceY(x, z)) differences++;
            }
        }
        assertTrue(differences > 300, "two seeds grew almost the same world: " + differences);
        assertEquals(column(LAND, 0, 0), column(other, 0, 0), "the arena is the same in every world");
    }

    private static TreeMap<Integer, Material> cached(Map<Long, TreeMap<Integer, Material>> cache, int x, int z) {
        return cache.computeIfAbsent(((long) x << 32) ^ (z & 0xffffffffL), key -> column(LAND, x, z));
    }
}
