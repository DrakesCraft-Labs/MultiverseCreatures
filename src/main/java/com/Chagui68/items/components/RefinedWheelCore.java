package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class RefinedWheelCore {

    public static final NamespacedKey REFINED_WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_refined_wheel_core");
    public static final ItemStack REFINED_WHEEL_CORE = ItemBuilder.of(Material.MUSIC_DISC_OTHERSIDE)
            .name(MscText.title(GOLD, "Refined Wheel Core"))
            .lore(
                    MscText.line(GRAY, "Molten wheel and molten netherite,"),
                    MscText.line(GRAY, "poured into each other until the two"),
                    MscText.line(GRAY, "turn as one."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"The wheel that adapts to all,"),
                    MscText.quote(DARK_PURPLE, "forged to break what breaks it.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(REFINED_WHEEL_CORE_KEY)
            .build();
}