package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Lance Flurry: three thrusts in quick succession into the cone in front, each a streak of light
 * off the spear's tip; the third lands harder and sends a ring of air out from its point.
 */
public class LanceFlurryAttack extends ChoreographedAttack.Ground {

    private static final int WARN = 14;
    private static final int CYCLE = 9;
    private static final double LENGTH = 14;
    private static final double HALF = Math.toRadians(28);

    public LanceFlurryAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.lance-flurry-damage", 7.0);
        Vector feet = stage.feet();
        Vector forward = stage.forward();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WARN, Poses.THRUST_COIL, Ease.IN_OUT);
        t.span(0, WARN + CYCLE * 3, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.cone(stage, feet, forward, HALF, LENGTH, Math.min(1, (double) tick / WARN));
        });
        t.at(0, () -> fx.sound(feet, Sfx.TRIDENT_RIPTIDE, 2f, 0.6f));
        t.span(0, WARN, (tick, p) -> fx.draw(Shapes.sphere(stage.body().spearTip(), 0.4 + p, 8), fx.dust(Palette.HOLY, 1.4f)));

        for (int i = 0; i < 3; i++) {
            int start = WARN + i * CYCLE;
            boolean last = i == 2;
            double spread = (i - 1) * Math.toRadians(14);
            tween(t, stage, start, start + 3, Poses.THRUST_COIL, Poses.THRUST.withRightArm(-14, -5 - Math.toDegrees(spread), 0), Ease.OUT_BACK);
            t.at(start + 2, () -> {
                Vector tip = stage.body().spearTip();
                Vector dir = Shapes.heading(Math.atan2(forward.getZ(), forward.getX()) + spread);
                Vector end = tip.clone().add(dir.clone().multiply(LENGTH * 0.55)).setY(feet.getY() + 1.5);
                fx.line(stage.body().rightHand(), end, 0.3, fx.dust(Palette.HOLY, 1.8f).and(fx.particle(Particle.CRIT).sometimes(0.5)));
                fx.line(tip, end, 0.6, fx.fade(Palette.GOLD, Palette.EMBER, 1.2f));
                fx.cloud(Particle.SWEEP_ATTACK, end, 2, 0.6, 0);
                fx.sound(tip, last ? Sfx.PLAYER_ATTACK_CRIT : Sfx.PLAYER_ATTACK_STRONG, 2f, last ? 0.5f : 0.9f + 0.15f * (float) Math.random());
                Area stab = Area.cone(feet, dir, Math.toRadians(16), LENGTH, 7);
                stage.hit(stab, last ? damage * 1.5 : damage, victim -> {
                    victim.push(dir.clone().multiply(last ? 1.4 : 0.6).setY(last ? 0.6 : 0.25));
                    victim.effect(Affliction.SLOWNESS, 20, 0);
                    fx.impact(victim.chest(), Palette.GOLD, 1.2);
                });
                if (last) {
                    fx.draw(Shapes.circle(end, 3, 20, Shapes.planeAxes(dir)[0], Shapes.planeAxes(dir)[1], 0),
                            fx.dust(Palette.ICE, 1.6f).and(fx.particle(Particle.CLOUD)));
                    fx.flash(end, Palette.HOLY);
                }
            });
            if (!last) tween(t, stage, start + 4, start + CYCLE, Poses.THRUST, Poses.THRUST_COIL, Ease.IN_OUT);
        }
        recover(t, stage, WARN + 3 * CYCLE + 2, WARN + 3 * CYCLE + 18, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "lanceflurry";
    }
}
