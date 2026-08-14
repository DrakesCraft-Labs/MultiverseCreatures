package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class VoidEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_void_essence");
    public static final ItemStack VOID_ESSENCE = ItemBuilder.of(Material.ENDER_EYE)
            .name(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Void Essence")
            .lore(
                    ChatColor.GRAY + "A droplet of un-space, wrung from",
                    ChatColor.GRAY + "the dissolving husk of a Void Crawler.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It is not there,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "and yet it is.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
