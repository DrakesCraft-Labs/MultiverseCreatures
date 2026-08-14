package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class RitualStructure {

    private static final Map<Location, Material> STRUCTURE_BLOCKS = new HashMap<>();
    private static final Map<Location, Material> CANDLE_LOCATIONS = new HashMap<>();

    static {
        Map<String, Material> blockMap = new HashMap<>();
        blockMap.put("0_0_1", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("0_0_2", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("0_0_3", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("0_0_4", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("0_0_5", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("1_0_0", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("1_0_1", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("1_0_2", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("1_0_3", Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("1_0_4", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("1_0_5", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("1_0_6", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("2_0_0", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("2_0_1", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("2_0_2", Material.CRYING_OBSIDIAN);
        blockMap.put("2_0_3", Material.POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("2_0_4", Material.CRYING_OBSIDIAN);
        blockMap.put("2_0_5", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("2_0_6", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("3_0_0", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("3_0_1", Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("3_0_2", Material.POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("3_0_3", Material.OBSIDIAN);
        blockMap.put("3_0_4", Material.POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("3_0_5", Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("3_0_6", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("4_0_0", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("4_0_1", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("4_0_2", Material.CRYING_OBSIDIAN);
        blockMap.put("4_0_3", Material.POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("4_0_4", Material.CRYING_OBSIDIAN);
        blockMap.put("4_0_5", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("4_0_6", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("5_0_0", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("5_0_1", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("5_0_2", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("5_0_3", Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        blockMap.put("5_0_4", Material.CHISELED_POLISHED_BLACKSTONE);
        blockMap.put("5_0_5", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("5_0_6", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("6_0_1", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("6_0_2", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("6_0_3", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("6_0_4", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("6_0_5", Material.POLISHED_BLACKSTONE_STAIRS);
        blockMap.put("1_1_2", Material.CANDLE);
        blockMap.put("1_1_3", Material.CANDLE);
        blockMap.put("1_1_4", Material.CANDLE);
        blockMap.put("2_1_1", Material.CANDLE);
        blockMap.put("2_1_2", Material.AIR);
        blockMap.put("2_1_3", Material.AIR);
        blockMap.put("2_1_4", Material.AIR);
        blockMap.put("2_1_5", Material.CANDLE);
        blockMap.put("3_1_1", Material.CANDLE);
        blockMap.put("3_1_2", Material.AIR);
        blockMap.put("3_1_3", Material.AIR);
        blockMap.put("3_1_4", Material.AIR);
        blockMap.put("3_1_5", Material.CANDLE);
        blockMap.put("4_1_1", Material.CANDLE);
        blockMap.put("4_1_2", Material.AIR);
        blockMap.put("4_1_3", Material.AIR);
        blockMap.put("4_1_4", Material.AIR);
        blockMap.put("4_1_5", Material.CANDLE);
        blockMap.put("5_1_2", Material.CANDLE);
        blockMap.put("5_1_3", Material.CANDLE);
        blockMap.put("5_1_4", Material.CANDLE);

        for (var entry : blockMap.entrySet()) {
            String[] parts = entry.getKey().split("_");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            STRUCTURE_BLOCKS.put(new Location(null, x, y, z), entry.getValue());

            if (entry.getValue() == Material.CANDLE) {
                CANDLE_LOCATIONS.put(new Location(null, x, y, z), Material.CANDLE);
            }
        }
    }

    public static boolean isStructureComplete(Location origin) {
        for (var entry : STRUCTURE_BLOCKS.entrySet()) {
            Material expected = entry.getValue();
            Location relative = entry.getKey();
            Location blockLoc = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + relative.getBlockX(),
                    origin.getBlockY() + relative.getBlockY(),
                    origin.getBlockZ() + relative.getBlockZ()
            );
            Block block = blockLoc.getBlock();

            if (expected == Material.CANDLE) {
                if (block.getType() != Material.CANDLE && block.getType() != Material.CANDLE_CAKE) {
                    return false;
                }
            } else {
                if (block.getType() != expected) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean areAllCandlesLit(Location origin) {
        for (var entry : CANDLE_LOCATIONS.entrySet()) {
            Location relative = entry.getKey();
            Location blockLoc = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + relative.getBlockX(),
                    origin.getBlockY() + relative.getBlockY(),
                    origin.getBlockZ() + relative.getBlockZ()
            );
            Block block = blockLoc.getBlock();

            if (block.getType() != Material.CANDLE) {
                return false;
            }

            org.bukkit.block.data.type.Candle candleData = (org.bukkit.block.data.type.Candle) block.getBlockData();
            if (!candleData.isLit()) {
                return false;
            }
        }
        return true;
    }

    public static void extinguishAllCandles(Location origin) {
        for (var entry : CANDLE_LOCATIONS.entrySet()) {
            Location relative = entry.getKey();
            Location blockLoc = new Location(
                    origin.getWorld(),
                    origin.getBlockX() + relative.getBlockX(),
                    origin.getBlockY() + relative.getBlockY(),
                    origin.getBlockZ() + relative.getBlockZ()
            );
            Block block = blockLoc.getBlock();

            if (block.getType() == Material.CANDLE) {
                org.bukkit.block.data.type.Candle candleData = (org.bukkit.block.data.type.Candle) block.getBlockData();
                if (candleData.isLit()) {
                    candleData.setLit(false);
                    block.setBlockData(candleData);
                }
            }
        }
    }

    public static Location getCenterLocation(Location origin) {
        return origin.clone().add(3, 0, 3);
    }

    public static double getRadius() {
        return 5.0;
    }

    public static Map<Location, Material> getCandleLocations() {
        return CANDLE_LOCATIONS;
    }
}