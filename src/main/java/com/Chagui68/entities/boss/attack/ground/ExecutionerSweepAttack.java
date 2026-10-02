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
 * Executioner Sweep: a slow, deliberate wind-up with the spear dragged back and the blade burning
 * red, then one enormous horizontal cut across the front half-circle. The blade's tip leaves a
 * crescent of blood-red light in the air.
 */
public class ExecutionerSweepAttack extends ChoreographedAttack.Ground {

    private static final int WIND = 26;
    private static final int CUT = 5;
    private static final double REACH = 15;
    private static final double HALF = Math.toRadians(75);

    public ExecutionerSweepAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.executioner-sweep-damage", 18.0);
        Vector feet = stage.feet();
        Vector forward = stage.forward();
        double base = Math.atan2(forward.getZ(), forward.getX());
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WIND, Poses.SWING_BACK.withBody(0, 35, 0).withLeftLeg(-25, 0, 0), Ease.IN_BACK);
        t.span(0, WIND, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.cone(stage, feet, forward, HALF, REACH, p);
            Vector hand = stage.body().rightHand();
            Vector tip = stage.body().spearTip();
            fx.line(hand, tip, 0.5, fx.fade(Palette.BLOOD, Palette.EMBER, 1.3f + (float) p));
            if (tick % 3 == 0) fx.cloud(Particle.LARGE_SMOKE, tip, 2, 0.3, 0.01);
        });
        t.at(0, () -> fx.sound(feet, Sfx.ELDER_GUARDIAN_CURSE, 2f, 0.6f));
        t.at(WIND - 8, () -> fx.sound(feet, Sfx.BELL_RESONATE, 2.5f, 0.5f));

        tween(t, stage, WIND, WIND + CUT, Poses.SWING_BACK.withBody(0, 35, 0), Poses.SWING_THROUGH.withBody(0, -35, 0), Ease.OUT);
        t.span(WIND, WIND + CUT, (tick, p) -> {
            // The cut itself: a band of light swept behind the blade as it crosses.
            double angle = base + HALF - 2 * HALF * Ease.at(Ease.OUT, p);
            double previous = base + HALF - 2 * HALF * Ease.at(Ease.OUT, Math.max(0, p - 0.3));
            Vector center = feet.clone().add(new Vector(0, 3.2, 0));
            fx.crescent(center, REACH * 0.35, REACH, previous, angle, Shapes.FLAT_U, Shapes.FLAT_V,
                    fx.dust(Palette.HOLY, 2.2f), fx.fade(Palette.BLOOD, Palette.VOID_DEEP, 1.8f));
            fx.cloud(Particle.SWEEP_ATTACK, center.clone().add(Shapes.heading(angle).multiply(REACH * 0.7)), 2, 0.5, 0);
        });
        t.at(WIND + 2, () -> {
            fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 3f, 0.4f);
            fx.sound(feet, Sfx.WITHER_SHOOT, 2f, 0.5f);
            stage.hit(Area.cone(feet, forward, HALF, REACH, 8), damage, victim -> {
                Vector away = victim.position().subtract(feet).setY(0);
                if (away.lengthSquared() > 1e-6) away.normalize();
                victim.fling(away.multiply(1.6).setY(0.5));
                victim.effect(Affliction.WITHER, 80, 1);
                fx.impact(victim.chest(), Palette.BLOOD, 2);
            });
        });
        // The crescent lingers and fades for a moment after the cut.
        t.span(WIND + CUT, WIND + CUT + 12, (tick, p) -> {
            if (tick % 2 != 0) return;
            Vector center = feet.clone().add(new Vector(0, 3.2 - p, 0));
            fx.crescent(center, REACH * 0.85, REACH, base - HALF, base + HALF, Shapes.FLAT_U, Shapes.FLAT_V,
                    fx.fade(Palette.BLOOD, Palette.ASH, 1.4f).sometimes(1 - p), p2 -> { });
        });
        recover(t, stage, WIND + CUT + 8, WIND + CUT + 26, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "executionsweep";
    }
}
