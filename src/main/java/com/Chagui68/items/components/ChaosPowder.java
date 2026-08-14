package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosPowder {

    public static final NamespacedKey CHAOS_POWDER_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_powder");
    public static final ItemStack CHAOS_POWDER = ItemBuilder.of(Material.ECHO_SHARD)
            .name(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Powder")
            .lore(
                    ChatColor.GRAY + "A fine dust ground from a Chaos Orb,",
                    ChatColor.GRAY + "still crackling with unstable energy.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Order is a lie told",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "by the calm.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(CHAOS_POWDER_KEY)
            .build();
}
