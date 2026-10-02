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
            .name(MscText.title(DARK_RED, "Filo del Verdugo"))
            .lore(
                    MscText.line(GRAY, "Un trozo del hacha de NIX, todavía"),
                    MscText.line(GRAY, "manchado con la sangre de sus condenados."),
                    MscText.blank(),
                    MscText.line(WHITE, "Ingrediente de Crafteo"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Forja la ", DARK_RED, "Guillotina del Verdugo"),
                    MscText.blank(),
                    MscText.quote(DARK_RED, "\"La sentencia ya fue dictada.\""),
                    MscText.blank(),
                    MscText.footer(DARK_RED, "NIX")
            )
            .tagged(KEY)
            .build();
}
