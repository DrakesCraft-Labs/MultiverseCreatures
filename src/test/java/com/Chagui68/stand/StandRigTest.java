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
    @DisplayName("Every Stand with a body, and DIO, ships an eleven-head model the plugin can read")
    void everyBodyShipsAHeadModel() throws Exception {
        List<String> models = new java.util.ArrayList<>();
        for (StandType type : StandType.values()) {
            if (type.hasBody()) {
                models.add(type.key());
            }
        }
        models.add("dio-brando");
        for (String name : models) {
            try (java.io.InputStream in = StandRigTest.class.getResourceAsStream("/stands/" + name + ".txt")) {
                assertNotNull(in, name + " has no model in stands/");
                HeadModel model = HeadModel.parse(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                assertEquals(11, model.pieces().size(), name);
            }
        }
    }
}
