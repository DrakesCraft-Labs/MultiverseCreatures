package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * DIO's blood: what DIO leaves behind when he falls, and the one ingredient of the Bearer's
 * Elixir. It is the only MultiverseCreatures component a brewing stand accepts.
 */
public class VampireBlood {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_vampire_blood");

    public static final ItemStack VAMPIRE_BLOOD = ItemBuilder.of(Material.RED_DYE)
            .name(MscText.title(DARK_RED, "Vampire Blood"))
            .lore(
                    MscText.line(GRAY, "Thick blood, still warm, drawn from DIO."),
                    MscText.line(GRAY, "It beats on its own, as if searching"),
                    MscText.line(GRAY, "for a new body to dwell in."),
                    MscText.blank(),
                    MscText.line(WHITE, "Brewing Ingredient"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Awkward Potion + ", DARK_RED, "Vampire Blood",
                            GRAY, " = ", RED, "Unstable Blood"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"My blood marks the beginning"),
                    MscText.quote(DARK_PURPLE, "of your new life!\""),
                    MscText.blank(),
                    MscText.footer("Jojos")
            )
            .tagged(KEY)
            .build();
}
