package com.Chagui68.items.weapons.melee;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * NIX's own weapon, forged from the edges of his axe: an executioner's axe with his chains
 * and his leap.
 */
public class ExecutionerGuillotine {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_executioner_guillotine");

    public static final ItemStack EXECUTIONER_GUILLOTINE = ItemBuilder.of(Material.NETHERITE_AXE)
            .name(MscText.title(DARK_RED, "Guillotina del Verdugo"))
            .lore(
                    MscText.line(GRAY, "El hacha de NIX, rehecha con los filos"),
                    MscText.line(GRAY, "que dejó atrás. Nunca falla un cuello."),
                    MscText.blank(),
                    MscText.line(WHITE, "Efectos Pasivos:"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Sentencia: ", GRAY, "+", RED, "40% ", GRAY,
                            "de daño a objetivos bajo el ", GOLD, "30%"),
                    MscText.line(GRAY, "    de su vida"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Sangrado: ", GOLD, "25% ", GRAY, "de aplicar ",
                            DARK_GRAY, "Wither I ", GRAY, "por ", GOLD, "3 s"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Habilidad: ", WHITE, "Cadenas del Juicio ", GRAY, "(Clic derecho)"),
                    MscText.line(GRAY, "  Atrapa al objetivo que miras (hasta 18 bloques)"),
                    MscText.line(GRAY, "  y lo arrastra hacia ti con Lentitud II."),
                    MscText.rich(GRAY, "  Enfriamiento: ", GOLD, "15 segundos"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Habilidad: ", WHITE, "Caída de la Guillotina ", GRAY, "(Shift + Clic derecho)"),
                    MscText.line(GRAY, "  Un salto con el hacha en alto y un golpe que"),
                    MscText.rich(GRAY, "  hace ", RED, "14 ", GRAY, "de daño en ", GOLD, "4 bloques", GRAY, " y lanza al aire."),
                    MscText.rich(GRAY, "  Enfriamiento: ", GOLD, "25 segundos"),
                    MscText.blank(),
                    MscText.quote(DARK_RED, "\"Todo condenado tiene su hora.\""),
                    MscText.blank(),
                    MscText.footer(DARK_RED, "NIX")
            )
            .tagged(KEY)
            .unbreakable()
            .build();
}
