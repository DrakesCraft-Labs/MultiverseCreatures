package com.Chagui68.entities.boss;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.attack.BossAttack;
import com.Chagui68.items.components.SentinelCore;
import com.Chagui68.entities.boss.attack.aerial.AerialRushAttack;
import com.Chagui68.entities.boss.attack.aerial.AirSlamAttack;
import com.Chagui68.entities.boss.attack.aerial.BladeRingAttack;
import com.Chagui68.entities.boss.attack.aerial.CrossSlashAttack;
import com.Chagui68.entities.boss.attack.aerial.DarkOrbAttack;
import com.Chagui68.entities.boss.attack.aerial.EclipseFallAttack;
import com.Chagui68.entities.boss.attack.aerial.GravityWellAttack;
import com.Chagui68.entities.boss.attack.aerial.HeavenlyJudgmentAttack;
import com.Chagui68.entities.boss.attack.aerial.HoverBarrageAttack;
import com.Chagui68.entities.boss.attack.aerial.LightningStormAttack;
import com.Chagui68.entities.boss.attack.aerial.NovaBurstAttack;
import com.Chagui68.entities.boss.attack.aerial.ObsidianWingsAttack;
import com.Chagui68.entities.boss.attack.aerial.RainOfLancesAttack;
import com.Chagui68.entities.boss.attack.aerial.SonicBoomAttack;
import com.Chagui68.entities.boss.attack.aerial.StarfallAttack;
import com.Chagui68.entities.boss.attack.aerial.WindCutterAttack;
import com.Chagui68.entities.boss.attack.ground.ArmorSpikesAttack;
import com.Chagui68.entities.boss.attack.ground.ChainGrappleAttack;
import com.Chagui68.entities.boss.attack.ground.DoomBeamAttack;
import com.Chagui68.entities.boss.attack.ground.EarthMawAttack;
import com.Chagui68.entities.boss.attack.ground.EarthPillarAttack;
import com.Chagui68.entities.boss.attack.ground.ExecutionerSweepAttack;
import com.Chagui68.entities.boss.attack.ground.GroundShatterAttack;
import com.Chagui68.entities.boss.attack.ground.GroundSlamAttack;
import com.Chagui68.entities.boss.attack.ground.LanceFlurryAttack;
import com.Chagui68.entities.boss.attack.ground.LanceStormAttack;
import com.Chagui68.entities.boss.attack.ground.MirrorImageAttack;
import com.Chagui68.entities.boss.attack.ground.ObsidianSpireAttack;
import com.Chagui68.entities.boss.attack.ground.RuneWardAttack;
import com.Chagui68.entities.boss.attack.ground.ShadowStepAttack;
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
import com.Chagui68.entities.boss.attack.ranged.PlagueBrandAttack;
import com.Chagui68.entities.boss.attack.ranged.RuneMineAttack;
import com.Chagui68.entities.boss.attack.ranged.ShadowVolleyAttack;
import com.Chagui68.entities.boss.attack.ranged.SoulTetherAttack;
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
import com.Chagui68.utils.MscBossBar;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscLog;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import java.util.concurrent.ConcurrentHashMap;

import com.Chagui68.entities.BossInstance.ShieldState;
import com.Chagui68.entities.BossInstance.DefenseState;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ArmorStandBoss implements Listener, BossHost {

    private final MultiverseCreatures plugin;
    private final Map<UUID, BossInstance> activeBosses = new HashMap<>();
    private final Map<String, BossAttack> attackRegistry = new HashMap<>();
    private final Random random = new Random();
    public static final String TAG = "MSC_ArmorStandBoss";
    public static final String SUMMON_TAG = "MSC_ArmorBossSummoned";
    private static final String BOSS_NAME = "THE OBSIDIAN SENTINEL";
    private static final String BAR_TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + BOSS_NAME;
    /** The armor stand's own name tag, which must read exactly like the boss bar. */
    private static final Component STAND_NAME = MscText.title(GOLD, BOSS_NAME);
    private static final double MAX_PROGRESS = 1.0;
    private static final String SHIELD_HOLDER_TAG = "MSC_ShieldHolder";
    /** Default ticks a stone-skin defence lasts (~10 s). */
    private static final int DEFENSE_STONE_SKIN_TICKS = 200;
    /** Default ticks a reflect-barrier defence lasts (~8 s). */
    private static final int DEFENSE_REFLECT_BARRIER_TICKS = 160;
    /** Default ticks an absorb-shield defence lasts (~15 s, or until the shield is broken). */
    private static final int DEFENSE_ABSORB_SHIELD_TICKS = 300;
    /** Default ticks the boss keeps fighting after the last player leaves its radius (~10 s). */
    private static final int NO_PLAYER_DESPAWN_TICKS = 200;
    /**
     * Share of a player's Resistance mitigation that penetrating hits ignore. Penetration is
     * partial on purpose: armour is bypassed outright, but the potion still protects with the
     * remaining 80% of its reduction.
     */
    static final double RESISTANCE_PIERCE = 0.2;
    private static final double FLY_HEIGHT = 15.0;
    private static final double DIST_CLOSE = 5.0;
    private static final double DIST_MEDIUM = 15.0;
    /** How far down the boss scans for a floor before declaring it has none. */
    private static final double GROUND_SCAN_RANGE = 80.0;
    /** Default ticks airborne without a floor before teleporting to a grounded spot (~2 s). */
    private static final int FLOOR_LOST_GRACE_TICKS = 40;
    /** Default rings of columns probed around the boss when looking for a place to stand. */
    private static final int GROUND_SEARCH_RADIUS = 12;
    /** Default minimum ticks between grounding attempts (5 s). */
    private static final int GROUND_SEARCH_COOLDOWN_TICKS = 100;

    private double sealDamage;
    private double hoverBarrageDamage;
    private double aggroRange;
    private double maxDamagePerHit;
    private double maxDamageDealtPerHit;
    private double penetratingResistancePierce;
    private boolean penetratingDamageEnabled;
    private boolean penetratingBossDamage;
    /**
     * Last penetrating hit served to each player, so {@code /msc debug} can show the breakdown the
     * damage event produced. Keyed by player; entries live for the rest of the session because one
     * small record per player is cheaper than tracking logouts, and the command reports its age.
     */
    private final Map<UUID, PenetratingHit> lastPenetratingHits = new ConcurrentHashMap<>();
    private int mediumRangeAttackChance;
    private int farRangeAttackChance;
    private int attackWeightHealingCircle;
    private int attackWeightFlyUp;
    private int attackWeightShieldSeal;
    private int attackWeightGround;
    private int defenseActivationChance;
    private int defenseWeightStoneSkin;
    private int defenseWeightReflectBarrier;
    private int defenseCooldownBaseTicks;
    private int defenseCooldownVarianceTicks;
    private int shieldPlantIntervalBaseTicks;
    private int shieldPlantIntervalVarianceTicks;
    private int hoverBarrageCooldownBaseTicks;
    private int hoverBarrageCooldownVarianceTicks;
    private int groundAttackCooldownBaseTicks;
    private int groundAttackCooldownVarianceTicks;
    private double phaseTransitionSlamDamage;
    private List<Integer> shieldRetrieveDelays;
    private int groundRecoveryGraceTicks;
    private int groundRecoverySearchRadius;
    private int groundRecoveryCooldownTicks;
    /** Health fractions at which each next phase begins, highest first. Drives the phase count. */
    private List<Double> phaseThresholds;
    private int defenseStoneSkinTicks;
    private int defenseReflectBarrierTicks;
    private int defenseAbsorbShieldTicks;
    private int noPlayerDespawnTicks;

    public ArmorStandBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        if (!plugin.isEnabled("entities.armor-stand-boss")) return;
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
        registerAttack(new EclipseFallAttack(this));
        registerAttack(new BladeRingAttack(this));
        registerAttack(new ObsidianWingsAttack(this));
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
        registerAttack(new ObsidianSpireAttack(this));
        registerAttack(new EarthMawAttack(this));
        registerAttack(new ShadowStepAttack(this));
        registerAttack(new RuneWardAttack(this));
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
        registerAttack(new SoulTetherAttack(this));
        registerAttack(new PlagueBrandAttack(this));
        registerAttack(new RuneMineAttack(this));
        // Defensive
        registerAttack(new StoneSkinAttack(this));
        registerAttack(new ReflectBarrierAttack(this));
        registerAttack(new AbsorbShieldAttack(this));
        registerAttack(new ShieldSealAttack(this));
        registerAttack(new HealingCircleAttack(this));
        registerAttack(new TriangleCallAttack(this));
    }

    public void reloadConfig() {
        this.sealDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.seal-damage", 15.0);
        this.hoverBarrageDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.hover-barrage-damage", 12.0);
        this.aggroRange = plugin.getConfig().getDouble("entities.armor-stand-boss.aggro-range", 50.0);
        this.maxDamagePerHit = plugin.getConfig().getDouble("entities.armor-stand-boss.max-damage-per-hit", 50.0);
        this.mediumRangeAttackChance = plugin.getConfig().getInt("entities.armor-stand-boss.medium-range-attack-chance", 15);
        this.farRangeAttackChance = plugin.getConfig().getInt("entities.armor-stand-boss.far-range-attack-chance", 85);
        this.attackWeightHealingCircle = plugin.getConfig().getInt("entities.armor-stand-boss.attack-weight-healing-circle", 25);
        this.attackWeightFlyUp = plugin.getConfig().getInt("entities.armor-stand-boss.attack-weight-fly-up", 15);
        this.attackWeightShieldSeal = plugin.getConfig().getInt("entities.armor-stand-boss.attack-weight-shield-seal", 35);
        this.attackWeightGround = plugin.getConfig().getInt("entities.armor-stand-boss.attack-weight-ground", 55);
        this.defenseActivationChance = plugin.getConfig().getInt("entities.armor-stand-boss.defense-activation-chance", 30);
        this.defenseWeightStoneSkin = plugin.getConfig().getInt("entities.armor-stand-boss.defense-weight-stone-skin", 35);
        this.defenseWeightReflectBarrier = plugin.getConfig().getInt("entities.armor-stand-boss.defense-weight-reflect-barrier", 65);
        this.defenseCooldownBaseTicks = plugin.getConfig().getInt("entities.armor-stand-boss.defense-cooldown-base-ticks", 300);
        this.defenseCooldownVarianceTicks = plugin.getConfig().getInt("entities.armor-stand-boss.defense-cooldown-variance-ticks", 300);
        this.shieldPlantIntervalBaseTicks = plugin.getConfig().getInt("entities.armor-stand-boss.shield-plant-interval-base-ticks", 300);
        this.shieldPlantIntervalVarianceTicks = plugin.getConfig().getInt("entities.armor-stand-boss.shield-plant-interval-variance-ticks", 100);
        this.penetratingDamageEnabled = plugin.getConfig().getBoolean("entities.armor-stand-boss.penetrating-damage", true);
        this.maxDamageDealtPerHit = plugin.getConfig().getDouble("entities.armor-stand-boss.max-damage-dealt", 15.0);
        this.penetratingResistancePierce = plugin.getConfig().getDouble(
                "entities.armor-stand-boss.penetrating-resistance-pierce", RESISTANCE_PIERCE);
        this.hoverBarrageCooldownBaseTicks = plugin.getConfig().getInt("entities.armor-stand-boss.hover-barrage-cooldown-base-ticks", 240);
        this.hoverBarrageCooldownVarianceTicks = plugin.getConfig().getInt("entities.armor-stand-boss.hover-barrage-cooldown-variance-ticks", 120);
        this.groundAttackCooldownBaseTicks = plugin.getConfig().getInt("entities.armor-stand-boss.ground-attack-cooldown-base-ticks", 40);
        this.groundAttackCooldownVarianceTicks = plugin.getConfig().getInt("entities.armor-stand-boss.ground-attack-cooldown-variance-ticks", 40);
        // Clamped: a grace of 0 or a cooldown of 0 would run the terrain scan every tick.
        this.groundRecoveryGraceTicks = Math.max(1, plugin.getConfig().getInt(
                "entities.armor-stand-boss.ground-recovery-grace-ticks", FLOOR_LOST_GRACE_TICKS));
        this.groundRecoverySearchRadius = Math.max(0, plugin.getConfig().getInt(
                "entities.armor-stand-boss.ground-recovery-search-radius", GROUND_SEARCH_RADIUS));
        this.groundRecoveryCooldownTicks = Math.max(20, plugin.getConfig().getInt(
                "entities.armor-stand-boss.ground-recovery-cooldown-ticks", GROUND_SEARCH_COOLDOWN_TICKS));
        this.phaseTransitionSlamDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.phase-transition-slam-damage", 10.0);
        // The ladder is the single source of truth: the phase count follows from its length.
        this.phaseThresholds = SentinelPhase.sanitizeThresholds(
                plugin.getConfig().getDoubleList("entities.armor-stand-boss.phase-thresholds"));
        // Clamped: a zero-tick defence would expire on the same tick it starts.
        this.defenseStoneSkinTicks = Math.max(1, plugin.getConfig().getInt(
                "entities.armor-stand-boss.defense-duration-stone-skin-ticks", DEFENSE_STONE_SKIN_TICKS));
        this.defenseReflectBarrierTicks = Math.max(1, plugin.getConfig().getInt(
                "entities.armor-stand-boss.defense-duration-reflect-barrier-ticks", DEFENSE_REFLECT_BARRIER_TICKS));
        this.defenseAbsorbShieldTicks = Math.max(1, plugin.getConfig().getInt(
                "entities.armor-stand-boss.defense-duration-absorb-shield-ticks", DEFENSE_ABSORB_SHIELD_TICKS));
        // 0 means "despawn as soon as nobody is in range".
        this.noPlayerDespawnTicks = Math.max(0, plugin.getConfig().getInt(
                "entities.armor-stand-boss.no-player-despawn-ticks", NO_PLAYER_DESPAWN_TICKS));
        List<Integer> delays = plugin.getConfig().getIntegerList("entities.armor-stand-boss.shield-retrieve-delays");
        this.shieldRetrieveDelays = delays.isEmpty() ? List.of(80, 90, 100, 110, 120) : delays;
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
        if (!plugin.isEnabled("entities.armor-stand-boss")) return false;
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        if (stand == null) return false;

        double health = plugin.getConfig().getDouble("entities.armor-stand-boss.health", 3200.0);
        MscEntityUtils.initVirtualHealth(stand, health);
        stand.setInvulnerable(false);

        stand.customName(STAND_NAME);
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
        BossBar bar = MscBossBar.create(BAR_TITLE, BarColor.RED, BarStyle.SEGMENTED_6, BarFlag.DARKEN_SKY);
        bar.setProgress(MAX_PROGRESS);
        MscBossBar.showInWorld(bar, instance.stand.getWorld());
        instance.bossBar = bar;
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
                MscEntityUtils.damageBy(stand.entidad(), player, damage);
                player.setVelocity(player.getVelocity().add(new org.bukkit.util.Vector(0, 0.4, 0)));
            }
        }

        instance.pentagramCenters.clear();
    }

    private BarColor getPhaseColor(int phase) {
        return SentinelPhase.barColor(phase);
    }

    /** The boss bar title for a phase, generated from the configured ladder instead of hardcoded. */
    private String getPhaseTitle(int phase) {
        return SentinelPhase.title(BOSS_NAME, phase, SentinelPhase.phaseCount(phaseThresholds));
    }

    private void updatePhase(BossInstance instance) {
        BossPuppet stand = instance.stand;
        double maxHealth = stand.getMaxHealth();
        double currentHealth = stand.getHealth();
        double healthPercent = maxHealth > 0.0 ? currentHealth / maxHealth : 1.0;

        // SentinelPhase owns the ladder; here we only make sure it never walks back upwards.
        int newPhase = SentinelPhase.phaseFor(healthPercent, phaseThresholds);
        if (newPhase < instance.currentPhase) {
            newPhase = instance.currentPhase;
        }

        if (newPhase != instance.currentPhase) {
            int oldPhase = instance.currentPhase;
            instance.currentPhase = newPhase;
            if (instance.bossBar != null) {
                instance.bossBar.setTitle(getPhaseTitle(newPhase));
                instance.bossBar.setColor(getPhaseColor(newPhase));
                instance.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(
                        instance.stand.getHealth(), maxHealth));
            }
            instance.stand.getWorld().playSound(instance.stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);

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
                    if (instance.defenseTimer >= defenseStoneSkinTicks) expired = true;
                }
                case REFLECT_BARRIER -> {
                    if (instance.defenseTimer >= defenseReflectBarrierTicks) expired = true;
                }
                case ABSORB_SHIELD -> {
                    if (instance.defenseTimer >= defenseAbsorbShieldTicks
                            || instance.absorbShieldHealth <= 0) expired = true;
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
        BukkitRunnable ai = new BukkitRunnable() {
            @Override
            public void run() {
                BossPuppet stand = instance.stand;

                if (stand.isDead() || !stand.isValid()) {
                    teardown(instance);
                    return;
                }

                if (instance.bossBar == null || !instance.bossBar.isVisible()) {
                    setupBossBar(instance);
                }

                updatePhase(instance);

                syncBossBarPlayers(instance);
                instance.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(stand.getHealth(), stand.getMaxHealth()));

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
                            double floorY = BossArena.findFloorY(loc, GROUND_SCAN_RANGE);
                            if (Double.isNaN(floorY)) {
                                // No solid block below within range. The convenience getGroundY here
                                // returned the boss's own Y, so the teleport was a no-op every tick:
                                // the stand hovered and the ground-attack branch below never ran.
                                instance.floorLostTicks++;
                                if (instance.groundSearchCooldown > 0) {
                                    instance.groundSearchCooldown--;
                                }
                                if (instance.floorLostTicks >= groundRecoveryGraceTicks
                                        && instance.groundSearchCooldown <= 0) {
                                    groundBoss(instance);
                                }
                            } else {
                                instance.floorLostTicks = 0;
                                if (loc.getY() - floorY > 0.3) {
                                    loc.setY(Math.max(floorY, loc.getY() - 0.8));
                                    stand.teleport(loc);
                                    stand.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.5, 0.1, 0.5, 0.02);
                                } else {
                                    loc.setY(floorY);
                                    stand.teleport(loc);
                                }
                            }
                        } else {
                            instance.floorLostTicks = 0;
                        }
                    } else if (instance.shieldState == ShieldState.NORMAL) {
                        instance.hoverBarrageCooldown++;
                        instance.groundAttackCooldown++;
                        if (instance.hoverBarrageCooldown >= hoverBarrageCooldownBaseTicks + random.nextInt(hoverBarrageCooldownVarianceTicks)) {
                            instance.hoverBarrageCooldown = 0;

                            double maxHealth = stand.getMaxHealth();
                            double healthPct = maxHealth > 0.0 ? stand.getHealth() / maxHealth : 1.0;
                            int choice = random.nextInt(100);

                            if (healthPct < 0.4 && choice < attackWeightHealingCircle) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                attackRegistry.get("healingcircle").execute(instance);
                            } else if (choice < attackWeightFlyUp) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                flyUp(instance);
                            } else if (choice < attackWeightShieldSeal) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.7f);
                                attackRegistry.get("shieldseal").execute(instance);
                            } else if (choice < attackWeightGround) {
                                stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                                executeRandomGroundAttack(instance);
                            } else {
                                startHoverBarrage(instance);
                            }
                        } else if (instance.defenseCooldown > 0) {
                            instance.defenseCooldown--;
                        } else if (instance.groundAttackCooldown >= groundAttackCooldownBaseTicks + random.nextInt(groundAttackCooldownVarianceTicks)) {
                            instance.groundAttackCooldown = 0;
                            double maxHealth = stand.getMaxHealth();
                            double healthPct = maxHealth > 0.0 ? stand.getHealth() / maxHealth : 1.0;
                            if (instance.activeDefense == DefenseState.NONE && healthPct < 0.5 && random.nextInt(100) < defenseActivationChance) {
                                int defChoice = random.nextInt(100);
                                if (defChoice < defenseWeightStoneSkin) {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("stoneskin").execute(instance);
                                } else if (defChoice < defenseWeightReflectBarrier) {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("reflectbarrier").execute(instance);
                                } else {
                                    stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                                    attackRegistry.get("absorbshield").execute(instance);
                                }
                                instance.defenseCooldown = defenseCooldownBaseTicks + random.nextInt(defenseCooldownVarianceTicks);
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
                    // The counter exists for this grace period. Despawning on the first playerless
                    // tick deleted the boss on a one-tick gap: a lag spike, a wipe, or everyone
                    // stepping just outside the radius mid-fight.
                    instance.noPlayerTicks++;
                    if (instance.noPlayerTicks >= noPlayerDespawnTicks) {
                        teardown(instance);
                        stand.remove();
                        return;
                    }
                }
            }
        };
        // Scheduled first: cancelling a runnable that was never scheduled throws, so the handle is
        // only published once the task really exists.
        ai.runTaskTimer(plugin, 0L, 1L);
        instance.aiTask = ai;
    }

    /**
     * Ends a fight exactly once: stops the AI tick, every attack task, the seals, the music, the
     * boss bar, and forgets the instance.
     *
     * The three ways a fight can end — the boss dies, nobody is left in range, or the death event
     * fires — each used to repeat this by hand, and the event path cancelled only five of the eight
     * attack tasks and could not stop the AI at all: it kept ticking until it noticed the corpse on
     * its own, cleaning up a second time. Doing it here also means a boss killed with {@code /kill}
     * leaves nothing behind.
     */
    private void teardown(BossInstance instance) {
        if (instance.aiTask != null) {
            instance.aiTask.cancel();
            instance.aiTask = null;
        }
        cleanupShield(instance);
        stopBossMusic(instance, true);
        if (instance.bossBar != null) {
            instance.bossBar.removeAll();
            instance.bossBar.setVisible(false);
        }
        activeBosses.remove(instance.stand.getUniqueId());
    }

    /**
     * Last-resort recovery for a grounded boss that ended up with no floor under it.
     *
     * Looks for the nearest column with solid ground and headroom, preferring the area around the
     * current target so the fight continues where the players are, then the world spawn. Cancels
     * any flight state and resets the attack cooldowns so combat resumes immediately instead of
     * waiting out timers that only advanced while the boss was idling.
     */
    private void groundBoss(BossInstance instance) {
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;

        Location current = stand.getLocation();
        World world = stand.getWorld();
        instance.floorLostTicks = 0;
        instance.groundSearchCooldown = groundRecoveryCooldownTicks;

        Location destination = BossArena.findGroundRestingPlace(
                current, groundRecoverySearchRadius, GROUND_SCAN_RANGE);

        if (destination == null) {
            Player target = detectTarget(stand);
            if (target != null) {
                destination = BossArena.findGroundRestingPlace(
                        target.getLocation(), groundRecoverySearchRadius, GROUND_SCAN_RANGE);
            }
        }
        if (destination == null) {
            Location spawn = world.getSpawnLocation();
            destination = BossArena.findGroundRestingPlace(
                    spawn, groundRecoverySearchRadius, GROUND_SCAN_RANGE);
            if (destination == null) {
                destination = spawn;
            }
        }

        if (instance.flyTask != null) {
            instance.flyTask.cancel();
            instance.flyTask = null;
        }
        instance.isFlying = false;
        instance.flyingTimer = 0;
        instance.airStuckTicks = 0;
        instance.groundAttackCooldown = 0;
        instance.hoverBarrageCooldown = 0;

        world.spawnParticle(Particle.CLOUD, current, 30, 1.5, 1.5, 1.5, 0.1);
        world.playSound(current, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.6f);
        stand.teleport(destination);
        world.spawnParticle(Particle.CLOUD, destination, 30, 1.5, 1.5, 1.5, 0.1);
        world.playSound(destination, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 0.7f);
        resetBossPose(instance);
        plugin.getLogger().fine("[ArmorStandBoss] no floor below the boss; relocated to " + destination);
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
            double distSq = p.getLocation().distanceSquared(loc);
            if (distSq < 900.0) {
                Vector away = p.getLocation().toVector().subtract(loc.toVector()).normalize();
                p.setVelocity(away.multiply(2.0).setY(1.0));
                MscEntityUtils.damageBy(stand.entidad(), p, phaseTransitionSlamDamage);
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
            double distSq = p.getLocation().distanceSquared(loc);
            if (distSq < 625.0) {
                double dist = Math.sqrt(distSq);
                MscEntityUtils.damageBy(stand.entidad(), p, dmg * 0.5 * (1 - dist / 25));
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
            double distSq = p.getLocation().distanceSquared(loc);
            if (distSq < 1225.0) {
                double dist = Math.sqrt(distSq);
                MscEntityUtils.damageBy(stand.entidad(), p, dmg * (1 - dist / 35));
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
            allAerial = new String[]{"aerialrush", "crossslash", "novaburst", "obsidianwings", "bladering"};
        } else if (nearestDist < 35) {
            allAerial = new String[]{"sonicboom", "windcutter", "gravitywell", "darkorb", "aerialrush", "eclipsefall"};
        } else {
            allAerial = new String[]{"starfall", "lightningstorm", "heavenlyjudgment", "darkorb", "eclipsefall"};
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
                "lanceflurry", "whirlwindslash", "executionsweep", "earthmaw", "shadowstep", "runeward"};
        String[] mediumAttacks = {"lancestorm", "earthpillar", "groundshatter", "groundshatter", "armorspikes", "vortexpull",
                "lanceflurry", "whirlwindslash", "obsidianspire", "earthmaw", "runeward"};
        String[] farAttacks = {"shieldbash", "obsidianspire"};

        String choice;
        if (nearestDist < DIST_CLOSE) {
            choice = closeAttacks[random.nextInt(closeAttacks.length)];
        } else if (nearestDist < DIST_MEDIUM) {
            if (random.nextInt(100) < mediumRangeAttackChance) {
                executeRangedAttack(instance);
                return;
            }
            choice = mediumAttacks[random.nextInt(mediumAttacks.length)];
        } else {
            if (random.nextInt(100) < farRangeAttackChance) {
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
                "arcanemissiles", "spiritbeam", "soultethers", "plaguebrand", "runemines"};
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
                return instance.stand.armorStand();
            }
        }
        return null;
    }

    public void spawnShockwaveWave(LivingEntity source, World world, Location center, double maxRadius) {
        final int ringCount = 10;
        final double ringSpacing = maxRadius / ringCount;
        final int ticksPerRing = 3;

        Location impactLoc = center.clone();
        impactLoc.setY(impactLoc.getY() + 0.1);

        final LivingEntity stand = source;

        for (int i = 0; i < ringCount; i++) {
            final int ringIndex = i;
            final double radius = ringIndex * ringSpacing;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (radius <= 0.5) return;
                    int samples = (int) Math.max(16, radius * 4);
                    Vector knockbackStrength = new Vector(0, 0.7 - radius / maxRadius * 0.3, 0);
                    double damageMultiplier = 1.0 - radius / maxRadius * 0.6;
                    List<Player> validPlayers = getValidPlayers(world);
                    Set<UUID> hitInThisRing = new HashSet<>();

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

                        for (Player p : validPlayers) {
                            if (hitInThisRing.contains(p.getUniqueId())) continue;
                            Location pLoc = p.getLocation();
                            if (pLoc.getY() <= pl.getY() + 2 && pLoc.distanceSquared(pl) < 4.0) {
                                hitInThisRing.add(p.getUniqueId());
                                if (stand != null) MscEntityUtils.damageBy(stand, p, damageMultiplier * 5.0);
                                p.setVelocity(p.getVelocity().add(knockbackStrength));
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
        return shieldPlantIntervalBaseTicks + random.nextInt(shieldPlantIntervalVarianceTicks);
    }

    public int getShieldRetrieveDelay(int phase) {
        int index = Math.min(phase, shieldRetrieveDelays.size() - 1);
        return shieldRetrieveDelays.get(Math.max(0, index));
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
            } catch (Exception e) {
                MscLog.warn("could not start the boss music for " + p.getName(), e);
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

    /**
     * Runs one registered attack on any instance, for the {@code /msc dummy attack} preview.
     *
     * <p>The attack is the same object the Sentinel runs, so the animation, particles, sounds and
     * telegraphs are the real ones; the actor is a pose dummy, so {@link AttackPreview} refuses
     * every hit it lands, and that is what makes the preview harmless.
     *
     * <p>A fight knows whether the boss is airborne, and over half the attacks read that before they
     * aim, so the preview stages it here instead of demanding a flying dummy.
     *
     * @return true when the attack exists and has been started
     */
    public boolean previewAttack(BossInstance instance, String attackName) {
        if (instance == null || attackName == null) return false;
        String key = attackName.toLowerCase();
        BossAttack attack = attackRegistry.get(key);
        if (attack == null) return false;

        instance.isFlying = isAerialAttackName(key);
        instance.groundY = getGroundY(instance.stand.getLocation(), 80);
        attack.execute(instance);
        return true;
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
            "rainoflances", "airslam", "hoverbarrage", "eclipsefall", "bladering", "obsidianwings"
    );

    private static final java.util.Set<String> GROUND_ATTACK_NAMES = java.util.Set.of(
            "groundslam", "groundshatter", "shieldbash", "lancestorm", "earthpillar",
            "chaingrapple", "warstomp", "armorspikes", "vortexpull", "mirrorimage", "doombeam",
            "lanceflurry", "whirlwindslash", "executionsweep", "obsidianspire", "earthmaw",
            "shadowstep", "runeward"
    );

    private boolean isAerialAttackName(String name) {
        return AERIAL_ATTACK_NAMES.contains(name);
    }

    private boolean isGroundAttackName(String name) {
        return GROUND_ATTACK_NAMES.contains(name);
    }

    private void syncBossBarPlayers(BossInstance instance) {
        if (instance.bossBar == null) return;
        MscBossBar.showInWorld(instance.bossBar, instance.stand.getWorld());
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
    public void onBossAttacksPlayer(EntityDamageByEntityEvent event) {
        if (penetratingBossDamage) return;
        if (!(event.getDamager() instanceof ArmorStand stand) || !stand.getScoreboardTags().contains(TAG)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!penetratingDamageEnabled || event.isCancelled() || player.isDead()) return;
        // The engine already folded armour, Protection and Resistance into the event's damage, so
        // those three are credited back: a penetrating hit must not be shrunk by the very defences
        // it is supposed to pierce. Shields, absorption and everything else stay as they came.
        PenetratingHit hit = PenetratingHit.of(event.getDamage(),
                modifierValue(event, EntityDamageEvent.DamageModifier.ARMOR),
                modifierValue(event, EntityDamageEvent.DamageModifier.MAGIC),
                modifierValue(event, EntityDamageEvent.DamageModifier.RESISTANCE),
                resistanceAmplifier(player),
                penetratingResistancePierce,
                maxDamageDealtPerHit,
                System.currentTimeMillis());
        if (hit.raw() <= 0) return;
        event.setCancelled(true);
        lastPenetratingHits.put(player.getUniqueId(), hit);
        double dealt = hit.dealt();
        penetratingBossDamage = true;
        try {
            // OUT_OF_WORLD skips armour and Resistance on its own, so `dealt` already carries the
            // Resistance share we chose to keep; clearing the hurt cooldown keeps back-to-back
            // boss hits from being swallowed by the vanilla invulnerability window.
            player.setNoDamageTicks(0);
            player.damage(dealt, DamageSource.builder(DamageType.OUT_OF_WORLD)
                    .withDirectEntity(stand)
                    .withCausingEntity(stand)
                    .build());
        } finally {
            penetratingBossDamage = false;
        }
    }

    /** Most recent penetrating hit served to {@code playerId}, or {@code null} when there is none. */
    public PenetratingHit lastPenetratingHit(UUID playerId) {
        return lastPenetratingHits.get(playerId);
    }

    /** Whether penetrating damage is on; {@code /msc debug} notes when it is off. */
    public boolean isPenetratingDamageEnabled() {
        return penetratingDamageEnabled;
    }

    /** Value of a damage modifier, or {@code 0} when the engine does not apply it to this event. */
    private static double modifierValue(EntityDamageEvent event, EntityDamageEvent.DamageModifier modifier) {
        return event.isApplicable(modifier) ? event.getDamage(modifier) : 0.0;
    }

    /**
     * Damage left once the given mitigations are credited back. Mitigations arrive as the negative
     * modifiers the engine computed, so {@code 3.52 - (-17.6) - (-0.88) = 22} restores a hit that
     * full netherite had cut down to a fifth. Never returns less than zero.
     */
    static double unmitigated(double finalDamage, double... mitigations) {
        double total = finalDamage;
        for (double mitigation : mitigations) {
            total -= mitigation;
        }
        return Math.max(0.0, total);
    }

    /** Vanilla Resistance mitigation for an amplifier: 20% per level, capped at 100%. */
    static double resistanceMitigation(int amplifier) {
        return amplifier < 0 ? 0.0 : Math.min(1.0, 0.2 * (amplifier + 1));
    }

    /**
     * Damage a penetrating hit really deals. {@code pierce} is the share of the potion's mitigation
     * the boss ignores, so {@code 0} leaves Resistance fully effective, {@code 1} ignores it
     * completely, and the default {@code 0.2} shaves a fifth off its protection.
     */
    static double penetratingDamage(double raw, int resistanceAmplifier, double pierce) {
        double ignored = Math.max(0.0, Math.min(1.0, pierce));
        return raw * (1.0 - resistanceMitigation(resistanceAmplifier) * (1.0 - ignored));
    }

    /** Amplifier of the player's Resistance effect, or {@code -1} when they have none. */
    private static int resistanceAmplifier(Player player) {
        PotionEffect effect = player.getPotionEffect(PotionEffectType.RESISTANCE);
        return effect == null ? -1 : effect.getAmplifier();
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
            double incoming = event.getFinalDamage();
            // The maths is in SentinelDefense so it can be tested away from the server; this handler
            // only plays the effects the resolved hit calls for.
            SentinelDefense.Result hit = SentinelDefense.from(instance, maxDamagePerHit).resolve(incoming);
            double damage = hit.applied();

            if (instance != null) {
                if (instance.invulnerable) {
                    stand.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
                    player.sendMessage(ChatColor.GRAY + "The Sentinel is invulnerable!");
                } else {
                    if (instance.shieldSealActive) {
                        stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.3f);
                        stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 6, 0), 8, 3.0, 3.0, 3.0, 0.02);
                    }
                    if (instance.activeDefense == DefenseState.REFLECT_BARRIER) {
                        MscEntityUtils.damageBy(stand, player, hit.reflected());
                        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.1);
                    } else if (instance.activeDefense == DefenseState.ABSORB_SHIELD) {
                        instance.absorbShieldHealth -= hit.absorbed();
                        stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 5, 0), 5, 1, 1, 1, 0.02);
                        if (hit.shieldBroken()) {
                            stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.5f, 0.8f);
                        }
                    }
                }
            }

            plugin.getBossDamageLog().record(player.getUniqueId(), BossDamageSample.taken(
                    BossId.SENTINEL, "Incoming hit", incoming, damage, String.join(", ", hit.steps()),
                    System.currentTimeMillis()));

            double currentHealth = MscEntityUtils.getVirtualHealth(stand);
            double newHealth = Math.max(0, currentHealth - damage);
            MscEntityUtils.setVirtualHealth(stand, newHealth);
            event.setCancelled(true);

            double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
            double progress = MscEntityUtils.calculateVirtualProgress(newHealth, maxHealth);

            if (instance != null && instance.bossBar != null) {
                instance.bossBar.setProgress(progress);
            }

            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.armor-stand-boss.death-messages")) {
            if (!MscEntityUtils.applyDeathMessage(plugin, event, SUMMON_TAG, "entities.armor-stand-boss.death-messages")) {
                MscEntityUtils.applyDeathMessage(plugin, event, "MSC_BossMirror", "entities.armor-stand-boss.death-messages");
            }
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;

        BossInstance instance = activeBosses.get(stand.getUniqueId());
        if (instance != null) {
            teardown(instance);
        }

        event.getDrops().clear();
        event.setDroppedExp(1000);

        double dropChance = plugin.getConfig().getDouble("entities.armor-stand-boss.sentinel-core-drop-chance", 100.0);
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

            if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
                double health = plugin.getConfig().getDouble("entities.armor-stand-boss.health", 3200.0);
                MscEntityUtils.initVirtualHealth(stand, health);
            }

            BossInstance instance = new BossInstance(stand);
            activeBosses.put(stand.getUniqueId(), instance);
            setupBossBar(instance);
            startBossAI(instance);
            plugin.getLogger().info("Restarted ArmorStandBoss AI from chunk load at " + stand.getLocation());
        }
    }

}