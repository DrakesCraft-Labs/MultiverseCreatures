package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenNetherite {

    public static final NamespacedKey MOLTEN_NETHERITE_KEY = new NamespacedKey("multiversecreatures", "msc_molten_netherite");
    public static final ItemStack MOLTEN_NETHERITE = ItemBuilder.of(Material.ANCIENT_DEBRIS)
            .name(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Molten Netherite")
            .lore(
                    ChatColor.GRAY + "Refined Netherite reduced to flowing",
                    ChatColor.GRAY + "darkness in the same crucible that",
                    ChatColor.GRAY + "melts the wheel.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"What burns twice holds",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "twice the weight.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(MOLTEN_NETHERITE_KEY)
            .build();
}