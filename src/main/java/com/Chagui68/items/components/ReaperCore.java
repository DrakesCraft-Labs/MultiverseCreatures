package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReaperCore {

    public static final NamespacedKey REAPER_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_reaper_core");
    public static final ItemStack REAPER_CORE = ItemBuilder.of(Material.WITHER_ROSE)
            .name(MscText.title(BLACK, "Reaper Core"))
            .lore(
                    MscText.line(GRAY, "The condensed lament of every soul"),
                    MscText.line(GRAY, "reaped by the Soul Reaper, blooming"),
                    MscText.line(GRAY, "in a single dark flower."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Each soul makes the blade heavier,"),
                    MscText.quote(DARK_PURPLE, "yet the wielder lighter.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(REAPER_CORE_KEY)
            .build();
}
