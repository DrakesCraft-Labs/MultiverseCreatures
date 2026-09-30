package com.Chagui68.ritual.terrain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shape is what the fight needs, so these tests are written as invariants rather than as a
 * picture: the floor is flat, the wall cannot be walked out of, the canyons and peaks stay far
 * away, and the arena does not change from world to world.
 */
class ArenaShapeTest {

    private static final long SEED = 20_260_930L;
    private static final int SALT = ArenaNoise.saltOf(SEED) ^ 0x5EED;

    @Test
    @DisplayName("The terrace arithmetic stays consistent with the radii")
    void terracesFillTheRing() {
        assertEquals(ArenaShape.RIM_FIRST_STEP_Y + 3 * ArenaShape.RIM_STEP_HEIGHT, ArenaShape.RIM_TOP_Y);
        assertEquals(ArenaShape.RIM_RADIUS - ArenaShape.ARENA_RADIUS,
                4.0 * ArenaShape.RIM_STEP_DEPTH, 1.0e-9,
                "the four terraces must exactly fill the ring between the floor and the rim");
        assertTrue(ArenaShape.RIM_MIN_WALL_Y > ArenaShape.FLOOR_Y + 3,
                "the first terrace has to be too tall to jump");
    }

    @Test
    @DisplayName("The arena floor is perfectly flat, right up to the wall")
    void theFloorIsFlat() {
        for (int x = -70; x <= 70; x++) {
            for (int z = -70; z <= 70; z++) {
                if (ArenaShape.distance(x, z) > ArenaShape.ARENA_RADIUS) continue;
                assertEquals(ArenaShape.FLOOR_Y, ArenaShape.surfaceY(x, z, SEED),
                        "the floor must stay flat at " + x + "," + z + ": players build on it and the "
                                + "bosses' ground queries were tuned on it");
            }
        }
    }

    @Test
    @DisplayName("The wall rises in terraces, each too tall to walk up")
    void theWallCannotBeClimbed() {
        assertTrue(minimumRimHeight(ArenaShape.ARENA_RADIUS + 0.5) - ArenaShape.FLOOR_Y >= 4,
                "the floor itself has to step up to the first terrace");

        for (double boundary = ArenaShape.ARENA_RADIUS + ArenaShape.RIM_STEP_DEPTH;
                boundary < ArenaShape.RIM_RADIUS; boundary += ArenaShape.RIM_STEP_DEPTH) {
            int inside = minimumRimHeight(boundary - 0.5);
            int outside = minimumRimHeight(boundary + 0.5);
            assertTrue(outside - inside >= 4, "the step at r=" + boundary + " is only " + (outside - inside)
                    + " blocks: a player could climb out");
        }
    }

    /** The lowest point of the wall at a radius, sampled all the way round. */
    private static int minimumRimHeight(double radius) {
        int lowest = Integer.MAX_VALUE;
        for (int step = 0; step < 3600; step++) {
            double angle = Math.toRadians(step * 0.1);
            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);
            lowest = Math.min(lowest, ArenaShape.rimY(x, z, radius, SALT));
        }
        return lowest;
    }

    @Test
    @DisplayName("Every column of the ring is at least a terrace above the floor")
    void theRingIsAWallEverywhere() {
        for (int step = 0; step < 1440; step++) {
            double angle = Math.toRadians(step * 0.25);
            for (double r = ArenaShape.ARENA_RADIUS + 0.5; r <= ArenaShape.RIM_RADIUS; r += 1.0) {
                int x = (int) Math.round(Math.cos(angle) * r);
                int z = (int) Math.round(Math.sin(angle) * r);
                int height = ArenaShape.rimY(x, z, r, SALT);
                assertTrue(height >= ArenaShape.RIM_MIN_WALL_Y,
                        "the ring drops to " + height + " at radius " + r);
            }
        }
    }

    @Test
    @DisplayName("Buttress ribs crown the wall and fade out into the wilderness")
    void ribsCrownTheCrown() {
        int highest = Integer.MIN_VALUE;
        for (int step = 0; step < 720; step++) {
            double angle = Math.toRadians(step * 0.5);
            int x = (int) Math.round(Math.cos(angle) * (ArenaShape.RIM_RADIUS - 2.0));
            int z = (int) Math.round(Math.sin(angle) * (ArenaShape.RIM_RADIUS - 2.0));
            highest = Math.max(highest, ArenaShape.surfaceY(x, z, SEED));
        }
        assertTrue(highest >= ArenaShape.RIM_TOP_Y + 10,
                "no rib rises over the parapet: the silhouette is a plain cylinder, highest was " + highest);
    }

    @Test
    @DisplayName("Canyons never touch the coliseum")
    void canyonsStayAway() {
        for (int x = -200; x <= 200; x += 3) {
            for (int z = -200; z <= 200; z += 3) {
                double distance = ArenaShape.distance(x, z);
                if (distance > ArenaShape.CHASM_INNER_RADIUS) continue;
                assertEquals(0.0, ArenaShape.canyonStrength(x, z, distance, SALT), 1.0e-12,
                        "a canyon reaches the coliseum at " + x + "," + z);
            }
        }
    }

    @Test
    @DisplayName("Far out, canyons cut to the floor and peaks climb")
    void theWildernessHasCanyonsAndPeaks() {
        int deepest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        boolean floodedBottom = false;
        for (int x = -520; x <= 520; x += 6) {
            for (int z = -520; z <= 520; z += 6) {
                double distance = ArenaShape.distance(x, z);
                if (distance < ArenaShape.CHASM_INNER_RADIUS + 80) continue;
                int height = ArenaShape.surfaceY(x, z, SEED);
                deepest = Math.min(deepest, height);
                highest = Math.max(highest, height);
                if (height <= ArenaShape.CHASM_FLOOR_Y + 1) floodedBottom = true;
            }
        }
        assertTrue(highest >= 44, "no obsidian peak anywhere, highest was " + highest);
        assertTrue(deepest <= ArenaShape.WASTE_BASE_Y - 20,
                "no canyon carves deep enough, deepest was " + deepest);
        assertTrue(floodedBottom || deepest <= ArenaShape.CHASM_FLOOR_Y + 4,
                "no canyon reaches down to the flooded floor, deepest was " + deepest);
    }

    @Test
    @DisplayName("The arena is the same in every world; the wilderness is not")
    void theArenaIgnoresTheSeed() {
        for (int x = -60; x <= 60; x += 6) {
            for (int z = -60; z <= 60; z += 6) {
                if (!ArenaShape.inArena(x, z)) continue;
                assertEquals(ArenaShape.surfaceY(x, z, 1L), ArenaShape.surfaceY(x, z, 2L),
                        "the coliseum is a set piece: it must not shift from world to world");
            }
        }
        int differences = 0;
        for (int x = 200; x <= 400; x += 7) {
            for (int z = -200; z <= 200; z += 7) {
                if (ArenaShape.surfaceY(x, z, 1L) != ArenaShape.surfaceY(x, z, 2L)) differences++;
            }
        }
        assertTrue(differences > 100, "the wilderness ignores the world seed, it looks hand placed");
    }

    @Test
    @DisplayName("Zones meet exactly at their radii")
    void zonesSwitchAtTheRadii() {
        assertEquals(ArenaShape.Zone.ARENA, ArenaShape.zone(0, 0));
        assertEquals(ArenaShape.Zone.ARENA, ArenaShape.zone(64, 0));
        assertEquals(ArenaShape.Zone.RIM, ArenaShape.zone(65, 0));
        assertEquals(ArenaShape.Zone.RIM, ArenaShape.zone(0, 79));
        assertEquals(ArenaShape.Zone.WASTE, ArenaShape.zone(0, 81));
    }

    @Test
    @DisplayName("The spawn height is the one players have always landed at")
    void spawnClearsTheFloor() {
        assertEquals(10, ArenaShape.spawnY(),
                "changing this moves where the ritual drops players, so it is pinned on purpose");
        assertTrue(ArenaShape.spawnY() > ArenaShape.FLOOR_Y);
    }

    @Test
    @DisplayName("Landmarks are only ever scattered past the inner radius")
    void landmarksStayOut() {
        for (int cellX = -6; cellX <= 6; cellX++) {
            for (int cellZ = -6; cellZ <= 6; cellZ++) {
                ArenaLandmarks.Landmark landmark = ArenaLandmarks.at(cellX, cellZ, SEED);
                if (landmark == null) continue;
                assertTrue(ArenaShape.distance(landmark.x(), landmark.z()) >= ArenaShape.LANDMARK_INNER_RADIUS,
                        "a landmark stands inside the view of the arena at " + landmark.x() + "," + landmark.z());
            }
        }
    }

    @Test
    @DisplayName("The shards hang over the crown, above everything the fight uses")
    void shardsFloatAboveTheFight() {
        assertEquals(ArenaShape.RIB_COUNT, ArenaLandmarks.skyShards().size(),
                "one shard between each pair of ribs");
        for (ArenaLandmarks.Landmark shard : ArenaLandmarks.skyShards()) {
            assertTrue(ArenaShape.distance(shard.x(), shard.z()) > ArenaShape.RIM_RADIUS,
                    "a shard hangs over the wall itself");
            assertTrue(shard.y() - 8 > ArenaShape.ARENA_CLEAR_Y,
                    "a shard hangs low enough to be in the way of the fight: " + shard.y());
        }
    }
}
