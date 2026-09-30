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
 *
 * <p>{@link #showWalk} replays a model's walk cycle instead: the same hitbox, plus the limb skeleton
 * of the model, posed frame by frame from the model's own maths. It needs no boss — the skeleton is
 * drawn around an anchor in front of the player — so a knee or an elbow can be judged without one.
 */
public final class MscGeometryOverlay {

    /** Points sampled along each of the twelve edges. */
    private static final int EDGE_SAMPLES = 8;

    /** Points sampled along each bone of a walk replay; bones are short, box edges are not. */
    private static final int BONE_SAMPLES = 6;

    /** How long each overlay lives, and how often it is redrawn, in ticks. */
    public static final int DEFAULT_TICKS = 200;
    private static final long REDRAW_PERIOD = 5L;

    /** A scale-1 armour stand is 0.5 wide and 1.975 tall, with its feet on its own location. */
    private static final double STAND_HALF_WIDTH = 0.25;
    private static final double STAND_HEIGHT = 1.975;

    /** How often the walk replay redraws its (fixed) hitbox: particle dust fades within a second. */
    private static final int BOX_REFRESH = 10;

    private static final Particle.DustOptions HITBOX = new Particle.DustOptions(Color.fromRGB(0xFF5555), 1.0f);
    private static final Particle.DustOptions JOINT = new Particle.DustOptions(Color.fromRGB(0x55FFFF), 1.2f);
    private static final Particle.DustOptions BONE = new Particle.DustOptions(Color.fromRGB(0x5588FF), 0.7f);
    private static final Particle.DustOptions TIP = new Particle.DustOptions(Color.fromRGB(0x66FF66), 1.0f);

    /** A model that can be posed at a walk phase: the limb skeleton the replay draws. */
    @FunctionalInterface
    public interface WalkModel {
        List<MscLimb.Limb> at(float phase);
    }

    /**
     * A walk replay: the model to pose, the hitbox scale its body has to fit while walking, and the
     * pace of its step — all taken from the model itself, so the preview steps like the boss does.
     */
    public record WalkRig(double hitboxScale, float radiansPerTick, WalkModel model) {
    }

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
     * The box a suit's hitbox would occupy around a preview anchor, built the way a scaled armour
     * stand's is: centred on the anchor and starting at its feet.
     */
    public static BoundingBox previewBox(Location anchor, double hitboxScale) {
        double half = STAND_HALF_WIDTH * hitboxScale;
        return new BoundingBox(anchor.getX() - half, anchor.getY(), anchor.getZ() - half,
                anchor.getX() + half, anchor.getY() + STAND_HEIGHT * hitboxScale, anchor.getZ() + half);
    }

    /**
     * The bones of a posed skeleton, in model space: one segment for a limb that does not fold and
     * two for one that does — the pivot to the joint and the joint to the tip.
     */
    public static List<Segment> limbSegments(List<MscLimb.Limb> limbs) {
        List<Segment> bones = new ArrayList<>(limbs.size() * 2);
        for (MscLimb.Limb limb : limbs) {
            if (limb.joint() == null) {
                bones.add(new Segment(vector(limb.pivot()), vector(limb.tip())));
            } else {
                bones.add(new Segment(vector(limb.pivot()), vector(limb.joint())));
                bones.add(new Segment(vector(limb.joint()), vector(limb.tip())));
            }
        }
        return bones;
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
        drawBox(world, stand.getBoundingBox());

        Location root = stand.getLocation();
        for (Vector3f joint : joints) {
            dot(world, root, joint, JOINT);
        }
    }

    /**
     * Replays a model's walk cycle around an anchor: the hitbox of the stand the suit is hit through,
     * the joint dots and the limb bones, all posed from the model's own maths at its own pace.
     *
     * <p>Nothing is spawned and no boss is involved: the anchor is where the developer wants the
     * skeleton drawn, so a walk can be watched on its own instead of provoked out of a boss.
     */
    public static void showWalk(Plugin plugin, Location anchor, WalkRig rig, int ticks) {
        Location preview = anchor.clone();
        new BukkitRunnable() {
            private int elapsed;
            private float phase;

            @Override
            public void run() {
                if (elapsed >= ticks) {
                    cancel();
                    return;
                }
                drawWalk(preview, rig, phase, elapsed % BOX_REFRESH == 0);
                elapsed++;
                phase += rig.radiansPerTick();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void drawWalk(Location anchor, WalkRig rig, float phase, boolean refreshBox) {
        World world = anchor.getWorld();
        if (world == null) return;

        if (refreshBox) {
            drawBox(world, previewBox(anchor, rig.hitboxScale()));
        }

        List<MscLimb.Limb> limbs = rig.model().at(phase);
        for (Segment bone : limbSegments(limbs)) {
            drawBone(world, anchor, bone, BONE, BONE_SAMPLES);
        }
        for (MscLimb.Limb limb : limbs) {
            dot(world, anchor, limb.pivot(), JOINT);
            if (limb.joint() != null) dot(world, anchor, limb.joint(), JOINT);
            dot(world, anchor, limb.tip(), TIP);
        }
    }

    private static void drawBox(World world, BoundingBox box) {
        for (Segment edge : boxEdges(box)) {
            drawBone(world, edge, HITBOX, EDGE_SAMPLES);
        }
    }

    /** Samples a bone or a box edge, already in world coordinates. */
    private static void drawBone(World world, Segment segment, Particle.DustOptions options, int samples) {
        Vector step = segment.to().clone().subtract(segment.from()).multiply(1.0 / samples);
        for (int i = 0; i <= samples; i++) {
            Vector point = segment.from().clone().add(step.clone().multiply(i));
            world.spawnParticle(Particle.DUST, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0, options);
        }
    }

    /** Samples a model-space bone through the same frame the display pieces are placed in. */
    private static void drawBone(World world, Location anchor, Segment bone, Particle.DustOptions options, int samples) {
        drawBone(world, new Segment(offsetFor(anchor, modelPoint(bone.from())),
                offsetFor(anchor, modelPoint(bone.to()))), options, samples);
    }

    private static void dot(World world, Location anchor, Vector3f modelPoint, Particle.DustOptions options) {
        Location loc = anchor.clone().add(offsetFor(anchor, modelPoint));
        world.spawnParticle(Particle.DUST, loc, 6, 0.05, 0.05, 0.05, 0, options);
    }

    private static Vector vector(Vector3f point) {
        return new Vector(point.x, point.y, point.z);
    }

    private static Vector3f modelPoint(Vector point) {
        return new Vector3f((float) point.getX(), (float) point.getY(), (float) point.getZ());
    }
}
