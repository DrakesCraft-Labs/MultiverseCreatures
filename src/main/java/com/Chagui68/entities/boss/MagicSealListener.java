package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.seal.SealGeometry;
import com.Chagui68.entities.boss.seal.SealPlane;
import com.Chagui68.entities.boss.seal.SealPoint;
import com.Chagui68.entities.boss.seal.WingGeometry;
import com.Chagui68.entities.boss.seal.WingPoint;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Paints the magic seals: every round, triangle and wing the bosses throw on the ground.
 *
 * <p>This class is the brush, not the drawing. Where the particles go is {@link SealGeometry} and
 * {@link WingGeometry} — pure, tested rules that know nothing about a server — and this file only
 * walks their points and spawns the particles, with one {@link #repeat} helper shared by every
 * seal. A seal added here should be a short frame body: pick a shape, pick a color, return true
 * until the stand dies.
 */
public class MagicSealListener {

    /** Height a flat seal is drawn at, so its runes do not z-fight the floor it lies on. */
    private static final double FLAT_LIFT = 0.06;

    /** Height a drawn chord is lifted at; lines cross above the rings they belong to. */
    private static final double LINE_LIFT = 0.04;

    private final MultiverseCreatures plugin;
    private final Random random = new Random();

    private final Color goldColor = Color.fromRGB(0xFFAA00);
    private final Color cyanColor = Color.fromRGB(0x88CCFF);
    private final Color flameColor = Color.fromRGB(0xFF6600);

    public MagicSealListener(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ pentagrams

    public void spawnPentagramSeal(ArmorStand stand, int durationTicks) {
        spawnPentagramSeal(stand, durationTicks, SealPlane.XZ);
    }

    public void spawnPentagramSeal(ArmorStand stand, int durationTicks, SealPlane plane) {
        spawnPentagramSeal(stand.getLocation(), durationTicks, plane, stand);
    }

    public void spawnPentagramSeal(Location center, int durationTicks, SealPlane plane) {
        spawnPentagramSeal(center, durationTicks, plane, null);
    }

    private void spawnPentagramSeal(Location center, int durationTicks, SealPlane plane, ArmorStand stand) {
        Color red = Color.fromRGB(0xFF1A1A);
        Color redBright = Color.fromRGB(0xFF3333);
        repeat(5, durationTicks, (ticks, frame) -> {
            if (stand != null && !stand.isValid()) return false;
            drawSegments(center, plane, SealGeometry.pentagram(6.0, 60, Math.PI / 2), red, 1.6f);
            drawPoints(center, plane, SealGeometry.circle(7.4, 220, 0), redBright, 1.8f);
            drawPoints(center, plane, SealGeometry.circle(7.4, 110, 0), red, 1.8f);
            flameAura(center, plane, 2.5, 28);
            return true;
        });
    }

    public void spawnLargePentagramSeal(Location center, int durationTicks, double radius, SealPlane plane) {
        Color red = Color.fromRGB(0xFF1A1A);
        Color redBright = Color.fromRGB(0xFF3333);
        int perEdge = SealGeometry.pentagramSamples(radius);
        int ring = SealGeometry.ringSamples(radius);
        double ringRadius = SealGeometry.enclosingRingRadius(radius);
        repeat(5, durationTicks, (ticks, frame) -> {
            drawSegments(center, plane, SealGeometry.pentagram(radius, perEdge, Math.PI / 2), red, 1.6f);
            drawPoints(center, plane, SealGeometry.circle(ringRadius, ring, 0), redBright, 1.8f);
            drawPoints(center, plane, SealGeometry.circle(ringRadius, ring / 2, 0), red, 1.8f);
            flameAura(center, plane, SealGeometry.auraRadius(radius), SealGeometry.auraCount(radius));
            return true;
        });
    }

    // ------------------------------------------------------------------ shield seal

    public BukkitRunnable spawnFloatingShieldSealTask(Location shieldLoc, double groundY, int durationTicks) {
        World world = shieldLoc.getWorld();
        if (world == null) return null;
        double radius = 3.0;
        double height = SealGeometry.cylinderHeight(shieldLoc.getY(), groundY);
        List<SealPoint> base = SealGeometry.circle(radius, 50, 0);
        List<SealPoint> wall = SealGeometry.circle(radius, 40, 0);
        List<SealPoint> inner = SealGeometry.circle(radius * 0.6, 8, 0);
        return repeat(3, durationTicks, (ticks, frame) -> {
            double phase = (ticks * 0.25) % height;
            for (SealPoint point : base) {
                double[] at = SealPlane.XZ.toWorld(shieldLoc.getX(), groundY, shieldLoc.getZ(), point.u(), point.v());
                dust(world, at, goldColor, 1.5f);
                spark(world, at, Particle.END_ROD);
            }
            for (int i = 0; i < wall.size(); i++) {
                SealPoint point = wall.get(i);
                double[] at = SealPlane.XZ.toWorld(shieldLoc.getX(), groundY, shieldLoc.getZ(), point.u(), point.v(),
                        SealGeometry.spiralHeight(i, 0.3, phase, height));
                dust(world, at, cyanColor, 1.0f);
            }
            for (int i = 0; i < inner.size(); i++) {
                SealPoint point = inner.get(i);
                double[] at = SealPlane.XZ.toWorld(shieldLoc.getX(), groundY, shieldLoc.getZ(), point.u(), point.v(),
                        SealGeometry.spiralHeight(i, 0.7, phase * 0.7, height));
                spark(world, at, Particle.END_ROD);
            }
            return true;
        });
    }

    public void spawnFloatingShieldSeal(Location shieldLoc, int durationTicks) {
        spawnFloatingShieldSealTask(shieldLoc, shieldLoc.getY() - 3, durationTicks);
    }

    // ------------------------------------------------------------------ triangle and celestial

    public void spawnRunicTriangleSeal(ArmorStand stand, int durationTicks) {
        spawnRunicTriangleSeal(stand, durationTicks, SealPlane.XZ);
    }

    public void spawnRunicTriangleSeal(ArmorStand stand, int durationTicks, SealPlane plane) {
        Location center = stand.getLocation();
        repeat(5, durationTicks, (ticks, frame) -> {
            if (!stand.isValid()) return false;
            drawSegments(center, plane, SealGeometry.triangle(6.5, 120, 0), goldColor, 1.6f);
            drawJittered(center, plane, SealGeometry.circle(3.0, 72, 0), Color.RED, 1.4f, 5, 0.15);
            drawPoints(center, plane, SealGeometry.runes(4.5, 5.5, 100, random), Color.YELLOW, 1.5f);
            drawPoints(center, plane, SealGeometry.runes(4.5, 5.5, 40, random), flameColor, 1.5f);
            return true;
        });
    }

    public void spawnCelestialSeal(ArmorStand stand, int durationTicks) {
        spawnCelestialSeal(stand.getLocation(), durationTicks, SealPlane.XY);
    }

    public void spawnCelestialSeal(ArmorStand stand, int durationTicks, SealPlane plane) {
        spawnCelestialSeal(stand.getLocation(), durationTicks, plane);
    }

    public void spawnCelestialSeal(Location center, int durationTicks) {
        spawnCelestialSeal(center, durationTicks, SealPlane.XY);
    }

    public void spawnCelestialSeal(Location center, int durationTicks, SealPlane plane) {
        SealGeometry.CelestialRadii radii = SealGeometry.celestialRadii(plane);
        repeat(5, durationTicks, (ticks, frame) -> {
            double rotation = frame * 0.04;
            drawPoints(center, plane, SealGeometry.circle(radii.outer(), 120, rotation), Color.AQUA, 1.7f);
            drawSegments(center, plane, SealGeometry.starRing(radii.star(), 60, rotation * 0.7), Color.WHITE, 1.6f);
            drawPoints(center, plane, SealGeometry.circle(radii.middle(), 80, -rotation * 1.5), Color.WHITE, 1.7f);
            drawSegments(center, plane, SealGeometry.triangle(radii.triangle(), 30, rotation), cyanColor, 1.6f);
            sparkles(center, plane, radii.outer() * 0.35, 12);
            return true;
        });
    }

    // ------------------------------------------------------------------ wings

    public BukkitRunnable spawnWingSeal(Location center, float yaw, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return null;
        Location pose = center.clone();
        pose.setYaw(yaw);
        return wings(world, () -> pose, () -> true, WingGeometry.GOLDEN_WINGS, durationTicks,
                goldColor, Color.WHITE, Particle.END_ROD, 1.5f);
    }

    public BukkitRunnable spawnWingSeal(ArmorStand stand) {
        World world = stand.getWorld();
        if (world == null) return null;
        return wings(world, stand::getLocation, () -> stand.isValid() && !stand.isDead(),
                WingGeometry.GOLDEN_WINGS, Integer.MAX_VALUE, goldColor, Color.WHITE, Particle.END_ROD, 1.5f);
    }

    public BukkitRunnable spawnWingSeal2(Location center, float yaw, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return null;
        Location pose = center.clone();
        pose.setYaw(yaw);
        return wings(world, () -> pose, () -> true, WingGeometry.BURNING_WINGS, durationTicks,
                Color.fromRGB(0xFF1A1A), Color.fromRGB(0xFF6600), Particle.FLAME, 1.8f);
    }

    public BukkitRunnable spawnWingSeal2(ArmorStand stand) {
        World world = stand.getWorld();
        if (world == null) return null;
        return wings(world, stand::getLocation, () -> stand.isValid() && !stand.isDead(),
                WingGeometry.BURNING_WINGS, Integer.MAX_VALUE,
                Color.fromRGB(0xFF1A1A), Color.fromRGB(0xFF6600), Particle.FLAME, 1.8f);
    }

    private BukkitRunnable wings(World world, Supplier<Location> pose, BooleanSupplier alive,
                                 WingGeometry.Profile profile, int durationTicks,
                                 Color primary, Color secondary, Particle accent, float size) {
        Color bone = darken(secondary, 0.45);
        return repeat(2, durationTicks, (ticks, frame) -> {
            if (!alive.getAsBoolean()) return false;
            Location at = pose.get();
            for (WingGeometry.Stroke stroke : WingGeometry.strokes(
                    at.getX(), at.getY(), at.getZ(), at.getYaw(), ticks, profile)) {
                List<WingPoint> points = stroke.points();
                for (int i = 0; i < points.size(); i++) {
                    WingPoint point = points.get(i);
                    double f = points.size() == 1 ? 1 : i / (double) (points.size() - 1);
                    switch (stroke.part()) {
                        // A heavy, dark leading edge so the shape reads from a distance.
                        case BONE -> wingDust(world, point, bone, size * 1.35f);
                        // Flight feathers burn from the base colour to the accent towards their tips.
                        case FEATHER -> wingDust(world, point, mix(primary, secondary, f), f > 0.8 ? size * 0.8f : size);
                        case COVERT -> wingDust(world, point, mix(bone, primary, 0.6), size * 0.9f);
                    }
                }
                if (stroke.part() == WingGeometry.Part.FEATHER && ticks % 4 == 0) {
                    WingPoint tip = points.get(points.size() - 1);
                    world.spawnParticle(accent, tip.x(), tip.y(), tip.z(), 1, 0.1, 0.1, 0.1, 0.01, null, true);
                }
            }
            return true;
        });
    }

    /** Wing dust is forced: the Sentinel is fourteen blocks tall and its wings are seen from afar. */
    private static void wingDust(World world, WingPoint at, Color color, float size) {
        world.spawnParticle(Particle.DUST, at.x(), at.y(), at.z(), 1, 0, 0, 0, 0,
                new Particle.DustOptions(color, size), true);
    }

    private static Color mix(Color a, Color b, double t) {
        return Color.fromRGB(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static Color darken(Color c, double factor) {
        return Color.fromRGB((int) (c.getRed() * factor), (int) (c.getGreen() * factor), (int) (c.getBlue() * factor));
    }

    // ------------------------------------------------------------------ signature seals

    public void spawnInvulnerabilityAura(Location center, int durationTicks) {
        spawnInvulnerabilityAura(center, durationTicks, null);
    }

    public void spawnInvulnerabilityAura(Location center, int durationTicks, Player follow) {
        World world = center.getWorld();
        if (world == null) return;
        Color gold = Color.fromRGB(0xFFDD00);
        double radius = 3.5;
        repeat(3, durationTicks, (ticks, frame) -> {
            if (follow != null && !follow.isOnline()) return false;
            Location c = follow != null ? follow.getLocation().clone().add(0, 0.2, 0) : center;
            double rotation = ticks * 0.05;
            drawPoints(c, SealPlane.XZ, SealGeometry.circle(radius, 48, rotation), gold, 1.7f);
            drawPoints(c.clone().add(0, 0.9, 0), SealPlane.XZ, SealGeometry.circle(2.6, 40, -rotation),
                    Color.WHITE, 1.7f);
            drawPoints(c.clone().add(0, 1.1, 0), SealPlane.XY, SealGeometry.circle(1.8, 32, rotation * 1.5),
                    cyanColor, 1.7f);
            if (SealGeometry.every(ticks, 4)) {
                for (SealPoint point : SealGeometry.circle(radius, 6, rotation)) {
                    double[] at = SealPlane.XZ.toWorld(c.getX(), c.getY(), c.getZ(),
                            point.u(), point.v(), FLAT_LIFT);
                    spark(world, at, Particle.END_ROD);
                }
            }
            world.spawnParticle(Particle.WITCH, c.getX(), c.getY() + 1.0, c.getZ(), 1, 0.3, 0.3, 0.3, 0);
            return true;
        });
    }

    public void spawnVortexSeal(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return;
        Color purple = Color.fromRGB(0xAA44FF);
        Color darkPurple = Color.fromRGB(0x440066);
        double radius = 4.0;
        repeat(3, durationTicks, (ticks, frame) -> {
            double rotation = ticks * 0.06;
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius, 40, 0), purple, 1.8f);
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius * 0.6, 30, 0), darkPurple, 1.8f);
            List<SealPoint> orbit = SealGeometry.circle(1.0, 24, rotation);
            for (int i = 0; i < orbit.size(); i++) {
                double r = SealGeometry.vortexRadius(radius, ticks * 0.08 + i * 0.5);
                SealPoint point = orbit.get(i).scaled(r);
                double[] at = SealPlane.XZ.toWorld(center.getX(), center.getY(), center.getZ(),
                        point.u(), point.v(), FLAT_LIFT);
                dust(world, at, i % 2 == 0 ? purple : darkPurple, 1.8f);
                spark(world, at, Particle.PORTAL);
            }
            world.spawnParticle(Particle.WITCH, center.getX(), center.getY() + 0.5, center.getZ(),
                    2, 0.5, 0.2, 0.5, 0);
            return true;
        });
    }

    public void spawnQuakeSeal(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return;
        Color brown = Color.fromRGB(0x8B4513);
        Color orange = Color.fromRGB(0xFF6600);
        double radius = 5.0;
        repeat(4, durationTicks, (ticks, frame) -> {
            double rotation = ticks * 0.04;
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius, 50, 0), brown, 1.8f);
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius * 0.7, 40, 0), orange, 1.8f);
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius * 0.4, 30, 0), Color.YELLOW, 1.8f);
            List<SealPoint> orbit = SealGeometry.circle(1.0, 12, rotation);
            for (int i = 0; i < orbit.size(); i++) {
                double r = SealGeometry.quakeRadius(radius, ticks * 0.1 + i);
                SealPoint point = orbit.get(i).scaled(r);
                double[] at = SealPlane.XZ.toWorld(center.getX(), center.getY(), center.getZ(),
                        point.u(), point.v(), FLAT_LIFT);
                world.spawnParticle(Particle.CRIT, at[0], at[1], at[2], 1, 0.1, 0.1, 0.1, 0.1);
                spark(world, at, Particle.FLAME);
            }
            return true;
        });
    }

    public void spawnDivineSeal(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return;
        Color gold = Color.fromRGB(0xFFDD00);
        double radius = 5.0;
        repeat(4, durationTicks, (ticks, frame) -> {
            double rotation = ticks * 0.03;
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius, 60, 0), gold, 1.8f);
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius * 0.5, 40, 0), Color.WHITE, 1.8f);
            drawSegments(center, SealPlane.XZ, SealGeometry.spokes(0, radius, 4, rotation, 20),
                    gold, 1.6f, LINE_LIFT);
            List<SealPoint> orbit = SealGeometry.circle(1.0, 8, rotation);
            for (int i = 0; i < orbit.size(); i++) {
                double r = SealGeometry.divineRadius(radius, ticks * 0.06 + i);
                SealPoint point = orbit.get(i).scaled(r);
                double[] at = SealPlane.XZ.toWorld(center.getX(), center.getY(), center.getZ(),
                        point.u(), point.v(), FLAT_LIFT);
                spark(world, at, Particle.END_ROD);
            }
            return true;
        });
    }

    public void spawnStormSeal(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return;
        Color yellow = Color.fromRGB(0xFFFF00);
        double radius = 4.5;
        repeat(4, durationTicks, (ticks, frame) -> {
            double rotation = ticks * 0.07;
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius, 45, 0), cyanColor, 1.8f);
            drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius * 0.65, 35, 0), yellow, 1.8f);
            drawSegments(center, SealPlane.XZ, SealGeometry.spokes(radius * 0.4, radius, 6, rotation, 8),
                    Color.WHITE, 1.6f, LINE_LIFT);
            world.spawnParticle(Particle.FLAME, center.getX(), center.getY() + 0.5, center.getZ(),
                    3, 0.5, 0.2, 0.5, 0.02);
            return true;
        });
    }

    public void spawnFlamingPentagram(Location center, int durationTicks, float yaw) {
        World world = center.getWorld();
        if (world == null) return;
        Color orange = Color.fromRGB(0xFF6600);
        Color red = Color.fromRGB(0xFF1A1A);
        double radius = 3.0;
        repeat(3, durationTicks, (ticks, frame) -> {
            double rotation = ticks * 0.02;
            for (List<SealPoint> chord : SealGeometry.pentagram(radius, 24, rotation - Math.PI / 2)) {
                for (int i = 0; i < chord.size(); i++) {
                    SealPoint point = chord.get(i);
                    // Shift up by one radius so the lowest point touches the ground, not the floor.
                    double[] horizontal = SealGeometry.yawOffset(point.u(), yaw);
                    dust(world, center.getX() + horizontal[0], center.getY() + point.v() + radius,
                            center.getZ() + horizontal[1], i % 2 == 0 ? orange : red, 1.8f);
                    world.spawnParticle(Particle.FLAME,
                            center.getX() + horizontal[0], center.getY() + point.v() + radius,
                            center.getZ() + horizontal[1], 1, 0.02, 0.02, 0.02, 0);
                }
            }
            world.spawnParticle(Particle.FLAME, center.getX(), center.getY() + 0.5, center.getZ(),
                    6, 0.8, 0.2, 0.8, 0.03);
            return true;
        });
    }

    public void spawnLanceRain(Location center, int durationTicks, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        Color runeGold = Color.fromRGB(0xFFDD00);
        double topY = center.getY() + 14.0;
        BukkitRunnable task = repeat(2, durationTicks, (ticks, frame) -> {
            if (SealGeometry.every(ticks, 8)) {
                drawSegments(center, SealPlane.XZ, SealGeometry.triangle(radius, 40, 0), runeGold, 1.6f);
                drawPoints(center, SealPlane.XZ, SealGeometry.circle(radius, 50, 0), runeGold, 1.8f);
            }
            for (int i = 0; i < 6; i++) {
                SealPoint point = SealGeometry.randomRadial(0, radius, random);
                double x = center.getX() + point.u();
                double z = center.getZ() + point.v();
                for (int s = 0; s < 7; s++) {
                    double y = topY - s * 2.0;
                    spark(world, x, y, z, Particle.END_ROD);
                    dust(world, x, y, z, runeGold, 1.4f);
                }
                world.spawnParticle(Particle.CRIT, x, center.getY(), z, 4, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.FLASH, x, center.getY(), z, 1, 0, 0, 0, 0, Color.WHITE);
            }
            if (SealGeometry.inFinalWindow(ticks, durationTicks, 4)) {
                world.spawnParticle(Particle.CRIT, center.getX(), center.getY() + 1, center.getZ(),
                        30, radius, 2.0, radius, 0.3);
                world.spawnParticle(Particle.END_ROD, center.getX(), center.getY() + 1, center.getZ(),
                        20, radius, 2.0, radius, 0.1);
            }
            return true;
        });
        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, durationTicks + 10L);
    }

    public void spawnExecutionerCross(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null) return;
        Color red = Color.fromRGB(0xFF2020);
        Color bright = Color.fromRGB(0xFFB0B0);
        Color darkRed = Color.fromRGB(0x550000);
        double height = 7.0;
        double size = 2.4;
        double cx = center.getX();
        double cy = center.getY() + height;
        double cz = center.getZ();
        BukkitRunnable task = repeat(2, durationTicks, (ticks, frame) -> {
            double progress = SealGeometry.progress(ticks, durationTicks);
            double rotation = progress * Math.PI;
            double scale = SealGeometry.crossScale(size, ticks);
            Color main = SealGeometry.latePhase(progress, 0.7) ? bright : red;
            for (List<SealPoint> diagonal : SealGeometry.cross(scale, rotation, 26)) {
                for (int i = 0; i < diagonal.size(); i++) {
                    SealPoint point = diagonal.get(i);
                    double x = cx + point.u();
                    double y = cy + point.v();
                    dust(world, x, y, cz, i % 2 == 0 ? main : darkRed, 1.9f);
                    spark(world, x, y, cz, Particle.END_ROD);
                }
            }
            drawPoints(new Location(world, cx, cy, cz), SealPlane.XY,
                    SealGeometry.circle(0.45, 11, 0), darkRed, 1.6f);
            for (int i = 0; i <= 6; i++) {
                dust(world, cx, cy - 0.45 - i * 0.35, cz, darkRed, 1.4f);
            }
            for (int down = 0; down < 2; down++) {
                for (int left = 0; left < 2; left++) {
                    SealPoint tip = SealGeometry.rotate(left == 0 ? -scale : scale,
                            down == 0 ? scale : -scale, rotation);
                    double x = cx + tip.u();
                    double y = cy + tip.v();
                    world.spawnParticle(Particle.FLAME, x, y, cz, 2, 0.1, 0.1, 0.1, 0.01);
                    dust(world, x, y, cz, bright, 2.2f);
                }
            }
            world.spawnParticle(Particle.WITCH, cx, cy + 0.5, cz, 2, 0.4, 0.3, 0.4, 0.02);
            return true;
        });
        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, durationTicks + 10L);
    }

    // ------------------------------------------------------------------ the brush

    /**
     * Runs {@code frame} every {@code step} ticks for at most {@code durationTicks}, then cancels.
     *
     * <p>The frame gets the ticks drawn so far and the frame number (some effects rotate per frame,
     * not per tick) and returns false to stop early — a stand that fell, a player that left.
     */
    private BukkitRunnable repeat(int step, int durationTicks, SealFrame frame) {
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int frameNumber = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || !frame.draw(ticks, frameNumber)) {
                    cancel();
                    return;
                }
                ticks += step;
                frameNumber++;
            }
        };
        task.runTaskTimer(plugin, 0L, step);
        return task;
    }

    /** One frame of a seal: the ticks drawn so far and the frame number; false stops the task. */
    private interface SealFrame {
        boolean draw(int ticks, int frame);
    }

    private void drawPoints(Location center, SealPlane plane, List<SealPoint> points, Color color, float size) {
        drawPoints(center, plane, points, color, size, plane.vertical() ? 0 : FLAT_LIFT);
    }

    private void drawPoints(Location center, SealPlane plane, List<SealPoint> points, Color color, float size,
                            double lift) {
        World world = center.getWorld();
        if (world == null) return;
        for (SealPoint point : points) {
            dust(world, plane.toWorld(center.getX(), center.getY(), center.getZ(), point.u(), point.v(), lift),
                    color, size);
        }
    }

    private void drawSegments(Location center, SealPlane plane, List<List<SealPoint>> segments, Color color,
                              float size) {
        for (List<SealPoint> segment : segments) {
            drawPoints(center, plane, segment, color, size);
        }
    }

    private void drawSegments(Location center, SealPlane plane, List<List<SealPoint>> segments, Color color,
                              float size, double lift) {
        for (List<SealPoint> segment : segments) {
            drawPoints(center, plane, segment, color, size, lift);
        }
    }

    /** A ring sample drawn as a small cluster of jittered dots, the way the inner runes read. */
    private void drawJittered(Location center, SealPlane plane, List<SealPoint> points, Color color, float size,
                              int dotsPerPoint, double jitter) {
        for (SealPoint point : points) {
            for (int dot = 0; dot < dotsPerPoint; dot++) {
                double u = point.u() + (random.nextDouble() - 0.5) * jitter;
                double v = point.v() + (random.nextDouble() - 0.5) * jitter;
                drawPoints(center, plane, List.of(new SealPoint(u, v)), color, size);
            }
        }
    }

    /** The pentagram's red flame aura: flames scattered over the disc, wandering up to half a block. */
    private void flameAura(Location center, SealPlane plane, double radius, int count) {
        aura(center, plane, radius, count, 0.6, Particle.FLAME);
    }

    /** The celestial seal's end-rod sparkles: a wider, thinner wandering than the flames. */
    private void sparkles(Location center, SealPlane plane, double radius, int count) {
        aura(center, plane, radius, count, 0.4, Particle.END_ROD);
    }

    private void aura(Location center, SealPlane plane, double radius, int count, double innerFactor,
                      Particle particle) {
        World world = center.getWorld();
        if (world == null) return;
        for (int i = 0; i < count; i++) {
            SealPoint across = SealGeometry.randomRadial(radius * innerFactor, radius * 1.2, random);
            double up = (random.nextDouble() - 0.5) * 1.0;
            spark(world, auraOffsets(center, plane, across.u(), up, across.v()), particle);
        }
    }

    /**
     * The offset triple the auras have always used: (across, up, depth). It is deliberately not
     * {@link SealPlane#toWorld}: a vertical seal keeps the up offset on Y and collapses across and
     * depth onto one axis, which is the shape a celestial seal drawn on a stand has always had.
     */
    private static double[] auraOffsets(Location center, SealPlane plane, double across, double up, double depth) {
        return switch (plane) {
            case XZ -> new double[]{center.getX() + across, center.getY() + up, center.getZ() + depth};
            case XY -> new double[]{center.getX() + across, center.getY() + up, center.getZ()};
            case YZ -> new double[]{center.getX(), center.getY() + up, center.getZ() + depth};
        };
    }

    private static void dust(World world, double[] at, Color color, float size) {
        dust(world, at[0], at[1], at[2], color, size);
    }

    private static void dust(World world, double x, double y, double z, Color color, float size) {
        world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, new Particle.DustOptions(color, size));
    }

    private static void spark(World world, double[] at, Particle particle) {
        spark(world, at[0], at[1], at[2], particle);
    }

    private static void spark(World world, double x, double y, double z, Particle particle) {
        world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    }
}
