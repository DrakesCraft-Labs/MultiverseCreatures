package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReinforcedBoneBlock {

    public static final NamespacedKey REINFORCED_BONE_BLOCK_KEY = new NamespacedKey("multiversecreatures", "msc_reinforced_bone_block");
    public static final ItemStack REINFORCED_BONE_BLOCK = ItemBuilder.of(Material.BONE_BLOCK)
            .name(ChatColor.WHITE + "" + ChatColor.BOLD + "Reinforced Bone Block")
            .lore(
                    ChatColor.GRAY + "Nine reinforced bones fused into",
                    ChatColor.GRAY + "a single unbreakable slab.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The dead do not yield,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "they simply endure.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(REINFORCED_BONE_BLOCK_KEY)
            .build();
}
