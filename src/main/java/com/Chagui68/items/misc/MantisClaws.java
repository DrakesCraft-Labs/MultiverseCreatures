package com.Chagui68.items.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MantisClaws {

    public static final NamespacedKey MANTIS_CLAWS_KEY = new NamespacedKey("multiversecreatures", "msc_mantis_claws");
    public static final ItemStack MANTIS_CLAWS_ITEM = new ItemStack(Material.SHEARS);

    static {
        ItemMeta meta = MANTIS_CLAWS_ITEM.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Mantis Claws");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Claws forged from the silk and iron");
            lore.add(ChatColor.GRAY + "of Deepnest.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Abilities:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Shift to cling to walls");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Space to leap upward");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"The mantis lords watch from above.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Hallownest" + ChatColor.DARK_GRAY + " ✦");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(MANTIS_CLAWS_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            meta.setCustomModelData(1002);
            MANTIS_CLAWS_ITEM.setItemMeta(meta);
        }
    }
}
