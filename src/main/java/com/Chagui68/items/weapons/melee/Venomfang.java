package com.Chagui68.items.weapons.melee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class Venomfang {

    public static final NamespacedKey VENOMFANG_KEY = new NamespacedKey("multiversecreatures", "msc_venomfang");
    public static final ItemStack VENOMFANG = new ItemStack(Material.IRON_SWORD);

    public static final int POISON_DURATION_TICKS = 100;
    public static final int WITHER_DURATION_TICKS = 80;

    static {
        ItemMeta meta = VENOMFANG.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Venomfang");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A blade distilled from the corrosive");
            lore.add(ChatColor.GRAY + "venom of a Venom Witch.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each strike applies " + ChatColor.DARK_GREEN + "Poison I " + ChatColor.GRAY + "for " + ChatColor.GOLD + "5 seconds");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Each strike applies " + ChatColor.DARK_GRAY + "Wither I " + ChatColor.GRAY + "for " + ChatColor.GOLD + "4 seconds");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"One drop can dissolve");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "a man's resolve...\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(VENOMFANG_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            VENOMFANG.setItemMeta(meta);
        }
    }
}
