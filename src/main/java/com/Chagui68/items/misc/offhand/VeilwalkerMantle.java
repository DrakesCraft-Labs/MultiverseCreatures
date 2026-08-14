package com.Chagui68.items.misc.offhand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class VeilwalkerMantle {

    public static final NamespacedKey VEIL_KEY = new NamespacedKey("multiversecreatures", "msc_veilwalker_mantle");
    public static final ItemStack VEILWALKER_MANTLE = new ItemStack(Material.CLOCK);

    public static final long STEALTH_COOLDOWN_MS = 30000L;
    public static final int STEALTH_DURATION_TICKS = 200;
    public static final double BACKSTAB_DAMAGE_MULTIPLIER = 1.5;

    public static final String STEALTH_TAG = "MSC_VeilMantle_Stealth";

    static {
        ItemMeta meta = VEILWALKER_MANTLE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Veilwalker Mantle");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A chronomantic pocket-watch torn");
            lore.add(ChatColor.GRAY + "from the shadow of a Rogue. Its");
            lore.add(ChatColor.GRAY + "ticking bends both light and time.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Item Ability: " + ChatColor.AQUA + "Step Through " + ChatColor.GRAY + "(Right-Click Air)");
            lore.add(ChatColor.GRAY + "  Conceal the wearer for " + ChatColor.GOLD + "10 seconds" + ChatColor.GRAY + ",");
            lore.add(ChatColor.GRAY + "  granting Invisibility and Speed I.");
            lore.add(ChatColor.GRAY + "  Cooldown: " + ChatColor.GOLD + "30s");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effect:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "First strike from stealth deals " + ChatColor.RED + "+50% damage");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Time stops where I tread,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "and the world forgets my name.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(VEIL_KEY, PersistentDataType.INTEGER, 1);
            VEILWALKER_MANTLE.setItemMeta(meta);
        }
    }
}
