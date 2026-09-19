package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates and manages The Multiverse Terminal 5x5 ritual structure
 * used to invoke JACKSTAR — The System Architect.
 */
public class JackInvocationStructure {

    // 4 cardinal candle offsets relative to origin around center (2, 0, 2)
    public static final int[][] CANDLE_OFFSETS = {
            {2, 0, 1}, // North
            {2, 0, 3}, // South
            {1, 0, 2}, // West
            {3, 0, 2}  // East
    };

    // 4 corner pillar base offsets (Base at Y=0, Lightning Rod at Y=1)
    public static final int[][] CORNER_OFFSETS = {
            {0, 0},
            {4, 0},
            {0, 4},
            {4, 4}
    };

    public static boolean isStructureComplete(Location origin) {
        if (origin == null || origin.getWorld() == null) return false;

        // 1. Central Core Block at (2, 0, 2): Respawn Anchor or Lodestone
        Block center = getBlockAt(origin, 2, 0, 2);
        if (!isValidCore(center.getType())) {
            return false;
        }

        // 2. Four Cardinal Candles: Cyan or Light Blue candles
        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (!isValidCandle(candleBlock.getType())) {
                return false;
            }
        }

        // 3. Four Corner Pillars (Base Y=0, Lightning Rod Y=1)
        for (int[] corner : CORNER_OFFSETS) {
            Block base = getBlockAt(origin, corner[0], 0, corner[1]);
            Block rod = getBlockAt(origin, corner[0], 1, corner[1]);

            if (!isValidBase(base.getType())) return false;
            if (rod.getType() != Material.LIGHTNING_ROD) return false;
        }

        return true;
    }

    public static boolean areAllCandlesLit(Location origin) {
        if (origin == null || origin.getWorld() == null) return false;

        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (!isValidCandle(candleBlock.getType())) return false;
            if (candleBlock.getBlockData() instanceof Candle candleData && !candleData.isLit()) {
                return false;
            }
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin) {
        if (origin == null || origin.getWorld() == null) return;

        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (isValidCandle(candleBlock.getType()) && candleBlock.getBlockData() instanceof Candle candleData) {
                candleData.setLit(false);
                candleBlock.setBlockData(candleData);
            }
        }
    }

    public static boolean containsCandle(Location origin, Location candleLoc) {
        for (int[] offset : CANDLE_OFFSETS) {
            if (origin.getBlockX() + offset[0] == candleLoc.getBlockX()
                    && origin.getBlockY() + offset[1] == candleLoc.getBlockY()
                    && origin.getBlockZ() + offset[2] == candleLoc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    public static Location getCenterLocation(Location origin) {
        return origin.clone().add(2.5, 0.5, 2.5);
    }

    public static List<Location> getCornerRodLocations(Location origin) {
        List<Location> list = new ArrayList<>();
        for (int[] corner : CORNER_OFFSETS) {
            list.add(origin.clone().add(corner[0] + 0.5, 1.5, corner[1] + 0.5));
        }
        return list;
    }

    public static double getRadius() {
        return 3.2;
    }

    private static Block getBlockAt(Location origin, int x, int y, int z) {
        return origin.getWorld().getBlockAt(
                origin.getBlockX() + x,
                origin.getBlockY() + y,
                origin.getBlockZ() + z
        );
    }

    public static boolean isValidCore(Material material) {
        return material == Material.RESPAWN_ANCHOR
                || material == Material.LODESTONE
                || material == Material.ENCHANTING_TABLE
                || material == Material.BEACON;
    }

    public static boolean isValidCandle(Material material) {
        return material == Material.CYAN_CANDLE
                || material == Material.LIGHT_BLUE_CANDLE
                || material == Material.BLUE_CANDLE
                || material == Material.SOUL_CAMPFIRE
                || material == Material.CANDLE;
    }

    public static boolean isValidBase(Material material) {
        return material == Material.CRYING_OBSIDIAN
                || material == Material.OBSIDIAN
                || material == Material.POLISHED_BLACKSTONE_BRICKS
                || material == Material.CHISELED_POLISHED_BLACKSTONE
                || material == Material.DEEPSLATE_BRICKS
                || material == Material.IRON_BLOCK;
    }
}
