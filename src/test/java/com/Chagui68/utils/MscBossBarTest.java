package com.Chagui68.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards who sees a boss bar.
 *
 * <p>A bar is a packet per player, so the plugin has to answer one question repeatedly: which players
 * should have it right now? The old code answered it once, at spawn, which is why a player who joined
 * later never saw the bar and a player who left the world kept it. These tests drive the real
 * bookkeeping over fake players, worlds and bars, so the answer cannot drift back to "whoever was
 * online at the start".
 */
class MscBossBarTest {

    private int adds;
    private int removes;

    private final List<Player> overworldPlayers = new ArrayList<>();
    private final List<Player> netherPlayers = new ArrayList<>();
    private final World overworld = world("world", overworldPlayers);
    private final World nether = world("world_nether", netherPlayers);

    @Test
    @DisplayName("A player who logs in later gets the bar, and nobody is added twice")
    void latecomersArePickedUp() {
        Set<Player> shown = new LinkedHashSet<>();
        BossBar bar = bar(shown);

        Player already = player(overworld, 0, true);
        shown.add(already);
        Player latecomer = player(overworld, 5, true);
        overworldPlayers.addAll(List.of(already, latecomer));

        MscBossBar.showInWorld(bar, overworld);

        assertEquals(Set.of(already, latecomer), shown, "the latecomer must be wearing the bar too");
        assertEquals(1, adds, "the player who already had the bar must not be added again");
    }

    @Test
    @DisplayName("A viewer who logs out or leaves the world stops seeing the bar")
    void strayViewersAreDropped() {
        Set<Player> shown = new LinkedHashSet<>();
        BossBar bar = bar(shown);

        Player offline = player(overworld, 0, false);
        Player elsewhere = player(nether, 0, true);
        Player staying = player(overworld, 0, true);
        shown.add(offline);
        shown.add(elsewhere);
        shown.add(staying);
        overworldPlayers.add(staying);

        MscBossBar.showInWorld(bar, overworld);

        assertEquals(Set.of(staying), shown,
                "a logged-out player and one in another world must not keep a bar for a fight they left");
    }

    @Test
    @DisplayName("A distance-limited bar only follows the players inside it")
    void distanceLimitedBarsFollowTheRange() {
        Set<Player> shown = new LinkedHashSet<>();
        BossBar bar = bar(shown);

        Player near = player(overworld, 8, true);
        Player far = player(overworld, 40, true);
        Player otherWorld = player(nether, 0, true);
        shown.add(far);
        shown.add(otherWorld);
        overworldPlayers.addAll(List.of(near, far));

        MscBossBar.showNear(bar, new Location(overworld, 0, 0, 0), 10.0);

        assertEquals(Set.of(near), shown, "only the player within range should keep the bar");

        // Handing the same bar to another world has to swap its viewers, not add to them.
        netherPlayers.add(otherWorld);
        MscBossBar.showNear(bar, new Location(nether, 0, 0, 0), 10.0);
        assertEquals(Set.of(otherWorld), shown);
    }

    @Test
    @DisplayName("A missing bar or world is ignored instead of throwing")
    void missingArgumentsAreIgnored() {
        BossBar bar = bar(new LinkedHashSet<>());

        assertDoesNotThrow(() -> MscBossBar.showInWorld(null, overworld));
        assertDoesNotThrow(() -> MscBossBar.showInWorld(bar, null));
        assertDoesNotThrow(() -> MscBossBar.showNear(null, new Location(overworld, 0, 0, 0), 5));
        assertDoesNotThrow(() -> MscBossBar.showNear(bar, null, 5));
        assertDoesNotThrow(() -> MscBossBar.showNear(bar, new Location(null, 0, 0, 0), 5));

        assertEquals(0, adds);
        assertEquals(0, removes);
    }

    // --- fakes -------------------------------------------------------------------------------------

    private static World world(String name, List<Player> players) {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getPlayers" -> players;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> name;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Player player(World world, double x, boolean online) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isOnline" -> online;
                    case "getWorld" -> world;
                    case "getLocation" -> new Location(world, x, 0, 0);
                    case "getName" -> "player@" + x;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "player@" + x;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private BossBar bar(Set<Player> viewers) {
        return (BossBar) Proxy.newProxyInstance(BossBar.class.getClassLoader(), new Class<?>[]{BossBar.class},
                (proxy, method, args) -> switch (method.getName()) {
                    // The real API hands out a List, and it is only ever read or searched here.
                    case "getPlayers" -> new ArrayList<>(viewers);
                    case "addPlayer" -> {
                        adds++;
                        viewers.add((Player) args[0]);
                        yield null;
                    }
                    case "removePlayer" -> {
                        removes++;
                        viewers.remove((Player) args[0]);
                        yield null;
                    }
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "bar";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == long.class) return 0L;
        return null;
    }
}
