package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReaperEssence {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_reaper_essence");
    public static final ItemStack REAPER_ESSENCE = ItemBuilder.of(Material.SOUL_LANTERN)
            .name(MscText.title(BLACK, "Reaper Essence"))
            .lore(
                    MscText.line(GRAY, "A whispering wisp of souls, drawn"),
                    MscText.line(GRAY, "from the hollow skull of a Soul Reaper."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It hums with the lament"),
                    MscText.quote(DARK_PURPLE, "of the unreaped.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
