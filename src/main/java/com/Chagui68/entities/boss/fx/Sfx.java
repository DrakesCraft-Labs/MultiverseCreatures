package com.Chagui68.entities.boss.fx;

/**
 * The sounds attacks use, by their vanilla key.
 *
 * <p>Played by key rather than through {@code org.bukkit.Sound}: that type is backed by the server's
 * registry, so touching it outside a running server (in a test, in a preview) throws.
 */
public enum Sfx {
    ANVIL_LAND("block.anvil.land"),
    ANVIL_PLACE("block.anvil.place"),
    IRON_DOOR_CLOSE("block.iron_door.close"),
    NOTE_HAT("block.note_block.hat"),
    NOTE_BASEDRUM("block.note_block.basedrum"),
    PLAYER_ATTACK_KNOCKBACK("entity.player.attack.knockback"),
    WITHER_AMBIENT("entity.wither.ambient"),
    WITHER_DEATH("entity.wither.death"),
    ZOMBIE_WOODEN_DOOR("entity.zombie.break_wooden_door"),
    BEACON_ACTIVATE("block.beacon.activate"),
    BEACON_DEACTIVATE("block.beacon.deactivate"),
    BEACON_POWER("block.beacon.power_select"),
    BELL("block.bell.use"),
    BELL_RESONATE("block.bell.resonate"),
    BLAZE_SHOOT("entity.blaze.shoot"),
    BREEZE_SHOOT("entity.breeze.shoot"),
    BREEZE_WIND_BURST("entity.breeze.wind_burst"),
    CHAIN_BREAK("block.chain.break"),
    CONDUIT_ACTIVATE("block.conduit.activate"),
    DRAGON_FLAP("entity.ender_dragon.flap"),
    DRAGON_GROWL("entity.ender_dragon.growl"),
    DRAGON_SHOOT("entity.ender_dragon.shoot"),
    ELDER_GUARDIAN_CURSE("entity.elder_guardian.curse"),
    ENCHANT("block.enchantment_table.use"),
    END_PORTAL_SPAWN("block.end_portal.spawn"),
    ENDERMAN_TELEPORT("entity.enderman.teleport"),
    EVOKER_CAST("entity.evoker.cast_spell"),
    EVOKER_FANGS("entity.evoker_fangs.attack"),
    EVOKER_PREPARE_SUMMON("entity.evoker.prepare_summon"),
    EXPLODE("entity.generic.explode"),
    FIRE_EXTINGUISH("block.fire.extinguish"),
    FIRECHARGE("item.firecharge.use"),
    GLASS_BREAK("block.glass.break"),
    ILLUSIONER_CAST("entity.illusioner.cast_spell"),
    ILLUSIONER_MIRROR("entity.illusioner.prepare_mirror"),
    IRON_GOLEM_ATTACK("entity.iron_golem.attack"),
    IRON_GOLEM_DEATH("entity.iron_golem.death"),
    LIGHTNING_IMPACT("entity.lightning_bolt.impact"),
    LIGHTNING_THUNDER("entity.lightning_bolt.thunder"),
    MACE_SMASH_GROUND("item.mace.smash_ground_heavy"),
    PLAYER_ATTACK_SWEEP("entity.player.attack.sweep"),
    PLAYER_ATTACK_STRONG("entity.player.attack.strong"),
    PLAYER_ATTACK_CRIT("entity.player.attack.crit"),
    POINTED_DRIPSTONE_LAND("block.pointed_dripstone.land"),
    RAVAGER_ROAR("entity.ravager.roar"),
    RESPAWN_ANCHOR_CHARGE("block.respawn_anchor.charge"),
    RESPAWN_ANCHOR_DEPLETE("block.respawn_anchor.deplete"),
    SCULK_SHRIEK("block.sculk_shrieker.shriek"),
    SHIELD_BLOCK("item.shield.block"),
    SHIELD_BREAK("item.shield.break"),
    SHULKER_SHOOT("entity.shulker.shoot"),
    SOUL_ESCAPE("particle.soul_escape"),
    TRIDENT_RIPTIDE("item.trident.riptide_3"),
    TRIDENT_THROW("item.trident.throw"),
    TRIDENT_THUNDER("item.trident.thunder"),
    TRIAL_SPAWNER_OMINOUS("block.trial_spawner.ominous_activate"),
    WARDEN_ROAR("entity.warden.roar"),
    WARDEN_SONIC_BOOM("entity.warden.sonic_boom"),
    WARDEN_SONIC_CHARGE("entity.warden.sonic_charge"),
    WARDEN_HEARTBEAT("entity.warden.heartbeat"),
    WITHER_SHOOT("entity.wither.shoot"),
    WITHER_SPAWN("entity.wither.spawn"),
    WITHER_BREAK_BLOCK("entity.wither.break_block"),
    ZOMBIE_IRON_DOOR("entity.zombie.attack_iron_door"),
    GLOW_HIT("entity.glow_squid.squirt"),
    AMETHYST_CHIME("block.amethyst_block.chime"),
    AMETHYST_BREAK("block.amethyst_cluster.break"),
    VAULT_OMINOUS("block.vault.activate"),
    WIND_CHARGE_BURST("entity.wind_charge.wind_burst"),
    PHANTOM_SWOOP("entity.phantom.swoop"),
    BLAZE_AMBIENT("entity.blaze.ambient"),
    GHAST_SHOOT("entity.ghast.shoot"),
    BLOCK_LAVA_POP("block.lava.pop"),
    SOUL_SAND_BREAK("block.soul_sand.break"),
    BONE_BREAK("block.bone_block.break"),
    TOTEM_USE("item.totem.use");

    private final String key;

    Sfx(String key) {
        this.key = key;
    }

    /** The vanilla sound key, as {@code World.playSound} takes it. */
    public String key() {
        return key;
    }
}
