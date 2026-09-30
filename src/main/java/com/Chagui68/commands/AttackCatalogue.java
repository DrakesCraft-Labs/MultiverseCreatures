package com.Chagui68.commands;

import java.util.List;

/**
 * Data behind {@code /msc attack}: the attacks documented in the help menu and offered by tab
 * completion.
 *
 * <p>The names are the keys accepted by {@code ArmorStandBoss#triggerAttack}, so a new attack is
 * added here once and both the help pages and the tab completion pick it up. Descriptions keep
 * their {@code &} colour codes; {@link CommandMenu} translates them when rendering.
 */
final class AttackCatalogue {

    /** One documented attack: its command name, its help description and the page it shows on. */
    record Entry(String name, String description, int page) {}

    static final List<String> PAGE_TITLES = List.of(
            "GROUND ATTACKS & EARTH CONTROL",
            "AERIAL ASSAULTS & CELESTIAL RUSHES",
            "RANGED ARTILLERY & MAGIC PROJECTIONS",
            "DEFENSIVE SHIELDS & MAGIC SEALS");

    private static final List<Entry> ENTRIES = List.of(
            // Ground
            new Entry("groundslam", "&7Earth-shattering seismic leap impact", 1),
            new Entry("groundshatter", "&7Fissure wave that fractures the terrain", 1),
            new Entry("shieldbash", "&7Forward heavy rush that stuns targets", 1),
            new Entry("lancestorm", "&7Piercing lance barrage across the ground", 1),
            new Entry("earthpillar", "&7Stone pillars erupting from beneath foes", 1),
            new Entry("chaingrapple", "&7Iron chain hook pulling players in", 1),
            new Entry("warstomp", "&7Massive area shockwave knocking entities back", 1),
            new Entry("armorspikes", "&7Defensive spike retribution burst", 1),
            new Entry("vortexpull", "&7Gravitational vortex dragging entities to center", 1),
            new Entry("mirrorimage", "&7Illusionary decoys to confuse adversaries", 1),
            new Entry("doombeam", "&7Focused demonic ground laser sweep", 1),
            new Entry("lanceflurry", "&7Three rapid lance thrusts in a frontal cone", 1),
            new Entry("whirlwindslash", "&7Spinning sweep that drags foes in, then a finishing cut", 1),
            new Entry("executionsweep", "&7Devastating wide-arc executioner strike", 1),
            new Entry("obsidianspire", "&7Line of volcanic pillars shattering the ground ahead", 1),
            new Entry("earthmaw", "&7Stone jaws closing on everything in front", 1),
            new Entry("shadowstep", "&7Vanishes through a sigil to strike from behind", 1),
            new Entry("runeward", "&7Plants a pulsing rune ward that outlives the cast", 1),
            // Aerial
            new Entry("starfall", "&7Calling celestial stars crashing down", 2),
            new Entry("aerialrush", "&7High-speed aerial homing strike", 2),
            new Entry("sonicboom", "&7Acoustic blast wave penetrating defenses", 2),
            new Entry("lightningstorm", "&7Summoning consecutive lightning strikes", 2),
            new Entry("gravitywell", "&7Aerial singularity pulling upwards", 2),
            new Entry("crossslash", "&7Dual aerial sword cleave in cross shape", 2),
            new Entry("novaburst", "&7Explosive radiant detonation in midair", 2),
            new Entry("darkorb", "&7Floating orb radiating darkness damage", 2),
            new Entry("windcutter", "&7Slicing razor-wind blades", 2),
            new Entry("heavenlyjudgment", "&7Holy orbital beam strike", 2),
            new Entry("rainoflances", "&7Shower of holy lances from the sky", 2),
            new Entry("airslam", "&7Sky-dive slam pulverizing the landing zone", 2),
            new Entry("hoverbarrage", "&7Levitating volley of energy projectiles", 2),
            new Entry("eclipsefall", "&7Black eclipse disc dropped on the landing zone", 2),
            new Entry("bladering", "&7Orbiting lance ring fired out one by one", 2),
            new Entry("obsidianwings", "&7Wing beats sweeping obsidian shards outward", 2),
            // Ranged / magic
            new Entry("lancesnipe", "&7High-velocity sniper lance projectile", 3),
            new Entry("meteorstorm", "&7Shower of flaming meteorites", 3),
            new Entry("voidbeam", "&7Linear void disintegration laser", 3),
            new Entry("frostlance", "&7Piercing glacial spear inflicting deep freeze", 3),
            new Entry("lightningspear", "&7Electrified javelin shocking targets", 3),
            new Entry("shadowvolley", "&7Multi-directional flurry of dark arrows", 3),
            new Entry("chainlightning", "&7Electric arc bouncing between nearby players", 3),
            new Entry("crystalbarrage", "&7Rapid crystal shards barrage", 3),
            new Entry("arcaneorb", "&7Pulsing magical sphere of pure arcane power", 3),
            new Entry("voidrift", "&7Dimensional tear distorting spacetime", 3),
            new Entry("arcanemissiles", "&7Homing arcane bolts seeking players", 3),
            new Entry("spiritbeam", "&7Piercing spectral light beam", 3),
            new Entry("soultethers", "&7Visible tethers hook players and reel them in", 3),
            new Entry("plaguebrand", "&7Brands a player with a plague that spreads", 3),
            new Entry("runemines", "&7Scatters armed runes that burst when stepped on", 3),
            // Defensive
            new Entry("stoneskin", "&7Hardens boss defense, reducing all damage", 4),
            new Entry("reflectbarrier", "&7Prismatic shield reflecting projectiles", 4),
            new Entry("absorbshield", "&7Barrier converting incoming damage into healing", 4),
            new Entry("shieldseal", "&7Protective ancient ward preventing melee strikes", 4),
            new Entry("healingcircle", "&7Radiant circle regenerating boss vitality", 4),
            new Entry("trianglecall", "&7Sacred geometric barrier summoning reinforcements", 4));

    private AttackCatalogue() {}

    static List<Entry> entries() {
        return ENTRIES;
    }

    static int pages() {
        return PAGE_TITLES.size();
    }

    /** Header shown by {@code /msc attack help <page>}; pages are 1-based and pre-clamped. */
    static String pageTitle(int page) {
        return PAGE_TITLES.get(page - 1);
    }

    /** The rendered help lines of one page, in declaration order. */
    static List<String> helpLines(int page) {
        return ENTRIES.stream()
                .filter(entry -> entry.page() == page)
                .map(AttackCatalogue::helpLine)
                .toList();
    }

    static String helpLine(Entry entry) {
        return " &e• " + entry.name() + " &8- " + entry.description();
    }

    /** Every documented attack name, in help order — the source of tab completion. */
    static List<String> names() {
        return ENTRIES.stream().map(Entry::name).toList();
    }
}
