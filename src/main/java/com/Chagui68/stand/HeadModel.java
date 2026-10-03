package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import org.bukkit.util.Transformation;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Stand drawn with textured player heads: the eleven-head humanoid that BDEngine exports as a
 * {@code /summon block_display ... {Passengers:[item_display, ...]}} command.
 *
 * <p>The export is read as it is pasted: the eleven skins and the eleven transformation matrices.
 * Every piece of that format is a player head that hangs below the point it is placed at (its top
 * face sits on the anchor), so a head is a cube {@code 0.5 * scale} wide whose anchor is the middle
 * of its top face. The pieces are recognised by where they are: the biggest one is the head, the
 * two wide ones under it the chest and the belly, the four highest of the rest the upper arms and
 * forearms, the last four the thighs and shins.</p>
 *
 * <p>Whatever pose the model was exported in is thrown away: the pieces are stacked again straight,
 * on the joints of {@link StandRig}, so the Stand can be posed and animated like the block figures.
 * BDEngine models look towards {@code -z}, so the model is turned round to face {@code +z} with its
 * right hand on {@code -x}, the way the rest of the Stand code expects.</p>
 */
public final class HeadModel {

    /** One head of the figure: its skin, its part, where its top sits and how big it is. */
    public record Piece(String texture, Part part, Vector3f anchor, Vector3f scale) {
    }

    private static final Pattern TEXTURE = Pattern.compile("value:\"([A-Za-z0-9+/=]+)\"");
    private static final Pattern MATRIX = Pattern.compile("transformation:\\[([^\\]]+)]");
    private static final Quaternionf TURN_ROUND = new Quaternionf().rotationY((float) Math.PI);

    private final List<Piece> pieces;
    private final Map<Part, Vector3f> pivots;
    private final float height;

    private HeadModel(List<Piece> pieces, Map<Part, Vector3f> pivots, float height) {
        this.pieces = List.copyOf(pieces);
        this.pivots = pivots;
        this.height = height;
    }

    public List<Piece> pieces() {
        return pieces;
    }

    /** From the feet to the top of the head, at scale 1. */
    public float height() {
        return height;
    }

    /** The joint a part turns about, in this model's own proportions. */
    public Vector3f pivot(Part part) {
        return new Vector3f(pivots.get(part));
    }

    /** The display transformation of one head, carried by its part's frame and scaled about the feet. */
    public static Transformation place(Piece piece, Frame frame, float scale) {
        Vector3f translation = frame.apply(piece.anchor()).mul(scale);
        Quaternionf rotation = new Quaternionf(frame.rotation()).mul(TURN_ROUND);
        return new Transformation(translation, rotation, new Vector3f(piece.scale()).mul(scale), new Quaternionf());
    }

    // ------------------------------------------------------------------ reading

    /** One piece as the export has it, turned square to the model's own front. */
    private record Raw(String texture, Vector3f position, Vector3f scale) {
    }

    /**
     * Reads an exported command.
     *
     * @throws IllegalArgumentException when it is not an eleven-head humanoid
     */
    public static HeadModel parse(String command) {
        List<String> textures = new ArrayList<>();
        Matcher texture = TEXTURE.matcher(command);
        while (texture.find()) {
            textures.add(texture.group(1));
        }
        List<float[]> matrices = new ArrayList<>();
        Matcher matrix = MATRIX.matcher(command);
        while (matrix.find()) {
            String[] numbers = matrix.group(1).split(",");
            if (numbers.length != 16) {
                throw new IllegalArgumentException("a transformation has " + numbers.length + " numbers, not 16");
            }
            float[] values = new float[16];
            for (int i = 0; i < 16; i++) {
                values[i] = Float.parseFloat(numbers[i].trim().replace("f", "").replace("F", ""));
            }
            matrices.add(values);
        }
        if (textures.size() != 11 || matrices.size() != 11) {
            throw new IllegalArgumentException("expected 11 heads with a skin and a transformation, found "
                    + textures.size() + " skins and " + matrices.size() + " transformations");
        }
        return build(square(textures, matrices));
    }

    /** Takes out the turn the model was exported with, measured on its head, and centres it there. */
    private static List<Raw> square(List<String> textures, List<float[]> matrices) {
        int headIndex = 0;
        float biggest = -1;
        for (int i = 0; i < matrices.size(); i++) {
            Vector3f scale = columns(matrices.get(i));
            float volume = scale.x * scale.y * scale.z;
            if (volume > biggest) {
                biggest = volume;
                headIndex = i;
            }
        }
        float[] head = matrices.get(headIndex);
        // Row major: m[0] and m[2] are the head's x axis across x and z.
        float yaw = (float) Math.atan2(head[2], head[0]);
        Matrix3f undo = new Matrix3f().rotationY(-yaw);
        Vector3f origin = new Vector3f(head[3], head[7], head[11]).mul(undo);
        List<Raw> raws = new ArrayList<>();
        for (int i = 0; i < matrices.size(); i++) {
            float[] m = matrices.get(i);
            Vector3f position = new Vector3f(m[3], m[7], m[11]).mul(undo).sub(origin);
            raws.add(new Raw(textures.get(i), position, columns(m)));
        }
        raws.add(0, raws.remove(headIndex));
        return raws;
    }

    /** The length of each column of the 3x3 part of a row-major 4x4: the scale along each axis. */
    private static Vector3f columns(float[] m) {
        return new Vector3f(
                (float) Math.sqrt(m[0] * m[0] + m[4] * m[4] + m[8] * m[8]),
                (float) Math.sqrt(m[1] * m[1] + m[5] * m[5] + m[9] * m[9]),
                (float) Math.sqrt(m[2] * m[2] + m[6] * m[6] + m[10] * m[10]));
    }

    private static HeadModel build(List<Raw> raws) {
        Raw head = raws.get(0);
        List<Raw> rest = new ArrayList<>(raws.subList(1, raws.size()));
        // Chest and belly: the two pieces as wide as the head, straight under it.
        rest.sort(Comparator.comparingDouble(r -> Math.abs(r.position().x)));
        List<Raw> torso = new ArrayList<>(rest.subList(0, 2));
        if (Math.abs(torso.get(1).position().x) > 0.1f) {
            throw new IllegalArgumentException("no chest and belly under the head");
        }
        torso.sort(Comparator.comparingDouble((Raw r) -> r.position().y).reversed());
        List<Raw> limbs = new ArrayList<>(rest.subList(2, rest.size()));
        // Arms hang from the shoulders, higher than any piece of the legs.
        limbs.sort(Comparator.comparingDouble((Raw r) -> r.position().y).reversed());
        List<Raw> arms = limbs.subList(0, 4);
        List<Raw> legs = limbs.subList(4, 8);

        Raw chest = torso.get(0);
        Raw belly = torso.get(1);
        // The export looks towards -z, so its right hand is on +x.
        Raw[] rightArm = pair(arms, true);
        Raw[] leftArm = pair(arms, false);
        Raw[] rightLeg = pair(legs, true);
        Raw[] leftLeg = pair(legs, false);

        float headBottom = -0.5f * head.scale().y;
        float bellyTop = headBottom - 0.5f * chest.scale().y;
        float hips = bellyTop - 0.5f * belly.scale().y;
        float armX = 0.25f * chest.scale().x + 0.25f * rightArm[0].scale().x;
        float elbow = headBottom - 0.5f * rightArm[0].scale().y;
        float legX = 0.25f * rightLeg[0].scale().x;
        float knee = hips - 0.5f * rightLeg[0].scale().y;
        float feet = knee - 0.5f * rightLeg[1].scale().y;
        float lift = -feet;

        List<Piece> pieces = new ArrayList<>();
        pieces.add(piece(head, Part.HEAD, 0, 0, lift));
        pieces.add(piece(chest, Part.BODY, 0, headBottom, lift));
        pieces.add(piece(belly, Part.BODY, 0, bellyTop, lift));
        pieces.add(piece(rightArm[0], Part.ARM_R, -armX, headBottom, lift));
        pieces.add(piece(rightArm[1], Part.FOREARM_R, -armX, elbow, lift));
        pieces.add(piece(leftArm[0], Part.ARM_L, armX, headBottom, lift));
        pieces.add(piece(leftArm[1], Part.FOREARM_L, armX, elbow, lift));
        pieces.add(piece(rightLeg[0], Part.THIGH_R, -legX, hips, lift));
        pieces.add(piece(rightLeg[1], Part.SHIN_R, -legX, knee, lift));
        pieces.add(piece(leftLeg[0], Part.THIGH_L, legX, hips, lift));
        pieces.add(piece(leftLeg[1], Part.SHIN_L, legX, knee, lift));

        Map<Part, Vector3f> pivots = new EnumMap<>(Part.class);
        float shoulder = headBottom - 0.25f * rightArm[0].scale().y;
        pivots.put(Part.BODY, new Vector3f(0, hips + lift, 0));
        pivots.put(Part.HEAD, new Vector3f(0, headBottom + lift, 0));
        pivots.put(Part.ARM_R, new Vector3f(-armX, shoulder + lift, 0));
        pivots.put(Part.ARM_L, new Vector3f(armX, shoulder + lift, 0));
        pivots.put(Part.FOREARM_R, new Vector3f(-armX, elbow + lift, 0));
        pivots.put(Part.FOREARM_L, new Vector3f(armX, elbow + lift, 0));
        pivots.put(Part.THIGH_R, new Vector3f(-legX, hips + lift, 0));
        pivots.put(Part.THIGH_L, new Vector3f(legX, hips + lift, 0));
        pivots.put(Part.SHIN_R, new Vector3f(-legX, knee + lift, 0));
        pivots.put(Part.SHIN_L, new Vector3f(legX, knee + lift, 0));
        return new HeadModel(pieces, pivots, lift);
    }

    /** The upper and lower piece of the limb on one side of the export. */
    private static Raw[] pair(List<Raw> limb, boolean right) {
        List<Raw> side = new ArrayList<>();
        for (Raw raw : limb) {
            if ((raw.position().x > 0) == right) {
                side.add(raw);
            }
        }
        if (side.size() != 2) {
            throw new IllegalArgumentException("a limb does not have two pieces on each side");
        }
        side.sort(Comparator.comparingDouble((Raw r) -> r.position().y).reversed());
        return new Raw[]{side.get(0), side.get(1)};
    }

    private static Piece piece(Raw raw, Part part, float x, float top, float lift) {
        return new Piece(raw.texture(), part, new Vector3f(x, top + lift, 0), new Vector3f(raw.scale()));
    }
}
