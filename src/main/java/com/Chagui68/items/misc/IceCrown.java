package com.Chagui68.items.misc;

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

public class IceCrown {

    public static final NamespacedKey ICE_CROWN_KEY = new NamespacedKey("multiversecreatures", "msc_ice_crown");
    public static final ItemStack ICE_CROWN = new ItemStack(Material.HORN_CORAL_FAN);

    static {
        ItemMeta meta = ICE_CROWN.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(AQUA, "Ice King's Crown"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A crown of eternal winter..."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Abilities:"));
            lore.add(MscText.rich(AQUA, "  ▸ ", WHITE, "Right-Click: ", GRAY, "Launch targeted snow/ice block"));
            lore.add(MscText.rich(AQUA, "  ▸ ", WHITE, "Shift + Right-Click: ", GRAY, "Blizzard (AoE)"));
            lore.add(MscText.rich(AQUA, "  ▸ ", WHITE, "Left-Click: ", GRAY, "Toggle Ice Path"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Gunter, why you gotta be like that?\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Ooo"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(ICE_CROWN_KEY, PersistentDataType.INTEGER, 1);
            ICE_CROWN.setItemMeta(meta);
        }
    }
}