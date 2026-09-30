package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CondensedChaosOrb {

    public static final NamespacedKey CONDENSED_CHAOS_ORB_KEY = new NamespacedKey("multiversecreatures", "msc_condensed_chaos_orb");
    public static final ItemStack CONDENSED_CHAOS_ORB = ItemBuilder.of(Material.NETHER_STAR)
            .name(MscText.title(DARK_PURPLE, "Condensed Chaos Orb"))
            .lore(
                    MscText.line(GRAY, "A Chaos Orb pressed past the point"),
                    MscText.line(GRAY, "of breaking, holding an impossible"),
                    MscText.line(GRAY, "amount of entropy in a single point."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Reforge: ", GRAY, "Allows unlimited reforges"),
                    MscText.rich(GRAY, "in the ", WHITE, "Chaos Forge", GRAY, " (no once-per-item limit)."),
                    MscText.line(GRAY, "Requires the item to have been reforged"),
                    MscText.rich(GRAY, "with a ", LIGHT_PURPLE, "Chaos Orb", GRAY, " first."),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "All possibilities,"),
                    MscText.quote(DARK_PURPLE, "crushed into one."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(CONDENSED_CHAOS_ORB_KEY)
            .build();
}
