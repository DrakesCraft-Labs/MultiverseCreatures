package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Torbellino de lanza: el jefe gira sobre si mismo y golpea a todos los que
 * esten a su alrededor, arrastrandolos hacia el centro antes del segundo giro.
 */
public class WhirlwindSlashAttack extends BossAttackBase {
    private final double slashDamage;
    private final double finalSlashDamage;

    public WhirlwindSlashAttack(BossHost boss) {
        super(boss);
        slashDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.whirlwind-slash-damage", 9.0);
        finalSlashDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.whirlwind-slash-secondary-damage", 6.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand.armorStand();
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            int spins = 0;
            boolean charging = true;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                Location loc = stand.getLocation();

                if (charging) {
                    double phase = Math.min(1.0, (double) t / 15);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(60 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(-60 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));
                    double r = 2.5 + phase * 2.5;
                    for (int a = 0; a < 14; a++) {
                        double angle = (2 * Math.PI * a / 14) + t * 0.06;
                        double x = loc.getX() + Math.cos(angle) * r;
                        double z = loc.getZ() + Math.sin(angle) * r;
                        world.spawnParticle(Particle.DUST, new Location(world, x, loc.getY() + 0.5, z), 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFFAA44), 1.6f * (float) phase));
                    }
                    if (t == 1) world.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 0.8f);
                    if (t >= 15) {
                        charging = false;
                        t = 0;
                    }
                } else {
                    if (t % 20 == 0) {
                        spins++;
                        stand.setBodyPose(new EulerAngle(0, Math.toRadians(spins * 180), 0));
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-140), Math.toRadians(80), Math.toRadians(0)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-140), Math.toRadians(-80), Math.toRadians(0)));
                        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
                        for (int a = 0; a < 20; a++) {
                            double angle = (2 * Math.PI * a / 20);
                            double x = loc.getX() + Math.cos(angle) * 5.5;
                            double z = loc.getZ() + Math.sin(angle) * 5.5;
                            world.spawnParticle(Particle.SWEEP_ATTACK, new Location(world, x, loc.getY() + 1, z), 2, 0.5, 0.5, 0.5, 0);
                        }

                        for (Player p : boss.getValidPlayers(world)) {
                            Vector toP = p.getLocation().toVector().subtract(loc.toVector());
                            double distSq = toP.lengthSquared();
                            if (distSq > 81) continue;
                            if (distSq < 0.01) continue;

                            MscEntityUtils.damageBy(stand, p, slashDamage);
                            Vector pull = toP.normalize().multiply(-1.3);
                            p.setVelocity(p.getVelocity().add(pull.setY(0.25)));
                        }
                        if (spins >= 2) {
                            world.spawnParticle(Particle.EXPLOSION, loc, 10, 2.0, 1.0, 2.0, 0);
                            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
                            for (Player p : boss.getValidPlayers(world)) {
                                if (p.getLocation().distanceSquared(loc) < 49) {
                                    MscEntityUtils.damageBy(stand, p, finalSlashDamage);
                                    Vector away = p.getLocation().toVector().subtract(loc.toVector());
                                    if (away.lengthSquared() > 0.01) {
                                        p.setVelocity(away.normalize().multiply(1.6).setY(0.6));
                                    }
                                }
                            }
                            boss.resetBossPose(instance);
                            cancel();
                            return;
                        }
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "whirlwindslash";
    }
}