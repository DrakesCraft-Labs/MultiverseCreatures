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

public class VeilwalkerMantle {

    public static final NamespacedKey VEIL_KEY = new NamespacedKey("multiversecreatures", "msc_veilwalker_mantle");
    public static final ItemStack VEILWALKER_MANTLE = new ItemStack(Material.CLOCK);

    public static final long STEALTH_COOLDOWN_MS = 30000L;
    public static final int STEALTH_DURATION_TICKS = 200;
    public static final double BACKSTAB_DAMAGE_MULTIPLIER = 1.5;

    public static final String STEALTH_TAG = "MSC_VeilMantle_Stealth";

    static {
        ItemMeta meta = VEILWALKER_MANTLE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(DARK_GRAY, "Veilwalker Mantle"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A chronomantic pocket-watch torn"));
            lore.add(MscText.line(GRAY, "from the shadow of a Rogue. Its"));
            lore.add(MscText.line(GRAY, "ticking bends both light and time."));
            lore.add(MscText.blank());
            lore.add(MscText.rich(WHITE, "Item Ability: ", AQUA, "Step Through ", GRAY, "(Right-Click Air)"));
            lore.add(MscText.rich(GRAY, "  Conceal the wearer for ", GOLD, "10 seconds", GRAY, ","));
            lore.add(MscText.line(GRAY, "  granting Invisibility and Speed I."));
            lore.add(MscText.rich(GRAY, "  Cooldown: ", GOLD, "30s"));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effect:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "First strike from stealth deals ", RED, "+50% damage"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Time stops where I tread,"));
            lore.add(MscText.quote(DARK_PURPLE, "and the world forgets my name.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(VEIL_KEY, PersistentDataType.INTEGER, 1);
            VEILWALKER_MANTLE.setItemMeta(meta);
        }
    }
}
