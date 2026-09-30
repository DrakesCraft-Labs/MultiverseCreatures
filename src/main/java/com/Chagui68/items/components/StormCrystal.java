package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class StormCrystal {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_storm_crystal");
    public static final ItemStack STORM_CRYSTAL = ItemBuilder.of(Material.QUARTZ)
            .name(MscText.title(YELLOW, "Storm Crystal"))
            .lore(
                    MscText.line(GRAY, "A crackling shard of bottled lightning,"),
                    MscText.line(GRAY, "taken from the carcass of a Storm Caller."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Thunder made solid,"),
                    MscText.quote(DARK_PURPLE, "rage made still.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
