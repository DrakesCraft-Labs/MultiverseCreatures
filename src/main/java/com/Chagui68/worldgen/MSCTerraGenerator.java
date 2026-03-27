package com.Chagui68.worldgen;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Bukkit ChunkGenerator that uses 3D noise and slant calculations.
 * Includes Abyssal, Cliffs, and Valley logic, plus custom caves and resources.
 */
public class MSCTerraGenerator extends ChunkGenerator {
    private final MultiverseCreatures plugin;
    private NoiseSampler sampler;
    private SlantCalculator slantCalculator;
    private MSCBiomeProvider biomeProvider;
    private long lastSeed = -1;
    private boolean resourcesLoaded = false;

    private final Map<String, List<OreDefinition>> biomeResources = new ConcurrentHashMap<>();
    private final Map<String, Double> spawnerChances = new ConcurrentHashMap<>();
    private final Map<String, List<FloraDefinition>> biomeFlora = new ConcurrentHashMap<>();
    private final Map<String, List<TreeDefinition>> biomeTrees = new ConcurrentHashMap<>();
    private final Map<String, TerrainProperties> biomeTerrain = new ConcurrentHashMap<>();

    public MSCTerraGenerator(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    static class TreeDefinition {
        org.bukkit.TreeType type;
        double chance;
        TreeDefinition(org.bukkit.TreeType t, double c) { this.type = t; this.chance = c; }
    }

    static class OreDefinition {
        Material material;
        double chance;
        OreDefinition(Material m, double c) { this.material = m; this.chance = c; }
    }

    static class FloraLayer {
        Material material;
        int count;
        FloraLayer(Material m, int c) { this.material = m; this.count = c; }
    }

    static class FloraDefinition {
        double chance;
        List<FloraLayer> layers;
        FloraDefinition(double c, List<FloraLayer> l) { this.chance = c; this.layers = l; }
    }

    static class TerrainProperties {
        double baseHeight;
        double amplitude;
        TerrainProperties(double bh, double amp) { this.baseHeight = bh; this.amplitude = amp; }
    }

    private void checkInit(long seed) {
        if (sampler == null || seed != lastSeed) {
            this.sampler = new NoiseSampler(seed, 0.005f);
            this.slantCalculator = new SlantCalculator(sampler);
            this.biomeProvider = new MSCBiomeProvider(plugin, sampler);
            this.lastSeed = seed;
            this.resourcesLoaded = false; // Trigger reload for new world/seed
        }
        if (!resourcesLoaded) {
            synchronized (this) {
                if (!resourcesLoaded) {
                    loadResources();
                    resourcesLoaded = true;
                }
            }
        }
    }

    public void reload() {
        this.resourcesLoaded = false;
    }

    private void loadResources() {
        try {
            if (plugin == null) return;
            
            java.io.File file = new java.io.File(plugin.getDataFolder(), "biomes.yml");
            org.bukkit.Bukkit.getLogger().info("[MSC] Loading biomes from: " + file.getAbsolutePath());
            if (!file.exists()) {
                org.bukkit.Bukkit.getLogger().warning("[MSC] biomes.yml not found at " + file.getAbsolutePath() + ". Creating default.");
                plugin.saveResource("biomes.yml", true);
            }

            biomeResources.clear();
            spawnerChances.clear();
            biomeFlora.clear();
            biomeTrees.clear();
            biomeTerrain.clear();

            org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            org.bukkit.configuration.ConfigurationSection biomes = config.getConfigurationSection("biomes");
            if (biomes != null) {
                for (String key : biomes.getKeys(false)) {
                    String thematicId = key;
                    org.bukkit.configuration.ConfigurationSection section = biomes.getConfigurationSection(key);
                    if (section == null) {
                        org.bukkit.Bukkit.getLogger().warning("[MSC] Biome section " + key + " is null!");
                        continue;
                    }
                    
                    // DEBUG: Log the keys in this section
                    org.bukkit.Bukkit.getLogger().info("[MSC] Section '" + thematicId + "' keys: " + section.getKeys(false));

                    // Load Resources/Ores
                    org.bukkit.configuration.ConfigurationSection res = section.getConfigurationSection("resources");
                    if (res != null) {
                        spawnerChances.put(thematicId, res.getDouble("spawner_chance", 0.0));
                        List<OreDefinition> ores = new ArrayList<>();
                        List<?> oreList = res.getList("ores");
                        if (oreList != null) {
                            for (Object obj : oreList) {
                                if (obj instanceof Map<?, ?> map) {
                                    try {
                                        Object matObj = map.get("material");
                                        Object chanceObj = map.get("chance");
                                        if (matObj != null && chanceObj != null) {
                                            Material mat = Material.valueOf(matObj.toString().toUpperCase());
                                            double chance = ((Number) chanceObj).doubleValue();
                                            ores.add(new OreDefinition(mat, chance));
                                        }
                                    } catch (Exception e) {
                                        org.bukkit.Bukkit.getLogger().warning("[MSC] Failed to parse ore in " + thematicId + ": " + e.getMessage());
                                    }
                                }
                            }
                        }
                        biomeResources.put(thematicId, ores);
                    } else {
                        org.bukkit.Bukkit.getLogger().warning("[MSC] No 'resources' section for " + thematicId);
                    }

                    // Load Terrain
                    org.bukkit.configuration.ConfigurationSection terrain = section.getConfigurationSection("terrain");
                    if (terrain != null) {
                        double bh = terrain.getDouble("base_height", 62.0);
                        double amp = terrain.getDouble("amplitude", 48.0);
                        biomeTerrain.put(thematicId, new TerrainProperties(bh, amp));
                    }

                    // Load Flora
                    List<?> floraList = section.getList("flora");
                    int floraCount = 0;
                    if (floraList != null && !floraList.isEmpty()) {
                        List<FloraDefinition> floras = new ArrayList<>();
                        for (Object obj : floraList) {
                            if (obj instanceof Map<?, ?> floraMap) {
                                try {
                                    Object chanceObj = floraMap.get("chance");
                                    if (chanceObj == null) continue;
                                    double chance = ((Number) chanceObj).doubleValue();
                                    
                                    List<?> rawLayers = (List<?>) floraMap.get("layers");
                                    if (rawLayers == null) continue;
                                    
                                    List<FloraLayer> layers = new ArrayList<>();
                                    for (Object layerObj : rawLayers) {
                                        if (layerObj instanceof Map<?, ?> layerMap) {
                                            Material mat = Material.valueOf(layerMap.get("material").toString().toUpperCase());
                                            int count = ((Number) layerMap.get("count")).intValue();
                                            layers.add(new FloraLayer(mat, count));
                                        }
                                    }
                                    if (!layers.isEmpty()) {
                                        floras.add(new FloraDefinition(chance, layers));
                                        floraCount++;
                                    }
                                } catch (Exception e) {
                                    org.bukkit.Bukkit.getLogger().warning("[MSC] Failed to parse flora in " + thematicId + ": " + e.getMessage());
                                }
                            }
                        }
                        biomeFlora.put(thematicId, floras);
                    }

                    // Load Trees
                    List<?> treeList = section.getList("trees");
                    if (treeList != null && !treeList.isEmpty()) {
                        List<TreeDefinition> trees = new ArrayList<>();
                        for (Object obj : treeList) {
                            if (obj instanceof Map<?, ?> treeMap) {
                                try {
                                    org.bukkit.TreeType type = org.bukkit.TreeType.valueOf(treeMap.get("type").toString().toUpperCase());
                                    double chance = ((Number) treeMap.get("chance")).doubleValue();
                                    trees.add(new TreeDefinition(type, chance));
                                } catch (Exception e) {
                                    org.bukkit.Bukkit.getLogger().warning("[MSC] Failed to parse tree in " + thematicId + ": " + e.getMessage());
                                }
                            }
                        }
                        biomeTrees.put(thematicId, trees);
                    }
                    org.bukkit.Bukkit.getLogger().info("[MSC] Loaded Biome: " + thematicId + " (Ores: " + (biomeResources.get(thematicId) != null ? biomeResources.get(thematicId).size() : 0) + ", Flora: " + floraCount + ", Trees: " + (biomeTrees.get(thematicId) != null ? biomeTrees.get(thematicId).size() : 0) + ")");
                }
            }
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("[MSC] Failed to load resources/flora: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        checkInit(worldInfo.getSeed());
        return biomeProvider;
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        checkInit(worldInfo.getSeed());

        int xOrig = chunkX << 4;
        int zOrig = chunkZ << 4;

                for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int absX = xOrig + x;
                int absZ = zOrig + z;

                for (int y = worldInfo.getMaxHeight() - 1; y >= worldInfo.getMinHeight(); y--) {
                    if (y < worldInfo.getMinHeight() + 3) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                        continue;
                    }

                    // Sync noise scale (0.02) with MSCBiomeProvider for more detail
                    double noise = sampler.sample(absX * 0.02, y * 0.02, absZ * 0.02);
                    
                    String thematicId = biomeProvider.getThematicId(absX, y, absZ);
                    TerrainProperties props = biomeTerrain.getOrDefault(thematicId, new TerrainProperties(62.0, 48.0));

                    // Vertical bias: biome-specific terrain parameters
                    double heightGradient = (double) (y - props.baseHeight) / props.amplitude; 
                    double density = noise - heightGradient;

                    if (density > 0) {
                        double slant = slantCalculator.getSlant(absX, y, absZ);
                        
                        // --- CAVE SYSTEM ---
                        double caveNoise = sampler.sampleCave(absX, y, absZ);
                        if (y < 60 && caveNoise > 0.6) {
                            chunkData.setBlock(x, y, z, Material.AIR);
                            
                            // Potential Spawner on cave floor
                            double sChance = spawnerChances.getOrDefault(thematicId, 0.0);
                            if (sChance > 0 && random.nextDouble() < sChance) {
                                if (y > worldInfo.getMinHeight() && isSolidAt(worldInfo, absX, y - 1, absZ)) {
                                    chunkData.setBlock(x, y, z, Material.SPAWNER);
                                }
                            }
                            continue; 
                        }

                        // --- ORE PLACEMENT ---
                        List<OreDefinition> ores = biomeResources.get(thematicId);
                        Material blockType = null;
                        
                        // Only roll if embedded: solid block above (from chunkData) and solid block below (from density)
                        Material above = (y + 1 < worldInfo.getMaxHeight()) ? chunkData.getType(x, y + 1, z) : Material.AIR;
                        boolean solidAbove = !above.isAir() && above != Material.WATER;
                        
                        if (ores != null && solidAbove && isSolidAt(worldInfo, absX, y - 1, absZ)) { 
                            for (OreDefinition ore : ores) {
                                // Apply a 0.2 multiplier to the chance to prevent oversaturation given we roll per block
                                if (random.nextDouble() < (ore.chance * 0.2)) {
                                    blockType = ore.material;
                                    break;
                                }
                            }
                        }

                        // --- THEMATIC BLOCK PLACEMENT ---
                        if (thematicId.equals("abyssal_plains") || (y < 8)) {
                            if (blockType == null) {
                                blockType = (slant > 0.85) ? Material.BLACKSTONE : Material.DEEPSLATE;
                            } else if (!blockType.name().startsWith("DEEPSLATE_")) {
                                try {
                                    Material dsVersion = Material.valueOf("DEEPSLATE_" + blockType.name());
                                    blockType = dsVersion;
                                } catch (Exception ignored) {}
                            }
                            chunkData.setBlock(x, y, z, blockType);
                        } else if (thematicId.equals("cliffs")) {
                            chunkData.setBlock(x, y, z, (blockType != null) ? blockType : Material.STONE);
                        } else if (thematicId.equals("valley")) {
                            chunkData.setBlock(x, y, z, (blockType != null) ? blockType : (slant > 0.75 ? Material.STONE : Material.DIRT));
                        } else {
                            if (y > 100) {
                                chunkData.setBlock(x, y, z, (blockType != null) ? blockType : Material.SNOW_BLOCK);
                            } else {
                                chunkData.setBlock(x, y, z, (blockType != null) ? blockType : (slant > 0.75 ? Material.STONE : Material.DIRT));
                            }
                        }

                        // --- VEIN CLUSTERING (ONE-STEP DOWN) ---
                        if (blockType != null && blockType.name().contains("ORE") && y - 1 > worldInfo.getMinHeight()) {
                            if (random.nextDouble() < 0.4) { // Reduced chance for extra blocks
                                if (isSolidAt(worldInfo, absX, y - 1, absZ)) {
                                    chunkData.setBlock(x, y - 1, z, blockType);
                                }
                            }
                        }
                    } else if (y < 62 && thematicId.equals("abyssal_plains")) {
                        // Only fill water in abyssal zones, dry biomes (valley/cliffs) stay air
                        chunkData.setBlock(x, y, z, Material.WATER);
                    }
                }
            }
        }
    }

    private boolean isSolidAt(WorldInfo worldInfo, int absX, int y, int absZ) {
        if (y < worldInfo.getMinHeight() || y >= worldInfo.getMaxHeight()) return false;
        
        String thematicId = biomeProvider.getThematicId(absX, y, absZ);
        TerrainProperties props = biomeTerrain.getOrDefault(thematicId, new TerrainProperties(62.0, 48.0));
        
        // Use the same noise parameters as generateNoise
        double noise = sampler.sample(absX * 0.02, y * 0.02, absZ * 0.02);
        double heightGradient = (double) (y - props.baseHeight) / props.amplitude;
        double density = noise - heightGradient;
        
        if (density <= 0) return false;
        
        // Check for cave at this location
        double caveNoise = sampler.sampleCave(absX, y, absZ);
        if (y < 60 && caveNoise > 0.6) return false;
        
        return true;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = worldInfo.getMaxHeight() - 1; y >= worldInfo.getMinHeight(); y--) {
                    Material type = chunkData.getType(x, y, z);
                    if (type == Material.GRASS_BLOCK || type == Material.DIRT) {
                        if (y + 1 < worldInfo.getMaxHeight() && chunkData.getType(x, y + 1, z).isAir()) {
                             if (type == Material.DIRT) {
                                chunkData.setBlock(x, y, z, Material.GRASS_BLOCK);
                             }
                             // Spawn Flora
                             String thematicId = biomeProvider.getThematicId(chunkX << 4 | x, y, chunkZ << 4 | z);
                             spawnFlora(worldInfo, random, x, y + 1, z, chunkData, thematicId);
                        }
                        break;
                    } else if (type == Material.DEEPSLATE) {
                        if (y + 1 < worldInfo.getMaxHeight() && chunkData.getType(x, y + 1, z).isAir()) {
                             if (random.nextDouble() < 0.1) {
                                chunkData.setBlock(x, y, z, Material.MAGMA_BLOCK);
                            } else {
                                // Potential Abyssal Flora?
                                String thematicId = biomeProvider.getThematicId(chunkX << 4 | x, y, chunkZ << 4 | z);
                                spawnFlora(worldInfo, random, x, y + 1, z, chunkData, thematicId);
                            }
                        }
                        break;
                    } else if (type == Material.STONE) {
                         break;
                    }
                }
            }
        }
    }

    private void spawnFlora(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int y, int z, @NotNull ChunkData chunkData, String thematicId) {
        List<FloraDefinition> floras = biomeFlora.get(thematicId);
        if (floras == null || floras.isEmpty()) return;

        for (FloraDefinition flora : floras) {
            if (random.nextDouble() < flora.chance) {
                int currentY = y;
                for (FloraLayer layer : flora.layers) {
                    for (int i = 0; i < layer.count; i++) {
                        if (currentY < worldInfo.getMaxHeight()) {
                            Material mat = layer.material;
                            org.bukkit.block.data.BlockData data = org.bukkit.Bukkit.createBlockData(mat);
                            
                            if (data instanceof org.bukkit.block.data.Bisected bisected) {
                                if (layer.count == 2) {
                                    bisected.setHalf(i == 0 ? org.bukkit.block.data.Bisected.Half.BOTTOM : org.bukkit.block.data.Bisected.Half.TOP);
                                } else {
                                    // Fallback for single-layer bisected (usually shouldn't happen for tall plants)
                                    bisected.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);
                                }
                                chunkData.setBlock(x, currentY, z, bisected);
                            } else {
                                chunkData.setBlock(x, currentY, z, mat);
                            }
                            currentY++;
                        }
                    }
                }
                break; // Only spawn one flora type per block
            }
        }
    }

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
        return List.of(new MSCTreePopulator(plugin, this));
    }

    public List<TreeDefinition> getTrees(String thematicId) {
        return biomeTrees.get(thematicId);
    }
    
    public MSCBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }
}
