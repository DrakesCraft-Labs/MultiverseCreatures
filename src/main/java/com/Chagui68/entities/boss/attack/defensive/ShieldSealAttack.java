package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.MagicSealListener;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShieldSealAttack extends BossAttackBase {

    private static final double SHIELD_RADIUS = 5.5;

    public ShieldSealAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        startShieldSeal(instance, true);
    }

    public void startShieldSeal(BossInstance instance, boolean telegraph) {
        ArmorStand stand = instance.stand;
        if (instance.shieldSealActive || instance.isFlying) return;

        World world = stand.getLocation().getWorld();

        if (telegraph) {
            world.playSound(stand.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.8f);
        }

        EntityEquipment preEquip = stand.getEquipment();
        if (preEquip != null && preEquip.getItemInOffHand() != null && preEquip.getItemInOffHand().getType() == Material.SHIELD) {
            instance.shieldSealSavedShield = preEquip.getItemInOffHand().clone();
            preEquip.setItemInOffHand(null);
        } else {
            instance.shieldSealSavedShield = null;
        }

        instance.shieldSealActive = true;
        instance.shieldSealTimer = 0;

        instance.shieldSealTask = new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;
            boolean casting = telegraph;
            final double SHIELD_RADIUS = 5.5;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    removeShieldSeal(instance);
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (casting) {
                    t++;

                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-110), Math.toRadians(45), Math.toRadians(10)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-110), Math.toRadians(-45), Math.toRadians(-10)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(15), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(15), 0, 0));

                    double phase = Math.min(1.0, (double) t / 20);
                    double r = SHIELD_RADIUS * phase;
                    for (int phi = 0; phi < 8; phi++) {
                        double theta = (Math.PI * phi / 7);
                        for (int a = 0; a < 12; a++) {
                            double angle = (2 * Math.PI * a / 12) + t * 0.03;
                            double x = center.getX() + r * Math.sin(theta) * Math.cos(angle);
                            double y = center.getY() + 6 + r * Math.cos(theta);
                            double z = center.getZ() + r * Math.sin(theta) * Math.sin(angle);
                            Location pl = new Location(world, x, y, z);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new org.bukkit.Particle.DustOptions(Color.fromRGB(0x88CCFF), 1.8f * (float) phase));
                            world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                        }
                    }

                    for (int a = 0; a < 6; a++) {
                        double angle = (2 * Math.PI * a / 6) + t * 0.05;
                        double x = center.getX() + Math.cos(angle) * r * 0.8;
                        double z = center.getZ() + Math.sin(angle) * r * 0.8;
                        world.spawnParticle(Particle.DUST, new Location(world, x, center.getY() + 6, z), 1, 0, 0, 0, 0,
                                new org.bukkit.Particle.DustOptions(Color.WHITE, 1.5f));
                    }

                    if (t >= 25) {
                        casting = false;
                        t = 0;
                        world.playSound(center, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.8f);
                        world.spawnParticle(Particle.EXPLOSION, center.clone().add(0, 6, 0), 5, 2.0, 1.0, 2.0, 0);

                        spawnShieldDisplays(instance, world, center);
                    }
                    return;
                }

                if (!instance.shieldSealActive) {
                    removeShieldSeal(instance);
                    cancel();
                    return;
                }

                stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45), Math.toRadians(10)));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-45), Math.toRadians(-10)));
                stand.setBodyPose(new EulerAngle(Math.toRadians(8), 0, 0));
                stand.setHeadPose(new EulerAngle(Math.toRadians(8), 0, 0));

                double radius = SHIELD_RADIUS;
                for (int phi = 0; phi < 6; phi++) {
                    double theta = (Math.PI * phi / 5);
                    for (int a = 0; a < 8; a++) {
                        double angle = (2 * Math.PI * a / 8) + t * 0.02;
                        double x = center.getX() + radius * Math.sin(theta) * Math.cos(angle);
                        double y = center.getY() + 6 + radius * Math.cos(theta);
                        double z = center.getZ() + radius * Math.sin(theta) * Math.sin(angle);
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new org.bukkit.Particle.DustOptions(Color.fromRGB(0x88CCFF), 1.5f));
                        if (t % 3 == 0) {
                            world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                        }
                    }
                }

                for (int a = 0; a < 4; a++) {
                    double angle = (2 * Math.PI * a / 4) + t * 0.03;
                    double x = center.getX() + Math.cos(angle) * radius * 0.9;
                    double z = center.getZ() + Math.sin(angle) * radius * 0.9;
                    world.spawnParticle(Particle.WITCH, new Location(world, x, center.getY() + 6, z), 2, 0.3, 0.3, 0.3, 0);
                }

                updateShieldDisplays(instance, center, t);

                t++;
                if (t >= 200) {
                    removeShieldSeal(instance);
                    cancel();
                }
            }
        };
        instance.shieldSealTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnShieldDisplays(BossInstance instance, World world, Location center) {
        for (ItemDisplay d : instance.shieldSealDisplays) {
            if (d.isValid()) d.remove();
        }
        instance.shieldSealDisplays.clear();

        double[] angles = {0.0, 60.0, 120.0, 180.0, 240.0, 300.0, 30.0, 90.0, 150.0, 210.0, 270.0, 330.0};
        double baseHeight = 7.0;

        for (int i = 0; i < angles.length; i++) {
            double angleDeg = angles[i];
            double hOffset;
            if (i < 6) {
                hOffset = baseHeight;
            } else {
                double altIndex = i - 6;
                hOffset = (altIndex % 2 == 0) ? 11.0 : 3.0;
            }

            Location shieldLoc = center.clone().add(0, hOffset, 0);
            ItemDisplay holder;
            try {
                holder = (ItemDisplay) world.spawnEntity(shieldLoc, EntityType.ITEM_DISPLAY);
            } catch (Throwable t) {
                holder = null;
            }
            if (holder == null) continue;

            ItemStack shieldItem = new ItemStack(Material.SHIELD);
            ItemMeta meta = shieldItem.getItemMeta();
            if (meta != null) {
                meta.setUnbreakable(true);
                shieldItem.setItemMeta(meta);
            }

            try {
                org.bukkit.util.Transformation transformation = holder.getTransformation();
                transformation.getScale().set(3.5f, 3.5f, 3.5f);
                holder.setTransformation(transformation);
            } catch (Throwable ignored) {
            }

            holder.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);
            holder.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            holder.setViewRange(64.0f);
            holder.setItemStack(shieldItem);
            holder.setGravity(false);
            holder.setInvulnerable(true);
            holder.setPersistent(true);
            holder.addScoreboardTag("MSC_ShieldSealOrbit");
            instance.shieldSealDisplays.add(holder);
        }
    }

    private void updateShieldDisplays(BossInstance instance, Location center, int tick) {
        if (instance.shieldSealDisplays.isEmpty()) return;

        float yaw = center.getYaw();
        double baseR = 4.5;
        double rotSpeed = 0.04;

        double[] angles = {0.0, 60.0, 120.0, 180.0, 240.0, 300.0, 30.0, 90.0, 150.0, 210.0, 270.0, 330.0};

        for (int i = 0; i < instance.shieldSealDisplays.size(); i++) {
            ItemDisplay d = instance.shieldSealDisplays.get(i);
            if (!d.isValid()) continue;

            double angleDeg = angles[i];
            double hOffset;
            if (i < 6) {
                hOffset = 7.0;
            } else {
                double altIndex = i - 6;
                hOffset = (altIndex % 2 == 0) ? 11.0 : 3.0;
            }

            double rotAngle = Math.toRadians(yaw + angleDeg + tick * rotSpeed * 360);
            double x = center.getX() + baseR * Math.cos(rotAngle);
            double z = center.getZ() + baseR * Math.sin(rotAngle);
            Location target = new Location(center.getWorld(), x, center.getY() + hOffset, z);

            float displayYaw = (float) Math.toDegrees(rotAngle) - 90.0f;
            target.setYaw(displayYaw);
            target.setPitch(0);
            d.teleport(target);
        }
    }

    private void removeShieldSeal(BossInstance instance) {
        instance.shieldSealActive = false;
        instance.shieldSealTimer = 0;
        if (instance.shieldSealTask != null) {
            instance.shieldSealTask.cancel();
            instance.shieldSealTask = null;
        }
        for (ItemDisplay d : instance.shieldSealDisplays) {
            if (d.isValid()) d.remove();
        }
        instance.shieldSealDisplays.clear();

        if (instance.stand != null && instance.stand.isValid()) {
            EntityEquipment equip = instance.stand.getEquipment();
            if (equip != null && instance.shieldSealSavedShield != null
                    && equip.getItemInOffHand().getType() == org.bukkit.Material.AIR) {
                equip.setItemInOffHand(instance.shieldSealSavedShield);
            }
            instance.shieldSealSavedShield = null;
            boss.resetBossPose(instance);

            instance.stand.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, instance.stand.getLocation().add(0, 6, 0), 8, 2.0, 1.0, 2.0, 0);
            instance.stand.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, instance.stand.getLocation().add(0, 6, 0), 20, 2.5, 1.0, 2.5, 0.1);
        }
    }

    @Override
    public String getName() {
        return "shieldseal";
    }
}