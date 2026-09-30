package com.Chagui68.ritual.terrain;

import com.Chagui68.ritual.JackInvocationStructure;
import com.Chagui68.ritual.NixInvocationStructure;
import com.Chagui68.testsupport.RecordingTerrain;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the generator into a recording sink and checks what players and bosses will actually
 * stand on.
 *
 * <p>These are the tests the flat crying-obsidian plain used to be: the floor is flat and solid,
 * nothing stands in the play volume, the plaza is built from blocks the invocation structures
 * accept, the wall encloses the arena and the world is sealed from below. None of them need a
 * server, because the generator writes through {@link TerrainSink} — the chunk sink is the only
 * part that knows about chunks at all.
 */
class BossArenaTerrainTest {

    private static final long SEED = 7_777L;
    private static final long OTHER_SEED = 7_778L;
    private static final int CHUNK = 16;

    /** Chunks covering the block range -64..79, i.e. the whole arena and a slice of the wall. */
    private static final int ARENA_CHUNK_MIN = -4;
    private static final int ARENA_CHUNK_MAX = 4;

    @Test
    @DisplayName("The arena floor is flat, solid and clear above")
    void theArenaFloorIsFlatAndSolid() {
        for (int chunkX = ARENA_CHUNK_MIN; chunkX <= ARENA_CHUNK_MAX; chunkX++) {
            for (int chunkZ = ARENA_CHUNK_MIN; chunkZ <= ARENA_CHUNK_MAX; chunkZ++) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (int x = chunkX * CHUNK; x < chunkX * CHUNK + CHUNK; x++) {
                    for (int z = chunkZ * CHUNK; z < chunkZ * CHUNK + CHUNK; z++) {
                        if (ArenaShape.distance(x, z) > 60.0) continue;

                        assertEquals(ArenaShape.FLOOR_Y, terrain.topY(x, z),
                                "the arena floor is not flat at " + x + "," + z);
                        for (int y = ArenaShape.BEDROCK_Y; y <= ArenaShape.FLOOR_Y; y++) {
                            assertTrue(terrain.has(x, y, z), "the floor has a hole at " + x + "," + y + "," + z);
                        }
                        for (int y = ArenaShape.FLOOR_Y + 1; y <= ArenaShape.ARENA_CLEAR_Y; y++) {
                            assertFalse(terrain.has(x, y, z),
                                    "something stands in the play volume at " + x + "," + y + "," + z
                                            + ": the bosses' ground queries and the seal placement were tuned "
                                            + "on an empty arena");
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("The plaza is paved with the blocks the invocation structures accept")
    void thePlazaPavementIsAccepted() {
        int paved = 0;
        for (int chunkX = ARENA_CHUNK_MIN; chunkX <= ARENA_CHUNK_MAX; chunkX++) {
            for (int chunkZ = ARENA_CHUNK_MIN; chunkZ <= ARENA_CHUNK_MAX; chunkZ++) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (int x = chunkX * CHUNK; x < chunkX * CHUNK + CHUNK; x++) {
                    for (int z = chunkZ * CHUNK; z < chunkZ * CHUNK + CHUNK; z++) {
                        if (ArenaShape.distance(x, z) > ArenaShape.PLAZA_RADIUS) continue;
                        Material pavement = terrain.at(x, ArenaShape.FLOOR_Y, z);
                        assertTrue(ArenaPalette.PLAZA_PAVEMENT.contains(pavement),
                                "the plaza is paved with " + pavement + " at " + x + "," + z
                                        + ", which is not one of the blocks both rituals accept");
                        assertTrue(JackInvocationStructure.isValidBase(pavement),
                                "JACKSTAR's structure cannot be built on " + pavement);
                        assertTrue(NixInvocationStructure.isValidBase(pavement),
                                "NIX's structure cannot be built on " + pavement);
                        paved++;
                    }
                }
            }
        }
        assertTrue(paved > 3000, "the plaza should be thousands of columns, found " + paved);
    }

    @Test
    @DisplayName("The plaza lights itself from its own pavement")
    void thePlazaIsLit() {
        int glow = 0;
        int total = 0;
        for (int chunkX = ARENA_CHUNK_MIN; chunkX <= ARENA_CHUNK_MAX; chunkX++) {
            for (int chunkZ = ARENA_CHUNK_MIN; chunkZ <= ARENA_CHUNK_MAX; chunkZ++) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (int x = chunkX * CHUNK; x < chunkX * CHUNK + CHUNK; x++) {
                    for (int z = chunkZ * CHUNK; z < chunkZ * CHUNK + CHUNK; z++) {
                        if (ArenaShape.distance(x, z) > ArenaShape.PLAZA_RADIUS) continue;
                        total++;
                        if (terrain.at(x, ArenaShape.FLOOR_Y, z) == ArenaPalette.PLAZA_GLOW) glow++;
                    }
                }
            }
        }
        double share = (double) glow / total;
        // The dimension is stuck at midnight (doDaylightCycle off, time 14000) and the only lights
        // in the play volume are the ones in the floor, so the pavement has to carry enough of them
        // to see the fight — but not so many that the sigil stops reading as a sigil.
        assertTrue(share > 0.05 && share < 0.45,
                "glowing pavement is " + Math.round(share * 100) + "% of the plaza, which is outside 5-45%");
    }

    @Test
    @DisplayName("The wall is built above the floor all the way round")
    void theWallEnclosesTheArena() {
        int columns = 0;
        for (int chunkX = ARENA_CHUNK_MIN; chunkX <= ARENA_CHUNK_MAX; chunkX++) {
            for (int chunkZ = ARENA_CHUNK_MIN; chunkZ <= ARENA_CHUNK_MAX; chunkZ++) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (int x = chunkX * CHUNK; x < chunkX * CHUNK + CHUNK; x++) {
                    for (int z = chunkZ * CHUNK; z < chunkZ * CHUNK + CHUNK; z++) {
                        double distance = ArenaShape.distance(x, z);
                        if (distance <= ArenaShape.ARENA_RADIUS || distance > ArenaShape.RIM_RADIUS) continue;
                        assertTrue(terrain.topY(x, z) - ArenaShape.FLOOR_Y >= 4,
                                "the wall is only " + (terrain.topY(x, z) - ArenaShape.FLOOR_Y)
                                        + " blocks tall at " + x + "," + z + ": the arena is open");
                        columns++;
                    }
                }
            }
        }
        assertTrue(columns > 3000, "the ring should be thousands of columns, found " + columns);
    }

    @Test
    @DisplayName("The world is sealed from below, canyons included")
    void theWorldIsSealed() {
        for (int chunkX = -4; chunkX <= 12; chunkX += 4) {
            for (int chunkZ = -4; chunkZ <= 12; chunkZ += 4) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (int x = chunkX * CHUNK; x < chunkX * CHUNK + CHUNK; x += 3) {
                    for (int z = chunkZ * CHUNK; z < chunkZ * CHUNK + CHUNK; z += 3) {
                        int surface = ArenaShape.surfaceY(x, z, SEED);
                        int bedrock = surface >= ArenaShape.BEDROCK_Y
                                ? ArenaShape.BEDROCK_Y
                                : surface - BossArenaGenerator.CANYON_ROCK_DEPTH;
                        assertEquals(Material.BEDROCK, terrain.at(x, bedrock, z),
                                "no bedrock seal at " + x + "," + bedrock + "," + z);
                        assertFalse(terrain.has(x, bedrock - 1, z),
                                "the world is open under the seal at " + x + "," + (bedrock - 1) + "," + z);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Soul fire only ever stands on soul sand")
    void soulFireStandsOnSoulSand() {
        int fires = 0;
        for (int chunkX = 16; chunkX <= 24; chunkX++) {
            for (int chunkZ = 16; chunkZ <= 24; chunkZ++) {
                RecordingTerrain terrain = generate(chunkX, chunkZ);
                for (Map.Entry<Long, Material> entry : terrain.snapshot().entrySet()) {
                    if (entry.getValue() != Material.SOUL_FIRE) continue;
                    long key = entry.getKey();
                    int y = RecordingTerrain.keyY(key);
                    int z = RecordingTerrain.keyZ(key);
                    int x = RecordingTerrain.keyX(key);
                    assertEquals(Material.SOUL_SAND, terrain.at(x, y - 1, z),
                            "soul fire at " + x + "," + y + "," + z + " would go out: no soul sand under it");
                    fires++;
                }
            }
        }
        assertTrue(fires > 0, "the wilderness has no soul fire at all: the blue flames vanished");
    }

    @Test
    @DisplayName("Lava only pools at the bottom of a canyon")
    void lavaStaysAtTheCanyonFloor() {
        int[] canyonChunk = findCanyonChunk();
        assertNotNull(canyonChunk, "the wilderness has no canyon deep enough to flood");
        RecordingTerrain terrain = generate(canyonChunk[0], canyonChunk[1]);

        int lava = 0;
        for (Map.Entry<Long, Material> entry : terrain.snapshot().entrySet()) {
            if (entry.getValue() != Material.LAVA) continue;
            int y = RecordingTerrain.keyY(entry.getKey());
            assertTrue(y <= ArenaShape.CHASM_FLOOR_Y + 1,
                    "lava sits at y=" + y + ", above the canyon floor: it would run down the walls");
            lava++;
        }
        assertTrue(lava > 0, "the canyon floor is not flooded at " + canyonChunk[0] + "," + canyonChunk[1]);
    }

    @Test
    @DisplayName("Neighbouring chunks agree: the same column, the same blocks")
    void chunksAgreeAcrossBorders() {
        RecordingTerrain together = RecordingTerrain.window(0, 0, 32);
        for (int chunkX = 0; chunkX <= 1; chunkX++) {
            for (int chunkZ = 0; chunkZ <= 1; chunkZ++) {
                BossArenaGenerator.generateArea(chunkX * CHUNK, chunkZ * CHUNK, SEED, together);
            }
        }
        Map<Long, Material> reference = together.snapshot();
        assertTrue(reference.size() > 5000, "generated almost nothing: " + reference.size());

        for (int chunkX = 0; chunkX <= 1; chunkX++) {
            for (int chunkZ = 0; chunkZ <= 1; chunkZ++) {
                RecordingTerrain alone = generate(chunkX, chunkZ);
                for (Map.Entry<Long, Material> entry : alone.snapshot().entrySet()) {
                    assertEquals(entry.getValue(), reference.get(entry.getKey()),
                            "chunk " + chunkX + "," + chunkZ + " disagrees with the same column generated "
                                    + "from a wider window; a chunk border would show a seam");
                }
            }
        }
    }

    @Test
    @DisplayName("The same seed rebuilds the same chunk, a different seed does not")
    void generationIsDeterministic() {
        assertEquals(generate(9, -9).snapshot(), generate(9, -9).snapshot(),
                "regenerating a chunk gives a different world");

        RecordingTerrain mine = RecordingTerrain.window(9 * CHUNK, -9 * CHUNK, CHUNK);
        RecordingTerrain theirs = RecordingTerrain.window(9 * CHUNK, -9 * CHUNK, CHUNK);
        BossArenaGenerator.generateArea(9 * CHUNK, -9 * CHUNK, SEED, mine);
        BossArenaGenerator.generateArea(9 * CHUNK, -9 * CHUNK, OTHER_SEED, theirs);
        assertNotEqualsBlocks(mine.snapshot(), theirs.snapshot());
    }

    @Test
    @DisplayName("A chunk without landmarks writes nothing it has to throw away")
    void landmarkChunksAreTheOnlyOnesThatOverflow() {
        RecordingTerrain empty = generate(0, 0);
        assertEquals(0, empty.dropped(),
                "the arena chunk has no landmark near it, so nothing should have been dropped");
        assertTrue(empty.size() > 1000, "the arena chunk is nearly empty: " + empty.size());

        // A chunk under a floating shard legitimately writes outside itself; it still has to keep
        // most of what it produces, or generation would be paying for the whole landmark per chunk.
        int shardChunkX = (int) Math.floor(ArenaLandmarks.skyShards().get(0).x() / (double) CHUNK);
        int shardChunkZ = (int) Math.floor(ArenaLandmarks.skyShards().get(0).z() / (double) CHUNK);
        RecordingTerrain shard = generate(shardChunkX, shardChunkZ);
        assertTrue(shard.dropped() <= shard.size(),
                "the shard chunk dropped " + shard.dropped() + " of " + shard.size() + " writes");
        assertTrue(shard.size() > 1000, "the shard chunk is nearly empty: " + shard.size());
    }

    @Test
    @DisplayName("The shards really are built, high over the crown")
    void shardsAreBuiltAboveTheFight() {
        ArenaLandmarks.Landmark shard = ArenaLandmarks.skyShards().get(0);
        RecordingTerrain terrain = generate(
                (int) Math.floor(shard.x() / (double) CHUNK), (int) Math.floor(shard.z() / (double) CHUNK));

        // Only the shard's own footprint is measured: the buttress ribs share the chunk and they are
        // meant to reach well above the parapet.
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        for (Map.Entry<Long, Material> entry : terrain.snapshot().entrySet()) {
            long key = entry.getKey();
            int y = RecordingTerrain.keyY(key);
            int z = RecordingTerrain.keyZ(key);
            int x = RecordingTerrain.keyX(key);
            if (ArenaShape.distance(x - shard.x(), z - shard.z()) > 11.0) continue;
            // The wilderness below the shard is inside the same footprint: the plateau tops out at
            // the parapet, so anything above it belongs to the shard itself.
            if (y <= ArenaShape.RIM_TOP_Y + ArenaShape.MERLON_HEIGHT) continue;
            highest = Math.max(highest, y);
            lowest = Math.min(lowest, y);
        }
        assertTrue(highest >= shard.y(), "no shard blocks were written, highest was " + highest);
        assertTrue(lowest > ArenaShape.ARENA_CLEAR_Y,
                "shard rock hangs down to y=" + lowest + ", inside the volume the fight uses");
    }

    @Test
    @DisplayName("The pavement set is exactly what the palette can return")
    void pavementSetMatchesThePalette() {
        List<Material> expected = List.of(Material.POLISHED_BLACKSTONE_BRICKS,
                Material.DEEPSLATE_BRICKS, Material.CRYING_OBSIDIAN);
        assertEquals(expected, ArenaPalette.PLAZA_PAVEMENT);
        assertTrue(ArenaPalette.PLAZA_PAVEMENT.contains(ArenaPalette.PLAZA_GLOW));
    }

    // ------------------------------------------------------------------ helpers

    private static RecordingTerrain generate(int chunkX, int chunkZ) {
        RecordingTerrain terrain = RecordingTerrain.window(chunkX * CHUNK, chunkZ * CHUNK, CHUNK);
        BossArenaGenerator.generateArea(chunkX * CHUNK, chunkZ * CHUNK, SEED, terrain);
        return terrain;
    }

    /** A chunk whose middle sits on a deep canyon, or null when this seed has none in range. */
    private static int[] findCanyonChunk() {
        int salt = ArenaNoise.saltOf(SEED) ^ 0x51ED_2701;
        for (int chunkX = 20; chunkX <= 60; chunkX += 2) {
            for (int chunkZ = -60; chunkZ <= 60; chunkZ += 2) {
                int x = chunkX * CHUNK + 8;
                int z = chunkZ * CHUNK + 8;
                double distance = ArenaShape.distance(x, z);
                if (ArenaShape.canyonStrength(x, z, distance, salt) < 0.9) continue;
                if (ArenaShape.surfaceY(x, z, SEED) > ArenaShape.CHASM_FLOOR_Y + 1) continue;
                return new int[]{chunkX, chunkZ};
            }
        }
        return null;
    }

    private static void assertNotEqualsBlocks(Map<Long, Material> first, Map<Long, Material> second) {
        int differences = 0;
        for (Map.Entry<Long, Material> entry : first.entrySet()) {
            if (entry.getValue() != second.get(entry.getKey())) differences++;
        }
        assertTrue(differences > first.size() / 4,
                "two different world seeds grew almost the same wilderness: " + differences + " of "
                        + first.size() + " blocks differ");
    }
}
