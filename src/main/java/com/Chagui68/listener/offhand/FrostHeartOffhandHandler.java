package com.Chagui68.listener.offhand;

import com.Chagui68.items.misc.offhand.FrostHeartOffhand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class FrostHeartOffhandHandler implements Listener {

    private static final int AURA_INTERVAL_TICKS = 10;
    private static final int FROST_WALKER_RADIUS = 1;

    private final org.bukkit.plugin.Plugin plugin;

    public FrostHeartOffhandHandler(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
        startAuraTask();
    }

    private void startAuraTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isFrostHeart(p.getInventory().getItemInOffHand())) continue;
                    chillNearby(p);
                    freezeUnderFeet(p);
                }
            }
        }.runTaskTimer(plugin, 0L, AURA_INTERVAL_TICKS);
    }

    private boolean isFrostHeart(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(FrostHeartOffhand.FROST_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        ItemStack off = p.getInventory().getItem(EquipmentSlot.OFF_HAND);
        if (!isFrostHeart(off)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, FrostHeartOffhand.CHILL_TICKS, FrostHeartOffhand.CHILL_AMPLIFIER_SLOW, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, FrostHeartOffhand.CHILL_TICKS, FrostHeartOffhand.CHILL_AMPLIFIER_WEAK, false, false));
        attacker.getWorld().spawnParticle(Particle.SNOWFLAKE, attacker.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 0.8f, 1.2f);
    }

    private void chillNearby(Player p) {
        Location center = p.getLocation();
        for (Entity e : p.getWorld().getNearbyEntities(center, FrostHeartOffhand.FROST_RADIUS, FrostHeartOffhand.FROST_RADIUS, FrostHeartOffhand.FROST_RADIUS)) {
            if (e.equals(p)) continue;
            if (e instanceof Player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, AURA_INTERVAL_TICKS + 10, 0, false, false));
        }
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, center.clone().add(0, 0.5, 0), 2, FrostHeartOffhand.FROST_RADIUS, 0.5, FrostHeartOffhand.FROST_RADIUS, 0);
    }

    private void freezeUnderFeet(Player p) {
        Block ground = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        for (int dx = -FROST_WALKER_RADIUS; dx <= FROST_WALKER_RADIUS; dx++) {
            for (int dz = -FROST_WALKER_RADIUS; dz <= FROST_WALKER_RADIUS; dz++) {
                Block b = ground.getRelative(dx, 0, dz);
                if (b.getType() == Material.WATER) {
                    b.setType(Material.FROSTED_ICE);
                }
            }
        }
    }
}