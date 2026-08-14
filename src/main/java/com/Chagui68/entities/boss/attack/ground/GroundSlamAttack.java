package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class GroundSlamAttack extends BossAttackBase {

    private static final double SHIELD_RADIUS = 6.0;

    public GroundSlamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        if (instance.groundSlamTask != null) return;
        ArmorStand stand = instance.stand;
        if (instance.shieldState != BossInstance.ShieldState.NORMAL) return;

        World world = stand.getWorld();
        instance.shieldState = BossInstance.ShieldState.NORMAL;
        instance.shieldCooldown = 0;
        instance.shieldTimer = 0;

        BukkitRunnable task = new BukkitRunnable() {
            int phase = 0;
            int tick = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    instance.groundSlamTask = null;
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                switch (phase) {
                    case 0 -> {
                        if (!boss.isOnGround(stand)) {
                            instance.groundSlamTask = null;
                            cancel();
                            return;
                        }
                        tick++;
                        if (tick >= ((ArmorStandBoss) boss).getShieldPlantInterval()) {
                            plantShield(instance, center, world);
                            phase = 1;
                            tick = 0;
                        }
                    }
                    case 1 -> {
                        if (!boss.isOnGround(stand)) {
                            retrieveShield(instance, center, world);
                            instance.groundSlamTask = null;
                            cancel();
                            return;
                        }
                        tick++;
                        int plantDelay = instance.shieldSealActive ? 20 : 60;
                        if (tick >= plantDelay) {
                            doSlam(instance, center, world);
                            phase = 2;
                            tick = 0;
                        }
                    }
                    case 2 -> {
                        tick++;
                        if (tick >= ((ArmorStandBoss) boss).getShieldRetrieveDelay(instance.currentPhase)) {
                            retrieveShield(instance, center, world);
                            instance.groundSlamTask = null;
                            cancel();
                        }
                    }
                }
            }
        };
        instance.groundSlamTask = task;
        task.runTaskTimer(plugin, 0L, 1L);
    }

    private void plantShield(BossInstance instance, Location center, World world) {
        ArmorStand stand = instance.stand;
        instance.shieldState = BossInstance.ShieldState.PLANTED;
        instance.shieldTimer = 0;

        EntityEquipment equip = stand.getEquipment();
        ItemStack shieldItem = equip.getItemInOffHand();
        if (shieldItem == null || shieldItem.getType() == Material.AIR) return;

        equip.setItemInOffHand(null);

        if (instance.shieldSealActive) {
            world.playSound(stand.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.7f);
            world.spawnParticle(Particle.SMOKE, stand.getLocation(), 10, 1.0, 0.5, 1.0, 0.05);
            return;
        }

        float yaw = stand.getLocation().getYaw();
        double yawRadians = Math.toRadians(yaw);
        double leftX = Math.cos(yawRadians) * SHIELD_RADIUS;
        double leftZ = Math.sin(yawRadians) * SHIELD_RADIUS;
        Location plantLoc = stand.getLocation().add(leftX, 10.0, leftZ);

        ItemDisplay holder;
        try {
            holder = (ItemDisplay) plantLoc.getWorld().spawnEntity(plantLoc, EntityType.ITEM_DISPLAY);
        } catch (Throwable t) {
            holder = null;
        }
        if (holder == null) {
            equipShield(stand);
            return;
        }
        org.bukkit.util.Transformation transformation = holder.getTransformation();
        transformation.getScale().set(7.5f);
        holder.setTransformation(transformation);
        holder.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);
        holder.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED);
        holder.setViewRange(64.0f);
        holder.setItemStack(shieldItem);
        holder.setGravity(false);
        holder.setInvulnerable(true);
        holder.setPersistent(true);
        holder.setCustomName("MSC_ShieldHolder");
        holder.setCustomNameVisible(false);
        holder.addScoreboardTag("MSC_ShieldHolder");

        instance.shieldHolder = holder;

        if (plugin.getMagicSealListener() != null) {
            instance.floatingShieldTask = plugin.getMagicSealListener()
                    .spawnFloatingShieldSealTask(holder.getLocation(), stand.getLocation().getY(), 60 + 80);
        }

        plantLoc.getWorld().playSound(plantLoc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.7f);
        plantLoc.getWorld().spawnParticle(Particle.EXPLOSION, plantLoc, 1, 0, 0, 0, 0);
        plantLoc.getWorld().spawnParticle(Particle.SMOKE, plantLoc, 15, 0.5, 0.3, 0.5, 0.05);
    }

    private void doSlam(BossInstance instance, Location center, World world) {
        ArmorStand stand = instance.stand;

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.3f);

        for (int y = 20; y >= -3; y -= 1) {
            Location pl = center.clone().add(0, y, 0);
            world.spawnParticle(Particle.EXPLOSION, pl, 1, 0.2, 0.2, 0.2, 0);
            world.spawnParticle(Particle.FLAME, pl, 2, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticle(Particle.CRIT, pl, 1, 0.3, 0.3, 0.3, 0.05);
        }

        for (int i = 0; i < 3; i++) {
            double r = 2.0 + i * 2.0;
            for (int a = 0; a < 30; a++) {
                double angle = (2 * Math.PI * a) / 30;
                double x = center.getX() + Math.cos(angle) * r;
                double z = center.getZ() + Math.sin(angle) * r;
                Location loc = new Location(world, x, center.getY(), z);
                world.spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CRIT, loc.clone().add(0, 0.5, 0), 2, 0.2, 0.2, 0.2, 0.05);
            }
        }

        world.spawnParticle(Particle.CLOUD, center.clone().add(0, 0.5, 0), 40, 3.0, 0.2, 3.0, 0.1);

        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(30), Math.toRadians(0)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-15), Math.toRadians(0)));
        stand.setHeadPose(new EulerAngle(Math.toRadians(7), 0, 0));
        stand.setBodyPose(new EulerAngle(Math.toRadians(5), 0, 0));
        stand.setRightLegPose(new EulerAngle(Math.toRadians(15), 0, 0));
        stand.setLeftLegPose(new EulerAngle(Math.toRadians(-2), 0, 0));

        ((ArmorStandBoss) boss).skyPentagramAttack(instance);

        instance.shieldState = BossInstance.ShieldState.SLAM_DONE;
        instance.shieldTimer = 0;

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                boss.resetBossPose(instance);
            }
        }.runTaskLater(plugin, 50L);
    }

    private void retrieveShield(BossInstance instance, Location center, World world) {
        ArmorStand stand = instance.stand;

        if (instance.floatingShieldTask != null) {
            instance.floatingShieldTask.cancel();
            instance.floatingShieldTask = null;
        }
        if (instance.shieldHolder != null && instance.shieldHolder.isValid()) {
            Location holderLoc = instance.shieldHolder.getLocation();

            instance.shieldHolder.getWorld().spawnParticle(
                    Particle.END_ROD, holderLoc, 20, 0.5, 0.5, 0.5, 0.05
            );
            instance.shieldHolder.getWorld().spawnParticle(
                    Particle.FLAME, holderLoc, 15, 0.3, 0.3, 0.3, 0.03
            );
            instance.shieldHolder.getWorld().playSound(
                    holderLoc, Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.2f
            );

            instance.shieldHolder.remove();
        }
        instance.shieldHolder = null;
        instance.shieldState = BossInstance.ShieldState.NORMAL;

        equipShield(stand);
        boss.resetBossPose(instance);

        world.spawnParticle(
                Particle.END_ROD, center.add(0, 1, 0), 25, 1.0, 1.0, 1.0, 0.1
        );
        world.playSound(
                center, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.5f
        );
    }

    private void equipShield(ArmorStand stand) {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta shieldMeta = shield.getItemMeta();
        if (shieldMeta != null) {
            shieldMeta.setUnbreakable(true);
            shield.setItemMeta(shieldMeta);
        }
        EntityEquipment equip = stand.getEquipment();
        if (equip.getItemInOffHand().getType() == Material.AIR) {
            equip.setItemInOffHand(shield);
        }
    }

    @Override
    public String getName() {
        return "groundslam";
    }
}