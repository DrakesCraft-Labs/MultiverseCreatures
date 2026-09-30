package com.Chagui68.entities.boss;

import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the most recent {@link BossDamageSample} per player, boss and direction, so {@code /msc
 * debug} can read one player's whole picture from a single place instead of reaching into every
 * boss.
 *
 * <p>Only the latest sample of each slot is kept: the command answers "why did that last hit come
 * out that way", and a history would just push the useful line off the screen.
 *
 * <p>The log is also its own listener, so it prunes itself with the things it describes: a player's
 * samples go when they disconnect, and a boss's samples go when its stand leaves the world — which
 * is exactly when it dies or despawns. Without that, a long-running server would hold a row for
 * every player who ever fought a boss.
 */
public final class BossDamageLog implements Listener {

    private final Map<UUID, Map<String, BossDamageSample>> byPlayer = new ConcurrentHashMap<>();

    /**
     * Stores {@code sample} as the newest sample for its player, boss and direction, replacing the
     * previous one.
     */
    public void record(UUID playerId, BossDamageSample sample) {
        if (playerId == null || sample == null) return;
        byPlayer.computeIfAbsent(playerId, key -> new ConcurrentHashMap<>())
                .put(slot(sample.boss(), sample.direction()), sample);
    }

    /**
     * Every sample known for {@code playerId}, ordered by {@link BossId} and then by direction, so
     * the report is stable rather than hash-order. Empty when nothing was recorded.
     */
    public List<BossDamageSample> samplesFor(UUID playerId) {
        Map<String, BossDamageSample> slots = byPlayer.get(playerId);
        if (slots == null) return List.of();
        List<BossDamageSample> samples = new ArrayList<>();
        for (BossId boss : BossId.values()) {
            for (BossDamageSample.Direction direction : BossDamageSample.Direction.values()) {
                BossDamageSample sample = slots.get(slot(boss, direction));
                if (sample != null) samples.add(sample);
            }
        }
        return samples;
    }

    /** Drops everything recorded for a player, e.g. on logout. */
    public void forget(UUID playerId) {
        byPlayer.remove(playerId);
    }

    /**
     * Drops every player's samples for one boss, e.g. when it dies or despawns.
     *
     * <p>The log is keyed by boss and not by instance, so this also covers the rare case of two
     * instances of the same boss being alive at once. That is an acceptable trade for a debug tool:
     * the alternative would be an instance-scoped key that the command would then have to filter.
     */
    public void forgetBoss(BossId boss) {
        if (boss == null) return;
        for (BossDamageSample.Direction direction : BossDamageSample.Direction.values()) {
            String slot = slot(boss, direction);
            for (Map<String, BossDamageSample> slots : byPlayer.values()) {
                slots.remove(slot);
            }
        }
        byPlayer.values().removeIf(Map::isEmpty);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        forget(event.getPlayer().getUniqueId());
    }

    /**
     * Forgets a boss once its stand is gone. This one event covers both endings: the stand is
     * removed whether the boss was killed or despawned (idle timeout, chunk unload, plugin reload).
     */
    @EventHandler
    public void onBossStandRemoved(EntityRemoveEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        BossId boss = bossOf(stand);
        if (boss != null) forgetBoss(boss);
    }

    /** Maps a removed stand back to the boss that owns it, or {@code null} for anything else. */
    private static BossId bossOf(ArmorStand stand) {
        if (stand.getScoreboardTags().contains(ArmorStandBoss.TAG)) return BossId.SENTINEL;
        if (stand.getScoreboardTags().contains(NixBoss.TAG)) return BossId.NIX;
        if (stand.getScoreboardTags().contains(JackStarBoss.TAG)) return BossId.JACK_STAR;
        return null;
    }

    private static String slot(BossId boss, BossDamageSample.Direction direction) {
        return boss.name() + "." + direction.name();
    }
}
