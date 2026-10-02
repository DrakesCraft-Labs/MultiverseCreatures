package com.Chagui68.stand;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.potions.VampirePotions;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * DIO's blood. Whoever drinks the Bearer's Elixir can carry a Stand, and becomes a vampire:
 *
 * <ul>
 *   <li>The sun burns them. Standing under the open sky in daytime deals true damage every
 *   second: it goes straight to their health, past armour, Resistance, enchantments,
 *   absorption and totems. Rain and roofs protect them; a death by sunlight gets its own
 *   death message.</li>
 *   <li>The night is theirs: Strength and Speed after dusk, night vision always, and every
 *   blow they land drinks a little of the damage back.</li>
 * </ul>
 */
public final class VampireManager implements Listener {

    private final MultiverseCreatures plugin;
    private final Set<UUID> burnedBySun = new HashSet<>();
    private final Set<UUID> smoking = new HashSet<>();
    private BukkitTask ticker;

    public VampireManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        int every = Math.max(5, plugin.getConfig().getInt("vampire.sun-check-ticks", 20));
        ticker = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, every, every);
    }

    /**
     * True when sunlight reaches that spot: an overworld in daytime, open sky above the
     * player's head and, unless rain is set not to protect, a clear sky.
     */
    public static boolean sunlit(World.Environment environment, boolean day, boolean clear, int skyLight,
                                 boolean rainProtects) {
        return environment == World.Environment.NORMAL && day && skyLight >= 15 && (clear || !rainProtects);
    }

    private boolean exposed(Player player) {
        World world = player.getWorld();
        Location eyes = player.getEyeLocation();
        return sunlit(world.getEnvironment(), world.isDayTime(), world.isClearWeather(),
                eyes.getBlock().getLightFromSky(), plugin.getConfig().getBoolean("vampire.rain-protects", true));
    }

    private void tick() {
        double damage = plugin.getConfig().getDouble("vampire.sun-damage", 2.0);
        boolean perks = plugin.getConfig().getBoolean("vampire.night-perks", true);
        int every = Math.max(5, plugin.getConfig().getInt("vampire.sun-check-ticks", 20));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!StandData.isVampire(player) || player.isDead()) {
                continue;
            }
            boolean mortal = player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
            boolean burning = mortal && exposed(player);
            if (burning) {
                burn(player, damage);
            }
            if (burning != smoking.contains(player.getUniqueId())) {
                player.setVisualFire(burning);
                if (burning) {
                    smoking.add(player.getUniqueId());
                    player.sendActionBar(Component.text("☀ ¡El sol te está quemando! Busca sombra", GOLD,
                            TextDecoration.BOLD));
                } else {
                    smoking.remove(player.getUniqueId());
                }
            }
            if (perks) {
                int length = every * 3 + 20;
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 15, 0, true, false));
                if (!player.getWorld().isDayTime() || player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, length, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, length, 0, true, false));
                }
            }
        }
    }

    /**
     * True damage: straight off the health, no matter what protects the player.
     */
    private void burn(Player player, double damage) {
        Location at = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.SMOKE, at, 12, 0.3, 0.6, 0.3, 0.02);
        player.getWorld().spawnParticle(Particle.FLAME, at, 4, 0.3, 0.5, 0.3, 0.01);
        player.getWorld().playSound(at, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.8f, 0.9f);
        player.playHurtAnimation(0f);
        double left = player.getHealth() - damage;
        if (left <= 0) {
            burnedBySun.add(player.getUniqueId());
            player.setHealth(0);
        } else {
            player.setHealth(left);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!burnedBySun.remove(player.getUniqueId())) {
            return;
        }
        smoking.remove(player.getUniqueId());
        player.setVisualFire(false);
        List<String> messages = plugin.getConfig().getStringList("vampire.sun-death-messages");
        if (messages.isEmpty()) {
            return;
        }
        String raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        event.deathMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(raw.replace("%player%", player.getName())));
    }

    /** Drinking the Bearer's Elixir: DIO's blood takes the body. */
    @EventHandler(ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent event) {
        if (!VampirePotions.isBearerElixir(event.getItem())) {
            return;
        }
        Player player = event.getPlayer();
        if (StandData.isBearer(player)) {
            player.sendMessage(Component.text("La sangre de DIO ya corre por tus venas.", DARK_RED));
            return;
        }
        StandData.drinkBlood(player);
        player.showTitle(Title.title(Component.text("SANGRE DE DIO", DARK_RED, TextDecoration.BOLD),
                Component.text("Ahora eres un vampiro y portador de Stand", GRAY),
                Title.Times.times(Duration.ofMillis(400), Duration.ofSeconds(4), Duration.ofSeconds(1))));
        player.sendMessage(MscText.rich(DARK_RED, "✦ ", GRAY, "Tu cuerpo puede sostener un ", GOLD, "Stand", GRAY,
                ": si la Flecha te elige, despertará. Pero el ", YELLOW, "sol", GRAY,
                " ya no te perdona: bajo el cielo abierto de día, arderás."));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.6f);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 60, 0.5, 0.9, 0.5, 0,
                new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0x6B0000), 1.5f));
    }

    /** A vampire drinks back part of the damage of every blow. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBite(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player vampire) || !StandData.isVampire(vampire)
                || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        double share = plugin.getConfig().getDouble("vampire.lifesteal", 0.15);
        if (share <= 0) {
            return;
        }
        AttributeInstance max = vampire.getAttribute(Attribute.MAX_HEALTH);
        double top = max == null ? 20.0 : max.getValue();
        vampire.setHealth(Math.min(top, vampire.getHealth() + event.getFinalDamage() * share));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        smoking.remove(event.getPlayer().getUniqueId());
        burnedBySun.remove(event.getPlayer().getUniqueId());
    }

    public void stopAll() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        for (UUID id : smoking) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                player.setVisualFire(false);
            }
        }
        smoking.clear();
    }
}
