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

public class CinderGreatsword {

    public static final NamespacedKey CINDER_KEY = new NamespacedKey("multiversecreatures", "msc_cinder_greatsword");
    public static final ItemStack CINDER_GREATSWORD = new ItemStack(Material.NETHERITE_SWORD);

    public static final long SLAM_COOLDOWN_MS = 10000L;
    public static final double SLAM_RADIUS = 5.0;
    public static final double SLAM_DAMAGE = 12.0;
    public static final int SLAM_FIRE_TICKS = 80;

    static {
        ItemMeta meta = CINDER_GREATSWORD.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(GOLD, "Cinder Greatsword"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A massive blade forged from the heart"));
            lore.add(MscText.line(GRAY, "of a Flame Elemental. Too heavy to"));
            lore.add(MscText.line(GRAY, "wield alongside a second weapon."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Two-handed: cannot pair with off-hand items"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Sets struck foes ablaze (Fire Aspect II)"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Wielder gains ", GOLD, "Fire Resistance", GRAY, " while held"));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Cinder Slam ", GRAY, "(Right-Click)"));
            lore.add(MscText.line(GRAY, "  Channel flame into the blade and slam"));
            lore.add(MscText.line(GRAY, "  the ground, igniting all enemies in a"));
            lore.add(MscText.rich(GRAY, "  ", GOLD, "5-block", GRAY, " radius. Cooldown: ", GOLD, "10s"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Where it falls, the world burns.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(CINDER_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            CINDER_GREATSWORD.setItemMeta(meta);
        }
    }
}
