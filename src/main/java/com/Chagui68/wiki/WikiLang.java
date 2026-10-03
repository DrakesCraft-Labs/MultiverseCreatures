package com.Chagui68.wiki;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

/**
 * The two languages the wiki is written in. A player reads it in the language of their game
 * client until they switch it with the flag in the menu; that choice is kept on the player.
 */
public enum WikiLang {
    EN("English"),
    ES("Español");

    private static final NamespacedKey KEY = new NamespacedKey("multiversecreatures", "wiki_lang");

    private final String label;

    WikiLang(String label) {
        this.label = label;
    }

    /** The language's own name, as the toggle shows it. */
    public String label() {
        return label;
    }

    /** The text in this language. */
    public String pick(String en, String es) {
        return this == ES ? es : en;
    }

    public WikiLang other() {
        return this == ES ? EN : ES;
    }

    /** The language a player reads the wiki in: their own choice, else their client's. */
    public static WikiLang of(Player player) {
        String stored = player.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (stored != null) {
            try {
                return valueOf(stored);
            } catch (IllegalArgumentException unknown) {
                // An old or hand-edited value: fall back to the client language.
                com.Chagui68.utils.MscLog.debug("Unknown wiki language " + stored + " on " + player.getName(), unknown);
            }
        }
        Locale locale = player.locale();
        return fromLocale(locale == null ? null : locale.getLanguage());
    }

    /** Spanish for any Spanish client ({@code es}, {@code es_mx}, ...), English otherwise. */
    public static WikiLang fromLocale(String language) {
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("es") ? ES : EN;
    }

    public static void set(Player player, WikiLang lang) {
        player.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, lang.name());
    }
}
