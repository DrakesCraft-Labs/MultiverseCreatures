package com.Chagui68.commands;

import com.Chagui68.testsupport.ProjectPaths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps the five places an attack is declared in agreement.
 *
 * Adding an attack means touching five lists that cannot see each other: the class itself, the
 * {@code registerAttack} block in {@code ArmorStandBoss}, the aerial/ground name sets that gate the
 * {@code /msc attack} command, the random rotation arrays, and {@link AttackCatalogue}. Forgetting one
 * of them fails silently — an unregistered attack simply never runs, and one that is registered but
 * missing from the catalogue cannot be discovered by players — so this guard reads the sources and
 * checks the contract instead of trusting it.
 */
class AttackRegistryCoherenceTest {

    private static final Path BOSS_SOURCE = ProjectPaths.source(
            "com", "Chagui68", "entities", "boss", "ArmorStandBoss.java");
    private static final Path ATTACK_SOURCES = ProjectPaths.source(
            "com", "Chagui68", "entities", "boss", "attack");

    private static final List<String> CATEGORY_FOLDERS = List.of("ground", "aerial", "ranged", "defensive");

    /** {@code getName()} literals look like this and nothing else does inside an attack class. */
    private static final Pattern ATTACK_NAME = Pattern.compile("return\\s+\"([a-z]+)\"\\s*;");

    /** Class names whose attack name is not just the class name without {@code Attack}. */
    private static final Map<String, String> NAME_EXCEPTIONS = Map.of(
            "ExecutionerSweepAttack", "executionsweep",
            "SoulTetherAttack", "soultethers",
            "RuneMineAttack", "runemines");

    /** One attack source file: where it lives and the name it answers to. */
    private record AttackFile(String className, String folder, String attackName) {}

    // ------------------------------------------------------------------ catalogue

    @Test
    @DisplayName("Every attack class is registered, categorised where it belongs and documented")
    void registryAgreesWithTheAttackSources() {
        List<AttackFile> attacks = attackSources();
        assertFalse(attacks.isEmpty(), "no attack sources found under " + ATTACK_SOURCES);

        String boss = read(BOSS_SOURCE);
        Set<String> registered = new LinkedHashSet<>();
        for (AttackFile attack : attacks) {
            assertTrue(boss.contains("new " + attack.className() + "(this)"),
                    attack.className() + " is never registered in initAttacks()");

            String derived = NAME_EXCEPTIONS.getOrDefault(attack.className(),
                    attack.className().replace("Attack", "").toLowerCase());
            assertEquals(derived, attack.attackName(),
                    attack.className() + " answers to a name that no longer matches its class");
            assertTrue(registered.add(attack.attackName()),
                    "two attacks share the name " + attack.attackName());
        }

        assertEquals(attacks.size(), registered.size());
        Set<String> catalogue = new LinkedHashSet<>(AttackCatalogue.names());
        assertEquals(catalogue, registered,
                "the help catalogue and the attack sources disagree:\n  only in catalogue: "
                        + difference(catalogue, registered) + "\n  only in sources: " + difference(registered, catalogue));
    }

    @Test
    @DisplayName("Aerial and ground sets cover their folders and leave the other attacks alone")
    void categorySetsMatchTheFolders() {
        String boss = read(BOSS_SOURCE);
        Set<String> aerial = setLiteral(boss, "AERIAL_ATTACK_NAMES");
        Set<String> ground = setLiteral(boss, "GROUND_ATTACK_NAMES");

        assertTrue(aerial.stream().noneMatch(ground::contains), "an attack sits in both category sets");

        for (AttackFile attack : attackSources()) {
            boolean expectedAerial = attack.folder().equals("aerial");
            boolean expectedGround = attack.folder().equals("ground");
            assertEquals(expectedAerial, aerial.contains(attack.attackName()),
                    attack.attackName() + " (" + attack.folder() + ") is misplaced in AERIAL_ATTACK_NAMES");
            assertEquals(expectedGround, ground.contains(attack.attackName()),
                    attack.attackName() + " (" + attack.folder() + ") is misplaced in GROUND_ATTACK_NAMES");
        }
    }

    @Test
    @DisplayName("The random rotations only ever draw registered attacks")
    void rotationsOnlyUseRegisteredAttacks() {
        String boss = read(BOSS_SOURCE);
        int from = boss.indexOf("private void executeRandomAerialAttack");
        int to = boss.indexOf("public void airSlam");
        assertTrue(from > 0 && to > from, "the random rotation methods moved; update this guard");

        Set<String> registered = new LinkedHashSet<>();
        for (AttackFile attack : attackSources()) registered.add(attack.attackName());

        Matcher literals = Pattern.compile("\"([a-z]+)\"").matcher(boss.substring(from, to));
        List<String> checked = new ArrayList<>();
        while (literals.find()) checked.add(literals.group(1));

        assertFalse(checked.isEmpty(), "no attack names found in the rotation arrays");
        for (String name : checked) {
            assertTrue(registered.contains(name),
                    "the rotation arrays still ask for " + name + ", which is not a registered attack");
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Every attack source file with the name it reports from {@code getName()}. */
    private static List<AttackFile> attackSources() {
        List<AttackFile> attacks = new ArrayList<>();
        for (String folder : CATEGORY_FOLDERS) {
            Path directory = ATTACK_SOURCES.resolve(folder);
            List<Path> files;
            try (Stream<Path> stream = Files.list(directory)) {
                files = stream.filter(path -> path.toString().endsWith(".java")).sorted().toList();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            for (Path file : files) {
                String source = read(file);
                Matcher matcher = ATTACK_NAME.matcher(source);
                assertTrue(matcher.find(), file + " has no getName() literal");
                attacks.add(new AttackFile(file.getFileName().toString().replace(".java", ""),
                        folder, matcher.group(1)));
            }
        }
        return attacks;
    }

    /** The quoted names inside one {@code java.util.Set.of(...)} constant. */
    private static Set<String> setLiteral(String source, String constant) {
        Matcher block = Pattern.compile(constant + "\\s*=\\s*java\\.util\\.Set\\.of\\((.*?)\\);",
                Pattern.DOTALL).matcher(source);
        assertTrue(block.find(), constant + " is gone from ArmorStandBoss");
        Set<String> names = new LinkedHashSet<>();
        Matcher literals = Pattern.compile("\"([a-z]+)\"").matcher(block.group(1));
        while (literals.find()) names.add(literals.group(1));
        assertFalse(names.isEmpty(), constant + " is empty");
        return names;
    }

    private static Set<String> difference(Set<String> left, Set<String> right) {
        Set<String> only = new LinkedHashSet<>(left);
        only.removeAll(right);
        return only;
    }

    private static String read(Path path) {
        return ProjectPaths.read(path);
    }
}
