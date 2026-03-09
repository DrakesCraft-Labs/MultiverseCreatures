package com.Chagui68.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;

import org.bukkit.entity.LivingEntity;
import org.bukkit.Bukkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Iterator;
import java.util.Objects;

/**
 * Utility class to handle version-specific changes between Minecraft versions
 * (specifically 1.20.1 up to 1.20.6).
 */
public class VersionSafe {

    private static final Logger LOGGER = Logger.getLogger("MultiverseCreatures");

    public static void spawnExplosionParticle(Location loc, int count) {
        spawnParticleSafe(loc, count, "EXPLOSION_EMITTER", "EXPLOSION_HUGE", "EXPLOSION");
    }

    public static void spawnLargeExplosion(Location loc, int count) {
        spawnParticleSafe(loc, count, "LARGE_EXPLOSION", "EXPLOSION_LARGE", "EXPLOSION");
    }

    public static void spawnBreathParticle(Location loc, int count, double offsetX, double offsetY, double offsetZ,
            double speed) {
        Particle p = getParticleSafe("DRAGON_BREATH", "DRAGON_BREATH");
        if (p != null) {
            loc.getWorld().spawnParticle(p, loc, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    public static void spawnSmokeParticle(Location loc, int count, double offsetX, double offsetY, double offsetZ,
            double speed) {
        spawnParticleSafeWithOffset(loc, count, offsetX, offsetY, offsetZ, speed, "SMOKE", "SMOKE_NORMAL");
    }

    public static void spawnFireParticle(Location loc, int count, double ox, double oy, double oz, double speed) {
        spawnParticleSafeWithOffset(loc, count, ox, oy, oz, speed, "FLAME", "FLAME");
    }

    public static void spawnLavaParticle(Location loc, int count, double ox, double oy, double oz, double speed) {
        spawnParticleSafeWithOffset(loc, count, ox, oy, oz, speed, "LAVA", "LAVA");
    }

    public static void spawnMistParticle(Location loc, int count, double ox, double oy, double oz, double speed) {
        spawnParticleSafeWithOffset(loc, count, ox, oy, oz, speed, "SQUID_INK", "SQUID_INK");
    }

    public static void spawnPortalParticle(Location loc, int count, double ox, double oy, double oz, double speed) {
        spawnParticleSafeWithOffset(loc, count, ox, oy, oz, speed, "PORTAL", "PORTAL");
    }

    public static void spawnLargeSmokeParticle(Location loc, int count, double ox, double oy, double oz, double speed) {
        spawnParticleSafeWithOffset(loc, count, ox, oy, oz, speed, "LARGE_SMOKE", "SMOKE_LARGE");
    }

    private static void spawnParticleSafe(Location loc, int count, String... names) {
        Particle p = getParticleSafe(names);
        if (p != null && loc.getWorld() != null) {
            loc.getWorld().spawnParticle(p, loc, count);
        }
    }

    private static void spawnParticleSafeWithOffset(Location loc, int count, double ox, double oy, double oz,
            double speed, String... names) {
        Particle p = getParticleSafe(names);
        if (p != null && loc.getWorld() != null) {
            loc.getWorld().spawnParticle(p, loc, count, ox, oy, oz, speed);
        }
    }

    private static Particle getParticleSafe(String... names) {
        for (String name : names) {
            try {
                return Particle.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Safely gets a PotionEffectType by name to ensure compatibility across
     * versions.
     */
    public static PotionEffectType getPotionEffectSafe(String name) {
        try {
            // First try direct modern access if possible, then fallback to getByName
            return PotionEffectType.getByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely plays a sound to ensure compatibility across versions.
     */
    public static void playSoundSafe(Location loc, float volume, float pitch, String... names) {
        if (loc == null || loc.getWorld() == null)
            return;
        for (String name : names) {
            try {
                Sound sound = Sound.valueOf(name);
                loc.getWorld().playSound(loc, sound, volume, pitch);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Safely gets an attribute for both 1.20.1 and 1.20.6+.
     */
    public static Attribute getAttributeSafe(String... names) {
        for (String name : names) {
            try {
                return Attribute.valueOf(name);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Safely sets the base value of an attribute for an entity.
     */
    public static void setAttributeBaseSafe(LivingEntity entity, double value, String... names) {
        if (entity == null)
            return;
        Attribute attr = getAttributeSafe(names);
        if (attr != null && entity.getAttribute(attr) != null) {
            entity.getAttribute(attr).setBaseValue(value);
        }
    }

    /**
     * Overload for standard Bukkit Sound enum.
     */
    public static void playSoundSafe(Location loc, Sound sound, float volume, float pitch) {
        if (loc == null || loc.getWorld() == null || sound == null)
            return;
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    /**
     * Safely sends an action bar message to a player.
     */
    public static void sendActionBarSafe(Player player, String message) {
        if (player == null || message == null)
            return;

        try {
            // Try Paper/Newer Bukkit method via reflection to avoid compile-time failure on
            // Spigot
            player.getClass().getMethod("sendActionBar", String.class).invoke(player, message);
        } catch (Exception e) {
            // Fallback to Spigot's sub-API
            try {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(message));
            } catch (NoClassDefFoundError | NoSuchMethodError | Exception e2) {
                // If all fails, just use sendMessage (standard chat)
                player.sendMessage(message);
            }
        }
    }

    /**
     * Safely apply a potion effect to a player.
     * Prevents NullPointerException during player ticking on some hybrid servers.
     */
    public static void applyPotionEffectSafe(Player player, PotionEffectType type, int duration, int amplifier) {
        if (player == null || type == null)
            return;

        try {
            // First, sanitize the player's effect state if they are already in a corrupted
            // state
            sanitizePlayerEffects(player);

            // Check if player reportedly has the effect
            if (player.hasPotionEffect(type)) {
                org.bukkit.potion.PotionEffect active = player.getPotionEffect(type);
                if (active != null) {
                    // Optimization: Don't re-apply if the existing effect is stronger or redundant
                    if (active.getAmplifier() > amplifier)
                        return;
                    if (active.getAmplifier() == amplifier && active.getDuration() > duration - 40)
                        return;
                } else {
                    // Corrupted state: hasPotionEffect is true but getPotionEffect is null.
                    // This is likely what leads to the "Ticking player" crash.
                    Bukkit.getLogger().warning("[MSC] Detected corrupted effect state for " + type.getName() + " on "
                            + player.getName() + "... Force clearing.");

                    // Force removal of the corrupted effect
                    try {
                        player.removePotionEffect(type);
                    } catch (Exception ignored) {
                    }
                }
            }

            // Sanitization: Ensure values are non-negative and valid for modern versions
            int finalDuration = Math.max(1, duration);
            int finalAmplifier = Math.max(0, amplifier);

            player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, finalDuration, finalAmplifier));
        } catch (Exception e) {
            // General safety net to prevent server crash during player ticking
            Bukkit.getLogger().log(Level.WARNING,
                    "[MSC] CRITICAL: Failed to apply " + type.getName() + " to " + player.getName(), e);
        }
    }

    /**
     * Attempts to sanitize a player's potion effects by removing null entries
     * from the internal Minecraft effect map using reflection.
     * This is an aggressive fix for the "Ticking player" NullPointerException.
     */
    public static void sanitizePlayerEffects(Player player) {
        if (player == null)
            return;

        try {
            // 1. Try to detect null effects via Bukkit first
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                if (effect == null || effect.getType() == null) {
                    Bukkit.getLogger().warning(
                            "[MSC] Null effect found on " + player.getName() + ". Attempting aggressive cleanup...");
                    forceCleanupInternalMap(player);
                    return;
                }
            }
        } catch (Exception e) {
            // High-level check failed, maybe the map is already so broken it throws on
            // iteration
            forceCleanupInternalMap(player);
        }
    }

    private static void forceCleanupInternalMap(Player player) {
        try {
            // Get the NMS handle (CraftPlayer -> EntityPlayer)
            Method getHandle = player.getClass().getMethod("getHandle");
            Object nmsPlayer = getHandle.invoke(player);

            // Search for the activeEffects map in LivingEntity (nmsPlayer's superclass)
            Class<?> livingEntityClass = nmsPlayer.getClass();
            while (livingEntityClass != null && !livingEntityClass.getSimpleName().equals("LivingEntity")
                    && !livingEntityClass.getSimpleName().equals("EntityLiving")) {
                livingEntityClass = livingEntityClass.getSuperclass();
            }

            if (livingEntityClass == null)
                return;

            // In 1.20.1, the map is often called 'activeEffects' or 'f' (obfuscated)
            // We'll search for any field of type Map
            for (java.lang.reflect.Field field : livingEntityClass.getDeclaredFields()) {
                if (java.util.Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    java.util.Map<?, ?> map = (java.util.Map<?, ?>) field.get(nmsPlayer);
                    if (map != null) {
                        // Remove any null values from the map
                        try {
                            map.values().removeIf(java.util.Objects::isNull);
                            map.keySet().removeIf(java.util.Objects::isNull);
                        } catch (Exception e) {
                            // If removeIf fails (Immutable map or ConcurrentModification),
                            // we might need to create a new map or use an iterator
                            java.util.Iterator<? extends java.util.Map.Entry<?, ?>> it = map.entrySet().iterator();
                            while (it.hasNext()) {
                                java.util.Map.Entry<?, ?> entry = it.next();
                                if (entry.getKey() == null || entry.getValue() == null) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.FINE, "[MSC] Reflection cleanup failed for " + player.getName(), e);
        }
    }
}
