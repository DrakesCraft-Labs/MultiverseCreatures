package com.Chagui68.biome;

import java.util.*;

/**
 * Registry de biomas (equivalente a: com.dfsek.terra.api.registry.Registry)
 * Almacena y permite recuperar biomas por ID
 */
public class BiomeRegistry {
    private final Map<String, BiomeConfig> biomesByID = new HashMap<>();
    
    /**
     * Registrar un bioma (equivalente a registry.register())
     */
    public void register(String id, BiomeConfig biome) {
        if (biomesByID.containsKey(id)) {
            throw new IllegalArgumentException("Biome with ID '" + id + "' is already registered");
        }
        biomesByID.put(id, biome);
    }
    
    /**
     * Obtener bioma por ID exacto (equivalente a registry.getByID())
     */
    public Optional<BiomeConfig> getByID(String id) {
        return Optional.ofNullable(biomesByID.get(id));
    }
    
    /**
     * Obtener bioma sin Optional (lanza excepción si no existe)
     */
    public BiomeConfig getByIDOrThrow(String id) {
        BiomeConfig biome = biomesByID.get(id);
        if (biome == null) {
            throw new IllegalArgumentException("Biome not found: " + id);
        }
        return biome;
    }
    
    /**
     * Verificar si existe un bioma
     */
    public boolean contains(String id) {
        return biomesByID.containsKey(id);
    }
    
    /**
     * Obtener todos los biomas
     */
    public Collection<BiomeConfig> getAll() {
        return biomesByID.values();
    }
    
    /**
     * Obtener todos los IDs
     */
    public Set<String> getIds() {
        return biomesByID.keySet();
    }
    
    /**
     * Buscar biomas por parte del ID (equivalente a registry.getMatches())
     */
    public Map<String, BiomeConfig> findMatches(String partialId) {
        Map<String, BiomeConfig> matches = new HashMap<>();
        for (var entry : biomesByID.entrySet()) {
            if (entry.getKey().contains(partialId)) {
                matches.put(entry.getKey(), entry.getValue());
            }
        }
        return matches;
    }
    
    /**
     * Obtener número de biomas registrados
     */
    public int size() {
        return biomesByID.size();
    }
    
    /**
     * Iterar sobre biomas
     */
    public void forEach(java.util.function.BiConsumer<String, BiomeConfig> consumer) {
        biomesByID.forEach(consumer);
    }
    
    /**
     * Limpiar el registry
     */
    public void clear() {
        biomesByID.clear();
    }
}
