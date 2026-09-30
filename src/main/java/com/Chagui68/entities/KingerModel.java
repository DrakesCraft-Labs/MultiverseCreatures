package com.Chagui68.entities;

import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Pure kinematics of Kinger's fifteen-part suit: where each piece rests and how a limb rotation
 * swings it around its joint.
 *
 * <p>The per-piece matrices in {@link Kinger.KingerPart} are the reference model's transforms,
 * written in the same flat column-major format the game itself uses. The reference data was exported
 * around the torso, not around a block, so {@link Kinger.KingerPart#CENTER} re-centres the suit on
 * the torso axis: the trunk, the legs and the head all share that axis to within a millimetre, which
 * is what puts the visible body over the invisible armour stand that carries the hitbox.
 *
 * <p>A limb is a rigid group. Before this class existed every piece rotated about its own anchor, so
 * the shin swung from its own knee and the thigh from its own hip: the leg came apart as soon as
 * Kinger walked. Here the whole group revolves around one shared joint, exactly like the Jack Star
 * and NIX models, and the pieces below that joint fold about a second one of their own, so the knee
 * bends with the step instead of the leg swinging as one piece.
 */
public final class KingerModel {

    /** Top of the right arm: the shoulder it hangs from. */
    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.0742f, 1.6296f, 0f);
    /** Top of the left arm; the suit is asymmetric, so it does not mirror the right shoulder. */
    public static final Vector3f PIVOT_SHOULDER_LEFT = new Vector3f(-0.0686f, 1.5812f, 0f);
    /** Top of the right thigh, where the right leg, its boot plate and the foot swing. */
    public static final Vector3f PIVOT_HIP_RIGHT = new Vector3f(0.0929f, 0.9094f, 0f);
    /** Top of the left thigh. */
    public static final Vector3f PIVOT_HIP_LEFT = new Vector3f(-0.0923f, 0.9101f, 0f);
    /** Base of the skull: the point the head pitches around while it tracks a player. */
    public static final Vector3f PIVOT_NECK = new Vector3f(0.0f, 1.6905f, 0f);
    /** Waist: the point the whole trunk leans around. */
    public static final Vector3f PIVOT_TORSO = new Vector3f(0.0f, 0.8101f, 0f);
    /** Right knee: the joint between the exported thigh and shin, and where the shin folds. */
    public static final Vector3f PIVOT_KNEE_RIGHT = kneeOf(Kinger.KingerPart.LEG_RIGHT_UPPER,
            Kinger.KingerPart.LEG_RIGHT_LOWER);
    /** Left knee. */
    public static final Vector3f PIVOT_KNEE_LEFT = kneeOf(Kinger.KingerPart.LEG_LEFT_UPPER,
            Kinger.KingerPart.LEG_LEFT_LOWER);

    /** Radians the thigh of a walking leg swings by; the arms swing a little further. */
    private static final float STRIDE = 0.3f;
    private static final float ARM_SWING = 0.35f;

    private KingerModel() {
    }

    /**
     * The second joint of a limb, or {@code null} when the limb is a single segment. Kinger has one
     * piece per arm, so only the legs fold; the helper stays general for the other models.
     */
    public static Vector3f secondJoint(Kinger.LimbGroup group) {
        return switch (group) {
            case LEG_RIGHT -> PIVOT_KNEE_RIGHT;
            case LEG_LEFT -> PIVOT_KNEE_LEFT;
            case ARM_RIGHT, ARM_LEFT, HEAD, TORSO_UPPER, TORSO_LOWER -> null;
        };
    }

    /** The pieces that hang from that joint: they fold with it, the rest of the limb does not. */
    public static boolean hangsFromSecondJoint(Kinger.KingerPart part) {
        return part == Kinger.KingerPart.LEG_RIGHT_LOWER || part == Kinger.KingerPart.LEG_LEFT_LOWER;
    }

    /**
     * How far a limb swings on a walk at a given phase, in radians: the angle its joint turns by.
     * Positive is forward, which is the direction a leg folds away from.
     */
    public static float walkSwing(Kinger.LimbGroup group, float phase) {
        return (float) (Math.sin(phase) * switch (group) {
            case LEG_RIGHT -> STRIDE;
            case LEG_LEFT -> -STRIDE;
            case ARM_RIGHT -> -ARM_SWING;
            case ARM_LEFT -> ARM_SWING;
            case HEAD, TORSO_UPPER, TORSO_LOWER -> 0f;
        });
    }

    /**
     * The rotation of the piece of a limb below its second joint, for a walk at this phase: identity
     * for the pieces above it, which the parent's joint alone carries.
     */
    public static Quaternionf lowerRotation(Kinger.KingerPart part, float phase) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        float swing = walkSwing(part.group(), phase);
        return switch (part.group()) {
            case ARM_RIGHT, ARM_LEFT -> MscLimb.elbow(swing);
            default -> MscLimb.knee(swing);
        };
    }

    /** The joint a limb group rotates around. */
    public static Vector3f pivot(Kinger.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_SHOULDER_RIGHT;
            case ARM_LEFT -> PIVOT_SHOULDER_LEFT;
            case LEG_RIGHT -> PIVOT_HIP_RIGHT;
            case LEG_LEFT -> PIVOT_HIP_LEFT;
            case HEAD -> PIVOT_NECK;
            case TORSO_UPPER, TORSO_LOWER -> PIVOT_TORSO;
        };
    }

    /** Where a piece rests with no limb rotation, in model space. */
    public static Vector3f baseTranslation(Kinger.KingerPart part) {
        return new Vector3f(
                part.offset.x - Kinger.KingerPart.CENTER.x,
                part.offset.y,
                part.offset.z - Kinger.KingerPart.CENTER.z
        );
    }

    /**
     * The display transform of one piece: its rest pose rotated about its limb joint.
     *
     * @param limbRotation rotation to apply about the limb joint (identity for a still limb)
     */
    public static Transformation compose(Kinger.KingerPart part, Quaternionf limbRotation) {
        return compose(part, limbRotation, new Quaternionf());
    }

    /**
     * The display transform of one piece.
     *
     * @param limbRotation rotation to apply about the limb joint (identity for a still limb)
     * @param lowerRotation rotation of the joint below it, applied by the pieces that hang from it so
     *                      the knee folds while the thigh keeps the joint the whole limb hangs from
     */
    public static Transformation compose(Kinger.KingerPart part, Quaternionf limbRotation, Quaternionf lowerRotation) {
        Vector3f pivot = pivot(part.group());
        Vector3f joint = secondJoint(part.group());
        boolean folds = joint != null && hangsFromSecondJoint(part);

        Vector3f translation = folds
                ? MscLimb.swingAndFold(baseTranslation(part), pivot, joint, limbRotation, lowerRotation)
                : MscLimb.swing(baseTranslation(part), pivot, limbRotation);
        Quaternionf rotation = (folds ? MscLimb.chain(limbRotation, lowerRotation) : new Quaternionf(limbRotation))
                .mul(part.rotation);

        return new Transformation(translation, rotation, new Vector3f(part.scale), new Quaternionf());
    }

    /** The joint between an exported thigh and shin, in the re-centred space the pieces move in. */
    private static Vector3f kneeOf(Kinger.KingerPart thigh, Kinger.KingerPart shin) {
        return MscLimb.jointBetween(baseTranslation(thigh), baseTranslation(shin));
    }
}
