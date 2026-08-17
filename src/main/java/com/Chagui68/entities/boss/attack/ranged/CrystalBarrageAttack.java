package com.Chagui68.entities.boss.attack.ranged;

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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CrystalBarrageAttack extends BossAttackBase {
    public CrystalBarrageAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        new BukkitRunnable() {
            int t = 0;
            int shots = 0;
            final List<Location> crystals = new ArrayList<>();
            final List<Vector> directions = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 90) {
                    cancel();
                    return;
                }
                if (t < 15) {
                    double phase = (double) t / 15;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-100 * phase), Math.toRadians(20), 0));
                    if (t == 1) world.playSound(center, Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
                } else if (shots < 3) {
                    if (t % 10 == 0) {
                        shots++;
                        Vector dir = target.getLocation().toVector().subtract(center.toVector()).normalize();
                        crystals.add(center.clone().add(0, 1.5, 0));
                        directions.add(dir);
                        world.playSound(center, Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.8f);
                    }
                }
                Iterator<Location> it = crystals.iterator();
                Iterator<Vector> itd = directions.iterator();
                while (it.hasNext()) {
                    Location p = it.next();
                    Vector d = itd.next();
                    p.add(d.clone().multiply(1.4));
                    world.spawnParticle(Particle.END_ROD, p, 3, 0.05, 0.05, 0.05, 0);
                    world.spawnParticle(Particle.DUST, p, 2, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xAA66FF), 1.5f));
                    for (Player pl : boss.getValidPlayers(world)) {
                        if (pl.getLocation().distanceSquared(p) < 9) {
                            MscEntityUtils.damageBy(stand, pl, sealDamage * 0.55);
                            pl.setVelocity(d.clone().setY(0.4).multiply(0.4));
                            world.spawnParticle(Particle.EXPLOSION, p, 8, 0.5, 0.5, 0.5, 0);
                            world.playSound(p, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.7f);
                            it.remove();
                            itd.remove();
                            break;
                        }
                    }
                    if (p.distanceSquared(center) > 1600) {
                        it.remove();
                        itd.remove();
                    }
                }
                if (shots >= 3 && crystals.isEmpty()) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "crystalbarrage";
    }
}
