package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosFragment {

    public static final NamespacedKey CHAOS_FRAGMENT_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_fragment");
    public static final ItemStack CHAOS_FRAGMENT = ItemBuilder.of(Material.AMETHYST_SHARD)
            .name(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Fragment")
            .lore(
                    ChatColor.GRAY + "Crystallized shards of compressed chaos,",
                    ChatColor.GRAY + "far more potent than the dust",
                    ChatColor.GRAY + "they were born from.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Every fragment",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "screams in a single voice.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(CHAOS_FRAGMENT_KEY)
            .build();
}
