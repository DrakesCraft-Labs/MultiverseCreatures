package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class EarthPillarAttack extends BossAttackBase {
    public EarthPillarAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(60 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-60 * phase), 0));
                    world.spawnParticle(Particle.END_ROD, center, 2, 1, 0.2, 1, 0.01);
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
                    if (t % 5 == 0) {
                        for (int a = 0; a < 8; a++) {
                            double angle = (2 * Math.PI * a / 8);
                            double x = center.getX() + Math.cos(angle) * 4;
                            double z = center.getZ() + Math.sin(angle) * 4;
                            world.spawnParticle(Particle.DUST, new Location(world, x, center.getY(), z), 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0x8B4513), 1.5f * (float) phase));
                        }
                    }
                } else if (t < 65) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(60), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-60), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(10), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-5), 0, 0));
                    if (t % 8 == 0) {
                        Player target = boss.detectTarget(stand);
                        if (target != null) {
                            Location tLoc = target.getLocation();
                            for (int h = 0; h < 8; h++) {
                                Location pl = tLoc.clone().add(0, h * 0.5, 0);
                                world.spawnParticle(Particle.BLOCK, pl, 5, 0.3, 0.1, 0.3, 0.1, Material.STONE.createBlockData());
                                world.spawnParticle(Particle.DUST, pl, 3, 0.2, 0.1, 0.2, 0, new Particle.DustOptions(Color.fromRGB(0x8B4513), 2.0f));
                            }
                            world.playSound(tLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.7f);
                            double dmg = sealDamage * 0.8;
                            MscEntityUtils.damageBy(stand.entidad(), target, dmg);
                            boss.launchPlayer(target, 1.2);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                        }
                    }
                } else if (t >= 65) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "earthpillar";
    }
}
