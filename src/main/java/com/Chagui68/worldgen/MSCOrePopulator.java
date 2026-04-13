package com.Chagui68.worldgen;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.biome.BiomeConfig;
import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MSCOrePopulator extends BlockPopulator {
    private final MultiverseCreatures plugin;
    private final MSCTerraGenerator generator;

    public MSCOrePopulator(MultiverseCreatures plugin, MSCTerraGenerator generator) {
        this.plugin = plugin;
        this.generator = generator;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        com.Chagui68.biome.BiomeProvider biomeProvider = generator.getInternalBiomeProvider();
        if (biomeProvider == null) return;

        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        BiomeConfig biome = biomeProvider.getBiome(centerX, 64, centerZ, worldInfo.getSeed());
        if (biome == null) return;

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        // 1. Ore Generation (Vein style)
        if (biome.getOres() != null) {
            for (BiomeConfig.OreDefinition ore : biome.getOres()) {
                // Approximate attempts based on chance. Increased multiplier for better density. e.g. 0.05 chance -> 50 veins per chunk. 
                int attempts = (int) Math.max(1, ore.chance * 1000); 

                for (int i = 0; i < attempts; i++) {
                    int x = (chunkX << 4) + random.nextInt(16);
                    int z = (chunkZ << 4) + random.nextInt(16);
                    
                    // Scan downwards from a reasonable maximum height (e.g. 220) to find the actual terrain surface
                    int surfaceY = Math.min(maxY - 1, 220);
                    while (surfaceY > minY && limitedRegion.isInRegion(x, surfaceY, z) && limitedRegion.getType(x, surfaceY, z).isAir()) {
                        surfaceY--;
                    }
                    
                    if (surfaceY <= minY + 5) continue; // No solid ground found
                    
                    // Clamp ore ranges to world/terrain bounds
                    int actualMin = Math.max(minY + 2, ore.minY);
                    int actualMax = Math.min(surfaceY - 2, ore.maxY);
                    
                    if (actualMax <= actualMin) continue;
                    
                    int y = actualMin + random.nextInt(actualMax - actualMin + 1);

                    if (!limitedRegion.isInRegion(x, y, z)) continue;

                    // Removed strict check for the starting block being stone. 
                    // This allows veins to start in cave air and "walk" into the stone walls, creating exposed ores.
                    Material oreMat = ore.material;
                    if (y <= 0 && !oreMat.name().contains("DEEPSLATE")) {
                        try { 
                            oreMat = Material.valueOf("DEEPSLATE_" + oreMat.name()); 
                        } catch (Exception ignored) { }
                    }

                    // Generate a slightly more massive vein (4 to 9 blocks)
                    int veinSize = 4 + random.nextInt(6);
                    int vx = x;
                    int vy = y;
                    int vz = z;

                    for (int v = 0; v < veinSize; v++) {
                        if (limitedRegion.isInRegion(vx, vy, vz)) {
                            Material t = limitedRegion.getType(vx, vy, vz);
                            if (t == Material.STONE || t == Material.DEEPSLATE || t == Material.BLACKSTONE) {
                                limitedRegion.setBlockData(vx, vy, vz, oreMat.createBlockData());
                            }
                        }
                        // Random walk
                        vx += random.nextInt(3) - 1;
                        vy += random.nextInt(3) - 1;
                        vz += random.nextInt(3) - 1;
                    }
                }
            }
        }

        // 2. Mountain Waterfalls (For 'valley' and 'cliffs')
        if (biome.getId().equals("valley") || biome.getId().equals("cliffs")) {
            if (random.nextDouble() < 0.08) { // 8% chance per chunk to try to spawn a waterfall
                int x = (chunkX << 4) + random.nextInt(16);
                int z = (chunkZ << 4) + random.nextInt(16);
                
                // Scan downwards from a reasonable mountain height looking for a cliff face
                for (int y = Math.min(maxY - 1, 200); y > 64; y--) {
                    if (!limitedRegion.isInRegion(x, y, z)) continue;
                    
                    Material type = limitedRegion.getType(x, y, z);
                    
                    // Look for valid cliff materials
                    if (type == Material.STONE || type == Material.DEEPSLATE || type == Material.DIRT) {
                        
                        // We want the spring to spawn INSIDE a wall, so it flows out naturally.
                        // Require exactly 1 air face (the waterfall exit) and 3 solid rock/soil faces.
                        int airCount = 0;
                        int solidCount = 0;
                        int[][] sides = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                        
                        for (int[] side : sides) {
                            int sx = x + side[0];
                            int sz = z + side[1];
                            if (!limitedRegion.isInRegion(sx, y, sz)) continue;
                            
                            Material st = limitedRegion.getType(sx, y, sz);
                            if (st.isAir()) {
                                airCount++;
                            } else if (st == Material.STONE || st == Material.DEEPSLATE || st == Material.DIRT || st == Material.GRASS_BLOCK || st == Material.BLACKSTONE) {
                                solidCount++;
                            }
                        }
                        
                        // Check if it's a valid cliff hole
                        if (airCount == 1 && solidCount == 3) {
                            // Ensure the block below is also solid so the water flows horizontally out the hole
                            if (limitedRegion.isInRegion(x, y-1, z)) {
                                Material below = limitedRegion.getType(x, y-1, z);
                                if (below == Material.STONE || below == Material.DEEPSLATE || below == Material.DIRT || below == Material.BLACKSTONE) {
                                    
                                    // 25% chance to spawn on valid spots, allowing waterfalls to spawn lower on the cliff
                                    if (random.nextDouble() < 0.25) {
                                        limitedRegion.setBlockData(x, y, z, Material.WATER.createBlockData());
                                        break; // Only one waterfall per chunk column
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Small Valley Brooks (Improved containment)
        if (biome.getId().equals("valley")) {
            if (random.nextDouble() < 0.15) { // Adjusted chance
                int x = (chunkX << 4) + random.nextInt(16);
                int z = (chunkZ << 4) + random.nextInt(16);
                
                int y = maxY - 1;
                while (y > 60 && limitedRegion.isInRegion(x, y, z) && limitedRegion.getType(x, y, z).isAir()) {
                    y--;
                }
                
                if (y > 60 && limitedRegion.isInRegion(x, y, z)) {
                    Material type = limitedRegion.getType(x, y, z);
                    if (type == Material.GRASS_BLOCK || type == Material.DIRT || type == Material.STONE) {
                        int solidCount = 0;
                        int[][] sides = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                        
                        for (int[] side : sides) {
                            int sx = x + side[0];
                            int sz = z + side[1];
                            if (limitedRegion.isInRegion(sx, y, sz) && limitedRegion.getType(sx, y, sz).isSolid()) {
                                solidCount++;
                            }
                        }
                        
                        // Enclosure check: Must have at least 3 solid sides to be a "niche" and a solid floor
                        if (solidCount >= 3 && limitedRegion.isInRegion(x, y - 1, z) && limitedRegion.getType(x, y - 1, z).isSolid()) {
                            // Clear decorations above (floating flora fix)
                            for (int dy = 1; dy <= 2; dy++) {
                                if (limitedRegion.isInRegion(x, y + dy, z) && !limitedRegion.getType(x, y + dy, z).isSolid()) {
                                    limitedRegion.setBlockData(x, y + dy, z, Material.AIR.createBlockData());
                                }
                            }
                            // Carve 1 block deep for water containment
                            limitedRegion.setBlockData(x, y - 1, z, Material.WATER.createBlockData());
                            limitedRegion.setBlockData(x, y, z, Material.AIR.createBlockData());
                        }
                    }
                }
            }

            // 4. Valley Ponds (Redesigned: Stability, Variety, Decoration)
            if (random.nextDouble() < 0.04) {
                int cx = (chunkX << 4) + random.nextInt(16);
                int cz = (chunkZ << 4) + random.nextInt(16);
                int radius = 2 + random.nextInt(3);
                
                int cy = maxY - 1;
                while (cy > minY && limitedRegion.isInRegion(cx, cy, cz) && limitedRegion.getType(cx, cy, cz).isAir()) {
                    cy--;
                }
                
                boolean stable = true;
                for (int dx = -radius; dx <= radius; dx += radius) {
                    for (int dz = -radius; dz <= radius; dz += radius) {
                        int ty = cy + 2;
                        while (ty > minY && limitedRegion.isInRegion(cx + dx, ty, cz + dz) && limitedRegion.getType(cx + dx, ty, cz + dz).isAir()) ty--;
                        if (Math.abs(ty - cy) > 2) { stable = false; break; }
                    }
                    if (!stable) break;
                }

                if (stable && cy > minY && limitedRegion.isInRegion(cx, cy, cz)) {
                    for (int dx = -radius - 2; dx <= radius + 2; dx++) {
                        for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                            int lx = cx + dx;
                            int lz = cz + dz;
                            if (!limitedRegion.isInRegion(lx, cy, lz)) continue;

                            double dist = Math.sqrt(dx * dx + dz * dz) + (random.nextDouble() * 0.8);
                            if (dist <= radius) {
                                // 1. TOP-DOWN CLEARING (Lawnmower)
                                // Scan from far above down to water level to clear ANY hanging flora/terrain
                                for (int scanY = cy + 10; scanY > cy; scanY--) {
                                    if (limitedRegion.isInRegion(lx, scanY, lz)) {
                                        Material mat = limitedRegion.getType(lx, scanY, lz);
                                        // Clear all non-solid blocks (flora) and even solid blocks if they are in the pond's direct column
                                        if (scanY > cy) {
                                            limitedRegion.setBlockData(lx, scanY, lz, Material.AIR.createBlockData());
                                        }
                                    }
                                }
                                
                                // 2. CARVE BASIN (2 blocks deep)
                                for (int dy = 0; dy >= -2; dy--) {
                                    int wy = cy + dy;
                                    if (limitedRegion.isInRegion(lx, wy, lz)) {
                                        if (dy == -2) {
                                            // Pond Floor
                                            Material floor = (random.nextDouble() < 0.2) ? Material.SAND : Material.DIRT;
                                            if (random.nextDouble() < 0.1) floor = Material.CLAY;
                                            limitedRegion.setBlockData(lx, wy, lz, floor.createBlockData());
                                        } else {
                                            // Water
                                            limitedRegion.setBlockData(lx, wy, lz, Material.WATER.createBlockData());
                                        }
                                    }
                                }

                                // 3. DECORATION
                                if (random.nextDouble() < 0.12 && limitedRegion.getType(lx, cy + 1, lz).isAir()) {
                                    limitedRegion.setBlockData(lx, cy + 1, lz, Material.LILY_PAD.createBlockData());
                                }
                            } else if (dist <= radius + 1.2) {
                                if (limitedRegion.isInRegion(lx, cy, lz)) {
                                    // Shoreline
                                    if (!limitedRegion.getType(lx, cy, lz).isSolid()) {
                                        limitedRegion.setBlockData(lx, cy, lz, Material.GRASS_BLOCK.createBlockData());
                                    }
                                    if (random.nextDouble() < 0.1 && limitedRegion.getType(lx, cy + 1, lz).isAir()) {
                                        limitedRegion.setBlockData(lx, cy + 1, lz, Material.SUGAR_CANE.createBlockData());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 5. Spawners / Custom Dungeons
            
            // Legacy basic spawner
            double spawnerChance = biome.getSpawnerChance();
            if (spawnerChance > 0 && random.nextDouble() < spawnerChance) {
                int cy = minY + 10 + random.nextInt(Math.abs(60 - minY + 1));
                spawnBasicDungeon(chunkX, chunkZ, random, cy, limitedRegion, null);
            }
            
            // Custom spawner list from biome
            for (BiomeConfig.SpawnerDefinition spawnerDef : biome.getSpawners()) {
                if (random.nextDouble() < spawnerDef.chance) {
                    int cx = (chunkX << 4) + random.nextInt(16);
                    int cz = (chunkZ << 4) + random.nextInt(16);
                    
                    // Respect spawner height limits
                    int actualMin = Math.max(minY + 2, spawnerDef.minY);
                    int actualMax = Math.min(maxY - 10, spawnerDef.maxY);
                    
                    if (actualMax <= actualMin) continue;
                    
                    int cy = actualMin + random.nextInt(actualMax - actualMin + 1);
                    
                    if (limitedRegion.isInRegion(cx, cy, cz)) {
                        if (spawnerDef.isCustom && spawnerDef.customId != null) {
                            CustomSpawnerRegistry.generate(spawnerDef.customId, limitedRegion, cx, cy, cz, random);
                        } else {
                            spawnBasicDungeon(cx, cz, random, cy, limitedRegion, spawnerDef.type);
                        }
                    }
                }
            }
        }
    }
    
    private void spawnBasicDungeon(int cx, int cz, Random random, int cy, LimitedRegion limitedRegion, String specificType) {
        if (!limitedRegion.isInRegion(cx, cy, cz)) return;
        
        int roomWidth = 3 + random.nextInt(2); // Radius 3 or 4
        int roomHeight = 3 + random.nextInt(2); // Height 3 or 4
        
        // Carve room and walls
        for (int dx = -roomWidth; dx <= roomWidth; dx++) {
            for (int dy = -1; dy < roomHeight; dy++) {
                for (int dz = -roomWidth; dz <= roomWidth; dz++) {
                    int lx = cx + dx;
                    int ly = cy + dy;
                    int lz = cz + dz;
                    
                    if (!limitedRegion.isInRegion(lx, ly, lz)) continue;
                    
                    if (dy == -1) {
                        limitedRegion.setBlockData(lx, ly, lz, (random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE).createBlockData());
                    } else if (dx == -roomWidth || dx == roomWidth || dz == -roomWidth || dz == roomWidth) {
                        if (limitedRegion.getType(lx, ly, lz).isSolid()) {
                            limitedRegion.setBlockData(lx, ly, lz, (random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE).createBlockData());
                        }
                    } else {
                        limitedRegion.setBlockData(lx, ly, lz, Material.AIR.createBlockData());
                    }
                }
            }
        }
        
        // Set spawner block
        if (limitedRegion.isInRegion(cx, cy, cz)) {
            limitedRegion.setBlockData(cx, cy, cz, Material.SPAWNER.createBlockData());
            try {
                org.bukkit.block.BlockState state = limitedRegion.getBlockState(cx, cy, cz);
                if (state instanceof org.bukkit.block.CreatureSpawner) {
                    org.bukkit.block.CreatureSpawner spawner = (org.bukkit.block.CreatureSpawner) state;
                    org.bukkit.entity.EntityType typeToSpawn;
                    
                    if (specificType != null) {
                        try {
                            typeToSpawn = org.bukkit.entity.EntityType.valueOf(specificType.toUpperCase());
                        } catch (Exception e) {
                            typeToSpawn = org.bukkit.entity.EntityType.ZOMBIE;
                        }
                    } else {
                        org.bukkit.entity.EntityType[] types = { 
                            org.bukkit.entity.EntityType.ZOMBIE, 
                            org.bukkit.entity.EntityType.SKELETON, 
                            org.bukkit.entity.EntityType.SPIDER,
                            org.bukkit.entity.EntityType.CAVE_SPIDER
                        };
                        typeToSpawn = types[random.nextInt(types.length)];
                    }
                    
                    CustomSpawnerRegistry.applyLightRule(spawner, typeToSpawn);
                }
            } catch (Exception ignored) { }
        }
    }
}
