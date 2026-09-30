package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class VoidEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_void_essence");
    public static final ItemStack VOID_ESSENCE = ItemBuilder.of(Material.ENDER_EYE)
            .name(MscText.title(DARK_PURPLE, "Void Essence"))
            .lore(
                    MscText.line(GRAY, "A droplet of un-space, wrung from"),
                    MscText.line(GRAY, "the dissolving husk of a Void Crawler."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It is not there,"),
                    MscText.quote(DARK_PURPLE, "and yet it is.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
