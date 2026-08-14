package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosOrb {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_chaos_orb");
    public static final ItemStack CHAOS_ORB = ItemBuilder.of(Material.NETHER_STAR)
            .name(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Orb")
            .lore(
                    ChatColor.GRAY + "A sphere of pure entropy, plucked",
                    ChatColor.GRAY + "from the spell-scatter of a Chaos Mage.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"In the orb, all possibilities;",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "in the hand, only one.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
