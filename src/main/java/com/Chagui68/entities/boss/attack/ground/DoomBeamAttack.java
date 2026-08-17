package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
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

public class DoomBeamAttack extends BossAttackBase {
    public DoomBeamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnPentagramSeal(center.clone().add(0, 0.5, 0), 60, MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 40) {
                    double phase = (double) t / 40;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-45 * phase), Math.toRadians(30 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(45 * phase), Math.toRadians(-30 * phase)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30 * phase), 0, 0));
                    double r = 1.5 + phase * 3.0;
                    for (int a = 0; a < 12; a++) {
                        double angle = (2 * Math.PI * a / 12) + t * 0.06;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + 4 + Math.sin(angle * 2) * 1, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFF2200), 2.0f * (float) phase));
                        world.spawnParticle(Particle.FLAME, pl, 1, 0, 0, 0, 0);
                    }
                    for (int i = 0; i < 4; i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double rr = random.nextDouble() * 2;
                        Location pl = new Location(world, center.getX() + Math.cos(angle) * rr, center.getY() + 4 + random.nextDouble() * 2, center.getZ() + Math.sin(angle) * rr);
                        world.spawnParticle(Particle.EXPLOSION, pl, 1, 0.3, 0.3, 0.3, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5f, 0.4f);
                    if (t % 5 == 0) world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.3f);
                } else if (t == 40) {
                    Player target = boss.detectTarget(stand);
                    if (target != null) {
                        Vector dir = target.getLocation().toVector().subtract(center.toVector());
                        dir.normalize();
                        for (double d = 0; d < 30; d += 0.8) {
                            Location pl = center.clone().add(dir.clone().multiply(d));
                            pl.setY(pl.getY() + 4);
                            world.spawnParticle(Particle.FLAME, pl, 3, 0.2, 0.2, 0.2, 0.02);
                            world.spawnParticle(Particle.DUST, pl, 2, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0xFF2200), 2.0f));
                            world.spawnParticle(Particle.SONIC_BOOM, pl, 1, 0, 0, 0, 0);
                        }
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                        double dmg = sealDamage * 1.5;
                        MscEntityUtils.damageBy(stand, target, dmg);
                        target.setVelocity(dir.multiply(1.5).setY(0.5));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                    }
                } else {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "doombeam";
    }
}
