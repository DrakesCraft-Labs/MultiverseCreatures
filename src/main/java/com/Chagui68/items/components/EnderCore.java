package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class EnderCore {

    public static final NamespacedKey ENDER_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_ender_core");
    public static final ItemStack ENDER_CORE = ItemBuilder.of(Material.SHULKER_SHELL)
            .name(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Ender Core")
            .lore(
                    ChatColor.GRAY + "A heart of compressed Ender energy,",
                    ChatColor.GRAY + "channeling the void between worlds.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "What is lost between worlds",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "is never truly gone.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(ENDER_CORE_KEY)
            .build();
}
