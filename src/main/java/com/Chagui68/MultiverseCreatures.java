package com.Chagui68;

import com.Chagui68.ability.FreezeAbility;
import com.Chagui68.entities.miniboss.Mahoraga;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.handler.MobHandler;
import com.Chagui68.entities.Kinger;
import com.Chagui68.entities.BoneShield;
import com.Chagui68.entities.ChaosMage;
import com.Chagui68.entities.CreeperJr;
import com.Chagui68.entities.DiscTrader;
import com.Chagui68.entities.miniboss.DioBoss;
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
import com.Chagui68.listener.combat.ItemCombatHandler;
import com.Chagui68.listener.CustomItemPlaceHandler;
import com.Chagui68.listener.ComponentEventGuard;
import com.Chagui68.listener.dio.DioStandHandler;
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

public class MultiverseCreatures extends JavaPlugin {

    private FreezeAbility freezeAbility;
    private DioBoss dioBoss;
    private DioStandHandler dioStandHandler;
    private CreeperJr creeperJr;
    private HeadSlime headSlime;
    private ZombieHorseTrap zombieHorseTrap;
    private Mahoraga mahoraga;
    private ArmorStandBoss armorStandBoss;
    private MagicSealListener magicSealListener;
    private MusicManager musicManager;
    private BossDimensionManager bossDimensionManager;
    private RitualManager ritualManager;
    private ShadowRogue shadowRogue;
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
    private DiscTrader discTrader;
    private Warlord warlord;
    private DiscJukeboxHandler discJukeboxHandler;

    /**
     * Returns whether a config feature (item/entity section) is enabled.
     * Sections live under `items.<name>` or `entities.<name>` and each carries
     * an `enabled` switch. Missing flags default to true (backwards compatible).
     */
    public boolean isEnabled(String section) {
        return getConfig().getBoolean(section + ".enabled", true);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getConfig().getBoolean("recipes.enabled", true)) {
            if (getConfig().getBoolean("recipes.deferred-registration", true)) {
                getServer().getScheduler().runTaskLater(this, RecipeManager::registerRecipes, 40L);
            } else {
                RecipeManager.registerRecipes();
            }
        }

        freezeAbility = new FreezeAbility(this);
        dioBoss = new DioBoss(this);
        dioStandHandler = new DioStandHandler(this);
        creeperJr = new CreeperJr(this);
        headSlime = new HeadSlime(this);
        zombieHorseTrap = new ZombieHorseTrap(this);
        mahoraga = new Mahoraga(this);
        armorStandBoss = new ArmorStandBoss(this);
        magicSealListener = new MagicSealListener(this);
        musicManager = new MusicManager(this);
        shadowRogue = new ShadowRogue(this);
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
        discTrader = new DiscTrader(this);
        warlord = new Warlord(this);

        bossDimensionManager = new BossDimensionManager(this);

        ritualManager = new RitualManager(this);

        getServer().getScheduler().runTask(this, () -> {
            bossDimensionManager.createBossDimension();
        });

        MobHandler mobHandler = new MobHandler(this);
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(this), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemCombatHandler(this), this);
        getServer().getPluginManager().registerEvents(new IceCrownHandler(this), this);
        getServer().getPluginManager().registerEvents(new WirtsLanternHandler(this), this);
        getServer().getPluginManager().registerEvents(new MantisClawsHandler(this), this);
        getServer().getPluginManager().registerEvents(new MineHandler(this), this);
        getServer().getPluginManager().registerEvents(new RecipeGuardListener(), this);
        getServer().getPluginManager().registerEvents(new BossDimensionCommandHandler(this), this);
        getServer().getPluginManager().registerEvents(new BossDimensionBlockHandler(this), this);
        getServer().getPluginManager().registerEvents(new RitualCandleListener(this), this);
        getServer().getPluginManager().registerEvents(new BossInvocationManager(this), this);

        getServer().getPluginManager().registerEvents(new CinderGreatswordHandler(this), this);
        getServer().getPluginManager().registerEvents(new VeilwalkerMantleHandler(this), this);
        getServer().getPluginManager().registerEvents(new SoulreapScytheHandler(this), this);
        getServer().getPluginManager().registerEvents(new MarrowAegisHandler(this), this);
        getServer().getPluginManager().registerEvents(new ObsidianBastionHandler(this), this);
        getServer().getPluginManager().registerEvents(new FrostHeartOffhandHandler(this), this);
        getServer().getPluginManager().registerEvents(new SkyfireTalismanHandler(this), this);
        getServer().getPluginManager().registerEvents(new NullshearEdgeHandler(this), this);
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
    }

    @Override
    public void onDisable() {
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

    public FreezeAbility getFreezeAbility() {
        return freezeAbility;
    }

    public DioBoss getDioBoss() {
        return dioBoss;
    }

    public DioStandHandler getDioStandHandler() {
        return dioStandHandler;
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

    public ArmorStandBoss getArmorStandBoss() {
        return armorStandBoss;
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

    public DiscTrader getDiscTrader() {
        return discTrader;
    }
}