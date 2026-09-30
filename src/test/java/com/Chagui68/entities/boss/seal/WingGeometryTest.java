package com.Chagui68.entities.boss.seal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wings are the one seal that is not a flat shape, so the invariants here are about the body:
 * every feather starts at its shoulder, the two wings mirror each other, a turned stand carries its
 * wings around, and a flap rotates them without stretching them.
 */
class WingGeometryTest {

    private static final double EPS = 1.0e-9;
    private static final List<WingGeometry.Profile> PROFILES =
            List.of(WingGeometry.GOLDEN_WINGS, WingGeometry.BURNING_WINGS);

    @Test
    @DisplayName("Every feather runs from its shoulder to a reachable, finite tip")
    void feathersRunFromShoulderToTip() {
        for (WingGeometry.Profile profile : PROFILES) {
            List<List<WingPoint>> wings = WingGeometry.feathers(0, 10, 0, 0, 5, profile);
            assertEquals(2 * profile.feathers(), wings.size());
            for (List<WingPoint> feather : wings) {
                assertEquals(profile.samples() + 1, feather.size());
                WingPoint shoulder = feather.get(0);
                assertEquals(10 + profile.shoulderHeight(), shoulder.y(), EPS);
                assertEquals(profile.shoulderWidth(), Math.abs(shoulder.x()), EPS,
                        "the shoulders sit half a span either side of the body");
                assertEquals(0, shoulder.z(), EPS);

                WingPoint tip = feather.get(feather.size() - 1);
                assertTrue(Double.isFinite(tip.x()) && Double.isFinite(tip.y()) && Double.isFinite(tip.z()),
                        "a profile evaluated to NaN — the last feather's sine runs negative");
                assertTrue(Math.abs(tip.y() - shoulder.y()) <= profile.length() + profile.reach() + EPS);
                assertTrue(Math.abs(tip.z() - shoulder.z()) <= profile.length() + profile.reach() + EPS);
            }
        }
    }

    @Test
    @DisplayName("The two wings mirror each other across the body")
    void theWingsMirror() {
        for (WingGeometry.Profile profile : PROFILES) {
            for (int ticks : new int[]{0, 7}) {
                List<List<WingPoint>> wings = WingGeometry.feathers(4, 10, -3, 0, ticks, profile);
                for (int feather = 0; feather < profile.feathers(); feather++) {
                    List<WingPoint> left = wings.get(feather);
                    List<WingPoint> right = wings.get(profile.feathers() + feather);
                    for (int sample = 0; sample < left.size(); sample++) {
                        assertEquals(8 - left.get(sample).x(), right.get(sample).x(), EPS,
                                "feather " + feather + " sample " + sample + " is not mirrored");
                        assertEquals(left.get(sample).y(), right.get(sample).y(), EPS);
                        assertEquals(left.get(sample).z(), right.get(sample).z(), EPS);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("A stand that turns carries its wings: the cloud rotates with the yaw")
    void theWingsFollowTheYaw() {
        WingGeometry.Profile profile = WingGeometry.GOLDEN_WINGS;
        List<List<WingPoint>> straight = WingGeometry.feathers(1, 8, 2, 0, 3, profile);
        List<List<WingPoint>> turned = WingGeometry.feathers(1, 8, 2, 90, 3, profile);
        for (int feather = 0; feather < straight.size(); feather++) {
            for (int sample = 0; sample < straight.get(feather).size(); sample++) {
                WingPoint before = straight.get(feather).get(sample);
                WingPoint after = turned.get(feather).get(sample);
                // A quarter turn takes an (x, z) offset to (-z, x).
                assertEquals(1 - (before.z() - 2), after.x(), EPS, "the wing did not turn with the stand");
                assertEquals(2 + (before.x() - 1), after.z(), EPS, "the wing did not turn with the stand");
                assertEquals(before.y(), after.y(), EPS, "a turn must not lift the wings");
            }
        }
    }

    @Test
    @DisplayName("The flap rotates the wings and never stretches them")
    void theFlapRotatesWithoutStretching() {
        WingGeometry.Profile profile = WingGeometry.GOLDEN_WINGS;
        List<List<WingPoint>> rest = WingGeometry.feathers(0, 10, 0, 0, 0, profile);
        List<List<WingPoint>> flapped = WingGeometry.feathers(0, 10, 0, 0, 18, profile);
        boolean moved = false;
        for (int feather = 0; feather < rest.size(); feather++) {
            List<WingPoint> still = rest.get(feather);
            List<WingPoint> movedFeather = flapped.get(feather);
            WingPoint shoulder = still.get(0);
            WingPoint restTip = still.get(still.size() - 1);
            WingPoint flapTip = movedFeather.get(movedFeather.size() - 1);
            assertEquals(distance(shoulder, restTip), distance(shoulder, flapTip), EPS,
                    "the flap turns the feather rigidly");
            if (Math.abs(restTip.y() - flapTip.y()) > 1.0e-6) moved = true;
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
        assertTrue(burning.reach() > golden.reach());
        assertTrue(burning.flapSpeed() < golden.flapSpeed(), "the bigger wings beat slower");
        assertTrue(burning.flapAmplitude() > golden.flapAmplitude());
    }

    @Test
    @DisplayName("Profiles describe real wings")
    void profilesAreSane() {
        for (WingGeometry.Profile profile : PROFILES) {
            assertTrue(profile.feathers() >= 2, "a wing needs room for a taper");
            assertTrue(profile.samples() >= 1);
            assertTrue(profile.shoulderHeight() > 0 && profile.shoulderWidth() > 0);
            assertTrue(profile.length() > 0 && profile.reach() >= 0);
            assertTrue(profile.spreadSpan() > 0);
            assertTrue(profile.lengthExponent() >= 1);
            assertTrue(profile.flapAmplitude() > 0 && profile.flapSpeed() > 0);
            assertTrue(profile.flapFalloff() >= 0 && profile.flapFalloff() < 1);
        }
    }

    private static double distance(WingPoint first, WingPoint second) {
        return Math.sqrt(Math.pow(first.x() - second.x(), 2)
                + Math.pow(first.y() - second.y(), 2)
                + Math.pow(first.z() - second.z(), 2));
    }
}
