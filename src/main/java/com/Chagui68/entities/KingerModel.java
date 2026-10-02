package com.Chagui68.entities;

import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure kinematics of Kinger's fifteen-piece suit: where each piece rests, where its joints are and
 * how a pose moves it.
 *
 * <p><strong>How a piece is drawn.</strong> Every piece is a player head on an item display with no
 * item transform. Such a head is centred on its anchor in x and z but hangs <em>below</em> it: in
 * its own units it spans {@code y ∈ [-0.5, 0]}. The export was built on that rule and only fits
 * together with it — the thigh ends exactly where the shin starts (y 0.2326), the two robe pieces
 * meet at y 0.810, the feet stand on the ground and the cross bar crosses the cross. An earlier
 * version assumed centred pieces, which put the shoulders above the eyes, the neck at the tip of the
 * crown and the hips inside the robe, so every animation turned around the wrong point.
 *
 * <p><strong>The skeleton.</strong> The legs hang from the hips and fold at the knees. The torso
 * leans about the hip line and carries the head and both arms with it, so a lean never leaves the
 * head floating behind. The head turns about the neck on top of the robe, eyes and crown included.
 */
public final class KingerModel {

    /** Bottom of a head in its own units: the piece hangs from 0 down to here. */
    public static final float PIECE_BOTTOM = -0.5f;
    /** Half the width and depth of a head in its own units. */
    public static final float PIECE_HALF = 0.25f;

    /** Top of the right thigh, where the right leg swings. */
    public static final Vector3f PIVOT_HIP_RIGHT = KingerModel.baseTranslation(Kinger.KingerPart.LEG_RIGHT_UPPER);
    /** Top of the left thigh. */
    public static final Vector3f PIVOT_HIP_LEFT = KingerModel.baseTranslation(Kinger.KingerPart.LEG_LEFT_UPPER);
    /** Right knee: the top of the shin, which is exactly where the thigh ends. */
    public static final Vector3f PIVOT_KNEE_RIGHT = KingerModel.baseTranslation(Kinger.KingerPart.LEG_RIGHT_LOWER);
    /** Left knee. */
    public static final Vector3f PIVOT_KNEE_LEFT = KingerModel.baseTranslation(Kinger.KingerPart.LEG_LEFT_LOWER);
    /**
     * Right shoulder: the inner end of the sleeve, where it leaves the robe, on the body's plane
     * (the export puts it 6 mm in front of it).
     */
    public static final Vector3f PIVOT_SHOULDER_RIGHT = onBodyPlane(KingerModel.baseTranslation(Kinger.KingerPart.ARM_RIGHT));
    /** Left shoulder; the suit is slightly asymmetric, so it does not mirror the right one. */
    public static final Vector3f PIVOT_SHOULDER_LEFT = onBodyPlane(KingerModel.baseTranslation(Kinger.KingerPart.ARM_LEFT));
    /** The hip line on the body axis: the torso leans about it and the legs stay planted. */
    public static final Vector3f PIVOT_TORSO = new Vector3f(0f, (PIVOT_HIP_RIGHT.y + PIVOT_HIP_LEFT.y) * 0.5f, 0f);
    /** Top of the robe, on the body axis: the whole head turns about it. */
    public static final Vector3f PIVOT_NECK = new Vector3f(0f,
            KingerModel.baseTranslation(Kinger.KingerPart.TORSO_UPPER).y, 0f);
    /** Height of the eyes' centres: where he looks from, and where the ranged shot is aimed from. */
    public static final float EYE_HEIGHT = (centre(Kinger.KingerPart.EYE_RIGHT).y + centre(Kinger.KingerPart.EYE_LEFT).y) * 0.5f;

    /** Radians a walking thigh swings by. */
    private static final float STRIDE = 0.3f;
    /** Radians a walking arm sweeps forward and back by, opposite the leg on its side. */
    private static final float ARM_SWING = 0.4f;
    /** Largest angle the head pitches by to follow a player above or below him. */
    public static final float MAX_LOOK = (float) Math.toRadians(30);

    /**
     * How fast a step advances while Kinger walks, in radians per tick: the mob's own tick uses it,
     * and the walk replay of {@code /msc debug geometry} poses the skeleton with it.
     */
    public static final float WALK_RATE = 0.3f;

    /** Fraction of the ranged animation at which the shot leaves his hand: the arm is fully raised. */
    public static final float RANGED_FIRE_PROGRESS = 0.35f;

    // Melee, "Royal Decree": both arms fly up, then sweep down and forward as he lunges.
    private static final float[] MELEE_T = {0f, 0.40f, 0.52f, 1f};
    private static final float[] MELEE_RAISE = {0f, 1.5f, 0.1f, 0f};
    private static final float[] MELEE_FORWARD = {0f, -0.35f, 1.25f, 0f};
    private static final float[] MELEE_LEAN = {0f, 0.12f, -0.25f, 0f};
    private static final float[] MELEE_HEAD = {0f, 0.3f, -0.2f, 0f};

    // Ranged, "Royal Command": the right arm points straight at the target and holds while he fires.
    private static final float[] RANGED_T = {0f, RANGED_FIRE_PROGRESS, 0.75f, 1f};
    private static final float[] RANGED_RIGHT_RAISE = {0f, 0.45f, 0.45f, 0f};
    private static final float[] RANGED_RIGHT_FORWARD = {0f, 1.4f, 1.4f, 0f};
    private static final float[] RANGED_LEFT_RAISE = {0f, 0.25f, 0.25f, 0f};
    private static final float[] RANGED_LEFT_FORWARD = {0f, -0.2f, -0.2f, 0f};
    private static final float[] RANGED_LEAN = {0f, -0.06f, -0.06f, 0f};

    private KingerModel() {
    }

    // ------------------------------------------------------------------ pose

    /**
     * One frame of the skeleton: a rotation per joint. The head and the arms are given in the
     * torso's frame and ride along when it leans; the legs and knees are given in the body's frame.
     */
    public record Pose(Quaternionf torso, Quaternionf head, Quaternionf armRight, Quaternionf armLeft,
                       Quaternionf legRight, Quaternionf legLeft, Quaternionf kneeRight, Quaternionf kneeLeft) {

        public static Pose rest() {
            return new Pose(new Quaternionf(), new Quaternionf(), new Quaternionf(), new Quaternionf(),
                    new Quaternionf(), new Quaternionf(), new Quaternionf(), new Quaternionf());
        }

        /** The rotation a group turns by about its own joint. */
        public Quaternionf limb(Kinger.LimbGroup group) {
            return switch (group) {
                case HEAD -> head;
                case TORSO -> torso;
                case ARM_RIGHT -> armRight;
                case ARM_LEFT -> armLeft;
                case LEG_RIGHT -> legRight;
                case LEG_LEFT -> legLeft;
            };
        }

        /** The rotation of the joint below a group's own, for the pieces that hang from it. */
        public Quaternionf lower(Kinger.KingerPart part) {
            if (!hangsFromSecondJoint(part)) return new Quaternionf();
            return part.group() == Kinger.LimbGroup.LEG_RIGHT ? kneeRight : kneeLeft;
        }
    }

    /**
     * Kinger's pose for one tick.
     *
     * @param walking  whether he is stepping; the legs and arms then follow {@code walkPhase}
     * @param melee    progress of the melee swing in [0, 1], or a negative number when he is not swinging
     * @param ranged   progress of the ranged shot in [0, 1], or a negative number when he is not shooting
     * @param look     how far the head pitches to follow his target, in radians; positive looks up
     */
    public static Pose pose(float walkPhase, boolean walking, float melee, float ranged, float look) {
        Pose pose = Pose.rest();
        if (walking) {
            pose.legRight().rotateX(walkSwing(Kinger.LimbGroup.LEG_RIGHT, walkPhase));
            pose.legLeft().rotateX(walkSwing(Kinger.LimbGroup.LEG_LEFT, walkPhase));
            pose.kneeRight().set(lowerRotation(Kinger.KingerPart.LEG_RIGHT_LOWER, walkPhase));
            pose.kneeLeft().set(lowerRotation(Kinger.KingerPart.LEG_LEFT_LOWER, walkPhase));
            float sweep = armSweep(walkPhase);
            pose.armRight().set(arm(1, 0f, sweep));
            pose.armLeft().set(arm(-1, 0f, -sweep));
        }

        float head = clampLook(look);
        if (melee >= 0f) {
            float p = clamp01(melee);
            float raise = keyframe(p, MELEE_T, MELEE_RAISE);
            float forward = keyframe(p, MELEE_T, MELEE_FORWARD);
            pose.armRight().set(arm(1, raise, forward));
            pose.armLeft().set(arm(-1, raise, forward));
            pose.torso().rotateX(keyframe(p, MELEE_T, MELEE_LEAN));
            pose.kneeRight().set(meleeLowerRotation(Kinger.KingerPart.LEG_RIGHT_LOWER, p));
            pose.kneeLeft().set(meleeLowerRotation(Kinger.KingerPart.LEG_LEFT_LOWER, p));
            head += keyframe(p, MELEE_T, MELEE_HEAD);
        } else if (ranged >= 0f) {
            float p = clamp01(ranged);
            pose.armRight().set(arm(1, keyframe(p, RANGED_T, RANGED_RIGHT_RAISE),
                    keyframe(p, RANGED_T, RANGED_RIGHT_FORWARD)));
            pose.armLeft().set(arm(-1, keyframe(p, RANGED_T, RANGED_LEFT_RAISE),
                    keyframe(p, RANGED_T, RANGED_LEFT_FORWARD)));
            pose.torso().rotateX(keyframe(p, RANGED_T, RANGED_LEAN));
        }
        pose.head().rotateX(head);
        return pose;
    }

    /**
     * An arm's rotation about its shoulder: {@code raise} lifts the hand out to the side and up,
     * {@code forward} sweeps it towards his front. Both read the same for either arm.
     *
     * @param side +1 for the right arm, -1 for the left
     */
    public static Quaternionf arm(int side, float raise, float forward) {
        return new Quaternionf().rotateY(side * forward).rotateZ(side * raise);
    }

    /** The head's pitch towards a point {@code dy} above his eyes and {@code horizontal} away. */
    public static float lookPitch(double dy, double horizontal) {
        if (horizontal < 0.5) return 0f;
        return clampLook((float) Math.atan2(dy, horizontal));
    }

    private static float clampLook(float pitch) {
        return Math.max(-MAX_LOOK, Math.min(MAX_LOOK, pitch));
    }

    private static Vector3f onBodyPlane(Vector3f point) {
        return point.setComponent(2, 0f);
    }

    /** Smoothly interpolates a value through keyframes at the given times. */
    static float keyframe(float t, float[] times, float[] values) {
        if (t <= times[0]) return values[0];
        for (int i = 1; i < times.length; i++) {
            if (t <= times[i]) {
                float span = times[i] - times[i - 1];
                float local = span <= 0f ? 1f : (t - times[i - 1]) / span;
                float eased = local * local * (3f - 2f * local);
                return values[i - 1] + (values[i] - values[i - 1]) * eased;
            }
        }
        return values[values.length - 1];
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    // ------------------------------------------------------------------ walk

    /**
     * How far a leg swings on a walk at a given phase, in radians: the angle its hip turns by.
     * Positive brings the foot forward. The arms sweep about a different axis, see {@link #armSweep}.
     */
    public static float walkSwing(Kinger.LimbGroup group, float phase) {
        return (float) (Math.sin(phase) * switch (group) {
            case LEG_RIGHT -> STRIDE;
            case LEG_LEFT -> -STRIDE;
            case HEAD, TORSO, ARM_RIGHT, ARM_LEFT -> 0f;
        });
    }

    /** How far the right arm sweeps forward at a walk phase; the left arm sweeps the other way. */
    public static float armSweep(float phase) {
        return (float) (-Math.sin(phase) * ARM_SWING);
    }

    /** The shin's fold for a walk at this phase: identity for every piece that is not a shin. */
    public static Quaternionf lowerRotation(Kinger.KingerPart part, float phase) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        return MscLimb.knee(walkSwing(part.group(), phase));
    }

    /** The knees flex into the melee strike, deepest at its peak. */
    public static Quaternionf meleeLowerRotation(Kinger.KingerPart part, float progress) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        float p = clamp01(progress);
        return MscLimb.bendAngle((float) (-0.3f * Math.sin(p * Math.PI)));
    }

    /**
     * The suit at a walk phase: the four limbs as joint, knee (legs only) and the end of the hand or
     * foot, posed with the same maths the display pieces run. {@code /msc debug geometry kinger walk}
     * replays it.
     */
    public static List<MscLimb.Limb> walkPose(float phase) {
        Pose pose = pose(phase, true, -1f, -1f, 0f);
        List<MscLimb.Limb> limbs = new ArrayList<>(4);
        limbs.add(new MscLimb.Limb(PIVOT_SHOULDER_RIGHT, null, end(Kinger.KingerPart.ARM_RIGHT))
                .swung(pose.armRight(), new Quaternionf()));
        limbs.add(new MscLimb.Limb(PIVOT_SHOULDER_LEFT, null, end(Kinger.KingerPart.ARM_LEFT))
                .swung(pose.armLeft(), new Quaternionf()));
        limbs.add(new MscLimb.Limb(PIVOT_HIP_RIGHT, PIVOT_KNEE_RIGHT, end(Kinger.KingerPart.LEG_RIGHT_LOWER))
                .swung(pose.legRight(), pose.kneeRight()));
        limbs.add(new MscLimb.Limb(PIVOT_HIP_LEFT, PIVOT_KNEE_LEFT, end(Kinger.KingerPart.LEG_LEFT_LOWER))
                .swung(pose.legLeft(), pose.kneeLeft()));
        return limbs;
    }

    // ------------------------------------------------------------------ skeleton

    /** The second joint of a limb, or {@code null} when the limb is a single segment: only the legs fold. */
    public static Vector3f secondJoint(Kinger.LimbGroup group) {
        return switch (group) {
            case LEG_RIGHT -> PIVOT_KNEE_RIGHT;
            case LEG_LEFT -> PIVOT_KNEE_LEFT;
            case ARM_RIGHT, ARM_LEFT, HEAD, TORSO -> null;
        };
    }

    /** The pieces that hang from that joint: they fold with it, the rest of the limb does not. */
    public static boolean hangsFromSecondJoint(Kinger.KingerPart part) {
        return part == Kinger.KingerPart.LEG_RIGHT_LOWER || part == Kinger.KingerPart.LEG_LEFT_LOWER;
    }

    /** The joint a group rotates around. */
    public static Vector3f pivot(Kinger.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_SHOULDER_RIGHT;
            case ARM_LEFT -> PIVOT_SHOULDER_LEFT;
            case LEG_RIGHT -> PIVOT_HIP_RIGHT;
            case LEG_LEFT -> PIVOT_HIP_LEFT;
            case HEAD -> PIVOT_NECK;
            case TORSO -> PIVOT_TORSO;
        };
    }

    /** Whether a group rides the torso, so a lean carries it along. */
    public static boolean ridesTorso(Kinger.LimbGroup group) {
        return group == Kinger.LimbGroup.HEAD || group == Kinger.LimbGroup.ARM_RIGHT
                || group == Kinger.LimbGroup.ARM_LEFT;
    }

    /** Where a piece's anchor — the centre of its top face — rests, in model space. */
    public static Vector3f baseTranslation(Kinger.KingerPart part) {
        return new Vector3f(
                part.offset.x - Kinger.KingerPart.CENTER.x,
                part.offset.y,
                part.offset.z - Kinger.KingerPart.CENTER.z
        );
    }

    /**
     * A point of a resting piece, given in the piece's own units: {@code (0, 0, 0)} is its anchor,
     * {@code (0, -0.25, 0)} its centre and {@code (0, -0.5, 0)} the middle of its bottom face.
     */
    public static Vector3f restPoint(Kinger.KingerPart part, float x, float y, float z) {
        Vector3f local = new Vector3f(x * part.scale.x, y * part.scale.y, z * part.scale.z);
        part.rotation.transform(local);
        return local.add(baseTranslation(part));
    }

    /** The centre of a resting piece. */
    public static Vector3f centre(Kinger.KingerPart part) {
        return restPoint(part, 0f, PIECE_BOTTOM * 0.5f, 0f);
    }

    /** The far end of a resting piece: the middle of the face its head hangs down to. */
    public static Vector3f end(Kinger.KingerPart part) {
        return restPoint(part, 0f, PIECE_BOTTOM, 0f);
    }

    /** The eight corners of a resting piece. */
    public static List<Vector3f> corners(Kinger.KingerPart part) {
        List<Vector3f> corners = new ArrayList<>(8);
        for (float x : new float[] {-PIECE_HALF, PIECE_HALF}) {
            for (float y : new float[] {PIECE_BOTTOM, 0f}) {
                for (float z : new float[] {-PIECE_HALF, PIECE_HALF}) {
                    corners.add(restPoint(part, x, y, z));
                }
            }
        }
        return corners;
    }

    /** Where a point that rests on {@code part} ends up in {@code pose}. */
    public static Vector3f posePoint(Kinger.KingerPart part, Vector3f restPoint, Pose pose) {
        Kinger.LimbGroup group = part.group();
        Vector3f pivot = pivot(group);
        Vector3f joint = secondJoint(group);
        Quaternionf own = pose.limb(group);
        Vector3f moved = joint != null && hangsFromSecondJoint(part)
                ? MscLimb.swingAndFold(restPoint, pivot, joint, own, pose.lower(part))
                : MscLimb.swing(restPoint, pivot, own);
        return ridesTorso(group) ? MscLimb.swing(moved, PIVOT_TORSO, pose.torso()) : moved;
    }

    /** The orientation a piece is turned to in {@code pose}, before its own exported rotation. */
    public static Quaternionf poseRotation(Kinger.KingerPart part, Pose pose) {
        Kinger.LimbGroup group = part.group();
        Quaternionf own = hangsFromSecondJoint(part)
                ? MscLimb.chain(pose.limb(group), pose.lower(part))
                : new Quaternionf(pose.limb(group));
        return ridesTorso(group) ? new Quaternionf(pose.torso()).mul(own) : own;
    }

    /** The display transform of one piece in a pose. */
    public static Transformation compose(Kinger.KingerPart part, Pose pose) {
        Vector3f translation = posePoint(part, baseTranslation(part), pose);
        Quaternionf rotation = poseRotation(part, pose).mul(part.rotation);
        return new Transformation(translation, rotation, new Vector3f(part.scale), new Quaternionf());
    }

    /** The tip of his right hand in a pose: where the ranged shot leaves from. */
    public static Vector3f rightHand(Pose pose) {
        return posePoint(Kinger.KingerPart.ARM_RIGHT, end(Kinger.KingerPart.ARM_RIGHT), pose);
    }
}
