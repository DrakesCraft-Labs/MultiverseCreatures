package com.Chagui68.entities.boss.attack.ranged;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow Volley: shards of darkness gather in a fan behind the Sentinel's hands, then fly one after
 * another, each bending a little towards its mark and leaving a smear of shadow behind it.
 */
public class ShadowVolleyAttack extends ChoreographedAttack.Ranged {

    private static final int GATHER = 16;
    private static final int BOLTS = 9;
    private static final double SPEED = 1.5;

    public ShadowVolleyAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        if (stage.victims().isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.35);
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Missile> missiles = new ArrayList<>();
        cleanupMissiles(t, missiles);

        tweenTo(t, stage, 0, GATHER, Poses.CAST_FORWARD, Ease.IN_OUT);
        t.span(0, GATHER, (tick, p) -> {
            for (int i = 0; i < BOLTS; i++) {
                Vector at = fanPoint(stage, i);
                fx.draw(Shapes.sphere(at, 0.3 + p * 0.4, 6), fx.dust(Palette.VOID_DEEP, 1.6f));
                if (tick % 3 == 0) fx.cloud(Particle.SQUID_INK, at, 1, 0.1, 0.01);
            }
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.EVOKER_CAST, 2f, 0.8f));

        for (int i = 0; i < BOLTS; i++) {
            int index = i;
            t.at(GATHER + i * 2, () -> {
                Victim mark = victims.get(index % victims.size());
                Vector from = fanPoint(stage, index);
                Vector velocity = mark.chest().subtract(from).normalize().multiply(SPEED)
                        .add(stage.body().right().multiply((index - BOLTS / 2) * 0.08));
                Missile missile = new Missile(from, velocity, 1.2)
                        .homing(mark, 0.06)
                        .look((at, dir, age) -> {
                            fx.dust(Palette.VOID_DEEP, 2.0f).at(at);
                            fx.line(at, at.clone().subtract(dir.clone().multiply(2.5)), 0.5, fx.fade(Palette.VOID, Palette.VOID_DEEP, 1.2f));
                            if (age % 2 == 0) fx.cloud(Particle.SMOKE, at, 1, 0.1, 0);
                        })
                        .onHit(victim -> {
                            stage.damage(victim, damage);
                            victim.effect(Affliction.DARKNESS, 50, 0);
                        })
                        .onBurst(at -> {
                            fx.burst(at, Particle.SQUID_INK, 10, 0.2);
                            fx.draw(Shapes.sphere(at, 0.9, 12), fx.dust(Palette.AMETHYST, 1.4f));
                            fx.sound(at, Sfx.WITHER_SHOOT, 0.8f, 1.6f);
                        });
                missiles.add(missile);
                fly(t, stage, GATHER + index * 2 + 1, 50, missile);
                fx.sound(from, Sfx.BREEZE_SHOOT, 1f, 1.4f);
            });
        }
        recover(t, stage, GATHER + BOLTS * 2, GATHER + BOLTS * 2 + 14, Poses.GUARD);
        t.hold(GATHER + BOLTS * 2 + 52);
        return t;
    }

    /** Where shard {@code i} waits: a fan spread behind and above the hands. */
    private static Vector fanPoint(Stage stage, int i) {
        Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
        double angle = Math.PI * (i + 0.5) / BOLTS;
        return hands.clone()
                .add(stage.body().right().multiply(Math.cos(angle) * 5))
                .add(new Vector(0, Math.sin(angle) * 4, 0))
                .subtract(stage.forward().multiply(1));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return GATHER + BOLTS * 2 + 14;
    }

    @Override
    public String getName() {
        return "shadowvolley";
    }
}
