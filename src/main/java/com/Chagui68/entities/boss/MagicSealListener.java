package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MagicSealListener {

    public enum Plane {
        XZ,
        XY,
        YZ
    }

    private final MultiverseCreatures plugin;
    private final Random random = new Random();

    public MagicSealListener(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private final Color goldColor = Color.fromRGB(0xFFAA00);
    private final Color cyanColor = Color.fromRGB(0x88CCFF);
    private final Color flameColor = Color.fromRGB(0xFF6600);

    public void spawnPentagramSeal(ArmorStand stand, int durationTicks) {
        spawnPentagramSeal(stand, durationTicks, Plane.XZ);
    }

    public void spawnPentagramSeal(ArmorStand stand, int durationTicks, Plane plane) {
        spawnPentagramSeal(stand.getLocation(), durationTicks, plane, stand);
    }

    public void spawnPentagramSeal(Location center, int durationTicks, Plane plane) {
        spawnPentagramSeal(center, durationTicks, plane, null);
    }

    public void spawnLargePentagramSeal(Location center, int durationTicks, double radius, Plane plane) {
        final Color red = Color.fromRGB(0xFF1A1A);
        final Color redBright = Color.fromRGB(0xFF3333);
        final int perLine = Math.max(60, (int) (60 * (radius / 6.0)));
        final int ringSamples = Math.max(220, (int) (220 * (radius / 6.0)));
        final double pentagramR = radius;
        final double circleR = radius * 1.24;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                drawClassicPentagram(center, pentagramR, red, perLine, plane);
                drawFullCircle(center, circleR, redBright, ringSamples, plane);
                drawFullCircle(center, circleR, red, ringSamples / 2, plane);
                spawnRedFlameAura(center, radius * 0.42, (int) (28 * radius / 6.0), plane);
                t += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public BukkitRunnable spawnFloatingShieldSealTask(Location shieldLoc, double groundY, int durationTicks) {
        final Color gold = Color.fromRGB(0xFFAA00);
        final Color cyan = Color.fromRGB(0x88CCFF);
        final double radius = 3.0;
        final int wallSamples = 40;
        final int baseSamples = 50;
        final World world = shieldLoc.getWorld();
        if (world == null) return null;
        final double cx = shieldLoc.getX();
        final double cy = shieldLoc.getY();
        final double cz = shieldLoc.getZ();
        final double cylinderHeight = Math.max(0.5, cy - groundY);
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }

                double phase = (t * 0.25) % cylinderHeight;

                // Stable circle at ground level
                for (int i = 0; i < baseSamples; i++) {
                    double theta = (2 * Math.PI * i / baseSamples);
                    double x = cx + Math.cos(theta) * radius;
                    double z = cz + Math.sin(theta) * radius;
                    Location loc = new Location(world, x, groundY, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(gold, 1.5f));
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }

                // Ascending particles along the cylinder walls from ground to shield
                for (int i = 0; i < wallSamples; i++) {
                    double theta = (2 * Math.PI * i / wallSamples);
                    double x = cx + Math.cos(theta) * radius;
                    double z = cz + Math.sin(theta) * radius;
                    double yOff = ((i * 0.3 + phase) % cylinderHeight);
                    Location loc = new Location(world, x, groundY + yOff, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(cyan, 1.0f));
                }

                // Inner ascending particles from ground to shield
                for (int i = 0; i < 8; i++) {
                    double theta = (2 * Math.PI * i / 8);
                    double r = radius * 0.6;
                    double x = cx + Math.cos(theta) * r;
                    double z = cz + Math.sin(theta) * r;
                    double yOff = ((i * 0.7 + phase * 0.7) % cylinderHeight);
                    Location loc = new Location(world, x, groundY + yOff, z);
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }

                t += 3;
            }
        };
        task.runTaskTimer(plugin, 0L, 3L);
        return task;
    }

    public void spawnFloatingShieldSeal(Location shieldLoc, int durationTicks) {
        spawnFloatingShieldSealTask(shieldLoc, shieldLoc.getY() - 3, durationTicks);
    }

    private void spawnPentagramSeal(Location center, int durationTicks, Plane plane, ArmorStand stand) {
        final Color red = Color.fromRGB(0xFF1A1A);
        final Color redBright = Color.fromRGB(0xFF3333);
        final int perLine = 60;
        final int ringSamples = 220;
        final double pentagramR = 6.0;
        final double circleR = 7.4;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks || (stand != null && !stand.isValid())) {
                    cancel();
                    return;
                }
                drawClassicPentagram(center, pentagramR, red, perLine, plane);
                drawFullCircle(center, circleR, redBright, ringSamples, plane);
                drawFullCircle(center, circleR, red, ringSamples / 2, plane);
                spawnRedFlameAura(center, 2.5, 28, plane);
                t += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void spawnRunicTriangleSeal(ArmorStand stand, int durationTicks) {
        spawnRunicTriangleSeal(stand, durationTicks, Plane.XZ);
    }

    public void spawnRunicTriangleSeal(ArmorStand stand, int durationTicks, Plane plane) {
        Location center = stand.getLocation();
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks || !stand.isValid()) {
                    cancel();
                    return;
                }
                drawTriangle(center, 6.5, goldColor, 120, plane);
                drawInnerCircle(center, 3.0, Color.RED, 72, 5, plane);
                drawRunicArcs(center, 4.5, 5.5, Color.YELLOW, 100, plane);
                drawRunicArcs(center, 4.5, 5.5, flameColor, 40, plane);
                t += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void spawnCelestialSeal(ArmorStand stand, int durationTicks) {
        spawnCelestialSeal(stand.getLocation(), durationTicks, Plane.XY);
    }

    public void spawnCelestialSeal(ArmorStand stand, int durationTicks, Plane plane) {
        spawnCelestialSeal(stand.getLocation(), durationTicks, plane);
    }

    public void spawnCelestialSeal(Location center, int durationTicks) {
        spawnCelestialSeal(center, durationTicks, Plane.XY);
    }

    public void spawnCelestialSeal(Location center, int durationTicks, Plane plane) {
        final boolean vertical = (plane == Plane.XY || plane == Plane.YZ);
        final double mul = vertical ? 1.3 : 1.0;
        final double rOuter = 3.0 * mul;
        final double rMid = 2.0 * mul;
        final double rStar = 2.5 * mul;
        final double rTri = 1.4 * mul;
        final int ringSamples = 120;
        final int midSamples = 80;
        final int starSamples = 60;
        final int triSamples = 30;
        new BukkitRunnable() {
            int t = 0;
            int frame = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rot = frame * 0.04;
                drawCircle(center, rOuter, Color.AQUA, ringSamples, rot, plane);
                drawOuterStarRing(center, rStar, Color.WHITE, starSamples, rot * 0.7, plane);
                drawCircle(center, rMid, Color.WHITE, midSamples, -rot * 1.5, plane);
                drawRotatedTriangle(center, rTri, cyanColor, triSamples, rot, plane);
                spawnEndRodSparkles(center, rOuter * 0.35, 12, plane);
                t += 5;
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnFlameAura(Location center, double radius, int count, Plane plane) {
        World world = center.getWorld();
        double[] axis = new double[3];
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius * (0.6 + random.nextDouble() * 0.6);
            double h = (random.nextDouble() - 0.5) * 1.0;
            computeAxis(center, plane, r * Math.cos(a), h, r * Math.sin(a), axis);
            Location loc = new Location(world, axis[0], axis[1], axis[2]);
            world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
        }
    }

    private void spawnEndRodSparkles(Location center, double radius, int count, Plane plane) {
        World world = center.getWorld();
        double[] axis = new double[3];
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius * (0.4 + random.nextDouble() * 0.8);
            double h = (random.nextDouble() - 0.5) * 1.0;
            computeAxis(center, plane, r * Math.cos(a), h, r * Math.sin(a), axis);
            Location loc = new Location(world, axis[0], axis[1], axis[2]);
            world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
        }
    }

    private void computeAxis(Location center, Plane plane, double a, double b, double c, double[] out) {
        switch (plane) {
            case XZ -> {
                out[0] = center.getX() + a;
                out[1] = center.getY() + b;
                out[2] = center.getZ() + c;
            }
            case XY -> {
                out[0] = center.getX() + a;
                out[1] = center.getY() + b;
                out[2] = center.getZ();
            }
            case YZ -> {
                out[0] = center.getX();
                out[1] = center.getY() + b;
                out[2] = center.getZ() + c;
            }
        }
    }

    private double baseY(Location center, Plane plane) {
        return plane == Plane.XZ ? center.getY() + 0.05 : center.getY();
    }

    private void drawCircle(Location center, double radius, Color color, int samples, double rotOffset, Plane plane) {
        World world = center.getWorld();
        double step = (2 * Math.PI) / samples;
        for (int i = 0; i < samples; i++) {
            double a = i * step + rotOffset;
            double[] p = angToXZ(a, radius);
            double[] tp = mapTriple(p, center, plane);
            Location loc = new Location(world, tp[0], tp[1], tp[2]);
            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.7f));
        }
    }

    private void drawOuterRing(Location center, double radius, Color color, int samples, int leap, Plane plane) {
        World world = center.getWorld();
        double step = (2 * Math.PI) / samples;
        for (int i = 0; i < samples; i += leap) {
            double a = i * step;
            double[] p = angToXZ(a, radius);
            double[] tp = mapTriple(p, center, plane);
            Location loc = new Location(world, tp[0], tp[1], tp[2]);
            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 2.0f));
        }
    }

    private double[] angToXZ(double angle, double radius) {
        return new double[]{radius * Math.cos(angle), radius * Math.sin(angle)};
    }

    private void drawClassicPentagram(Location center, double radius, Color color, int samplesPerEdge, Plane plane) {
        World world = center.getWorld();
        double[][] pts = new double[5][2];
        for (int i = 0; i < 5; i++) {
            double a = Math.PI / 2 + i * (2 * Math.PI / 5);
            pts[i][0] = radius * Math.cos(a);
            pts[i][1] = radius * Math.sin(a);
        }
        int[] order = {0, 2, 4, 1, 3, 0};
        for (int i = 0; i < order.length - 1; i++) {
            double[] p1 = pts[order[i]];
            double[] p2 = pts[order[i + 1]];
            sampleLine(world, center, plane,
                    mapTriplePair(p1, center, plane),
                    mapTriplePair(p2, center, plane),
                    color, samplesPerEdge);
        }
        sampleLine(world, center, plane,
                mapTriplePair(pts[0], center, plane),
                mapTriplePair(pts[2], center, plane),
                color, samplesPerEdge);
    }

    private double[] mapTriplePair(double[] p, Location center, Plane plane) {
        return switch (plane) {
            case XZ -> new double[]{center.getX() + p[0], center.getY() + 0.06, center.getZ() + p[1]};
            case XY -> new double[]{center.getX() + p[0], center.getY() + p[1], center.getZ()};
            case YZ -> new double[]{center.getX(), center.getY() + p[0], center.getZ() + p[1]};
        };
    }

    private void drawFullCircle(Location center, double radius, Color color, int samples, Plane plane) {
        World world = center.getWorld();
        double step = (2 * Math.PI) / samples;
        for (int i = 0; i < samples; i++) {
            double a = i * step;
            double[] p = angToXZ(a, radius);
            double[] tp = mapTriplePair(p, center, plane);
            Location loc = new Location(world, tp[0], tp[1], tp[2]);
            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.8f));
        }
    }

    private void spawnRedFlameAura(Location center, double radius, int count, Plane plane) {
        World world = center.getWorld();
        double[] axis = new double[3];
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius * (0.6 + random.nextDouble() * 0.6);
            double h = (random.nextDouble() - 0.5) * 1.0;
            computeAxis(center, plane, r * Math.cos(a), h, r * Math.sin(a), axis);
            Location loc = new Location(world, axis[0], axis[1], axis[2]);
            world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
        }
    }

    private void drawTriangle(Location center, double radius, Color color, int samples, Plane plane) {
        World world = center.getWorld();
        double[] angles = {0, 2 * Math.PI / 3, 4 * Math.PI / 3};
        double[][] pts = new double[5][2];
        for (int i = 0; i < 3; i++) {
            pts[i] = angToXZ(angles[i], radius);
        }
        for (int i = 0; i < 3; i++) {
            int next = (i + 1) % 3;
            double[] p1 = pts[i];
            double[] p2 = pts[next];
            sampleLine(world, center, plane, mapTriple(p1, center, plane), mapTriple(p2, center, plane),
                    color, samples / 3);
        }
    }

    private double[] mapTriple(double[] p, Location center, Plane plane) {
        return switch (plane) {
            case XZ -> new double[]{center.getX() + p[0], center.getY() + 0.06, center.getZ() + p[1]};
            case XY -> new double[]{center.getX() + p[0], center.getY() + p[1], center.getZ()};
            case YZ -> new double[]{center.getX(), center.getY() + p[0], center.getZ() + p[1]};
        };
    }

    private void drawRotatedTriangle(Location center, double radius, Color color, int samples, double rotOffset, Plane plane) {
        World world = center.getWorld();
        double[] angles = {0, 2 * Math.PI / 3, 4 * Math.PI / 3};
        double[][] pts = new double[3][2];
        for (int i = 0; i < 3; i++) {
            pts[i] = angToXZ(angles[i] + rotOffset, radius);
        }
        for (int i = 0; i < 3; i++) {
            int next = (i + 1) % 3;
            double[] p1 = pts[i];
            double[] p2 = pts[next];
            sampleLine(world, center, plane, mapTriple(p1, center, plane), mapTriple(p2, center, plane),
                    color, samples / 3);
        }
    }

    private void drawOuterStarRing(Location center, double radius, Color color, int samples, double rotOffset, Plane plane) {
        World world = center.getWorld();
        for (int i = 0; i < 6; i++) {
            double a1 = i * Math.PI / 3 + rotOffset;
            double a2 = a1 + Math.PI / 3;
            double[] outer = angToXZ(a1, radius);
            double[] inner = angToXZ(a2, radius * 0.55);
            sampleLine(world, center, plane,
                    mapTriple(outer, center, plane),
                    mapTriple(inner, center, plane),
                    color, samples / 12);
            double[] outer2 = angToXZ(a2, radius);
            sampleLine(world, center, plane,
                    mapTriple(inner, center, plane),
                    mapTriple(outer2, center, plane),
                    color, samples / 12);
        }
    }

    private void drawInnerCircle(Location center, double radius, Color color, int samples, int dotsPerStep, Plane plane) {
        World world = center.getWorld();
        double step = (2 * Math.PI) / samples;
        for (int i = 0; i < samples; i++) {
            double a = i * step;
            double[] p = angToXZ(a, radius);
            double[] tp = mapTriple(p, center, plane);
            for (int j = 0; j < dotsPerStep; j++) {
                double jx = (random.nextDouble() - 0.5) * 0.15;
                double jy = (random.nextDouble() - 0.5) * 0.15;
                double[] axis = switch (plane) {
                    case XZ -> new double[]{tp[0] + jx, tp[1], tp[2] + jy};
                    case XY -> new double[]{tp[0] + jx, tp[1] + jy, tp[2]};
                    case YZ -> new double[]{tp[0], tp[1] + jx, tp[2] + jy};
                };
                Location loc = new Location(world, axis[0], axis[1], axis[2]);
                world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.4f));
            }
        }
    }

    private void drawRunicArcs(Location center, double minR, double maxR, Color color, int samples, Plane plane) {
        World world = center.getWorld();
        double step = (2 * Math.PI) / samples;
        for (int i = 0; i < samples; i++) {
            double a = i * step;
            double r = minR + random.nextDouble() * (maxR - minR);
            double[] p = angToXZ(a, r);
            double[] tp = mapTriple(p, center, plane);
            Location loc = new Location(world, tp[0], tp[1], tp[2]);
            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.5f));
        }
    }

    public BukkitRunnable spawnWingSeal(Location center, float yaw, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return null;
        final double cx = center.getX();
        final double cy = center.getY();
        final double cz = center.getZ();
        final double yawRad = Math.toRadians(yaw);
        final double rx = Math.cos(yawRad);
        final double rz = Math.sin(yawRad);
        final double fx = -Math.sin(yawRad);
        final double fz = Math.cos(yawRad);
        final double bx = -fx;
        final double bz = -fz;
        final Color gold = Color.fromRGB(0xFFAA00);
        final Color white = Color.WHITE;
        final double shoulderH = 9.0;
        final double shoulderW = 3.75;
        final double wingLen = 9.0;
        final double wingReach = 7.0;
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double flap = Math.sin(t * 0.08) * 0.5;
                for (int side = -1; side <= 1; side += 2) {
                    double shoulderX = cx + side * shoulderW * rx;
                    double shoulderZ = cz + side * shoulderW * rz;
                    for (int f = 0; f < 8; f++) {
                        double p = (double) f / 7.0;
                        double spread = -0.6 + p * 1.8;
                        double len = Math.sin(p * Math.PI + 0.1) * wingLen;
                        double tipOut = side * Math.cos(spread) * len;
                        double tipUp = Math.sin(spread) * len;
                        double tipBack = p * wingReach;
                        double cosF = Math.cos(flap * (1.0 - p * 0.3));
                        double sinF = Math.sin(flap * (1.0 - p * 0.3));
                        double upF = tipUp * cosF - tipBack * sinF;
                        double backF = tipUp * sinF + tipBack * cosF;
                        double tipWx = shoulderX + tipOut * rx + backF * bx;
                        double tipWy = cy + shoulderH + upF;
                        double tipWz = shoulderZ + tipOut * rz + backF * bz;
                        for (int s = 0; s <= 6; s++) {
                            double frac = (double) s / 6.0;
                            double lx = shoulderX + (tipWx - shoulderX) * frac;
                            double ly = cy + shoulderH + (tipWy - (cy + shoulderH)) * frac;
                            double lz = shoulderZ + (tipWz - shoulderZ) * frac;
                            Location loc = new Location(world, lx, ly, lz);
                            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(s % 2 == 0 ? gold : white, 1.5f));
                            world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        return task;
    }

    public BukkitRunnable spawnWingSeal(ArmorStand stand) {
        final World world = stand.getWorld();
        if (world == null) return null;
        final Color gold = Color.fromRGB(0xFFAA00);
        final Color white = Color.WHITE;
        final double shoulderH = 9.0;
        final double shoulderW = 3.75;
        final double wingLen = 9.0;
        final double wingReach = 7.0;
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!stand.isValid() || stand.isDead()) {
                    cancel();
                    return;
                }
                Location loc = stand.getLocation();
                double cx = loc.getX();
                double cy = loc.getY();
                double cz = loc.getZ();
                double yawRad = Math.toRadians(loc.getYaw());
                double rx = Math.cos(yawRad);
                double rz = Math.sin(yawRad);
                double fx = -Math.sin(yawRad);
                double fz = Math.cos(yawRad);
                double bx = -fx;
                double bz = -fz;
                double flap = Math.sin(t * 0.08) * 0.5;
                for (int side = -1; side <= 1; side += 2) {
                    double shoulderX = cx + side * shoulderW * rx;
                    double shoulderZ = cz + side * shoulderW * rz;
                    for (int f = 0; f < 8; f++) {
                        double p = (double) f / 7.0;
                        double spread = -0.6 + p * 1.8;
                        double len = Math.sin(p * Math.PI + 0.1) * wingLen;
                        double tipOut = side * Math.cos(spread) * len;
                        double tipUp = Math.sin(spread) * len;
                        double tipBack = p * wingReach;
                        double cosF = Math.cos(flap * (1.0 - p * 0.3));
                        double sinF = Math.sin(flap * (1.0 - p * 0.3));
                        double upF = tipUp * cosF - tipBack * sinF;
                        double backF = tipUp * sinF + tipBack * cosF;
                        double tipWx = shoulderX + tipOut * rx + backF * bx;
                        double tipWy = cy + shoulderH + upF;
                        double tipWz = shoulderZ + tipOut * rz + backF * bz;
                        for (int s = 0; s <= 6; s++) {
                            double frac = (double) s / 6.0;
                            double lx = shoulderX + (tipWx - shoulderX) * frac;
                            double ly = cy + shoulderH + (tipWy - (cy + shoulderH)) * frac;
                            double lz = shoulderZ + (tipWz - shoulderZ) * frac;
                            Location pl = new Location(world, lx, ly, lz);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(s % 2 == 0 ? gold : white, 1.5f));
                            world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                        }
                    }
                }
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        return task;
    }

    public BukkitRunnable spawnWingSeal2(Location center, float yaw, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return null;
        final double cx = center.getX();
        final double cy = center.getY();
        final double cz = center.getZ();
        final double yawRad = Math.toRadians(yaw);
        final double rx = Math.cos(yawRad);
        final double rz = Math.sin(yawRad);
        final double fx = -Math.sin(yawRad);
        final double fz = Math.cos(yawRad);
        final double bx = -fx;
        final double bz = -fz;
        final Color red = Color.fromRGB(0xFF1A1A);
        final Color orange = Color.fromRGB(0xFF6600);
        final double shoulderH = 9.0;
        final double shoulderW = 3.75;
        final double wingLen = 11.0;
        final double wingReach = 8.0;
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double flap = Math.sin(t * 0.06) * 0.6;
                for (int side = -1; side <= 1; side += 2) {
                    double shoulderX = cx + side * shoulderW * rx;
                    double shoulderZ = cz + side * shoulderW * rz;
                    for (int f = 0; f < 12; f++) {
                        double p = (double) f / 11.0;
                        double spread = -0.8 + p * 2.0;
                        double len = Math.pow(Math.sin(p * Math.PI + 0.1), 1.5) * wingLen;
                        double tipOut = side * Math.cos(spread) * len;
                        double tipUp = Math.sin(spread) * len;
                        double tipBack = p * wingReach;
                        double cosF = Math.cos(flap * (1.0 - p * 0.4));
                        double sinF = Math.sin(flap * (1.0 - p * 0.4));
                        double upF = tipUp * cosF - tipBack * sinF;
                        double backF = tipUp * sinF + tipBack * cosF;
                        double tipWx = shoulderX + tipOut * rx + backF * bx;
                        double tipWy = cy + shoulderH + upF;
                        double tipWz = shoulderZ + tipOut * rz + backF * bz;
                        for (int s = 0; s <= 5; s++) {
                            double frac = (double) s / 5.0;
                            double lx = shoulderX + (tipWx - shoulderX) * frac;
                            double ly = cy + shoulderH + (tipWy - (cy + shoulderH)) * frac;
                            double lz = shoulderZ + (tipWz - shoulderZ) * frac;
                            Location loc = new Location(world, lx, ly, lz);
                            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(s % 2 == 0 ? red : orange, 1.8f));
                            world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        return task;
    }

    public BukkitRunnable spawnWingSeal2(ArmorStand stand) {
        final World world = stand.getWorld();
        if (world == null) return null;
        final Color red = Color.fromRGB(0xFF1A1A);
        final Color orange = Color.fromRGB(0xFF6600);
        final double shoulderH = 9.0;
        final double shoulderW = 3.75;
        final double wingLen = 11.0;
        final double wingReach = 8.0;
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!stand.isValid() || stand.isDead()) {
                    cancel();
                    return;
                }
                Location loc = stand.getLocation();
                double cx = loc.getX();
                double cy = loc.getY();
                double cz = loc.getZ();
                double yawRad = Math.toRadians(loc.getYaw());
                double rx = Math.cos(yawRad);
                double rz = Math.sin(yawRad);
                double fx = -Math.sin(yawRad);
                double fz = Math.cos(yawRad);
                double bx = -fx;
                double bz = -fz;
                double flap = Math.sin(t * 0.06) * 0.6;
                for (int side = -1; side <= 1; side += 2) {
                    double shoulderX = cx + side * shoulderW * rx;
                    double shoulderZ = cz + side * shoulderW * rz;
                    for (int f = 0; f < 12; f++) {
                        double p = (double) f / 11.0;
                        double spread = -0.8 + p * 2.0;
                        double len = Math.pow(Math.sin(p * Math.PI + 0.1), 1.5) * wingLen;
                        double tipOut = side * Math.cos(spread) * len;
                        double tipUp = Math.sin(spread) * len;
                        double tipBack = p * wingReach;
                        double cosF = Math.cos(flap * (1.0 - p * 0.4));
                        double sinF = Math.sin(flap * (1.0 - p * 0.4));
                        double upF = tipUp * cosF - tipBack * sinF;
                        double backF = tipUp * sinF + tipBack * cosF;
                        double tipWx = shoulderX + tipOut * rx + backF * bx;
                        double tipWy = cy + shoulderH + upF;
                        double tipWz = shoulderZ + tipOut * rz + backF * bz;
                        for (int s = 0; s <= 5; s++) {
                            double frac = (double) s / 5.0;
                            double lx = shoulderX + (tipWx - shoulderX) * frac;
                            double ly = cy + shoulderH + (tipWy - (cy + shoulderH)) * frac;
                            double lz = shoulderZ + (tipWz - shoulderZ) * frac;
                            Location pl = new Location(world, lx, ly, lz);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(s % 2 == 0 ? red : orange, 1.8f));
                            world.spawnParticle(Particle.FLAME, pl, 1, 0, 0, 0, 0);
                        }
                    }
                }
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        return task;
    }

    public void spawnInvulnerabilityAura(Location center, int durationTicks) {
        spawnInvulnerabilityAura(center, durationTicks, null);
    }

    public void spawnInvulnerabilityAura(Location center, int durationTicks, Player follow) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color gold = Color.fromRGB(0xFFDD00);
        final Color cyan = Color.fromRGB(0x88CCFF);
        final double radius = 3.5;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks || (follow != null && !follow.isOnline())) {
                    cancel();
                    return;
                }
                Location c = follow != null ? follow.getLocation().clone().add(0, 0.2, 0) : center;
                double rot = t * 0.05;
                drawCircle(c, radius, gold, 48, rot, Plane.XZ);
                drawCircle(c.clone().add(0, 0.9, 0), 2.6, Color.WHITE, 40, -rot, Plane.XZ);
                drawCircle(c.clone().add(0, 1.1, 0), 1.8, cyan, 32, rot * 1.5, Plane.XY);
                if (t % 4 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double a = (2 * Math.PI * i / 6) + rot;
                        double[] p = angToXZ(a, radius);
                        double[] tp = mapTriplePair(p, c, Plane.XZ);
                        Location loc = new Location(world, tp[0], tp[1], tp[2]);
                        world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                    }
                }
                world.spawnParticle(Particle.WITCH, c.clone().add(0, 1.0, 0), 1, 0.3, 0.3, 0.3, 0);
                t += 3;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    public void spawnVortexSeal(Location center, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color purple = Color.fromRGB(0xAA44FF);
        final Color darkPurple = Color.fromRGB(0x440066);
        final double radius = 4.0;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rot = t * 0.06;
                drawFullCircle(center, radius, purple, 40, Plane.XZ);
                drawFullCircle(center, radius * 0.6, darkPurple, 30, Plane.XZ);
                for (int i = 0; i < 24; i++) {
                    double a = (2 * Math.PI * i / 24) + rot;
                    double r = radius * (0.3 + (Math.sin(t * 0.08 + i * 0.5) * 0.3 + 0.5) * 0.4);
                    double[] p = angToXZ(a, r);
                    double[] tp = mapTriplePair(p, center, Plane.XZ);
                    Location loc = new Location(world, tp[0], tp[1], tp[2]);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(i % 2 == 0 ? purple : darkPurple, 1.8f));
                    world.spawnParticle(Particle.PORTAL, loc, 1, 0, 0, 0, 0);
                }
                world.spawnParticle(Particle.WITCH, center.clone().add(0, 0.5, 0), 2, 0.5, 0.2, 0.5, 0);
                t += 3;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    public void spawnQuakeSeal(Location center, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color brown = Color.fromRGB(0x8B4513);
        final Color orange = Color.fromRGB(0xFF6600);
        final double radius = 5.0;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rot = t * 0.04;
                drawFullCircle(center, radius, brown, 50, Plane.XZ);
                drawFullCircle(center, radius * 0.7, orange, 40, Plane.XZ);
                drawFullCircle(center, radius * 0.4, Color.YELLOW, 30, Plane.XZ);
                for (int i = 0; i < 12; i++) {
                    double a = (2 * Math.PI * i / 12) + rot;
                    double r = radius * 0.8 + Math.sin(t * 0.1 + i) * 0.5;
                    double[] p = angToXZ(a, r);
                    double[] tp = mapTriplePair(p, center, Plane.XZ);
                    Location loc = new Location(world, tp[0], tp[1], tp[2]);
                    world.spawnParticle(Particle.CRIT, loc, 1, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
                }
                t += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    public void spawnDivineSeal(Location center, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color gold = Color.fromRGB(0xFFDD00);
        final Color white = Color.WHITE;
        final double radius = 5.0;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rot = t * 0.03;
                drawFullCircle(center, radius, gold, 60, Plane.XZ);
                drawFullCircle(center, radius * 0.5, white, 40, Plane.XZ);
                for (int i = 0; i < 4; i++) {
                    double a = i * Math.PI / 2 + rot;
                    double[] outer = angToXZ(a, radius);
                    double[] inner = angToXZ(0, 0);
                    double[] tp1 = mapTriplePair(outer, center, Plane.XZ);
                    double[] tp2 = mapTriplePair(inner, center, Plane.XZ);
                    sampleLine(world, center, Plane.XZ, tp1, tp2, gold, 20);
                }
                for (int i = 0; i < 8; i++) {
                    double a = (2 * Math.PI * i / 8) + rot;
                    double r = radius * 0.75 + Math.sin(t * 0.06 + i) * 0.3;
                    double[] p = angToXZ(a, r);
                    double[] tp = mapTriplePair(p, center, Plane.XZ);
                    Location loc = new Location(world, tp[0], tp[1], tp[2]);
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }
                t += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    public void spawnStormSeal(Location center, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color yellow = Color.fromRGB(0xFFFF00);
        final Color cyan = Color.fromRGB(0x88CCFF);
        final double radius = 4.5;
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rot = t * 0.07;
                drawFullCircle(center, radius, cyan, 45, Plane.XZ);
                drawFullCircle(center, radius * 0.65, yellow, 35, Plane.XZ);
                for (int i = 0; i < 6; i++) {
                    double a = (2 * Math.PI * i / 6) + rot;
                    double[] p1 = angToXZ(a, radius * 0.4);
                    double[] p2 = angToXZ(a, radius);
                    double[] tp1 = mapTriplePair(p1, center, Plane.XZ);
                    double[] tp2 = mapTriplePair(p2, center, Plane.XZ);
                    sampleLine(world, center, Plane.XZ, tp1, tp2, Color.WHITE, 8);
                }
                world.spawnParticle(Particle.FLAME, center.clone().add(0, 0.5, 0), 3, 0.5, 0.2, 0.5, 0.02);
                t += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    public void spawnFlamingPentagram(Location center, int durationTicks, float yaw) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color flameOrange = Color.fromRGB(0xFF6600);
        final Color flameRed = Color.fromRGB(0xFF1A1A);
        final double radius = 3.0;
        final double cosYaw = Math.cos(Math.toRadians(-yaw));
        final double sinYaw = Math.sin(Math.toRadians(-yaw));
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double rotation = t * 0.02;
                // Shift up by one radius so the lowest point touches the ground
                // instead of sinking into the floor.
                for (int v = 0; v < 5; v++) {
                    double a1 = rotation + v * 2 * Math.PI / 5 - Math.PI / 2;
                    double a2 = rotation + (v + 2) % 5 * 2 * Math.PI / 5 - Math.PI / 2;
                    double x1 = Math.cos(a1) * radius;
                    double y1 = Math.sin(a1) * radius + radius;
                    double x2 = Math.cos(a2) * radius;
                    double y2 = Math.sin(a2) * radius + radius;
                    for (int i = 0; i <= 24; i++) {
                        double f = i / 24.0;
                        double x = x1 + (x2 - x1) * f;
                        double y = y1 + (y2 - y1) * f;
                        Location loc = new Location(world, center.getX() + x * cosYaw, center.getY() + y, center.getZ() + x * sinYaw);
                        world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(i % 2 == 0 ? flameOrange : flameRed, 1.8f));
                        world.spawnParticle(Particle.FLAME, loc, 1, 0.02, 0.02, 0.02, 0);
                    }
                }
                world.spawnParticle(Particle.FLAME, center.clone().add(0, 0.5, 0), 6, 0.8, 0.2, 0.8, 0.03);
                t += 3;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    public void spawnLanceRain(Location center, int durationTicks, double radius) {
        final World world = center.getWorld();
        if (world == null) return;
        final Random random = new Random();
        final Color runeGold = Color.fromRGB(0xFFDD00);
        final double topY = center.getY() + 14.0;
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                if (t % 8 == 0) {
                    drawTriangle(center, radius, runeGold, 40, Plane.XZ);
                    drawFullCircle(center, radius, runeGold, 50, Plane.XZ);
                }
                for (int i = 0; i < 6; i++) {
                    double a = random.nextDouble() * 2 * Math.PI;
                    double r = random.nextDouble() * radius;
                    double x = center.getX() + Math.cos(a) * r;
                    double z = center.getZ() + Math.sin(a) * r;
                    for (int s = 0; s < 7; s++) {
                        Location loc = new Location(world, x, topY - s * 2.0, z);
                        world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(runeGold, 1.4f));
                    }
                    Location impact = new Location(world, x, center.getY(), z);
                    world.spawnParticle(Particle.CRIT, impact, 4, 0.2, 0.2, 0.2, 0.1);
                    world.spawnParticle(Particle.FLASH, impact, 1, 0, 0, 0, 0, Color.WHITE);
                }
                if (t >= durationTicks - 4) {
                    Location burst = center.clone().add(0, 1, 0);
                    world.spawnParticle(Particle.CRIT, burst, 30, radius, 2.0, radius, 0.3);
                    world.spawnParticle(Particle.END_ROD, burst, 20, radius, 2.0, radius, 0.1);
                }
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, durationTicks + 10L);
    }

    public void spawnExecutionerCross(Location center, int durationTicks) {
        final World world = center.getWorld();
        if (world == null) return;
        final Color red = Color.fromRGB(0xFF2020);
        final Color bright = Color.fromRGB(0xFFB0B0);
        final Color darkRed = Color.fromRGB(0x550000);
        final double height = 7.0;
        final double size = 2.4;
        final double cx = center.getX();
        final double cy = center.getY() + height;
        final double cz = center.getZ();
        BukkitRunnable task = new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= durationTicks) {
                    cancel();
                    return;
                }
                double progress = (double) t / durationTicks;
                double rot = progress * Math.PI;
                double s = size * (1.0 + 0.08 * Math.sin(t * 0.12));
                Color main = progress > 0.7 ? bright : red;
                double cos = Math.cos(rot);
                double sin = Math.sin(rot);
                for (int i = 0; i <= 26; i++) {
                    double f = i / 26.0;
                    for (int d = 0; d < 2; d++) {
                        double lx = -s + 2 * s * f;
                        double ly = d == 0 ? (s - 2 * s * f) : -(s - 2 * s * f);
                        double rx = lx * cos - ly * sin;
                        double ry = lx * sin + ly * cos;
                        Location loc = new Location(world, cx + rx, cy + ry, cz);
                        world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(i % 2 == 0 ? main : darkRed, 1.9f));
                        world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                    }
                }
                for (int i = 0; i <= 10; i++) {
                    double a = 2 * Math.PI * i / 10;
                    Location knot = new Location(world, cx + Math.cos(a) * 0.45, cy + Math.sin(a) * 0.45, cz);
                    world.spawnParticle(Particle.DUST, knot, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(darkRed, 1.6f));
                }
                for (int i = 0; i <= 6; i++) {
                    Location rope = new Location(world, cx, cy - 0.45 - i * 0.35, cz);
                    world.spawnParticle(Particle.DUST, rope, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(darkRed, 1.4f));
                }
                for (int d = 0; d < 2; d++) {
                    for (int e = 0; e < 2; e++) {
                        double ly = d == 0 ? s : -s;
                        double lx = e == 0 ? -s : s;
                        double rx = lx * cos - ly * sin;
                        double ry = lx * sin + ly * cos;
                        Location tip = new Location(world, cx + rx, cy + ry, cz);
                        world.spawnParticle(Particle.FLAME, tip, 2, 0.1, 0.1, 0.1, 0.01);
                        world.spawnParticle(Particle.DUST, tip, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(bright, 2.2f));
                    }
                }
                world.spawnParticle(Particle.WITCH, new Location(world, cx, cy + 0.5, cz), 2, 0.4, 0.3, 0.4, 0.02);
                t += 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, durationTicks + 10L);
    }

    private void sampleLine(World world, Location center, Plane plane,
                            double[] p1, double[] p2, Color color, int samples) {
        double yOffset = plane == Plane.XZ ? 0.04 : 0.0;
        for (int i = 0; i <= samples; i++) {
            double t = (double) i / samples;
            double x;
            double y;
            double z;
            switch (plane) {
                case XZ -> {
                    x = p1[0] + (p2[0] - p1[0]) * t;
                    y = p1[1] + (p2[1] - p1[1]) * t;
                    z = p1[2] + (p2[2] - p1[2]) * t;
                }
                case XY -> {
                    x = p1[0] + (p2[0] - p1[0]) * t;
                    y = p1[1] + (p2[1] - p1[1]) * t;
                    z = p1[2];
                }
                case YZ -> {
                    x = p1[0];
                    y = p1[1] + (p2[1] - p1[1]) * t;
                    z = p1[2] + (p2[2] - p1[2]) * t;
                }
                default -> {
                    x = p1[0];
                    y = p1[1];
                    z = p1[2];
                }
            }
            Location loc = new Location(world, x, y + yOffset, z);
            world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.6f));
        }
    }
}
