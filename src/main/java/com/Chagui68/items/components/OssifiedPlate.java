package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class OssifiedPlate {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_ossified_plate");
    public static final ItemStack OSSIFIED_PLATE = ItemBuilder.of(Material.CALCITE)
            .name(ChatColor.WHITE + "" + ChatColor.BOLD + "Ossified Plate")
            .lore(
                    ChatColor.GRAY + "A slab of calcite-bone laminate,",
                    ChatColor.GRAY + "each layer marrow-set and hammered",
                    ChatColor.GRAY + "flat until it rings like iron.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Bone, made unbreakable,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "made patient, made a wall.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}