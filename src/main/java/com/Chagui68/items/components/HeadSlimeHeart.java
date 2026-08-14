package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class HeadSlimeHeart {

    public static final NamespacedKey HEART_KEY = new NamespacedKey("multiversecreatures", "msc_head_slime_heart");
    public static final ItemStack HEAD_SLIME_HEART = ItemBuilder.of(Material.SLIME_BALL)
            .name(ChatColor.GREEN + "" + ChatColor.BOLD + "Head Slime Heart")
            .lore(
                    ChatColor.GRAY + "The pulsating core of a Head Slime.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It still squirms...\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Slime Kingdom" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(HEART_KEY)
            .build();
}
