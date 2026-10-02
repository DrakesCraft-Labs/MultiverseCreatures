package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Rain of Lances: from the air the Sentinel sweeps its spear across the field and a downpour of
 * burning lances follows, wave after wave, each lance aimed at a small marked circle. The waves
 * sweep from one side of the field to the other, so there is always somewhere to step.
 */
public class RainOfLancesAttack extends ChoreographedAttack.Aerial {

    private static final int RAISE = 14;
    private static final int WAVES = 5;
    private static final int PER_WAVE = 7;
    private static final int WAVE_GAP = 8;
    private static final int WARN = 14;
    private static final int FALL = 5;
    private static final double RADIUS = 2;
    private static final double FIELD = 22;

    public RainOfLancesAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.5);
        Vector center = stage.onGround(stage.feet());
        Vector sweep = stage.forward();
        Vector side = new Vector(-sweep.getZ(), 0, sweep.getX());
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, RAISE, Poses.SPEAR_RAISED, Ease.OUT);
        t.at(0, () -> fx.sound(center, Sfx.EVOKER_PREPARE_SUMMON, 2.5f, 0.8f));
        t.span(0, RAISE + WAVES * WAVE_GAP + WARN, (tick, p) ->
                fx.ring(center.clone().add(new Vector(0, 32, 0)), FIELD, 1.4, tick * 0.05,
                        fx.dust(Palette.EMBER, 2.2f).and(fx.particle(Particle.LARGE_SMOKE)).sometimes(0.5)));

        for (int w = 0; w < WAVES; w++) {
            int wave = w;
            int warnAt = RAISE + w * WAVE_GAP;
            // Each wave falls on a band of the field, the bands sweeping across it.
            double band = -FIELD + (2 * FIELD) * (w + 0.5) / WAVES;
            List<Vector> spots = new ArrayList<>();
            t.at(warnAt, () -> {
                for (int i = 0; i < PER_WAVE; i++) {
                    Vector spot;
                    if (i == 0 && !victims.isEmpty()) {
                        spot = victims.get(wave % victims.size()).position();
                    } else {
                        double along = (stage.random().nextDouble() - 0.5) * 2 * FIELD;
                        double across = band + (stage.random().nextDouble() - 0.5) * (2 * FIELD / WAVES);
                        spot = center.clone().add(side.clone().multiply(along)).add(sweep.clone().multiply(across));
                    }
                    spots.add(stage.onGround(spot));
                }
            });
            t.span(warnAt, warnAt + WARN, (tick, p) -> {
                if (tick % 2 != 0) return;
                for (Vector spot : spots) Telegraph.circle(stage, spot, RADIUS, p);
            });
            int fallAt = warnAt + WARN;
            for (int i = 0; i < PER_WAVE; i++) {
                int index = i;
                Prop[] lance = new Prop[1];
                t.at(fallAt - FALL + (i % 3), () -> {
                    Vector spot = spots.get(index);
                    Vector top = spot.clone().add(new Vector(0, 30, 0));
                    lance[0] = spear(stage, Material.NETHERITE_SPEAR, top, new Vector(0, -1, 0), 3.5f);
                    lance[0].glow(Palette.EMBER);
                    props.add(lance[0]);
                    lance[0].moveTo(spot.clone().add(new Vector(0, 1.5, 0)), FALL);
                    fx.trail(top, spot, Palette.EMBER, FALL + 2);
                });
                t.at(fallAt + (i % 3), () -> {
                    Vector spot = spots.get(index);
                    fx.impact(spot.clone().add(new Vector(0, 0.5, 0)), Palette.EMBER, 1.5);
                    fx.flatBurst(spot, Particle.FLAME, 10, 0.25);
                    if (index % 2 == 0) fx.sound(spot, Sfx.TRIDENT_THUNDER, 1f, 1.5f);
                    stage.hit(Area.cylinder(spot, RADIUS, 1, 3), damage, victim -> {
                        victim.ignite(40);
                        victim.push(new Vector(0, 0.4, 0));
                    });
                });
                t.at(fallAt + 24 + (i % 3), () -> {
                    if (lance[0] != null) lance[0].remove();
                });
            }
        }
        int end = RAISE + WAVES * WAVE_GAP + WARN;
        tweenTo(t, stage, end - 2, end + 12, Poses.HOVER, Ease.IN_OUT);
        t.hold(end + 28);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return RAISE + WAVES * WAVE_GAP + WARN + 12;
    }

    @Override
    public String getName() {
        return "rainoflances";
    }
}
