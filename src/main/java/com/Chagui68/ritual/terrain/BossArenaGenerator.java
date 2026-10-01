package com.Chagui68.ritual.terrain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Terrain for the ritual dimension: a coliseum cut into a shattered plain.
 *
 * <p>What replaced the crying-obsidian plain:
 *
 * <ul>
 *   <li>A paved arena with an eight-pointed sigil, glowing rings and spokes; the pavement is made
 *       only of the blocks the invocation structures accept (see
 *       {@link ArenaPalette#PLAZA_PAVEMENT}).</li>
 *   <li>A terraced wall around it with battlements, glowing galleries and eight buttress ribs
 *       carrying the eye up.</li>
 *   <li>Beyond the wall, a wilderness of shelves, obsidian peaks, soul-fire flats, lava-floored
 *       canyons and ruins, with eight shards floating over the crown.</li>
 * </ul>
 *
 * <p><strong>Nothing is ever generated inside the play volume.</strong> Every block inside
 * {@link ArenaShape#ARENA_RADIUS} stops at {@link ArenaShape#FLOOR_Y}; the drama is all in the
 * pavement and beyond the wall. The bosses' ground queries, the seal placement, the arena ground
 * recovery and the players' build height all assume a flat floor, and none of them had to change.
 *
 * <p>The generator holds no state: the same chunk always produces the same blocks, which is what
 * makes it safe to generate chunks in parallel (see {@link #isParallelCapable()}).
 */
public final class BossArenaGenerator extends ChunkGenerator {

    /** Rock kept under a canyon floor, so digging at the bottom never reaches the void. */
    public static final int CANYON_ROCK_DEPTH = 16;

    /**
     * The common seal under the whole wilderness: below the lowest surface the shape allows and
     * below the deepest well a landmark digs into it (a well bottoms out 14 blocks under its rim).
     */
    public static final int WASTE_BEDROCK_Y = ArenaShape.MIN_BUILD_Y - CANYON_ROCK_DEPTH;

    private static final int CHUNK_SIZE = 16;

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        generateArea(chunkX * CHUNK_SIZE, chunkZ * CHUNK_SIZE, worldInfo.getSeed(),
                new ChunkSink(chunkData, chunkX, chunkZ));
    }

    /**
     * Generates one chunk-sized area into a sink, in world coordinates.
     *
     * <p>This is the seam the tests drive: {@link #generateNoise} only wraps it with a sink that
     * knows the current chunk, and the {@code worldInfo}'s seed is the only other input, so the
     * whole terrain can be built into a recording sink without a server. Note the {@code Random}
     * the server passes in is deliberately unused — a generator that randomises per call would make
     * the same chunk look different every time it is generated.
     */
    public static void generateArea(int baseX, int baseZ, long seed, TerrainSink sink) {
        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                writeColumn(baseX + localX, baseZ + localZ, seed, sink);
            }
        }
        for (ArenaLandmarks.Landmark landmark
                : ArenaLandmarks.touching(baseX, baseZ, baseX + CHUNK_SIZE - 1, baseZ + CHUNK_SIZE - 1, seed)) {
            ArenaLandmarks.write(landmark, seed, sink);
        }
    }

    /**
     * One column of the world: the rock up to its surface, the surface block, and whatever stands
     * on it.
     *
     * <p>The rock is written as runs of one material because the strata only change every few
     * blocks; a canyon column is sealed with bedrock {@link #CANYON_ROCK_DEPTH} under its floor so
     * the bottom of the world is never reachable by digging.
     */
    private static void writeColumn(int x, int z, long seed, TerrainSink sink) {
        int surface = ArenaShape.surfaceY(x, z, seed);
        ArenaShape.Zone zone = ArenaShape.zone(x, z);
        int bedrockY = bedrockY(zone, surface);

        Material top = switch (zone) {
            case ARENA -> ArenaPalette.arenaFloor(x, z, seed);
            case RIM -> ArenaPalette.rimSurface(x, z, isTerraceFloor(x, z, seed), seed);
            case WASTE -> ArenaPalette.wasteSurface(x, z, surface, seed);
        };

        Material current = top;
        int runTop = surface;
        for (int y = surface - 1; y >= bedrockY; y--) {
            Material block = ArenaPalette.fill(x, y, z, surface, bedrockY, seed, zone);
            if (block != current) {
                sink.column(x, z, y + 1, runTop, current);
                current = block;
                runTop = y;
            }
        }
        sink.column(x, z, bedrockY, runTop, current);

        if (zone == ArenaShape.Zone.WASTE && ArenaPalette.soulFireAt(x, z, top, seed)) {
            sink.set(x, surface + 1, z, Material.SOUL_FIRE);
        }
    }

    /**
     * Where a column's bedrock seal sits.
     *
     * <p>The arena and the wall keep theirs at {@link ArenaShape#BEDROCK_Y}. The wilderness is
     * different: canyons cut down to {@link ArenaShape#CHASM_FLOOR_Y}, and the seal used to follow
     * each column's own surface, so a canyon floor at -22 stood next to an ordinary column whose
     * rock stopped at 0. Everything between was air: the canyon walls opened sideways onto a void
     * under the whole wilderness, the lava pooled at the bottom ran out into it, and a well dug
     * near the rim broke through into the same hollow. Every wilderness column now rests on one
     * common seal below the deepest thing the generator carves, so neighbours always meet rock.
     */
    static int bedrockY(ArenaShape.Zone zone, int surface) {
        if (zone == ArenaShape.Zone.WASTE) {
            return Math.min(WASTE_BEDROCK_Y, surface - CANYON_ROCK_DEPTH);
        }
        return surface >= ArenaShape.BEDROCK_Y ? ArenaShape.BEDROCK_Y : surface - CANYON_ROCK_DEPTH;
    }

    /**
     * Whether a rim column is a terrace floor rather than the rock face of the next terrace.
     *
     * <p>Two neighbours are enough: the terraces are concentric, so a column whose outward
     * neighbours are level with it belongs to a gallery, and one that steps up does not.
     */
    static boolean isTerraceFloor(int x, int z, long seed) {
        int surface = ArenaShape.surfaceY(x, z, seed);
        return ArenaShape.surfaceY(x + 1, z, seed) == surface
                && ArenaShape.surfaceY(x, z + 1, seed) == surface
                && ArenaShape.surfaceY(x - 1, z, seed) == surface;
    }

    // ------------------------------------------------------------------ stage and world flags

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, org.bukkit.HeightMap heightMap) {
        return Math.max(0, ArenaShape.surfaceY(x, z, worldInfo.getSeed()) + 1);
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0.5, ArenaShape.spawnY(), 0.5);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.emptyList();
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new ArenaBiomeProvider();
    }

    /**
     * The dimension keeps the plains biome on purpose: {@code BossDimensionSky} recolours that
     * biome's sky and fog to the red the ritual is known for, so any other biome would show a
     * patch of ordinary sky across the arena.
     */
    private static final class ArenaBiomeProvider extends BiomeProvider {

        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return Biome.PLAINS;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return Collections.singletonList(Biome.PLAINS);
        }
    }

    // ------------------------------------------------------------------ sink

    /** Writes world coordinates into one chunk, dropping everything outside it. */
    private static final class ChunkSink implements TerrainSink {

        private final ChunkData data;
        private final int baseX;
        private final int baseZ;
        private final int minY;
        private final int maxY;

        private ChunkSink(ChunkData data, int chunkX, int chunkZ) {
            this.data = data;
            this.baseX = chunkX * CHUNK_SIZE;
            this.baseZ = chunkZ * CHUNK_SIZE;
            this.minY = data.getMinHeight();
            // getMaxHeight() is exclusive; keep maxY as the highest writable block.
            this.maxY = data.getMaxHeight() - 1;
        }

        @Override
        public void set(int x, int y, int z, Material material) {
            int localX = x - baseX;
            int localZ = z - baseZ;
            if (localX < 0 || localX >= CHUNK_SIZE || localZ < 0 || localZ >= CHUNK_SIZE) return;
            if (y < minY || y > maxY) return;
            data.setBlock(localX, y, localZ, material);
        }

        @Override
        public void column(int x, int z, int fromY, int toY, Material material) {
            int localX = x - baseX;
            int localZ = z - baseZ;
            if (localX < 0 || localX >= CHUNK_SIZE || localZ < 0 || localZ >= CHUNK_SIZE) return;
            int start = Math.max(fromY, minY);
            int end = Math.min(toY, maxY);
            for (int y = start; y <= end; y++) {
                data.setBlock(localX, y, localZ, material);
            }
        }
    }
}
