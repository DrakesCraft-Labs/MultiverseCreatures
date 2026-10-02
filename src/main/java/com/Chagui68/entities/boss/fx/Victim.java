package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;

import java.util.UUID;

/** A player an attack can hit, seen only through what an attack does to it. */
public interface Victim {

    UUID id();

    /** The player's feet. */
    Vector position();

    default Vector eyes() {
        return position().add(new Vector(0, 1.62, 0));
    }

    default Vector chest() {
        return position().add(new Vector(0, 1.0, 0));
    }

    /** Adds to the player's velocity. */
    void push(Vector velocity);

    /** Sets the player's velocity outright. */
    void fling(Vector velocity);

    void effect(Affliction affliction, int ticks, int amplifier);

    void ignite(int ticks);

    /** Moves the player, keeping where they look. */
    void moveTo(Vector feet);
}
