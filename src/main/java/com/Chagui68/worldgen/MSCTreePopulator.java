package com.Chagui68.worldgen;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;



public class MSCTreePopulator extends BlockPopulator {
    private final MultiverseCreatures plugin;
    private final MSCTerraGenerator generator;

    public MSCTreePopulator(MultiverseCreatures plugin, MSCTerraGenerator generator) {
        this.plugin = plugin;
        this.generator = generator;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Get the internal biome provider (not the Bukkit adapter)
        com.Chagui68.biome.BiomeProvider biomeProvider = generator.getInternalBiomeProvider();
        if (biomeProvider == null) return;

        // Number of attempts to place a tree in this chunk.
        // Scaled by per-biome `treeRate` (configured in biomes.yml). We sample the center of the chunk
        // to decide density for this chunk.
        int baseAttempts = 6 + random.nextInt(6);
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        com.Chagui68.biome.BiomeConfig centerBiome = biomeProvider.getBiome(centerX, 64, centerZ, worldInfo.getSeed());
        double treeRate = centerBiome != null ? centerBiome.getTreeRate() : 1.0;
        int attempts = Math.max(0, (int) Math.round(baseAttempts * treeRate));

        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);

            // Find the surface manually instead of using getHighestBlockYAt
            int y = -1;
            for (int checkY = worldInfo.getMaxHeight() - 1; checkY >= worldInfo.getMinHeight(); checkY--) {
                Material mat = limitedRegion.getType(x, checkY, z);
                if (mat == Material.GRASS_BLOCK || mat == Material.DIRT || mat == Material.MOSS_BLOCK || mat == Material.COARSE_DIRT || mat == Material.PODZOL) {
                    y = checkY;
                    break;
                }
            }
            
            if (y < worldInfo.getMinHeight() || y > 255) continue;

            // Check base block for trees (allow a few more ground types)
            Material base = limitedRegion.getType(x, y, z);
            if (base != Material.GRASS_BLOCK && base != Material.DIRT && base != Material.MOSS_BLOCK
                && base != Material.COARSE_DIRT && base != Material.PODZOL) continue;

            // Check that we have space above for the tree (at least 5 blocks of air)
            boolean hasSpace = true;
            int requiredSpace = 5;
            for (int checkY = y + 1; checkY <= Math.min(y + requiredSpace, worldInfo.getMaxHeight() - 1); checkY++) {
                Material blockAbove = limitedRegion.getType(x, checkY, z);
                if (!blockAbove.isAir()) {
                    hasSpace = false;
                    break;
                }
            }
            if (!hasSpace) continue;

                // Get biome and trees from it
                com.Chagui68.biome.BiomeConfig biomeConfig = biomeProvider.getBiome(x, y, z, worldInfo.getSeed());
                if (biomeConfig == null) continue;

                List<com.Chagui68.biome.BiomeConfig.TreeDefinition> trees = biomeConfig.getTrees();

                if (trees != null && !trees.isEmpty()) {
                    // Try each tree type with configured chances
                    for (com.Chagui68.biome.BiomeConfig.TreeDefinition tree : trees) {
                        if (random.nextDouble() < tree.chance) {
                            boolean treeGenerated = false;
                            if (tree.isCustom && tree.customId != null) {
                                treeGenerated = CustomTreeRegistry.generate(tree.customId, limitedRegion, x, y + 1, z, random);
                            } else {
                                treeGenerated = generateTree(limitedRegion, x, y + 1, z, tree.type, random);
                            }
                            if (treeGenerated) break; // Only one tree per spot
                        }
                    }
                }
        }
    }

    private boolean generateTree(LimitedRegion region, int x, int y, int z, TreeType type, Random random) {
        switch (type) {
            case REDWOOD:
            case TALL_REDWOOD:
                return spawnSpruce(region, x, y, z, random);
            case TREE:
            case BIG_TREE:
                return spawnOak(region, x, y, z, random);
            case BIRCH:
            case TALL_BIRCH:
                return spawnBirch(region, x, y, z, random);
            case ACACIA:
                return spawnOak(region, x, y, z, random); // Fallback to oak
            default:
                return spawnOak(region, x, y, z, random);
        }
    }

    private boolean spawnOak(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 4 + random.nextInt(3);
        
        // Verify we can place the trunk
        for (int i = 0; i < height; i++) {
            if (!region.isInRegion(x, y + i, z)) return false;
        }
        
        // Place trunk
        for (int i = 0; i < height; i++) {
            setLog(region, x, y + i, z, Material.OAK_LOG);
        }
        
        // Place leaves
        for (int ly = -2; ly <= 1; ly++) {
            int radius = (ly > -1) ? 1 : 2;
            for (int lx = -radius; lx <= radius; lx++) {
                for (int lz = -radius; lz <= radius; lz++) {
                    if (Math.abs(lx) == radius && Math.abs(lz) == radius && (ly == 1 || random.nextInt(2) == 0)) continue;
                    setLeaves(region, x + lx, y + height + ly, z + lz, Material.OAK_LEAVES);
                }
            }
        }
        return true;
    }

    private boolean spawnBirch(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 5 + random.nextInt(3);
        
        for (int i = 0; i < height; i++) {
            if (!region.isInRegion(x, y + i, z)) return false;
        }
        
        for (int i = 0; i < height; i++) {
            setLog(region, x, y + i, z, Material.BIRCH_LOG);
        }
        for (int ly = -2; ly <= 1; ly++) {
            int radius = (ly > -1) ? 1 : 2;
            for (int lx = -radius; lx <= radius; lx++) {
                for (int lz = -radius; lz <= radius; lz++) {
                    if (Math.abs(lx) == radius && Math.abs(lz) == radius && (ly == 1 || random.nextInt(2) == 0)) continue;
                    setLeaves(region, x + lx, y + height + ly, z + lz, Material.BIRCH_LEAVES);
                }
            }
        }
        return true;
    }

    private boolean spawnSpruce(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 6 + random.nextInt(4);
        
        for (int i = 0; i < height; i++) {
            if (!region.isInRegion(x, y + i, z)) return false;
        }
        
        for (int i = 0; i < height; i++) {
            setLog(region, x, y + i, z, Material.SPRUCE_LOG);
        }
        int leafStart = 2 + random.nextInt(2);
        for (int ly = leafStart; ly <= height + 1; ly++) {
            int radius = (height + 1 - ly) / 2 + 1;
            if (radius > 2) radius = 2;
            for (int lx = -radius; lx <= radius; lx++) {
                for (int lz = -radius; lz <= radius; lz++) {
                    if (Math.abs(lx) == radius && Math.abs(lz) == radius && ly % 2 == 0) continue;
                    setLeaves(region, x + lx, y + ly, z + lz, Material.SPRUCE_LEAVES);
                }
            }
        }
        return true;
    }



    private void setLog(LimitedRegion region, int x, int y, int z, Material log) {
        if (region.isInRegion(x, y, z)) {
            region.setBlockData(x, y, z, log.createBlockData());
        }
    }

    private void setLeaves(LimitedRegion region, int x, int y, int z, Material leaves) {
        if (region.isInRegion(x, y, z) && region.getType(x, y, z).isAir()) {
            region.setBlockData(x, y, z, leaves.createBlockData());
        }
    }
}
