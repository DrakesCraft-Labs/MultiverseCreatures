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
            .name(MscText.title(AQUA, "Architect Kernel"))
            .lore(
                    MscText.line(GRAY, "A condensed core torn from the entity"),
                    MscText.line(GRAY, "that runs the physical laws of the system."),
                    MscText.blank(),
                    MscText.line(WHITE, "Multiversal Relic · ROOT Level"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Core of the ", AQUA, "Architect's Deployer"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Offering that summons ", AQUA, "JACKSTAR"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"The service must go on.\""),
                    MscText.quote(DARK_PURPLE, "Status: HEALTHY · Uptime: ∞ · Auth: ROOT"),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .build();
}
