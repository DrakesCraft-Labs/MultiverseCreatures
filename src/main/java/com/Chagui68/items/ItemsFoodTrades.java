package com.Chagui68.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemsFoodTrades {

    public static final NamespacedKey COOKIE_KEY = new NamespacedKey("multiversecreatures", "msc_scooby_cookie");
    public static final ItemStack SCOOBY_COOKIES = new ItemStack(Material.COOKIE);

    static {
        ItemMeta meta = SCOOBY_COOKIES.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Scooby Cookie");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A cookie that will fill you with courage");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(COOKIE_KEY, PersistentDataType.INTEGER, 1);
            SCOOBY_COOKIES.setItemMeta(meta);
        }
    }
}
