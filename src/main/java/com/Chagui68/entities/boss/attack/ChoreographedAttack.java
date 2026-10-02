package com.Chagui68.entities.boss.attack;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * An attack written as a choreography: a {@link Timeline} of poses, telegraphs, effects and hits,
 * played on a {@link Stage}.
 *
 * <p>Every attack follows the same arc — a telegraph that warns where, a wind-up that warns when, the
 * blow, and a recovery back to the guard — and owns the body for its whole length, so nothing else
 * starts until it is done. The helpers below are the moves several attacks share.
 */
public abstract class ChoreographedAttack extends BossAttackBase {

    protected ChoreographedAttack(BossHost boss) {
        super(boss);
    }

    /** Whether this attack can start now (on the ground, in the air, with a target...). */
    protected boolean ready(BossInstance instance) {
        return true;
    }

    /** The choreography, or null when there is nothing to do (no target, say). */
    public abstract Timeline choreograph(Stage stage);

    /** Ticks the body is locked for; the whole choreography unless an attack leaves something lingering. */
    public int lockTicks(Timeline timeline) {
        return timeline.length();
    }

    @Override
    public final void execute(BossInstance instance) {
        if (instance == null || !ready(instance)) return;
        LiveStage stage = new LiveStage(boss, instance);
        if (!stage.alive()) return;
        // The attack owns the heading from here: the AI stops turning the boss while it is busy.
        Victim target = stage.target();
        if (target != null) stage.face(target.position());
        Timeline timeline = choreograph(stage);
        if (timeline == null) return;
        stage.play(timeline, lockTicks(timeline));
    }

    // ------------------------------------------------------------------ shared moves

    /** Blends the body from {@code from} to {@code to} over {@code [start, end)}. */
    protected static void tween(Timeline timeline, Stage stage, int start, int end, Pose from, Pose to, Ease ease) {
        timeline.span(start, end, (tick, progress) -> stage.pose(from.lerp(to, Ease.at(ease, progress))));
    }

    /** Blends from whatever pose the body has when the span starts. */
    protected static void tweenTo(Timeline timeline, Stage stage, int start, int end, Pose to, Ease ease) {
        Pose[] from = new Pose[1];
        timeline.span(start, end, (tick, progress) -> {
            if (from[0] == null) from[0] = stage.pose();
            stage.pose(from[0].lerp(to, Ease.at(ease, progress)));
        });
    }

    /** The guard the body returns to after every attack, and a snap to it if the attack is cut short. */
    protected static void recover(Timeline timeline, Stage stage, int start, int end, Pose guard) {
        tweenTo(timeline, stage, start, end, guard, Ease.IN_OUT);
        timeline.onFinish(() -> {
            if (stage.alive()) stage.pose(guard);
        });
    }

    /** Removes props when the choreography ends, however it ends. */
    protected static void cleanup(Timeline timeline, List<Prop> props) {
        timeline.onFinish(() -> props.forEach(Prop::remove));
    }

    protected static void cleanupMissiles(Timeline timeline, List<Missile> missiles) {
        timeline.onFinish(() -> missiles.forEach(Missile::expire));
    }

    /** Flies a missile from {@code start} for at most {@code maxTicks}. */
    protected static void fly(Timeline timeline, Stage stage, int start, int maxTicks, Missile missile) {
        timeline.span(start, start + maxTicks, (tick, progress) -> {
            if (!missile.burst()) missile.step(stage);
        });
    }

    /**
     * An expanding shockwave along the ground: a ring of dust, crumbling floor and kicked-up debris
     * that hits each player once as it passes under them. Jumping over it is a way out.
     */
    protected static void shockwave(Timeline timeline, Stage stage, int start, int ticks, Vector center,
                                    double maxRadius, Color color, double damage, Consumer<Victim> then) {
        Set<UUID> struck = new HashSet<>();
        Fx fx = stage.fx();
        Material ground = stage.groundMaterial(center);
        timeline.span(start, start + ticks, (tick, progress) -> {
            double radius = Math.max(0.5, maxRadius * progress);
            double previous = Math.max(0, maxRadius * (tick - 1.0) / Math.max(1, ticks - 1));
            Fx.Brush brush = fx.dust(color, 2.0f).and(fx.crumble(ground, 2, 0.3).sometimes(0.6));
            for (Vector p : Shapes.ring(center, radius, 0.9, tick * 0.3)) {
                brush.at(stage.onGround(p).add(new Vector(0, 0.3, 0)));
            }
            if (tick % 2 == 0) {
                for (Vector p : Shapes.ring(center, radius, 3.5, tick)) {
                    fx.cloud(Particle.CLOUD, stage.onGround(p), 1, 0.2, 0.05);
                }
            }
            if (tick % 3 == 0 && radius > 2) {
                Vector kick = Shapes.ring(center, radius, radius, stage.random().nextDouble() * 6).get(0);
                Vector out = Shapes.flat(kick.clone().subtract(center)).multiply(0.15).setY(0.5);
                stage.debris(stage.onGround(kick), out, ground, 18);
            }
            for (Victim victim : stage.victimsIn(Area.ring(center, previous - 1.0, radius + 1.0, 1.4))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                if (then != null) then.accept(victim);
            }
        });
    }

    /** Energy drawn in towards a point: streaks converging and a tightening sphere of dust. */
    protected static void gather(Timeline timeline, Stage stage, int start, int end, java.util.function.Supplier<Vector> at,
                                 double radius, Color color) {
        Fx fx = stage.fx();
        timeline.span(start, end, (tick, progress) -> {
            Vector point = at.get();
            if (tick % 3 == 0) fx.gather(point, radius * (1.2 - progress * 0.5), 6, color, 10);
            fx.draw(Shapes.sphere(point, 0.3 + progress * radius * 0.25, 10), fx.dust(color, 1.6f));
            fx.cloud(Particle.END_ROD, point, 1, 0.2, 0.01);
        });
    }

    /** A list that removes its props when the choreography ends. */
    protected static List<Prop> props(Timeline timeline) {
        List<Prop> props = new ArrayList<>();
        cleanup(timeline, props);
        return props;
    }

    /** The shared seal damage from config.yml, times {@code multiplier}, for attacks with no key of their own. */
    protected static double seal(Stage stage, double multiplier) {
        return stage.config("entities.armor-stand-boss.seal-damage", 15.0) * multiplier;
    }

    /** The point straight below the boss's spear tip, on the floor: where a slam lands. */
    protected static Vector spearGround(Stage stage) {
        return stage.onGround(stage.body().spearTip());
    }

    /** Where to aim at a player, or straight ahead when there is none. */
    protected static Vector aimAt(Stage stage, Victim target, double ahead) {
        if (target != null) return target.position();
        return stage.feet().add(stage.forward().multiply(ahead));
    }

    /** A spear (or any held item) shown as a solid object, its point along {@code direction}. */
    protected static Prop spear(Stage stage, Material item, Vector at, Vector direction, float scale) {
        return stage.item(item, at, scale, diagonal(direction));
    }

    /**
     * The rotation that lays an item sprite's blade along {@code direction}: a held item's sprite runs
     * from its handle at the bottom-left to its point at the top-right.
     */
    protected static org.joml.Quaternionf diagonal(Vector direction) {
        Vector d = direction.clone().normalize();
        float k = (float) (1 / Math.sqrt(2));
        return new org.joml.Quaternionf().rotationTo(k, k, 0, (float) d.getX(), (float) d.getY(), (float) d.getZ());
    }

    /**
     * A column of {@code material} that bursts up out of the floor at {@code base} to {@code height}
     * over {@code rise} ticks, holds, then sinks back; with dust and crumbling floor as it breaks out.
     */
    protected static void pillar(Timeline t, Stage stage, int start, Vector base, Material material,
                                 float width, double height, int rise, int hold, List<Prop> props) {
        Fx fx = stage.fx();
        Prop[] prop = new Prop[1];
        org.joml.Quaternionf upright = new org.joml.Quaternionf();
        t.at(start, () -> {
            prop[0] = stage.block(material, base, width, upright);
            prop[0].resize(width, 0.05f, upright, 0);
            props.add(prop[0]);
            fx.draw(Shapes.ring(base, width * 0.7, 0.4, 0), fx.crumble(stage.groundMaterial(base), 4, 0.25));
            fx.flatBurst(base, Particle.CLOUD, 16, 0.25);
        });
        t.at(start + 1, () -> prop[0].resize(width, (float) height, upright, rise));
        t.at(start + rise + hold, () -> {
            prop[0].resize(width, 0.05f, upright, 12);
            fx.draw(Shapes.ring(base, width * 0.7, 0.5, 0), fx.crumble(material, 3, 0.3));
        });
        t.at(start + rise + hold + 13, () -> prop[0].remove());
    }

    /** The Sentinel's outline drawn in particles at another place: clones, after-images, arrivals. */
    protected static void silhouette(Stage stage, Vector feet, float yaw, com.Chagui68.entities.boss.fx.Pose pose,
                                     Fx.Brush brush, double spacing) {
        com.Chagui68.entities.boss.fx.SentinelBody.Anatomy b =
                com.Chagui68.entities.boss.fx.SentinelBody.resolve(feet, yaw, stage.scale(), pose);
        Fx fx = stage.fx();
        Vector hips = feet.clone().add(new Vector(0, 0.75 * stage.scale(), 0));
        Vector side = b.right().clone().multiply(0.12 * stage.scale());
        fx.line(hips.clone().add(side), b.rightFoot(), spacing, brush);
        fx.line(hips.clone().subtract(side), b.leftFoot(), spacing, brush);
        fx.line(hips, b.chest(), spacing, brush);
        fx.line(b.chest(), b.head(), spacing, brush);
        fx.line(b.rightShoulder(), b.leftShoulder(), spacing, brush);
        fx.line(b.rightShoulder(), b.rightHand(), spacing, brush);
        fx.line(b.leftShoulder(), b.leftHand(), spacing, brush);
        fx.line(b.rightHand(), b.spearTip(), spacing, brush);
        fx.draw(Shapes.sphere(b.head(), 0.5 * stage.scale() / 7.5 * 3, 12), brush);
    }

    // ------------------------------------------------------------------ categories

    /** An attack thrown standing on the ground. */
    public abstract static class Ground extends ChoreographedAttack {
        protected Ground(BossHost boss) {
            super(boss);
        }

        @Override
        protected boolean ready(BossInstance instance) {
            return !instance.isFlying && !instance.hoverBarrageActive;
        }
    }

    /** An attack thrown while flying. */
    public abstract static class Aerial extends ChoreographedAttack {
        protected Aerial(BossHost boss) {
            super(boss);
        }

        @Override
        protected boolean ready(BossInstance instance) {
            return instance.isFlying;
        }
    }

    /** An attack thrown from anywhere. */
    public abstract static class Ranged extends ChoreographedAttack {
        protected Ranged(BossHost boss) {
            super(boss);
        }
    }
}
