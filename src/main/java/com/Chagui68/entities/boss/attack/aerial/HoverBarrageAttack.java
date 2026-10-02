package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Hover Barrage: the Sentinel rises into the air (if it is not already there) and writes a glowing X
 * in front of it with the tip of its spear, stroke by stroke, then sends it at a player; once per
 * player. Then it comes back down to where it was.
 */
public class HoverBarrageAttack extends ChoreographedAttack {

    private static final int RISE = 20;
    private static final int TRACE = 12;
    private static final int CYCLE = 22;
    private static final double SIZE = 3;
    private static final double SPEED = 1.4;
    private static final double BLAST = 4;

    public HoverBarrageAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return !instance.hoverBarrageActive && !instance.shieldSealActive;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        List<Victim> victims = stage.victims();
        if (victims.isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.hover-barrage-damage", 12.0);
        BossInstance instance = stage.instance();
        boolean grounded = instance == null || !instance.isFlying;
        Vector start = stage.feet();
        Vector hover = grounded ? start.clone().add(new Vector(0, 15, 0)) : start.clone();
        int rise = grounded ? RISE : 0;
        int marks = Math.min(victims.size(), 4);
        int barrageEnd = rise + marks * CYCLE;
        Timeline t = new Timeline();
        List<Missile> missiles = new ArrayList<>();
        cleanupMissiles(t, missiles);
        if (instance != null) {
            instance.hoverBarrageActive = true;
            t.onFinish(() -> instance.hoverBarrageActive = false);
        }

        if (grounded) {
            tweenTo(t, stage, 0, rise, Poses.HOVER, Ease.IN_OUT);
            t.span(0, rise, (tick, p) -> {
                stage.moveTo(start.clone().add(hover.clone().subtract(start).multiply(Ease.at(Ease.IN_OUT, p))));
                fx.cloud(Particle.CLOUD, stage.feet(), 6, 1.5, 0.05);
                fx.ring(stage.onGround(start).add(new Vector(0, 0.3, 0)), 3 + p * 5, 0.8, tick * 0.2, fx.dust(Palette.ICE, 1.4f));
            });
            t.at(0, () -> fx.sound(start, Sfx.DRAGON_FLAP, 3f, 0.5f));
        }

        for (int m = 0; m < marks; m++) {
            Victim mark = victims.get(m);
            int at = rise + m * CYCLE;
            tween(t, stage, at, at + TRACE / 2, Poses.HOVER, Poses.SWING_BACK.withRightArm(-110, 50, 20), Ease.OUT);
            tween(t, stage, at + TRACE / 2, at + TRACE, Poses.SWING_BACK.withRightArm(-110, 50, 20), Poses.SWING_THROUGH.withRightArm(-40, -50, -10), Ease.OUT);
            Vector[] center = new Vector[1];
            t.span(at, at + TRACE, (tick, p) -> {
                if (center[0] == null) center[0] = stage.body().chest().add(stage.forward().multiply(6));
                double first = Math.min(1, tick / (TRACE / 2.0));
                double second = Math.max(0, (tick - TRACE / 2.0) / (TRACE / 2.0));
                drawX(fx, center[0], mark.chest().subtract(center[0]), first, second, 0);
                if (tick == 0 || tick == TRACE / 2) fx.sound(center[0], Sfx.PLAYER_ATTACK_SWEEP, 2f, 1.3f);
            });
            t.at(at + TRACE, () -> {
                Vector from = center[0];
                Vector velocity = mark.chest().subtract(from).normalize().multiply(SPEED);
                fx.sound(from, Sfx.WIND_CHARGE_BURST, 2f, 1.4f);
                Missile x = new Missile(from, velocity, 2)
                        .homing(mark, 0.04)
                        .look((pos, dir, age) -> drawX(fx, pos, dir, 1, 1, age * 0.3))
                        .onBurst(pos -> {
                            fx.impact(pos, Palette.HOLY, 2.5);
                            fx.sound(pos, Sfx.EXPLODE, 1.5f, 1.2f);
                            stage.hit(Area.sphere(pos, BLAST).or(Area.cylinder(stage.onGround(pos), BLAST, 1, 3)), damage,
                                    victim -> victim.push(new Vector(0, 0.5, 0)));
                        });
                missiles.add(x);
                fly(t, stage, at + TRACE + 1, 40, x);
            });
        }

        if (grounded) {
            tweenTo(t, stage, barrageEnd + 8, barrageEnd + 28, Poses.GUARD, Ease.IN_OUT);
            t.span(barrageEnd + 8, barrageEnd + 28, (tick, p) -> stage.moveTo(hover.clone().add(start.clone().subtract(hover).multiply(Ease.at(Ease.IN, p)))));
            t.at(barrageEnd + 28, () -> {
                fx.flatBurst(stage.onGround(start), Particle.CLOUD, 30, 0.6);
                fx.sound(start, Sfx.DRAGON_FLAP, 2f, 0.7f);
            });
            t.hold(barrageEnd + 44);
        } else {
            tweenTo(t, stage, barrageEnd, barrageEnd + 14, Poses.HOVER, Ease.IN_OUT);
            t.hold(barrageEnd + 42);
        }
        return t;
    }

    /** A glowing X facing along {@code facing}, its strokes drawn up to their progress. */
    private static void drawX(Fx fx, Vector center, Vector facing, double first, double second, double spin) {
        Vector[] axes = Shapes.planeAxes(facing);
        Vector u = axes[0].clone().multiply(Math.cos(spin)).add(axes[1].clone().multiply(Math.sin(spin)));
        Vector v = axes[1].clone().multiply(Math.cos(spin)).subtract(axes[0].clone().multiply(Math.sin(spin)));
        Vector a = u.clone().add(v).multiply(SIZE * 0.7);
        Vector b = u.clone().subtract(v).multiply(SIZE * 0.7);
        if (first > 0) {
            Vector from = center.clone().add(a);
            fx.line(from, from.clone().add(a.clone().multiply(-2 * first)), 0.3, fx.dust(Palette.GOLD, 1.8f).and(fx.particle(Particle.END_ROD).sometimes(0.2)));
        }
        if (second > 0) {
            Vector from = center.clone().add(b);
            fx.line(from, from.clone().add(b.clone().multiply(-2 * second)), 0.3, fx.dust(Palette.GOLD, 1.8f).and(fx.particle(Particle.END_ROD).sometimes(0.2)));
        }
    }

    @Override
    public String getName() {
        return "hoverbarrage";
    }
}
