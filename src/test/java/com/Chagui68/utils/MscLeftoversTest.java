package com.Chagui68.utils;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the startup sweep against two opposite mistakes: leaving an attack's props behind, and
 * deleting something that is supposed to stay — a creature, a boss's suit piece, or a marker a player
 * placed on purpose. The list of tags is therefore tested from both sides.
 */
class MscLeftoversTest {

    @Test
    @DisplayName("Props of an attack nobody is running any more are leftovers")
    void attackPropsAreLeftovers() {
        for (String tag : MscLeftovers.ATTACK_TAGS) {
            assertTrue(MscLeftovers.isAttackLeftover(Set.of(tag)), tag + " must be swept");
        }
        assertTrue(MscLeftovers.isAttackLeftover(Set.of("some_other_plugin_tag", "MSC_ShieldHolder")));
    }

    @Test
    @DisplayName("Creatures, suit pieces and hand-placed markers are never swept")
    void thingsWithALifeOfTheirOwnAreKept() {
        for (String tag : List.of("MSC_FrostGolem", "MSC_HeadSlime", "MSC_Warlord", "MSC_ArmorBossSummoned",
                "MSC_KingerPart", "MSC_NixPart", "MSC_JackPart", "MSC_Kinger", "MSC_ArmorStandBoss",
                "MSC_Dummy", "MSC_SealMarker")) {
            assertFalse(MscLeftovers.isAttackLeftover(Set.of(tag)),
                    tag + " has an owner or was placed by hand and must survive a restart");
            assertFalse(MscLeftovers.ATTACK_TAGS.contains(tag), tag + " must not be on the sweep list");
        }
        assertFalse(MscLeftovers.isAttackLeftover(Set.of()));
    }

    @Test
    @DisplayName("The sweep removes the leftovers and counts them, leaving everything else alone")
    void theSweepOnlyTakesLeftovers() {
        List<Entity> entities = new ArrayList<>();
        List<Entity> removed = new ArrayList<>();
        entities.add(entity(removed, "MSC_FrostGolem"));
        entities.add(entity(removed, "MSC_ShieldHolder"));
        entities.add(entity(removed, "MSC_KingerPart", "MSC_KingerOwner_abc"));
        entities.add(entity(removed, "MSC_BladeRing", "another_tag"));
        entities.add(entity(removed, "MSC_Dummy"));

        int swept = MscLeftovers.sweep(world(entities));

        assertEquals(2, swept, "the shield holder and the blade ring are the only leftovers");
        assertEquals(2, removed.size());
        assertEquals(5, entities.size(), "the sweep must not unload the world around it");
    }

    @Test
    @DisplayName("A missing world is ignored instead of throwing")
    void missingWorldIsIgnored() {
        assertEquals(0, MscLeftovers.sweep(null));
    }

    // --- fakes -------------------------------------------------------------------------------------

    private static World world(List<Entity> entities) {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getEntities" -> new ArrayList<>(entities);
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "world";
                    default -> null;
                });
    }

    private static Entity entity(List<Entity> removed, String... tags) {
        return (Entity) Proxy.newProxyInstance(Entity.class.getClassLoader(), new Class<?>[]{Entity.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScoreboardTags" -> Set.of(tags);
                    case "remove" -> {
                        removed.add((Entity) proxy);
                        yield null;
                    }
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> String.join("+", tags);
                    default -> null;
                });
    }
}
