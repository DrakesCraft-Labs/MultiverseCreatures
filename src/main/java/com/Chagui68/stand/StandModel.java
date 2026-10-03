package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A summoned Stand: a detailed figure of block displays floating behind its user's right
 * shoulder (see {@link StandDesign} for what each one looks like and {@link StandRig} for the
 * skeleton). It follows its user every tick, breathes, turns its head where its user looks and,
 * for a barrage, steps in front and throws punch after punch, leaving afterimages of its fists.
 *
 * <p>It materialises out of its aura when summoned, with the menacing ゴゴゴ rising round it.
 * Hermit Purple has no body: it is a double coil of thorned vines turning round its user's
 * right arm. Every piece is a non persistent display tagged {@link #TAG}, so nothing is left
 * behind by a crash.</p>
 */
public final class StandModel {

    /** Scoreboard tag carried by every piece of a Stand. */
    public static final String TAG = "MSC_StandPart";

    private static final float SCALE = 0.9f;
    /** Ticks the body takes to grow out of its aura. */
    private static final int MATERIALISE_TICKS = 7;
    /** Ticks between two idle frames; the displays interpolate in between. */
    private static final int IDLE_EVERY = 3;
    private static final int VINE_SEGMENTS = 14;

    private final org.bukkit.plugin.Plugin plugin;
    private final StandType type;
    private final List<StandDesign.Piece> pieces;
    private final List<BlockDisplay> parts = new ArrayList<>();
    private final List<BlockDisplay> vines = new ArrayList<>();
    private int age;
    private int barrageTicks;
    private int beat;

    public StandModel(org.bukkit.plugin.Plugin plugin, StandType type) {
        this.plugin = plugin;
        this.type = type;
        this.pieces = StandDesign.of(type);
    }

    public StandType type() {
        return type;
    }

    /** Builds the figure at its user's shoulder, growing out of the aura. */
    public void spawn(Player user) {
        World world = user.getWorld();
        if (!type.hasBody()) {
            Location at = vineAnchor(user);
            for (int i = 0; i < VINE_SEGMENTS * 2 + VINE_SEGMENTS / 2; i++) {
                Material material = i >= VINE_SEGMENTS * 2 ? Material.MAGENTA_TERRACOTTA
                        : (i % 2 == 0 ? Material.PURPLE_CONCRETE : Material.PURPLE_TERRACOTTA);
                vines.add(display(world, at, material, false, null));
            }
            twistVines(2);
            world.spawnParticle(Particle.DUST, at.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0,
                    new Particle.DustOptions(type.aura(), 1.2f));
            return;
        }
        Location at = anchor(user, false);
        Map<Part, Frame> frames = StandRig.solve(StandRig.idle(type, 0, user.getLocation().getPitch()), new Vector3f());
        for (StandDesign.Piece piece : pieces) {
            Transformation seed = StandRig.place(piece.center(), piece.size(), piece.spin(),
                    frames.get(piece.part()), SCALE * 0.05f);
            parts.add(display(world, at, piece.material(), piece.bright(), seed));
        }
        // Next tick, grow to full size so the client interpolates the whole way.
        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> apply(
                    StandRig.idle(type, age, user.getLocation().getPitch()), MATERIALISE_TICKS), 1L);
        }
        world.spawnParticle(Particle.DUST, at.clone().add(0, 1, 0), 60, 0.4, 0.9, 0.4, 0,
                new Particle.DustOptions(type.aura(), 1.5f));
        world.spawnParticle(Particle.END_ROD, at.clone().add(0, 1, 0), 12, 0.3, 0.7, 0.3, 0.02);
        menace(user, at);
    }

    private BlockDisplay display(World world, Location at, Material material, boolean bright,
                                 Transformation transformation) {
        return world.spawn(at, BlockDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.addScoreboardTag(TAG);
            entity.setBlock(material.createBlockData());
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setTeleportDuration(2);
            entity.setInterpolationDuration(2);
            entity.setShadowRadius(0f);
            entity.setViewRange(bright ? 1.2f : 1.0f);
            if (transformation != null) {
                entity.setTransformation(transformation);
            }
        });
    }

    /** The ゴゴゴ that rises round a Stand when it appears. */
    private void menace(Player user, Location at) {
        if (!plugin.isEnabled()) {
            return;
        }
        Vector right = rightOf(user);
        for (int i = 0; i < 4; i++) {
            double side = i % 2 == 0 ? 1 : -1;
            Location spot = at.clone().add(right.clone().multiply(side * (0.7 + i * 0.12)))
                    .add(0, 0.6 + i * 0.35, 0);
            TextDisplay text = at.getWorld().spawn(spot, TextDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.addScoreboardTag(TAG);
                entity.text(Component.text("ゴ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                entity.setShadowed(true);
                entity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(1.6f, 1.6f, 1.6f), new Quaternionf()));
            });
            long delay = 1L + i * 3L;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (text.isValid()) {
                    text.setInterpolationDelay(0);
                    text.setInterpolationDuration(26);
                    text.setTransformation(new Transformation(new Vector3f(0, 1.1f, 0), new Quaternionf(),
                            new Vector3f(2.0f, 2.0f, 2.0f), new Quaternionf()));
                }
            }, delay);
            plugin.getServer().getScheduler().runTaskLater(plugin, text::remove, delay + 28L);
        }
    }

    /** Moves the figure to its user; called every tick. */
    public void follow(Player user) {
        age++;
        if (!type.hasBody()) {
            Location at = vineAnchor(user);
            for (BlockDisplay vine : vines) {
                if (vine.isValid()) {
                    vine.teleport(at);
                }
            }
            if (age % 2 == 0) {
                twistVines(2);
            }
            if (age % 6 == 0) {
                user.getWorld().spawnParticle(Particle.DUST, at.clone().add(rightOf(user).multiply(0.37))
                        .add(0, 1.0, 0), 2, 0.15, 0.3, 0.15, 0, new Particle.DustOptions(type.aura(), 0.8f));
            }
            return;
        }
        boolean front = barrageTicks > 0;
        Location at = anchor(user, front);
        for (BlockDisplay part : parts) {
            if (part.isValid()) {
                part.teleport(at);
            }
        }
        if (age <= MATERIALISE_TICKS) {
            return;
        }
        if (front) {
            barrageTicks--;
            if (age % 2 == 0) {
                apply(StandRig.barrage(beat++), 2);
                afterimages(at);
            }
            if (barrageTicks == 0) {
                apply(StandRig.idle(type, age, user.getLocation().getPitch()), 4);
            }
        } else if (age % IDLE_EVERY == 0) {
            apply(StandRig.idle(type, age, user.getLocation().getPitch()), IDLE_EVERY);
        }
        if (age % 4 == 0) {
            Location aura = at.clone().add(0, 0.3, 0);
            user.getWorld().spawnParticle(Particle.DUST, aura, 3, 0.2, 0.25, 0.2, 0,
                    new Particle.DustOptions(type.aura(), 1.0f));
            if (type == StandType.MAGICIANS_RED) {
                user.getWorld().spawnParticle(Particle.FLAME, at.clone().add(0, 1.0, 0), 2, 0.35, 0.4, 0.35, 0.01);
            }
        }
    }

    /** Poses every piece, the displays sliding there over {@code ticks}. */
    private void apply(Map<Part, Quaternionf> pose, int ticks) {
        float lift = (float) Math.sin(age / 14.0) * 0.015f;
        Map<Part, Frame> frames = StandRig.solve(pose, new Vector3f(0, lift, 0));
        for (int i = 0; i < parts.size(); i++) {
            BlockDisplay part = parts.get(i);
            if (!part.isValid()) {
                continue;
            }
            StandDesign.Piece piece = pieces.get(i);
            part.setInterpolationDelay(0);
            part.setInterpolationDuration(ticks);
            part.setTransformation(StandRig.place(piece.center(), piece.size(), piece.spin(),
                    frames.get(piece.part()), SCALE));
        }
    }

    /** Ghost fists flickering round the real ones while the Stand hits. */
    private void afterimages(Location at) {
        if (!plugin.isEnabled()) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Material fist = StandDesign.fist(type);
        for (int i = 0; i < 3; i++) {
            Vector3f where = new Vector3f((float) random.nextDouble(-0.55, 0.55), (float) random.nextDouble(0.95, 1.55),
                    (float) random.nextDouble(0.55, 1.25)).mul(SCALE);
            float size = 0.2f * SCALE;
            Transformation pose = new Transformation(new Vector3f(where).sub(size / 2, size / 2, size / 2),
                    new Quaternionf(), new Vector3f(size, size, size * 1.1f), new Quaternionf());
            BlockDisplay ghost = display(at.getWorld(), at, fist, false, pose);
            plugin.getServer().getScheduler().runTaskLater(plugin, ghost::remove, 3L);
        }
    }

    /** Hermit Purple: two coils of thorned vine turning round the right arm. */
    private void twistVines(int ticks) {
        double phase = age * 0.22;
        for (int i = 0; i < vines.size(); i++) {
            BlockDisplay vine = vines.get(i);
            if (!vine.isValid()) {
                continue;
            }
            int segment;
            double strand;
            float size;
            double reach;
            if (i < VINE_SEGMENTS * 2) {
                segment = i % VINE_SEGMENTS;
                strand = i < VINE_SEGMENTS ? 0 : Math.PI;
                size = 0.075f;
                reach = 0.19;
            } else {
                // A thorn on every other segment of the first coil, pointing out.
                segment = (i - VINE_SEGMENTS * 2) * 2 + 1;
                strand = 0;
                size = 0.045f;
                reach = 0.27;
            }
            double t = segment / (double) (VINE_SEGMENTS - 1);
            double angle = t * Math.PI * 4 + phase + strand;
            float x = (float) (-0.37 + Math.cos(angle) * reach);
            float y = (float) (0.72 + t * 0.8);
            float z = (float) (Math.sin(angle) * reach);
            Quaternionf spin = new Quaternionf().rotationXYZ((float) angle, (float) angle * 0.5f, 0.6f);
            Vector3f corner = new Vector3f(size, size, size).mul(-0.5f).rotate(spin).add(x, y, z);
            vine.setInterpolationDelay(0);
            vine.setInterpolationDuration(ticks);
            vine.setTransformation(new Transformation(corner, spin, new Vector3f(size, size, size), new Quaternionf()));
        }
    }

    /** The Stand steps in front of its user and pumps its fists for that many ticks. */
    public void barrage(int ticks) {
        barrageTicks = Math.max(barrageTicks, ticks);
    }

    /** A shout floating where the Stand hits, such as "ORA ORA". */
    public void shout(Location at, Component text) {
        TextDisplay display = at.getWorld().spawn(at, TextDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.addScoreboardTag(TAG);
            entity.text(text);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            entity.setShadowed(true);
            entity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(),
                    new Vector3f(1.4f, 1.4f, 1.4f), new Quaternionf()));
        });
        plugin.getServer().getScheduler().runTaskLater(plugin, display::remove, 12L);
    }

    public void remove() {
        for (BlockDisplay part : parts) {
            if (part != null && part.isValid()) {
                part.remove();
            }
        }
        parts.clear();
        for (BlockDisplay vine : vines) {
            if (vine != null && vine.isValid()) {
                vine.remove();
            }
        }
        vines.clear();
    }

    public boolean alive() {
        List<BlockDisplay> all = type.hasBody() ? parts : vines;
        for (BlockDisplay part : all) {
            if (part == null || !part.isValid()) {
                return false;
            }
        }
        return !all.isEmpty();
    }

    /** Where the Stand floats: behind the right shoulder, or a step in front while it hits. */
    private Location anchor(Player user, boolean front) {
        Location eye = user.getLocation();
        Vector forward = new Vector(-Math.sin(Math.toRadians(eye.getYaw())), 0, Math.cos(Math.toRadians(eye.getYaw())));
        Vector right = rightOf(user);
        double bob = Math.sin(age / 8.0) * 0.06;
        Location at = front
                ? eye.clone().add(forward.clone().multiply(0.95)).add(0, 0.05 + bob, 0)
                : eye.clone().add(right.multiply(0.6)).add(forward.multiply(-0.55)).add(0, 0.25 + bob, 0);
        at.setYaw(eye.getYaw());
        at.setPitch(0);
        return at;
    }

    /** Hermit Purple lives on its user's arm: their feet, turned the way they face. */
    private static Location vineAnchor(Player user) {
        Location at = user.getLocation();
        at.setPitch(0);
        return at;
    }

    private static Vector rightOf(Player user) {
        double yaw = Math.toRadians(user.getLocation().getYaw());
        return new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
    }
}
