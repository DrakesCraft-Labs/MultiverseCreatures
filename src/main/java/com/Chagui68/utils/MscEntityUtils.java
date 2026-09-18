package com.Chagui68.utils;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Reusable helpers for MSC custom mobs. Eliminates the per-class duplication of:
 * setAttribute / spawnEntity cast + tag + customName + persistent + FIRE_RESISTANCE
 * / clearDrops + dropChance + setDroppedExp / target-guard (creative/spectator/isDead).
 */
public final class MscEntityUtils {

    public static final int PERMANENT_DURATION = 999999;

    private MscEntityUtils() {
    }

    public static void setAttribute(Entity entity, Attribute attribute, double value) {
        if (entity instanceof LivingEntity le) {
            AttributeInstance attr = le.getAttribute(attribute);
            if (attr != null) attr.setBaseValue(value);
        }
    }

    public static final org.bukkit.NamespacedKey KEY_VIRTUAL_HEALTH = new org.bukkit.NamespacedKey("multiversecreatures", "virtual_health");
    public static final org.bukkit.NamespacedKey KEY_VIRTUAL_MAX_HEALTH = new org.bukkit.NamespacedKey("multiversecreatures", "virtual_max_health");

    public static double calculateSafeHealth(double targetHealth, double effectiveMax) {
        return Math.max(0.1, Math.min(targetHealth, effectiveMax));
    }

    public static double calculateVirtualProgress(double currentHealth, double maxHealth) {
        if (maxHealth <= 0.0) return 0.0;
        return Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));
    }

    public static double calculateScaledPhysicalHealth(double currentHealth, double maxHealth, double physicalMax) {
        if (currentHealth <= 0.0 || maxHealth <= 0.0 || physicalMax <= 0.0) return 0.0;
        double ratio = currentHealth / maxHealth;
        return Math.max(0.1, Math.min(physicalMax, ratio * physicalMax));
    }

    /**
     * Initializes the entity with virtual health tracking in its PersistentDataContainer,
     * while safely capping physical entity health within the server's attribute threshold.
     */
    public static void initVirtualHealth(LivingEntity entity, double maxHealth) {
        if (entity == null) return;
        setMaxHealthAndHeal(entity, maxHealth);
        try {
            entity.getPersistentDataContainer().set(KEY_VIRTUAL_MAX_HEALTH, PersistentDataType.DOUBLE, maxHealth);
            entity.getPersistentDataContainer().set(KEY_VIRTUAL_HEALTH, PersistentDataType.DOUBLE, maxHealth);
        } catch (Exception ignored) {
        }
    }

    public static double getVirtualMaxHealth(LivingEntity entity) {
        if (entity == null) return 20.0;
        try {
            Double val = entity.getPersistentDataContainer().get(KEY_VIRTUAL_MAX_HEALTH, PersistentDataType.DOUBLE);
            if (val != null && val > 0) return val;
        } catch (Exception ignored) {
        }
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        return attr != null ? attr.getValue() : 20.0;
    }

    public static double getVirtualHealth(LivingEntity entity) {
        if (entity == null) return 0.0;
        try {
            Double val = entity.getPersistentDataContainer().get(KEY_VIRTUAL_HEALTH, PersistentDataType.DOUBLE);
            if (val != null) return Math.max(0.0, val);
        } catch (Exception ignored) {
        }
        return entity.getHealth();
    }

    public static void setVirtualHealth(LivingEntity entity, double health) {
        if (entity == null) return;
        double max = getVirtualMaxHealth(entity);
        double clamped = Math.max(0.0, Math.min(health, max));
        try {
            entity.getPersistentDataContainer().set(KEY_VIRTUAL_HEALTH, PersistentDataType.DOUBLE, clamped);
        } catch (Exception ignored) {
        }

        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            double physicalMax = attr.getValue();
            if (clamped <= 0.0) {
                entity.setHealth(0.0);
            } else {
                double scaled = calculateScaledPhysicalHealth(clamped, max, physicalMax);
                entity.setHealth(scaled);
            }
        } else {
            if (clamped <= 0.0) {
                entity.setHealth(0.0);
            }
        }
    }

    /**
     * Sets the entity's max health attribute and sets current health to the target value,
     * safely bounded by the server's attribute maximum (e.g. 1024.0 or spigot.yml maxHealth.max)
     * to prevent IllegalArgumentException.
     */
    public static void setMaxHealthAndHeal(LivingEntity entity, double targetHealth) {
        if (entity == null) return;
        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            try {
                maxHealthAttr.setBaseValue(targetHealth);
            } catch (Exception ignored) {
            }
            entity.setHealth(calculateSafeHealth(targetHealth, maxHealthAttr.getValue()));
        } else {
            entity.setHealth(calculateSafeHealth(targetHealth, 20.0));
        }
    }

    /**
     * Spawns a tagged, persistent, non-despawning MSC entity. Returns null if the spawn failed.
     * Caller is expected to know the concrete EntityType / cast.
     */
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> T spawnTagged(
            Location location,
            EntityType type,
            String tag,
            String customName,
            Consumer<T> configure) {
        Entity raw = location.getWorld().spawnEntity(location, type);
        if (raw == null || !(raw instanceof LivingEntity)) return null;
        T entity = (T) raw;
        entity.addScoreboardTag(tag);
        if (customName != null) {
            entity.setCustomName(customName);
            entity.setCustomNameVisible(true);
        }
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.setAI(true);
        if (configure != null) configure.accept(entity);
        return entity;
    }

    public static void permanentFireResistance(LivingEntity entity) {
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PERMANENT_DURATION, 0, false, false));
    }

    public static boolean isCreativeOrSpectator(Player player) {
        GameMode gm = player.getGameMode();
        return gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR;
    }

    /**
     * True if the player is a valid MSC target (alive, online, not in creative/spectator).
     */
    public static boolean isValidTarget(Player player) {
        return player != null && !player.isDead() && player.isOnline() && !isCreativeOrSpectator(player);
    }

    /**
     * Clears natural drops, rolls a chance for each extra drop, sets fixed experience.
     * Mirrors the onDeath boilerplate duplicated across all MSC mob classes.
     */
    public static void handleDeath(EntityDeathEvent event, String expectedTag,
                                   double dropChance, int droppedExp, ItemStack... extraDrops) {
        LivingEntity entity = event.getEntity();
        if (expectedTag != null && !entity.getScoreboardTags().contains(expectedTag)) return;
        event.getDrops().clear();
        if (extraDrops != null && extraDrops.length > 0 && Math.random() < dropChance) {
            for (ItemStack drop : extraDrops) {
                if (drop != null) entity.getWorld().dropItemNaturally(entity.getLocation(), drop.clone());
            }
        }
        event.setDroppedExp(droppedExp);
    }

    /**
     * Overrides a player's death message with a random one from the config list
     * when the killer is a tagged MSC mob. Returns true if the message was applied.
     */
    public static boolean applyDeathMessage(MultiverseCreatures plugin, PlayerDeathEvent event, String tag, String configKey) {
        Entity killer = event.getDamageSource().getCausingEntity();
        if (killer == null || !killer.getScoreboardTags().contains(tag)) return false;
        List<String> messages = plugin.getConfig().getStringList(configKey);
        if (messages.isEmpty()) return false;
        String raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        return true;
    }

    /**
     * Damages the victim with the attacker as causing entity (GENERIC source: no
     * difficulty scaling, no knockback, armor applies exactly like plain damage()).
     * Lets PlayerDeathEvent attribute the kill to the attacking mob.
     */
    public static void damageBy(LivingEntity attacker, LivingEntity victim, double amount) {
        victim.damage(amount, DamageSource.builder(DamageType.GENERIC)
                .withDirectEntity(attacker)
                .withCausingEntity(attacker)
                .build());
    }

    /**
     * Applies appropriate ambient persistence to an entity.
     *
     * Ambient = mobs converted from natural spawns. By default they behave like vanilla
     * mobs and despawn when players move far away. Bosses and manual summons do not use this.
     */
    public static void applyAmbientPersistence(MultiverseCreatures plugin, LivingEntity entity) {
        if (entity == null) return;
        boolean persistent = plugin.getConfig()
                .getBoolean("general.natural-spawn-persistent", false);
        entity.setPersistent(persistent);
        entity.setRemoveWhenFarAway(!persistent);
    }

    /** Counts living MSC plugin creatures in a world (tag `MSC_*`). */
    public static int countAlive(World world) {
        if (world == null) return 0;
        int n = 0;
        try {
            for (Entity e : world.getEntities()) {
                if (e == null || !e.isValid()) continue;
                try {
                    java.util.Set<String> tags = e.getScoreboardTags();
                    if (tags == null) continue;
                    for (String tag : tags) {
                        if (tag != null && tag.startsWith("MSC_")) {
                            n++;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return n;
    }

}
