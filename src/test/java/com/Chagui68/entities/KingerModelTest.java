package com.Chagui68.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards Kinger's suit the way the Jack Star and NIX model suites guard theirs: fifteen pieces pinned
 * to the exported model, limbs that actually hang from their joints, the body centred on the
 * invisible armour stand that receives the hits, and a hitbox that covers the model without
 * swallowing swings aimed at thin air.
 *
 * <p>The reference numbers are the translations of the model as exported for the plugin, in the same
 * column-major matrices {@link Kinger.KingerPart} carries. The export was made around the torso and
 * not around a block, so the code re-centres the suit on that torso axis; these tests pin both halves
 * of the contract, so neither the export nor the re-centring can drift unnoticed.
 */
class KingerModelTest {

    /**
     * Reference model: one entry per piece, straight from the export, translation only. The full
     * matrices live in {@code KingerPart}.
     */
    private static final Map<Kinger.KingerPart, Vector3f> EXPORT = new EnumMap<>(Kinger.KingerPart.class);

    static {
        EXPORT.put(Kinger.KingerPart.BASE_LEFT, new Vector3f(0.3886122987f, 0.6775875205f, 0.501853708f));
        EXPORT.put(Kinger.KingerPart.BASE_RIGHT, new Vector3f(0.634953709f, 0.6814326334f, 0.4967771821f));

        EXPORT.put(Kinger.KingerPart.LEG_RIGHT_LOWER, new Vector3f(0.5950490686f, 0.232635498f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_RIGHT_UPPER, new Vector3f(0.5950490686f, 0.683807373f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_LEFT_LOWER, new Vector3f(0.409919674f, 0.2333874512f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.LEG_LEFT_UPPER, new Vector3f(0.409919674f, 0.6845593262f, 0.5025390625f));

        EXPORT.put(Kinger.KingerPart.TORSO_UPPER, new Vector3f(0.5022069787f, 1.2012714355f, 0.5028765625f));
        EXPORT.put(Kinger.KingerPart.TORSO_LOWER, new Vector3f(0.5021828576f, 0.810135498f, 0.5025390625f));
        EXPORT.put(Kinger.KingerPart.NECK, new Vector3f(0.5052224005f, 1.5827280655f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.BELT, new Vector3f(0.5052224005f, 1.3913218155f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.COLLAR, new Vector3f(0.5052224005f, 1.619095253f, 0.4711609839f));
        EXPORT.put(Kinger.KingerPart.HEAD, new Vector3f(0.5050075647f, 1.7741992188f, 0.4708984375f));

        EXPORT.put(Kinger.KingerPart.ARM_RIGHT, new Vector3f(0.5764186975f, 1.511015625f, 0.34375f));
        EXPORT.put(Kinger.KingerPart.ARM_LEFT, new Vector3f(0.4335964318f, 1.4631640625f, 0.34375f));
        EXPORT.put(Kinger.KingerPart.ORNAMENT_RIGHT, new Vector3f(0.5764186975f, 1.7048144531f, 0.4603515625f));
    }

    @Test
    @DisplayName("Every piece still carries the exported transform")
    void partsKeepTheExportedTransforms() {
        assertEquals(Kinger.KingerPart.values().length, EXPORT.size(),
                "the reference table must cover every piece");

        for (Map.Entry<Kinger.KingerPart, Vector3f> entry : EXPORT.entrySet()) {
            Vector3f exported = entry.getValue();
            Vector3f actual = entry.getKey().offset;

            assertEquals(exported.x, actual.x, 0.001f, entry.getKey() + " drifted from the export");
            assertEquals(exported.y, actual.y, 0.001f, entry.getKey() + " drifted vertically from the export");
            assertEquals(exported.z, actual.z, 0.001f, entry.getKey() + " drifted in depth from the export");
        }

        // The trunk and the legs were exported around one shared axis; the arms are the only pieces
        // that deliberately sit in front of it.
        for (Kinger.KingerPart trunk : List.of(Kinger.KingerPart.TORSO_UPPER, Kinger.KingerPart.TORSO_LOWER,
                Kinger.KingerPart.LEG_RIGHT_UPPER, Kinger.KingerPart.LEG_RIGHT_LOWER,
                Kinger.KingerPart.LEG_LEFT_UPPER, Kinger.KingerPart.LEG_LEFT_LOWER)) {
            assertEquals(0.5025390625f, EXPORT.get(trunk).z, 0.001f,
                    trunk + " is not on the body's single shared Z axis");
        }

        // Each half of a leg is stacked on its own axis, so a swinging leg cannot come apart sideways.
        assertEquals(EXPORT.get(Kinger.KingerPart.LEG_RIGHT_UPPER).x, EXPORT.get(Kinger.KingerPart.LEG_RIGHT_LOWER).x, 0.001f);
        assertEquals(EXPORT.get(Kinger.KingerPart.LEG_LEFT_UPPER).x, EXPORT.get(Kinger.KingerPart.LEG_LEFT_LOWER).x, 0.001f);
    }

    @Test
    @DisplayName("The body is centred on the torso axis instead of a centimetre off the hitbox")
    void theBodyIsCentredOnTheTorsoAxis() {
        assertEquals(midTorso('x'), Kinger.KingerPart.CENTER.x, 1.0e-5f,
                "CENTER must be the torso axis, not an average the arms can drag around");
        assertEquals(midTorso('z'), Kinger.KingerPart.CENTER.z, 1.0e-5f);

        // The trunk sits exactly on the invisible armour stand that carries the hitbox...
        for (Kinger.KingerPart trunk : List.of(Kinger.KingerPart.TORSO_UPPER, Kinger.KingerPart.TORSO_LOWER)) {
            assertEquals(0f, base(trunk).x, 0.001f, trunk + " must sit on the armour stand");
            assertEquals(0f, base(trunk).z, 0.001f, trunk + " must sit on the armour stand");
        }
        // ...the legs share that axis...
        for (Kinger.KingerPart leg : List.of(Kinger.KingerPart.LEG_RIGHT_UPPER, Kinger.KingerPart.LEG_RIGHT_LOWER,
                Kinger.KingerPart.LEG_LEFT_UPPER, Kinger.KingerPart.LEG_LEFT_LOWER)) {
            assertEquals(0f, base(leg).z, 0.001f, leg + " is not on the torso axis");
        }
        // ...and re-centring only touches the horizontal axes: heights are the export's own.
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            assertEquals(part.offset.y, base(part).y, 1.0e-6f, part + " was shifted vertically");
        }

        // The two centres that were rejected: the average of the fifteen anchors drags the trunk
        // 0.03 blocks forward, and the bounding-box midpoint 0.08, because the arms sit in front.
        Vector3f mean = new Vector3f();
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            mean.add(part.offset);
            minZ = Math.min(minZ, part.offset.z);
            maxZ = Math.max(maxZ, part.offset.z);
        }
        mean.div(Kinger.KingerPart.values().length);

        assertTrue(Math.abs(mean.z - Kinger.KingerPart.CENTER.z) > 0.02f,
                "the mean of the anchors is not the torso axis, and centring on it would move the trunk off the stand");
        assertTrue(Math.abs((minZ + maxZ) * 0.5f - Kinger.KingerPart.CENTER.z) > 0.05f,
                "the bounding-box midpoint is even further forward than the mean");
    }

    @Test
    @DisplayName("Head above torso, torso above legs, feet off the ground and about two blocks of body")
    void verticalLayoutIsAChessPiece() {
        assertTrue(baseY(Kinger.KingerPart.HEAD) > baseY(Kinger.KingerPart.TORSO_UPPER) + 0.3f);
        assertTrue(baseY(Kinger.KingerPart.TORSO_UPPER) > baseY(Kinger.KingerPart.TORSO_LOWER) + 0.2f);
        assertTrue(baseY(Kinger.KingerPart.TORSO_LOWER) > baseY(Kinger.KingerPart.LEG_RIGHT_UPPER) + 0.1f);
        assertTrue(baseY(Kinger.KingerPart.LEG_RIGHT_UPPER) > baseY(Kinger.KingerPart.LEG_RIGHT_LOWER) + 0.4f);

        float headTop = topOf(Kinger.KingerPart.HEAD);
        assertTrue(headTop > 1.8f && headTop < 1.95f, "head top should be just under two blocks: " + headTop);
        assertTrue(feetBottom() > 0.05f && feetBottom() < 0.4f,
                "feet should clear the ground: " + feetBottom());
    }

    @Test
    @DisplayName("Every joint is on the limb it drives, and every limb hangs from its joint")
    void jointsSitOnTheLimbTheyDrive() {
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            Kinger.LimbGroup group = part.group();
            Vector3f pivot = KingerModel.pivot(group);
            Vector3f base = base(part);

            if (!isLimb(group)) {
                // The trunk and the head hang from the body axis, so their joints are on that axis.
                assertEquals(0f, pivot.x, 1.0e-6f, group + "'s joint is off the body axis");
                assertEquals(0f, pivot.z, 1.0e-6f);
                assertTrue(Math.abs(base.x) < 0.1f, part + " is not part of the trunk it was given to");
                continue;
            }

            assertTrue(Math.signum(base.x) == Math.signum(pivot.x),
                    part + " swings around a joint on the wrong side (piece x=" + base.x + ", joint x=" + pivot.x + ")");

            float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
            for (Kinger.KingerPart sibling : Kinger.KingerPart.values()) {
                if (sibling.group() != group) continue;
                minX = Math.min(minX, base(sibling).x);
                maxX = Math.max(maxX, base(sibling).x);
            }
            assertTrue(pivot.x > minX - 0.02f && pivot.x < maxX + 0.02f,
                    group + "'s joint (" + pivot.x + ") is outside the limb it drives (" + minX + ".." + maxX + ")");
            assertTrue(pivot.y >= base.y - 0.001f, part + " does not hang from its joint");
        }
    }

    @Test
    @DisplayName("A still limb rests exactly where the export puts it")
    void anIdleLimbRestsWhereTheExportPutsIt() {
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            Vector3f rest = base(part);
            Vector3f composed = KingerModel.compose(part, new Quaternionf()).getTranslation();

            assertEquals(rest.x, composed.x, 1.0e-5f, part + " moved while idle");
            assertEquals(rest.y, composed.y, 1.0e-5f, part + " moved while idle");
            assertEquals(rest.z, composed.z, 1.0e-5f, part + " moved while idle");
        }
    }

    @Test
    @DisplayName("A limb rotates about its joint instead of detaching from the body")
    void limbsSwingAroundTheirJoints() {
        Quaternionf swing = new Quaternionf().rotateX(0.32f);
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            Vector3f rest = base(part);
            Vector3f pivot = KingerModel.pivot(part.group());
            Vector3f moved = KingerModel.compose(part, new Quaternionf(swing)).getTranslation();

            assertEquals(rest.x, moved.x, 1.0e-4f, part + " slid sideways while swinging");
            assertTrue(rest.distance(moved) < 0.4f, part + " flew away from its joint: " + rest.distance(moved));
            assertEquals(rest.distance(pivot), moved.distance(pivot), 1.0e-3f,
                    part + " is no longer the same distance from its joint");
        }
    }

    @Test
    @DisplayName("The pieces of one limb move as one body instead of each spinning in place")
    void theWholeLimbSwingsAsOne() {
        // Named check first: the shin used to stay put while the thigh swung, because every piece
        // rotated about its own anchor. Walking tore the leg apart.
        Quaternionf swing = new Quaternionf().rotateX(0.3f);
        Vector3f upperRest = base(Kinger.KingerPart.LEG_RIGHT_UPPER);
        Vector3f lowerRest = base(Kinger.KingerPart.LEG_RIGHT_LOWER);
        Vector3f upperMoved = KingerModel.compose(Kinger.KingerPart.LEG_RIGHT_UPPER, new Quaternionf(swing)).getTranslation();
        Vector3f lowerMoved = KingerModel.compose(Kinger.KingerPart.LEG_RIGHT_LOWER, new Quaternionf(swing)).getTranslation();

        assertTrue(lowerRest.distance(lowerMoved) > 0.1f,
                "the shin must swing with the thigh, not from its own knee");
        assertEquals(upperRest.distance(lowerRest), upperMoved.distance(lowerMoved), 1.0e-4f,
                "the leg changed length while swinging");

        // And the same for every multi-piece group: nothing stands still, nothing stretches.
        for (Kinger.LimbGroup group : Kinger.LimbGroup.values()) {
            List<Kinger.KingerPart> members = membersOf(group);
            if (members.size() < 2) continue;

            for (Kinger.KingerPart part : members) {
                Vector3f rest = base(part);
                Vector3f moved = KingerModel.compose(part, new Quaternionf(swing)).getTranslation();
                assertTrue(rest.distance(moved) > 0.01f, part + " spun in place instead of following " + group);
            }
            for (int i = 0; i < members.size(); i++) {
                for (int j = i + 1; j < members.size(); j++) {
                    Vector3f restGap = base(members.get(i)).sub(base(members.get(j)));
                    Vector3f movedGap = KingerModel.compose(members.get(i), new Quaternionf(swing)).getTranslation()
                            .sub(KingerModel.compose(members.get(j), new Quaternionf(swing)).getTranslation());
                    assertEquals(restGap.length(), movedGap.length(), 1.0e-4f,
                            members.get(i) + " and " + members.get(j) + " came apart while swinging");
                }
            }
        }
    }

    @Test
    @DisplayName("The fifteen pieces keep their own place in the body")
    void partsNeverCollapseOntoEachOther() {
        List<Kinger.KingerPart> parts = List.of(Kinger.KingerPart.values());
        for (int i = 0; i < parts.size(); i++) {
            for (int j = i + 1; j < parts.size(); j++) {
                float gap = base(parts.get(i)).distance(base(parts.get(j)));
                assertTrue(gap > 0.015f, parts.get(i) + " and " + parts.get(j) + " sit on top of each other");
            }
        }
    }

    @Test
    @DisplayName("The armour stand's hitbox covers the whole suit, and is no bigger than it needs")
    void hitboxCoversTheModel() {
        // The stand is the only hitbox the suit has: the pieces are zero-sized so the client cannot
        // pick one instead, which means a swing that misses the stand hits nothing at all.
        float halfWidth = (float) (0.25 * Kinger.MODEL_HITBOX_SCALE);
        float height = (float) (1.975 * Kinger.MODEL_HITBOX_SCALE);

        float needed = 0f;
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            Vector3f base = base(part);
            float reachX = Math.abs(base.x) + part.scale.x * 0.25f;
            float reachZ = Math.abs(base.z) + part.scale.z * 0.25f;
            float top = topOf(part);

            assertTrue(reachX < halfWidth, part + " leans out of the side of the hitbox, so a swing at it would miss the stand");
            assertTrue(reachZ < halfWidth, part + " stands out of the front of the hitbox: " + reachZ);
            assertTrue(top < height, part + " pokes out of the top of the hitbox: " + top);
            assertTrue(base.y - part.scale.y * 0.25f > 0f, part + " hangs below the stand's feet");

            needed = Math.max(needed, reachX / 0.25f);
            needed = Math.max(needed, reachZ / 0.25f);
            needed = Math.max(needed, top / 1.975f);
        }

        assertTrue(Kinger.MODEL_HITBOX_SCALE >= needed,
                "the box is smaller than the suit needs: " + Kinger.MODEL_HITBOX_SCALE + " < " + needed);
        assertTrue(Kinger.MODEL_HITBOX_SCALE - needed < 0.1f,
                "the box is far bigger than the suit needs, so it swallows swings at thin air: "
                        + Kinger.MODEL_HITBOX_SCALE + " vs " + needed);

        // The suit fits inside a plain, unscaled armour stand: the old literal 2.0 doubled the box in
        // every direction for nothing.
        assertTrue(Kinger.MODEL_HITBOX_SCALE <= 1.0f, "an unscaled stand is already enough for this model");
    }

    @Test
    @DisplayName("Every piece carries its own tag, so an adoption can never confuse two of them")
    void pieceTagsAreUnique() {
        Set<String> pieceTags = new HashSet<>();
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            assertTrue(pieceTags.add(Kinger.partTag(part)), part + " shares a tag with another piece");
            assertTrue(Kinger.partTag(part).startsWith(Kinger.PART_TAG),
                    "the damage listener finds pieces by " + Kinger.PART_TAG);
        }

        String one = Kinger.partOwnerTag(java.util.UUID.randomUUID());
        String two = Kinger.partOwnerTag(java.util.UUID.randomUUID());
        assertTrue(one.startsWith(Kinger.PART_OWNER_TAG_PREFIX));
        assertNotEquals(one, two, "two bosses must not answer to the same ownership tag");
    }

    @Test
    @DisplayName("A piece never lags, and a reload reattaches the suit and its boss bar instead of duplicating them")
    void partDisplaysFollowTheStandExactly() throws IOException {
        String source = Files.readString(Path.of("src", "main", "java",
                "com", "Chagui68", "entities", "Kinger.java"));
        String suit = Files.readString(Path.of("src", "main", "java",
                "com", "Chagui68", "utils", "DisplaySuit.java"));

        // The pieces are placed on the stand's exact position every tick, so any interpolation would
        // make the suit trail the invisible hitbox, and any display box would swallow the swing aimed
        // at it. Both were true of the first version of this model. The values live in the suit every
        // dressed boss shares, so this guard pins the one place they are applied.
        for (String call : List.of("setTeleportDuration", "setInterpolationDuration", "setInterpolationDelay",
                "setDisplayWidth", "setDisplayHeight")) {
            Matcher calls = Pattern.compile(Pattern.quote(call) + "\\(([^)]*)\\)").matcher(suit);
            int found = 0;
            while (calls.find()) {
                found++;
                assertTrue(calls.group(1).trim().matches("0(\\.0+)?f?"),
                        call + " must be zero, found " + calls.group(1).trim());
            }
            assertEquals(1, found, call + " should be configured once, for every piece, in DisplaySuit");
        }
        assertTrue(source.contains("DisplaySuit.spawn("),
                "a piece display must be built by the shared suit, not by hand");

        // Enabling the plugin over a live Kinger must reattach the suit it already spawned: spawning
        // first is what used to leave two overlapping bodies.
        assertTrue(source.contains("restorePartDisplays("),
                "the enable path must adopt the pieces of a boss that is already alive");
        assertTrue(source.contains("findPartDisplay("),
                "syncDisplays must adopt an orphaned piece before spawning a new one");
        assertTrue(source.contains("setupBossBar(inst)"),
                "a boss that survived a restart must get its boss bar back, or it fights with none");
        assertTrue(source.contains("KEY_VIRTUAL_MAX_HEALTH"),
                "a restored boss must have the virtual health its bar progress is read from");

        // The animations run through the joint maths, not through a per-piece transform: that is what
        // keeps a limb rigid.
        assertTrue(source.contains("KingerModel.compose("),
                "piece transforms must be built as a rotation about the limb joint");
        assertFalse(source.contains("computeAnimQuat("),
                "the old per-piece animation rotated every piece about its own anchor");
        assertTrue(source.contains("setBaseValue(MODEL_HITBOX_SCALE)"),
                "the hitbox scale must be the named, tested constant");
    }

    private static float midTorso(char axis) {
        float upper = axis == 'x' ? Kinger.KingerPart.TORSO_UPPER.offset.x : Kinger.KingerPart.TORSO_UPPER.offset.z;
        float lower = axis == 'x' ? Kinger.KingerPart.TORSO_LOWER.offset.x : Kinger.KingerPart.TORSO_LOWER.offset.z;
        return (upper + lower) * 0.5f;
    }

    private static Vector3f base(Kinger.KingerPart part) {
        return KingerModel.baseTranslation(part);
    }

    private static float baseY(Kinger.KingerPart part) {
        return base(part).y;
    }

    private static float topOf(Kinger.KingerPart part) {
        return baseY(part) + part.scale.y * 0.25f;
    }

    private static float feetBottom() {
        float lowest = Float.POSITIVE_INFINITY;
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            if (part.group() != Kinger.LimbGroup.LEG_RIGHT && part.group() != Kinger.LimbGroup.LEG_LEFT) continue;
            lowest = Math.min(lowest, baseY(part) - part.scale.y * 0.25f);
        }
        return lowest;
    }

    private static boolean isLimb(Kinger.LimbGroup group) {
        return group == Kinger.LimbGroup.ARM_RIGHT || group == Kinger.LimbGroup.ARM_LEFT
                || group == Kinger.LimbGroup.LEG_RIGHT || group == Kinger.LimbGroup.LEG_LEFT;
    }

    private static List<Kinger.KingerPart> membersOf(Kinger.LimbGroup group) {
        List<Kinger.KingerPart> members = new java.util.ArrayList<>();
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            if (part.group() == group) members.add(part);
        }
        return members;
    }
}
