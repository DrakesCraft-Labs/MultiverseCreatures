package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CondensedChaosOrb {

    public static final NamespacedKey CONDENSED_CHAOS_ORB_KEY = new NamespacedKey("multiversecreatures", "msc_condensed_chaos_orb");
    public static final ItemStack CONDENSED_CHAOS_ORB = ItemBuilder.of(Material.NETHER_STAR)
            .name(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Condensed Chaos Orb")
            .lore(
                    ChatColor.GRAY + "A Chaos Orb pressed past the point",
                    ChatColor.GRAY + "of breaking, holding an impossible",
                    ChatColor.GRAY + "amount of entropy in a single point.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.AQUA + "Reforge: " + ChatColor.GRAY + "Allows unlimited reforges",
                    ChatColor.GRAY + "in the " + ChatColor.WHITE + "Chaos Forge" + ChatColor.GRAY + " (no once-per-item limit).",
                    ChatColor.GRAY + "Requires the item to have been reforged",
                    ChatColor.GRAY + "with a " + ChatColor.LIGHT_PURPLE + "Chaos Orb" + ChatColor.GRAY + " first.",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "All possibilities,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "crushed into one.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(CONDENSED_CHAOS_ORB_KEY)
            .build();
}
