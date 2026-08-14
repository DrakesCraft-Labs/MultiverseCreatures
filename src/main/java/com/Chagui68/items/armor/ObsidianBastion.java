package com.Chagui68.items.armor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ObsidianBastion {

    public static final NamespacedKey HELMET_KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_bastion_helmet");
    public static final NamespacedKey CHEST_KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_bastion_chest");
    public static final NamespacedKey LEGS_KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_bastion_legs");
    public static final NamespacedKey BOOTS_KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_bastion_boots");

    public static final ItemStack HELMET = buildPiece(Material.NETHERITE_HELMET, HELMET_KEY, "Helm");
    public static final ItemStack CHESTPLATE = buildPiece(Material.NETHERITE_CHESTPLATE, CHEST_KEY, "Chestplate");
    public static final ItemStack LEGGINGS = buildPiece(Material.NETHERITE_LEGGINGS, LEGS_KEY, "Greaves");
    public static final ItemStack BOOTS = buildPiece(Material.NETHERITE_BOOTS, BOOTS_KEY, "Sabatons");

    public static final String SET_TAG = "MSC_ObsidianBastion";

    public static final double MAX_HEALTH_BONUS = 0.4;
    public static final double SPEED_PENALTY = 0.2;

    private static ItemStack buildPiece(Material mat, NamespacedKey key, String pieceName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Obsidian Bastion " + pieceName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Forged from obsidian stripped of an");
            lore.add(ChatColor.GRAY + "Obsidian Guard. Heavy. Nigh-unbreakable.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Set Bonus (full set):");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "+" + ChatColor.GREEN + "40% Max Health");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Full " + ChatColor.BLUE + "Knockback Resistance");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Immunity to fire and lava");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Movement Speed " + ChatColor.RED + "-20%");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Blacker than night,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "harder than resolve.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, true);
            meta.addEnchant(Enchantment.THORNS, 2, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            item.setItemMeta(meta);
        }
        return item;
    }
}
