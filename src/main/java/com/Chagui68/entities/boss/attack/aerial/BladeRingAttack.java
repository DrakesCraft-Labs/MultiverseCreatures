package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscLog;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Blade Ring — eight netherite lances orbit the boss and then fly off one by one.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-20: both arms spread straight out while the lances materialise in a visible ring around
 *       him, held together by a wing seal. This is the only attack with <b>props</b> orbiting the
 *       boss.</li>
 *   <li>21-40: he spins and the ring spins with him, sweeping the space around him.</li>
 *   <li>41+: the ring widens and the lances launch outward in a staggered salvo, so the volley has a
 *       rhythm instead of a single burst.</li>
 * </ul>
 */
public class BladeRingAttack extends BossAttackBase {

    private static final String RING_TAG = "MSC_BladeRing";
    private static final int SUMMON_TICKS = 21;
    private static final int SPIN_TICKS = 20;
    private static final int LANCES = 8;
    private static final double RING_RADIUS = 3.0;
    private static final double FIRED_RADIUS = 4.8;
    private static final double HIT_RADIUS = 1.3;

    private final double damage;

    public BladeRingAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.blade-ring-damage", 7.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        LivingEntity attacker = stand.entidad();

        new BukkitRunnable() {
            int t = 0;
            int fired = 0;
            BukkitRunnable wings = null;
            final List<ItemDisplay> ring = new ArrayList<>();
            final List<Location> flying = new ArrayList<>();
            final List<Vector> flyingDirs = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 120) {
                    cleanup();
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t == 0 && plugin.getMagicSealListener() != null && stand.armorStand() != null) {
                    wings = plugin.getMagicSealListener().spawnWingSeal(stand.armorStand());
                    world.playSound(center, Sound.ENTITY_SHULKER_SHOOT, 1.2f, 0.7f);
                }

                if (t < SUMMON_TICKS) {
                    double p = (double) t / SUMMON_TICKS;
                    stand.setRightArmPose(new EulerAngle(0, Math.toRadians(95 * p), Math.toRadians(-8 * p)));
                    stand.setLeftArmPose(new EulerAngle(0, Math.toRadians(-95 * p), Math.toRadians(8 * p)));
                    stand.setBodyPose(new EulerAngle(0, 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-8), 0, 0));

                    if (t == 0) {
                        spawnRing(world, center);
                        world.playSound(center, Sound.BLOCK_ANVIL_PLACE, 1.4f, 1.2f);
                    }
                    for (int i = 0; i < LANCES; i++) {
                        double angle = Math.PI * 2 * i / LANCES + t * 0.12;
                        Location pl = center.clone().add(Math.cos(angle) * RING_RADIUS, 1.5, Math.sin(angle) * RING_RADIUS);
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                } else if (t < SUMMON_TICKS + SPIN_TICKS) {
                    int spinTick = t - SUMMON_TICKS;
                    float spin = (float) (spinTick / (double) SPIN_TICKS) * (float) (Math.PI * 4);
                    stand.setBodyPose(new EulerAngle(0, spin, 0));
                    stand.setHeadPose(new EulerAngle(0, 0, 0));

                    for (int i = 0; i < ring.size(); i++) {
                        ItemDisplay lance = ring.get(i);
                        if (lance == null || !lance.isValid()) continue;
                        double angle = Math.PI * 2 * i / LANCES + spin;
                        Location pl = center.clone().add(Math.cos(angle) * RING_RADIUS, 1.5, Math.sin(angle) * RING_RADIUS);
                        pl.setYaw((float) Math.toDegrees(-angle) + 90f);
                        pl.setPitch(0);
                        lance.teleport(pl);
                        if (spinTick % 4 == 0) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                        }
                    }
                    if (spinTick % 5 == 0) world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
                } else if (fired < LANCES && t % 2 == 0) {
                    fire(world, center, fired);
                    fired++;
                } else if (fired >= LANCES && flying.isEmpty() && t > SUMMON_TICKS + SPIN_TICKS + LANCES * 2 + 10) {
                    cleanup();
                    boss.resetBossPose(instance);
                    cancel();
                }

                advanceLances(world, attacker);
                t++;
            }

            private void spawnRing(World world, Location center) {
                for (int i = 0; i < LANCES; i++) {
                    double angle = Math.PI * 2 * i / LANCES;
                    Location pl = center.clone().add(Math.cos(angle) * RING_RADIUS, 1.5, Math.sin(angle) * RING_RADIUS);
                    pl.setYaw((float) Math.toDegrees(-angle) + 90f);
                    pl.setPitch(0);

                    ItemDisplay lance;
                    try {
                        lance = (ItemDisplay) world.spawnEntity(pl, EntityType.ITEM_DISPLAY);
                    } catch (Throwable refused) {
                        lance = null;
                    }
                    if (lance == null) continue;
                    lance.setItemStack(new ItemStack(Material.NETHERITE_SWORD));
                    lance.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    lance.setBillboard(Display.Billboard.FIXED);
                    try {
                        org.bukkit.util.Transformation transformation = lance.getTransformation();
                        transformation.getScale().set(1.6f);
                        lance.setTransformation(transformation);
                    } catch (Throwable e) {
                        MscLog.debug("could not scale a ring lance", e);
                    }
                    lance.setViewRange(64.0f);
                    lance.setGravity(false);
                    lance.setInvulnerable(true);
                    lance.setPersistent(false);
                    lance.addScoreboardTag(RING_TAG);
                    ring.add(lance);
                }
            }

            private void fire(World world, Location center, int index) {
                Location start;
                Vector direction;
                if (index < ring.size() && ring.get(index) != null && ring.get(index).isValid()) {
                    ItemDisplay lance = ring.get(index);
                    start = lance.getLocation().clone();
                    world.spawnParticle(Particle.CLOUD, start, 8, 0.2, 0.2, 0.2, 0.05);
                    lance.remove();
                } else {
                    double angle = Math.PI * 2 * index / LANCES;
                    start = center.clone().add(Math.cos(angle) * FIRED_RADIUS, 1.5, Math.sin(angle) * FIRED_RADIUS);
                }

                Player nearest = nearestPlayer(start);
                direction = nearest != null
                        ? nearest.getLocation().add(0, 1.0, 0).toVector().subtract(start.toVector()).normalize()
                        : start.toVector().subtract(center.toVector()).setY(0.05).normalize();

                flying.add(start);
                flyingDirs.add(direction.multiply(1.35));
                world.playSound(start, Sound.ENTITY_ARROW_SHOOT, 1.2f, 0.8f);
            }

            /** Moves every launched lance, damages what it touches and retires it after 30 blocks. */
            private void advanceLances(World world, LivingEntity attacker) {
                Iterator<Location> positions = flying.iterator();
                Iterator<Vector> directions = flyingDirs.iterator();
                while (positions.hasNext()) {
                    Location current = positions.next();
                    Vector direction = directions.next();
                    current.add(direction);

                    world.spawnParticle(Particle.END_ROD, current, 2, 0.05, 0.05, 0.05, 0);
                    world.spawnParticle(Particle.DUST, current, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x5566FF), 1.2f));

                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().add(0, 1.0, 0).distanceSquared(current) < HIT_RADIUS * HIT_RADIUS) {
                            MscEntityUtils.damageBy(attacker, p, damage);
                            p.setVelocity(direction.clone().normalize().multiply(0.4).setY(0.18));
                            world.spawnParticle(Particle.CRIT, current, 12, 0.2, 0.2, 0.2, 0.1);
                            positions.remove();
                            directions.remove();
                            break;
                        }
                    }

                    if (current.distanceSquared(stand.getLocation()) > 900) {
                        positions.remove();
                        directions.remove();
                    }
                }
            }

            private Player nearestPlayer(Location from) {
                Player best = null;
                double bestDistance = Double.MAX_VALUE;
                for (Player p : boss.getValidPlayers(world)) {
                    double d = p.getLocation().distanceSquared(from);
                    if (d < bestDistance) {
                        bestDistance = d;
                        best = p;
                    }
                }
                return best;
            }

            private void cleanup() {
                if (wings != null) {
                    wings.cancel();
                    wings = null;
                }
                for (ItemDisplay lance : ring) {
                    if (lance != null && lance.isValid()) {
                        world.spawnParticle(Particle.END_ROD, lance.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
                        lance.remove();
                    }
                }
                ring.clear();
                flying.clear();
                flyingDirs.clear();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "bladering";
    }
}
