package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class FrostHeart {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_frost_heart");
    public static final ItemStack FROST_HEART = ItemBuilder.of(Material.BLUE_ICE)
            .name(ChatColor.AQUA + "" + ChatColor.BOLD + "Frost Heart")
            .lore(
                    ChatColor.GRAY + "A frozen core that never melts,",
                    ChatColor.GRAY + "shattered from the chest of a Frost Golem.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It beats once a century,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "and winter follows.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
