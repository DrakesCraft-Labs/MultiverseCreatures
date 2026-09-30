package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class EnderCore {

    public static final NamespacedKey ENDER_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_ender_core");
    public static final ItemStack ENDER_CORE = ItemBuilder.of(Material.SHULKER_SHELL)
            .name(MscText.title(DARK_PURPLE, "Ender Core"))
            .lore(
                    MscText.line(GRAY, "A heart of compressed Ender energy,"),
                    MscText.line(GRAY, "channeling the void between worlds."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "What is lost between worlds"),
                    MscText.quote(DARK_PURPLE, "is never truly gone."),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(ENDER_CORE_KEY)
            .build();
}
