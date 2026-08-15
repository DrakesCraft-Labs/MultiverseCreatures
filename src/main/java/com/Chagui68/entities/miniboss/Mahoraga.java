package com.Chagui68.entities.miniboss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.integration.SlimefunArmorAdaptation;
import com.Chagui68.items.components.WheelEssence;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Mahoraga implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Set<UUID> adapters = new HashSet<>();
    private final Set<UUID> removeQueue = new HashSet<>();
    private final boolean slimefunAdaptation;
    private final boolean instakillInfinityArmor;
    private final boolean ignoreDiamondMod;
    private final boolean infinityWeaponAdaptation;
    private static final String TAG = "MSC_Mahoraga";
    private static final double IGNORE_DIAMOND_CHANCE = 1.0;
    private static final double INFINITY_WEAPON_DAMAGE = 1.0;

    public Mahoraga(MultiverseCreatures plugin) {
        this.plugin = plugin;
        this.slimefunAdaptation = plugin.getConfig().getBoolean("mahoraga.slimefun-adaptation", true);
        this.instakillInfinityArmor = plugin.getConfig().getBoolean("mahoraga.instakill-infinity-armor", true);
        this.ignoreDiamondMod = plugin.getConfig().getBoolean("mahoraga.ignore-diamond-mod", true);
        this.infinityWeaponAdaptation = plugin.getConfig().getBoolean("mahoraga.infinity-weapon-adaptation", true);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Zombie zombie : world.getEntitiesByClass(Zombie.class)) {
                if (zombie.getScoreboardTags().contains(TAG)) {
                    adapters.add(zombie.getUniqueId());
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : Set.copyOf(adapters)) {
                    if (removeQueue.contains(id)) continue;
                    Entity e = Bukkit.getEntity(id);
                    if (e == null || e.isDead() || !e.isValid()) {
                        removeQueue.add(id);
                        continue;
                    }
                    if (!(e instanceof Zombie zombie)) continue;
                    if (!zombie.getWorld().isChunkLoaded(zombie.getLocation().getChunk())) continue;
                    tickAdapter(zombie);
                }
                for (UUID id : removeQueue) {
                    adapters.remove(id);
                }
                removeQueue.clear();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void tickAdapter(Zombie zombie) {
        if (!(zombie.getTarget() instanceof Player target) || target.isDead() || !target.isOnline()) {
            resetAdaptation(zombie);
            return;
        }
        if (target.getGameMode() == org.bukkit.GameMode.CREATIVE || target.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            zombie.setTarget(null);
            resetAdaptation(zombie);
            return;
        }

        double bonus = 0;
        int totalOverLevel = 0;
        int totalKnockback = 0;

        for (ItemStack armor : target.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir()) continue;

            Material type = armor.getType();
            double armorBonus = getArmorBonus(type);
            bonus += armorBonus;

            int protLevel = armor.getEnchantmentLevel(Enchantment.PROTECTION);
            if (protLevel > 0) {
                bonus += getProtectionBonus(type) * protLevel;
            }

            if (slimefunAdaptation) {
                bonus += SlimefunArmorAdaptation.getBonus(armor);
            }

            int thornsLevel = armor.getEnchantmentLevel(Enchantment.THORNS);
            bonus += thornsLevel * 0.5;

            totalKnockback += armor.getEnchantmentLevel(Enchantment.KNOCKBACK);

            if (isDiamondOrNetherite(type) && protLevel > 5) {
                totalOverLevel += protLevel - 5;
            }
        }

        totalKnockback += target.getInventory().getItemInOffHand().getEnchantmentLevel(Enchantment.KNOCKBACK);

        int maxSharpness = 0;
        int maxKnockback = 0;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            int sharp = item.getEnchantmentLevel(Enchantment.SHARPNESS);
            int smite = item.getEnchantmentLevel(Enchantment.SMITE);
            int fighting = sharp + smite;
            if (fighting > maxSharpness) maxSharpness = fighting;
            int kb = item.getEnchantmentLevel(Enchantment.KNOCKBACK);
            if (kb > maxKnockback) maxKnockback = kb;
        }
        totalKnockback += maxKnockback;

        double baseDamage = 4.0;
        double totalDamage = baseDamage + bonus;

        if (totalOverLevel > 0) {
            int strengthAmplifier = totalOverLevel / 5;
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, strengthAmplifier, false, false));
        } else {
            zombie.removePotionEffect(PotionEffectType.STRENGTH);
        }

        int resistanceLevel = Math.min(maxSharpness / 5, 4);
        if (resistanceLevel > 0) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, resistanceLevel - 1, false, false));
        } else {
            zombie.removePotionEffect(PotionEffectType.RESISTANCE);
        }

        double knockbackResistance = totalKnockback * 0.3;
        if (zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null) {
            zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
        }

        if (zombie.getLocation().distanceSquared(target.getLocation()) > 16) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, false, false));
        } else {
            zombie.removePotionEffect(PotionEffectType.SPEED);
        }

        if (zombie.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(totalDamage);
        }
    }

    private void resetAdaptation(Zombie zombie) {
        if (zombie.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(4.0);
        }
        if (zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null) {
            zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0);
        }
        zombie.removePotionEffect(PotionEffectType.STRENGTH);
        zombie.removePotionEffect(PotionEffectType.RESISTANCE);
        zombie.removePotionEffect(PotionEffectType.SPEED);
    }

    private boolean isDiamondOrNetherite(Material material) {
        String name = material.name();
        return name.startsWith("DIAMOND_") || name.startsWith("NETHERITE_");
    }

    private double getArmorBonus(Material material) {
        String name = material.name();
        if (name.startsWith("LEATHER_")) return 0.1;
        if (name.startsWith("GOLDEN_")) return 0.2;
        if (name.startsWith("CHAINMAIL_") || name.startsWith("COPPER_")) return 0.3;
        if (name.startsWith("IRON_")) return 0.5;
        if (name.startsWith("DIAMOND_")) return 1.7;
        if (name.startsWith("NETHERITE_")) return 2.0;
        return 0;
    }

    private double getProtectionBonus(Material material) {
        String name = material.name();
        if (name.startsWith("LEATHER_")) return 0.2;
        if (name.startsWith("GOLDEN_")) return 0.3;
        if (name.startsWith("CHAINMAIL_") || name.startsWith("COPPER_")) return 0.4;
        if (name.startsWith("IRON_")) return 0.5;
        if (name.startsWith("DIAMOND_")) return 1.0;
        if (name.startsWith("NETHERITE_")) return 1.2;
        return 0;
    }

    public boolean trySpawn(Location location) {
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, org.bukkit.entity.EntityType.ZOMBIE);
        if (zombie == null) return false;
        zombie.setBaby(false);

        zombie.addScoreboardTag(TAG);
        zombie.setCustomName(ChatColor.WHITE + "" + ChatColor.BOLD + "Mahoraga");
        zombie.setCustomNameVisible(true);
        zombie.setPersistent(true);
        zombie.setRemoveWhenFarAway(false);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));

        setAttribute(zombie, Attribute.MAX_HEALTH, 250.0);
        zombie.setHealth(250.0);

        setWhiteLeatherArmor(zombie);

        adapters.add(zombie.getUniqueId());
        return true;
    }

    private void setWhiteLeatherArmor(Zombie zombie) {
        EntityEquipment eq = zombie.getEquipment();
        if (eq == null) return;

        ItemStack helmet = new ItemStack(Material.WHITE_STAINED_GLASS);
        eq.setHelmet(helmet);
        eq.setHelmetDropChance(0);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        for (ItemStack piece : new ItemStack[]{chestplate, leggings, boots}) {
            LeatherArmorMeta meta = (LeatherArmorMeta) piece.getItemMeta();
            if (meta != null) {
                meta.setColor(Color.WHITE);
                meta.setUnbreakable(true);
                piece.setItemMeta(meta);
            }
        }

        eq.setChestplate(chestplate);
        eq.setLeggings(leggings);
        eq.setBoots(boots);

        eq.setChestplateDropChance(0);
        eq.setLeggingsDropChance(0);
        eq.setBootsDropChance(0);
    }

    private void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr != null) attr.setBaseValue(value);
    }

    @EventHandler
    public void onMahoragaDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains(TAG)) return;
        event.getDrops().clear();
        if (Math.random() < 0.75) {
            zombie.getWorld().dropItemNaturally(zombie.getLocation(), WheelEssence.WHEEL_ESSENCE.clone());
        }
        event.setDroppedExp(150);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("mahoraga.death-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()));
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMahoragaHitsInfinityArmor(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Zombie zombie) || !zombie.getScoreboardTags().contains(TAG)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (instakillInfinityArmor && SlimefunArmorAdaptation.isInfinitySingularityLinksSet(player)) {
            // Mahoraga has adapted: it pierces the "Infinite Defence" Tinker trait
            // that would force all damage to 1, killing the wearer outright.
            event.setCancelled(true);
            player.setAbsorptionAmount(0);
            player.setHealth(0);
            return;
        }

        if (ignoreDiamondMod && event.isCancelled() && !player.isDead()
                && SlimefunArmorAdaptation.hasDiamondMod(player.getInventory().getItemInMainHand())
                && random.nextDouble() < IGNORE_DIAMOND_CHANCE) {
            // 30% chance to ignore the Tinker Diamond modification, which
            // reflects the hit back at Mahoraga and cancels it. Un-cancel so
            // the blow lands (the reflected damage was already dealt and is
            // handled by onDiamondModHitsMahoraga).
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDiamondModHitsMahoraga(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Zombie zombie) || !zombie.getScoreboardTags().contains(TAG)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (infinityWeaponAdaptation && SlimefunArmorAdaptation.isInfinitySingularityWeapon(weapon)) {
            // Mahoraga has adapted to the Infinity Singularity sword: only a
            // fixed 1 damage gets through, no matter the raw hit.
            event.setDamage(Math.min(event.getDamage(), INFINITY_WEAPON_DAMAGE));
            return;
        }

        if (ignoreDiamondMod && SlimefunArmorAdaptation.hasDiamondMod(weapon)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        boolean damagerIsMSC = false;
        boolean damagedIsMSC = false;

        for (String tag : damager.getScoreboardTags()) {
            if (tag.startsWith("MSC_")) {
                damagerIsMSC = true;
                break;
            }
        }
        for (String tag : damaged.getScoreboardTags()) {
            if (tag.startsWith("MSC_")) {
                damagedIsMSC = true;
                break;
            }
        }

        if (damagerIsMSC && damagedIsMSC) {
            event.setCancelled(true);
        }
    }
}
