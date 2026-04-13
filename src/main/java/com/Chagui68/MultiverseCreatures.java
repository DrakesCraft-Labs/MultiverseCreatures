package com.Chagui68;

import com.Chagui68.config.ConfigPack;
import com.Chagui68.entities.MobHandler;
import com.Chagui68.entities.dragon.DragonCombatHandler;
import com.Chagui68.entities.dragon.DragonSpawnHandler;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemCombatHandler;
import com.Chagui68.listener.ItemFoodHandler;
import com.Chagui68.config.ConfigPackRegistry;
import com.Chagui68.utils.WorldBootstrapService;
import com.Chagui68.worldgen.CustomStructureRegistry;
import com.Chagui68.worldgen.CustomTreeRegistry;
import com.Chagui68.worldgen.MSCTerraGenerator;
import com.Chagui68.worldgen.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class MultiverseCreatures extends JavaPlugin {
    private final List<com.Chagui68.worldgen.MSCTerraGenerator> generators = new ArrayList<>();
    private ConfigPackRegistry configPackRegistry;
    private DragonCombatHandler dragonCombatHandler;

    @Override
    public void onLoad() {
        // No longer using custom biomes for stability, strictly thematic visuals now.
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Save default biomes.yml only if it doesn't exist. DO NOT OVERWRITE.
        if (!new File(getDataFolder(), "biomes.yml").exists()) {
            saveResource("biomes.yml", false);
            getLogger().info("Created default biomes.yml");
        }

        // Initialize ConfigPackRegistry directly from getDataFolder()
        this.configPackRegistry = new ConfigPackRegistry();
        this.configPackRegistry.loadAll(getDataFolder());
        
        // World Bootstrap
        WorldBootstrapService bootstrapService = new WorldBootstrapService(this);
        bootstrapService.syncBukkitDefaultWorldGenerator();

        // Load schematics from plugin folder (JSON, .schem, .litematic)
        File schemFolder = new File(getDataFolder(), "schematics");
        if (!schemFolder.exists()) schemFolder.mkdirs();
        
        // Extract default schematics if they don't exist
        String defaultSchem = "schematics/Tree3-from-abfielder.litematic";
        if (!new File(getDataFolder(), defaultSchem).exists()) {
            try {
                saveResource(defaultSchem, false);
            } catch (Exception e) {
                // Ignore if resource not found in some builds
            }
        }

        File[] schemFiles = schemFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lower = name.toLowerCase();
                return lower.endsWith(".json") || lower.endsWith(".schem")
                        || lower.endsWith(".schematic") || lower.endsWith(".litematic");
            }
        });
        if (schemFiles != null) {
            for (File f : schemFiles) {
                try {
                    Schematic s = Schematic.loadAuto(f);
                    String id = f.getName().replaceAll("\\.(json|schem|schematic|litematic)$", "");
                    CustomStructureRegistry.registerSchematic(id, s);
                    CustomTreeRegistry.registerSchematic(id, s);
                    getLogger().info("Registered schematic: " + f.getName() + " (" + s.getBlockCount() + " blocks)");
                } catch (Exception e) {
                    getLogger().warning("Failed to load schematic " + f.getName() + ": " + e.getMessage());
                }
            }
        }
        
        MobHandler mobHandler = new MobHandler();
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemCombatHandler(this), this);

        // Dragon Handlers
        this.dragonCombatHandler = new DragonCombatHandler(this);
        getServer().getPluginManager().registerEvents(dragonCombatHandler, this);
        getServer().getPluginManager().registerEvents(new DragonSpawnHandler(dragonCombatHandler), this);

        // Biome Display Handler (Action Bar)
        new com.Chagui68.listener.BiomeDisplayHandler(this);

        // Commands
        com.Chagui68.commands.MSCCommand mscCommand = new com.Chagui68.commands.MSCCommand(this, mobHandler,
                dragonCombatHandler);
        getCommand("msc").setExecutor(mscCommand);
        getCommand("msc").setTabCompleter(mscCommand);
    }

    @Override
    public void onDisable() {
        if (dragonCombatHandler != null) {
            dragonCombatHandler.cleanup();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        // Get ConfigPack (equivalent to how Terra provides ConfigPack to its generators)
        ConfigPack configPack = configPackRegistry.getDefaultPack();
        if (configPack == null) {
            getLogger().severe("No default ConfigPack available! Using fallback.");
            configPack = configPackRegistry.getByID("default").orElseThrow(() -> new RuntimeException("No default config pack available"));
        }
        
        MSCTerraGenerator gen = new MSCTerraGenerator(this, configPack);
        generators.add(gen);
        return gen;
    }

    public void reloadGenerators() {
        reloadConfig();
        saveResource("biomes.yml", true);

        // Reload config packs
        java.io.File packsFolder = new java.io.File(getDataFolder(), "packs");
        if (!packsFolder.exists()) packsFolder.mkdirs();
        this.configPackRegistry = new ConfigPackRegistry();
        this.configPackRegistry.loadAll(packsFolder);

        for (MSCTerraGenerator gen : generators) {
            getLogger().info("Reloading generator with updated configpacks...");
        }
        getLogger().info("[MSC] Reload complete. New worlds will use updated config.");
    }

    public ConfigPackRegistry getConfigPackRegistry() {
        return configPackRegistry;
    }
}
