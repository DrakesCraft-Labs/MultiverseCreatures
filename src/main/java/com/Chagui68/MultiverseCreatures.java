package com.Chagui68;

import com.Chagui68.entities.MobHandler;
import com.Chagui68.entities.dragon.DragonCombatHandler;
import com.Chagui68.entities.dragon.DragonSpawnHandler;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemCombatHandler;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class MultiverseCreatures extends JavaPlugin {
    private final List<com.Chagui68.worldgen.MSCTerraGenerator> generators = new ArrayList<>();

    @Override
    public void onLoad() {
        // No longer using custom biomes for stability, strictly thematic visuals now.
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("biomes.yml", true);

        // World Bootstrap
        com.Chagui68.utils.WorldBootstrapService bootstrapService = new com.Chagui68.utils.WorldBootstrapService(this);
        bootstrapService.syncBukkitDefaultWorldGenerator();
        
        MobHandler mobHandler = new MobHandler(this);
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemCombatHandler(this), this);

        // Dragon Handlers
        DragonCombatHandler dragonCombatHandler = new DragonCombatHandler(this);
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
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        com.Chagui68.worldgen.MSCTerraGenerator gen = new com.Chagui68.worldgen.MSCTerraGenerator(this);
        generators.add(gen);
        return gen;
    }

    public void reloadGenerators() {
        for (com.Chagui68.worldgen.MSCTerraGenerator gen : generators) {
            gen.reload();
        }
    }
}
