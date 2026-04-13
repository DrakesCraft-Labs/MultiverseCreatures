package com.Chagui68.biome;

import com.Chagui68.worldgen.NoiseSampler;
import com.Chagui68.worldgen.SlantCalculator;

/**
 * Implementación de BiomeProvider usando ruido 3D
 * (equivalente a cómo Terra distribuye biomas con ruido)
 */
public class NoiseBiomeProvider implements BiomeProvider {
    
    private final NoiseSampler sampler;
    private final SlantCalculator slantCalculator;
    private final BiomeRegistry registry;
    
    public NoiseBiomeProvider(NoiseSampler sampler, BiomeRegistry registry) {
        this.sampler = sampler;
        this.slantCalculator = new SlantCalculator(sampler);
        this.registry = registry;
    }
    
    /**
     * Determinar el bioma en (x, y, z) basado en ruido y pendiente
     * (equivalente a BiomeProvider.getBiome() de Terra)
     */
    @Override
    public BiomeConfig getBiome(int x, int y, int z, long seed) {
        // Samplear ruido 3D
        double noise = sampler.sample(x * 0.02f, y * 0.02f, z * 0.02f);
        
        // Calcular pendiente/slant
        double slant = slantCalculator.getSlant(x, y, z);
        
        // Lógica de distribución basada en ruido y pendiente
        // (similar a la que estaba en MSCBiomeProvider pero modular)
        
        // Si la pendiente es muy pronunciada → Cliffs
        // Force generation of ONLY the valley (meadow) biome as requested for testing
        return registry.getByID("valley").orElse(getDefaultBiome());
    }
    
    @Override
    public Iterable<BiomeConfig> getBiomes() {
        return registry.getAll();
    }
    
    /**
     * Bioma por defecto en caso de que algo falle
     */
    private BiomeConfig getDefaultBiome() {
        return registry.getByID("valley")
            .orElseThrow(() -> new RuntimeException("No default biome found!"));
    }
}
