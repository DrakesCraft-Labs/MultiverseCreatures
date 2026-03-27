package com.Chagui68.worldgen;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides biomes for the MSC world generator.
 * Maps 3D noise and verticality to specific biome IDs.
 */
public class MSCBiomeProvider extends BiomeProvider {
    private final NoiseSampler sampler;
    private final SlantCalculator slantCalculator;
    
    private final MultiverseCreatures plugin;
    private final Biome abyssalPlains = Biome.DEEP_LUKEWARM_OCEAN;
    private final Biome cliffs = Biome.JAGGED_PEAKS;
    private final Biome valley = Biome.MEADOW;

    public MSCBiomeProvider(MultiverseCreatures plugin, NoiseSampler sampler) {
        this.plugin = plugin;
        this.sampler = sampler;
        this.slantCalculator = new SlantCalculator(sampler);
    }

    @NotNull
    public String getThematicId(int x, int y, int z) {
        if (sampler == null) return "valley";
        
        double noise = sampler.sample(x * 0.02, 62 * 0.02, z * 0.02);
        double slant = slantCalculator != null ? slantCalculator.getSlant(x, y, z) : 0;

        // Removed hardcoded abyssal_plains check as it should be deactivated or config-driven
        
        if (slant > 0.8) return "cliffs";
        
        return "valley";
    }

    @NotNull
    public String getThematicName(int x, int y, int z) {
        String id = getThematicId(x, y, z);
        switch (id) {
            case "abyssal_plains": return "§1§lABYSSAL PLAINS";
            case "cliffs": return "§8§lCLIFFS";
            case "valley": return "§2§lVALLEY";
            default: return "§2§lVALLEY";
        }
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        String id = getThematicId(x, y, z);
        switch (id) {
            case "abyssal_plains": return abyssalPlains;
            case "cliffs": return cliffs;
            case "valley": return valley;
            default: return valley;
        }
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(Biome.PLAINS, cliffs, valley);
    }
}
