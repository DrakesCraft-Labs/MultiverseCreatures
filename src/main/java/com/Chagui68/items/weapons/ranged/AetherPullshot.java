package com.Chagui68.items.weapons.ranged;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AetherPullshot {

    public static final NamespacedKey PULLSHOT_KEY = new NamespacedKey("multiversecreatures", "msc_aether_pullshot");
    public static final ItemStack AETHER_PULLSHOT = new ItemStack(Material.TRIDENT);

    public static final long PULL_COOLDOWN_MS = 30000L;
    public static final double PULL_RANGE = 40.0;
    public static final int PULL_DURATION_TICKS = 60;
    public static final double PULL_SPEED = 0.5;
    public static final double PULL_INITIAL_DAMAGE = 6.0;
    public static final double PULL_FINAL_DAMAGE = 10.0;

    public static final String COOLDOWN_KEY = "msc_aether_pullshot_until";

    static {
        ItemMeta meta = AETHER_PULLSHOT.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Aether Pullshot");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A trident forged from an Ender");
            lore.add(ChatColor.GRAY + "Fragment, strung with a leash of");
            lore.add(ChatColor.GRAY + "threadbare space.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Aether Pull " + ChatColor.GRAY + "(Right-Click Entity)");
            lore.add(ChatColor.GRAY + "  Strike a target up to " + ChatColor.GOLD + "40 blocks " + ChatColor.GRAY + "away.");
            lore.add(ChatColor.GRAY + "  The struck enemy is " + ChatColor.BLUE + "pulled toward you " + ChatColor.GRAY + "over");
            lore.add(ChatColor.GRAY + "  " + ChatColor.GOLD + "3 seconds" + ChatColor.GRAY + ", taking " + ChatColor.RED + "6 initial damage");
            lore.add(ChatColor.GRAY + "  and " + ChatColor.RED + "10 damage " + ChatColor.GRAY + "if pulled all the way in.");
            lore.add(ChatColor.GRAY + "  Cooldown: " + ChatColor.GOLD + "30 seconds");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"A leash not of rope,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "but of distance denied.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(PULLSHOT_KEY, PersistentDataType.INTEGER, 1);
            meta.addEnchant(Enchantment.LOYALTY, 3, true);
            meta.setUnbreakable(true);
            AETHER_PULLSHOT.setItemMeta(meta);
        }
    }
}
