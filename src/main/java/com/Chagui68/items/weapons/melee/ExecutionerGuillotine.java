package com.Chagui68.items.weapons.melee;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * NIX's own weapon, forged from the edges of his axe: an executioner's axe with his chains
 * and his leap.
 */
public class ExecutionerGuillotine {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_executioner_guillotine");

    public static final ItemStack EXECUTIONER_GUILLOTINE = ItemBuilder.of(Material.NETHERITE_AXE)
            .name(MscText.title(DARK_RED, "Executioner's Guillotine"))
            .lore(
                    MscText.line(GRAY, "NIX's axe, reforged from the edges"),
                    MscText.line(GRAY, "he left behind. It never misses a neck."),
                    MscText.blank(),
                    MscText.line(WHITE, "Passive Effects:"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Sentence: ", GRAY, "+", RED, "40% ", GRAY,
                            "damage to targets below ", GOLD, "30%"),
                    MscText.line(GRAY, "    of their health"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Bleed: ", GOLD, "25% ", GRAY, "chance to apply ",
                            DARK_GRAY, "Wither I ", GRAY, "for ", GOLD, "3 seconds"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Item Ability: ", WHITE, "Chains of Judgment ", GRAY, "(Right-Click)"),
                    MscText.rich(GRAY, "  Snares the target you look at (up to ", GOLD, "18 blocks", GRAY, ")"),
                    MscText.rich(GRAY, "  and drags it to you with ", DARK_GRAY, "Slowness II", GRAY, "."),
                    MscText.rich(GRAY, "  Cooldown: ", GOLD, "15 seconds"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Item Ability: ", WHITE, "Guillotine Drop ", GRAY, "(Shift + Right-Click)"),
                    MscText.line(GRAY, "  A leap with the axe held high and a blow that"),
                    MscText.rich(GRAY, "  deals ", RED, "14 ", GRAY, "damage in ", GOLD, "4 blocks", GRAY, " and launches foes."),
                    MscText.rich(GRAY, "  Cooldown: ", GOLD, "25 seconds"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Every condemned soul has its hour.\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .unbreakable()
            .build();
}
