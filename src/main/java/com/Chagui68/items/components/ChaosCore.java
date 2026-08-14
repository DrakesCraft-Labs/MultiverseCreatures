package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosCore {

    public static final NamespacedKey CHAOS_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_core");
    public static final ItemStack CHAOS_CORE = ItemBuilder.of(Material.END_CRYSTAL)
            .name(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Core")
            .lore(
                    ChatColor.GRAY + "A furnace-bright core of pure disorder,",
                    ChatColor.GRAY + "bound tight enough to hold,",
                    ChatColor.GRAY + "barely.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The tighter you hold it,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "the louder it roars.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(CHAOS_CORE_KEY)
            .build();
}
