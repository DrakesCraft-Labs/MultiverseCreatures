package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class StoneSkinAttack extends BossAttackBase {

    public StoneSkinAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.activeDefense != instance.activeDefense.NONE || !boss.isOnGround(instance.stand)) return;
        instance.activeDefense = instance.activeDefense.STONE_SKIN;
        instance.defenseTimer = 0;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.BLOCK_STONE_BREAK, 1.5f, 0.8f);
        world.playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.6f);
        world.spawnParticle(Particle.CRIT, loc.clone().add(0, 5, 0), 40, 2, 5, 2, 0.05);
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = 1 + random.nextDouble() * 3;
            Location pl = new Location(world, loc.getX() + Math.cos(angle) * r, loc.getY() + random.nextDouble() * 10, loc.getZ() + Math.sin(angle) * r);
            world.spawnParticle(Particle.BLOCK, pl, 3, 0.2, 0.2, 0.2, 0.05, Material.STONE.createBlockData());
        }

        stand.setBodyPose(new EulerAngle(0, 0, 0));
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(45), Math.toRadians(10)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(-45), Math.toRadians(-10)));
        stand.setHeadPose(new EulerAngle(Math.toRadians(5), 0, 0));

        instance.defenseTask = new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || instance.activeDefense != instance.activeDefense.STONE_SKIN) {
                    cancel();
                    return;
                }
                if (t % 10 == 0) {
                    world.spawnParticle(Particle.CRIT, stand.getLocation().add(0, 5, 0), 10, 1.5, 4, 1.5, 0.02);
                    world.spawnParticle(Particle.BLOCK, stand.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.02, Material.STONE.createBlockData());
                }
                t++;
            }
        };
        instance.defenseTask.runTaskTimer(plugin, 0L, 2L);
    }

    @Override
    public String getName() {
        return "stoneskin";
    }
}