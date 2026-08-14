package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class RefinedWheelCore {

    public static final NamespacedKey REFINED_WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_refined_wheel_core");
    public static final ItemStack REFINED_WHEEL_CORE = ItemBuilder.of(Material.MUSIC_DISC_OTHERSIDE)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Refined Wheel Core")
            .lore(
                    ChatColor.GRAY + "Molten wheel and molten netherite,",
                    ChatColor.GRAY + "poured into each other until the two",
                    ChatColor.GRAY + "turn as one.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"The wheel that adapts to all,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "forged to break what breaks it.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(REFINED_WHEEL_CORE_KEY)
            .build();
}