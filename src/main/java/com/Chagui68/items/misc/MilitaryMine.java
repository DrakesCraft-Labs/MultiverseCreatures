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

public class MilitaryMine {

    public static final NamespacedKey MINE_KEY = new NamespacedKey("multiversecreatures", "msc_military_mine");
    public static final ItemStack MILITARY_MINE = new ItemStack(Material.TNT);

    static {
        ItemMeta meta = MILITARY_MINE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(RED, "Military Mine"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A crafted explosive device"));
            lore.add(MscText.line(GRAY, "used for battlefield traps."));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"One step is all it takes.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Military"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(MINE_KEY, PersistentDataType.INTEGER, 1);
            MILITARY_MINE.setItemMeta(meta);
        }
    }
}
