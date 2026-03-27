package com.Chagui68.utils;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class WorldBootstrapService {

    private final MultiverseCreatures plugin;

    public WorldBootstrapService(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    /**
     * Synchronizes the MultiverseCreatures generator in the bukkit.yml file
     * for the main world defined in server.properties.
     */
    public void syncBukkitDefaultWorldGenerator() {
        File serverProperties = new File(plugin.getServer().getWorldContainer(), "server.properties");
        String defaultWorldName = "world";

        if (serverProperties.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(serverProperties)) {
                props.load(in);
                defaultWorldName = props.getProperty("level-name", "world");
            } catch (IOException ex) {
                plugin.getLogger().warning("[MSC] Could not read server.properties: " + ex.getMessage());
            }
        }

        File bukkitYml = new File(plugin.getServer().getWorldContainer(), "bukkit.yml");
        if (!bukkitYml.exists()) {
            plugin.getLogger().warning("[MSC] bukkit.yml not found. Could not synchronize the generator.");
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(bukkitYml);
        String expected = plugin.getName(); // "MultiverseCreatures"
        String path = "worlds." + defaultWorldName + ".generator";
        String current = yaml.getString(path, "");

        if (expected.equalsIgnoreCase(current)) {
            return;
        }

        yaml.set(path, expected);
        try {
            yaml.save(bukkitYml);
            plugin.getLogger().info("[MSC] World '" + defaultWorldName + "' has been linked to MultiverseCreatures.");
            plugin.getLogger().info("[MSC] This will take effect on next boot.");
        } catch (IOException ex) {
            plugin.getLogger().warning("[MSC] Could not write to bukkit.yml: " + ex.getMessage());
        }
    }
}
