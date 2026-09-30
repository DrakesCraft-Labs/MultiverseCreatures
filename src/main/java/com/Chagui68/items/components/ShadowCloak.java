package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ShadowCloak {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_shadow_cloak");
    public static final ItemStack SHADOW_CLOAK = ItemBuilder.of(Material.BLACK_WOOL)
            .name(MscText.title(DARK_GRAY, "Shadow Cloak Fragment"))
            .lore(
                    MscText.line(GRAY, "A shred of woven darkness, torn from"),
                    MscText.line(GRAY, "a Shadow Rogue during the kill."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Light bends around it,"),
                    MscText.quote(DARK_PURPLE, "as if afraid to touch it.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
