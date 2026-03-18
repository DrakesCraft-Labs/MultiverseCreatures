package com.Chagui68.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getWorlds;

public class DatapackManager {

    /**
     * Extracts the internal biome datapack to the primary world's datapacks folder.
     */
    public static void installDatapack(JavaPlugin plugin) {
        plugin.getLogger().info("[MSC] Starting biome datapack installation check...");
        
        File worldFolder = null;
        
        // Method 1: Get from loaded worlds
        if (!getWorlds().isEmpty()) {
            worldFolder = getWorlds().get(0).getWorldFolder();
        } 
        
        // Method 2: Fallback to common names
        if (worldFolder == null) {
            File worldContainer = Bukkit.getWorldContainer();
            String[] commonNames = {"world", "world_nether", "world_the_end"};
            for (String name : commonNames) {
                File folder = new File(worldContainer, name);
                if (folder.exists() && new File(folder, "level.dat").exists()) {
                    worldFolder = folder;
                    break;
                }
            }
        }

        if (worldFolder == null) {
            plugin.getLogger().warning("[MSC] Could not detect main world folder automatically!");
            return;
        }

        // List of files to extract from the JAR
        String[] resources = {
            "datapacks/msc_biomes/pack.mcmeta",
            "datapacks/msc_biomes/data/msc/worldgen/biome/abyssal_plains.json",
            "datapacks/msc_biomes/data/minecraft/worldgen/multi_noise_biome_source_parameter_list/overworld.json",
            "datapacks/msc_biomes/data/msc/worldgen/configured_feature/abyssal_floor.json",
            "datapacks/msc_biomes/data/msc/worldgen/placed_feature/abyssal_floor.json",
            "datapacks/msc_biomes/data/msc/worldgen/configured_feature/abyssal_patches.json",
            "datapacks/msc_biomes/data/msc/worldgen/placed_feature/abyssal_patches.json"
        };

        boolean installedAny = false;
        for (String resourcePath : resources) {
            File targetFile = new File(worldFolder, resourcePath);
            
            // Always extract to ensure the latest version from the plugin JAR is used
            plugin.getLogger().info("[MSC] Extracting " + resourcePath + "...");
            if (extractResource(plugin, resourcePath, targetFile)) {
                installedAny = true;
            }
        }

        if (installedAny) {
            plugin.getLogger().info("[MSC] New biome datapack files installed in: " + worldFolder.getName());
            plugin.getLogger().info("[MSC] RESTART THE SERVER to apply changes!");
        }
    }

    private static boolean extractResource(JavaPlugin plugin, String resourcePath, File targetFile) {
        try {
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) return false;

            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            try (OutputStream out = new FileOutputStream(targetFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            in.close();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[MSC] Failed to extract " + resourcePath, e);
            return false;
        }
    }
}
