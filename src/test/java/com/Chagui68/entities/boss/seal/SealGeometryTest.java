package com.Chagui68.entities.boss.seal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The seals are drawn particle by particle, so these tests are written as invariants of the shape
 * rather than as a picture: a circle keeps its radius, a pentagram is five chords that visit every
 * vertex twice, a star ring closes on itself. If a constant drifts, the invariant breaks and the
 * failure names the seal it broke.
 */
class SealGeometryTest {

    private static final double EPS = 1.0e-9;

    @Test
    @DisplayName("A circle keeps its radius, its count and its even spacing")
    void aCircleKeepsItsRadius() {
        int samples = 16;
        double rotation = 0.7;
        List<SealPoint> points = SealGeometry.circle(4.0, samples, rotation);
        assertEquals(samples, points.size());
        double step = 2 * Math.PI / samples;
        for (int i = 0; i < samples; i++) {
            assertEquals(4.0, points.get(i).length(), EPS, "sample " + i + " left the circle");
            assertEquals(4.0 * Math.cos(i * step + rotation), points.get(i).u(), EPS);
            assertEquals(4.0 * Math.sin(i * step + rotation), points.get(i).v(), EPS);
        }
    }

    @Test
    @DisplayName("A pentagram is five chords, each one skipping a vertex")
    void aPentagramIsFiveChords() {
        double radius = 6.0;
        List<List<SealPoint>> chords = SealGeometry.pentagram(radius, 10, Math.PI / 2);
        assertEquals(5, chords.size(), "a pentagram has five chords");

        double chordLength = 2 * radius * Math.sin(Math.toRadians(72));
        for (List<SealPoint> chord : chords) {
            assertEquals(11, chord.size(), "each chord is sampled end to end");
            assertEquals(chordLength, distance(chord.get(0), chord.get(chord.size() - 1)), EPS,
                    "every chord spans two vertices of the pentagon");
        }

        List<SealPoint> vertices = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI / 2 + i * 2 * Math.PI / 5;
            vertices.add(new SealPoint(radius * Math.cos(angle), radius * Math.sin(angle)));
        }
        for (SealPoint vertex : vertices) {
            int ends = 0;
            for (List<SealPoint> chord : chords) {
                if (near(vertex, chord.get(0)) || near(vertex, chord.get(chord.size() - 1))) ends++;
            }
            assertEquals(2, ends, "every vertex closes exactly two chords at " + vertex
                    + "; four means a chord is drawn twice");
        }
    }

    @Test
    @DisplayName("The pentagram starts at the angle the caller asked for")
    void thePentagramStartsWhereAsked() {
        double radius = 4.0;
        SealPoint top = SealGeometry.pentagram(radius, 4, Math.PI / 2).get(0).get(0);
        assertEquals(0, top.u(), EPS);
        assertEquals(radius, top.v(), EPS, "start angle PI/2 points the first vertex up");

        SealPoint east = SealGeometry.pentagram(radius, 4, 0).get(0).get(0);
        assertEquals(radius, east.u(), EPS);
        assertEquals(0, east.v(), EPS);
    }

    @Test
    @DisplayName("A triangle divides its sample budget over three equal edges")
    void aTriangleDividesItsBudget() {
        double radius = 6.5;
        List<List<SealPoint>> edges = SealGeometry.triangle(radius, 120, 0.3);
        assertEquals(3, edges.size());
        for (List<SealPoint> edge : edges) {
            assertEquals(41, edge.size(), "120 samples split into three edges of 40");
            assertEquals(radius * Math.sqrt(3), distance(edge.get(0), edge.get(edge.size() - 1)), EPS,
                    "an inscribed triangle's side is radius times the square root of three");
        }
        assertEquals(2, SealGeometry.triangle(radius, 2, 0).get(0).size(),
                "a tiny budget still draws one point at each end");
    }

    @Test
    @DisplayName("A star ring zigs between the two radii and closes on itself")
    void aStarRingZigsBetweenTheTwoRadii() {
        double radius = 5.0;
        List<List<SealPoint>> segments = SealGeometry.starRing(radius, 60, 0.2);
        assertEquals(12, segments.size(), "six outer points, twelve segments");
        for (List<SealPoint> segment : segments) {
            assertEquals(6, segment.size(), "60 samples split into twelve segments of 5");
            double start = segment.get(0).length();
            double end = segment.get(segment.size() - 1).length();
            assertTrue((near(start, radius) && near(end, radius * SealGeometry.INNER_STAR))
                            || (near(start, radius * SealGeometry.INNER_STAR) && near(end, radius)),
                    "a segment runs between the outer ring and the inner star point");
        }
        assertEquals(0, distance(segments.get(0).get(0), segments.get(11).get(5)), EPS,
                "the ring has to close where it started");
    }

    @Test
    @DisplayName("A rune band keeps every point inside its two radii")
    void runesStayInTheirBand() {
        Random random = new Random(42);
        List<SealPoint> points = SealGeometry.runes(4.5, 5.5, 100, random);
        assertEquals(100, points.size());
        for (SealPoint point : points) {
            assertTrue(point.length() >= 4.5 - EPS && point.length() <= 5.5 + EPS,
                    "a rune left the band at radius " + point.length());
        }
        assertEquals(points, SealGeometry.runes(4.5, 5.5, 100, new Random(42)),
                "a seeded run has to reproduce the same band");
    }

    @Test
    @DisplayName("Spokes run from their inner ring to their outer one")
    void spokesRunOutwards() {
        List<List<SealPoint>> spokes = SealGeometry.spokes(1.8, 4.5, 6, 0.1, 8);
        assertEquals(6, spokes.size());
        for (List<SealPoint> spoke : spokes) {
            assertEquals(9, spoke.size());
            assertEquals(1.8, spoke.get(0).length(), EPS);
            assertEquals(4.5, spoke.get(spoke.size() - 1).length(), EPS);
        }
    }

    @Test
    @DisplayName("The executioner's cross is two perpendicular diagonals through the centre")
    void theCrossIsTwoDiagonals() {
        double size = 2.4;
        List<List<SealPoint>> diagonals = SealGeometry.cross(size, 0, 26);
        assertEquals(2, diagonals.size());
        for (List<SealPoint> diagonal : diagonals) {
            assertEquals(27, diagonal.size());
            assertEquals(size * Math.sqrt(2), diagonal.get(0).length(), EPS);
            assertEquals(size * Math.sqrt(2), diagonal.get(26).length(), EPS);
            assertEquals(0, diagonal.get(13).length(), EPS, "the diagonals cross at the centre");
        }
        double[] first = direction(diagonals.get(0));
        double[] second = direction(diagonals.get(1));
        assertEquals(0, first[0] * second[0] + first[1] * second[1], EPS, "the arms must be perpendicular");

        SealPoint turned = SealGeometry.cross(size, Math.PI / 2, 26).get(0).get(0);
        SealPoint expected = SealGeometry.rotate(diagonals.get(0).get(0).u(), diagonals.get(0).get(0).v(),
                Math.PI / 2);
        assertEquals(expected.u(), turned.u(), EPS);
        assertEquals(expected.v(), turned.v(), EPS);
    }

    @Test
    @DisplayName("Chords and world lines sample both ends")
    void linesSampleBothEnds() {
        List<SealPoint> chord = SealGeometry.chord(new SealPoint(0, 0), new SealPoint(4, 2), 4);
        assertEquals(5, chord.size());
        assertEquals(0, chord.get(0).u(), EPS);
        assertEquals(4, chord.get(4).u(), EPS);
        assertEquals(2, chord.get(2).u(), EPS);
        assertEquals(1, chord.get(2).v(), EPS);

        List<double[]> line = SealGeometry.line(new double[]{1, 2, 3}, new double[]{5, 4, 3}, 2);
        assertEquals(3, line.size());
        assertArrayEquals(new double[]{3, 3, 3}, line.get(1), EPS);
    }

    @Test
    @DisplayName("Densities have floors and grow with the seal")
    void densitiesHaveFloors() {
        assertEquals(60, SealGeometry.pentagramSamples(3), "a small star still gets a legible line");
        assertEquals(60, SealGeometry.pentagramSamples(6));
        assertEquals(120, SealGeometry.pentagramSamples(12));
        assertEquals(220, SealGeometry.ringSamples(6));
        assertEquals(440, SealGeometry.ringSamples(12));
        assertEquals(28, SealGeometry.auraCount(6));
        assertEquals(56, SealGeometry.auraCount(12));
        assertEquals(4.2, SealGeometry.auraRadius(10), EPS);
        assertEquals(12.4, SealGeometry.enclosingRingRadius(10), EPS);
        assertTrue(SealGeometry.enclosingRingRadius(6) > 6, "the ring has to enclose the star it frames");
    }

    @Test
    @DisplayName("A celestial seal is bigger when it stands, with its proportions intact")
    void aVerticalCelestialIsBigger() {
        SealGeometry.CelestialRadii flat = SealGeometry.celestialRadii(SealPlane.XZ);
        assertEquals(3.0, flat.outer(), EPS);
        assertEquals(2.0, flat.middle(), EPS);
        assertEquals(2.5, flat.star(), EPS);
        assertEquals(1.4, flat.triangle(), EPS);
        for (SealPlane vertical : List.of(SealPlane.XY, SealPlane.YZ)) {
            SealGeometry.CelestialRadii raised = SealGeometry.celestialRadii(vertical);
            assertEquals(SealGeometry.VERTICAL_SCALE, raised.outer() / flat.outer(), EPS,
                    vertical + " did not get the vertical scale");
            assertEquals(flat.outer() / flat.middle(), raised.outer() / raised.middle(), EPS,
                    "scaling must not change the proportions");
        }
    }

    @Test
    @DisplayName("The shield column never collapses and the spiral stays inside it")
    void theShieldColumnHoldsItsSpiral() {
        assertEquals(10.0, SealGeometry.cylinderHeight(20, 10), EPS);
        assertEquals(0.5, SealGeometry.cylinderHeight(10.4, 10), EPS,
                "a shield resting on the ground still gets half a block of column");
        double height = 7.5;
        for (int index = 0; index < 40; index++) {
            for (double phase : new double[]{0, 1.3, 7.4}) {
                double y = SealGeometry.spiralHeight(index, 0.3, phase, height);
                assertEquals((index * 0.3 + phase) % height, y, EPS);
                assertTrue(y >= 0 && y < height, "sample " + index + " left the column at " + y);
            }
        }
    }

    @Test
    @DisplayName("The wandering rings stay in the band they document")
    void theWanderingRingsStayInTheirBand() {
        for (int step = 0; step <= 1000; step++) {
            double phase = step * 2 * Math.PI / 1000;
            double vortex = SealGeometry.vortexRadius(4.0, phase);
            assertTrue(vortex >= 1.52 - EPS && vortex <= 2.48 + EPS, "vortex left its band at " + vortex);
            double quake = SealGeometry.quakeRadius(5.0, phase);
            assertTrue(quake >= 3.5 - EPS && quake <= 4.5 + EPS, "quake left its band at " + quake);
            double divine = SealGeometry.divineRadius(5.0, phase);
            assertTrue(divine >= 3.45 - EPS && divine <= 4.05 + EPS, "divine left its band at " + divine);
            double scale = SealGeometry.crossScale(2.4, step);
            assertTrue(scale >= 2.208 - EPS && scale <= 2.592 + EPS, "the cross pulse left its band");
        }
    }

    @Test
    @DisplayName("The timing rules keep their boundaries")
    void timingRulesKeepTheirBoundaries() {
        assertTrue(SealGeometry.every(0, 8));
        assertTrue(SealGeometry.every(16, 8));
        assertFalse(SealGeometry.every(9, 8));
        assertFalse(SealGeometry.inFinalWindow(95, 100, 4));
        assertTrue(SealGeometry.inFinalWindow(96, 100, 4));
        assertEquals(0.5, SealGeometry.progress(50, 100), EPS);
        assertFalse(SealGeometry.latePhase(0.7, 0.7), "the bright phase starts after the threshold");
        assertTrue(SealGeometry.latePhase(0.7, 0.6));
    }

    @Test
    @DisplayName("The yaw turns the seal without stretching it")
    void theYawTurnsTheSeal() {
        double[] straight = SealGeometry.yawOffset(3.0, 0);
        assertEquals(3.0, straight[0], EPS);
        assertEquals(0, straight[1], EPS);
        double[] quarter = SealGeometry.yawOffset(3.0, 90);
        assertEquals(0, quarter[0], EPS);
        assertEquals(-3.0, quarter[1], EPS, "the +u axis swings a quarter turn with the yaw");
        for (float yaw : new float[]{-180, -37, 0, 37, 180}) {
            double[] offset = SealGeometry.yawOffset(3.0, yaw);
            assertEquals(3.0, Math.hypot(offset[0], offset[1]), EPS, "a turn must not change the length");
            double[] centre = SealGeometry.yawOffset(0, yaw);
            assertEquals(0, Math.hypot(centre[0], centre[1]), EPS, "the centre stays at the centre");
        }
    }

    private static boolean near(SealPoint first, SealPoint second) {
        return distance(first, second) < EPS;
    }

    private static boolean near(double first, double second) {
        return Math.abs(first - second) < EPS;
    }

    private static double distance(SealPoint first, SealPoint second) {
        return Math.hypot(first.u() - second.u(), first.v() - second.v());
    }

    private static double[] direction(List<SealPoint> segment) {
        SealPoint from = segment.get(0);
        SealPoint to = segment.get(segment.size() - 1);
        double length = distance(from, to);
        return new double[]{(to.u() - from.u()) / length, (to.v() - from.v()) / length};
    }
}
