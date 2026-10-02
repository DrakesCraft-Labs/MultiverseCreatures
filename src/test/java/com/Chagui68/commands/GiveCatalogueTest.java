package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the {@code /msc give} data down.
 *
 * The old command carried a 60 case switch, a 20 line tab-completion list and four pages of help
 * lines — the same item described three times. The item table is now the single source of truth, so
 * this test guards the parts that used to be duplicated: the help text (compared against the
 * literal text of the previous build), the aliases resolving, and every item named in the help
 * actually being givable.
 */
class GiveCatalogueTest {

    private static final List<String> LEGACY_PAGE_1 = List.of(
            "excalibur &8- &6Holy Blade of Kings",
            "cindergreatsword &8- &cBlazing Heavy Greatsword",
            "nullshearedge &8- &5Void Spatial Slicer",
            "soulreapscythe &8- &8Life-draining Scythe",
            "venomfang &8- &2Poison-tipped Dagger",
            "aetherpullshot &8- &bGravitational Pull Bow",
            "chaosforge &8- &dChaos Casting Hammer",
            "skyfiretalisman &8- &6Celestial Skyfire Charm",
            "sentinelgrimoire &8- &9Guardian Spell Grimoire",
            "executionerguillotine &8- &4NIX Executioner Axe",
            "architectdeployer &8- &bJackStar Data Bow");

    private static final List<String> LEGACY_PAGE_2 = List.of(
            "eighthandledwheel &8- &fMahoraga's Sacred Wheel",
            "obsidianbastionhelmet &8- &8Obsidian Bastion Helmet",
            "obsidianbastionchestplate &8- &8Obsidian Bastion Chestplate",
            "obsidianbastionleggings &8- &8Obsidian Bastion Leggings",
            "obsidianbastionboots &8- &8Obsidian Bastion Boots",
            "icecrown &8- &bGlacial Monarch Crown",
            "wirtslantern &8- &6Illuminating Explorer Lantern",
            "mantisclaws &8- &aPreying Mantis Dual Claws",
            "militarymine &8- &cProximity Landmine",
            "frostheartoffhand &8- &9Cryo Shield Offhand",
            "marrowaegis &8- &fBone Marrow Aegis Shield",
            "veilwalkermantle &8- &5Shadow Veilwalker Cloak");

    private static final List<String> LEGACY_PAGE_3 = List.of(
            "executionerwarrant &8- &4NIX Scaffold Summon Warrant",
            "compressedgoldblock &8- &6Boss Altar Anchor Block",
            "multiversalcore &8- &dMultiverse Nexus Core",
            "wheelcore &8- &fDivergent Sila Wheel Core",
            "moltenwheelcore &8- &cMolten Infused Wheel Core",
            "refinedwheelcore &8- &bPurified Wheel Core",
            "reapercore &8- &8Soul Reaper Core",
            "sentinelcore &8- &9Ancient Sentinel Core",
            "endercore &8- &5End Void Dimensional Core",
            "starcore &8- &eCelestial Star Core",
            "chaoscore &8- &dRaw Concentrated Chaos Core",
            "scoobycookie &8- &6Mystery Scooby Snack (Food)",
            "executioneredge &8- &4NIX Axe Shard",
            "vampireblood &8/ &eunstableblood &8/ &ebearerelixir &8- &4DIO Blood & Elixir");

    private static final List<String> LEGACY_PAGE_4 = List.of(
            "reaperessence &8/ &evoidessence &8/ &ewheelessence &8- &7Essences",
            "stormcrystal &8/ &emagmacore &8/ &efrostheart &8- &7Elemental Cores",
            "obsidianshard &8/ &erefinednetherite &8/ &emoltennetherite &8- &7Metals",
            "headslimeheart &8/ &eheadslimegelatin &8/ &evenomgland &8- &7Organics",
            "shadowcloak &8/ &eswordmold &8/ &emilitarycomponent &8- &7Relics",
            "reinforcedbone &8/ &ereinforcedboneblock &8- &7Bone Crafting",
            "chaosorb &8/ &echaospowder &8/ &echaosfragment &8- &7Chaos Alch.",
            "condensedchaosorb &8/ &eenderfragment &8- &7Infused Catalysts");

    @Test
    @DisplayName("Help pages reproduce the literal text the command used to print")
    void helpPagesMatchLegacyText() {
        assertEquals(LEGACY_PAGE_1, GiveCatalogue.helpLines(1));
        assertEquals(LEGACY_PAGE_2, GiveCatalogue.helpLines(2));
        assertEquals(LEGACY_PAGE_3, GiveCatalogue.helpLines(3));
        assertEquals(LEGACY_PAGE_4, GiveCatalogue.helpLines(4));
    }

    @Test
    @DisplayName("Help menu keeps its four titled pages")
    void helpMenuShape() {
        assertEquals(4, GiveCatalogue.pages());
        assertEquals("LEGENDARY WEAPONS & MAGIC", GiveCatalogue.pageTitle(1));
        assertEquals("ARMOR SETS, RELICS & OFFHANDS", GiveCatalogue.pageTitle(2));
        assertEquals("BOSS CATALYSTS & APEX COMPONENTS", GiveCatalogue.pageTitle(3));
        assertEquals("CRAFTING MATERIALS & ESSENCES", GiveCatalogue.pageTitle(4));
    }

    @Test
    @DisplayName("Aliases are unique and lowercase so the lookup stays case-insensitive")
    void aliasesAreUnique() {
        List<String> all = GiveCatalogue.aliases();
        Set<String> distinct = new HashSet<>(all);
        assertEquals(all.size(), distinct.size(), "duplicate give alias in " + all);
        for (String alias : all) {
            assertFalse(alias.isBlank(), "blank alias");
            assertEquals(alias.toLowerCase(), alias, "aliases stay lowercase: " + alias);
        }
    }

    @Test
    @DisplayName("Every item named in the help text is actually givable")
    void helpOnlyNamesRealItems() {
        List<String> aliases = GiveCatalogue.aliases();
        for (int page = 1; page <= GiveCatalogue.pages(); page++) {
            for (String line : GiveCatalogue.helpLines(page)) {
                for (String named : aliasesIn(line)) {
                    assertTrue(aliases.contains(named),
                            "help page " + page + " advertises '" + named + "', which is not a givable alias");
                }
            }
        }
    }

    @Test
    @DisplayName("Every alias of an entry is offered by tab completion")
    void completionFollowsTheTable() {
        List<String> aliases = GiveCatalogue.aliases();
        for (GiveCatalogue.Entry entry : GiveCatalogue.entries()) {
            assertFalse(entry.aliases().isEmpty(), "entry without aliases");
            assertNotNull(entry.template(), "entry without an item factory");
            for (String alias : entry.aliases()) {
                assertTrue(aliases.contains(alias));
            }
        }
        // The aliases the old hardcoded completion list never offered.
        for (String alias : List.of("cookie", "sword", "crown", "lantern", "star", "mold", "claws",
                "component", "mine", "heart", "gelatin", "pullshot", "greatsword", "nullshear", "scythe",
                "dagger", "talisman", "grimoire", "wheel", "bastionhelmet", "frostoffhand", "aegis",
                "mantle", "architectkernel", "kernel", "architect", "deathwarrant", "condensed")) {
            assertTrue(aliases.contains(alias), "missing completion alias: " + alias);
        }
    }

    @Test
    @DisplayName("An alias that is not in the table gives nothing instead of throwing")
    void unknownAliasIsNull() {
        assertNull(GiveCatalogue.find("not-an-item"));
        assertNull(GiveCatalogue.find(null));
        assertNull(GiveCatalogue.find(""));
    }

    /** Splits "a &8/ &eb &8- description" into the item aliases it advertises. */
    private static List<String> aliasesIn(String helpLine) {
        String[] parts = helpLine.split(" &8- ", 2);
        List<String> aliases = new ArrayList<>();
        for (String token : Arrays.asList(parts[0].split(" &8/ &e"))) {
            String alias = token.startsWith("&e") ? token.substring(2) : token;
            aliases.add(alias.trim());
        }
        return aliases;
    }
}
