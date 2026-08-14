package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class StarCore {

    public static final NamespacedKey STAR_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_star_core");
    public static final ItemStack STAR_CORE = ItemBuilder.of(Material.NETHER_STAR)
            .name(ChatColor.YELLOW + "" + ChatColor.BOLD + "Star Core")
            .lore(
                    ChatColor.GRAY + "The strongest of this world, mixed with",
                    ChatColor.GRAY + "the strongest of another, forged around",
                    ChatColor.GRAY + "the heart of a superior entity.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The heart of a fallen star,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "beating with ancient power.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(STAR_CORE_KEY)
            .build();
}
