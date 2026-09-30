package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MultiversalCore {

    public static final NamespacedKey MULTIVERSAL_CORE_KEY = new NamespacedKey("multiversecreatures", "msc_multiversal_core");
    public static final ItemStack MULTIVERSAL_CORE = ItemBuilder.of(Material.TOTEM_OF_UNDYING)
            .name(MscText.title(GOLD, "Multiversal Core"))
            .lore(
                    MscText.line(GRAY, "A Sentinel Core reforged at the"),
                    MscText.line(GRAY, "crossroads of a thousand worlds,"),
                    MscText.line(GRAY, "bound with stars and refined netherite."),
                    MscText.blank(),
                    MscText.line(WHITE, "Crafting Ingredient"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Every universe remembers"),
                    MscText.quote(DARK_PURPLE, "what it forged.\""),
                    MscText.blank(),
                    MscText.footer("Multiverse")
            )
            .tagged(MULTIVERSAL_CORE_KEY)
            .build();
}