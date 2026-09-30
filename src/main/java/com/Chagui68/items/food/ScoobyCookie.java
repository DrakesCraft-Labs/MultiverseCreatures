package com.Chagui68.items.food;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ScoobyCookie {

    public static final NamespacedKey COOKIE_KEY = new NamespacedKey("multiversecreatures", "msc_scooby_cookie");
    public static final ItemStack SCOOBY_COOKIE = new ItemStack(Material.COOKIE);

    static {
        ItemMeta meta = SCOOBY_COOKIE.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(GOLD, "Scooby Cookie"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A mysterious cookie pulsating"));
            lore.add(MscText.line(GRAY, "with otherworldly energy."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Effect on Consume:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Resistance VI ", DARK_GRAY, "(10 seconds)"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Food: ", WHITE, "2 ", AQUA, "Saturation: ", WHITE, "0.4"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Scooby-Dooby-Doo...\""));
            lore.add(MscText.quote(DARK_PURPLE, "\"This tastes like courage!\""));
            lore.add(MscText.blank());
            lore.add(MscText.rich(GOLD, "✦ ", YELLOW, "Special", GOLD, " ✦"));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Mystery Inc."));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(COOKIE_KEY, PersistentDataType.INTEGER, 1);
            SCOOBY_COOKIE.setItemMeta(meta);
        }
    }
}