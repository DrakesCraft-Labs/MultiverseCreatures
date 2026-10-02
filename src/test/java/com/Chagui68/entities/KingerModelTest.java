package com.Chagui68.entities;

import com.Chagui68.testsupport.LimbGeometry;
import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.utils.MscLimb;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards Kinger's suit: fifteen pieces pinned to the exported model, read with the rule the game
 * draws them by, a skeleton whose joints sit where the pieces actually meet, and the poses that move
 * it.
 *
 * <p>A piece is a player head on an item display with no item transform: centred on its anchor in x
 * and z, hanging below it from 0 to -0.5 of its own height. The first version of this suite assumed
 * centred pieces, which is why it never noticed that the "arms" were the eyes and that every joint
 * was off: under the wrong rule nothing in the export fits, under the right one it fits to the
 * millimetre. The tests below pin that fit, so a re-export or a wrong assumption fails here.
 */
class KingerModelTest {

    /** Reference model: one anchor per piece, straight from the export. */
    private static final Map<Kinger.KingerPart, Vector3f> EXPORT = new EnumMap<>(Kinger.KingerPart.class);

    static {
        EXPORT.put(Kinger.KingerPart.ARM_LEFT, new Vector3f(0.3886122987f, 0.6775875205f, 0.501853708f));
        EXPORT.put(Kinger.KingerPart.ARM_RIGHT, new Vector3f(0.634953709f, 0.6814326334f, 0.4967771821f));

        EXPORT.put(Kinger.KingerPart.LEG_RIGHT_LOWER, new Vector3f(0.5950490686f, 0.232635498f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_RIGHT_UPPER, new Vector3f(0.5950490686f, 0.683807373f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_LEFT_LOWER, new Vector3f(0.409919674f, 0.2333874512f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_LEFT_UPPER, new Vector3f(0.409919674f, 0.6845593262f, 0.5025390625f));

        EXPORT.put(Kinger.KingerPart.TORSO_UPPER, new Vector3f(0.5022069787f, 1.2012714355f, 0.5028765625f));
        EXPORT.put(Kinger.KingerPart.TORSO_LOWER, new Vector3f(0.5021828576f, 0.810135498f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.HEAD, new Vector3f(0.5052224005f, 1.5827280655f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.NECK, new Vector3f(0.5052224005f, 1.3913218155f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.CROWN, new Vector3f(0.5052224005f, 1.619095253f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.CROSS, new Vector3f(0.5050075647f, 1.7741992188f, 0.4708984375f));
        EXPORT.put(Kinger.KingerPart.CROSS_BAR, new Vector3f(0.5764186975f, 1.7048144531f, 0.4603515625f));

        EXPORT.put(Kinger.KingerPart.EYE_RIGHT, new Vector3f(0.5764186975f, 1.511015625f, 0.34375f));
        EXPORT.put(Kinger.KingerPart.EYE_LEFT, new Vector3f(0.4335964318f, 1.4631640625f, 0.34375f));
    }

    private static final float HALF_WIDTH = (float) (0.25 * Kinger.MODEL_HITBOX_SCALE);
    private static final float HEIGHT = (float) (1.975 * Kinger.MODEL_HITBOX_SCALE);

    // ------------------------------------------------------------------ the export

    @Test
    @DisplayName("Every piece still carries the exported transform")
    void partsKeepTheExportedTransforms() {
        assertEquals(Kinger.KingerPart.values().length, EXPORT.size(), "the reference table must cover every piece");
        for (Map.Entry<Kinger.KingerPart, Vector3f> entry : EXPORT.entrySet()) {
            Vector3f actual = entry.getKey().offset;
            assertEquals(entry.getValue().x, actual.x, 0.001f, entry.getKey() + " drifted from the export");
            assertEquals(entry.getValue().y, actual.y, 0.001f, entry.getKey() + " drifted vertically");
            assertEquals(entry.getValue().z, actual.z, 0.001f, entry.getKey() + " drifted in depth");
        }
    }

    @Test
    @DisplayName("The export fits together only with pieces hanging below their anchor")
    void piecesHangBelowTheirAnchor() {
        // Thigh ends where the shin starts, on both legs.
        assertEquals(base(Kinger.KingerPart.LEG_RIGHT_LOWER).y, end(Kinger.KingerPart.LEG_RIGHT_UPPER).y, 0.002f);
        assertEquals(base(Kinger.KingerPart.LEG_LEFT_LOWER).y, end(Kinger.KingerPart.LEG_LEFT_UPPER).y, 0.002f);
        // The two robe pieces meet.
        assertEquals(base(Kinger.KingerPart.TORSO_LOWER).y, end(Kinger.KingerPart.TORSO_UPPER).y, 0.002f);
        // He stands on the ground.
        assertEquals(0f, end(Kinger.KingerPart.LEG_RIGHT_LOWER).y, 0.02f, "the right foot floats or sinks");
        assertEquals(0f, end(Kinger.KingerPart.LEG_LEFT_LOWER).y, 0.02f, "the left foot floats or sinks");
        // The cross bar crosses the cross: centred on it, inside its height.
        Vector3f bar = centre(Kinger.KingerPart.CROSS_BAR);
        assertEquals(centre(Kinger.KingerPart.CROSS).x, bar.x, 0.01f, "the cross bar is off the cross");
        assertTrue(bar.y > end(Kinger.KingerPart.CROSS).y && bar.y < base(Kinger.KingerPart.CROSS).y,
                "the cross bar is not on the cross");
    }

    @Test
    @DisplayName("The body is centred on the torso axis, over the stand that carries the hitbox")
    void theBodyIsCentredOnTheTorsoAxis() {
        for (Kinger.KingerPart trunk : List.of(Kinger.KingerPart.TORSO_UPPER, Kinger.KingerPart.TORSO_LOWER)) {
            assertEquals(0f, base(trunk).x, 0.001f, trunk + " must sit on the armour stand");
            assertEquals(0f, base(trunk).z, 0.001f, trunk + " must sit on the armour stand");
        }
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            assertEquals(part.offset.y, base(part).y, 1.0e-6f, part + " was shifted vertically");
        }
    }

    @Test
    @DisplayName("The eyes are the two white pieces on the front of the face, and turn with the head")
    void theEyesAreOnTheFace() {
        float faceFront = Float.POSITIVE_INFINITY;
        float faceBottom = Float.POSITIVE_INFINITY;
        float faceTop = Float.NEGATIVE_INFINITY;
        for (Vector3f corner : KingerModel.corners(Kinger.KingerPart.HEAD)) {
            faceFront = Math.min(faceFront, corner.z);
            faceBottom = Math.min(faceBottom, corner.y);
            faceTop = Math.max(faceTop, corner.y);
        }
        for (Kinger.KingerPart eye : List.of(Kinger.KingerPart.EYE_RIGHT, Kinger.KingerPart.EYE_LEFT)) {
            Vector3f c = centre(eye);
            assertEquals(Kinger.LimbGroup.HEAD, eye.group(), eye + " must turn with the head, not swing like an arm");
            assertTrue(c.z < faceFront + 0.05f, eye + " is not on the front of the face: z=" + c.z);
            assertTrue(c.y > faceBottom && c.y < faceTop, eye + " is not at face height: y=" + c.y);
        }
        assertTrue(centre(Kinger.KingerPart.EYE_RIGHT).x > 0f && centre(Kinger.KingerPart.EYE_LEFT).x < 0f,
                "the right eye sits on his right (+x), the left on his left");
    }

    @Test
    @DisplayName("The arms are the sleeves: they leave the robe at the shoulder and end in a hand outside it")
    void theArmsAreTheSleeves() {
        float robeHalf = 0f;
        for (Vector3f corner : KingerModel.corners(Kinger.KingerPart.TORSO_LOWER)) robeHalf = Math.max(robeHalf, Math.abs(corner.x));
        for (Kinger.LimbGroup group : List.of(Kinger.LimbGroup.ARM_RIGHT, Kinger.LimbGroup.ARM_LEFT)) {
            Kinger.KingerPart arm = group == Kinger.LimbGroup.ARM_RIGHT ? Kinger.KingerPart.ARM_RIGHT : Kinger.KingerPart.ARM_LEFT;
            assertEquals(group, arm.group());
            Vector3f shoulder = KingerModel.pivot(group);
            Vector3f hand = end(arm);
            assertTrue(Math.abs(shoulder.x) < robeHalf, group + "'s shoulder is outside the robe");
            assertTrue(Math.abs(hand.x) > Math.abs(shoulder.x) + 0.1f, group + "'s hand is not out to the side");
            assertTrue(hand.y < shoulder.y, group + "'s hand does not hang below the shoulder");
            assertTrue(Math.signum(hand.x) == Math.signum(group == Kinger.LimbGroup.ARM_RIGHT ? 1 : -1),
                    group + " is on the wrong side");
        }
    }

    // ------------------------------------------------------------------ the skeleton

    @Test
    @DisplayName("Every joint sits where its pieces meet")
    void jointsSitWhereThePiecesMeet() {
        assertEquals(0f, KingerModel.PIVOT_HIP_RIGHT.distance(base(Kinger.KingerPart.LEG_RIGHT_UPPER)), 1e-6f);
        assertEquals(0f, KingerModel.PIVOT_KNEE_RIGHT.distance(base(Kinger.KingerPart.LEG_RIGHT_LOWER)), 1e-6f);
        assertEquals(KingerModel.PIVOT_HIP_RIGHT.x, KingerModel.PIVOT_KNEE_RIGHT.x, 1e-3f, "a knee must sit on its leg");
        assertEquals(KingerModel.PIVOT_HIP_LEFT.x, KingerModel.PIVOT_KNEE_LEFT.x, 1e-3f, "a knee must sit on its leg");
        assertEquals(end(Kinger.KingerPart.TORSO_UPPER).y, KingerModel.PIVOT_TORSO.y, 0.15f,
                "the torso leans about the hips, inside the lower robe");
        assertEquals(base(Kinger.KingerPart.TORSO_UPPER).y, KingerModel.PIVOT_NECK.y, 1e-6f,
                "the head turns on top of the robe");
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            if (part.group() == Kinger.LimbGroup.HEAD) {
                assertTrue(centre(part).y > KingerModel.PIVOT_NECK.y - 0.1f, part + " hangs below the neck it turns on");
            }
        }
        assertTrue(KingerModel.EYE_HEIGHT > 1.35f && KingerModel.EYE_HEIGHT < 1.5f, "eye height " + KingerModel.EYE_HEIGHT);
    }

    @Test
    @DisplayName("At rest every piece is exactly where the export puts it")
    void restPoseIsTheExport() {
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            var t = KingerModel.compose(part, KingerModel.Pose.rest());
            assertEquals(0f, t.getTranslation().distance(base(part)), 1e-5f, part + " moved while idle");
            assertTrue(t.getLeftRotation().equals(part.rotation, 1e-5f), part + " turned while idle");
        }
    }

    @Test
    @DisplayName("Each group moves as one rigid body in every pose")
    void groupsStayRigid() {
        for (KingerModel.Pose pose : samplePoses()) {
            for (Kinger.LimbGroup group : Kinger.LimbGroup.values()) {
                List<Kinger.KingerPart> members = membersOf(group);
                for (int i = 0; i < members.size(); i++) {
                    for (int j = i + 1; j < members.size(); j++) {
                        if (KingerModel.hangsFromSecondJoint(members.get(i)) != KingerModel.hangsFromSecondJoint(members.get(j))) continue;
                        float rest = base(members.get(i)).distance(base(members.get(j)));
                        float moved = posed(members.get(i), pose).distance(posed(members.get(j), pose));
                        assertEquals(rest, moved, 1e-4f, members.get(i) + " and " + members.get(j) + " came apart");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("A lean carries the head and both arms along instead of leaving them behind")
    void theTorsoCarriesHeadAndArms() {
        KingerModel.Pose lean = KingerModel.Pose.rest();
        lean.torso().rotateX(-0.3f);
        for (Kinger.KingerPart part : List.of(Kinger.KingerPart.NECK, Kinger.KingerPart.EYE_RIGHT,
                Kinger.KingerPart.ARM_RIGHT, Kinger.KingerPart.ARM_LEFT)) {
            float rest = base(part).distance(base(Kinger.KingerPart.TORSO_UPPER));
            float moved = posed(part, lean).distance(posed(Kinger.KingerPart.TORSO_UPPER, lean));
            assertEquals(rest, moved, 1e-4f, part + " did not follow the torso's lean");
        }
        assertTrue(posed(Kinger.KingerPart.CROSS, lean).z < base(Kinger.KingerPart.CROSS).z - 0.1f,
                "a forward lean must bring the crown forward");
    }

    @Test
    @DisplayName("Only the shins fold, at the knee, and only on the back half of the step")
    void theKneeFollowsTheWalk() {
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            for (Kinger.KingerPart shin : List.of(Kinger.KingerPart.LEG_RIGHT_LOWER, Kinger.KingerPart.LEG_LEFT_LOWER)) {
                float swing = KingerModel.walkSwing(shin.group(), phase);
                assertEquals(MscLimb.knee(swing), KingerModel.lowerRotation(shin, phase));
            }
        }
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            if (KingerModel.hangsFromSecondJoint(part)) continue;
            assertEquals(new Quaternionf(), KingerModel.lowerRotation(part, (float) (-Math.PI / 2)), part + " must stay rigid");
            assertEquals(new Quaternionf(), KingerModel.meleeLowerRotation(part, 0.5f), part + " must stay rigid");
        }
        for (Kinger.LimbGroup group : Kinger.LimbGroup.values()) {
            boolean leg = group == Kinger.LimbGroup.LEG_RIGHT || group == Kinger.LimbGroup.LEG_LEFT;
            assertEquals(leg, KingerModel.secondJoint(group) != null, group + ": only the legs have a second joint");
        }
        LimbGeometry.Split<Kinger.KingerPart> split =
                LimbGeometry.largestGap(membersOf(Kinger.LimbGroup.LEG_RIGHT), part -> base(part).y);
        assertTrue(split.lower().contains(Kinger.KingerPart.LEG_RIGHT_LOWER));
        assertNotEquals(new Quaternionf(), KingerModel.meleeLowerRotation(Kinger.KingerPart.LEG_RIGHT_LOWER, 0.5f),
                "the knees flex into the melee strike");
    }

    // ------------------------------------------------------------------ the hitbox

    @Test
    @DisplayName("The stand's hitbox covers the body, and is no bigger than it needs")
    void hitboxCoversTheModel() {
        float needed = 0f;
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            boolean arm = part.group() == Kinger.LimbGroup.ARM_RIGHT || part.group() == Kinger.LimbGroup.ARM_LEFT;
            for (Vector3f corner : KingerModel.corners(part)) {
                assertTrue(corner.y > -0.02f && corner.y < HEIGHT, part + " leaves the hitbox vertically: " + corner.y);
                if (arm) {
                    // The hands are small and stick out to the sides by design; a few centimetres only.
                    assertTrue(Math.abs(corner.x) < HALF_WIDTH + 0.12f, part + " reaches far outside the hitbox");
                    continue;
                }
                assertTrue(Math.abs(corner.x) < HALF_WIDTH + 0.01f, part + " leans out of the side of the hitbox");
                assertTrue(Math.abs(corner.z) < HALF_WIDTH + 0.01f, part + " stands out of the front of the hitbox");
                needed = Math.max(needed, Math.max(Math.abs(corner.x), Math.abs(corner.z)) / 0.25f);
                needed = Math.max(needed, corner.y / 1.975f);
            }
        }
        assertTrue(Kinger.MODEL_HITBOX_SCALE >= needed, "the box is smaller than the body: " + needed);
        assertTrue(Kinger.MODEL_HITBOX_SCALE - needed < 0.15f, "the box swallows swings at thin air: " + needed);
    }

    @Test
    @DisplayName("The whole walk keeps the body's pieces over the hitbox")
    void theWalkStaysOverTheHitbox() {
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.05f) {
            KingerModel.Pose pose = KingerModel.pose(phase, true, -1f, -1f, 0f);
            for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
                Vector3f c = KingerModel.posePoint(part, centre(part), pose);
                float limit = part.group() == Kinger.LimbGroup.ARM_RIGHT || part.group() == Kinger.LimbGroup.ARM_LEFT
                        ? HALF_WIDTH + 0.12f : HALF_WIDTH;
                assertTrue(Math.abs(c.x) < limit, part + " swung out sideways at phase " + phase);
                assertTrue(Math.abs(c.z) < limit, part + " swung out front or back at phase " + phase + ": " + c.z);
                assertTrue(c.y > 0f && c.y < HEIGHT, part + " left the hitbox vertically at phase " + phase);
            }
        }
    }

    // ------------------------------------------------------------------ the poses

    @Test
    @DisplayName("He looks up at a player above his eyes and down at one below")
    void theHeadLooksTheRightWay() {
        assertTrue(KingerModel.lookPitch(1.0, 3.0) > 0f, "a player above must make him look up");
        assertTrue(KingerModel.lookPitch(-1.0, 3.0) < 0f, "a player below must make him look down");
        assertEquals(0f, KingerModel.lookPitch(5.0, 0.1), "straight overhead he does not snap his neck");
        assertEquals(KingerModel.MAX_LOOK, KingerModel.lookPitch(50.0, 1.0), 1e-6f);

        // Positive pitch turns the face (which points to -z) upwards: the eyes rise.
        KingerModel.Pose up = KingerModel.pose(0f, false, -1f, -1f, KingerModel.MAX_LOOK);
        assertTrue(posed(Kinger.KingerPart.EYE_RIGHT, up).y > base(Kinger.KingerPart.EYE_RIGHT).y,
                "looking up must lift the eyes");
    }

    @Test
    @DisplayName("Melee: the arms rise on the wind-up and come down in front of him on the strike")
    void meleeSwingsTheArms() {
        Vector3f rest = handTip(KingerModel.Pose.rest());
        Vector3f windUp = handTip(KingerModel.pose(0f, false, 0.4f, -1f, 0f));
        Vector3f strike = handTip(KingerModel.pose(0f, false, 0.52f, -1f, 0f));
        assertTrue(windUp.y > rest.y + 0.1f, "the wind-up must raise the hand: " + windUp.y + " vs " + rest.y);
        assertTrue(strike.z < rest.z - 0.06f, "the strike must bring the hand in front of him: " + strike.z);
        for (Kinger.KingerPart eye : List.of(Kinger.KingerPart.EYE_RIGHT, Kinger.KingerPart.EYE_LEFT)) {
            Vector3f eyeAtStrike = posed(eye, KingerModel.pose(0f, false, 0.52f, -1f, 0f));
            assertTrue(eyeAtStrike.distance(base(eye)) < 0.6f, eye + " flew away from the face during the swing");
        }
        assertEquals(KingerModel.Pose.rest().armRight(), KingerModel.pose(0f, false, 1f, -1f, 0f).armRight(),
                "the swing ends at rest");
    }

    @Test
    @DisplayName("Ranged: the shot leaves his right hand, raised and pointing ahead, never his eyes")
    void theShotLeavesTheHand() {
        KingerModel.Pose firing = KingerModel.pose(0f, false, -1f, KingerModel.RANGED_FIRE_PROGRESS, 0f);
        Vector3f hand = KingerModel.rightHand(firing);
        Vector3f restHand = KingerModel.rightHand(KingerModel.Pose.rest());
        assertTrue(hand.z < KingerModel.PIVOT_SHOULDER_RIGHT.z - 0.1f, "the arm must point ahead when he fires: " + hand.z);
        assertTrue(hand.y > restHand.y, "the arm must be raised when he fires");
        for (Kinger.KingerPart eye : List.of(Kinger.KingerPart.EYE_RIGHT, Kinger.KingerPart.EYE_LEFT)) {
            assertTrue(hand.distance(centre(eye)) > 0.5f, "the shot starts at the eyes");
        }
    }

    @Test
    @DisplayName("The walk pose is a rigid skeleton: arms of one piece, legs folding at the knee")
    void theWalkPoseIsARigidSkeleton() {
        List<MscLimb.Limb> rest = KingerModel.walkPose(0f);
        assertEquals(4, rest.size(), "the four limbs are the whole walk skeleton");
        for (float phase = 0f; phase < (float) (2 * Math.PI); phase += 0.1f) {
            List<MscLimb.Limb> posed = KingerModel.walkPose(phase);
            for (int index = 0; index < posed.size(); index++) {
                MscLimb.Limb limb = posed.get(index);
                MscLimb.Limb idle = rest.get(index);
                assertEquals(0f, limb.pivot().distance(idle.pivot()), 1e-5f, "a limb's pivot moved");
                if (limb.joint() == null) {
                    assertEquals(idle.pivot().distance(idle.tip()), limb.pivot().distance(limb.tip()), 1e-4f);
                } else {
                    assertEquals(idle.pivot().distance(idle.joint()), limb.pivot().distance(limb.joint()), 1e-4f);
                    assertEquals(idle.joint().distance(idle.tip()), limb.joint().distance(limb.tip()), 1e-4f);
                }
            }
        }
        assertNull(rest.get(0).joint(), "an arm is a single piece");
        assertNotNull(rest.get(2).joint(), "a leg folds at its knee");

        String source = ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "entities", "Kinger.java"));
        assertTrue(source.contains("KingerModel.WALK_RATE"),
                "the mob's own step and the walk replay must advance at the same rate");
    }

    // ------------------------------------------------------------------ the entity

    @Test
    @DisplayName("Every piece carries its own versioned tag; a piece of the old suit is never adopted")
    void pieceTagsAreUniqueAndVersioned() {
        Set<String> pieceTags = new HashSet<>();
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            assertTrue(pieceTags.add(Kinger.partTag(part)), part + " shares a tag with another piece");
            assertTrue(Kinger.partTag(part).startsWith(Kinger.PART_TAG));
            assertTrue(Kinger.isCurrentPiece(Set.of(Kinger.PART_TAG, Kinger.partTag(part))));
        }
        // The old build tagged the right eye as an arm: under the new names that would be a sleeve.
        assertFalse(Kinger.isCurrentPiece(Set.of(Kinger.PART_TAG, Kinger.PART_TAG + "_ARM_RIGHT")));
        assertNotEquals(Kinger.partOwnerTag(java.util.UUID.randomUUID()), Kinger.partOwnerTag(java.util.UUID.randomUUID()));
    }

    @Test
    @DisplayName("Animation progress runs from 0 to 1 as the ticks count down")
    void progressCountsUp() {
        assertEquals(0f, Kinger.progress(20, 20));
        assertEquals(0.5f, Kinger.progress(10, 20));
        assertEquals(1f, Kinger.progress(0, 20));
        assertEquals(1f, Kinger.progress(5, 0));
    }

    @Test
    @DisplayName("A piece never lags, and a reload reattaches the suit instead of duplicating it")
    void partDisplaysFollowTheStandExactly() throws IOException {
        String source = ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "entities", "Kinger.java"));
        String suit = ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "utils", "DisplaySuit.java"));

        for (String call : List.of("setTeleportDuration", "setInterpolationDuration", "setInterpolationDelay",
                "setDisplayWidth", "setDisplayHeight")) {
            Matcher calls = Pattern.compile(Pattern.quote(call) + "\\(([^)]*)\\)").matcher(suit);
            int found = 0;
            while (calls.find()) {
                found++;
                assertTrue(calls.group(1).trim().matches("0(\\.0+)?f?"), call + " must be zero");
            }
            assertEquals(1, found, call + " should be configured once, in DisplaySuit");
        }
        for (String required : List.of("DisplaySuit.spawn(", "restorePartDisplays(", "findPartDisplay(",
                "setupBossBar(inst)", "KEY_VIRTUAL_MAX_HEALTH", "KingerModel.compose(", "entities.kinger.hitbox-scale",
                "MODEL_HITBOX_SCALE", "clampHitboxScale(", "isCurrentPiece(", "KingerModel.rightHand(")) {
            assertTrue(source.contains(required), "Kinger.java no longer uses " + required);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static List<KingerModel.Pose> samplePoses() {
        List<KingerModel.Pose> poses = new java.util.ArrayList<>();
        for (float t = 0f; t <= 1f; t += 0.1f) {
            poses.add(KingerModel.pose(t * 6f, true, t, -1f, 0.3f));
            poses.add(KingerModel.pose(t * 6f, false, -1f, t, -0.3f));
        }
        return poses;
    }

    private static Vector3f handTip(KingerModel.Pose pose) {
        return KingerModel.rightHand(pose);
    }

    private static Vector3f posed(Kinger.KingerPart part, KingerModel.Pose pose) {
        return KingerModel.compose(part, pose).getTranslation();
    }

    private static Vector3f base(Kinger.KingerPart part) {
        return KingerModel.baseTranslation(part);
    }

    private static Vector3f centre(Kinger.KingerPart part) {
        return KingerModel.centre(part);
    }

    private static Vector3f end(Kinger.KingerPart part) {
        return KingerModel.end(part);
    }

    private static List<Kinger.KingerPart> membersOf(Kinger.LimbGroup group) {
        List<Kinger.KingerPart> members = new java.util.ArrayList<>();
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            if (part.group() == group) members.add(part);
        }
        return members;
    }
}
