package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * The World's Throne: the 5x5 structure that summons DIO in the Boss Dimension.
 *
 * <pre>
 *   S . . . S      S = corner pillar: gold block, emerald block, a skull or head on top
 *   . . c . .      c = yellow candle
 *   . c G c .      G = central gold block (the throne)
 *   . . c . .
 *   S . . . S
 * </pre>
 *
 * Lighting the four candles wakes it; dropping a clock on the throne stops time and DIO steps out.
 */
public class DioInvocationStructure {

    /** Candles around the throne at (2, 0, 2). */
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

    public static boolean isStructureComplete(Location origin) {
        if (origin.getWorld() == null) return false;
        if (getBlockAt(origin, 2, 0, 2).getType() != Material.GOLD_BLOCK) return false;
        for (int[] offset : CANDLE_OFFSETS) {
            if (getBlockAt(origin, offset[0], offset[1], offset[2]).getType() != Material.YELLOW_CANDLE) return false;
        }
        for (int[] corner : CORNER_OFFSETS) {
            if (getBlockAt(origin, corner[0], 0, corner[1]).getType() != Material.GOLD_BLOCK) return false;
            if (getBlockAt(origin, corner[0], 1, corner[1]).getType() != Material.EMERALD_BLOCK) return false;
            if (!isHead(getBlockAt(origin, corner[0], 2, corner[1]).getType())) return false;
        }
        return true;
    }

    public static boolean areAllCandlesLit(Location origin) {
        if (origin.getWorld() == null) return false;
        for (int[] offset : CANDLE_OFFSETS) {
            Block candle = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candle.getType() != Material.YELLOW_CANDLE) return false;
            if (candle.getBlockData() instanceof Candle data && !data.isLit()) return false;
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin) {
        if (origin.getWorld() == null) return;
        for (int[] offset : CANDLE_OFFSETS) {
            Block candle = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candle.getType() == Material.YELLOW_CANDLE && candle.getBlockData() instanceof Candle data) {
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

    /** The centre of the throne's top face, where the clock is offered and DIO appears. */
    public static Location getThroneLocation(Location origin) {
        return origin.clone().add(2.5, 1.0, 2.5);
    }

    /** Just above each corner's head, where the ritual's light comes from. */
    public static List<Location> getPillarTops(Location origin) {
        List<Location> list = new ArrayList<>();
        for (int[] corner : CORNER_OFFSETS) {
            list.add(origin.clone().add(corner[0] + 0.5, 2.6, corner[1] + 0.5));
        }
        return list;
    }

    /** How close to the throne the clock has to land. */
    public static double getRadius() {
        return 3.0;
    }

    static boolean isHead(Material material) {
        return material == Material.PLAYER_HEAD
                || material == Material.PLAYER_WALL_HEAD
                || material == Material.SKELETON_SKULL
                || material == Material.SKELETON_WALL_SKULL
                || material == Material.WITHER_SKELETON_SKULL
                || material == Material.WITHER_SKELETON_WALL_SKULL
                || material == Material.ZOMBIE_HEAD
                || material == Material.ZOMBIE_WALL_HEAD;
    }

    private static Block getBlockAt(Location origin, int x, int y, int z) {
        return origin.getWorld().getBlockAt(origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
    }
}
