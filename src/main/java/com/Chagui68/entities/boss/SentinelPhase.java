package com.Chagui68.entities.boss;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Obsidian Sentinel's phase model: health fraction to phase, and phase to colour and title.
 *
 * WHY IT EXISTS
 *
 * The phase ladder used to be four hardcoded comparisons in the boss tick ({@code > 0.8}, {@code > 0.6},
 * {@code > 0.4}, {@code > 0.2}), the name colours a six-armed switch that repeated the boss name
 * literal, and the title a hand-written five-square string per phase. Nothing about that was
 * configurable and the number of phases was duplicated in three places: a {@code PHASES} constant
 * that nothing read, the comparisons, and the switch arms.
 *
 * Here the whole ladder is derived from one list of thresholds, the palettes are data, and the
 * title is generated from the phase count. The class is stateless so it can be tested on its own.
 *
 * THE TEXT IS UNCHANGED
 *
 * {@link #title} reproduces the six strings exactly — including that the phase colour applies to
 * the name while the squares are always red for the phases still to come and grey for the ones
 * already spent. It returns a legacy {@code §}-coded string because that is what the Bukkit boss
 * bar still takes.
 */
public final class SentinelPhase {

    /** Default health fractions (of max health) at or below which phases 1, 2, 3 and 4 begin. */
    public static final List<Double> DEFAULT_THRESHOLDS = List.of(0.8, 0.6, 0.4, 0.2);

    /**
     * Name colours, one per phase, dimmest first: the Sentinel darkens its name in the opening two
     * phases and brightens it as it is worn down. A server that configures more phases than there
     * are colours reuses the last one.
     */
    private static final List<ChatColor> NAME_COLORS = List.of(
            ChatColor.DARK_RED, ChatColor.DARK_RED, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.BLUE);

    /** Boss bar colours, one per phase, matching {@link #NAME_COLORS}. */
    private static final List<BarColor> BAR_COLORS = List.of(
            BarColor.RED, BarColor.RED, BarColor.YELLOW, BarColor.GREEN, BarColor.BLUE);

    /** One square of the phase bar in the boss bar title. */
    private static final String SQUARE = "\u25a0";

    private SentinelPhase() {
    }

    /**
     * Cleans a configured threshold list.
     *
     * Entries that are not finite or not inside {@code (0, 1]} are dropped — a threshold of 0 marks a
     * phase that can never start and one above 1 marks a phase that starts before the fight — the
     * rest are sorted highest first, because the ladder is walked downwards, and duplicates are
     * collapsed since two equal thresholds would create a phase of zero width.
     *
     * An empty result falls back to {@link #DEFAULT_THRESHOLDS}, so a typo in {@code config.yml}
     * cannot leave the boss with a single phase.
     */
    static List<Double> sanitizeThresholds(List<Double> configured) {
        if (configured == null) return DEFAULT_THRESHOLDS;

        List<Double> thresholds = new ArrayList<>();
        for (Double value : configured) {
            if (value == null || !Double.isFinite(value)) continue;
            if (value <= 0.0 || value > 1.0) continue;
            if (thresholds.contains(value)) continue;
            thresholds.add(value);
        }
        if (thresholds.isEmpty()) return DEFAULT_THRESHOLDS;

        thresholds.sort(Comparator.reverseOrder());
        return List.copyOf(thresholds);
    }

    /** The number of phases a threshold list describes: one more than the number of thresholds. */
    static int phaseCount(List<Double> thresholds) {
        return thresholds.size() + 1;
    }

    /**
     * The 0-based phase for a health fraction.
     *
     * A threshold counts as crossed when the health fraction reaches it or falls below it, which is
     * exactly what the old comparison chain did: at precisely 80% health the boss was already in
     * phase 1, not phase 0. Phase 0 is the full-health phase and the last phase is the enraged one,
     * so the result only ever grows as health drops.
     */
    static int phaseFor(double healthPercent, List<Double> thresholds) {
        int phase = 0;
        for (double threshold : thresholds) {
            if (healthPercent > threshold) break;
            phase++;
        }
        return Math.min(phase, thresholds.size());
    }

    /** The boss bar colour for a phase; phases past the palette reuse its last colour. */
    static BarColor barColor(int phase) {
        return BAR_COLORS.get(paletteIndex(phase, BAR_COLORS.size()));
    }

    /**
     * The boss bar title for a phase: the boss name and one square per phase, red for the phases
     * still to come and grey for the ones already spent, so the bar visibly empties as it is fought.
     *
     * The name takes the phase colour. With the default five phases this returns exactly the six
     * strings the switch used to hold.
     */
    static String title(String bossName, int phase, int phases) {
        int current = Math.max(0, Math.min(phase, phases));
        int remaining = phases - current;
        ChatColor nameColor = NAME_COLORS.get(paletteIndex(current, NAME_COLORS.size()));

        StringBuilder title = new StringBuilder()
                .append(nameColor.toString()).append(ChatColor.BOLD)
                .append(bossName).append(' ')
                .append(ChatColor.RED).append(SQUARE.repeat(remaining));
        // Only colour the spent squares when there are any: a trailing colour code with no text
        // after it would render the same but is not the string the hardcoded title held.
        if (current > 0) {
            title.append(ChatColor.GRAY).append(SQUARE.repeat(current));
        }
        return title.toString();
    }

    /** Clamps a phase into a palette, so a longer phase ladder cannot walk off the end of a list. */
    private static int paletteIndex(int phase, int paletteSize) {
        if (phase < 0) return 0;
        return Math.min(phase, paletteSize - 1);
    }
}
