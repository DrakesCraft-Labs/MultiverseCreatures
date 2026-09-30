package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class HeadSlimeHeart {

    public static final NamespacedKey HEART_KEY = new NamespacedKey("multiversecreatures", "msc_head_slime_heart");
    public static final ItemStack HEAD_SLIME_HEART = ItemBuilder.of(Material.SLIME_BALL)
            .name(MscText.title(GREEN, "Head Slime Heart"))
            .lore(
                    MscText.line(GRAY, "The pulsating core of a Head Slime."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It still squirms...\""),
                    MscText.blank(),
                    MscText.footer("Slime Kingdom")
            )
            .tagged(HEART_KEY)
            .build();
}
