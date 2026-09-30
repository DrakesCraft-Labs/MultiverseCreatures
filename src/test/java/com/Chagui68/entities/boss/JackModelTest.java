package com.Chagui68.entities.boss;

import com.Chagui68.testsupport.LimbGeometry;
import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the model itself: eleven parts, each in its own place, joints on the correct side and the
 * whole body centred on the invisible armour stand that receives the hits.
 *
 * <p>The reference numbers are the passenger transforms of the model as built in game
 * ({@code /summon block_display ... {Passengers:[item_display x11]}}). The plugin keeps the same
 * shape with one difference: the reference data carries a global X offset for the whole body, and
 * the code re-centres it so the visible body sits over the hitbox. These tests pin both halves of
 * that contract.
 */
class JackModelTest {

    /**
     * Reference model, straight from the in-game passengers: one entry per part, in the order the
     * game wrote them. Only the translation matters here; the full matrices live in JackPart.
     */
    private static final Map<JackStarBoss.JackPart, Vector3f> REFERENCE = new EnumMap<>(JackStarBoss.JackPart.class);

    static {
        REFERENCE.put(JackStarBoss.JackPart.HEAD, new Vector3f(-0.9301796875f, 1.872775625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.TORSO_UPPER, new Vector3f(-0.9301796875f, 1.404275625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.TORSO_LOWER, new Vector3f(-0.9301796875f, 1.170025625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.LEG_R_UPPER, new Vector3f(-1.0473046875f, 0.701525625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.LEG_R_LOWER, new Vector3f(-1.0473046875f, 0.467275625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.LEG_L_UPPER, new Vector3f(-0.8130546875f, 0.701525625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.LEG_L_LOWER, new Vector3f(-0.8130546875f, 0.467275625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.ARM_R_UPPER, new Vector3f(-0.5823184375f, 1.404275625f, -0.0165821875f));
        REFERENCE.put(JackStarBoss.JackPart.ARM_R_LOWER, new Vector3f(-0.586125f, 1.170025625f, -0.016875f));
        REFERENCE.put(JackStarBoss.JackPart.ARM_L_UPPER, new Vector3f(-1.2815546875f, 1.404275625f, -0.016851575f));
        REFERENCE.put(JackStarBoss.JackPart.ARM_L_LOWER, new Vector3f(-1.2815546875f, 1.170025625f, -0.016851575f));
    }

    @Test
    @DisplayName("Every part still matches the in-game reference model, up to one shared X offset")
    void partsMatchTheReferenceModel() {
        assertEquals(JackStarBoss.JackPart.values().length, REFERENCE.size(),
                "the reference table must cover every part");

        Float sharedShift = null;
        for (Map.Entry<JackStarBoss.JackPart, Vector3f> entry : REFERENCE.entrySet()) {
            JackStarBoss.JackPart part = entry.getKey();
            Vector3f reference = entry.getValue();

            assertEquals(reference.y, part.offset.y, 0.001f, part + " drifted vertically from the model");
            assertEquals(reference.z, part.offset.z, 0.001f, part + " drifted in depth from the model");

            float shift = part.offset.x - reference.x;
            if (sharedShift == null) {
                sharedShift = shift;
                assertTrue(Math.abs(shift) > 0.9f, "the reference body is offset as a whole: " + shift);
            }
            assertEquals(sharedShift, shift, 0.005f,
                    part + " is not on the model's single shared X axis");
        }
    }

    @Test
    @DisplayName("The body is centred on the hitbox instead of a block away from it")
    void modelIsCentredOnTheHitbox() {
        assertEquals(0.0f, JackStarBoss.JackPart.CENTER.x, 0.005f,
                "the body axis must sit where the invisible armour stand is");
        assertEquals(0.0f, JackModel.baseTranslation(JackStarBoss.JackPart.HEAD).x, 0.01f);
        assertEquals(0.0f, JackModel.baseTranslation(JackStarBoss.JackPart.TORSO_UPPER).x, 0.01f);
    }

    @Test
    @DisplayName("Head sits above the torso, the torso above the legs, and the body is about two blocks tall")
    void verticalLayoutIsAHumanoid() {
        assertTrue(baseY(JackStarBoss.JackPart.HEAD) > baseY(JackStarBoss.JackPart.TORSO_UPPER) + 0.3f);
        assertTrue(baseY(JackStarBoss.JackPart.TORSO_UPPER) > baseY(JackStarBoss.JackPart.TORSO_LOWER) + 0.2f);
        assertTrue(baseY(JackStarBoss.JackPart.TORSO_LOWER) > baseY(JackStarBoss.JackPart.LEG_R_UPPER) + 0.4f);
        assertTrue(baseY(JackStarBoss.JackPart.LEG_R_UPPER) > baseY(JackStarBoss.JackPart.LEG_R_LOWER) + 0.2f);

        float headTop = baseY(JackStarBoss.JackPart.HEAD) + JackStarBoss.JackPart.HEAD.scale.y * 0.25f;
        float feetBottom = baseY(JackStarBoss.JackPart.LEG_L_LOWER)
                - JackStarBoss.JackPart.LEG_L_LOWER.scale.y * 0.25f;
        assertTrue(headTop > 2.0f && headTop < 2.3f, "head top should be around two blocks: " + headTop);
        assertTrue(feetBottom > 0.1f && feetBottom < 0.4f, "feet should clear the ground: " + feetBottom);
    }

    @Test
    @DisplayName("Left and right limbs are mirrored around the body axis")
    void limbsAreMirrored() {
        assertMirrored(JackStarBoss.JackPart.LEG_R_UPPER, JackStarBoss.JackPart.LEG_L_UPPER);
        assertMirrored(JackStarBoss.JackPart.LEG_R_LOWER, JackStarBoss.JackPart.LEG_L_LOWER);
        assertMirrored(JackStarBoss.JackPart.ARM_R_UPPER, JackStarBoss.JackPart.ARM_L_UPPER);
        assertMirrored(JackStarBoss.JackPart.ARM_R_LOWER, JackStarBoss.JackPart.ARM_L_LOWER);
    }

    @Test
    @DisplayName("Every joint is on the same side as the limb it drives")
    void jointsSitOnTheirOwnLimb() {
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            Vector3f base = JackModel.baseTranslation(part);
            if (Math.abs(base.x) <= 0.05f) continue; // head and torso hang from the spine, not a side
            float jointX = JackModel.pivot(part.group).x;
            assertTrue(Math.signum(base.x) == Math.signum(jointX),
                    part + " swings around a joint on the wrong side (part x=" + base.x + ", joint x=" + jointX + ")");
        }
    }

    @Test
    @DisplayName("A limb rotates about its joint instead of detaching from the body")
    void limbsSwingAroundTheirJoints() {
        Quaternionf swing = new Quaternionf().rotateX(0.32f);
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            Vector3f rest = JackModel.baseTranslation(part);
            Vector3f moved = JackModel.compose(part, new Quaternionf(swing), 1.0f).getTranslation();

            assertEquals(rest.x, moved.x, 1.0e-4f, part + " slid sideways while swinging");
            assertTrue(rest.distance(moved) < 0.35f, part + " flew away from its joint: " + rest.distance(moved));
        }
    }

    @Test
    @DisplayName("Each elbow and knee sits where the export splits its limb, and only the lower half hangs from it")
    void theJointsSitWhereTheExportSplitsEachLimb() {
        for (JackStarBoss.LimbGroup group : List.of(JackStarBoss.LimbGroup.LEG_RIGHT, JackStarBoss.LimbGroup.LEG_LEFT,
                JackStarBoss.LimbGroup.ARM_RIGHT, JackStarBoss.LimbGroup.ARM_LEFT)) {
            List<JackStarBoss.JackPart> limb = partsIn(group);
            LimbGeometry.Split<JackStarBoss.JackPart> split =
                    LimbGeometry.largestGap(limb, part -> JackModel.baseTranslation(part).y);

            for (JackStarBoss.JackPart part : limb) {
                assertEquals(split.lower().contains(part), JackModel.hangsFromSecondJoint(part),
                        part + " is on the wrong side of its joint, so the walk would fold the wrong piece");
            }

            Vector3f joint = JackModel.secondJoint(group);
            assertNotNull(joint, group + " must expose the joint its lower segment folds about");
            assertEquals(split.joint(), joint.y, 0.01,
                    group + "'s joint is not where the export leaves the gap between its two segments");
            assertEquals(JackModel.pivot(group).x, joint.x, 0.01f, "a joint must sit on its limb's own axis");
        }

        for (JackStarBoss.LimbGroup group : List.of(JackStarBoss.LimbGroup.HEAD, JackStarBoss.LimbGroup.TORSO_UPPER,
                JackStarBoss.LimbGroup.TORSO_LOWER)) {
            assertNull(JackModel.secondJoint(group), group + " has no second segment");
        }
    }

    @Test
    @DisplayName("Only the lower half of a limb folds, and it folds by the angle the limb itself walks with")
    void onlyTheLowerHalfFolds() {
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
                Quaternionf folded = JackModel.lowerRotation(part, phase);
                if (!JackModel.hangsFromSecondJoint(part)) {
                    assertEquals(new Quaternionf(), folded, part + " is above the joint and must stay rigid");
                    continue;
                }
                float swing = JackModel.walkSwing(part.group, phase);
                Quaternionf expected = part.group == JackStarBoss.LimbGroup.ARM_RIGHT
                        || part.group == JackStarBoss.LimbGroup.ARM_LEFT
                        ? MscLimb.elbow(swing) : MscLimb.knee(swing);
                assertEquals(expected, folded, part + " does not follow its own limb's swing at phase " + phase);
            }
        }
    }

    @Test
    @DisplayName("The whole walk keeps the body over the stand's hitbox")
    void theWalkStaysOverTheHitbox() {
        float halfWidth = 0.25f * (float) JackStarBoss.MODEL_HITBOX_SCALE;

        // A step bends the knees, which carries the shins further from the axis than the rest pose
        // does, so the rest-pose coverage above is not enough on its own. Only the parts the box is
        // meant to cover are checked: the arms sit outside it by design, as the hitbox test above says.
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.2f) {
            for (JackStarBoss.JackPart part : List.of(JackStarBoss.JackPart.HEAD, JackStarBoss.JackPart.TORSO_UPPER,
                    JackStarBoss.JackPart.TORSO_LOWER, JackStarBoss.JackPart.LEG_R_UPPER,
                    JackStarBoss.JackPart.LEG_R_LOWER, JackStarBoss.JackPart.LEG_L_UPPER,
                    JackStarBoss.JackPart.LEG_L_LOWER)) {
                float swing = JackModel.walkSwing(part.group, phase);
                Vector3f moved = JackModel.compose(part, new Quaternionf().rotateX(swing),
                        JackModel.lowerRotation(part, phase), 1.0f).getTranslation();

                assertTrue(Math.abs(moved.x) < halfWidth,
                        part + " swung out of the hitbox sideways at phase " + phase);
                assertTrue(Math.abs(moved.z) < halfWidth,
                        part + " swung out of the hitbox front or back at phase " + phase + ": z=" + moved.z);
            }
        }
    }

    @Test
    @DisplayName("The eleven parts keep their own place in the body")
    void partsNeverCollapseOntoEachOther() {
        List<JackStarBoss.JackPart> parts = new ArrayList<>(List.of(JackStarBoss.JackPart.values()));
        for (int i = 0; i < parts.size(); i++) {
            for (int j = i + 1; j < parts.size(); j++) {
                float gap = JackModel.baseTranslation(parts.get(i)).distance(JackModel.baseTranslation(parts.get(j)));
                assertTrue(gap > 0.02f, parts.get(i) + " and " + parts.get(j) + " sit on top of each other");
            }
        }
    }

    @Test
    @DisplayName("Shape shifting scales the whole body and the stand stays inside the hitbox")
    void shapeShiftScalesTheBody() {
        JackStarBoss.JackPart part = JackStarBoss.JackPart.TORSO_UPPER;
        Vector3f rest = JackModel.baseTranslation(part);

        Transformation big = JackModel.compose(part, new Quaternionf(), 2.2f);
        assertEquals(rest.x * 2.2f, big.getTranslation().x, 1.0e-3f);
        assertEquals(rest.y * 2.2f, big.getTranslation().y, 1.0e-3f);
        assertEquals(part.scale.y * 2.2f, big.getScale().y, 1.0e-3f);

        for (JackStarBoss.JackPart p : JackStarBoss.JackPart.values()) {
            Vector3f base = JackModel.baseTranslation(p);
            assertTrue(base.y > 0.2f && base.y < 2.2f, p + " sits outside the stand's height: " + base.y);
        }
    }

    @Test
    @DisplayName("The invisible stand's hitbox covers the visible body")
    void hitboxCoversTheBody() {
        // The stand is the only hitbox the model has: the displays must stay un-hittable, so the
        // swing has to land on the stand for the boss to take damage at all.
        float halfWidth = 0.25f * (float) JackStarBoss.MODEL_HITBOX_SCALE;
        float height = 1.975f * (float) JackStarBoss.MODEL_HITBOX_SCALE;

        List<JackStarBoss.JackPart> spine = List.of(
                JackStarBoss.JackPart.HEAD,
                JackStarBoss.JackPart.TORSO_UPPER,
                JackStarBoss.JackPart.TORSO_LOWER,
                JackStarBoss.JackPart.LEG_R_UPPER,
                JackStarBoss.JackPart.LEG_R_LOWER,
                JackStarBoss.JackPart.LEG_L_UPPER,
                JackStarBoss.JackPart.LEG_L_LOWER);
        for (JackStarBoss.JackPart part : spine) {
            Vector3f base = JackModel.baseTranslation(part);
            assertTrue(Math.abs(base.x) + part.scale.x * 0.25f < halfWidth,
                    part + " leans outside the hitbox, so a swing at it would miss the stand");
            assertTrue(base.y < height, part + " sits above the hitbox: " + base.y);
        }

        float headTop = baseY(JackStarBoss.JackPart.HEAD) + JackStarBoss.JackPart.HEAD.scale.y * 0.25f;
        assertTrue(headTop < height, "the head top must be inside the scaled hitbox: " + headTop);
        assertTrue(headTop > 1.975f,
                "the scaled stand is pointless unless a vanilla box would have missed the head: " + headTop);
    }

    @Test
    @DisplayName("The walk pose is a rigid skeleton whose elbows and knees fold over the legs' hitbox")
    void theWalkPoseIsARigidSkeleton() {
        List<MscLimb.Limb> rest = JackModel.walkPose(0f);
        assertEquals(4, rest.size(), "the four limbs are the whole walk skeleton");
        for (JackStarBoss.LimbGroup group : List.of(JackStarBoss.LimbGroup.ARM_RIGHT, JackStarBoss.LimbGroup.ARM_LEFT,
                JackStarBoss.LimbGroup.LEG_RIGHT, JackStarBoss.LimbGroup.LEG_LEFT)) {
            MscLimb.Limb limb = limbAt(rest, JackModel.pivot(group));
            assertNotNull(limb, group + " is missing from the walk skeleton");
            assertEquals(0f, limb.joint().distance(JackModel.secondJoint(group)), 1.0e-5f,
                    group + " must fold exactly at the joint the display pieces fold at");
        }

        float halfWidth = 0.25f * (float) JackStarBoss.MODEL_HITBOX_SCALE;
        float height = 1.975f * (float) JackStarBoss.MODEL_HITBOX_SCALE;
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            List<MscLimb.Limb> posed = JackModel.walkPose(phase);
            assertEquals(rest.size(), posed.size());
            for (int index = 0; index < posed.size(); index++) {
                MscLimb.Limb limb = posed.get(index);
                MscLimb.Limb idle = rest.get(index);
                assertEquals(0f, limb.pivot().distance(idle.pivot()), 1.0e-5f,
                        "a limb's pivot moved at phase " + phase);
                assertEquals(idle.pivot().distance(idle.joint()), limb.pivot().distance(limb.joint()),
                        1.0e-4f, "the upper segment changed length at phase " + phase);
                assertEquals(idle.joint().distance(idle.tip()), limb.joint().distance(limb.tip()),
                        1.0e-4f, "the lower segment came away from its joint at phase " + phase);

                // The arms sit outside the box by design, as the hitbox test above says; the walk is
                // judged where the box promises to cover it, on the legs.
                if (!isLeg(limb)) continue;
                for (Vector3f point : List.of(limb.pivot(), limb.joint(), limb.tip())) {
                    assertTrue(Math.abs(point.x) < halfWidth,
                            "the walk left the hitbox sideways at phase " + phase);
                    assertTrue(Math.abs(point.z) < halfWidth,
                            "the walk left the hitbox front or back at phase " + phase);
                    assertTrue(point.y > 0f && point.y < height,
                            "the walk left the hitbox vertically at phase " + phase);
                }
            }
        }

        // The knee folds on the back half of the step and the elbow on the forward half, each
        // bringing the end closer to the joint it hangs from.
        float backPhase = (float) (-Math.PI / 2);
        MscLimb.Limb bentLeg = limbAt(JackModel.walkPose(backPhase), JackModel.pivot(JackStarBoss.LimbGroup.LEG_RIGHT));
        MscLimb.Limb straightLeg = limbAt(rest, JackModel.pivot(JackStarBoss.LimbGroup.LEG_RIGHT));
        Vector3f straightFoot = MscLimb.swing(straightLeg.tip(), straightLeg.pivot(),
                new Quaternionf().rotateX(JackModel.walkSwing(JackStarBoss.LimbGroup.LEG_RIGHT, backPhase)));
        assertTrue(bentLeg.tip().z > straightFoot.z, "the knee must fold the foot behind the straight leg");
        assertTrue(bentLeg.tip().distance(bentLeg.pivot()) < straightFoot.distance(straightLeg.pivot()),
                "the fold must bring the foot closer to the hip, or the knee never bent");

        float forwardPhase = (float) (Math.PI / 2);
        MscLimb.Limb bentArm = limbAt(JackModel.walkPose(forwardPhase), JackModel.pivot(JackStarBoss.LimbGroup.ARM_LEFT));
        MscLimb.Limb straightArm = limbAt(rest, JackModel.pivot(JackStarBoss.LimbGroup.ARM_LEFT));
        Vector3f straightHand = MscLimb.swing(straightArm.tip(), straightArm.pivot(),
                new Quaternionf().rotateX(JackModel.walkSwing(JackStarBoss.LimbGroup.ARM_LEFT, forwardPhase)));
        assertTrue(bentArm.tip().z < straightHand.z, "the elbow must fold the hand in front of the straight arm");
        assertTrue(bentArm.tip().distance(bentArm.pivot()) < straightHand.distance(straightArm.pivot()),
                "the fold must bring the hand closer to the shoulder, or the elbow never bent");

        // And the boss itself steps at the same rate the replay does.
        String source = ProjectPaths.read(
                ProjectPaths.source("com", "Chagui68", "entities", "boss", "JackStarBoss.java"));
        assertTrue(source.contains("JackModel.WALK_RATE"),
                "the boss's own step and the walk replay must advance at the same rate");
    }

    private static boolean isLeg(MscLimb.Limb limb) {
        return limb.pivot().distance(JackModel.pivot(JackStarBoss.LimbGroup.LEG_RIGHT)) < 1.0e-5f
                || limb.pivot().distance(JackModel.pivot(JackStarBoss.LimbGroup.LEG_LEFT)) < 1.0e-5f;
    }

    private static MscLimb.Limb limbAt(List<MscLimb.Limb> limbs, Vector3f pivot) {
        for (MscLimb.Limb limb : limbs) {
            if (limb.pivot().distance(pivot) < 1.0e-5f) return limb;
        }
        return null;
    }

    private static float baseY(JackStarBoss.JackPart part) {
        return JackModel.baseTranslation(part).y;
    }

    private static List<JackStarBoss.JackPart> partsIn(JackStarBoss.LimbGroup group) {
        List<JackStarBoss.JackPart> members = new ArrayList<>();
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            if (part.group == group) members.add(part);
        }
        return members;
    }

    private static void assertMirrored(JackStarBoss.JackPart right, JackStarBoss.JackPart left) {
        Vector3f r = JackModel.baseTranslation(right);
        Vector3f l = JackModel.baseTranslation(left);
        assertEquals(-r.x, l.x, 0.02f, right + " / " + left + " are not mirrored");
        assertEquals(r.y, l.y, 0.02f, right + " / " + left + " are at different heights");
        assertEquals(r.z, l.z, 0.02f, right + " / " + left + " are at different depths");
    }
}
