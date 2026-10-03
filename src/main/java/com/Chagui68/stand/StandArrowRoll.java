package com.Chagui68.stand;

/**
 * What happens to somebody the Arrow pierces. Kept free of Bukkit so the rule is tested.
 *
 * <ul>
 *   <li>A Stand bearer (who drank the Bearer's Elixir) is safe: the Arrow never kills them and
 *   always awakens their Stand.</li>
 *   <li>Anybody else is judged: most die, and dying marks them unworthy; the ones it spares
 *   awaken a Stand.</li>
 *   <li>An unworthy player was already judged: the Arrow gives them nothing until DIO's blood
 *   makes them a bearer (drinking it clears the mark).</li>
 *   <li>Somebody who already has a Stand is never chosen again.</li>
 * </ul>
 */
public final class StandArrowRoll {

    /** The outcomes of a hit. */
    public enum Outcome {
        /** The Arrow kills, and marks the player unworthy. */
        DEATH,
        /** A Stand awakens. */
        STAND,
        /** Already judged unworthy: no Stand without DIO's blood. */
        REJECTED,
        /** Already a Stand user: the Arrow only resonates with the Stand. */
        RESONATE
    }

    private StandArrowRoll() {
    }

    /**
     * @param roll        uniform roll in {@code [0, 1)}
     * @param deathChance chance of dying for a player who is not a bearer, 0.7 by default
     * @param bearer      true when the player drank the Bearer's Elixir
     * @param unworthy    true when the Arrow already killed the player once
     * @param hasStand    true when the player already has a Stand
     */
    public static Outcome decide(double roll, double deathChance, boolean bearer, boolean unworthy, boolean hasStand) {
        if (hasStand) {
            return Outcome.RESONATE;
        }
        if (bearer) {
            return Outcome.STAND;
        }
        if (unworthy) {
            return Outcome.REJECTED;
        }
        if (roll < Math.max(0.0, Math.min(1.0, deathChance))) {
            return Outcome.DEATH;
        }
        return Outcome.STAND;
    }
}
