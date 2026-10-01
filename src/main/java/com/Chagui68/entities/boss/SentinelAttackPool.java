package com.Chagui68.entities.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Which attack the Obsidian Sentinel throws next, as pure functions so the rotation can be tested
 * without a server.
 *
 * <p>Two problems lived in the old inline arrays. At the distance players actually fight a
 * fourteen-block boss from (five to fifteen blocks), the medium list held ten distinct attacks and
 * was used 85% of the time, so a fight looked like the same ten moves on a loop; and {@code
 * doombeam} and {@code rainoflances} were registered but listed nowhere, so they never fired.
 * Every registered attack now appears in at least one pool (a test pins that down), and
 * {@link #pick} skips the last {@link #HISTORY} attacks thrown so the rotation keeps moving.
 */
public final class SentinelAttackPool {

    /** How many of the most recent attacks are kept out of the next pick. */
    public static final int HISTORY = 6;

    /** Aerial attacks thrown before a flight may end early; the pools below hold five or more each. */
    public static final int AERIAL_ATTACKS_PER_FLIGHT = 5;

    /** Ground attacks for a player within melee reach of the boss. */
    public static final List<String> GROUND_CLOSE = List.of(
            "shieldbash", "warstomp", "chaingrapple", "armorspikes", "mirrorimage", "vortexpull",
            "groundshatter", "lanceflurry", "whirlwindslash", "executionsweep", "earthmaw",
            "shadowstep", "runeward");

    /** Ground attacks for the usual fighting distance. */
    public static final List<String> GROUND_MEDIUM = List.of(
            "lancestorm", "earthpillar", "groundshatter", "armorspikes", "vortexpull", "lanceflurry",
            "whirlwindslash", "obsidianspire", "earthmaw", "runeward", "doombeam", "chaingrapple",
            "shadowstep");

    /** Ground attacks that reach a player keeping their distance. */
    public static final List<String> GROUND_FAR = List.of(
            "shieldbash", "obsidianspire", "doombeam", "chaingrapple", "shadowstep", "earthpillar");

    /** Projectiles and beams, thrown from the ground at medium and long range. */
    public static final List<String> RANGED = List.of(
            "lancesnipe", "meteorstorm", "voidbeam", "frostlance", "lightningspear", "shadowvolley",
            "chainlightning", "crystalbarrage", "arcaneorb", "voidrift", "arcanemissiles", "spiritbeam",
            "soultethers", "plaguebrand", "runemines");

    public static final List<String> AERIAL_CLOSE = List.of(
            "aerialrush", "crossslash", "novaburst", "obsidianwings", "bladering", "rainoflances");

    public static final List<String> AERIAL_MEDIUM = List.of(
            "sonicboom", "windcutter", "gravitywell", "darkorb", "aerialrush", "eclipsefall",
            "rainoflances");

    public static final List<String> AERIAL_FAR = List.of(
            "starfall", "lightningstorm", "heavenlyjudgment", "darkorb", "eclipsefall", "rainoflances");

    private SentinelAttackPool() {
    }

    /**
     * A random attack from {@code pool} that is not in {@code recent}; the whole pool when every
     * entry was used recently. Returns null only for an empty pool.
     */
    public static String pick(List<String> pool, Collection<String> recent, Random random) {
        if (pool == null || pool.isEmpty()) return null;
        List<String> fresh = new ArrayList<>(pool.size());
        for (String name : pool) {
            if (recent == null || !recent.contains(name)) fresh.add(name);
        }
        List<String> from = fresh.isEmpty() ? pool : fresh;
        return from.get(random.nextInt(from.size()));
    }

    /** Records an attack as the most recent one, forgetting the oldest beyond {@link #HISTORY}. */
    public static void remember(Deque<String> recent, String name) {
        if (recent == null || name == null) return;
        recent.remove(name);
        recent.addLast(name);
        while (recent.size() > HISTORY) {
            recent.removeFirst();
        }
    }
}
