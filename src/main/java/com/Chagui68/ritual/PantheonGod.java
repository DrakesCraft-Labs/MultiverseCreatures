package com.Chagui68.ritual;

import com.Chagui68.ritual.PantheonAltarStructure.Pantheon;
import org.bukkit.Material;

import java.util.Locale;
import java.util.function.Function;

/**
 * Every DrakesBosses god a Pantheon Altar can call, with the pantheon whose altar answers it and the
 * offering that wakes it.
 *
 * <p>{@link #id()} is the type DrakesBosses' {@code BossManager.spawnBoss} takes. The offerings
 * follow DrakesBosses' own altar where it has one, so a player who learnt them there already knows
 * them here; operators can override each under {@code drakes-bosses.offerings.<id>}. No two gods
 * share an offering, so the item alone says which god it calls.
 */
public enum PantheonGod {
    ZEUS("zeus", "Zeus", Pantheon.OLYMPUS, Material.LIGHTNING_ROD),
    POSEIDON("poseidon", "Poseidón", Pantheon.OLYMPUS, Material.HEART_OF_THE_SEA),
    HADES("hades", "Hades", Pantheon.OLYMPUS, Material.WITHER_SKELETON_SKULL),
    ARES("ares", "Ares", Pantheon.OLYMPUS, Material.NETHERITE_SWORD),
    ARTEMISA("artemisa", "Artemisa", Pantheon.OLYMPUS, Material.SPECTRAL_ARROW),
    PROMETEO("prometeo", "Prometeo", Pantheon.OLYMPUS, Material.FIRE_CHARGE),
    CIRCE("circe", "Circe", Pantheon.OLYMPUS, Material.AMETHYST_SHARD),
    POLIFEMO("polifemo", "Polifemo", Pantheon.OLYMPUS, Material.FERMENTED_SPIDER_EYE),
    KRATOS("kratos", "Kratos", Pantheon.OLYMPUS, Material.NETHERITE_AXE),
    TIFON("tifon", "Tifón", Pantheon.OLYMPUS, Material.MAGMA_BLOCK),
    HIDRA("hidra", "Hidra", Pantheon.OLYMPUS, Material.PRISMARINE_SHARD),
    CERBERO("cerbero", "Cerbero", Pantheon.OLYMPUS, Material.BONE_BLOCK),

    THOR("thor", "Thor", Pantheon.ASGARD, Material.IRON_BLOCK),
    ODIN("odin", "Odín", Pantheon.ASGARD, Material.GOLD_BLOCK),
    LOKI("loki", "Loki", Pantheon.ASGARD, Material.ENDER_PEARL),
    HEIMDALL("heimdall", "Heimdall", Pantheon.ASGARD, Material.BLAZE_ROD),

    RA("ra", "Ra", Pantheon.DUAT, Material.GOLDEN_CARROT),
    ISIS("isis", "Isis", Pantheon.DUAT, Material.FEATHER),
    ANUBIS("anubis", "Anubis", Pantheon.DUAT, Material.ROTTEN_FLESH),
    SET("set", "Set", Pantheon.DUAT, Material.REDSTONE_BLOCK),

    COLOSO_END("coloso_end", "Coloso del End", Pantheon.VOID, Material.ECHO_SHARD),
    GAROU_COSMICO("garou_cosmico", "Garou Cósmico", Pantheon.VOID, Material.NETHER_STAR),
    DIOS_CORRUPTO("dios_corrupto", "Dios Corrupto", Pantheon.VOID, Material.TOTEM_OF_UNDYING),
    WITHER_STORM("wither_storm", "Wither Storm", Pantheon.VOID, Material.WITHER_ROSE),
    DRAGON_ANCESTRAL("dragon_ancestral", "Dragón Ancestral", Pantheon.VOID, Material.DRAGON_BREATH),
    JAX("jax", "Jax", Pantheon.VOID, Material.LANTERN);

    /** Config section holding the offering overrides, one key per {@link #id()}. */
    public static final String OFFERINGS_KEY = "drakes-bosses.offerings";

    private final String id;
    private final String displayName;
    private final Pantheon pantheon;
    private final Material defaultOffering;

    PantheonGod(String id, String displayName, Pantheon pantheon, Material defaultOffering) {
        this.id = id;
        this.displayName = displayName;
        this.pantheon = pantheon;
        this.defaultOffering = defaultOffering;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Pantheon pantheon() {
        return pantheon;
    }

    public Material defaultOffering() {
        return defaultOffering;
    }

    /**
     * The offering this god takes, reading the override through {@code configured} (id → material
     * name, null when unset). An unknown or non-item name falls back to the default.
     */
    public Material offering(Function<String, String> configured) {
        String name = configured.apply(id);
        if (name == null || name.isBlank()) return defaultOffering;
        Material material = Material.matchMaterial(name.trim().toUpperCase(Locale.ROOT));
        return material != null && material.isItem() && !material.isAir() ? material : defaultOffering;
    }

    /** The god this item calls, or null. The first god in declaration order wins a duplicate override. */
    public static PantheonGod byOffering(Material item, Function<String, String> configured) {
        for (PantheonGod god : values()) {
            if (god.offering(configured) == item) return god;
        }
        return null;
    }
}
