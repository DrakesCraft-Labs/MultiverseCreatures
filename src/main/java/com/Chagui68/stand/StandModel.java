package com.Chagui68.stand;

import net.kyori.adventure.text.Component;
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

/**
 * A summoned Stand: a figure of block displays floating behind its user's right shoulder,
 * outlined in the colour of the Stand. It follows the user every tick and steps in front of
 * them for a barrage, its fists pumping while it hits.
 *
 * <p>Hermit Purple has no body: it is drawn as purple vines coiling round the user's arm.
 * Every piece is a non persistent display tagged {@link #TAG}, so nothing is left behind by a
 * crash.</p>
 */
public final class StandModel {

    /** Scoreboard tag carried by every piece of a Stand. */
    public static final String TAG = "MSC_StandPart";

    private static final float SCALE = 0.9f;

    /** One piece of the figure: its block, middle and size in the Stand's own frame. */
    private record Piece(Material material, Vector3f center, Vector3f size, boolean arm, int side) {
    }

    private final org.bukkit.plugin.Plugin plugin;
    private final StandType type;
    private final List<BlockDisplay> parts = new ArrayList<>();
    private final List<Piece> pieces = new ArrayList<>();
    private int age;
    private int barrageTicks;

    public StandModel(org.bukkit.plugin.Plugin plugin, StandType type) {
        this.plugin = plugin;
        this.type = type;
        if (type.hasBody()) {
            design();
        }
    }

    public StandType type() {
        return type;
    }

    private void design() {
        // Head, crest, eyes.
        pieces.add(new Piece(type.head(), new Vector3f(0, 1.55f, 0), new Vector3f(0.42f, 0.42f, 0.42f), false, 0));
        pieces.add(new Piece(type.accent(), new Vector3f(0, 1.8f, -0.02f), new Vector3f(0.46f, 0.1f, 0.46f), false, 0));
        pieces.add(new Piece(Material.WHITE_CONCRETE, new Vector3f(-0.09f, 1.58f, 0.21f),
                new Vector3f(0.08f, 0.05f, 0.02f), false, 0));
        pieces.add(new Piece(Material.WHITE_CONCRETE, new Vector3f(0.09f, 1.58f, 0.21f),
                new Vector3f(0.08f, 0.05f, 0.02f), false, 0));
        // Torso, belt and emblem.
        pieces.add(new Piece(type.body(), new Vector3f(0, 1.05f, 0), new Vector3f(0.52f, 0.62f, 0.26f), false, 0));
        pieces.add(new Piece(type.accent(), new Vector3f(0, 0.76f, 0), new Vector3f(0.54f, 0.08f, 0.28f), false, 0));
        pieces.add(new Piece(type.accent(), new Vector3f(0, 1.16f, 0.135f), new Vector3f(0.14f, 0.14f, 0.02f), false, 0));
        // Shoulders, arms and fists.
        for (int side : new int[]{-1, 1}) {
            pieces.add(new Piece(type.accent(), new Vector3f(side * 0.33f, 1.33f, 0),
                    new Vector3f(0.2f, 0.1f, 0.28f), false, 0));
            pieces.add(new Piece(type.arms(), new Vector3f(side * 0.36f, 1.04f, 0),
                    new Vector3f(0.16f, 0.56f, 0.16f), true, side));
            pieces.add(new Piece(type.head(), new Vector3f(side * 0.36f, 0.7f, 0),
                    new Vector3f(0.2f, 0.2f, 0.2f), true, side));
        }
        // The lower body fades into the aura.
        pieces.add(new Piece(type.body(), new Vector3f(0, 0.5f, 0), new Vector3f(0.4f, 0.4f, 0.22f), false, 0));
    }

    /** Builds the figure at the user's shoulder. */
    public void spawn(Player user) {
        Location at = anchor(user, false);
        World world = user.getWorld();
        for (Piece piece : pieces) {
            BlockDisplay display = world.spawn(at, BlockDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.addScoreboardTag(TAG);
                entity.setBlock(piece.material().createBlockData());
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setTeleportDuration(2);
                entity.setInterpolationDuration(2);
                entity.setGlowColorOverride(type.aura());
                entity.setGlowing(true);
                entity.setTransformation(pose(piece, 0));
            });
            parts.add(display);
        }
        world.spawnParticle(Particle.DUST, at.clone().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0,
                new Particle.DustOptions(type.aura(), 1.4f));
    }

    /** Moves the figure to its user; called every tick. */
    public void follow(Player user) {
        age++;
        boolean front = barrageTicks > 0;
        Location at = anchor(user, front);
        for (BlockDisplay part : parts) {
            if (part.isValid()) {
                part.teleport(at);
            }
        }
        if (front) {
            barrageTicks--;
            if (age % 2 == 0) {
                // Fists pumping: the arms swap places every other tick.
                for (int i = 0; i < parts.size(); i++) {
                    Piece piece = pieces.get(i);
                    if (piece.arm()) {
                        BlockDisplay part = parts.get(i);
                        part.setInterpolationDelay(0);
                        part.setTransformation(pose(piece, (age / 2 + (piece.side() > 0 ? 1 : 0)) % 2 == 0 ? 0.55f : 0f));
                    }
                }
            }
            if (barrageTicks == 0) {
                for (int i = 0; i < parts.size(); i++) {
                    parts.get(i).setTransformation(pose(pieces.get(i), 0));
                }
            }
        }
        if (age % 4 == 0) {
            Location aura = at.clone().add(0, type.hasBody() ? 0.9 : 1.1, 0);
            if (type.hasBody()) {
                user.getWorld().spawnParticle(Particle.DUST, aura, 3, 0.3, 0.5, 0.3, 0,
                        new Particle.DustOptions(type.aura(), 1.0f));
            } else {
                vines(user);
            }
        }
    }

    /** Hermit Purple: a coil of purple thorns round the user's right arm. */
    private void vines(Player user) {
        Location base = user.getLocation().add(0, 1.0, 0);
        Vector right = rightOf(user);
        for (int i = 0; i < 8; i++) {
            double t = age * 0.25 + i * 0.8;
            Vector offset = right.clone().multiply(0.45).add(new Vector(Math.cos(t) * 0.25, i * 0.1 - 0.35,
                    Math.sin(t) * 0.25));
            user.getWorld().spawnParticle(Particle.DUST, base.clone().add(offset), 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0x7A3FB8), 0.9f));
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
    }

    public boolean alive() {
        if (!type.hasBody()) {
            return true;
        }
        for (BlockDisplay part : parts) {
            if (part == null || !part.isValid()) {
                return false;
            }
        }
        return !parts.isEmpty();
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

    private static Vector rightOf(Player user) {
        double yaw = Math.toRadians(user.getLocation().getYaw());
        return new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
    }

    /** The pose of a piece, an arm pushed forward by {@code punch} blocks. */
    private static Transformation pose(Piece piece, float punch) {
        Vector3f size = new Vector3f(piece.size()).mul(SCALE);
        Vector3f center = new Vector3f(piece.center()).mul(SCALE);
        if (piece.arm()) {
            center.z += punch;
        }
        Vector3f corner = new Vector3f(center).sub(new Vector3f(size).mul(0.5f));
        return new Transformation(corner, new Quaternionf(), size, new Quaternionf());
    }
}
