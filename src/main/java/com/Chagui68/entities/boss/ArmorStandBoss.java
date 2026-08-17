package com.Chagui68.entities.boss;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.attack.BossAttack;
import com.Chagui68.items.components.SentinelCore;
import com.Chagui68.entities.boss.attack.aerial.AerialRushAttack;
import com.Chagui68.entities.boss.attack.aerial.AirSlamAttack;
import com.Chagui68.entities.boss.attack.aerial.CrossSlashAttack;
import com.Chagui68.entities.boss.attack.aerial.DarkOrbAttack;
import com.Chagui68.entities.boss.attack.aerial.GravityWellAttack;
import com.Chagui68.entities.boss.attack.aerial.HeavenlyJudgmentAttack;
import com.Chagui68.entities.boss.attack.aerial.HoverBarrageAttack;
import com.Chagui68.entities.boss.attack.aerial.LightningStormAttack;
import com.Chagui68.entities.boss.attack.aerial.NovaBurstAttack;
import com.Chagui68.entities.boss.attack.aerial.RainOfLancesAttack;
import com.Chagui68.entities.boss.attack.aerial.SonicBoomAttack;
import com.Chagui68.entities.boss.attack.aerial.StarfallAttack;
import com.Chagui68.entities.boss.attack.aerial.WindCutterAttack;
import com.Chagui68.entities.boss.attack.ground.ArmorSpikesAttack;
import com.Chagui68.entities.boss.attack.ground.ChainGrappleAttack;
import com.Chagui68.entities.boss.attack.ground.DoomBeamAttack;
import com.Chagui68.entities.boss.attack.ground.EarthPillarAttack;
import com.Chagui68.entities.boss.attack.ground.ExecutionerSweepAttack;
import com.Chagui68.entities.boss.attack.ground.GroundShatterAttack;
import com.Chagui68.entities.boss.attack.ground.GroundSlamAttack;
import com.Chagui68.entities.boss.attack.ground.LanceFlurryAttack;
import com.Chagui68.entities.boss.attack.ground.LanceStormAttack;
import com.Chagui68.entities.boss.attack.ground.MirrorImageAttack;
import com.Chagui68.entities.boss.attack.ground.ShieldBashAttack;
import com.Chagui68.entities.boss.attack.ground.VortexPullAttack;
import com.Chagui68.entities.boss.attack.ground.WarStompAttack;
import com.Chagui68.entities.boss.attack.ground.WhirlwindSlashAttack;
import com.Chagui68.entities.boss.attack.ranged.ArcaneMissilesAttack;
import com.Chagui68.entities.boss.attack.ranged.ArcaneOrbAttack;
import com.Chagui68.entities.boss.attack.ranged.ChainLightningAttack;
import com.Chagui68.entities.boss.attack.ranged.CrystalBarrageAttack;
import com.Chagui68.entities.boss.attack.ranged.FrostLanceAttack;
import com.Chagui68.entities.boss.attack.ranged.LanceSnipeAttack;
import com.Chagui68.entities.boss.attack.ranged.LightningSpearAttack;
import com.Chagui68.entities.boss.attack.ranged.MeteorStormAttack;
import com.Chagui68.entities.boss.attack.ranged.ShadowVolleyAttack;
import com.Chagui68.entities.boss.attack.ranged.SpiritBeamAttack;
import com.Chagui68.entities.boss.attack.ranged.VoidBeamAttack;
import com.Chagui68.entities.boss.attack.ranged.VoidRiftAttack;
import com.Chagui68.entities.boss.attack.defensive.AbsorbShieldAttack;
import com.Chagui68.entities.boss.attack.defensive.HealingCircleAttack;
import com.Chagui68.entities.boss.attack.defensive.ReflectBarrierAttack;
import com.Chagui68.entities.boss.attack.defensive.ShieldSealAttack;
import com.Chagui68.entities.boss.attack.defensive.StoneSkinAttack;
import com.Chagui68.entities.boss.attack.defensive.TriangleCallAttack;
import com.Chagui68.MultiverseCreatures;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

import com.Chagui68.entities.BossInstance.ShieldState;
import com.Chagui68.entities.BossInstance.DefenseState;

public class ArmorStandBoss implements Listener, BossHost {

    private final MultiverseCreatures plugin;
    private final Map<UUID, BossInstance> activeBosses = new HashMap<>();
    private final Map<String, BossAttack> attackRegistry = new HashMap<>();
    private final Random random = new Random();
    public static final String TAG = "MSC_ArmorStandBoss";
    public static final String SUMMON_TAG = "MSC_ArmorBossSummoned";
    private static final String BAR_TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL";
    private static final double MAX_PROGRESS = 1.0;
    private static final int PHASES = 5;
    private static final String SHIELD_HOLDER_TAG = "MSC_ShieldHolder";
    private static final double FLY_HEIGHT = 15.0;
    private static final double DIST_CLOSE = 5.0;
    private static final double DIST_MEDIUM = 15.0;

    private double sealDamage;
    private double hoverBarrageDamage;
    private double aggroRange;
    private double maxDamagePerHit;

    public ArmorStandBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExistingBosses();
        initAttacks();
    }

    public void registerAttack(BossAttack attack) {
        attackRegistry.put(attack.getName().toLowerCase(), attack);
    }

    private void initAttacks() {
        // Aerial
        registerAttack(new StarfallAttack(this));
        registerAttack(new AerialRushAttack(this));
        registerAttack(new SonicBoomAttack(this));
        registerAttack(new LightningStormAttack(this));
        registerAttack(new GravityWellAttack(this));
        registerAttack(new CrossSlashAttack(this));
        registerAttack(new NovaBurstAttack(this));
        registerAttack(new DarkOrbAttack(this));
        registerAttack(new WindCutterAttack(this));
        registerAttack(new HeavenlyJudgmentAttack(this));
        registerAttack(new RainOfLancesAttack(this));
        registerAttack(new AirSlamAttack(this));
        registerAttack(new HoverBarrageAttack(this));
        // Ground
        registerAttack(new GroundSlamAttack(this));
        registerAttack(new GroundShatterAttack(this));
        registerAttack(new ShieldBashAttack(this));
        registerAttack(new LanceStormAttack(this));
        registerAttack(new EarthPillarAttack(this));
        registerAttack(new ChainGrappleAttack(this));
        registerAttack(new WarStompAttack(this));
        registerAttack(new ArmorSpikesAttack(this));
        registerAttack(new VortexPullAttack(this));
        registerAttack(new MirrorImageAttack(this));
        registerAttack(new DoomBeamAttack(this));
        registerAttack(new LanceFlurryAttack(this));
        registerAttack(new WhirlwindSlashAttack(this));
        registerAttack(new ExecutionerSweepAttack(this));
        // Ranged
        registerAttack(new LanceSnipeAttack(this));
        registerAttack(new MeteorStormAttack(this));
        registerAttack(new VoidBeamAttack(this));
        registerAttack(new FrostLanceAttack(this));
        registerAttack(new LightningSpearAttack(this));
        registerAttack(new ShadowVolleyAttack(this));
        registerAttack(new ChainLightningAttack(this));
        registerAttack(new CrystalBarrageAttack(this));
        registerAttack(new ArcaneOrbAttack(this));
        registerAttack(new VoidRiftAttack(this));
        registerAttack(new ArcaneMissilesAttack(this));
        registerAttack(new SpiritBeamAttack(this));
        // Defensive
        registerAttack(new StoneSkinAttack(this));
        registerAttack(new ReflectBarrierAttack(this));
        registerAttack(new AbsorbShieldAttack(this));
        registerAttack(new ShieldSealAttack(this));
        registerAttack(new HealingCircleAttack(this));
        registerAttack(new TriangleCallAttack(this));
    }

    public void reloadConfig() {
        this.sealDamage = plugin.getConfig().getDouble("armor-stand-boss.seal-damage", 15.0);
        this.hoverBarrageDamage = plugin.getConfig().getDouble("armor-stand-boss.hover-barrage-damage", 12.0);
        this.aggroRange = plugin.getConfig().getDouble("armor-stand-boss.aggro-range", 50.0);
        this.maxDamagePerHit = plugin.getConfig().getDouble("armor-stand-boss.max-damage-per-hit", 50.0);
    }

    public MultiverseCreatures getPlugin() {
        return plugin;
    }

    public double getSealDamage() {
        return sealDamage;
    }

    public double getHoverBarrageDamage() {
        return hoverBarrageDamage;
    }

    @Override
    public List<Player> getValidPlayers(World world) {
        return BossArena.getValidPlayers(world);
    }

    @Override
    public List<Player> getValidPlayersNear(Location center, double radiusSq) {
        return BossArena.getValidPlayersNear(center, radiusSq);
    }

    @Override
    public void launchPlayer(Player p, double y) {
        BossArena.launchPlayer(p, y);
    }

    private void reloadExistingBosses() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains(SHIELD_HOLDER_TAG)) {
                    entity.remove();
                    continue;
                }
                if (!(entity instanceof ArmorStand stand)) continue;
                if (!stand.getScoreboardTags().contains(TAG)) continue;
                BossInstance instance = new BossInstance(stand);
                activeBosses.put(stand.getUniqueId(), instance);
                setupBossBar(instance);
                startBossAI(instance);
                plugin.getLogger().info("Restarted ArmorStandBoss AI at " + stand.getLocation());
            }
        }
    }

    public boolean trySpawn(Location location) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        if (stand == null) return false;

        double health = plugin.getConfig().getDouble("armor-stand-boss.health", 1000.0) * 1.5;
        AttributeInstance maxHealthAttr = stand.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) maxHealthAttr.setBaseValue(health);
        stand.setHealth(health);
        stand.setInvulnerable(false);

        stand.setCustomName(BAR_TITLE);
        stand.setCustomNameVisible(true);
        stand.setRemoveWhenFarAway(false);
        stand.setPersistent(true);
        stand.setAI(true);
        stand.setCanPickupItems(false);
        stand.setSmall(false);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setGravity(false);

        AttributeInstance scaleAttr = stand.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(7.5);

        stand.setMaximumNoDamageTicks(0);
        stand.addScoreboardTag(TAG);

        EntityEquipment equip = stand.getEquipment();
        equip.setHelmet(createTrimmedNetherite(Material.NETHERITE_HELMET));
        equip.setChestplate(createTrimmedNetherite(Material.NETHERITE_CHESTPLATE));
        equip.setLeggings(createTrimmedNetherite(Material.NETHERITE_LEGGINGS));
        equip.setBoots(createTrimmedNetherite(Material.NETHERITE_BOOTS));

        ItemStack lance = createNetheriteLance();
        equip.setItemInMainHand(lance);

        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta shieldMeta = shield.getItemMeta();
        if (shieldMeta != null) {
            shieldMeta.setUnbreakable(true);
            shield.setItemMeta(shieldMeta);
        }
        equip.setItemInOffHand(shield);

        BossInstance instance = new BossInstance(stand);
        activeBosses.put(stand.getUniqueId(), instance);
        setupBossBar(instance);
        startBossAI(instance);

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(
                    stand.getLocation(),
                    80,
                    12.0,
                    MagicSealListener.Plane.XZ
            );

            instance.wingTask = plugin.getMagicSealListener().spawnWingSeal2(stand);
        }

        return true;
    }

    private ItemStack createTrimmedNetherite(Material material) {
        ItemStack armor = new ItemStack(material);
        ItemMeta meta = armor.getItemMeta();
        if (meta instanceof ArmorMeta armorMeta) {
            try {
                armorMeta.setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SILENCE));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not apply trim on " + material + ": " + e.getMessage());
            }
            armorMeta.setUnbreakable(true);
            armor.setItemMeta(armorMeta);
        }
        return armor;
    }

    public ItemStack createNetheriteLance() {
        ItemStack lance = new ItemStack(Material.NETHERITE_SPEAR);
        ItemMeta meta = lance.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            lance.setItemMeta(meta);
        }
        return lance;
    }

    private void setupBossBar(BossInstance instance) {
        BossBar bar = Bukkit.createBossBar(BAR_TITLE, BarColor.RED, BarStyle.SEGMENTED_6, BarFlag.DARKEN_SKY);
        bar.setProgress(MAX_PROGRESS);
        bar.setVisible(true);

        for (Player p : instance.stand.getWorld().getPlayers()) {
            bar.addPlayer(p);
        }

        instance.bossBar = bar;
    }

    public void triggerSealForPhase(BossInstance instance, int phase) {
    }

    public void skyPentagramAttack(BossInstance instance) {
        if (plugin.getMagicSealListener() == null) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        MagicSealListener seals = plugin.getMagicSealListener();

        final int PENTAGRAM_HEIGHT = 30;
        final int PENTAGRAM_DURATION = 80;
        instance.pentagramCenters.clear();

        for (Player player : world.getPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) continue;
            double rangeSq = player.getLocation().distanceSquared(stand.getLocation());
            if (rangeSq > 10000) continue;

            Location above = player.getLocation().add(0, PENTAGRAM_HEIGHT, 0);
            instance.pentagramCenters.put(player.getUniqueId(), player.getLocation());
            seals.spawnPentagramSeal(above, PENTAGRAM_DURATION, MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                skyPentagramStrike(instance, PENTAGRAM_HEIGHT);
            }
        }.runTaskLater(plugin, PENTAGRAM_DURATION);
    }

    public void skyPentagramStrike(BossInstance instance, int columnHeight) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        double damage = sealDamage;
        final double sealRadius = 6.0;
        final double sealRadiusSq = sealRadius * sealRadius;

        for (var entry : instance.pentagramCenters.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Location base = entry.getValue();
            if (player == null || !player.isOnline() || !player.getWorld().equals(world)) {
                continue;
            }

            for (int y = columnHeight; y >= 0; y -= 2) {
                for (int p = 0; p < 3; p++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double r = random.nextDouble() * sealRadius;
                    Location pl = base.clone().add(
                            Math.cos(angle) * r,
                            y,
                            Math.sin(angle) * r
                    );
                    world.spawnParticle(Particle.EXPLOSION, pl, 1, 0.3, 0.3, 0.3, 0);
                    world.spawnParticle(Particle.FLAME, pl, 3, 0.2, 0.2, 0.2, 0.03);
                    world.spawnParticle(Particle.CRIT, pl, 2, 0.2, 0.5, 0.2, 0.05);
                }
            }

            world.spawnParticle(Particle.CLOUD, base.clone().add(0, 1, 0), 20, 1.5, 0.2, 1.5, 0.1);
            world.spawnParticle(Particle.EXPLOSION, base.clone().add(0, 1, 0), 5, 2.0, 0.5, 2.0, 0.1);
            world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);

            double distSq = player.getLocation().distanceSquared(base);
            if (distSq <= sealRadiusSq) {
                MscEntityUtils.damageBy(stand, player, damage);
                player.setVelocity(player.getVelocity().add(new org.bukkit.util.Vector(0, 0.4, 0)));
            }
        }

        instance.pentagramCenters.clear();
    }

    private BarColor getPhaseColor(int phase) {
        return switch (phase) {
            case 0, 1 -> BarColor.RED;
            case 2 -> BarColor.YELLOW;
            case 3 -> BarColor.GREEN;
            case 4 -> BarColor.BLUE;
            default -> BarColor.RED;
        };
    }

    private String getPhaseTitle(int phase) {
        return switch (phase) {
            case 0 ->
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0\u25a0\u25a0\u25a0\u25a0";
            case 1 ->
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0\u25a0\u25a0\u25a0" + ChatColor.GRAY + "\u25a0";
            case 2 ->
                    ChatColor.YELLOW + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0\u25a0\u25a0" + ChatColor.GRAY + "\u25a0\u25a0";
            case 3 ->
                    ChatColor.GREEN + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0\u25a0" + ChatColor.GRAY + "\u25a0\u25a0\u25a0";
            case 4 ->
                    ChatColor.BLUE + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0" + ChatColor.GRAY + "\u25a0\u25a0\u25a0\u25a0";
            default ->
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL " + ChatColor.RED + "\u25a0\u25a0\u25a0\u25a0\u25a0";
        };
    }

    private void updatePhase(BossInstance instance) {
        BossPuppet stand = instance.stand;
        AttributeInstance maxHealthAttr = stand.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 500.0;
        double currentHealth = stand.getHealth();
        double healthPercent = currentHealth / maxHealth;

        int newPhase;
        if (healthPercent > 0.8) newPhase = 0;
        else if (healthPercent > 0.6) newPhase = 1;
        else if (healthPercent > 0.4) newPhase = 2;
        else if (healthPercent > 0.2) newPhase = 3;
        else newPhase = 4;

        if (newPhase < instance.currentPhase) {
            newPhase = instance.currentPhase;
        }

        if (newPhase != instance.currentPhase) {
            int oldPhase = instance.currentPhase;
            instance.currentPhase = newPhase;
            if (instance.bossBar != null) {
                instance.bossBar.setTitle(getPhaseTitle(newPhase));
                instance.bossBar.setColor(getPhaseColor(newPhase));
                instance.bossBar.setProgress(healthPercent);
            }
            instance.stand.getWorld().playSound(instance.stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
            triggerSealForPhase(instance, newPhase);

            if (oldPhase == 0 && newPhase == 1) {
                phaseTransitionRage(instance);
            } else if (oldPhase == 1 && newPhase == 2) {
                phaseTransitionBarrier(instance);
            } else if (oldPhase == 2 && newPhase == 3) {
                phaseTransitionStorm(instance);
            } else if (oldPhase == 3 && newPhase == 4) {
                phaseTransitionDespair(instance);
            }
        }

        if (instance.invulnerableTimer > 0) {
            instance.invulnerableTimer--;
            if (instance.invulnerableTimer == 0) {
                instance.invulnerable = false;
            }
        }

        if (instance.activeDefense != DefenseState.NONE) {
            instance.defenseTimer++;
            boolean expired = false;
            switch (instance.activeDefense) {
                case STONE_SKIN -> {
                    if (instance.defenseTimer >= 200) expired = true;
                }
                case REFLECT_BARRIER -> {
                    if (instance.defenseTimer >= 160) expired = true;
                }
                case ABSORB_SHIELD -> {
                    if (instance.defenseTimer >= 300 || instance.absorbShieldHealth <= 0) expired = true;
                }
            }
            if (expired) {
                instance.activeDefense = DefenseState.NONE;
                instance.defenseTimer = 0;
                if (instance.defenseTask != null) {
                    instance.defenseTask.cancel();
                    instance.defenseTask = null;
                }
            }
        }
    }

    private void startBossAI(BossInstance instance) {
        new BukkitRunnable() {
            @Override
            public void run() {
                BossPuppet stand = instance.stand;

                if (stand.isDead() || !stand.isValid()) {
                    cleanupShield(instance);
                    stopBossMusic(instance, true);
                    if (instance.bossBar != null) {
                        instance.bossBar.removeAll();
                        instance.bossBar.setVisible(false);
                    }
                    activeBosses.remove(stand.getUniqueId());
                    cancel();
                    return;
                }

                if (instance.bossBar == null || !instance.bossBar.isVisible()) {
                    setupBossBar(instance);
                }

                updatePhase(instance);

                syncBossBarPlayers(instance);
                instance.bossBar.setProgress(Math.max(0.0, stand.getHealth() / (stand.getAttribute(Attribute.MAX_HEALTH) != null ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0)));

                updateBossMusic(instance, stand.getLocation());

                if (instance.hoverBarrageActive) {
                    instance.hoverBarrageTicks++;
                    if (instance.hoverBarrageTicks > 800) {
                        if (instance.hoverBarrageTask != null) {
                            instance.hoverBarrageTask.cancel();
                            instance.hoverBarrageTask = null;
                        }
                        instance.hoverBarrageActive = false;
                        instance.hoverBarrageTicks = 0;
                        stand.getWorld().spawnParticle(Particle.CLOUD, stand.getLocation(), 20, 1, 1, 1, 0.1);
                    }
                } else {
                    instance.hoverBarrageTicks = 0;
                }

                if (instance.flyTask != null) {
                    double currentY = stand.getLocation().getY();
                    if (Math.abs(currentY - instance.lastAirY) > 0.001) {
                        instance.airStuckTicks = 0;
                        instance.lastAirY = currentY;
                    } else {
                        instance.airStuckTicks++;
                        if (instance.airStuckTicks > 60) {
                            instance.flyTask.cancel();
                            instance.flyTask = null;
                            instance.isFlying = false;
                            instance.flyingTimer = 0;
                            instance.airStuckTicks = 0;
                        }
                    }
                } else {
                    instance.airStuckTicks = 0;
                    instance.lastAirY = stand.getLocation().getY();
                }

                if (instance.healingCircleActive || instance.shieldSealActive) {
                    if (instance.shieldSealActive) {
                        attackRegistry.get("groundslam").execute(instance);
                        instance.shieldSealTimer++;
                        if (!instance.triangleCallActive && !instance.hoverBarrageActive
                                && instance.shieldSealTimer > 40 && instance.shieldSealTimer % 100 == 0) {
                            attackRegistry.get("trianglecall").execute(instance);
                        }
                    }
                } else {
                    attackRegistry.get("groundslam").execute(instance);

                    if (instance.isFlying) {
                        instance.flyingTimer++;

                        if (!instance.hoverBarrageActive && !instance.triangleCallActive
                                && instance.flyingTimer > 40 && instance.flyingTimer % 80 == 0) {
                            stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                            executeRandomAerialAttack(instance);
                        }

                        boolean allAerialDone = instance.aerialAttacksDone.size() >= 10;
                        int minFlyTime = 200 + random.nextInt(200);
                        if (!instance.hoverBarrageActive && !instance.triangleCallActive
                                && ((allAerialDone && instance.flyingTimer >= minFlyTime) || instance.flyingTimer >= 800)) {
                            if (random.nextBoolean()) {
                                land(instance);
                            } else {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.3f);
                                airSlam(instance, true);
                            }
                        }
                    } else if (!isOnGround(stand)) {
                        if (instance.flyTask == null && !instance.hoverBarrageActive) {
                            Location loc = stand.getLocation();
                            double groundY = getGroundY(loc, 80);
                            if (loc.getY() - groundY > 0.3) {
                                loc.setY(Math.max(groundY, loc.getY() - 0.8));
                                stand.teleport(loc);
                                stand.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.5, 0.1, 0.5, 0.02);
                            } else {
                                loc.setY(groundY);
                                stand.teleport(loc);
                            }
                        }
                    } else if (instance.shieldState == ShieldState.NORMAL) {
                        instance.hoverBarrageCooldown++;
                        instance.groundAttackCooldown++;
                        if (instance.hoverBarrageCooldown >= 240 + random.nextInt(120)) {
                            instance.hoverBarrageCooldown = 0;

                            double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                                    ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0;
                            double healthPct = stand.getHealth() / maxHealth;
                            int choice = random.nextInt(100);

                            if (healthPct < 0.4 && choice < 25) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                attackRegistry.get("healingcircle").execute(instance);
                            } else if (choice < 15) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                flyUp(instance);
                            } else if (choice < 35) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.7f);
                                attackRegistry.get("shieldseal").execute(instance);
                            } else if (choice < 55) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                executeRandomGroundAttack(instance);
                            } else {
                                startHoverBarrage(instance);
                            }
                        } else if (instance.defenseCooldown > 0) {
                            instance.defenseCooldown--;
                        } else if (instance.groundAttackCooldown >= 40 + random.nextInt(40)) {
                            instance.groundAttackCooldown = 0;
                            double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                                    ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0;
                            double healthPct = stand.getHealth() / maxHealth;
                            if (instance.activeDefense == DefenseState.NONE && healthPct < 0.5 && random.nextInt(100) < 30) {
                                int defChoice = random.nextInt(100);
                                if (defChoice < 35) {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("stoneskin").execute(instance);
                                } else if (defChoice < 65) {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("reflectbarrier").execute(instance);
                                } else {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("absorbshield").execute(instance);
                                }
                                instance.defenseCooldown = 300 + random.nextInt(300);
                            } else {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                executeRandomGroundAttack(instance);
                            }
                        }
                    }
                }

                Player target = detectTarget(stand);
                if (target != null) {
                    Location current = stand.getLocation();
                    Location targetLoc = target.getLocation();
                    current.setDirection(targetLoc.toVector().subtract(current.toVector()));
                    stand.teleport(current);

                    double dxz = Math.sqrt(current.distanceSquared(targetLoc));
                    if (dxz > 0.5) {
                        double bossEyeY = current.getY() + 10;
                        double targetEyeY = targetLoc.getY() + 1.6;
                        double dy = bossEyeY - targetEyeY;
                        double headPitch = Math.atan2(dy, dxz);
                        stand.setHeadPose(new EulerAngle(
                                Math.max(-0.78, Math.min(0.78, headPitch)), 0, 0
                        ));
                    }
                }

                boolean hasPlayer = countPlayersInRange(stand.getLocation(), 100) > 0;
                if (hasPlayer) {
                    instance.noPlayerTicks = 0;
                } else {
                    instance.noPlayerTicks++;
                    if (instance.noPlayerTicks >= 1) {
                        cleanupShield(instance);
                        stopBossMusic(instance, true);
                        if (instance.bossBar != null) {
                            instance.bossBar.removeAll();
                            instance.bossBar.setVisible(false);
                        }
                        activeBosses.remove(stand.getUniqueId());
                        stand.remove();
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void resetBossPose(BossInstance instance) {
        if (instance.stand == null || !instance.stand.isValid()) return;
        instance.stand.setRightArmPose(new org.bukkit.util.EulerAngle(0, 0, 0));
        instance.stand.setLeftArmPose(new org.bukkit.util.EulerAngle(0, 0, 0));
        instance.stand.setBodyPose(new org.bukkit.util.EulerAngle(0, 0, 0));
        instance.stand.setHeadPose(new org.bukkit.util.EulerAngle(0, 0, 0));
        instance.stand.setRightLegPose(new org.bukkit.util.EulerAngle(0, 0, 0));
        instance.stand.setLeftLegPose(new org.bukkit.util.EulerAngle(0, 0, 0));
    }

    private void phaseTransitionRage(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 5, 0), 30, 3, 3, 3, 0);
        world.spawnParticle(Particle.FLAME, loc.clone().add(0, 5, 0), 60, 4, 4, 4, 0.08);
        world.spawnParticle(Particle.SMOKE, loc.clone().add(0, 5, 0), 40, 3, 5, 3, 0.1);

        stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(90), Math.toRadians(0)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-90), Math.toRadians(0)));
        stand.setBodyPose(new EulerAngle(Math.toRadians(-15), 0, 0));
        stand.setHeadPose(new EulerAngle(Math.toRadians(-30), 0, 0));

        for (Player p : getValidPlayers(world)) {
            double dist = p.getLocation().distance(loc);
            if (dist < 30) {
                Vector away = p.getLocation().toVector().subtract(loc.toVector()).normalize();
                p.setVelocity(away.multiply(2.0).setY(1.0));
                MscEntityUtils.damageBy(stand, p, 10.0);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
            }
        }

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(loc.clone().add(0, 5, 0), 60, 8.0, MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                resetBossPose(instance);
            }
        }.runTaskLater(plugin, 40L);
    }

    private void phaseTransitionBarrier(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        instance.invulnerable = true;
        instance.invulnerableTimer = 100;

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.3f);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.8f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 5, 0), 1,
                Color.WHITE);
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 5, 0), 50, 5, 5, 5, 0);
        for (int i = 0; i < 40; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = 5 + random.nextDouble() * 5;
            double x = loc.getX() + Math.cos(angle) * r;
            double z = loc.getZ() + Math.sin(angle) * r;
            double y = loc.getY() + 2 + random.nextDouble() * 8;
            Location pl = new Location(world, x, y, z);
            world.spawnParticle(Particle.END_ROD, pl, 3, 0.2, 0.2, 0.2, 0.02);
            world.spawnParticle(Particle.DUST, pl, 2, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0x88CCFF), 2.0f));
        }

        stand.setRightArmPose(new EulerAngle(Math.toRadians(-110), Math.toRadians(45), Math.toRadians(10)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-110), Math.toRadians(-45), Math.toRadians(-10)));
        stand.setBodyPose(new EulerAngle(Math.toRadians(15), 0, 0));
        stand.setHeadPose(new EulerAngle(Math.toRadians(-15), 0, 0));

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnInvulnerabilityAura(stand.getLocation().clone().add(0, 7, 0), 100);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                resetBossPose(instance);
            }
        }.runTaskLater(plugin, 40L);
    }

    private void phaseTransitionStorm(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.6f);
        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 5, 0), 1,
                Color.WHITE);

        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = 2 + random.nextDouble() * 12;
            double x = loc.getX() + Math.cos(angle) * r;
            double z = loc.getZ() + Math.sin(angle) * r;
            Location strike = new Location(world, x, loc.getY(), z);
            world.strikeLightningEffect(strike);
        }

        stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(20)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-20)));
        stand.setBodyPose(new EulerAngle(0, 0, Math.toRadians(10)));
        stand.setHeadPose(new EulerAngle(Math.toRadians(-30), 0, 0));

        double dmg = sealDamage;
        for (Player p : getValidPlayers(world)) {
            double dist = p.getLocation().distance(loc);
            if (dist < 25) {
                MscEntityUtils.damageBy(stand, p, dmg * 0.5 * (1 - dist / 25));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
            }
        }

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnStormSeal(loc.clone().add(0, 5, 0), 80);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                resetBossPose(instance);
            }
        }.runTaskLater(plugin, 30L);
    }

    private void phaseTransitionDespair(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        instance.invulnerable = true;
        instance.invulnerableTimer = 80;

        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.3f);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 5, 0), 1,
                Color.WHITE);
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 5, 0), 80, 8, 8, 8, 0);
        world.spawnParticle(Particle.SOUL, loc.clone().add(0, 5, 0), 100, 6, 6, 6, 0.1);
        world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 5, 0), 80, 5, 5, 5, 0.05);

        stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(30)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-30)));
        stand.setBodyPose(new EulerAngle(Math.toRadians(-20), 0, 0));
        stand.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, 0));

        double dmg = sealDamage * 1.5;
        for (Player p : getValidPlayers(world)) {
            double dist = p.getLocation().distance(loc);
            if (dist < 35) {
                MscEntityUtils.damageBy(stand, p, dmg * (1 - dist / 35));
                p.setVelocity(new Vector(0, 1.5, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            }
        }

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(loc.clone().add(0, 5, 0), 100, 10.0, MagicSealListener.Plane.XZ);
            plugin.getMagicSealListener().spawnVortexSeal(loc.clone().add(0, 2, 0), 80);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                resetBossPose(instance);
            }
        }.runTaskLater(plugin, 50L);
    }

    public void startHoverBarrage(BossInstance instance) {
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
            final List<XMark> xMarks = new ArrayList<>();
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
                        targets.addAll(getValidPlayersNear(stand.getLocation(), 10000));
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
                        xMarks.add(new XMark(xOrigin, t));
                        xMarksFired++;
                    }

                    Iterator<XMark> it = xMarks.iterator();
                    XMark tracingX = null;
                    while (it.hasNext()) {
                        XMark x = it.next();
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
                        double groundY = getGroundY(startLoc, 80);
                        double newY = loc.getY() - 0.5;
                        if (newY <= groundY) {
                            startLoc.setY(groundY);
                            stand.teleport(startLoc);
                            stand.getWorld().spawnParticle(Particle.CLOUD, startLoc, 20, 1, 0.5, 1, 0.1);
                            stand.getWorld().playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f);
                            resetBossPose(instance);
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

    private void flyUp(BossInstance instance) {
        flyUp(instance, true);
    }

    public void flyUp(BossInstance instance, boolean telegraph) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();

        instance.groundY = getGroundY(stand.getLocation(), 80);
        instance.isFlying = true;
        instance.flyingTimer = 0;
        instance.aerialAttacksDone.clear();

        if (instance.flyTask != null) {
            instance.flyTask.cancel();
            instance.flyTask = null;
        }

        double startY = instance.groundY;
        double targetY = startY + FLY_HEIGHT;

        instance.flyTask = new BukkitRunnable() {
            int ticks = 0;
            boolean windup = telegraph;
            final int WINDUP_DURATION = 20;
            final int ASCEND_DURATION = 30;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    instance.flyTask = null;
                    cancel();
                    return;
                }

                if (windup) {
                    ticks++;
                    Location loc = stand.getLocation();
                    double phase = Math.min(1.0, (double) ticks / WINDUP_DURATION);

                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(20 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-20 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));

                    double ringR = 1.5 + phase * 3.0;
                    for (int a = 0; a < 12; a++) {
                        double angle = (2 * Math.PI * a / 12) + ticks * 0.08;
                        double x = loc.getX() + Math.cos(angle) * ringR;
                        double z = loc.getZ() + Math.sin(angle) * ringR;
                        Location pl = new Location(world, x, loc.getY(), z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x88DDFF), 1.8f * (float) phase));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }

                    for (int i = 0; i < (int) (3 + phase * 6); i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double r = random.nextDouble() * 2.0 * phase;
                        double x = loc.getX() + Math.cos(angle) * r;
                        double z = loc.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, loc.getY() + 0.1, z);
                        world.spawnParticle(Particle.CLOUD, pl, 1, 0, 0, 0, 0);
                    }

                    world.spawnParticle(Particle.CLOUD, loc.clone().add(0, -0.5, 0), 5, 1.0, 0.2, 1.0, 0.03);
                    world.spawnParticle(Particle.END_ROD, loc, (int) (2 + phase * 4), 0.5, 0.1, 0.5, 0.02);

                    if (ticks == 1) {
                        world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.7f, 1.2f);
                    }
                    if (ticks % 5 == 0 && ticks > 0) {
                        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f * (float) phase, 0.5f);
                    }

                    if (ticks >= WINDUP_DURATION) {
                        windup = false;
                        ticks = 0;
                        resetBossPose(instance);
                        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
                    }
                    return;
                }

                if (ticks >= ASCEND_DURATION) {
                    Location loc = stand.getLocation();
                    loc.setY(targetY);
                    stand.teleport(loc);
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
                    world.spawnParticle(Particle.CLOUD, loc, 30, 2.0, 0.5, 2.0, 0.1);
                    instance.flyTask = null;
                    cancel();
                    return;
                }
                ticks++;
                Location loc = stand.getLocation();
                double progress = (double) ticks / ASCEND_DURATION;
                double newY = startY + (targetY - startY) * progress;
                loc.setY(newY);
                stand.teleport(loc);
                world.spawnParticle(Particle.CLOUD, loc, 4, 0.5, 0.1, 0.5, 0.02);
                world.spawnParticle(Particle.END_ROD, loc, 2, 0.3, 0.3, 0.3, 0.01);
            }
        };
        instance.flyTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void land(BossInstance instance) {
        land(instance, true);
    }

    public void land(BossInstance instance, boolean telegraph) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();

        instance.isFlying = false;
        instance.flyingTimer = 0;
        instance.aerialAttacksDone.clear();

        if (instance.flyTask != null) {
            instance.flyTask.cancel();
            instance.flyTask = null;
        }

        double targetY = getGroundY(stand.getLocation(), 80);

        instance.flyTask = new BukkitRunnable() {
            int ticks = 0;
            boolean windup = telegraph;
            final int WINDUP_DURATION = 18;
            final int DESCEND_DURATION = 25;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    instance.flyTask = null;
                    cancel();
                    return;
                }

                Location loc = stand.getLocation();

                if (windup) {
                    ticks++;
                    double phase = Math.min(1.0, (double) ticks / WINDUP_DURATION);

                    stand.setRightArmPose(new EulerAngle(Math.toRadians(20 * phase), 0, 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(20 * phase), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(15 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(8 * phase), 0, 0));

                    double ringR = 1.0 + phase * 2.5;
                    for (int a = 0; a < 10; a++) {
                        double angle = (2 * Math.PI * a / 10) + ticks * 0.1;
                        double x = loc.getX() + Math.cos(angle) * ringR;
                        double z = loc.getZ() + Math.sin(angle) * ringR;
                        Location pl = new Location(world, x, loc.getY() - 0.5, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFFAA44), 1.5f * (float) phase));
                        world.spawnParticle(Particle.CLOUD, pl, 1, 0, 0, 0, 0);
                    }

                    world.spawnParticle(Particle.CLOUD, loc.clone().add(0, -0.5, 0), (int) (4 + phase * 8), 1.0, 0.2, 1.0, 0.05);

                    if (ticks == 1) {
                        world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.7f, 1.0f);
                    }
                    if (ticks % 4 == 0) {
                        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f - (float) ticks * 0.02f);
                    }

                    if (ticks >= WINDUP_DURATION) {
                        windup = false;
                        ticks = 0;
                        resetBossPose(instance);
                    }
                    return;
                }

                if (ticks >= DESCEND_DURATION || loc.getY() - 0.5 <= targetY) {
                    loc.setY(targetY);
                    stand.teleport(loc);
                    resetBossPose(instance);
                    world.spawnParticle(Particle.CLOUD, loc, 30, 2.0, 0.5, 2.0, 0.1);
                    world.spawnParticle(Particle.EXPLOSION, loc, 5, 1.0, 0.3, 1.0, 0);
                    for (int a = 0; a < 18; a++) {
                        double angle = (2 * Math.PI * a / 18);
                        double x = loc.getX() + Math.cos(angle) * 6;
                        double z = loc.getZ() + Math.sin(angle) * 6;
                        Location pl = new Location(world, x, loc.getY() + 0.2, z);
                        world.spawnParticle(Particle.CLOUD, pl, 2, 0.2, 0.2, 0.2, 0.02);
                    }
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.7f);
                    instance.flyTask = null;
                    cancel();
                    return;
                }
                ticks++;
                double newY = Math.max(targetY, loc.getY() - 0.5);
                loc.setY(newY);
                stand.teleport(loc);
                world.spawnParticle(Particle.CLOUD, loc, 3, 0.3, 0.1, 0.3, 0.02);
            }
        };
        instance.flyTask.runTaskTimer(plugin, 0L, 1L);
    }

    public double getNearestPlayerDistance(Location loc) {
        return BossArena.getNearestPlayerDistance(loc);
    }

    @Override
    public double getGroundY(Location loc, double maxScan) {
        return BossArena.getGroundY(loc, maxScan);
    }

    private enum DistCategory {CLOSE, MEDIUM, FAR}

    private void executeRandomAerialAttack(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;

        double nearestDist = getNearestPlayerDistance(stand.getLocation());
        String[] allAerial;
        if (nearestDist < 15) {
            allAerial = new String[]{"aerialrush", "crossslash", "novaburst"};
        } else if (nearestDist < 35) {
            allAerial = new String[]{"sonicboom", "windcutter", "gravitywell", "darkorb", "aerialrush"};
        } else {
            allAerial = new String[]{"starfall", "lightningstorm", "heavenlyjudgment", "darkorb"};
        }

        List<String> available = new ArrayList<>();
        for (String a : allAerial) {
            if (!instance.aerialAttacksDone.contains(a)) available.add(a);
        }
        if (available.isEmpty()) {
            available.addAll(Arrays.asList(allAerial));
        }

        String choice = available.get(random.nextInt(available.size()));
        instance.aerialAttacksDone.add(choice);

        BossAttack attack = attackRegistry.get(choice);
        if (attack != null) attack.execute(instance);
    }

    private void executeRandomGroundAttack(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;

        double nearestDist = getNearestPlayerDistance(stand.getLocation());

        String[] closeAttacks = {"shieldbash", "warstomp", "chaingrapple", "armorspikes", "mirrorimage", "vortexpull", "groundshatter",
                "lanceflurry", "whirlwindslash", "executionsweep"};
        String[] mediumAttacks = {"lancestorm", "earthpillar", "groundshatter", "groundshatter", "armorspikes", "vortexpull",
                "lanceflurry", "whirlwindslash"};
        String[] farAttacks = {"shieldbash"};

        String choice;
        if (nearestDist < DIST_CLOSE) {
            choice = closeAttacks[random.nextInt(closeAttacks.length)];
        } else if (nearestDist < DIST_MEDIUM) {
            if (random.nextInt(100) < 15) {
                executeRangedAttack(instance);
                return;
            }
            choice = mediumAttacks[random.nextInt(mediumAttacks.length)];
        } else {
            if (random.nextInt(100) < 85) {
                executeRangedAttack(instance);
                return;
            }
            choice = farAttacks[random.nextInt(farAttacks.length)];
        }

        BossAttack attack = attackRegistry.get(choice);
        if (attack != null) attack.execute(instance);
    }

    private void executeRangedAttack(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;

        String[] rangedAttacks = {"lancesnipe", "meteorstorm", "voidbeam", "frostlance", "lightningspear",
                "shadowvolley", "chainlightning", "crystalbarrage", "arcaneorb", "voidrift",
                "arcanemissiles", "spiritbeam"};
        String choice = rangedAttacks[random.nextInt(rangedAttacks.length)];

        BossAttack attack = attackRegistry.get(choice);
        if (attack != null) attack.execute(instance);
    }

    public void airSlam(BossInstance instance, boolean telegraph) {
        executeAttack("airslam", instance, telegraph);
    }

    private ArmorStand getBossStand(World world) {
        for (BossInstance instance : activeBosses.values()) {
            if (instance.stand.getWorld().equals(world)) {
                return instance.stand;
            }
        }
        return null;
    }

    public void spawnShockwaveWave(World world, Location center, double maxRadius) {
        final int ringCount = 10;
        final double ringSpacing = maxRadius / ringCount;
        final int ticksPerRing = 3;

        Location impactLoc = center.clone();
        impactLoc.setY(impactLoc.getY() + 0.1);

        final ArmorStand stand = getBossStand(world);

        for (int i = 0; i < ringCount; i++) {
            final int ringIndex = i;
            final double radius = ringIndex * ringSpacing;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (radius <= 0.5) return;
                    int samples = (int) Math.max(16, radius * 4);
                    for (int a = 0; a < samples; a++) {
                        double angle = (2 * Math.PI * a / samples);
                        double x = impactLoc.getX() + Math.cos(angle) * radius;
                        double z = impactLoc.getZ() + Math.sin(angle) * radius;
                        Location pl = new Location(world, x, impactLoc.getY(), z);

                        world.spawnParticle(Particle.BLOCK, pl, 12, 0.3, 0.6, 0.3, 0.15,
                                org.bukkit.Material.DIRT.createBlockData());
                        world.spawnParticle(Particle.DUST, pl, 2, 0.3, 0.4, 0.3, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFF6622), 2.5f));
                        world.spawnParticle(Particle.CLOUD, pl, 1, 0.3, 0.5, 0.3, 0.06);

                        if (a % 4 == 0) {
                            spawnRisingBlock(world, pl.clone());
                        }

                        Vector knockbackStrength = new Vector(0, 0.7 - radius / maxRadius * 0.3, 0);
                        double damageMultiplier = 1.0 - radius / maxRadius * 0.6;

                        for (Player p : getValidPlayers(world)) {
                            double dist = p.getLocation().distance(pl);
                            if (dist < 2.0 && p.getLocation().getY() <= pl.getY() + 2) {
                            if (stand != null) MscEntityUtils.damageBy(stand, p, damageMultiplier * 5.0);
                                p.setVelocity(p.getVelocity().add(knockbackStrength.clone()));
                            }
                        }
                    }

                    world.playSound(new Location(world,
                                    impactLoc.getX() + radius, impactLoc.getY(), impactLoc.getZ()),
                            Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f + (float) (ringIndex * 0.04f));
                }
            }.runTaskLater(plugin, (long) i * ticksPerRing);
        }
    }

    public void spawnRisingBlock(World world, Location origin) {
        Location spawnLoc = origin.clone();
        spawnLoc.setY(origin.getY() - 0.5);

        org.bukkit.block.Block blockBelow = spawnLoc.getBlock();
        if (!blockBelow.getType().isSolid() && !blockBelow.getType().isAir()) return;
        if (blockBelow.getType().isAir()) return;

        org.bukkit.entity.FallingBlock fb = world.spawnFallingBlock(spawnLoc, blockBelow.getBlockData());
        fb.setDropItem(false);
        fb.setGravity(true);

        fb.setVelocity(new Vector(
                (Math.random() - 0.5) * 0.4,
                0.6 + Math.random() * 0.3,
                (Math.random() - 0.5) * 0.4
        ));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fb.isValid()) fb.remove();
            }
        }.runTaskLater(plugin, 12L);

        world.spawnParticle(Particle.BLOCK, spawnLoc, 6, 0.2, 0.1, 0.2, 0.1,
                blockBelow.getBlockData());
    }



    // ============== NEW GROUND ATTACKS (10) ==============

    // ============== NEW AERIAL ATTACKS (10) ==============

    // ============== NEW RANGED ATTACKS (10) ==============

    public class XMark {
        final Location pos;
        final Player target;
        final Vector direction;
        final Vector right;
        final Vector up;
        final List<Vector> localPoints = new ArrayList<>();
        int tick = 0;
        boolean tracing = true;
        boolean done = false;
        static final double X_SIZE = 3.0;

        public XMark(Location origin, Player target) {
            this.pos = origin.clone();
            this.target = target;

            Vector toTarget = target.getLocation().toVector().subtract(origin.toVector());
            if (toTarget.lengthSquared() < 0.01) toTarget = new Vector(0, 0, 1);
            direction = toTarget.normalize();

            Vector dirH = direction.clone();
            dirH.setY(0);
            if (dirH.lengthSquared() < 0.01) dirH = new Vector(0, 0, 1);
            dirH.normalize();

            right = dirH.clone().crossProduct(new Vector(0, 1, 0)).normalize();
            up = new Vector(0, 1, 0);

            double half = X_SIZE / 2;
            for (int i = 0; i <= 7; i++) {
                double t = (double) i / 7;
                localPoints.add(new Vector(-half + t * X_SIZE, half - t * X_SIZE, 0));
            }
            for (int i = 0; i <= 7; i++) {
                double t = (double) i / 7;
                localPoints.add(new Vector(half - t * X_SIZE, half - t * X_SIZE, 0));
            }
        }

        public boolean isDone() {
            return done;
        }

        public boolean isTracing() {
            return tracing && !done && tick <= localPoints.size();
        }

        public Vector getCurrentLocalPoint() {
            if (!tracing || done) return null;
            int idx = Math.min(tick - 1, localPoints.size() - 1);
            if (idx < 0) return null;
            return localPoints.get(idx);
        }

        public void update() {
            if (done) return;

            if (tracing) {
                if (tick > localPoints.size()) {
                    tracing = false;
                    tick = 0;
                    return;
                }
                for (int i = 0; i < Math.min(tick, localPoints.size()); i++) {
                    spawnPoint(i, Particle.END_ROD, Particle.GLOW_SQUID_INK);
                }
                tick++;
            } else {
                pos.add(direction.clone().multiply(1.5));
                tick++;
                for (int i = 0; i < localPoints.size(); i++) {
                    spawnPoint(i, Particle.END_ROD, Particle.CRIT);
                }
                if (tick > 40 || pos.distanceSquared(target.getLocation()) < 9
                        || pos.clone().subtract(0, 0.3, 0).getBlock().getType().isSolid()) {
                    impact();
                    done = true;
                }
            }
        }

        void spawnPoint(int index, Particle a, Particle b) {
            Vector lp = localPoints.get(index);
            Location pLoc = pos.clone().add(
                    right.clone().multiply(lp.getX()).add(up.clone().multiply(lp.getY()))
            );
            pLoc.getWorld().spawnParticle(a, pLoc, 1, 0, 0, 0, 0);
            pLoc.getWorld().spawnParticle(b, pLoc, 1, 0.03, 0.03, 0.03, 0);
        }

        void impact() {
            World w = pos.getWorld();
            w.spawnParticle(Particle.EXPLOSION, pos, 3, 0.5, 0.5, 0.5, 0);
            w.spawnParticle(Particle.CLOUD, pos, 15, 1.5, 0.5, 1.5, 0.1);
            w.spawnParticle(Particle.FLAME, pos, 10, 0.5, 0.5, 0.5, 0.05);
            w.spawnParticle(Particle.CRIT, pos, 20, 1, 1, 1, 0.1);
            w.playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

            final ArmorStand stand = getBossStand(w);

            double damage = hoverBarrageDamage;
            double radius = 4.0;
            for (Player p : getValidPlayers(w)) {
                if (p.getLocation().distanceSquared(pos) <= radius * radius) {
                    if (stand != null) MscEntityUtils.damageBy(stand, p, damage);
                    p.setVelocity(p.getVelocity().add(new Vector(0, 0.5, 0)));
                }
            }
        }
    }


    public int getShieldPlantInterval() {
        return 300 + random.nextInt(100);
    }

    public int getShieldRetrieveDelay(int phase) {
        return switch (phase) {
            case 0 -> 80;
            case 1 -> 90;
            case 2 -> 100;
            case 3 -> 110;
            case 4 -> 120;
            default -> 80;
        };
    }

    private void cleanupShield(BossInstance instance) {
        if (instance.groundSlamTask != null) {
            instance.groundSlamTask.cancel();
            instance.groundSlamTask = null;
        }
        if (instance.wingTask != null) {
            instance.wingTask.cancel();
            instance.wingTask = null;
        }
        if (instance.floatingShieldTask != null) {
            instance.floatingShieldTask.cancel();
            instance.floatingShieldTask = null;
        }
        if (instance.hoverBarrageTask != null) {
            instance.hoverBarrageTask.cancel();
            instance.hoverBarrageTask = null;
        }
        if (instance.triangleCallTask != null) {
            instance.triangleCallTask.cancel();
            instance.triangleCallTask = null;
        }
        if (instance.flyTask != null) {
            instance.flyTask.cancel();
            instance.flyTask = null;
        }
        if (instance.shieldSealTask != null) {
            instance.shieldSealTask.cancel();
            instance.shieldSealTask = null;
        }
        if (instance.healingCircleTask != null) {
            instance.healingCircleTask.cancel();
            instance.healingCircleTask = null;
        }
        if (instance.shieldHolder != null && instance.shieldHolder.isValid()) {
            instance.shieldHolder.remove();
        }
        for (ItemDisplay d : instance.shieldSealDisplays) {
            if (d.isValid()) d.remove();
        }
        instance.shieldSealDisplays.clear();
        instance.hoverBarrageActive = false;
        instance.triangleCallActive = false;
        instance.isFlying = false;
        instance.flyingTimer = 0;
        instance.aerialAttacksDone.clear();
        instance.shieldSealActive = false;
        instance.shieldSealTimer = 0;
        instance.healingCircleActive = false;
        instance.healingCircleTimer = 0;
        instance.healingCircleHealed = 0;
        instance.shieldHolder = null;
        instance.shieldState = ShieldState.NORMAL;
    }

    private void updateBossMusic(BossInstance instance, Location bossLoc) {
        instance.bossMusicTick++;
        if (instance.bossMusicTick % 10 != 0) return;

        if (plugin.getMusicManager() == null) return;

        final double MUSIC_RANGE = 100.0;
        List<UUID> currentListeners = new ArrayList<>();

        for (Player p : getValidPlayersNear(bossLoc, MUSIC_RANGE * MUSIC_RANGE)) {
            if (!p.getWorld().equals(bossLoc.getWorld())) continue;
            if (plugin.getMusicManager().isPlaying(p)) {
                if (!instance.bossMusicListeners.contains(p.getUniqueId())) {
                    currentListeners.add(p.getUniqueId());
                }
                continue;
            }

            try {
                plugin.getMusicManager().play("Undertale-Megalovania", p, true);
                currentListeners.add(p.getUniqueId());
            } catch (Exception ignored) {
            }
        }

        if (!currentListeners.isEmpty()) {
            instance.bossMusicListeners.addAll(currentListeners);
        }

        if (!instance.bossMusicListeners.isEmpty()) {
            Iterator<UUID> it = instance.bossMusicListeners.iterator();
            List<UUID> toRemove = new ArrayList<>();
            while (it.hasNext()) {
                UUID id = it.next();
                Player p = Bukkit.getPlayer(id);
                if (p == null || !p.isOnline() || !p.getWorld().equals(bossLoc.getWorld()) || p.isDead()) {
                    if (p != null && p.isOnline()) {
                        plugin.getMusicManager().stop(p);
                    }
                    toRemove.add(id);
                    continue;
                }
                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                    plugin.getMusicManager().stop(p);
                    toRemove.add(id);
                    continue;
                }
                if (p.getLocation().distanceSquared(bossLoc) > MUSIC_RANGE * MUSIC_RANGE) {
                    plugin.getMusicManager().stop(p);
                    toRemove.add(id);
                }
            }
            for (UUID id : toRemove) {
                instance.bossMusicListeners.remove(id);
            }
        }
    }

    private void stopBossMusic(BossInstance instance, boolean forAll) {
        if (!forAll || instance.bossMusicListeners.isEmpty()) return;
        if (plugin.getMusicManager() == null) return;
        for (UUID id : new ArrayList<>(instance.bossMusicListeners)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) {
                plugin.getMusicManager().stop(p);
            }
        }
        instance.bossMusicListeners.clear();
    }

    public UUID findNearestBoss(Location loc, double range) {
        UUID nearest = null;
        double nearestDistSq = range * range;
        for (BossInstance instance : activeBosses.values()) {
            if (!instance.stand.getWorld().equals(loc.getWorld())) continue;
            double distSq = instance.stand.getLocation().distanceSquared(loc);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = instance.stand.getUniqueId();
            }
        }
        return nearest;
    }

    public boolean isBossActive() {
        return !activeBosses.isEmpty();
    }

    public boolean triggerAttack(UUID bossId, String attackName) {
        BossInstance instance = activeBosses.get(bossId);
        if (instance == null) return false;
        if (instance.stand.isDead() || !instance.stand.isValid()) return false;
        BossPuppet stand = instance.stand;

        String key = attackName.toLowerCase();
        BossAttack attack = attackRegistry.get(key);
        if (attack != null) {
            boolean isAerial = isAerialAttackName(key);
            boolean isGround = isGroundAttackName(key);
            if (isAerial && !instance.isFlying) {
                return false;
            }
            if (isGround && instance.isFlying) {
                return false;
            }
            attack.execute(instance);
            return true;
        }

        switch (key) {
            case "crossbarrage" -> {
                if (!instance.isFlying) return false;
                if (instance.shieldSealActive) return false;
                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                startHoverBarrage(instance);
            }
            case "groundslam", "slam" -> {
                if (instance.isFlying) return false;
                if (instance.shieldState == ShieldState.PLANTED || instance.shieldState == ShieldState.SLAM_DONE) {
                    attackRegistry.get("groundslam").execute(instance);
                } else {
                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                    attackRegistry.get("groundslam").execute(instance);
                }
            }
            case "trianglecall", "call" -> {
                if (instance.triangleCallActive) return false;
                attackRegistry.get("trianglecall").execute(instance);
            }
            case "rain", "rainoflances" -> {
                if (!instance.isFlying) return false;
                executeAttack("rainoflances", instance, false);
            }
            case "flyup", "takeoff" -> {
                if (instance.isFlying || instance.shieldSealActive) return false;
                flyUp(instance, false);
            }
            case "land", "descend" -> {
                if (!instance.isFlying) return false;
                land(instance, false);
            }
            case "airslam" -> {
                if (!instance.isFlying) return false;
                executeAttack("airslam", instance, false);
            }
            case "shieldseal", "barrier" -> {
                if (instance.shieldSealActive || instance.isFlying) return false;
                attackRegistry.get("shieldseal").execute(instance);
            }
            case "heal", "healingcircle" -> {
                if (instance.healingCircleActive || instance.isFlying) return false;
                attackRegistry.get("healingcircle").execute(instance);
            }
            // Ground attacks — only while NOT flying
            case "groundshatter", "shieldbash", "lancestorm", "earthpillar", "chaingrapple",
                 "warstomp", "armorspikes", "vortexpull", "mirrorimage", "doombeamer", "doombeam",
                 "lanceflurry", "whirlwindslash", "executionsweep" -> {
                if (instance.isFlying) return false;
                String lookup = key.equals("doombeamer") ? "doombeam" : key;
                BossAttack a = attackRegistry.get(lookup);
                if (a != null) a.execute(instance);
            }
            // Aerial attacks — only while flying
            case "starfall", "aerialrush", "sonicboom", "lightningstorm", "gravitywell",
                 "crossslash", "novaburst", "darkorb", "windcutter", "heavenlyjudgment" -> {
                if (!instance.isFlying) return false;
                BossAttack a = attackRegistry.get(key);
                if (a != null) a.execute(instance);
            }
            // Ranged attacks — usable in both states (ground + air)
            case "lancesnipe", "meteorstorm", "voidbeam", "frostlance", "lightningspear",
                 "shadowvolley", "chainlightning", "crystalbarrage", "arcaneorb", "voidrift",
                 "arcanemissiles", "spiritbeam" -> {
                BossAttack a = attackRegistry.get(key);
                if (a != null) a.execute(instance);
            }
            case "reset", "resetpose" -> resetBossPose(instance);
            // Phase-change attacks
            case "phaserage" -> phaseTransitionRage(instance);
            case "phasebarrier" -> phaseTransitionBarrier(instance);
            case "phasestorm" -> phaseTransitionStorm(instance);
            case "phasedespair" -> phaseTransitionDespair(instance);
            // Defensive moves
            case "stoneskin" -> {
                if (instance.activeDefense != DefenseState.NONE) return false;
                attackRegistry.get("stoneskin").execute(instance);
            }
            case "reflectbarrier" -> {
                if (instance.activeDefense != DefenseState.NONE) return false;
                attackRegistry.get("reflectbarrier").execute(instance);
            }
            case "absorbshield" -> {
                if (instance.activeDefense != DefenseState.NONE) return false;
                attackRegistry.get("absorbshield").execute(instance);
            }
        }
        return true;
    }

    /**
     * Dispatch helper for attacks that accept a telegraph flag (RainOfLances, AirSlam).
     */
    private void executeAttack(String name, BossInstance instance, boolean telegraph) {
        BossAttack a = attackRegistry.get(name);
        if (a instanceof RainOfLancesAttack r) r.execute(instance, telegraph);
        else if (a instanceof AirSlamAttack s) s.execute(instance, telegraph);
        else if (a != null) a.execute(instance);
    }

    private static final java.util.Set<String> AERIAL_ATTACK_NAMES = java.util.Set.of(
            "starfall", "aerialrush", "sonicboom", "lightningstorm", "gravitywell",
            "crossslash", "novaburst", "darkorb", "windcutter", "heavenlyjudgment",
            "rainoflances", "airslam", "hoverbarrage"
    );

    private static final java.util.Set<String> GROUND_ATTACK_NAMES = java.util.Set.of(
            "groundslam", "groundshatter", "shieldbash", "lancestorm", "earthpillar",
            "chaingrapple", "warstomp", "armorspikes", "vortexpull", "mirrorimage", "doombeam",
            "lanceflurry", "whirlwindslash", "executionsweep"
    );

    private boolean isAerialAttackName(String name) {
        return AERIAL_ATTACK_NAMES.contains(name);
    }

    private boolean isGroundAttackName(String name) {
        return GROUND_ATTACK_NAMES.contains(name);
    }

    private void syncBossBarPlayers(BossInstance instance) {
        if (instance.bossBar == null) return;
        BossBar bar = instance.bossBar;
        BossPuppet stand = instance.stand;

        List<Player> toRemove = new ArrayList<>();
        for (Player p : bar.getPlayers()) {
            if (!p.isOnline() || !p.getWorld().equals(stand.getWorld())) {
                toRemove.add(p);
            }
        }
        for (Player p : toRemove) {
            bar.removePlayer(p);
        }

        for (Player p : stand.getWorld().getPlayers()) {
            if (!bar.getPlayers().contains(p)) {
                bar.addPlayer(p);
            }
        }
    }

    @Override
    public Player detectTarget(BossPuppet stand) {
        return BossArena.detectTarget(stand, aggroRange);
    }

    @Override
    public int countPlayersInRange(Location center, double radius) {
        return BossArena.countPlayersInRange(center, radius);
    }

    @Override
    public Player findNearestPlayer(Location center, double range) {
        return BossArena.findNearestPlayer(center, range);
    }

    @Override
    public boolean isOnGround(BossPuppet stand) {
        return BossArena.isOnGround(stand);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        if (stand.getScoreboardTags().contains(TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot interact with this entity!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSummonedFriendlyFire(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (target.getScoreboardTags().contains(SUMMON_TAG) && damager.getScoreboardTags().contains(SUMMON_TAG)) {
            event.setCancelled(true);
            return;
        }

        if (target.getScoreboardTags().contains(SUMMON_TAG)) {
            Entity direct = damager;
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                direct = shooter;
            }
            if (direct.getScoreboardTags().contains(SUMMON_TAG)) {
                event.setCancelled(true);
                return;
            }
            if (direct.getScoreboardTags().contains(TAG)) {
                event.setCancelled(true);
            }
        }

        if (damager.getScoreboardTags().contains(SUMMON_TAG)) {
            Entity direct = damager;
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                direct = shooter;
            }
            if (direct.getScoreboardTags().contains(TAG)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBossManipulate(PlayerArmorStandManipulateEvent event) {
        ArmorStand stand = event.getRightClicked();
        if (stand.getScoreboardTags().contains(TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot modify this entity's armor!");
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;

        Player player = null;
        if (event.getDamager() instanceof Player p) {
            player = p;
        } else if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player p) {
            player = p;
        }

        if (player != null) {
            BossInstance instance = activeBosses.get(stand.getUniqueId());
            double damage = event.getFinalDamage();

            if (instance != null) {
                if (instance.invulnerable) {
                    damage = 0;
                    stand.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
                    player.sendMessage(ChatColor.GRAY + "The Sentinel is invulnerable!");
                } else {
                    if (instance.shieldSealActive) {
                        damage *= 0.5;
                        stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.3f);
                        stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 6, 0), 8, 3.0, 3.0, 3.0, 0.02);
                    }
                    if (instance.healingCircleActive) damage *= 0.8;

                    switch (instance.activeDefense) {
                        case STONE_SKIN -> damage *= 0.5;
                        case REFLECT_BARRIER -> {
                            damage *= 0.7;
                            MscEntityUtils.damageBy(stand, player, damage * 0.3);
                            player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.1);
                        }
                        case ABSORB_SHIELD -> {
                            double absorbed = Math.min(instance.absorbShieldHealth, damage);
                            instance.absorbShieldHealth -= absorbed;
                            damage -= absorbed;
                            if (damage < 0) damage = 0;
                            stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 5, 0), 5, 1, 1, 1, 0.02);
                            if (instance.absorbShieldHealth <= 0) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.5f, 0.8f);
                            }
                        }
                    }
                }
            }

            if (damage > maxDamagePerHit) {
                damage = maxDamagePerHit;
            }

            double newHealth = Math.max(0, stand.getHealth() - damage);
            stand.setHealth(newHealth);
            event.setCancelled(true);

            double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                    ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0;
            double progress = Math.max(0.0, newHealth / maxHealth);

            if (instance != null && instance.bossBar != null) {
                instance.bossBar.setProgress(progress);
            }

            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!MscEntityUtils.applyDeathMessage(plugin, event, TAG, "armor-stand-boss.death-messages")) {
            if (!MscEntityUtils.applyDeathMessage(plugin, event, SUMMON_TAG, "armor-stand-boss.death-messages")) {
                MscEntityUtils.applyDeathMessage(plugin, event, "MSC_BossMirror", "armor-stand-boss.death-messages");
            }
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;

        BossInstance instance = activeBosses.remove(stand.getUniqueId());
        if (instance != null) {
            if (instance.groundSlamTask != null) {
                instance.groundSlamTask.cancel();
                instance.groundSlamTask = null;
            }
            if (instance.wingTask != null) {
                instance.wingTask.cancel();
                instance.wingTask = null;
            }
            if (instance.hoverBarrageTask != null) {
                instance.hoverBarrageTask.cancel();
                instance.hoverBarrageTask = null;
            }
            if (instance.triangleCallTask != null) {
                instance.triangleCallTask.cancel();
                instance.triangleCallTask = null;
            }
            if (instance.flyTask != null) {
                instance.flyTask.cancel();
                instance.flyTask = null;
            }
            cleanupShield(instance);
            stopBossMusic(instance, true);
            if (instance.bossBar != null) {
                instance.bossBar.removeAll();
                instance.bossBar.setVisible(false);
            }
        }

        event.getDrops().clear();
        event.setDroppedExp(1000);

        double dropChance = plugin.getConfig().getDouble("armor-stand-boss.sentinel-core-drop-chance", 100.0);
        if (dropChance > 0.0 && Math.random() * 100.0 < dropChance) {
            event.getDrops().add(SentinelCore.SENTINEL_CORE.clone());
        }

        stand.getWorld().strikeLightningEffect(stand.getLocation());
        stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.5f, 0.5f);

        for (Player p : stand.getWorld().getPlayers()) {
            p.sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "THE OBSIDIAN SENTINEL",
                    ChatColor.GRAY + "Has been defeated!", 10, 70, 20);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getScoreboardTags().contains(SHIELD_HOLDER_TAG)) {
                entity.remove();
                continue;
            }
            if (!(entity instanceof ArmorStand stand)) continue;
            if (!stand.getScoreboardTags().contains(TAG)) continue;
            if (activeBosses.containsKey(stand.getUniqueId())) continue;

            BossInstance instance = new BossInstance(stand);
            activeBosses.put(stand.getUniqueId(), instance);
            setupBossBar(instance);
            startBossAI(instance);
            plugin.getLogger().info("Restarted ArmorStandBoss AI from chunk load at " + stand.getLocation());
        }
    }

}