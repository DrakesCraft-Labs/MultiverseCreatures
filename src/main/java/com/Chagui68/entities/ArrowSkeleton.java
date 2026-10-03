package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.stand.StandArrowRoll;
import com.Chagui68.stand.StandData;
import com.Chagui68.stand.StandType;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * The Archer of the Arrow: a skeleton in a golden crown and a purple coat that carries the
 * Stand Arrow among its own. Two shots in a hundred are the Arrow.
 *
 * <p>A player who drank DIO's blood is a Stand bearer: the Arrow never kills them and always
 * awakens their Stand. Anybody else is judged: most die (70% by default) and are marked unworthy,
 * and the survivors awaken a Stand. An unworthy player gets nothing from the Arrow until they
 * drink DIO's blood. A Stand user is never chosen twice. The Arrow glows gold in flight,
 * cannot be picked up and leaves a trail, so everybody knows what is coming.</p>
 */
public class ArrowSkeleton implements Listener {

    public static final String TAG = "MSC_ArrowSkeleton";
    /** Marks a projectile as the Stand Arrow. */
    public static final NamespacedKey STAND_ARROW = new NamespacedKey("multiversecreatures", "msc_stand_arrow");

    private final MultiverseCreatures plugin;
    private final Map<UUID, Skeleton> archers = new HashMap<>();
    private final Set<UUID> arrows = new HashSet<>();
    private final Set<UUID> killedByArrow = new HashSet<>();
    private BukkitTask ticker;

    public ArrowSkeleton(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (!plugin.isEnabled("entities.arrow-skeleton")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Skeleton sk : world.getEntitiesByClass(Skeleton.class)) {
                if (sk.getScoreboardTags().contains(TAG)) {
                    archers.put(sk.getUniqueId(), sk);
                }
            }
        }
    }

    private void startTicker() {
        if (ticker != null) ticker.cancel();
        ticker = new BukkitRunnable() {
            int age = 0;

            @Override
            public void run() {
                age++;
                archers.values().removeIf(sk -> sk.isDead() || !sk.isValid());
                if (age % 10 == 0) {
                    for (Skeleton sk : archers.values()) {
                        sk.getWorld().spawnParticle(Particle.ENCHANT, sk.getLocation().add(0, 1.6, 0), 4,
                                0.3, 0.4, 0.3, 0.6);
                    }
                }
                // The Arrow in flight leaves a golden trail.
                arrows.removeIf(id -> {
                    if (!(Bukkit.getEntity(id) instanceof AbstractArrow arrow) || !arrow.isValid() || arrow.isInBlock()) {
                        return true;
                    }
                    arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(0xF2C230), 1.1f));
                    return false;
                });
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Stops the tick loop; the plugin calls this from its own {@code onDisable}. */
    public void stopTasks() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.arrow-skeleton")) return false;
        Skeleton sk = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        if (sk == null) return false;
        double health = plugin.getConfig().getDouble("entities.arrow-skeleton.health", 50.0);
        sk.addScoreboardTag(TAG);
        sk.customName(MscText.title(GOLD, "Archer of the Arrow"));
        sk.setCustomNameVisible(true);
        MscEntityUtils.applyAmbientPersistence(plugin, sk);
        MscEntityUtils.setAttribute(sk, Attribute.MAX_HEALTH, health);
        sk.setHealth(health);
        MscEntityUtils.setAttribute(sk, Attribute.MOVEMENT_SPEED,
                plugin.getConfig().getDouble("entities.arrow-skeleton.speed", 0.28));
        MscEntityUtils.setAttribute(sk, Attribute.FOLLOW_RANGE, 32.0);
        sk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0,
                false, false));
        EntityEquipment eq = sk.getEquipment();
        if (eq != null) {
            ItemStack bow = new ItemStack(Material.BOW);
            bow.editMeta(meta -> {
                meta.addEnchant(Enchantment.POWER, 3, true);
                meta.itemName(Component.text("Bow of the Arrow"));
            });
            ItemStack crown = new ItemStack(Material.GOLDEN_HELMET);
            crown.editMeta(meta -> meta.addEnchant(Enchantment.PROTECTION, 2, true));
            ItemStack coat = new ItemStack(Material.LEATHER_CHESTPLATE);
            if (coat.getItemMeta() instanceof LeatherArmorMeta meta) {
                meta.setColor(Color.fromRGB(0x4B1E78));
                coat.setItemMeta(meta);
            }
            eq.setItemInMainHand(bow);
            eq.setHelmet(crown);
            eq.setChestplate(coat);
            eq.setItemInMainHandDropChance(0);
            eq.setHelmetDropChance(0);
            eq.setChestplateDropChance(0);
        }
        archers.put(sk.getUniqueId(), sk);
        return true;
    }

    /** Two shots in a hundred are the Arrow. */
    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Skeleton sk) || !sk.getScoreboardTags().contains(TAG)
                || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }
        double chance = plugin.getConfig().getDouble("entities.arrow-skeleton.stand-arrow-chance", 0.02);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        arrow.getPersistentDataContainer().set(STAND_ARROW, PersistentDataType.BYTE, (byte) 1);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setGlowing(true);
        arrow.setColor(Color.fromRGB(0xF2C230));
        arrows.add(arrow.getUniqueId());
        sk.getWorld().playSound(sk.getLocation(), Sound.BLOCK_BELL_USE, 1.2f, 0.6f);
        sk.getWorld().playSound(sk.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.4f, 1.8f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)
                || !arrow.getPersistentDataContainer().has(STAND_ARROW, PersistentDataType.BYTE)) {
            return;
        }
        arrows.remove(arrow.getUniqueId());
        if (!(event.getHitEntity() instanceof Player player)
                || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // The arrow is spent on whoever it chose.
        arrow.remove();
        pierce(player);
    }

    /** The Arrow has pierced a player: death, a Stand, or a body that rejects it. */
    public void pierce(Player player) {
        double deathChance = plugin.getConfig().getDouble("entities.arrow-skeleton.death-chance", 0.7);
        StandArrowRoll.Outcome outcome = StandArrowRoll.decide(ThreadLocalRandom.current().nextDouble(), deathChance,
                StandData.isBearer(player), StandData.isUnworthy(player), StandData.stand(player) != null);
        Location at = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.DUST, at, 40, 0.4, 0.8, 0.4, 0,
                new Particle.DustOptions(Color.fromRGB(0xF2C230), 1.4f));
        player.getWorld().playSound(at, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
        switch (outcome) {
            case DEATH -> {
                player.showTitle(Title.title(Component.text("THE ARROW", GOLD, TextDecoration.BOLD),
                        Component.text("did not find you worthy", DARK_RED),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(600))));
                killedByArrow.add(player.getUniqueId());
                StandData.markUnworthy(player);
                player.sendMessage(MscText.rich(DARK_RED, "✦ ", GRAY, "The Arrow marked you as ", DARK_RED,
                        "unworthy", GRAY, ". Only ", DARK_RED, "DIO's blood", GRAY, " can make you a Stand bearer now."));
                player.setHealth(0);
            }
            case STAND -> {
                StandType stand = plugin.getStandManager().awaken(player, null);
                if (stand == null) {
                    player.sendMessage(Component.text("The Arrow chose you, but no Stand answered.", GRAY));
                }
            }
            case REJECTED -> {
                player.showTitle(Title.title(Component.text("UNWORTHY", DARK_RED, TextDecoration.BOLD),
                        Component.text("the Arrow already judged you", GRAY),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(600))));
                player.sendMessage(MscText.rich(GOLD, "✦ ", GRAY, "The Arrow passes through you. Drink ",
                        DARK_RED, "DIO's blood", GRAY, " to become a Stand bearer."));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
            }
            case RESONATE -> player.sendActionBar(Component.text("The Arrow resonates with your Stand and fades away",
                    GOLD));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (killedByArrow.remove(player.getUniqueId())) {
            List<String> messages = plugin.getConfig().getStringList("entities.arrow-skeleton.arrow-death-messages");
            if (!messages.isEmpty()) {
                String raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
                event.deathMessage(LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(raw.replace("%player%", player.getName())));
            }
            return;
        }
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.arrow-skeleton.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton sk) || !sk.getScoreboardTags().contains(TAG)) return;
        archers.remove(sk.getUniqueId());
        event.setDroppedExp(40);
    }
}
