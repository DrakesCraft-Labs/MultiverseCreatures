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

public class Venomfang {

    public static final NamespacedKey VENOMFANG_KEY = new NamespacedKey("multiversecreatures", "msc_venomfang");
    public static final ItemStack VENOMFANG = new ItemStack(Material.IRON_SWORD);

    public static final int POISON_DURATION_TICKS = 100;
    public static final int WITHER_DURATION_TICKS = 80;

    static {
        ItemMeta meta = VENOMFANG.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(DARK_GREEN, "Venomfang"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A blade distilled from the corrosive"));
            lore.add(MscText.line(GRAY, "venom of a Venom Witch."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each strike applies ", DARK_GREEN, "Poison I ", GRAY, "for ", GOLD, "5 seconds"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Each strike applies ", DARK_GRAY, "Wither I ", GRAY, "for ", GOLD, "4 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"One drop can dissolve"));
            lore.add(MscText.quote(DARK_PURPLE, "a man's resolve...\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(VENOMFANG_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            VENOMFANG.setItemMeta(meta);
        }
    }
}
