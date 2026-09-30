package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SentinelCore {

    public static final NamespacedKey SENTINEL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_sentinel_core");
    public static final ItemStack SENTINEL_CORE = ItemBuilder.of(Material.HEART_OF_THE_SEA)
            .name(MscText.title(DARK_PURPLE, "Sentinel Core"))
            .lore(
                    MscText.line(GRAY, "The still-beating heart of the"),
                    MscText.line(GRAY, "Obsidian Sentinel, harvested before"),
                    MscText.line(GRAY, "the lightning could claim it."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It watched over every phase."),
                    MscText.quote(DARK_PURPLE, "Now it watches over none.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(SENTINEL_CORE_KEY)
            .build();
}