package com.Chagui68.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemCombatTrades {
    public static final NamespacedKey EXCALIBUR_KEY = new NamespacedKey("multiversecreatures",
            "msc_excalibur_sword");
    public static final ItemStack EXCALIBUR_SWORD = new ItemStack(Material.NETHERITE_SWORD);

    static {
        ItemMeta meta = EXCALIBUR_SWORD.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Excalibur Sword");

            List<String> lore = new ArrayList<>();
            lore.add("§7§oTHE REAL SWORD");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(EXCALIBUR_KEY, PersistentDataType.INTEGER, 1);
            EXCALIBUR_SWORD.setItemMeta(meta);
        }
    }
}