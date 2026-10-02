package com.Chagui68.entities.boss.attack.ground;

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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Cataclysm: the Sentinel's ultimate on the ground. It drives its spear into the earth and three
 * bands of the floor around it start to glow, with safe rings of untouched ground between them.
 * One after another, from the inside out, the bands burst open in walls of fire and jagged
 * obsidian. Reading the rings and standing in the gaps is the only way through.
 */
public class CataclysmAttack extends ChoreographedAttack.Ground {

    private static final int PLANT = 20;
    private static final int WARN = 34;
    private static final int GAP = 12;
    /** Inner and outer radius of each band that erupts; the rings between them are safe. */
    private static final double[][] BANDS = {{0, 7}, {11, 18}, {22, 29}};

    public CataclysmAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.cataclysm-damage", 20.0);
        Vector center = stage.onGround(stage.feet());
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        // Raise, then drive the spear into the ground.
        tweenTo(t, stage, 0, PLANT - 6, Poses.SPEAR_OVERHEAD.withLeftArm(-160, 0, -20).withHead(-20, 0, 0), Ease.OUT);
        tween(t, stage, PLANT - 6, PLANT, Poses.SPEAR_OVERHEAD, Poses.KNEEL.withRightArm(30, 0, 5), Ease.IN);
        t.at(0, () -> {
            fx.sound(center, Sfx.WARDEN_ROAR, 3f, 0.6f);
            fx.sound(center, Sfx.TRIAL_SPAWNER_OMINOUS, 2f, 0.5f);
        });
        gather(t, stage, 0, PLANT - 6, () -> stage.body().spearTip(), 6, Palette.EMBER);
        t.at(PLANT, () -> {
            Vector tip = spearGround(stage);
            fx.impact(tip.clone().add(new Vector(0, 0.5, 0)), Palette.MOLTEN, 3);
            fx.sound(tip, Sfx.MACE_SMASH_GROUND, 3f, 0.5f);
            fx.sound(tip, Sfx.ANVIL_LAND, 2f, 0.4f);
            // Cracks running out from the spear across all three bands.
            for (int i = 0; i < 10; i++) {
                Vector way = Shapes.heading(i * Math.PI / 5 + 0.2);
                fx.line(tip, stage.onGround(center.clone().add(way.multiply(BANDS[2][1]))).add(new Vector(0, 0.15, 0)), 0.5,
                        fx.dust(Palette.EMBER, 1.3f).and(fx.crumble(stage.groundMaterial(tip), 1, 0.1).sometimes(0.3)));
            }
        });

        // Warning: each band glows hotter until its turn; the safe rings stay dark.
        for (int b = 0; b < BANDS.length; b++) {
            int band = b;
            int erupt = PLANT + WARN + b * GAP;
            t.span(PLANT, erupt, (tick, p) -> {
                if (tick % 3 != band % 3) return;
                double inner = BANDS[band][0], outer = BANDS[band][1];
                Telegraph.ring(stage, center, inner, outer, p);
                if (tick % 6 == 0) {
                    double r = inner + stage.random().nextDouble() * (outer - inner);
                    Vector at = stage.onGround(center.clone().add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(r)));
                    fx.cloud(Particle.LAVA, at, 1, 0.3, 0);
                    fx.cloud(Particle.SMOKE, at, 3, 0.4, 0.03);
                }
            });
            t.at(erupt - 10, () -> fx.sound(center, Sfx.RESPAWN_ANCHOR_CHARGE, 2.5f, 0.5f + band * 0.2f));
            t.at(erupt, () -> eruptBand(t, stage, center, band, damage, props));
        }
        // The spear stays planted, the Sentinel kneeling over it, until the last band has gone up.
        t.span(PLANT, PLANT + WARN + 2 * GAP, (tick, p) -> {
            if (tick % 4 == 0) fx.cloud(Particle.FLAME, stage.body().spearTip(), 3, 0.4, 0.02);
        });
        recover(t, stage, PLANT + WARN + 2 * GAP + 6, PLANT + WARN + 2 * GAP + 26, Poses.GUARD);
        return t;
    }

    private static void eruptBand(Timeline t, Stage stage, Vector center, int band, double damage, List<Prop> props) {
        Fx fx = stage.fx();
        double inner = BANDS[band][0], outer = BANDS[band][1];
        double mid = (inner + outer) / 2;
        fx.sound(center, Sfx.EXPLODE, 3f, 0.5f + band * 0.15f);
        fx.sound(center, Sfx.BLAZE_SHOOT, 2.5f, 0.5f);
        // Walls of fire across the band.
        for (Vector p : Shapes.ring(center, mid, 2.2, band * 0.7)) {
            Vector floor = stage.onGround(p);
            fx.line(floor, floor.clone().add(new Vector(0, 4 + stage.random().nextDouble() * 4, 0)), 0.6,
                    fx.particle(Particle.FLAME, 2, 0.4, 0.03).and(fx.dust(Palette.MOLTEN, 2.2f).sometimes(0.5)));
            fx.cloud(Particle.LAVA, floor, 2, (outer - inner) * 0.3, 0);
        }
        for (Vector p : Shapes.ring(center, outer, 1.6, band)) fx.crumble(stage.groundMaterial(p), 3, 0.4).at(stage.onGround(p));
        // Jagged obsidian thrusting up through it.
        int spikes = 6 + band * 4;
        for (int i = 0; i < spikes; i++) {
            double angle = i * Math.PI * 2 / spikes + stage.random().nextDouble() * 0.3;
            double r = inner + 1 + stage.random().nextDouble() * (outer - inner - 2);
            Vector base = stage.onGround(center.clone().add(Shapes.heading(angle).multiply(Math.max(1.5, r))));
            pillar(t, stage, t.now() + 1 + i % 3, base, i % 3 == 0 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN,
                    0.9f + (float) stage.random().nextDouble() * 0.6f, 3 + stage.random().nextDouble() * 3, 3, 16, props);
        }
        stage.hit(Area.ring(center, inner, outer, 4), damage, victim -> {
            victim.fling(new Vector(0, 1.3, 0));
            victim.ignite(80);
        });
    }

    @Override
    public String getName() {
        return "cataclysm";
    }
}
