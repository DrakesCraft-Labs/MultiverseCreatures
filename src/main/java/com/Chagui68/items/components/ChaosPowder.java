package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosPowder {

    public static final NamespacedKey CHAOS_POWDER_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_powder");
    public static final ItemStack CHAOS_POWDER = ItemBuilder.of(Material.ECHO_SHARD)
            .name(MscText.title(LIGHT_PURPLE, "Chaos Powder"))
            .lore(
                    MscText.line(GRAY, "A fine dust ground from a Chaos Orb,"),
                    MscText.line(GRAY, "still crackling with unstable energy."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "Order is a lie told"),
                    MscText.quote(DARK_PURPLE, "by the calm."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(CHAOS_POWDER_KEY)
            .build();
}
