package com.Chagui68.ritual.terrain;

import org.bukkit.HeightMap;
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
 * Writes {@link Wasteland} into chunks.
 *
 * <p>Every vanilla stage is off. In particular {@link #shouldGenerateNoise()} must stay false: true
 * makes the server generate ordinary overworld terrain before this generator runs, and the previous
 * coliseum generator came out mixed with hills, water and stone because of exactly that.
 */
public final class WastelandGenerator extends ChunkGenerator {

    private static final int CHUNK_SIZE = 16;

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        Wasteland wasteland = new Wasteland(worldInfo.getSeed());
        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight() - 1;
        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                int lx = localX;
                int lz = localZ;
                wasteland.write(chunkX * CHUNK_SIZE + localX, chunkZ * CHUNK_SIZE + localZ, (fromY, toY, material) -> {
                    int from = Math.max(fromY, minY);
                    int to = Math.min(toY, maxY);
                    if (from > to) return;
                    chunkData.setRegion(lx, from, lz, lx + 1, to + 1, lz + 1, material);
                });
            }
        }
    }

    /** Where players arrive: the middle of the arena, on its floor. */
    public static Location spawn(World world) {
        return new Location(world, 0.5, Wasteland.FLOOR_Y + 1, 0.5);
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return spawn(world);
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        return new Wasteland(worldInfo.getSeed()).surfaceY(x, z) + 1;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
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
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.emptyList();
    }

    /**
     * Plains on purpose: {@code BossDimensionSky} recolours that biome's sky and fog to the ritual's
     * red, so any other biome would show ordinary sky over the battlefield.
     */
    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return Biome.PLAINS;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return Collections.singletonList(Biome.PLAINS);
            }
        };
    }
}
