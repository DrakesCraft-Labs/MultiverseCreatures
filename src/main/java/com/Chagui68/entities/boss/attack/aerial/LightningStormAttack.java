package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Bolts;
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

import java.util.List;

/**
 * Lightning Storm: a ring of storm cloud gathers over the Sentinel, then bolts come down on the
 * players, each one called by a crackling circle under their feet a moment before.
 */
public class LightningStormAttack extends ChoreographedAttack.Aerial {

    private static final int GATHER = 20;
    private static final int BOLTS = 10;
    private static final int GAP = 5;
    private static final int WARN = 14;
    private static final double RADIUS = 3;

    public LightningStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.6);
        Vector feet = stage.feet();
        Vector cloud = feet.clone().add(new Vector(0, 14, 0));
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, GATHER, Poses.CAST_SKY, Ease.OUT);
        t.at(0, () -> fx.sound(feet, Sfx.LIGHTNING_THUNDER, 2f, 0.5f));
        int end = GATHER + BOLTS * GAP + WARN;
        t.span(0, end, (tick, p) -> {
            double radius = 8 + Math.min(1, tick / (double) GATHER) * 16;
            fx.ring(cloud, radius, 1.6, tick * 0.05,
                    fx.particle(Particle.LARGE_SMOKE).and(fx.dust(Palette.ASH, 3.0f)).sometimes(0.7));
            fx.ring(cloud.clone().add(new Vector(0, 1.5, 0)), radius * 0.8, 2.0, -tick * 0.04, fx.dust(Palette.STORM, 2.0f).sometimes(0.4));
            if (tick % 7 == 0) Bolts.inCloud(fx, cloud, radius, stage.random());
        });

        for (int i = 0; i < BOLTS; i++) {
            int strike = GATHER + i * GAP + WARN;
            int index = i;
            Vector[] spot = new Vector[1];
            t.at(strike - WARN, () -> {
                Vector around = victims.isEmpty() ? stage.onGround(feet)
                        : victims.get(index % victims.size()).position();
                spot[0] = stage.onGround(around.add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(index < victims.size() ? 0 : 4)));
            });
            t.span(strike - WARN, strike, (tick, p) -> {
                if (spot[0] == null || tick % 2 != 0) return;
                Telegraph.circle(stage, spot[0], RADIUS, p);
                fx.cloud(Particle.ELECTRIC_SPARK, spot[0].clone().add(new Vector(0, 0.5, 0)), 4, RADIUS * 0.5, 0.05);
            });
            t.at(strike, () -> {
                stage.lightning(spot[0]);
                Bolts.strike(fx, spot[0], stage.random());
                fx.flash(spot[0].clone().add(new Vector(0, 1, 0)), Palette.ICE);
                fx.flatBurst(spot[0], Particle.ELECTRIC_SPARK, 24, 0.5);
                stage.hit(Area.cylinder(spot[0], RADIUS, 1, 5), damage, victim -> {
                    victim.effect(Affliction.SLOWNESS, 40, 1);
                    victim.push(new Vector(0, 0.5, 0));
                });
            });
        }
        tweenTo(t, stage, end - 6, end + 10, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    @Override
    public String getName() {
        return "lightningstorm";
    }
}
