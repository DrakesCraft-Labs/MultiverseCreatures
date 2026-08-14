package com.Chagui68.items.food;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class HeadSlimeGelatin {

    public static final NamespacedKey GELATIN_KEY = new NamespacedKey("multiversecreatures", "msc_head_slime_gelatin");
    public static final ItemStack HEAD_SLIME_GELATIN = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);

    static {
        ItemMeta meta = HEAD_SLIME_GELATIN.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Head Slime Gelatin");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Bouncy and wobbly, yet strangely tasty.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Effect on Consume:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Head Slime Immunity " + ChatColor.DARK_GRAY + "(10 seconds)");
            lore.add("");
            lore.add(ChatColor.AQUA + "Food: " + ChatColor.WHITE + "4 " + ChatColor.AQUA + "Saturation: " + ChatColor.WHITE + "2.4");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Slimy yet satisfying!\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Slime Kingdom" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(GELATIN_KEY, PersistentDataType.INTEGER, 1);
            HEAD_SLIME_GELATIN.setItemMeta(meta);
        }
    }
}
