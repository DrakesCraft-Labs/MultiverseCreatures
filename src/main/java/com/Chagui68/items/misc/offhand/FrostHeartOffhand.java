package com.Chagui68.items.misc.offhand;

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
            meta.displayName(MscText.title(AQUA, "Frost Heart"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A frozen core pulsed from a Frost"));
            lore.add(MscText.line(GRAY, "Golem's chest. Only the off-hand"));
            lore.add(MscText.line(GRAY, "can steady its endless chill."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects (off-hand only):"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Melee attackers are chilled:"));
            lore.add(MscText.line(GRAY, "    Slowness II and Weakness I for 3s"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Enemies within ", AQUA, "4 blocks", GRAY, " are slowed"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Wielder gains ", AQUA, "Frost Walker I", GRAY, " while held"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"It beats once a century,"));
            lore.add(MscText.quote(DARK_PURPLE, "and winter follows.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(FROST_KEY, PersistentDataType.INTEGER, 1);
            FROST_HEART_OFFHAND.setItemMeta(meta);
        }
    }
}
