package com.Chagui68.entities.boss.fx;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Where effects go. In the game that is a world; in the tests and the offline previews it is a
 * recorder, which is what lets an attack's look be checked without a server.
 */
public interface FxSink {

    /**
     * One particle call, as {@code World.spawnParticle} takes it, except that block particles carry
     * their {@code Material}: block data needs a running server, so the game's sink builds it.
     */
    void particle(Particle type, Vector at, int count, double spreadX, double spreadY, double spreadZ,
                  double speed, Object data);

    /** A particle that streaks from {@code from} to {@code to} over {@code ticks} ticks. */
    void trail(Vector from, Vector to, Color color, int ticks);

    void sound(Vector at, Sfx sound, float volume, float pitch);
}
