package com.Chagui68.ritual;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * The Pantheon Altar: the 5x5 structure that summons DrakesBosses' gods in the Boss Dimension.
 *
 * <pre>
 *   P . . . P      P = corner pillar: two blocks of the pantheon's pillar
 *   . . c . .      c = candle in the pantheon's colour
 *   . c A c .      A = the pantheon's altar core
 *   . . c . .
 *   P . . . P
 * </pre>
 *
 * The four pantheons share the shape and differ in their blocks, so the altar a player builds says
 * which gods it can call. Lighting the four candles opens it; dropping one god's offering on the
 * core brings that god. Every material here stays clear of the other rituals (Sentinel and Nix use
 * red candles, DIO yellow, JackStar the blue family) and of DrakesBosses' own altar cores.
 */
public final class PantheonAltarStructure {

    /** Candles around the core at (2, 0, 2). */
    public static final int[][] CANDLE_OFFSETS = {
            {2, 0, 1},
            {2, 0, 3},
            {1, 0, 2},
            {3, 0, 2}
    };

    /** The four corner pillars. */
    public static final int[][] CORNER_OFFSETS = {
            {0, 0},
            {4, 0},
            {0, 4},
            {4, 4}
    };

    /** Blocks each pillar is tall. */
    public static final int PILLAR_HEIGHT = 2;

    /** The four families of gods, each with its own altar. */
    public enum Pantheon {
        OLYMPUS("Olimpo", Material.WHITE_CANDLE, Material.CHISELED_QUARTZ_BLOCK, Material.QUARTZ_PILLAR,
                Color.fromRGB(0xF4F1E6), Color.fromRGB(0xFFD23F)),
        ASGARD("Asgard", Material.GREEN_CANDLE, Material.CHISELED_DEEPSLATE, Material.SPRUCE_LOG,
                Color.fromRGB(0x3E8E4A), Color.fromRGB(0x9FD8FF)),
        DUAT("Duat", Material.ORANGE_CANDLE, Material.CHISELED_SANDSTONE, Material.SMOOTH_SANDSTONE,
                Color.fromRGB(0xF2A33A), Color.fromRGB(0x2FB8A6)),
        VOID("el Vacío", Material.PURPLE_CANDLE, Material.END_STONE_BRICKS, Material.PURPUR_PILLAR,
                Color.fromRGB(0x8E3CFF), Color.fromRGB(0x1A0B2E));

        private final String displayName;
        private final Material candle;
        private final Material core;
        private final Material pillar;
        private final Color primary;
        private final Color accent;

        Pantheon(String displayName, Material candle, Material core, Material pillar, Color primary, Color accent) {
            this.displayName = displayName;
            this.candle = candle;
            this.core = core;
            this.pillar = pillar;
            this.primary = primary;
            this.accent = accent;
        }

        public String displayName() {
            return displayName;
        }

        public Material candle() {
            return candle;
        }

        public Material core() {
            return core;
        }

        public Material pillar() {
            return pillar;
        }

        public Color primary() {
            return primary;
        }

        public Color accent() {
            return accent;
        }

        /** The pantheon whose altar uses this candle, or null. */
        public static Pantheon byCandle(Material candle) {
            for (Pantheon pantheon : values()) {
                if (pantheon.candle == candle) return pantheon;
            }
            return null;
        }
    }

    private PantheonAltarStructure() {
    }

    public static boolean isStructureComplete(Location origin, Pantheon pantheon) {
        if (origin.getWorld() == null) return false;
        if (getBlockAt(origin, 2, 0, 2).getType() != pantheon.core()) return false;
        for (int[] offset : CANDLE_OFFSETS) {
            if (getBlockAt(origin, offset[0], offset[1], offset[2]).getType() != pantheon.candle()) return false;
        }
        for (int[] corner : CORNER_OFFSETS) {
            for (int y = 0; y < PILLAR_HEIGHT; y++) {
                if (getBlockAt(origin, corner[0], y, corner[1]).getType() != pantheon.pillar()) return false;
            }
        }
        return true;
    }

    public static boolean areAllCandlesLit(Location origin, Pantheon pantheon) {
        if (origin.getWorld() == null) return false;
        for (int[] offset : CANDLE_OFFSETS) {
            Block candle = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candle.getType() != pantheon.candle()) return false;
            if (candle.getBlockData() instanceof Candle data && !data.isLit()) return false;
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin, Pantheon pantheon) {
        if (origin.getWorld() == null) return;
        for (int[] offset : CANDLE_OFFSETS) {
            Block candle = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candle.getType() == pantheon.candle() && candle.getBlockData() instanceof Candle data) {
                data.setLit(false);
                candle.setBlockData(data);
            }
        }
    }

    public static boolean containsCandle(Location origin, Location candle) {
        for (int[] offset : CANDLE_OFFSETS) {
            if (origin.getBlockX() + offset[0] == candle.getBlockX()
                    && origin.getBlockY() + offset[1] == candle.getBlockY()
                    && origin.getBlockZ() + offset[2] == candle.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    /** The centre of the core's top face, where the offering is dropped and the god appears. */
    public static Location getAltarLocation(Location origin) {
        return origin.clone().add(2.5, 1.0, 2.5);
    }

    /** Just above each pillar, where the ritual's light comes from. */
    public static List<Location> getPillarTops(Location origin) {
        List<Location> list = new ArrayList<>();
        for (int[] corner : CORNER_OFFSETS) {
            list.add(origin.clone().add(corner[0] + 0.5, PILLAR_HEIGHT + 0.2, corner[1] + 0.5));
        }
        return list;
    }

    /** How close to the altar the offering has to land. */
    public static double getRadius() {
        return 3.0;
    }

    private static Block getBlockAt(Location origin, int x, int y, int z) {
        return origin.getWorld().getBlockAt(origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
    }
}
