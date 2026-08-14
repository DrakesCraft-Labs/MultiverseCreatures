package com.Chagui68.items.weapons.melee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SoulreapScythe {

    public static final NamespacedKey SCYTHE_KEY = new NamespacedKey("multiversecreatures", "msc_soulreap_scythe");
    public static final ItemStack SOULREAP_SCYTHE = new ItemStack(Material.NETHERITE_HOE);

    public static final int LIFESTEAL_HIT = 2;
    public static final int SOULS_REQUIRED = 10;
    public static final int REAP_DURATION_TICKS = 200;
    public static final double REAP_DAMAGE_MULTIPLIER = 2.0;

    public static final String SOUL_COUNTER_KEY = "msc_soulreap_counter";
    public static final String REAP_ACTIVE_KEY = "msc_soulreap_reap_until";

    static {
        ItemMeta meta = SOULREAP_SCYTHE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.BOLD + "Soulreap Scythe");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A curved void-steel blade humming");
            lore.add(ChatColor.GRAY + "with the lament of the unreaped.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each strike drains " + ChatColor.RED + "4 HP" + ChatColor.GRAY + " and heals the wielder " + ChatColor.GREEN + "2 HP");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each strike collects a soul");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Reap " + ChatColor.GRAY + "(Passive)");
            lore.add(ChatColor.GRAY + "  After collecting " + ChatColor.GOLD + "10 souls" + ChatColor.GRAY + ", enter Reap for");
            lore.add(ChatColor.GRAY + "  " + ChatColor.GOLD + "10 seconds" + ChatColor.GRAY + ": double damage, improved");
            lore.add(ChatColor.GRAY + "  lifesteal, and aura of gathered souls.");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Each soul makes the blade heavier,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "yet the wielder lighter.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(SCYTHE_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            SOULREAP_SCYTHE.setItemMeta(meta);
        }
    }
}
