package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ObsidianShard {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_obsidian_shard");
    public static final ItemStack OBSIDIAN_SHARD = ItemBuilder.of(Material.OBSIDIAN)
            .name(MscText.title(DARK_GRAY, "Obsidian Shard"))
            .lore(
                    MscText.line(GRAY, "A flawless splinter of obsidian, hewn"),
                    MscText.line(GRAY, "from the armor of an Obsidian Guard."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Blacker than night,"),
                    MscText.quote(DARK_PURPLE, "harder than resolve.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}
