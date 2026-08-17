package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.FrostHeart;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FrostGolem implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, FrostGolemInstance> active = new HashMap<>();
    private static final String TAG = "MSC_FrostGolem";

    public FrostGolem(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (placed.getType() != Material.CARVED_PUMPKIN && placed.getType() != Material.JACK_O_LANTERN) return;

        Block below = placed.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.PACKED_ICE && below.getType() != Material.BLUE_ICE) return;

        Block below2 = below.getRelative(BlockFace.DOWN);
        if (below2.getType() != Material.PACKED_ICE && below2.getType() != Material.BLUE_ICE) return;

        Block leftArm = below.getRelative(BlockFace.WEST);
        Block rightArm = below.getRelative(BlockFace.EAST);
        boolean armsValid = (leftArm.getType() == Material.ICE || leftArm.getType() == Material.PACKED_ICE || leftArm.getType() == Material.BLUE_ICE)
                && (rightArm.getType() == Material.ICE || rightArm.getType() == Material.PACKED_ICE || rightArm.getType() == Material.BLUE_ICE);

        if (!armsValid) return;

        leftArm.setType(Material.AIR);
        rightArm.setType(Material.AIR);
        below.setType(Material.AIR);
        below2.setType(Material.AIR);
        placed.setType(Material.AIR);

        Location spawnLoc = below2.getLocation().add(0.5, 0, 0.5);
        trySpawn(spawnLoc);

        spawnLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, spawnLoc, 30, 0.5, 1, 0.5, 0.05);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.6f);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (IronGolem g : world.getEntitiesByClass(IronGolem.class)) {
                if (g.getScoreboardTags().contains(TAG)) {
                    active.put(g.getUniqueId(), new FrostGolemInstance(g));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    FrostGolemInstance inst = entry.getValue();
                    if (inst.golem.isDead() || !inst.golem.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickFrost(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        IronGolem golem = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);
        if (golem == null) return false;
        golem.addScoreboardTag(TAG);
        golem.setCustomName(ChatColor.AQUA + "" + ChatColor.BOLD + "Frost Golem");
        golem.setCustomNameVisible(true);
        golem.setPersistent(true);
        golem.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(golem, Attribute.MAX_HEALTH, 200.0);
        golem.setHealth(200.0);
        MscEntityUtils.setAttribute(golem, Attribute.ATTACK_DAMAGE, 12.0);
        MscEntityUtils.setAttribute(golem, Attribute.MOVEMENT_SPEED, 0.18);
        golem.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment eq = golem.getEquipment();
        if (eq != null) {
            ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
            if (meta != null) {
                meta.setColor(Color.AQUA);
                meta.setUnbreakable(true);
                chest.setItemMeta(meta);
            }
            eq.setChestplate(chest);
            eq.setChestplateDropChance(0);
        }
        golem.setAI(true);
        active.put(golem.getUniqueId(), new FrostGolemInstance(golem));
        return true;
    }

    private void tickFrost(FrostGolemInstance inst) {
        IronGolem golem = inst.golem;
        if (!(golem.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            golem.setTarget(null);
            return;
        }

        Location gLoc = golem.getLocation();
        Location tLoc = target.getLocation();
        double dist = gLoc.distance(tLoc);
        inst.iceAuraCooldown++;
        inst.freezeBeamCooldown++;

        if (dist < 8 && inst.iceAuraCooldown > 40) {
            for (int a = 0; a < 16; a++) {
                double angle = (2 * Math.PI * a / 16);
                double r = 4.0;
                double x = gLoc.getX() + Math.cos(angle) * r;
                double z = gLoc.getZ() + Math.sin(angle) * r;
                Location pl = new Location(gLoc.getWorld(), x, gLoc.getY() + 0.2, z);
                gLoc.getWorld().spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x88DDFF), 2.0f));
                gLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, pl, 2, 0.2, 0.1, 0.2, 0);
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
            target.damage(4.0);
            gLoc.getWorld().playSound(gLoc, Sound.BLOCK_POWDER_SNOW_BREAK, 1.0f, 0.7f);
            inst.iceAuraCooldown = 0;
        }

        if (dist > 5 && dist < 20 && inst.freezeBeamCooldown > 80) {
            Vector dir = tLoc.toVector().subtract(gLoc.toVector()).normalize();
            for (double d = 0; d < dist; d += 0.5) {
                Location pl = gLoc.clone().add(dir.clone().multiply(d));
                pl.setY(pl.getY() + 1.5);
                gLoc.getWorld().spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x88DDFF), 1.8f));
                gLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, pl, 1, 0, 0, 0, 0);
            }
            target.damage(10.0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, -4));
            tLoc.getWorld().playSound(tLoc, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.2f, 0.8f);
            inst.freezeBeamCooldown = 0;
        }
    }

    @EventHandler
    public void onPotionApply(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)) return;
        if (!golem.getScoreboardTags().contains(TAG)) return;
        // getNewEffect() es null cuando el evento es la RETIRADA de un efecto, no su aplicacion.
        // Este manejador escucha EntityPotionEffectEvent sin filtrar, asi que le llega cualquier
        // efecto de cualquier entidad del servidor: en los logs del 16 esto reventaba decenas de
        // veces seguidas y se llevaba por delante el evento entero.
        PotionEffect nuevo = event.getNewEffect();
        if (nuevo == null) return;
        PotionEffectType type = nuevo.getType();
        if (type == PotionEffectType.SLOWNESS || type == PotionEffectType.WEAKNESS || type == PotionEffectType.JUMP_BOOST) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof IronGolem golem)) return;
        if (!golem.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
            p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)) return;
        if (!golem.getScoreboardTags().contains(TAG)) return;
        active.remove(golem.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < 0.75) {
            golem.getWorld().dropItemNaturally(golem.getLocation(), FrostHeart.FROST_HEART.clone());
        }
        event.setDroppedExp(80);
    }

    private static class FrostGolemInstance {
        final IronGolem golem;
        int iceAuraCooldown = 0;
        int freezeBeamCooldown = 0;

        FrostGolemInstance(IronGolem g) {
            this.golem = g;
        }
    }
}