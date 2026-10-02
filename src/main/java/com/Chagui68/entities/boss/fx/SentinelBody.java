package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Where the parts of an armour stand are in the world, for a given pose, heading and scale.
 *
 * <p>Effects have to come out of the body: a lance thrust starts at the lance's tip, a cast at the
 * hands, a roar at the head. The old attacks used fixed offsets like "feet + 1.5 blocks", which on a
 * stand scaled 7.5 times is its ankles.
 *
 * <h2>The model</h2>
 * Vanilla's armour-stand model, in model pixels (y grows downwards, the front faces -z): the head
 * pivots at {@code (0, 1, 0)}, the arms at {@code (∓5, 2, 0)} and hang 10 px, the legs at
 * {@code (∓1.9, 12, 0)} and hang 11 px; the feet are at y = 24. A part's rotation is applied as
 * {@code Rz · Ry · Rx}, and the renderer maps the model into the entity's frame with
 * {@code (x, y, z) → (x, -y, -z)}, so the entity faces +z, its left is +x and up is +y. That frame is
 * then turned by the stand's yaw and scaled from the feet.
 *
 * <p>Consequences worth knowing when writing a pose: an arm at {@code x = -90} points forward,
 * {@code -180} points straight up; {@code y > 0} on a forward arm swings it to the boss's right;
 * {@code z > 0} lifts the right arm out sideways ({@code z < 0} for the left).
 *
 * <p>A held item follows vanilla's in-hand transform (the arm's {@code -90° X, 180° Y}, then the
 * handheld display rotation {@code 0, -90, 55}): with the arm hanging, the blade points forward and
 * about ten degrees down. So the spear points up when the arm is raised forward, lies level with the
 * arm low, and a level sweep is a low arm turned on {@code y}.
 */
public final class SentinelBody {

    private static final float PX = 1f / 16f;
    private static final float FEET_Y = 24f;
    /** Length of the spear from the hand to its tip, in model pixels. */
    private static final float SPEAR_PX = 24f;

    private SentinelBody() {
    }

    /** Every point an effect may anchor to, in world coordinates. */
    public record Anatomy(Vector feet, Vector forward, Vector right, double scale,
                          Vector head, Vector eyes, Vector chest,
                          Vector rightShoulder, Vector rightHand, Vector spearTip, Vector spearAxis,
                          Vector leftShoulder, Vector leftHand, Vector shieldFace,
                          Vector rightFoot, Vector leftFoot) {

        /** The point {@code along} of the way from the hand to the spear's tip. */
        public Vector alongSpear(double along) {
            return rightHand.clone().add(spearTip.clone().subtract(rightHand).multiply(along));
        }
    }

    /**
     * Resolves the anatomy.
     *
     * @param feet  the stand's location (the bottom of its feet)
     * @param yaw   Minecraft yaw in degrees (0 faces +z, 90 faces -x)
     * @param scale the stand's scale attribute
     */
    public static Anatomy resolve(Vector feet, float yaw, double scale, Pose pose) {
        Frame f = new Frame(feet, yaw, scale);

        Vector rightShoulder = f.point(-5, 2, 0);
        Vector rightHand = f.limbEnd(-5, 2, pose.rightArm(), 10);
        Vector spearAxis = f.direction(pose.rightArm(), new Vector3f(0, 0.174f, -0.985f).normalize());
        Vector spearTip = rightHand.clone().add(spearAxis.clone().multiply(SPEAR_PX * PX * scale));

        Vector leftShoulder = f.point(5, 2, 0);
        Vector leftHand = f.limbEnd(5, 2, pose.leftArm(), 10);
        Vector shieldAxis = f.direction(pose.leftArm(), new Vector3f(0, 0, -1f));
        Vector shieldFace = leftHand.clone().add(shieldAxis.multiply(4 * PX * scale));

        Vector head = f.limbEnd(0, 1, pose.head(), -6);
        Vector eyes = f.limbEnd(0, 1, pose.head(), -4);
        Vector chest = f.point(0, 5, 0);
        Vector rightFoot = f.limbEnd(-1.9f, 12, pose.rightLeg(), 11);
        Vector leftFoot = f.limbEnd(1.9f, 12, pose.leftLeg(), 11);

        return new Anatomy(feet.clone(), f.forward(), f.right(), scale, head, eyes, chest,
                rightShoulder, rightHand, spearTip, spearAxis, leftShoulder, leftHand, shieldFace,
                rightFoot, leftFoot);
    }

    /** The stand's frame: model pixels in, world coordinates out. */
    private record Frame(Vector feet, float yaw, double scale) {

        Vector forward() {
            double rad = Math.toRadians(yaw);
            return new Vector(-Math.sin(rad), 0, Math.cos(rad));
        }

        Vector right() {
            double rad = Math.toRadians(yaw);
            return new Vector(-Math.cos(rad), 0, -Math.sin(rad));
        }

        /** A model-space point (pixels) to the world. */
        Vector point(float mx, float my, float mz) {
            return toWorld(new Vector3f(mx, my, mz), true);
        }

        /** The end of a limb {@code length} pixels long that pivots at {@code (px, py)}. */
        Vector limbEnd(float px, float py, double[] rotation, float length) {
            Vector3f along = rotate(rotation, new Vector3f(0, length, 0));
            return toWorld(new Vector3f(px, py, 0).add(along), true);
        }

        /** A unit direction carried by a joint's rotation, in the world. */
        Vector direction(double[] rotation, Vector3f modelDirection) {
            return toWorld(rotate(rotation, new Vector3f(modelDirection)), false).normalize();
        }

        private static Vector3f rotate(double[] degrees, Vector3f v) {
            Quaternionf q = new Quaternionf().rotationZYX(
                    (float) Math.toRadians(degrees[2]), (float) Math.toRadians(degrees[1]),
                    (float) Math.toRadians(degrees[0]));
            return q.transform(v);
        }

        /** Model to entity frame ({@code (x, -y, -z)}), then yaw, scale and the feet. */
        private Vector toWorld(Vector3f m, boolean isPoint) {
            double lx = m.x * PX;
            double ly = (isPoint ? FEET_Y - m.y : -m.y) * PX;
            double lz = -m.z * PX;
            double rad = Math.toRadians(yaw);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            double wx = lx * cos - lz * sin;
            double wz = lx * sin + lz * cos;
            Vector v = new Vector(wx, ly, wz).multiply(scale);
            return isPoint ? v.add(feet) : v;
        }
    }
}
