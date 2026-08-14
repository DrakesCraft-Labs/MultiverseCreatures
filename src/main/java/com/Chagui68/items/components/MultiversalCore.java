package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MultiversalCore {

    public static final NamespacedKey MULTIVERSAL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_multiversal_core");
    public static final ItemStack MULTIVERSAL_CORE = ItemBuilder.of(Material.TOTEM_OF_UNDYING)
            .name(ChatColor.GOLD + "" + ChatColor.BOLD + "Multiversal Core")
            .lore(
                    ChatColor.GRAY + "A Sentinel Core reforged at the",
                    ChatColor.GRAY + "crossroads of a thousand worlds,",
                    ChatColor.GRAY + "bound with stars and refined netherite.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Every universe remembers",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "what it forged.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(MULTIVERSAL_CORE_KEY)
            .build();
}