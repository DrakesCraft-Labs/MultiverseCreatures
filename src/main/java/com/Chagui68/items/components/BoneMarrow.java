package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class BoneMarrow {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_bone_marrow");
    public static final ItemStack BONE_MARROW = ItemBuilder.of(Material.BONE_MEAL)
            .name(ChatColor.WHITE + "" + ChatColor.BOLD + "Bone Marrow")
            .lore(
                    ChatColor.GRAY + "The red marrow crushed out of a",
                    ChatColor.GRAY + "reinforced bone, pulsing with the",
                    ChatColor.GRAY + "last warmth of its undead owner.",
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