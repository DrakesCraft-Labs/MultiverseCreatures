package com.Chagui68;

import com.Chagui68.entities.miniboss.Mahoraga;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.BossDamageLog;
import com.Chagui68.entities.boss.DioBoss;
import com.Chagui68.entities.boss.JackStarBoss;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.NixBoss;
import com.Chagui68.entities.handler.MobHandler;
import com.Chagui68.entities.Kinger;
import com.Chagui68.entities.BoneShield;
import com.Chagui68.entities.ChaosMage;
import com.Chagui68.entities.CreeperJr;
import com.Chagui68.entities.DiscTrader;
import com.Chagui68.entities.EnderKnight;
import com.Chagui68.entities.FlameElemental;
import com.Chagui68.entities.FrostGolem;
import com.Chagui68.entities.HeadSlime;
import com.Chagui68.entities.ObsidianGuard;
import com.Chagui68.entities.ShadowRogue;
import com.Chagui68.entities.SoulReaper;
import com.Chagui68.entities.StormCaller;
import com.Chagui68.entities.VenomWitch;
import com.Chagui68.entities.VoidCrawler;
import com.Chagui68.entities.Warlord;
import com.Chagui68.entities.ZombieHorseTrap;
import com.Chagui68.items.recipes.RecipeManager;
import com.Chagui68.listener.armor.EightHandledWheelHandler;
import com.Chagui68.listener.armor.ObsidianBastionHandler;
import com.Chagui68.listener.bossdimension.BossDimensionBlockHandler;
import com.Chagui68.listener.bossdimension.BossDimensionCommandHandler;
import com.Chagui68.listener.bossdimension.BossInvocationManager;
import com.Chagui68.listener.bossdimension.JackInvocationManager;
import com.Chagui68.listener.bossdimension.DioInvocationManager;
import com.Chagui68.listener.bossdimension.NixInvocationManager;
import com.Chagui68.listener.bossdimension.PantheonInvocationManager;
import com.Chagui68.listener.combat.ItemCombatHandler;
import com.Chagui68.listener.CustomItemPlaceHandler;
import com.Chagui68.listener.ComponentEventGuard;
import com.Chagui68.listener.entities.EntitiesIAHandler;
import com.Chagui68.listener.food.ItemFoodHandler;
import com.Chagui68.listener.misc.DiscJukeboxHandler;
import com.Chagui68.listener.misc.IceCrownHandler;
import com.Chagui68.listener.misc.MantisClawsHandler;
import com.Chagui68.listener.misc.MineHandler;
import com.Chagui68.listener.misc.WirtsLanternHandler;
import com.Chagui68.listener.offhand.FrostHeartOffhandHandler;
import com.Chagui68.listener.offhand.MarrowAegisHandler;
import com.Chagui68.listener.offhand.VeilwalkerMantleHandler;
import com.Chagui68.listener.recipes.RecipeGuardListener;
import com.Chagui68.listener.ritual.RitualCandleListener;
import com.Chagui68.listener.weapons.magic.ChaosForgeHandler;
import com.Chagui68.listener.weapons.magic.GrimoireHandler;
import com.Chagui68.listener.weapons.magic.SkyfireTalismanHandler;
import com.Chagui68.listener.weapons.melee.CinderGreatswordHandler;
import com.Chagui68.listener.weapons.melee.NullshearEdgeHandler;
import com.Chagui68.listener.weapons.melee.SoulreapScytheHandler;
import com.Chagui68.listener.weapons.melee.VenomfangHandler;
import com.Chagui68.listener.weapons.ranged.AetherPullshotHandler;
import com.Chagui68.music.MusicManager;
import com.Chagui68.ritual.BossDimensionManager;
import com.Chagui68.ritual.RitualManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MultiverseCreatures extends JavaPlugin {

    private CreeperJr creeperJr;
    private HeadSlime headSlime;
    private ZombieHorseTrap zombieHorseTrap;
    private Mahoraga mahoraga;
    private com.Chagui68.entities.miniboss.GarouBoss garouBoss;
    private ArmorStandBoss armorStandBoss;
    /** Latest damage samples the bosses report, read by {@code /msc debug}. */
    private final BossDamageLog bossDamageLog = new BossDamageLog();
    private MagicSealListener magicSealListener;
    private MusicManager musicManager;
    private BossDimensionManager bossDimensionManager;
    private RitualManager ritualManager;
    private ShadowRogue shadowRogue;
    private com.Chagui68.entities.ArrowSkeleton arrowSkeleton;
    private com.Chagui68.stand.StandManager standManager;
    private com.Chagui68.stand.VampireManager vampireManager;
    private FlameElemental flameElemental;
    private FrostGolem frostGolem;
    private VoidCrawler voidCrawler;
    private StormCaller stormCaller;
    private BoneShield boneShield;
    private VenomWitch venomWitch;
    private ObsidianGuard obsidianGuard;
    private SoulReaper soulReaper;
    private ChaosMage chaosMage;
    private EnderKnight enderKnight;
    private Kinger kinger;
    private NixBoss nixBoss;
    private JackStarBoss jackStarBoss;
    private DioBoss dioBoss;
    private JackInvocationManager jackInvocationManager;
    /** DrakesBosses' gods, summoned at the Pantheon Altars of the Boss Dimension. */
    private PantheonInvocationManager pantheonInvocationManager;
    private DiscTrader discTrader;
    private Warlord warlord;
    private DiscJukeboxHandler discJukeboxHandler;
    private ItemCombatHandler itemCombatHandler;
    private WirtsLanternHandler wirtsLanternHandler;
    private MantisClawsHandler mantisClawsHandler;
    private ObsidianBastionHandler obsidianBastionHandler;
    private FrostHeartOffhandHandler frostHeartOffhandHandler;
    /** The population recount, the only task the plugin itself starts. */
    private BukkitTask recountTask;

    /**
     * Returns whether a config feature (item/entity section) is enabled.
     * Sections live under `items.<name>` or `entities.<name>` and each carries
     * an `enabled` switch. Missing flags default to true (backwards compatible).
     */
    public boolean isEnabled(String section) {
        return getConfig().getBoolean(section + ".enabled", true);
    }

    /** Creates a preventive backup of config.yml before any operation. */
    private void backupConfigFile() {
        try {
            java.io.File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            java.io.File target = new java.io.File(dataFolder, "config.yml");
            if (target.exists() && target.length() > 0) {
                java.io.File backupsDir = new java.io.File(dataFolder, "backups");
                if (!backupsDir.exists()) {
                    backupsDir.mkdirs();
                }
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
                String timestamp = sdf.format(new java.util.Date());
                java.io.File backupFile = new java.io.File(backupsDir, "config_backup_" + timestamp + ".yml");
                java.nio.file.Files.copy(target.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            getLogger().warning("[Backup] Could not back up config.yml: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        com.Chagui68.utils.MscLog.init(getLogger());
        backupConfigFile();
        saveDefaultConfig();
        // saveDefaultConfig only writes a config when there is none, so an updated server keeps its
        // old file and every new knob stays invisible. This adds what is missing before anything
        // reads the config.
        com.Chagui68.utils.MscConfigMigration.run(this);
        com.Chagui68.stand.HeadModels.init(this);

        if (getConfig().getBoolean("recipes.enabled", true)) {
            if (getConfig().getBoolean("recipes.deferred-registration", true)) {
                getServer().getScheduler().runTaskLater(this, RecipeManager::registerRecipes, 40L);
            } else {
                RecipeManager.registerRecipes();
            }
        }

        // Props from an attack cut short by the last restart have no owner left to remove them: the
        // mobs, the suit pieces and the hand-placed markers all have one. Runs before any boss can be
        // mid-attack, so a tag in that list can only belong to a fight that is already over.
        int leftovers = com.Chagui68.utils.MscLeftovers.sweepAll();
        if (leftovers > 0) {
            getLogger().info("Removed " + leftovers + " leftover boss-attack displays from the previous run.");
        }

        creeperJr = new CreeperJr(this);
        headSlime = new HeadSlime(this);
        zombieHorseTrap = new ZombieHorseTrap(this);
        mahoraga = new Mahoraga(this);
        garouBoss = new com.Chagui68.entities.miniboss.GarouBoss(this);
        armorStandBoss = new ArmorStandBoss(this);
        magicSealListener = new MagicSealListener(this);
        musicManager = new MusicManager(this);
        shadowRogue = new ShadowRogue(this);
        standManager = new com.Chagui68.stand.StandManager(this);
        vampireManager = new com.Chagui68.stand.VampireManager(this);
        arrowSkeleton = new com.Chagui68.entities.ArrowSkeleton(this);
        flameElemental = new FlameElemental(this);
        frostGolem = new FrostGolem(this);
        voidCrawler = new VoidCrawler(this);
        stormCaller = new StormCaller(this);
        boneShield = new BoneShield(this);
        venomWitch = new VenomWitch(this);
        obsidianGuard = new ObsidianGuard(this);
        soulReaper = new SoulReaper(this);
        chaosMage = new ChaosMage(this);
        enderKnight = new EnderKnight(this);
        kinger = new Kinger(this);
        nixBoss = new NixBoss(this);
        jackStarBoss = new JackStarBoss(this);
        dioBoss = new DioBoss(this);
        discTrader = new DiscTrader(this);
        warlord = new Warlord(this);

        bossDimensionManager = new BossDimensionManager(this);

        ritualManager = new RitualManager(this);

        getServer().getScheduler().runTask(this, () -> {
            bossDimensionManager.createBossDimension();
        });

        MobHandler mobHandler = new MobHandler(this);
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(bossDamageLog, this);
        // El tope de poblacion se calcula aqui, fuera de CreatureSpawnEvent: contar
        // entidades dentro del evento rompe el iterador del mundo. Cada 5 s basta.
        recountTask = getServer().getScheduler().runTaskTimer(this, mobHandler::refrescarRecuento, 100L, 100L);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(this), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
        itemCombatHandler = new ItemCombatHandler(this);
        getServer().getPluginManager().registerEvents(itemCombatHandler, this);
        getServer().getPluginManager().registerEvents(new IceCrownHandler(this), this);
        wirtsLanternHandler = new WirtsLanternHandler(this);
        getServer().getPluginManager().registerEvents(wirtsLanternHandler, this);
        mantisClawsHandler = new MantisClawsHandler(this);
        getServer().getPluginManager().registerEvents(mantisClawsHandler, this);
        getServer().getPluginManager().registerEvents(new MineHandler(this), this);
        getServer().getPluginManager().registerEvents(new RecipeGuardListener(), this);
        getServer().getPluginManager().registerEvents(new BossDimensionCommandHandler(this), this);
        getServer().getPluginManager().registerEvents(new BossDimensionBlockHandler(this), this);
        getServer().getPluginManager().registerEvents(new RitualCandleListener(this), this);
        getServer().getPluginManager().registerEvents(new BossInvocationManager(this), this);
        getServer().getPluginManager().registerEvents(new NixInvocationManager(this), this);
        getServer().getPluginManager().registerEvents(new DioInvocationManager(this), this);
        jackInvocationManager = new JackInvocationManager(this);
        getServer().getPluginManager().registerEvents(jackInvocationManager, this);
        pantheonInvocationManager = new PantheonInvocationManager(this);
        getServer().getPluginManager().registerEvents(pantheonInvocationManager, this);

        getServer().getPluginManager().registerEvents(new CinderGreatswordHandler(this), this);
        getServer().getPluginManager().registerEvents(new VeilwalkerMantleHandler(this), this);
        getServer().getPluginManager().registerEvents(new SoulreapScytheHandler(this), this);
        getServer().getPluginManager().registerEvents(new MarrowAegisHandler(this), this);
        obsidianBastionHandler = new ObsidianBastionHandler(this);
        getServer().getPluginManager().registerEvents(obsidianBastionHandler, this);
        frostHeartOffhandHandler = new FrostHeartOffhandHandler(this);
        getServer().getPluginManager().registerEvents(frostHeartOffhandHandler, this);
        getServer().getPluginManager().registerEvents(new SkyfireTalismanHandler(this), this);
        getServer().getPluginManager().registerEvents(new NullshearEdgeHandler(this), this);
        getServer().getPluginManager().registerEvents(
                new com.Chagui68.listener.weapons.melee.ExecutionerGuillotineHandler(this), this);
        getServer().getPluginManager().registerEvents(
                new com.Chagui68.listener.weapons.ranged.ArchitectDeployerHandler(this), this);
        getServer().getPluginManager().registerEvents(new EightHandledWheelHandler(this), this);
        getServer().getPluginManager().registerEvents(new AetherPullshotHandler(this), this);
        getServer().getPluginManager().registerEvents(new ChaosForgeHandler(), this);
        getServer().getPluginManager().registerEvents(new VenomfangHandler(), this);
        getServer().getPluginManager().registerEvents(new GrimoireHandler(this), this);
        getServer().getPluginManager().registerEvents(new CustomItemPlaceHandler(), this);
        getServer().getPluginManager().registerEvents(new ComponentEventGuard(), this);
        discJukeboxHandler = new DiscJukeboxHandler(this);
        getServer().getPluginManager().registerEvents(discJukeboxHandler, this);

        com.Chagui68.commands.MSCCommand mscCommand = new com.Chagui68.commands.MSCCommand(this, mobHandler);
        getCommand("msc").setExecutor(mscCommand);
        getCommand("msc").setTabCompleter(mscCommand);
        com.Chagui68.stand.StandCommand standCommand = new com.Chagui68.stand.StandCommand(this);
        getCommand("stand").setExecutor(standCommand);
        getCommand("stand").setTabCompleter(standCommand);
        getServer().getPluginManager().registerEvents(new com.Chagui68.wiki.WikiListener(), this);
        getServer().getPluginManager().registerEvents(new com.Chagui68.items.LegacyItemRefresher(), this);
        com.Chagui68.wiki.WikiCommand wikiCommand = new com.Chagui68.wiki.WikiCommand();
        getCommand("wiki").setExecutor(wikiCommand);
        getCommand("wiki").setTabCompleter(wikiCommand);

        // The last line of a good start: the version compatibility check on GitHub waits for it.
        getLogger().info("MultiverseCreatures " + getPluginMeta().getVersion() + " ready on "
                + getServer().getName() + " " + getServer().getMinecraftVersion());
    }

    @Override
    public void onDisable() {
        HeadSlime.clearAllImmunity();
        // Every tick loop the plugin starts stops here. Bukkit would cancel them on its own, but
        // only this side of the shutdown knows which subsystem was running; a handle nobody
        // cancels is just a field (see SchedulerHandleGuardTest).
        stopTasks();
        if (musicManager != null) {
            musicManager.stopAll();
        }
        if (discJukeboxHandler != null) {
            discJukeboxHandler.stopAll();
        }
        if (ritualManager != null) {
            ritualManager.stopAllRituals();
        }

        if (bossDimensionManager != null) {
            bossDimensionManager.unloadBossDimension();
        }
    }

    /** Stops the tick loops that outlive a fight: the mobs, the item auras and the recount. */
    private void stopTasks() {
        if (recountTask != null) {
            recountTask.cancel();
            recountTask = null;
        }
        if (headSlime != null) headSlime.stopTasks();
        if (zombieHorseTrap != null) zombieHorseTrap.stopTasks();
        if (mahoraga != null) mahoraga.stopTasks();
        if (shadowRogue != null) shadowRogue.stopTasks();
        if (arrowSkeleton != null) arrowSkeleton.stopTasks();
        if (standManager != null) standManager.stopAll();
        if (vampireManager != null) vampireManager.stopAll();
        if (flameElemental != null) flameElemental.stopTasks();
        if (frostGolem != null) frostGolem.stopTasks();
        if (voidCrawler != null) voidCrawler.stopTasks();
        if (stormCaller != null) stormCaller.stopTasks();
        if (boneShield != null) boneShield.stopTasks();
        if (venomWitch != null) venomWitch.stopTasks();
        if (obsidianGuard != null) obsidianGuard.stopTasks();
        if (soulReaper != null) soulReaper.stopTasks();
        if (chaosMage != null) chaosMage.stopTasks();
        if (enderKnight != null) enderKnight.stopTasks();
        if (kinger != null) kinger.stopTasks();
        if (nixBoss != null) nixBoss.stopTasks();
        if (jackStarBoss != null) jackStarBoss.stopTasks();
        if (dioBoss != null) dioBoss.stopTasks();
        if (pantheonInvocationManager != null) pantheonInvocationManager.stopTasks();
        if (itemCombatHandler != null) itemCombatHandler.stopTasks();
        if (wirtsLanternHandler != null) wirtsLanternHandler.stopTasks();
        if (mantisClawsHandler != null) mantisClawsHandler.stopTasks();
        if (obsidianBastionHandler != null) obsidianBastionHandler.stopTasks();
        if (frostHeartOffhandHandler != null) frostHeartOffhandHandler.stopTasks();
    }

    public CreeperJr getCreeperJr() {
        return creeperJr;
    }

    public ZombieHorseTrap getZombieHorseTrap() {
        return zombieHorseTrap;
    }

    public HeadSlime getHeadSlime() {
        return headSlime;
    }

    public Mahoraga getMahoraga() {
        return mahoraga;
    }

    public com.Chagui68.entities.miniboss.GarouBoss getGarouBoss() {
        return garouBoss;
    }

    public ArmorStandBoss getArmorStandBoss() {
        return armorStandBoss;
    }

    public BossDamageLog getBossDamageLog() {
        return bossDamageLog;
    }

    public MagicSealListener getMagicSealListener() {
        return magicSealListener;
    }

    public BossDimensionManager getBossDimensionManager() {
        return bossDimensionManager;
    }

    public RitualManager getRitualManager() {
        return ritualManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public com.Chagui68.entities.ArrowSkeleton getArrowSkeleton() {
        return arrowSkeleton;
    }

    public com.Chagui68.stand.StandManager getStandManager() {
        return standManager;
    }

    public com.Chagui68.stand.VampireManager getVampireManager() {
        return vampireManager;
    }

    public ShadowRogue getShadowRogue() {
        return shadowRogue;
    }

    public FlameElemental getFlameElemental() {
        return flameElemental;
    }

    public FrostGolem getFrostGolem() {
        return frostGolem;
    }

    public VoidCrawler getVoidCrawler() {
        return voidCrawler;
    }

    public StormCaller getStormCaller() {
        return stormCaller;
    }

    public BoneShield getBoneShield() {
        return boneShield;
    }

    public VenomWitch getVenomWitch() {
        return venomWitch;
    }

    public ObsidianGuard getObsidianGuard() {
        return obsidianGuard;
    }

    public SoulReaper getSoulReaper() {
        return soulReaper;
    }

    public ChaosMage getChaosMage() {
        return chaosMage;
    }

    public EnderKnight getEnderKnight() {
        return enderKnight;
    }

    public Warlord getWarlord() {
        return warlord;
    }

    public Kinger getKinger() {
        return kinger;
    }

    public NixBoss getNixBoss() {
        return nixBoss;
    }

    public JackStarBoss getJackStarBoss() {
        return jackStarBoss;
    }

    public DioBoss getDioBoss() {
        return dioBoss;
    }

    /** Whether any of the arena bosses, or a summoned DrakesBosses god, is fighting in {@code world}. */
    public boolean isBossFightIn(org.bukkit.World world) {
        return (armorStandBoss != null && armorStandBoss.isBossActiveIn(world))
                || (nixBoss != null && nixBoss.isBossActiveIn(world))
                || (jackStarBoss != null && jackStarBoss.isBossActiveIn(world))
                || (dioBoss != null && dioBoss.isBossActiveIn(world))
                || (pantheonInvocationManager != null && pantheonInvocationManager.isBossActiveIn(world));
    }

    public JackInvocationManager getJackInvocationManager() {
        return jackInvocationManager;
    }

    public DiscTrader getDiscTrader() {
        return discTrader;
    }
}