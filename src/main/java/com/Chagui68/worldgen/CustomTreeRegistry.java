package com.Chagui68.worldgen;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CustomTreeRegistry {
    public interface CustomTreeGenerator {
        boolean generate(LimitedRegion region, int x, int y, int z, Random random);
    }

    private static final Map<String, CustomTreeGenerator> registry = new HashMap<>();

    /**
     * Register a schematic as a custom tree.
     */
    public static void registerSchematic(String id, Schematic schematic) {
        if (id == null || schematic == null) return;
        register(id, (region, x, y, z, random) -> {
            try {
                // For trees, we usually want to skip the ground blocks captured in the schematic
                // and place it starting from y (the surface).
                return schematic.pasteIntoRegion(region, x, y, z, true);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static void register(String id, CustomTreeGenerator generator) {
        if (id == null || generator == null) return;
        registry.put(id.toUpperCase(Locale.ROOT), generator);
    }

    public static boolean generate(String id, LimitedRegion region, int x, int y, int z, Random random) {
        if (id == null) return false;
        CustomTreeGenerator gen = registry.get(id.toUpperCase(Locale.ROOT));
        if (gen == null) return false;
        try {
            return gen.generate(region, x, y, z, random);
        } catch (Exception e) {
            return false;
        }
    }
}
