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

public class SkyfireTalisman {

    public static final NamespacedKey TALISMAN_KEY = new NamespacedKey("multiversecreatures", "msc_skyfire_talisman");
    public static final ItemStack SKYFIRE_TALISMAN = new ItemStack(Material.COPPER_INGOT);

    public static final long STRIKE_COOLDOWN_MS = 10000L;
    public static final double STRIKE_RANGE = 50.0;
    public static final double STRIKE_DAMAGE = 8.0;
    public static final double STRIKE_RADIUS = 3.0;

    public static final String COOLDOWN_KEY = "msc_skyfire_talisman_until";

    static {
        ItemMeta meta = SKYFIRE_TALISMAN.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(YELLOW, "Skyfire Talisman"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "An amulet of weathered copper, humming"));
            lore.add(MscText.line(GRAY, "with the lingering rage of a Storm Caller."));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Skyfire Strike ", GRAY, "(Right-Click Block)"));
            lore.add(MscText.line(GRAY, "  Call down a lightning bolt on the block"));
            lore.add(MscText.rich(GRAY, "  you are looking at, up to ", GOLD, "50 blocks", GRAY, " away."));
            lore.add(MscText.rich(GRAY, "  Enemies within ", GOLD, "3 blocks", GRAY, " of impact take"));
            lore.add(MscText.rich(GRAY, "  ", RED, "8 damage", GRAY, " and are briefly stunned."));
            lore.add(MscText.rich(GRAY, "  Cooldown: ", GOLD, "10 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effect:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Wielder is immune to lightning damage while held"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"The storm answers,"));
            lore.add(MscText.quote(DARK_PURPLE, "even when the sky is silent.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(TALISMAN_KEY, PersistentDataType.INTEGER, 1);
            SKYFIRE_TALISMAN.setItemMeta(meta);
        }
    }
}
