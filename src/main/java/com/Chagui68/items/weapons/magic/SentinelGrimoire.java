package com.Chagui68.items.weapons.magic;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.format.NamedTextColor.*;
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
            meta.displayName(MscText.title(YELLOW, "Sentinel Grimoire"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A forbidden tome bound with the"));
            lore.add(MscText.line(GRAY, "leather of a fallen Sentinel."));
            lore.add(MscText.line(GRAY, "It burns with multiversal power."));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Spells ", WHITE, "(Right-Click to cast)"));
            lore.add(MscText.rich(GRAY, "  1. ", RED, "Blazing Pentagram"));
            lore.add(MscText.rich(GRAY, "  2. ", YELLOW, "Lance Rain"));
            lore.add(MscText.rich(GRAY, "  3. ", GOLD, "Divine Judgment"));
            lore.add(MscText.rich(GRAY, "  4. ", DARK_RED, "Executioner's Mark"));
            lore.add(MscText.rich(GRAY, "  5. ", LIGHT_PURPLE, "Singular Vortex"));
            lore.add(MscText.rich(GRAY, "  6. ", GOLD, "Earthquake"));
            lore.add(MscText.rich(GRAY, "  7. ", AQUA, "Celestial Bulwark"));
            lore.add(MscText.rich(GRAY, "  8. ", YELLOW, "Sentinel Aura"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(WHITE, "Shift + Right-Click: ", GRAY, "change spell page"));
            lore.add(MscText.line(GRAY, "The action bar shows the selected page."));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Every universe answers"));
            lore.add(MscText.quote(DARK_PURPLE, "to the one who reads.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(GRIMOIRE_KEY, PersistentDataType.INTEGER, 1);
            GRIMOIRE.setItemMeta(meta);
        }
    }

    private SentinelGrimoire() {
    }
}
