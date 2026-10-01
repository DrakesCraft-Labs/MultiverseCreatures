package com.Chagui68.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Kinematics of a limb made of two segments.
 *
 * <p>The dressed bosses are built from display pieces that were exported as segments: a thigh and a
 * shin, an upper arm and a forearm. Rotating a whole limb about one joint — the hip or the shoulder —
 * keeps it rigid but leaves the joint below it dead, so a walking leg swings like a pendulum and the
 * elbow never bends. This is the maths that adds the second joint:
 *
 * <ul>
 *   <li>the parent joint turns the whole limb, exactly as before;</li>
 *   <li>the second joint then turns the lower segment about the point the parent joint moved it to,
 *       so the lower segment cannot come away from the limb however far it swings.</li>
 * </ul>
 *
 * <p>Every function here is pure and free of the game API, so the models and their tests share one
 * implementation instead of each re-deriving the chain.
 */
public final class MscLimb {

    /** Largest bend a second joint may take, in radians (~69°). */
    public static final float MAX_BEND = 1.2f;
    /** How much of the parent's swing the lower segment adds on top of it. */
    public static final float BEND_PER_SWING = 1.5f;

    private MscLimb() {
    }

    /**
     * One limb of a walking skeleton, in model space: the joint it hangs from, the joint below it —
     * {@code null} when the export gave the limb a single segment — and the piece that reaches
     * furthest, the hand or the foot.
     *
     * <p>{@link #swung} poses the limb with the same two rotations a display piece carries, so a
     * skeleton built from these points moves exactly where the suit does. That is what lets the
     * geometry overlay replay a walk without a boss: these are the model's own points, not a
     * screenshot of one.
     */
    public record Limb(Vector3f pivot, Vector3f joint, Vector3f tip) {

        /** The limb after a step: the parent joint swings it and the second joint folds the end. */
        public Limb swung(Quaternionf upper, Quaternionf lower) {
            if (joint == null) {
                return new Limb(pivot, null, swing(tip, pivot, upper));
            }
            return new Limb(pivot, swing(joint, pivot, upper), swingAndFold(tip, pivot, joint, upper, lower));
        }
    }

    /**
     * The joint between two stacked export segments: halfway between their centres.
     *
     * <p>The two segments are stacked on one axis in the export and their centres sit on either side
     * of the joint, so the midpoint is the joint. Nothing has to be assumed about how big a piece is
     * — an assumption that would be wrong for the pieces whose exported size is not their extent —
     * and a re-export that moves a segment moves the joint with it.
     */
    public static Vector3f jointBetween(Vector3f upperSegment, Vector3f lowerSegment) {
        return new Vector3f(upperSegment).add(lowerSegment).mul(0.5f);
    }

    /** Where a point ends up when the joint above it turns. */
    public static Vector3f swing(Vector3f restPoint, Vector3f pivot, Quaternionf rotation) {
        Vector3f relative = new Vector3f(restPoint).sub(pivot);
        rotation.transform(relative);
        return new Vector3f(pivot).add(relative);
    }

    /**
     * Where a point below a second joint ends up: the parent joint turns the whole limb, then the
     * second joint folds the lower segment about the point the parent moved it to.
     */
    public static Vector3f swingAndFold(Vector3f restPoint, Vector3f parentPivot, Vector3f joint,
                                        Quaternionf upper, Quaternionf lower) {
        Vector3f movedJoint = swing(joint, parentPivot, upper);
        Vector3f relative = new Vector3f(restPoint).sub(joint);
        chain(upper, lower).transform(relative);
        return movedJoint.add(relative);
    }

    /** The rotation a piece below a second joint carries: the parent's, then the joint's. */
    public static Quaternionf chain(Quaternionf upper, Quaternionf lower) {
        return new Quaternionf(lower).mul(upper);
    }

    /**
     * A knee's rotation: it stays straight while the leg swings forward and folds as the leg swings
     * back, so the heel lifts on the back half of the step and the leg meets the ground extended.
     */
    public static Quaternionf knee(float swing) {
        return bend(Math.min(0f, swing));
    }

    /** An elbow's rotation: the mirror of a knee, folding on the forward half of the swing. */
    public static Quaternionf elbow(float swing) {
        return bend(Math.max(0f, swing));
    }

    /**
     * An arbitrary bend angle around X, bounded by [-MAX_BEND, MAX_BEND].
     */
    public static Quaternionf bendAngle(float angle) {
        float clamped = Math.max(-MAX_BEND, Math.min(MAX_BEND, angle));
        return new Quaternionf().rotateX(clamped);
    }

    /** Both joints only ever add to the swing they are given, and never past {@link #MAX_BEND}. */
    private static Quaternionf bend(float swing) {
        return bendAngle(swing * BEND_PER_SWING);
    }
}
