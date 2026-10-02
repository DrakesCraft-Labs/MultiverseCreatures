package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Gravity Well: a black singularity opens over the target and the ground lets go — everything near
 * it is lifted towards the core while matter spirals in. Then gravity returns all at once and slams
 * everyone it held back into the floor; the closer to the core, the harder.
 */
public class GravityWellAttack extends ChoreographedAttack.Aerial {

    private static final int OPEN = 16;
    private static final int HOLD = 40;
    private static final double RADIUS = 10;
    private static final double LIFT = 6;

    public GravityWellAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double maxDamage = stage.config("entities.armor-stand-boss.gravity-well-max-damage", 14.0);
        double minDamage = stage.config("entities.armor-stand-boss.gravity-well-min-damage", 5.0);
        Vector ground = stage.onGround(aimAt(stage, stage.target(), 10));
        Vector core = ground.clone().add(new Vector(0, LIFT, 0));
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, OPEN, Poses.CAST_FORWARD.withRightArm(-50, 10, 0).withLeftArm(-50, -10, 0).withHead(30, 0, 0), Ease.IN_OUT);
        t.span(0, OPEN, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, ground, RADIUS, p);
            Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
            fx.line(hands, core, 1.0, fx.dust(Palette.VOID, 1.2f).sometimes(0.6));
            fx.draw(Shapes.sphere(core, 0.4 + p * 1.2, 16), fx.dust(Palette.VOID_DEEP, 2.2f));
        });
        t.at(0, () -> fx.sound(core, Sfx.RESPAWN_ANCHOR_CHARGE, 2.5f, 0.5f));

        t.span(OPEN, OPEN + HOLD, (tick, p) -> {
            fx.draw(Shapes.sphere(core, 1.8, 30), fx.dust(Palette.VOID_DEEP, 2.4f));
            Vector tilt = new Vector(Math.cos(tick * 0.07), 0.25, Math.sin(tick * 0.07)).normalize();
            Vector[] disk = Shapes.planeAxes(tilt);
            for (double r = 2.4; r < 6; r += 0.8) {
                fx.draw(Shapes.circle(core, r, (int) (r * 5), disk[0], disk[1], tick * 0.3 / r),
                        fx.dust(Palette.mix(Palette.AMETHYST, Palette.EMBER, (r - 2.4) / 3.6), 1.3f));
            }
            if (tick % 2 == 0) fx.gather(core, RADIUS, 6, Palette.VOID, 12);
            fx.cloud(Particle.REVERSE_PORTAL, core, 6, 2, 0.1);
            // Debris torn from the floor rises towards the core.
            if (tick % 4 == 0) {
                Vector from = stage.onGround(ground.clone().add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(RADIUS * stage.random().nextDouble())));
                stage.debris(from, core.clone().subtract(from).multiply(0.06).setY(0.35), stage.groundMaterial(from), 18);
            }
            for (Victim victim : stage.victimsIn(Area.cylinder(ground, RADIUS, 2, LIFT + 4))) {
                Vector in = core.clone().subtract(victim.chest());
                victim.fling(in.multiply(0.08).setY(Math.max(0.05, Math.min(0.25, in.getY() * 0.06))));
            }
            if (tick % 10 == 0) fx.sound(core, Sfx.WARDEN_HEARTBEAT, 2f, 0.7f);
        });

        int slam = OPEN + HOLD;
        tween(t, stage, slam - 2, slam + 2, Poses.CAST_FORWARD, Poses.DIVE.withHead(40, 0, 0), Ease.OUT_BACK);
        t.at(slam, () -> {
            fx.flash(core, Palette.VOID);
            fx.burst(core, Particle.REVERSE_PORTAL, 80, 1.2);
            fx.sound(core, Sfx.MACE_SMASH_GROUND, 3f, 0.5f);
            for (Victim victim : stage.victimsIn(Area.cylinder(ground, RADIUS, 2, LIFT + 6))) {
                double closeness = 1 - Math.min(1, victim.position().distance(ground) / RADIUS);
                stage.damage(victim, minDamage + (maxDamage - minDamage) * closeness);
                victim.fling(new Vector(0, -2.2, 0));
            }
        });
        shockwave(t, stage, slam + 2, 12, ground, RADIUS + 2, Palette.AMETHYST, minDamage, null);
        tweenTo(t, stage, slam + 6, slam + 20, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    @Override
    public String getName() {
        return "gravitywell";
    }
}
