package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.VoidEssence;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VoidCrawler implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, VoidCrawlerInstance> active = new HashMap<>();
    private static final String TAG = "MSC_VoidCrawler";

    public VoidCrawler(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (!plugin.isEnabled("entities.void-crawler")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Spider s : world.getEntitiesByClass(Spider.class)) {
                if (s.getScoreboardTags().contains(TAG)) {
                    active.put(s.getUniqueId(), new VoidCrawlerInstance(s));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    VoidCrawlerInstance inst = entry.getValue();
                    if (inst.spider.isDead() || !inst.spider.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickVoid(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.void-crawler")) return false;
        Spider spider = (Spider) location.getWorld().spawnEntity(location, EntityType.SPIDER);
        if (spider == null) return false;
        spider.addScoreboardTag(TAG);
        spider.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Void Crawler");
        spider.setCustomNameVisible(true);
        spider.setPersistent(true);
        spider.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(spider, Attribute.MAX_HEALTH, 80.0);
        spider.setHealth(80.0);
        MscEntityUtils.setAttribute(spider, Attribute.MOVEMENT_SPEED, 0.35);
        spider.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        spider.setAI(true);
        active.put(spider.getUniqueId(), new VoidCrawlerInstance(spider));
        return true;
    }

    private void tickVoid(VoidCrawlerInstance inst) {
        Spider spider = inst.spider;
        if (!(spider.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            spider.setTarget(null);
            return;
        }

        Location sLoc = spider.getLocation();
        Location tLoc = target.getLocation();
        if (!sLoc.getWorld().equals(tLoc.getWorld())) {
            spider.setTarget(null);
            return;
        }
        double dist = sLoc.distance(tLoc);
        inst.phaseCooldown++;
        inst.poisonBurstCooldown++;

        if (dist > 4 && inst.phaseCooldown > 50) {
            if (sLoc.getBlock().getType().isSolid()) {
                Location free = findFreeSpace(spider);
                if (free != null) {
                    spider.teleport(free);
                    sLoc.getWorld().spawnParticle(Particle.PORTAL, sLoc, 20, 0.5, 0.5, 0.5, 0.05);
                    sLoc.getWorld().spawnParticle(Particle.PORTAL, free, 20, 0.5, 0.5, 0.5, 0.05);
                    sLoc.getWorld().playSound(free, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.6f);
                    inst.phaseCooldown = 0;
                }
            } else if (random.nextDouble() < 0.1) {
                List<Location> blocks = new ArrayList<>();
                for (int x = -2; x <= 2; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -2; z <= 2; z++) {
                            Location loc = sLoc.clone().add(x, y, z);
                            if (!loc.getBlock().getType().isSolid() && loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                                blocks.add(loc);
                            }
                        }
                    }
                }
                if (!blocks.isEmpty()) {
                    Location newLoc = blocks.get(random.nextInt(blocks.size()));
                    spider.teleport(newLoc);
                    sLoc.getWorld().spawnParticle(Particle.PORTAL, sLoc, 15, 0.3, 0.3, 0.3, 0.03);
                    sLoc.getWorld().spawnParticle(Particle.PORTAL, newLoc, 15, 0.3, 0.3, 0.3, 0.03);
                    inst.phaseCooldown = 0;
                }
            }
        }

        if (dist < 6 && inst.poisonBurstCooldown > 60) {
            for (int a = 0; a < 12; a++) {
                double angle = (2 * Math.PI * a / 12);
                double r = 3.0;
                Location pl = sLoc.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                sLoc.getWorld().spawnParticle(Particle.DUST, pl, 3, 0.2, 0.2, 0.2, 0,
                        new Particle.DustOptions(Color.fromRGB(0x8800AA), 1.5f));
                sLoc.getWorld().spawnParticle(Particle.WITCH, pl, 2, 0.2, 0.2, 0.2, 0);
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
            MscEntityUtils.damageBy(spider, target, 6.0);
            sLoc.getWorld().playSound(sLoc, Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 0.5f);
            inst.poisonBurstCooldown = 0;
        }
    }

    private Location findFreeSpace(Spider spider) {
        for (int i = 0; i < 10; i++) {
            Location loc = spider.getLocation().add(
                    (random.nextDouble() - 0.5) * 10, 0, (random.nextDouble() - 0.5) * 10);
            loc.setY(loc.getWorld().getHighestBlockYAt(loc));
            if (!loc.getBlock().getType().isSolid() && loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                return loc;
            }
        }
        return null;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Spider spider)) return;
        if (!spider.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Spider spider)) return;
        if (!spider.getScoreboardTags().contains(TAG)) return;
        active.remove(spider.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < 0.5) {
            spider.getWorld().dropItemNaturally(spider.getLocation(), VoidEssence.VOID_ESSENCE.clone());
        }
        event.setDroppedExp(35);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.void-crawler.death-messages");
    }

    private static class VoidCrawlerInstance {
        final Spider spider;
        int phaseCooldown = 0;
        int poisonBurstCooldown = 0;

        VoidCrawlerInstance(Spider s) {
            this.spider = s;
        }
    }
}