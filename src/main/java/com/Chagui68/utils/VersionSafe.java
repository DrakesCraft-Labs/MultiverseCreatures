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
                    // This is likely what leads to the "Ticking player" crash in certain
                    // environments.
                    // Clear it manually before re-adding.
                    Bukkit.getLogger().warning("[MSC] Detected corrupted effect state for " + type.getName() + " on "
                            + player.getName() + "... Clearing.");
                    player.removePotionEffect(type);
                }
            }

            // Sanitization: Ensure values are non-negative and valid for modern versions
            if (duration < 1)
                duration = 1;
            if (amplifier < 0)
                amplifier = 0;

            player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
        } catch (Exception e) {
            // General safety net to prevent server crash during player ticking
            Bukkit.getLogger().warning("[MSC] CRITICAL: Failed to apply " + type.getName() + " to " + player.getName());
            e.printStackTrace();
        }
    }
}
