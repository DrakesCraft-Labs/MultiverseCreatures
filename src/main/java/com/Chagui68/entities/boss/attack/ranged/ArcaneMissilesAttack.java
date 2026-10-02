package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
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
 * Arcane Missiles: a ring of sparks spins up around the Sentinel's hands and spits a dozen small
 * homing missiles, each thrown out sideways before it curls round onto its mark.
 */
public class ArcaneMissilesAttack extends ChoreographedAttack.Ranged {

    private static final int SPIN = 14;
    private static final int MISSILES = 12;
    private static final double SPEED = 1.1;

    public ArcaneMissilesAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        if (stage.victims().isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.25);
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Missile> missiles = new ArrayList<>();
        cleanupMissiles(t, missiles);

        tweenTo(t, stage, 0, SPIN, Poses.CAST_FORWARD, Ease.IN_OUT);
        t.span(0, SPIN + MISSILES * 2, (tick, p) -> {
            Vector core = core(stage);
            Vector[] axes = Shapes.planeAxes(stage.forward());
            fx.draw(Shapes.circle(core, 2.2, 12, axes[0], axes[1], tick * 0.4), fx.dust(Palette.SPECTRAL, 1.4f));
            fx.draw(Shapes.circle(core, 1.4, 8, axes[0], axes[1], -tick * 0.6), fx.dust(Palette.AMETHYST, 1.2f));
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.ENCHANT, 2f, 1.4f));

        for (int i = 0; i < MISSILES; i++) {
            int index = i;
            t.at(SPIN + i * 2, () -> {
                Victim mark = victims.get(index % victims.size());
                Vector core = core(stage);
                Vector[] axes = Shapes.planeAxes(stage.forward());
                Vector out = Shapes.onCircle(new Vector(), 1, index * 2.4, axes[0], axes[1]);
                Vector from = core.clone().add(out.clone().multiply(2.2));
                Vector velocity = out.clone().multiply(0.8).add(stage.forward().multiply(0.6)).normalize().multiply(SPEED);
                Missile missile = new Missile(from, velocity, 1.0)
                        .homing(mark, 0.14)
                        .look((at, dir, age) -> {
                            fx.dust(Palette.SPECTRAL, 1.6f).at(at);
                            fx.line(at, at.clone().subtract(dir.clone().multiply(1.6)), 0.4, fx.dust(Palette.AMETHYST, 1.0f));
                            if (age % 3 == 0) fx.cloud(Particle.END_ROD, at, 1, 0.05, 0);
                        })
                        .onHit(victim -> stage.damage(victim, damage))
                        .onBurst(at -> {
                            fx.burst(at, Particle.END_ROD, 8, 0.2);
                            fx.draw(Shapes.sphere(at, 0.8, 10), fx.dust(Palette.SPECTRAL, 1.4f));
                            fx.sound(at, Sfx.AMETHYST_CHIME, 1f, 1.6f);
                        });
                missiles.add(missile);
                fly(t, stage, SPIN + index * 2 + 1, 60, missile);
                fx.sound(from, Sfx.SHULKER_SHOOT, 0.8f, 1.8f);
            });
        }
        int end = SPIN + MISSILES * 2;
        recover(t, stage, end, end + 14, Poses.GUARD);
        t.hold(end + 62);
        return t;
    }

    private static Vector core(Stage stage) {
        return stage.body().rightHand().midpoint(stage.body().leftHand()).add(stage.forward().multiply(2));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SPIN + MISSILES * 2 + 14;
    }

    @Override
    public String getName() {
        return "arcanemissiles";
    }
}
