package com.Chagui68.items.weapons.melee;

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

public class SoulreapScythe {

    public static final NamespacedKey SCYTHE_KEY = new NamespacedKey("multiversecreatures", "msc_soulreap_scythe");
    public static final ItemStack SOULREAP_SCYTHE = new ItemStack(Material.NETHERITE_HOE);

    public static final int LIFESTEAL_HIT = 2;
    public static final int SOULS_REQUIRED = 10;
    public static final int REAP_DURATION_TICKS = 200;
    public static final double REAP_DAMAGE_MULTIPLIER = 2.0;

    public static final String SOUL_COUNTER_KEY = "msc_soulreap_counter";
    public static final String REAP_ACTIVE_KEY = "msc_soulreap_reap_until";

    static {
        ItemMeta meta = SOULREAP_SCYTHE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(BLACK, "Soulreap Scythe"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A curved void-steel blade humming"));
            lore.add(MscText.line(GRAY, "with the lament of the unreaped."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each strike drains ", RED, "4 HP", GRAY, " and heals the wielder ", GREEN, "2 HP"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each strike collects a soul"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Reap ", GRAY, "(Passive)"));
            lore.add(MscText.rich(GRAY, "  After collecting ", GOLD, "10 souls", GRAY, ", enter Reap for"));
            lore.add(MscText.rich(GRAY, "  ", GOLD, "10 seconds", GRAY, ": double damage, improved"));
            lore.add(MscText.line(GRAY, "  lifesteal, and aura of gathered souls."));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Each soul makes the blade heavier,"));
            lore.add(MscText.quote(DARK_PURPLE, "yet the wielder lighter.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(SCYTHE_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            SOULREAP_SCYTHE.setItemMeta(meta);
        }
    }
}
