package com.Chagui68.items.weapons.ranged;

import com.Chagui68.utils.ItemBuilder;
import com.Chagui68.utils.MscText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * Jack Star's legacy: a bow built round the Architect Kernel that fires packets of data,
 * deletes whatever stands in a line and fails over when its holder is about to fall.
 */
public class ArchitectDeployer {

    public static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "msc_architect_deployer");

    public static final ItemStack ARCHITECT_DEPLOYER = ItemBuilder.of(Material.BOW)
            .name(MscText.title(AQUA, "Architect's Deployer"))
            .lore(
                    MscText.line(GRAY, "A bow compiled around the Architect"),
                    MscText.line(GRAY, "Kernel. Every arrow is a deployment."),
                    MscText.blank(),
                    MscText.line(WHITE, "Passive Effects:"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Guided Packets: ", GRAY, "arrows home in on the"),
                    MscText.rich(GRAY, "    nearest target within ", GOLD, "8 blocks", GRAY, " and deal ", RED, "+20% ",
                            GRAY, "damage"),
                    MscText.rich(YELLOW, "  ▸ ", WHITE, "Failover: ", GRAY, "dropping below ", GOLD, "30%", GRAY,
                            " health blinks"),
                    MscText.rich(GRAY, "    you behind the attacker with ", AQUA, "Resistance I", GRAY, " (",
                            GOLD, "12 s", GRAY, " cooldown)"),
                    MscText.blank(),
                    MscText.rich(AQUA, "Item Ability: ", WHITE, "sudo rm -rf ", GRAY, "(Shift + Left-Click)"),
                    MscText.line(GRAY, "  A beam that pierces everything in a straight"),
                    MscText.rich(GRAY, "  line (", GOLD, "40 blocks", GRAY, ") and deals ", RED, "12 ", GRAY,
                            "damage to each target."),
                    MscText.rich(GRAY, "  Cooldown: ", GOLD, "18 seconds"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"Deploy: SUCCESS · Rollback: NEVER\""),
                    MscText.blank(),
                    MscText.footer("DrakesCraft")
            )
            .tagged(KEY)
            .unbreakable()
            .build();
}
