package com.Chagui68.items.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WirtsLantern {

    public static final NamespacedKey WIRTS_LANTERN_KEY = new NamespacedKey("multiversecreatures", "msc_wirts_lantern");
    public static final ItemStack WIRTS_LANTERN = new ItemStack(Material.SOUL_LANTERN);

    static {
        ItemMeta meta = WIRTS_LANTERN.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Wirt's Lantern");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A lantern that holds a lost soul.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Repels hostile mobs in a radius");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"The flame knows no winter.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Khand" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(WIRTS_LANTERN_KEY, PersistentDataType.INTEGER, 1);
            WIRTS_LANTERN.setItemMeta(meta);
        }
    }
}