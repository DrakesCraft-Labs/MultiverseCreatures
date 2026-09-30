package com.Chagui68.entities.boss;

/**
 * Jack Star's incoming-damage maths: the Load Balancer split and the chances it raises while the
 * boss is demoted.
 *
 * <p>Both used to live inline in the damage handler, where the numbers could not be read or tested:
 * a share of the hit was silently redirected to everyone standing nearby, and the dodge and packet
 * loss chances were swapped for {@code 0.45} by an unnamed scale threshold. The split conserves the
 * hit exactly — {@code toBoss + sharedTotal == incoming} — which is the property worth protecting,
 * so a tuning change cannot quietly delete or duplicate damage.
 */
public final class JackResilience {

    /** Share of an incoming hit the party absorbs together. */
    public static final double SHARED_SHARE = 0.35;
    /** Share of an incoming hit that still lands on Jack. */
    public static final double DIRECT_SHARE = 0.65;
    /** Radius within which players count as part of the party. */
    public static final double PARTY_RADIUS = 14.0;
    /** Scale under which Jack is demoted and dodges / drops packets more often. */
    public static final float DEMOTED_SCALE = 0.8f;
    /** Chance used instead of the configured one while Jack is demoted. */
    public static final double DEMOTED_CHANCE = 0.45;

    private JackResilience() {
    }

    /**
     * Divides one incoming hit between Jack and the party. A party of one (or none) does not split
     * anything, so a solo fight is fair game.
     */
    public static Split splitIncoming(double incoming, int partySize) {
        if (partySize <= 1) return new Split(incoming, 0.0, 0.0);
        double sharedTotal = incoming * SHARED_SHARE;
        return new Split(incoming * DIRECT_SHARE, sharedTotal, sharedTotal / partySize);
    }

    /**
     * The chance to roll for a dodge or a packet drop: the configured one, unless Jack is demoted,
     * in which case both jump to {@link #DEMOTED_CHANCE}.
     */
    public static double effectiveChance(double configuredChance, float scale) {
        return scale < DEMOTED_SCALE ? DEMOTED_CHANCE : configuredChance;
    }

    /** The outcome of a hit that was dodged outright: nothing at all reaches Jack. */
    private static final Resolution DODGED = new Resolution(true, new Split(0.0, 0.0, 0.0));

    /**
     * Decides what one incoming hit does to Jack: a dodge takes nothing, anything else is split.
     *
     * <p>This is the only door a hit can come through, so "JackStar took no damage" can only ever
     * mean an explicit dodge. Earlier versions could silently swallow a hit behind a ritual or an
     * immunity flag, which is exactly how a boss ends up unkillable.
     *
     * @param roll a value in {@code [0, 1)} drawn by the caller, compared against the dodge chance
     */
    public static Resolution resolve(double incoming, double roll, double dodgeChance, int partySize) {
        if (roll < dodgeChance) return DODGED;
        return new Resolution(false, splitIncoming(incoming, partySize));
    }

    /**
     * One load-balancer split.
     *
     * @param toBoss         damage that still lands on Jack
     * @param sharedTotal    damage the party absorbs together
     * @param perPartyMember damage each party member is asked to take
     */
    public record Split(double toBoss, double sharedTotal, double perPartyMember) {
    }

    /** A resolved hit: whether Jack dodged it, plus the split to apply when he did not. */
    public record Resolution(boolean dodged, Split split) {

        /** Damage that still lands on Jack. */
        public double toBoss() {
            return split.toBoss();
        }
    }
}
