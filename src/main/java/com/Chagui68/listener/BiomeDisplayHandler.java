package com.Chagui68.listener;

import com.Chagui68.MultiverseCreatures;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the display of biome names in the Action Bar for biomes.
 */
public class BiomeDisplayHandler extends BukkitRunnable {

    private final MultiverseCreatures plugin;
    private final Map<String, String> displayNames = new HashMap<>();

    public BiomeDisplayHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
        loadDisplayNames();
        this.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    private void loadDisplayNames() {
        File configFile = new File(plugin.getDataFolder(), "biomes.yml");
        if (!configFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        
        if (biomesSection != null) {
            for (String key : biomesSection.getKeys(false)) {
                String displayName = biomesSection.getString(key + ".display_name");
                if (displayName != null) {
                    displayNames.put(key, displayName.replace("&", "§"));
                }
            }
        }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            org.bukkit.generator.ChunkGenerator gen = player.getWorld().getGenerator();
            if (gen instanceof com.Chagui68.worldgen.MSCTerraGenerator mscGen) {
                com.Chagui68.worldgen.MSCBiomeProvider provider = (com.Chagui68.worldgen.MSCBiomeProvider) mscGen.getDefaultBiomeProvider(player.getWorld());
                if (provider != null) {
                    int x = player.getLocation().getBlockX();
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();
                    String thematicId = provider.getThematicId(x, y, z);
                    
                    if (displayNames.containsKey(thematicId)) {
                        String name = displayNames.get(thematicId);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7Currently in: " + name));
                    }
                }
            }
        }
    }
}
