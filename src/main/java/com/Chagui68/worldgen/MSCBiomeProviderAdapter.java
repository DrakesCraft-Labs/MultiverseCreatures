package com.Chagui68.worldgen;

import com.Chagui68.biome.BiomeConfig;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Adapter converting our BiomeProvider to Bukkit's BiomeProvider
 * (equivalent to how Terra exposes its BiomeProvider system)
 */
public class MSCBiomeProviderAdapter extends BiomeProvider {
    
    private final com.Chagui68.biome.BiomeProvider biomeProvider;
    
    public MSCBiomeProviderAdapter(com.Chagui68.biome.BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }
    
    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        BiomeConfig biomeConfig = biomeProvider.getBiome(x, y, z, worldInfo.getSeed());
        return biomeConfig.getVanillaBiome();
    }
    
    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        // Return all vanilla biomes available from our registry
        List<Biome> biomes = new java.util.ArrayList<>();
        for (com.Chagui68.biome.BiomeConfig config : getNativeProvider().getBiomes()) {
            biomes.add(config.getVanillaBiome());
        }
        return biomes;
    }
    
    private com.Chagui68.biome.BiomeProvider getNativeProvider() {
        return biomeProvider;
    }
}
