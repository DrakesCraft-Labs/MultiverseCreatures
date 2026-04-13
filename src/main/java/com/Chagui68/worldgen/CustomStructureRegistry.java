package com.Chagui68.worldgen;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CustomStructureRegistry {
    public interface StructureGenerator {
        boolean generate(ChunkData chunkData, int localX, int localY, int localZ, Random random, WorldInfo worldInfo);
    }

    private static final Map<String, StructureGenerator> registry = new HashMap<>();

    static {
        // Register a simple ruin structure
        register("RUIN", (chunkData, lx, ly, lz, random, worldInfo) -> {
            // Small 6x6 ruined platform centered at lx,lz with stone bricks and moss
            int radius = 3;
            int baseY = ly;
            // Ensure within chunk
            if (lx - radius < 0 || lx + radius >= 16 || lz - radius < 0 || lz + radius >= 16) return false;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int ax = lx + dx;
                    int az = lz + dz;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= radius + 0.2) {
                        // foundation
                        if (baseY >= worldInfo.getMinHeight() && baseY < worldInfo.getMaxHeight()) {
                            chunkData.setBlock(ax, baseY, az, Material.STONE_BRICKS);
                        }
                        // add rubble
                        if (dist > radius - 1.0 && random.nextDouble() < 0.6) {
                            if (baseY + 1 < worldInfo.getMaxHeight()) chunkData.setBlock(ax, baseY + 1, az, Material.COBBLESTONE);
                        }
                    }
                }
            }
            // Add a central broken pillar
            if (baseY + 1 < worldInfo.getMaxHeight()) chunkData.setBlock(lx, baseY + 1, lz, Material.STONE_BRICK_WALL);
            if (baseY + 2 < worldInfo.getMaxHeight() && random.nextDouble() < 0.4) chunkData.setBlock(lx, baseY + 2, lz, Material.STONE_BRICK_WALL);
            return true;
        });
    }

    public static void register(String id, StructureGenerator generator) {
        if (id == null || generator == null) return;
        registry.put(id.toUpperCase(Locale.ROOT), generator);
    }

    /**
     * Register a simple JSON schematic (using our `Schematic` loader) under the given id.
     */
    public static void registerSchematic(String id, Schematic schematic) {
        if (id == null || schematic == null) return;
        register(id, (chunkData, lx, ly, lz, random, worldInfo) -> {
            try {
                return schematic.pasteIntoChunk(chunkData, lx, ly, lz, worldInfo);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static boolean generate(String id, ChunkData chunkData, int localX, int localY, int localZ, Random random, WorldInfo worldInfo) {
        if (id == null) return false;
        StructureGenerator gen = registry.get(id.toUpperCase(Locale.ROOT));
        if (gen == null) return false;
        try {
            return gen.generate(chunkData, localX, localY, localZ, random, worldInfo);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRegistered(String id) {
        return registry.containsKey(id == null ? "" : id.toUpperCase(Locale.ROOT));
    }
}
