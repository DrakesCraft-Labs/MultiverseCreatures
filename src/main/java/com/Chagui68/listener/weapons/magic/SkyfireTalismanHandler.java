package com.Chagui68.listener.weapons.magic;

import com.Chagui68.items.weapons.magic.SkyfireTalisman;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkyfireTalismanHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public SkyfireTalismanHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isTalisman(ItemStack item) {
        if (item == null || item.getType() != Material.COPPER_INGOT) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(SkyfireTalisman.TALISMAN_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = event.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        if (!isTalisman(main)) return;

        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId(), 0L) > now) {
            long left = (cooldowns.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage(ChatColor.RED + "Skyfire Strike on cooldown: " + left + "s");
            return;
        }

        cooldowns.put(p.getUniqueId(), now + SkyfireTalisman.STRIKE_COOLDOWN_MS);
        strike(p, event.getClickedBlock().getLocation());
    }

    private void strike(Player p, Location target) {
        Location strikeLoc = target.clone().add(0.5, 0, 0.5);
        p.getWorld().strikeLightningEffect(strikeLoc);
        p.getWorld().playSound(strikeLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
        p.getWorld().spawnParticle(org.bukkit.Particle.FLASH, strikeLoc, 1,
                org.bukkit.Color.WHITE);

        double radius = SkyfireTalisman.STRIKE_RADIUS;
        double dmg = SkyfireTalisman.STRIKE_DAMAGE;
        for (LivingEntity e : p.getWorld().getLivingEntities()) {
            if (e instanceof Player && e.getUniqueId().equals(p.getUniqueId())) continue;
            if (e.getLocation().distanceSquared(strikeLoc) <= radius * radius) {
                e.damage(dmg, p);
                e.setVelocity(e.getVelocity().add(new org.bukkit.util.Vector(0, 0.6, 0)));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}