package com.Chagui68.listener;

import com.Chagui68.items.ItemCombatTrades;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import static org.bukkit.Bukkit.getOnlinePlayers;

public class ItemCombatHandler implements Listener {

    @EventHandler
    public void cuandoSostieneExcalibur(PlayerItemHeldEvent event) {
        efectoEspada(event.getPlayer());
    }

    public static void efectoEspada(Player player){
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType().isAir()) {
                return;
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(ItemCombatTrades.EXCALIBUR_KEY, PersistentDataType.INTEGER)){
                player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(80 , 2));
            }
        }
    public static void inicializarReloj(Plugin plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : getOnlinePlayers()) {
                efectoEspada(player);
            }
        }, 0L, 20L); // 20 ticks = 1 segundo
    }
}


