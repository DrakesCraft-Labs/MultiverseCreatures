package com.Chagui68.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a custom biome configuration (equivalent to Terra's UserDefinedBiome)
 * Reference: com.dfsek.terra.addons.biome.UserDefinedBiome
 */
public class BiomeConfig {
    private final String id;                          // Unique identifier
    private final Biome vanillaBiome;                 // Base vanilla biome
    private final String displayName;                 // Display name
    
    // Terrain properties
    private double baseHeight = 64.0;
    private double amplitude = 20.0;
    
    // Palette of blocks by depth
    private final List<Material> palette = new ArrayList<>();
    
    // Ore/mineral resources
    private final List<OreDefinition> ores = new ArrayList<>();
    
    // Flora (plants, grass, etc.)
    private final List<FloraDefinition> flora = new ArrayList<>();
    
    // Trees
    private final List<TreeDefinition> trees = new ArrayList<>();
    
    // Per-biome tree spawn rate multiplier (1.0 = default)
    private double treeRate = 1.0;
    
    // Chance for a legacy spawner (dungeon) to generate in this biome
    private double spawnerChance = 0.0;
    
    // Custom spawners/dungeons list
    private final List<SpawnerDefinition> spawners = new ArrayList<>();
    
    // Tags for biome categorization (like Terra)
    private final Set<String> tags = new HashSet<>();
    
    // Internal ID (like Terra)
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final int intID;
    
    // Biome color
    private int color = 0x00FF00;

    /**
     * Constructor (equivalent to Terra's UserDefinedBiome)
     */
    public BiomeConfig(String id, Biome vanillaBiome, String displayName) {
        this.id = id;
        this.vanillaBiome = vanillaBiome;
        this.displayName = displayName;
        this.intID = ID_COUNTER.getAndIncrement();
        
        // Tags automáticos
        this.tags.add("BIOME:" + id);
        this.tags.add("ALL");
    }

    // ========== GETTERS & SETTERS ==========
    
    public String getId() {
        return id;
    }

    public Biome getVanillaBiome() {
        return vanillaBiome;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseHeight() {
        return baseHeight;
    }

    public void setBaseHeight(double baseHeight) {
        this.baseHeight = baseHeight;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public List<Material> getPalette() {
        return palette;
    }

    public void addPaletteBlock(Material material) {
        this.palette.add(material);
    }

    public List<OreDefinition> getOres() {
        return ores;
    }

    public void addOre(OreDefinition ore) {
        this.ores.add(ore);
    }

    public List<FloraDefinition> getFlora() {
        return flora;
    }

    public void addFlora(FloraDefinition flora) {
        this.flora.add(flora);
    }

    public List<TreeDefinition> getTrees() {
        return trees;
    }

    public void addTree(TreeDefinition tree) {
        this.trees.add(tree);
    }

    public double getTreeRate() {
        return treeRate;
    }

    public void setTreeRate(double treeRate) {
        this.treeRate = treeRate;
    }

    public double getSpawnerChance() {
        return spawnerChance;
    }

    public void setSpawnerChance(double spawnerChance) {
        this.spawnerChance = spawnerChance;
    }

    public List<SpawnerDefinition> getSpawners() {
        return spawners;
    }

    public void addSpawner(SpawnerDefinition spawner) {
        this.spawners.add(spawner);
    }

    public Set<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public int getIntID() {
        return intID;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "{BIOME:" + id + "}";
    }

    // ========== INNER CLASSES ==========

    public static class OreDefinition {
        public final Material material;
        public final double chance;
        public final int minY;
        public final int maxY;

        public OreDefinition(Material material, double chance, int minY, int maxY) {
            this.material = material;
            this.chance = chance;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    public static class FloraDefinition {
        public final double chance;
        public final List<FloraLayer> layers;

        public FloraDefinition(double chance, List<FloraLayer> layers) {
            this.chance = chance;
            this.layers = layers;
        }
    }

    public static class FloraLayer {
        public final Material material;
        public final int count;

        public FloraLayer(Material material, int count) {
            this.material = material;
            this.count = count;
        }
    }

    public static class SpawnerDefinition {
        public final String type;
        public final String customId;
        public final double chance;
        public final boolean isCustom;
        public final int minY;
        public final int maxY;

        public SpawnerDefinition(String type, double chance, int minY, int maxY) {
            this.type = type;
            this.customId = null;
            this.chance = chance;
            this.isCustom = false;
            this.minY = minY;
            this.maxY = maxY;
        }

        public SpawnerDefinition(String customId, double chance, boolean isCustom, int minY, int maxY) {
            this.type = null;
            this.customId = customId;
            this.chance = chance;
            this.isCustom = true;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    public static class TreeDefinition {
        public final org.bukkit.TreeType type;
        public final String customId;
        public final double chance;
        public final boolean isCustom;

        public TreeDefinition(org.bukkit.TreeType type, double chance) {
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
}
