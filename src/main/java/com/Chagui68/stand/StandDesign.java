package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Part;
import org.bukkit.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.*;

/**
 * What each Stand looks like: every box of its figure, the block it is made of and the part of
 * the skeleton it belongs to.
 *
 * <p>All Stands share one athletic frame (see {@link StandRig} for the axes and the joints): a
 * broad chest, a narrow waist, two-segment arms ending in clenched fists and legs that fade into
 * the Stand's aura from the knee down, the way Stands are drawn. On top of it each one wears its
 * own head and details: Star Platinum's mane and red scarf, The World's helmet with its breathing
 * tubes and emerald hearts, Magician's Red's beak and plumes, Crazy Diamond's hearts and diamond,
 * Killer Queen's cat ears and skulls.</p>
 */
public final class StandDesign {

    /** One box of a Stand. {@code bright} boxes (eyes, flames) are the ones that read as glowing. */
    public record Piece(Material material, Part part, Vector3f center, Vector3f size, Quaternionf spin,
                        boolean bright) {
    }

    /** The colours of the shared frame. */
    private record Palette(Material skin, Material shade, Material fist, Material band, Material legs,
                           Material aura) {
    }

    private StandDesign() {
    }

    /** The boxes of a Stand, or an empty list for Hermit Purple, which has no body. */
    public static List<Piece> of(StandType type) {
        Sculpt s = new Sculpt();
        switch (type) {
            case STAR_PLATINUM -> starPlatinum(s);
            case THE_WORLD -> theWorld(s);
            case MAGICIANS_RED -> magiciansRed(s);
            case CRAZY_DIAMOND -> crazyDiamond(s);
            case KILLER_QUEEN -> killerQueen(s);
            case HERMIT_PURPLE -> {
            }
        }
        return s.pieces;
    }

    /** The block of the Stand's fists, used for the afterimages of a barrage. */
    public static Material fist(StandType type) {
        return palette(type).fist();
    }

    /** The stained glass the Stand fades into below the knee. */
    public static Material aura(StandType type) {
        return palette(type).aura();
    }

    private static Palette palette(StandType type) {
        return switch (type) {
            case STAR_PLATINUM -> new Palette(LIGHT_BLUE_TERRACOTTA, BLUE_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                    GOLD_BLOCK, LIGHT_BLUE_TERRACOTTA, PURPLE_STAINED_GLASS);
            case THE_WORLD -> new Palette(YELLOW_TERRACOTTA, GOLD_BLOCK, YELLOW_TERRACOTTA, GOLD_BLOCK,
                    YELLOW_TERRACOTTA, YELLOW_STAINED_GLASS);
            case MAGICIANS_RED -> new Palette(RED_TERRACOTTA, BROWN_TERRACOTTA, RED_TERRACOTTA, SHROOMLIGHT,
                    RED_TERRACOTTA, RED_STAINED_GLASS);
            case CRAZY_DIAMOND -> new Palette(PINK_CONCRETE, MAGENTA_TERRACOTTA, PINK_CONCRETE, LIGHT_BLUE_CONCRETE,
                    PINK_CONCRETE, PINK_STAINED_GLASS);
            case KILLER_QUEEN -> new Palette(WHITE_TERRACOTTA, PURPLE_TERRACOTTA, WHITE_TERRACOTTA, PURPLE_TERRACOTTA,
                    WHITE_TERRACOTTA, MAGENTA_STAINED_GLASS);
            case HERMIT_PURPLE -> new Palette(PURPLE_CONCRETE, PURPLE_CONCRETE, PURPLE_CONCRETE, PURPLE_CONCRETE,
                    PURPLE_CONCRETE, PURPLE_STAINED_GLASS);
        };
    }

    // ---------------------------------------------------------------- the frame

    /** The body every Stand shares, without the head. */
    private static void frame(Sculpt s, Palette p) {
        // Trunk: chest, pecs, abdomen and its six blocks, waist, neck, trapezius and lats.
        s.box(Part.BODY, p.skin(), 0, 1.28f, 0, 0.60f, 0.32f, 0.32f);
        s.pair(Part.BODY, p.skin(), 0.135f, 1.31f, 0.165f, 0.25f, 0.17f, 0.045f);
        s.box(Part.BODY, p.shade(), 0, 1.01f, 0, 0.46f, 0.22f, 0.26f);
        for (float y : new float[]{1.075f, 1.01f, 0.945f}) {
            s.pair(Part.BODY, p.skin(), 0.06f, y, 0.135f, 0.09f, 0.05f, 0.02f);
        }
        s.box(Part.BODY, p.shade(), 0, 0.83f, 0, 0.48f, 0.14f, 0.28f);
        s.box(Part.BODY, p.skin(), 0, 1.47f, 0, 0.17f, 0.09f, 0.17f);
        s.pair(Part.BODY, p.skin(), 0.17f, 1.445f, -0.02f, 0.17f, 0.06f, 0.2f, 0, 0, -14f);
        s.pair(Part.BODY, p.shade(), 0.22f, 1.17f, -0.05f, 0.12f, 0.24f, 0.22f);

        // Arms: deltoid, upper arm and biceps; forearm, band, fist, knuckles and thumb.
        s.pair(Part.ARM_L, p.skin(), 0.42f, 1.33f, 0, 0.25f, 0.22f, 0.27f);
        s.pair(Part.ARM_L, p.skin(), 0.42f, 1.13f, 0, 0.18f, 0.30f, 0.18f);
        s.pair(Part.ARM_L, p.skin(), 0.42f, 1.14f, 0.075f, 0.13f, 0.16f, 0.06f);
        s.pair(Part.FOREARM_L, p.skin(), 0.42f, 0.86f, 0, 0.17f, 0.25f, 0.17f);
        s.pair(Part.FOREARM_L, p.band(), 0.42f, 0.755f, 0, 0.195f, 0.05f, 0.195f);
        s.pair(Part.FOREARM_L, p.fist(), 0.42f, 0.66f, 0.01f, 0.20f, 0.16f, 0.19f);
        s.pair(Part.FOREARM_L, p.shade(), 0.42f, 0.665f, 0.105f, 0.18f, 0.055f, 0.03f);
        s.pair(Part.FOREARM_L, p.fist(), 0.325f, 0.69f, 0.06f, 0.04f, 0.08f, 0.06f);

        // Legs: thigh and knee; the shin and the foot are the aura they fade into.
        s.pair(Part.THIGH_L, p.legs(), 0.14f, 0.59f, 0, 0.21f, 0.37f, 0.21f);
        s.pair(Part.SHIN_L, p.aura(), 0.14f, 0.23f, 0, 0.17f, 0.34f, 0.17f);
        s.pair(Part.SHIN_L, p.aura(), 0.14f, 0.04f, 0.05f, 0.17f, 0.08f, 0.27f);
    }

    /** The head box, two eyes with pupils, the brows, the nose and the mouth. */
    private static void face(Sculpt s, Material skin, Material eye, Material pupil, Material brow, float tilt) {
        s.box(Part.HEAD, skin, 0, 1.70f, 0, 0.40f, 0.40f, 0.40f);
        s.glowPair(Part.HEAD, eye, 0.09f, 1.725f, 0.205f, 0.10f, 0.045f, 0.02f, 0, 0, tilt);
        if (pupil != null) {
            s.glowPair(Part.HEAD, pupil, 0.09f, 1.725f, 0.214f, 0.035f, 0.045f, 0.01f, 0, 0, tilt);
        }
        s.pair(Part.HEAD, brow, 0.09f, 1.765f, 0.207f, 0.12f, 0.025f, 0.02f, 0, 0, tilt);
        s.box(Part.HEAD, skin, 0, 1.665f, 0.215f, 0.05f, 0.08f, 0.03f);
    }

    /** A heart drawn as a small diamond on the front of whatever it is placed on. */
    private static void heart(Sculpt s, Part part, Material material, float x, float y, float z, float size) {
        s.boxSpin(part, material, x, y, z, size, size, 0.02f, 0, 0, 45f);
    }

    // ------------------------------------------------------------- Star Platinum

    private static void starPlatinum(Sculpt s) {
        Palette p = palette(StandType.STAR_PLATINUM);
        frame(s, p);
        face(s, p.skin(), WHITE_CONCRETE, CYAN_CONCRETE, BLACK_CONCRETE, 6f);
        s.box(Part.HEAD, BLUE_TERRACOTTA, 0, 1.60f, 0.205f, 0.12f, 0.02f, 0.02f);
        // The mane: crown, fringe, sides, the back and the spikes swept behind.
        s.box(Part.HEAD, BLACK_CONCRETE, 0, 1.93f, -0.02f, 0.44f, 0.08f, 0.44f);
        s.box(Part.HEAD, BLACK_CONCRETE, 0, 1.865f, 0.195f, 0.42f, 0.07f, 0.05f);
        s.pair(Part.HEAD, BLACK_CONCRETE, 0.215f, 1.75f, -0.04f, 0.04f, 0.30f, 0.34f);
        s.box(Part.HEAD, BLACK_CONCRETE, 0, 1.70f, -0.22f, 0.44f, 0.44f, 0.06f);
        s.boxSpin(Part.HEAD, BLACK_CONCRETE, 0, 1.47f, -0.27f, 0.36f, 0.32f, 0.06f, 14f, 0, 0);
        s.boxSpin(Part.HEAD, BLACK_CONCRETE, 0, 1.99f, -0.16f, 0.11f, 0.16f, 0.11f, -35f, 0, 0);
        s.pairSpin(Part.HEAD, BLACK_CONCRETE, 0.13f, 1.96f, -0.18f, 0.10f, 0.15f, 0.10f, -40f, 0, -22f);
        s.pairSpin(Part.HEAD, BLACK_CONCRETE, 0.16f, 1.62f, -0.24f, 0.09f, 0.30f, 0.06f, 18f, 0, -14f);
        // The gold band across the forehead.
        s.box(Part.HEAD, GOLD_BLOCK, 0, 1.835f, 0.212f, 0.34f, 0.04f, 0.02f);
        // The red scarf round the neck and its tail in the wind.
        s.box(Part.BODY, RED_CONCRETE, 0, 1.475f, 0, 0.27f, 0.10f, 0.27f);
        s.boxSpin(Part.BODY, RED_CONCRETE, 0.08f, 1.33f, -0.21f, 0.11f, 0.30f, 0.04f, 28f, 0, -8f);
        s.boxSpin(Part.BODY, RED_CONCRETE, -0.06f, 1.27f, -0.23f, 0.09f, 0.34f, 0.04f, 34f, 0, 10f);
        // Gold shoulder guards with a darker rim.
        s.pair(Part.ARM_L, GOLD_BLOCK, 0.43f, 1.405f, 0, 0.28f, 0.10f, 0.30f);
        s.pair(Part.ARM_L, YELLOW_TERRACOTTA, 0.43f, 1.35f, 0, 0.30f, 0.03f, 0.32f);
        // Belt, the loincloth before and behind, the knee guards.
        s.box(Part.BODY, GOLD_BLOCK, 0, 0.905f, 0, 0.50f, 0.05f, 0.30f);
        s.boxSpin(Part.BODY, RED_CONCRETE, 0, 0.70f, 0.15f, 0.22f, 0.28f, 0.03f, -8f, 0, 0);
        s.boxSpin(Part.BODY, RED_CONCRETE, 0, 0.70f, -0.15f, 0.30f, 0.28f, 0.03f, 8f, 0, 0);
        s.pair(Part.THIGH_L, GOLD_BLOCK, 0.14f, 0.43f, 0.08f, 0.17f, 0.12f, 0.07f);
    }

    // ------------------------------------------------------------------ The World

    private static void theWorld(Sculpt s) {
        Palette p = palette(StandType.THE_WORLD);
        frame(s, p);
        face(s, p.skin(), WHITE_CONCRETE, null, GOLD_BLOCK, 12f);
        // The helmet: crown, ridge, back and sides, the emerald set in the brow.
        s.box(Part.HEAD, GOLD_BLOCK, 0, 1.885f, -0.02f, 0.44f, 0.10f, 0.44f);
        s.box(Part.HEAD, GOLD_BLOCK, 0, 1.96f, 0.02f, 0.07f, 0.07f, 0.36f);
        s.box(Part.HEAD, GOLD_BLOCK, 0, 1.72f, -0.215f, 0.44f, 0.38f, 0.04f);
        s.pair(Part.HEAD, GOLD_BLOCK, 0.215f, 1.74f, -0.05f, 0.03f, 0.28f, 0.30f);
        s.pair(Part.HEAD, GOLD_BLOCK, 0.22f, 1.66f, 0.02f, 0.03f, 0.10f, 0.08f);
        heart(s, Part.HEAD, EMERALD_BLOCK, 0, 1.835f, 0.22f, 0.08f);
        // The mouth guard and the two breathing tubes running back along the cheeks.
        s.box(Part.HEAD, GOLD_BLOCK, 0, 1.585f, 0.19f, 0.26f, 0.10f, 0.05f);
        s.pair(Part.HEAD, LIGHT_GRAY_CONCRETE, 0.175f, 1.585f, 0.02f, 0.045f, 0.045f, 0.36f);
        s.pair(Part.HEAD, GRAY_CONCRETE, 0.175f, 1.585f, 0.205f, 0.06f, 0.06f, 0.03f);
        // Gold plate over the chest with an emerald seam; shoulders with an emerald stud.
        s.pair(Part.BODY, GOLD_BLOCK, 0.135f, 1.31f, 0.17f, 0.26f, 0.18f, 0.045f);
        s.box(Part.BODY, EMERALD_BLOCK, 0, 1.24f, 0.172f, 0.03f, 0.24f, 0.02f);
        s.pair(Part.ARM_L, GOLD_BLOCK, 0.43f, 1.41f, 0, 0.30f, 0.11f, 0.31f);
        s.pair(Part.ARM_L, EMERALD_BLOCK, 0.43f, 1.468f, 0, 0.07f, 0.02f, 0.07f);
        // Hearts: on the back of each fist, the knees and the buckle.
        heart(s, Part.FOREARM_L, EMERALD_BLOCK, 0.42f, 0.70f, 0.122f, 0.06f);
        heart(s, Part.FOREARM_R, EMERALD_BLOCK, -0.42f, 0.70f, 0.122f, 0.06f);
        s.pair(Part.THIGH_L, GOLD_BLOCK, 0.14f, 0.43f, 0.085f, 0.17f, 0.13f, 0.06f);
        heart(s, Part.THIGH_L, EMERALD_BLOCK, 0.14f, 0.43f, 0.122f, 0.08f);
        heart(s, Part.THIGH_R, EMERALD_BLOCK, -0.14f, 0.43f, 0.122f, 0.08f);
        s.box(Part.BODY, GOLD_BLOCK, 0, 0.905f, 0, 0.50f, 0.06f, 0.30f);
        heart(s, Part.BODY, EMERALD_BLOCK, 0, 0.905f, 0.162f, 0.09f);
        // The back plate.
        s.box(Part.BODY, GOLD_BLOCK, 0, 1.28f, -0.17f, 0.44f, 0.26f, 0.03f);
    }

    // -------------------------------------------------------------- Magician's Red

    private static void magiciansRed(Sculpt s) {
        Palette p = palette(StandType.MAGICIANS_RED);
        frame(s, p);
        // The bird's head: skull, brow ridge, the hooked beak and the yellow eyes.
        s.box(Part.HEAD, ORANGE_TERRACOTTA, 0, 1.70f, 0, 0.38f, 0.38f, 0.40f);
        s.box(Part.HEAD, RED_CONCRETE, 0, 1.78f, 0.17f, 0.40f, 0.06f, 0.08f);
        s.boxSpin(Part.HEAD, YELLOW_TERRACOTTA, 0, 1.665f, 0.27f, 0.11f, 0.08f, 0.17f, 14f, 0, 0);
        s.boxSpin(Part.HEAD, YELLOW_TERRACOTTA, 0, 1.625f, 0.35f, 0.06f, 0.06f, 0.07f, 32f, 0, 0);
        s.box(Part.HEAD, ORANGE_TERRACOTTA, 0, 1.605f, 0.24f, 0.08f, 0.04f, 0.10f);
        s.glowPair(Part.HEAD, YELLOW_CONCRETE, 0.11f, 1.725f, 0.205f, 0.07f, 0.05f, 0.02f, 0, 0, 0);
        s.glowPair(Part.HEAD, BLACK_CONCRETE, 0.11f, 1.725f, 0.214f, 0.03f, 0.05f, 0.01f, 0, 0, 0);
        // The crest: three long plumes swept back, a shorter layer below, feathers on the cheeks.
        s.boxSpin(Part.HEAD, RED_CONCRETE, 0, 1.93f, -0.15f, 0.07f, 0.05f, 0.44f, 25f, 0, 0);
        s.pairSpin(Part.HEAD, RED_CONCRETE, 0.09f, 1.90f, -0.14f, 0.06f, 0.05f, 0.40f, 18f, -12f, 0);
        s.boxSpin(Part.HEAD, ORANGE_CONCRETE, 0, 1.82f, -0.24f, 0.06f, 0.05f, 0.32f, 40f, 0, 0);
        s.pairSpin(Part.HEAD, ORANGE_CONCRETE, 0.20f, 1.62f, 0.05f, 0.04f, 0.14f, 0.18f, 0, 0, -10f);
        // Feathered chest and the golden ankh.
        s.box(Part.BODY, ORANGE_CONCRETE, 0, 1.39f, 0.165f, 0.36f, 0.10f, 0.02f);
        s.box(Part.BODY, GOLD_BLOCK, 0, 1.30f, 0.175f, 0.09f, 0.10f, 0.015f);
        s.box(Part.BODY, RED_TERRACOTTA, 0, 1.30f, 0.182f, 0.04f, 0.05f, 0.01f);
        s.box(Part.BODY, GOLD_BLOCK, 0, 1.235f, 0.175f, 0.13f, 0.03f, 0.015f);
        s.box(Part.BODY, GOLD_BLOCK, 0, 1.17f, 0.175f, 0.03f, 0.11f, 0.015f);
        // Feathers on the shoulders and along the outside of each forearm.
        s.pair(Part.ARM_L, ORANGE_CONCRETE, 0.43f, 1.40f, 0, 0.28f, 0.09f, 0.30f);
        s.pairSpin(Part.ARM_L, RED_CONCRETE, 0.51f, 1.33f, 0, 0.10f, 0.17f, 0.28f, 0, 0, -20f);
        s.pairSpin(Part.FOREARM_L, RED_CONCRETE, 0.52f, 0.87f, -0.03f, 0.04f, 0.24f, 0.13f, 0, 0, -10f);
        s.pairSpin(Part.FOREARM_L, ORANGE_CONCRETE, 0.515f, 0.90f, -0.09f, 0.03f, 0.20f, 0.08f, 0, 0, -16f);
        // Belt and the skirt of feathers round the hips.
        s.box(Part.BODY, GOLD_BLOCK, 0, 0.905f, 0, 0.50f, 0.05f, 0.30f);
        s.boxSpin(Part.BODY, RED_CONCRETE, 0, 0.70f, 0.145f, 0.17f, 0.26f, 0.03f, -12f, 0, 0);
        s.pairSpin(Part.BODY, ORANGE_CONCRETE, 0.16f, 0.71f, 0.12f, 0.12f, 0.24f, 0.03f, -12f, -20f, 0);
        s.boxSpin(Part.BODY, RED_CONCRETE, 0, 0.68f, -0.15f, 0.32f, 0.30f, 0.03f, 15f, 0, 0);
        s.pairSpin(Part.BODY, ORANGE_CONCRETE, 0.25f, 0.70f, 0, 0.03f, 0.24f, 0.20f, 0, 0, -14f);
    }

    // --------------------------------------------------------------- Crazy Diamond

    private static void crazyDiamond(Sculpt s) {
        Palette p = palette(StandType.CRAZY_DIAMOND);
        frame(s, p);
        face(s, p.skin(), WHITE_CONCRETE, LIGHT_BLUE_CONCRETE, MAGENTA_TERRACOTTA, 8f);
        // Helmet with its crest fin and a heart on the brow.
        s.box(Part.HEAD, WHITE_CONCRETE, 0, 1.89f, -0.02f, 0.44f, 0.08f, 0.44f);
        s.box(Part.HEAD, PINK_CONCRETE, 0, 1.965f, -0.03f, 0.05f, 0.08f, 0.38f);
        s.box(Part.HEAD, WHITE_CONCRETE, 0, 1.73f, -0.215f, 0.44f, 0.34f, 0.04f);
        heart(s, Part.HEAD, LIGHT_BLUE_CONCRETE, 0, 1.835f, 0.218f, 0.07f);
        // The mouth guard and the tubes along the cheeks.
        s.box(Part.HEAD, WHITE_CONCRETE, 0, 1.585f, 0.19f, 0.24f, 0.09f, 0.05f);
        s.pair(Part.HEAD, LIGHT_BLUE_CONCRETE, 0.175f, 1.585f, 0.02f, 0.045f, 0.045f, 0.36f);
        s.pair(Part.HEAD, WHITE_CONCRETE, 0.175f, 1.585f, 0.205f, 0.06f, 0.06f, 0.03f);
        // The diamond on the chest, the pipes over the collarbones, white shoulder guards.
        s.boxSpin(Part.BODY, DIAMOND_BLOCK, 0, 1.24f, 0.175f, 0.11f, 0.11f, 0.03f, 0, 0, 45f);
        s.pairSpin(Part.BODY, LIGHT_BLUE_CONCRETE, 0.15f, 1.415f, 0.17f, 0.20f, 0.035f, 0.035f, 0, 0, -18f);
        s.pair(Part.ARM_L, WHITE_CONCRETE, 0.43f, 1.40f, 0, 0.29f, 0.10f, 0.31f);
        heart(s, Part.ARM_L, LIGHT_BLUE_CONCRETE, 0.43f, 1.39f, 0.165f, 0.08f);
        heart(s, Part.ARM_R, LIGHT_BLUE_CONCRETE, -0.43f, 1.39f, 0.165f, 0.08f);
        // Belt with a heart buckle; white knee guards with hearts.
        s.box(Part.BODY, WHITE_CONCRETE, 0, 0.905f, 0, 0.50f, 0.06f, 0.30f);
        heart(s, Part.BODY, LIGHT_BLUE_CONCRETE, 0, 0.905f, 0.162f, 0.08f);
        s.pair(Part.THIGH_L, WHITE_CONCRETE, 0.14f, 0.43f, 0.085f, 0.17f, 0.13f, 0.06f);
        heart(s, Part.THIGH_L, LIGHT_BLUE_CONCRETE, 0.14f, 0.43f, 0.122f, 0.07f);
        heart(s, Part.THIGH_R, LIGHT_BLUE_CONCRETE, -0.14f, 0.43f, 0.122f, 0.07f);
    }

    // ---------------------------------------------------------------- Killer Queen

    private static void killerQueen(Sculpt s) {
        Palette p = palette(StandType.KILLER_QUEEN);
        frame(s, p);
        face(s, p.skin(), WHITE_CONCRETE, BLACK_CONCRETE, PURPLE_TERRACOTTA, 14f);
        s.box(Part.HEAD, PURPLE_TERRACOTTA, 0, 1.60f, 0.205f, 0.10f, 0.02f, 0.02f);
        // Cat ears, the stripe over the skull and the marks on the temples.
        s.pairSpin(Part.HEAD, p.skin(), 0.13f, 1.945f, 0, 0.10f, 0.13f, 0.07f, 0, 0, -16f);
        s.pairSpin(Part.HEAD, PINK_CONCRETE, 0.13f, 1.945f, 0.036f, 0.05f, 0.08f, 0.01f, 0, 0, -16f);
        s.box(Part.HEAD, PURPLE_TERRACOTTA, 0, 1.905f, 0, 0.06f, 0.015f, 0.40f);
        s.box(Part.HEAD, PURPLE_TERRACOTTA, 0, 1.845f, 0.205f, 0.06f, 0.10f, 0.015f);
        s.pair(Part.HEAD, PURPLE_TERRACOTTA, 0.205f, 1.72f, 0, 0.015f, 0.06f, 0.22f);
        // Stripes down the chest, pink shoulder guards studded with steel.
        s.pairSpin(Part.BODY, PURPLE_TERRACOTTA, 0.12f, 1.27f, 0.168f, 0.035f, 0.26f, 0.01f, 0, 0, 18f);
        s.pair(Part.ARM_L, PINK_CONCRETE, 0.43f, 1.405f, 0, 0.28f, 0.10f, 0.30f);
        for (float x : new float[]{0.35f, 0.43f, 0.51f}) {
            s.pair(Part.ARM_L, LIGHT_GRAY_CONCRETE, x, 1.46f, 0, 0.04f, 0.02f, 0.04f);
        }
        // Studded bands at the wrists and knees.
        for (float z : new float[]{-0.1f, 0.1f}) {
            s.pair(Part.FOREARM_L, LIGHT_GRAY_CONCRETE, 0.42f, 0.755f, z, 0.04f, 0.035f, 0.02f);
        }
        s.pair(Part.THIGH_L, PINK_CONCRETE, 0.14f, 0.43f, 0.085f, 0.17f, 0.12f, 0.06f);
        s.pair(Part.THIGH_L, LIGHT_GRAY_CONCRETE, 0.14f, 0.43f, 0.12f, 0.04f, 0.04f, 0.02f);
        // The belt and its skull buckle.
        s.box(Part.BODY, PURPLE_TERRACOTTA, 0, 0.905f, 0, 0.50f, 0.06f, 0.30f);
        skull(s, Part.BODY, 0, 0.905f, 0.16f, 1f);
        // The skull on the back of the right hand, the one that touches.
        skull(s, Part.FOREARM_R, -0.42f, 0.70f, 0.108f, 0.75f);
    }

    private static void skull(Sculpt s, Part part, float x, float y, float z, float k) {
        s.box(part, WHITE_CONCRETE, x, y, z, 0.10f * k, 0.09f * k, 0.03f);
        s.box(part, WHITE_CONCRETE, x, y - 0.06f * k, z, 0.06f * k, 0.03f * k, 0.025f);
        s.box(part, BLACK_CONCRETE, x - 0.025f * k, y + 0.01f * k, z + 0.017f, 0.025f * k, 0.025f * k, 0.01f);
        s.box(part, BLACK_CONCRETE, x + 0.025f * k, y + 0.01f * k, z + 0.017f, 0.025f * k, 0.025f * k, 0.01f);
    }

    // ---------------------------------------------------------------- building

    /** Collects the boxes; {@code pair} adds a box and its mirror on the other side of the body. */
    private static final class Sculpt {

        private final List<Piece> pieces = new ArrayList<>();

        void box(Part part, Material m, float x, float y, float z, float sx, float sy, float sz) {
            add(part, m, x, y, z, sx, sy, sz, 0, 0, 0, false);
        }

        void boxSpin(Part part, Material m, float x, float y, float z, float sx, float sy, float sz,
                     float rx, float ry, float rz) {
            add(part, m, x, y, z, sx, sy, sz, rx, ry, rz, false);
        }

        void pair(Part part, Material m, float x, float y, float z, float sx, float sy, float sz) {
            pairSpin(part, m, x, y, z, sx, sy, sz, 0, 0, 0);
        }

        void pair(Part part, Material m, float x, float y, float z, float sx, float sy, float sz,
                  float rx, float ry, float rz) {
            pairSpin(part, m, x, y, z, sx, sy, sz, rx, ry, rz);
        }

        /** The left box as given, the right one reflected across the middle of the body. */
        void pairSpin(Part part, Material m, float x, float y, float z, float sx, float sy, float sz,
                      float rx, float ry, float rz) {
            add(part, m, x, y, z, sx, sy, sz, rx, ry, rz, false);
            add(part.mirror(), m, -x, y, z, sx, sy, sz, rx, -ry, -rz, false);
        }

        void glowPair(Part part, Material m, float x, float y, float z, float sx, float sy, float sz,
                      float rx, float ry, float rz) {
            add(part, m, x, y, z, sx, sy, sz, rx, ry, rz, true);
            add(part.mirror(), m, -x, y, z, sx, sy, sz, rx, -ry, -rz, true);
        }

        private void add(Part part, Material m, float x, float y, float z, float sx, float sy, float sz,
                         float rx, float ry, float rz, boolean bright) {
            pieces.add(new Piece(m, part, new Vector3f(x, y, z), new Vector3f(sx, sy, sz),
                    StandRig.degrees(rx, ry, rz), bright || m == SHROOMLIGHT));
        }
    }
}
