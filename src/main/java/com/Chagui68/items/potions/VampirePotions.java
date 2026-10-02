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
 *   <li><b>Sangre Inestable</b>: Awkward Potion + Vampire Blood.</li>
 *   <li><b>Elixir del Portador</b>: Sangre Inestable + Wither Rose. Drinking it turns the
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
            MscText.title(RED, "Sangre Inestable"), List.of(
                    MscText.line(GRAY, "La sangre de DIO no acepta el agua:"),
                    MscText.line(GRAY, "hierve, se separa y vuelve a unirse."),
                    MscText.blank(),
                    MscText.line(WHITE, "Paso intermedio de Destilado"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "+ ", DARK_GRAY, "Rosa del Wither", GRAY, " = ",
                            DARK_RED, "Elixir del Portador"),
                    MscText.blank(),
                    MscText.quote(RED, "\"Todavía no está lista.\""),
                    MscText.blank(),
                    MscText.footer(GOLD, "DIO")));

    public static final ItemStack BEARER_ELIXIR = potion(ELIXIR_KEY, Color.fromRGB(0x3A0008),
            MscText.title(DARK_RED, "Elixir del Portador"), List.of(
                    MscText.line(GRAY, "Sangre de DIO domada por la muerte"),
                    MscText.line(GRAY, "de una Rosa del Wither."),
                    MscText.blank(),
                    MscText.line(WHITE, "Al beberla:"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Te conviertes en ", GOLD, "portador de Stand", GRAY, ": si la"),
                    MscText.rich(GRAY, "    ", GOLD, "Flecha", GRAY, " te elige, tu Stand despertará"),
                    MscText.rich(YELLOW, "  ▸ ", GRAY, "Te conviertes en ", DARK_RED, "vampiro", GRAY, ": fuerza y"),
                    MscText.line(GRAY, "    velocidad de noche, visión nocturna y robo de vida"),
                    MscText.rich(RED, "  ✖ ", GRAY, "El ", YELLOW, "sol", GRAY, " te inflige ", RED, "daño verdadero"),
                    MscText.line(GRAY, "    bajo el cielo abierto: ignora toda armadura"),
                    MscText.blank(),
                    MscText.quote(DARK_RED, "\"¡Yo he abandonado mi humanidad, JoJo!\""),
                    MscText.blank(),
                    MscText.footer(GOLD, "DIO")));

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
