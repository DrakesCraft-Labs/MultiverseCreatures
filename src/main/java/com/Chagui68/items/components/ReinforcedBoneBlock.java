package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReinforcedBoneBlock {

    public static final NamespacedKey REINFORCED_BONE_BLOCK_KEY = new NamespacedKey("multiversecreatures", "msc_reinforced_bone_block");
    public static final ItemStack REINFORCED_BONE_BLOCK = ItemBuilder.of(Material.BONE_BLOCK)
            .name(MscText.title(WHITE, "Reinforced Bone Block"))
            .lore(
                    MscText.line(GRAY, "Nine reinforced bones fused into"),
                    MscText.line(GRAY, "a single unbreakable slab."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "The dead do not yield,"),
                    MscText.quote(DARK_PURPLE, "they simply endure."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(REINFORCED_BONE_BLOCK_KEY)
            .build();
}
