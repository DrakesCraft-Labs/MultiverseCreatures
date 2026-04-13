package com.Chagui68.worldgen;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.biome.BiomeConfig;
import com.Chagui68.biome.BiomeProvider;
import com.Chagui68.config.ConfigPack;
import com.Chagui68.worldgen.MSCBiomeProvider;
import com.Chagui68.worldgen.MSCBiomeProviderCompat;
import com.Chagui68.worldgen.NoiseSampler;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * Main world generator implementing a Terra-style heightmap fill,
 * ore embedding checks, sparse flora, ponds and simple structure placement.
 */
public class MSCTerraGenerator extends ChunkGenerator {

    private final MultiverseCreatures plugin;
    private final ConfigPack configPack;
    private com.Chagui68.biome.BiomeProvider biomeProvider;
    private NoiseSampler noiseSampler;
    private MSCBiomeProvider compatBiomeProvider;

    public MSCTerraGenerator(MultiverseCreatures plugin, ConfigPack configPack) {
        this.plugin = plugin;
        this.configPack = configPack;
    }

    private void checkInit(long seed) {
        if (this.noiseSampler == null) {
            this.noiseSampler = new NoiseSampler(seed, 0.02f);
        }
        if (this.configPack.getBiomeProvider() == null) {
            this.configPack.initializeBiomeProvider(this.noiseSampler);
        }
        if (this.biomeProvider == null) {
            this.biomeProvider = this.configPack.getBiomeProvider();
        }
        if (this.compatBiomeProvider == null) {
            this.compatBiomeProvider = new MSCBiomeProviderCompat(this.biomeProvider, this.plugin, this.noiseSampler);
        }
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        checkInit(worldInfo.getSeed());

        int xOrig = chunkX << 4;
        int zOrig = chunkZ << 4;
        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        // Heightmap-based fill: compute a target surface height per column and fill below it.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int absX = xOrig + x;
                int absZ = zOrig + z;

                BiomeConfig biome = biomeProvider.getBiome(absX, 64, absZ, worldInfo.getSeed());
                if (biome == null) continue;

                double avgBaseHeight = 0;
                double avgAmplitude = 0;
                int count = 0;
                for (int dx = -4; dx <= 4; dx += 4) {
                    for (int dz = -4; dz <= 4; dz += 4) {
                        BiomeConfig b = biomeProvider.getBiome(absX + dx, 64, absZ + dz, worldInfo.getSeed());
                        if (b != null) {
                            avgBaseHeight += b.getBaseHeight();
                            avgAmplitude += b.getAmplitude();
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    avgBaseHeight /= count;
                    avgAmplitude /= count;
                } else {
                    avgBaseHeight = biome.getBaseHeight();
                    avgAmplitude = biome.getAmplitude();
                }

                double baseNoise = noiseSampler.sample(absX, avgBaseHeight, absZ);
                double jitter = noiseSampler.sampleCave(absX, avgBaseHeight, absZ) * 0.08;
                double columnNoise = baseNoise + jitter;

                int targetHeight = (int) Math.round(avgBaseHeight + columnNoise * avgAmplitude);
                targetHeight = Math.max(minY, Math.min(maxY - 1, targetHeight));

                for (int y = minY; y < maxY; y++) {
                    if (y < minY + 3) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                        continue;
                    }

                    if (y > targetHeight) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                        continue;
                    }



                    Material block = getBlockFromBiomePalette(biome, y, targetHeight, x, z, absX, absZ, random, worldInfo, chunkData);
                    chunkData.setBlock(x, y, z, block);
                }

                if (biome.getId().equals("abyssal_plains")) {
                    for (int y = minY; y <= Math.min(62, maxY - 1); y++) {
                        if (y < maxY) {
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get block from biome palette with embedded ore checks.
     */
    private Material getBlockFromBiomePalette(BiomeConfig biome, int y, int targetHeight, int localX, int localZ, int absX, int absZ,
                                             Random random, WorldInfo worldInfo, ChunkData chunkData) {
        List<Material> palette = biome.getPalette();

        Material block;
        if (palette.isEmpty()) {
            if (biome.getId().equals("abyssal_plains")) {
                block = y < 5 ? Material.DEEPSLATE : Material.BLACKSTONE;
            } else if (biome.getId().equals("cliffs")) {
                block = y > 150 ? Material.SNOW_BLOCK : (y <= 0 ? Material.DEEPSLATE : Material.STONE);
            } else {
                block = y > 62 ? Material.DIRT : (y <= 0 ? Material.DEEPSLATE : Material.STONE);
            }
        } else {
            int depth = targetHeight - y;
            if (depth < 0) depth = 0;
            int idx = Math.max(0, Math.min(depth, palette.size() - 1));
            block = palette.get(idx);
        }



        if (y <= 0) {
            if (block == Material.STONE) return Material.DEEPSLATE;
            if (block == Material.DIRT && y < -10) return Material.DEEPSLATE;
        }

        return block;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random,
                               int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        checkInit(worldInfo.getSeed());

        int xOrig = chunkX << 4;
        int zOrig = chunkZ << 4;

        boolean pondPlanned = false;
        int pondLocalX = -1;
        int pondLocalZ = -1;
        boolean structurePlanned = false;
        int structLocalX = -1;
        int structLocalZ = -1;

        com.Chagui68.biome.BiomeConfig centerBiome = biomeProvider.getBiome(xOrig + 8, 64, zOrig + 8, worldInfo.getSeed());
        if (centerBiome != null && (centerBiome.getId().equals("valley") || centerBiome.getId().toLowerCase().contains("meadow"))) {
            if (random.nextDouble() < 0.006) {
                structurePlanned = true;
                structLocalX = random.nextInt(16);
                structLocalZ = random.nextInt(16);
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int absX = xOrig + x;
                int absZ = zOrig + z;

                for (int y = worldInfo.getMaxHeight() - 1; y >= worldInfo.getMinHeight(); y--) {
                    Material type = chunkData.getType(x, y, z);

                    if (type == Material.DIRT || type == Material.GRASS_BLOCK) {
                        if (y + 1 < worldInfo.getMaxHeight() && chunkData.getType(x, y + 1, z).isAir()) {
                            chunkData.setBlock(x, y, z, Material.GRASS_BLOCK);

                            if (structurePlanned && x == structLocalX && z == structLocalZ) {
                                if (CustomStructureRegistry.isRegistered("RUIN")) {
                                    CustomStructureRegistry.generate("RUIN", chunkData, x, y + 1, z, random, worldInfo);
                                }
                            } else {
                                BiomeConfig biome = biomeProvider.getBiome(absX, y, absZ, worldInfo.getSeed());
                                if (biome != null) {
                                    generateFlora(biome, chunkData, x, y + 1, z, random, worldInfo);
                                }
                            }
                        }
                        break;
                    } else if (type == Material.STONE || type == Material.DEEPSLATE) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Generate flora at surface level
     */
    private void generateFlora(BiomeConfig biome, ChunkData chunkData, int x, int y, int z,
                          Random random, WorldInfo worldInfo) {
    List<BiomeConfig.FloraDefinition> floraList = biome.getFlora();
    if (floraList == null || floraList.isEmpty()) return;

    for (BiomeConfig.FloraDefinition flora : floraList) {
        if (random.nextDouble() < flora.chance) {
            int currentY = y;
            int maxY = worldInfo.getMaxHeight();

            for (BiomeConfig.FloraLayer layer : flora.layers) {
                for (int i = 0; i < layer.count && currentY < maxY; i++) {
                    Material mat = layer.material;
                    org.bukkit.block.data.BlockData data = org.bukkit.Bukkit.createBlockData(mat);

                    if (data instanceof org.bukkit.block.data.Bisected bisected) {
                        bisected.setHalf(i == 0 ? org.bukkit.block.data.Bisected.Half.BOTTOM : org.bukkit.block.data.Bisected.Half.TOP);
                        chunkData.setBlock(x, currentY, z, bisected);
                    } else {
                        chunkData.setBlock(x, currentY, z, mat);
                    }
                    currentY++;
                }
            }
            break;
        }
    }
}

    // Pond generation migrated to MSCOrePopulator for cross-chunk support

    @Override
    public boolean shouldGenerateNoise() { return true; }

    @Override
    public boolean shouldGenerateSurface() { return true; }

    @Override
    public boolean shouldGenerateBedrock() { return true; }

    @Override
    public boolean shouldGenerateCaves() { return true; }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return List.of(new MSCOrePopulator(plugin, this), new MSCTreePopulator(plugin, this));
    }

    @Override
    public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        checkInit(worldInfo.getSeed());
        return new MSCBiomeProviderAdapter(this.biomeProvider);
    }

    // Compatibility helpers
    public MSCBiomeProvider getBiomeProvider() {
        checkInit(0);
        return compatBiomeProvider;
    }

    public List<TreeDefinition> getTrees(String biomeId) {
        BiomeConfig biome = configPack.getBiomeRegistry().getByID(biomeId).orElse(null);
        if (biome == null) return new ArrayList<>();

        List<TreeDefinition> trees = new ArrayList<>();
        for (BiomeConfig.TreeDefinition treeDef : biome.getTrees()) {
            if (treeDef.isCustom && treeDef.customId != null) {
                trees.add(new TreeDefinition(treeDef.customId, treeDef.chance));
            } else {
                trees.add(new TreeDefinition(treeDef.type, treeDef.chance));
            }
        }
        return trees;
    }

    public static class TreeDefinition {
        public TreeType type;
        public String customId;
        public double chance;
        public boolean isCustom;

        public TreeDefinition(TreeType type, double chance) {
            this.type = type;
            this.customId = null;
            this.chance = chance;
            this.isCustom = false;
        }

        public TreeDefinition(String customId, double chance) {
            this.type = null;
            this.customId = customId;
            this.chance = chance;
            this.isCustom = true;
        }
    }

    // Getters
    public ConfigPack getConfigPack() { return configPack; }
    public BiomeProvider getInternalBiomeProvider() { return biomeProvider; }
    public NoiseSampler getNoiseSampler() { return noiseSampler; }
}
