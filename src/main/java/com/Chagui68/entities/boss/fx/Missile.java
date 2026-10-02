package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;

import java.util.function.Consumer;

/**
 * A projectile flown by a choreography: it moves every tick, optionally turns towards a target, draws
 * itself, and bursts on the first player it touches or on the ground.
 */
public final class Missile {

    /** Draws the missile at its position, given its direction of travel and its age. */
    @FunctionalInterface
    public interface Look {
        void draw(Vector position, Vector direction, int age);
    }

    private final Vector position;
    private Vector velocity;
    private final double radius;
    private Victim homingOn;
    private double turnRate;
    private double gravity;
    private Look look = (p, d, a) -> { };
    private Consumer<Vector> onBurst = p -> { };
    private Consumer<Victim> onHit = v -> { };
    private boolean burst;
    private int age;
    private Prop prop;

    public Missile(Vector from, Vector velocity, double radius) {
        this.position = from.clone();
        this.velocity = velocity.clone();
        this.radius = radius;
    }

    /** Turns towards {@code target} by at most {@code turnRate} (0..1) of the way each tick. */
    public Missile homing(Victim target, double turnRate) {
        this.homingOn = target;
        this.turnRate = turnRate;
        return this;
    }

    /** Pulls the missile down by {@code gravity} blocks/tick² so it flies in an arc. */
    public Missile gravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public Missile look(Look look) {
        this.look = look;
        return this;
    }

    /** A solid object riding along with the missile (a lance, a crystal), pointed where it flies. */
    public Missile carrying(Prop prop) {
        this.prop = prop;
        return this;
    }

    /** What happens where it bursts, whatever it hit. */
    public Missile onBurst(Consumer<Vector> onBurst) {
        this.onBurst = onBurst;
        return this;
    }

    /** What happens to the player it struck, if it struck one. */
    public Missile onHit(Consumer<Victim> onHit) {
        this.onHit = onHit;
        return this;
    }

    public boolean burst() {
        return burst;
    }

    public Vector position() {
        return position.clone();
    }

    /** Advances one tick; returns false once it has burst. */
    public boolean step(Stage stage) {
        if (burst) return false;
        if (homingOn != null) {
            Vector want = homingOn.chest().subtract(position);
            if (want.lengthSquared() > 1e-6) {
                double speed = velocity.length();
                Vector turned = velocity.clone().normalize().multiply(1 - turnRate)
                        .add(want.normalize().multiply(turnRate));
                velocity = turned.normalize().multiply(speed);
            }
        }
        if (gravity != 0) velocity.setY(velocity.getY() - gravity);
        // Sub-steps, so a fast missile cannot pass through a player between two ticks.
        double speed = velocity.length();
        int steps = Math.max(1, (int) Math.ceil(speed / Math.max(0.3, radius)));
        Vector delta = velocity.clone().multiply(1.0 / steps);
        for (int i = 0; i < steps; i++) {
            position.add(delta);
            for (Victim victim : stage.victimsIn(Area.sphere(position, radius))) {
                burst = true;
                onHit.accept(victim);
            }
            if (!burst && position.getY() <= stage.floorY(position.getX(), position.getY(), position.getZ()) + 0.1) {
                burst = true;
            }
            if (burst) break;
        }
        Vector direction = speed < 1e-6 ? new Vector(0, -1, 0) : velocity.clone().normalize();
        look.draw(position.clone(), direction, age);
        if (prop != null) {
            prop.moveTo(position, 1);
        }
        age++;
        if (burst) {
            if (prop != null) prop.remove();
            onBurst.accept(position.clone());
            return false;
        }
        return true;
    }

    /** Called when the choreography ends with the missile still in flight. */
    public void expire() {
        if (prop != null) prop.remove();
    }
}
