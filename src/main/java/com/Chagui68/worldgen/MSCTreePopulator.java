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
        MSCBiomeProvider biomeProvider = generator.getBiomeProvider();
        if (biomeProvider == null) return;

        // Number of attempts to place a tree in this chunk
        int attempts = 4 + random.nextInt(4);

        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);
            
            int y = limitedRegion.getHighestBlockYAt(x, z);
            if (y < worldInfo.getMinHeight() || y > 150) continue;

            Material base = limitedRegion.getType(x, y, z);
            if (base != Material.GRASS_BLOCK && base != Material.DIRT && base != Material.MOSS_BLOCK) continue;

            String thematicId = biomeProvider.getThematicId(x, y, z);
            List<MSCTerraGenerator.TreeDefinition> trees = generator.getTrees(thematicId);
            
            if (trees != null && !trees.isEmpty()) {
                for (MSCTerraGenerator.TreeDefinition tree : trees) {
                    if (random.nextDouble() < tree.chance) {
                        generateTree(limitedRegion, x, y + 1, z, tree.type, random);
                        break; // Only one tree per spot
                    }
                }
            }
        }
    }

    private void generateTree(LimitedRegion region, int x, int y, int z, TreeType type, Random random) {
        switch (type) {
            case REDWOOD:
            case TALL_REDWOOD:
                spawnSpruce(region, x, y, z, random);
                break;
            case TREE:
            case BIG_TREE:
                spawnOak(region, x, y, z, random);
                break;
            case BIRCH:
            case TALL_BIRCH:
                spawnBirch(region, x, y, z, random);
                break;
            case DARK_OAK:
                spawnDarkOak(region, x, y, z, random);
                break;
            default:
                spawnOak(region, x, y, z, random);
                break;
        }
    }

    private void spawnOak(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 4 + random.nextInt(3);
        // Trunk
        for (int i = 0; i < height; i++) {
            setLog(region, x, y + i, z, Material.OAK_LOG);
        }
        // Leaves
        for (int ly = -2; ly <= 1; ly++) {
            int radius = (ly > -1) ? 1 : 2;
            for (int lx = -radius; lx <= radius; lx++) {
                for (int lz = -radius; lz <= radius; lz++) {
                    if (Math.abs(lx) == radius && Math.abs(lz) == radius && (ly == 1 || random.nextInt(2) == 0)) continue;
                    setLeaves(region, x + lx, y + height + ly, z + lz, Material.OAK_LEAVES);
                }
            }
        }
    }

    private void spawnBirch(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 5 + random.nextInt(3);
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
    }

    private void spawnSpruce(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 6 + random.nextInt(4);
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
    }

    private void spawnDarkOak(LimitedRegion region, int x, int y, int z, Random random) {
        // Simple 2x2 trunk approximation or just 1x1 with bigger canopy
        int height = 6 + random.nextInt(2);
        for (int i = 0; i < height; i++) {
            setLog(region, x, y + i, z, Material.DARK_OAK_LOG);
            setLog(region, x + 1, y + i, z, Material.DARK_OAK_LOG);
            setLog(region, x, y + i, z + 1, Material.DARK_OAK_LOG);
            setLog(region, x + 1, y + i, z + 1, Material.DARK_OAK_LOG);
        }
        for (int ly = -1; ly <= 2; ly++) {
            int radius = 3 - ly / 2;
            for (int lx = -radius; lx <= radius + 1; lx++) {
                for (int lz = -radius; lz <= radius + 1; lz++) {
                    setLeaves(region, x + lx, y + height + ly, z + lz, Material.DARK_OAK_LEAVES);
                }
            }
        }
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
