package com.Chagui68.entities.boss;

import com.Chagui68.utils.MscLimb;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure kinematics of Jack Star's eleven-part model: where each part rests and how a limb rotation
 * swings it around its joint.
 *
 * <p>The per-part matrices in {@link JackStarBoss.JackPart} are the reference model's passenger
 * transforms, written in the same row-major format the game itself uses
 * ({@code /data get entity}). The reference data carries one global X offset for the whole body;
 * here the model is re-centred on {@link JackStarBoss.JackPart#CENTER} so the visible body sits over
 * the invisible armour stand that acts as the hitbox. Without that re-centring the model would
 * render most of a block away from its own hitbox and swings aimed at the body would never reach the
 * boss.
 *
 * <p>Right-hand limbs live at negative X, the same convention the Nix model uses; each joint below
 * is therefore on the same side as the parts it drives.
 *
 * <p>Both arms and both legs were exported in two segments, so unlike a rigid limb the elbows and
 * knees work: the shoulder or hip carries the whole limb and the joint below it folds the lower
 * segment, which is what lets a walking leg bend instead of swinging as one piece.
 */
public final class JackModel {

    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_SHOULDER_LEFT = new Vector3f(-0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_HIP_RIGHT = new Vector3f(-0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_HIP_LEFT = new Vector3f(0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_NECK = new Vector3f(0.0f, 1.87f, 0f);
    public static final Vector3f PIVOT_TORSO = new Vector3f(0.0f, 1.25f, 0f);
    /** Right elbow: the joint between the exported upper arm and forearm. */
    public static final Vector3f PIVOT_ELBOW_RIGHT = jointOf(JackStarBoss.JackPart.ARM_R_UPPER,
            JackStarBoss.JackPart.ARM_R_LOWER);
    /** Left elbow. */
    public static final Vector3f PIVOT_ELBOW_LEFT = jointOf(JackStarBoss.JackPart.ARM_L_UPPER,
            JackStarBoss.JackPart.ARM_L_LOWER);
    /** Right knee: the joint between the exported thigh and shin. */
    public static final Vector3f PIVOT_KNEE_RIGHT = jointOf(JackStarBoss.JackPart.LEG_R_UPPER,
            JackStarBoss.JackPart.LEG_R_LOWER);
    /** Left knee. */
    public static final Vector3f PIVOT_KNEE_LEFT = jointOf(JackStarBoss.JackPart.LEG_L_UPPER,
            JackStarBoss.JackPart.LEG_L_LOWER);

    /** Radians the thigh of a walking leg swings by; the arms swing a little less. */
    private static final float STRIDE = 0.32f;
    private static final float ARM_SWING = 0.28f;

    /**
     * How fast a step advances while Jack Star walks, in radians per tick: the boss's own tick uses
     * it, and the walk replay of {@code /msc debug geometry} poses the skeleton with it, so the
     * preview steps at the pace the boss walks.
     */
    public static final float WALK_RATE = 0.18f;

    private JackModel() {
    }

    /**
     * The model at a walk phase: the four limbs as three points each — the joint it hangs from, the
     * elbow or knee, and the hand or foot — posed with the same maths the display pieces run at
     * scale 1, the size the shape-shifting starts from.
     *
     * <p>This is what {@code /msc debug geometry <boss> walk} replays: the skeleton of a walking
     * Jack Star, drawn where a boss would stand, without spawning one. The trunk and the head are
     * not part of it: the audit is how the limbs fold.
     */
    public static List<MscLimb.Limb> walkPose(float phase) {
        List<MscLimb.Limb> limbs = new ArrayList<>(4);
        for (JackStarBoss.LimbGroup group : List.of(JackStarBoss.LimbGroup.ARM_RIGHT,
                JackStarBoss.LimbGroup.ARM_LEFT, JackStarBoss.LimbGroup.LEG_RIGHT,
                JackStarBoss.LimbGroup.LEG_LEFT)) {
            JackStarBoss.JackPart tip = tipOf(group);
            Vector3f joint = secondJoint(group);
            Quaternionf upper = new Quaternionf().rotateX(walkSwing(group, phase));
            Quaternionf lower = (joint == null) ? new Quaternionf() : lowerRotation(tip, phase);
            limbs.add(new MscLimb.Limb(pivot(group), joint, baseTranslation(tip)).swung(upper, lower));
        }
        return limbs;
    }

    /** The piece a limb ends on: the one that reaches furthest from its pivot, the hand or the foot. */
    private static JackStarBoss.JackPart tipOf(JackStarBoss.LimbGroup group) {
        JackStarBoss.JackPart tip = null;
        float reach = -1f;
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            if (part.group != group) continue;
            float distance = baseTranslation(part).distance(pivot(group));
            if (distance > reach) {
                reach = distance;
                tip = part;
            }
        }
        return tip;
    }

    /** The joint a limb group rotates around. */
    public static Vector3f pivot(JackStarBoss.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_SHOULDER_RIGHT;
            case ARM_LEFT -> PIVOT_SHOULDER_LEFT;
            case LEG_RIGHT -> PIVOT_HIP_RIGHT;
            case LEG_LEFT -> PIVOT_HIP_LEFT;
            case HEAD -> PIVOT_NECK;
            case TORSO_UPPER, TORSO_LOWER -> PIVOT_TORSO;
        };
    }

    /** The second joint of a limb: every one of Jack Star's limbs was exported in two segments. */
    public static Vector3f secondJoint(JackStarBoss.LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_ELBOW_RIGHT;
            case ARM_LEFT -> PIVOT_ELBOW_LEFT;
            case LEG_RIGHT -> PIVOT_KNEE_RIGHT;
            case LEG_LEFT -> PIVOT_KNEE_LEFT;
            case HEAD, TORSO_UPPER, TORSO_LOWER -> null;
        };
    }

    /** The pieces that hang from that joint: they fold with it, the rest of the limb does not. */
    public static boolean hangsFromSecondJoint(JackStarBoss.JackPart part) {
        return part == JackStarBoss.JackPart.LEG_R_LOWER || part == JackStarBoss.JackPart.LEG_L_LOWER
                || part == JackStarBoss.JackPart.ARM_R_LOWER || part == JackStarBoss.JackPart.ARM_L_LOWER;
    }

    /**
     * How far a limb swings on a walk at a given phase, in radians: the angle its joint turns by.
     * Positive is forward, which is the direction a knee folds away from.
     */
    public static float walkSwing(JackStarBoss.LimbGroup group, float phase) {
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
    public static Quaternionf lowerRotation(JackStarBoss.JackPart part, float phase) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        float swing = walkSwing(part.group, phase);
        return switch (part.group) {
            case ARM_RIGHT, ARM_LEFT -> MscLimb.elbow(swing);
            default -> MscLimb.knee(swing);
        };
    }

    /**
     * The rotation of the piece below a limb's second joint during a slash animation.
     *
     * <p>During the Zoro-style 3-slash sweep, the swinging arms fold their elbows tightly in the
     * middle of the slash arc and extend as they follow through. Meanwhile, the legs sink into an
     * athletic combat crouch/flexion.
     */
    public static Quaternionf slashLowerRotation(JackStarBoss.JackPart part, float progress) {
        if (!hangsFromSecondJoint(part)) return new Quaternionf();
        float p = Math.max(0f, Math.min(1f, progress));
        if (part.group == JackStarBoss.LimbGroup.ARM_RIGHT || part.group == JackStarBoss.LimbGroup.ARM_LEFT) {
            // Elbows bend inward dynamically during the sword sweep
            float elbow = (float) (0.6f * Math.sin(p * Math.PI));
            return MscLimb.bendAngle(elbow);
        } else if (part.group == JackStarBoss.LimbGroup.LEG_RIGHT || part.group == JackStarBoss.LimbGroup.LEG_LEFT) {
            // Legs crouch / flex knees into combat stance during slash
            float knee = (float) (-0.25f * Math.sin(p * Math.PI));
            return MscLimb.bendAngle(knee);
        }
        return new Quaternionf();
    }

    /** Where a part rests with no limb rotation, in model space and at scale 1. */
    public static Vector3f baseTranslation(JackStarBoss.JackPart part) {
        return new Vector3f(
                part.offset.x - JackStarBoss.JackPart.CENTER.x,
                part.offset.y,
                part.offset.z - JackStarBoss.JackPart.CENTER.z
        );
    }

    /**
     * The display transform of one part: its rest pose rotated about its limb joint, then scaled by
     * the boss's current size.
     *
     * @param limbRotation rotation to apply about the limb joint (identity for a still limb)
     * @param scale        1.0 for the base model, otherwise the shape-shifting factor
     */
    public static Transformation compose(JackStarBoss.JackPart part, Quaternionf limbRotation, float scale) {
        return compose(part, limbRotation, new Quaternionf(), scale);
    }

    /**
     * The display transform of one part.
     *
     * @param limbRotation  rotation to apply about the limb joint (identity for a still limb)
     * @param lowerRotation rotation of the joint below it, applied by the pieces that hang from it so
     *                      the elbow or knee folds while the limb keeps the joint it hangs from
     * @param scale         1.0 for the base model, otherwise the shape-shifting factor
     */
    public static Transformation compose(JackStarBoss.JackPart part, Quaternionf limbRotation,
                                         Quaternionf lowerRotation, float scale) {
        Vector3f pivot = pivot(part.group);
        Vector3f joint = secondJoint(part.group);
        boolean folds = joint != null && hangsFromSecondJoint(part);

        Vector3f translation = folds
                ? MscLimb.swingAndFold(baseTranslation(part), pivot, joint, limbRotation, lowerRotation)
                : MscLimb.swing(baseTranslation(part), pivot, limbRotation);
        translation.mul(scale);
        Quaternionf rotation = (folds ? MscLimb.chain(limbRotation, lowerRotation) : new Quaternionf(limbRotation))
                .mul(part.rotation);
        Vector3f partScale = new Vector3f(part.scale).mul(scale);

        return new Transformation(translation, rotation, partScale, new Quaternionf());
    }

    /** The joint between two exported segments, in the re-centred space the parts move in. */
    private static Vector3f jointOf(JackStarBoss.JackPart upper, JackStarBoss.JackPart lower) {
        return MscLimb.jointBetween(baseTranslation(upper), baseTranslation(lower));
    }
}
