package com.Chagui68.entities.boss.attack.ranged;

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

public class VoidRiftAttack extends BossAttackBase {
    public VoidRiftAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        Location riftLoc = target.getLocation();
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 90) {
                    cancel();
                    return;
                }
                if (t < 30) {
                    double phase = (double) t / 30;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(60), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-60), 0));
                    int samples = (int) (8 + phase * 20);
                    for (int i = 0; i < samples; i++) {
                        double angle = (2 * Math.PI * i / samples) + t * 0.08;
                        double r = 4.0 * phase;
                        double x = riftLoc.getX() + Math.cos(angle) * r;
                        double z = riftLoc.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, riftLoc.getY() + 0.1, z);
                        world.spawnParticle(Particle.PORTAL, pl, 2, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x440066), 1.5f * (float) phase));
                    }
                    if (t == 1) world.playSound(riftLoc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.5f);
                } else if (t < 75) {
                    int samples = 28;
                    for (int i = 0; i < samples; i++) {
                        double angle = (2 * Math.PI * i / samples) + t * 0.05;
                        double r = 4.0;
                        double x = riftLoc.getX() + Math.cos(angle) * r;
                        double z = riftLoc.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, riftLoc.getY() + 0.1, z);
                        world.spawnParticle(Particle.PORTAL, pl, 3, 0.1, 0.1, 0.1, 0.08);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x440066), 2.0f));
                    }
                    for (Player p : boss.getValidPlayers(world)) {
                        Vector toCenter = riftLoc.toVector().subtract(p.getLocation().toVector());
                        double dist = toCenter.length();
                        if (dist < 8 && dist > 0.5) {
                            p.setVelocity(p.getVelocity().add(toCenter.normalize().multiply(0.12)));
                            if (dist < 4.5) {
                                p.damage(sealDamage * 0.3);
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 30, 0));
                            }
                        }
                    }
                    if (t % 10 == 0) world.playSound(riftLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 0.4f);
                } else if (t == 75) {
                    world.spawnParticle(Particle.EXPLOSION, riftLoc.clone().add(0, 1, 0), 25, 2, 1, 2, 0);
                    world.playSound(riftLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.4f);
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(riftLoc) < 25) {
                            p.damage(sealDamage * 0.7);
                            Vector away = p.getLocation().toVector().subtract(riftLoc.toVector());
                            if (away.lengthSquared() > 0) p.setVelocity(away.normalize().multiply(0.6).setY(0.6));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 1));
                        }
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
        return "voidrift";
    }
}
