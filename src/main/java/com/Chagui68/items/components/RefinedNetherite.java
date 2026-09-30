package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class RefinedNetherite {

    public static final NamespacedKey REFINED_NETHERITE_KEY = new NamespacedKey("multiversecreatures", "msc_refined_netherite");
    public static final ItemStack REFINED_NETHERITE = ItemBuilder.of(Material.NETHERITE_INGOT)
            .name(MscText.title(DARK_GRAY, "Refined Netherite"))
            .lore(
                    MscText.line(GRAY, "Netherite scrap pressed and reforged"),
                    MscText.line(GRAY, "into a flawless, denser alloy."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Blacker than night,"),
                    MscText.quote(DARK_PURPLE, "harder than resolve.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(REFINED_NETHERITE_KEY)
            .build();
}
