package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MilitaryComponent {

    public static final NamespacedKey MILITARY_KEY = new NamespacedKey("multiversecreatures", "msc_military_component");
    public static final ItemStack MILITARY_COMPONENT = ItemBuilder.of(Material.GUNPOWDER)
            .name(ChatColor.GREEN + "" + ChatColor.BOLD + "Military Component")
            .lore(
                    ChatColor.GRAY + "A piece of military-grade equipment",
                    ChatColor.GRAY + "salvaged from the battlefield.",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Standard issue. Nothing more, nothing less.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Military" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(MILITARY_KEY)
            .build();
}
