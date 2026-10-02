package com.Chagui68.entities.boss.fx;

import org.bukkit.Color;

/**
 * The Sentinel's colours. Every attack draws from one family, so the fight reads as one boss:
 * violet and void for its magic, ember and gold for fire and judgement, frost and storm for the
 * cold and lightning attacks, and a warning red for every telegraph.
 */
public final class Palette {

    private Palette() {
    }

    // Obsidian / void magic
    public static final Color VOID_DEEP = Color.fromRGB(0x2A0845);
    public static final Color VOID = Color.fromRGB(0x6A1FB8);
    public static final Color AMETHYST = Color.fromRGB(0xB57BFF);
    public static final Color SPECTRAL = Color.fromRGB(0xE6CCFF);

    // Fire and judgement
    public static final Color EMBER = Color.fromRGB(0xFF5A1F);
    public static final Color MOLTEN = Color.fromRGB(0xFFA43A);
    public static final Color GOLD = Color.fromRGB(0xFFD45C);
    public static final Color HOLY = Color.fromRGB(0xFFF4C2);

    // Frost and storm
    public static final Color FROST = Color.fromRGB(0x9EE7FF);
    public static final Color ICE = Color.fromRGB(0xDDF7FF);
    public static final Color STORM = Color.fromRGB(0x4FA8FF);

    // Soul and plague
    public static final Color SOUL = Color.fromRGB(0x3FE0D0);
    public static final Color PLAGUE = Color.fromRGB(0x8FD13A);
    public static final Color BLOOD = Color.fromRGB(0xB0122A);

    // Earth
    public static final Color ASH = Color.fromRGB(0x4A4048);
    public static final Color STONE = Color.fromRGB(0x8A8290);

    /** Every telegraph: "something is coming here". */
    public static final Color WARNING = Color.fromRGB(0xFF2A2A);
    public static final Color WARNING_HOT = Color.fromRGB(0xFFE14D);

    /** A colour {@code t} of the way between two others. */
    public static Color mix(Color a, Color b, double t) {
        double k = Math.max(0.0, Math.min(1.0, t));
        return Color.fromRGB(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }
}
