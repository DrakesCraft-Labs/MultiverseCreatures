package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenNetherite {

    public static final NamespacedKey MOLTEN_NETHERITE_KEY = new NamespacedKey("multiversecreatures", "msc_molten_netherite");
    public static final ItemStack MOLTEN_NETHERITE = ItemBuilder.of(Material.ANCIENT_DEBRIS)
            .name(MscText.title(DARK_GRAY, "Molten Netherite"))
            .lore(
                    MscText.line(GRAY, "Refined Netherite reduced to flowing"),
                    MscText.line(GRAY, "darkness in the same crucible that"),
                    MscText.line(GRAY, "melts the wheel."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"What burns twice holds"),
                    MscText.quote(DARK_PURPLE, "twice the weight.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(MOLTEN_NETHERITE_KEY)
            .build();
}