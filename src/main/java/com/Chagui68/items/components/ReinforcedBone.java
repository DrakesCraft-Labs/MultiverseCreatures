package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ReinforcedBone {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_reinforced_bone");
    public static final ItemStack REINFORCED_BONE = ItemBuilder.of(Material.BONE)
            .name(MscText.title(WHITE, "Reinforced Bone"))
            .lore(
                    MscText.line(GRAY, "A bone denser than diamond, broken"),
                    MscText.line(GRAY, "from the living wall of a Bone Shield."),
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
