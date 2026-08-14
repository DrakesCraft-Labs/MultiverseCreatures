package com.Chagui68.listener.weapons.melee;

import com.Chagui68.items.weapons.melee.Venomfang;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VenomfangHandler implements Listener {

    private boolean isVenomfang(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_SWORD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(Venomfang.VENOMFANG_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        if (!isVenomfang(p.getInventory().getItemInMainHand())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Venomfang.POISON_DURATION_TICKS, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Venomfang.WITHER_DURATION_TICKS, 0, false, false));

        target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_SPIDER_AMBIENT, 0.8f, 1.4f);
        target.getWorld().spawnParticle(org.bukkit.Particle.ITEM_SLIME, target.getLocation().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.1);
    }
}
