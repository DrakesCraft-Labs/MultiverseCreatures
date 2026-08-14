package com.Chagui68.items.food;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ScoobyCookie {

    public static final NamespacedKey COOKIE_KEY = new NamespacedKey("multiversecreatures", "msc_scooby_cookie");
    public static final ItemStack SCOOBY_COOKIE = new ItemStack(Material.COOKIE);

    static {
        ItemMeta meta = SCOOBY_COOKIE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Scooby Cookie");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A mysterious cookie pulsating");
            lore.add(ChatColor.GRAY + "with otherworldly energy.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Effect on Consume:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Resistance VI " + ChatColor.DARK_GRAY + "(10 seconds)");
            lore.add("");
            lore.add(ChatColor.AQUA + "Food: " + ChatColor.WHITE + "2 " + ChatColor.AQUA + "Saturation: " + ChatColor.WHITE + "0.4");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Scooby-Dooby-Doo...\"");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"This tastes like courage!\"");
            lore.add("");
            lore.add(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "Special" + ChatColor.GOLD + " ✦");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Mystery Inc." + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(COOKIE_KEY, PersistentDataType.INTEGER, 1);
            SCOOBY_COOKIE.setItemMeta(meta);
        }
    }
}