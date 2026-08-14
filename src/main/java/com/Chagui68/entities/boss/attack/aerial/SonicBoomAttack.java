package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
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

public class SonicBoomAttack extends BossAttackBase {
    public SonicBoomAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double groundY = boss.getGroundY(center, 40);
        Location blastLoc = new Location(world, center.getX(), groundY + 1.0, center.getZ());

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(60 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(-60 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));
                    double r = 1.0 + phase * 4.0;
                    for (int a = 0; a < 18; a++) {
                        double angle = (2 * Math.PI * a / 18) + t * 0.08;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY(), z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xCCFFFF), 2.0f * (float) phase));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
                } else if (t == 25) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(0), Math.toRadians(30), Math.toRadians(0)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(0), Math.toRadians(-30), Math.toRadians(0)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(20), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(15), 0, 0));
                    world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.8f);
                    world.spawnParticle(Particle.SONIC_BOOM, blastLoc, 10, 1, 1, 1, 0);
                    world.spawnParticle(Particle.EXPLOSION, blastLoc, 20, 5, 2, 5, 0);
                    Vector dir = center.getDirection();
                    if (dir.lengthSquared() < 0.01) dir = new Vector(0, 0, 1);
                    dir.setY(0).normalize();
                    for (Player p : boss.getValidPlayers(world)) {
                        Vector toPlayer = p.getLocation().toVector().subtract(blastLoc.toVector());
                        double dist = toPlayer.length();
                        if (dist < 25) {
                            double dot = toPlayer.normalize().dot(dir);
                            if (dot > 0.3) {
                                double dmg = sealDamage * (1 - dist / 25 * 0.5);
                                p.damage(dmg);
                                p.setVelocity(dir.clone().multiply(2.0 * (1 - dist / 25)).setY(0.5));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 1));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                            }
                        }
                    }
                    for (double d = 0; d < 25; d += 0.5) {
                        Location pl = blastLoc.clone().add(dir.clone().multiply(d));
                        world.spawnParticle(Particle.DUST, pl, 2, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.fromRGB(0xCCFFFF), 2.5f));
                        world.spawnParticle(Particle.SONIC_BOOM, pl, 1, 0.1, 0.1, 0.1, 0);
                    }
                } else if (t < 40) {
                    for (double d = 0; d < 25; d += 0.5) {
                        Location pl = blastLoc.clone().add(center.getDirection().clone().setY(0).normalize().multiply(d));
                        world.spawnParticle(Particle.DUST, pl, 1, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(0xCCFFFF), 1.5f));
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
        return "sonicboom";
    }
}
