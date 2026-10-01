package com.Chagui68.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the world-allowlist semantics down.
 *
 * config.yml documents an empty {@code general.allowed-creature-worlds} as "every world", but the
 * implementation used to treat it as a hardcoded allowlist of the five default world names. On a
 * server with a custom world name that meant no conversions at all, and worse, the periodic
 * recount deleted any MSC creature it found there.
 */
class MscWorldPolicyTest {

    @ParameterizedTest(name = "Empty allowlist allows \"{0}\"")
    @ValueSource(strings = {"world", "survival", "creative_plots", "evento_temporal"})
    @DisplayName("Verify an empty allowlist means every world, as config.yml documents")
    void testEmptyAllowlistAllowsEveryWorld(String worldName) {
        assertTrue(MscWorldPolicy.isAllowed(List.of(), worldName));
        assertTrue(MscWorldPolicy.isAllowed(null, worldName));
    }


    @Test
    @DisplayName("Verify a populated allowlist restricts and normalizes world names")
    void testAllowlistRestrictsWorlds() {
        List<String> configured = List.of("  SURVIVAL ", "world_nether");

        assertTrue(MscWorldPolicy.isAllowed(configured, "survival"), "Case and blanks must not matter");
        assertTrue(MscWorldPolicy.isAllowed(configured, "WORLD_NETHER"));
        assertFalse(MscWorldPolicy.isAllowed(configured, "creative_plots"));
    }

    @ParameterizedTest(name = "Plugin world \"{0}\" stays allowed")
    @ValueSource(strings = {"boss_dimension", "drakes_bosses", "DRAKES_BOSSES"})
    @DisplayName("Verify the plugin's own worlds always stay allowed")
    void testInternalWorldsAreAlwaysAllowed(String worldName) {
        // Even behind an explicit allowlist that does not mention them, the boss dimension the
        // plugin creates itself must keep working.
        assertTrue(MscWorldPolicy.isAllowed(List.of("survival"), worldName));
    }


    @Test
    @DisplayName("Verify a null world name is never allowed")
    void testNullWorldIsRejected() {
        assertFalse(MscWorldPolicy.isAllowed(List.of(), null));
        assertFalse(MscWorldPolicy.isAllowed(List.of("survival"), null));
        assertFalse(MscWorldPolicy.isAllowed(List.of("survival"), ""));
    }
}
