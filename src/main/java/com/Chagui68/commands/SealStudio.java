package com.Chagui68.commands;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.seal.SealPlane;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

/**
 * The {@code /msc seal} subsystem: the pattern table, the plane parsing and the two hand-rolled
 * particle shapes (circle and six-point star) that draw themselves every tick.
 *
 * <p>Split out of {@code MSCCommand} so the pattern list is declared once for help and tab
 * completion, and the particle code lives next to the seal listener it feeds.
 */
final class SealStudio {

    /** Every pattern accepted by {@code /msc seal}, in tab-completion and help order. */
    static final List<String> PATTERNS = List.of(
            "pentagram", "triangle", "celestial", "circle", "ring", "star",
            "floating", "wings", "wings2", "vortex", "quake", "divine", "storm");

    private static final List<String> PLANE_HELP = List.of(
            "horizontal (default)", "vertical-north", "vertical-east");

    private final MultiverseCreatures plugin;

    SealStudio(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    static List<String> helpLines() {
        List<String> lines = new ArrayList<>(CommandMenu.category("Patterns", PATTERNS));
        lines.addAll(CommandMenu.category("Planes", PLANE_HELP));
        return lines;
    }

    void handle(CommandSender sender, String[] args) {
        CommandMenu menu = new CommandMenu(sender);
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can spawn seals.");
            return;
        }

        if (args.length < 2) {
            menu.sealHelp(1);
            return;
        }

        String type = args[1].toLowerCase();
        if (type.equals("help")) {
            menu.sealHelp(menu.parsePage(args, 2));
            return;
        }
        if (type.matches("\\d+")) {
            menu.sealHelp(Integer.parseInt(type));
            return;
        }

        final SealPlane plane;
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "vertical-north", "vertical", "v", "xy" -> plane = SealPlane.XY;
                case "vertical-east", "ez", "yz" -> plane = SealPlane.YZ;
                case "horizontal", "h", "xz" -> plane = SealPlane.XZ;
                default -> {
                    sender.sendMessage(RED + "Unknown plane. Use: horizontal, vertical-north, vertical-east");
                    return;
                }
            }
        } else {
            plane = SealPlane.XZ;
        }

        Location center = player.getLocation();
        MagicSealListener listener = plugin.getMagicSealListener();

        switch (type) {
            case "pentagram" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnPentagramSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Pentagram Seal for 6 seconds in plane " + plane + ".");
            }
            case "triangle", "runic" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnRunicTriangleSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Runic Triangle Seal for 6 seconds in plane " + plane + ".");
            }
            case "celestial" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnCelestialSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Celestial Seal for 6 seconds in plane " + plane + ".");
            }
            case "circle" -> {
                drawSingleCircle(center, 5.0, Color.fromRGB(0xFFFF55), 200, 130, plane);
                sender.sendMessage(GOLD + "Drew single yellow circle (radius 5) in plane " + plane + ".");
            }
            case "ring" -> {
                drawSingleCircle(center, 8.0, Color.fromRGB(0x00FFFF), 280, 130, plane);
                sender.sendMessage(GOLD + "Drew single aqua ring (radius 8) in plane " + plane + ".");
            }
            case "star" -> {
                drawSixPointStar(center, 6.0, Color.WHITE, 100, 130, plane);
                sender.sendMessage(GOLD + "Drew six-point star (radius 6) in plane " + plane + ".");
            }
            case "floating", "shield" -> {
                listener.spawnFloatingShieldSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Floating Shield Seal for 6 seconds.");
            }
            case "wings" -> {
                listener.spawnWingSeal(center, player.getLocation().getYaw(), 120);
                sender.sendMessage(GOLD + "Spawned Wing Seal for 6 seconds.");
            }
            case "wings2" -> {
                listener.spawnWingSeal2(center, player.getLocation().getYaw(), 120);
                sender.sendMessage(GOLD + "Spawned Wing Seal 2 for 6 seconds.");
            }
            case "vortex" -> {
                listener.spawnVortexSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Vortex Seal for 6 seconds.");
            }
            case "quake" -> {
                listener.spawnQuakeSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Quake Seal for 6 seconds.");
            }
            case "divine" -> {
                listener.spawnDivineSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Divine Seal for 6 seconds.");
            }
            case "storm" -> {
                listener.spawnStormSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Storm Seal for 6 seconds.");
            }
            default -> menu.sealHelp(1);
        }
    }

    private interface SealTask {
        void run(ArmorStand stand, int durationTicks);
    }

    private void spawnTemporaryStandAndFire(Player player, Location center, SealTask task) {
        ArmorStand marker = player.getWorld().spawn(center, ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setMarker(true);
            entity.setCustomNameVisible(false);
        });
        marker.addScoreboardTag("MSC_SealMarker");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> task.run(marker, 120), 1L);
        plugin.getServer().getScheduler().runTaskLater(plugin, marker::remove, 130L);
    }

    private void drawSingleCircle(Location center, double radius, Color color, int samples, int ticks,
                                  SealPlane plane) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) {
                    cancel();
                    return;
                }
                double step = (2 * Math.PI) / samples;
                World world = center.getWorld();
                for (int i = 0; i < samples; i++) {
                    double a = i * step + (t * 0.05);
                    double c = radius * Math.cos(a);
                    double s = radius * Math.sin(a);
                    double x, y, z;
                    switch (plane) {
                        case XZ -> {
                            x = center.getX() + c;
                            y = center.getY() + 0.05;
                            z = center.getZ() + s;
                        }
                        case XY -> {
                            x = center.getX() + c;
                            y = center.getY() + s;
                            z = center.getZ();
                        }
                        case YZ -> {
                            x = center.getX();
                            y = center.getY() + c;
                            z = center.getZ() + s;
                        }
                        default -> {
                            x = center.getX() + c;
                            y = center.getY() + 0.05;
                            z = center.getZ() + s;
                        }
                    }
                    Location loc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.8f));
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void drawSixPointStar(Location center, double radius, Color color, int samples, int ticks,
                                  SealPlane plane) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) {
                    cancel();
                    return;
                }
                double step = (2 * Math.PI) / samples;
                World world = center.getWorld();
                for (int i = 0; i < samples; i++) {
                    double angle = i * step + (t * 0.03);
                    double r = radius * (i % (samples / 6) == 0 ? 1.0 : 0.55);
                    double x, y, z;
                    switch (plane) {
                        case XZ -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + 0.1;
                            z = center.getZ() + r * Math.sin(angle);
                        }
                        case XY -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + r * Math.sin(angle);
                            z = center.getZ();
                        }
                        case YZ -> {
                            x = center.getX();
                            y = center.getY() + r * Math.cos(angle);
                            z = center.getZ() + r * Math.sin(angle);
                        }
                        default -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + 0.1;
                            z = center.getZ() + r * Math.sin(angle);
                        }
                    }
                    Location loc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.8f));
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
