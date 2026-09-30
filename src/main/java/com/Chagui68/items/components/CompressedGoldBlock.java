package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CompressedGoldBlock {

    public static final NamespacedKey COMPRESSED_GOLD_BLOCK_KEY = new NamespacedKey("multiversecreatures", "msc_compressed_gold_block");
    public static final ItemStack COMPRESSED_GOLD_BLOCK = ItemBuilder.of(Material.GOLD_BLOCK)
            .name(MscText.title(GOLD, "Compressed Gold Block"))
            .lore(
                    MscText.line(GRAY, "Nine gold blocks pressed into one,"),
                    MscText.line(GRAY, "dense enough to anchor a smithing"),
                    MscText.line(GRAY, "ritual of its own."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Gold remembers every hand"),
                    MscText.quote(DARK_PURPLE, "that weighed it.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(COMPRESSED_GOLD_BLOCK_KEY)
            .build();
}