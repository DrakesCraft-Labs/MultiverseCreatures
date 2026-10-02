package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates and manages The Executioner's Scaffold 5x5 structure
 * used to invoke NIX - The Executioner in the Boss Dimension.
 */
public class NixInvocationStructure {

    // Relative candle positions around the anvil at (2, 0, 2)
    public static final int[][] CANDLE_OFFSETS = {
            {2, 0, 1}, // North of anvil
            {2, 0, 3}, // South of anvil
            {1, 0, 2}, // West of anvil
            {3, 0, 2}  // East of anvil
    };

    // Corner pillar base offsets
    public static final int[][] CORNER_OFFSETS = {
            {0, 0},
            {4, 0},
            {0, 4},
            {4, 4}
    };

    public static boolean isStructureComplete(Location origin) {
        if (origin.getWorld() == null) return false;

        // 1. Check central anvil at (2, 0, 2)
        Block anvilBlock = getBlockAt(origin, 2, 0, 2);
        if (!isAnvil(anvilBlock.getType())) {
            return false;
        }

        // 2. Check 4 red candles
        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candleBlock.getType() != Material.RED_CANDLE) {
                return false;
            }
        }

        // 3. Check 4 corner gallows (Base Y=0, Chain Y=1, Skull Y=2)
        for (int[] corner : CORNER_OFFSETS) {
            Block base = getBlockAt(origin, corner[0], 0, corner[1]);
            Block chain = getBlockAt(origin, corner[0], 1, corner[1]);
            Block skull = getBlockAt(origin, corner[0], 2, corner[1]);

            if (!isValidBase(base.getType())) return false;
            if (!isChain(chain.getType())) return false;
            if (!isValidSkull(skull.getType())) return false;
        }

        return true;
    }

    public static boolean areAllCandlesLit(Location origin) {
        if (origin.getWorld() == null) return false;

        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candleBlock.getType() != Material.RED_CANDLE) return false;
            if (candleBlock.getBlockData() instanceof Candle candleData && !candleData.isLit()) {
                return false;
            }
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin) {
        if (origin.getWorld() == null) return;

        for (int[] offset : CANDLE_OFFSETS) {
            Block candleBlock = getBlockAt(origin, offset[0], offset[1], offset[2]);
            if (candleBlock.getType() == Material.RED_CANDLE && candleBlock.getBlockData() instanceof Candle candleData) {
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

    public static Location getAnvilLocation(Location origin) {
        return origin.clone().add(2.5, 0.5, 2.5);
    }

    public static List<Location> getCornerGallowsLocations(Location origin) {
        List<Location> list = new ArrayList<>();
        for (int[] corner : CORNER_OFFSETS) {
            list.add(origin.clone().add(corner[0] + 0.5, 1.8, corner[1] + 0.5));
        }
        return list;
    }

    public static double getRadius() {
        return 3.0;
    }

    private static Block getBlockAt(Location origin, int x, int y, int z) {
        return origin.getWorld().getBlockAt(
                origin.getBlockX() + x,
                origin.getBlockY() + y,
                origin.getBlockZ() + z
        );
    }

    private static boolean isAnvil(Material material) {
        return material == Material.ANVIL
                || material == Material.CHIPPED_ANVIL
                || material == Material.DAMAGED_ANVIL;
    }

    private static boolean isValidBase(Material material) {
        return material == Material.POLISHED_BLACKSTONE_BRICKS
                || material == Material.POLISHED_BLACKSTONE
                || material == Material.DEEPSLATE_BRICKS
                || material == Material.POLISHED_DEEPSLATE
                || material == Material.CRYING_OBSIDIAN
                || material == Material.IRON_BLOCK;
    }

    private static boolean isChain(Material material) {
        return material == Material.IRON_CHAIN || material.name().endsWith("CHAIN");
    }

    private static boolean isValidSkull(Material material) {
        return material == Material.SKELETON_SKULL
                || material == Material.SKELETON_WALL_SKULL
                || material == Material.WITHER_SKELETON_SKULL
                || material == Material.WITHER_SKELETON_WALL_SKULL
                || material == Material.PLAYER_HEAD
                || material == Material.PLAYER_WALL_HEAD
                || material == Material.ZOMBIE_HEAD
                || material == Material.ZOMBIE_WALL_HEAD;
    }
}
