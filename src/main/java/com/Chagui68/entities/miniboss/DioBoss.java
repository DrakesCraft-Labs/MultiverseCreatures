package com.Chagui68.entities.miniboss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ability.FreezeAbility;
import com.Chagui68.items.dio.DioStandHead;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.*;
import org.bukkit.entity.ItemDisplay;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.net.URL;
import java.util.*;
import java.util.Base64;

public class DioBoss implements Listener {

    // Dio queda desactivado directamente en código por decisión de diseño (alto
    // consumo de recursos). No puede reactivarse desde config.yml.
    private static final boolean DIO_FEATURE_ENABLED = false;

    public static boolean isFeatureEnabled() {
        return DIO_FEATURE_ENABLED;
    }

    private final MultiverseCreatures plugin;
    private final Map<UUID, DioBossInstance> activeBosses = new HashMap<>();
    private final Map<UUID, Long> bossCooldowns = new HashMap<>();
    private final Random random = new Random();

    private boolean debug;
    private double standOffsetZ = -1.0;
    private double standOffsetY = 2.0;
    private long cooldownMs = 120000;
    private double freezeRadius = 50.0;
    private double freezeDamageRadius = 30.0;
    private double freezeDamage = 10.0;
    private int freezeDurationTicks = 100;
    private double teleportInnerRadius = 25.0;
    private int teleportDarknessDuration = 100;
    private int teleportSlownessDuration = 100;

    private static final String DIO_HEAD_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAyZmJlNDVmZGRmMzU0NWRiNTlkOTgyNTlkZWM0Mjk2MWU2ZDM4NzYyMzU4NWY5OWY0N2QwNmQxOGEwMjQ5YSJ9fX0=";

    public DioBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (!DIO_FEATURE_ENABLED || !plugin.isEnabled("entities.dio-boss")) {
            plugin.getLogger().warning("[DioBoss] Disabled in code due to excessive resource consumption. "
                    + "Natural spawns and /msc spawn dio are disabled.");
            removeExistingBosses();
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadConfig();
        reloadExistingBosses();
    }

    private void removeExistingBosses() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Zombie zombie : world.getEntitiesByClass(Zombie.class)) {
                if (!zombie.getScoreboardTags().contains("MSC_DioBoss")) continue;
                for (Entity entity : zombie.getNearbyEntities(50, 20, 50)) {
                    if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains("MSC_DioStand")) {
                        stand.remove();
                        break;
                    }
                }
                activeBosses.remove(zombie.getUniqueId());
                zombie.remove();
                removed++;
            }
        }
        if (removed > 0) {
            plugin.getLogger().info("[DioBoss] Removed " + removed + " leftover Dio boss(es) because the feature is disabled.");
        }
    }

    public void reloadConfig() {
        standOffsetZ = plugin.getConfig().getDouble("entities.dio-boss.stand-offset-z", -1.0);
        standOffsetY = plugin.getConfig().getDouble("entities.dio-boss.stand-offset-y", 2.0);
        cooldownMs = plugin.getConfig().getLong("entities.dio-boss.cooldown-ms", 120000);
        freezeRadius = plugin.getConfig().getDouble("entities.dio-boss.freeze-radius", 50.0);
        freezeDamageRadius = plugin.getConfig().getDouble("entities.dio-boss.freeze-damage-radius", 30.0);
        freezeDamage = plugin.getConfig().getDouble("entities.dio-boss.freeze-damage", 10.0);
        freezeDurationTicks = plugin.getConfig().getInt("entities.dio-boss.freeze-duration-ticks", 100);
        teleportInnerRadius = plugin.getConfig().getDouble("entities.dio-boss.teleport-inner-radius", 25.0);
        teleportDarknessDuration = plugin.getConfig().getInt("entities.dio-boss.teleport-darkness-duration", 100);
        teleportSlownessDuration = plugin.getConfig().getInt("entities.dio-boss.teleport-slowness-duration", 100);
        debug = plugin.getConfig().getBoolean("entities.dio-boss.debug", false);
    }

    private void reloadExistingBosses() {
        if (!DIO_FEATURE_ENABLED || !plugin.isEnabled("entities.dio-boss")) return;
        for (World world : Bukkit.getWorlds()) {
            for (Zombie zombie : world.getEntitiesByClass(Zombie.class)) {
                if (!zombie.getScoreboardTags().contains("MSC_DioBoss")) continue;

                ArmorStand stand = null;
                for (Entity entity : zombie.getNearbyEntities(50, 20, 50)) {
                    if (entity instanceof ArmorStand as && entity.getScoreboardTags().contains("MSC_DioStand")) {
                        stand = as;
                        break;
                    }
                }

                if (stand == null || !stand.isValid()) {
                    if (debug) plugin.getLogger().warning("[DioBoss] Stand missing at " + zombie.getLocation() + ", will be respawned");
                }

                DioBossInstance instance = new DioBossInstance(zombie, stand);
                activeBosses.put(zombie.getUniqueId(), instance);
                startBossAI(instance);
                if (debug) plugin.getLogger().info("[DioBoss] Restarted boss AI for zombie at " + zombie.getLocation());
            }
        }
    }

    private PlayerProfile createProfile(String name, String base64Texture) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), name);
        try {
            String json = new String(Base64.getDecoder().decode(base64Texture));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String url = obj.getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url").getAsString();
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse Dio head texture: " + e.getMessage());
        }
        return profile;
    }

    public boolean trySpawnDio(Location location) {
        if (!DIO_FEATURE_ENABLED || !plugin.isEnabled("entities.dio-boss")) return false;
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        if (zombie == null) return false;

        double health = plugin.getConfig().getDouble("entities.dio-boss.health", 300.0);
        AttributeInstance maxHealthAttr = zombie.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) maxHealthAttr.setBaseValue(health);
        zombie.setHealth(health);
        AttributeInstance atkDmgAttr = zombie.getAttribute(Attribute.ATTACK_DAMAGE);
        if (atkDmgAttr != null) atkDmgAttr.setBaseValue(plugin.getConfig().getDouble("entities.dio-boss.damage", 10.0));
        zombie.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + "Dio Brando");
        zombie.setCustomNameVisible(true);
        zombie.setRemoveWhenFarAway(false);
        zombie.setPersistent(true);
        zombie.setCollidable(true);
        zombie.setAI(true);
        zombie.setCanPickupItems(false);
        zombie.setBaby(false);

        EntityEquipment equip = zombie.getEquipment();
        equip.setHelmet(createDioHead());
        equip.setChestplate(createTrimmedArmor(Material.GOLDEN_CHESTPLATE, TrimMaterial.NETHERITE, TrimPattern.VEX));
        equip.setLeggings(createTrimmedArmor(Material.GOLDEN_LEGGINGS, TrimMaterial.EMERALD, TrimPattern.SILENCE));
        equip.setBoots(createTrimmedArmor(Material.GOLDEN_BOOTS, TrimMaterial.EMERALD, TrimPattern.WARD));
        equip.setHelmetDropChance(0f);
        equip.setChestplateDropChance(0f);
        equip.setLeggingsDropChance(0f);
        equip.setBootsDropChance(0f);

        zombie.addScoreboardTag("MSC_DioBoss");
        zombie.setMaximumNoDamageTicks(0);

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(true);
        stand.setMarker(false);
        stand.setSmall(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setCanPickupItems(false);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.addScoreboardTag("MSC_DioStand");

        ItemStack standHead = DioStandHead.getHead();
        if (stand.getEquipment() != null) {
            stand.getEquipment().setHelmet(standHead);
            stand.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            stand.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
            stand.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        }

        DioBossInstance instance = new DioBossInstance(zombie, stand);
        activeBosses.put(zombie.getUniqueId(), instance);

        startBossAI(instance);
        return true;
    }

    private ItemStack createDioHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            try {
                org.bukkit.profile.PlayerProfile profile = createProfile("Dio", DIO_HEAD_TEXTURE);
                meta.setOwnerProfile(profile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set Dio head texture: " + e.getMessage());
            }
            meta.setDisplayName(ChatColor.GOLD + "Dio's Head");
            meta.setUnbreakable(true);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createTrimmedArmor(Material material, TrimMaterial trimMaterial, TrimPattern trimPattern) {
        ItemStack armor = new ItemStack(material);
        ItemMeta meta = armor.getItemMeta();
        if (meta instanceof ArmorMeta armorMeta) {
            try {
                armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not apply trim: " + e.getMessage());
            }
            armor.setItemMeta(armorMeta);
        }
        return armor;
    }

    private void startBossAI(DioBossInstance instance) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Zombie zombie = instance.zombie;
                ArmorStand stand = instance.stand;

                if (!DIO_FEATURE_ENABLED || !plugin.isEnabled("entities.dio-boss")) {
                    if (stand != null && stand.isValid()) stand.remove();
                    activeBosses.remove(zombie.getUniqueId());
                    zombie.remove();
                    cancel();
                    return;
                }

                if (zombie.isDead() || !zombie.isValid()) {
                    if (stand != null && stand.isValid()) stand.remove();
                    activeBosses.remove(zombie.getUniqueId());
                    cancel();
                    return;
                }

                if (stand == null || !stand.isValid()) {
                    if (stand != null && stand.isValid()) stand.remove();
                    for (Entity e : zombie.getNearbyEntities(50, 20, 50)) {
                        if (e instanceof ArmorStand as && e.getScoreboardTags().contains("MSC_DioStand")) {
                            as.remove();
                            break;
                        }
                    }
                    stand = (ArmorStand) zombie.getWorld().spawnEntity(zombie.getLocation(), EntityType.ARMOR_STAND);
                    stand.setVisible(true);
                    stand.setMarker(false);
                    stand.setSmall(false);
                    stand.setGravity(false);
                    stand.setInvulnerable(true);
                    stand.setCollidable(false);
                    stand.setCanPickupItems(false);
                    stand.setArms(true);
                    stand.setBasePlate(false);
                    stand.addScoreboardTag("MSC_DioStand");
                    ItemStack standHead = DioStandHead.getHead();
                    if (stand.getEquipment() != null) {
                        stand.getEquipment().setHelmet(standHead);
                        stand.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
                        stand.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
                        stand.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
                    }
                    instance.stand = stand;
                }

                if (!instance.isStandPunchingActive()) {
                    if (stand.getLocation().getChunk().isLoaded()) {
                        Location behind = zombie.getLocation()
                                .add(zombie.getLocation().getDirection().multiply(-1).setY(0).normalize().multiply(Math.abs(standOffsetZ)))
                                .add(0, standOffsetY, 0);
                        stand.teleport(behind);
                        stand.setRotation(zombie.getLocation().getYaw(), 0);
                    }
                }

                if (zombie.getTarget() instanceof Player target && target.isOnline()) {
                    UUID bossId = zombie.getUniqueId();
                    long now = System.currentTimeMillis();
                    Long lastAbility = bossCooldowns.getOrDefault(bossId, 0L);

                    if (now - lastAbility >= cooldownMs) {
                        List<Player> nearby = getNearbyPlayers(zombie.getLocation(), freezeRadius);
                        if (!nearby.isEmpty()) {
                            boolean usedAbility = false;

                            Player fleeingTarget = findPlayerOutsideInnerRadius(zombie.getLocation(), nearby, teleportInnerRadius);

                            if (fleeingTarget != null) {
                                performTheWorldTeleport(zombie, fleeingTarget);
                                usedAbility = true;
                            } else {
                                performTheWorldFreezing(zombie, nearby, freezeRadius, freezeDamageRadius, freezeDamage, freezeDurationTicks);
                                usedAbility = true;
                            }

                            if (usedAbility) {
                                bossCooldowns.put(bossId, now);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void performTheWorldFreezing(Zombie zombie, List<Player> nearby, double freezeRadius, double freezeDamageRadius, double damage, int durationTicks) {
        FreezeAbility freeze = plugin.getFreezeAbility();
        Location center = zombie.getLocation();

        List<Player> damagedPlayers = new ArrayList<>();

        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= freezeRadius * freezeRadius) {
                freeze.freezePlayer(p, durationTicks, "Dio - THE WORLD: FREEZING");

                if (p.getLocation().distanceSquared(center) <= freezeDamageRadius * freezeDamageRadius) {
                    damagedPlayers.add(p);
                }

                p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "THE WORLD: FREEZING",
                        ChatColor.GRAY + "Time has stopped!", 5, 40, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
            }
        }

        for (Player p : nearby) {
            p.getWorld().spawnParticle(Particle.PORTAL,
                    center.clone().add(random.nextDouble() - 0.5, random.nextDouble() * 3, random.nextDouble() - 0.5),
                    50, 0.5, 2, 0.5, 0.1);
        }

        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || zombie.isDead()) {
                    for (Player p : damagedPlayers) {
                        if (p.isOnline() && p.getLocation().distanceSquared(center) <= freezeDamageRadius * freezeDamageRadius) {
                            p.damage(damage, zombie);
                            p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.1);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                        }
                    }
                    cancel();
                    return;
                }
                for (Player p : nearby) {
                    if (p.isOnline()) {
                        Location ploc = p.getLocation();
                        p.getWorld().spawnParticle(Particle.PORTAL, ploc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        spawnSwordAnimation(zombie, nearby, center, freezeRadius, durationTicks);
    }

    private void spawnSwordAnimation(Zombie zombie, List<Player> targets, Location center, double radius, int durationTicks) {
        World world = center.getWorld();
        int totalSwords = 24;
        int spawnPhaseTicks = 20;
        int flyDurationTicks = 15;

        new BukkitRunnable() {
            int phase = 0;
            int phaseTicks = 0;
            Map<Player, List<ItemDisplay>> playerSwords = new HashMap<>();
            Map<ItemDisplay, Location> swordSpawnLocations = new HashMap<>();
            Map<ItemDisplay, Vector> swordFlyDirections = new HashMap<>();

            @Override
            public void run() {
                if (zombie.isDead() || !zombie.isValid() || phase >= 3) {
                    for (List<ItemDisplay> list : playerSwords.values()) {
                        for (ItemDisplay d : list) if (d.isValid()) d.remove();
                    }
                    playerSwords.clear();
                    swordSpawnLocations.clear();
                    swordFlyDirections.clear();
                    cancel();
                    return;
                }

                if (phase == 0) {
                    if (phaseTicks == 0) {
                        for (Player p : targets) {
                            if (!p.isOnline() || !p.getWorld().equals(world)) continue;
                            Location pLoc = p.getLocation();
                            double dist = pLoc.distance(center);
                            if (dist > radius) continue;

                            List<ItemDisplay> pSwords = new ArrayList<>();
                            for (int i = 0; i < totalSwords; i++) {
                                double angle = (2 * Math.PI * i) / totalSwords;
                                double spawnRadius = 8.0;
                                double height = 1.0 + random.nextDouble() * 2.5;
                                Location spawnLoc = pLoc.clone().add(
                                        Math.cos(angle) * spawnRadius,
                                        height,
                                        Math.sin(angle) * spawnRadius
                                );

                                ItemDisplay sword = (ItemDisplay) world.spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
                                sword.setItemStack(new ItemStack(Material.IRON_SWORD));
                                sword.setBrightness(new Display.Brightness(15, 15));
                                sword.setShadowRadius(0);
                                sword.setGlowing(true);
                                sword.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

                                Vector dirToPlayer = spawnLoc.toVector().subtract(pLoc.toVector()).multiply(-1).normalize();
                                float yaw = (float) Math.atan2(-dirToPlayer.getX(), dirToPlayer.getZ());
                                Quaternionf leftRot = new Quaternionf().rotateY(yaw).rotateX((float) Math.toRadians(-90));
                                sword.setTransformation(new Transformation(new Vector3f(), leftRot, new Vector3f(0.7f), new Quaternionf()));
                                sword.setInterpolationDelay(0);
                                sword.setInterpolationDuration(spawnPhaseTicks);

                                pSwords.add(sword);
                                swordSpawnLocations.put(sword, spawnLoc);
                            }
                            playerSwords.put(p, pSwords);
                        }
                    } else if (phaseTicks >= spawnPhaseTicks) {
                        phase = 1;
                        phaseTicks = 0;
                    }
                } else if (phase == 1) {
                    if (phaseTicks == 0) {
                        for (var entry : playerSwords.entrySet()) {
                            Player p = entry.getKey();
                            if (!p.isOnline() || !p.getWorld().equals(world)) continue;
                            Location targetLoc = p.getLocation().add(0, 1.5, 0);
                            List<ItemDisplay> pSwords = entry.getValue();

                            for (int i = 0; i < pSwords.size(); i++) {
                                ItemDisplay sword = pSwords.get(i);
                                if (!sword.isValid()) continue;
                                Location spawnLoc = swordSpawnLocations.get(sword);
                                if (spawnLoc == null) continue;

                                Vector dir = targetLoc.toVector().subtract(spawnLoc.toVector());
                                dir.normalize();
                                swordFlyDirections.put(sword, dir);
                            }
                        }
                    }

                    for (var entry : playerSwords.entrySet()) {
                        Player p = entry.getKey();
                        if (!p.isOnline() || !p.getWorld().equals(world)) continue;
                        Location targetLoc = p.getLocation().add(0, 1.5, 0);
                        List<ItemDisplay> pSwords = entry.getValue();

                        for (int i = 0; i < pSwords.size(); i++) {
                            ItemDisplay sword = pSwords.get(i);
                            if (!sword.isValid()) continue;

                            Location spawnLoc = swordSpawnLocations.get(sword);
                            if (spawnLoc == null) continue;

                            Vector flyDir = swordFlyDirections.get(sword);
                            if (flyDir == null) continue;

                            Location passThrough = targetLoc.clone().add(flyDir.clone().multiply(4));

                            float progress = Math.min(1.0f, (float) phaseTicks / flyDurationTicks);
                            double lerpX = spawnLoc.getX() + (passThrough.getX() - spawnLoc.getX()) * progress;
                            double lerpY = spawnLoc.getY() + (passThrough.getY() - spawnLoc.getY()) * progress;
                            double lerpZ = spawnLoc.getZ() + (passThrough.getZ() - spawnLoc.getZ()) * progress;

                            float yaw = (float) Math.atan2(-flyDir.getX(), flyDir.getZ());
                            Vector3f translation = new Vector3f(
                                    (float) (lerpX - spawnLoc.getX()),
                                    (float) (lerpY - spawnLoc.getY()),
                                    (float) (lerpZ - spawnLoc.getZ())
                            );
                            Quaternionf leftRot = new Quaternionf().rotateY(yaw).rotateX((float) Math.toRadians(-90));
                            sword.setTransformation(new Transformation(translation, leftRot, new Vector3f(0.7f), new Quaternionf()));
                            sword.setInterpolationDelay(0);
                            sword.setInterpolationDuration(1);
                        }
                    }
                    if (phaseTicks >= flyDurationTicks) {
                        phase = 2;
                        phaseTicks = 0;
                    }
                } else if (phase == 2) {
                    if (phaseTicks >= 5) {
                        for (List<ItemDisplay> list : playerSwords.values()) {
                            for (ItemDisplay d : list) if (d.isValid()) d.remove();
                        }
                        playerSwords.clear();
                        swordSpawnLocations.clear();
                        swordFlyDirections.clear();
                        phase = 3;
                    }
                }
                phaseTicks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void performTheWorldTeleport(Zombie zombie, Player target) {
        Location zombieLoc = zombie.getLocation();
        Location teleportPos = target.getLocation().add(0, 0, -2);
        zombie.teleport(teleportPos);

        target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, teleportDarknessDuration, 0, false, true));
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, teleportSlownessDuration, 0, false, true));
        target.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "THE WORLD: TELEPORT",
                ChatColor.GRAY + "You can't escape!", 5, 40, 10);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);

        zombie.getWorld().spawnParticle(Particle.DRAGON_BREATH, teleportPos, 30, 0.5, 1, 0.5, 0.05, 0.0f);
        zombie.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation(), 20, 0.5, 1, 0.5, 0.05, 0.0f);
        zombie.setTarget(target);
        AttributeInstance atkSpeed = zombie.getAttribute(Attribute.ATTACK_SPEED);
        if (atkSpeed != null) atkSpeed.setBaseValue(100);

        DioBossInstance instance = activeBosses.get(zombie.getUniqueId());
        if (instance != null && instance.stand != null && instance.stand.isValid()) {
            instance.triggerStandPunch(instance.stand, target, plugin);
        }
    }

    private Player findFurthestPlayer(Location center, List<Player> players, double radius) {
        Player furthest = null;
        double maxDist = 0;
        for (Player p : players) {
            double dist = p.getLocation().distanceSquared(center);
            if (dist > maxDist && dist <= radius * radius) {
                maxDist = dist;
                furthest = p;
            }
        }
        return furthest;
    }

    private Player findPlayerOutsideInnerRadius(Location center, List<Player> players, double innerRadius) {
        double innerRadiusSq = innerRadius * innerRadius;
        Player target = null;
        double maxDist = 0;
        for (Player p : players) {
            double dist = p.getLocation().distanceSquared(center);
            if (dist > innerRadiusSq && dist > maxDist) {
                maxDist = dist;
                target = p;
            }
        }
        return target;
    }

    private List<Player> getNearbyPlayers(Location center, double radius) {
        List<Player> result = new ArrayList<>();
        double radiusSq = radius * radius;
        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= radiusSq) {
                result.add(p);
            }
        }
        return result;
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!DIO_FEATURE_ENABLED || !plugin.isEnabled("entities.dio-boss")) return;
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Zombie zombie)) continue;
            if (!zombie.getScoreboardTags().contains("MSC_DioBoss")) continue;
            if (activeBosses.containsKey(zombie.getUniqueId())) continue;

            ArmorStand stand = null;
            for (Entity e : zombie.getNearbyEntities(50, 20, 50)) {
                if (e instanceof ArmorStand as && e.getScoreboardTags().contains("MSC_DioStand")) {
                    stand = as;
                    break;
                }
            }

            if (stand == null || !stand.isValid()) {
                if (debug) plugin.getLogger().warning("[DioBoss] Stand missing at " + zombie.getLocation() + ", will be respawned");
            }

            DioBossInstance instance = new DioBossInstance(zombie, stand);
            activeBosses.put(zombie.getUniqueId(), instance);
            startBossAI(instance);
            if (debug) plugin.getLogger().info("[DioBoss] Restarted boss AI from chunk load at " + zombie.getLocation());
        }
    }

    @EventHandler
    public void onDioDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Zombie zombie && zombie.getScoreboardTags().contains("MSC_DioBoss")) {
            if (event.getEntity() instanceof Player player) {
                if (player.getHealth() <= 0) return;

                DioBossInstance instance = activeBosses.get(zombie.getUniqueId());
                if (instance != null && instance.stand != null && instance.stand.isValid()) {
                    instance.triggerStandPunch(instance.stand, player, plugin);
                }
            }
        }
    }

    @EventHandler
    public void onDioDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains("MSC_DioBoss")) return;

        DioBossInstance instance = activeBosses.remove(zombie.getUniqueId());
        if (instance != null && instance.stand != null && instance.stand.isValid()) {
            instance.stand.remove();
        }

        event.getDrops().clear();
        event.setDroppedExp(500);

        double dropChance = plugin.getConfig().getDouble("entities.dio-boss.drop-chance", 0.1);
        if (random.nextDouble() < dropChance) {
            zombie.getWorld().dropItemNaturally(zombie.getLocation(), DioStandHead.getHead());
        }

        zombie.getWorld().strikeLightningEffect(zombie.getLocation());
        zombie.getWorld().playSound(zombie.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.5f, 0.5f);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, "MSC_DioBoss", "entities.dio-boss.death-messages");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bossCooldowns.remove(event.getPlayer().getUniqueId());
    }

    private static class DioBossInstance {
        final Zombie zombie;
        ArmorStand stand;
        private boolean standPunching = false;
        private Player punchTarget;
        private int punchTicks = 0;
        private boolean leftArm = true;

        DioBossInstance(Zombie zombie, ArmorStand stand) {
            this.zombie = zombie;
            this.stand = stand;
        }

        void triggerStandPunch(ArmorStand stand, Player target, MultiverseCreatures plugin) {
            if (stand == null || !stand.isValid()) return;
            this.standPunching = true;
            this.punchTarget = target;
            this.punchTicks = 0;
            this.leftArm = true;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!stand.isValid() || punchTarget == null || !punchTarget.isOnline() || punchTicks >= 3) {
                        if (stand.isValid()) {
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                        }
                        standPunching = false;
                        punchTarget = null;
                        cancel();
                        return;
                    }

                    Location zombieLoc = zombie.getLocation();
                    Location standPos = zombieLoc.clone()
                            .add(zombieLoc.getDirection().multiply(1.5))
                            .add(0, 1.5, 0);
                    if (stand.getWorld().equals(zombie.getWorld()) && stand.getLocation().distanceSquared(standPos) > 1) {
                        stand.teleport(standPos);
                        stand.setRotation(zombieLoc.getYaw(), 0);
                    }

                    if (punchTicks == 0) {
                        stand.setLeftArmPose(new EulerAngle(-1.57f, 0, 0));
                        stand.setRightArmPose(new EulerAngle(-1.57f, 0, 0));

                        if (punchTarget.isOnline()) {
                            punchTarget.getWorld().spawnParticle(Particle.CRIT, punchTarget.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
                            punchTarget.getWorld().playSound(punchTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                        }
                    } else if (punchTicks == 1) {
                        stand.setLeftArmPose(new EulerAngle(-0.8f, 0, 0));
                        stand.setRightArmPose(new EulerAngle(-0.8f, 0, 0));
                    } else if (punchTicks == 2) {
                        stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                        stand.setRightArmPose(new EulerAngle(0, 0, 0));
                    }

                    punchTicks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        boolean isStandPunching() {
            return standPunching;
        }

        boolean isStandPunchingActive() {
            return standPunching && punchTarget != null;
        }

        Player getPunchTarget() {
            return punchTarget;
        }
    }
}
