package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MagmaCore {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_magma_core");
    public static final ItemStack MAGMA_CORE = ItemBuilder.of(Material.MAGMA_CREAM)
            .name(MscText.title(GOLD, "Magma Core"))
            .lore(
                    MscText.line(GRAY, "A sphere of condensed flame, ripped"),
                    MscText.line(GRAY, "from the heart of a Flame Elemental."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It burns without fuel,"),
                    MscText.quote(DARK_PURPLE, "a sun that fits in the palm.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
