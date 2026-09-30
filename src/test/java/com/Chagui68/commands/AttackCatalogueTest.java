package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the {@code /msc attack} help table down.
 *
 * The attack list existed twice: once as the four help pages and once as the tab-completion list,
 * neither of them derived from the boss's attack registry. Both now come from one table, so the
 * text is compared against what the command printed before, and the completion names are checked to
 * be a clean superset of the old suggestion list.
 *
 * <p>Page 1 also gained the three ground attacks that only the wiki documented
 * ({@code lanceflurry}, {@code whirlwindslash}, {@code executionsweep}), so the help menu now
 * advertises all 55 registered attacks: the legacy pages plus the ten attacks of the second wave
 * (four ground, three aerial, three ranged) appended to the page they belong to.
 */
class AttackCatalogueTest {

    /**
     * Page 1: the legacy text plus the three ground attacks the wiki documented but the help menu
     * never listed ({@code lanceflurry}, {@code whirlwindslash}, {@code executionsweep}).
     */
    private static final List<String> LEGACY_PAGE_1 = List.of(
            " &e• groundslam &8- &7Earth-shattering seismic leap impact",
            " &e• groundshatter &8- &7Fissure wave that fractures the terrain",
            " &e• shieldbash &8- &7Forward heavy rush that stuns targets",
            " &e• lancestorm &8- &7Piercing lance barrage across the ground",
            " &e• earthpillar &8- &7Stone pillars erupting from beneath foes",
            " &e• chaingrapple &8- &7Iron chain hook pulling players in",
            " &e• warstomp &8- &7Massive area shockwave knocking entities back",
            " &e• armorspikes &8- &7Defensive spike retribution burst",
            " &e• vortexpull &8- &7Gravitational vortex dragging entities to center",
            " &e• mirrorimage &8- &7Illusionary decoys to confuse adversaries",
            " &e• doombeam &8- &7Focused demonic ground laser sweep",
            " &e• lanceflurry &8- &7Three rapid lance thrusts in a frontal cone",
            " &e• whirlwindslash &8- &7Spinning sweep that drags foes in, then a finishing cut",
            " &e• executionsweep &8- &7Devastating wide-arc executioner strike",
            " &e• obsidianspire &8- &7Line of volcanic pillars shattering the ground ahead",
            " &e• earthmaw &8- &7Stone jaws closing on everything in front",
            " &e• shadowstep &8- &7Vanishes through a sigil to strike from behind",
            " &e• runeward &8- &7Plants a pulsing rune ward that outlives the cast");

    private static final List<String> LEGACY_PAGE_2 = List.of(
            " &e• starfall &8- &7Calling celestial stars crashing down",
            " &e• aerialrush &8- &7High-speed aerial homing strike",
            " &e• sonicboom &8- &7Acoustic blast wave penetrating defenses",
            " &e• lightningstorm &8- &7Summoning consecutive lightning strikes",
            " &e• gravitywell &8- &7Aerial singularity pulling upwards",
            " &e• crossslash &8- &7Dual aerial sword cleave in cross shape",
            " &e• novaburst &8- &7Explosive radiant detonation in midair",
            " &e• darkorb &8- &7Floating orb radiating darkness damage",
            " &e• windcutter &8- &7Slicing razor-wind blades",
            " &e• heavenlyjudgment &8- &7Holy orbital beam strike",
            " &e• rainoflances &8- &7Shower of holy lances from the sky",
            " &e• airslam &8- &7Sky-dive slam pulverizing the landing zone",
            " &e• hoverbarrage &8- &7Levitating volley of energy projectiles",
            " &e• eclipsefall &8- &7Black eclipse disc dropped on the landing zone",
            " &e• bladering &8- &7Orbiting lance ring fired out one by one",
            " &e• obsidianwings &8- &7Wing beats sweeping obsidian shards outward");

    private static final List<String> LEGACY_PAGE_3 = List.of(
            " &e• lancesnipe &8- &7High-velocity sniper lance projectile",
            " &e• meteorstorm &8- &7Shower of flaming meteorites",
            " &e• voidbeam &8- &7Linear void disintegration laser",
            " &e• frostlance &8- &7Piercing glacial spear inflicting deep freeze",
            " &e• lightningspear &8- &7Electrified javelin shocking targets",
            " &e• shadowvolley &8- &7Multi-directional flurry of dark arrows",
            " &e• chainlightning &8- &7Electric arc bouncing between nearby players",
            " &e• crystalbarrage &8- &7Rapid crystal shards barrage",
            " &e• arcaneorb &8- &7Pulsing magical sphere of pure arcane power",
            " &e• voidrift &8- &7Dimensional tear distorting spacetime",
            " &e• arcanemissiles &8- &7Homing arcane bolts seeking players",
            " &e• spiritbeam &8- &7Piercing spectral light beam",
            " &e• soultethers &8- &7Visible tethers hook players and reel them in",
            " &e• plaguebrand &8- &7Brands a player with a plague that spreads",
            " &e• runemines &8- &7Scatters armed runes that burst when stepped on");

    private static final List<String> LEGACY_PAGE_4 = List.of(
            " &e• stoneskin &8- &7Hardens boss defense, reducing all damage",
            " &e• reflectbarrier &8- &7Prismatic shield reflecting projectiles",
            " &e• absorbshield &8- &7Barrier converting incoming damage into healing",
            " &e• shieldseal &8- &7Protective ancient ward preventing melee strikes",
            " &e• healingcircle &8- &7Radiant circle regenerating boss vitality",
            " &e• trianglecall &8- &7Sacred geometric barrier summoning reinforcements");

    @Test
    @DisplayName("Help pages match the 55 registered attacks, pages 2-4 byte-for-byte")
    void helpPagesMatchLegacyText() {
        assertEquals(LEGACY_PAGE_1, AttackCatalogue.helpLines(1));
        assertEquals(LEGACY_PAGE_2, AttackCatalogue.helpLines(2));
        assertEquals(LEGACY_PAGE_3, AttackCatalogue.helpLines(3));
        assertEquals(LEGACY_PAGE_4, AttackCatalogue.helpLines(4));
    }

    @Test
    @DisplayName("Help menu keeps its four titled pages")
    void helpMenuShape() {
        assertEquals(4, AttackCatalogue.pages());
        assertEquals("GROUND ATTACKS & EARTH CONTROL", AttackCatalogue.pageTitle(1));
        assertEquals("AERIAL ASSAULTS & CELESTIAL RUSHES", AttackCatalogue.pageTitle(2));
        assertEquals("RANGED ARTILLERY & MAGIC PROJECTIONS", AttackCatalogue.pageTitle(3));
        assertEquals("DEFENSIVE SHIELDS & MAGIC SEALS", AttackCatalogue.pageTitle(4));
    }

    @Test
    @DisplayName("Completion names are the documented attacks, each declared once")
    void namesAreUniqueAndComplete() {
        List<String> names = AttackCatalogue.names();
        Set<String> distinct = new HashSet<>(names);
        assertEquals(names.size(), distinct.size(), "duplicate attack in " + names);
        assertEquals(LEGACY_PAGE_1.size() + LEGACY_PAGE_2.size() + LEGACY_PAGE_3.size() + LEGACY_PAGE_4.size(),
                names.size(), "every help line must correspond to one attack name");
        for (String name : names) {
            assertEquals(name.toLowerCase(), name, "tab completion expects lowercase names: " + name);
            assertFalse(name.contains(" "), "attack names are single words: " + name);
        }
    }

    @ParameterizedTest(name = "{0} is part of the attack list")
    @ValueSource(strings = {"groundslam", "starfall", "spiritbeam", "trianglecall", "hoverbarrage"})
    void catalogueContainsTheOldSuggestions(String name) {
        assertTrue(AttackCatalogue.names().contains(name));
    }

    @Test
    @DisplayName("Nothing is advertised that the boss can no longer trigger")
    void helpNamesStayLowercaseAndAligned() {
        for (AttackCatalogue.Entry entry : AttackCatalogue.entries()) {
            assertFalse(entry.name().isBlank());
            assertEquals(entry.name().toLowerCase(), entry.name());
            assertTrue(entry.page() >= 1 && entry.page() <= AttackCatalogue.pages(),
                    entry.name() + " is on a page that does not exist: " + entry.page());
            assertTrue(entry.description().startsWith("&7"), "descriptions share the &7 body colour: " + entry.name());
        }
    }
}
