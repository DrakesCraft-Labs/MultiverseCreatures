package com.Chagui68.config;

import com.Chagui68.biome.BiomeConfig;
import com.Chagui68.biome.BiomeRegistry;
import com.Chagui68.biome.BiomeProvider;
import com.Chagui68.biome.NoiseBiomeProvider;
import com.Chagui68.worldgen.NoiseSampler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.TreeType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * ConfigPack - Contenedor de toda la configuración de un pack de mundos
 * (equivalente a: com.dfsek.terra.api.config.ConfigPack)
 * 
 * Contiene:
 * - Registry de biomas
 * - BiomeProvider (distribución de biomas)
 * - Configuración general
 */
public class ConfigPack {
    private final String id;                    // ID del pack (ej: "default", "mythical")
    private final String version;
    private final String author;
    private final File packFolder;
    
    private final BiomeRegistry biomeRegistry;  // Todos los biomas
    private BiomeProvider biomeProvider;        // Provider que distribuye biomas
    
    /**
     * Constructor - Carga un ConfigPack desde carpeta
     * Equivalente a new ConfigPackImpl(path, platform) de Terra
     */
    public ConfigPack(String id, File packFolder) {
        this.id = id;
        this.packFolder = packFolder;
        this.biomeRegistry = new BiomeRegistry();
        
        // Cargar metadata
        this.version = "1.0";
        this.author = "Unknown";
        
        // Cargar configuración desde YAML
        loadFromYAML();
        
        Bukkit.getLogger().info("[MSC] Loaded config pack: " + id);
    }
    
    /**
     * Cargar configuración desde biomes.yml
     * Equivalente a como Terra carga biomas desde YAML
     */
    private void loadFromYAML() {
        File biomesFile = new File(packFolder, "biomes.yml");
        
        if (!biomesFile.exists()) {
            Bukkit.getLogger().warning("[MSC] biomes.yml not found in pack: " + id);
            createDefaultBiomes();
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(biomesFile);
        
        // Iterar sobre cada bioma en el YAML
        var biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection != null) {
            for (String biomeKey : biomesSection.getKeys(false)) {
                var section = biomesSection.getConfigurationSection(biomeKey);
                if (section != null) {
                    BiomeConfig biome = loadBiomeFromYAML(biomeKey, section);
                    biomeRegistry.register(biomeKey, biome);
                }
            }
        }
        
        Bukkit.getLogger().info("[MSC] Loaded " + biomeRegistry.size() + " biomes for pack: " + id);
    }
    
    /**
     * Cargar un bioma individual desde YAML
     * Equivalente a cómo Terra carga BiomeTemplate
     */
    private BiomeConfig loadBiomeFromYAML(String biomeId, 
                                         org.bukkit.configuration.ConfigurationSection section) {
        try {
            // Obtener bioma vanilla base (handle both "PLAINS" and "minecraft:plains" formats)
            String vanillaBiomeName = section.getString("base_biome", "PLAINS");
            // Strip namespace prefix if present (e.g. "minecraft:deep_ocean" -> "deep_ocean")
            if (vanillaBiomeName.contains(":")) {
                vanillaBiomeName = vanillaBiomeName.substring(vanillaBiomeName.indexOf(':') + 1);
            }
            Biome vanillaBiome = Biome.valueOf(vanillaBiomeName.toUpperCase());
            
            // Crear BiomeConfig
            String displayName = section.getString("display_name", biomeId);
            BiomeConfig biome = new BiomeConfig(biomeId, vanillaBiome, displayName);
            
            // Propiedades del terreno
            if (section.contains("terrain")) {
                var terrainSection = section.getConfigurationSection("terrain");
                if (terrainSection != null) {
                    biome.setBaseHeight(terrainSection.getDouble("base_height", 64.0));
                    biome.setAmplitude(terrainSection.getDouble("amplitude", 20.0));
                }
            }
            
            // Paleta de bloques
            if (section.contains("palette")) {
                List<String> paletteList = section.getStringList("palette");
                if (paletteList != null) {
                    for (String matStr : paletteList) {
                        try {
                            Material mat = Material.valueOf(matStr);
                            biome.addPaletteBlock(mat);
                        } catch (IllegalArgumentException e) {
                            Bukkit.getLogger().warning("[MSC] Invalid material: " + matStr);
                        }
                    }
                }
            }
            
            // Minerales
            if (section.contains("resources.ores")) {
                List<?> oresList = section.getList("resources.ores");
                if (oresList != null) {
                    for (Object obj : oresList) {
                        if (obj instanceof Map<?, ?> map) {
                            Material mat = Material.valueOf(map.get("material").toString());
                            double chance = Double.parseDouble(map.get("chance").toString());
                            int minY = map.containsKey("min_y") ? ((Number) map.get("min_y")).intValue() : -64;
                            int maxY = map.containsKey("max_y") ? ((Number) map.get("max_y")).intValue() : 320;
                            biome.addOre(new BiomeConfig.OreDefinition(mat, chance, minY, maxY));
                        }
                    }
                }
            }
            
            // Spawners (legacy chance)
            if (section.contains("resources.spawner_chance")) {
                biome.setSpawnerChance(section.getDouble("resources.spawner_chance", 0.0));
            }

            // Custom Spawners List
            if (section.contains("resources.spawners")) {
                List<?> spawnerList = section.getList("resources.spawners");
                if (spawnerList != null) {
                    for (Object obj : spawnerList) {
                        if (obj instanceof Map<?, ?> spawnerMap) {
                            try {
                                String typeStr = spawnerMap.get("type").toString();
                                double chance = Double.parseDouble(spawnerMap.get("chance").toString());
                                int minY = spawnerMap.containsKey("min_y") ? ((Number) spawnerMap.get("min_y")).intValue() : -64;
                                int maxY = spawnerMap.containsKey("max_y") ? ((Number) spawnerMap.get("max_y")).intValue() : 320;
                                if (typeStr.startsWith("CUSTOM:")) {
                                    String customId = typeStr.substring("CUSTOM:".length());
                                    biome.addSpawner(new BiomeConfig.SpawnerDefinition(customId, chance, true, minY, maxY));
                                } else {
                                    biome.addSpawner(new BiomeConfig.SpawnerDefinition(typeStr, chance, minY, maxY));
                                }
                            } catch (Exception e) {
                                Bukkit.getLogger().warning("[MSC] Failed to parse spawner definition: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            
            // Flora
            if (section.contains("flora")) {
                List<?> floraList = section.getList("flora");
                if (floraList != null) {
                    for (Object obj : floraList) {
                        if (obj instanceof Map<?, ?> floraMap) {
                            Double chanceObj = (Double) floraMap.get("chance");
                            if (chanceObj == null) continue;
                            double chance = chanceObj;
                            
                            List<?> rawLayers = (List<?>) floraMap.get("layers");
                            if (rawLayers == null) continue;
                            
                            List<BiomeConfig.FloraLayer> layers = new ArrayList<>();
                            for (Object layerObj : rawLayers) {
                                if (layerObj instanceof Map<?, ?> layerMap) {
                                    try {
                                        Material mat = Material.valueOf(layerMap.get("material").toString());
                                        int count = ((Number) layerMap.get("count")).intValue();
                                        layers.add(new BiomeConfig.FloraLayer(mat, count));
                                    } catch (Exception e) {
                                        Bukkit.getLogger().warning("[MSC] Failed to parse flora layer: " + e.getMessage());
                                    }
                                }
                            }
                            
                            if (!layers.isEmpty()) {
                                biome.addFlora(new BiomeConfig.FloraDefinition(chance, layers));
                            }
                        }
                    }
                }
            }

            // Trees
            if (section.contains("trees")) {
                List<?> treesList = section.getList("trees");
                if (treesList != null) {
                    for (Object obj : treesList) {
                        if (obj instanceof Map<?, ?> treeMap) {
                            try {
                                String typeStr = treeMap.get("type").toString();
                                double chance = Double.parseDouble(treeMap.get("chance").toString());
                                if (typeStr.startsWith("CUSTOM:")) {
                                    String customId = typeStr.substring("CUSTOM:".length());
                                    biome.addTree(new BiomeConfig.TreeDefinition(customId, chance));
                                } else {
                                    TreeType t = TreeType.valueOf(typeStr);
                                    biome.addTree(new BiomeConfig.TreeDefinition(t, chance));
                                }
                            } catch (Exception e) {
                                Bukkit.getLogger().warning("[MSC] Failed to parse tree definition: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // Tree spawn rate multiplier (optional)
            if (section.contains("tree_rate")) {
                double treeRate = section.getDouble("tree_rate", 1.0);
                biome.setTreeRate(treeRate);
            }
            
            // Color
            if (section.contains("color")) {
                biome.setColor(section.getInt("color", 0x00FF00));
            }
            
            return biome;
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("[MSC] Failed to load biome " + biomeId + ": " + e.getMessage());
            return createDefaultBiome(biomeId);
        }
    }
    
    /**
     * Crear biomas por defecto si no hay YAML
     */
    private void createDefaultBiomes() {
        // Crear bioma valley
        BiomeConfig valley = new BiomeConfig("valley", Biome.MEADOW, "§2§lVALLEY");
        valley.setBaseHeight(64.0);
        valley.setAmplitude(20.0);
        valley.addPaletteBlock(Material.GRASS_BLOCK);
        valley.addPaletteBlock(Material.DIRT);
        valley.addPaletteBlock(Material.STONE);
        biomeRegistry.register("valley", valley);
        
        // Crear bioma cliffs
        BiomeConfig cliffs = new BiomeConfig("cliffs", Biome.JAGGED_PEAKS, "§fCLIFFS");
        cliffs.setBaseHeight(120.0);
        cliffs.setAmplitude(60.0);
        cliffs.addPaletteBlock(Material.STONE);
        cliffs.addPaletteBlock(Material.STONE);
        biomeRegistry.register("cliffs", cliffs);
        
        // Crear bioma abyssal_plains
        BiomeConfig abyssal = new BiomeConfig("abyssal_plains", Biome.DEEP_OCEAN, "§1ABYSSAL");
        abyssal.setBaseHeight(32.0);
        abyssal.setAmplitude(10.0);
        abyssal.addPaletteBlock(Material.DEEPSLATE);
        biomeRegistry.register("abyssal_plains", abyssal);
    }
    
    /**
     * Crear un bioma por defecto con propiedades básicas
     */
    private BiomeConfig createDefaultBiome(String id) {
        BiomeConfig biome = new BiomeConfig(id, Biome.PLAINS, id);
        biome.setBaseHeight(64.0);
        biome.setAmplitude(20.0);
        biome.addPaletteBlock(Material.GRASS_BLOCK);
        biome.addPaletteBlock(Material.DIRT);
        biome.addPaletteBlock(Material.STONE);
        return biome;
    }
    
    /**
     * Inicializar el BiomeProvider con un NoiseSampler
     * (equivalente a setear el generador en Terra)
     */
    public void initializeBiomeProvider(NoiseSampler sampler) {
        this.biomeProvider = new NoiseBiomeProvider(sampler, biomeRegistry);
    }
    
    // ========== GETTERS ==========
    
    public String getId() {
        return id;
    }
    
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }
    
    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public File getPackFolder() {
        return packFolder;
    }
}
