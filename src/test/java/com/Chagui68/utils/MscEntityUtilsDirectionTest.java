package com.Chagui68.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: JackStar's Three-Slash knocked back a player standing exactly where the boss stood.
 * {@code subtract(...).normalize()} of a zero vector is NaN and {@code setVelocity} threw
 * "x not finite", killing the boss's tick task.
 */
class MscEntityUtilsDirectionTest {

    @Test
    @DisplayName("Two coinciding points give the fallback, never NaN")
    void coincidingPointsUseTheFallback() {
        Vector at = new Vector(10, 64, -3);
        Vector dir = MscEntityUtils.direction(at, at.clone(), new Vector(0, 0, 1));
        assertEquals(new Vector(0, 0, 1), dir);
    }

    @Test
    @DisplayName("Distinct points give a unit vector towards the target")
    void distinctPointsNormalise() {
        Vector dir = MscEntityUtils.direction(new Vector(0, 0, 0), new Vector(3, 0, 4), new Vector(1, 0, 0));
        assertEquals(0.6, dir.getX(), 1e-9);
        assertEquals(0.8, dir.getZ(), 1e-9);
    }

    @Test
    @DisplayName("Horizontal direction ignores height and stays finite on the same block column")
    void horizontalStaysFinite() {
        Location boss = new Location(null, 5, 70, 5, 90f, 0f);
        Location player = new Location(null, 5, 72, 5);
        Vector dir = MscEntityUtils.horizontalDirection(boss, player);
        assertTrue(Double.isFinite(dir.getX()) && Double.isFinite(dir.getY()) && Double.isFinite(dir.getZ()));
        assertEquals(0.0, dir.getY(), 1e-9);
        assertEquals(1.0, dir.length(), 1e-9);
    }
}
