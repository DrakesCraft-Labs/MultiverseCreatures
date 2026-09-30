package com.Chagui68.entities.boss;

import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

/**
 * Pure kinematics of NIX's twenty-seven-part model: where each part rests and how a limb rotation
 * swings it around its joint.
 *
 * <p>The per-part matrices in {@link NixBoss.NixPart} are the reference model's transforms, written
 * in the same flat column-major format the game itself uses. The reference data carries one shared X
 * offset (the whole body was exported about a block off the spine); here the model is re-centred on
 * {@link NixBoss.NixPart#CENTER} so the visible body sits over the invisible armour stand that acts
 * as the hitbox. Without that re-centring the model would render away from its own hitbox and swings
 * aimed at the body would never reach the boss.
 *
 * <p>Right arms live at positive X and right legs at negative X, the same convention the Jack Star
 * model uses; each joint below is therefore on the same side as the parts it drives.
 *
 * <p>Each limb is a stack of six numbered pieces, and the numbering is not the stacking order: the
 * pieces named {@code _4} are the ones the export puts at the joint above the rest (an upper arm or a
 * thigh), while {@code _1}, {@code _2}, {@code _3}, {@code _5} and {@code _6} hang from it. Those are
 * the pieces that fold at the elbow or the knee; see {@link #hangsFromSecondJoint}.
 */
public final class NixModel {

    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.3514f, 1.405f, 0f);
    public static final Vector3f PIVOT_SHOULDER_LEFT = new Vector3f(-0.3514f, 1.405f, 0f);
    public static final Vector3f PIVOT_HIP_RIGHT = new Vector3f(-0.1171f, 0.702f, 0f);
    public static final Vector3f PIVOT_HIP_LEFT = new Vector3f(0.1171f, 0.702f, 0f);
    public static final Vector3f PIVOT_NECK = new Vector3f(0.0f, 1.650f, 0f);
    public static final Vector3f PIVOT_TORSO = new Vector3f(0.0f, 1.171f, 0f);
    /** Right elbow: the joint between the upper arm piece and the forearm stack below it. */
    public static final Vector3f PIVOT_ELBOW_RIGHT = jointOf(NixBoss.NixPart.ARM_R_4, NixBoss.NixPart.ARM_R_5);
    /** Left elbow. */
    public static final Vector3f PIVOT_ELBOW_LEFT = jointOf(NixBoss.NixPart.ARM_L_4, NixBoss.NixPart.ARM_L_5);
    /** Right knee: the joint between the thigh piece and the shin stack below it. */
    public static final Vector3f PIVOT_KNEE_RIGHT = jointOf(NixBoss.NixPart.LEG_R_4, NixBoss.NixPart.LEG_R_5);
    /** Left knee. */
    public static final Vector3f PIVOT_KNEE_LEFT = jointOf(NixBoss.NixPart.LEG_L_4, NixBoss.NixPart.LEG_L_5);

    /** The pieces below a limb's second joint: the ones the {@code _4} piece carries. */
    private static final Set<NixBoss.NixPart> SECOND_SEGMENT = Set.of(
            NixBoss.NixPart.LEG_R_1, NixBoss.NixPart.LEG_R_2, NixBoss.NixPart.LEG_R_3,
            NixBoss.NixPart.LEG_R_5, NixBoss.NixPart.LEG_R_6,
            NixBoss.NixPart.LEG_L_1, NixBoss.NixPart.LEG_L_2, NixBoss.NixPart.LEG_L_3,
            NixBoss.NixPart.LEG_L_5, NixBoss.NixPart.LEG_L_6,
            NixBoss.NixPart.ARM_R_1, NixBoss.NixPart.ARM_R_2, NixBoss.NixPart.ARM_R_3,
            NixBoss.NixPart.ARM_R_5, NixBoss.NixPart.ARM_R_6,
            NixBoss.NixPart.ARM_L_1, NixBoss.NixPart.ARM_L_2, NixBoss.NixPart.ARM_L_3,
            NixBoss.NixPart.ARM_L_5, NixBoss.NixPart.ARM_L_6);

    /** Radians the thigh of a walking leg swings by; the arms swing a little less. */
    private static final float STRIDE = 0.32f;
    private static final float ARM_SWING = 0.25f;

    private NixModel() {
    }

    /** The joint a limb group rotates around. */
    public static Vector3f pivot(NixBoss.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_SHOULDER_RIGHT;
            case ARM_LEFT -> PIVOT_SHOULDER_LEFT;
            case LEG_RIGHT -> PIVOT_HIP_RIGHT;
            case LEG_LEFT -> PIVOT_HIP_LEFT;
            case HEAD -> PIVOT_NECK;
            case TORSO_UPPER, TORSO_LOWER -> PIVOT_TORSO;
        };
    }

    /** The second joint of a limb: every one of NIX's limbs is a stack of two segments. */
    public static Vector3f secondJoint(NixBoss.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_ELBOW_RIGHT;
            case ARM_LEFT -> PIVOT_ELBOW_LEFT;
            case LEG_RIGHT -> PIVOT_KNEE_RIGHT;
            case LEG_LEFT -> PIVOT_KNEE_LEFT;
            case HEAD, TORSO_UPPER, TORSO_LOWER -> null;
        };
    }

    /** The pieces that hang from that joint: they fold with it, the rest of the limb does not. */
    public static boolean hangsFromSecondJoint(NixBoss.NixPart part) {
        return SECOND_SEGMENT.contains(part);
    }

    /**
     * How far a limb swings on a walk at a given phase, in radians: the angle its joint turns by.
     * Positive is forward, which is the direction a knee folds away from.
     */
    public static float walkSwing(NixBoss.LimbGroup group, float phase) {
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
     * for the pieces above it, which the shoulder or hip alone carries.
     */
    public static Quaternionf lowerRotation(NixBoss.NixPart part, float phase) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        float swing = walkSwing(part.group, phase);
        return switch (part.group) {
            case ARM_RIGHT, ARM_LEFT -> MscLimb.elbow(swing);
            default -> MscLimb.knee(swing);
        };
    }

    /** Where a part rests with no limb rotation, in model space. */
    public static Vector3f baseTranslation(NixBoss.NixPart part) {
        return new Vector3f(
                part.offset.x - NixBoss.NixPart.CENTER.x,
                part.offset.y,
                part.offset.z - NixBoss.NixPart.CENTER.z
        );
    }

    /**
     * The display transform of one part: its rest pose rotated about its limb joint.
     *
     * @param limbRotation rotation to apply about the limb joint (identity for a still limb)
     */
    public static Transformation compose(NixBoss.NixPart part, Quaternionf limbRotation) {
        return compose(part, limbRotation, new Quaternionf());
    }

    /**
     * The display transform of one part.
     *
     * @param limbRotation  rotation to apply about the limb joint (identity for a still limb)
     * @param lowerRotation rotation of the joint below it, applied by the pieces that hang from it so
     *                      the elbow or knee folds while the limb keeps the joint it hangs from
     */
    public static Transformation compose(NixBoss.NixPart part, Quaternionf limbRotation, Quaternionf lowerRotation) {
        Vector3f pivot = pivot(part.group);
        Vector3f joint = secondJoint(part.group);
        boolean folds = joint != null && hangsFromSecondJoint(part);

        Vector3f translation = folds
                ? MscLimb.swingAndFold(baseTranslation(part), pivot, joint, limbRotation, lowerRotation)
                : MscLimb.swing(baseTranslation(part), pivot, limbRotation);
        Quaternionf rotation = (folds ? MscLimb.chain(limbRotation, lowerRotation) : new Quaternionf(limbRotation))
                .mul(part.rotation);

        return new Transformation(translation, rotation, new Vector3f(part.scale), new Quaternionf());
    }

    /** The joint between two exported segments, in the re-centred space the parts move in. */
    private static Vector3f jointOf(NixBoss.NixPart upper, NixBoss.NixPart lower) {
        return MscLimb.jointBetween(baseTranslation(upper), baseTranslation(lower));
    }
}
