package com.Chagui68.entities.boss;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
 */
public final class NixModel {

    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.3514f, 1.405f, 0f);
    public static final Vector3f PIVOT_SHOULDER_LEFT = new Vector3f(-0.3514f, 1.405f, 0f);
    public static final Vector3f PIVOT_HIP_RIGHT = new Vector3f(-0.1171f, 0.702f, 0f);
    public static final Vector3f PIVOT_HIP_LEFT = new Vector3f(0.1171f, 0.702f, 0f);
    public static final Vector3f PIVOT_NECK = new Vector3f(0.0f, 1.650f, 0f);
    public static final Vector3f PIVOT_TORSO = new Vector3f(0.0f, 1.171f, 0f);

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
        Vector3f pivot = pivot(part.group);
        Vector3f relToPivot = baseTranslation(part).sub(pivot);
        limbRotation.transform(relToPivot);

        Vector3f translation = new Vector3f(pivot).add(relToPivot);
        Quaternionf rotation = new Quaternionf(limbRotation).mul(part.rotation);

        return new Transformation(translation, rotation, new Vector3f(part.scale), new Quaternionf());
    }
}
