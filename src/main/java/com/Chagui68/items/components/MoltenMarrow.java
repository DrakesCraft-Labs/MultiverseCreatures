package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenMarrow {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_molten_marrow");
    public static final ItemStack MOLTEN_MARROW = ItemBuilder.of(Material.REDSTONE)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Molten Marrow")
            .lore(
                    ChatColor.GRAY + "An Ossified Plate held past its",
                    ChatColor.GRAY + "melting point in the fires of a",
                    ChatColor.GRAY + "blast furnace, glowing like hot blood.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Only the hottest fire can make",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "bone remember it was alive.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}