package com.Chagui68.items.weapons.magic;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ChaosForge {

    public static final NamespacedKey FORGE_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_forge");
    public static final ItemStack CHAOS_FORGE = new ItemStack(Material.ANVIL);

    public static final int MAX_ENCHANT_LEVEL = 254;
    public static final String REFORGED_PDC_KEY = "msc_chaos_reforged";

    static {
        ItemMeta meta = CHAOS_FORGE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(LIGHT_PURPLE, "Chaos Forge"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A portable anvil laced with entropy."));
            lore.add(MscText.line(GRAY, "It cannot create — only twist what"));
            lore.add(MscText.line(GRAY, "is already written upon an item."));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Reforge ", GRAY, "(Right-Click)"));
            lore.add(MscText.rich(GRAY, "  Hold the item to reforge in your ", WHITE, "off-hand", GRAY, " and the"));
            lore.add(MscText.rich(WHITE, "  Chaos Forge", GRAY, " in your main hand, then right-click."));
            lore.add(MscText.rich(GRAY, "  Improves ", GOLD, "1 random enchantment", GRAY, " by"));
            lore.add(MscText.rich(GRAY, "  ", GOLD, "+1 level ", GRAY, "(cap ", GOLD, "254", GRAY, ")."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Restrictions:"));
            lore.add(MscText.rich(RED, "  ▸ ", GRAY, "Only items with existing enchantments"));
            lore.add(MscText.rich(RED, "  ▸ ", GRAY, "Consumes ", LIGHT_PURPLE, "1 Chaos Orb ", GRAY, "(once per item)"));
            lore.add(MscText.rich(RED, "  ▸ ", GRAY, "or ", DARK_PURPLE, "1 Condensed Chaos Orb ", GRAY, "for unlimited reforges"));
            lore.add(MscText.rich(RED, "  ▸ ", GRAY, "Requires the item to have been reforged once"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"In the orb, all possibilities;"));
            lore.add(MscText.quote(DARK_PURPLE, "in the hand, only one.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(FORGE_KEY, PersistentDataType.INTEGER, 1);
            CHAOS_FORGE.setItemMeta(meta);
        }
    }
}
