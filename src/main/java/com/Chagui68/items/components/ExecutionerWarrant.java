package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ExecutionerWarrant {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_executioner_warrant");

    public static final ItemStack EXECUTIONER_WARRANT = ItemBuilder.of(Material.PAPER)
            .name(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Execution Warrant")
            .lore(
                    ChatColor.GRAY + "A grim decree of capital punishment sealed in dried blood.",
                    ChatColor.GRAY + "Bearing the mark of an unforgiving executioner.",
                    "",
                    ChatColor.WHITE + "Boss Invocation Catalyst",
                    "",
                    ChatColor.YELLOW + "Drop upon the Executioner's Scaffold anvil",
                    ChatColor.YELLOW + "within the Boss Dimension to summon NIX.",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"The sentence has been passed...\"",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "DrakesCraft" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
