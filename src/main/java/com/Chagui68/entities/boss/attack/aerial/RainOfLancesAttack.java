package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RainOfLancesAttack extends BossAttackBase {
    public RainOfLancesAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        execute(instance, true);
    }

    public void execute(BossInstance instance, boolean telegraph) {
        if (!instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();

        List<Player> targets = boss.getValidPlayersNear(stand.getLocation(), 10000);
        if (targets.isEmpty()) return;

        List<Location> spawnPoints = new ArrayList<>();
        for (Player target : targets) {
            int count = 2 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                double spread = 3.0;
                double ox = (random.nextDouble() - 0.5) * spread * 2;
                double oz = (random.nextDouble() - 0.5) * spread * 2;
                Location loc = target.getLocation().add(ox, 0, oz);
                loc.setY(stand.getLocation().getY() + 20 + random.nextDouble() * 5);
                spawnPoints.add(loc);
            }
        }

        new BukkitRunnable() {
            int tick = 0;
            boolean windup = telegraph;
            List<org.bukkit.entity.Item> lances = new ArrayList<>();
            final double FALL_SPEED = 0.6;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    for (org.bukkit.entity.Item ls : lances) {
                        if (ls.isValid()) ls.remove();
                    }
                    cancel();
                    return;
                }

                if (windup) {
                    tick++;
                    double phase = Math.min(1.0, (double) tick / 25);

                    stand.setRightArmPose(new EulerAngle(
                            Math.toRadians(-180 + 90 * phase),
                            Math.toRadians(10 * phase),
                            Math.toRadians(20 * phase)
                    ));
                    stand.setLeftArmPose(new EulerAngle(
                            Math.toRadians(-180 + 90 * phase),
                            Math.toRadians(-10 * phase),
                            Math.toRadians(-20 * phase)
                    ));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(15 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));

                    if (tick == 1) {
                        world.playSound(stand.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
                    }

                    for (Location sp : spawnPoints) {
                        world.spawnParticle(Particle.END_ROD, sp, (int) (1 + phase * 2), 0.3, 0.3, 0.3, 0.01);
                        if (tick % 5 == 0) {
                            for (int a = 0; a < (int) (4 * phase); a++) {
                                double angle = (2 * Math.PI * a / 4);
                                double r = 0.5 + phase * 1.0;
                                double x = sp.getX() + Math.cos(angle) * r;
                                double z = sp.getZ() + Math.sin(angle) * r;
                                Location pl = new Location(world, x, sp.getY(), z);
                                world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0xFFAA00), 1.2f * (float) phase));
                            }
                        }
                    }

                    if (tick >= 30) {
                        windup = false;
                        tick = 0;
                        boss.resetBossPose(instance);
                        world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.8f);

                        ItemStack lance = ((ArmorStandBoss) boss).createNetheriteLance();
                        for (Location sp : spawnPoints) {
                            org.bukkit.entity.Item lanceItem = world.dropItem(sp, lance.clone());
                            if (lanceItem == null) continue;
                            lanceItem.setPickupDelay(Integer.MAX_VALUE);
                            lanceItem.setGravity(true);
                            lanceItem.setInvulnerable(true);
                            lanceItem.setSilent(true);
                            lanceItem.setTicksLived(1);
                            lances.add(lanceItem);

                            world.spawnParticle(Particle.EXPLOSION, sp, 2, 0.5, 0.5, 0.5, 0);
                            world.playSound(sp, Sound.ENTITY_ARROW_SHOOT, 0.8f, 0.5f);
                        }
                    }
                    return;
                }

                tick++;
                if (tick <= 15) {
                    for (org.bukkit.entity.Item ls : lances) {
                        if (!ls.isValid()) continue;
                        world.spawnParticle(Particle.END_ROD, ls.getLocation(), 1, 0.2, 0.2, 0.2, 0);
                    }
                    return;
                }

                Iterator<org.bukkit.entity.Item> it = lances.iterator();
                while (it.hasNext()) {
                    org.bukkit.entity.Item ls = it.next();
                    if (!ls.isValid()) {
                        it.remove();
                        continue;
                    }

                    Location loc = ls.getLocation();
                    double newY = loc.getY() - FALL_SPEED;
                    loc.setY(newY);
                    ls.teleport(loc);

                    world.spawnParticle(Particle.CRIT, loc, 2, 0.1, 0.3, 0.1, 0.02);
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0.1, 0.1, 0.1, 0);

                    boolean hitGround = loc.clone().subtract(0, 0.3, 0).getBlock().getType().isSolid();
                    boolean nearPlayer = false;
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(loc) < 9) {
                            nearPlayer = true;
                            break;
                        }
                    }

                    if (hitGround || nearPlayer || newY < -10) {
                        world.spawnParticle(Particle.EXPLOSION, loc, 3, 0.5, 0.5, 0.5, 0);
                        world.spawnParticle(Particle.FLAME, loc, 8, 0.3, 0.3, 0.3, 0.03);
                        world.spawnParticle(Particle.CLOUD, loc, 10, 1.0, 0.3, 1.0, 0.05);
                        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);

                        double damage = sealDamage * 0.6;
                        for (Player p : boss.getValidPlayers(world)) {
                            if (p.getLocation().distanceSquared(loc) < 16) {
                                MscEntityUtils.damageBy(stand, p, damage);
                                boss.launchPlayer(p, 0.3);
                            }
                        }

                        ls.remove();
                        it.remove();
                    }
                }

                if (lances.isEmpty()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "rainoflances";
    }
}
