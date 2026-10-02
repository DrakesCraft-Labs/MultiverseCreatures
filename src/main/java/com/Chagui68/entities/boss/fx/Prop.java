package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;
import org.joml.Quaternionf;

/**
 * A solid object an attack puts in the world for a while: a lance, a crystal, a pillar, a block of
 * debris. Display entities in the game, so they move with the client's own interpolation instead of
 * stuttering from tick to tick.
 */
public interface Prop {

    Vector position();

    /** Moves the prop, gliding there over {@code ticks} ticks. */
    void moveTo(Vector position, int ticks);

    /** Changes the prop's size and orientation, gliding there over {@code ticks} ticks. */
    default void reshape(float scale, Quaternionf rotation, int ticks) {
        resize(scale, scale, rotation, ticks);
    }

    /**
     * Changes the prop's width and height (along its own +Y) and orientation, gliding there over
     * {@code ticks} ticks. A block prop stands on its base, so growing its height makes it rise.
     */
    void resize(float width, float height, Quaternionf rotation, int ticks);

    /** Makes the prop glow in a colour. */
    void glow(org.bukkit.Color color);

    void remove();

    /** A rotation turning the model's +Y axis to point along {@code direction}. */
    static Quaternionf pointing(Vector direction) {
        Vector d = direction.clone().normalize();
        return new Quaternionf().rotationTo(0, 1, 0, (float) d.getX(), (float) d.getY(), (float) d.getZ());
    }
}
