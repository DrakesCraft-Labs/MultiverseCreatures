package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.MilitaryComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZombieHorseTrap implements Listener {

    private final MultiverseCreatures plugin;
    private static final String TRAP_TAG = "MSC_ZombieHorseTrap";
    private static final String ARMY_TAG = "MSC_ZombieArmy";
    private static final String TANK_TAG = "MSC_ZombieTank";
    private static final String DUELIST_TAG = "MSC_Duelist";
    private static final String LANCER_TAG = "MSC_Lancer";
    private static final String CAMEL_ZOMBIE_TAG = "MSC_CamelZombie";
    private static final String CAMEL_SKELETON_TAG = "MSC_CamelSkeleton";
    private static final String SNIPER_TAG = "MSC_Sniper";
    private static final String LANCER_HORSE_TAG = "MSC_LancerHorse";
    private static final String CAMEL_TAG = "MSC_ArmyCamel";

    private final Map<UUID, ArmyInstance> armies = new ConcurrentHashMap<>();

    public ZombieHorseTrap(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkTrapActivation();
                for (ArmyInstance army : armies.values()) {
                    try {
                        tickArmy(army);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ZombieHorseTrap] Error ticking army: " + e.getMessage());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean trySpawn(Location location) {
        ZombieHorse horse = (ZombieHorse) location.getWorld().spawnEntity(location, EntityType.ZOMBIE_HORSE);
        if (horse == null) return false;

        horse.addScoreboardTag(TRAP_TAG);
        horse.setCustomName(ChatColor.WHITE + "" + ChatColor.BOLD + "Military Zombie Horse");
        horse.setCustomNameVisible(true);
        horse.setPersistent(true);
        horse.setRemoveWhenFarAway(false);
        horse.setTamed(true);
        horse.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        return true;
    }

    public boolean trySpawnTank(Location location) {
        Zombie tank = spawnTank(location);
        if (tank == null) return false;
        ArmyInstance army = new ArmyInstance();
        army.tank = tank;
        army.tankDead = false;
        army.entities.add(tank.getUniqueId());
        armies.put(army.id, army);
        return true;
    }

    public boolean trySpawnDuelist(Location location) {
        Skeleton duelist = spawnDuelist(location);
        if (duelist == null) return false;
        ArmyInstance army = new ArmyInstance();
        army.duelist1 = duelist;
        army.tankDead = true;
        army.entities.add(duelist.getUniqueId());
        armies.put(army.id, army);
        return true;
    }

    public boolean trySpawnLancer(Location location) {
        ArmyInstance army = new ArmyInstance();
        spawnLancer(location, army);
        if (army.lancer == null) return false;
        army.tankDead = true;
        armies.put(army.id, army);
        return true;
    }

    public boolean trySpawnCamel(Location location) {
        ArmyInstance army = new ArmyInstance();
        spawnCamel(location, army, true);
        if (army.camel1 == null) return false;
        army.tankDead = true;
        armies.put(army.id, army);
        return true;
    }

    public boolean trySpawnSniper(Location location) {
        WitherSkeleton sniper = spawnSniper(location);
        if (sniper == null) return false;
        ArmyInstance army = new ArmyInstance();
        army.sniper = sniper;
        army.tankDead = true;
        army.entities.add(sniper.getUniqueId());
        armies.put(army.id, army);
        return true;
    }

    @EventHandler
    public void onTankDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains(TANK_TAG)) return;

        if (event.getCause() == DamageCause.PROJECTILE) {
            event.setDamage(event.getDamage() * 0.5);
        }
    }

    @EventHandler
    public void onSniperHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof WitherSkeleton)) return;
        if (!(event.getHitEntity() instanceof Player player)) return;

        WitherSkeleton shooter = (WitherSkeleton) event.getEntity().getShooter();
        if (!shooter.getScoreboardTags().contains(SNIPER_TAG)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0, false, true));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity.getScoreboardTags().contains(LANCER_HORSE_TAG)) {
            for (var entry : armies.entrySet()) {
                ArmyInstance army = entry.getValue();
                if (army.lancerHorse != null && army.lancerHorse.getUniqueId().equals(entity.getUniqueId())) {
                    army.lancerHorseDead = true;
                    if (army.lancer != null) {
                        army.lancer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2, false, false));
                        army.lancer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 0, false, false));
                    }
                    break;
                }
            }
        }

        if (entity.getScoreboardTags().contains(CAMEL_TAG)) {
            for (var entry : armies.entrySet()) {
                ArmyInstance army = entry.getValue();
                UUID eid = entity.getUniqueId();
                if ((army.camel1 != null && army.camel1.getUniqueId().equals(eid)) ||
                        (army.camel2 != null && army.camel2.getUniqueId().equals(eid))) {
                    if (army.camel1 != null && army.camel1.getUniqueId().equals(eid)) {
                        army.camel1Dead = true;
                        if (army.camelZombie1 != null) {
                            army.camelZombie1.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
                            army.camelZombie1.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
                        }
                        if (army.camelSkeleton1 != null) {
                            army.camelSkeleton1.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));
                        }
                    } else {
                        army.camel2Dead = true;
                        if (army.camelZombie2 != null) {
                            army.camelZombie2.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
                            army.camelZombie2.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
                        }
                        if (army.camelSkeleton2 != null) {
                            army.camelSkeleton2.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));
                        }
                    }
                    break;
                }
            }
        }

        if (entity.getScoreboardTags().contains(TANK_TAG)) {
            for (ArmyInstance army : armies.values()) {
                if (army.tank != null && army.tank.getUniqueId().equals(entity.getUniqueId())) {
                    army.tankDead = true;
                    break;
                }
            }
        }

        double dropChance = plugin.getConfig().getDouble("zombie-horse-trap.military-component-drop-chance", 0.3);
        if (Math.random() < dropChance) {
            for (String tag : new String[]{TANK_TAG, DUELIST_TAG, LANCER_TAG, CAMEL_ZOMBIE_TAG, CAMEL_SKELETON_TAG, SNIPER_TAG}) {
                if (entity.getScoreboardTags().contains(tag)) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), MilitaryComponent.MILITARY_COMPONENT.clone());
                    break;
                }
            }
        }
    }

    private void checkTrapActivation() {
        for (World world : Bukkit.getWorlds()) {
            for (ZombieHorse horse : world.getEntitiesByClass(ZombieHorse.class)) {
                if (!horse.getScoreboardTags().contains(TRAP_TAG)) continue;
                if (horse.isDead()) continue;

                for (Player player : world.getPlayers()) {
                    if (player.isDead()) continue;
                    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
                        continue;
                    if (player.getLocation().distanceSquared(horse.getLocation()) <= 36) {
                        activateTrap(horse);
                        break;
                    }
                }
            }
        }
    }

    private void activateTrap(ZombieHorse horse) {
        Location loc = horse.getLocation();
        loc.setPitch(0);
        World world = loc.getWorld();
        double yaw = Math.toRadians(loc.getYaw());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);

        ArmyInstance army = new ArmyInstance();

        Location tankLoc = loc.clone();
        army.tank = spawnTank(tankLoc);
        if (army.tank != null) {
            army.tank.addScoreboardTag(ARMY_TAG);
            army.entities.add(army.tank.getUniqueId());
        }

        Location duelist1Loc = loc.clone().add(-fz * 3, 0, fx * 3).add(-fx * 2, 0, -fz * 2);
        army.duelist1 = spawnDuelist(duelist1Loc);
        if (army.duelist1 != null) {
            army.duelist1.addScoreboardTag(ARMY_TAG);
            army.entities.add(army.duelist1.getUniqueId());
        }

        Location duelist2Loc = loc.clone().add(fz * 3, 0, -fx * 3).add(-fx * 2, 0, -fz * 2);
        army.duelist2 = spawnDuelist(duelist2Loc);
        if (army.duelist2 != null) {
            army.duelist2.addScoreboardTag(ARMY_TAG);
            army.entities.add(army.duelist2.getUniqueId());
        }

        Location lancerLoc = loc.clone().add(-fx * 3, 0, -fz * 3);
        spawnLancer(lancerLoc, army);

        Location camel1Loc = loc.clone().add(-fz * 4, 0, fx * 4).add(-fx * 5, 0, -fz * 5);
        spawnCamel(camel1Loc, army, true);

        Location camel2Loc = loc.clone().add(fz * 4, 0, -fx * 4).add(-fx * 5, 0, -fz * 5);
        spawnCamel(camel2Loc, army, false);

        Location sniperLoc = loc.clone().add(-fx * 10, 0, -fz * 10);
        army.sniper = spawnSniper(sniperLoc);
        if (army.sniper != null) {
            army.sniper.addScoreboardTag(ARMY_TAG);
            army.entities.add(army.sniper.getUniqueId());
        }

        armies.put(army.id, army);
        horse.remove();
    }

    private Zombie spawnTank(Location loc) {
        Zombie tank = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        if (tank == null) return null;
        tank.setBaby(false);

        tank.addScoreboardTag(TANK_TAG);
        tank.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "Zombie Tank");
        tank.setCustomNameVisible(true);
        tank.setPersistent(true);
        tank.setRemoveWhenFarAway(false);

        setAttribute(tank, Attribute.MAX_HEALTH, 350.0);
        tank.setHealth(350.0);
        setAttribute(tank, Attribute.ATTACK_DAMAGE, 10.0);
        setAttribute(tank, Attribute.SCALE, 1.5);
        setAttribute(tank, Attribute.MOVEMENT_SPEED, 0.15);

        tank.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 999999, 0, false, false));
        tank.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
        tank.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        setArmor(tank, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
        setLeatherHelmetColor(tank, Color.LIME);

        return tank;
    }

    private Skeleton spawnDuelist(Location loc) {
        Skeleton duelist = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
        if (duelist == null) return null;

        duelist.addScoreboardTag(DUELIST_TAG);
        duelist.setCustomName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Military Skeleton Duelist");
        duelist.setCustomNameVisible(true);
        duelist.setPersistent(true);
        duelist.setRemoveWhenFarAway(false);

        setAttribute(duelist, Attribute.MAX_HEALTH, 50.0);
        duelist.setHealth(50.0);

        setArmor(duelist, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
        setLeatherHelmetColor(duelist, Color.fromRGB(0xCC88FF));

        EntityEquipment spawnEq = duelist.getEquipment();
        if (spawnEq != null) {
            spawnEq.setItemInMainHand(createBow());
            spawnEq.setItemInMainHandDropChance(0);
        }
        duelist.setAI(true);
        duelist.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        return duelist;
    }

    private void spawnLancer(Location loc, ArmyInstance army) {
        Zombie lancer = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        if (lancer == null) return;
        lancer.setBaby(false);

        lancer.addScoreboardTag(LANCER_TAG);
        lancer.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "Zombie Lancer");
        lancer.setCustomNameVisible(true);
        lancer.setPersistent(true);
        lancer.setRemoveWhenFarAway(false);

        setArmor(lancer, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
        setLeatherHelmetColor(lancer, Color.GRAY);

        EntityEquipment eq = lancer.getEquipment();
        if (eq != null) {
            eq.setItemInMainHand(createLance(Material.IRON_SPEAR, "Iron Lance"));
            eq.setItemInMainHandDropChance(0);
        }

        lancer.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        ZombieHorse horse = (ZombieHorse) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);
        if (horse != null) {
            horse.addScoreboardTag(LANCER_HORSE_TAG);
            horse.setCustomName(ChatColor.WHITE + "" + ChatColor.BOLD + "Military Zombie Horse");
            horse.setCustomNameVisible(false);
            horse.setPersistent(true);
            horse.setRemoveWhenFarAway(false);
            horse.setTamed(true);

            horse.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
            horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2, false, false));
            horse.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

            horse.addPassenger(lancer);
            army.lancerHorse = horse;
            army.entities.add(horse.getUniqueId());
        }

        army.lancer = lancer;
        army.entities.add(lancer.getUniqueId());
    }

    private void spawnCamel(Location loc, ArmyInstance army, boolean isFirst) {
        Camel camel = (Camel) loc.getWorld().spawnEntity(loc, EntityType.CAMEL_HUSK);
        if (camel == null) return;

        camel.addScoreboardTag(CAMEL_TAG);
        camel.setCustomNameVisible(false);
        camel.setPersistent(true);
        camel.setRemoveWhenFarAway(false);

        camel.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));
        camel.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 1, false, false));
        camel.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        if (zombie != null) {
            zombie.setBaby(false);
            zombie.addScoreboardTag(CAMEL_ZOMBIE_TAG);
            zombie.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Camel Zombie Rider");
            zombie.setCustomNameVisible(true);
            zombie.setPersistent(true);
            zombie.setRemoveWhenFarAway(false);

            setLeatherHelmetColor(zombie, Color.ORANGE);
            setArmor(zombie, null, Material.COPPER_CHESTPLATE, Material.COPPER_LEGGINGS, Material.COPPER_BOOTS);

            EntityEquipment eq = zombie.getEquipment();
            if (eq != null) {
                eq.setItemInMainHand(createLance(Material.DIAMOND_SPEAR, "Diamond Lance"));
                eq.setItemInMainHandDropChance(0);
            }

            zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
            camel.addPassenger(zombie);
        }

        LivingEntity skeleton = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.BOGGED);
        if (skeleton != null) {
            skeleton.addScoreboardTag(CAMEL_SKELETON_TAG);
            skeleton.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Camel Skeleton Rider");
            skeleton.setCustomNameVisible(true);
            skeleton.setPersistent(true);
            skeleton.setRemoveWhenFarAway(false);

            setLeatherHelmetColor(skeleton, Color.ORANGE);
            setArmor(skeleton, null, Material.COPPER_CHESTPLATE, Material.COPPER_LEGGINGS, Material.COPPER_BOOTS);

            EntityEquipment skeleq = skeleton.getEquipment();
            if (skeleq != null) {
                ItemStack bow = new ItemStack(Material.BOW);
                ItemMeta bowMeta = bow.getItemMeta();
                if (bowMeta != null) {
                    bowMeta.addEnchant(Enchantment.POWER, 2, true);
                    bowMeta.addEnchant(Enchantment.PUNCH, 2, true);
                    bow.setItemMeta(bowMeta);
                }
                skeleq.setItemInMainHand(bow);
                skeleq.setItemInMainHandDropChance(0);
            }

            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 0, false, false));
            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
            camel.addPassenger(skeleton);
        }

        if (isFirst) {
            army.camel1 = camel;
            army.camelZombie1 = zombie;
            army.camelSkeleton1 = skeleton;
        } else {
            army.camel2 = camel;
            army.camelZombie2 = zombie;
            army.camelSkeleton2 = skeleton;
        }
        army.entities.add(camel.getUniqueId());
    }

    private WitherSkeleton spawnSniper(Location loc) {
        WitherSkeleton sniper = (WitherSkeleton) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
        if (sniper == null) return null;

        sniper.addScoreboardTag(SNIPER_TAG);
        sniper.setCustomName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Sniper Skeleton");
        sniper.setCustomNameVisible(true);
        sniper.setPersistent(true);
        sniper.setRemoveWhenFarAway(false);

        setAttribute(sniper, Attribute.MAX_HEALTH, 40.0);
        sniper.setHealth(40.0);
        setAttribute(sniper, Attribute.FOLLOW_RANGE, 40.0);
        sniper.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        EntityEquipment eq = sniper.getEquipment();
        if (eq != null) {
            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta bowMeta = bow.getItemMeta();
            if (bowMeta != null) {
                bowMeta.addEnchant(Enchantment.POWER, 5, true);
                bowMeta.addEnchant(Enchantment.INFINITY, 1, true);
                bowMeta.setItemName("Sniper Bow");
                bow.setItemMeta(bowMeta);
            }
            eq.setItemInMainHand(bow);
            eq.setItemInMainHandDropChance(0);
        }

        setFullLeatherArmorColor(sniper, Color.GREEN);

        sniper.setAI(true);

        return sniper;
    }

    private void tickArmy(ArmyInstance army) {
        if (army.cleanup) return;

        boolean anyAlive = false;
        for (UUID id : army.entities) {
            Entity e = Bukkit.getEntity(id);
            if (e != null && !e.isDead()) {
                anyAlive = true;
                break;
            }
        }
        if (!anyAlive) {
            army.cleanup = true;
            armies.remove(army.id);
            return;
        }

        if (army.tank != null && army.tank.isDead()) army.tankDead = true;
        if (army.duelist1 != null && army.duelist1.isDead()) army.duelist1 = null;
        if (army.duelist2 != null && army.duelist2.isDead()) army.duelist2 = null;
        if (army.lancer != null && army.lancer.isDead()) army.lancer = null;
        if (army.lancerHorse != null && army.lancerHorse.isDead()) army.lancerHorse = null;
        if (army.camel1 != null && army.camel1.isDead()) army.camel1 = null;
        if (army.camel2 != null && army.camel2.isDead()) army.camel2 = null;
        if (army.sniper != null && army.sniper.isDead()) army.sniper = null;

        tickDuelist(army, army.duelist1);
        tickDuelist(army, army.duelist2);

        tickSniper(army);

        if (army.camelSkeleton1 != null && !army.camelSkeleton1.isDead() && !army.tankDead) {
            tickCamelSkeleton(army, army.camelSkeleton1);
        }
        if (army.camelSkeleton2 != null && !army.camelSkeleton2.isDead() && !army.tankDead) {
            tickCamelSkeleton(army, army.camelSkeleton2);
        }
    }

    private void tickDuelist(ArmyInstance army, Skeleton duelist) {
        if (duelist == null || duelist.isDead()) return;

        Player nearest = findNearestPlayer(duelist.getLocation(), 30);
        if (nearest == null) return;

        duelist.setTarget(nearest);

        double dist = duelist.getLocation().distance(nearest.getLocation());
        EntityEquipment eq = duelist.getEquipment();
        if (eq == null) return;

        if (dist <= 6) {
            ItemStack hand = eq.getItemInMainHand();
            if (hand == null || hand.getType() != Material.IRON_SWORD) {
                eq.setItemInMainHand(createDuelistSword());
            }
        } else {
            ItemStack hand = eq.getItemInMainHand();
            if (hand == null || hand.getType() != Material.BOW) {
                eq.setItemInMainHand(createBow());
            }

            if (!army.tankDead && army.tank != null && !army.tank.isDead()) {
                Location tankLoc = army.tank.getLocation();
                double yaw = Math.toRadians(tankLoc.getYaw());
                double fx = -Math.sin(yaw);
                double fz = Math.cos(yaw);

                Location behind = tankLoc.clone().add(-fx * 2, 0, -fz * 2);
                boolean isLeft = army.duelist1 != null && duelist.getUniqueId().equals(army.duelist1.getUniqueId());
                if (isLeft) {
                    behind.add(-fz * 3, 0, fx * 3);
                } else {
                    behind.add(fz * 3, 0, -fx * 3);
                }

                double distToBehind = duelist.getLocation().distanceSquared(behind);
                if (distToBehind > 16) {
                    duelist.setVelocity(behind.toVector().subtract(duelist.getLocation().toVector()).normalize().multiply(0.3));
                }

                double distToTank = duelist.getLocation().distanceSquared(army.tank.getLocation());
                if (distToTank < 4) {
                    Vector away = duelist.getLocation().toVector().subtract(army.tank.getLocation().toVector());
                    if (away.lengthSquared() > 0) {
                        duelist.setVelocity(away.normalize().multiply(0.4));
                    }
                }
            }
        }
    }

    private void tickCamelSkeleton(ArmyInstance army, LivingEntity skeleton) {
        if (skeleton == null || skeleton.isDead()) return;
        if (army.tank == null || army.tank.isDead()) return;

        Location tankLoc = army.tank.getLocation();
        double yaw = Math.toRadians(tankLoc.getYaw());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);

        Location behind = tankLoc.clone().add(-fx * 2, 0, -fz * 2);
        boolean isLeft = army.camelSkeleton1 != null && skeleton.getUniqueId().equals(army.camelSkeleton1.getUniqueId());
        if (isLeft) {
            behind.add(-fz * 3, 0, fx * 3);
        } else {
            behind.add(fz * 3, 0, -fx * 3);
        }

        double dist = skeleton.getLocation().distanceSquared(behind);
        if (dist > 16) {
            skeleton.setVelocity(behind.toVector().subtract(skeleton.getLocation().toVector()).normalize().multiply(0.3));
        }
    }

    private Player findNearestPlayer(Location loc, double range) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.isDead() || !p.isOnline()) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            double dist = loc.distanceSquared(p.getLocation());
            if (dist < nearestDist && dist <= range * range) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    private void tickSniper(ArmyInstance army) {
        if (army.sniper == null || army.sniper.isDead()) return;

        WitherSkeleton sniper = army.sniper;
        Player target = findNearestPlayer(sniper.getLocation(), 50);
        if (target == null) return;

        sniper.setTarget(target);

        Location sniperLoc = sniper.getEyeLocation();
        Location playerLoc = target.getLocation();

        Vector playerVel = target.getVelocity();
        double distance = sniperLoc.distance(playerLoc);
        double arrowSpeed = 3.0;
        long ticksToReach = Math.max(1, (long) (distance / arrowSpeed));

        Location predicted = playerLoc.clone().add(playerVel.clone().multiply(ticksToReach));
        predicted.add(0, distance * distance * 0.0028, 0);

        Vector direction = predicted.toVector().subtract(sniperLoc.toVector()).normalize();

        Location lookAt = sniperLoc.clone().add(direction);
        sniper.lookAt(lookAt);

        army.sniperTicks++;
        if (army.sniperTicks % 30 == 0) {
            Arrow arrow = sniper.getWorld().spawnArrow(
                    sniper.getEyeLocation(),
                    direction,
                    (float) arrowSpeed,
                    0
            );
            if (arrow != null) {
                arrow.setDamage(6.0);
                arrow.setCritical(true);
                arrow.setShooter(sniper);
            }
        }
    }

    private void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr != null) attr.setBaseValue(value);
    }

    private void setArmor(LivingEntity entity, Material helmet, Material chestplate, Material leggings, Material boots) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;
        if (helmet != null) {
            eq.setHelmet(new ItemStack(helmet));
            eq.setHelmetDropChance(0);
        }
        if (chestplate != null) {
            eq.setChestplate(new ItemStack(chestplate));
            eq.setChestplateDropChance(0);
        }
        if (leggings != null) {
            eq.setLeggings(new ItemStack(leggings));
            eq.setLeggingsDropChance(0);
        }
        if (boots != null) {
            eq.setBoots(new ItemStack(boots));
            eq.setBootsDropChance(0);
        }
    }

    private void setFullLeatherArmorColor(LivingEntity entity, Color color) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;
        for (Material mat : new Material[]{Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS}) {
            ItemStack armor = new ItemStack(mat);
            LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
            if (meta != null) {
                meta.setColor(color);
                meta.setUnbreakable(true);
                armor.setItemMeta(meta);
            }
            switch (mat) {
                case LEATHER_HELMET -> eq.setHelmet(armor);
                case LEATHER_CHESTPLATE -> eq.setChestplate(armor);
                case LEATHER_LEGGINGS -> eq.setLeggings(armor);
                case LEATHER_BOOTS -> eq.setBoots(armor);
            }
        }
        eq.setHelmetDropChance(0);
        eq.setChestplateDropChance(0);
        eq.setLeggingsDropChance(0);
        eq.setBootsDropChance(0);
    }

    private void setLeatherHelmetColor(LivingEntity entity, Color color) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            helmet.setItemMeta(meta);
        }
        eq.setHelmet(helmet);
        eq.setHelmetDropChance(0);
    }

    private ItemStack createLance(Material material, String name) {
        ItemStack lance = new ItemStack(material);
        ItemMeta meta = lance.getItemMeta();
        if (meta != null) {
            meta.setItemName(name);
            meta.setUnbreakable(true);
            lance.setItemMeta(meta);
        }
        return lance;
    }

    private ItemStack createBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.FLAME, 1, true);
            meta.addEnchant(Enchantment.POWER, 3, true);
            bow.setItemMeta(meta);
        }
        return bow;
    }

    private ItemStack createDuelistSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.SHARPNESS, 3, true);
            meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            meta.setItemName("Duelist Sword");
            sword.setItemMeta(meta);
        }
        return sword;
    }

    private static class ArmyInstance {
        final UUID id = UUID.randomUUID();
        final java.util.Set<UUID> entities = new java.util.HashSet<>();
        boolean cleanup;
        boolean tankDead;
        boolean lancerHorseDead;
        boolean camel1Dead;
        boolean camel2Dead;

        Zombie tank;
        Skeleton duelist1;
        Skeleton duelist2;
        Zombie lancer;
        ZombieHorse lancerHorse;
        Camel camel1;
        Camel camel2;
        Zombie camelZombie1;
        Zombie camelZombie2;
        LivingEntity camelSkeleton1;
        LivingEntity camelSkeleton2;
        WitherSkeleton sniper;
        int sniperTicks;
    }
}
