package com.Chagui68.stand;

/**
 * What happens to somebody the Arrow pierces. Kept free of Bukkit so the rule is tested.
 *
 * <p>The Arrow kills most of the people it hits. The ones it spares awaken a Stand, but only a
 * body that drank DIO's blood (a Stand bearer) can hold one: anybody else survives the wound
 * and nothing more. Somebody who already has a Stand is never chosen again.</p>
 */
public final class StandArrowRoll {

    /** The outcomes of a hit. */
    public enum Outcome {
        /** The Arrow kills. */
        DEATH,
        /** A bearer awakens a Stand. */
        STAND,
        /** Spared, but the body cannot hold a Stand without DIO's blood. */
        REJECTED,
        /** Already a Stand user: the Arrow only resonates with the Stand. */
        RESONATE
    }

    private StandArrowRoll() {
    }

    /**
     * @param roll        uniform roll in {@code [0, 1)}
     * @param deathChance chance of dying, 0.7 by default
     * @param bearer      true when the player drank the Bearer's Elixir
     * @param hasStand    true when the player already has a Stand
     */
    public static Outcome decide(double roll, double deathChance, boolean bearer, boolean hasStand) {
        if (hasStand) {
            return Outcome.RESONATE;
        }
        if (roll < Math.max(0.0, Math.min(1.0, deathChance))) {
            return Outcome.DEATH;
        }
        return bearer ? Outcome.STAND : Outcome.REJECTED;
    }
}
