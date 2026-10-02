package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Soul Tethers: soul-fire chains shoot from the Sentinel's chest into every player. Straining
 * against them hurts and drags you back; at the end they snap, and the snap hurts everyone still
 * bound. The chains sag and burn brighter the tighter they are pulled.
 */
public class SoulTetherAttack extends ChoreographedAttack.Ranged {

    private static final int CAST = 14;
    private static final int BOUND = 90;
    private static final double SLACK = 16;

    public SoulTetherAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        List<Victim> bound = stage.victims();
        if (bound.isEmpty()) return null;
        Fx fx = stage.fx();
        double strainDamage = stage.config("entities.armor-stand-boss.soul-tether-damage", 3.0);
        double snapDamage = stage.config("entities.armor-stand-boss.soul-tether-snap-damage", 8.0);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CAST, Poses.ROAR, Ease.OUT_BACK);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.SCULK_SHRIEK, 2.5f, 0.7f));
        t.span(0, CAST, (tick, p) -> {
            Vector chest = stage.body().chest();
            for (Victim victim : bound) {
                Vector reach = chest.clone().add(victim.chest().subtract(chest).multiply(p));
                drawChain(fx, chest, reach, 0, tick);
            }
            fx.cloud(Particle.SOUL, chest, 4, 1.5, 0.05);
        });
        t.at(CAST, () -> {
            for (Victim victim : bound) {
                fx.burst(victim.chest(), Particle.SOUL_FIRE_FLAME, 16, 0.2);
                victim.effect(Affliction.SLOWNESS, BOUND, 0);
            }
            fx.sound(stage.feet(), Sfx.CHAIN_BREAK, 2f, 0.5f);
        });

        t.span(CAST, CAST + BOUND, (tick, p) -> {
            Vector chest = stage.body().chest();
            for (Victim victim : bound) {
                double distance = victim.position().distance(stage.feet());
                double strain = Math.max(0, (distance - SLACK) / 8);
                drawChain(fx, chest, victim.chest(), strain, tick);
                if (strain > 0) {
                    Vector back = stage.feet().subtract(victim.position()).setY(0);
                    if (back.lengthSquared() > 1e-6) victim.push(back.normalize().multiply(0.12 * Math.min(1, strain)));
                    if (tick % 10 == 0) {
                        stage.damage(victim, strainDamage);
                        fx.burst(victim.chest(), Particle.SOUL, 6, 0.1);
                    }
                }
            }
            if (tick % 20 == 0) fx.sound(stage.feet(), Sfx.SOUL_ESCAPE, 1.5f, 0.6f);
        });

        int snap = CAST + BOUND;
        tween(t, stage, snap - 4, snap, Poses.ROAR, Poses.SPREAD, Ease.OUT_BACK);
        t.at(snap, () -> {
            Vector chest = stage.body().chest();
            for (Victim victim : bound) {
                for (Vector point : Shapes.line(chest, victim.chest(), 1.2)) {
                    fx.burst(point, Particle.SOUL_FIRE_FLAME, 2, 0.15);
                }
                stage.damage(victim, snapDamage);
                victim.effect(Affliction.WEAKNESS, 60, 0);
                fx.flash(victim.chest(), Palette.SOUL);
            }
            fx.sound(chest, Sfx.CHAIN_BREAK, 3f, 0.4f);
            fx.sound(chest, Sfx.EXPLODE, 1.5f, 1.4f);
        });
        recover(t, stage, CAST + 4, CAST + 20, Poses.GUARD);
        return t;
    }

    /** A soul-fire chain that sags when slack and pulls straight and bright when strained. */
    private static void drawChain(Fx fx, Vector from, Vector to, double strain, int tick) {
        double sag = Math.max(0, 2.5 - strain * 2.5);
        List<Vector> points = Shapes.line(from, to, 0.6);
        for (int i = 0; i < points.size(); i++) {
            double t = (double) i / Math.max(1, points.size() - 1);
            Vector point = points.get(i).clone().subtract(new Vector(0, Math.sin(Math.PI * t) * sag, 0));
            fx.dust(i % 2 == 0 ? Palette.SOUL : Palette.mix(Palette.SOUL, Palette.HOLY, Math.min(1, strain)), 1.2f + (float) Math.min(1, strain)).at(point);
            if ((i + tick) % 9 == 0) fx.cloud(Particle.SOUL_FIRE_FLAME, point, 1, 0.05, 0.01);
        }
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return CAST + 20;
    }

    @Override
    public String getName() {
        return "soultethers";
    }
}
