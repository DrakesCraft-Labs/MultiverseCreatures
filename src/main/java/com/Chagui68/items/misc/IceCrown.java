package com.Chagui68.items.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class IceCrown {

    public static final NamespacedKey ICE_CROWN_KEY = new NamespacedKey("multiversecreatures", "msc_ice_crown");
    public static final ItemStack ICE_CROWN = new ItemStack(Material.HORN_CORAL_FAN);

    static {
        ItemMeta meta = ICE_CROWN.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ice King's Crown");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A crown of eternal winter...");
            lore.add("");
            lore.add(ChatColor.WHITE + "Abilities:");
            lore.add(ChatColor.AQUA + "  ▸ " + ChatColor.WHITE + "Right-Click: " + ChatColor.GRAY + "Launch targeted snow/ice block");
            lore.add(ChatColor.AQUA + "  ▸ " + ChatColor.WHITE + "Shift + Right-Click: " + ChatColor.GRAY + "Blizzard (AoE)");
            lore.add(ChatColor.AQUA + "  ▸ " + ChatColor.WHITE + "Left-Click: " + ChatColor.GRAY + "Toggle Ice Path");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Gunter, why you gotta be like that?\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Ooo" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(ICE_CROWN_KEY, PersistentDataType.INTEGER, 1);
            ICE_CROWN.setItemMeta(meta);
        }
    }
}