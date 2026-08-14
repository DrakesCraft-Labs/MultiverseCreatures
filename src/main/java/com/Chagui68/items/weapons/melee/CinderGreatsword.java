package com.Chagui68.items.weapons.melee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CinderGreatsword {

    public static final NamespacedKey CINDER_KEY = new NamespacedKey("multiversecreatures", "msc_cinder_greatsword");
    public static final ItemStack CINDER_GREATSWORD = new ItemStack(Material.NETHERITE_SWORD);

    public static final long SLAM_COOLDOWN_MS = 10000L;
    public static final double SLAM_RADIUS = 5.0;
    public static final double SLAM_DAMAGE = 12.0;
    public static final int SLAM_FIRE_TICKS = 80;

    static {
        ItemMeta meta = CINDER_GREATSWORD.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Cinder Greatsword");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A massive blade forged from the heart");
            lore.add(ChatColor.GRAY + "of a Flame Elemental. Too heavy to");
            lore.add(ChatColor.GRAY + "wield alongside a second weapon.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Two-handed: cannot pair with off-hand items");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Sets struck foes ablaze (Fire Aspect II)");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Wielder gains " + ChatColor.GOLD + "Fire Resistance" + ChatColor.GRAY + " while held");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Cinder Slam " + ChatColor.GRAY + "(Right-Click)");
            lore.add(ChatColor.GRAY + "  Channel flame into the blade and slam");
            lore.add(ChatColor.GRAY + "  the ground, igniting all enemies in a");
            lore.add(ChatColor.GRAY + "  " + ChatColor.GOLD + "5-block" + ChatColor.GRAY + " radius. Cooldown: " + ChatColor.GOLD + "10s");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Where it falls, the world burns.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(CINDER_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            CINDER_GREATSWORD.setItemMeta(meta);
        }
    }
}
