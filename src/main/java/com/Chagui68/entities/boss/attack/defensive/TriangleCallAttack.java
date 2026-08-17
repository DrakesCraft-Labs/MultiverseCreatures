package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.MultiverseCreatures;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Random;

public class TriangleCallAttack extends BossAttackBase {

    public TriangleCallAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.triangleCallActive) return;
        instance.triangleCallActive = true;

        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        boolean airMode = !boss.isOnGround(stand);
        int playerCount = boss.countPlayersInRange(center, 100);
        int extraSets = playerCount / 3;

        instance.triangleCallTask = new org.bukkit.scheduler.BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || tick >= 120) {
                    instance.triangleCallActive = false;
                    cancel();
                    return;
                }
                if (tick == 8) {
                    if (airMode) {
                        spawnAirCall(stand, world, center, extraSets);
                    } else {
                        spawnGroundCall(stand, world, center, extraSets);
                    }
                }
                tick++;
            }
        };
        instance.triangleCallTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnAirCall(BossPuppet stand, World world, Location center, int extraSets) {
        Vector dir = stand.getLocation().getDirection();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        if (right.lengthSquared() < 0.01) right = new Vector(1, 0, 0);

        int count = 1 + extraSets;

        for (int side = -1; side <= 1; side += 2) {
            Location sealLoc = center.clone().add(right.clone().multiply(side * 8));
            spawnTriangleSeal(world, sealLoc, 100, MagicSealListener.Plane.YZ);

            for (int i = 0; i < count; i++) {
                Location spawnLoc = sealLoc.clone().add(0, 2 + i * 4, 0);

                Ghast ghast = (Ghast) world.spawnEntity(spawnLoc, EntityType.GHAST);
                if (ghast != null) {
                    ghast.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + "Infernal Ghast");
                    ghast.setCustomNameVisible(true);
                    ghast.setPersistent(true);
                    ghast.setRemoveWhenFarAway(false);
                    ghast.addScoreboardTag("MSC_ArmorBossSummoned");
                    org.bukkit.attribute.AttributeInstance ghastHealth = ghast.getAttribute(Attribute.MAX_HEALTH);
                    if (ghastHealth != null) ghastHealth.setBaseValue(20.0);
                    ghast.setHealth(20.0);
                    Player near = boss.findNearestPlayer(spawnLoc, 100);
                    if (near != null) ghast.setTarget(near);
                }

                Location phantomLoc = sealLoc.clone().add(0, 6 + i * 4, 0);
                Phantom phantom = (Phantom) world.spawnEntity(phantomLoc, EntityType.PHANTOM);
                if (phantom != null) {
                    phantom.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Night Stalker");
                    phantom.setCustomNameVisible(true);
                    phantom.setPersistent(true);
                    phantom.setRemoveWhenFarAway(false);
                    phantom.addScoreboardTag("MSC_ArmorBossSummoned");
                    org.bukkit.attribute.AttributeInstance phantomHealth = phantom.getAttribute(Attribute.MAX_HEALTH);
                    if (phantomHealth != null) phantomHealth.setBaseValue(30.0);
                    phantom.setHealth(30.0);
                    Player near = boss.findNearestPlayer(phantomLoc, 100);
                    if (near != null) phantom.setTarget(near);

                    WitherSkeleton sniper = (WitherSkeleton) world.spawnEntity(phantomLoc, EntityType.WITHER_SKELETON);
                    if (sniper != null) {
                        sniper.setCustomName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Sniper Skeleton");
                        sniper.setCustomNameVisible(true);
                        sniper.setPersistent(true);
                        sniper.setRemoveWhenFarAway(false);
                        sniper.addScoreboardTag("MSC_ArmorBossSummoned");
                        org.bukkit.attribute.AttributeInstance sniperHealth = sniper.getAttribute(Attribute.MAX_HEALTH);
                        if (sniperHealth != null) sniperHealth.setBaseValue(40.0);
                        sniper.setHealth(40.0);
                        org.bukkit.attribute.AttributeInstance followRange = sniper.getAttribute(Attribute.FOLLOW_RANGE);
                        if (followRange != null) followRange.setBaseValue(40.0);
                        sniper.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
                        EntityEquipment eq = sniper.getEquipment();
                        if (eq != null) {
                            ItemStack bow = new ItemStack(Material.BOW);
                            ItemMeta bowMeta = bow.getItemMeta();
                            if (bowMeta != null) {
                                bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.POWER, 5, true);
                                bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY, 1, true);
                                bowMeta.setItemName("Sniper Bow");
                                bow.setItemMeta(bowMeta);
                            }
                            eq.setItemInMainHand(bow);
                            eq.setItemInMainHandDropChance(0);
                        }
                        phantom.addPassenger(sniper);
                    }
                }
            }
        }
    }

    private void spawnGroundCall(BossPuppet stand, World world, Location center, int extraSets) {
        Vector dir = stand.getLocation().getDirection();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        if (right.lengthSquared() < 0.01) right = new Vector(1, 0, 0);

        int count = 1 + extraSets;
        double groundY = center.getY();
        Location groundCenter = center.clone();
        groundCenter.setY(groundY);

        for (int side = -1; side <= 1; side += 2) {
            Location sealLoc = groundCenter.clone().add(right.clone().multiply(side * 5));
            sealLoc.setY(groundY + 1);
            spawnTriangleSeal(world, sealLoc, 100, MagicSealListener.Plane.YZ);

            for (int i = 0; i < count; i++) {
                Location spawnLoc = groundCenter.clone().add(right.clone().multiply(side * (5 + i * 4)));
                spawnLoc.setY(groundY);

                Ravager ravager = (Ravager) world.spawnEntity(spawnLoc, EntityType.RAVAGER);
                if (ravager != null) {
                    ravager.setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "War Beast");
                    ravager.setCustomNameVisible(true);
                    ravager.setPersistent(true);
                    ravager.setRemoveWhenFarAway(false);
                    ravager.addScoreboardTag("MSC_ArmorBossSummoned");
                    org.bukkit.attribute.AttributeInstance ravagerHealth = ravager.getAttribute(Attribute.MAX_HEALTH);
                    if (ravagerHealth != null) ravagerHealth.setBaseValue(300.0);
                    ravager.setHealth(300.0);
                    org.bukkit.attribute.AttributeInstance ravagerDamage = ravager.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (ravagerDamage != null) ravagerDamage.setBaseValue(24.0);
                    Player near = boss.findNearestPlayer(spawnLoc, 100);
                    if (near != null) ravager.setTarget(near);

                    Evoker evoker = (Evoker) world.spawnEntity(spawnLoc, EntityType.EVOKER);
                    if (evoker != null) {
                        evoker.setCustomName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Dark Priest");
                        evoker.setCustomNameVisible(true);
                        evoker.setPersistent(true);
                        evoker.setRemoveWhenFarAway(false);
                        evoker.addScoreboardTag("MSC_ArmorBossSummoned");
                        org.bukkit.attribute.AttributeInstance evokerHealth = evoker.getAttribute(Attribute.MAX_HEALTH);
                        if (evokerHealth != null) evokerHealth.setBaseValue(40.0);
                        evoker.setHealth(40.0);
                        evoker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
                        ravager.addPassenger(evoker);
                    }
                }
            }
        }
    }

    private void spawnTriangleSeal(World world, Location loc, int durationTicks, MagicSealListener.Plane plane) {
        MagicSealListener listener = plugin.getMagicSealListener();
        if (listener == null) return;
        ArmorStand marker = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        if (marker == null) return;
        marker.setVisible(false);
        marker.setGravity(false);
        marker.setMarker(true);
        marker.setCustomNameVisible(false);
        marker.addScoreboardTag("MSC_TriangleSeal");
        listener.spawnRunicTriangleSeal(marker, durationTicks, plane);
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (marker.isValid()) marker.remove();
        }, durationTicks + 5);
    }

    @Override
    public String getName() {
        return "trianglecall";
    }
}