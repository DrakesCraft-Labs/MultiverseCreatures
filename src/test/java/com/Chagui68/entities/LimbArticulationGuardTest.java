package com.Chagui68.entities;

import com.Chagui68.entities.boss.JackModel;
import com.Chagui68.entities.boss.JackStarBoss;
import com.Chagui68.entities.boss.NixBoss;
import com.Chagui68.entities.boss.NixModel;
import com.Chagui68.testsupport.LimbGeometry;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The dressed models are exports: somebody else decided how many pieces a limb has, and the code
 * decides which of those pieces a walk folds. When the two disagree the failure is silent — the
 * limb just swings rigid, or folds in the middle — and the per-model tests only look at the limbs
 * they were told about, so a re-export that splits a new limb in two, or shrinks one somebody
 * already folded, slips through all of them.
 *
 * <p>This guard walks <strong>every</strong> limb group of every dressed model and asks the export
 * for its answer: whatever the biggest gap between two stacked pieces is, that is where a joint
 * belongs, and the code has to fold exactly the pieces below it — no more and no fewer. A limb
 * exported in one piece may not report a joint; one exported in segments may not skip it.
 *
 * <p>Only limbs are asked to fold. A body group can be exported in several pieces with a visible
 * gap between them (Kinger's head has seven pieces, neck to cross) and still move as one block: the
 * torso leans around the waist, it does not bend in the middle. What the guard does insist on is
 * that such a group reports no joint at all, so a fold cannot be invented where the model has none.
 */
class LimbArticulationGuardTest {

    /** A gap this wide between two stacked pieces is a joint, not two halves of one segment. */
    private static final double JOINT_GAP = 0.02;

    @Test
    @DisplayName("Kinger articulates exactly the limbs its export splits")
    void kingerArticulatesItsExport() {
        assertArticulated("Kinger", List.of(Kinger.LimbGroup.values()),
                group -> members(Kinger.KingerPart.values(), Kinger.KingerPart::group, group),
                part -> KingerModel.baseTranslation(part).y,
                KingerModel::secondJoint,
                KingerModel::hangsFromSecondJoint);
    }

    @Test
    @DisplayName("NIX articulates exactly the limbs its export splits")
    void nixArticulatesItsExport() {
        assertArticulated("NIX", List.of(NixBoss.LimbGroup.values()),
                group -> members(NixBoss.NixPart.values(), part -> part.group, group),
                part -> NixModel.baseTranslation(part).y,
                NixModel::secondJoint,
                NixModel::hangsFromSecondJoint);
    }

    @Test
    @DisplayName("JACKSTAR articulates exactly the limbs its export splits")
    void jackstarArticulatesItsExport() {
        assertArticulated("JACKSTAR", List.of(JackStarBoss.LimbGroup.values()),
                group -> members(JackStarBoss.JackPart.values(), part -> part.group, group),
                part -> JackModel.baseTranslation(part).y,
                JackModel::secondJoint,
                JackModel::hangsFromSecondJoint);
    }

    /** Checks one model's limb groups against the geometry of its own export. */
    private static <P, G> void assertArticulated(String model, List<G> groups, Function<G, List<P>> members,
                                                 ToDoubleFunction<P> height, Function<G, Vector3f> secondJoint,
                                                 Predicate<P> hangsFromSecondJoint) {
        int articulated = 0;
        for (G group : groups) {
            List<P> limb = members.apply(group);
            if (!isLimb(group)) {
                assertNull(secondJoint.apply(group),
                        model + ": " + group + " is part of the body: it moves as a block around its own pivot "
                                + "and must not expose a second joint");
                continue;
            }
            if (limb.size() < 2) {
                assertNull(secondJoint.apply(group),
                        model + ": " + group + " is a single piece, so nothing can hang from a joint in it");
                continue;
            }

            LimbGeometry.Split<P> split = LimbGeometry.largestGap(limb, height);
            double gap = split.upperEnd() - split.lowerEnd();
            boolean exportedInSegments = gap >= JOINT_GAP;

            if (!exportedInSegments) {
                assertNull(secondJoint.apply(group),
                        model + ": " + group + " is exported as one segment (biggest gap " + gap
                                + "), but the code folds it in the middle");
                continue;
            }

            Vector3f joint = secondJoint.apply(group);
            assertNotNull(joint, model + ": " + group + " is exported in two segments (gap " + gap
                    + ") that the code never articulates, so that half of the limb always swings rigid");
            assertTrue(joint.y <= split.upperEnd() && joint.y >= split.lowerEnd(),
                    model + ": " + group + "'s joint (" + joint.y + ") is not in the gap the export leaves between "
                            + split.upperEnd() + " and " + split.lowerEnd());

            for (P part : limb) {
                boolean below = split.lower().contains(part);
                if (below) {
                    assertTrue(hangsFromSecondJoint.test(part),
                            model + ": " + part + " is below " + group + "'s joint but does not fold with it");
                } else {
                    assertFalse(hangsFromSecondJoint.test(part),
                            model + ": " + part + " is above " + group + "'s joint and must not fold with it");
                }
            }
            articulated++;
        }
        assertTrue(articulated > 0,
                model + " has no articulated limb at all, so this guard proved nothing about it");
    }

    /**
     * Whether a group is a limb, i.e. something a walk folds.
     *
     * <p>By name on purpose: the groups come from each model's own enum, and a new limb added to an
     * export will be called a leg or an arm. Anything else (head, both torso halves) is body.
     */
    private static boolean isLimb(Object group) {
        String name = String.valueOf(group);
        return name.startsWith("LEG_") || name.startsWith("ARM_");
    }

    private static <P, G> List<P> members(P[] parts, Function<P, G> groupOf, G group) {
        return java.util.Arrays.stream(parts).filter(part -> groupOf.apply(part) == group).toList();
    }
}
