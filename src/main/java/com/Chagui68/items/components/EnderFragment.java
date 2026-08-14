package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class EnderFragment {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_ender_fragment");
    public static final ItemStack ENDER_FRAGMENT = ItemBuilder.of(Material.ENDER_PEARL)
            .name(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Ender Fragment")
            .lore(
                    ChatColor.GRAY + "A splinter of an End knight's pearl,",
                    ChatColor.GRAY + "still humming with the spaces between.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"A step taken sideways",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "across the veil.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
