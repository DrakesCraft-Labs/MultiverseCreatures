package com.Chagui68.entities.boss;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.List;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.BossInstance;

/**
 * What an attack needs from its boss.
 *
 * WHY IT EXISTS
 *
 * The 42 plugin attacks were written against ArmorStandBoss, the concrete Obsidian Sentinel
 * class. They work perfectly, but depending on the concrete type prevents any other boss
 * from reusing them: to reuse an attack you had to copy it and change the type, which is
 * how two copies of the same code diverge.
 *
 * With this interface in the middle, any boss implementing it reuses the 42 attacks as-is.
 * The goal is for all server bosses to share the same attack logic and only differ in
 * values and presentation.
 *
 * WHAT BELONGS HERE AND WHAT DOES NOT
 *
 * Only generic concerns live here: who is nearby, where the ground is, how to push a player,
 * how to reset the pose. Sentinel-specific concerns — sky pentagram, netherite spear,
 * shield timings — stay in ArmorStandBoss, because a different boss may not need them.
 */
public interface BossHost {

    /** The plugin, for scheduling tasks and reading config. */
    MultiverseCreatures getPlugin();

    /** Resets the boss to its idle pose after an attack. */
    void resetBossPose(BossInstance instance);

    /** Players in the world this boss can attack. */
    default List<Player> getValidPlayers(World world) {
        return BossArena.getValidPlayers(world);
    }

    /** Valid players within a radius (squared). */
    default List<Player> getValidPlayersNear(Location center, double radiusSq) {
        return BossArena.getValidPlayersNear(center, radiusSq);
    }

    /** Launches a player upward. */
    default void launchPlayer(Player p, double y) {
        BossArena.launchPlayer(p, y);
    }

    /** Ground height under a location. */
    default double getGroundY(Location loc, double maxScan) {
        return BossArena.getGroundY(loc, maxScan);
    }

    /** Whether the boss entity is on ground. */
    default boolean isOnGround(BossPuppet stand) {
        return BossArena.isOnGround(stand);
    }

    /** How many valid players are in range. */
    default int countPlayersInRange(Location center, double radius) {
        return BossArena.countPlayersInRange(center, radius);
    }

    /** Nearest valid player, or null. */
    default Player findNearestPlayer(Location center, double range) {
        return BossArena.findNearestPlayer(center, range);
    }

    /** Boss target based on aggro range, or null. */
    Player detectTarget(BossPuppet stand);

    /** Shockwave from a point. Shared by several ground attacks. */
    void spawnShockwaveWave(World world, Location center, double maxRadius);

    /**
     * Seal and aerial barrage damage.
     *
     * Defaults are provided because a new boss may not use either of the attacks
     * that query them; the Sentinel overrides them with its config values.
     */
    default double getSealDamage() {
        return 6.0;
    }

    default double getHoverBarrageDamage() {
        return 4.0;
    }
}
