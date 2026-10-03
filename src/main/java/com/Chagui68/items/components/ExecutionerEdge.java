package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * A shard of NIX's axe, dropped when the Executioner falls: the blade of the Executioner's
 * Guillotine.
 */
public class ExecutionerEdge {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_executioner_edge");

    public static final ItemStack EXECUTIONER_EDGE = ItemBuilder.of(Material.NETHERITE_SCRAP)
            .name(MscText.title(DARK_RED, "Executioner's Edge"))
            .lore(
                    MscText.line(GRAY, "A shard of NIX's axe, still stained"),
                    MscText.line(GRAY, "with the blood of the condemned."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Forges the ", DARK_RED, "Executioner's Guillotine"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"The sentence has already been passed.\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .build();
}
