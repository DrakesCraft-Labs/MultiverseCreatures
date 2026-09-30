package com.Chagui68.items.weapons.ranged;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AetherPullshot {

    public static final NamespacedKey PULLSHOT_KEY = new NamespacedKey("multiversecreatures", "msc_aether_pullshot");
    public static final ItemStack AETHER_PULLSHOT = new ItemStack(Material.TRIDENT);

    public static final long PULL_COOLDOWN_MS = 30000L;
    public static final double PULL_RANGE = 40.0;
    public static final int PULL_DURATION_TICKS = 60;
    public static final double PULL_SPEED = 0.5;
    public static final double PULL_INITIAL_DAMAGE = 6.0;
    public static final double PULL_FINAL_DAMAGE = 10.0;

    public static final String COOLDOWN_KEY = "msc_aether_pullshot_until";

    static {
        ItemMeta meta = AETHER_PULLSHOT.getItemMeta();
        if (meta != null) {
            meta.displayName(MscText.title(DARK_AQUA, "Aether Pullshot"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A trident forged from an Ender"));
            lore.add(MscText.line(GRAY, "Fragment, strung with a leash of"));
            lore.add(MscText.line(GRAY, "threadbare space."));
            lore.add(MscText.blank());
            lore.add(MscText.rich(AQUA, "Item Ability: ", WHITE, "Aether Pull ", GRAY, "(Right-Click Entity)"));
            lore.add(MscText.rich(GRAY, "  Strike a target up to ", GOLD, "40 blocks ", GRAY, "away."));
            lore.add(MscText.rich(GRAY, "  The struck enemy is ", BLUE, "pulled toward you ", GRAY, "over"));
            lore.add(MscText.rich(GRAY, "  ", GOLD, "3 seconds", GRAY, ", taking ", RED, "6 initial damage"));
            lore.add(MscText.rich(GRAY, "  and ", RED, "10 damage ", GRAY, "if pulled all the way in."));
            lore.add(MscText.rich(GRAY, "  Cooldown: ", GOLD, "30 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"A leash not of rope,"));
            lore.add(MscText.quote(DARK_PURPLE, "but of distance denied.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(PULLSHOT_KEY, PersistentDataType.INTEGER, 1);
            meta.addEnchant(Enchantment.LOYALTY, 3, true);
            meta.setUnbreakable(true);
            AETHER_PULLSHOT.setItemMeta(meta);
        }
    }
}
