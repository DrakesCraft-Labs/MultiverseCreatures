package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class WheelCore {

    public static final NamespacedKey WHEEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_wheel_core");
    public static final ItemStack WHEEL_CORE = ItemBuilder.of(Material.MUSIC_DISC_OTHERSIDE)
            .name(MscText.title(GOLD, "Wheel Core"))
            .lore(
                    MscText.line(GRAY, "A fragment of the Eight-Handled Wheel,"),
                    MscText.line(GRAY, "turned into a core that spins toward"),
                    MscText.line(GRAY, "perfection and endless adaptation."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"That which adapts cannot break,"),
                    MscText.quote(DARK_PURPLE, "that which breaks cannot return.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(WHEEL_CORE_KEY)
            .build();
}
