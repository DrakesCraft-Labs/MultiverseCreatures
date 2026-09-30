package com.Chagui68.entities.boss.seal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The plane mapping is the whole difference between a seal on the floor and a seal in the air, and
 * it is the one piece of drawing every other test depends on. These tests pin it as an isometry:
 * a shape never changes size or shape when it changes plane, only its axes do.
 */
class SealPlaneTest {

    private static final double EPS = 1.0e-9;

    @Test
    @DisplayName("A flat seal keeps one height; a vertical one keeps one axis")
    void eachPlaneKeepsItsOwnAxis() {
        double[] flat = SealPlane.XZ.toWorld(10, 20, 30, 1, 2);
        assertEquals(11, flat[0], EPS);
        assertEquals(20, flat[1], EPS, "a flat seal lies at its centre's height");
        assertEquals(32, flat[2], EPS);

        double[] upright = SealPlane.XY.toWorld(10, 20, 30, 1, 2);
        assertEquals(11, upright[0], EPS);
        assertEquals(22, upright[1], EPS);
        assertEquals(30, upright[2], EPS, "an XY seal keeps its centre's Z");

        double[] sideways = SealPlane.YZ.toWorld(10, 20, 30, 1, 2);
        assertEquals(10, sideways[0], EPS, "a YZ seal keeps its centre's X");
        assertEquals(21, sideways[1], EPS);
        assertEquals(32, sideways[2], EPS);

        assertFalse(SealPlane.XZ.vertical());
        assertTrue(SealPlane.XY.vertical());
        assertTrue(SealPlane.YZ.vertical());
    }

    @Test
    @DisplayName("The normal offset leaves the plane along its own normal")
    void theNormalOffsetLeavesThePlane() {
        double[] flat = SealPlane.XZ.toWorld(10, 20, 30, 1, 2, 0.06);
        assertEquals(20.06, flat[1], EPS, "XZ's normal is up");
        assertEquals(11, flat[0], EPS, "and the offset does not move the in-plane point");
        assertEquals(32, flat[2], EPS);

        assertEquals(30.06, SealPlane.XY.toWorld(10, 20, 30, 1, 2, 0.06)[2], EPS, "XY's normal is Z");
        assertEquals(10.06, SealPlane.YZ.toWorld(10, 20, 30, 1, 2, 0.06)[0], EPS, "YZ's normal is X");
    }

    @Test
    @DisplayName("A ring maps to a ring: the radius and the plane survive")
    void aRingStaysARing() {
        double radius = 3.0;
        for (SealPlane plane : SealPlane.values()) {
            for (SealPoint point : SealGeometry.circle(radius, 16, 0.3)) {
                double[] at = plane.toWorld(5, 7, -2, point.u(), point.v());
                double measured = switch (plane) {
                    case XZ -> Math.hypot(at[0] - 5, at[2] + 2);
                    case XY -> Math.hypot(at[0] - 5, at[1] - 7);
                    case YZ -> Math.hypot(at[1] - 7, at[2] + 2);
                };
                double fixed = switch (plane) {
                    case XZ -> at[1];
                    case XY -> at[2];
                    case YZ -> at[0];
                };
                double kept = switch (plane) {
                    case XZ -> 7;
                    case XY -> -2;
                    case YZ -> 5;
                };
                assertEquals(radius, measured, EPS, plane + " tilted the ring");
                assertEquals(kept, fixed, EPS, plane + " let the ring leave its plane");
            }
        }
    }

    @Test
    @DisplayName("Distances survive the mapping: no plane stretches a shape")
    void theMappingIsAnIsometry() {
        SealPoint first = new SealPoint(0, 0);
        SealPoint second = new SealPoint(3, 4);
        for (SealPlane plane : List.of(SealPlane.XZ, SealPlane.XY, SealPlane.YZ)) {
            double[] a = plane.toWorld(0, 0, 0, first.u(), first.v());
            double[] b = plane.toWorld(0, 0, 0, second.u(), second.v());
            assertEquals(5.0, Math.sqrt(square(a[0] - b[0]) + square(a[1] - b[1]) + square(a[2] - b[2])),
                    EPS, plane + " changed the distance between two points");
        }
    }

    private static double square(double value) {
        return value * value;
    }
}
