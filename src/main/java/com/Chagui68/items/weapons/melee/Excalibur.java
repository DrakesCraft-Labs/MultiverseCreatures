package com.Chagui68.items.weapons.melee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class Excalibur {

    public static final NamespacedKey EXCALIBUR_KEY = new NamespacedKey("multiversecreatures", "msc_excalibur_sword");
    public static final ItemStack EXCALIBUR_SWORD = new ItemStack(Material.NETHERITE_SWORD);

    static {
        ItemMeta meta = EXCALIBUR_SWORD.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Excalibur");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "The legendary blade of kings,");
            lore.add(ChatColor.GRAY + "forged from a fallen star's heart.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effect:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Grants " + ChatColor.GOLD + "Strength III" + ChatColor.GRAY + " while held");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Solar Flare " + ChatColor.GRAY + "(Right-Click)");
            lore.add(ChatColor.GRAY + "  Unleash a wave of radiant energy,");
            lore.add(ChatColor.GRAY + "  burning all enemies before you.");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Whosoever holds this sword,\"");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"if they be worthy, shall possess\"");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"the power of the Sun itself.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Avalon" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(EXCALIBUR_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            EXCALIBUR_SWORD.setItemMeta(meta);
        }
    }
}