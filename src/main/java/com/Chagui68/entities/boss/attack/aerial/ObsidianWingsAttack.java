package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Obsidian Wings: two great wings of obsidian shards unfold from the Sentinel's back. Each beat
 * flings a volley of shards down at the players; the last beat drives a gust into the floor below.
 */
public class ObsidianWingsAttack extends ChoreographedAttack.Aerial {

    private static final int UNFOLD = 20;
    private static final int BEAT = 16;
    private static final int BEATS = 3;
    private static final int FEATHERS = 6;

    public ObsidianWingsAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double shardDamage = stage.config("entities.armor-stand-boss.obsidian-wings-damage", 6.0);
        double slamDamage = stage.config("entities.armor-stand-boss.obsidian-wings-slam-damage", 12.0);
        List<Victim> victims = stage.victims();
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        List<Prop> feathers = new ArrayList<>();
        double[] flap = {0};

        tweenTo(t, stage, 0, UNFOLD, Poses.SPREAD, Ease.OUT);
        t.at(0, () -> {
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < FEATHERS; i++) {
                    Prop feather = stage.block(i % 3 == 0 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN,
                            stage.body().chest(), 0.8f, new org.joml.Quaternionf());
                    feather.resize(0.8f, 0.1f, new org.joml.Quaternionf(), 0);
                    feathers.add(feather);
                    props.add(feather);
                }
            }
            fx.sound(stage.feet(), Sfx.DRAGON_GROWL, 2.5f, 0.6f);
        });
        int end = UNFOLD + BEAT * BEATS;
        t.span(1, end + 10, (tick, p) -> {
            double open = Math.min(1, tick / (double) UNFOLD);
            flap[0] = tick < UNFOLD ? 0 : Math.sin((tick - UNFOLD) * 2 * Math.PI / BEAT);
            layWings(stage, feathers, open, flap[0]);
            if (tick % 3 == 0) fx.cloud(Particle.REVERSE_PORTAL, stage.body().chest(), 4, 2, 0.05);
        });

        for (int b = 0; b < BEATS; b++) {
            int beat = UNFOLD + b * BEAT + BEAT / 4;
            boolean last = b == BEATS - 1;
            t.at(beat, () -> {
                fx.sound(stage.feet(), Sfx.DRAGON_FLAP, 3f, 0.5f);
                Vector chest = stage.body().chest();
                for (int i = 0; i < 10; i++) {
                    Vector from = chest.clone().add(stage.body().right().multiply((i - 4.5) * 1.2)).add(new Vector(0, 2, 0));
                    Victim mark = victims.isEmpty() ? null : victims.get(i % victims.size());
                    Vector aim = mark == null ? stage.onGround(from.clone().add(stage.forward().multiply(10)))
                            : mark.position().add(new Vector((stage.random().nextDouble() - 0.5) * 4, 0, (stage.random().nextDouble() - 0.5) * 4));
                    Vector velocity = aim.subtract(from).normalize().multiply(1.6);
                    Missile shard = new Missile(from, velocity, 0.9)
                            .look((at, dir, age) -> {
                                fx.dust(Palette.VOID_DEEP, 1.6f).at(at);
                                fx.line(at, at.clone().subtract(dir.clone().multiply(1.5)), 0.5, fx.dust(Palette.AMETHYST, 0.9f));
                            })
                            .onHit(victim -> stage.damage(victim, shardDamage))
                            .onBurst(at -> fx.crumble(Material.OBSIDIAN, 6, 0.3).at(at));
                    fly(t, stage, beat + 1, 30, shard);
                }
                if (last) {
                    Vector ground = stage.onGround(stage.feet());
                    fx.flatBurst(ground, Particle.CLOUD, 60, 1.0);
                    fx.sound(ground, Sfx.WIND_CHARGE_BURST, 3f, 0.5f);
                    stage.hit(Area.cylinder(ground, 10, 1, 5), slamDamage, victim -> {
                        Vector away = victim.position().subtract(ground).setY(0);
                        if (away.lengthSquared() > 1e-6) away.normalize();
                        victim.fling(away.multiply(1.4).setY(-0.6));
                    });
                }
            });
        }
        t.at(end + 10, () -> {
            for (Prop feather : feathers) feather.resize(0.8f, 0.1f, new org.joml.Quaternionf(), 8);
            fx.burst(stage.body().chest(), Particle.REVERSE_PORTAL, 40, 0.8);
        });
        tweenTo(t, stage, end, end + 14, Poses.HOVER, Ease.IN_OUT);
        t.hold(end + 20);
        return t;
    }

    /**
     * Places the feathers: two fans growing out of the shoulder blades, opened by {@code open} (0..1)
     * and swept forward and back by {@code flap} (-1..1).
     */
    private static void layWings(Stage stage, List<Prop> feathers, double open, double flap) {
        Vector back = stage.body().chest().subtract(stage.forward().multiply(0.8)).add(new Vector(0, 1.5, 0));
        Vector right = stage.body().right();
        Vector behind = stage.forward().multiply(-1);
        for (int side = 0; side < 2; side++) {
            double sign = side == 0 ? 1 : -1;
            for (int i = 0; i < FEATHERS; i++) {
                Prop feather = feathers.get(side * FEATHERS + i);
                double fan = Math.toRadians(-10 + i * 18) * open;
                Vector out = right.clone().multiply(sign * Math.cos(fan)).add(new Vector(0, Math.sin(fan), 0))
                        .add(behind.clone().multiply(0.3 + flap * 0.5)).normalize();
                Vector root = back.clone().add(right.clone().multiply(sign * 0.6));
                feather.moveTo(root, 1);
                feather.resize(0.8f, (float) ((5 + i * 0.8) * open), Prop.pointing(out), 1);
            }
        }
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return UNFOLD + BEAT * BEATS + 14;
    }

    @Override
    public String getName() {
        return "obsidianwings";
    }
}
