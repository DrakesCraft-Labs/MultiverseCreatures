package com.Chagui68.listener;

import com.Chagui68.items.ItemsFoodTrades;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemFoodHandler implements Listener {

    @EventHandler
    public void cuandoCome(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;

        }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(ItemsFoodTrades.COOKIE_KEY, PersistentDataType.INTEGER)){

                player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(200,5));
            }
        }
    }

