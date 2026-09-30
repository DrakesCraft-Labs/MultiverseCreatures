package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class WheelEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_wheel_essence");
    public static final ItemStack WHEEL_ESSENCE = ItemBuilder.of(Material.NETHERITE_SCRAP)
            .name(MscText.title(WHITE, "Wheel Essence"))
            .lore(
                    MscText.line(GRAY, "A fragment of the Eight-Handled"),
                    MscText.line(GRAY, "Wheel, severed from a fallen Mahoraga."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"That which adapts cannot break,"),
                    MscText.quote(DARK_PURPLE, "that which breaks cannot return.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
