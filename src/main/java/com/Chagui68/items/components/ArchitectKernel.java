package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ArchitectKernel {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_architect_kernel");

    public static final ItemStack ARCHITECT_KERNEL = ItemBuilder.of(Material.NETHER_STAR)
            .name(ChatColor.AQUA + "" + ChatColor.BOLD + "Kernel del Arquitecto")
            .lore(
                    ChatColor.GRAY + "Un núcleo condensado extraído de la entidad",
                    ChatColor.GRAY + "que administra las leyes físicas del sistema.",
                    "",
                    ChatColor.WHITE + "Reliquia Multiversal · Nivel ROOT",
                    "",
                    ChatColor.YELLOW + "Pasiva: " + ChatColor.AQUA + "Failover",
                    ChatColor.GRAY + "Al recibir daño crítico, conmuta tu posición",
                    ChatColor.GRAY + "detrás del atacante con Resistencia I (CD: 12s).",
                    "",
                    ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"El servicio debe continuar.\"",
                    ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Status: HEALTHY · Uptime: ∞ · Auth: ROOT",
                    "",
                    ChatColor.DARK_GRAY + "✦ " + ChatColor.GOLD + "JackStar Systems" + ChatColor.DARK_GRAY + " ✦"
            )
            .tagged(KEY)
            .build();
}
