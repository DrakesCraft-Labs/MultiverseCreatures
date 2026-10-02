package com.Chagui68.entities.boss.fx;

/**
 * The status effects attacks inflict, by their vanilla key.
 *
 * <p>Named here rather than through {@code PotionEffectType}, which is registry-backed and cannot be
 * touched outside a running server; the game stage resolves the key when it applies the effect.
 */
public enum Affliction {
    SLOWNESS("slowness"),
    WEAKNESS("weakness"),
    BLINDNESS("blindness"),
    DARKNESS("darkness"),
    LEVITATION("levitation"),
    WITHER("wither"),
    POISON("poison"),
    GLOWING("glowing"),
    NAUSEA("nausea"),
    MINING_FATIGUE("mining_fatigue"),
    HUNGER("hunger");

    private final String key;

    Affliction(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
