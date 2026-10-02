package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.BossInstance.DefenseState;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Reflect Barrier: the Sentinel spreads its arms and a faceted dome of prismatic light closes around
 * it, its facets shifting colour. Hits on it are thrown back at the attacker for as long as it holds.
 */
public class ReflectBarrierAttack extends ChoreographedAttack {

    private static final int RAISE = 20;
    private static final int MAX = 400;
    private static final double RADIUS = 9;
    private static final Color[] PRISM = {Palette.ICE, Palette.AMETHYST, Palette.GOLD, Palette.SOUL};

    public ReflectBarrierAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return instance.activeDefense == DefenseState.NONE && boss.isOnGround(instance.stand);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        BossInstance instance = stage.instance();
        if (instance != null) {
            instance.activeDefense = DefenseState.REFLECT_BARRIER;
            instance.defenseTimer = 0;
        }
        Vector center = stage.feet().add(new Vector(0, 6, 0));
        List<Vector> facets = Shapes.sphere(new Vector(), 1, 70);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, RAISE, Poses.SPREAD.withHead(-25, 0, 0), Ease.OUT);
        t.at(0, () -> {
            fx.sound(center, Sfx.BEACON_ACTIVATE, 2.5f, 1.2f);
            fx.sound(center, Sfx.ILLUSIONER_CAST, 2f, 1.5f);
        });
        t.span(0, RAISE, (tick, p) -> {
            // The dome closes from the floor upwards.
            double top = -1 + 2 * Ease.at(Ease.OUT, p);
            for (int i = 0; i < facets.size(); i++) {
                Vector f = facets.get(i);
                if (f.getY() > top) continue;
                fx.dust(PRISM[i % PRISM.length], 1.6f).at(center.clone().add(f.clone().multiply(RADIUS)));
            }
        });
        t.at(RAISE, () -> {
            fx.flash(center, Palette.ICE);
            fx.sound(center, Sfx.AMETHYST_CHIME, 2.5f, 0.8f);
        });
        recover(t, stage, RAISE, RAISE + 14, Poses.GUARD);

        t.span(RAISE, MAX, (tick, p) -> {
            boolean active = instance == null ? tick < 160 : instance.activeDefense == DefenseState.REFLECT_BARRIER;
            if (!active) {
                fx.burst(center, Particle.END_ROD, 60, 0.8);
                fx.sound(center, Sfx.GLASS_BREAK, 2.5f, 0.8f);
                t.stop();
                return;
            }
            if (tick % 3 != 0) return;
            Vector c = stage.feet().add(new Vector(0, 6, 0));
            for (int i = 0; i < facets.size(); i++) {
                Color color = PRISM[(i + tick / 6) % PRISM.length];
                fx.dust(color, 1.3f).at(c.clone().add(facets.get(i).clone().multiply(RADIUS)));
            }
            // Edges between neighbouring facets, so the dome reads as cut crystal.
            for (int i = 0; i < facets.size(); i += 7) {
                Vector a = c.clone().add(facets.get(i).clone().multiply(RADIUS));
                Vector b = c.clone().add(facets.get((i + 13) % facets.size()).clone().multiply(RADIUS));
                if (a.distance(b) < RADIUS * 0.6) fx.line(a, b, 0.7, fx.dust(Palette.SPECTRAL, 0.8f));
            }
            if (tick % 30 == 0) fx.sound(c, Sfx.AMETHYST_CHIME, 1f, 1.6f);
        });
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return RAISE + 14;
    }

    @Override
    public String getName() {
        return "reflectbarrier";
    }
}
