package com.Chagui68.music;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MusicManager {

    private static final Sound[] INSTRUMENTS = {
            Sound.BLOCK_NOTE_BLOCK_HARP,           // 0: Piano
            Sound.BLOCK_NOTE_BLOCK_BASS,           // 1: Bass Guitar
            Sound.BLOCK_NOTE_BLOCK_BASEDRUM,       // 2: Bass Drum
            Sound.BLOCK_NOTE_BLOCK_SNARE,          // 3: Snare
            Sound.BLOCK_NOTE_BLOCK_HAT,            // 4: Click
            Sound.BLOCK_NOTE_BLOCK_GUITAR,         // 5: Guitar
            Sound.BLOCK_NOTE_BLOCK_FLUTE,          // 6: Flute
            Sound.BLOCK_NOTE_BLOCK_BELL,           // 7: Bell
            Sound.BLOCK_NOTE_BLOCK_CHIME,          // 8: Chime
            Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,      // 9: Xylophone
            Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, // 10: Iron Xylophone
            Sound.BLOCK_NOTE_BLOCK_COW_BELL,       // 11: Cow Bell
            Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,     // 12: Didgeridoo
            Sound.BLOCK_NOTE_BLOCK_BIT,            // 13: Bit
            Sound.BLOCK_NOTE_BLOCK_BANJO,          // 14: Banjo
            Sound.BLOCK_NOTE_BLOCK_PLING,          // 15: Pling
    };

    private final MultiverseCreatures plugin;
    private final Map<String, NBSSong> songs = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public MusicManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
        loadSongs();
    }

    private void loadSongs() {
        File musicDir = new File(plugin.getDataFolder(), "music");
        if (!musicDir.exists()) musicDir.mkdirs();
        extractDefaultSongs(musicDir);
        File[] files = musicDir.listFiles((dir, name) -> name.endsWith(".nbs"));
        if (files != null) {
            for (File f : files) {
                try {
                    NBSSong song = NBSSong.parse(f);
                    songs.put(f.getName().replace(".nbs", "").toLowerCase(), song);
                    plugin.getLogger().info("Loaded song: " + f.getName().replace(".nbs", ""));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load song: " + f.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    private void extractDefaultSongs(File musicDir) {
        java.util.Set<String> jarSongs = new java.util.HashSet<>();
        try {
            java.net.URL jarUrl = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            java.nio.file.Path path = java.nio.file.Paths.get(jarUrl.toURI());

            if (java.nio.file.Files.isDirectory(path)) {
                java.nio.file.Path musicPath = path.resolve("music");
                if (!java.nio.file.Files.exists(musicPath)) return;
                java.nio.file.Files.list(musicPath)
                        .filter(p -> p.toString().endsWith(".nbs"))
                        .forEach(p -> {
                            String fileName = p.getFileName().toString();
                            jarSongs.add(fileName);
                            File target = new File(musicDir, fileName);
                            try {
                                java.nio.file.Files.copy(p, target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (Exception ignored) {
                            }
                        });
            } else {
                try (java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(path, (ClassLoader) null)) {
                    java.nio.file.Path musicPath = fs.getPath("/music");
                    if (!java.nio.file.Files.exists(musicPath)) return;
                    java.nio.file.Files.list(musicPath)
                            .filter(p -> p.toString().endsWith(".nbs"))
                            .forEach(p -> {
                                String fileName = p.getFileName().toString();
                                jarSongs.add(fileName);
                                plugin.saveResource("music/" + fileName, true);
                            });
                }
            }
        } catch (Exception ignored) {
        }

        if (!jarSongs.isEmpty()) {
            File[] existing = musicDir.listFiles((dir, name) -> name.endsWith(".nbs"));
            if (existing != null) {
                for (File f : existing) {
                    if (!jarSongs.contains(f.getName())) {
                        f.delete();
                        plugin.getLogger().info("Removed stale song: " + f.getName());
                    }
                }
            }
        }
    }

    public void play(String songName, Player player, boolean loop) {
        stop(player);

        NBSSong song = songs.get(songName.toLowerCase());
        if (song == null) {
            player.sendMessage(ChatColor.RED + "Song not found. Use /msc music list to see available songs.");
            return;
        }

        NBSSong finalSong = song;
        long period = Math.max(1, Math.round(20.0 / song.getSpeed()));
        plugin.getLogger().info("Playing " + songName + " (period=" + period + " ticks, speed=" + song.getSpeed() + ")");

        BukkitTask task = new BukkitRunnable() {
            int currentTick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(player.getUniqueId());
                    return;
                }
                List<NBSSong.NoteEvent> notes = finalSong.getNotesAtTick(currentTick);
                for (NBSSong.NoteEvent note : notes) {
                    playNote(player, note);
                }
                currentTick++;
                if (currentTick >= finalSong.getLength()) {
                    if (loop) {
                        currentTick = 0;
                    } else {
                        cancel();
                        activeTasks.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, period);

        activeTasks.put(player.getUniqueId(), task);

        player.sendMessage(ChatColor.GREEN + "Now playing: " + ChatColor.GOLD + songName
                + (loop ? ChatColor.GRAY + " (loop)" : ""));
    }

    private void playNote(Player player, NBSSong.NoteEvent note) {
        Sound sound = instrumentSound(note.instrument);
        if (sound == null) return;
        float pitch = (float) Math.pow(2.0, (note.key - 45) / 12.0);
        float volume = Math.min(1.0f, note.volume / 100.0f);
        if (volume < 0.01f) volume = 0.01f;
        player.playSound(player.getEyeLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    private Sound instrumentSound(int instrument) {
        if (instrument >= 0 && instrument < INSTRUMENTS.length) {
            return INSTRUMENTS[instrument];
        }
        return Sound.BLOCK_NOTE_BLOCK_PLING;
    }

    public void stop(Player player) {
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    public void stopAll() {
        for (UUID id : new ArrayList<>(activeTasks.keySet())) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) stop(p);
        }
    }

    public List<String> getSongNames() {
        return new ArrayList<>(songs.keySet());
    }

    public String getSongTitle(String songName) {
        NBSSong song = songs.get(songName.toLowerCase());
        if (song == null) return songName;
        String title = song.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = prettifyKey(songName);
        }
        return title;
    }

    private static String prettifyKey(String key) {
        String name = key.replace(".nbs", "").replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : name.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1)).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Plays a song at a world location to every player inside the given
     * radius, repeating until the task is cancelled or the song ends (if
     * loop is false). Used by jukebox music discs.
     */
    public BukkitTask playAt(String songName, Location loc, double radius, boolean loop) {
        NBSSong song = songs.get(songName.toLowerCase());
        if (song == null || loc.getWorld() == null) return null;

        long period = Math.max(1, Math.round(20.0 / song.getSpeed()));
        return new BukkitRunnable() {
            int currentTick = 0;

            @Override
            public void run() {
                if (loc.getWorld() == null) {
                    cancel();
                    return;
                }
                List<NBSSong.NoteEvent> notes = song.getNotesAtTick(currentTick);
                if (!notes.isEmpty()) {
                    double r2 = radius * radius;
                    for (Player p : loc.getWorld().getPlayers()) {
                        if (p.getLocation().distanceSquared(loc) <= r2) {
                            for (NBSSong.NoteEvent note : notes) {
                                playNote(p, note);
                            }
                        }
                    }
                }
                currentTick++;
                if (currentTick >= song.getLength()) {
                    if (loop) {
                        currentTick = 0;
                    } else {
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, period);
    }

    public boolean isPlaying(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }
}
