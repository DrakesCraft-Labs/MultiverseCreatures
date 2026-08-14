package com.Chagui68.items.weapons.magic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ChaosForge {

    public static final NamespacedKey FORGE_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_forge");
    public static final ItemStack CHAOS_FORGE = new ItemStack(Material.ANVIL);

    public static final int MAX_ENCHANT_LEVEL = 254;
    public static final String REFORGED_PDC_KEY = "msc_chaos_reforged";
    public static final String REFORGED_LORE_TAG = ChatColor.DARK_RED + "" + ChatColor.ITALIC + "⟡ Reforged by Chaos ⟡";

    static {
        ItemMeta meta = CHAOS_FORGE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Forge");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A portable anvil laced with entropy.");
            lore.add(ChatColor.GRAY + "It cannot create — only twist what");
            lore.add(ChatColor.GRAY + "is already written upon an item.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Reforge " + ChatColor.GRAY + "(Right-Click)");
            lore.add(ChatColor.GRAY + "  Hold the item to reforge in your " + ChatColor.WHITE + "off-hand" + ChatColor.GRAY + " and the");
            lore.add(ChatColor.WHITE + "  Chaos Forge" + ChatColor.GRAY + " in your main hand, then right-click.");
            lore.add(ChatColor.GRAY + "  Improves " + ChatColor.GOLD + "1 random enchantment" + ChatColor.GRAY + " by");
            lore.add(ChatColor.GRAY + "  " + ChatColor.GOLD + "+1 level " + ChatColor.GRAY + "(cap " + ChatColor.GOLD + "254" + ChatColor.GRAY + ").");
            lore.add("");
            lore.add(ChatColor.WHITE + "Restrictions:");
            lore.add(ChatColor.RED + "  ▸ " + ChatColor.GRAY + "Only items with existing enchantments");
            lore.add(ChatColor.RED + "  ▸ " + ChatColor.GRAY + "Consumes " + ChatColor.LIGHT_PURPLE + "1 Chaos Orb " + ChatColor.GRAY + "(once per item)");
            lore.add(ChatColor.RED + "  ▸ " + ChatColor.GRAY + "or " + ChatColor.DARK_PURPLE + "1 Condensed Chaos Orb " + ChatColor.GRAY + "for unlimited reforges");
            lore.add(ChatColor.RED + "  ▸ " + ChatColor.GRAY + "Requires the item to have been reforged once");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"In the orb, all possibilities;");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "in the hand, only one.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(FORGE_KEY, PersistentDataType.INTEGER, 1);
            CHAOS_FORGE.setItemMeta(meta);
        }
    }
}
