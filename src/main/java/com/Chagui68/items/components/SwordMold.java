package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SwordMold {

    public static final NamespacedKey SWORD_MOLD_KEY = new NamespacedKey("multiversecreatures", "msc_sword_mold");
    public static final ItemStack SWORD_MOLD = ItemBuilder.of(Material.IRON_HORSE_ARMOR)
            .name(MscText.title(GRAY, "Sword Mold"))
            .lore(
                    MscText.line(GRAY, "An iron template shaped like a blade,"),
                    MscText.line(GRAY, "ready to cast a venomous edge."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "Forged in the furnace,"),
                    MscText.quote(DARK_PURPLE, "cooled in the hunt."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(SWORD_MOLD_KEY)
            .build();
}
