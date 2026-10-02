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
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Absorb Shield: a bubble of light swells out of the Sentinel's chest and holds around it, soaking up
 * damage. It shows its health: it fades from cold blue to an angry, cracking red as it is worn down,
 * and bursts when it breaks.
 */
public class AbsorbShieldAttack extends ChoreographedAttack {

    private static final int SWELL = 18;
    private static final int MAX = 400;
    private static final double RADIUS = 8;
    private static final double SHIELD = 100.0;

    public AbsorbShieldAttack(BossHost boss) {
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
            instance.activeDefense = DefenseState.ABSORB_SHIELD;
            instance.defenseTimer = 0;
            instance.absorbShieldHealth = SHIELD;
        }
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, SWELL, Poses.CHANNEL.withHead(10, 0, 0), Ease.IN_OUT);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.SHIELD_BLOCK, 2.5f, 1.4f));
        t.span(0, SWELL, (tick, p) -> {
            Vector chest = stage.body().chest();
            fx.draw(Shapes.sphere(chest, RADIUS * Ease.at(Ease.OUT_BACK, p), 80), fx.dust(Palette.FROST, 1.4f));
        });
        t.at(SWELL, () -> fx.sound(stage.feet(), Sfx.BEACON_ACTIVATE, 2f, 0.6f));
        recover(t, stage, SWELL, SWELL + 14, Poses.GUARD);

        t.span(SWELL, MAX, (tick, p) -> {
            boolean active = instance == null ? tick < 200 : instance.activeDefense == DefenseState.ABSORB_SHIELD;
            double left = instance == null ? 1 : Math.max(0, instance.absorbShieldHealth / SHIELD);
            Vector chest = stage.body().chest();
            if (!active) {
                fx.flash(chest, left <= 0 ? Palette.WARNING : Palette.FROST);
                fx.burst(chest, Particle.END_ROD, 70, 0.9);
                fx.sound(chest, left <= 0 ? Sfx.SHIELD_BREAK : Sfx.BEACON_DEACTIVATE, 2.5f, 0.8f);
                t.stop();
                return;
            }
            if (tick % 3 != 0) return;
            fx.draw(Shapes.sphere(chest, RADIUS, 90), fx.dust(Palette.mix(Palette.WARNING, Palette.FROST, left), 1.2f).sometimes(0.6 + 0.4 * left));
            // Cracks spread across the bubble as it weakens.
            int cracks = (int) ((1 - left) * 8);
            for (int i = 0; i < cracks; i++) {
                Vector a = Shapes.sphere(chest, RADIUS, 8).get(i);
                Vector b = a.clone().add(Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(4));
                Vector bOnShell = chest.clone().add(b.subtract(chest).normalize().multiply(RADIUS));
                fx.line(a, bOnShell, 0.4, fx.dust(Palette.HOLY, 1.0f));
            }
        });
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SWELL + 14;
    }

    @Override
    public String getName() {
        return "absorbshield";
    }
}
