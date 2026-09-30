package com.Chagui68.entities.boss.attack.ground;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Obsidian Spire — a directional line of volcanic pillars.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-18: both arms rise straight overhead and the body leans back while the ground ahead charges,
 *       so the player can read which way the line will run.</li>
 *   <li>19: the arms slam forward and down and the pillars erupt one after another, 1.7 blocks apart.
 *       Each one throws its victim <b>upwards</b> instead of pushing them back, which no other ground
 *       attack does.</li>
 *   <li>The cast is marked with an Executioner Cross sigil drawn along the path.</li>
 * </ul>
 */
public class ObsidianSpireAttack extends BossAttackBase {

    private static final int WIND_UP_TICKS = 19;
    private static final int SPIRES = 6;
    private static final double SPACING = 1.7;
    private static final double HIT_RADIUS = 1.5;

    private final double damage;

    public ObsidianSpireAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.obsidian-spire-damage", 11.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location origin = stand.getLocation().clone();
        LivingEntity attacker = stand.entidad();

        Vector facing = origin.getDirection().setY(0);
        if (facing.lengthSquared() < 0.01) facing = new Vector(0, 0, 1);
        final Vector direction = facing.normalize();

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnExecutionerCross(
                    origin.clone().add(direction.clone().multiply(SPIRES * SPACING * 0.5)).add(0, 0.4, 0), 50);
        }

        new BukkitRunnable() {
            int t = 0;
            int spires = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 90) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < WIND_UP_TICKS) {
                    double p = (double) t / WIND_UP_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-30 - 145 * p), Math.toRadians(12), Math.toRadians(-25 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-30 - 145 * p), Math.toRadians(-12), Math.toRadians(25 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-16 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20 * p), 0, 0));

                    for (int i = 0; i < SPIRES; i++) {
                        Location mark = pathPoint(center, direction, i);
                        world.spawnParticle(Particle.DUST, mark, 3, 0.25, 0.15, 0.25, 0,
                                new Particle.DustOptions(Color.fromRGB(0x3B2A55), 1.0f + (float) p));
                        if (t % 3 == 0) {
                            world.spawnParticle(Particle.FLAME, mark, 1, 0.2, 0.05, 0.2, 0.01);
                        }
                    }
                    if (t == 0) world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.4f, 0.7f);
                    if (t % 5 == 0) world.playSound(center, Sound.BLOCK_FIRE_AMBIENT, 0.7f, 0.6f);
                } else if (t == WIND_UP_TICKS) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-18), Math.toRadians(6), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-18), Math.toRadians(-6), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(18), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(14), 0, 0));
                    world.playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.55f);
                    world.playSound(center, Sound.BLOCK_BASALT_BREAK, 1.4f, 0.7f);
                    world.spawnParticle(Particle.EXPLOSION, center, 4, 1.0, 0.2, 1.0, 0);
                } else if (spires < SPIRES && t % 3 == 0) {
                    Location base = pathPoint(center, direction, spires).subtract(0, 0.15, 0);
                    erupt(world, base, 4.2f - spires * 0.45f, attacker);
                    spires++;
                } else if (t > WIND_UP_TICKS + SPIRES * 3 + 12) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Location pathPoint(Location center, Vector direction, int index) {
        return center.clone().add(direction.clone().multiply((index + 1) * SPACING)).add(0, 0.15, 0);
    }

    /** One pillar: it grows out of the ground over a few ticks and throws nearby players upward. */
    private void erupt(World world, Location base, float height, LivingEntity attacker) {
        world.playSound(base, Sound.BLOCK_BASALT_BREAK, 1.2f, 0.55f);
        world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.4f);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step > 7) {
                    cancel();
                    return;
                }

                double grown = height * (step / 7.0);
                for (double y = 0; y <= grown; y += 0.6) {
                    double taper = 1.0 - Math.min(0.55, y * 0.12);
                    world.spawnParticle(Particle.BLOCK, base.clone().add(0, y, 0), 3,
                            0.3 * taper, 0.1, 0.3 * taper, 0.02, Material.OBSIDIAN.createBlockData());
                    world.spawnParticle(Particle.DUST, base.clone().add(0, y, 0), 1, 0.2, 0.1, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFF8844), 1.4f));
                }
                world.spawnParticle(Particle.LAVA, base.clone().add(0, Math.max(0.4, grown - 0.3), 0), 2, 0.2, 0.1, 0.2, 0);

                if (step == 7) {
                    Location tip = base.clone().add(0, height, 0);
                    world.spawnParticle(Particle.FLAME, tip, 12, 0.4, 0.2, 0.4, 0.02);
                    world.spawnParticle(Particle.CLOUD, tip, 8, 0.5, 0.15, 0.5, 0.05);

                    for (Player p : boss.getValidPlayers(world)) {
                        Location pl = p.getLocation();
                        if (pl.getY() > base.getY() - 1.5 && pl.getY() < base.getY() + height
                                && pl.distanceSquared(base) < HIT_RADIUS * HIT_RADIUS) {
                            MscEntityUtils.damageBy(attacker, p, damage);
                            boss.launchPlayer(p, 0.62);
                        }
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "obsidianspire";
    }
}
