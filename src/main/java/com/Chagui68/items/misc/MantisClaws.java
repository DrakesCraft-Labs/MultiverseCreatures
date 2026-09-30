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

public class MantisClaws {

    public static final NamespacedKey MANTIS_CLAWS_KEY = new NamespacedKey("multiversecreatures", "msc_mantis_claws");
    public static final ItemStack MANTIS_CLAWS_ITEM = new ItemStack(Material.SHEARS);

    static {
        ItemMeta meta = MANTIS_CLAWS_ITEM.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(GOLD, "Mantis Claws"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "Claws forged from the silk and iron"));
            lore.add(MscText.line(GRAY, "of Deepnest."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Abilities:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Shift to cling to walls"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Space to leap upward"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"The mantis lords watch from above.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Hallownest"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(MANTIS_CLAWS_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            meta.setCustomModelData(1002);
            MANTIS_CLAWS_ITEM.setItemMeta(meta);
        }
    }
}
