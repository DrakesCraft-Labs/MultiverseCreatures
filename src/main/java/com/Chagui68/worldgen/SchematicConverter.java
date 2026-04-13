package com.Chagui68.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Converts .schem (Sponge/WorldEdit) and .litematic (Litematica) files
 * to the plugin's simple JSON schematic format.
 *
 * Output format:
 * {
 *   "width": N, "height": N, "length": N,
 *   "blocks": [ { "x":0, "y":0, "z":0, "material":"STONE", "blockData":"facing=north" }, ... ]
 * }
 */
public class SchematicConverter {

    private static final Set<String> SKIP_BLOCKS = Set.of(
            "AIR", "CAVE_AIR", "VOID_AIR", "STRUCTURE_VOID"
    );

    /**
     * Result of a conversion, holding the JSON string and stats.
     */
    public static class ConversionResult {
        public final String json;
        public final int blockCount;
        public final int width;
        public final int height;
        public final int length;

        public ConversionResult(String json, int blockCount, int width, int height, int length) {
            this.json = json;
            this.blockCount = blockCount;
            this.width = width;
            this.height = height;
            this.length = length;
        }
    }

    /**
     * Auto-detect format by extension and convert to JSON string.
     */
    public static ConversionResult convert(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".schem") || name.endsWith(".schematic")) {
            return convertSponge(file);
        } else if (name.endsWith(".litematic")) {
            return convertLitematic(file);
        } else {
            throw new IOException("Unsupported schematic format: " + name);
        }
    }

    /**
     * Convert and write the JSON to an output file.
     */
    public static ConversionResult convertAndSave(File input, File output) throws IOException {
        ConversionResult result = convert(input);
        try (FileWriter w = new FileWriter(output)) {
            w.write(result.json);
        }
        return result;
    }

    // ==================== SPONGE (.schem) ====================

    @SuppressWarnings("unchecked")
    private static ConversionResult convertSponge(File file) throws IOException {
        Map<String, Object> root = NBTReader.readFile(file);

        // Sponge v3 wraps in "Schematic" compound, v2 is flat
        Map<String, Object> schem = NBTReader.getCompound(root, "Schematic");
        if (schem == null) schem = root;

        int width = getShortOrInt(schem, "Width");
        int height = getShortOrInt(schem, "Height");
        int length = getShortOrInt(schem, "Length");

        // Read palette: v2 uses "Palette", v3 uses "BlockPalette"
        // The palette maps block state strings -> integer IDs
        Map<String, Object> paletteRaw = NBTReader.getCompound(schem, "Palette");
        if (paletteRaw == null) paletteRaw = NBTReader.getCompound(schem, "BlockPalette");

        if (paletteRaw == null) {
            throw new IOException("No Palette/BlockPalette found in .schem file");
        }

        // Invert: ID -> block state string
        Map<Integer, String> idToBlock = new HashMap<>();
        for (Map.Entry<String, Object> entry : paletteRaw.entrySet()) {
            int id = ((Number) entry.getValue()).intValue();
            idToBlock.put(id, entry.getKey());
        }

        // Read block data (varint-encoded byte array)
        byte[] blockData = NBTReader.getByteArray(schem, "BlockData");
        if (blockData == null) {
            throw new IOException("No BlockData found in .schem file");
        }

        // Decode varint array
        int[] blockIds = decodeVarintArray(blockData, width * height * length);

        // Build JSON
        JsonObject result = new JsonObject();
        result.addProperty("width", width);
        result.addProperty("height", height);
        result.addProperty("length", length);

        JsonArray blocksArray = new JsonArray();
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * length + z) * width + x;
                    if (index >= blockIds.length) continue;
                    int id = blockIds[index];

                    String blockState = idToBlock.get(id);
                    if (blockState == null) continue;

                    BlockInfo info = parseBlockState(blockState);
                    if (SKIP_BLOCKS.contains(info.material)) continue;

                    JsonObject blockObj = new JsonObject();
                    blockObj.addProperty("x", x);
                    blockObj.addProperty("y", y);
                    blockObj.addProperty("z", z);
                    blockObj.addProperty("material", info.material);
                    if (info.properties != null && !info.properties.isEmpty()) {
                        blockObj.addProperty("blockData", info.properties);
                    }
                    blocksArray.add(blockObj);
                    count++;
                }
            }
        }

        result.add("blocks", blocksArray);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return new ConversionResult(gson.toJson(result), count, width, height, length);
    }

    /**
     * Decode varint-encoded byte array into an int array.
     * Sponge schematic format uses variable-length integers for block IDs.
     */
    private static int[] decodeVarintArray(byte[] data, int expectedSize) {
        List<Integer> result = new ArrayList<>(expectedSize);
        int i = 0;
        while (i < data.length) {
            int value = 0;
            int shift = 0;
            while (true) {
                if (i >= data.length) {
                    break;
                }
                int b = data[i++] & 0xFF;
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) break;
                shift += 7;
                if (i >= data.length) {
                    break;
                }
            }
            result.add(value);
        }
        // Verify we didn't exceed expected size
        if (result.size() > expectedSize) {
            throw new IllegalArgumentException("Decoded varint array (" + result.size() + 
                    ") exceeds expected size (" + expectedSize + ")");
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    // ==================== LITEMATIC (.litematic) ====================

    @SuppressWarnings("unchecked")
    private static ConversionResult convertLitematic(File file) throws IOException {
        Map<String, Object> root = NBTReader.readFile(file);

        // Get metadata for total dimensions
        Map<String, Object> metadata = NBTReader.getCompound(root, "Metadata");
        Map<String, Object> enclosingSize = metadata != null ? NBTReader.getCompound(metadata, "EnclosingSize") : null;

        int totalWidth = 0, totalHeight = 0, totalLength = 0;
        if (enclosingSize != null) {
            totalWidth = NBTReader.getInt(enclosingSize, "x", 0);
            totalHeight = NBTReader.getInt(enclosingSize, "y", 0);
            totalLength = NBTReader.getInt(enclosingSize, "z", 0);
        }

        // Get regions
        Map<String, Object> regions = NBTReader.getCompound(root, "Regions");
        if (regions == null) {
            throw new IOException("No Regions compound found in .litematic file");
        }

        JsonArray allBlocks = new JsonArray();
        int totalCount = 0;

        // Process each region
        for (Map.Entry<String, Object> regionEntry : regions.entrySet()) {
            if (!(regionEntry.getValue() instanceof Map)) continue;
            Map<String, Object> region = (Map<String, Object>) regionEntry.getValue();

            // Get region position offset
            Map<String, Object> position = NBTReader.getCompound(region, "Position");
            int offsetX = position != null ? NBTReader.getInt(position, "x", 0) : 0;
            int offsetY = position != null ? NBTReader.getInt(position, "y", 0) : 0;
            int offsetZ = position != null ? NBTReader.getInt(position, "z", 0) : 0;

            // Get region size (can be negative!)
            Map<String, Object> size = NBTReader.getCompound(region, "Size");
            if (size == null) continue;
            int sizeX = NBTReader.getInt(size, "x", 0);
            int sizeY = NBTReader.getInt(size, "y", 0);
            int sizeZ = NBTReader.getInt(size, "z", 0);

            // Handle negative sizes — if size is negative, the origin/offset needs adjusting
            int regionW = Math.abs(sizeX);
            int regionH = Math.abs(sizeY);
            int regionL = Math.abs(sizeZ);
            // Adjust offset for negative dimensions
            int adjX = sizeX < 0 ? offsetX + sizeX + 1 : offsetX;
            int adjY = sizeY < 0 ? offsetY + sizeY + 1 : offsetY;
            int adjZ = sizeZ < 0 ? offsetZ + sizeZ + 1 : offsetZ;

            if (totalWidth == 0) totalWidth = regionW;
            if (totalHeight == 0) totalHeight = regionH;
            if (totalLength == 0) totalLength = regionL;

            // Read block state palette
            List<Object> palette = NBTReader.getList(region, "BlockStatePalette");
            if (palette == null || palette.isEmpty()) continue;

            // Build palette index -> block info
            String[] paletteEntries = new String[palette.size()];
            String[] paletteProperties = new String[palette.size()];
            for (int i = 0; i < palette.size(); i++) {
                if (palette.get(i) instanceof Map) {
                    Map<String, Object> entry = (Map<String, Object>) palette.get(i);
                    String blockName = NBTReader.getString(entry, "Name", "minecraft:air");
                    BlockInfo info = parseBlockState(blockName);
                    paletteEntries[i] = info.material;

                    // Also extract properties from the Properties compound
                    Map<String, Object> props = NBTReader.getCompound(entry, "Properties");
                    if (props != null && !props.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, Object> prop : props.entrySet()) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(prop.getKey()).append("=").append(prop.getValue());
                        }
                        paletteProperties[i] = sb.toString();
                    } else {
                        paletteProperties[i] = info.properties;
                    }
                }
            }

            // Read bit-packed block states
            long[] blockStates = NBTReader.getLongArray(region, "BlockStates");
            if (blockStates == null) continue;

            int volume = regionW * regionH * regionL;
            int bitsPerBlock = Math.max(2, ceilLog2(palette.size()));

            // Decode bit-packed data
            for (int idx = 0; idx < volume; idx++) {
                int paletteId = getBitPackedValue(blockStates, idx, bitsPerBlock);
                if (paletteId < 0 || paletteId >= paletteEntries.length) continue;

                String material = paletteEntries[paletteId];
                if (material == null || SKIP_BLOCKS.contains(material)) continue;

                // Calculate XYZ from linear index
                int x = idx % regionW;
                int z = (idx / regionW) % regionL;
                int y = idx / (regionW * regionL);

                JsonObject blockObj = new JsonObject();
                blockObj.addProperty("x", x + adjX);
                blockObj.addProperty("y", y + adjY);
                blockObj.addProperty("z", z + adjZ);
                blockObj.addProperty("material", material);
                if (paletteProperties[paletteId] != null && !paletteProperties[paletteId].isEmpty()) {
                    blockObj.addProperty("blockData", paletteProperties[paletteId]);
                }
                allBlocks.add(blockObj);
                totalCount++;
            }
        }

        // Normalize positions to start at (0,0,0)
        normalizePositions(allBlocks);

        JsonObject result = new JsonObject();
        result.addProperty("width", totalWidth);
        result.addProperty("height", totalHeight);
        result.addProperty("length", totalLength);
        result.add("blocks", allBlocks);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return new ConversionResult(gson.toJson(result), totalCount, totalWidth, totalHeight, totalLength);
    }

    /**
     * Extract a value from a bit-packed long array.
     * Uses the original "spanning" format that Litematica uses internally.
     */
    private static int getBitPackedValue(long[] data, int index, int bitsPerBlock) {
        long mask = (1L << bitsPerBlock) - 1;
        int bitIndex = index * bitsPerBlock;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;

        if (longIndex >= data.length) return 0;

        long value = (data[longIndex] >>> bitOffset) & mask;

        // Handle values that span two longs
        if (bitOffset + bitsPerBlock > 64 && longIndex + 1 < data.length) {
            int bitsInFirstLong = 64 - bitOffset;
            long highBits = data[longIndex + 1] & ((1L << (bitsPerBlock - bitsInFirstLong)) - 1);
            value |= highBits << bitsInFirstLong;
        }

        return (int) value;
    }

    /**
     * Shift all block positions so the minimum is at (0, 0, 0).
     */
    private static void normalizePositions(JsonArray blocks) {
        if (blocks.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (int i = 0; i < blocks.size(); i++) {
            JsonObject b = blocks.get(i).getAsJsonObject();
            minX = Math.min(minX, b.get("x").getAsInt());
            minY = Math.min(minY, b.get("y").getAsInt());
            minZ = Math.min(minZ, b.get("z").getAsInt());
        }

        if (minX == 0 && minY == 0 && minZ == 0) return;

        for (int i = 0; i < blocks.size(); i++) {
            JsonObject b = blocks.get(i).getAsJsonObject();
            b.addProperty("x", b.get("x").getAsInt() - minX);
            b.addProperty("y", b.get("y").getAsInt() - minY);
            b.addProperty("z", b.get("z").getAsInt() - minZ);
        }
    }

    // ==================== SHARED UTILITIES ====================

    private static class BlockInfo {
        String material;
        String properties;
    }

    /**
     * Parse a block state string like "minecraft:oak_stairs[facing=north,half=top]"
     * into a material name ("OAK_STAIRS") and property string ("facing=north,half=top").
     */
    private static BlockInfo parseBlockState(String blockState) {
        BlockInfo info = new BlockInfo();

        // Strip namespace
        String raw = blockState;
        if (raw.contains(":")) {
            raw = raw.substring(raw.indexOf(':') + 1);
        }

        // Extract properties
        int bracket = raw.indexOf('[');
        if (bracket >= 0) {
            info.properties = raw.substring(bracket + 1, raw.length() - 1);
            raw = raw.substring(0, bracket);
        }

        info.material = raw.toUpperCase(Locale.ROOT);
        return info;
    }

    /**
     * Ceiling of log base 2. Returns the number of bits needed to represent values 0..n-1.
     */
    private static int ceilLog2(int n) {
        if (n <= 1) return 1;
        return 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    /**
     * Get a dimension value that might be stored as Short or Int in NBT.
     */
    private static int getShortOrInt(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }
}
