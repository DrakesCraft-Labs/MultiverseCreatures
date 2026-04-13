package com.Chagui68.worldgen;

import com.Chagui68.biome.BiomeConfig;
import com.Chagui68.biome.BiomeProvider;
import com.Chagui68.MultiverseCreatures;

/**
 * Backward compatibility adapter for MSCBiomeProvider
 * Allows existing code (like MSCTreePopulator) to continue working
 * while using our new BiomeProvider system
 */
public class MSCBiomeProviderCompat extends MSCBiomeProvider {
    
    private final BiomeProvider biomeProvider;
    
    public MSCBiomeProviderCompat(BiomeProvider biomeProvider, MultiverseCreatures plugin, NoiseSampler sampler) {
        super(plugin, sampler);
        this.biomeProvider = biomeProvider;
    }
    
    @Override
    public String getThematicId(int x, int y, int z) {
        // We use a dummy seed that is consistent for the provider
        // but it's better to use the one from the caller if possible.
        // In the adapter it is overridden anyway.
        BiomeConfig biomeConfig = biomeProvider.getBiome(x, y, z, 0); 
        return biomeConfig.getId();
    }
    
    public String getThematicId(int x, int z) {
        BiomeConfig biomeConfig = biomeProvider.getBiome(x, z, 0);
        return biomeConfig.getId();
    }
}
