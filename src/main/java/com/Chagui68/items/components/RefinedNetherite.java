package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class RefinedNetherite {

    public static final NamespacedKey REFINED_NETHERITE_KEY = new NamespacedKey("multiversecreatures", "msc_refined_netherite");
    public static final ItemStack REFINED_NETHERITE = ItemBuilder.of(Material.NETHERITE_INGOT)
            .name(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Refined Netherite")
            .lore(
                    ChatColor.GRAY + "Netherite scrap pressed and reforged",
                    ChatColor.GRAY + "into a flawless, denser alloy.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Blacker than night,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "harder than resolve.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(REFINED_NETHERITE_KEY)
            .build();
}
