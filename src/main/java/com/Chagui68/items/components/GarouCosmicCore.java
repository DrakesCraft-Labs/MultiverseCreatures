package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/** What Garou leaves behind when he falls: a trophy of the miniboss. */
public class GarouCosmicCore {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_garou_cosmic_core");

    public static final ItemStack GAROU_COSMIC_CORE = ItemBuilder.of(Material.NETHER_STAR)
            .name(MscText.title(DARK_PURPLE, "Garou Cosmic Core"))
            .lore(
                    MscText.line(GRAY, "A fragment of primordial martial"),
                    MscText.line(GRAY, "power, torn from the Hero Hunter."),
                    MscText.blank(),
                    MscText.line(WHITE, "Boss Trophy"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Dropped by ", DARK_PURPLE, "Garou", GRAY, " when he falls"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"I am the monster that hunts heroes.\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .build();
}
