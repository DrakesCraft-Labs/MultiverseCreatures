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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Sundering Charge: the Sentinel drops its spear point to the floor and charges, the tip ripping a
 * glowing fissure through the ground behind it. At the end of the run it heaves the spear up in a
 * rising cut that throws a fan of obsidian blades out of the earth, and a moment later the whole
 * fissure it left behind erupts in a line of fire.
 */
public class SunderingChargeAttack extends ChoreographedAttack.Ground {

    private static final int BRACE = 20;
    private static final int CHARGE = 14;
    private static final double STEP = 1.7;
    private static final int ERUPT_DELAY = 14;

    public SunderingChargeAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.sundering-charge-damage", 14.0);
        Vector start = stage.feet();
        Vector dir = stage.forward();
        Vector laneEnd = start.clone().add(dir.clone().multiply(STEP * CHARGE + 6));
        List<Vector> fissure = new ArrayList<>();
        Set<UUID> struck = new HashSet<>();
        boolean[] stopped = {false};
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        // Brace: spear point down to the floor, weight back, the lane glowing ahead.
        tweenTo(t, stage, 0, BRACE, Poses.THRUST_COIL.withRightArm(-20, 25, 15).withBody(12, 0, 0)
                .withRightLeg(-25, 0, 0).withLeftLeg(20, 0, 0), Ease.IN_OUT);
        t.span(0, BRACE, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, start, laneEnd, 4, p);
            Vector tip = stage.onGround(stage.body().spearTip()).add(new Vector(0, 0.2, 0));
            fx.cloud(Particle.LAVA, tip, 1, 0.2, 0);
            fx.dust(Palette.MOLTEN, 1.4f, 0.3, 2).at(tip);
            if (tick % 4 == 0) fx.crumble(stage.groundMaterial(tip), 6, 0.4).at(tip);
        });
        t.at(0, () -> fx.sound(start, Sfx.RAVAGER_ROAR, 2.5f, 0.7f));
        t.at(BRACE - 6, () -> fx.sound(start, Sfx.WARDEN_SONIC_CHARGE, 2f, 1.4f));

        // The charge: the tip drags a molten fissure along the floor.
        tweenTo(t, stage, BRACE, BRACE + 4, Poses.THRUST.withRightArm(-10, 15, 10).withBody(18, 0, 0)
                .withRightLeg(30, 0, 0).withLeftLeg(-30, 0, 0), Ease.OUT);
        t.span(BRACE, BRACE + CHARGE, (tick, p) -> {
            if (stopped[0]) return;
            // Legs scissor while running.
            double stride = Math.sin(tick * 1.2) * 35;
            stage.pose(stage.pose().withRightLeg(stride, 0, 0).withLeftLeg(-stride, 0, 0));
            Vector before = stage.feet();
            boolean moved = stage.walk(dir.clone().multiply(STEP));
            Vector after = stage.feet();
            Vector tip = stage.onGround(stage.body().spearTip());
            fissure.add(tip.clone());
            fx.line(stage.onGround(before).add(new Vector(0, 0.15, 0)), tip.clone().add(new Vector(0, 0.15, 0)), 0.4,
                    fx.dust(Palette.MOLTEN, 1.6f).and(fx.particle(Particle.LAVA).sometimes(0.15)));
            fx.crumble(stage.groundMaterial(tip), 12, 0.6).at(tip.clone().add(new Vector(0, 0.3, 0)));
            fx.cloud(Particle.FLAME, tip, 4, 0.3, 0.05);
            stage.debris(tip, dir.clone().multiply(-0.1).add(new Vector(stage.random().nextGaussian() * 0.15, 0.45, 0)),
                    stage.groundMaterial(tip), 16);
            if (tick % 3 == 0) fx.sound(after, Sfx.WITHER_BREAK_BLOCK, 1.2f, 0.7f);
            Area swept = Area.segment(before, after.clone().add(dir.clone().multiply(2.5)), 3.2);
            for (Victim victim : stage.victimsIn(swept)) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                Vector side = Shapes.flat(victim.position().subtract(after)).normalize();
                victim.fling(side.multiply(1.2).add(dir.clone().multiply(0.8)).setY(0.9));
                victim.ignite(60);
                fx.impact(victim.chest(), Palette.EMBER, 2);
            }
            if (!moved) stopped[0] = true;
        });

        // The rising cut and the fan of blades.
        int cut = BRACE + CHARGE;
        tween(t, stage, cut, cut + 3, Poses.THRUST, Poses.SWING_BACK.withRightArm(-160, 20, 20).withBody(-8, 0, 0), Ease.OUT_BACK);
        t.at(cut, () -> {
            Vector origin = stage.feet().add(stage.forward().multiply(3));
            fx.sound(origin, Sfx.PLAYER_ATTACK_SWEEP, 3f, 0.5f);
            fx.sound(origin, Sfx.MACE_SMASH_GROUND, 2.5f, 0.7f);
            fx.draw(Shapes.arc(stage.body().chest(), 6, -1.2, 1.2, 40, stage.forward(), Shapes.UP), fx.dust(Palette.EMBER, 2.2f));
            for (int i = -2; i <= 2; i++) {
                Vector way = rotate(stage.forward(), i * 0.3);
                for (int k = 1; k <= 3; k++) {
                    Vector base = stage.onGround(origin.clone().add(way.clone().multiply(k * 3.0)));
                    pillar(t, stage, cut + k * 2, base, k % 2 == 0 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN,
                            1.1f, 2.5 + k, 3, 10, props);
                }
            }
        });
        t.span(cut + 2, cut + 10, (tick, p) -> {
            Vector origin = stage.feet().add(stage.forward().multiply(3));
            double reach = 3 + p * 9;
            for (Victim victim : stage.victimsIn(Area.cone(stage.feet(), stage.forward(), 0.7, reach, 4))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage * 0.8);
                victim.fling(new Vector(0, 1.1, 0).add(stage.forward().multiply(0.6)));
            }
            if (tick % 2 == 0) fx.cloud(Particle.LAVA, origin, 3, reach * 0.5, 0);
        });

        // The fissure left behind erupts.
        int erupt = cut + ERUPT_DELAY;
        t.span(cut, erupt, (tick, p) -> {
            if (tick % 2 != 0) return;
            for (Vector point : fissure) {
                fx.dust(Palette.mix(Palette.EMBER, Palette.WARNING_HOT, p), 1.8f, 0.3, 1).at(point.clone().add(new Vector(0, 0.2, 0)));
            }
        });
        Set<UUID> burned = new HashSet<>();
        t.at(erupt, () -> {
            for (int i = 0; i < fissure.size(); i++) {
                Vector point = fissure.get(i);
                fx.line(point, point.clone().add(new Vector(0, 5 + stage.random().nextDouble() * 3, 0)), 0.5,
                        fx.particle(Particle.FLAME, 2, 0.2, 0.04).and(fx.dust(Palette.MOLTEN, 2f).sometimes(0.6)));
                fx.cloud(Particle.LAVA, point, 4, 0.5, 0);
                if (i % 3 == 0) fx.burst(point.clone().add(new Vector(0, 1, 0)), Particle.EXPLOSION, 1, 0);
            }
            fx.sound(stage.feet(), Sfx.EXPLODE, 2.5f, 0.6f);
            fx.sound(stage.feet(), Sfx.BLAZE_SHOOT, 2f, 0.5f);
            if (fissure.size() < 2) return;
            Area lane = Area.segment(fissure.get(0), fissure.get(fissure.size() - 1), 2.2);
            for (Victim victim : stage.victimsIn(lane)) {
                if (!burned.add(victim.id())) continue;
                stage.damage(victim, damage * 0.6);
                victim.push(new Vector(0, 0.9, 0));
                victim.ignite(80);
                victim.effect(Affliction.SLOWNESS, 40, 1);
            }
        });
        t.span(erupt, erupt + 20, (tick, p) -> {
            if (tick % 3 != 0) return;
            for (Vector point : fissure) fx.cloud(Particle.LARGE_SMOKE, point.clone().add(new Vector(0, 0.5, 0)), 1, 0.3, 0.03);
        });
        recover(t, stage, cut + 8, cut + 24, Poses.GUARD);
        return t;
    }

    private static Vector rotate(Vector dir, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vector(dir.getX() * cos - dir.getZ() * sin, 0, dir.getX() * sin + dir.getZ() * cos).normalize();
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return BRACE + CHARGE + 24;
    }

    @Override
    public String getName() {
        return "sunderingcharge";
    }
}
