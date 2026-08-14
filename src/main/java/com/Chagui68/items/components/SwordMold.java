package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SwordMold {

    public static final NamespacedKey SWORD_MOLD_KEY = new NamespacedKey("multiversecreatures", "msc_sword_mold");
    public static final ItemStack SWORD_MOLD = ItemBuilder.of(Material.IRON_HORSE_ARMOR)
            .name(ChatColor.GRAY + "" + ChatColor.BOLD + "Sword Mold")
            .lore(
                    ChatColor.GRAY + "An iron template shaped like a blade,",
                    ChatColor.GRAY + "ready to cast a venomous edge.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Forged in the furnace,",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "cooled in the hunt.",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(SWORD_MOLD_KEY)
            .build();
}
