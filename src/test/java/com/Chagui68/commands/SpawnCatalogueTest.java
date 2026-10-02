package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the {@code /msc spawn} data down.
 *
 * The alias list, the success/failure messages and the three help pages used to be three separate
 * hardcoded copies inside a 200 line switch. They now come from one table, so nothing but a test
 * stops the copies from drifting: the help text below is the literal text the command printed
 * before the split (taken from the previous compiled class), and the message expectations are the
 * exact strings the old branches produced.
 */
class SpawnCatalogueTest {

    /** Page 1 exactly as the old {@code sendSpawnHelp} printed it. */
    private static final List<String> LEGACY_PAGE_1 = List.of(
            " &e• nix &8- &cNIX - The Executioner &7(Scaffold Ritual Boss)",
            " &e• dio &8- &6DIO &7(The World — JoJo's Bizarre Adventure)",
            " &e• armorstand &8- &6The Ancient Armor Stand &7(Multiverse Boss)",
            " &e• mahoraga &8- &fMahoraga &7(Adapting Divine General)",
            " &e• garou &8- &bGarou &7(Martial Arts Miniboss)",
            " &e• kinger &8- &5Kinger &7(The Chess King)",
            " &e• disctrader &8- &dDisc Trader &7(Music & Relic Merchant)");

    private static final List<String> LEGACY_PAGE_2 = List.of(
            " &e• zombietrap &8- &cHorse Trap Trigger &7(Spawns Military Army)",
            " &e• tank &8- &aZombie Tank &7(Heavy Armor, Shield & Slam)",
            " &e• duelist &8- &dMilitary Duelist &7(Dynamic Sword/Bow Swap)",
            " &e• lancer &8- &eZombie Lancer &7(Charging Cavalry Rider)",
            " &e• camel &8- &6Siege Camels &7(Husk Cavalry & Archers)",
            " &e• sniper &8- &8Wither Sniper &7(High-velocity Bow Snipes)");

    private static final List<String> LEGACY_PAGE_3 = List.of(
            " &e• chaosmage &8- &dChaos Mage &7(Magic Bolts & Blink)",
            " &e• soulreaper &8- &cSoul Reaper &7(Scythe Life Drain)",
            " &e• enderknight &8- &5Ender Knight &7(Abyssal Void Blade)",
            " &e• obsidianguard &8- &8Obsidian Guard &7(Reinforced Shield)",
            " &e• stormcaller &8- &bStorm Caller &7(Lightning Summoner)",
            " &e• frostgolem &8- &9Frost Golem &7(Cryo Slow Aura)",
            " &e• flameelemental &8- &6Flame Elemental &7(Fire Nova)",
            " &e• shadowrogue &8- &8Shadow Rogue &7(Stealth Infiltrator)",
            " &e• voidcrawler &8- &5Void Crawler &7(End Abyss Parasite)",
            " &e• boneshield &8- &fBone Shield Skeleton &7(Arrow Defense)",
            " &e• arrowskeleton &8- &6Arquero de la Flecha &7(Stand Arrow Archer)",
            " &e• venomwitch &8- &2Venom Witch &7(Toxic Splash Potions)",
            " &e• warlord &8- &4Orcish Warlord &7(Berserker Rage)",
            " &e• creeperjr &8- &aCreeper Jr. &7(Fast Micro-Exploder)",
            " &e• headslime &8- &aHead Slime &7(Leaping Parasite)",
            " &e• merchant &8- &eMultiverse Merchant &7(Custom Trades)");

    @Test
    @DisplayName("Help pages reproduce the literal text the command used to print")
    void helpPagesMatchLegacyText() {
        assertEquals(LEGACY_PAGE_1, SpawnCatalogue.helpLines(1));
        assertEquals(LEGACY_PAGE_2, SpawnCatalogue.helpLines(2));
        assertEquals(LEGACY_PAGE_3, SpawnCatalogue.helpLines(3));
    }

    @Test
    @DisplayName("Help menu keeps its three titled pages")
    void helpMenuShape() {
        assertEquals(3, SpawnCatalogue.pages());
        assertEquals("BOSSES & APEX ENTITIES", SpawnCatalogue.pageTitle(1));
        assertEquals("MILITARY STRIKE FORCE", SpawnCatalogue.pageTitle(2));
        assertEquals("MULTIVERSE CREATURES & ELITES", SpawnCatalogue.pageTitle(3));
        assertEquals(LEGACY_PAGE_1.size() + LEGACY_PAGE_2.size() + LEGACY_PAGE_3.size(),
                SpawnCatalogue.helpLines(1).size() + SpawnCatalogue.helpLines(2).size() + SpawnCatalogue.helpLines(3).size());
    }

    @Test
    @DisplayName("Every declared alias resolves back to its own type, case-insensitively")
    void aliasesAreReachable() {
        for (SpawnCatalogue.Type type : SpawnCatalogue.types()) {
            for (String alias : type.aliases()) {
                SpawnCatalogue.Type found = SpawnCatalogue.find(alias);
                assertNotNull(found, "alias not reachable: " + alias);
                assertEquals(type.id(), found.id(), "alias " + alias + " resolved to the wrong type");
                assertEquals(type.id(), SpawnCatalogue.find(alias.toUpperCase()).id());
            }
        }
    }

    @Test
    @DisplayName("Aliases are unique across the catalogue and free of blanks")
    void aliasesAreUnique() {
        List<String> all = SpawnCatalogue.aliases();
        Set<String> distinct = new HashSet<>(all);
        assertEquals(all.size(), distinct.size(), "duplicate spawn alias in " + all);
        assertTrue(all.containsAll(distinct));
        for (String alias : all) {
            assertFalse(alias.isBlank(), "blank alias");
            assertEquals(alias.toLowerCase(), alias, "aliases stay lowercase for tab completion: " + alias);
        }
    }

    @Test
    @DisplayName("Unknown and null spawn aliases resolve to nothing")
    void unknownAliasIsNull() {
        assertNull(SpawnCatalogue.find("not-a-mob"));
        assertNull(SpawnCatalogue.find(null));
        assertNull(SpawnCatalogue.find(""));
    }

    @Test
    @DisplayName("Tab completion offers every working alias, including the legacy shortcuts")
    void completionCoversDocumentedShortcuts() {
        List<String> aliases = SpawnCatalogue.aliases();
        // The shortcut aliases the old tab-completion list simply forgot about.
        for (String alias : List.of("army", "rogue", "flame", "frost", "void", "storm", "bone", "venom",
                "obsidian", "reaper", "chaos", "ender", "armorstandboss", "jack")) {
            assertTrue(aliases.contains(alias), "missing completion alias: " + alias);
        }
    }

    @Test
    @DisplayName("The Jack boss answers to exactly one alias")
    void jackHasASingleAlias() {
        SpawnCatalogue.Type jack = SpawnCatalogue.find("jack");
        assertNotNull(jack);
        assertEquals(List.of("jack"), jack.aliases(), "Jack must be invocable through one alias only");
        // The old shortcuts are gone for good.
        for (String retired : List.of("jackstar", "arquitecto", "systemarchitect")) {
            assertNull(SpawnCatalogue.find(retired), "retired Jack alias still resolves: " + retired);
            assertFalse(SpawnCatalogue.aliases().contains(retired), "retired Jack alias still completes: " + retired);
        }
    }

    @Test
    @DisplayName("Spawn messages keep the exact wording of the old branches")
    void legacyMessages() {
        assertEquals("Spawned NIX - The Executioner!", success("nix"));
        assertEquals("Failed to spawn NIX - The Executioner.", failure("nix"));
        assertEquals("Spawned Military Zombie Horse trap!", success("zombietrap"));
        assertEquals("Failed to spawn trap.", failure("zombietrap"));
        assertEquals("Spawned Military Skeleton Duelist!", success("duelist"));
        assertEquals("Failed to spawn Duelist.", failure("duelist"));
        assertEquals("Spawned Garou [Hero Hunter]!", success("garou"));
        assertEquals("Failed to spawn Garou.", failure("garou"));
        assertEquals("Spawned JackStar — El Arquitecto del Sistema!", success("jack"));
        assertEquals("Failed to spawn JackStar Boss.", failure("jack"));
    }

    @Test
    @DisplayName("Every type can build both of its messages")
    void messagesAreAlwaysUsable() {
        for (SpawnCatalogue.Type type : SpawnCatalogue.types()) {
            assertNotNull(type.spawner());
            assertFalse(type.spawnedName().isBlank(), type.id() + " has no spawned name");
            assertFalse(type.failureName().isBlank(), type.id() + " has no failure name");
            assertFalse(success(type.id()).contains("null"));
            assertFalse(failure(type.id()).contains("null"));
        }
    }

    @Test
    @DisplayName("Spawnable-but-undocumented kinds stay out of the help menu")
    void undocumentedTypesAreNotListed() {
        SpawnCatalogue.Type jack = SpawnCatalogue.find("jack");
        assertNotNull(jack);
        assertEquals(0, jack.helpPage());
        assertFalse(jack.documented());
        List<String> listed = new ArrayList<>();
        for (int page = 1; page <= SpawnCatalogue.pages(); page++) {
            listed.addAll(SpawnCatalogue.helpLines(page));
        }
        assertTrue(listed.stream().noneMatch(line -> line.startsWith(" &e• jack")));
    }

    private static String success(String alias) {
        return "Spawned " + SpawnCatalogue.find(alias).spawnedName() + "!";
    }

    private static String failure(String alias) {
        return "Failed to spawn " + SpawnCatalogue.find(alias).failureName() + ".";
    }

    @Nested
    @DisplayName("Documented types")
    class Documented {

        @Test
        @DisplayName("Every documented type is listed on exactly one of its pages")
        void eachDocumentedTypeAppearsOnce() {
            List<String> listed = new ArrayList<>();
            for (int page = 1; page <= SpawnCatalogue.pages(); page++) {
                listed.addAll(SpawnCatalogue.helpLines(page));
            }
            for (SpawnCatalogue.Type type : SpawnCatalogue.types()) {
                long hits = listed.stream().filter(line -> line.startsWith(" &e• " + type.id() + " ")).count();
                assertEquals(type.documented() ? 1 : 0, hits, type.id() + " is listed " + hits + " times");
            }
        }

        @Test
        @DisplayName("First alias is the one the help and the completion list show")
        void primaryAliasMatchesHelp() {
            for (SpawnCatalogue.Type type : SpawnCatalogue.types()) {
                assertEquals(type.id(), type.aliases().get(0));
            }
        }
    }
}
