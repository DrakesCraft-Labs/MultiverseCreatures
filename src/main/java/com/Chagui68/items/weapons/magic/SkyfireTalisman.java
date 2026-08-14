package com.Chagui68.items.weapons.magic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SkyfireTalisman {

    public static final NamespacedKey TALISMAN_KEY = new NamespacedKey("multiversecreatures", "msc_skyfire_talisman");
    public static final ItemStack SKYFIRE_TALISMAN = new ItemStack(Material.COPPER_INGOT);

    public static final long STRIKE_COOLDOWN_MS = 10000L;
    public static final double STRIKE_RANGE = 50.0;
    public static final double STRIKE_DAMAGE = 8.0;
    public static final double STRIKE_RADIUS = 3.0;

    public static final String COOLDOWN_KEY = "msc_skyfire_talisman_until";

    static {
        ItemMeta meta = SKYFIRE_TALISMAN.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Skyfire Talisman");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "An amulet of weathered copper, humming");
            lore.add(ChatColor.GRAY + "with the lingering rage of a Storm Caller.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Item Ability: " + ChatColor.WHITE + "Skyfire Strike " + ChatColor.GRAY + "(Right-Click Block)");
            lore.add(ChatColor.GRAY + "  Call down a lightning bolt on the block");
            lore.add(ChatColor.GRAY + "  you are looking at, up to " + ChatColor.GOLD + "50 blocks" + ChatColor.GRAY + " away.");
            lore.add(ChatColor.GRAY + "  Enemies within " + ChatColor.GOLD + "3 blocks" + ChatColor.GRAY + " of impact take");
            lore.add(ChatColor.GRAY + "  " + ChatColor.RED + "8 damage" + ChatColor.GRAY + " and are briefly stunned.");
            lore.add(ChatColor.GRAY + "  Cooldown: " + ChatColor.GOLD + "10 seconds");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effect:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Wielder is immune to lightning damage while held");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"The storm answers,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "even when the sky is silent.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(TALISMAN_KEY, PersistentDataType.INTEGER, 1);
            SKYFIRE_TALISMAN.setItemMeta(meta);
        }
    }
}
