package com.Chagui68.entities.boss.fx;

import com.Chagui68.entities.BossInstance;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Everything a choreography may touch: the boss's body, the effects, the ground, the players and the
 * clock. Attacks talk only to this, which is why an attack can be played in a test or rendered
 * offline exactly as it plays in the game.
 */
public interface Stage {

    /** Whether the boss is still there; a choreography stops when it is not. */
    boolean alive();

    /** The fight this stage belongs to, or null in a preview. */
    BossInstance instance();

    Vector feet();

    float yaw();

    double scale();

    Pose pose();

    void pose(Pose pose);

    default SentinelBody.Anatomy body() {
        return SentinelBody.resolve(feet(), yaw(), scale(), pose());
    }

    /** The boss's forward direction, flat. */
    default Vector forward() {
        return body().forward();
    }

    Fx fx();

    /** Height of the floor under a point, scanning down from a little above it. */
    double floorY(double x, double y, double z);

    /** The point moved onto the floor, just above it so a decal is not swallowed by the block. */
    default Vector onGround(Vector p) {
        return new Vector(p.getX(), floorY(p.getX(), p.getY(), p.getZ()) + 0.15, p.getZ());
    }

    /** Moves the boss, keeping its heading. */
    void moveTo(Vector feet);

    /**
     * Moves the boss by a horizontal {@code step} the way it walks: up one block at most, down by
     * falling, never through a wall.
     *
     * @return false when a wall stopped it
     */
    boolean walk(Vector step);

    /** Turns the boss to look at a point. */
    void face(Vector point);

    /** The player the boss is fighting, or null. */
    Victim target();

    /** Every player in the fight. */
    List<Victim> victims();

    default List<Victim> victimsIn(Area area) {
        List<Victim> inside = new ArrayList<>();
        for (Victim victim : victims()) {
            if (area.contains(victim.position())) inside.add(victim);
        }
        return inside;
    }

    void damage(Victim victim, double amount);

    /** Damages everyone inside {@code area} and applies {@code then} to each of them. */
    default int hit(Area area, double damage, Consumer<Victim> then) {
        List<Victim> inside = victimsIn(area);
        for (Victim victim : inside) {
            damage(victim, damage);
            if (then != null) then.accept(victim);
        }
        return inside.size();
    }

    /** A lightning bolt that looks and sounds real but burns nothing. */
    void lightning(Vector at);

    /** An item shown as a solid object: lances, crystals, skulls. */
    Prop item(Material material, Vector at, float scale, Quaternionf rotation);

    /** A block shown as a solid object: pillars, spikes, boulders. */
    Prop block(Material material, Vector at, float scale, Quaternionf rotation);

    /** A chunk of {@code material} flung from {@code at}, falling under gravity for a while. */
    void debris(Vector at, Vector velocity, Material material, int ticks);

    /** The block the ground at a point is made of, for dust and debris that match the floor. */
    Material groundMaterial(Vector at);

    /** A number from config.yml (a full path), or the fallback when it is missing. */
    double config(String path, double fallback);

    Random random();

    /**
     * Runs {@code action} against the real world, and does nothing in a preview: summons and other
     * things that only exist on a server.
     */
    void onServer(Consumer<org.bukkit.World> action);

    /**
     * Plays a choreography to the end, one step per tick. The boss starts nothing else for the
     * first {@code lockTicks} ticks, so two attacks never fight over its arms.
     */
    void play(Timeline timeline, int lockTicks);
}
