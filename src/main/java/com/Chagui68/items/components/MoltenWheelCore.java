package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenWheelCore {

    public static final NamespacedKey MOLTEN_WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_molten_wheel_core");
    public static final ItemStack MOLTEN_WHEEL_CORE = ItemBuilder.of(Material.BLAZE_POWDER)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Molten Wheel Core")
            .lore(
                    ChatColor.GRAY + "A Wheel Core held past its melting",
                    ChatColor.GRAY + "point in the fires of a blast furnace,",
                    ChatColor.GRAY + "burning like a captured ember.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Smelted only in the hottest fire,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "it turns like the wheel it came from.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(MOLTEN_WHEEL_CORE_KEY)
            .build();
}