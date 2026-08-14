package com.Chagui68.listener;

import com.Chagui68.items.misc.MilitaryMine;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemPlaceHandler implements Listener {

    private static final String NAMESPACE = "multiversecreatures";

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isCustomItem(event.getItemInHand()) && !isMilitaryMine(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This custom item cannot be placed.");
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        EquipmentSlot slot = event.getHand();
        ItemStack hand = (slot == EquipmentSlot.OFF_HAND)
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        if (isCustomItem(hand)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "This custom item cannot be placed.");
        }
    }

    private boolean isMilitaryMine(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                .has(MilitaryMine.MINE_KEY, PersistentDataType.INTEGER);
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (NamespacedKey key : pdc.getKeys()) {
            if (key.getNamespace().equals(NAMESPACE)) {
                return true;
            }
        }
        return false;
    }
}
