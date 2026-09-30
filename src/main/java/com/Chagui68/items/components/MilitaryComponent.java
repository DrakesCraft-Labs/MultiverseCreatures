package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MilitaryComponent {

    public static final NamespacedKey MILITARY_KEY = new NamespacedKey("multiversecreatures", "msc_military_component");
    public static final ItemStack MILITARY_COMPONENT = ItemBuilder.of(Material.GUNPOWDER)
            .name(MscText.title(GREEN, "Military Component"))
            .lore(
                    MscText.line(GRAY, "A piece of military-grade equipment"),
                    MscText.line(GRAY, "salvaged from the battlefield."),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Standard issue. Nothing more, nothing less.\""),
                    MscText.blank(),
                    MscText.footer("Military")
            )
            .tagged(MILITARY_KEY)
            .build();
}
