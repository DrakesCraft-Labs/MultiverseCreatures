package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;

import java.util.HashMap;
import java.util.Map;

public class BossInvocationStructure {

    private static final Map<Location, Material> STRUCTURE_BLOCKS = new HashMap<>();

    static {
        int[][] layout = {
                {0, 1, 1, 1, 0},
                {1, 0, 0, 0, 1},
                {1, 0, 0, 0, 1},
                {1, 0, 0, 0, 1},
                {0, 1, 1, 1, 0}
        };
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (layout[z][x] == 1) {
                    STRUCTURE_BLOCKS.put(new Location(null, x, 0, z), Material.RED_CANDLE);
                }
            }
        }
    }

    public static boolean isStructureComplete(Location origin) {
        for (var entry : STRUCTURE_BLOCKS.entrySet()) {
            Location rel = entry.getKey();
            Block block = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + rel.getBlockX(),
                    origin.getBlockY() + rel.getBlockY(),
                    origin.getBlockZ() + rel.getBlockZ()
            ).getBlock();
            if (block.getType() != Material.RED_CANDLE) return false;
        }
        return true;
    }

    public static boolean areAllCandlesLit(Location origin) {
        for (var entry : STRUCTURE_BLOCKS.entrySet()) {
            Location rel = entry.getKey();
            Block block = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + rel.getBlockX(),
                    origin.getBlockY() + rel.getBlockY(),
                    origin.getBlockZ() + rel.getBlockZ()
            ).getBlock();
            if (block.getType() != Material.RED_CANDLE) return false;
            if (block.getBlockData() instanceof Candle candleData && !candleData.isLit()) return false;
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin) {
        for (var entry : STRUCTURE_BLOCKS.entrySet()) {
            Location rel = entry.getKey();
            Block block = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + rel.getBlockX(),
                    origin.getBlockY() + rel.getBlockY(),
                    origin.getBlockZ() + rel.getBlockZ()
            ).getBlock();
            if (block.getType() == Material.RED_CANDLE && block.getBlockData() instanceof Candle candleData && candleData.isLit()) {
                candleData.setLit(false);
                block.setBlockData(candleData);
            }
        }
    }

    public static boolean containsCandle(Location origin, Location candleLoc) {
        for (var entry : STRUCTURE_BLOCKS.entrySet()) {
            Location rel = entry.getKey();
            if (origin.getBlockX() + rel.getBlockX() == candleLoc.getBlockX()
                    && origin.getBlockY() + rel.getBlockY() == candleLoc.getBlockY()
                    && origin.getBlockZ() + rel.getBlockZ() == candleLoc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    public static Location getCenterLocation(Location origin) {
        return origin.clone().add(2, 0, 2);
    }

    public static double getRadius() {
        return 3.0;
    }
}
