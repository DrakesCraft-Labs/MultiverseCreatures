package com.Chagui68.utils;

import com.Chagui68.entities.Kinger;
import com.Chagui68.entities.KingerModel;
import com.Chagui68.entities.boss.JackModel;
import com.Chagui68.entities.boss.NixBoss;
import com.Chagui68.entities.boss.NixModel;
import com.Chagui68.testsupport.ProjectPaths;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the numbers behind {@code /msc debug geometry}.
 *
 * <p>The overlay is only useful if what it draws is the real thing: a box whose twelve edges are the
 * box (an outline missing an edge would hide the very gap the audit is looking for), and a joint
 * placed in the same frame the display pieces are positioned in — if the dot and the limb disagree,
 * the overlay would lie about a model being correct.
 */
class MscGeometryOverlayTest {

    @Test
    @DisplayName("The hitbox is drawn along its twelve edges and nothing else")
    void theBoxIsDrawnAlongItsTwelveEdges() {
        BoundingBox box = new BoundingBox(0.0, 0.0, 0.0, 0.5, 1.975, 0.5);
        List<MscGeometryOverlay.Segment> edges = MscGeometryOverlay.boxEdges(box);

        assertEquals(12, edges.size());
        Set<String> corners = new HashSet<>();
        double total = 0.0;
        for (MscGeometryOverlay.Segment edge : edges) {
            int axes = 0;
            if (edge.from().getX() != edge.to().getX()) axes++;
            if (edge.from().getY() != edge.to().getY()) axes++;
            if (edge.from().getZ() != edge.to().getZ()) axes++;
            assertEquals(1, axes, "an edge of a box runs along exactly one axis: " + edge);

            total += edge.from().distance(edge.to());
            corners.add(corner(edge.from()));
            corners.add(corner(edge.to()));
        }

        assertEquals(4.0 * (0.5 + 1.975 + 0.5), total, 1.0e-9,
                "the edges must add up to four of each of the three side lengths");
        assertEquals(8, corners.size(), "the twelve edges must meet at exactly the eight corners");
    }

    @Test
    @DisplayName("An empty box still draws its edges instead of throwing")
    void anEmptyBoxIsStillDrawn() {
        List<MscGeometryOverlay.Segment> edges = MscGeometryOverlay.boxEdges(new BoundingBox(0, 0, 0, 0, 0, 0));
        assertEquals(12, edges.size());
        for (MscGeometryOverlay.Segment edge : edges) {
            assertEquals(0.0, edge.from().distance(edge.to()), 1.0e-9);
        }
    }

    @Test
    @DisplayName("A joint is placed in the same frame the display pieces use")
    void jointsShareTheDisplayFrame() {
        Vector3f rightArm = new Vector3f(0.0742f, 1.6296f, -0.159f);

        // A stand facing north shows the suit through a half turn: the model faces the way the stand
        // faces, so the joint mirrors around the stand's axis.
        Vector facingNorth = MscGeometryOverlay.offsetFor(new Location(null, 0, 0, 0, 0, 0), rightArm);
        assertEquals(-rightArm.x, facingNorth.getX(), 1.0e-3);
        assertEquals(rightArm.y, facingNorth.getY(), 1.0e-3);
        assertEquals(-rightArm.z, facingNorth.getZ(), 1.0e-3);

        // Turning the boss turns the joint with it, and never changes a height.
        Vector quarterTurn = MscGeometryOverlay.offsetFor(new Location(null, 0, 0, 0, 90, 0), rightArm);
        assertEquals(rightArm.y, quarterTurn.getY(), 1.0e-3, "a joint must never float away from its height");
        assertEquals(rightArm.z, quarterTurn.getX(), 1.0e-3);
        assertEquals(-rightArm.x, quarterTurn.getZ(), 1.0e-3);

        for (float yaw : new float[]{0, 45, 90, 135, 180, 225, 270, 315}) {
            Vector offset = MscGeometryOverlay.offsetFor(new Location(null, 0, 0, 0, yaw, 0), rightArm);
            assertEquals(Math.hypot(rightArm.x, rightArm.z), Math.hypot(offset.getX(), offset.getZ()), 1.0e-3,
                    "the joint must keep its distance from the stand at yaw " + yaw);
        }
    }

    @Test
    @DisplayName("Every dressed boss's shipped joints, elbows and knees included, sit inside its own body")
    void jointsSitInsideTheBody() {
        // The dot is only useful sitting under the limb it drives: a pivot above the head or below
        // the feet would draw a line to nothing. These are the real constants the command hands over.
        List<Vector3f> joints = List.of(
                KingerModel.PIVOT_SHOULDER_RIGHT, KingerModel.PIVOT_SHOULDER_LEFT,
                KingerModel.PIVOT_HIP_RIGHT, KingerModel.PIVOT_HIP_LEFT,
                KingerModel.PIVOT_KNEE_RIGHT, KingerModel.PIVOT_KNEE_LEFT,
                KingerModel.PIVOT_NECK, KingerModel.PIVOT_TORSO,
                NixModel.PIVOT_SHOULDER_RIGHT, NixModel.PIVOT_SHOULDER_LEFT,
                NixModel.PIVOT_HIP_RIGHT, NixModel.PIVOT_HIP_LEFT,
                NixModel.PIVOT_ELBOW_RIGHT, NixModel.PIVOT_ELBOW_LEFT,
                NixModel.PIVOT_KNEE_RIGHT, NixModel.PIVOT_KNEE_LEFT,
                NixModel.PIVOT_NECK, NixModel.PIVOT_TORSO,
                JackModel.PIVOT_SHOULDER_RIGHT, JackModel.PIVOT_SHOULDER_LEFT,
                JackModel.PIVOT_HIP_RIGHT, JackModel.PIVOT_HIP_LEFT,
                JackModel.PIVOT_ELBOW_RIGHT, JackModel.PIVOT_ELBOW_LEFT,
                JackModel.PIVOT_KNEE_RIGHT, JackModel.PIVOT_KNEE_LEFT,
                JackModel.PIVOT_NECK, JackModel.PIVOT_TORSO);

        for (Vector3f joint : joints) {
            assertTrue(joint.y > 0.2f && joint.y < 2.2f, "joint out of the body: " + joint);
            assertTrue(Math.abs(joint.x) < 0.5f, "joint off the body axis: " + joint);
            assertEquals(0f, joint.z, 1.0e-3f, "a joint hangs on the body's own plane: " + joint);
        }
    }

    @Test
    @DisplayName("A walk replay draws the stand's hitbox and the bones that connect the joints")
    void aWalkReplayDrawsTheStandBoxAndItsBones() {
        // The preview has no stand to measure, so its box is the scaled stand's own: half a block
        // wide and 1.975 tall at scale 1, centred on the anchor with its feet on it.
        Location anchor = new Location(null, 10.5, 64.0, -3.25, 0f, 0f);
        BoundingBox kinger = MscGeometryOverlay.previewBox(anchor, Kinger.MODEL_HITBOX_SCALE);
        assertEquals(0.5, kinger.getWidthX(), 1.0e-9);
        assertEquals(1.975, kinger.getHeight(), 1.0e-9);
        assertEquals(anchor.getX(), kinger.getCenterX(), 1.0e-9);
        assertEquals(anchor.getZ(), kinger.getCenterZ(), 1.0e-9);
        assertEquals(anchor.getY(), kinger.getMinY(), 1.0e-9);

        BoundingBox nix = MscGeometryOverlay.previewBox(anchor, NixBoss.MODEL_HITBOX_SCALE);
        assertEquals(0.5 * NixBoss.MODEL_HITBOX_SCALE, nix.getWidthX(), 1.0e-9);
        assertEquals(1.975 * NixBoss.MODEL_HITBOX_SCALE, nix.getHeight(), 1.0e-9);

        // A limb that folds is two bones meeting at its joint; a rigid one is a single bone.
        Vector3f pivot = new Vector3f(0f, 1f, 0f);
        Vector3f joint = new Vector3f(0f, 0.6f, 0f);
        Vector3f tip = new Vector3f(0f, 0.2f, 0f);
        List<MscGeometryOverlay.Segment> folded =
                MscGeometryOverlay.limbSegments(List.of(new MscLimb.Limb(pivot, joint, tip)));
        assertEquals(2, folded.size());
        assertEquals(0.0, folded.get(0).from().distance(vectorOf(pivot)), 1.0e-9);
        assertEquals(0.0, folded.get(0).to().distance(vectorOf(joint)), 1.0e-9);
        assertEquals(0.0, folded.get(1).from().distance(vectorOf(joint)), 1.0e-9,
                "the two bones must meet at the joint instead of leaving a gap");
        assertEquals(0.0, folded.get(1).to().distance(vectorOf(tip)), 1.0e-9);

        List<MscGeometryOverlay.Segment> rigid =
                MscGeometryOverlay.limbSegments(List.of(new MscLimb.Limb(pivot, null, tip)));
        assertEquals(1, rigid.size());
        assertEquals(0.0, rigid.get(0).from().distance(vectorOf(pivot)), 1.0e-9);
        assertEquals(0.0, rigid.get(0).to().distance(vectorOf(tip)), 1.0e-9);

        // And the rigs the command ships: Kinger's arms are single pieces, so six bones; NIX and Jack
        // Star were exported in two segments on all four limbs, so eight.
        assertEquals(6, MscGeometryOverlay.limbSegments(KingerModel.walkPose(0.4f)).size());
        assertEquals(8, MscGeometryOverlay.limbSegments(NixModel.walkPose(0.4f)).size());
        assertEquals(8, MscGeometryOverlay.limbSegments(JackModel.walkPose(0.4f)).size());
    }

    @Test
    @DisplayName("The command replays every model that walks, at the pace the model walks at")
    void theCommandReplaysEveryModelThatWalks() {
        String source = ProjectPaths.read(
                ProjectPaths.source("com", "Chagui68", "commands", "MSCCommand.java"));

        for (String model : List.of("KingerModel", "NixModel", "JackModel")) {
            assertTrue(source.contains(model + "::walkPose"),
                    model + " is not replayable, so its walk can only be judged by provoking a boss");
            assertTrue(source.contains(model + ".WALK_RATE"),
                    model + "'s replay must step at the pace the boss itself walks");
        }
        assertTrue(source.contains("MscGeometryOverlay.showWalk("),
                "the walk replay belongs to the overlay, not to a second drawing in the command");
        assertEquals(3, occurrences(source, "::walkPose"),
                "a fourth walk rig would mean a model that does not exist, or a copy of one that does");
    }

    private static Vector vectorOf(Vector3f point) {
        return new Vector(point.x, point.y, point.z);
    }

    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = source.indexOf(needle); index >= 0; index = source.indexOf(needle, index + 1)) {
            count++;
        }
        return count;
    }

    private static String corner(Vector point) {
        return String.format("%.4f,%.4f,%.4f", point.getX(), point.getY(), point.getZ());
    }
}
