package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CompressedGoldBlock {

    public static final NamespacedKey COMPRESSED_GOLD_BLOCK_KEY = new NamespacedKey("multiversecreatures", "msc_compressed_gold_block");
    public static final ItemStack COMPRESSED_GOLD_BLOCK = ItemBuilder.of(Material.GOLD_BLOCK)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Compressed Gold Block")
            .lore(
                    ChatColor.GRAY + "Nine gold blocks pressed into one,",
                    ChatColor.GRAY + "dense enough to anchor a smithing",
                    ChatColor.GRAY + "ritual of its own.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Gold remembers every hand",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "that weighed it.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(COMPRESSED_GOLD_BLOCK_KEY)
            .build();
}