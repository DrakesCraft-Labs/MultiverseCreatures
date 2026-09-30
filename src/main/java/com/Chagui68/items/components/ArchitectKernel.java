package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ArchitectKernel {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_architect_kernel");

    public static final ItemStack ARCHITECT_KERNEL = ItemBuilder.of(Material.NETHER_STAR)
            .name(MscText.title(AQUA, "Kernel del Arquitecto"))
            .lore(
                    MscText.line(GRAY, "Un núcleo condensado extraído de la entidad"),
                    MscText.line(GRAY, "que administra las leyes físicas del sistema."),
                    MscText.blank(),
                    MscText.line(WHITE, "Reliquia Multiversal · Nivel ROOT"),
                    MscText.blank(),
                    MscText.rich(YELLOW, "Pasiva: ", AQUA, "Failover"),
                    MscText.line(GRAY, "Al recibir daño crítico, conmuta tu posición"),
                    MscText.line(GRAY, "detrás del atacante con Resistencia I (CD: 12s)."),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"El servicio debe continuar.\""),
                    MscText.quote(DARK_AQUA, "Status: HEALTHY · Uptime: ∞ · Auth: ROOT"),
                    MscText.blank(),
                    MscText.footer(GOLD, "JackStar Systems")
            )
            .tagged(KEY)
            .build();
}
