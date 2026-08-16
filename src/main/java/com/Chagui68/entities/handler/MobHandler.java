package com.Chagui68.entities.handler;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.misc.IceCrown;
import com.Chagui68.items.weapons.melee.Excalibur;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.items.misc.MantisClaws;
import com.Chagui68.items.components.StarCore;
import com.Chagui68.items.misc.WirtsLantern;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.Chagui68.items.weapons.melee.Excalibur.EXCALIBUR_SWORD;
import static com.Chagui68.items.food.ScoobyCookie.SCOOBY_COOKIE;

public class MobHandler implements Listener {

    private static final double SHAGGY_CHANCE = 0.3;

    private final Random random = new Random();
    private final MultiverseCreatures plugin;

    private double spawnRateMultiplier;
    private double dioBossChance;
    private double mahoragaChance;
    private double obsidianGuardChance;
    private double headSlimeChance;
    private double creeperJrChance;
    private double shadowRogueChance;
    private double boneShieldChance;
    private double flameElementalChance;
    private double frostGolemBuildChance;
    private double voidCrawlerChance;
    private double stormCallerChance;
    private double venomWitchChance;
    private double soulReaperChance;
    private double chaosMageChance;
    private double enderKnightChance;
    private double discTraderChance;
    private double warlordChance;
    private double warlordRaidChance;
    private double stormCallerRaidChance;
    private double venomWitchRaidChance;
    private double chaosMageRaidChance;
    private boolean debug;

    public MobHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        spawnRateMultiplier = config.getDouble("general.spawn-rate-multiplier", 0.5);
        dioBossChance = config.getDouble("dio-boss.spawn-chance", 0.005) * spawnRateMultiplier;
        mahoragaChance = config.getDouble("mahoraga.spawn-chance", 0.02) * spawnRateMultiplier;
        obsidianGuardChance = config.getDouble("obsidian-guard.spawn-chance", 0.02) * spawnRateMultiplier;
        headSlimeChance = config.getDouble("head-slime.spawn-chance", 0.1) * spawnRateMultiplier;
        creeperJrChance = config.getDouble("creeper-jr.spawn-chance", 0.15) * spawnRateMultiplier;
        shadowRogueChance = config.getDouble("shadow-rogue.spawn-chance", 0.05) * spawnRateMultiplier;
        boneShieldChance = config.getDouble("bone-shield.spawn-chance", 0.06) * spawnRateMultiplier;
        flameElementalChance = config.getDouble("flame-elemental.spawn-chance", 0.1) * spawnRateMultiplier;
        frostGolemBuildChance = config.getDouble("frost-golem.build-spawn-chance", 0.2) * spawnRateMultiplier;
        voidCrawlerChance = config.getDouble("void-crawler.spawn-chance", 0.07) * spawnRateMultiplier;
        stormCallerChance = config.getDouble("storm-caller.spawn-chance", 0.04) * spawnRateMultiplier;
        venomWitchChance = config.getDouble("venom-witch.spawn-chance", 0.05) * spawnRateMultiplier;
        soulReaperChance = config.getDouble("soul-reaper.spawn-chance", 0.05) * spawnRateMultiplier;
        chaosMageChance = config.getDouble("chaos-mage.spawn-chance", 0.06) * spawnRateMultiplier;
        enderKnightChance = config.getDouble("ender-knight.spawn-chance", 0.04) * spawnRateMultiplier;
        discTraderChance = config.getDouble("disc-trader.spawn-chance", 0.05) * spawnRateMultiplier;
        warlordChance = config.getDouble("warlord.spawn-chance", 0.1) * spawnRateMultiplier;
        warlordRaidChance = config.getDouble("warlord.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        stormCallerRaidChance = config.getDouble("storm-caller.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        venomWitchRaidChance = config.getDouble("venom-witch.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        chaosMageRaidChance = config.getDouble("chaos-mage.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        debug = config.getBoolean("general.debug", false);
        if (debug) {
            plugin.getLogger().info("[MobHandler] spawnRateMultiplier=" + spawnRateMultiplier
                    + " warlordRaidChance=" + warlordRaidChance
                    + " stormCallerRaidChance=" + stormCallerRaidChance
                    + " venomWitchRaidChance=" + venomWitchRaidChance
                    + " chaosMageRaidChance=" + chaosMageRaidChance);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BREEDING
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.PATROL
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
            return;
        }

        Location loc = event.getLocation();
        EntityType type = event.getEntityType();

        switch (type) {
            case ZOMBIE -> handleZombieSpawn(event, loc);
            case SLIME -> handleSlimeSpawn(event, loc);
            case CREEPER -> handleCreeperSpawn(event, loc);
            case WANDERING_TRADER -> handleTraderSpawn(event);
            case SKELETON -> handleSkeletonSpawn(event, loc);
            case BLAZE -> handleBlazeSpawn(event, loc);
            case IRON_GOLEM -> handleGolemSpawn(event, loc);
            case SPIDER -> handleSpiderSpawn(event, loc);
            case WITCH -> handleWitchSpawn(event, loc);
            case WITHER_SKELETON -> handleWitherSkeletonSpawn(event, loc);
            case EVOKER -> handleEvokerSpawn(event, loc);
            case PILLAGER -> handlePillagerSpawn(event, loc);
            case ENDERMAN -> handleEndermanSpawn(event, loc);
            case VILLAGER -> handleVillagerSpawn(event, loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRaidSpawn(CreatureSpawnEvent event) {
        if (debug) {
            EntityType type = event.getEntityType();
            if (type == EntityType.WITCH || type == EntityType.EVOKER || type == EntityType.PILLAGER
                    || type == EntityType.RAVAGER || type == EntityType.VINDICATOR) {
                plugin.getLogger().info("[MobHandler] Raid-tracked spawn: " + type
                        + " reason=" + event.getSpawnReason() + " at " + event.getLocation());
            }
        }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.RAID) return;
        switch (event.getEntityType()) {
            case WITCH -> handleRaidWitchSpawn(event);
            case EVOKER -> handleRaidEvokerSpawn(event);
            case PILLAGER -> handleRaidPillagerSpawn(event);
        }
    }

    private void handleRaidWitchSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < stormCallerRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting witch to Storm Caller at " + event.getLocation());
            plugin.getStormCaller().convertExisting((Witch) event.getEntity());
            return;
        }
        if (random.nextDouble() < venomWitchRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting witch to Venom Witch at " + event.getLocation());
            plugin.getVenomWitch().convertExisting((Witch) event.getEntity());
        }
    }

    private void handleRaidEvokerSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < chaosMageRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting evoker to Chaos Mage at " + event.getLocation());
            plugin.getChaosMage().convertExisting((Evoker) event.getEntity());
        }
    }

    private void handleRaidPillagerSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < warlordRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting pillager to Warlord at " + event.getLocation());
            plugin.getWarlord().convertExisting((Pillager) event.getEntity());
        }
    }

    private void handleZombieSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        World world = loc.getWorld();

        if (random.nextDouble() < dioBossChance) {
            event.setCancelled(true);
            plugin.getDioBoss().trySpawnDio(loc);
            return;
        }

        if (world.getMoonPhase() == MoonPhase.FULL_MOON && random.nextDouble() < 0.001) {
            event.setCancelled(true);
            plugin.getZombieHorseTrap().trySpawn(loc);
            return;
        }

        if (random.nextDouble() < mahoragaChance) {
            event.setCancelled(true);
            plugin.getMahoraga().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < obsidianGuardChance) {
            event.setCancelled(true);
            plugin.getObsidianGuard().trySpawn(loc);
        }
    }

    private void handleSlimeSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getEntity().getScoreboardTags().contains("MSC_HeadSlime")) return;

        if (random.nextDouble() < headSlimeChance) {
            event.setCancelled(true);
            plugin.getHeadSlime().trySpawn(loc);
        }
    }

    private void handleCreeperSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getEntity().getScoreboardTags().contains("MSC_CreeperJr")) return;

        if (random.nextDouble() < creeperJrChance) {
            event.setCancelled(true);
            plugin.getCreeperJr().trySpawn(loc);
        }
    }

    private void handleSkeletonSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < shadowRogueChance) {
            event.setCancelled(true);
            plugin.getShadowRogue().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < boneShieldChance) {
            event.setCancelled(true);
            plugin.getBoneShield().trySpawn(loc);
        }
    }

    private void handleBlazeSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < flameElementalChance) {
            event.setCancelled(true);
            plugin.getFlameElemental().trySpawn(loc);
        }
    }

    private void handleGolemSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) return;
        if (random.nextDouble() < frostGolemBuildChance) {
            event.setCancelled(true);
            plugin.getFrostGolem().trySpawn(loc);
        }
    }

    private void handleSpiderSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < voidCrawlerChance) {
            event.setCancelled(true);
            plugin.getVoidCrawler().trySpawn(loc);
        }
    }

    private void handleWitchSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < stormCallerChance) {
            event.setCancelled(true);
            plugin.getStormCaller().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < venomWitchChance) {
            event.setCancelled(true);
            plugin.getVenomWitch().trySpawn(loc);
        }
    }

    private void handleWitherSkeletonSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < soulReaperChance) {
            event.setCancelled(true);
            plugin.getSoulReaper().trySpawn(loc);
        }
    }

    private void handleEvokerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < chaosMageChance) {
            event.setCancelled(true);
            plugin.getChaosMage().trySpawn(loc);
        }
    }

    private void handlePillagerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < warlordChance) {
            event.setCancelled(true);
            plugin.getWarlord().trySpawn(loc);
        }
    }

    private void handleEndermanSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < enderKnightChance) {
            event.setCancelled(true);
            plugin.getEnderKnight().trySpawn(loc);
        }
    }

    private void handleVillagerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < discTraderChance) {
            event.setCancelled(true);
            plugin.getDiscTrader().trySpawn(loc);
        }
    }

    private void handleTraderSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < SHAGGY_CHANCE) {
            equipWanderingVillager((WanderingTrader) event.getEntity());
        }
    }

    public void equipWanderingVillager(WanderingTrader trader) {
        List<MerchantRecipe> trades = new ArrayList<>();
        trader.setCustomName(ChatColor.GOLD + "Multiverse Merchant");
        trader.setCustomNameVisible(true);

        ItemStack cookies = SCOOBY_COOKIE.clone();
        cookies.setAmount(5);
        MerchantRecipe cookiesTrade = new MerchantRecipe(cookies, 999);
        cookiesTrade.addIngredient(new ItemStack(Material.DIAMOND, 20));
        trades.add(cookiesTrade);

        ItemStack excalibur = EXCALIBUR_SWORD.clone();
        MerchantRecipe excaliburTrade = new MerchantRecipe(excalibur, 1);
        ItemStack starCoreIngredient = StarCore.STAR_CORE.clone();
        starCoreIngredient.setAmount(16);
        excaliburTrade.addIngredient(starCoreIngredient);
        excaliburTrade.addIngredient(new ItemStack(Material.NETHERITE_INGOT, 32));
        trades.add(excaliburTrade);

        ItemStack iceCrown = IceCrown.ICE_CROWN.clone();
        MerchantRecipe iceCrownTrade = new MerchantRecipe(iceCrown, 1);
        iceCrownTrade.addIngredient(new ItemStack(Material.NETHER_STAR, 48));
        iceCrownTrade.addIngredient(new ItemStack(Material.BLUE_ICE, 64));
        trades.add(iceCrownTrade);

        ItemStack wirtsLantern = WirtsLantern.WIRTS_LANTERN.clone();
        MerchantRecipe lanternTrade = new MerchantRecipe(wirtsLantern, 1);
        lanternTrade.addIngredient(new ItemStack(Material.SOUL_SAND, 32));
        lanternTrade.addIngredient(new ItemStack(Material.SOUL_SOIL, 16));
        trades.add(lanternTrade);

        ItemStack mantisClaws = MantisClaws.MANTIS_CLAWS_ITEM.clone();
        MerchantRecipe mantisTrade = new MerchantRecipe(mantisClaws, 999);
        mantisTrade.addIngredient(new ItemStack(Material.IRON_INGOT, 16));
        mantisTrade.addIngredient(new ItemStack(Material.STRING, 8));
        trades.add(mantisTrade);

        trader.setRecipes(trades);
        trader.addScoreboardTag("MSC_MultiverseMerchant");
    }

    public void spawnShaggy(Location location) {
        WanderingTrader shaggy = (WanderingTrader) location.getWorld().spawnEntity(location, EntityType.WANDERING_TRADER);
        equipWanderingVillager(shaggy);
    }
}