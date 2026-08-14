package com.Chagui68.listener.weapons.melee;

import com.Chagui68.items.weapons.melee.CinderGreatsword;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CinderGreatswordHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> slamCooldowns = new ConcurrentHashMap<>();

    public CinderGreatswordHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isCinder(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(CinderGreatsword.CINDER_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();
        if (isCinder(main) || isCinder(off)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The Cinder Greatsword cannot be paired with an off-hand item.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player p = event.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        if (!isCinder(main)) return;

        // Two-handed: the ability can only be used if the off-hand is empty.
        ItemStack off = p.getInventory().getItemInOffHand();
        if (off != null && off.getType() != Material.AIR) {
            if (event.getAction().isRightClick()) {
                p.sendMessage(ChatColor.RED + "The Cinder Greatsword requires both hands. Free your off-hand to use Cinder Slam.");
            }
            return;
        }

        if (event.getAction().isRightClick()) {
            long now = System.currentTimeMillis();
            if (slamCooldowns.getOrDefault(p.getUniqueId(), 0L) > now) {
                long remaining = (slamCooldowns.get(p.getUniqueId()) - now) / 1000;
                p.sendMessage(ChatColor.RED + "Slam on cooldown: " + remaining + "s");
                return;
            }

            slamCooldowns.put(p.getUniqueId(), now + CinderGreatsword.SLAM_COOLDOWN_MS);
            slam(p);
        }
    }

    private void slam(Player p) {
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.6f);
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, p.getLocation(), 15, 1.5, 0.5, 1.5, 0.1);
        p.getWorld().spawnParticle(org.bukkit.Particle.FLAME, p.getLocation(), 30, 1.5, 0.5, 1.5, 0.1);
        p.getWorld().spawnParticle(org.bukkit.Particle.LAVA, p.getLocation(), 20, 1.5, 0.5, 1.5, 0);

        for (Entity e : p.getNearbyEntities(CinderGreatsword.SLAM_RADIUS, CinderGreatsword.SLAM_RADIUS, CinderGreatsword.SLAM_RADIUS)) {
            if (e instanceof LivingEntity le && le != p) {
                le.damage(CinderGreatsword.SLAM_DAMAGE, p);
                le.setFireTicks(CinderGreatsword.SLAM_FIRE_TICKS);
                le.setVelocity(le.getVelocity().add(new org.bukkit.util.Vector(0, 0.6, 0)));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        if (!isCinder(p.getInventory().getItemInMainHand())) return;
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        le.setFireTicks(CinderGreatsword.SLAM_FIRE_TICKS / 2);
    }
}