package com.Chagui68.wiki;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * One way to get something that is not a recipe: a creature or a boss that drops it, a trader, a
 * ritual. The chance, when there is one, is read from config.yml when the page opens, so the wiki
 * always shows the server's own numbers.
 *
 * @param chancePath    config path of the drop chance, or null
 * @param chanceDefault value the code falls back to when the path is missing
 * @param percent       true when that config value is already a percentage (0-100) instead of a
 *                      share (0-1)
 */
public record WikiSource(Material icon, String titleEn, String titleEs, String textEn, String textEs,
                         String chancePath, double chanceDefault, boolean percent) {

    /** A source with no chance to show. */
    public static WikiSource of(Material icon, String titleEn, String titleEs, String textEn, String textEs) {
        return new WikiSource(icon, titleEn, titleEs, textEn, textEs, null, 0, false);
    }

    /** A creature drop whose chance is the share at {@code path}. */
    public static WikiSource drop(Material icon, String mobEn, String mobEs, String textEn, String textEs,
                                  String path, double fallback) {
        return new WikiSource(icon, mobEn, mobEs, textEn, textEs, path, fallback, false);
    }

    public String title(WikiLang lang) {
        return lang.pick(titleEn, titleEs);
    }

    public String text(WikiLang lang) {
        return lang.pick(textEn, textEs);
    }

    /** The chance as the server has it, such as "60%", or null when this source has none. */
    public String chance(FileConfiguration config) {
        if (chancePath == null) {
            return null;
        }
        double value = config == null ? chanceDefault : config.getDouble(chancePath, chanceDefault);
        if (chancePath.startsWith("stands.") && chancePath.endsWith(".weight")) {
            return formatChance(standShare(value, config));
        }
        double share = percent ? value / 100.0 : value;
        return formatChance(share);
    }

    /** A Stand's weight as its share of every Stand's weight: how likely the Arrow is to roll it. */
    private static double standShare(double weight, FileConfiguration config) {
        double total = 0;
        for (com.Chagui68.stand.StandType type : com.Chagui68.stand.StandType.values()) {
            double each = config == null ? type.defaultWeight()
                    : config.getDouble("stands." + type.key() + ".weight", type.defaultWeight());
            total += Math.max(0, each);
        }
        return total <= 0 ? 0 : Math.max(0, weight) / total;
    }

    /** A share in {@code [0, 1]} as a percentage with at most one decimal. */
    static String formatChance(double share) {
        double clamped = Math.max(0, Math.min(1, share)) * 100;
        if (Math.abs(clamped - Math.rint(clamped)) < 1e-6) {
            return (long) Math.rint(clamped) + "%";
        }
        return String.format(java.util.Locale.ROOT, "%.1f%%", clamped);
    }

    /** Every line the source shows, wrapped. */
    List<String> lines(WikiLang lang) {
        return WikiText.wrap(text(lang), WikiText.WIDTH);
    }

    static List<WikiSource> none() {
        return List.of();
    }
}
