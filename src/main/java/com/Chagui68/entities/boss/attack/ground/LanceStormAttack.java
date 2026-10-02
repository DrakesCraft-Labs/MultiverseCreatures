package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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
 * Lance Storm: the spear is raised to the sky and a crown of spectral lances forms over the Sentinel,
 * turning. Circles light up on the floor — under every player and around the boss — and the lances
 * come down one after another, each into its circle.
 */
public class LanceStormAttack extends ChoreographedAttack.Ground {

    private static final int RAISE = 18;
    private static final int WARN = 22;
    private static final int LANCES = 14;
    private static final double CROWN_HEIGHT = 20;
    private static final double HIT_RADIUS = 2.6;

    public LanceStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.6);
        Vector feet = stage.feet();
        Vector crownCenter = feet.clone().add(new Vector(0, CROWN_HEIGHT, 0));
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        // Where the lances land: one on each player, the rest scattered around the boss.
        List<Vector> spots = new ArrayList<>();
        for (Victim victim : stage.victims()) {
            if (spots.size() >= LANCES / 2) break;
            spots.add(stage.onGround(victim.position()));
        }
        while (spots.size() < LANCES) {
            double angle = stage.random().nextDouble() * Math.PI * 2;
            double radius = 6 + stage.random().nextDouble() * 12;
            spots.add(stage.onGround(feet.clone().add(Shapes.heading(angle).multiply(radius))));
        }

        tweenTo(t, stage, 0, RAISE, Poses.SPEAR_RAISED, Ease.OUT);
        t.at(0, () -> fx.sound(feet, Sfx.EVOKER_PREPARE_SUMMON, 2.5f, 0.6f));
        // The crown assembles lance by lance.
        Prop[] lances = new Prop[LANCES];
        for (int i = 0; i < LANCES; i++) {
            int index = i;
            t.at(2 + i, () -> {
                Vector at = crownPoint(crownCenter, index, 0);
                lances[index] = spear(stage, Material.NETHERITE_SPEAR, at, new Vector(0, -1, 0), 4.5f);
                lances[index].glow(Palette.AMETHYST);
                props.add(lances[index]);
                fx.draw(Shapes.sphere(at, 0.8, 10), fx.dust(Palette.SPECTRAL, 1.6f));
                fx.sound(at, Sfx.AMETHYST_CHIME, 1.5f, 0.6f + index * 0.08f);
            });
        }
        t.span(0, RAISE + WARN, (tick, p) -> {
            Vector tip = stage.body().spearTip();
            if (tick % 2 == 0) fx.line(tip, crownCenter, 0.7, fx.dust(Palette.AMETHYST, 1.3f).sometimes(0.6));
            for (int i = 0; i < LANCES; i++) {
                if (lances[i] != null) lances[i].moveTo(crownPoint(crownCenter, i, tick * 0.05), 2);
            }
            fx.draw(Shapes.ring(crownCenter, 7, 0.9, tick * 0.05), fx.dust(Palette.VOID, 1.2f).sometimes(0.5));
        });
        t.span(RAISE, RAISE + WARN, (tick, p) -> {
            if (tick % 2 != 0) return;
            for (Vector spot : spots) Telegraph.circle(stage, spot, HIT_RADIUS, p);
        });

        // The fall: each lance to its spot, a tick apart.
        int fall = RAISE + WARN;
        for (int i = 0; i < LANCES; i++) {
            int index = i;
            int launch = fall + i;
            t.at(launch, () -> {
                if (lances[index] == null) return;
                lances[index].moveTo(spots.get(index).clone().add(new Vector(0, 2.5, 0)), 3);
                fx.trail(crownPoint(crownCenter, index, 0), spots.get(index), Palette.AMETHYST, 4);
                fx.sound(spots.get(index), Sfx.TRIDENT_THROW, 1.5f, 0.6f);
            });
            t.at(launch + 3, () -> {
                Vector spot = spots.get(index);
                fx.impact(spot.clone().add(new Vector(0, 0.6, 0)), Palette.AMETHYST, 2);
                fx.draw(Shapes.ring(spot, HIT_RADIUS, 0.4, 0), fx.crumble(stage.groundMaterial(spot), 3, 0.2));
                fx.flatBurst(spot, Particle.END_ROD, 12, 0.3);
                fx.sound(spot, Sfx.TRIDENT_THUNDER, 1.2f, 1.2f);
                stage.hit(Area.cylinder(spot, HIT_RADIUS, 1, 3.5), damage, victim -> {
                    victim.push(new Vector(0, 0.5, 0));
                    victim.effect(Affliction.SLOWNESS, 30, 1);
                });
            });
            t.at(launch + 30, () -> {
                if (lances[index] != null) lances[index].remove();
            });
        }
        tweenTo(t, stage, fall, fall + 6, Poses.SPEAR_SLAM, Ease.OUT_BACK);
        recover(t, stage, fall + LANCES + 6, fall + LANCES + 22, Poses.GUARD);
        t.hold(fall + LANCES + 32);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return RAISE + WARN + LANCES + 22;
    }

    private static Vector crownPoint(Vector center, int index, double spin) {
        return Shapes.onCircle(center, 7, spin + 2 * Math.PI * index / LANCES, Shapes.FLAT_U, Shapes.FLAT_V);
    }

    @Override
    public String getName() {
        return "lancestorm";
    }
}
