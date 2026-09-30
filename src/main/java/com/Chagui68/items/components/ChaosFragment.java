package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosFragment {

    public static final NamespacedKey CHAOS_FRAGMENT_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_fragment");
    public static final ItemStack CHAOS_FRAGMENT = ItemBuilder.of(Material.AMETHYST_SHARD)
            .name(MscText.title(LIGHT_PURPLE, "Chaos Fragment"))
            .lore(
                    MscText.line(GRAY, "Crystallized shards of compressed chaos,"),
                    MscText.line(GRAY, "far more potent than the dust"),
                    MscText.line(GRAY, "they were born from."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "Every fragment"),
                    MscText.quote(DARK_PURPLE, "screams in a single voice."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(CHAOS_FRAGMENT_KEY)
            .build();
}
