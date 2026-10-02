package com.Chagui68.stand;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * The Stands the Arrow can awaken, with how often each one appears and how it looks.
 *
 * <p>The weights are the defaults; {@code stands.<key>.weight} in config.yml overrides them. A
 * Stand with weight 0 is never rolled.</p>
 */
public enum StandType {

    HERMIT_PURPLE("hermit-purple", "Hermit Purple", NamedTextColor.DARK_PURPLE, 30,
            Color.fromRGB(0x7A3FB8), null, null, null, null),
    MAGICIANS_RED("magicians-red", "Magician's Red", NamedTextColor.RED, 25,
            Color.fromRGB(0xE0451B), Material.ORANGE_TERRACOTTA, Material.RED_CONCRETE, Material.ORANGE_CONCRETE,
            Material.YELLOW_CONCRETE),
    CRAZY_DIAMOND("crazy-diamond", "Crazy Diamond", NamedTextColor.LIGHT_PURPLE, 18,
            Color.fromRGB(0xF28CC8), Material.PINK_CONCRETE, Material.PINK_TERRACOTTA, Material.PINK_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE),
    KILLER_QUEEN("killer-queen", "Killer Queen", NamedTextColor.LIGHT_PURPLE, 14,
            Color.fromRGB(0xE7A1C9), Material.PINK_TERRACOTTA, Material.PINK_CONCRETE, Material.MAGENTA_TERRACOTTA,
            Material.BONE_BLOCK),
    STAR_PLATINUM("star-platinum", "Star Platinum", NamedTextColor.BLUE, 10,
            Color.fromRGB(0x5B4BD6), Material.LIGHT_BLUE_TERRACOTTA, Material.PURPLE_CONCRETE,
            Material.LIGHT_BLUE_TERRACOTTA, Material.GOLD_BLOCK),
    THE_WORLD("the-world", "The World", NamedTextColor.GOLD, 3,
            Color.fromRGB(0xF2C230), Material.YELLOW_TERRACOTTA, Material.GOLD_BLOCK, Material.YELLOW_CONCRETE,
            Material.EMERALD_BLOCK);

    private final String key;
    private final String displayName;
    private final NamedTextColor textColor;
    private final int defaultWeight;
    private final Color aura;
    private final Material head;
    private final Material body;
    private final Material arms;
    private final Material accent;

    StandType(String key, String displayName, NamedTextColor textColor, int defaultWeight, Color aura,
              Material head, Material body, Material arms, Material accent) {
        this.key = key;
        this.displayName = displayName;
        this.textColor = textColor;
        this.defaultWeight = defaultWeight;
        this.aura = aura;
        this.head = head;
        this.body = body;
        this.arms = arms;
        this.accent = accent;
    }

    /** The id used in config.yml, in commands and stored in the player. */
    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public NamedTextColor textColor() {
        return textColor;
    }

    public int defaultWeight() {
        return defaultWeight;
    }

    public Color aura() {
        return aura;
    }

    /** True for a Stand that is drawn as a body; Hermit Purple is only its vines. */
    public boolean hasBody() {
        return head != null;
    }

    public Material head() {
        return head;
    }

    public Material body() {
        return body;
    }

    public Material arms() {
        return arms;
    }

    public Material accent() {
        return accent;
    }

    /** True for the Stands that can stop time, and therefore move inside a stopped time. */
    public boolean stopsTime() {
        return this == STAR_PLATINUM || this == THE_WORLD;
    }

    /** The Stand with that id or name, ignoring case, spaces and dashes; null when unknown. */
    public static StandType byKey(String raw) {
        if (raw == null) {
            return null;
        }
        String wanted = raw.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")
                .replace("'", "");
        for (StandType type : values()) {
            if (type.key.replace("-", "").equals(wanted)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Picks a Stand from a uniform roll in {@code [0, 1)}, each one as likely as its weight.
     *
     * @return null when every weight is zero
     */
    public static StandType roll(double roll, ToIntFunction<StandType> weights) {
        int total = 0;
        for (StandType type : values()) {
            total += Math.max(0, weights.applyAsInt(type));
        }
        if (total <= 0) {
            return null;
        }
        double point = Math.max(0.0, Math.min(0.999999, roll)) * total;
        int running = 0;
        for (StandType type : values()) {
            running += Math.max(0, weights.applyAsInt(type));
            if (point < running) {
                return type;
            }
        }
        return values()[values().length - 1];
    }

    /** Same as {@link #roll(double, ToIntFunction)} with fixed weights, for tests. */
    public static StandType roll(double roll, Map<StandType, Integer> weights) {
        return roll(roll, type -> weights.getOrDefault(type, 0));
    }
}
