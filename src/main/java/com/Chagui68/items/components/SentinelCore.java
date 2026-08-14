package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SentinelCore {

    public static final NamespacedKey SENTINEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_sentinel_core");
    public static final ItemStack SENTINEL_CORE = ItemBuilder.of(Material.HEART_OF_THE_SEA)
            .name(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Sentinel Core")
            .lore(
                    ChatColor.GRAY + "The still-beating heart of the",
                    ChatColor.GRAY + "Obsidian Sentinel, harvested before",
                    ChatColor.GRAY + "the lightning could claim it.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It watched over every phase.",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Now it watches over none.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(SENTINEL_CORE_KEY)
            .build();
}