package com.Chagui68.items.weapons.ranged;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * Jack Star's legacy: a bow built round the Architect Kernel that fires packets of data,
 * deletes whatever stands in a line and fails over when its holder is about to fall.
 */
public class ArchitectDeployer {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_architect_deployer");

    public static final ItemStack ARCHITECT_DEPLOYER = ItemBuilder.of(Material.BOW)
            .name(MscText.title(AQUA, "Desplegador del Arquitecto"))
            .lore(
                    MscText.line(GRAY, "Un arco compilado alrededor del Kernel del"),
                    MscText.line(GRAY, "Arquitecto. Cada flecha es un despliegue."),
                    MscText.blank(),
                    MscText.line(WHITE, "Efectos Pasivos:"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Paquetes Guiados: ", GRAY, "las flechas buscan al ser"),
                    MscText.rich(GRAY, "    más cercano en ", GOLD, "8 bloques", GRAY, " y hacen ", RED, "+20% ", GRAY, "de daño"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Failover: ", GRAY, "al caer bajo el ", GOLD, "30%", GRAY,
                            " de vida, saltas"),
                    MscText.rich(GRAY, "    detrás del atacante con ", AQUA, "Resistencia I", GRAY, " (",
                            GOLD, "12 s", GRAY, " de enfriamiento)"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Habilidad: ", WHITE, "sudo rm -rf ", GRAY, "(Shift + Clic izquierdo)"),
                    MscText.line(GRAY, "  Un haz que atraviesa todo en línea recta"),
                    MscText.rich(GRAY, "  (", GOLD, "40 bloques", GRAY, ") y hace ", RED, "12 ", GRAY, "de daño a cada objetivo."),
                    MscText.rich(GRAY, "  Enfriamiento: ", GOLD, "18 segundos"),
                    MscText.blank(),
                    MscText.quote(DARK_AQUA, "\"Deploy: SUCCESS · Rollback: NEVER\""),
                    MscText.blank(),
                    MscText.footer(GOLD, "JackStar Systems")
            )
            .tagged(KEY)
            .unbreakable()
            .build();
}
