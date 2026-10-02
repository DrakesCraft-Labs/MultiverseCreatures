package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Spear Cyclone: the Sentinel whirls its spear over its head until the air around it turns into a
 * spiralling wall of wind, then sweeps it down and sends the cyclone rolling across the arena after
 * its target. The cyclone drags in and lifts anyone near it, tearing up the floor on its way, and
 * bursts apart at the end of its run, flinging everything it holds.
 */
public class SpearCycloneAttack extends ChoreographedAttack.Ground {

    private static final int SPIN = 30;
    private static final int TRAVEL = 50;
    private static final double SPEED = 0.55;
    private static final double RADIUS = 3.2;
    private static final double HEIGHT = 11;
    private static final double PULL = 7;

    public SpearCycloneAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.spear-cyclone-damage", 4.0);
        Victim target = stage.target();
        Timeline t = new Timeline();
        Vector[] eye = new Vector[1];
        double[] spin = {0};

        // Whirl: the spear arm circles overhead, faster and faster.
        tweenTo(t, stage, 0, 8, Poses.SPEAR_RAISED, Ease.OUT);
        t.span(8, SPIN, (tick, p) -> {
            double angle = tick * (0.35 + p * 0.5);
            Pose whirl = Poses.SPEAR_RAISED.withRightArm(-165, Math.toDegrees(angle) % 360 - 180, 15)
                    .withBody(0, Math.sin(angle) * 10, 0);
            stage.pose(whirl);
        });
        t.span(0, SPIN, (tick, p) -> {
            spin[0] += 0.25 + p * 0.4;
            Vector feet = stage.feet();
            double r = 2 + p * 4;
            for (int arm = 0; arm < 3; arm++) {
                fx.draw(Shapes.helix(feet, r, 3 + p * 9, 1.2, 18, spin[0] + arm * 2.09),
                        fx.dust(Palette.mix(Palette.ASH, Palette.STORM, p), 1.3f));
            }
            fx.ring(stage.body().spearTip(), 1.5 + p * 2, 0.6, -spin[0], fx.dust(Palette.ICE, 1.0f));
            if (tick % 2 == 0) fx.cloud(Particle.CLOUD, feet.clone().add(new Vector(0, 0.5, 0)), 3, r, 0.1, 0.05);
            if (tick % 6 == 0) fx.sound(feet, Sfx.BREEZE_WIND_BURST, 1.5f, 0.5f + (float) p);
            if (tick % 4 == 0) {
                Vector[] path = {stage.feet(), aimAt(stage, target, 20)};
                Telegraph.line(stage, path[0], path[0].clone().add(Shapes.flat(path[1].clone().subtract(path[0])).normalize()
                        .multiply(SPEED * TRAVEL)), RADIUS * 2, p);
            }
        });

        // Release: the spear sweeps down and the cyclone sets off.
        tween(t, stage, SPIN, SPIN + 4, Poses.SPEAR_RAISED, Poses.SWING_THROUGH, Ease.OUT_BACK);
        t.at(SPIN, () -> {
            eye[0] = stage.onGround(stage.feet().add(stage.forward().multiply(3)));
            fx.sound(eye[0], Sfx.BREEZE_SHOOT, 3f, 0.5f);
            fx.sound(eye[0], Sfx.PLAYER_ATTACK_SWEEP, 2.5f, 0.6f);
            fx.flatBurst(eye[0], Particle.GUST, 6, 0.5);
        });
        t.span(SPIN, SPIN + TRAVEL, (tick, p) -> {
            if (eye[0] == null) return;
            // Drifts after the target, turning slowly so it can be outrun sideways.
            Vector want = target != null ? Shapes.flat(target.position().subtract(eye[0])) : stage.forward();
            Vector heading = want.lengthSquared() < 0.01 ? stage.forward() : want.normalize();
            eye[0] = stage.onGround(eye[0].clone().add(heading.multiply(SPEED)));
            drawCyclone(stage, eye[0], tick, Math.min(1, tick / 6.0));
            if (tick % 8 == 0) fx.sound(eye[0], Sfx.BREEZE_WIND_BURST, 2f, 0.6f);
            for (Victim victim : stage.victimsIn(Area.cylinder(eye[0], PULL, 1, HEIGHT))) {
                Vector in = Shapes.flat(eye[0].clone().subtract(victim.position()));
                double dist = in.length();
                Vector tangent = new Vector(-in.getZ(), 0, in.getX()).normalize().multiply(0.25);
                Vector pull = (dist < 0.1 ? new Vector() : in.normalize().multiply(0.18)).add(tangent);
                if (dist < RADIUS) pull.setY(0.35);
                victim.push(pull);
                if (dist < RADIUS && tick % 5 == 0) stage.damage(victim, damage);
            }
        });
        int burst = SPIN + TRAVEL;
        t.at(burst, () -> {
            if (eye[0] == null) return;
            Vector mid = eye[0].clone().add(new Vector(0, 3, 0));
            fx.impact(mid, Palette.STORM, 3);
            fx.burst(mid, Particle.GUST_EMITTER_SMALL, 3, 0);
            fx.burst(mid, Particle.CLOUD, 60, 0.6);
            fx.sound(mid, Sfx.WIND_CHARGE_BURST, 3f, 0.5f);
            fx.sound(mid, Sfx.EXPLODE, 2f, 1.2f);
            for (int i = 0; i < 10; i++) {
                Vector out = Shapes.heading(i * Math.PI / 5).multiply(0.4).setY(0.6);
                stage.debris(eye[0].clone().add(new Vector(0, 1, 0)), out, stage.groundMaterial(eye[0]), 24);
            }
            stage.hit(Area.cylinder(eye[0], RADIUS + 3, 1, HEIGHT), damage * 3, victim -> {
                Vector away = Shapes.flat(victim.position().subtract(eye[0]));
                Vector dir = away.lengthSquared() < 0.01 ? new Vector(1, 0, 0) : away.normalize();
                victim.fling(dir.multiply(1.8).setY(1.0));
                victim.effect(Affliction.NAUSEA, 80, 0);
            });
        });
        recover(t, stage, SPIN + 8, SPIN + 24, Poses.GUARD);
        return t;
    }

    /** A column of wind: three twisting arms of dust and gusts, wide at the top, debris whirling in it. */
    private static void drawCyclone(Stage stage, Vector base, int tick, double grown) {
        Fx fx = stage.fx();
        double phase = tick * 0.6;
        for (int arm = 0; arm < 3; arm++) {
            for (double y = 0; y < HEIGHT * grown; y += 0.5) {
                double r = (0.6 + RADIUS * y / HEIGHT) * grown;
                double a = phase + arm * 2.09 + y * 0.5;
                Vector p = base.clone().add(new Vector(Math.cos(a) * r, y, Math.sin(a) * r));
                fx.dust(Palette.mix(Palette.STONE, Palette.ICE, y / HEIGHT), 1.5f).at(p);
            }
        }
        if (tick % 2 == 0) fx.cloud(Particle.CLOUD, base.clone().add(new Vector(0, HEIGHT * 0.5, 0)), 3, RADIUS, HEIGHT * 0.4, 0.05);
        if (tick % 3 == 0) fx.cloud(Particle.GUST, base.clone().add(new Vector(0, 2, 0)), 1, RADIUS * 0.6, 1.5, 0);
        Material floor = stage.groundMaterial(base);
        fx.crumble(floor, 6, 1.2).at(base.clone().add(new Vector(0, 0.5, 0)));
        if (tick % 4 == 0) {
            double a = stage.random().nextDouble() * Math.PI * 2;
            stage.debris(base.clone().add(Shapes.heading(a).multiply(1.5)), Shapes.heading(a + 1.5).multiply(0.3).setY(0.7), floor, 18);
        }
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SPIN + 24;
    }

    @Override
    public String getName() {
        return "spearcyclone";
    }
}
