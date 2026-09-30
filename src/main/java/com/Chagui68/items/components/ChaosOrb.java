package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosOrb {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_chaos_orb");
    public static final ItemStack CHAOS_ORB = ItemBuilder.of(Material.NETHER_STAR)
            .name(MscText.title(LIGHT_PURPLE, "Chaos Orb"))
            .lore(
                    MscText.line(GRAY, "A sphere of pure entropy, plucked"),
                    MscText.line(GRAY, "from the spell-scatter of a Chaos Mage."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"In the orb, all possibilities;"),
                    MscText.quote(DARK_PURPLE, "in the hand, only one.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
