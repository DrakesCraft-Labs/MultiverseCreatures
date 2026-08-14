package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReaperCore {

    public static final NamespacedKey REAPER_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_reaper_core");
    public static final ItemStack REAPER_CORE = ItemBuilder.of(Material.WITHER_ROSE)
            .name(ChatColor.BLACK + "" + ChatColor.BOLD + "Reaper Core")
            .lore(
                    ChatColor.GRAY + "The condensed lament of every soul",
                    ChatColor.GRAY + "reaped by the Soul Reaper, blooming",
                    ChatColor.GRAY + "in a single dark flower.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Each soul makes the blade heavier,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "yet the wielder lighter.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(REAPER_CORE_KEY)
            .build();
}
