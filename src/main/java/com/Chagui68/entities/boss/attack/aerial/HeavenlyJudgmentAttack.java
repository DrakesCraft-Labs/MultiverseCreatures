package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossPuppet;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class HeavenlyJudgmentAttack extends BossAttackBase {
    public HeavenlyJudgmentAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double groundY = boss.getGroundY(center, 40);
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnDivineSeal(new Location(world, center.getX(), groundY + 0.5, center.getZ()), 80);
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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(45 * phase), Math.toRadians(30 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-45 * phase), Math.toRadians(-30 * phase)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-45 * phase), 0, 0));
                    double r = 2.0 + phase * 8.0;
                    for (int a = 0; a < 30; a++) {
                        double angle = (2 * Math.PI * a / 30) + t * 0.02;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = center.getY() + Math.sin(angle * 3) * 2.0 * phase;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFFDD00), 2.5f * (float) phase));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                    for (int i = 0; i < 5; i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double rr = random.nextDouble() * 4 * phase;
                        Location pl = new Location(world, center.getX() + Math.cos(angle) * rr, center.getY() + 8 + random.nextDouble() * 5, center.getZ() + Math.sin(angle) * rr);
                        world.spawnParticle(Particle.FLAME, pl, 3, 0.3, 0.3, 0.3, 0.02);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5f, 0.3f);
                    if (t % 8 == 0) world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 0.4f);
                } else if (t == 40) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(30)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-30)));
                    world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
                    world.spawnParticle(Particle.EXPLOSION, center, 50, 8, 5, 8, 0);
                    world.spawnParticle(Particle.FLASH, center, 1,
                            Color.WHITE);
                } else if (t < 60) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(30)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-30)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, 0));
                    for (Player p : boss.getValidPlayers(world)) {
                        Location pLoc = p.getLocation();
                        for (int y = 0; y < 25; y++) {
                            Location beam = new Location(world, pLoc.getX(), pLoc.getY() + 24 - y, pLoc.getZ());
                            world.spawnParticle(Particle.DUST, beam, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0xFFDD00), 2.0f));
                            world.spawnParticle(Particle.FLAME, beam, 1, 0, 0, 0, 0);
                            if (y % 5 == 0) world.spawnParticle(Particle.SONIC_BOOM, beam, 1);
                        }
                        double dmg = sealDamage * 2.0;
                        MscEntityUtils.damageBy(stand.entidad(), p, dmg);
                        p.setVelocity(new Vector(0, -0.5, 0));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 2));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));
                    }
                } else if (t < 70) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(30)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-30)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, 0));
                    for (Player p : boss.getValidPlayers(world)) {
                        Location pLoc = p.getLocation();
                        world.spawnParticle(Particle.CLOUD, pLoc, 10, 1, 0.5, 1, 0.05);
                        world.spawnParticle(Particle.END_ROD, pLoc.clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.02);
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
        return "heavenlyjudgment";
    }
}
