package com.Chagui68.items.components;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class ExecutionerWarrant {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_executioner_warrant");

    public static final ItemStack EXECUTIONER_WARRANT = ItemBuilder.of(Material.PAPER)
            .name(MscText.title(DARK_RED, "Execution Warrant"))
            .lore(
                    MscText.line(GRAY, "A grim decree of capital punishment sealed in dried blood."),
                    MscText.line(GRAY, "Bearing the mark of an unforgiving executioner."),
                    MscText.blank(),
                    MscText.line(WHITE, "Boss Invocation Catalyst"),
                    MscText.blank(),
                    MscText.line(YELLOW, "Drop upon the Executioner's Scaffold anvil"),
                    MscText.line(YELLOW, "within the Boss Dimension to summon NIX."),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"The sentence has been passed...\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .build();
}
