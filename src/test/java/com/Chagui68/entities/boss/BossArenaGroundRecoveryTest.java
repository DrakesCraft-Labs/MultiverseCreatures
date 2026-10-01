package com.Chagui68.entities.boss;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the terrain queries behind the boss ground-recovery fallback.
 *
 * The bug this replaces was invisible in production: {@code getGroundY} returned the boss's own Y
 * when there was nothing under it, so "no floor" and "standing on the floor" were the same value
 * and a grounded boss simply stopped attacking. {@link BossArena#findFloorY} exists to tell them
 * apart, and these tests pin that distinction down.
 *
 * The block scanning is driven through probes, so no server and no {@code Material} registry are
 * needed; the two Bukkit-facing overloads are only exercised for their documented fallbacks.
 */
class BossArenaGroundRecoveryTest {

    /** A probe where every column is solid up to {@code floorY}, which is therefore the floor. */
    private static BossArena.ColumnProbe floorEverywhere(int floorY) {
        return (x, z) -> floorY;
    }

    /** A probe with solid blocks at or below {@code topSolidY}. */
    private static BossArena.VerticalProbe solidBelow(int topSolidY) {
        return blockY -> blockY <= topSolidY;
    }

    /** A world whose blocks are all missing, so every query falls through to its empty answer. */
    private static World emptyWorld() {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getBlockAt":
                            return null;
                        case "equals":
                            return proxy == args[0];
                        case "hashCode":
                            return 42;
                        case "getName":
                            return "test_world";
                        case "getUID":
                            return UUID.fromString("00000000-0000-0000-0000-000000000003");
                        default:
                            return null;
                    }
                });
    }

    // --- Vertical scan ---------------------------------------------------------------------

    @Test
    @DisplayName("findFloorY reports the top of the first solid block below")
    void testFindFloorBelowSolidGround() {
        assertEquals(65.0, BossArena.findFloorY(70, 80, solidBelow(64)), 1e-9);
    }

    @Test
    @DisplayName("A boss hovering less than a block up is told where the floor really is")
    void findFloorYIgnoresTheHoverFraction() {
        // The Sentinel stuck at 64.5 over a floor at 64: the old scan answered 64.5, its own Y.
        assertEquals(64.0, BossArena.findFloorY(64.5, 80, solidBelow(63)), 1e-9);
        assertEquals(64.0, BossArena.findFloorY(64.05, 80, solidBelow(63)), 1e-9);
        assertEquals(64.0, BossArena.findFloorY(65.7, 80, solidBelow(63)), 1e-9);
        assertEquals(64.0, BossArena.findFloorY(64.0, 80, solidBelow(63)), 1e-9);
    }

    @Test
    @DisplayName("A body sunk into a block is pushed back on top of it")
    void findFloorYLiftsABodyOutOfTheBlockItIsIn() {
        assertEquals(64.0, BossArena.findFloorY(63.4, 80, solidBelow(63)), 1e-9);
    }

    @Test
    @DisplayName("A slab is stood on at its own height, not at the next full block")
    void findFloorYUsesTheSurfaceHeight() {
        BossArena.SurfaceProbe slabOnRock = y -> y == 63 ? 63.5 : (y < 63 ? y + 1.0 : Double.NaN);
        assertEquals(63.5, BossArena.findFloorY(63.5, 80, slabOnRock), 1e-9);
        assertEquals(63.5, BossArena.findFloorY(66.0, 80, slabOnRock), 1e-9);
    }

    @Test
    @DisplayName("On the ground means within the tolerance of the floor findFloorY reports")
    void restsOnUsesTheSameFloor() {
        assertTrue(BossArena.restsOn(64.0, 64.0));
        assertTrue(BossArena.restsOn(64.15, 64.0));
        assertFalse(BossArena.restsOn(64.5, 64.0), "half a block up is airborne, and must be pulled down");
        assertFalse(BossArena.restsOn(64.0, Double.NaN), "no floor is never ground");
    }

    @Test
    @DisplayName("findFloorY is NaN when the scan reaches no solid block")
    void testFindFloorReturnsNaNOverVoid() {
        // Solid floor far below, but the scan range does not reach it.
        assertTrue(Double.isNaN(BossArena.findFloorY(400, 80, solidBelow(64))),
                "A scan that reaches nothing must be distinguishable from a floor");
    }

    @Test
    @DisplayName("getGroundY keeps its fallback of returning the current Y")
    void testGetGroundYFallbackIsUnchanged() {
        Location boss = new Location(emptyWorld(), 10, 400, -5);

        // Documented legacy behaviour: the convenience version cannot say "nothing underneath".
        assertEquals(400.0, BossArena.getGroundY(boss, 80), 1e-9);
        // ... which is exactly why findFloorY exists.
        assertTrue(Double.isNaN(BossArena.findFloorY(boss, 80)));
    }

    // --- Column order -----------------------------------------------------------------------

    @Test
    @DisplayName("ringOffsets starts at the origin and grows by non-decreasing distance")
    void testRingOffsetsAreOrderedByDistance() {
        List<int[]> offsets = BossArena.ringOffsets(4);

        assertArrayEquals(new int[]{0, 0}, offsets.get(0), "The origin must be probed first");

        Set<String> seen = new HashSet<>();
        double previous = -1;
        for (int[] offset : offsets) {
            assertTrue(seen.add(offset[0] + "," + offset[1]),
                    "Duplicate offset " + offset[0] + "," + offset[1]);
            assertTrue(Math.max(Math.abs(offset[0]), Math.abs(offset[1])) <= 4,
                    "Offset outside the requested radius");
            double distance = Math.hypot(offset[0], offset[1]);
            assertTrue(distance >= previous, "Offsets must not go back towards the origin");
            previous = distance;
        }
        assertEquals(1 + countWithin(4), offsets.size());
    }

    @Test
    @DisplayName("ringOffsets of a non-positive radius is just the origin")
    void testRingOffsetsNegativeRadius() {
        List<int[]> offsets = BossArena.ringOffsets(-3);

        assertEquals(1, offsets.size());
        assertArrayEquals(new int[]{0, 0}, offsets.get(0));
        assertEquals(1, BossArena.ringOffsets(0).size());
    }

    private static int countWithin(int radius) {
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius && (dx != 0 || dz != 0)) {
                    count++;
                }
            }
        }
        return count;
    }

    // --- Column search ----------------------------------------------------------------------

    @Test
    @DisplayName("findUsableColumn prefers the column the boss is already in")
    void testUsableColumnPrefersOwnColumn() {
        int[] column = BossArena.findUsableColumn(3, 7, 8, floorEverywhere(64));

        assertNotNull(column);
        assertArrayEquals(new int[]{3, 64, 7}, column);
    }

    @Test
    @DisplayName("findUsableColumn walks outward to the nearest column with ground")
    void testUsableColumnWalksOutward() {
        // The boss's column is void; terrain only exists from x >= 2 onwards.
        int[] column = BossArena.findUsableColumn(0, 0, 8, (x, z) -> x >= 2 ? 64 : Double.NaN);

        assertNotNull(column, "A nearby column has ground, so one must be found");
        assertArrayEquals(new int[]{2, 64, 0}, column, "The nearest usable column must win");
    }

    @Test
    @DisplayName("findUsableColumn returns null when nothing within the radius has ground")
    void testUsableColumnOverVoid() {
        assertNull(BossArena.findUsableColumn(0, 0, 6, (x, z) -> Double.NaN),
                "No safe column must be reported as null so the caller can try the next strategy");
        assertNull(BossArena.findUsableColumn(0, 0, 2, (x, z) -> x == 5 ? 64 : Double.NaN),
                "Ground outside the requested radius must not be used");
        assertNull(BossArena.findUsableColumn(0, 0, 2, null));
    }

    @Test
    @DisplayName("findGroundRestingPlace stands the boss one block above the floor")
    void testRestingPlaceCentreOffset() {
        Location singleColumnWorld = new Location(emptyWorld(), 3, 70, 7);

        // The empty world can never provide ground, so the caller must be told so.
        assertNull(BossArena.findGroundRestingPlace(singleColumnWorld, 4, 80));
        assertNull(BossArena.findGroundRestingPlace(null, 4, 80));
    }
}
