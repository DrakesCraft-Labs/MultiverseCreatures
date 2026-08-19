package com.Chagui68.listener.misc;

import com.Chagui68.items.misc.WirtsLantern;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WirtsLanternHandler implements Listener {

    private final Plugin plugin;
    private final Random random = new Random();
    private final Set<UUID> activeRepel = ConcurrentHashMap.newKeySet();
    private double repelRadius;
    private int repelIntervalTicks;
    private double repelParticleChance;

    public WirtsLanternHandler(Plugin plugin) {
        this.plugin = plugin;
        repelRadius = plugin.getConfig().getDouble("items.wirts-lantern.mob-repel.radius", 12.0);
        repelIntervalTicks = plugin.getConfig().getInt("items.wirts-lantern.mob-repel.interval-ticks", 20);
        repelParticleChance = plugin.getConfig().getDouble("items.wirts-lantern.mob-repel.particle-chance", 0.3);
        startRepelTask();
    }

    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new HashSet<>(activeRepel)) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline() || !hasLantern(p)) {
                        activeRepel.remove(uuid);
                        continue;
                    }
                    applyNightVision(p);
                    repelNearbyEntities(p);
                }
            }
        }.runTaskTimer(plugin, 0L, repelIntervalTicks);
    }

    private boolean hasLantern(Player p) {
        return isLantern(p.getInventory().getItemInMainHand()) || isLantern(p.getInventory().getItemInOffHand());
    }

    private boolean isLantern(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(WirtsLantern.WIRTS_LANTERN_KEY, PersistentDataType.INTEGER);
    }

    private void repelNearbyEntities(Player p) {
        Location center = p.getLocation();
        for (Entity entity : p.getWorld().getNearbyEntities(center, repelRadius, repelRadius, repelRadius)) {
            if (entity.equals(p)) continue;
            if (entity instanceof Player) continue;
            if (!(entity instanceof LivingEntity living)) continue;

            Vector direction = living.getLocation().toVector().subtract(center.toVector());
            if (direction.lengthSquared() > 0) {
                Vector away = direction.normalize().multiply(1.2);
                away.setY(Math.min(away.getY() + 0.3, 0.6));
                living.setVelocity(living.getVelocity().add(away));
            }

            if (living instanceof Mob mob && mob.getTarget() == p) {
                mob.setTarget(null);
            }

            if (random.nextDouble() < repelParticleChance) {
                p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, living.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player p)) return;
        if (!hasLantern(p)) return;
        if (e.getEntity() instanceof LivingEntity) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!hasLantern(p)) return;

        if (e instanceof EntityDamageByEntityEvent byEntity) {
            if (byEntity.getDamager() instanceof Player) return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player damager = getPlayerDamager(e.getDamager());
        if (damager == null) return;
        if (!hasLantern(damager)) return;
        if (e.getEntity() instanceof Player) return;
        e.setCancelled(true);
    }

    private Player getPlayerDamager(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        checkLantern(e.getPlayer());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        checkLantern(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        checkLantern(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        activeRepel.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !isLantern(item)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        ItemStack item = e.getPlayerItem();
        if (item == null || !isLantern(item)) return;
        e.setCancelled(true);
    }

    private void checkLantern(Player p) {
        if (hasLantern(p)) {
            activeRepel.add(p.getUniqueId());
            applyNightVision(p);
        } else {
            activeRepel.remove(p.getUniqueId());
        }
    }

    private void applyNightVision(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0, false, false), false);
    }
}
