package com.Chagui68.entities.boss;

import org.bukkit.boss.BarColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the Sentinel's phase ladder down.
 *
 * The thresholds, the defence durations' trigger points, the bar colours and the six boss bar title
 * strings used to live inline in a 2200-line tick loop, with the phase count written in three
 * places at once. They now come from one list, so the numbers need a test of their own: the ladder
 * has to keep reproducing the old comparison chain exactly, or the fight changes difficulty.
 */
class SentinelPhaseTest {

    private static final String BOSS_NAME = "THE OBSIDIAN SENTINEL";
    /** The five phases the default ladder describes. */
    private static final int DEFAULT_PHASES = 5;

    @Nested
    @DisplayName("Phase ladder")
    class Ladder {

        /**
         * The ladder the boss used to hardcode, kept here only as the oracle the new one must match.
         */
        private static int oldComparisonChain(double healthPercent) {
            if (healthPercent > 0.8) return 0;
            if (healthPercent > 0.6) return 1;
            if (healthPercent > 0.4) return 2;
            if (healthPercent > 0.2) return 3;
            return 4;
        }

        @Test
        @DisplayName("Verify the default ladder is one more phase than it has thresholds")
        void testDefaultLadderHasFivePhases() {
            assertEquals(DEFAULT_PHASES, SentinelPhase.phaseCount(SentinelPhase.DEFAULT_THRESHOLDS));
        }

        @ParameterizedTest(name = "Verify {0} health is in phase {1}")
        @CsvSource({
                "1.0,    0",
                "0.99,   0",
                "0.8001, 0",
                "0.8,    1",  // at exactly 80% the old chain had already moved on
                "0.7999, 1",
                "0.6,    2",
                "0.5999, 2",
                "0.4,    3",
                "0.3999, 3",
                "0.2,    4",
                "0.1999, 4",
                "0.0,    4",
                "-0.5,   4",  // clamped rather than throwing on a nonsense fraction
                "2.0,    0",  // overheal keeps the boss in its opening phase
        })
        void testPhaseForHealthBoundaries(double healthPercent, int expectedPhase) {
            assertEquals(expectedPhase,
                    SentinelPhase.phaseFor(healthPercent, SentinelPhase.DEFAULT_THRESHOLDS));
        }

        @Test
        @DisplayName("Verify the configurable ladder reproduces the old hardcoded chain exactly")
        void testLadderMatchesTheOldComparisonChain() {
            for (int step = -50; step <= 1050; step++) {
                double healthPercent = step / 1000.0;
                assertEquals(oldComparisonChain(healthPercent),
                        SentinelPhase.phaseFor(healthPercent, SentinelPhase.DEFAULT_THRESHOLDS),
                        "phase differs at health " + healthPercent);
            }
        }

        @Test
        @DisplayName("Verify the phase only ever grows as health drops")
        void testPhaseIsMonotonicAsHealthDrops() {
            int previous = SentinelPhase.phaseFor(1.0, SentinelPhase.DEFAULT_THRESHOLDS);
            for (int step = 1000; step >= 0; step--) {
                int phase = SentinelPhase.phaseFor(step / 1000.0, SentinelPhase.DEFAULT_THRESHOLDS);
                assertTrue(phase >= previous,
                        "phase went backwards at health " + (step / 1000.0) + ": " + previous + " -> " + phase);
                previous = phase;
            }
        }

        @Test
        @DisplayName("Verify every phase of a ladder is reachable")
        void testEveryPhaseIsReachable() {
            List<Double> thresholds = SentinelPhase.DEFAULT_THRESHOLDS;
            List<Double> healthSamples = new ArrayList<>(thresholds);
            healthSamples.add(1.0);

            boolean[] seen = new boolean[SentinelPhase.phaseCount(thresholds)];
            for (double healthPercent : healthSamples) {
                seen[SentinelPhase.phaseFor(healthPercent, thresholds)] = true;
            }

            for (int phase = 0; phase < seen.length; phase++) {
                assertTrue(seen[phase], "no health fraction reaches phase " + phase);
            }
        }

        @Test
        @DisplayName("Verify a custom ladder rescales the fight")
        void testCustomLadderChangesThePhaseCount() {
            List<Double> halves = SentinelPhase.sanitizeThresholds(List.of(0.5));
            assertEquals(2, SentinelPhase.phaseCount(halves));
            assertEquals(0, SentinelPhase.phaseFor(0.9, halves));
            assertEquals(1, SentinelPhase.phaseFor(0.5, halves));
            assertEquals(1, SentinelPhase.phaseFor(0.1, halves));
        }
    }

    @Nested
    @DisplayName("Threshold sanitising")
    class Sanitising {

        @Test
        @DisplayName("Verify impossible thresholds are dropped and the rest sorted highest first")
        void testSanitiseDropsImpossibleValuesAndSorts() {
            List<Double> sanitised = SentinelPhase.sanitizeThresholds(
                    Arrays.asList(0.5, 1.5, -2.0, 0.9, Double.NaN, Double.POSITIVE_INFINITY));

            assertEquals(List.of(0.9, 0.5), sanitised,
                    "the ladder must walk downwards and never contain a phase that cannot start");
        }

        @Test
        @DisplayName("Verify duplicate thresholds are collapsed, since they would be a zero-width phase")
        void testSanitiseCollapsesDuplicates() {
            assertEquals(List.of(0.5), SentinelPhase.sanitizeThresholds(List.of(0.5, 0.5, 0.5)));
        }

        @Test
        @DisplayName("Verify an empty or unusable ladder falls back to the defaults")
        void testSanitiseFallsBackToDefaults() {
            assertEquals(SentinelPhase.DEFAULT_THRESHOLDS, SentinelPhase.sanitizeThresholds(null));
            assertEquals(SentinelPhase.DEFAULT_THRESHOLDS, SentinelPhase.sanitizeThresholds(List.of()));
            assertEquals(SentinelPhase.DEFAULT_THRESHOLDS,
                    SentinelPhase.sanitizeThresholds(List.of(0.0, 1.4, -1.0)),
                    "a typo in config.yml must not leave the boss with a single phase");
        }

        @Test
        @DisplayName("Verify a threshold of exactly 1.0 is kept: the phase starts at full health")
        void testSanitiseKeepsFullHealthThreshold() {
            assertEquals(List.of(1.0), SentinelPhase.sanitizeThresholds(List.of(1.0)));
        }

        @Test
        @DisplayName("Verify a sanitised ladder is immutable, so a tick cannot rewrite it")
        void testSanitisedLadderIsImmutable() {
            List<Double> sanitised = SentinelPhase.sanitizeThresholds(List.of(0.5, 0.25));
            assertThrows(UnsupportedOperationException.class, () -> sanitised.add(0.1));
        }
    }

    @Nested
    @DisplayName("Boss bar title and colour")
    class Presentation {

        @ParameterizedTest(name = "Verify phase {0} shows {1}")
        @CsvSource(delimiter = '|', value = {
                "0 | \u00a74\u00a7lTHE OBSIDIAN SENTINEL \u00a7c\u25a0\u25a0\u25a0\u25a0\u25a0",
                "1 | \u00a74\u00a7lTHE OBSIDIAN SENTINEL \u00a7c\u25a0\u25a0\u25a0\u25a0\u00a77\u25a0",
                "2 | \u00a7e\u00a7lTHE OBSIDIAN SENTINEL \u00a7c\u25a0\u25a0\u25a0\u00a77\u25a0\u25a0",
                "3 | \u00a7a\u00a7lTHE OBSIDIAN SENTINEL \u00a7c\u25a0\u25a0\u00a77\u25a0\u25a0\u25a0",
                "4 | \u00a79\u00a7lTHE OBSIDIAN SENTINEL \u00a7c\u25a0\u00a77\u25a0\u25a0\u25a0\u25a0",
        })
        @DisplayName("Verify the generated titles are byte-identical to the old hardcoded ones")
        void testTitleMatchesTheOldHardcodedStrings(int phase, String expected) {
            assertEquals(expected, SentinelPhase.title(BOSS_NAME, phase, DEFAULT_PHASES));
        }

        @Test
        @DisplayName("Verify the title bar tracks the configured phase count, not a fixed five squares")
        void testTitleFollowsThePhaseCount() {
            // Three phases: three squares at the start, one red and two grey in the last one.
            assertEquals("\u00a74\u00a7lOBSIDIAN \u00a7c\u25a0\u25a0\u25a0",
                    SentinelPhase.title("OBSIDIAN", 0, 3));
            assertEquals("\u00a7e\u00a7lOBSIDIAN \u00a7c\u25a0\u00a77\u25a0\u25a0",
                    SentinelPhase.title("OBSIDIAN", 2, 3));
        }

        @ParameterizedTest(name = "Verify phase {0} of a three-phase ladder still renders three squares")
        @ValueSource(ints = {3, 4, 99})
        @DisplayName("Verify a phase past the ladder cannot emit negative or missing squares")
        void testTitleClampsAPhasePastTheLadder(int phase) {
            String title = SentinelPhase.title("OBSIDIAN", phase, 3);

            assertEquals(3, title.chars().filter(c -> c == '\u25a0').count(),
                    "the bar must keep one square per phase, never fewer and never a negative count");
            assertTrue(title.startsWith("\u00a7"), "the title still starts with a colour code");
        }

        @Test
        @DisplayName("Verify the title still renders for a single-phase ladder")
        void testTitleForASinglePhaseLadder() {
            assertEquals("\u00a74\u00a7lOBSIDIAN \u00a7c\u25a0", SentinelPhase.title("OBSIDIAN", 0, 1));
        }

        @Test
        @DisplayName("Verify the bar colour follows the phase and clamps past the palette")
        void testBarColorFollowsThePhase() {
            assertEquals(BarColor.RED, SentinelPhase.barColor(0));
            assertEquals(BarColor.RED, SentinelPhase.barColor(1));
            assertEquals(BarColor.YELLOW, SentinelPhase.barColor(2));
            assertEquals(BarColor.GREEN, SentinelPhase.barColor(3));
            assertEquals(BarColor.BLUE, SentinelPhase.barColor(4));
            assertEquals(BarColor.BLUE, SentinelPhase.barColor(9),
                    "a longer ladder reuses the last colour instead of falling back to red");
            assertEquals(BarColor.RED, SentinelPhase.barColor(-1));
        }
    }
}
