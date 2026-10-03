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
                    MscText.line(GRAY, "A grim decree of capital punishment,"),
                    MscText.line(GRAY, "sealed in dried blood and bearing the"),
                    MscText.line(GRAY, "mark of an unforgiving executioner."),
                    MscText.blank(),
                    MscText.line(WHITE, "Boss Invocation Catalyst"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Drop it on the ", RED, "Executioner's Scaffold"),
                    MscText.rich(GRAY, "    anvil in the Boss Dimension to summon ", DARK_RED, "NIX"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Binds the ", DARK_RED, "Executioner's Guillotine"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"The sentence has been passed...\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .build();
}
