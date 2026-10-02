package com.Chagui68.stand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Killer Queen's second bomb: a small skull-faced tank that rolls after one player and
 * explodes when it reaches them.
 *
 * <p>It is made of displays, so nothing can hurt it: it has no health and no hitbox. It is
 * slow, it phases through whatever it cannot climb (see {@link SheerHeartAttackPath}) and it
 * keeps the chunk it is in loaded with a plugin ticket, so it never stops because nobody is
 * near it. It vanishes when its target leaves the server, when it explodes or when the plugin
 * stops.</p>
 */
public final class SheerHeartAttack {

    /** Scoreboard tag carried by every piece. */
    public static final String TAG = "MSC_SheerHeartAttack";

    private record Piece(BlockData block, Vector3f center, Vector3f size) {
    }

    private final Plugin plugin;
    private final UUID owner;
    private final UUID target;
    private final String targetName;
    private final double speed;
    private final double damage;
    private final double reach;
    private final long dieAt;
    private final List<Entity> parts = new ArrayList<>();
    private final List<Piece> pieces = new ArrayList<>();
    private TextDisplay label;
    private Location position;
    private Chunk ticketed;
    private int age;
    private boolean done;

    public SheerHeartAttack(Plugin plugin, Player owner, Player target, double speed, double damage, double reach,
                            long lifetimeMillis) {
        this.plugin = plugin;
        this.owner = owner.getUniqueId();
        this.target = target.getUniqueId();
        this.targetName = target.getName();
        this.speed = speed;
        this.damage = damage;
        this.reach = reach;
        this.dieAt = lifetimeMillis > 0 ? System.currentTimeMillis() + lifetimeMillis : Long.MAX_VALUE;
        Location start = owner.getLocation();
        start.setPitch(0);
        this.position = start;
        design();
        spawn();
    }

    public UUID owner() {
        return owner;
    }

    public UUID target() {
        return target;
    }

    public boolean done() {
        return done;
    }

    private void design() {
        // Hull, treads, turret dome, the Killer Queen badge and the skull at the front.
        pieces.add(new Piece(Material.LIGHT_GRAY_CONCRETE.createBlockData(), new Vector3f(0, 0.25f, 0),
                new Vector3f(0.56f, 0.26f, 0.7f)));
        for (int side : new int[]{-1, 1}) {
            pieces.add(new Piece(Material.BLACK_CONCRETE.createBlockData(), new Vector3f(side * 0.34f, 0.13f, 0),
                    new Vector3f(0.14f, 0.24f, 0.8f)));
            for (int wheel = -1; wheel <= 1; wheel++) {
                pieces.add(new Piece(Material.GRAY_CONCRETE.createBlockData(),
                        new Vector3f(side * 0.415f, 0.13f, wheel * 0.25f), new Vector3f(0.02f, 0.14f, 0.14f)));
            }
        }
        pieces.add(new Piece(Material.IRON_BLOCK.createBlockData(), new Vector3f(0, 0.44f, -0.06f),
                new Vector3f(0.38f, 0.14f, 0.42f)));
        pieces.add(new Piece(Material.PINK_CONCRETE.createBlockData(), new Vector3f(0, 0.53f, -0.1f),
                new Vector3f(0.12f, 0.04f, 0.12f)));
        pieces.add(new Piece(Material.LIGHT_GRAY_CONCRETE.createBlockData(), new Vector3f(0, 0.62f, -0.22f),
                new Vector3f(0.03f, 0.22f, 0.03f)));
        BlockData skull = Material.SKELETON_SKULL.createBlockData();
        if (skull instanceof Rotatable rotatable) {
            rotatable.setRotation(BlockFace.SOUTH);
        }
        // A skull block only fills the middle of its cube: scaled to 0.8 it is 0.4 wide, its
        // chin at the hull line and its face looking ahead (+z).
        pieces.add(new Piece(skull, new Vector3f(0, 0.52f, 0.32f), new Vector3f(0.8f, 0.8f, 0.8f)));
    }

    private void spawn() {
        World world = position.getWorld();
        loadChunk();
        for (Piece piece : pieces) {
            BlockDisplay display = world.spawn(position, BlockDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.addScoreboardTag(TAG);
                entity.setBlock(piece.block());
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setTeleportDuration(2);
                entity.setGlowColorOverride(Color.fromRGB(0xE7A1C9));
                entity.setGlowing(true);
                Vector3f corner = new Vector3f(piece.center()).sub(new Vector3f(piece.size()).mul(0.5f));
                entity.setTransformation(new Transformation(corner, new Quaternionf(), piece.size(), new Quaternionf()));
            });
            parts.add(display);
        }
        label = world.spawn(position.clone().add(0, 1.0, 0), TextDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.addScoreboardTag(TAG);
            entity.text(Component.text("☠ Sheer Heart Attack", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                    .append(Component.newline())
                    .append(Component.text("» " + targetName, NamedTextColor.GRAY)));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setTeleportDuration(2);
            entity.setBackgroundColor(Color.fromARGB(110, 0, 0, 0));
            entity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(),
                    new Vector3f(0.6f, 0.6f, 0.6f), new Quaternionf()));
        });
        parts.add(label);
        world.playSound(position, Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 1.6f);
    }

    /** One tick of the hunt. */
    public void tick() {
        if (done) {
            return;
        }
        age++;
        Player prey = plugin.getServer().getPlayer(target);
        if (prey == null || !prey.isOnline() || System.currentTimeMillis() > dieAt) {
            remove();
            return;
        }
        World world = position.getWorld();
        if (!prey.getWorld().equals(world) || prey.isDead()) {
            // The target is out of reach for now (another world, respawning): it waits where it is.
            idle();
            return;
        }
        Location goal = prey.getLocation();
        SheerHeartAttackPath.Step step = SheerHeartAttackPath.step(position.getX(), position.getY(), position.getZ(),
                goal.getX(), goal.getY(), goal.getZ(), speed,
                (x, y, z) -> world.getBlockAt(x, y, z).getType().isSolid());
        float yaw = (float) Math.toDegrees(Math.atan2(-(goal.getX() - position.getX()), goal.getZ() - position.getZ()));
        position = new Location(world, step.x(), step.y(), step.z(), yaw, 0);
        loadChunk();
        for (Entity part : parts) {
            if (part.isValid()) {
                part.teleport(part == label ? position.clone().add(0, 1.0, 0) : position);
            }
        }
        if (step.phasing() && age % 3 == 0) {
            world.spawnParticle(Particle.BLOCK, position.clone().add(0, 0.3, 0), 6, 0.3, 0.2, 0.3, 0,
                    Material.STONE.createBlockData());
        }
        effects(world);
        if (SheerHeartAttackPath.arrived(position.getX(), position.getY(), position.getZ(),
                goal.getX(), goal.getY(), goal.getZ(), reach)) {
            explode(prey);
        }
    }

    private void idle() {
        loadChunk();
        if (age % 40 == 0) {
            position.getWorld().spawnParticle(Particle.SMOKE, position.clone().add(0, 0.6, 0), 4, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private void effects(World world) {
        if (age % 4 == 0) {
            world.spawnParticle(Particle.SMOKE, position.clone().add(0, 0.35, -0.4), 2, 0.05, 0.05, 0.05, 0.01);
        }
        if (age % 24 == 0) {
            world.playSound(position, Sound.ENTITY_MINECART_RIDING, 0.35f, 1.6f);
        }
        if (age % 80 == 0) {
            // "Kocchi wo miro!": its one line.
            world.playSound(position, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.8f);
            Location shout = position.clone().add(0, 1.5, 0);
            TextDisplay text = world.spawn(shout, TextDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.addScoreboardTag(TAG);
                entity.text(Component.text("¡Mira aquí!", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            });
            plugin.getServer().getScheduler().runTaskLater(plugin, text::remove, 30L);
        }
    }

    private void explode(Player prey) {
        World world = position.getWorld();
        Location at = prey.getLocation().add(0, 0.8, 0);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, at, 1);
        world.spawnParticle(Particle.FLAME, at, 40, 0.6, 0.6, 0.6, 0.08);
        world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.6f, 0.8f);
        Player bomber = plugin.getServer().getPlayer(owner);
        DamageSource source = bomber != null
                ? DamageSource.builder(DamageType.PLAYER_EXPLOSION).withCausingEntity(bomber).withDirectEntity(bomber)
                .withDamageLocation(at).build()
                : DamageSource.builder(DamageType.EXPLOSION).withDamageLocation(at).build();
        prey.damage(damage, source);
        prey.sendActionBar(Component.text("☠ Sheer Heart Attack te alcanzó", NamedTextColor.LIGHT_PURPLE));
        if (bomber != null) {
            bomber.sendActionBar(Component.text("☠ Sheer Heart Attack alcanzó a " + prey.getName(),
                    NamedTextColor.LIGHT_PURPLE));
        }
        remove();
    }

    /** Keeps the chunk under it loaded, moving the ticket along with it. */
    private void loadChunk() {
        Chunk chunk = position.getChunk();
        if (chunk.equals(ticketed)) {
            return;
        }
        chunk.addPluginChunkTicket(plugin);
        if (ticketed != null) {
            ticketed.removePluginChunkTicket(plugin);
        }
        ticketed = chunk;
    }

    /** Takes it away and releases its chunk. */
    public void remove() {
        if (done) {
            return;
        }
        done = true;
        for (Entity part : parts) {
            if (part.isValid()) {
                part.remove();
            }
        }
        parts.clear();
        if (ticketed != null) {
            ticketed.removePluginChunkTicket(plugin);
            ticketed = null;
        }
    }
}
