package com.Chagui68.worldgen;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Standalone NBT (Named Binary Tag) reader for Minecraft schematic files.
 * Supports GZip-compressed NBT as used by .schem and .litematic formats.
 * No external dependencies required.
 */
public class NBTReader {

    // NBT Tag Type IDs
    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;

    /**
     * Read an NBT file (GZip compressed) and return the root compound as a Map.
     */
    public static Map<String, Object> readFile(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))))) {
            int tagType = dis.readByte() & 0xFF;
            if (tagType != TAG_COMPOUND) {
                throw new IOException("Root tag is not a Compound (got type " + tagType + ")");
            }
            // Read root tag name (usually empty string "")
            dis.readUTF();
            return readCompound(dis);
        }
    }

    private static Map<String, Object> readCompound(DataInputStream dis) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        while (true) {
            int tagType = dis.readByte() & 0xFF;
            if (tagType == TAG_END) break;
            String name = dis.readUTF();
            Object value = readTag(dis, tagType);
            map.put(name, value);
        }
        return map;
    }

    private static Object readTag(DataInputStream dis, int tagType) throws IOException {
        return switch (tagType) {
            case TAG_BYTE -> dis.readByte();
            case TAG_SHORT -> dis.readShort();
            case TAG_INT -> dis.readInt();
            case TAG_LONG -> dis.readLong();
            case TAG_FLOAT -> dis.readFloat();
            case TAG_DOUBLE -> dis.readDouble();
            case TAG_BYTE_ARRAY -> {
                int len = dis.readInt();
                byte[] arr = new byte[len];
                dis.readFully(arr);
                yield arr;
            }
            case TAG_STRING -> dis.readUTF();
            case TAG_LIST -> readList(dis);
            case TAG_COMPOUND -> readCompound(dis);
            case TAG_INT_ARRAY -> {
                int len = dis.readInt();
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readInt();
                yield arr;
            }
            case TAG_LONG_ARRAY -> {
                int len = dis.readInt();
                long[] arr = new long[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readLong();
                yield arr;
            }
            default -> throw new IOException("Unknown NBT tag type: " + tagType);
        };
    }

    private static List<Object> readList(DataInputStream dis) throws IOException {
        int elementType = dis.readByte() & 0xFF;
        int length = dis.readInt();
        List<Object> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(readTag(dis, elementType));
        }
        return list;
    }

    // ========== UTILITY METHODS ==========

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCompound(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof Map) return (Map<String, Object>) val;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof List) return (List<Object>) val;
        return null;
    }

    public static int getInt(Map<String, Object> parent, String key, int defaultValue) {
        Object val = parent.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    public static short getShort(Map<String, Object> parent, String key, short defaultValue) {
        Object val = parent.get(key);
        if (val instanceof Number) return ((Number) val).shortValue();
        return defaultValue;
    }

    public static String getString(Map<String, Object> parent, String key, String defaultValue) {
        Object val = parent.get(key);
        if (val instanceof String) return (String) val;
        return defaultValue;
    }

    public static byte[] getByteArray(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof byte[]) return (byte[]) val;
        return null;
    }

    public static int[] getIntArray(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof int[]) return (int[]) val;
        return null;
    }

    public static long[] getLongArray(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof long[]) return (long[]) val;
        return null;
    }
}
