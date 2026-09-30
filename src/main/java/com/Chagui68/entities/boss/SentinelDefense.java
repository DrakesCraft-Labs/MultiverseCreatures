package com.Chagui68.entities.boss;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.BossInstance.DefenseState;

import java.util.ArrayList;
import java.util.List;

/**
 * The Obsidian Sentinel's defences, frozen at the moment a hit lands, plus the pure maths that turns
 * that hit into the damage the boss actually loses.
 *
 * <p>The event handler used to inline this: halve for a planted shield seal, shave for a healing
 * circle, apply the active defence, spend the absorb shield, then clamp to
 * {@code max-damage-per-hit}. That is the boss's balance in one place, and it lived inside a
 * 2 300-line class with no test touching it. Here it is a {@code resolve} call with no server, no
 * entity and no side effect, so every step — including the order of the cap and the absorb shield —
 * is pinned by a test.
 *
 * <p>Everything that has to *happen* rather than be computed stays in the caller:
 * {@link Result#absorbed()} is subtracted from the live shield, {@link Result#reflected()} is thrown
 * back at the attacker, and {@link Result#shieldBroken()} and {@link Result#steps()} drive the
 * sounds and the {@code /msc debug} report.
 *
 * @param invulnerable     the boss is shielded during its invocation; the hit is ignored entirely
 * @param shieldSealActive a planted seal halves incoming damage
 * @param healingCircleActive the healing circle shaves a fifth off incoming damage
 * @param activeDefense    the defence currently up, or {@link DefenseState#NONE}
 * @param absorbShieldHealth health left on the absorb shield
 * @param cap              {@code max-damage-per-hit}, applied last
 */
public record SentinelDefense(
        boolean invulnerable,
        boolean shieldSealActive,
        boolean healingCircleActive,
        DefenseState activeDefense,
        double absorbShieldHealth,
        double cap) {

    /** Damage multiplier while a shield seal is planted. */
    static final double SHIELD_SEAL_MULTIPLIER = 0.5;
    /** Damage multiplier while the healing circle is active. */
    static final double HEALING_CIRCLE_MULTIPLIER = 0.8;
    /** Damage multiplier under stone skin. */
    static final double STONE_SKIN_MULTIPLIER = 0.5;
    /** Damage multiplier under the reflect barrier. */
    static final double REFLECT_BARRIER_MULTIPLIER = 0.7;
    /** Share of the reduced hit the reflect barrier throws back at the attacker. */
    static final double REFLECT_SHARE = 0.3;

    /**
     * Snapshots a boss instance's defensive state. A {@code null} instance — a stand that is not a
     * tracked boss — has every defence off, which leaves only the cap in force.
     */
    public static SentinelDefense from(BossInstance instance, double cap) {
        if (instance == null) {
            return new SentinelDefense(false, false, false, DefenseState.NONE, 0.0, cap);
        }
        return new SentinelDefense(instance.invulnerable, instance.shieldSealActive,
                instance.healingCircleActive, instance.activeDefense, instance.absorbShieldHealth, cap);
    }

    /**
     * Resolves one incoming hit. The order matters and is the contract: seal, healing circle,
     * active defence (spending the absorb shield), and only then the cap — so no burst can push a
     * hit past {@code max-damage-per-hit} and the potion/defence stack cannot raise it either.
     */
    public Result resolve(double incoming) {
        List<String> steps = new ArrayList<>();

        if (invulnerable) {
            steps.add("invulnerable");
            return new Result(0.0, 0.0, 0.0, false, List.copyOf(steps));
        }

        double damage = incoming;
        double absorbed = 0.0;
        double reflected = 0.0;
        boolean shieldBroken = false;

        if (shieldSealActive) {
            damage *= SHIELD_SEAL_MULTIPLIER;
            steps.add("shield seal \u00d7" + SHIELD_SEAL_MULTIPLIER);
        }
        if (healingCircleActive) {
            damage *= HEALING_CIRCLE_MULTIPLIER;
            steps.add("healing circle \u00d7" + HEALING_CIRCLE_MULTIPLIER);
        }

        if (activeDefense == DefenseState.STONE_SKIN) {
            damage *= STONE_SKIN_MULTIPLIER;
            steps.add("stone skin \u00d7" + STONE_SKIN_MULTIPLIER);
        } else if (activeDefense == DefenseState.REFLECT_BARRIER) {
            damage *= REFLECT_BARRIER_MULTIPLIER;
            steps.add("reflect barrier \u00d7" + REFLECT_BARRIER_MULTIPLIER);
            // Taken from the reduced hit, before the cap: the barrier punishes what it deflected.
            reflected = damage * REFLECT_SHARE;
        } else if (activeDefense == DefenseState.ABSORB_SHIELD) {
            absorbed = Math.min(absorbShieldHealth, damage);
            damage -= absorbed;
            if (absorbed > 0) steps.add("absorb shield -" + absorbed);
            shieldBroken = absorbShieldHealth - absorbed <= 0;
            if (damage < 0) damage = 0;
        }

        if (damage > cap) {
            damage = cap;
            steps.add("cap " + cap);
        }
        if (damage < 0) damage = 0;

        return new Result(damage, absorbed, reflected, shieldBroken, List.copyOf(steps));
    }

    /**
     * One resolved hit. {@code steps} is the human-readable trace of what the hit went through, in
     * order, and is what the caller joins into the {@code /msc debug} note.
     *
     * @param applied      damage the boss loses from this hit
     * @param absorbed     damage the absorb shield soaked up (subtract it from the live shield)
     * @param reflected    damage the reflect barrier throws back at the attacker, or {@code 0}
     * @param shieldBroken the absorb shield is now empty
     * @param steps        ordered trace of the defences applied
     */
    public record Result(double applied, double absorbed, double reflected, boolean shieldBroken,
                         List<String> steps) {
    }
}
