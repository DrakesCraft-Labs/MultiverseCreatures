package com.Chagui68.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the maths that gives the dressed bosses a second joint.
 *
 * <p>Two things have to hold for a knee or an elbow to be an improvement rather than a new glitch:
 * the lower segment must stay attached at every angle, and the joint must only ever add to the
 * parent's swing instead of leading it — a knee that bent forward would look broken.
 */
class MscLimbTest {

    private static final float EPS = 1.0e-5f;

    @Test
    @DisplayName("The joint sits halfway between the two segments it separates")
    void theJointSitsBetweenTheSegments() {
        Vector3f joint = MscLimb.jointBetween(new Vector3f(0.1f, 0.7f, 0f), new Vector3f(0.1f, 0.2f, 0f));

        assertEquals(0.1f, joint.x, EPS, "the joint must stay on the limb's own axis");
        assertEquals(0.45f, joint.y, EPS);
        assertEquals(0f, joint.z, EPS);

        // It follows the export: move a segment and the joint moves with it.
        Vector3f moved = MscLimb.jointBetween(new Vector3f(0.1f, 0.5f, 0f), new Vector3f(0.1f, 0.2f, 0f));
        assertEquals(0.35f, moved.y, EPS);
    }

    @Test
    @DisplayName("A still limb rests exactly where the export puts it, second joint and all")
    void aStillLimbRestsWhereTheExportPutsIt() {
        Vector3f rest = new Vector3f(0.1f, 0.3f, 0.05f);
        Vector3f swung = MscLimb.swingAndFold(rest, new Vector3f(0.09f, 0.9f, 0f), new Vector3f(0.09f, 0.45f, 0f),
                new Quaternionf(), new Quaternionf());

        assertEquals(rest.x, swung.x, EPS, "a still limb moved");
        assertEquals(rest.y, swung.y, EPS);
        assertEquals(rest.z, swung.z, EPS);
    }

    @Test
    @DisplayName("The lower segment stays the same distance from the second joint at every angle")
    void theLowerSegmentStaysAttached() {
        Vector3f hip = new Vector3f(0.09f, 0.9f, 0f);
        Vector3f knee = new Vector3f(0.09f, 0.45f, 0f);
        Vector3f shin = new Vector3f(0.09f, 0.23f, 0f);
        float restGap = shin.distance(knee);

        for (float swing = -1.5f; swing <= 1.5f; swing += 0.1f) {
            Quaternionf upper = new Quaternionf().rotateX(swing);
            Quaternionf lower = MscLimb.knee(swing);

            Vector3f movedJoint = MscLimb.swing(knee, hip, new Quaternionf(upper));
            Vector3f movedShin = MscLimb.swingAndFold(shin, hip, knee, upper, lower);

            assertEquals(restGap, movedShin.distance(movedJoint), 1.0e-4f,
                    "the shin came away from its knee at " + swing + " rad");
        }

        // And over the range a walk can reach — the models' stride is at most 0.35 rad, which folds
        // to a 50° knee — the shin stays below its knee instead of folding up through the thigh.
        for (float swing = -0.05f; swing >= -0.4f; swing -= 0.05f) {
            Vector3f movedJoint = MscLimb.swing(knee, hip, new Quaternionf().rotateX(swing));
            Vector3f movedShin = MscLimb.swingAndFold(shin, hip, knee, new Quaternionf().rotateX(swing),
                    MscLimb.knee(swing));

            assertTrue(movedShin.y < movedJoint.y,
                    "the shin ended up above its knee at " + swing + " rad, so the leg folded the wrong way");
        }
    }

    @Test
    @DisplayName("A knee folds on the back half of the swing and an elbow on the forward half")
    void theJointsOnlyBendOneWay() {
        assertRotation(0f, MscLimb.knee(0f), "a leg at the end of its step is straight");
        assertRotation(0f, MscLimb.knee(0.4f), "a knee must not bend forward");
        assertRotation(0f, MscLimb.elbow(0f), "an arm at rest is straight");
        assertRotation(0f, MscLimb.elbow(-0.4f), "an elbow must not bend backwards");

        // The fold is the documented share of the swing, in the direction the limb is already going.
        assertRotation(-0.3f * MscLimb.BEND_PER_SWING, MscLimb.knee(-0.3f), "the knee must fold the way the leg swings");
        assertRotation(0.3f * MscLimb.BEND_PER_SWING, MscLimb.elbow(0.3f), "the elbow must fold the way the arm swings");

        // And a piece below the joint carries both rotations: the swing, then the fold on top.
        Quaternionf thigh = new Quaternionf().rotateX(-0.3f);
        assertRotation(-0.3f - 0.3f * MscLimb.BEND_PER_SWING, MscLimb.chain(thigh, MscLimb.knee(-0.3f)),
                "the shin must add to the thigh's swing instead of replacing it");
    }

    @ParameterizedTest(name = "A {0} rad swing bends by a bounded angle")
    @ValueSource(floats = {-8f, -2f, -0.3f, 0f, 0.3f, 2f, 8f})
    @DisplayName("No fold ever passes the bend limit, however extreme the swing is")
    void noFoldPassesTheBendLimit(float swing) {
        // A fold deeper than the limit would be a second rotation of the whole limb, not a bend.
        for (Quaternionf bend : new Quaternionf[]{MscLimb.knee(swing), MscLimb.elbow(swing)}) {
            assertTrue(Math.abs(xRotationOf(bend)) <= MscLimb.MAX_BEND + EPS,
                    "a " + swing + " rad swing bent the joint by " + xRotationOf(bend));
        }
        // The cap is in the direction the limb is swinging, and a swing the joint ignores stays ignored.
        assertRotation(-MscLimb.MAX_BEND, MscLimb.knee(-8f), "a deep back swing must cap at the limit");
        assertRotation(MscLimb.MAX_BEND, MscLimb.elbow(8f), "a deep forward swing must cap at the limit");
        assertRotation(0f, MscLimb.knee(8f), "a forward swing never bends a knee");
        assertRotation(0f, MscLimb.elbow(-8f), "a back swing never bends an elbow");
    }

    // --- helpers -----------------------------------------------------------------------------------

    /** The angle, in radians, of a rotation about X: positive turns a hanging limb forwards. */
    private static float xRotationOf(Quaternionf rotation) {
        return (float) Math.atan2(2.0 * (rotation.w * rotation.x), 1.0 - 2.0 * rotation.x * rotation.x);
    }

    private static void assertRotation(float expectedRadians, Quaternionf actual, String message) {
        Quaternionf expected = new Quaternionf().rotateX(expectedRadians);
        assertEquals(expected.x, actual.x, EPS, message);
        assertEquals(expected.y, actual.y, EPS, message);
        assertEquals(expected.z, actual.z, EPS, message);
        assertEquals(expected.w, actual.w, EPS, message);
    }
}
