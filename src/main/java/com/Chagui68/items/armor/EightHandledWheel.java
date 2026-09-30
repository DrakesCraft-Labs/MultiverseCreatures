package com.Chagui68.items.armor;

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

public class EightHandledWheel {

    public static final NamespacedKey WHEEL_KEY = new NamespacedKey("multiversecreatures", "msc_eight_handled_wheel");
    public static final ItemStack EIGHT_HANDLED_WHEEL = new ItemStack(Material.NETHERITE_HELMET);

    public static final int MAX_CHARGES = 8;
    public static final int CHARGE_REGEN_TICKS = 300;
    public static final int BLOCK_DURATION_TICKS = 160;

    public static final String CHARGES_KEY = "msc_wheel_charges";
    public static final String BLOCKED_CAUSE_KEY = "msc_wheel_blocked_cause";
    public static final String BLOCK_UNTIL_KEY = "msc_wheel_block_until";
    public static final String NEXT_BLOCK_KEY = "msc_wheel_next_block";

    static {
        ItemMeta meta = EIGHT_HANDLED_WHEEL.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(WHITE, "Eight-Handled Wheel"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A crown carved from a fragment of the"));
            lore.add(MscText.line(GRAY, "Eight-Handled Wheel that once turned"));
            lore.add(MscText.line(GRAY, "against all harm."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Holds up to ", GOLD, "8 charges"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each charge regenerates over ", GOLD, "15 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Adaptation ", GRAY, "(Passive)"));
            lore.add(MscText.rich(GRAY, "  On receiving damage, consume ", GOLD, "1 charge ", GRAY, "to become"));
            lore.add(MscText.rich(GRAY, "  immune to that damage type for ", GOLD, "8 seconds", GRAY, "."));
            lore.add(MscText.line(GRAY, "  Multiple types in the same tick each spawn"));
            lore.add(MscText.line(GRAY, "  separate immunity effects."));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"That which adapts cannot break,"));
            lore.add(MscText.quote(DARK_PURPLE, "that which breaks cannot return.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(WHEEL_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            EIGHT_HANDLED_WHEEL.setItemMeta(meta);
        }
    }
}
