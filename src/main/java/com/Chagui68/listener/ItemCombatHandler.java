package com.Chagui68.listener;

import com.Chagui68.items.ItemCombatTrades;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.Chagui68.utils.VersionSafe;

public class ItemCombatHandler implements Listener {

    public ItemCombatHandler(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand == null || itemInHand.getType().isAir())
                        continue;

                    ItemMeta meta = itemInHand.getItemMeta();
                    if (meta == null)
                        continue;

                    if (meta.getPersistentDataContainer().has(ItemCombatTrades.EXCALIBUR_KEY,
                            PersistentDataType.INTEGER)) {
                        VersionSafe.applyPotionEffectSafe(player, PotionEffectType.STRENGTH, 80, 2);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
