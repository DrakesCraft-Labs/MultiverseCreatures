package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class VenomGland {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_venom_gland");
    public static final ItemStack VENOM_GLAND = ItemBuilder.of(Material.SPIDER_EYE)
            .name(MscText.title(DARK_GREEN, "Venom Gland"))
            .lore(
                    MscText.line(GRAY, "A pulsating sac of corrosive venom,"),
                    MscText.line(GRAY, "harvested from a Venom Witch."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"One drop can dissolve"),
                    MscText.quote(DARK_PURPLE, "a man's resolve...\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
