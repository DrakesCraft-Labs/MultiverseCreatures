package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The Stand skeleton keeps its joints together and its poses read the way they should. */
class StandRigTest {

    private static final float EPSILON = 1e-4f;

    @Test
    @DisplayName("A bent elbow stays on the arm however the shoulder turns")
    void elbowStaysAttached() {
        for (int beat = 0; beat < 4; beat++) {
            Map<Part, Frame> frames = StandRig.solve(StandRig.barrage(beat), new Vector3f());
            for (Part forearm : List.of(Part.FOREARM_R, Part.FOREARM_L)) {
                Vector3f elbow = forearm.pivot();
                Vector3f onArm = frames.get(forearm.parent()).apply(elbow);
                Vector3f onForearm = frames.get(forearm).apply(elbow);
                assertEquals(0, onArm.distance(onForearm), EPSILON, forearm + " came off its arm");
            }
            Vector3f knee = Part.SHIN_R.pivot();
            assertEquals(0, frames.get(Part.THIGH_R).apply(knee).distance(frames.get(Part.SHIN_R).apply(knee)),
                    EPSILON, "the shin came off the thigh");
        }
    }

    @Test
    @DisplayName("A barrage throws the right fist, then the left, straight ahead")
    void barrageAlternatesFists() {
        Map<Part, Frame> first = StandRig.solve(StandRig.barrage(0), new Vector3f());
        Map<Part, Frame> second = StandRig.solve(StandRig.barrage(1), new Vector3f());
        Vector3f right = StandRig.fist(first, true);
        Vector3f left = StandRig.fist(second, false);
        assertTrue(right.z > 0.55f, "the right fist reaches forward: " + right);
        assertTrue(left.z > 0.55f, "the left fist reaches forward: " + left);
        assertTrue(StandRig.fist(first, false).z < right.z, "the other fist is drawn back");
        assertTrue(right.y > 1.0f && right.y < 1.6f, "punches land at chest height: " + right);
    }

    @Test
    @DisplayName("At rest the fists hang below the head, on their own side")
    void idleKeepsFistsAtTheSides() {
        for (StandType type : StandType.values()) {
            Map<Part, Frame> frames = StandRig.solve(StandRig.idle(type, 0, 0f), new Vector3f());
            Vector3f right = StandRig.fist(frames, true);
            Vector3f left = StandRig.fist(frames, false);
            assertTrue(right.x < 0 && left.x > 0, type + " crossed its arms: " + right + " " + left);
            assertTrue(right.y < 1.75f && left.y < 1.75f, type + " fists above the head");
        }
    }

    @Test
    @DisplayName("The head follows where its user looks, within a limit")
    void headFollowsPitch() {
        Quaternionf down = StandRig.idle(StandType.STAR_PLATINUM, 0, 60f).get(Part.HEAD);
        Vector3f nose = new Vector3f(0, 0, 1).rotate(down);
        assertTrue(nose.y < -0.3f, "looking down tilts the head down");
        assertTrue(nose.y > -0.65f, "the tilt is clamped");
    }

    @Test
    @DisplayName("Every Stand with a body is a detailed figure, mirrored on both sides")
    void designsAreDetailedAndSymmetric() {
        for (StandType type : StandType.values()) {
            List<StandDesign.Piece> pieces = StandDesign.of(type);
            if (!type.hasBody()) {
                assertTrue(pieces.isEmpty(), "Hermit Purple has no body");
                continue;
            }
            assertTrue(pieces.size() >= 60, type + " has only " + pieces.size() + " pieces");
            for (StandDesign.Piece piece : pieces) {
                String side = piece.part().name();
                if (side.endsWith("_R")) {
                    assertTrue(piece.center().x < 0.05f, type + " right piece on the left: " + piece);
                } else if (side.endsWith("_L")) {
                    assertTrue(piece.center().x > -0.05f, type + " left piece on the right: " + piece);
                }
                assertTrue(piece.size().x > 0 && piece.size().y > 0 && piece.size().z > 0, "empty box " + piece);
            }
        }
    }
}
