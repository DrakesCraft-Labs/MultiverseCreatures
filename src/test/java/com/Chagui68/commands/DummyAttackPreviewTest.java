package com.Chagui68.commands;

import com.Chagui68.entities.boss.AttackPreview;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The dummy's attack preview: {@code /msc dummy attack <attack|random|list>}.
 *
 * <p>What the dummy plays is the Sentinel's own attack object, so this suite covers the two ends
 * that are testable without a server: the names the dummy accepts (which come from the same
 * catalogue {@link AttackCatalogueTest} pins, and from the same registry
 * {@code AttackRegistryCoherenceTest} pins) and the promise that a dummy acting out an attack cannot
 * damage anyone.
 */
class DummyAttackPreviewTest {

    @Test
    @DisplayName("Every documented attack is one the dummy can perform, case-insensitively")
    void everyDocumentedAttackIsPreviewable() {
        List<String> names = AttackCatalogue.names();
        assertFalse(names.isEmpty(), "the catalogue must document the attacks the dummy can run");

        for (String name : names) {
            assertEquals(name, DummyStudio.attackFor(name, names, new Random(1)),
                    name + " is documented but the dummy refuses it");
            assertEquals(name, DummyStudio.attackFor(name.toUpperCase(Locale.ROOT), names, new Random(1)),
                    name + " must be accepted in any case");
        }
    }

    @Test
    @DisplayName("An unknown name is refused instead of quietly running something else")
    void unknownNamesAreRefused() {
        List<String> names = AttackCatalogue.names();
        assertNull(DummyStudio.attackFor("not-an-attack", names, new Random(1)));
        assertNull(DummyStudio.attackFor(null, names, new Random(1)));
        assertNull(DummyStudio.attackFor("random", List.of(), new Random(1)),
                "an empty catalogue must not throw out of the random pick");
    }

    @Test
    @DisplayName("random picks a documented attack, and picks it reproducibly")
    void randomPicksFromTheCatalogue() {
        List<String> names = AttackCatalogue.names();
        String picked = DummyStudio.attackFor("random", names, new Random(7));

        assertNotNull(picked);
        assertTrue(names.contains(picked), picked + " is not a documented attack");
        assertEquals(picked, DummyStudio.attackFor("RANDOM", names, new Random(7)),
                "the same seed must pick the same attack");
    }

    @Test
    @DisplayName("Tab completion offers the attacks plus the two keywords")
    void completionsCoverTheAttacks() {
        List<String> completions = DummyStudio.attackCompletions();
        assertTrue(completions.contains("random"));
        assertTrue(completions.contains("list"));
        assertTrue(completions.containsAll(AttackCatalogue.names()),
                "every attack the dummy runs must be offered by completion");
        assertTrue(DummyStudio.ACTIONS.contains("attack"), "the dummy must advertise the action");
    }

    @Test
    @DisplayName("The dummy help has a section for the preview")
    void helpDocumentsThePreview() {
        String help = String.join("\n", DummyStudio.helpLines());
        assertTrue(help.contains("/msc dummy attack <attack|random>"), help);
        assertTrue(help.contains("/msc dummy attack list"), help);
    }

    @Test
    @DisplayName("Every preview page lists exactly the attacks documented on it")
    void everyPreviewPageRenders() {
        List<String> sent = new ArrayList<>();
        CommandMenu menu = new CommandMenu(recordingSender(sent));

        for (int page = 1; page <= AttackCatalogue.pages(); page++) {
            sent.clear();
            menu.dummyAttackHelp(page);
            String plain = String.join("\n", sent).replaceAll("\u00a7.", "");

            assertTrue(plain.contains("MSC DUMMY - ATTACK PREVIEW"), "page " + page + " has no header");
            assertTrue(plain.contains("Page " + page + "/" + AttackCatalogue.pages()),
                    "page " + page + " has no page indicator: " + plain);
            for (AttackCatalogue.Entry entry : AttackCatalogue.entries()) {
                if (entry.page() != page) continue;
                assertTrue(plain.contains(entry.name()), entry.name() + " is missing from preview page " + page);
            }
        }
    }

    @Test
    @DisplayName("A dummy acting out an attack cannot damage anyone")
    void previewDummyCannotDamage() {
        List<String> damage = new ArrayList<>();
        LivingEntity victim = entity(Set.of(), damage);
        LivingEntity dummy = entity(Set.of(AttackPreview.TAG), new ArrayList<>());

        // The refusal is the first line of damageBy, so the hit never even builds a DamageSource.
        // The opposite case (a real boss still damages) cannot run headless: DamageType.GENERIC only
        // resolves against a live registry.
        assertDoesNotThrow(() -> MscEntityUtils.damageBy(dummy, victim, 12.0));
        assertTrue(damage.isEmpty(), "a preview must play without anyone losing health");
    }

    @Test
    @DisplayName("Only the tagged dummy counts as an attacker in preview")
    void onlyTheDummyIsAPreviewActor() {
        assertTrue(AttackPreview.isActor(entity(Set.of(AttackPreview.TAG), new ArrayList<>())));
        assertFalse(AttackPreview.isActor(entity(Set.of("MSC_NixBoss"), new ArrayList<>())));
        assertFalse(AttackPreview.isActor(null));
    }

    // ------------------------------------------------------------------ helpers

    private static CommandSender recordingSender(List<String> sent) {
        return (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage") && args != null && args.length == 1
                            && args[0] instanceof String line) {
                        sent.add(line);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static LivingEntity entity(Set<String> tags, List<String> damageCalls) {
        return (LivingEntity) Proxy.newProxyInstance(LivingEntity.class.getClassLoader(),
                new Class<?>[]{LivingEntity.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getScoreboardTags" -> {
                            return new HashSet<>(tags);
                        }
                        case "damage" -> {
                            damageCalls.add(String.valueOf(args[0]));
                            return null;
                        }
                        case "toString" -> {
                            return "proxy";
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        default -> {
                            return defaultValue(method.getReturnType());
                        }
                    }
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == void.class) return null;
        if (type == char.class) return (char) 0;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == long.class) return 0L;
        return 0;
    }
}
