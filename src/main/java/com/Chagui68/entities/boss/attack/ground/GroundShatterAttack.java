package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Ground Shatter: the spear is raised, glowing with magma, and driven into the floor. A fissure of
 * lava tears towards the target, throwing up rock and flame; it branches, and it keeps smouldering.
 */
public class GroundShatterAttack extends ChoreographedAttack.Ground {

    private static final int WIND = 20;
    private static final double LENGTH = 32;
    private static final double SPEED = 1.6;

    public GroundShatterAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        Vector start = stage.feet();
        Vector dir = Shapes.flat(aimAt(stage, stage.target(), 20).subtract(start));
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        Vector origin = start.clone().add(dir.clone().multiply(4));
        Vector end = origin.clone().add(dir.clone().multiply(LENGTH));
        double damage = seal(stage, 0.9);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WIND, Poses.SPEAR_OVERHEAD, Ease.IN_OUT);
        t.span(0, WIND, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, origin, end, 4, p);
            Vector tip = stage.body().spearTip();
            fx.draw(Shapes.sphere(tip, 0.6 + p, 10), fx.fade(Palette.MOLTEN, Palette.EMBER, 1.8f));
            fx.cloud(Particle.LAVA, tip, 1, 0.3, 0);
        });
        t.at(2, () -> fx.sound(start, Sfx.BLAZE_SHOOT, 2f, 0.5f));
        t.at(14, () -> fx.sound(start, Sfx.RESPAWN_ANCHOR_CHARGE, 2f, 0.6f));

        tween(t, stage, WIND, WIND + 4, Poses.SPEAR_OVERHEAD, Poses.SPEAR_SLAM, Ease.OUT_BACK);
        t.at(WIND + 3, () -> {
            Vector hit = spearGround(stage);
            fx.impact(hit.clone().add(new Vector(0, 0.8, 0)), Palette.MOLTEN, 3);
            fx.flatBurst(hit, Particle.FLAME, 30, 0.45);
            fx.sound(hit, Sfx.MACE_SMASH_GROUND, 3f, 0.6f);
            fx.sound(hit, Sfx.EXPLODE, 2f, 0.7f);
        });

        List<Vector> crack = new ArrayList<>();
        Set<UUID> struck = new HashSet<>();
        int travel = (int) Math.ceil(LENGTH / SPEED);
        t.span(WIND + 3, WIND + 3 + travel, (tick, p) -> {
            double along = tick * SPEED;
            double wobble = Math.sin(along * 0.7) * 1.2 + (stage.random().nextDouble() - 0.5) * 0.8;
            Vector head = stage.onGround(origin.clone().add(dir.clone().multiply(along)).add(side.clone().multiply(wobble)));
            Vector previous = crack.isEmpty() ? stage.onGround(origin) : crack.get(crack.size() - 1);
            crack.add(head);
            fx.line(previous, head, 0.35, fx.dust(Palette.EMBER, 1.8f).and(fx.particle(Particle.LAVA).sometimes(0.15)));
            fx.cloud(Particle.FLAME, head.clone().add(new Vector(0, 0.6, 0)), 6, 0.4, 0.05);
            fx.cloud(Particle.LARGE_SMOKE, head.clone().add(new Vector(0, 1.2, 0)), 3, 0.5, 0.03);
            fx.crumble(stage.groundMaterial(head), 8, 0.6).at(head);
            if (tick % 2 == 0) {
                Vector up = side.clone().multiply(stage.random().nextGaussian() * 0.12).setY(0.55 + stage.random().nextDouble() * 0.3);
                stage.debris(head.clone().add(new Vector(0, 0.3, 0)), up, stage.groundMaterial(head), 22);
            }
            if (tick % 4 == 3) {
                // A short branch splitting off the main crack.
                Vector branchDir = dir.clone().multiply(0.6).add(side.clone().multiply(stage.random().nextBoolean() ? 1 : -1)).normalize();
                Vector tip = head.clone().add(branchDir.multiply(3 + stage.random().nextDouble() * 3));
                fx.line(head, stage.onGround(tip), 0.4, fx.dust(Palette.MOLTEN, 1.3f));
            }
            if (tick % 3 == 0) fx.sound(head, Sfx.WITHER_BREAK_BLOCK, 1.2f, 0.6f + (float) p * 0.4f);
            for (Victim victim : stage.victimsIn(Area.cylinder(head, 2.6, 1, 3))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                victim.push(new Vector(0, 1.1, 0).add(side.clone().multiply(wobble > 0 ? 0.4 : -0.4)));
                victim.ignite(60);
                victim.effect(Affliction.SLOWNESS, 40, 1);
            }
        });
        // The fissure keeps smouldering after it has opened.
        t.every(WIND + 3 + travel, WIND + 3 + travel + 40, 4, tick -> {
            for (Vector point : crack) {
                if (stage.random().nextDouble() < 0.35) fx.cloud(Particle.SMALL_FLAME, point, 1, 0.3, 0.01);
                if (stage.random().nextDouble() < 0.08) fx.cloud(Particle.LAVA, point, 1, 0.2, 0);
            }
        });

        recover(t, stage, WIND + 12, WIND + 30, Poses.GUARD);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return WIND + 32;
    }

    @Override
    public String getName() {
        return "groundshatter";
    }
}
