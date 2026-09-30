package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ChaosCore {

    public static final NamespacedKey CHAOS_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_core");
    public static final ItemStack CHAOS_CORE = ItemBuilder.of(Material.END_CRYSTAL)
            .name(MscText.title(LIGHT_PURPLE, "Chaos Core"))
            .lore(
                    MscText.line(GRAY, "A furnace-bright core of pure disorder,"),
                    MscText.line(GRAY, "bound tight enough to hold,"),
                    MscText.line(GRAY, "barely."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "The tighter you hold it,"),
                    MscText.quote(DARK_PURPLE, "the louder it roars."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(CHAOS_CORE_KEY)
            .build();
}
