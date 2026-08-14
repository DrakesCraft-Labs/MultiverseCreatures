package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MagmaCore {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_magma_core");
    public static final ItemStack MAGMA_CORE = ItemBuilder.of(Material.MAGMA_CREAM)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Magma Core")
            .lore(
                    ChatColor.GRAY + "A sphere of condensed flame, ripped",
                    ChatColor.GRAY + "from the heart of a Flame Elemental.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It burns without fuel,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "a sun that fits in the palm.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
