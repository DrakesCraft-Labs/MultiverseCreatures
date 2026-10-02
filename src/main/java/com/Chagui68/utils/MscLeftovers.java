package com.Chagui68.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Set;

/**
 * Cleans up the displays a boss attack left behind.
 *
 * WHY IT EXISTS
 *
 * An attack spawns its props — the Sentinel's orbiting shields, its planted shield holder, the ring
 * of lances, the wing panels, the mirror copies, the triangle seal — and removes them when the attack
 * ends. If the server stops mid-fight, nothing removes them: the attack object is gone, so the props
 * stay in the world as frozen litter that no command points at and no boss owns. Two of them were
 * even written with {@code setPersistent(true)}, so the world saved them and they came back on every
 * start.
 *
 * The sweep runs once, at enable, before any boss can be mid-attack: at that moment a tag from this
 * list can only belong to a fight that is over.
 *
 * What is deliberately NOT in the list: the mob tags ({@code MSC_FrostGolem} and friends), which are
 * creatures with a life of their own; the suit pieces ({@code MSC_KingerPart}, {@code MSC_NixPart},
 * {@code MSC_JackPart}), which each boss adopts or removes on enable; and the things a player places
 * by hand ({@code MSC_Dummy}, {@code MSC_SealMarker}), which are supposed to survive a restart.
 */
public final class MscLeftovers {

    /** Tags that only ever belong to a running attack. */
    public static final Set<String> ATTACK_TAGS = Set.of(
            "MSC_BladeRing",
            "MSC_ObsidianWings",
            "MSC_ShieldSealOrbit",
            "MSC_ShieldHolder",
            "MSC_TriangleSeal",
            "MSC_BossMirror",
            "MSC_KingerBullet",
            com.Chagui68.entities.boss.fx.LiveStage.PROP_TAG);

    private MscLeftovers() {
    }

    /** Whether these tags mark a visual whose attack no longer exists. */
    public static boolean isAttackLeftover(Collection<String> scoreboardTags) {
        for (String tag : scoreboardTags) {
            if (ATTACK_TAGS.contains(tag)) return true;
        }
        return false;
    }

    /** Removes every leftover attack visual in {@code world} and reports how many went. */
    public static int sweep(World world) {
        if (world == null) return 0;
        int removed = 0;
        for (Entity entity : world.getEntities()) {
            if (isAttackLeftover(entity.getScoreboardTags())) {
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    /** Sweeps every loaded world. */
    public static int sweepAll() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            removed += sweep(world);
        }
        return removed;
    }
}
