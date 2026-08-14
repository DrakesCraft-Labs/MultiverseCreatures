package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class WheelEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_wheel_essence");
    public static final ItemStack WHEEL_ESSENCE = ItemBuilder.of(Material.NETHERITE_SCRAP)
            .name(ChatColor.WHITE + "" + ChatColor.BOLD + "Wheel Essence")
            .lore(
                    ChatColor.GRAY + "A fragment of the Eight-Handled",
                    ChatColor.GRAY + "Wheel, severed from a fallen Mahoraga.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"That which adapts cannot break,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "that which breaks cannot return.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
