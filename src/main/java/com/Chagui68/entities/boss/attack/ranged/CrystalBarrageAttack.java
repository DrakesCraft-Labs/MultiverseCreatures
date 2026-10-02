package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Crystal Barrage: amethyst crystals grow out of thin air around the Sentinel's raised hand and
 * circle it, then are flung at the players one by one, shattering into shards where they land.
 */
public class CrystalBarrageAttack extends ChoreographedAttack.Ranged {

    private static final int GROW = 20;
    private static final int CRYSTALS = 8;
    private static final int GAP = 3;
    private static final double SPEED = 1.9;
    private static final Pose CONJURE = Poses.GUARD.withLeftArm(-150, 0, -25).withHead(-15, 0, 0);

    public CrystalBarrageAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        if (stage.victims().isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.45);
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] crystals = new Prop[CRYSTALS];
        boolean[] launched = new boolean[CRYSTALS];

        tweenTo(t, stage, 0, GROW, CONJURE, Ease.IN_OUT);
        for (int i = 0; i < CRYSTALS; i++) {
            int index = i;
            t.at(2 + i * 2, () -> {
                Vector at = orbit(stage, index, 0);
                crystals[index] = stage.item(Material.AMETHYST_CLUSTER, at, 2.6f, new org.joml.Quaternionf());
                crystals[index].glow(Palette.AMETHYST);
                props.add(crystals[index]);
                fx.burst(at, Particle.END_ROD, 8, 0.15);
                fx.sound(at, Sfx.AMETHYST_CHIME, 1.5f, 0.8f + index * 0.1f);
            });
        }
        int end = GROW + CRYSTALS * GAP;
        t.span(0, end, (tick, p) -> {
            for (int i = 0; i < CRYSTALS; i++) {
                if (crystals[i] == null || launched[i]) continue;
                Vector at = orbit(stage, i, tick * 0.12);
                crystals[i].moveTo(at, 2);
                crystals[i].reshape(2.6f, new org.joml.Quaternionf().rotationY(tick * 0.2f + i), 2);
                if (tick % 3 == 0) fx.dust(Palette.SPECTRAL, 1.2f).at(at);
            }
        });

        for (int i = 0; i < CRYSTALS; i++) {
            int index = i;
            int launch = GROW + i * GAP;
            t.at(launch, () -> {
                if (crystals[index] == null) return;
                launched[index] = true;
                Victim mark = victims.get(index % victims.size());
                Vector from = orbit(stage, index, launch * 0.12);
                Vector velocity = mark.chest().subtract(from).normalize().multiply(SPEED);
                fx.sound(from, Sfx.SHULKER_SHOOT, 1.5f, 1.2f);
                Missile missile = new Missile(from, velocity, 1.3)
                        .homing(mark, 0.05)
                        .carrying(crystals[index])
                        .look((at, dir, age) -> fx.line(at, at.clone().subtract(dir.clone().multiply(2)), 0.4,
                                fx.fade(Palette.SPECTRAL, Palette.AMETHYST, 1.2f)))
                        .onHit(victim -> stage.damage(victim, damage))
                        .onBurst(at -> {
                            fx.burst(at, Particle.END_ROD, 16, 0.3);
                            fx.draw(Shapes.sphere(at, 1.2, 18), fx.dust(Palette.AMETHYST, 1.6f));
                            fx.sound(at, Sfx.AMETHYST_BREAK, 2f, 0.9f);
                        });
                fly(t, stage, launch + 1, 45, missile);
            });
        }
        recover(t, stage, end, end + 14, Poses.GUARD);
        t.hold(end + 46);
        return t;
    }

    /** Crystal {@code i}'s place on the ring around the raised hand. */
    private static Vector orbit(Stage stage, int i, double spin) {
        Vector hand = stage.body().leftHand().add(new Vector(0, 2, 0));
        return Shapes.onCircle(hand, 4, spin + 2 * Math.PI * i / CRYSTALS, Shapes.FLAT_U, Shapes.FLAT_V)
                .add(new Vector(0, Math.sin(spin * 2 + i) * 0.8, 0));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return GROW + CRYSTALS * GAP + 14;
    }

    @Override
    public String getName() {
        return "crystalbarrage";
    }
}
