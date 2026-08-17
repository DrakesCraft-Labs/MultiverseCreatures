package com.Chagui68.listener.weapons.magic;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.items.weapons.magic.SentinelGrimoire;
import com.Chagui68.items.weapons.magic.SentinelGrimoire.GrimoireSpell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GrimoireHandler implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, Long> invulnerableUntil = new ConcurrentHashMap<>();

    public GrimoireHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private boolean isGrimoire(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(SentinelGrimoire.GRIMOIRE_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.isEnabled("items.grimoire")) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if (!isGrimoire(main)) return;

        event.setCancelled(true);
        if (player.isSneaking()) {
            cyclePage(player);
        } else {
            castSpell(player);
        }
    }

    private void cyclePage(Player player) {
        int page = getPage(player);
        page = (page % GrimoireSpell.values().length) + 1;
        setPage(player, page);
        GrimoireSpell spell = GrimoireSpell.byPage(page);
        sendActionBar(player, ChatColor.YELLOW + "Grimoire: " + ChatColor.WHITE + spell.getDisplay()
                + ChatColor.GRAY + " (" + page + "/" + GrimoireSpell.values().length + ")");
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.4f);
    }

    private void castSpell(Player player) {
        int page = getPage(player);
        GrimoireSpell spell = GrimoireSpell.byPage(page);

        long now = System.currentTimeMillis();
        long until = cooldownUntil(player, spell);
        if (until > now) {
            long left = (until - now) / 1000;
            sendActionBar(player, ChatColor.RED + spell.getDisplay() + " on cooldown: " + left + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            return;
        }

        int cooldown = plugin.getConfig().getInt("items.grimoire." + spell.getConfigKey() + ".cooldown",
                spell.getCooldownSeconds());
        setCooldown(player, spell, now + cooldown * 1000L);

        sendActionBar(player, ChatColor.YELLOW + "Casting: " + ChatColor.WHITE + spell.getDisplay());
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);

        switch (spell) {
            case BLAZING_PENTAGRAM -> castBlazingPentagram(player, spell);
            case LANCE_RAIN -> castLanceRain(player, spell);
            case DIVINE_JUDGMENT -> castDivineJudgment(player, spell);
            case EXECUTIONERS_MARK -> castExecutionersMark(player, spell);
            case SINGULAR_VORTEX -> castSingularVortex(player, spell);
            case EARTHQUAKE -> castEarthquake(player, spell);
            case CELESTIAL_BULWARK -> castCelestialBulwark(player, spell);
            case SENTINEL_AURA -> castSentinelAura(player, spell);
        }
    }

    private void castBlazingPentagram(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell);
        magicSeals().spawnFlamingPentagram(target, 45, player.getLocation().getYaw());
        damageEntities(target, 3.5, damage, player, true);
        target.getWorld().playSound(target, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }

    private void castLanceRain(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell) / 3.0;
        magicSeals().spawnLanceRain(target, 50, 3.5);
        for (long delay : new long[]{10L, 25L, 40L}) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                damageEntities(target, 3.5, damage, player, false);
                target.getWorld().playSound(target, Sound.ENTITY_SHULKER_BULLET_HIT, 0.8f, 1.1f);
            }, delay);
        }
    }

    private void castDivineJudgment(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell) / 3.0;
        magicSeals().spawnDivineSeal(target, 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = 0; i < 3; i++) {
                Location strike = target.clone().add(
                        Math.random() * 5 - 2.5, 0, Math.random() * 5 - 2.5);
                strike.setY(target.getWorld().getHighestBlockYAt(strike));
                strike.getWorld().strikeLightningEffect(strike);
                strike.getWorld().playSound(strike, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.1f);
                damageEntities(strike, 2.5, damage, player, false);
            }
        }, 20L);
    }

    private void castExecutionersMark(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell);
        magicSeals().spawnExecutionerCross(target, 50);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.getWorld().createExplosion(target, 0f, false);
            target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
            target.getWorld().spawnParticle(Particle.EXPLOSION, target, 1, 0, 0, 0, 0);
            for (Entity e : target.getWorld().getNearbyEntities(target, 4.0, 4.0, 4.0)) {
                if (!(e instanceof LivingEntity le)) continue;
                if (e instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) continue;
                le.damage(damage, player);
                le.setVelocity(le.getVelocity().add(new Vector(0, 0.9, 0)));
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
        }, 50L);
    }

    private void castSingularVortex(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell);
        magicSeals().spawnVortexSeal(target, 60);
        final Location center = target.clone().add(0, 0.5, 0);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= 30) {
                    damageEntities(target, 6.0, damage, player, false);
                    cancel();
                    return;
                }
                for (Entity e : center.getWorld().getNearbyEntities(center, 6.0, 6.0, 6.0)) {
                    if (!(e instanceof LivingEntity le)) continue;
                    if (e instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) continue;
                    Vector pull = center.toVector().subtract(le.getLocation().toVector())
                            .normalize().multiply(0.55);
                    le.setVelocity(le.getVelocity().add(pull).multiply(0.6));
                }
                t += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void castEarthquake(Player player, GrimoireSpell spell) {
        Location target = getTargetLocation(player);
        double damage = damageOf(spell);
        magicSeals().spawnQuakeSeal(target, 50);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f);
            for (Entity e : target.getWorld().getNearbyEntities(target, 5.0, 5.0, 5.0)) {
                if (!(e instanceof LivingEntity le)) continue;
                if (e instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) continue;
                le.damage(damage, player);
                le.setVelocity(le.getVelocity().add(new Vector(0, 1.0, 0)));
            }
        }, 25L);
    }

    private void castCelestialBulwark(Player player, GrimoireSpell spell) {
        magicSeals().spawnCelestialSeal(player.getLocation(), 60);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 0));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    private void castSentinelAura(Player player, GrimoireSpell spell) {
        magicSeals().spawnInvulnerabilityAura(player.getLocation(), 100, player);
        invulnerableUntil.put(player.getUniqueId(), System.currentTimeMillis() + 5000L);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Long until = invulnerableUntil.get(player.getUniqueId());
        if (until != null && until > System.currentTimeMillis()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        invulnerableUntil.remove(event.getPlayer().getUniqueId());
    }

    private double damageOf(GrimoireSpell spell) {
        return plugin.getConfig().getDouble("items.grimoire." + spell.getConfigKey() + ".damage",
                spell.getDefaultDamage());
    }

    private void damageEntities(Location center, double radius, double damage, Player source, boolean ignite) {
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e instanceof Player p && p.getUniqueId().equals(source.getUniqueId())) continue;
            le.damage(damage, source);
            if (ignite) le.setFireTicks(40);
        }
    }

    private Location getTargetLocation(Player player) {
        Location eye = player.getEyeLocation();
        Block block = player.getTargetBlockExact(64);
        if (block != null && block.getType() != Material.AIR) {
            return block.getLocation().add(0.5, 1.0, 0.5);
        }
        return eye.clone().add(eye.getDirection().multiply(6));
    }

    private int getPage(Player player) {
        return player.getPersistentDataContainer()
                .getOrDefault(SentinelGrimoire.PAGE_KEY, PersistentDataType.INTEGER, 1);
    }

    private void setPage(Player player, int page) {
        player.getPersistentDataContainer().set(SentinelGrimoire.PAGE_KEY, PersistentDataType.INTEGER, page);
    }

    private long cooldownUntil(Player player, GrimoireSpell spell) {
        NamespacedKey key = new NamespacedKey("multiversecreatures",
                SentinelGrimoire.COOLDOWN_KEY_PREFIX + spell.getConfigKey());
        return player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG, 0L);
    }

    private void setCooldown(Player player, GrimoireSpell spell, long until) {
        NamespacedKey key = new NamespacedKey("multiversecreatures",
                SentinelGrimoire.COOLDOWN_KEY_PREFIX + spell.getConfigKey());
        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, until);
    }

    private MagicSealListener magicSeals() {
        return plugin.getMagicSealListener();
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
