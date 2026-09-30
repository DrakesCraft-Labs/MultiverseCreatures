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

public class NullshearEdge {

    public static final NamespacedKey NULL_KEY = new NamespacedKey("multiversecreatures", "msc_nullshear_edge");
    public static final ItemStack NULLSHEAR_EDGE = new ItemStack(Material.NETHERITE_SWORD);

    public static final double VOID_FRACTION = 0.3;
    public static final int DARKNESS_DURATION_TICKS = 100;
    public static final double DARKNESS_CHANCE = 0.1;

    public static final long VOID_BLINK_COOLDOWN_MS = 20000L;
    public static final double VOID_BLINK_RANGE = 30.0;

    public static final String BLINK_COOLDOWN_KEY = "msc_nullshear_blink_until";

    static {
        ItemMeta meta = NULLSHEAR_EDGE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(DARK_PURPLE, "Nullshear Edge"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A blade that cuts the seam between"));
            lore.add(MscText.line(GRAY, "the world and the nothing behind it."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each strike inflicts ", RED, "30% ", GRAY, "of damage as"));
            lore.add(MscText.rich(DARK_PURPLE, "    void damage ", GRAY, "(ignores armor)"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Striking outdoors has a ", GOLD, "10% ", GRAY, "chance"));
            lore.add(MscText.rich(GRAY, "    to apply ", DARK_GRAY, "Darkness ", GRAY, "for ", GOLD, "5 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Void Blink ", GRAY, "(Shift + Right-Click)"));
            lore.add(MscText.line(GRAY, "  Shear through space, teleporting to the"));
            lore.add(MscText.rich(GRAY, "  block you are looking at (up to ", GOLD, "30 blocks", GRAY, ")."));
            lore.add(MscText.rich(GRAY, "  Cooldown: ", GOLD, "20 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"It is not there,"));
            lore.add(MscText.quote(DARK_PURPLE, "and yet it is.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(NULL_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            NULLSHEAR_EDGE.setItemMeta(meta);
        }
    }
}
