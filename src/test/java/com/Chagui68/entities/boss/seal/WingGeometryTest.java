package com.Chagui68.entities.boss.seal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wings are the one seal that is not a flat shape, so the invariants here are about the body:
 * each wing is a bone with feathers hanging from it, the two wings mirror each other, a turned
 * stand carries its wings around, and a flap turns a wing about its root without stretching it.
 */
class WingGeometryTest {

    private static final double EPS = 1.0e-9;
    private static final List<WingGeometry.Profile> PROFILES =
            List.of(WingGeometry.GOLDEN_WINGS, WingGeometry.BURNING_WINGS);

    @Test
    @DisplayName("Each wing is a bone from its root, with every feather starting on the bone")
    void feathersHangFromTheBone() {
        for (WingGeometry.Profile profile : PROFILES) {
            List<WingGeometry.Stroke> strokes = WingGeometry.strokes(0, 10, 0, 0, 5, profile);
            assertEquals(2 * (1 + 2 * profile.feathers()), strokes.size());
            for (int side = 0; side < 2; side++) {
                int first = side * (1 + 2 * profile.feathers());
                WingGeometry.Stroke bone = strokes.get(first);
                assertEquals(WingGeometry.Part.BONE, bone.part());
                WingPoint root = WingGeometry.root(0, 10, 0, 0, side == 0 ? -1 : 1, profile);
                assertEquals(0, distance(root, bone.points().get(0)), EPS, "the bone leaves the wing's root");
                for (int k = first + 1; k <= first + 2 * profile.feathers(); k++) {
                    WingGeometry.Stroke feather = strokes.get(k);
                    assertTrue(feather.part() != WingGeometry.Part.BONE);
                    WingPoint start = feather.points().get(0);
                    assertTrue(nearest(start, bone.points()) < profile.length() * 0.1,
                            "a feather has to start on the bone, not in the air");
                    for (WingPoint p : feather.points()) {
                        assertTrue(Double.isFinite(p.x()) && Double.isFinite(p.y()) && Double.isFinite(p.z()));
                    }
                    WingPoint tip = feather.points().get(feather.points().size() - 1);
                    assertTrue(tip.y() < start.y(), "feathers hang down from the bone");
                }
            }
        }
    }

    @Test
    @DisplayName("The wings sit behind the body and reach out to either side")
    void theWingsSpreadFromTheBack() {
        for (WingGeometry.Profile profile : PROFILES) {
            List<WingGeometry.Stroke> strokes = WingGeometry.strokes(0, 0, 0, 0, 0, profile);
            WingPoint root = strokes.get(0).points().get(0);
            // Yaw 0 faces +z, so the back is -z.
            assertTrue(root.z() < 0, "the roots are on the back, not inside the chest");
            List<WingPoint> bone = strokes.get(0).points();
            WingPoint tip = bone.get(bone.size() - 1);
            assertTrue(Math.abs(tip.x()) > profile.length() * 0.8, "a spread wing reaches out sideways");
            assertTrue(tip.y() > root.y(), "the wing rises from its root");
        }
    }

    @Test
    @DisplayName("The two wings mirror each other across the body")
    void theWingsMirror() {
        for (WingGeometry.Profile profile : PROFILES) {
            for (int ticks : new int[]{0, 7}) {
                List<WingGeometry.Stroke> strokes = WingGeometry.strokes(4, 10, -3, 0, ticks, profile);
                int half = strokes.size() / 2;
                for (int k = 0; k < half; k++) {
                    List<WingPoint> left = strokes.get(k).points();
                    List<WingPoint> right = strokes.get(half + k).points();
                    for (int i = 0; i < left.size(); i++) {
                        assertEquals(8 - left.get(i).x(), right.get(i).x(), EPS, "stroke " + k + " is not mirrored");
                        assertEquals(left.get(i).y(), right.get(i).y(), EPS);
                        assertEquals(left.get(i).z(), right.get(i).z(), EPS);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("A stand that turns carries its wings: the cloud rotates with the yaw")
    void theWingsFollowTheYaw() {
        WingGeometry.Profile profile = WingGeometry.GOLDEN_WINGS;
        List<WingGeometry.Stroke> straight = WingGeometry.strokes(1, 8, 2, 0, 3, profile);
        List<WingGeometry.Stroke> turned = WingGeometry.strokes(1, 8, 2, 90, 3, profile);
        for (int k = 0; k < straight.size(); k++) {
            for (int i = 0; i < straight.get(k).points().size(); i++) {
                WingPoint before = straight.get(k).points().get(i);
                WingPoint after = turned.get(k).points().get(i);
                // A quarter turn takes an (x, z) offset to (-z, x).
                assertEquals(1 - (before.z() - 2), after.x(), EPS, "the wing did not turn with the stand");
                assertEquals(2 + (before.x() - 1), after.z(), EPS, "the wing did not turn with the stand");
                assertEquals(before.y(), after.y(), EPS, "a turn must not lift the wings");
            }
        }
    }

    @Test
    @DisplayName("The flap turns each wing rigidly about its root")
    void theFlapRotatesWithoutStretching() {
        WingGeometry.Profile profile = WingGeometry.BURNING_WINGS;
        List<WingGeometry.Stroke> rest = WingGeometry.strokes(0, 10, 0, 0, 0, profile);
        List<WingGeometry.Stroke> flapped = WingGeometry.strokes(0, 10, 0, 0, 18, profile);
        WingPoint root = rest.get(0).points().get(0);
        boolean moved = false;
        int half = rest.size() / 2;
        for (int k = 0; k < half; k++) {
            for (int i = 0; i < rest.get(k).points().size(); i++) {
                WingPoint still = rest.get(k).points().get(i);
                WingPoint beat = flapped.get(k).points().get(i);
                assertEquals(distance(root, still), distance(root, beat), 1e-6, "the flap turns the wing rigidly");
                if (Math.abs(still.y() - beat.y()) > 1e-6) moved = true;
            }
        }
        assertTrue(moved, "a non-zero flap amplitude has to move the wings");
    }

    @Test
    @DisplayName("The flap is the profile's sine")
    void theFlapIsTheProfilesSine() {
        for (WingGeometry.Profile profile : PROFILES) {
            for (int ticks : new int[]{0, 5, 17, 41}) {
                assertEquals(Math.sin(ticks * profile.flapSpeed()) * profile.flapAmplitude(),
                        WingGeometry.flap(ticks, profile), EPS);
            }
            assertEquals(0, WingGeometry.flap(0, profile), EPS, "the rest pose is not flapped");
        }
    }

    @Test
    @DisplayName("The burning wings are the longer, fuller, slower pair")
    void profilesAreOrdered() {
        WingGeometry.Profile golden = WingGeometry.GOLDEN_WINGS;
        WingGeometry.Profile burning = WingGeometry.BURNING_WINGS;
        assertTrue(burning.feathers() > golden.feathers());
        assertTrue(burning.length() > golden.length());
        assertTrue(burning.featherLength() > golden.featherLength());
        assertTrue(burning.flapSpeed() < golden.flapSpeed(), "the bigger wings beat slower");
        assertTrue(burning.flapAmplitude() > golden.flapAmplitude());
    }

    private static double nearest(WingPoint p, List<WingPoint> line) {
        double best = Double.MAX_VALUE;
        for (WingPoint q : line) best = Math.min(best, distance(p, q));
        return best;
    }

    private static double distance(WingPoint first, WingPoint second) {
        return Math.sqrt(Math.pow(first.x() - second.x(), 2)
                + Math.pow(first.y() - second.y(), 2)
                + Math.pow(first.z() - second.z(), 2));
    }
}
