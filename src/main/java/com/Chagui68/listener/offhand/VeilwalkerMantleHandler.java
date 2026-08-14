package com.Chagui68.listener.offhand;

import com.Chagui68.items.misc.offhand.VeilwalkerMantle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VeilwalkerMantleHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> stealthEnds = new ConcurrentHashMap<>();

    public VeilwalkerMantleHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isMantle(ItemStack item) {
        if (item == null || item.getType() != Material.CLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(VeilwalkerMantle.VEIL_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        Player p = event.getPlayer();
        ItemStack offhand = p.getInventory().getItemInOffHand();
        if (!isMantle(offhand)) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId(), 0L) > now) {
            long remaining = (cooldowns.get(p.getUniqueId()) - now) / 1000;
            p.sendMessage(ChatColor.RED + "Cooldown: " + remaining + "s");
            return;
        }

        cooldowns.put(p.getUniqueId(), now + VeilwalkerMantle.STEALTH_COOLDOWN_MS);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, VeilwalkerMantle.STEALTH_DURATION_TICKS, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, VeilwalkerMantle.STEALTH_DURATION_TICKS, 0, false, false));
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.5f);
        p.sendMessage(ChatColor.DARK_PURPLE + "You fade into the veil...");

        stealthEnds.put(p.getUniqueId(), now + (VeilwalkerMantle.STEALTH_DURATION_TICKS * 50L));
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!stealthEnds.containsKey(attacker.getUniqueId())) return;

        ItemStack offhand = attacker.getInventory().getItemInOffHand();
        if (!isMantle(offhand)) return;

        long now = System.currentTimeMillis();
        if (stealthEnds.get(attacker.getUniqueId()) < now) {
            stealthEnds.remove(attacker.getUniqueId());
            return;
        }

        double mult = VeilwalkerMantle.BACKSTAB_DAMAGE_MULTIPLIER;
        event.setDamage(event.getDamage() * mult);
        attacker.sendMessage(ChatColor.DARK_PURPLE + "Backstab! +" + (int) ((mult - 1) * 100) + "% damage.");

        stealthEnds.remove(attacker.getUniqueId());
        attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
        attacker.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stealthEnds.remove(event.getPlayer().getUniqueId());
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}