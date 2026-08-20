package com.Chagui68.listener.weapons.melee;

import com.Chagui68.items.weapons.melee.NullshearEdge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NullshearEdgeHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> blinkCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> inVoidDamage = ConcurrentHashMap.newKeySet();
    private final double darknessChance;
    private final double voidFraction;
    private final int darknessDurationTicks;
    private final long voidBlinkCooldownMs;
    private final double voidBlinkRange;

    public NullshearEdgeHandler(Plugin plugin) {
        this.plugin = plugin;
        var config = plugin.getConfig();
        darknessChance = config.getDouble("items.nullshear-edge.darkness-chance", 0.1);
        voidFraction = config.getDouble("items.nullshear-edge.void-fraction", 0.3);
        darknessDurationTicks = config.getInt("items.nullshear-edge.darkness-duration-ticks", 100);
        voidBlinkCooldownMs = config.getLong("items.nullshear-edge.void-blink-cooldown-ms", 20000L);
        voidBlinkRange = config.getDouble("items.nullshear-edge.void-blink-range", 30.0);
    }

    private boolean isNullshear(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(NullshearEdge.NULL_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player p = event.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        if (!isNullshear(main)) return;

        if (event.getAction().isRightClick() && p.isSneaking()) {
            long now = System.currentTimeMillis();
            if (blinkCooldowns.getOrDefault(p.getUniqueId(), 0L) > now) {
                long remaining = (blinkCooldowns.get(p.getUniqueId()) - now) / 1000;
                p.sendMessage(ChatColor.RED + "Void Blink on cooldown: " + remaining + "s");
                return;
            }

            blinkCooldowns.put(p.getUniqueId(), now + voidBlinkCooldownMs);
            voidBlink(p);
        }
    }

    private void voidBlink(Player p) {
        org.bukkit.util.RayTraceResult result = p.rayTraceBlocks(voidBlinkRange);
        org.bukkit.Location dest;
        if (result != null && result.getHitBlock() != null) {
            dest = result.getHitBlock().getLocation().add(0, 1, 0).getBlock().getRelative(org.bukkit.block.BlockFace.UP).getLocation();
        } else {
            org.bukkit.util.Vector dir = p.getEyeLocation().getDirection().normalize().multiply(voidBlinkRange);
            dest = p.getEyeLocation().add(dir);
            dest.setY(dest.getWorld().getHighestBlockYAt(dest) + 1);
        }

        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        p.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, p.getLocation(), 30, 0.5, 1, 0.5, 0.1);
        p.teleport(dest);
        p.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, dest, 30, 0.5, 1, 0.5, 0.1);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        if (!isNullshear(p.getInventory().getItemInMainHand())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (!inVoidDamage.add(p.getUniqueId())) return;
        try {
            double voidDmg = event.getDamage() * voidFraction;
            target.setHealth(Math.max(0, target.getHealth() - voidDmg));
        } finally {
            inVoidDamage.remove(p.getUniqueId());
        }

        // Darkness chance
        if (p.getWorld().hasStorm() || Math.random() < darknessChance) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, darknessDurationTicks, 0, false, false));
        }
    }
}