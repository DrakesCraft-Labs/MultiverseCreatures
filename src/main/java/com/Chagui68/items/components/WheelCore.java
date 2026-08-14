package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class WheelCore {

    public static final NamespacedKey WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_wheel_core");
    public static final ItemStack WHEEL_CORE = ItemBuilder.of(Material.MUSIC_DISC_OTHERSIDE)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Wheel Core")
            .lore(
                    ChatColor.GRAY + "A fragment of the Eight-Handled Wheel,",
                    ChatColor.GRAY + "turned into a core that spins toward",
                    ChatColor.GRAY + "perfection and endless adaptation.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"That which adapts cannot break,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "that which breaks cannot return.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(WHEEL_CORE_KEY)
            .build();
}
