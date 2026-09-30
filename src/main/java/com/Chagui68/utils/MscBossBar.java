package com.Chagui68.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creation and viewer bookkeeping for the plugin's boss bars.
 *
 * WHY IT EXISTS
 *
 * A {@link BossBar} is a packet sent to individual players, so it is not tied to the world: whoever
 * is added at spawn keeps seeing it until it is taken away, and whoever joins later never sees it at
 * all. Every boss solved that differently — the Sentinel re-checked its viewers every tick, Jack Star
 * ran a one-second task that compared distances, and Nix and Kinger only ever added the players who
 * happened to be online the moment the boss spawned. With those rules a player who logged in later
 * fought the boss with no bar, and a player who left the world kept the bar of a fight they were no
 * longer in.
 *
 * Here the rule is one place: the bar belongs to the boss's world, and it takes a fresh look often
 * enough that a login, a logout or a world change corrects itself.
 */
public final class MscBossBar {

    private MscBossBar() {
    }

    /** A visible bar at full health; callers override the progress as the fight goes on. */
    public static BossBar create(String title, BarColor color, BarStyle style, BarFlag... flags) {
        BossBar bar = Bukkit.createBossBar(title, color, style, flags);
        bar.setProgress(1.0);
        bar.setVisible(true);
        return bar;
    }

    /** Everyone in {@code world} should see the bar, and nobody else. */
    public static void showInWorld(BossBar bar, World world) {
        if (bar == null || world == null) return;
        update(bar, world.getPlayers(), world, null, 0.0);
    }

    /** Only the players within {@code range} blocks of {@code center} should see the bar. */
    public static void showNear(BossBar bar, Location center, double range) {
        if (bar == null || center == null || center.getWorld() == null) return;
        update(bar, center.getWorld().getPlayers(), center.getWorld(), center, range * range);
    }

    private static void update(BossBar bar, Collection<? extends Player> candidates, World world,
                               Location center, double rangeSq) {
        List<Player> stale = new ArrayList<>();
        for (Player viewer : bar.getPlayers()) {
            if (!viewer.isOnline() || !stays(viewer, world, center, rangeSq)) {
                stale.add(viewer);
            }
        }
        for (Player viewer : stale) {
            bar.removePlayer(viewer);
        }
        for (Player player : candidates) {
            if (!bar.getPlayers().contains(player) && stays(player, world, center, rangeSq)) {
                bar.addPlayer(player);
            }
        }
    }

    private static boolean stays(Player player, World world, Location center, double rangeSq) {
        if (!player.getWorld().equals(world)) return false;
        return center == null || player.getLocation().distanceSquared(center) <= rangeSq;
    }
}
