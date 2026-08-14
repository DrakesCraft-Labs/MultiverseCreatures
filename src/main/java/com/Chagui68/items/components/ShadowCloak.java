package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ShadowCloak {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_shadow_cloak");
    public static final ItemStack SHADOW_CLOAK = ItemBuilder.of(Material.BLACK_WOOL)
            .name(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Shadow Cloak Fragment")
            .lore(
                    ChatColor.GRAY + "A shred of woven darkness, torn from",
                    ChatColor.GRAY + "a Shadow Rogue during the kill.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Light bends around it,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "as if afraid to touch it.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
