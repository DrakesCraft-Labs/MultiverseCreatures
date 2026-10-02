package com.Chagui68.entities.boss.fx;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossArena;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** The stage in the game: a real boss, a real world, real players. */
public final class LiveStage implements Stage {

    /** Tag on every display an attack spawns, so the startup sweep can clear any a crash left behind. */
    public static final String PROP_TAG = "MSC_AttackProp";

    /** How far from the boss a player still counts as part of the fight. */
    private static final double FIGHT_RADIUS = 80.0;

    private final BossHost boss;
    private final BossInstance instance;
    private final BossPuppet body;
    private final World world;
    private final Fx fx;
    private final Random random = new Random();

    public LiveStage(BossHost boss, BossInstance instance) {
        this.boss = boss;
        this.instance = instance;
        this.body = instance.stand;
        this.world = body.getWorld();
        this.fx = new Fx(new WorldSink(world));
    }

    @Override
    public boolean alive() {
        return body.isValid() && !body.isDead();
    }

    @Override
    public BossInstance instance() {
        return instance;
    }

    @Override
    public Vector feet() {
        return body.getLocation().toVector();
    }

    @Override
    public float yaw() {
        return body.getLocation().getYaw();
    }

    @Override
    public double scale() {
        AttributeInstance attribute = body.getAttribute(Attribute.SCALE);
        return attribute == null ? 1.0 : attribute.getValue();
    }

    @Override
    public Pose pose() {
        return instance.pose;
    }

    @Override
    public void pose(Pose pose) {
        instance.pose = pose;
        body.setHeadPose(Pose.euler(pose.head()));
        body.setBodyPose(Pose.euler(pose.body()));
        body.setLeftArmPose(Pose.euler(pose.leftArm()));
        body.setRightArmPose(Pose.euler(pose.rightArm()));
        body.setLeftLegPose(Pose.euler(pose.leftLeg()));
        body.setRightLegPose(Pose.euler(pose.rightLeg()));
    }

    @Override
    public Fx fx() {
        return fx;
    }

    @Override
    public double floorY(double x, double y, double z) {
        double floor = BossArena.findFloorY(new Location(world, x, y + 6, z), 48);
        return Double.isNaN(floor) ? y : floor;
    }

    @Override
    public void moveTo(Vector feet) {
        Location current = body.getLocation();
        body.teleport(new Location(world, feet.getX(), feet.getY(), feet.getZ(), current.getYaw(), current.getPitch()));
    }

    @Override
    public boolean walk(Vector step) {
        Location current = body.getLocation();
        Location next = current.clone();
        boolean moved = BossArena.walk(next, step, false);
        if (moved) body.teleport(next);
        return moved;
    }

    @Override
    public void face(Vector point) {
        Location current = body.getLocation();
        Vector to = point.clone().subtract(current.toVector()).setY(0);
        if (to.lengthSquared() < 1e-6) return;
        current.setDirection(to);
        body.teleport(current);
    }

    @Override
    public Victim target() {
        Player player = boss.detectTarget(body);
        return player == null ? null : new PlayerVictim(player);
    }

    @Override
    public List<Victim> victims() {
        List<Victim> out = new ArrayList<>();
        for (Player player : boss.getValidPlayersNear(body.getLocation(), FIGHT_RADIUS * FIGHT_RADIUS)) {
            out.add(new PlayerVictim(player));
        }
        return out;
    }

    @Override
    public void damage(Victim victim, double amount) {
        if (victim instanceof PlayerVictim pv) {
            MscEntityUtils.damageBy(body.entidad(), pv.player, amount);
        }
    }

    @Override
    public void lightning(Vector at) {
        world.strikeLightningEffect(new Location(world, at.getX(), at.getY(), at.getZ()));
    }

    @Override
    public Prop item(Material material, Vector at, float scale, Quaternionf rotation) {
        ItemDisplay display = world.spawn(at.toLocation(world), ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(material));
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            prepare(d, scale, rotation, new Vector3f());
        });
        return new DisplayProp(display, scale, rotation, false);
    }

    @Override
    public Prop block(Material material, Vector at, float scale, Quaternionf rotation) {
        BlockDisplay display = world.spawn(at.toLocation(world), BlockDisplay.class, d -> {
            d.setBlock(material.createBlockData());
            prepare(d, scale, rotation, blockOffset(scale, rotation));
        });
        return new DisplayProp(display, scale, rotation, true);
    }

    /** A block model's origin is its corner; this centres it on its base so it grows from the point. */
    private static Vector3f blockOffset(float scale, Quaternionf rotation) {
        return new Quaternionf(rotation).transform(new Vector3f(-0.5f * scale, 0, -0.5f * scale));
    }

    private static void prepare(Display display, float scale, Quaternionf rotation, Vector3f translation) {
        display.setPersistent(false);
        display.addScoreboardTag(PROP_TAG);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setViewRange(4.0f);
        display.setTeleportDuration(1);
        display.setTransformation(new Transformation(translation, new Quaternionf(rotation),
                new Vector3f(scale, scale, scale), new Quaternionf()));
    }

    @Override
    public void debris(Vector at, Vector velocity, Material material, int ticks) {
        Material block = material.isBlock() && material.isSolid() ? material : Material.BLACKSTONE;
        float size = 0.45f + random.nextFloat() * 0.5f;
        Quaternionf spin = new Quaternionf().rotationXYZ(random.nextFloat() * 6, random.nextFloat() * 6, random.nextFloat() * 6);
        Prop prop = block(block, at, size, spin);
        new BukkitRunnable() {
            final Vector position = at.clone();
            final Vector speed = velocity.clone();
            int age = 0;

            @Override
            public void run() {
                if (age++ >= ticks) {
                    prop.remove();
                    cancel();
                    return;
                }
                speed.setY(speed.getY() - 0.08);
                speed.multiply(0.98);
                position.add(speed);
                double floor = floorY(position.getX(), position.getY(), position.getZ());
                if (position.getY() < floor) {
                    position.setY(floor);
                    speed.multiply(0.3).setY(Math.abs(speed.getY()) * 0.3);
                    world.spawnParticle(Particle.BLOCK, position.toLocation(world), 4, 0.2, 0.1, 0.2, 0,
                            block.createBlockData(), true);
                }
                prop.moveTo(position, 2);
                if (age % 4 == 0) {
                    prop.reshape(size, spin.rotateXYZ(0.6f, 0.4f, 0.3f), 4);
                }
            }
        }.runTaskTimer(boss.getPlugin(), 1L, 1L);
    }

    @Override
    public Material groundMaterial(Vector at) {
        double floor = floorY(at.getX(), at.getY(), at.getZ());
        Block block = world.getBlockAt((int) Math.floor(at.getX()), (int) Math.floor(floor) - 1, (int) Math.floor(at.getZ()));
        Material type = block.getType();
        return type.isAir() ? Material.BLACKSTONE : type;
    }

    @Override
    public double config(String path, double fallback) {
        return boss.getPlugin().getConfig().getDouble(path, fallback);
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public void onServer(java.util.function.Consumer<World> action) {
        action.accept(world);
    }

    @Override
    public void play(Timeline timeline, int lockTicks) {
        instance.busyUntil = Math.max(instance.busyUntil, instance.clock + Math.max(0, lockTicks));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive()) {
                    timeline.stop();
                    timeline.tick();
                    cancel();
                    return;
                }
                if (!timeline.tick()) cancel();
            }
        }.runTaskTimer(boss.getPlugin(), 0L, 1L);
    }

    /** The same particle and sound kit for code outside a choreography (the other bosses' moves). */
    public static Fx fxIn(World world) {
        return new Fx(new WorldSink(world));
    }

    // ------------------------------------------------------------------ the world's side

    /** Draws into a world, forcing every particle so a fight fourteen blocks tall stays visible from afar. */
    private record WorldSink(World world) implements FxSink {

        @Override
        public void particle(Particle type, Vector at, int count, double spreadX, double spreadY, double spreadZ,
                             double speed, Object data) {
            Object resolved = data instanceof Material material ? material.createBlockData() : data;
            world.spawnParticle(type, at.getX(), at.getY(), at.getZ(), count, spreadX, spreadY, spreadZ, speed,
                    resolved, true);
        }

        @Override
        public void trail(Vector from, Vector to, Color color, int ticks) {
            world.spawnParticle(Particle.TRAIL, from.getX(), from.getY(), from.getZ(), 1, 0, 0, 0, 0,
                    new Particle.Trail(to.toLocation(world), color, ticks), true);
        }

        @Override
        public void sound(Vector at, Sfx sound, float volume, float pitch) {
            world.playSound(at.toLocation(world), sound.key(), org.bukkit.SoundCategory.HOSTILE, volume, pitch);
        }
    }

    private static final class PlayerVictim implements Victim {
        private final Player player;

        PlayerVictim(Player player) {
            this.player = player;
        }

        @Override
        public UUID id() {
            return player.getUniqueId();
        }

        @Override
        public Vector position() {
            return player.getLocation().toVector();
        }

        @Override
        public Vector eyes() {
            return player.getEyeLocation().toVector();
        }

        @Override
        public void push(Vector velocity) {
            player.setVelocity(player.getVelocity().add(velocity));
        }

        @Override
        public void fling(Vector velocity) {
            player.setVelocity(velocity);
        }

        @Override
        public void effect(Affliction affliction, int ticks, int amplifier) {
            PotionEffectType type = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft(affliction.key()));
            if (type != null) player.addPotionEffect(new PotionEffect(type, ticks, amplifier));
        }

        @Override
        public void ignite(int ticks) {
            player.setFireTicks(Math.max(player.getFireTicks(), ticks));
        }

        @Override
        public void moveTo(Vector feet) {
            Location current = player.getLocation();
            player.teleport(new Location(player.getWorld(), feet.getX(), feet.getY(), feet.getZ(),
                    current.getYaw(), current.getPitch()));
        }
    }

    private static final class DisplayProp implements Prop {
        private final Display display;
        private final boolean block;
        private float scale;
        private Quaternionf rotation;

        DisplayProp(Display display, float scale, Quaternionf rotation, boolean block) {
            this.display = display;
            this.scale = scale;
            this.rotation = new Quaternionf(rotation);
            this.block = block;
        }

        @Override
        public Vector position() {
            return display.getLocation().toVector();
        }

        @Override
        public void moveTo(Vector position, int ticks) {
            if (!display.isValid()) return;
            display.setTeleportDuration(Math.max(0, Math.min(59, ticks)));
            Location current = display.getLocation();
            display.teleport(new Location(display.getWorld(), position.getX(), position.getY(), position.getZ(),
                    current.getYaw(), current.getPitch()));
        }

        @Override
        public void resize(float width, float height, Quaternionf rotation, int ticks) {
            if (!display.isValid()) return;
            this.scale = width;
            this.rotation = new Quaternionf(rotation);
            Vector3f translation = block ? blockOffset(width, rotation) : new Vector3f();
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(Math.max(0, ticks));
            display.setTransformation(new Transformation(translation, new Quaternionf(rotation),
                    new Vector3f(width, height, width), new Quaternionf()));
        }

        @Override
        public void glow(Color color) {
            if (!display.isValid()) return;
            display.setGlowColorOverride(color);
            display.setGlowing(true);
        }

        @Override
        public void remove() {
            if (display.isValid()) display.remove();
        }
    }
}
