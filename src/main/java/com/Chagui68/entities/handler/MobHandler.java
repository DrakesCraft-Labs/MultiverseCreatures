package com.Chagui68.entities.handler;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.misc.IceCrown;
import com.Chagui68.items.weapons.melee.Excalibur;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.items.misc.MantisClaws;
import com.Chagui68.items.components.StarCore;
import com.Chagui68.items.misc.WirtsLantern;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.Bukkit;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import static com.Chagui68.items.weapons.melee.Excalibur.EXCALIBUR_SWORD;
import static com.Chagui68.items.food.ScoobyCookie.SCOOBY_COOKIE;

public class MobHandler implements Listener {

    private static final double SHAGGY_CHANCE = 0.3;

    private final Random random = new Random();
    private final MultiverseCreatures plugin;
    private final Map<EntityType, Long> lastSpawnFailureLog = new EnumMap<>(EntityType.class);

    private double spawnRateMultiplier;
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
    private double garouChance;
    private double zombieHorseTrapChance;
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
        mahoragaChance = config.getDouble("entities.mahoraga.spawn-chance", 0.002) * spawnRateMultiplier;
        garouChance = config.getDouble("entities.garou.spawn-chance", 0.0015) * spawnRateMultiplier;
        obsidianGuardChance = config.getDouble("entities.obsidian-guard.spawn-chance", 0.02) * spawnRateMultiplier;
        headSlimeChance = config.getDouble("entities.head-slime.spawn-chance", 0.1) * spawnRateMultiplier;
        creeperJrChance = config.getDouble("entities.creeper-jr.spawn-chance", 0.15) * spawnRateMultiplier;
        shadowRogueChance = config.getDouble("entities.shadow-rogue.spawn-chance", 0.05) * spawnRateMultiplier;
        boneShieldChance = config.getDouble("entities.bone-shield.spawn-chance", 0.06) * spawnRateMultiplier;
        flameElementalChance = config.getDouble("entities.flame-elemental.spawn-chance", 0.1) * spawnRateMultiplier;
        frostGolemBuildChance = config.getDouble("entities.frost-golem.build-spawn-chance", 0.2) * spawnRateMultiplier;
        voidCrawlerChance = config.getDouble("entities.void-crawler.spawn-chance", 0.07) * spawnRateMultiplier;
        stormCallerChance = config.getDouble("entities.storm-caller.spawn-chance", 0.04) * spawnRateMultiplier;
        venomWitchChance = config.getDouble("entities.venom-witch.spawn-chance", 0.05) * spawnRateMultiplier;
        soulReaperChance = config.getDouble("entities.soul-reaper.spawn-chance", 0.05) * spawnRateMultiplier;
        chaosMageChance = config.getDouble("entities.chaos-mage.spawn-chance", 0.4) * spawnRateMultiplier;
        enderKnightChance = config.getDouble("entities.ender-knight.spawn-chance", 0.04) * spawnRateMultiplier;
        discTraderChance = config.getDouble("entities.disc-trader.spawn-chance", 0.05) * spawnRateMultiplier;
        warlordChance = config.getDouble("entities.warlord.spawn-chance", 0.1) * spawnRateMultiplier;
        warlordRaidChance = config.getDouble("entities.warlord.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        stormCallerRaidChance = config.getDouble("entities.storm-caller.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        venomWitchRaidChance = config.getDouble("entities.venom-witch.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        chaosMageRaidChance = config.getDouble("entities.chaos-mage.raid-spawn-chance", 0.5) * spawnRateMultiplier;
        zombieHorseTrapChance = config.getDouble("entities.zombie-horse-trap.spawn-chance", 0.001);
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
        // Bloquear terminantemente generadores de mobs (spawners y trial spawners)
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
            return;
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.PATROL
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
            return;
        }

        Location loc = event.getLocation();
        EntityType type = event.getEntityType();

        try {
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
        } catch (RuntimeException exception) {
            logSpawnFailure(type, event, exception);
        }
    }

    /**
     * Keeps a broken optional conversion from failing Minecraft's spawn event and
     * flooding production logs. One complete diagnostic is retained per mob type
     * and minute so the actual conversion can be repaired without hiding it.
     */
    private void logSpawnFailure(EntityType type, CreatureSpawnEvent event, RuntimeException exception) {
        long now = System.currentTimeMillis();
        long lastLog = lastSpawnFailureLog.getOrDefault(type, 0L);
        if (now - lastLog < 60_000L) {
            return;
        }

        lastSpawnFailureLog.put(type, now);
        plugin.getLogger().log(
                Level.SEVERE,
                "Failed optional conversion for " + type + " (reason=" + event.getSpawnReason()
                        + ", world=" + event.getLocation().getWorld().getName()
                        + ", x=" + event.getLocation().getBlockX()
                        + ", y=" + event.getLocation().getBlockY()
                        + ", z=" + event.getLocation().getBlockZ() + ")",
                exception
        );
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
            if (!plugin.isEnabled("entities.storm-caller")) return;
            Witch witch = (Witch) event.getEntity();
            scheduleRaidConversion(() -> plugin.getStormCaller().convertExisting(witch));
            return;
        }
        if (random.nextDouble() < venomWitchRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting witch to Venom Witch at " + event.getLocation());
            if (!plugin.isEnabled("entities.venom-witch")) return;
            Witch witch = (Witch) event.getEntity();
            scheduleRaidConversion(() -> plugin.getVenomWitch().convertExisting(witch));
        }
    }

    private void handleRaidEvokerSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < chaosMageRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting evoker to Chaos Mage at " + event.getLocation());
            if (!plugin.isEnabled("entities.chaos-mage")) return;
            Evoker evoker = (Evoker) event.getEntity();
            scheduleRaidConversion(() -> plugin.getChaosMage().convertExisting(evoker));
        }
    }

    private void handleRaidPillagerSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < warlordRaidChance) {
            if (debug) plugin.getLogger().info("[MobHandler] Converting pillager to Warlord at " + event.getLocation());
            if (!plugin.isEnabled("entities.warlord")) return;
            Pillager pillager = (Pillager) event.getEntity();
            scheduleRaidConversion(() -> plugin.getWarlord().convertExisting(pillager));
        }
    }

    private void scheduleRaidConversion(Runnable conversion) {
        Bukkit.getScheduler().runTask(plugin, conversion);
    }

    private void handleZombieSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        World world = loc.getWorld();
        if (world == null) return;

        if (world.getMoonPhase() == MoonPhase.FULL_MOON && random.nextDouble() < zombieHorseTrapChance) {
            if (!plugin.isEnabled("entities.zombie-horse-trap")) return;
            event.setCancelled(true);
            plugin.getZombieHorseTrap().trySpawn(loc);
            return;
        }

        if (random.nextDouble() < mahoragaChance) {
            if (!plugin.isEnabled("entities.mahoraga")) return;
            boolean hasMahoraga = !world.getNearbyEntities(loc, 64, 32, 64, e -> e.getScoreboardTags().contains("MSC_Mahoraga")).isEmpty();
            if (hasMahoraga) return;
            event.setCancelled(true);
            plugin.getMahoraga().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < obsidianGuardChance) {
            if (!plugin.isEnabled("entities.obsidian-guard")) return;
            event.setCancelled(true);
            plugin.getObsidianGuard().trySpawn(loc);
        }
    }

    private void handleSlimeSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getEntity().getScoreboardTags().contains("MSC_HeadSlime")) return;

        if (random.nextDouble() < headSlimeChance) {
            if (!plugin.isEnabled("entities.head-slime")) return;
            event.setCancelled(true);
            plugin.getHeadSlime().trySpawn(loc);
        }
    }

    private void handleCreeperSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getEntity().getScoreboardTags().contains("MSC_CreeperJr")) return;

        if (random.nextDouble() < creeperJrChance) {
            if (!plugin.isEnabled("entities.creeper-jr")) return;
            event.setCancelled(true);
            plugin.getCreeperJr().trySpawn(loc);
        }
    }

    private void handleSkeletonSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < shadowRogueChance) {
            if (!plugin.isEnabled("entities.shadow-rogue")) return;
            event.setCancelled(true);
            plugin.getShadowRogue().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < boneShieldChance) {
            if (!plugin.isEnabled("entities.bone-shield")) return;
            event.setCancelled(true);
            plugin.getBoneShield().trySpawn(loc);
        }
    }

    private void handleBlazeSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < flameElementalChance) {
            if (!plugin.isEnabled("entities.flame-elemental")) return;
            event.setCancelled(true);
            plugin.getFlameElemental().trySpawn(loc);
        }
    }

    private void handleGolemSpawn(CreatureSpawnEvent event, Location loc) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) return;
        if (random.nextDouble() < frostGolemBuildChance) {
            if (!plugin.isEnabled("entities.frost-golem")) return;
            event.setCancelled(true);
            plugin.getFrostGolem().trySpawn(loc);
        }
    }

    private void handleSpiderSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < voidCrawlerChance) {
            if (!plugin.isEnabled("entities.void-crawler")) return;
            event.setCancelled(true);
            plugin.getVoidCrawler().trySpawn(loc);
        }
    }

    private void handleWitchSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < stormCallerChance) {
            if (!plugin.isEnabled("entities.storm-caller")) return;
            event.setCancelled(true);
            plugin.getStormCaller().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < venomWitchChance) {
            if (!plugin.isEnabled("entities.venom-witch")) return;
            event.setCancelled(true);
            plugin.getVenomWitch().trySpawn(loc);
        }
    }

    private void handleWitherSkeletonSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < garouChance) {
            if (!plugin.isEnabled("entities.garou")) return;
            if (loc.getWorld() != null) {
                boolean hasGarou = !loc.getWorld().getNearbyEntities(loc, 64, 32, 64, e -> e.getScoreboardTags().contains("MSC_Garou")).isEmpty();
                if (hasGarou) return;
            }
            event.setCancelled(true);
            plugin.getGarouBoss().trySpawn(loc);
            return;
        }
        if (random.nextDouble() < soulReaperChance) {
            if (!plugin.isEnabled("entities.soul-reaper")) return;
            event.setCancelled(true);
            plugin.getSoulReaper().trySpawn(loc);
        }
    }

    private void handleEvokerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < chaosMageChance) {
            if (!plugin.isEnabled("entities.chaos-mage")) return;
            event.setCancelled(true);
            plugin.getChaosMage().trySpawn(loc);
        }
    }

    private void handlePillagerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < warlordChance) {
            if (!plugin.isEnabled("entities.warlord")) return;
            event.setCancelled(true);
            plugin.getWarlord().trySpawn(loc);
        }
    }

    private void handleEndermanSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < enderKnightChance) {
            if (!plugin.isEnabled("entities.ender-knight")) return;
            event.setCancelled(true);
            plugin.getEnderKnight().trySpawn(loc);
        }
    }

    private void handleVillagerSpawn(CreatureSpawnEvent event, Location loc) {
        if (random.nextDouble() < discTraderChance) {
            if (!plugin.isEnabled("entities.disc-trader")) return;
            event.setCancelled(true);
            plugin.getDiscTrader().trySpawn(loc);
        }
    }

    private void handleTraderSpawn(CreatureSpawnEvent event) {
        if (random.nextDouble() < SHAGGY_CHANCE) {
            if (!plugin.isEnabled("entities.merchant")) return;
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
        if (!plugin.isEnabled("entities.merchant")) return;
        WanderingTrader shaggy = (WanderingTrader) location.getWorld().spawnEntity(location, EntityType.WANDERING_TRADER);
        equipWanderingVillager(shaggy);
    }
}
