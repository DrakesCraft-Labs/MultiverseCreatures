package com.Chagui68.entities.boss;

/**
 * One damage sample a boss produced or absorbed, kept so {@code /msc debug} can show a per-player
 * breakdown of every boss's damage and its caps.
 *
 * <p>Both directions share one shape so the report reads the same everywhere:
 * <ul>
 *   <li>{@link Direction#DEALT_TO_PLAYER} — {@code before} is the damage the attack asked for and
 *       {@code after} is what the player actually took, once their armour and effects applied;</li>
 *   <li>{@link Direction#TAKEN_FROM_PLAYER} — {@code before} is the hit as the engine reported it and
 *       {@code after} is what the boss actually lost, i.e. after its cap or split.</li>
 * </ul>
 * {@code note} carries the free-text explanation of the steps in between (active defences, the cap
 * in force, the load-balancer split), which is the part the command exists to reveal.
 *
 * @param boss            which boss this sample belongs to
 * @param direction       whether the boss dealt the damage or took it
 * @param source          the attack or mechanic that produced the sample
 * @param before          damage before the boss's mechanic or the player's defences
 * @param after           damage actually applied
 * @param note            explanation of the steps between {@code before} and {@code after}; may be blank
 * @param timestampMillis when the sample was recorded, for the age shown by the command
 */
public record BossDamageSample(
        BossId boss,
        Direction direction,
        String source,
        double before,
        double after,
        String note,
        long timestampMillis) {

    /** Which way the damage travelled relative to the boss. */
    public enum Direction {
        DEALT_TO_PLAYER,
        TAKEN_FROM_PLAYER
    }

    /** A hit the boss landed on a player: {@code intended} damage vs. what the player actually took. */
    public static BossDamageSample dealt(BossId boss, String source, double intended, double taken,
                                         long timestampMillis) {
        return new BossDamageSample(boss, Direction.DEALT_TO_PLAYER, source, intended, taken, "",
                timestampMillis);
    }

    /** A hit the boss absorbed: {@code incoming} damage vs. what it lost after {@code note}'s steps. */
    public static BossDamageSample taken(BossId boss, String source, double incoming, double applied,
                                         String note, long timestampMillis) {
        return new BossDamageSample(boss, Direction.TAKEN_FROM_PLAYER, source, incoming, applied,
                note == null ? "" : note, timestampMillis);
    }

    /** Milliseconds elapsed since the sample was recorded; never negative for a clock that went back. */
    public long ageMillis(long now) {
        return Math.max(0L, now - timestampMillis);
    }
}
