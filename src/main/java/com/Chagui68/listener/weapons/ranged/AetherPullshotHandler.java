package com.Chagui68.listener.weapons.ranged;

import com.Chagui68.items.weapons.ranged.AetherPullshot;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AetherPullshotHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public AetherPullshotHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isPullshot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(AetherPullshot.PULLSHOT_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;
        Player p = event.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        if (!isPullshot(main)) return;

        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId(), 0L) > now) {
            long left = (cooldowns.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage(ChatColor.RED + "Aether Pull on cooldown: " + left + "s");
            return;
        }

        org.bukkit.util.RayTraceResult ray = p.rayTraceEntities((int) AetherPullshot.PULL_RANGE);
        if (ray != null && ray.getHitEntity() instanceof LivingEntity target) {
            cooldowns.put(p.getUniqueId(), now + AetherPullshot.PULL_COOLDOWN_MS);
            startPull(p, target);
        }
    }

    private void startPull(Player p, LivingEntity target) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 20, 0.5, 1, 0.5, 0.05);
        target.damage(AetherPullshot.PULL_INITIAL_DAMAGE, p);

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= AetherPullshot.PULL_DURATION_TICKS) {
                    cancel();
                    return;
                }
                if (!target.isValid() || target.isDead()) {
                    cancel();
                    return;
                }
                if (!p.isValid() || p.isDead()) {
                    cancel();
                    return;
                }

                Location pLoc = p.getLocation();
                Location tLoc = target.getLocation();
                Vector dir = pLoc.toVector().subtract(tLoc.toVector()).normalize();
                target.setVelocity(dir.multiply(AetherPullshot.PULL_SPEED).setY(0.2));

                if (tLoc.distance(pLoc) < 2) {
                    target.damage(AetherPullshot.PULL_FINAL_DAMAGE, p);
                    p.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 5, 0.3, 0.5, 0.3, 0);
                    p.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                    cancel();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}