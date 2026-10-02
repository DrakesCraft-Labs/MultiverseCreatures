package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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
 * Dark Orb: three orbs of darkness are lobbed from the Sentinel's hands. Where each lands it spreads
 * into a pool of void on the floor that drags at the feet and eats away at whoever stands in it.
 */
public class DarkOrbAttack extends ChoreographedAttack.Aerial {

    private static final int FORM = 18;
    private static final int ORBS = 3;
    private static final int POOL = 80;
    private static final double POOL_RADIUS = 4.5;
    private static final int FLIGHT = 18;
    private static final double GRAVITY = 0.06;

    public DarkOrbAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        if (stage.victims().isEmpty()) return null;
        Fx fx = stage.fx();
        double impact = seal(stage, 0.6);
        double pool = seal(stage, 0.15);
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Vector> pools = new ArrayList<>();
        List<Integer> born = new ArrayList<>();

        tweenTo(t, stage, 0, FORM, Poses.CHANNEL, Ease.IN_OUT);
        t.span(0, FORM, (tick, p) -> {
            for (int i = 0; i < ORBS; i++) {
                Vector at = orbAt(stage, i, tick);
                fx.draw(Shapes.sphere(at, 0.3 + p * 0.9, 14), fx.dust(Palette.VOID_DEEP, 2.2f));
                fx.cloud(Particle.SQUID_INK, at, 1, 0.2, 0.01);
            }
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.EVOKER_CAST, 2f, 0.5f));

        int[] clock = {0};
        t.span(0, FORM + 50 + POOL, (tick, p) -> clock[0] = tick);
        for (int i = 0; i < ORBS; i++) {
            int index = i;
            t.at(FORM + i * 4, () -> {
                Victim mark = victims.get(index % victims.size());
                Vector from = orbAt(stage, index, FORM);
                Vector to = mark.position();
                // A lob that lands on the mark in FLIGHT ticks under GRAVITY.
                Vector velocity = to.clone().subtract(from).multiply(1.0 / FLIGHT).add(new Vector(0, GRAVITY * FLIGHT / 2, 0));
                Missile orb = new Missile(from, velocity, 1.4)
                        .gravity(GRAVITY)
                        .look((at, dir, age) -> {
                            fx.draw(Shapes.sphere(at, 1.1, 14), fx.dust(Palette.VOID_DEEP, 2.2f));
                            fx.dust(Palette.AMETHYST, 1.2f, 0.4, 2).at(at);
                        })
                        .onHit(victim -> stage.damage(victim, impact))
                        .onBurst(at -> {
                            Vector floor = stage.onGround(at);
                            pools.add(floor);
                            born.add(clock[0]);
                            fx.burst(at, Particle.SQUID_INK, 30, 0.4);
                            fx.flash(at, Palette.VOID);
                            fx.sound(at, Sfx.GLOW_HIT, 2f, 0.5f);
                            stage.hit(Area.cylinder(floor, 3, 1, 3), impact, null);
                        });
                fly(t, stage, FORM + index * 4 + 1, 50, orb);
                fx.sound(from, Sfx.SHULKER_SHOOT, 1.5f, 0.5f);
            });
        }
        t.span(FORM, FORM + 50 + POOL, (tick, p) -> {
            for (int i = 0; i < pools.size(); i++) {
                int age = clock[0] - born.get(i);
                if (age > POOL) continue;
                Vector center = pools.get(i);
                double radius = POOL_RADIUS * Math.min(1, age / 8.0) * (age > POOL - 10 ? (POOL - age) / 10.0 : 1);
                if (tick % 2 == 0) {
                    fx.disc(center.clone().add(new Vector(0, 0.1, 0)), radius, 0.9, fx.dust(Palette.VOID_DEEP, 1.8f));
                    fx.ring(center.clone().add(new Vector(0, 0.2, 0)), radius, 0.6, tick * 0.1, fx.dust(Palette.AMETHYST, 1.2f));
                }
                fx.cloud(Particle.REVERSE_PORTAL, center.clone().add(new Vector(0, 0.3, 0)), 2, radius * 0.5, 0.02);
                if (age % 10 == 0) {
                    stage.hit(Area.cylinder(center, radius, 1, 2), pool, victim -> victim.effect(Affliction.SLOWNESS, 25, 2));
                }
            }
        });
        tweenTo(t, stage, FORM + 12, FORM + 26, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    private static Vector orbAt(Stage stage, int i, int tick) {
        Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
        return Shapes.onCircle(hands, 2.5, tick * 0.15 + i * 2 * Math.PI / ORBS, Shapes.FLAT_U, Shapes.FLAT_V);
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return FORM + 26;
    }

    @Override
    public String getName() {
        return "darkorb";
    }
}
