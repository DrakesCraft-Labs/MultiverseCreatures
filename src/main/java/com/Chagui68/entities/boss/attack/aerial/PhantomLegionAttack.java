package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
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
 * Phantom Legion: from the air the Sentinel spreads its arms and four spectral copies of itself
 * rise out of the floor in a ring around its target, spears coiled. One after another each phantom
 * marks its line and lunges straight through the ring; the last two strike together. The lines
 * show where every lunge will go, so the ring can be danced through.
 */
public class PhantomLegionAttack extends ChoreographedAttack.Aerial {

    private static final int SUMMON = 24;
    private static final int AIM = 12;
    private static final int LUNGE = 6;
    private static final int[] STARTS = {0, 14, 28, 28};
    private static final double RING = 9;
    private static final double REACH = 18;

    public PhantomLegionAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.phantom-legion-damage", 11.0);
        Vector center = stage.onGround(target.position());
        double scale = stage.scale() * 0.6;
        Vector[] feet = new Vector[STARTS.length];
        float[] yaws = new float[STARTS.length];
        double offset = stage.random().nextDouble() * Math.PI;
        for (int i = 0; i < feet.length; i++) {
            feet[i] = stage.onGround(center.clone().add(Shapes.heading(offset + i * Math.PI / 2).multiply(RING)));
            yaws[i] = yawTowards(feet[i], center);
        }
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, 14, Poses.SPREAD.withHead(-15, 0, 0), Ease.OUT_BACK);
        t.at(0, () -> {
            fx.sound(stage.feet(), Sfx.ILLUSIONER_MIRROR, 3f, 0.5f);
            fx.sound(stage.feet(), Sfx.SOUL_ESCAPE, 3f, 0.5f);
        });
        // The phantoms climb out of the floor, drawn from the feet up.
        t.span(0, SUMMON, (tick, p) -> {
            for (int i = 0; i < feet.length; i++) {
                if (tick % 2 != i % 2) continue;
                Vector rise = feet[i].clone().subtract(new Vector(0, scale * 2 * (1 - Ease.at(Ease.OUT, p)), 0));
                phantom(stage, rise, yaws[i], Poses.GUARD, scale, 0.5);
                fx.ring(feet[i].clone().add(new Vector(0, 0.2, 0)), 1.5, 0.5, tick * 0.2, fx.dust(Palette.SPECTRAL, 1.2f));
                fx.cloud(Particle.SOUL, feet[i], 2, 0.8, 0.02);
            }
            if (tick % 3 == 0) {
                for (Vector f : feet) fx.line(stage.body().chest(), f.clone().add(new Vector(0, 2, 0)), 1.5, fx.dust(Palette.SOUL, 0.8f).sometimes(0.4));
            }
        });

        for (int i = 0; i < feet.length; i++) {
            int index = i;
            int aim = SUMMON + STARTS[i];
            int lunge = aim + AIM;
            Vector[] from = new Vector[1];
            Vector[] to = new Vector[1];
            Set<UUID> struck = new HashSet<>();
            // Idle, then coil and mark the line, re-aimed at the target's position while marking.
            t.span(SUMMON, lunge, (tick, p) -> {
                if (SUMMON + tick < aim) {
                    if (tick % 2 == index % 2) phantom(stage, feet[index], yaws[index], Poses.GUARD, scale, 0.5);
                    return;
                }
                double q = (SUMMON + tick - aim) / (double) AIM;
                Vector goal = stage.onGround(target.position());
                Vector dir = Shapes.flat(goal.clone().subtract(feet[index]));
                if (dir.lengthSquared() < 0.01) dir = Shapes.flat(center.clone().subtract(feet[index]));
                dir.normalize();
                from[0] = feet[index];
                to[0] = feet[index].clone().add(dir.clone().multiply(REACH));
                yaws[index] = yawTowards(feet[index], to[0]);
                phantom(stage, feet[index], yaws[index], Poses.GUARD.lerp(Poses.THRUST_COIL, Ease.at(Ease.OUT, q)), scale, 0.4);
                if (tick % 2 == 0) Telegraph.line(stage, from[0], to[0], 2.5, q);
            });
            t.at(aim, () -> fx.sound(feet[index], Sfx.PHANTOM_SWOOP, 2f, 0.6f));
            t.span(lunge, lunge + LUNGE, (tick, p) -> {
                if (from[0] == null) return;
                Vector before = from[0].clone().add(to[0].clone().subtract(from[0]).multiply(Ease.at(Ease.OUT, Math.max(0, p - 1.0 / LUNGE))));
                Vector at = from[0].clone().add(to[0].clone().subtract(from[0]).multiply(Ease.at(Ease.OUT, p)));
                phantom(stage, at, yaws[index], Poses.THRUST, scale, 0.35);
                fx.line(before.clone().add(new Vector(0, scale, 0)), at.clone().add(new Vector(0, scale, 0)), 0.4,
                        fx.dust(Palette.SPECTRAL, 1.8f).and(fx.particle(Particle.SOUL_FIRE_FLAME).sometimes(0.3)));
                for (Victim victim : stage.victimsIn(Area.segment(before, at, 1.8))) {
                    if (!struck.add(victim.id())) continue;
                    stage.damage(victim, damage);
                    victim.fling(to[0].clone().subtract(from[0]).normalize().multiply(0.9).setY(0.5));
                    victim.effect(Affliction.SLOWNESS, 30, 1);
                    fx.impact(victim.chest(), Palette.SPECTRAL, 1.5);
                }
            });
            t.at(lunge, () -> {
                fx.sound(from[0], Sfx.PLAYER_ATTACK_SWEEP, 2.5f, 1.4f);
                fx.sound(from[0], Sfx.TRIDENT_RIPTIDE, 2f, 1.2f);
            });
            // The phantom comes apart at the end of its lunge.
            t.at(lunge + LUNGE, () -> {
                if (to[0] == null) return;
                Vector chest = to[0].clone().add(new Vector(0, scale, 0));
                fx.burst(chest, Particle.SOUL, 25, 0.1);
                fx.flash(chest, Palette.SPECTRAL);
                fx.sound(chest, Sfx.SOUL_ESCAPE, 2f, 1.2f);
            });
        }
        tweenTo(t, stage, SUMMON + 4, SUMMON + 18, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    /** A phantom: the Sentinel's outline in ghost light, smaller and see-through. */
    private static void phantom(Stage stage, Vector feet, float yaw, Pose pose, double scale, double spacing) {
        Fx fx = stage.fx();
        com.Chagui68.entities.boss.fx.SentinelBody.Anatomy b =
                com.Chagui68.entities.boss.fx.SentinelBody.resolve(feet, yaw, scale, pose);
        Fx.Brush brush = fx.dust(Palette.SPECTRAL, 1.3f).and(fx.dust(Palette.SOUL, 0.8f).sometimes(0.3));
        Vector hips = feet.clone().add(new Vector(0, 0.75 * scale, 0));
        fx.line(hips, b.rightFoot(), spacing, brush);
        fx.line(hips, b.leftFoot(), spacing, brush);
        fx.line(hips, b.chest(), spacing, brush);
        fx.line(b.chest(), b.head(), spacing, brush);
        fx.line(b.rightShoulder(), b.leftShoulder(), spacing, brush);
        fx.line(b.rightShoulder(), b.rightHand(), spacing, brush);
        fx.line(b.leftShoulder(), b.leftHand(), spacing, brush);
        fx.line(b.rightHand(), b.spearTip(), spacing * 0.8, fx.dust(Palette.HOLY, 1.2f));
        fx.draw(Shapes.sphere(b.head(), 0.18 * scale, 10), brush);
    }

    /** The armor-stand yaw (degrees, Minecraft convention) that faces from {@code from} towards {@code to}. */
    private static float yawTowards(Vector from, Vector to) {
        Vector d = to.clone().subtract(from);
        return (float) Math.toDegrees(Math.atan2(-d.getX(), d.getZ()));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SUMMON + 18;
    }

    @Override
    public String getName() {
        return "phantomlegion";
    }
}
