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
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shield Bash: the shield comes up glowing while wind gathers behind it, then the Sentinel charges
 * down the telegraphed lane, over the terrain, flattening whoever stands in it, and ends with a slam
 * of the shield. A wall stops the charge early, in a shower of sparks.
 */
public class ShieldBashAttack extends ChoreographedAttack.Ground {

    private static final int BRACE = 16;
    private static final int CHARGE = 12;
    private static final double STEP = 1.9;

    public ShieldBashAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.shield-bash-damage", 12.0);
        Vector start = stage.feet();
        Vector dir = stage.forward();
        Vector laneEnd = start.clone().add(dir.clone().multiply(STEP * CHARGE + 4));
        Set<UUID> struck = new HashSet<>();
        boolean[] stopped = {false};
        int[] chargeEnd = {BRACE + CHARGE};
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, BRACE, Poses.SHIELD_WALL, Ease.IN_OUT);
        t.span(0, BRACE, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, start, laneEnd, 7, p);
            Vector shield = stage.body().shieldFace();
            fx.draw(Shapes.circle(shield, 1.2 + p * 0.8, 14, stage.body().right(), Shapes.UP, tick * 0.3),
                    fx.dust(Palette.mix(Palette.STORM, Palette.ICE, p), 1.5f));
            Vector behind = stage.feet().subtract(dir.clone().multiply(3)).add(new Vector(0, 2, 0));
            fx.moving(Particle.CLOUD, behind.clone().add(Vector.getRandom().multiply(3)), dir.clone().multiply(0.4));
        });
        t.at(0, () -> fx.sound(start, Sfx.ZOMBIE_IRON_DOOR, 2f, 0.5f));
        t.at(10, () -> fx.sound(start, Sfx.RAVAGER_ROAR, 2.5f, 0.6f));

        t.span(BRACE, BRACE + CHARGE, (tick, p) -> {
            if (stopped[0]) return;
            Vector before = stage.feet();
            boolean moved = stage.walk(dir.clone().multiply(STEP));
            Vector after = stage.feet();
            Vector shield = stage.body().shieldFace();
            fx.line(before.clone().add(new Vector(0, 2, 0)), after.clone().add(new Vector(0, 2, 0)), 0.6,
                    fx.particle(Particle.GUST).sometimes(0.3).and(fx.dust(Palette.ICE, 1.4f, 1.2, 2)));
            fx.cloud(Particle.SWEEP_ATTACK, shield, 3, 1.2, 0);
            fx.crumble(stage.groundMaterial(after), 10, 1.2).at(stage.onGround(after));
            if (tick % 3 == 0) fx.sound(after, Sfx.IRON_GOLEM_ATTACK, 1.5f, 0.6f);
            Area swept = Area.segment(before, after.clone().add(dir.clone().multiply(2.5)), 3.6);
            for (Victim victim : stage.victimsIn(swept)) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                victim.fling(dir.clone().multiply(2.2).setY(0.7));
                victim.effect(Affliction.SLOWNESS, 60, 3);
                victim.effect(Affliction.WEAKNESS, 60, 1);
                fx.impact(victim.chest(), Palette.STORM, 2);
            }
            if (!moved) {
                stopped[0] = true;
                chargeEnd[0] = BRACE + tick;
                fx.burst(shield, Particle.ELECTRIC_SPARK, 40, 0.6);
                fx.sound(shield, Sfx.ANVIL_LAND, 2f, 0.6f);
            }
        });
        // The finishing slam happens where the charge ended.
        t.at(BRACE + CHARGE, () -> {
            fx.sound(stage.feet(), Sfx.MACE_SMASH_GROUND, 2.5f, 0.8f);
            fx.flash(stage.body().shieldFace(), Palette.ICE);
        });
        shockwaveAtEnd(t, stage, damage * 0.5);
        tweenTo(t, stage, BRACE + CHARGE, BRACE + CHARGE + 4, Poses.STOMP_DOWN, Ease.OUT_BACK);
        recover(t, stage, BRACE + CHARGE + 8, BRACE + CHARGE + 24, Poses.GUARD);
        return t;
    }

    /** A small ring from wherever the boss stands when the charge ends. */
    private static void shockwaveAtEnd(Timeline t, Stage stage, double damage) {
        Vector[] center = new Vector[1];
        t.at(BRACE + CHARGE, () -> center[0] = stage.feet());
        Set<UUID> struck = new HashSet<>();
        t.span(BRACE + CHARGE, BRACE + CHARGE + 10, (tick, p) -> {
            if (center[0] == null) return;
            double radius = 1 + p * 8;
            Fx fx = stage.fx();
            fx.draw(Shapes.ring(center[0], radius, 0.8, tick), fx.dust(Palette.STORM, 1.8f));
            for (Victim victim : stage.victimsIn(Area.ring(center[0], radius - 1.2, radius + 1.2, 1.5))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                victim.push(new Vector(0, 0.6, 0));
            }
        });
    }

    @Override
    public String getName() {
        return "shieldbash";
    }
}
