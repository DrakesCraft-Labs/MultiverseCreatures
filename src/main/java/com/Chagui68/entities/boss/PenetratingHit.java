package com.Chagui68.entities.boss;

/**
 * Immutable snapshot of one Obsidian Sentinel penetrating hit, kept so {@code /msc debug} can show
 * why a player took the damage they did.
 *
 * <p>The numbers only exist while the damage event is being processed, so the handler builds this
 * record and stores it; the command later reads it back. The pipeline itself is the same three steps
 * the live handler runs — credit the armour/Protection/Resistance modifiers back, apply the per-hit
 * cap, then apply the partially pierced Resistance — which is why {@link #of} is the single place
 * that arithmetic lives.
 *
 * @param eventDamage               damage the engine reported on the original event
 * @param armorCreditedBack         {@code ARMOR} modifier that was credited back (negative)
 * @param protectionCreditedBack    {@code MAGIC} modifier (Protection enchantments) credited back
 * @param resistanceCreditedBack    {@code RESISTANCE} modifier (the potion) credited back
 * @param throughArmor              damage once those three are credited back
 * @param cap                       {@code max-damage-dealt} applied before Resistance
 * @param raw                       damage after the cap
 * @param resistanceAmplifier       player's Resistance amplifier, {@code -1} when they have none
 * @param pierce                    share of the potion's mitigation the boss ignored
 * @param dealt                     final damage the player was actually hit with
 * @param timestampMillis           when the hit was recorded, for the age shown by the command
 */
public record PenetratingHit(
        double eventDamage,
        double armorCreditedBack,
        double protectionCreditedBack,
        double resistanceCreditedBack,
        double throughArmor,
        double cap,
        double raw,
        int resistanceAmplifier,
        double pierce,
        double dealt,
        long timestampMillis) {

    /**
     * Runs the penetrating-damage pipeline with the same maths the live handler uses.
     *
     * <p>Kept here rather than inline in the event handler so the whole calculation — including the
     * order of the cap and the Resistance step — is one testable path.
     */
    public static PenetratingHit of(double eventDamage,
                                    double armorCreditedBack,
                                    double protectionCreditedBack,
                                    double resistanceCreditedBack,
                                    int resistanceAmplifier,
                                    double pierce,
                                    double cap,
                                    long timestampMillis) {
        double throughArmor = ArmorStandBoss.unmitigated(eventDamage,
                armorCreditedBack, protectionCreditedBack, resistanceCreditedBack);
        double raw = Math.min(throughArmor, cap);
        double dealt = ArmorStandBoss.penetratingDamage(raw, resistanceAmplifier, pierce);
        return new PenetratingHit(eventDamage, armorCreditedBack, protectionCreditedBack,
                resistanceCreditedBack, throughArmor, cap, raw, resistanceAmplifier, pierce, dealt,
                timestampMillis);
    }

    /** Milliseconds elapsed since this hit was recorded; never negative for a clock that went back. */
    public long ageMillis(long now) {
        return Math.max(0L, now - timestampMillis);
    }

    /** Resistance level shown to players (amplifier + 1), or {@code 0} when the effect is absent. */
    public int resistanceLevel() {
        return resistanceAmplifier < 0 ? 0 : resistanceAmplifier + 1;
    }
}
