package com.Chagui68.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a boss's geometry in the world: the box that actually takes the hits, and the joints its
 * limbs swing around.
 *
 * WHY IT EXISTS
 *
 * Every model audit so far needed a throwaway test that printed the real numbers and then had to be
 * deleted again — the pieces only exist at runtime, and a wrong number (a hitbox that ends below the
 * head, a joint on the wrong side) is invisible until somebody swings at thin air. With this, a
 * developer stands next to the boss, runs {@code /msc debug geometry <boss>} and sees it: every piece
 * has to sit inside the red box, and every limb has to hang from one of the cyan dots.
 *
 * The maths is separated from the drawing on purpose, because that is the part worth testing: the box
 * is drawn along its twelve edges, and a joint is placed by reusing the same {@code yaw + 180} frame
 * the display pieces are positioned in.
 */
public final class MscGeometryOverlay {

    /** Points sampled along each of the twelve edges. */
    private static final int EDGE_SAMPLES = 8;

    /** How long each overlay lives, and how often it is redrawn, in ticks. */
    public static final int DEFAULT_TICKS = 200;
    private static final long REDRAW_PERIOD = 5L;

    private static final Particle.DustOptions HITBOX = new Particle.DustOptions(Color.fromRGB(0xFF5555), 1.0f);
    private static final Particle.DustOptions JOINT = new Particle.DustOptions(Color.fromRGB(0x55FFFF), 1.2f);

    private MscGeometryOverlay() {
    }

    /** One edge of the box. */
    public record Segment(Vector from, Vector to) {
    }

    /** The twelve edges of {@code box}, in world coordinates. */
    public static List<Segment> boxEdges(BoundingBox box) {
        double[] xs = {box.getMinX(), box.getMaxX()};
        double[] ys = {box.getMinY(), box.getMaxY()};
        double[] zs = {box.getMinZ(), box.getMaxZ()};

        List<Segment> edges = new ArrayList<>(12);
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
                edges.add(new Segment(new Vector(xs[0], ys[j], zs[k]), new Vector(xs[1], ys[j], zs[k])));
                edges.add(new Segment(new Vector(xs[j], ys[0], zs[k]), new Vector(xs[j], ys[1], zs[k])));
                edges.add(new Segment(new Vector(xs[j], ys[k], zs[0]), new Vector(xs[j], ys[k], zs[1])));
            }
        }
        return edges;
    }

    /**
     * Where a point of the model ends up in the world, for a stand at {@code standLocation}.
     *
     * <p>The same frame the pieces are positioned in: the model is turned by the stand's yaw plus
     * half a turn, which is what makes it face the way the stand faces.
     */
    public static Vector offsetFor(Location standLocation, Vector3f modelPoint) {
        double yaw = Math.toRadians(standLocation.getYaw() + 180);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        return new Vector(
                modelPoint.x * cos - modelPoint.z * sin,
                modelPoint.y,
                modelPoint.x * sin + modelPoint.z * cos);
    }

    /** Draws the hitbox and the joints for a while, following the boss as it moves. */
    public static void show(Plugin plugin, ArmorStand stand, List<Vector3f> joints, int ticks) {
        new BukkitRunnable() {
            private int elapsed;

            @Override
            public void run() {
                if (elapsed >= ticks || stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                elapsed += REDRAW_PERIOD;
                draw(stand, joints);
            }
        }.runTaskTimer(plugin, 0L, REDRAW_PERIOD);
    }

    private static void draw(ArmorStand stand, List<Vector3f> joints) {
        World world = stand.getWorld();
        for (Segment edge : boxEdges(stand.getBoundingBox())) {
            Vector step = edge.to().clone().subtract(edge.from()).multiply(1.0 / EDGE_SAMPLES);
            for (int i = 0; i <= EDGE_SAMPLES; i++) {
                Vector point = edge.from().clone().add(step.clone().multiply(i));
                world.spawnParticle(Particle.DUST, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0, HITBOX);
            }
        }

        Location root = stand.getLocation();
        for (Vector3f joint : joints) {
            Location loc = root.clone().add(offsetFor(root, joint));
            world.spawnParticle(Particle.DUST, loc, 6, 0.05, 0.05, 0.05, 0, JOINT);
        }
    }
}
