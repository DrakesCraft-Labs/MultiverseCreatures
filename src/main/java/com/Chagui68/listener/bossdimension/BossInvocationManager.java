package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ritual.BossDimensionManager;
import com.Chagui68.ritual.BossInvocationStructure;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossInvocationManager implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, InvocationData> activeInvocations = new HashMap<>();

    public BossInvocationManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.RED_CANDLE) return;

        World world = block.getWorld();
        BossDimensionManager dim = plugin.getBossDimensionManager();
        if (dim == null || dim.getBossWorld() == null || !world.equals(dim.getBossWorld())) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location candleLoc = block.getLocation();
            for (int ox = -4; ox <= 0; ox++) {
                for (int oz = -4; oz <= 0; oz++) {
                    Location origin = candleLoc.clone().add(ox, 0, oz);
                    if (!BossInvocationStructure.isStructureComplete(origin)) continue;
                    if (!BossInvocationStructure.containsCandle(origin, candleLoc)) continue;
                    if (activeInvocations.containsKey(origin.getWorld().getUID())) continue;
                    if (!BossInvocationStructure.areAllCandlesLit(origin)) continue;

                    startInvocation(origin);
                    return;
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.ECHO_SHARD) return;

        InvocationData data = activeInvocations.get(event.getPlayer().getWorld().getUID());
        if (data == null) return;

        Location dropLoc = event.getItemDrop().getLocation();
        Location center = BossInvocationStructure.getCenterLocation(data.origin);
        double radius = BossInvocationStructure.getRadius();

        if (dropLoc.distance(center) > radius) return;
        if (Math.abs(dropLoc.getY() - data.origin.getY()) > 3) return;

        event.getItemDrop().remove();
        event.getPlayer().sendMessage(ChatColor.GOLD + "The Echo Shard is consumed... The boss awakens!");

        stopInvocation(data.origin.getWorld());
        BossInvocationStructure.extinguishAllCandles(data.origin);

        com.Chagui68.entities.boss.ArmorStandBoss boss = plugin.getArmorStandBoss();
        if (boss != null) {
            boss.trySpawn(center);
        }
    }

    private void startInvocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!BossInvocationStructure.areAllCandlesLit(origin)
                        || !BossInvocationStructure.isStructureComplete(origin)) {
                    stopInvocation(world);
                    return;
                }
                spawnPentagramFlame(world, origin);
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        activeInvocations.put(world.getUID(), new InvocationData(origin, task));
        plugin.getLogger().info("Boss invocation started at " + origin);
    }

    private void stopInvocation(World world) {
        InvocationData data = activeInvocations.remove(world.getUID());
        if (data != null && data.task != null) {
            data.task.cancel();
        }
    }

    private void spawnPentagramFlame(World world, Location origin) {
        double cx = 2.5, cz = 2.5, r = 2.3;
        Location[] pts = new Location[5];
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(i * 72 - 90);
            pts[i] = new Location(world,
                    origin.getX() + cx + r * Math.cos(a),
                    origin.getY(),
                    origin.getZ() + cz + r * Math.sin(a));
        }
        drawFireLine(world, pts[0], pts[2], 20);
        drawFireLine(world, pts[2], pts[4], 20);
        drawFireLine(world, pts[4], pts[1], 20);
        drawFireLine(world, pts[1], pts[3], 20);
        drawFireLine(world, pts[3], pts[0], 20);
    }

    private void drawFireLine(World world, Location from, Location to, int samples) {
        for (int i = 0; i <= samples; i++) {
            double t = (double) i / samples;
            double x = from.getX() + (to.getX() - from.getX()) * t;
            double y = from.getY() + (to.getY() - from.getY()) * t;
            double z = from.getZ() + (to.getZ() - from.getZ()) * t;
            world.spawnParticle(Particle.FLAME, new Location(world, x, y, z), 1, 0, 0, 0, 0);
        }
    }

    private static class InvocationData {
        final Location origin;
        final BukkitRunnable task;

        InvocationData(Location origin, BukkitRunnable task) {
            this.origin = origin;
            this.task = task;
        }
    }
}
