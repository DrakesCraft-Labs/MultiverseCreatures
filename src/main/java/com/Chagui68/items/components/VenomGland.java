package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class VenomGland {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_venom_gland");
    public static final ItemStack VENOM_GLAND = ItemBuilder.of(Material.SPIDER_EYE)
            .name(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Venom Gland")
            .lore(
                    ChatColor.GRAY + "A pulsating sac of corrosive venom,",
                    ChatColor.GRAY + "harvested from a Venom Witch.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"One drop can dissolve",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "a man's resolve...\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
