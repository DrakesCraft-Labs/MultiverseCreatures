package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class FrostHeart {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_frost_heart");
    public static final ItemStack FROST_HEART = ItemBuilder.of(Material.BLUE_ICE)
            .name(MscText.title(AQUA, "Frost Heart"))
            .lore(
                    MscText.line(GRAY, "A frozen core that never melts,"),
                    MscText.line(GRAY, "shattered from the chest of a Frost Golem."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It beats once a century,"),
                    MscText.quote(DARK_PURPLE, "and winter follows.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
