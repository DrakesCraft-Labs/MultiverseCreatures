package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class ReflectBarrierAttack extends BossAttackBase {

    public ReflectBarrierAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.activeDefense != instance.activeDefense.NONE || !boss.isOnGround(instance.stand)) return;
        instance.activeDefense = instance.activeDefense.REFLECT_BARRIER;
        instance.defenseTimer = 0;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 5, 0), 1,
                Color.WHITE);
        world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 5, 0), 50, 3, 5, 3, 0.05);

        stand.setBodyPose(new EulerAngle(0, 0, 0));
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45), Math.toRadians(0)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-45), Math.toRadians(0)));
        stand.setHeadPose(new EulerAngle(Math.toRadians(-10), 0, 0));

        instance.defenseTask = new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || instance.activeDefense != instance.activeDefense.REFLECT_BARRIER) {
                    cancel();
                    return;
                }
                if (t % 8 == 0) {
                    world.spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 5, 0), 15, 2.5, 4, 2.5, 0.03);
                    world.spawnParticle(Particle.DUST, stand.getLocation().add(0, 5, 0), 10, 2, 4, 2, 0,
                            new org.bukkit.Particle.DustOptions(Color.fromRGB(0x88CCFF), 2.5f));
                }
                t++;
            }
        };
        instance.defenseTask.runTaskTimer(plugin, 0L, 2L);
    }

    @Override
    public String getName() {
        return "reflectbarrier";
    }
}