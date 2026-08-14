package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReinforcedBone {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_reinforced_bone");
    public static final ItemStack REINFORCED_BONE = ItemBuilder.of(Material.BONE)
            .name(ChatColor.WHITE + "" + ChatColor.BOLD + "Reinforced Bone")
            .lore(
                    ChatColor.GRAY + "A bone denser than diamond, broken",
                    ChatColor.GRAY + "from the living wall of a Bone Shield.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Death's architecture,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "preserved in marrow.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
