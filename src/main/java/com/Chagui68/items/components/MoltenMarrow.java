package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenMarrow {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_molten_marrow");
    public static final ItemStack MOLTEN_MARROW = ItemBuilder.of(Material.REDSTONE)
            .name(MscText.title(GOLD, "Molten Marrow"))
            .lore(
                    MscText.line(GRAY, "An Ossified Plate held past its"),
                    MscText.line(GRAY, "melting point in the fires of a"),
                    MscText.line(GRAY, "blast furnace, glowing like hot blood."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Only the hottest fire can make"),
                    MscText.quote(DARK_PURPLE, "bone remember it was alive.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}