package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
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
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Air Slam: how a flight ends badly for the players. The Sentinel rises a little, folds, and comes
 * down spear-first on the marked spot; the landing cracks the floor and sends two shockwaves out.
 * It lands for good: the boss is on the ground afterwards.
 */
public class AirSlamAttack extends ChoreographedAttack.Aerial {

    private static final int RISE = 16;
    private static final int DIVE = 7;
    private static final double INNER = 6;
    private static final double OUTER = 18;

    public AirSlamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 1.0);
        Vector start = stage.feet();
        Vector landing = stage.onGround(aimAt(stage, stage.target(), 8)).subtract(new Vector(0, 0.15, 0));
        Vector peak = start.clone().add(new Vector(0, 4, 0)).add(Shapes.flat(landing.clone().subtract(start)).multiply(-2));
        BossInstance instance = stage.instance();
        if (instance != null) {
            instance.aerialAttacksDone.clear();
            if (instance.flyTask != null) {
                instance.flyTask.cancel();
                instance.flyTask = null;
            }
        }
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, RISE, Poses.DIVE, Ease.IN_OUT);
        t.span(0, RISE, (tick, p) -> {
            stage.moveTo(start.clone().add(peak.clone().subtract(start).multiply(Ease.at(Ease.OUT, p))));
            if (tick % 2 == 0) {
                Telegraph.circle(stage, landing, INNER, p);
                Telegraph.ring(stage, landing, OUTER - 0.5, OUTER, p);
            }
            fx.cloud(Particle.FLAME, stage.body().chest(), 4, 1.5, 0.02);
        });
        t.at(0, () -> fx.sound(start, Sfx.DRAGON_GROWL, 2.5f, 0.4f));
        t.at(RISE - 4, () -> fx.sound(start, Sfx.PHANTOM_SWOOP, 3f, 0.4f));

        t.span(RISE, RISE + DIVE, (tick, p) -> {
            Vector before = stage.feet();
            Vector at = peak.clone().add(landing.clone().subtract(peak).multiply(Ease.at(Ease.IN, p)));
            stage.moveTo(at);
            fx.line(before.clone().add(new Vector(0, 6, 0)), at.clone().add(new Vector(0, 6, 0)), 0.6,
                    fx.fade(Palette.MOLTEN, Palette.EMBER, 2.4f).and(fx.particle(Particle.FLAME).sometimes(0.4)));
        });
        tween(t, stage, RISE + DIVE - 2, RISE + DIVE + 2, Poses.DIVE, Poses.SPEAR_SLAM, Ease.OUT_BACK);
        int impact = RISE + DIVE;
        t.at(impact, () -> {
            if (instance != null) {
                instance.isFlying = false;
                instance.flyingTimer = 0;
            }
            fx.impact(landing.clone().add(new Vector(0, 1, 0)), Palette.EMBER, 6);
            fx.flatBurst(landing, Particle.FLAME, 60, 0.9);
            fx.flatBurst(landing, Particle.LARGE_SMOKE, 40, 0.6);
            for (int i = 0; i < 10; i++) {
                Vector crack = landing.clone().add(Shapes.heading(i * Math.PI / 5 + 0.3).multiply(INNER + 3));
                fx.line(landing, stage.onGround(crack), 0.4, fx.dust(Palette.EMBER, 1.6f));
                stage.debris(landing.clone().add(new Vector(0, 0.5, 0)),
                        Shapes.heading(i * Math.PI / 5).multiply(0.35).setY(0.7), stage.groundMaterial(landing), 26);
            }
            fx.sound(landing, Sfx.EXPLODE, 3f, 0.4f);
            fx.sound(landing, Sfx.MACE_SMASH_GROUND, 3f, 0.5f);
            stage.hit(Area.cylinder(landing, INNER, 2, 8), damage, victim -> {
                victim.fling(new Vector(0, 1.3, 0));
                victim.effect(Affliction.SLOWNESS, 60, 2);
            });
        });
        shockwave(t, stage, impact + 1, 14, landing, OUTER * 0.7, Palette.EMBER, damage * 0.4, null);
        shockwave(t, stage, impact + 7, 16, landing, OUTER, Palette.ASH, damage * 0.3, null);
        recover(t, stage, impact + 12, impact + 30, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "airslam";
    }
}
