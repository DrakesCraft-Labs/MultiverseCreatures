package com.Chagui68.stand;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Everything a Stand user can do.
 *
 * <ul>
 *   <li><b>Sneak + F</b> (swap hands) summons the Stand or sends it back.</li>
 *   <li><b>F</b> with the Stand out uses its main ability.</li>
 *   <li><b>Sneak + left click</b> with an empty hand and the Stand out uses the second one
 *   (the time stop of Star Platinum and The World).</li>
 *   <li>{@code /stand} does the same from chat, and {@code /stand sha} sends Killer Queen's
 *   Sheer Heart Attack.</li>
 * </ul>
 *
 * <p>Killer Queen's first bomb is passive: every player its user hits takes an explosion as
 * well (with a short cooldown). Every number lives under {@code stands.} in config.yml.</p>
 */
public final class StandManager implements Listener {

    private final MultiverseCreatures plugin;
    private final TimeStopManager timeStop;
    private final Map<UUID, StandModel> summoned = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, SheerHeartAttack> attacks = new HashMap<>();
    private final Map<UUID, Long> bombCooldowns = new HashMap<>();
    private BukkitTask ticker;
    private boolean bombing;

    public StandManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
        this.timeStop = new TimeStopManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(timeStop, plugin);
        ticker = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public TimeStopManager timeStop() {
        return timeStop;
    }

    // ------------------------------------------------------------------ config

    private double number(StandType type, String setting, double fallback) {
        return plugin.getConfig().getDouble("stands." + type.key() + "." + setting, fallback);
    }

    public int weight(StandType type) {
        return plugin.getConfig().getInt("stands." + type.key() + ".weight", type.defaultWeight());
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean("stands.enabled", true);
    }

    // ------------------------------------------------------------------- awakening

    /**
     * Awakens a Stand in a player, rolled by weight when {@code type} is null.
     *
     * @return the Stand awakened, or null when every weight is zero
     */
    public StandType awaken(Player player, StandType type) {
        StandType stand = type != null ? type : StandType.roll(ThreadLocalRandom.current().nextDouble(), this::weight);
        if (stand == null) {
            return null;
        }
        dismiss(player);
        StandData.setStand(player, stand);
        player.showTitle(Title.title(
                Component.text("「" + stand.displayName() + "」", stand.textColor(), TextDecoration.BOLD),
                Component.text("Tu Stand ha despertado", GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofMillis(800))));
        player.sendMessage(MscText.rich(GOLD, "✦ ", GRAY, "Tu Stand es ", stand.textColor(), stand.displayName(),
                GRAY, ". Agáchate y pulsa ", YELLOW, "F", GRAY, " para invocarlo; ", YELLOW, "/stand", GRAY,
                " para ver sus habilidades."));
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 80, 0.6, 1.0, 0.6, 0,
                new Particle.DustOptions(stand.aura(), 1.6f));
        return stand;
    }

    /** Takes a Stand away from a player (admin command). */
    public void strip(Player player) {
        dismiss(player);
        StandData.setStand(player, null);
    }

    // ------------------------------------------------------------------- summoning

    public boolean isSummoned(Player player) {
        return summoned.containsKey(player.getUniqueId());
    }

    /** Summons the Stand, or sends it back when it is out. */
    public void toggle(Player player) {
        StandType stand = StandData.stand(player);
        if (stand == null) {
            player.sendActionBar(Component.text("No tienes ningún Stand", GRAY));
            return;
        }
        if (isSummoned(player)) {
            dismiss(player);
            player.sendActionBar(Component.text(stand.displayName() + " regresa", stand.textColor()));
            return;
        }
        if (!enabled()) {
            player.sendActionBar(Component.text("Los Stands están desactivados", RED));
            return;
        }
        StandModel model = new StandModel(plugin, stand);
        model.spawn(player);
        summoned.put(player.getUniqueId(), model);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.9f, 1.2f);
        player.sendActionBar(Component.text("「" + stand.displayName() + "」", stand.textColor(), TextDecoration.BOLD));
    }

    public void dismiss(Player player) {
        StandModel model = summoned.remove(player.getUniqueId());
        if (model != null) {
            model.remove();
        }
    }

    // ------------------------------------------------------------------- abilities

    /** Seconds left of a cooldown, or 0 when it is ready. */
    private long remaining(Player player, String ability) {
        long until = cooldowns.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(ability, 0L);
        return Math.max(0, (until - System.currentTimeMillis() + 999) / 1000);
    }

    private boolean ready(Player player, String ability, String label) {
        long left = remaining(player, ability);
        if (left > 0) {
            player.sendActionBar(Component.text(label + " en enfriamiento: " + left + "s", RED));
            return false;
        }
        return true;
    }

    private void cool(Player player, String ability, double seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + (long) (seconds * 1000));
    }

    /**
     * Uses one of the Stand's abilities.
     *
     * @param which 1 for the main ability, 2 for the second one
     */
    public void ability(Player player, int which) {
        StandType stand = StandData.stand(player);
        if (stand == null) {
            player.sendActionBar(Component.text("No tienes ningún Stand", GRAY));
            return;
        }
        if (!isSummoned(player)) {
            player.sendActionBar(Component.text("Invoca a tu Stand primero (agáchate + F)", GRAY));
            return;
        }
        if (timeStop.frozen(player)) {
            return;
        }
        switch (stand) {
            case HERMIT_PURPLE -> spiritPhotography(player);
            case MAGICIANS_RED -> crossfireHurricane(player);
            case CRAZY_DIAMOND -> restoration(player);
            case KILLER_QUEEN -> openSheerHeartAttack(player);
            case STAR_PLATINUM, THE_WORLD -> {
                if (which == 2) {
                    stopTime(player, stand);
                } else {
                    barrage(player, stand);
                }
            }
        }
    }

    /** Star Platinum's ORA and The World's MUDA: a storm of fists on whoever stands in front. */
    private void barrage(Player player, StandType stand) {
        if (!ready(player, "barrage", "La ráfaga")) {
            return;
        }
        double range = number(stand, "range", 4.0);
        RayTraceResult ray = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), range, 0.6,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
                        && !entity.getScoreboardTags().contains(StandModel.TAG));
        LivingEntity target = ray != null && ray.getHitEntity() instanceof LivingEntity living ? living : null;
        cool(player, "barrage", number(stand, "cooldown-seconds", 20));
        int hits = (int) number(stand, "barrage-hits", 10);
        double damage = number(stand, "barrage-damage", 1.5);
        double finisher = number(stand, "barrage-finisher", 5.0);
        String cry = stand == StandType.THE_WORLD ? "MUDA" : "ORA";
        StandModel model = summoned.get(player.getUniqueId());
        if (model != null) {
            model.barrage(hits * 2 + 6);
        }
        new BukkitRunnable() {
            int hit = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || (target != null && (target.isDead() || !target.isValid()))) {
                    cancel();
                    return;
                }
                Location front = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.6));
                if (hit < hits) {
                    if (target != null && target.getLocation().distanceSquared(player.getLocation()) <= (range + 1) * (range + 1)) {
                        target.setNoDamageTicks(0);
                        target.damage(damage, standDamage(player));
                        target.setVelocity(new Vector());
                        front = target.getLocation().add(0, 1, 0);
                    }
                    player.getWorld().spawnParticle(Particle.CRIT, front, 6, 0.3, 0.3, 0.3, 0.2);
                    player.getWorld().playSound(front, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, 1.4f + hit * 0.03f);
                    if (hit % 3 == 0 && model != null) {
                        model.shout(front.clone().add(ThreadLocalRandom.current().nextDouble(-0.6, 0.6), 0.5,
                                ThreadLocalRandom.current().nextDouble(-0.6, 0.6)),
                                Component.text(cry + "!", stand.textColor(), TextDecoration.BOLD, TextDecoration.ITALIC));
                    }
                    hit++;
                    return;
                }
                if (target != null) {
                    target.setNoDamageTicks(0);
                    target.damage(finisher, standDamage(player));
                    Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector());
                    target.setVelocity(push.setY(0).normalize().multiply(1.4).setY(0.45));
                }
                if (model != null) {
                    model.shout(front.clone().add(0, 0.8, 0),
                            Component.text(cry + cry.substring(cry.length() - 1).repeat(2) + "!", stand.textColor(),
                                    TextDecoration.BOLD));
                }
                player.getWorld().playSound(front, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /** ZA WARUDO: everything nearby stands still for a few seconds, except the user. */
    private void stopTime(Player player, StandType stand) {
        if (!ready(player, "time-stop", "La detención del tiempo")) {
            return;
        }
        double seconds = number(stand, "time-stop-seconds", stand == StandType.THE_WORLD ? 3 : 1.5);
        double radius = number(stand, "time-stop-radius", stand == StandType.THE_WORLD ? 14 : 10);
        cool(player, "time-stop", number(stand, "time-stop-cooldown-seconds", 120));
        String shout = stand == StandType.THE_WORLD ? "ZA WARUDO! TOKI WO TOMARE!" : "STAR PLATINUM: THE WORLD!";
        for (Player near : player.getWorld().getPlayers()) {
            if (near.getLocation().distanceSquared(player.getLocation()) <= 60 * 60) {
                near.showTitle(Title.title(Component.text(shout, stand.textColor(), TextDecoration.BOLD),
                        Component.text(player.getName(), GRAY),
                        Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1200), Duration.ofMillis(300))));
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.4f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.4f, 0.6f);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1, 0), 200,
                radius / 3, 1.5, radius / 3, 0.2);
        timeStop.stop(player, radius, (int) (seconds * 20), entity -> immuneToTimeStop(entity));
    }

    private boolean immuneToTimeStop(Entity entity) {
        if (entity instanceof Player other) {
            StandType stand = StandData.stand(other);
            return (stand != null && stand.stopsTime()) || other.getGameMode() == GameMode.CREATIVE
                    || other.getGameMode() == GameMode.SPECTATOR;
        }
        for (String tag : entity.getScoreboardTags()) {
            if (tag.contains("Boss") || tag.equals(StandModel.TAG) || tag.equals(SheerHeartAttack.TAG)) {
                return true;
            }
        }
        return false;
    }

    /** Hermit Purple: a spirit photograph that shows every living thing nearby. */
    private void spiritPhotography(Player player) {
        if (!ready(player, "photo", "La fotografía espiritual")) {
            return;
        }
        StandType stand = StandType.HERMIT_PURPLE;
        double radius = number(stand, "reveal-radius", 48);
        int ticks = (int) (number(stand, "reveal-seconds", 10) * 20);
        cool(player, "photo", number(stand, "cooldown-seconds", 30));
        int found = 0;
        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || entity.getScoreboardTags().contains(StandModel.TAG)) {
                continue;
            }
            living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0, false, false));
            found++;
            if (entity instanceof Player other) {
                double distance = other.getLocation().distanceSquared(player.getLocation());
                if (distance < best) {
                    best = distance;
                    nearest = other;
                }
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 0.8f, 1.6f);
        player.getWorld().spawnParticle(Particle.FLASH, player.getEyeLocation(), 1);
        Component line = MscText.rich(DARK_PURPLE, "✦ Hermit Purple ", GRAY, "revela ", GOLD, found + "", GRAY,
                " seres a tu alrededor");
        if (nearest != null) {
            line = line.append(MscText.rich(GRAY, " · más cercano: ", LIGHT_PURPLE, nearest.getName(), GRAY,
                    " a " + (int) Math.sqrt(best) + " bloques"));
        }
        player.sendMessage(line);
    }

    /** Magician's Red: a fan of burning ankhs. */
    private void crossfireHurricane(Player player) {
        if (!ready(player, "fire", "Crossfire Hurricane")) {
            return;
        }
        StandType stand = StandType.MAGICIANS_RED;
        cool(player, "fire", number(stand, "cooldown-seconds", 12));
        int count = Math.max(1, (int) number(stand, "fireballs", 3));
        Vector direction = player.getEyeLocation().getDirection();
        for (int i = 0; i < count; i++) {
            double spread = (i - (count - 1) / 2.0) * 0.18;
            Vector aim = direction.clone().rotateAroundY(spread);
            SmallFireball ball = player.launchProjectile(SmallFireball.class, aim.multiply(1.4));
            ball.setIsIncendiary(false);
            ball.addScoreboardTag("MSC_CrossfireHurricane");
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.7f);
        player.sendActionBar(Component.text("CROSSFIRE HURRICANE!", RED, TextDecoration.BOLD));
    }

    /** Crazy Diamond: heals whoever it points at (or its user) and mends what they hold. */
    private void restoration(Player player) {
        if (!ready(player, "restore", "La restauración")) {
            return;
        }
        StandType stand = StandType.CRAZY_DIAMOND;
        RayTraceResult ray = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), number(stand, "range", 6.0), 0.5,
                entity -> entity instanceof Player && !entity.equals(player));
        Player patient = ray != null && ray.getHitEntity() instanceof Player other ? other : player;
        cool(player, "restore", number(stand, "cooldown-seconds", 30));
        AttributeInstance max = patient.getAttribute(Attribute.MAX_HEALTH);
        double top = max == null ? 20.0 : max.getValue();
        patient.setHealth(Math.min(top, patient.getHealth() + number(stand, "heal", 8.0)));
        ItemStack held = patient.getInventory().getItemInMainHand();
        if (held.getItemMeta() instanceof Damageable damageable && damageable.hasDamage()) {
            int mend = (int) Math.ceil(held.getType().getMaxDurability() * number(stand, "repair-fraction", 0.5));
            damageable.setDamage(Math.max(0, damageable.getDamage() - mend));
            held.setItemMeta(damageable);
        }
        patient.getWorld().spawnParticle(Particle.HEART, patient.getLocation().add(0, 2, 0), 8, 0.4, 0.3, 0.4, 0);
        patient.getWorld().spawnParticle(Particle.DUST, patient.getLocation().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0,
                new Particle.DustOptions(Color.fromRGB(0xF28CC8), 1.2f));
        patient.getWorld().playSound(patient.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.4f);
        player.sendActionBar(Component.text("DORARARARA!", LIGHT_PURPLE, TextDecoration.BOLD));
    }

    private static DamageSource standDamage(Player player) {
        return DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build();
    }

    // ------------------------------------------------------------- Killer Queen

    /** Opens the target menu of Sheer Heart Attack (also {@code /stand sha}). */
    public void openSheerHeartAttack(Player player) {
        if (StandData.stand(player) != StandType.KILLER_QUEEN) {
            player.sendMessage(Component.text("Solo el usuario de Killer Queen puede usar Sheer Heart Attack.", RED));
            return;
        }
        SheerHeartAttack running = attacks.get(player.getUniqueId());
        if (running != null && !running.done()) {
            player.sendMessage(Component.text("Sheer Heart Attack ya está persiguiendo a alguien.", RED));
            return;
        }
        if (!ready(player, "sha", "Sheer Heart Attack")) {
            return;
        }
        player.openInventory(new SheerHeartAttackMenu(player, 0).getInventory());
    }

    private void launch(Player owner, Player target) {
        StandType stand = StandType.KILLER_QUEEN;
        SheerHeartAttack attack = new SheerHeartAttack(plugin, owner, target,
                number(stand, "sha-speed", 0.12), number(stand, "sha-damage", 18.0),
                number(stand, "sha-reach", 1.2), (long) (number(stand, "sha-lifetime-seconds", 0) * 1000));
        attacks.put(owner.getUniqueId(), attack);
        cool(owner, "sha", number(stand, "sha-cooldown-seconds", 300));
        owner.sendMessage(MscText.rich(LIGHT_PURPLE, "☠ ", GRAY, "Killer Queen: ", LIGHT_PURPLE,
                "Sheer Heart Attack", GRAY, " va tras ", WHITE, target.getName(), GRAY, "."));
        owner.getWorld().playSound(owner.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.6f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SheerHeartAttackMenu menu)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == SheerHeartAttackMenu.CLOSE) {
            player.closeInventory();
        } else if (slot == SheerHeartAttackMenu.PREVIOUS || slot == SheerHeartAttackMenu.NEXT) {
            int page = menu.page() + (slot == SheerHeartAttackMenu.NEXT ? 1 : -1);
            player.openInventory(new SheerHeartAttackMenu(player, page).getInventory());
        } else {
            UUID id = menu.target(slot);
            Player target = id == null ? null : plugin.getServer().getPlayer(id);
            if (target == null) {
                return;
            }
            player.closeInventory();
            if (StandData.stand(player) == StandType.KILLER_QUEEN && remaining(player, "sha") == 0
                    && (attacks.get(player.getUniqueId()) == null || attacks.get(player.getUniqueId()).done())) {
                launch(player, target);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SheerHeartAttackMenu) {
            event.setCancelled(true);
        }
    }

    /** Killer Queen's first bomb: whoever its user hits explodes. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (bombing || !(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (StandData.stand(attacker) != StandType.KILLER_QUEEN || !enabled()) {
            return;
        }
        // Only the user's own blows plant the bomb, never another explosion.
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }
        StandType stand = StandType.KILLER_QUEEN;
        if (plugin.getConfig().getBoolean("stands.killer-queen.require-summoned", false) && !isSummoned(attacker)) {
            return;
        }
        if (!(victim instanceof Player) && !plugin.getConfig().getBoolean("stands.killer-queen.include-mobs", false)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (bombCooldowns.getOrDefault(attacker.getUniqueId(), 0L) > now) {
            return;
        }
        bombCooldowns.put(attacker.getUniqueId(), now + (long) number(stand, "explosion-cooldown-ms", 1500));
        double damage = number(stand, "explosion-damage", 4.0);
        // Next tick, so the bomb is its own hit and not swallowed by the punch's invulnerability.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!victim.isValid() || victim.isDead()) {
                return;
            }
            Location at = victim.getLocation().add(0, 1, 0);
            victim.getWorld().spawnParticle(Particle.EXPLOSION, at, 2, 0.2, 0.2, 0.2, 0);
            victim.getWorld().spawnParticle(Particle.DUST, at, 20, 0.4, 0.5, 0.4, 0,
                    new Particle.DustOptions(Color.fromRGB(0xE7A1C9), 1.3f));
            victim.getWorld().playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 1.3f);
            bombing = true;
            try {
                victim.setNoDamageTicks(0);
                victim.damage(damage, DamageSource.builder(DamageType.PLAYER_EXPLOSION).withCausingEntity(attacker)
                        .withDirectEntity(attacker).withDamageLocation(at).build());
            } finally {
                bombing = false;
            }
            attacker.sendActionBar(Component.text("Killer Queen ya ha tocado", LIGHT_PURPLE, TextDecoration.ITALIC));
        });
    }

    // ------------------------------------------------------------------- input

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (StandData.stand(player) == null) {
            return;
        }
        if (player.isSneaking()) {
            event.setCancelled(true);
            toggle(player);
        } else if (isSummoned(player)) {
            event.setCancelled(true);
            ability(player, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() || !isSummoned(player) || player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }
        StandType stand = StandData.stand(player);
        if (stand != null && stand.stopsTime()) {
            ability(player, 2);
        }
    }

    // --------------------------------------------------------------- lifecycle

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dismiss(event.getPlayer());
        UUID id = event.getPlayer().getUniqueId();
        // A Sheer Heart Attack hunting the player who left vanishes with them.
        for (SheerHeartAttack attack : new ArrayList<>(attacks.values())) {
            if (attack.target().equals(id)) {
                attack.remove();
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        dismiss(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        dismiss(event.getPlayer());
    }

    /** The fireballs of Magician's Red hurt with fire, at the configured strength. */
    @EventHandler(ignoreCancelled = true)
    public void onFireball(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getScoreboardTags().contains("MSC_CrossfireHurricane")) {
            event.setDamage(number(StandType.MAGICIANS_RED, "fire-damage", 6.0));
            event.getEntity().setFireTicks(Math.max(event.getEntity().getFireTicks(), 60));
        }
    }

    /** Stands are not hurt by their own user's explosions. */
    @EventHandler(ignoreCancelled = true)
    public void onSelfBlast(EntityDamageEvent event) {
        if (event.getEntity().getScoreboardTags().contains(StandModel.TAG)
                || event.getEntity().getScoreboardTags().contains(SheerHeartAttack.TAG)) {
            event.setCancelled(true);
        }
    }

    private void tick() {
        for (Map.Entry<UUID, StandModel> entry : new HashMap<>(summoned).entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null || !player.isOnline() || player.isDead()) {
                entry.getValue().remove();
                summoned.remove(entry.getKey());
                continue;
            }
            entry.getValue().follow(player);
        }
        for (Map.Entry<UUID, SheerHeartAttack> entry : new HashMap<>(attacks).entrySet()) {
            SheerHeartAttack attack = entry.getValue();
            attack.tick();
            if (attack.done()) {
                attacks.remove(entry.getKey());
            }
        }
    }

    /** Takes every Stand and every bomb away; the plugin calls this when it stops. */
    public void stopAll() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        for (StandModel model : summoned.values()) {
            model.remove();
        }
        summoned.clear();
        for (SheerHeartAttack attack : attacks.values()) {
            attack.remove();
        }
        attacks.clear();
        timeStop.releaseAll();
    }

    /** One line per ability, for {@code /stand}. */
    public static List<Component> describe(StandType stand) {
        List<Component> lines = new ArrayList<>();
        switch (stand) {
            case HERMIT_PURPLE -> lines.add(MscText.rich(YELLOW, "F: ", WHITE, "Fotografía espiritual ",
                    GRAY, "(hace brillar a todo ser cercano y señala al jugador más próximo)"));
            case MAGICIANS_RED -> lines.add(MscText.rich(YELLOW, "F: ", WHITE, "Crossfire Hurricane ",
                    GRAY, "(abanico de cruces de fuego)"));
            case CRAZY_DIAMOND -> lines.add(MscText.rich(YELLOW, "F: ", WHITE, "Restauración ",
                    GRAY, "(cura al jugador que miras o a ti y repara lo que sostiene)"));
            case KILLER_QUEEN -> {
                lines.add(MscText.rich(YELLOW, "Pasiva: ", WHITE, "Primera Bomba ",
                        GRAY, "(cada jugador que golpeas recibe una explosión)"));
                lines.add(MscText.rich(YELLOW, "F o /stand sha: ", WHITE, "Sheer Heart Attack ",
                        GRAY, "(una bomba inmortal persigue al jugador que elijas)"));
            }
            case STAR_PLATINUM, THE_WORLD -> {
                lines.add(MscText.rich(YELLOW, "F: ", WHITE, stand == StandType.THE_WORLD ? "MUDA MUDA " : "ORA ORA ",
                        GRAY, "(ráfaga de puñetazos al que tienes delante)"));
                lines.add(MscText.rich(YELLOW, "Agachado + clic izquierdo con la mano vacía: ", WHITE,
                        stand == StandType.THE_WORLD ? "ZA WARUDO " : "Star Platinum: The World ",
                        GRAY, "(detiene el tiempo a tu alrededor)"));
            }
        }
        return lines;
    }
}
