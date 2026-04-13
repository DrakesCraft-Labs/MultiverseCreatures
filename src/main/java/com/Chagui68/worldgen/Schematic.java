package com.Chagui68.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Schematic loader supporting:
 * - Simple JSON: { "blocks": [ {x, y, z, material, blockData?}, ... ] }
 * - Binary formats: .schem (Sponge/WorldEdit) and .litematic (Litematica)
 *   via SchematicConverter auto-conversion
 */
public class Schematic {
    public static class BlockEntry {
        public int x;
        public int y;
        public int z;
        public String material;
        public String blockData; // Optional block state properties (e.g. "facing=north,half=top")
    }

    private final List<BlockEntry> blocks;

    public Schematic(List<BlockEntry> blocks) {
        this.blocks = blocks;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    /**
     * Load from a simple JSON file.
     */
    public static Schematic loadFromFile(File file) throws Exception {
        Gson gson = new Gson();
        try (FileReader r = new FileReader(file)) {
            JsonObject obj = gson.fromJson(r, JsonObject.class);
            return parseJsonObject(obj);
        }
    }

    /**
     * Load from a binary schematic file (.schem or .litematic).
     * Auto-converts to JSON internally using SchematicConverter.
     */
    public static Schematic loadFromBinarySchematic(File file) throws Exception {
        SchematicConverter.ConversionResult result = SchematicConverter.convert(file);
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(result.json, JsonObject.class);
        return parseJsonObject(obj);
    }

    /**
     * Load from any supported format (auto-detect by extension).
     */
    public static Schematic loadAuto(File file) throws Exception {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) {
            return loadFromFile(file);
        } else if (name.endsWith(".schem") || name.endsWith(".schematic") || name.endsWith(".litematic")) {
            return loadFromBinarySchematic(file);
        } else {
            throw new Exception("Unsupported schematic format: " + name);
        }
    }

    private static Schematic parseJsonObject(JsonObject obj) {
        JsonArray arr = obj.getAsJsonArray("blocks");
        List<BlockEntry> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                JsonObject b = arr.get(i).getAsJsonObject();
                BlockEntry be = new BlockEntry();
                be.x = b.get("x").getAsInt();
                be.y = b.get("y").getAsInt();
                be.z = b.get("z").getAsInt();
                be.material = b.get("material").getAsString();
                if (b.has("blockData")) {
                    be.blockData = b.get("blockData").getAsString();
                }
                list.add(be);
            }
        }
        return new Schematic(list);
    }

    /**
     * Paste schematic into the provided chunkData at local chunk coords.
     * Only blocks that fall inside this chunk (0..15) are written.
     * Supports block states when blockData is present.
     */
    public boolean pasteIntoChunk(ChunkGenerator.ChunkData chunkData, int localX, int localY, int localZ, WorldInfo worldInfo) {
        boolean any = false;
        for (BlockEntry be : blocks) {
            int lx = localX + be.x;
            int ly = localY + be.y;
            int lz = localZ + be.z;
            if (lx < 0 || lx >= 16 || lz < 0 || lz >= 16) continue;
            if (ly < worldInfo.getMinHeight() || ly >= worldInfo.getMaxHeight()) continue;

            try {
                Material mat = Material.valueOf(be.material);

                if (be.blockData != null && !be.blockData.isEmpty()) {
                    try {
                        String dataString = "minecraft:" + be.material.toLowerCase(Locale.ROOT)
                                + "[" + be.blockData + "]";
                        BlockData data = Bukkit.createBlockData(dataString);
                        chunkData.setBlock(lx, ly, lz, data);
                    } catch (Exception e) {
                        chunkData.setBlock(lx, ly, lz, mat);
                    }
                } else {
                    chunkData.setBlock(lx, ly, lz, mat);
                }
                any = true;
            } catch (IllegalArgumentException e) {
                // Unknown material, skip
            }
        }
        return any;
    }

    // Blocks to skip when pasting as a tree (ground layer of the schematic)
    private static final java.util.Set<String> GROUND_BLOCKS = java.util.Set.of(
            "GRASS_BLOCK", "DIRT", "COARSE_DIRT", "PODZOL", "MYCELIUM",
            "ROOTED_DIRT", "MUD", "MOSS_BLOCK", "STONE", "DEEPSLATE"
    );

    /**
     * Paste schematic into a LimitedRegion (used during tree population).
     * Centers the schematic at (x, y, z) and optionally skips ground blocks
     * so the tree blends naturally with existing terrain.
     *
     * @param region    The limited region to paste into
     * @param x         World X (center of placement)
     * @param y         World Y (ground level)
     * @param z         World Z (center of placement)
     * @param skipGround If true, skips GRASS_BLOCK/DIRT/STONE at y=0 of the schematic
     * @return true if any blocks were placed
     */
    public boolean pasteIntoRegion(org.bukkit.generator.LimitedRegion region,
                                   int x, int y, int z, boolean skipGround) {
        // Calculate bounding box to center the schematic
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        for (BlockEntry be : blocks) {
            minX = Math.min(minX, be.x);
            maxX = Math.max(maxX, be.x);
            minZ = Math.min(minZ, be.z);
            maxZ = Math.max(maxZ, be.z);
            minY = Math.min(minY, be.y);
        }
        int centerOffsetX = (maxX + minX) / 2;
        int centerOffsetZ = (maxZ + minZ) / 2;

        boolean any = false;
        for (BlockEntry be : blocks) {
            // Skip ground blocks if requested (first layer of schematic)
            if (skipGround && be.y == minY && GROUND_BLOCKS.contains(be.material)) {
                continue;
            }

            int wx = x + (be.x - centerOffsetX);
            int wy = y + (be.y - minY); // Place from ground level up
            int wz = z + (be.z - centerOffsetZ);

            if (!region.isInRegion(wx, wy, wz)) continue;

            try {
                Material mat = Material.valueOf(be.material);

                if (be.blockData != null && !be.blockData.isEmpty()) {
                    try {
                        String dataString = "minecraft:" + be.material.toLowerCase(Locale.ROOT)
                                + "[" + be.blockData + "]";
                        BlockData data = Bukkit.createBlockData(dataString);
                        region.setBlockData(wx, wy, wz, data);
                    } catch (Exception e) {
                        region.setBlockData(wx, wy, wz, mat.createBlockData());
                    }
                } else {
                    region.setBlockData(wx, wy, wz, mat.createBlockData());
                }

                // Apply light rules to spawners
                if (mat == Material.SPAWNER) {
                    try {
                        org.bukkit.block.BlockState state = region.getBlockState(wx, wy, wz);
                        if (state instanceof org.bukkit.block.CreatureSpawner sp) {
                            CustomSpawnerRegistry.applyLightRule(sp, null);
                        }
                    } catch (Exception ignored) {}
                }
                any = true;
            } catch (IllegalArgumentException e) {
                // Unknown material, skip
            }
        }
        return any;
    }
}
