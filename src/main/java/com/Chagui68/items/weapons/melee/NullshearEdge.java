package com.Chagui68.items.weapons.melee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class NullshearEdge {

    public static final NamespacedKey NULL_KEY = new NamespacedKey("multiversecreatures", "msc_nullshear_edge");
    public static final ItemStack NULLSHEAR_EDGE = new ItemStack(Material.NETHERITE_SWORD);

    public static final double VOID_FRACTION = 0.3;
    public static final int DARKNESS_DURATION_TICKS = 100;
    public static final double DARKNESS_CHANCE = 0.1;

    public static final long VOID_BLINK_COOLDOWN_MS = 20000L;
    public static final double VOID_BLINK_RANGE = 30.0;

    public static final String BLINK_COOLDOWN_KEY = "msc_nullshear_blink_until";

    static {
        ItemMeta meta = NULLSHEAR_EDGE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Nullshear Edge");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A blade that cuts the seam between");
            lore.add(ChatColor.GRAY + "the world and the nothing behind it.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each strike inflicts " + ChatColor.RED + "30% " + ChatColor.GRAY + "of damage as");
            lore.add(ChatColor.DARK_PURPLE + "    void damage " + ChatColor.GRAY + "(ignores armor)");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Striking outdoors has a " + ChatColor.GOLD + "10% " + ChatColor.GRAY + "chance");
            lore.add(ChatColor.GRAY + "    to apply " + ChatColor.DARK_GRAY + "Darkness " + ChatColor.GRAY + "for " + ChatColor.GOLD + "5 seconds");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Void Blink " + ChatColor.GRAY + "(Shift + Right-Click)");
            lore.add(ChatColor.GRAY + "  Shear through space, teleporting to the");
            lore.add(ChatColor.GRAY + "  block you are looking at (up to " + ChatColor.GOLD + "30 blocks" + ChatColor.GRAY + ").");
            lore.add(ChatColor.GRAY + "  Cooldown: " + ChatColor.GOLD + "20 seconds");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It is not there,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "and yet it is.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(NULL_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            NULLSHEAR_EDGE.setItemMeta(meta);
        }
    }
}
