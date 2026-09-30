package com.Chagui68.entities;

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
 * and NIX models.
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

    private KingerModel() {
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
        Vector3f pivot = pivot(part.group());
        Vector3f relToPivot = baseTranslation(part).sub(pivot);
        limbRotation.transform(relToPivot);

        Vector3f translation = new Vector3f(pivot).add(relToPivot);
        Quaternionf rotation = new Quaternionf(limbRotation).mul(part.rotation);

        return new Transformation(translation, rotation, new Vector3f(part.scale), new Quaternionf());
    }
}
