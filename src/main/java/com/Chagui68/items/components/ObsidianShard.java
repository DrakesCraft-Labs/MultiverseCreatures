package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ObsidianShard {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_shard");
    public static final ItemStack OBSIDIAN_SHARD = ItemBuilder.of(Material.OBSIDIAN)
            .name(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Obsidian Shard")
            .lore(
                    ChatColor.GRAY + "A flawless splinter of obsidian, hewn",
                    ChatColor.GRAY + "from the armor of an Obsidian Guard.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Blacker than night,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "harder than resolve.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
