package com.Chagui68.ritual;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BossDimensionSky {

    private static boolean applied = false;

    public static void apply(MultiverseCreatures plugin) {
        if (applied) return;
        try {
            Object craftServer = Bukkit.getServer();
            Method getServerMethod = craftServer.getClass().getMethod("getServer");
            Object minecraftServer = getServerMethod.invoke(craftServer);

            Method registryAccessMethod = minecraftServer.getClass().getMethod("registryAccess");
            Object registryAccess = registryAccessMethod.invoke(minecraftServer);

            Class<?> registriesClass = Class.forName("net.minecraft.core.registries.Registries");
            Field biomeRegistryKeyField = registriesClass.getDeclaredField("BIOME");
            biomeRegistryKeyField.setAccessible(true);
            Object biomeResourceKey = biomeRegistryKeyField.get(null);

            Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
            Method lookupOrThrowMethod = registryAccess.getClass()
                    .getMethod("lookupOrThrow", resourceKeyClass);
            Object biomeRegistry = lookupOrThrowMethod.invoke(registryAccess, biomeResourceKey);

            Class<?> biomesClass = Class.forName("net.minecraft.world.level.biome.Biomes");
            Field plainsField = biomesClass.getDeclaredField("PLAINS");
            plainsField.setAccessible(true);
            Object plainsKey = plainsField.get(null);

            Method getMethod = biomeRegistry.getClass().getMethod("get", resourceKeyClass);
            Object rawBiome = getMethod.invoke(biomeRegistry, plainsKey);

            if (rawBiome instanceof java.util.Optional<?> opt) {
                rawBiome = opt.orElseThrow(() -> new RuntimeException("PLAINS biome not found in registry"));
            }

            Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
            if (holderClass.isInstance(rawBiome)) {
                Method valueMethod = holderClass.getMethod("value");
                rawBiome = valueMethod.invoke(rawBiome);
            }

            Method getSpecialEffectsMethod = rawBiome.getClass().getMethod("getSpecialEffects");
            Object specialEffects = getSpecialEffectsMethod.invoke(rawBiome);

            setFinalIntField(specialEffects, "skyColor", 0xCC0000);
            setFinalIntField(specialEffects, "fogColor", 0x330000);

            applied = true;
            plugin.getLogger().info("Boss dimension sky set to red via biome override");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set boss dimension sky color via NMS: " + e.getMessage());
        }
    }

    private static void setFinalIntField(Object instance, String fieldName, int value) throws Exception {
        Class<?> clazz = instance.getClass();

        Field field = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                field = f;
                break;
            }
        }
        if (field == null) {
            for (Field f : clazz.getSuperclass().getDeclaredFields()) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
        }
        if (field == null) return;

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        field.setInt(instance, value);
    }
}
