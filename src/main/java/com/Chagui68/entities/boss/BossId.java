package com.Chagui68.entities.boss;

/**
 * The bosses {@code /msc debug} knows about, declared in the order their sections are printed.
 *
 * <p>An enum (rather than a string key) keeps the damage log and the report from drifting apart: a
 * sample recorded for a boss that is not listed here could never be rendered.
 */
public enum BossId {

    SENTINEL("OBSIDIAN SENTINEL"),
    NIX("NIX - THE EXECUTIONER"),
    JACK_STAR("JACK STAR - THE SYSTEM ARCHITECT");

    private final String displayName;

    BossId(String displayName) {
        this.displayName = displayName;
    }

    /** Heading shown above the boss's section in the chat report. */
    public String displayName() {
        return displayName;
    }
}
