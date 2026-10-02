package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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

/**
 * Cross Slash: two cuts in the air — one high-left to low-right, one back across — leave an X of
 * light that tears loose and flies at the target, spinning as it goes.
 */
public class CrossSlashAttack extends ChoreographedAttack.Aerial {

    private static final int WIND = 10;
    private static final int CUT = 5;
    private static final double SIZE = 4.5;
    private static final double SPEED = 1.5;

    public CrossSlashAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.8);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WIND, Poses.SWING_BACK.withRightArm(-120, 60, 30), Ease.IN_BACK);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.BREEZE_SHOOT, 2f, 0.7f));
        tween(t, stage, WIND, WIND + CUT, Poses.SWING_BACK.withRightArm(-120, 60, 30), Poses.SWING_THROUGH.withRightArm(-20, -60, -10), Ease.OUT);
        tween(t, stage, WIND + CUT + 2, WIND + 2 * CUT + 2, Poses.SWING_THROUGH.withRightArm(-20, -60, -10), Poses.SWING_BACK.withRightArm(-120, 60, 10), Ease.OUT);
        // The two strokes, drawn where the boss is facing, in front of its chest.
        t.span(WIND, WIND + 2 * CUT + 2, (tick, p) -> {
            Vector center = stage.body().chest().add(stage.forward().multiply(4));
            drawCross(fx, center, stage.forward(), SIZE, 0, Math.min(1, tick / (double) CUT), Math.max(0, (tick - CUT - 2) / (double) CUT));
        });
        t.at(WIND + 1, () -> fx.sound(stage.feet(), Sfx.PLAYER_ATTACK_SWEEP, 2.5f, 0.8f));
        t.at(WIND + CUT + 3, () -> fx.sound(stage.feet(), Sfx.PLAYER_ATTACK_SWEEP, 2.5f, 1.1f));

        int launch = WIND + 2 * CUT + 3;
        t.at(launch, () -> {
            Vector from = stage.body().chest().add(stage.forward().multiply(4));
            Vector velocity = target.chest().subtract(from).normalize().multiply(SPEED);
            fx.sound(from, Sfx.WIND_CHARGE_BURST, 2f, 1.2f);
            Missile cross = new Missile(from, velocity, SIZE * 0.6)
                    .look((at, dir, age) -> drawCross(fx, at, dir, SIZE, age * 0.35, 1, 1))
                    .onHit(victim -> {
                        stage.damage(victim, damage);
                        victim.effect(Affliction.WEAKNESS, 60, 0);
                        victim.push(velocity.clone().normalize().multiply(0.8).setY(0.4));
                    })
                    .onBurst(at -> {
                        fx.flash(at, Palette.HOLY);
                        fx.burst(at, Particle.SWEEP_ATTACK, 8, 0.3);
                        fx.sound(at, Sfx.PLAYER_ATTACK_CRIT, 2f, 0.8f);
                    });
            fly(t, stage, launch + 1, 40, cross);
        });
        tweenTo(t, stage, launch + 4, launch + 18, Poses.HOVER, Ease.IN_OUT);
        t.hold(launch + 42);
        return t;
    }

    /** An X of light facing along {@code facing}, turned by {@code spin}; each stroke drawn up to its progress. */
    private static void drawCross(Fx fx, Vector center, Vector facing, double size, double spin, double first, double second) {
        Vector[] axes = Shapes.planeAxes(facing);
        Vector u = axes[0].clone().multiply(Math.cos(spin)).add(axes[1].clone().multiply(Math.sin(spin)));
        Vector v = axes[1].clone().multiply(Math.cos(spin)).subtract(axes[0].clone().multiply(Math.sin(spin)));
        Vector a = u.clone().add(v).multiply(size * 0.7);
        Vector b = u.clone().subtract(v).multiply(size * 0.7);
        if (first > 0) stroke(fx, center.clone().add(a), center.clone().subtract(a), first);
        if (second > 0) stroke(fx, center.clone().add(b), center.clone().subtract(b), second);
    }

    private static void stroke(Fx fx, Vector from, Vector to, double progress) {
        Vector end = from.clone().add(to.clone().subtract(from).multiply(progress));
        fx.line(from, end, 0.3, fx.dust(Palette.HOLY, 1.8f));
        fx.line(from, end, 0.7, fx.dust(Palette.ICE, 2.4f, 0.15, 1));
    }

    @Override
    public String getName() {
        return "crossslash";
    }
}
