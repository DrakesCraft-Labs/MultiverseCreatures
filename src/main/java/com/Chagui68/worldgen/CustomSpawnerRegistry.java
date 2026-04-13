package com.Chagui68.worldgen;

import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.LimitedRegion;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CustomSpawnerRegistry {
    public interface CustomSpawnerGenerator {
        boolean generate(LimitedRegion region, int x, int y, int z, Random random);
    }

    private static final Map<String, CustomSpawnerGenerator> registry = new HashMap<>();

    static {
        // 1. "NIDO_ARANAS" (Spider Nest)
        register("NIDO_ARANAS", (region, lx, ly, lz, random) -> {
            int radius = 4;
            int height = 5;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -1; dy <= height; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int ax = lx + dx;
                        int ay = ly + dy;
                        int az = lz + dz;
                        if (!region.isInRegion(ax, ay, az)) continue;
                        
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist <= radius) {
                            if (dy == -1 || dy == height || dist > radius - 1) {
                                // Shell (Walls, floor, ceiling)
                                org.bukkit.Material mat = org.bukkit.Material.ROOTED_DIRT;
                                if (random.nextDouble() < 0.3) mat = org.bukkit.Material.MOSS_BLOCK;
                                else if (random.nextDouble() < 0.2) mat = org.bukkit.Material.COBBLESTONE;
                                region.setBlockData(ax, ay, az, mat.createBlockData());
                            } else {
                                // Interior (carved out + cobwebs)
                                if (random.nextDouble() < 0.12) {
                                    region.setBlockData(ax, ay, az, org.bukkit.Material.COBWEB.createBlockData());
                                } else {
                                    region.setBlockData(ax, ay, az, org.bukkit.Material.AIR.createBlockData());
                                }
                            }
                        }
                    }
                }
            }
            
            // Set Spawner
            if (region.isInRegion(lx, ly, lz)) {
                region.setBlockData(lx, ly, lz, org.bukkit.Material.SPAWNER.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx, ly, lz);
                    if (state instanceof CreatureSpawner sp) {
                        applyLightRule(sp, EntityType.CAVE_SPIDER);
                    }
                } catch (Exception ignored) {}
            }
            
            // Set Loot Chest
            if (region.isInRegion(lx + 1, ly, lz)) {
                region.setBlockData(lx + 1, ly, lz, org.bukkit.Material.CHEST.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx + 1, ly, lz);
                    if (state instanceof org.bukkit.block.Chest) {
                        org.bukkit.block.Chest ch = (org.bukkit.block.Chest) state;
                        ch.setLootTable(org.bukkit.loot.LootTables.SIMPLE_DUNGEON.getLootTable());
                        ch.update(true, false);
                    }
                } catch (Exception ignored) {}
            }
            return true;
        });

        // 2. "PRISION_ESQUELETOS"
        register("PRISION_ESQUELETOS", (region, lx, ly, lz, random) -> {
            int radius = 4;
            int height = 5;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -1; dy < height; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int ax = lx + dx;
                        int ay = ly + dy;
                        int az = lz + dz;
                        if (!region.isInRegion(ax, ay, az)) continue;
                        
                        // Walls, Floor, Ceiling
                        if (dy == -1 || dy == height - 1 || dx == -radius || dx == radius || dz == -radius || dz == radius) {
                            org.bukkit.Material mat = org.bukkit.Material.DEEPSLATE_BRICKS;
                            if (random.nextDouble() < 0.2) mat = org.bukkit.Material.CRACKED_DEEPSLATE_BRICKS;
                            else if (random.nextDouble() < 0.1) mat = org.bukkit.Material.POLISHED_DEEPSLATE;
                            region.setBlockData(ax, ay, az, mat.createBlockData());
                        } else {
                            // Air space
                            region.setBlockData(ax, ay, az, org.bukkit.Material.AIR.createBlockData());
                            
                            // Prison Cell in a corner (e.g., dx, dz from 1 to 3)
                            if (dx >= 1 && dx <= 3 && dz >= 1 && dz <= 3) {
                                if (dx == 1 || dz == 1) { // Cell bars
                                    // Leave a gap for a "door" at height 0,1
                                    if (!(dz == 2 && dy <= 1)) {
                                        region.setBlockData(ax, ay, az, org.bukkit.Material.IRON_BARS.createBlockData());
                                        applyBarConnections(region, ax, ay, az);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Hang iron bars and lantern
            if (region.isInRegion(lx, ly + height - 2, lz)) {
                region.setBlockData(lx, ly + height - 2, lz, org.bukkit.Material.IRON_BARS.createBlockData());
                applyBarConnections(region, lx, ly + height - 2, lz);
                region.setBlockData(lx, ly + height - 3, lz, org.bukkit.Material.LANTERN.createBlockData());
            }
            
            // Set Spawner
            if (region.isInRegion(lx, ly, lz)) {
                region.setBlockData(lx, ly, lz, org.bukkit.Material.SPAWNER.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx, ly, lz);
                    if (state instanceof CreatureSpawner sp) {
                        applyLightRule(sp, EntityType.SKELETON);
                    }
                } catch (Exception ignored) {}
            }
            
            // Set Chest (inside the cell)
            if (region.isInRegion(lx + 2, ly, lz + 2)) {
                region.setBlockData(lx + 2, ly, lz + 2, org.bukkit.Material.CHEST.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx + 2, ly, lz + 2);
                    if (state instanceof org.bukkit.block.Chest) {
                        org.bukkit.block.Chest ch = (org.bukkit.block.Chest) state;
                        ch.setLootTable(org.bukkit.loot.LootTables.SIMPLE_DUNGEON.getLootTable());
                        ch.update(true, false);
                    }
                } catch (Exception ignored) {}
            }
            return true;
        });

        // 3. "ALTAR_MALDITO"
        register("ALTAR_MALDITO", (region, lx, ly, lz, random) -> {
            int radius = 5;
            int height = 6;
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -1; dy < height; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int ax = lx + dx;
                        int ay = ly + dy;
                        int az = lz + dz;
                        if (!region.isInRegion(ax, ay, az)) continue;
                        
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist <= radius) {
                            if (dy == -1) { 
                                org.bukkit.Material mat = org.bukkit.Material.POLISHED_BLACKSTONE_BRICKS;
                                if (dist <= 2 && random.nextDouble() < 0.5) mat = org.bukkit.Material.MAGMA_BLOCK;
                                region.setBlockData(ax, ay, az, mat.createBlockData());
                            } else if (dy == height - 1) {
                                region.setBlockData(ax, ay, az, org.bukkit.Material.POLISHED_BLACKSTONE_BRICKS.createBlockData());
                            } else if (dist > radius - 1) { 
                                region.setBlockData(ax, ay, az, org.bukkit.Material.POLISHED_BLACKSTONE_BRICKS.createBlockData());
                            } else {
                                region.setBlockData(ax, ay, az, org.bukkit.Material.AIR.createBlockData());
                                // Pillars
                                if ((Math.abs(dx) == 3 && Math.abs(dz) == 3)) {
                                    region.setBlockData(ax, ay, az, org.bukkit.Material.POLISHED_BASALT.createBlockData());
                                }
                            }
                        }
                    }
                }
            }
            
            // Base (Crying obsidian)
            if (region.isInRegion(lx, ly - 1, lz)) {
                region.setBlockData(lx, ly - 1, lz, org.bukkit.Material.CRYING_OBSIDIAN.createBlockData());
            }
            // Spawner
            if (region.isInRegion(lx, ly, lz)) {
                region.setBlockData(lx, ly, lz, org.bukkit.Material.SPAWNER.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx, ly, lz);
                    if (state instanceof CreatureSpawner sp) {
                        applyLightRule(sp, EntityType.ZOMBIE);
                    }
                } catch (Exception ignored) {}
            }
            
            // Set Chest
            if (region.isInRegion(lx, ly, lz + 2)) {
                region.setBlockData(lx, ly, lz + 2, org.bukkit.Material.CHEST.createBlockData());
                try {
                    org.bukkit.block.BlockState state = region.getBlockState(lx, ly, lz + 2);
                    if (state instanceof org.bukkit.block.Chest) {
                        org.bukkit.block.Chest ch = (org.bukkit.block.Chest) state;
                        ch.setLootTable(org.bukkit.loot.LootTables.SIMPLE_DUNGEON.getLootTable());
                        ch.update(true, false);
                    }
                } catch (Exception ignored) {}
            }
            return true;
        });
    }

    public static void applyLightRule(CreatureSpawner spawner, EntityType type) {
        if (spawner == null) return;
        
        // Set the primary type
        if (type != null) {
            spawner.setSpawnedType(type);
        }
        
        // Create a rule that allows spawning at ANY light level (0-15)
        // minBlockLight, maxBlockLight, minSkyLight, maxSkyLight
        SpawnRule unrestricted = new SpawnRule(0, 15, 0, 15);
        
        // Update all potential spawns with the unrestricted rule
        java.util.List<SpawnerEntry> potentials = spawner.getPotentialSpawns();
        java.util.List<SpawnerEntry> updatedPotentials = new java.util.ArrayList<>();
        
        if (potentials.isEmpty()) {
            // If for some reason it's empty, we ensure it's at least initialized by the current type
            // spawner.update(true, false); // Optional: force a sync if needed
            potentials = spawner.getPotentialSpawns();
        }

        for (SpawnerEntry entry : potentials) {
            updatedPotentials.add(new SpawnerEntry(entry.getSnapshot(), entry.getSpawnWeight(), unrestricted));
        }
        
        if (!updatedPotentials.isEmpty()) {
            spawner.setPotentialSpawns(updatedPotentials);
        }
        
        // Final commit to the world state in the LimitedRegion
        // Using force=true and applyPhysics=true to ensure the block state is correctly pushed back
        spawner.update(true, true);
    }

    private static void applyBarConnections(LimitedRegion region, int x, int y, int z) {
        org.bukkit.block.data.BlockData data = region.getBlockData(x, y, z);
        if (data instanceof MultipleFacing mf) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            for (BlockFace face : faces) {
                int nx = x + face.getModX();
                int nz = z + face.getModZ();
                if (region.isInRegion(nx, y, nz)) {
                    org.bukkit.Material type = region.getType(nx, y, nz);
                    if (type.isSolid() || type == org.bukkit.Material.IRON_BARS) {
                        mf.setFace(face, true);
                    }
                }
            }
            region.setBlockData(x, y, z, mf);
            
            // Also update neighbors if they are bars
            for (BlockFace face : faces) {
                int nx = x + face.getModX();
                int nz = z + face.getModZ();
                if (region.isInRegion(nx, y, nz) && region.getType(nx, y, nz) == org.bukkit.Material.IRON_BARS) {
                    org.bukkit.block.data.BlockData nData = region.getBlockData(nx, y, nz);
                    if (nData instanceof MultipleFacing nMf) {
                        nMf.setFace(face.getOppositeFace(), true);
                        region.setBlockData(nx, y, nz, nMf);
                    }
                }
            }
        }
    }

    /**
     * Register a schematic as a custom spawner/dungeon.
     */
    public static void registerSchematic(String id, Schematic schematic) {
        if (id == null || schematic == null) return;
        register(id, (region, x, y, z, random) -> {
            try {
                // For spawners/dungeons, we do NOT skip air blocks, because we want the schematic to carve out the room inside the mountain/cave.
                return schematic.pasteIntoRegion(region, x, y, z, false);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static void register(String id, CustomSpawnerGenerator generator) {
        if (id == null || generator == null) return;
        registry.put(id.toUpperCase(Locale.ROOT), generator);
    }

    public static boolean generate(String id, LimitedRegion region, int x, int y, int z, Random random) {
        if (id == null) return false;
        CustomSpawnerGenerator gen = registry.get(id.toUpperCase(Locale.ROOT));
        if (gen == null) return false;
        try {
            return gen.generate(region, x, y, z, random);
        } catch (Exception e) {
            return false;
        }
    }
}
