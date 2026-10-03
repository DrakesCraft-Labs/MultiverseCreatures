package com.Chagui68.items.potions;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * The two potions brewed from DIO's blood.
 *
 * <ol>
 *   <li><b>Unstable Blood</b>: Awkward Potion + Vampire Blood.</li>
 *   <li><b>Bearer's Elixir</b>: Unstable Blood + Wither Rose. Drinking it turns the
 *   player into a vampire who can carry a Stand.</li>
 * </ol>
 *
 * <p>Neither has a vanilla potion type, so no vanilla brewing recipe can turn them into
 * something else.</p>
 */
public final class VampirePotions {

    public static final NamespacedKey UNSTABLE_KEY = new NamespacedKey("multiversecreatures", "msc_unstable_blood");
    public static final NamespacedKey ELIXIR_KEY = new NamespacedKey("multiversecreatures", "msc_bearer_elixir");

    public static final ItemStack UNSTABLE_BLOOD = potion(UNSTABLE_KEY, Color.fromRGB(0x8B0A0A),
            MscText.title(RED, "Unstable Blood"), List.of(
                    MscText.line(GRAY, "DIO's blood refuses the water:"),
                    MscText.line(GRAY, "it boils, splits and binds again."),
                    MscText.blank(),
                    MscText.line(WHITE, "Brewing Ingredient"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "+ ", DARK_GRAY, "Wither Rose", GRAY, " = ",
                            DARK_RED, "Bearer's Elixir"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"It is not ready yet.\""),
                    MscText.blank(),
                    MscText.footer("Jojos")));

    public static final ItemStack BEARER_ELIXIR = potion(ELIXIR_KEY, Color.fromRGB(0x3A0008),
            MscText.title(DARK_RED, "Bearer's Elixir"), List.of(
                    MscText.line(GRAY, "DIO's blood, tamed by the death"),
                    MscText.line(GRAY, "of a Wither Rose."),
                    MscText.blank(),
                    MscText.line(WHITE, "Effect on Consume:"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "You become a ", GOLD, "Stand bearer", GRAY, ": the"),
                    MscText.rich(GRAY, "    ", GOLD, "Arrow", GRAY, " can no longer kill you and"),
                    MscText.line(GRAY, "    always awakens your Stand"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Lifts the Arrow's mark of the ", DARK_RED, "unworthy"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "You become a ", DARK_RED, "vampire", GRAY, ": Strength and"),
                    MscText.line(GRAY, "    Speed at night, Night Vision and life steal"),
                    MscText.rich(RED, "  ✖ ", GRAY, "The ", YELLOW, "sun", GRAY, " deals ", RED, "true damage"),
                    MscText.line(GRAY, "    under the open sky: it ignores all armor"),
                    MscText.blank(),
                    MscText.quote(DARK_PURPLE, "\"I reject my humanity, JoJo!\""),
                    MscText.blank(),
                    MscText.footer("Jojos")));

    private VampirePotions() {
    }

    private static ItemStack potion(NamespacedKey key, Color colour, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.POTION);
        if (item.getItemMeta() instanceof PotionMeta meta) {
            meta.displayName(name);
            meta.lore(lore);
            meta.setColor(colour);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static boolean has(ItemStack item, NamespacedKey key) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    public static boolean isUnstableBlood(ItemStack item) {
        return has(item, UNSTABLE_KEY);
    }

    public static boolean isBearerElixir(ItemStack item) {
        return has(item, ELIXIR_KEY);
    }
}
