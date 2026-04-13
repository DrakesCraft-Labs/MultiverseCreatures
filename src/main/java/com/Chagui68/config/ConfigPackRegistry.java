package com.Chagui68.config;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Registry de ConfigPacks (equivalente a: com.dfsek.terra.registry.master.ConfigRegistry)
 * Administra múltiples ConfigPacks
 */
public class ConfigPackRegistry {
    private final Map<String, ConfigPack> packs = new HashMap<>();
    private ConfigPack defaultPack;
    
    /**
     * Cargar configuración desde la carpeta principal del plugin
     */
    public void loadAll(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        try {
            ConfigPack pack = new ConfigPack("default", dataFolder);
            packs.put("default", pack);
            defaultPack = pack;
            Bukkit.getLogger().info("[MSC] Loaded default config pack directly from " + dataFolder.getName());
        } catch (Exception e) {
            Bukkit.getLogger().severe("[MSC] Failed to load config pack: " + e.getMessage());
        }
        
        Bukkit.getLogger().info("[MSC] Loaded " + packs.size() + " config pack(s)");
    }

    
    /**
     * Obtener un pack por ID
     * (equivalente a ConfigRegistry.getByID() de Terra)
     */
    public Optional<ConfigPack> getByID(String id) {
        return Optional.ofNullable(packs.get(id));
    }
    
    /**
     * Obtener un pack por ID, lanza excepción si no existe
     */
    public ConfigPack getByIDOrDefault(String id) {
        return packs.getOrDefault(id, defaultPack);
    }
    
    /**
     * Obtener el pack por defecto
     */
    public ConfigPack getDefaultPack() {
        if (defaultPack == null) {
            throw new RuntimeException("No default config pack found!");
        }
        return defaultPack;
    }
    
    /**
     * Establecer el pack por defecto
     */
    public void setDefaultPack(String id) {
        ConfigPack pack = packs.get(id);
        if (pack == null) {
            throw new IllegalArgumentException("Pack not found: " + id);
        }
        this.defaultPack = pack;
    }
    
    /**
     * Obtener todos los packs
     */
    public Collection<ConfigPack> getAll() {
        return packs.values();
    }
    
    /**
     * Obtener todos los IDs de packs
     */
    public Set<String> getIds() {
        return packs.keySet();
    }
    
    /**
     * Verificar si existe un pack
     */
    public boolean contains(String id) {
        return packs.containsKey(id);
    }
    
    /**
     * Número de packs cargados
     */
    public int size() {
        return packs.size();
    }
    
    /**
     * Iterar sobre todos los packs
     */
    public void forEach(java.util.function.BiConsumer<String, ConfigPack> consumer) {
        packs.forEach(consumer);
    }
}
