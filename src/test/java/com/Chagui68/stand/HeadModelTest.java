package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The World's BDEngine export reads into a straight, articulated figure. */
class HeadModelTest {

    private static final float EPSILON = 1e-3f;

    private static HeadModel theWorld() throws IOException {
        try (InputStream in = HeadModelTest.class.getResourceAsStream("/stands/the-world.txt")) {
            assertNotNull(in, "the jar ships The World's model");
            return HeadModel.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("The export gives eleven heads, one per part of the skeleton")
    void readsElevenHeads() throws IOException {
        HeadModel model = theWorld();
        assertEquals(11, model.pieces().size());
        Map<Part, Integer> count = new EnumMap<>(Part.class);
        for (HeadModel.Piece piece : model.pieces()) {
            count.merge(piece.part(), 1, Integer::sum);
            assertTrue(piece.texture().length() > 100, "every head has a skin");
        }
        assertEquals(2, count.get(Part.BODY), "chest and belly");
        for (Part part : List.of(Part.HEAD, Part.ARM_R, Part.FOREARM_R, Part.ARM_L, Part.FOREARM_L,
                Part.THIGH_R, Part.SHIN_R, Part.THIGH_L, Part.SHIN_L)) {
            assertEquals(1, count.get(part), part.name());
        }
    }

    @Test
    @DisplayName("It stands straight on its feet, about two heads wide and four heads tall")
    void standsStraight() throws IOException {
        HeadModel model = theWorld();
        assertEquals(1.874f, model.height(), 0.01f, "four heads of 0.937 scale");
        for (HeadModel.Piece piece : model.pieces()) {
            float bottom = piece.anchor().y - 0.5f * piece.scale().y;
            assertTrue(bottom > -EPSILON, piece.part() + " below the feet");
            assertEquals(0, piece.anchor().z, EPSILON, "no piece is left in its exported pose");
            if (piece.part().name().endsWith("_R")) {
                assertTrue(piece.anchor().x < 0, "the right side is on -x");
            }
        }
        HeadModel.Piece head = model.pieces().get(0);
        assertEquals(Part.HEAD, head.part());
        assertEquals(model.height(), head.anchor().y, EPSILON, "the top of the head is the top of the model");
    }

    @Test
    @DisplayName("Elbows and knees stay joined whatever the pose")
    void jointsHold() throws IOException {
        HeadModel model = theWorld();
        for (Map<Part, Quaternionf> pose : List.of(StandRig.barrage(0), StandRig.barrage(1),
                StandRig.idle(StandType.THE_WORLD, 30, 20f))) {
            Map<Part, Frame> frames = StandRig.solve(pose, new Vector3f(), model::pivot);
            for (Part lower : List.of(Part.FOREARM_R, Part.FOREARM_L, Part.SHIN_R, Part.SHIN_L)) {
                Vector3f joint = model.pivot(lower);
                Vector3f onUpper = frames.get(lower.parent()).apply(joint);
                Vector3f onLower = frames.get(lower).apply(joint);
                assertEquals(0, onUpper.distance(onLower), EPSILON, lower + " came apart");
            }
        }
    }

    @Test
    @DisplayName("A head is placed turned round, so the exported face looks forward")
    void facesForward() throws IOException {
        HeadModel model = theWorld();
        HeadModel.Piece head = model.pieces().get(0);
        Map<Part, Frame> frames = StandRig.solve(Map.of(), new Vector3f(), model::pivot);
        Transformation placed = HeadModel.place(head, frames.get(Part.HEAD), 1f);
        Vector3f exportedFront = new Vector3f(0, 0, -1).rotate(placed.getLeftRotation());
        assertEquals(1f, exportedFront.z, EPSILON, "the face the export drew towards -z now looks along +z");
    }

    @Test
    @DisplayName("A boss's body eases into a new pose instead of jumping, and gets there")
    void easesBetweenPoses() throws IOException {
        HeadPuppet puppet = new HeadPuppet(theWorld(), 1f, "test", 0);
        Map<Part, Quaternionf> punch = StandRig.barrage(0);
        Quaternionf target = punch.get(Part.ARM_R);
        float full = angleBetween(new Quaternionf(), target);

        puppet.ease(punch, new Vector3f(), 0.4f, 1);
        float afterOne = angleBetween(puppet.shown(Part.ARM_R), target);
        assertEquals(full * 0.6f, afterOne, 0.02f, "one tick covers 40% of the way, not all of it");

        for (int tick = 0; tick < 40; tick++) {
            puppet.ease(punch, new Vector3f(), 0.4f, 1);
        }
        assertEquals(0, angleBetween(puppet.shown(Part.ARM_R), target), 1e-3f, "it ends in the pose");
    }

    private static float angleBetween(Quaternionf a, Quaternionf b) {
        return new Quaternionf(a).conjugate().mul(b).angle();
    }

    @Test
    @DisplayName("An export that is not an eleven-head humanoid is refused")
    void refusesOtherModels() {
        assertThrows(IllegalArgumentException.class, () -> HeadModel.parse("/summon block_display ~ ~ ~ {}"));
    }

    @Test
    @DisplayName("Armour-stand poses read the same on the rig: -90 on an arm points it forward")
    void armorStandPoses() {
        double[] zero = {0, 0, 0};
        Map<Part, Quaternionf> pose = StandRig.fromArmorStand(zero, zero, zero, new double[]{-90, 0, 0}, zero, zero);
        Vector3f hand = new Vector3f(0, -1, 0).rotate(pose.get(Part.ARM_R));
        assertEquals(1f, hand.z, EPSILON);
    }
}
