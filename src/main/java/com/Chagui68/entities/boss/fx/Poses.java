package com.Chagui68.entities.boss.fx;

import static com.Chagui68.entities.boss.fx.Pose.REST;

/**
 * The Sentinel's key poses. The spear is in the right hand, the shield in the left. Attacks blend
 * between these with {@link Pose#lerp} and an {@link Ease}; see {@link SentinelBody} for what the
 * angles mean.
 */
public final class Poses {

    private Poses() {
    }

    /** Ready stance: spear low at the side, shield across the body, feet apart. */
    public static final Pose GUARD = REST
            .withRightArm(-12, 0, 8).withLeftArm(-35, 20, -10)
            .withRightLeg(-6, 0, 2).withLeftLeg(6, 0, -2);

    // ------------------------------------------------------------------ spear

    /** Spear raised high over the head, weight on the back foot. */
    public static final Pose SPEAR_OVERHEAD = REST
            .withRightArm(-175, 0, 10).withLeftArm(-50, 0, -25)
            .withBody(-6, 0, 0).withHead(-15, 0, 0)
            .withRightLeg(-10, 0, 0).withLeftLeg(12, 0, 0);

    /** The spear driven down into the ground in front, body bent over it. */
    public static final Pose SPEAR_SLAM = REST
            .withRightArm(22, 0, 0).withLeftArm(-20, 0, -30)
            .withBody(18, 0, 0).withHead(20, 0, 0)
            .withRightLeg(-35, 0, 0).withLeftLeg(20, 0, 0);

    /** Spear drawn back at the hip, coiled to thrust. */
    public static final Pose THRUST_COIL = REST
            .withRightArm(35, 20, 15).withLeftArm(-80, -20, 0)
            .withBody(0, 20, 0).withHead(5, 0, 0)
            .withRightLeg(10, 0, 0).withLeftLeg(-20, 0, 0);

    /** Spear fully extended forward, lunging. */
    public static final Pose THRUST = REST
            .withRightArm(-14, -5, 0).withLeftArm(10, 0, -20)
            .withBody(8, -15, 0).withHead(5, 0, 0)
            .withRightLeg(-30, 0, 0).withLeftLeg(15, 0, 0);

    /** Spear wound back to the right for a sweep. */
    public static final Pose SWING_BACK = REST
            .withRightArm(-22, 85, 20).withLeftArm(-30, 0, -40)
            .withBody(0, 25, 0).withRightLeg(-10, 0, 5);

    /** The sweep carried through to the left. */
    public static final Pose SWING_THROUGH = REST
            .withRightArm(-22, -85, -15).withLeftArm(-20, 0, -20)
            .withBody(0, -25, 0).withLeftLeg(-10, 0, -5);

    /** Spear held overhead and back, about to be hurled. */
    public static final Pose THROW_COIL = REST
            .withRightArm(-165, 175, 0).withLeftArm(-70, -20, 0)
            .withBody(-10, 25, 0).withHead(-10, 0, 0).withLeftLeg(-25, 0, 0);

    /** The follow-through of a throw. */
    public static final Pose THROW = REST
            .withRightArm(-40, -10, 0).withLeftArm(-10, 0, -30)
            .withBody(15, -20, 0).withHead(10, 0, 0)
            .withRightLeg(-20, 0, 0).withLeftLeg(15, 0, 0);

    /** The spear held upright to the sky, the free hand raised: summoning. */
    public static final Pose SPEAR_RAISED = REST
            .withRightArm(-95, 0, 10).withLeftArm(-150, 0, -30)
            .withHead(-30, 0, 0).withBody(-6, 0, 0);

    // ------------------------------------------------------------------ magic

    /** Both arms raised to the sky, head thrown back. */
    public static final Pose CAST_SKY = REST
            .withRightArm(-170, 0, 20).withLeftArm(-170, 0, -20)
            .withHead(-35, 0, 0).withBody(-8, 0, 0);

    /** Both arms pushed forward, palms out. */
    public static final Pose CAST_FORWARD = REST
            .withRightArm(-95, 15, 0).withLeftArm(-95, -15, 0).withHead(5, 0, 0);

    /** Hands brought together in front, gathering power. */
    public static final Pose CHANNEL = REST
            .withRightArm(-110, 30, 0).withLeftArm(-110, -30, 0).withHead(-5, 0, 0);

    /** Crouched, hands spread over the ground. */
    public static final Pose CAST_GROUND = REST
            .withRightArm(-40, 0, 30).withLeftArm(-40, 0, -30)
            .withBody(22, 0, 0).withHead(25, 0, 0)
            .withRightLeg(-30, 0, 0).withLeftLeg(25, 0, 0);

    /** Arms flung wide. */
    public static final Pose SPREAD = REST
            .withRightArm(-90, 0, 85).withLeftArm(-90, 0, -85).withHead(-20, 0, 0);

    /** A roar: arms low and wide, chest out, head back. */
    public static final Pose ROAR = REST
            .withRightArm(-45, 0, 70).withLeftArm(-45, 0, -70)
            .withHead(-40, 0, 0).withBody(-12, 0, 0);

    // ------------------------------------------------------------------ body

    /** Shield raised in front, braced. */
    public static final Pose SHIELD_WALL = REST
            .withLeftArm(-95, -25, 0).withRightArm(-25, 0, 15)
            .withBody(6, 0, 0).withHead(8, 0, 0)
            .withRightLeg(10, 0, 0).withLeftLeg(-15, 0, 0);

    /** One leg lifted high before a stomp. */
    public static final Pose STOMP_LIFT = REST
            .withRightLeg(-75, 0, 0).withLeftLeg(5, 0, 0)
            .withRightArm(-40, 0, 40).withLeftArm(-40, 0, -40).withBody(-10, 0, 0);

    /** The stomp landed. */
    public static final Pose STOMP_DOWN = REST
            .withRightLeg(-10, 0, 0).withLeftLeg(15, 0, 0)
            .withRightArm(-20, 0, 30).withLeftArm(-20, 0, -30)
            .withBody(18, 0, 0).withHead(15, 0, 0);

    /** Down on one knee, spear planted. */
    public static final Pose KNEEL = REST
            .withRightLeg(-85, 0, 0).withLeftLeg(10, 0, 0)
            .withBody(25, 0, 0).withHead(15, 0, 0)
            .withRightArm(25, 0, 10).withLeftArm(-30, 0, -20);

    // ------------------------------------------------------------------ flight

    /** Hovering: legs trailing, arms out for balance. */
    public static final Pose HOVER = REST
            .withRightLeg(12, 0, 4).withLeftLeg(18, 0, -4)
            .withRightArm(-30, 0, 45).withLeftArm(-30, 0, -45).withHead(10, 0, 0);

    /** Diving: arms overhead, body folded forward. */
    public static final Pose DIVE = REST
            .withRightArm(-170, 0, 5).withLeftArm(-170, 0, -5)
            .withBody(25, 0, 0).withHead(30, 0, 0)
            .withRightLeg(25, 0, 0).withLeftLeg(25, 0, 0);
}
