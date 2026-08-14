package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class StormCrystal {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_storm_crystal");
    public static final ItemStack STORM_CRYSTAL = ItemBuilder.of(Material.QUARTZ)
            .name(ChatColor.YELLOW + "" + ChatColor.BOLD + "Storm Crystal")
            .lore(
                    ChatColor.GRAY + "A crackling shard of bottled lightning,",
                    ChatColor.GRAY + "taken from the carcass of a Storm Caller.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Thunder made solid,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "rage made still.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
