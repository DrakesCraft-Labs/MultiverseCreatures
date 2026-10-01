package com.Chagui68.ritual.terrain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The terrain is only seamless across chunk borders because every field is a pure function of the
 * coordinates, so these tests pin the two properties everything else leans on: the values stay
 * inside the documented range, and the same input always gives the same answer.
 */
class ArenaNoiseTest {

    private static final int SALT = 0x1234_5678;

    @Test
    @DisplayName("The hash is a function of its inputs, and mixes them all in")
    void hashIsDeterministic() {
        assertEquals(ArenaNoise.hash(11, -37, SALT), ArenaNoise.hash(11, -37, SALT));
        assertNotEquals(ArenaNoise.hash(11, -37, SALT), ArenaNoise.hash(11, -37, SALT + 1),
                "the salt must change the field, otherwise two worlds would grow the same wilderness");
        assertNotEquals(ArenaNoise.hash(11, -37, SALT), ArenaNoise.hash(38, -37, SALT));
        assertNotEquals(ArenaNoise.hash(11, -37, SALT), ArenaNoise.hash(11, -38, SALT));
    }

    @Test
    @DisplayName("Coordinates are not symmetric: the field is not mirrored along a diagonal")
    void hashDoesNotMirror() {
        int same = 0;
        for (int value = 1; value <= 100; value++) {
            if (ArenaNoise.hash(value, value * 3, SALT) == ArenaNoise.hash(value * 3, value, SALT)) {
                same++;
            }
        }
        assertTrue(same < 5, "swapping x and z should almost always change the value, matched " + same);
    }

    @Test
    @DisplayName("Every lattice value is in [0, 1)")
    void unitStaysInRange() {
        for (int x = -80; x <= 80; x += 7) {
            for (int z = -80; z <= 80; z += 7) {
                double value = ArenaNoise.unit(x, z, SALT);
                assertTrue(value >= 0.0 && value < 1.0, "unit out of range at " + x + "," + z + ": " + value);
            }
        }
    }

    @Test
    @DisplayName("Value noise, fractal noise and ridges all stay in [0, 1)")
    void fieldsStayInRange() {
        for (int x = -120; x <= 120; x += 11) {
            for (int z = -120; z <= 120; z += 11) {
                assertInRange("value", ArenaNoise.value(x * 0.05, z * 0.05, SALT));
                assertInRange("fbm", ArenaNoise.fbm(x, z, SALT, 0.03, 4));
                assertInRange("ridge", ArenaNoise.ridge(x, z, SALT, 0.02, 4));
            }
        }
    }

    @Test
    @DisplayName("Fractal noise is continuous: neighbouring blocks never jump")
    void fieldsAreContinuous() {
        for (int x = -60; x <= 60; x += 5) {
            double here = ArenaNoise.fbm(x, 0, SALT, 0.009, 4);
            double next = ArenaNoise.fbm(x + 1, 0, SALT, 0.009, 4);
            assertTrue(Math.abs(here - next) < 0.05,
                    "fbm jumped by " + Math.abs(here - next) + " at " + x);
        }
    }

    @Test
    @DisplayName("Smoothstep clamps at both ends and is symmetric in the middle")
    void smoothstepClamps() {
        assertEquals(0.0, ArenaNoise.smoothstep(10.0, 20.0, 5.0));
        assertEquals(0.0, ArenaNoise.smoothstep(10.0, 20.0, 10.0));
        assertEquals(1.0, ArenaNoise.smoothstep(10.0, 20.0, 20.0));
        assertEquals(1.0, ArenaNoise.smoothstep(10.0, 20.0, 25.0));
        assertEquals(0.5, ArenaNoise.smoothstep(10.0, 20.0, 15.0), 1.0e-9);
        assertEquals(0.5, ArenaNoise.smoothstep(20.0, 10.0, 15.0), 1.0e-9,
                "a descending smoothstep is how the ribs fade, so it has to work the other way too");
    }

    @Test
    @DisplayName("Seeds produce different salts")
    void saltsDiffer() {
        assertNotEquals(ArenaNoise.saltOf(1L), ArenaNoise.saltOf(2L));
        assertNotEquals(ArenaNoise.saltOf(1L), ArenaNoise.saltOf(1L + (1L << 32)));
    }

    private static void assertInRange(String name, double value) {
        assertTrue(value >= 0.0 && value < 1.0, name + " out of range: " + value);
    }
}
