package com.Chagui68.ritual;

import com.Chagui68.ritual.PantheonAltarStructure.Pantheon;
import com.Chagui68.testsupport.ProjectPaths;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PantheonAltarStructureTest {

    /** Blocks the dimension's other rituals and DrakesBosses' own altar already react to. */
    private static final Set<Material> TAKEN = Set.of(
            Material.RED_CANDLE, Material.YELLOW_CANDLE, Material.CYAN_CANDLE, Material.LIGHT_BLUE_CANDLE,
            Material.BLUE_CANDLE, Material.CANDLE,
            Material.CRYING_OBSIDIAN, Material.LODESTONE, Material.RESPAWN_ANCHOR, Material.BEACON,
            Material.CHISELED_POLISHED_BLACKSTONE, Material.GILDED_BLACKSTONE);

    @Test
    @DisplayName("Four candles sit right next to the core, pillars on the corners of the 5x5")
    void layout() {
        assertEquals(4, PantheonAltarStructure.CANDLE_OFFSETS.length);
        for (int[] offset : PantheonAltarStructure.CANDLE_OFFSETS) {
            assertEquals(0, offset[1]);
            assertEquals(1, Math.abs(offset[0] - 2) + Math.abs(offset[2] - 2));
        }
        for (int[] corner : PantheonAltarStructure.CORNER_OFFSETS) {
            assertTrue((corner[0] == 0 || corner[0] == 4) && (corner[1] == 0 || corner[1] == 4));
        }
        Location altar = PantheonAltarStructure.getAltarLocation(new Location(null, 10, 50, -20));
        assertEquals(12.5, altar.getX(), 1e-9);
        assertEquals(51.0, altar.getY(), 1e-9);
        assertEquals(-17.5, altar.getZ(), 1e-9);
    }

    @Test
    @DisplayName("Each pantheon has its own candle, core and pillar, none shared with another ritual")
    void pantheonsAreDistinct() {
        Set<Material> used = new HashSet<>();
        for (Pantheon pantheon : Pantheon.values()) {
            for (Material block : List.of(pantheon.candle(), pantheon.core(), pantheon.pillar())) {
                assertFalse(TAKEN.contains(block), pantheon + " uses " + block + ", which another ritual reacts to");
                assertTrue(used.add(block), pantheon + " shares " + block + " with another pantheon");
            }
            assertSame(pantheon, Pantheon.byCandle(pantheon.candle()));
        }
        assertNull(Pantheon.byCandle(Material.RED_CANDLE));
    }

    @Test
    @DisplayName("Every pantheon has gods, and no two gods share an offering or an id")
    void godsAreUnambiguous() {
        Set<Pantheon> withGods = EnumSet.noneOf(Pantheon.class);
        Set<Material> offerings = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (PantheonGod god : PantheonGod.values()) {
            withGods.add(god.pantheon());
            assertTrue(offerings.add(god.defaultOffering()), god + " shares its offering");
            assertTrue(ids.add(god.id()), god + " shares its id");
            assertSame(god, PantheonGod.byOffering(god.defaultOffering(), id -> null));
        }
        assertEquals(EnumSet.allOf(Pantheon.class), withGods);
        assertNull(PantheonGod.byOffering(Material.DIRT, id -> null));
    }

    @Test
    @DisplayName("A blank override keeps the default offering")
    void blankOverrideFallsBack() {
        assertEquals(Material.LIGHTNING_ROD, PantheonGod.ZEUS.offering(id -> " "));
    }

    @Test
    @DisplayName("config.yml ships every god's offering, matching the code's default")
    void configListsEveryOffering() {
        Map<?, ?> offerings = offeringsSection();
        assertEquals(PantheonGod.values().length, offerings.size(), "config.yml lists other gods: " + offerings.keySet());
        for (PantheonGod god : PantheonGod.values()) {
            assertEquals(god.defaultOffering().name(), offerings.get(god.id()),
                    "drakes-bosses.offerings." + god.id() + " drifted from the code's default");
        }
    }

    private static Map<?, ?> offeringsSection() {
        try (InputStream in = Files.newInputStream(ProjectPaths.resource("config.yml"))) {
            Map<?, ?> config = new Yaml().load(in);
            Object section = config.get("drakes-bosses");
            assertInstanceOf(Map.class, section, "config.yml lost its drakes-bosses section");
            Object offerings = ((Map<?, ?>) section).get("offerings");
            assertInstanceOf(Map.class, offerings, "drakes-bosses.offerings must be a map");
            return (Map<?, ?>) offerings;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
