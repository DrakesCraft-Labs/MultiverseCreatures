package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class StarCore {

    public static final NamespacedKey STAR_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_star_core");
    public static final ItemStack STAR_CORE = ItemBuilder.of(Material.NETHER_STAR)
            .name(MscText.title(YELLOW, "Star Core"))
            .lore(
                    MscText.line(GRAY, "The strongest of this world, mixed with"),
                    MscText.line(GRAY, "the strongest of another, forged around"),
                    MscText.line(GRAY, "the heart of a superior entity."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "The heart of a fallen star,"),
                    MscText.quote(DARK_PURPLE, "beating with ancient power."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(STAR_CORE_KEY)
            .build();
}
