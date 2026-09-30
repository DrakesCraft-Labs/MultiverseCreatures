package com.Chagui68.ritual.terrain;

/**
 * The ritual dimension's shape as pure functions of the world coordinates.
 *
 * <p>Every invariant the fight depends on is stated here and proved in
 * {@code ArenaShapeTest} / {@code BossArenaTerrainTest}:
 *
 * <ul>
 *   <li>{@link #FLOOR_Y} is the top solid block of the whole arena. The floor is perfectly flat
 *       (no holes, no bumps), because players build the invocation structures on it and the bosses
 *       land on it.</li>
 *   <li>Inside {@link #ARENA_RADIUS} nothing is ever built above the floor, up to
 *       {@link #ARENA_CLEAR_Y}: the bosses are gigantic and the old flat plain was the only reason
 *       their pathing worked.</li>
 *   <li>From {@link #ARENA_RADIUS} to {@link #RIM_RADIUS} the ground rises in terraces up to at
 *       least {@link #RIM_MIN_WALL_Y}, so the arena is a bowl a player cannot walk out of.</li>
 *   <li>Beyond {@link #RIM_RADIUS} the wilderness starts level with the rim (so there is no seam)
 *       and only drops away far enough out that the coliseum always stands on solid rock.</li>
 * </ul>
 */
public final class ArenaShape {

    /** Top solid block of the arena floor; the block below it is the pavement's sub-layer. */
    public static final int FLOOR_Y = 5;

    /** Bedrock sits here, as it did before, so the world still has an unbreakable bottom. */
    public static final int BEDROCK_Y = 0;

    /** Radius of the paved plaza: pavement the invocation structures accept, and nothing else. */
    public static final double PLAZA_RADIUS = 34.0;

    /** Radius of the flat fighting floor, measured from the world origin. */
    public static final double ARENA_RADIUS = 64.0;

    /** How far up the play volume has to stay clear of blocks. */
    public static final int ARENA_CLEAR_Y = 34;

    /** Outer edge of the terraced wall. */
    public static final double RIM_RADIUS = 80.0;

    /** First terrace, one step above the floor: a player cannot jump out of the arena. */
    public static final int RIM_FIRST_STEP_Y = 9;

    /** Every terrace is this many blocks taller than the one inside it. */
    public static final int RIM_STEP_HEIGHT = 6;

    /** Terraces are this wide; {@code 4 * RIM_STEP_DEPTH == RIM_RADIUS - ARENA_RADIUS}. */
    public static final int RIM_STEP_DEPTH = 4;

    /** Height of the tallest terrace. */
    public static final int RIM_TOP_Y = RIM_FIRST_STEP_Y + 3 * RIM_STEP_HEIGHT;

    /**
     * The first terrace is this far above the floor, so a player cannot jump out of the arena; the
     * wall only climbs from there.
     */
    public static final int RIM_MIN_WALL_Y = RIM_FIRST_STEP_Y;

    /** Merlons on the parapet: battlements that break the silhouette. */
    public static final int MERLON_HEIGHT = 3;

    /** Height of the buttress ribs that crown the wall. */
    public static final int RIB_HEIGHT = 20;

    /** How many buttress ribs stand around the arena. */
    public static final int RIB_COUNT = 8;

    /** Base height of the wilderness: level with the rim, so the wall reads as carved from it. */
    public static final int WASTE_BASE_Y = RIM_TOP_Y;

    /** Deepest a canyon can carve; lava pools sit at its bottom. */
    public static final int CHASM_FLOOR_Y = -22;

    /** Highest a peak may reach. */
    public static final int MAX_BUILD_Y = 92;

    /** Lowest y the generator ever writes, bedrock aside. */
    public static final int MIN_BUILD_Y = -24;

    /** Wilderness landmarks never spawn closer to the centre than this. */
    public static final double LANDMARK_INNER_RADIUS = 104.0;

    /** Canyons only start this far out, so they never undercut the coliseum. */
    public static final double CHASM_INNER_RADIUS = RIM_RADIUS + 30.0;

    /** Salt of the field the wilderness shape is drawn from; see {@link #surfaceY}. */
    private static final int WASTE_SALT = 0x51ED_2701;

    private ArenaShape() {
    }

    /** Which of the three rings a column belongs to. */
    public enum Zone {
        /** The flat fighting floor. */
        ARENA,
        /** The terraced wall that encloses it. */
        RIM,
        /** Everything beyond, seen over the parapet. */
        WASTE
    }

    /** Horizontal distance from the world origin, where the arena is centred. */
    public static double distance(int x, int z) {
        return Math.sqrt((double) x * x + (double) z * z);
    }

    /** Ring of the given column. */
    public static Zone zone(int x, int z) {
        double r = distance(x, z);
        if (r <= ARENA_RADIUS) return Zone.ARENA;
        if (r <= RIM_RADIUS) return Zone.RIM;
        return Zone.WASTE;
    }

    /** Whether the column is part of the flat fighting floor. */
    public static boolean inArena(int x, int z) {
        return distance(x, z) <= ARENA_RADIUS;
    }

    /** Whether the player still stands on the pavement the invocation structures accept. */
    public static boolean onPlaza(int x, int z) {
        return distance(x, z) <= PLAZA_RADIUS;
    }

    /**
     * Top solid block of the column.
     *
     * <p>The arena is flat, the rim is terraced, and the wilderness is fractal noise quantised into
     * shelves so the rock reads as layered strata instead of a blob. Canyons are carved with a
     * ridge field only past {@link #CHASM_INNER_RADIUS}.
     */
    public static int surfaceY(int x, int z, long seed) {
        double r = distance(x, z);
        if (r <= ARENA_RADIUS) return FLOOR_Y;
        int salt = ArenaNoise.saltOf(seed) ^ WASTE_SALT;
        if (r <= RIM_RADIUS) return rimY(x, z, r, salt);
        return wasteY(x, z, r, salt);
    }

    /** The wall: four terraces, battlements on top and ribs every {@code 360 / RIB_COUNT} degrees. */
    public static int rimY(int x, int z, double distance, int salt) {
        double t = (distance - ARENA_RADIUS) / (double) (RIM_STEP_DEPTH * 4);
        int step = (int) Math.min(3.0, Math.floor(t * 4.0));
        int height = RIM_FIRST_STEP_Y + step * RIM_STEP_HEIGHT;
        if (step == 3 && isMerlon(x, z)) {
            height += MERLON_HEIGHT;
        }
        // The ribs only start climbing on the upper galleries: lower down they would be spikes in
        // front of the first terrace instead of buttresses over the crown.
        return height + ribHeight(x, z, distance, ArenaNoise.smoothstep(70.0, RIM_RADIUS, distance));
    }

    /** Height added by the buttress ribs: a smooth bump centred on {@link #RIB_COUNT} azimuths. */
    public static int ribHeight(int x, int z, double distance, double fade) {
        if (fade <= 0.0) return 0;
        double theta = Math.atan2(z, x);
        double lobe = Math.cos(RIB_COUNT * theta);
        if (lobe <= 0.55) return 0;
        double shape = (lobe - 0.55) / 0.45;
        return (int) Math.round(RIB_HEIGHT * fade * Math.pow(shape, 2.0));
    }

    /** Battlements: three-degree merlons separated by three-degree gaps, all the way round. */
    private static boolean isMerlon(int x, int z) {
        double degrees = Math.toDegrees(Math.atan2(z, x));
        int bucket = (int) Math.floor((degrees + 540.0) / 3.0);
        return (bucket / 2) % 2 == 0;
    }

    /** The wilderness: shelves, peaks and canyons. */
    private static int wasteY(int x, int z, double distance, int salt) {
        double shelfNoise = (ArenaNoise.fbm(x, z, salt + 7, 0.02, 2) - 0.5) * 4.0;
        // Hills are held back to the far side of an esplanade: the ground just outside the wall
        // stays level with it, so the coliseum reads as cut out of the plateau instead of perched
        // on a random ledge.
        double hillMask = ArenaNoise.smoothstep(RIM_RADIUS, RIM_RADIUS + 40.0, distance);
        double rollingHills = hillMask * (ArenaNoise.fbm(x, z, salt + 11, 0.009, 4) - 0.45) * 26.0;
        double mountainMask = ArenaNoise.smoothstep(150.0, 260.0, distance);
        double crest = ArenaNoise.ridge(x, z, salt + 17, 0.0035, 4);
        double mountains = mountainMask * Math.pow(crest, 2.4) * 62.0;
        double ribs = ribHeight(x, z, distance, ArenaNoise.smoothstep(96.0, RIM_RADIUS, distance));

        double height = WASTE_BASE_Y + shelfNoise + rollingHills + mountains + ribs;

        // Shelves every three blocks: the strata the palette then colours by altitude.
        height = Math.floor(height / 3.0) * 3.0;

        double canyon = canyonStrength(x, z, distance, salt);
        if (canyon > 0.0) {
            // Eight digging levels rather than a slope, so the canyon walls are terraces too, and a
            // flat bottom: that floor is where the palette floods lava, and a sheet of it is what
            // makes a canyon read as a canyon from the parapet.
            double stepped = Math.min(1.0, Math.round(canyon * 8.0) / 8.0);
            height = Math.max(CHASM_FLOOR_Y, height - stepped * (WASTE_BASE_Y - CHASM_FLOOR_Y));
        }

        int clamped = (int) Math.round(Math.max(MIN_BUILD_Y, Math.min(MAX_BUILD_Y, height)));
        return Math.max(MIN_BUILD_Y, clamped);
    }

    /**
     * How deep a column is carved into a canyon, from 0 (untouched rock) to 1 (canyon bottom).
     *
     * <p>The mask is the distance to the crest line of a low frequency ridge field, faded in only
     * past {@link #CHASM_INNER_RADIUS}; a soft edge means the canyon walls are stepped, not sheer,
     * which is what makes the strata visible from above.
     */
    public static double canyonStrength(int x, int z, double distance, int salt) {
        double fade = ArenaNoise.smoothstep(CHASM_INNER_RADIUS, CHASM_INNER_RADIUS + 60.0, distance);
        if (fade <= 0.0) return 0.0;
        double spine = Math.abs(ArenaNoise.fbm(x, z, salt + 23, 0.0022, 3) - 0.5);
        double depth = 1.0 - ArenaNoise.smoothstep(0.02, 0.075, spine);
        return depth * fade;
    }

    /** Whether the column sits in a canyon (the palette floods its bottom with lava). */
    public static boolean isCanyon(int x, int z, long seed) {
        int salt = ArenaNoise.saltOf(seed) ^ WASTE_SALT;
        return canyonStrength(x, z, distance(x, z), salt) > 0.35;
    }

    /** The y the spawn teleports to: just above the floor, where players have always landed. */
    public static int spawnY() {
        return FLOOR_Y + 5;
    }
}
