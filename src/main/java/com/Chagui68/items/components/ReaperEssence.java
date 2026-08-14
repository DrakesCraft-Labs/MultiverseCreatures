package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReaperEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_reaper_essence");
    public static final ItemStack REAPER_ESSENCE = ItemBuilder.of(Material.SOUL_LANTERN)
            .name(ChatColor.BLACK + "" + ChatColor.BOLD + "Reaper Essence")
            .lore(
                    ChatColor.GRAY + "A whispering wisp of souls, drawn",
                    ChatColor.GRAY + "from the hollow skull of a Soul Reaper.",
                    "",
                    ChatColor.WHITE + "Crafting Ingredient",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It hums with the lament",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "of the unreaped.\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
