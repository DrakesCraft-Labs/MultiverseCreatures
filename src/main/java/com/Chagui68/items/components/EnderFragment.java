package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class EnderFragment {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_ender_fragment");
    public static final ItemStack ENDER_FRAGMENT = ItemBuilder.of(Material.ENDER_PEARL)
            .name(MscText.title(DARK_AQUA, "Ender Fragment"))
            .lore(
                    MscText.line(GRAY, "A splinter of an End knight's pearl,"),
                    MscText.line(GRAY, "still humming with the spaces between."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"A step taken sideways"),
                    MscText.quote(DARK_PURPLE, "across the veil.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
