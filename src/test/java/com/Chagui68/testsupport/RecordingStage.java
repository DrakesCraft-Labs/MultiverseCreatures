package com.Chagui68.testsupport;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.FxSink;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * A stage with no server: a flat floor, a few dummy players, and a record of every particle, prop,
 * hit and pose an attack produces, tick by tick. Tests check attacks with it, and the offline
 * previews render them from it.
 */
public final class RecordingStage implements Stage {

    public static final double FLOOR = 40.0;

    /** One particle (or trail) as the attack drew it. */
    public record Mark(int tick, Particle type, Vector at, Vector to, int count, double spread, Object data) {
    }

    /** One hit on a dummy player. */
    public record Hit(int tick, UUID victim, double damage) {
    }

    public final List<Mark> marks = new ArrayList<>();
    public final List<Hit> hits = new ArrayList<>();
    public final List<RecordedProp> props = new ArrayList<>();
    public final List<Pose> poses = new ArrayList<>();
    public final List<Vector> feetByTick = new ArrayList<>();
    public final List<DummyVictim> players = new ArrayList<>();
    public int sounds;
    public int lightning;

    private final Random random = new Random(7);
    private final Fx fx;
    private Vector feet = new Vector(0.5, FLOOR, 0.5);
    private float yaw;
    private final double scale;
    private Pose pose = Pose.REST;
    private Timeline timeline;
    private int lock;
    private int tick;

    public RecordingStage(double scale) {
        this.scale = scale;
        this.fx = new Fx(new Sink());
    }

    /** Adds a dummy player at {@code feet}; the first one is the target. */
    public DummyVictim player(double x, double z) {
        DummyVictim victim = new DummyVictim(new Vector(x, FLOOR, z));
        players.add(victim);
        return victim;
    }

    /** Plays the choreography to its end (or {@code maxTicks}), recording each tick. */
    public int run(int maxTicks) {
        if (timeline == null) return 0;
        while (tick < maxTicks) {
            boolean more = timeline.tick();
            poses.add(pose);
            feetByTick.add(feet.clone());
            for (RecordedProp prop : props) prop.trace.add(prop.removed ? null : prop.position.clone());
            tick++;
            if (!more) break;
        }
        return tick;
    }

    public Timeline timeline() {
        return timeline;
    }

    public int lock() {
        return lock;
    }

    public int tick() {
        return tick;
    }

    public void yaw(float yaw) {
        this.yaw = yaw;
    }

    // ------------------------------------------------------------------ Stage

    @Override
    public boolean alive() {
        return true;
    }

    @Override
    public BossInstance instance() {
        return null;
    }

    @Override
    public Vector feet() {
        return feet.clone();
    }

    @Override
    public float yaw() {
        return yaw;
    }

    @Override
    public double scale() {
        return scale;
    }

    @Override
    public Pose pose() {
        return pose;
    }

    @Override
    public void pose(Pose pose) {
        this.pose = pose;
    }

    @Override
    public Fx fx() {
        return fx;
    }

    @Override
    public double floorY(double x, double y, double z) {
        return FLOOR;
    }

    @Override
    public void moveTo(Vector feet) {
        this.feet = feet.clone();
    }

    /** Columns a test wants to behave as walls for {@link #walk}. */
    public java.util.function.Predicate<Vector> wall = p -> false;

    @Override
    public boolean walk(Vector step) {
        Vector next = feet.clone().add(new Vector(step.getX(), 0, step.getZ()));
        if (wall.test(next)) return false;
        feet = next;
        return true;
    }

    @Override
    public void face(Vector point) {
        Vector to = point.clone().subtract(feet).setY(0);
        if (to.lengthSquared() < 1e-6) return;
        yaw = (float) Math.toDegrees(Math.atan2(-to.getX(), to.getZ()));
    }

    @Override
    public Victim target() {
        return players.isEmpty() ? null : players.get(0);
    }

    @Override
    public List<Victim> victims() {
        return new ArrayList<>(players);
    }

    @Override
    public void damage(Victim victim, double amount) {
        hits.add(new Hit(tick, victim.id(), amount));
    }

    @Override
    public void lightning(Vector at) {
        lightning++;
        marks.add(new Mark(tick, Particle.FLASH, at.clone(), at.clone().add(new Vector(0, 30, 0)), 1, 0, Color.WHITE));
    }

    @Override
    public Prop item(Material material, Vector at, float scale, Quaternionf rotation) {
        RecordedProp prop = new RecordedProp(material, at, scale, rotation, tick);
        props.add(prop);
        return prop;
    }

    @Override
    public Prop block(Material material, Vector at, float scale, Quaternionf rotation) {
        return item(material, at, scale, rotation);
    }

    @Override
    public void debris(Vector at, Vector velocity, Material material, int ticks) {
        Vector p = at.clone();
        Vector v = velocity.clone();
        for (int i = 0; i < Math.min(ticks, 20); i++) {
            v.setY(v.getY() - 0.08);
            p.add(v);
            if (p.getY() < FLOOR) break;
            marks.add(new Mark(tick + i, Particle.BLOCK, p.clone(), null, 1, 0, material));
        }
    }

    @Override
    public Material groundMaterial(Vector at) {
        return Material.BLACKSTONE;
    }

    @Override
    public double config(String path, double fallback) {
        return fallback;
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public void onServer(java.util.function.Consumer<org.bukkit.World> action) {
    }

    @Override
    public void play(Timeline timeline, int lockTicks) {
        this.timeline = timeline;
        this.lock = lockTicks;
    }

    // ------------------------------------------------------------------ parts

    private final class Sink implements FxSink {
        @Override
        public void particle(Particle type, Vector at, int count, double spreadX, double spreadY, double spreadZ,
                             double speed, Object data) {
            if (!Double.isFinite(at.getX()) || !Double.isFinite(at.getY()) || !Double.isFinite(at.getZ())) {
                throw new IllegalStateException("particle at a non-finite position: " + at);
            }
            checkData(type, data);
            marks.add(new Mark(tick, type, at.clone(), null, count, Math.max(spreadX, Math.max(spreadY, spreadZ)), data));
        }

        @Override
        public void trail(Vector from, Vector to, Color color, int ticks) {
            marks.add(new Mark(tick, Particle.TRAIL, from.clone(), to.clone(), 1, 0, color));
        }

        @Override
        public void sound(Vector at, com.Chagui68.entities.boss.fx.Sfx sound, float volume, float pitch) {
            sounds++;
        }
    }

    /**
     * What the server would refuse: a particle whose data is missing or of the wrong type throws
     * inside {@code spawnParticle}. Block particles carry a {@code Material} here; the game's sink
     * turns it into block data.
     */
    static void checkData(Particle type, Object data) {
        Class<?> wanted = type.getDataType();
        if (wanted == Void.class) {
            if (data != null) throw new IllegalStateException(type + " takes no data, got " + data);
            return;
        }
        if (data == null) throw new IllegalStateException(type + " needs " + wanted.getSimpleName() + " data");
        if (org.bukkit.block.data.BlockData.class.isAssignableFrom(wanted)) {
            if (!(data instanceof Material)) throw new IllegalStateException(type + " needs a Material, got " + data);
            return;
        }
        if (!wanted.isInstance(data)) {
            throw new IllegalStateException(type + " needs " + wanted.getSimpleName() + ", got " + data.getClass().getSimpleName());
        }
    }

    public static final class RecordedProp implements Prop {
        public final Material material;
        public final int born;
        public Vector position;
        public float scale;
        public Quaternionf rotation;
        public boolean removed;
        public final List<Vector> trace = new ArrayList<>();
        public org.bukkit.Color glow;

        RecordedProp(Material material, Vector at, float scale, Quaternionf rotation, int born) {
            this.material = material;
            this.position = at.clone();
            this.scale = scale;
            this.rotation = new Quaternionf(rotation);
            this.born = born;
        }

        @Override
        public Vector position() {
            return position.clone();
        }

        @Override
        public void moveTo(Vector position, int ticks) {
            this.position = position.clone();
        }

        public float height = -1;

        @Override
        public void resize(float width, float height, Quaternionf rotation, int ticks) {
            this.scale = width;
            this.height = height;
            this.rotation = new Quaternionf(rotation);
        }

        @Override
        public void glow(org.bukkit.Color color) {
            this.glow = color;
        }

        @Override
        public void remove() {
            removed = true;
        }
    }

    public static final class DummyVictim implements Victim {
        private final UUID id = UUID.randomUUID();
        private Vector feet;
        public final Vector pushed = new Vector();
        public final List<com.Chagui68.entities.boss.fx.Affliction> effects = new ArrayList<>();

        DummyVictim(Vector feet) {
            this.feet = feet;
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public Vector position() {
            return feet.clone();
        }

        @Override
        public void push(Vector velocity) {
            pushed.add(velocity);
        }

        @Override
        public void fling(Vector velocity) {
            pushed.add(velocity);
        }

        @Override
        public void effect(com.Chagui68.entities.boss.fx.Affliction affliction, int ticks, int amplifier) {
            effects.add(affliction);
        }

        @Override
        public void ignite(int ticks) {
        }

        @Override
        public void moveTo(Vector feet) {
            this.feet = feet.clone();
        }
    }
}
