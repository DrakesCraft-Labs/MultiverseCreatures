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

public class HeadSlimeGelatin {

    public static final NamespacedKey GELATIN_KEY = new NamespacedKey("multiversecreatures", "msc_head_slime_gelatin");
    public static final ItemStack HEAD_SLIME_GELATIN = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);

    static {
        ItemMeta meta = HEAD_SLIME_GELATIN.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(LIGHT_PURPLE, "Head Slime Gelatin"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "Bouncy and wobbly, yet strangely tasty."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Effect on Consume:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Head Slime Immunity ", DARK_GRAY, "(10 seconds)"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Food: ", WHITE, "4 ", AQUA, "Saturation: ", WHITE, "2.4"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Slimy yet satisfying!\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Slime Kingdom"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(GELATIN_KEY, PersistentDataType.INTEGER, 1);
            HEAD_SLIME_GELATIN.setItemMeta(meta);
        }
    }
}
