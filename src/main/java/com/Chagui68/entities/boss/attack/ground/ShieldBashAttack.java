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

public class ShieldBashAttack extends BossAttackBase {
    public ShieldBashAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnRunicTriangleSeal(stand, 60);
        }
        Vector dir = center.getDirection();
        if (dir.lengthSquared() < 0.01) dir = new Vector(0, 0, 1);
        dir.setY(0).normalize();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        final Vector fDir = dir;

        new BukkitRunnable() {
            int t = 0;
            int chargeTicks = 0;
            boolean charging = true;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (charging) {
                    if (t < 20) {
                        double phase = (double) t / 20;
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-45 * phase), Math.toRadians(-30 * phase)));
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(45 * phase), Math.toRadians(30 * phase)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                        world.spawnParticle(Particle.END_ROD, center, 3, 0.5, 1, 0.5, 0.01);
                        if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 0.8f);
                    } else {
                        charging = false;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45), Math.toRadians(30)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-45), Math.toRadians(-30)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(-30), 0, 0));
                        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
                    }
                    t++;
                } else if (chargeTicks < 15) {
                    Location loc = stand.getLocation();
                    loc.add(fDir.clone().multiply(2.0));
                    stand.teleport(loc);
                    world.spawnParticle(Particle.CLOUD, loc, 5, 1, 0.2, 1, 0.02);
                    world.spawnParticle(Particle.CRIT, loc, 3, 0.5, 1, 0.5, 0.03);
                    world.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.5f);
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(loc) < 16) {
                            MscEntityUtils.damageBy(stand, p, 12.0);
                            p.setVelocity(fDir.clone().multiply(2.0).setY(0.5));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                        }
                    }
                    chargeTicks++;
                } else {
                    world.playSound(stand.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
                    world.spawnParticle(Particle.EXPLOSION, stand.getLocation(), 10, 2, 1, 2, 0);
                    boss.resetBossPose(instance);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "shieldbash";
    }
}
