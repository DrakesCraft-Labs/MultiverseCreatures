package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import com.Chagui68.utils.DisplaySuit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link HeadModel} in the world: one item display per head, all standing on the same spot and
 * placed by their own transformation, so moving the figure is one teleport per piece and posing it
 * is one transformation per piece. Used by the player's Stand and by DIO's The World alike.
 *
 * <p>Every piece is non persistent and tagged, so a crash or a restart leaves nothing behind; the
 * owner rebuilds the figure when it finds it gone.</p>
 */
public final class HeadPuppet {

    private final HeadModel model;
    private final float scale;
    private final String tag;
    private final int teleportTicks;
    private final List<ItemDisplay> parts = new ArrayList<>();
    private static final Quaternionf IDENTITY = new Quaternionf();
    /** The pose shown now, which {@link #ease} moves towards its target. */
    private final Map<Part, Quaternionf> current = new java.util.EnumMap<>(Part.class);

    /**
     * @param scale         size of the figure; 1 is the model as exported
     * @param teleportTicks how many ticks the client takes to follow a move (0 snaps)
     */
    public HeadPuppet(HeadModel model, float scale, String tag, int teleportTicks) {
        this.model = model;
        this.scale = scale;
        this.tag = tag;
        this.teleportTicks = teleportTicks;
    }

    /** The scale that makes a model {@code height} blocks tall. */
    public static float scaleFor(HeadModel model, double height) {
        return (float) (height / model.height());
    }

    /**
     * Builds the figure at {@code at} (its feet, turned to its yaw), in {@code pose}, the whole of
     * it shrunk to {@code grow} of its size so it can grow from there.
     */
    public void spawn(Location at, Map<Part, Quaternionf> pose, float grow) {
        remove();
        current.clear();
        for (Map.Entry<Part, Quaternionf> joint : pose.entrySet()) {
            current.put(joint.getKey(), new Quaternionf(joint.getValue()));
        }
        Map<Part, Frame> frames = solve(pose, new Vector3f());
        for (HeadModel.Piece piece : model.pieces()) {
            ItemStack head = DisplaySuit.head("MSC_Stand", piece.texture(), "Stand");
            ItemDisplay display = at.getWorld().spawn(at, ItemDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.addScoreboardTag(tag);
                entity.setItemStack(head);
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
                entity.setBillboard(Display.Billboard.FIXED);
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setTeleportDuration(teleportTicks);
                entity.setInterpolationDuration(1);
                entity.setShadowRadius(0f);
                entity.setDisplayWidth(0f);
                entity.setDisplayHeight(0f);
                entity.setTransformation(HeadModel.place(piece, frames.get(piece.part()), scale * grow));
            });
            parts.add(display);
        }
    }

    /** Moves the figure to {@code at}. */
    public void moveTo(Location at) {
        for (ItemDisplay part : parts) {
            if (part.isValid()) {
                part.teleport(at);
            }
        }
    }

    /**
     * Moves the pose a share {@code rate} of the way towards {@code target}, joint by joint, and
     * shows it.
     *
     * <p>This is how a boss is posed every tick. A boss's pose jumps whenever an attack begins or
     * ends; shown as it is, every head would slide on its own to its new place and the limbs would
     * come apart for a moment, which reads as the body shaking. Eased on the server, every tick is a
     * whole, joined pose a little further on.</p>
     */
    public void ease(Map<Part, Quaternionf> target, Vector3f lift, float rate, int ticks) {
        for (Part part : Part.values()) {
            Quaternionf now = current.computeIfAbsent(part, p -> new Quaternionf());
            now.slerp(target.getOrDefault(part, IDENTITY), rate).normalize();
        }
        pose(current, lift, ticks);
    }

    /** The rotation of one joint in the pose shown now, for tests. */
    Quaternionf shown(Part part) {
        return new Quaternionf(current.getOrDefault(part, IDENTITY));
    }

    /** Poses every head, the client sliding there over {@code ticks}. */
    public void pose(Map<Part, Quaternionf> pose, Vector3f lift, int ticks) {
        Map<Part, Frame> frames = solve(pose, lift);
        List<HeadModel.Piece> pieces = model.pieces();
        for (int i = 0; i < parts.size(); i++) {
            ItemDisplay part = parts.get(i);
            if (!part.isValid()) {
                continue;
            }
            HeadModel.Piece piece = pieces.get(i);
            part.setInterpolationDelay(0);
            part.setInterpolationDuration(ticks);
            part.setTransformation(HeadModel.place(piece, frames.get(piece.part()), scale));
        }
    }

    private Map<Part, Frame> solve(Map<Part, Quaternionf> pose, Vector3f lift) {
        return StandRig.solve(pose, lift, model::pivot);
    }

    /** True while every head of the figure is in the world. */
    public boolean alive() {
        for (ItemDisplay part : parts) {
            if (part == null || !part.isValid()) {
                return false;
            }
        }
        return !parts.isEmpty();
    }

    public boolean contains(org.bukkit.entity.Entity entity) {
        return parts.contains(entity);
    }

    public void remove() {
        for (ItemDisplay part : parts) {
            if (part != null && part.isValid()) {
                part.remove();
            }
        }
        parts.clear();
    }
}
