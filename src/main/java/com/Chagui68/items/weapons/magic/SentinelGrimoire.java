package com.Chagui68.items.weapons.magic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SentinelGrimoire {

    public static final NamespacedKey GRIMOIRE_KEY = new NamespacedKey("multiversecreatures", "msc_sentinel_grimoire");
    public static final NamespacedKey PAGE_KEY = new NamespacedKey("multiversecreatures", "msc_grimoire_page");
    public static final String COOLDOWN_KEY_PREFIX = "msc_grimoire_cd_";

    public static final ItemStack GRIMOIRE = new ItemStack(Material.ENCHANTED_BOOK);

    public enum GrimoireSpell {
        BLAZING_PENTAGRAM(1, "Blazing Pentagram", "blazing-pentagram", 8, 10.0),
        LANCE_RAIN(2, "Lance Rain", "lance-rain", 7, 12.0),
        DIVINE_JUDGMENT(3, "Divine Judgment", "divine-judgment", 10, 18.0),
        EXECUTIONERS_MARK(4, "Executioner's Mark", "executioners-mark", 10, 14.0),
        SINGULAR_VORTEX(5, "Singular Vortex", "singular-vortex", 15, 8.0),
        EARTHQUAKE(6, "Earthquake", "earthquake", 9, 10.0),
        CELESTIAL_BULWARK(7, "Celestial Bulwark", "celestial-bulwark", 20, 0.0),
        SENTINEL_AURA(8, "Sentinel Aura", "sentinel-aura", 45, 0.0);

        private final int page;
        private final String display;
        private final String configKey;
        private final int cooldownSeconds;
        private final double defaultDamage;

        GrimoireSpell(int page, String display, String configKey, int cooldownSeconds, double defaultDamage) {
            this.page = page;
            this.display = display;
            this.configKey = configKey;
            this.cooldownSeconds = cooldownSeconds;
            this.defaultDamage = defaultDamage;
        }

        public int getPage() {
            return page;
        }

        public String getDisplay() {
            return display;
        }

        public String getConfigKey() {
            return configKey;
        }

        public int getCooldownSeconds() {
            return cooldownSeconds;
        }

        public double getDefaultDamage() {
            return defaultDamage;
        }

        public static GrimoireSpell byPage(int page) {
            for (GrimoireSpell spell : values()) {
                if (spell.page == page) {
                    return spell;
                }
            }
            return BLAZING_PENTAGRAM;
        }
    }

    static {
        ItemMeta meta = GRIMOIRE.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Sentinel Grimoire");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A forbidden tome bound with the");
            lore.add(ChatColor.GRAY + "leather of a fallen Sentinel.");
            lore.add(ChatColor.GRAY + "It burns with multiversal power.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Spells " + ChatColor.WHITE + "(Right-Click to cast)");
            lore.add(ChatColor.GRAY + "  1. " + ChatColor.RED + "Blazing Pentagram");
            lore.add(ChatColor.GRAY + "  2. " + ChatColor.YELLOW + "Lance Rain");
            lore.add(ChatColor.GRAY + "  3. " + ChatColor.GOLD + "Divine Judgment");
            lore.add(ChatColor.GRAY + "  4. " + ChatColor.DARK_RED + "Executioner's Mark");
            lore.add(ChatColor.GRAY + "  5. " + ChatColor.LIGHT_PURPLE + "Singular Vortex");
            lore.add(ChatColor.GRAY + "  6. " + ChatColor.GOLD + "Earthquake");
            lore.add(ChatColor.GRAY + "  7. " + ChatColor.AQUA + "Celestial Bulwark");
            lore.add(ChatColor.GRAY + "  8. " + ChatColor.YELLOW + "Sentinel Aura");
            lore.add("");
            lore.add(ChatColor.WHITE + "Shift + Right-Click: " + ChatColor.GRAY + "change spell page");
            lore.add(ChatColor.GRAY + "The action bar shows the selected page.");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Every universe answers");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "to the one who reads.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(GRIMOIRE_KEY, PersistentDataType.INTEGER, 1);
            GRIMOIRE.setItemMeta(meta);
        }
    }

    private SentinelGrimoire() {
    }
}
