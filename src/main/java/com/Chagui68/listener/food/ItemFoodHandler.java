package com.Chagui68.listener.food;

import com.Chagui68.items.food.HeadSlimeGelatin;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.entities.HeadSlime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;

public class ItemFoodHandler implements Listener {

    private final Plugin plugin;

    public ItemFoodHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(ScoobyCookie.COOKIE_KEY, PersistentDataType.INTEGER)) {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 5));
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(HeadSlimeGelatin.GELATIN_KEY, PersistentDataType.INTEGER)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        HeadSlime.immunePlayers.add(player.getUniqueId());

        item.setAmount(item.getAmount() - 1);
        player.updateInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HeadSlime.immunePlayers.remove(player.getUniqueId());
        }, 200L);
    }
}