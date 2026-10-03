package com.Chagui68.stand;

import com.Chagui68.utils.MscLog;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Which Stands are drawn with textured heads, and where their models come from.
 *
 * <p>A Stand uses the head model in {@code plugins/MultiverseCreatures/stands/<stand>.txt} when the
 * server has one: the {@code /summon} command BDEngine exports, pasted as it is. Without one it uses
 * the model the jar ships for it (The World), and without that the block figure of
 * {@link StandDesign}. So a new Stand model is a file away, with no code and no restart beyond
 * {@code /msc reload}.</p>
 */
public final class HeadModels {

    private static final Map<String, Optional<HeadModel>> CACHE = new HashMap<>();
    private static Plugin plugin;

    private HeadModels() {
    }

    /** Called once on enable: remembers the data folder and writes the folder's README. */
    public static synchronized void init(Plugin owner) {
        plugin = owner;
        CACHE.clear();
        File folder = new File(owner.getDataFolder(), "stands");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            owner.getLogger().warning("Could not create " + folder);
            return;
        }
        File readme = new File(folder, "README.txt");
        if (!readme.exists()) {
            try {
                Files.writeString(readme.toPath(), README, StandardCharsets.UTF_8);
            } catch (IOException e) {
                MscLog.warn("Could not write " + readme, e);
            }
        }
    }

    /** Forgets every model, so the files are read again ({@code /msc reload}). */
    public static synchronized void reset() {
        CACHE.clear();
    }

    /** The head model of a Stand, or null when it is drawn with blocks. */
    public static HeadModel of(StandType type) {
        return of(type.key());
    }

    /**
     * The head model called {@code key} ({@code dio-brando} for DIO himself, a Stand's key for a
     * Stand), or null when there is none.
     */
    public static synchronized HeadModel of(String key) {
        return CACHE.computeIfAbsent(key, HeadModels::load).orElse(null);
    }

    private static Optional<HeadModel> load(String key) {
        String name = key + ".txt";
        if (plugin != null) {
            File file = new File(new File(plugin.getDataFolder(), "stands"), name);
            if (file.isFile()) {
                try {
                    return Optional.of(HeadModel.parse(Files.readString(file.toPath(), StandardCharsets.UTF_8)));
                } catch (IOException | RuntimeException e) {
                    MscLog.warn("The model in " + file + " could not be read; using the built-in one", e);
                }
            }
        }
        try (InputStream in = HeadModels.class.getResourceAsStream("/stands/" + name)) {
            if (in != null) {
                return Optional.of(HeadModel.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            }
        } catch (IOException | RuntimeException e) {
            MscLog.warn("The built-in model " + name + " could not be read", e);
        }
        return Optional.empty();
    }

    private static final String README = """
            Stand models drawn with textured player heads
            =============================================

            Put a file here named after a Stand to draw it with player heads instead of blocks:

              hermit-purple.txt   magicians-red.txt   crazy-diamond.txt
              killer-queen.txt    star-platinum.txt   the-world.txt

            The file holds the /summon command BDEngine exports for an eleven-head humanoid
            (head, chest, belly, two upper arms, two forearms, two thighs and two shins), pasted
            exactly as it is. The pose it was exported in does not matter: the plugin stands the
            model up straight and animates it itself (breathing, the head following its user,
            the barrage punches).

            dio-brando.txt draws DIO himself, the boss, the same way.

            The World already ships with a model; a the-world.txt here replaces it. Use
            /msc reload after adding or changing a file. A file that cannot be read is reported
            in the console and the Stand keeps its previous model.
            """;
}
