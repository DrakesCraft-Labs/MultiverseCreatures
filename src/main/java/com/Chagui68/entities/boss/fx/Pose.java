package com.Chagui68.entities.boss.fx;

import org.bukkit.util.EulerAngle;

/**
 * A full armour-stand pose: six joints, each as an X/Y/Z rotation in degrees.
 *
 * <p>Degrees on purpose: every pose in this package is written by hand, and "-90 means the arm
 * points forward" reads far better than a radian. {@link #lerp} blends two poses joint by joint,
 * which is what turns a list of key poses into an animation.
 */
public record Pose(double[] head, double[] body, double[] leftArm, double[] rightArm,
                   double[] leftLeg, double[] rightLeg) {

    private static final double[] ZERO = {0, 0, 0};

    /** Every joint at rest. */
    public static final Pose REST = new Pose(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);

    public static double[] deg(double x, double y, double z) {
        return new double[]{x, y, z};
    }

    public Pose withHead(double x, double y, double z) {
        return new Pose(deg(x, y, z), body, leftArm, rightArm, leftLeg, rightLeg);
    }

    public Pose withBody(double x, double y, double z) {
        return new Pose(head, deg(x, y, z), leftArm, rightArm, leftLeg, rightLeg);
    }

    public Pose withLeftArm(double x, double y, double z) {
        return new Pose(head, body, deg(x, y, z), rightArm, leftLeg, rightLeg);
    }

    public Pose withRightArm(double x, double y, double z) {
        return new Pose(head, body, leftArm, deg(x, y, z), leftLeg, rightLeg);
    }

    public Pose withLeftLeg(double x, double y, double z) {
        return new Pose(head, body, leftArm, rightArm, deg(x, y, z), rightLeg);
    }

    public Pose withRightLeg(double x, double y, double z) {
        return new Pose(head, body, leftArm, rightArm, leftLeg, deg(x, y, z));
    }

    /** The pose {@code t} of the way from this one to {@code to}. */
    public Pose lerp(Pose to, double t) {
        return new Pose(mix(head, to.head, t), mix(body, to.body, t), mix(leftArm, to.leftArm, t),
                mix(rightArm, to.rightArm, t), mix(leftLeg, to.leftLeg, t), mix(rightLeg, to.rightLeg, t));
    }

    private static double[] mix(double[] a, double[] b, double t) {
        return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    public static EulerAngle euler(double[] degrees) {
        return new EulerAngle(Math.toRadians(degrees[0]), Math.toRadians(degrees[1]), Math.toRadians(degrees[2]));
    }

    public static double[] degrees(EulerAngle angle) {
        return deg(Math.toDegrees(angle.getX()), Math.toDegrees(angle.getY()), Math.toDegrees(angle.getZ()));
    }
}
