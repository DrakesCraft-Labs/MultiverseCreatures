package com.Chagui68.items.misc.offhand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class FrostHeartOffhand {

    public static final NamespacedKey FROST_KEY = new NamespacedKey("multiversecreatures", "msc_frost_heart_offhand");
    public static final ItemStack FROST_HEART_OFFHAND = new ItemStack(Material.LIGHT_BLUE_DYE);

    public static final int CHILL_TICKS = 60;
    public static final int CHILL_AMPLIFIER_SLOW = 1;
    public static final int CHILL_AMPLIFIER_WEAK = 0;
    public static final int FROST_RADIUS = 4;

    static {
        ItemMeta meta = FROST_HEART_OFFHAND.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Frost Heart");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A frozen core pulsed from a Frost");
            lore.add(ChatColor.GRAY + "Golem's chest. Only the off-hand");
            lore.add(ChatColor.GRAY + "can steady its endless chill.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects (off-hand only):");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Melee attackers are chilled:");
            lore.add(ChatColor.GRAY + "    Slowness II and Weakness I for 3s");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Enemies within " + ChatColor.AQUA + "4 blocks" + ChatColor.GRAY + " are slowed");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Wielder gains " + ChatColor.AQUA + "Frost Walker I" + ChatColor.GRAY + " while held");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"It beats once a century,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "and winter follows.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(FROST_KEY, PersistentDataType.INTEGER, 1);
            FROST_HEART_OFFHAND.setItemMeta(meta);
        }
    }
}
