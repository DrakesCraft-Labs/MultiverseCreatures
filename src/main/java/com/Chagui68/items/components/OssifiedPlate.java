package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class OssifiedPlate {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_ossified_plate");
    public static final ItemStack OSSIFIED_PLATE = ItemBuilder.of(Material.CALCITE)
            .name(MscText.title(WHITE, "Ossified Plate"))
            .lore(
                    MscText.line(GRAY, "A slab of calcite-bone laminate,"),
                    MscText.line(GRAY, "each layer marrow-set and hammered"),
                    MscText.line(GRAY, "flat until it rings like iron."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Bone, made unbreakable,"),
                    MscText.quote(DARK_PURPLE, "made patient, made a wall.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}