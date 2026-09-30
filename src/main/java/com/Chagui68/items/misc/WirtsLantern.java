package com.Chagui68.items.misc;

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

public class WirtsLantern {

    public static final NamespacedKey WIRTS_LANTERN_KEY = new NamespacedKey("multiversecreatures", "msc_wirts_lantern");
    public static final ItemStack WIRTS_LANTERN = new ItemStack(Material.SOUL_LANTERN);

    static {
        ItemMeta meta = WIRTS_LANTERN.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(DARK_PURPLE, "Wirt's Lantern"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A lantern that holds a lost soul."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Repels hostile mobs in a radius"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"The flame knows no winter.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Khand"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(WIRTS_LANTERN_KEY, PersistentDataType.INTEGER, 1);
            WIRTS_LANTERN.setItemMeta(meta);
        }
    }
}