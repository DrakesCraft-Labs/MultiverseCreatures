package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Ráfaga de lanza: tres estocadas rápidas consecutivas contra los jugadores
 * que estén delante del jefe. Cada golpe pega en un cono de 120 grados.
 */
public class LanceFlurryAttack extends BossAttackBase {
    private final double flurryDamage;

    public LanceFlurryAttack(BossHost boss) {
        super(boss);
        flurryDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.lance-flurry-damage", 7.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand.armorStand();
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            int thrusts = 0;
            boolean charging = true;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                Location loc = stand.getLocation();
                Vector dir = loc.getDirection();
                if (dir.lengthSquared() < 0.01) dir = new Vector(0, 0, 1);
                dir.setY(0).normalize();
                final Vector fDir = dir;

                if (charging) {
                    double phase = Math.min(1.0, (double) t / 15);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-110 * phase), Math.toRadians(30 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-110 * phase), Math.toRadians(-30 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));
                    world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 6, 0), 2, 1.5, 0.5, 1.5, 0.01);
                    if (t == 1) world.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 0.9f);
                    if (t >= 15) {
                        charging = false;
                        t = 0;
                    }
                } else {
                    if (t == 0) {
                        thrusts++;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-160), Math.toRadians(45), Math.toRadians(0)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160), Math.toRadians(-45), Math.toRadians(0)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(-20), 0, 0));
                        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.5f, 1.1f + thrusts * 0.1f);
                        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(fDir.clone().multiply(4)), 8, 1.5, 1.5, 1.5, 0);
                        world.spawnParticle(Particle.CRIT, loc.clone().add(fDir.clone().multiply(5)), 20, 2.0, 1.0, 2.0, 0.1);

                        for (Player p : boss.getValidPlayers(world)) {
                            Vector toP = p.getLocation().toVector().subtract(loc.toVector());
                            toP.setY(0);
                            if (toP.lengthSquared() > 49) continue;
                            if (toP.lengthSquared() < 0.01) continue;
                            if (toP.normalize().dot(fDir) < 0.5) continue;

                            MscEntityUtils.damageBy(stand, p, flurryDamage);
                            p.setVelocity(fDir.clone().multiply(1.4).setY(0.4));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                        }
                        if (thrusts >= 3) {
                            boss.resetBossPose(instance);
                            cancel();
                            return;
                        }
                    } else if (t >= 10) {
                        t = -1;
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "lanceflurry";
    }
}