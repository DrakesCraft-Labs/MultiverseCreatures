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
            .name(MscText.title(DARK_RED, "Sangre Vampírica"))
            .lore(
                    MscText.line(GRAY, "Sangre espesa y todavía tibia de DIO."),
                    MscText.line(GRAY, "Late por sí sola, como si buscara"),
                    MscText.line(GRAY, "un nuevo cuerpo que habitar."),
                    MscText.blank(),
                    MscText.line(WHITE, "Ingrediente de Destilado"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Poción Rara + ", DARK_RED, "Sangre Vampírica",
                            GRAY, " = ", RED, "Sangre Inestable"),
                    MscText.blank(),
                    MscText.quote(DARK_RED, "\"¡Mi sangre es el inicio"),
                    MscText.quote(DARK_RED, "de tu nueva vida!\""),
                    MscText.blank(),
                    MscText.footer(GOLD, "DIO")
            )
            .tagged(KEY)
            .build();
}
