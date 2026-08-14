package com.Chagui68.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;

import java.util.List;
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
}
