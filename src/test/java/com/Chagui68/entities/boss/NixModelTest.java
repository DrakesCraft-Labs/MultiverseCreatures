package com.Chagui68.entities.boss;

import com.Chagui68.testsupport.LimbGeometry;
import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards NIX's model the way {@link JackModelTest} guards Jack Star's: twenty-seven parts, each in
 * its own place, joints on the correct side, the body centred on the invisible armour stand that
 * receives the hits, and a hitbox that covers the model.
 *
 * <p>The reference numbers are the translations of the model as exported for the plugin. That export
 * carries one shared X offset for the whole body (the spine sits at +0.066, not at zero); the code
 * re-centres it so the visible body sits over the hitbox. These tests pin both halves of that
 * contract, so neither the export nor the re-centring can drift unnoticed.
 */
class NixModelTest {

    /**
     * Reference model: one entry per part, straight from the export, only the translation the export
     * carries. The full matrices live in {@code NixPart}.
     */
    private static final Map<NixBoss.NixPart, Vector3f> EXPORT = new EnumMap<>(NixBoss.NixPart.class);

    static {
        EXPORT.put(NixBoss.NixPart.HEAD, new Vector3f(0.066366561f, 1.873507857f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.TORSO_UPPER, new Vector3f(0.066366561f, 1.405007839f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.TORSO_LOWER, new Vector3f(0.066366561f, 1.170757771f, -0.016734375f));

        EXPORT.put(NixBoss.NixPart.LEG_R_1, new Vector3f(-0.049001563f, 0.364937812f, 0.026601875f));
        EXPORT.put(NixBoss.NixPart.LEG_R_2, new Vector3f(-0.051929686f, 0.350882798f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_R_3, new Vector3f(-0.051929686f, 0.233757809f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_R_4, new Vector3f(-0.050758436f, 0.702257812f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_R_5, new Vector3f(-0.050758436f, 0.468007803f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_R_6, new Vector3f(-0.050758436f, 0.422329068f, -0.061241876f));

        EXPORT.put(NixBoss.NixPart.LEG_L_1, new Vector3f(0.181734681f, 0.364937812f, 0.026601875f));
        EXPORT.put(NixBoss.NixPart.LEG_L_2, new Vector3f(0.184662819f, 0.350882798f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_L_3, new Vector3f(0.184662819f, 0.233757809f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_L_4, new Vector3f(0.183491558f, 0.702257812f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_L_5, new Vector3f(0.183491558f, 0.468007803f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.LEG_L_6, new Vector3f(0.183491558f, 0.422329068f, -0.061241876f));

        EXPORT.put(NixBoss.NixPart.ARM_R_1, new Vector3f(0.415984690f, 1.067687869f, -0.060070626f));
        EXPORT.put(NixBoss.NixPart.ARM_R_2, new Vector3f(0.418912798f, 1.053632855f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_R_3, new Vector3f(0.418912798f, 0.936507821f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_R_4, new Vector3f(0.417741567f, 1.405007839f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_R_5, new Vector3f(0.417741567f, 1.170757771f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_R_6, new Vector3f(0.417741567f, 1.125079036f, 0.027773125f));

        EXPORT.put(NixBoss.NixPart.ARM_L_1, new Vector3f(-0.283251554f, 1.067687869f, -0.060070626f));
        EXPORT.put(NixBoss.NixPart.ARM_L_2, new Vector3f(-0.286179692f, 1.053632855f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_L_3, new Vector3f(-0.286179692f, 0.936507821f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_L_4, new Vector3f(-0.285008430f, 1.405007839f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_L_5, new Vector3f(-0.285008430f, 1.170757771f, -0.016734375f));
        EXPORT.put(NixBoss.NixPart.ARM_L_6, new Vector3f(-0.285008430f, 1.125079036f, 0.027773125f));
    }

    @Test
    @DisplayName("Every part still carries the exported transform")
    void partsKeepTheExportedTransforms() {
        assertEquals(NixBoss.NixPart.values().length, EXPORT.size(),
                "the reference table must cover every part");

        for (Map.Entry<NixBoss.NixPart, Vector3f> entry : EXPORT.entrySet()) {
            Vector3f exported = entry.getValue();
            Vector3f actual = entry.getKey().offset;

            assertEquals(exported.x, actual.x, 0.001f, entry.getKey() + " drifted from the export");
            assertEquals(exported.y, actual.y, 0.001f, entry.getKey() + " drifted vertically from the export");
            assertEquals(exported.z, actual.z, 0.001f, entry.getKey() + " drifted in depth from the export");
        }

        // The export writes the whole body about one shared X offset; the spine is that axis.
        for (NixBoss.NixPart spine : List.of(NixBoss.NixPart.HEAD, NixBoss.NixPart.TORSO_UPPER, NixBoss.NixPart.TORSO_LOWER)) {
            assertEquals(EXPORT.get(NixBoss.NixPart.HEAD).x, EXPORT.get(spine).x, 0.005f,
                    spine + " is not on the body's single shared X axis");
        }
    }

    @Test
    @DisplayName("The body is centred on the hitbox instead of six centimetres off it")
    void theBodyIsCentredOnTheHitbox() {
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (Vector3f exported : EXPORT.values()) {
            minX = Math.min(minX, exported.x);
            maxX = Math.max(maxX, exported.x);
            minZ = Math.min(minZ, exported.z);
            maxZ = Math.max(maxZ, exported.z);
        }

        assertEquals((minX + maxX) * 0.5f, NixBoss.NixPart.CENTER.x, 0.001f,
                "CENTER must be the midpoint of the exported body, not an average that any added part could pull around");
        assertEquals((minZ + maxZ) * 0.5f, NixBoss.NixPart.CENTER.z, 0.001f);

        for (NixBoss.NixPart spine : List.of(NixBoss.NixPart.HEAD, NixBoss.NixPart.TORSO_UPPER, NixBoss.NixPart.TORSO_LOWER)) {
            Vector3f base = NixModel.baseTranslation(spine);
            assertEquals(0.0f, base.x, 0.001f, spine + " must sit on the invisible armour stand");
            assertEquals(0.0f, base.z, 0.001f, spine + " must sit on the invisible armour stand");
        }
    }

    @Test
    @DisplayName("Head above torso, torso above legs, feet off the ground and about two blocks of body")
    void verticalLayoutIsAHumanoid() {
        assertTrue(baseY(NixBoss.NixPart.HEAD) > baseY(NixBoss.NixPart.TORSO_UPPER) + 0.3f);
        assertTrue(baseY(NixBoss.NixPart.TORSO_UPPER) > baseY(NixBoss.NixPart.TORSO_LOWER) + 0.2f);
        assertTrue(baseY(NixBoss.NixPart.TORSO_LOWER) > baseY(NixBoss.NixPart.LEG_R_4) + 0.4f);
        assertTrue(baseY(NixBoss.NixPart.LEG_R_4) > baseY(NixBoss.NixPart.LEG_R_3) + 0.4f);

        float headTop = topOf(NixBoss.NixPart.HEAD);
        assertTrue(headTop > 2.0f && headTop < 2.2f, "head top should be around two blocks: " + headTop);
        assertTrue(feetBottom() > 0.05f && feetBottom() < 0.4f,
                "feet should clear the ground: " + feetBottom());
    }

    @Test
    @DisplayName("Left and right limbs are mirrored around the body axis")
    void limbsAreMirrored() {
        for (int segment = 1; segment <= 6; segment++) {
            assertMirrored(leg("LEG_R_" + segment), leg("LEG_L_" + segment));
            assertMirrored(arm("ARM_R_" + segment), arm("ARM_L_" + segment));
        }
    }

    @Test
    @DisplayName("Every joint is on the same side as the limb it drives, and on that limb's axis")
    void jointsSitOnTheirOwnLimb() {
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            Vector3f base = NixModel.baseTranslation(part);
            if (Math.abs(base.x) <= 0.05f) continue; // head and torso hang from the spine, not a side
            float jointX = NixModel.pivot(part.group).x;
            assertTrue(Math.signum(base.x) == Math.signum(jointX),
                    part + " swings around a joint on the wrong side (part x=" + base.x + ", joint x=" + jointX + ")");
            assertEquals(base.x, jointX, 0.01f, part + " hangs off its joint instead of from it");
        }
    }

    @Test
    @DisplayName("A limb rotates about its joint instead of detaching from the body")
    void limbsSwingAroundTheirJoints() {
        Quaternionf swing = new Quaternionf().rotateX(0.32f);
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            Vector3f rest = NixModel.baseTranslation(part);
            Vector3f moved = NixModel.compose(part, new Quaternionf(swing)).getTranslation();

            assertEquals(rest.x, moved.x, 1.0e-4f, part + " slid sideways while swinging");
            assertTrue(rest.distance(moved) < 0.35f, part + " flew away from its joint: " + rest.distance(moved));
        }
    }

    @Test
    @DisplayName("Each elbow and knee sits where the export splits its limb, and only the stack below hangs from it")
    void theJointsSitWhereTheExportSplitsEachLimb() {
        for (NixBoss.LimbGroup group : List.of(NixBoss.LimbGroup.LEG_RIGHT, NixBoss.LimbGroup.LEG_LEFT,
                NixBoss.LimbGroup.ARM_RIGHT, NixBoss.LimbGroup.ARM_LEFT)) {
            List<NixBoss.NixPart> limb = partsIn(group);
            LimbGeometry.Split<NixBoss.NixPart> split =
                    LimbGeometry.largestGap(limb, part -> NixModel.baseTranslation(part).y);

            // The numbered pieces are not in stacking order, so the split is worth proving: the pieces
            // below the export's biggest gap are exactly the ones the code folds.
            assertEquals(1, split.upper().size(), group + " should have a single piece at the joint");
            for (NixBoss.NixPart part : limb) {
                assertEquals(split.lower().contains(part), NixModel.hangsFromSecondJoint(part),
                        part + " is on the wrong side of its joint, so the walk would fold the wrong piece");
            }

            Vector3f joint = NixModel.secondJoint(group);
            assertNotNull(joint, group + " must expose the joint its lower stack folds about");
            assertEquals(split.joint(), joint.y, 0.01,
                    group + "'s joint is not where the export leaves the gap between its two segments");
            assertEquals(NixModel.pivot(group).x, joint.x, 0.01f, "a joint must sit on its limb's own axis");
        }

        for (NixBoss.LimbGroup group : List.of(NixBoss.LimbGroup.HEAD, NixBoss.LimbGroup.TORSO_UPPER,
                NixBoss.LimbGroup.TORSO_LOWER)) {
            assertNull(NixModel.secondJoint(group), group + " has no second segment");
        }
    }

    @Test
    @DisplayName("Only the lower stack of a limb folds, and it folds by the angle the limb itself walks with")
    void onlyTheLowerStackFolds() {
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
                Quaternionf folded = NixModel.lowerRotation(part, phase);
                if (!NixModel.hangsFromSecondJoint(part)) {
                    assertEquals(new Quaternionf(), folded, part + " is above the joint and must stay rigid");
                    continue;
                }
                float swing = NixModel.walkSwing(part.group, phase);
                Quaternionf expected = part.group == NixBoss.LimbGroup.ARM_RIGHT
                        || part.group == NixBoss.LimbGroup.ARM_LEFT
                        ? MscLimb.elbow(swing) : MscLimb.knee(swing);
                assertEquals(expected, folded, part + " does not follow its own limb's swing at phase " + phase);
            }
        }
    }

    @Test
    @DisplayName("During cleave, elbows fold during windup and straighten on chop while knees flex")
    void cleaveFoldsElbowsAndKnees() {
        for (float prog = 0f; prog <= 1f; prog += 0.05f) {
            for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
                Quaternionf fold = NixModel.cleaveLowerRotation(part, prog);
                if (!NixModel.hangsFromSecondJoint(part)) {
                    assertEquals(new Quaternionf(), fold, part + " is above joint and must not fold");
                    continue;
                }
                assertNotNull(fold);
            }
        }
        // At windup peak (prog ~0.4), arms should have elbow bend
        for (NixBoss.NixPart armPart : List.of(NixBoss.NixPart.ARM_R_5, NixBoss.NixPart.ARM_L_5)) {
            Quaternionf elbowRot = NixModel.cleaveLowerRotation(armPart, 0.4f);
            assertNotEquals(new Quaternionf(), elbowRot, "elbow should fold at peak windup");
        }
        // At chop impact (prog ~0.7), arms should be straight
        for (NixBoss.NixPart armPart : List.of(NixBoss.NixPart.ARM_R_5, NixBoss.NixPart.ARM_L_5)) {
            Quaternionf elbowRot = NixModel.cleaveLowerRotation(armPart, 0.7f);
            assertEquals(new Quaternionf(), elbowRot, "elbow should snap straight on chop impact");
        }
        // At mid-cleave (prog 0.35), knees flex into squat
        for (NixBoss.NixPart legPart : List.of(NixBoss.NixPart.LEG_R_5, NixBoss.NixPart.LEG_L_5)) {
            Quaternionf kneeRot = NixModel.cleaveLowerRotation(legPart, 0.35f);
            assertNotEquals(new Quaternionf(), kneeRot, "knees should flex during cleave swing");
        }
    }

    @Test
    @DisplayName("The whole walk keeps the body over the stand's hitbox")
    void theWalkStaysOverTheHitbox() {
        float halfWidth = 0.25f * (float) NixBoss.MODEL_HITBOX_SCALE;

        // A step bends the elbows and knees, which carries those pieces further from the axis than the
        // rest pose does, so the rest-pose coverage above is not enough on its own.
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.2f) {
            for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
                float swing = NixModel.walkSwing(part.group, phase);
                Vector3f moved = NixModel.compose(part, new Quaternionf().rotateX(swing),
                        NixModel.lowerRotation(part, phase)).getTranslation();

                assertTrue(Math.abs(moved.x) + part.scale.x * 0.25f < halfWidth,
                        part + " swung out of the hitbox sideways at phase " + phase);
                assertTrue(Math.abs(moved.z) + part.scale.z * 0.25f < halfWidth,
                        part + " swung out of the hitbox front or back at phase " + phase + ": z=" + moved.z);
            }
        }
    }

    @Test
    @DisplayName("The twenty-seven parts keep their own place in the body")
    void partsNeverCollapseOntoEachOther() {
        List<NixBoss.NixPart> parts = new ArrayList<>(List.of(NixBoss.NixPart.values()));
        for (int i = 0; i < parts.size(); i++) {
            for (int j = i + 1; j < parts.size(); j++) {
                float gap = NixModel.baseTranslation(parts.get(i)).distance(NixModel.baseTranslation(parts.get(j)));
                assertTrue(gap > 0.02f, parts.get(i) + " and " + parts.get(j) + " sit on top of each other");
            }
        }
    }

    @Test
    @DisplayName("The invisible stand's hitbox covers the whole model, and is no bigger than the model needs")
    void hitboxCoversTheModel() {
        // The stand is the only hitbox the model has: the displays are zero-sized so the client
        // cannot pick one instead, which means a swing that misses the stand hits nothing at all.
        float halfWidth = 0.25f * (float) NixBoss.MODEL_HITBOX_SCALE;
        float height = 1.975f * (float) NixBoss.MODEL_HITBOX_SCALE;

        float needed = 0f;
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            Vector3f base = NixModel.baseTranslation(part);
            float edge = Math.abs(base.x) + part.scale.x * 0.25f;
            float top = topOf(part);

            assertTrue(edge < halfWidth, part + " leans outside the hitbox, so a swing at it would miss the stand");
            assertTrue(top < height, part + " pokes out of the top of the hitbox: " + top);
            assertTrue(base.y - part.scale.y * 0.25f > 0f, part + " hangs below the stand's feet");

            needed = Math.max(needed, edge / 0.25f);
            needed = Math.max(needed, top / 1.975f);
        }

        assertTrue(NixBoss.MODEL_HITBOX_SCALE >= needed,
                "the box is smaller than the model needs: " + NixBoss.MODEL_HITBOX_SCALE + " < " + needed);
        assertTrue(NixBoss.MODEL_HITBOX_SCALE < needed + 0.05f,
                "the box is far bigger than the model needs, so it blocks swings at thin air: "
                        + NixBoss.MODEL_HITBOX_SCALE + " vs " + needed);
        assertTrue(topOf(NixBoss.NixPart.HEAD) > 1.975f,
                "the scaled stand is pointless unless a vanilla box would have missed the head");
    }

    @Test
    @DisplayName("A part display never lags behind the stand, and a reload reattaches parts instead of duplicating them")
    void partDisplaysFollowTheStandExactly() throws IOException {
        String source = ProjectPaths.read(ProjectPaths.source(
                "com", "Chagui68", "entities", "boss", "NixBoss.java"));
        String suit = ProjectPaths.read(ProjectPaths.source(
                "com", "Chagui68", "utils", "DisplaySuit.java"));

        // The parts are placed on the stand's exact position every tick, so any interpolation would
        // make the body trail the invisible hitbox, and any display box would swallow the swing
        // aimed at it. Both were true of the first version of this model. The values live in the
        // suit every dressed boss shares, so this guard pins the one place they are applied.
        for (String call : List.of("setTeleportDuration", "setInterpolationDuration", "setInterpolationDelay",
                "setDisplayWidth", "setDisplayHeight")) {
            Matcher calls = Pattern.compile(Pattern.quote(call) + "\\(([^)]*)\\)").matcher(suit);
            int found = 0;
            while (calls.find()) {
                found++;
                assertTrue(calls.group(1).trim().matches("0(\\.0+)?f?"),
                        call + " must be zero, found " + calls.group(1).trim());
            }
            assertEquals(1, found, call + " should be configured once, for every part, in DisplaySuit");
        }
        assertTrue(source.contains("DisplaySuit.spawn("),
                "a part display must be built by the shared suit, not by hand");

        // Enabling the plugin over a live boss must reattach the parts it already spawned: spawning
        // first is what used to leave two overlapping bodies.
        assertTrue(source.contains("restorePartDisplays("),
                "the enable path must adopt the parts of a boss that is already alive");
        assertTrue(source.contains("findPartDisplay("),
                "syncDisplays must adopt an orphaned part before spawning a new one");
    }

    @Test
    @DisplayName("The walk pose is a rigid skeleton whose elbows and knees fold inside the hitbox")
    void theWalkPoseIsARigidSkeleton() {
        List<MscLimb.Limb> rest = NixModel.walkPose(0f);
        assertEquals(4, rest.size(), "the four limbs are the whole walk skeleton");
        for (NixBoss.LimbGroup group : List.of(NixBoss.LimbGroup.ARM_RIGHT, NixBoss.LimbGroup.ARM_LEFT,
                NixBoss.LimbGroup.LEG_RIGHT, NixBoss.LimbGroup.LEG_LEFT)) {
            MscLimb.Limb limb = limbAt(rest, NixModel.pivot(group));
            assertNotNull(limb, group + " is missing from the walk skeleton");
            assertEquals(0f, limb.joint().distance(NixModel.secondJoint(group)), 1.0e-5f,
                    group + " must fold exactly at the joint the display pieces fold at");
        }

        float halfWidth = 0.25f * (float) NixBoss.MODEL_HITBOX_SCALE;
        float height = 1.975f * (float) NixBoss.MODEL_HITBOX_SCALE;
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            List<MscLimb.Limb> posed = NixModel.walkPose(phase);
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
        MscLimb.Limb bentLeg = limbAt(NixModel.walkPose(backPhase), NixModel.pivot(NixBoss.LimbGroup.LEG_RIGHT));
        MscLimb.Limb straightLeg = limbAt(rest, NixModel.pivot(NixBoss.LimbGroup.LEG_RIGHT));
        Vector3f straightFoot = MscLimb.swing(straightLeg.tip(), straightLeg.pivot(),
                new Quaternionf().rotateX(NixModel.walkSwing(NixBoss.LimbGroup.LEG_RIGHT, backPhase)));
        assertTrue(bentLeg.tip().z > straightFoot.z, "the knee must fold the foot behind the straight leg");
        assertTrue(bentLeg.tip().distance(bentLeg.pivot()) < straightFoot.distance(straightLeg.pivot()),
                "the fold must bring the foot closer to the hip, or the knee never bent");

        float forwardPhase = (float) (Math.PI / 2);
        MscLimb.Limb bentArm = limbAt(NixModel.walkPose(forwardPhase), NixModel.pivot(NixBoss.LimbGroup.ARM_LEFT));
        MscLimb.Limb straightArm = limbAt(rest, NixModel.pivot(NixBoss.LimbGroup.ARM_LEFT));
        Vector3f straightHand = MscLimb.swing(straightArm.tip(), straightArm.pivot(),
                new Quaternionf().rotateX(NixModel.walkSwing(NixBoss.LimbGroup.ARM_LEFT, forwardPhase)));
        assertTrue(bentArm.tip().z < straightHand.z, "the elbow must fold the hand in front of the straight arm");
        assertTrue(bentArm.tip().distance(bentArm.pivot()) < straightHand.distance(straightArm.pivot()),
                "the fold must bring the hand closer to the shoulder, or the elbow never bent");

        // And the boss itself steps at the same rate the replay does.
        String source = ProjectPaths.read(
                ProjectPaths.source("com", "Chagui68", "entities", "boss", "NixBoss.java"));
        assertTrue(source.contains("NixModel.WALK_RATE"),
                "the boss's own step and the walk replay must advance at the same rate");
    }

    private static MscLimb.Limb limbAt(List<MscLimb.Limb> limbs, Vector3f pivot) {
        for (MscLimb.Limb limb : limbs) {
            if (limb.pivot().distance(pivot) < 1.0e-5f) return limb;
        }
        return null;
    }

    private static float baseY(NixBoss.NixPart part) {
        return NixModel.baseTranslation(part).y;
    }

    private static float topOf(NixBoss.NixPart part) {
        return baseY(part) + part.scale.y * 0.25f;
    }

    private static float feetBottom() {
        float lowest = Float.POSITIVE_INFINITY;
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            if (part.group != NixBoss.LimbGroup.LEG_RIGHT && part.group != NixBoss.LimbGroup.LEG_LEFT) continue;
            lowest = Math.min(lowest, baseY(part) - part.scale.y * 0.25f);
        }
        return lowest;
    }

    private static List<NixBoss.NixPart> partsIn(NixBoss.LimbGroup group) {
        List<NixBoss.NixPart> members = new ArrayList<>();
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            if (part.group == group) members.add(part);
        }
        return members;
    }

    private static NixBoss.NixPart leg(String name) {
        return NixBoss.NixPart.valueOf(name);
    }

    private static NixBoss.NixPart arm(String name) {
        return NixBoss.NixPart.valueOf(name);
    }

    private static void assertMirrored(NixBoss.NixPart right, NixBoss.NixPart left) {
        Vector3f r = NixModel.baseTranslation(right);
        Vector3f l = NixModel.baseTranslation(left);
        assertEquals(-r.x, l.x, 0.02f, right + " / " + left + " are not mirrored");
        assertEquals(r.y, l.y, 0.02f, right + " / " + left + " are at different heights");
        assertEquals(r.z, l.z, 0.02f, right + " / " + left + " are at different depths");
    }
}
