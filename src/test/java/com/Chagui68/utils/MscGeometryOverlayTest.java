package com.Chagui68.utils;

import com.Chagui68.entities.KingerModel;
import com.Chagui68.entities.boss.JackModel;
import com.Chagui68.entities.boss.NixModel;
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

    private static String corner(Vector point) {
        return String.format("%.4f,%.4f,%.4f", point.getX(), point.getY(), point.getZ());
    }
}
