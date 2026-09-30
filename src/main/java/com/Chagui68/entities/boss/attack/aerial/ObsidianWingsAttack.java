package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Obsidian Wings — the boss grows a pair of obsidian wings and beats them.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-19: four wing panels materialise around his shoulders and unfold while the arms cross over
 *       the chest. No other aerial attack carries <b>props attached to the boss himself</b>.</li>
 *   <li>20/32/44: three wing beats. Each one sweeps the panels down hard and throws an expanding ring
 *       of obsidian shards outward that shoves everyone caught in its band.</li>
 *   <li>56: the wings snap shut against the body and both of them slam down together, cracking a
 *       black shockwave under him before the panels dissolve.</li>
 * </ul>
 */
public class ObsidianWingsAttack extends BossAttackBase {

    private static final String WING_TAG = "MSC_ObsidianWings";
    private static final int SUMMON_TICKS = 20;
    private static final int FLAP_TICKS = 12;
    private static final int FLAPS = 3;
    private static final int RING_STEPS = 12;
    private static final double RING_START = 2.2;
    private static final double RING_END = 7.2;
    private static final double SLAM_RADIUS = 6.0;

    private final double flapDamage;
    private final double slamDamage;

    public ObsidianWingsAttack(BossHost boss) {
        super(boss);
        this.flapDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.obsidian-wings-damage", 8.0);
        this.slamDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.obsidian-wings-slam-damage", 15.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        LivingEntity attacker = stand.entidad();

        Player target = boss.detectTarget(stand);
        if (target != null) {
            Vector to = target.getLocation().toVector().subtract(stand.getLocation().toVector()).setY(0);
            if (to.lengthSquared() > 0.01) {
                Location turned = stand.getLocation().clone();
                turned.setDirection(to);
                stand.teleport(turned);
            }
        }

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnStormSeal(stand.getLocation().clone().subtract(0, 1.6, 0), 120);
        }

        new BukkitRunnable() {
            int t = 0;
            boolean slammed = false;
            final List<ItemDisplay> wings = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 140) {
                    cleanup(world, wings);
                    cancel();
                    return;
                }

                Location center = stand.getLocation();
                boolean beating = t >= SUMMON_TICKS && (t - SUMMON_TICKS) / FLAP_TICKS < FLAPS;
                int flapTick = beating ? (t - SUMMON_TICKS) % FLAP_TICKS : 0;
                boolean tucked = t >= SUMMON_TICKS + FLAPS * FLAP_TICKS;

                if (t == 0) {
                    summonWings(world, center, wings);
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.8f, 0.6f);
                }

                double spread = t < SUMMON_TICKS ? (double) t / SUMMON_TICKS : 1.0;
                double beat = beating ? flapCurve(flapTick) : 0.1 + Math.sin(t * 0.25) * 0.08;
                placeWings(world, center, wings, spread, tucked, beat);

                if (t < SUMMON_TICKS) {
                    double p = (double) t / SUMMON_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-30 - 40 * p), Math.toRadians(-95 * p), Math.toRadians(-35 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-30 - 40 * p), Math.toRadians(95 * p), Math.toRadians(35 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-12 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-18 * p), 0, 0));

                    for (int side = -1; side <= 1; side += 2) {
                        for (int row = 0; row < 2; row++) {
                            Location pl = wingAnchor(center, side, row, spread, false, 0.15);
                            world.spawnParticle(Particle.SMOKE, pl, 2, 0.3, 0.3, 0.3, 0.01);
                            world.spawnParticle(Particle.DUST, pl, 1, 0.2, 0.2, 0.2, 0,
                                    new Particle.DustOptions(Color.fromRGB(0x3B2A55), 1.2f));
                        }
                    }
                    if (t == SUMMON_TICKS / 2) world.playSound(center, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.1f, 0.7f);
                } else if (beating) {
                    double swing = flapCurve(flapTick);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20 + 55 * swing), Math.toRadians(-30), Math.toRadians(-70 * swing)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20 + 55 * swing), Math.toRadians(30), Math.toRadians(70 * swing)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-8 + 22 * swing), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-14 + 10 * swing), 0, 0));

                    if (flapTick == 3) ring(world, center, attacker);
                } else if (!slammed) {
                    slammed = true;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(35), Math.toRadians(-40), Math.toRadians(-110)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(35), Math.toRadians(40), Math.toRadians(110)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(26), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(30), 0, 0));
                    slam(world, center, attacker);
                } else if (t > SUMMON_TICKS + FLAPS * FLAP_TICKS + 22) {
                    cleanup(world, wings);
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** One wing beat: sweeps from raised to fully down inside the beat window. */
    private double flapCurve(int tick) {
        if (tick < 4) return tick / 4.0;
        return Math.max(0.0, 1.0 - (tick - 4) / (double) (FLAP_TICKS - 4));
    }

    /** Where a wing panel sits relative to the boss. */
    private Location wingAnchor(Location center, int side, int row, double spread, boolean tucked, double beat) {
        double out = (tucked ? 1.0 : 1.9 + row * 0.95) * (0.35 + 0.65 * spread);
        double up = (tucked ? 0.7 : 1.35 + row * 0.3) + beat * 0.9;
        double back = -0.15 - row * 0.35;
        Vector right = new Vector(Math.cos(Math.toRadians(center.getYaw())), 0, Math.sin(Math.toRadians(center.getYaw())));
        Vector facing = new Vector(-Math.sin(Math.toRadians(center.getYaw())), 0, Math.cos(Math.toRadians(center.getYaw())));
        return center.clone().add(right.multiply(side * out)).add(0, up, 0).add(facing.multiply(back));
    }

    private void placeWings(World world, Location center, List<ItemDisplay> wings, double spread, boolean tucked, double beat) {
        if (wings.isEmpty()) return;
        int index = 0;
        for (int side = -1; side <= 1; side += 2) {
            for (int row = 0; row < 2; row++) {
                ItemDisplay wing = index < wings.size() ? wings.get(index) : null;
                index++;
                if (wing == null || !wing.isValid()) continue;
                Location loc = wingAnchor(center, side, row, spread, tucked, beat);
                loc.setYaw(center.getYaw() + side * (52 + row * 12));
                loc.setPitch((float) (-58 * beat + 12));
                wing.teleport(loc);
                if (!tucked) {
                    world.spawnParticle(Particle.DUST, loc, 1, 0.2, 0.2, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(0x2A1B4A), 1.4f));
                }
            }
        }
    }

    private void summonWings(World world, Location center, List<ItemDisplay> wings) {
        for (int i = 0; i < 4; i++) {
            Location loc = center.clone();
            ItemDisplay wing;
            try {
                wing = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
            } catch (Throwable refused) {
                wing = null;
            }
            if (wing == null) continue;
            wing.setItemStack(new ItemStack(Material.ELYTRA));
            wing.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            wing.setBillboard(Display.Billboard.FIXED);
            try {
                org.bukkit.util.Transformation transformation = wing.getTransformation();
                transformation.getScale().set(1.7f);
                wing.setTransformation(transformation);
            } catch (Throwable ignored) {
            }
            wing.setViewRange(64.0f);
            wing.setGravity(false);
            wing.setInvulnerable(true);
            wing.setPersistent(false);
            wing.addScoreboardTag(WING_TAG);
            wings.add(wing);
        }
    }

    /** One wing beat pushes a band of obsidian shards outwards; each player is caught once. */
    private void ring(World world, Location center, LivingEntity attacker) {
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.6f, 0.55f);
        world.playSound(center, Sound.BLOCK_BASALT_BREAK, 1.1f, 0.8f);

        new BukkitRunnable() {
            int step = 0;
            final Set<UUID> caught = new HashSet<>();

            @Override
            public void run() {
                if (step > RING_STEPS) {
                    cancel();
                    return;
                }
                double radius = RING_START + (RING_END - RING_START) * (step / (double) RING_STEPS);
                int points = (int) Math.max(18, radius * 10);
                for (int i = 0; i < points; i++) {
                    double angle = Math.PI * 2 * i / points;
                    Location pl = center.clone().add(Math.cos(angle) * radius, 1.0, Math.sin(angle) * radius);
                    world.spawnParticle(Particle.BLOCK, pl, 1, 0.05, 0.05, 0.05, 0, Material.OBSIDIAN.createBlockData());
                    if (i % 2 == 0) world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                    if (i % 4 == 0) world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x2A1B4A), 1.5f));
                }
                for (Player p : boss.getValidPlayers(world)) {
                    double distance = p.getLocation().distance(center);
                    if (Math.abs(distance - radius) > 1.1 || !caught.add(p.getUniqueId())) continue;
                    MscEntityUtils.damageBy(attacker, p, flapDamage);
                    Vector away = p.getLocation().toVector().subtract(center.toVector()).setY(0);
                    if (away.lengthSquared() < 0.01) away = new Vector(0, 0, 1);
                    p.setVelocity(away.normalize().multiply(0.75).setY(0.32));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0));
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** The two tucked wings slam down together: black shockwave plus a heavy hit underneath. */
    private void slam(World world, Location center, LivingEntity attacker) {
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
        world.playSound(center, Sound.BLOCK_ANVIL_LAND, 1.6f, 0.5f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 0.6, 0), 1);
        world.spawnParticle(Particle.BLOCK, center.clone().add(0, 0.4, 0), 90, 2.4, 0.3, 2.4, 0.06,
                Material.OBSIDIAN.createBlockData());
        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.3, 0), 60, 2.0, 0.2, 2.0, 0,
                new Particle.DustOptions(Color.fromRGB(0x1B0B2A), 2.2f));
        boss.spawnShockwaveWave(world, center, SLAM_RADIUS);

        for (Player p : boss.getValidPlayers(world)) {
            if (p.getLocation().distanceSquared(center) > SLAM_RADIUS * SLAM_RADIUS) continue;
            MscEntityUtils.damageBy(attacker, p, slamDamage);
            Vector away = p.getLocation().toVector().subtract(center.toVector()).setY(0);
            if (away.lengthSquared() < 0.01) away = new Vector(0, 0, 1);
            p.setVelocity(away.normalize().multiply(0.5).setY(0.55));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
        }
    }

    private void cleanup(World world, List<ItemDisplay> wings) {
        for (ItemDisplay wing : wings) {
            if (wing != null && wing.isValid()) {
                world.spawnParticle(Particle.CLOUD, wing.getLocation(), 8, 0.2, 0.2, 0.2, 0.04);
                wing.remove();
            }
        }
        wings.clear();
    }

    @Override
    public String getName() {
        return "obsidianwings";
    }
}
