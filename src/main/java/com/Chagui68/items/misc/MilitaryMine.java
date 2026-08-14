package com.Chagui68.items.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MilitaryMine {

    public static final NamespacedKey MINE_KEY = new NamespacedKey("multiversecreatures", "msc_military_mine");
    public static final ItemStack MILITARY_MINE = new ItemStack(Material.TNT);

    static {
        ItemMeta meta = MILITARY_MINE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Military Mine");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A crafted explosive device");
            lore.add(ChatColor.GRAY + "used for battlefield traps.");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"One step is all it takes.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Military" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(MINE_KEY, PersistentDataType.INTEGER, 1);
            MILITARY_MINE.setItemMeta(meta);
        }
    }
}
