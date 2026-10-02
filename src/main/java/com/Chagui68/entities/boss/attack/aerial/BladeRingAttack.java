package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Blade Ring: spectral lances materialise in a ring around the Sentinel's waist, points out, and
 * wheel around it faster and faster; then they break off one by one and fly at the players.
 */
public class BladeRingAttack extends ChoreographedAttack.Aerial {

    private static final int FORM = 24;
    private static final int BLADES = 8;
    private static final int GAP = 4;
    private static final double RADIUS = 6;
    private static final double SPEED = 2.0;

    public BladeRingAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        List<Victim> victims = stage.victims();
        if (victims.isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.blade-ring-damage", 8.0);
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] blades = new Prop[BLADES];
        boolean[] gone = new boolean[BLADES];
        double[] spin = {0};

        tweenTo(t, stage, 0, FORM, Poses.SPREAD, Ease.IN_OUT);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.ILLUSIONER_MIRROR, 2f, 1.2f));
        for (int i = 0; i < BLADES; i++) {
            int index = i;
            t.at(1 + i * 2, () -> {
                Vector at = ringPoint(stage, index, 0);
                Vector out = at.clone().subtract(stage.body().chest()).setY(0);
                blades[index] = spear(stage, Material.NETHERITE_SPEAR, at, out, 3.5f);
                blades[index].glow(Palette.AMETHYST);
                props.add(blades[index]);
                fx.burst(at, Particle.END_ROD, 6, 0.15);
                fx.sound(at, Sfx.AMETHYST_CHIME, 1f, 1f + index * 0.08f);
            });
        }
        int end = FORM + BLADES * GAP;
        t.span(0, end, (tick, p) -> {
            spin[0] += 0.05 + Math.min(1, tick / (double) FORM) * 0.15;
            for (int i = 0; i < BLADES; i++) {
                if (blades[i] == null || gone[i]) continue;
                Vector at = ringPoint(stage, i, spin[0]);
                Vector out = at.clone().subtract(stage.body().chest()).setY(0);
                blades[i].moveTo(at, 1);
                blades[i].reshape(3.5f, diagonal(out), 1);
                fx.dust(Palette.SPECTRAL, 1.2f).at(at);
            }
            fx.ring(stage.body().chest(), RADIUS, 0.8, -spin[0], fx.dust(Palette.VOID, 1.0f).sometimes(0.4));
        });
        for (int i = 0; i < BLADES; i++) {
            int index = i;
            int launch = FORM + i * GAP;
            t.at(launch, () -> {
                if (blades[index] == null) return;
                gone[index] = true;
                Victim mark = victims.get(index % victims.size());
                Vector from = ringPoint(stage, index, spin[0]);
                Vector velocity = mark.chest().subtract(from).normalize().multiply(SPEED);
                blades[index].reshape(3.5f, diagonal(velocity), 1);
                fx.sound(from, Sfx.TRIDENT_THROW, 1.5f, 1.2f);
                Missile missile = new Missile(from, velocity, 1.4)
                        .homing(mark, 0.03)
                        .carrying(blades[index])
                        .look((at, dir, age) -> fx.line(at, at.clone().subtract(dir.clone().multiply(3)), 0.5, fx.fade(Palette.SPECTRAL, Palette.VOID, 1.2f)))
                        .onHit(victim -> stage.damage(victim, damage))
                        .onBurst(at -> {
                            fx.burst(at, Particle.END_ROD, 12, 0.3);
                            fx.sound(at, Sfx.TRIDENT_THUNDER, 1f, 1.6f);
                        });
                fly(t, stage, launch + 1, 40, missile);
            });
        }
        tweenTo(t, stage, end, end + 14, Poses.HOVER, Ease.IN_OUT);
        t.hold(end + 42);
        return t;
    }

    private static Vector ringPoint(Stage stage, int i, double spin) {
        return Shapes.onCircle(stage.body().chest(), RADIUS, spin + 2 * Math.PI * i / BLADES, Shapes.FLAT_U, Shapes.FLAT_V);
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return FORM + BLADES * GAP + 14;
    }

    @Override
    public String getName() {
        return "bladering";
    }
}
