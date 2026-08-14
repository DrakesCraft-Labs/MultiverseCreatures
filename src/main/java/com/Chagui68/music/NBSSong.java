package com.Chagui68.music;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NBSSong {

    public static class NoteEvent {
        public final int instrument;
        public final int key;
        public final int volume;

        public NoteEvent(int instrument, int key, int volume) {
            this.instrument = instrument;
            this.key = key;
            this.volume = volume;
        }
    }

    private final String title;
    private final int length;
    private final float speed;
    private final Map<Integer, List<NoteEvent>> notesByTick;

    public NBSSong(String title, float speed, Map<Integer, List<NoteEvent>> notesByTick, int length) {
        this.title = title;
        this.speed = speed;
        this.notesByTick = notesByTick;
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public int getLength() {
        return length;
    }

    public float getSpeed() {
        return speed;
    }

    public long getTickDelayMs() {
        return Math.max(25, (long) (1000.0f / speed));
    }

    public List<NoteEvent> getNotesAtTick(int tick) {
        return notesByTick.getOrDefault(tick, List.of());
    }

    public static NBSSong parse(File file) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

            int length = 0;
            int nbsVersion = 0;
            int firstCustomInstrument = 10;

            short firstShort = readShort(in);
            if (firstShort == 0) {
                nbsVersion = in.readByte() & 0xFF;
                firstCustomInstrument = in.readByte() & 0xFF;
                if (nbsVersion >= 3) {
                    length = readShort(in);
                }
            } else {
                length = firstShort;
            }

            int songHeight = readShort(in);
            String title = readString(in);
            String author = readString(in);
            readString(in);
            readString(in);
            float speed = readShort(in) / 100.0f;

            in.readBoolean();
            in.readByte();
            in.readByte();
            readInt(in);
            readInt(in);
            readInt(in);
            readInt(in);
            readInt(in);
            readString(in);

            if (nbsVersion >= 4) {
                in.readByte();
                in.readByte();
                readShort(in);
            }

            Map<Integer, List<NoteEvent>> notes = new HashMap<>();
            int currentTick = -1;

            while (true) {
                short jumpTicks = readShort(in);
                if (jumpTicks == 0) break;
                currentTick += jumpTicks;

                while (true) {
                    short jumpLayers = readShort(in);
                    if (jumpLayers == 0) break;

                    int instrument = in.readByte() & 0xFF;

                    if (firstCustomInstrument > 0 && instrument >= firstCustomInstrument) {
                        instrument += 0;
                    }

                    int key = in.readByte() & 0xFF;
                    int volume = 100;
                    if (nbsVersion >= 4) {
                        volume = in.readByte() & 0xFF;
                        in.readByte();
                        readShort(in);
                    }

                    notes.computeIfAbsent(currentTick, k -> new ArrayList<>())
                            .add(new NoteEvent(instrument, Math.min(key, 87), volume));
                }
            }

            if (nbsVersion > 0 && nbsVersion < 3) {
                length = currentTick;
            }

            if (length <= 0) length = currentTick + 1;

            return new NBSSong(title, speed, notes, length);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NBS file: " + file.getName(), e);
        }
    }

    private static short readShort(DataInputStream in) throws Exception {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (short) (b1 + (b2 << 8));
    }

    private static int readInt(DataInputStream in) throws Exception {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return b1 + (b2 << 8) + (b3 << 16) + (b4 << 24);
    }

    private static String readString(DataInputStream in) throws Exception {
        int len = readInt(in);
        if (len == 0) return "";
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        StringBuilder sb = new StringBuilder(len);
        for (byte b : bytes) {
            char c = (char) b;
            if (c == 0x0D) c = ' ';
            sb.append(c);
        }
        return sb.toString();
    }
}
