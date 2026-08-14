package com.Chagui68.items.armor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class EightHandledWheel {

    public static final NamespacedKey WHEEL_KEY = new NamespacedKey("multiversecreatures", "msc_eight_handled_wheel");
    public static final ItemStack EIGHT_HANDLED_WHEEL = new ItemStack(Material.NETHERITE_HELMET);

    public static final int MAX_CHARGES = 8;
    public static final int CHARGE_REGEN_TICKS = 300;
    public static final int BLOCK_DURATION_TICKS = 160;

    public static final String CHARGES_KEY = "msc_wheel_charges";
    public static final String BLOCKED_CAUSE_KEY = "msc_wheel_blocked_cause";
    public static final String BLOCK_UNTIL_KEY = "msc_wheel_block_until";
    public static final String NEXT_BLOCK_KEY = "msc_wheel_next_block";

    static {
        ItemMeta meta = EIGHT_HANDLED_WHEEL.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Eight-Handled Wheel");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A crown carved from a fragment of the");
            lore.add(ChatColor.GRAY + "Eight-Handled Wheel that once turned");
            lore.add(ChatColor.GRAY + "against all harm.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Holds up to " + ChatColor.GOLD + "8 charges");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each charge regenerates over " + ChatColor.GOLD + "15 seconds");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Adaptation " + ChatColor.GRAY + "(Passive)");
            lore.add(ChatColor.GRAY + "  On receiving damage, consume " + ChatColor.GOLD + "1 charge " + ChatColor.GRAY + "to become");
            lore.add(ChatColor.GRAY + "  immune to that damage type for " + ChatColor.GOLD + "8 seconds" + ChatColor.GRAY + ".");
            lore.add(ChatColor.GRAY + "  Multiple types in the same tick each spawn");
            lore.add(ChatColor.GRAY + "  separate immunity effects.");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"That which adapts cannot break,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "that which breaks cannot return.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(WHEEL_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            EIGHT_HANDLED_WHEEL.setItemMeta(meta);
        }
    }
}
