package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class BoneMarrow {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_bone_marrow");
    public static final ItemStack BONE_MARROW = ItemBuilder.of(Material.BONE_MEAL)
            .name(MscText.title(WHITE, "Bone Marrow"))
            .lore(
                    MscText.line(GRAY, "The red marrow crushed out of a"),
                    MscText.line(GRAY, "reinforced bone, pulsing with the"),
                    MscText.line(GRAY, "last warmth of its undead owner."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Death's architecture,"),
                    MscText.quote(DARK_PURPLE, "preserved in marrow.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(KEY)
            .build();
}