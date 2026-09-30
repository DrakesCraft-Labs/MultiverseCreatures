package com.Chagui68.entities.boss;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
 */
public final class JackModel {

    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_SHOULDER_LEFT = new Vector3f(-0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_HIP_RIGHT = new Vector3f(-0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_HIP_LEFT = new Vector3f(0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_NECK = new Vector3f(0.0f, 1.87f, 0f);
    public static final Vector3f PIVOT_TORSO = new Vector3f(0.0f, 1.25f, 0f);

    private JackModel() {
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
        Vector3f pivot = pivot(part.group);
        Vector3f relToPivot = baseTranslation(part).sub(pivot);
        limbRotation.transform(relToPivot);

        Vector3f translation = new Vector3f(pivot).add(relToPivot).mul(scale);
        Quaternionf rotation = new Quaternionf(limbRotation).mul(part.rotation);
        Vector3f partScale = new Vector3f(part.scale).mul(scale);

        return new Transformation(translation, rotation, partScale, new Quaternionf());
    }
}
