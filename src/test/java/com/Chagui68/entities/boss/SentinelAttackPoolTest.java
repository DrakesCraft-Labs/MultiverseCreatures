package com.Chagui68.entities.boss;

import com.Chagui68.testsupport.ProjectPaths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the Sentinel's attack rotation: every attack it owns is reachable, and it does not throw the
 * same few moves back to back.
 */
class SentinelAttackPoolTest {

    /** Attacks driven by the AI loop itself rather than drawn from a pool. */
    private static final Set<String> DRIVEN_ELSEWHERE = Set.of(
            "groundslam", "trianglecall", "airslam", "hoverbarrage", "healingcircle", "shieldseal",
            "stoneskin", "reflectbarrier", "absorbshield");

    @Test
    @DisplayName("Every registered attack is drawn from some pool or driven by the AI loop")
    void everyAttackIsReachable() {
        Set<String> pooled = new HashSet<>();
        Stream.of(SentinelAttackPool.GROUND_CLOSE, SentinelAttackPool.GROUND_MEDIUM,
                SentinelAttackPool.GROUND_FAR, SentinelAttackPool.RANGED, SentinelAttackPool.AERIAL_CLOSE,
                SentinelAttackPool.AERIAL_MEDIUM, SentinelAttackPool.AERIAL_FAR).forEach(pooled::addAll);

        Set<String> registered = registeredAttackNames();
        assertFalse(registered.isEmpty(), "no attack names were found to check");
        for (String name : registered) {
            assertTrue(pooled.contains(name) || DRIVEN_ELSEWHERE.contains(name),
                    name + " is registered but no pool ever picks it, so the Sentinel never uses it");
        }
        for (String name : pooled) {
            assertTrue(registered.contains(name), name + " is pooled but no attack answers to that name");
        }
    }

    @Test
    @DisplayName("A flight can end early: every aerial pool holds enough attacks")
    void aerialPoolsCoverAFlight() {
        for (List<String> pool : List.of(SentinelAttackPool.AERIAL_CLOSE, SentinelAttackPool.AERIAL_MEDIUM,
                SentinelAttackPool.AERIAL_FAR)) {
            assertTrue(new HashSet<>(pool).size() >= SentinelAttackPool.AERIAL_ATTACKS_PER_FLIGHT, pool.toString());
        }
    }

    @Test
    @DisplayName("Recent attacks are skipped until the pool runs out")
    void recentAttacksAreSkipped() {
        Random random = new Random(1);
        Deque<String> recent = new ArrayDeque<>();
        List<String> pool = SentinelAttackPool.GROUND_MEDIUM;
        for (int i = 0; i < 500; i++) {
            String pick = SentinelAttackPool.pick(pool, recent, random);
            assertFalse(recent.contains(pick), pick + " was thrown again within " + SentinelAttackPool.HISTORY + " attacks");
            SentinelAttackPool.remember(recent, pick);
            assertTrue(recent.size() <= SentinelAttackPool.HISTORY);
        }
    }

    @Test
    @DisplayName("A pool smaller than the history still yields an attack")
    void smallPoolsNeverRunDry() {
        Deque<String> recent = new ArrayDeque<>(List.of("a", "b"));
        assertNotNull(SentinelAttackPool.pick(List.of("a", "b"), recent, new Random(2)));
        assertNull(SentinelAttackPool.pick(List.of(), recent, new Random(2)));
    }

    /** Every {@code getName()} return value under the attack package. */
    private static Set<String> registeredAttackNames() {
        Set<String> names = new HashSet<>();
        Pattern pattern = Pattern.compile("getName\\(\\)\\s*\\{\\s*return\\s+\"([a-z]+)\"");
        for (Path file : ProjectPaths.javaFiles(ProjectPaths.source("com", "Chagui68", "entities", "boss", "attack"))) {
            Matcher matcher = pattern.matcher(ProjectPaths.read(file));
            while (matcher.find()) names.add(matcher.group(1));
        }
        return names;
    }
}
