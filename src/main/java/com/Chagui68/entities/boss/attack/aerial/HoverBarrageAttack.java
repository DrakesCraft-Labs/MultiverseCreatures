package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.BossHost;
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

public class HoverBarrageAttack extends BossAttackBase {
    public HoverBarrageAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        if (instance.hoverBarrageActive) return;
        instance.hoverBarrageActive = true;

        if (instance.hoverBarrageTask != null) {
            instance.hoverBarrageTask.cancel();
            instance.hoverBarrageTask = null;
        }

        BossPuppet stand = instance.stand;
        Location startLoc = stand.getLocation();
        boolean fromAir = instance.isFlying;
        double targetY = startLoc.getY() + (fromAir ? 0 : 15);

        instance.hoverBarrageTask = new BukkitRunnable() {
            final List<ArmorStandBoss.XMark> xMarks = new ArrayList<>();
            int xMarksFired = 0;
            int tick = 0;
            boolean rising = !fromAir;
            boolean descending = false;
            final List<Player> targets = new ArrayList<>();
            int targetIndex = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    instance.hoverBarrageActive = false;
                    cancel();
                    return;
                }

                if (rising) {
                    tick++;
                    Location loc = stand.getLocation();
                    double newY = loc.getY() + 0.5;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
                    if (newY >= targetY) {
                        newY = targetY;
                        rising = false;
                        tick = 0;
                        targets.clear();
                        targets.addAll(boss.getValidPlayersNear(stand.getLocation(), 10000));
                        if (targets.isEmpty()) {
                            instance.hoverBarrageActive = false;
                            cancel();
                            return;
                        }
                        stand.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
                    }
                    loc.setY(newY);
                    stand.teleport(loc);
                    stand.getWorld().spawnParticle(Particle.CLOUD, loc, 3, 0.5, 0.1, 0.5, 0.02);
                    return;
                }

                if (!descending) {
                    tick++;
                    if (tick % 40 == 0 && xMarksFired < targets.size()) {
                        Player t = targets.get(targetIndex % targets.size());
                        targetIndex++;
                        Location xOrigin = stand.getLocation().add(0, 8, 0);
                        Vector fwd = stand.getLocation().getDirection();
                        if (fwd.lengthSquared() > 0.01) {
                            xOrigin.add(fwd.clone().multiply(3));
                        }
                        xMarks.add(((ArmorStandBoss) boss).new XMark(xOrigin, t));
                        xMarksFired++;
                    }

                    Iterator<ArmorStandBoss.XMark> it = xMarks.iterator();
                    ArmorStandBoss.XMark tracingX = null;
                    while (it.hasNext()) {
                        ArmorStandBoss.XMark x = it.next();
                        if (x.isDone()) {
                            it.remove();
                        } else {
                            if (tracingX == null && x.isTracing()) tracingX = x;
                            x.update();
                        }
                    }

                    if (tracingX != null) {
                        Vector lp = tracingX.getCurrentLocalPoint();
                        if (lp != null) {
                            double pitch = -lp.getY() * 15 + 10;
                            double yaw = lp.getX() * 15;
                            stand.setRightArmPose(new EulerAngle(Math.toRadians(pitch), Math.toRadians(yaw), 0));
                            stand.setLeftArmPose(new EulerAngle(Math.toRadians(pitch), Math.toRadians(-yaw), 0));
                            stand.setBodyPose(new EulerAngle(Math.toRadians(8), 0, 0));
                        }
                    } else {
                        stand.setRightArmPose(new EulerAngle(0, 0, 0));
                        stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                    }

                    if (xMarksFired >= targets.size() && xMarks.isEmpty()) {
                        if (fromAir) {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            instance.hoverBarrageActive = false;
                            cancel();
                        } else {
                            descending = true;
                            tick = 0;
                        }
                    }
                } else {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(20), Math.toRadians(10), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(20), Math.toRadians(-10), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-5), 0, 0));
                    tick++;
                    if (tick % 2 == 0) {
                        Location loc = stand.getLocation();
                        double newY = loc.getY() - 0.5;
                        if (newY <= startLoc.getY()) {
                            stand.teleport(startLoc);
                            stand.getWorld().spawnParticle(Particle.CLOUD, startLoc, 20, 1, 0.5, 1, 0.1);
                            stand.getWorld().playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f);
                            boss.resetBossPose(instance);
                            instance.hoverBarrageActive = false;
                            cancel();
                        } else {
                            loc.setY(newY);
                            stand.teleport(loc);
                            stand.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.3, 0.1, 0.3, 0.02);
                        }
                    }
                }
            }
        };
        instance.hoverBarrageTask.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "hoverbarrage";
    }
}
