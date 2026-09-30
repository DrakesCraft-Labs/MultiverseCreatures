package com.Chagui68.items.weapons.melee;

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

public class Excalibur {

    public static final NamespacedKey EXCALIBUR_KEY = new NamespacedKey("multiversecreatures", "msc_excalibur_sword");
    public static final ItemStack EXCALIBUR_SWORD = new ItemStack(Material.NETHERITE_SWORD);

    static {
        ItemMeta meta = EXCALIBUR_SWORD.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(GOLD, "Excalibur"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "The legendary blade of kings,"));
            lore.add(MscText.line(GRAY, "forged from a fallen star's heart."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effect:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Grants ", GOLD, "Strength III", GRAY, " while held"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Solar Flare ", GRAY, "(Right-Click)"));
            lore.add(MscText.line(GRAY, "  Unleash a wave of radiant energy,"));
            lore.add(MscText.line(GRAY, "  burning all enemies before you."));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Whosoever holds this sword,\""));
            lore.add(MscText.quote(DARK_PURPLE, "\"if they be worthy, shall possess\""));
            lore.add(MscText.quote(DARK_PURPLE, "\"the power of the Sun itself.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Avalon"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(EXCALIBUR_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            EXCALIBUR_SWORD.setItemMeta(meta);
        }
    }
}