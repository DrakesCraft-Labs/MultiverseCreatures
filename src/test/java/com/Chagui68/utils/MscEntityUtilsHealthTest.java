package com.Chagui68.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MscEntityUtilsHealthTest {

    @ParameterizedTest(name = "Target health {0} with server cap {1} clamps to {2}")
    @CsvSource({
            "3200.0, 1024.0, 1024.0", // ArmorStandBoss on default Paper/Spigot server
            "3200.0, 5000.0, 3200.0", // ArmorStandBoss on server with raised attribute limits
            "450.0,  1024.0, 450.0",  // NIX boss on default server
            "200.0,  1024.0, 200.0",  // Frost Golem on default server
            "50.0,   20.0,   20.0",   // Entity without attribute instance defaulting to vanilla 20
            "0.0,    1024.0, 0.1",    // Prevents instant death on spawn (minimum 0.1 health)
            "-10.0,  1024.0, 0.1"     // Negative input protection
    })
    @DisplayName("Verify calculateSafeHealth clamps health to server max limit avoiding IllegalArgumentException")
    void testCalculateSafeHealth(double targetHealth, double effectiveMax, double expectedHealth) {
        double result = MscEntityUtils.calculateSafeHealth(targetHealth, effectiveMax);
        assertEquals(expectedHealth, result, 1e-9,
                "Health must be clamped between 0.1 and effectiveMax");
    }

    @Test
    @DisplayName("Verify requested health below server cap is preserved completely")
    void testPreserveHealthBelowCap() {
        double result = MscEntityUtils.calculateSafeHealth(500.0, 1024.0);
        assertEquals(500.0, result, 1e-9);
    }

    @ParameterizedTest(name = "Current {0} / Max {1} produces progress {2}")
    @CsvSource({
            "3200.0, 3200.0, 1.0",
            "1600.0, 3200.0, 0.5",
            "0.0,    3200.0, 0.0",
            "-50.0,  3200.0, 0.0",
            "4000.0, 3200.0, 1.0",
            "0.0,    0.0,    0.0"
    })
    @DisplayName("Verify calculateVirtualProgress clamps correctly between 0.0 and 1.0")
    void testCalculateVirtualProgress(double current, double max, double expected) {
        double result = MscEntityUtils.calculateVirtualProgress(current, max);
        assertEquals(expected, result, 1e-9);
    }

    @ParameterizedTest(name = "Virtual {0}/{1} on physicalMax {2} scales to {3}")
    @CsvSource({
            "3200.0, 3200.0, 1024.0, 1024.0",
            "1600.0, 3200.0, 1024.0, 512.0",
            "320.0,  3200.0, 1024.0, 102.4",
            "1.0,    3200.0, 1024.0, 0.32",
            "0.0,    3200.0, 1024.0, 0.0",
            "-10.0,  3200.0, 1024.0, 0.0"
    })
    @DisplayName("Verify calculateScaledPhysicalHealth scales properly and handles death at 0")
    void testCalculateScaledPhysicalHealth(double current, double max, double physicalMax, double expected) {
        double result = MscEntityUtils.calculateScaledPhysicalHealth(current, max, physicalMax);
        assertEquals(expected, result, 1e-9);
    }
}
