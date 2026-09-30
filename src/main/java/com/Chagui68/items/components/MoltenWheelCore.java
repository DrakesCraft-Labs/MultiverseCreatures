package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MoltenWheelCore {

    public static final NamespacedKey MOLTEN_WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_molten_wheel_core");
    public static final ItemStack MOLTEN_WHEEL_CORE = ItemBuilder.of(Material.BLAZE_POWDER)
            .name(MscText.title(GOLD, "Molten Wheel Core"))
            .lore(
                    MscText.line(GRAY, "A Wheel Core held past its melting"),
                    MscText.line(GRAY, "point in the fires of a blast furnace,"),
                    MscText.line(GRAY, "burning like a captured ember."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Smelted only in the hottest fire,"),
                    MscText.quote(DARK_PURPLE, "it turns like the wheel it came from.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(MOLTEN_WHEEL_CORE_KEY)
            .build();
}