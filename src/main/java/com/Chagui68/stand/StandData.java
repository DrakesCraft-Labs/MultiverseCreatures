package com.Chagui68.stand;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * What a player is, stored in the player itself so it survives restarts and logouts: whether
 * they drank DIO's blood (which makes them a vampire and a Stand bearer) and which Stand, if
 * any, the Arrow awakened in them.
 */
public final class StandData {

    public static final NamespacedKey BEARER = new NamespacedKey("multiversecreatures", "msc_stand_bearer");
    public static final NamespacedKey VAMPIRE = new NamespacedKey("multiversecreatures", "msc_vampire");
    public static final NamespacedKey STAND = new NamespacedKey("multiversecreatures", "msc_stand");

    private StandData() {
    }

    public static boolean isBearer(Player player) {
        return player.getPersistentDataContainer().has(BEARER, PersistentDataType.BYTE);
    }

    public static boolean isVampire(Player player) {
        return player.getPersistentDataContainer().has(VAMPIRE, PersistentDataType.BYTE);
    }

    /** Makes the player a vampire who can carry a Stand: what the Bearer's Elixir does. */
    public static void drinkBlood(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(BEARER, PersistentDataType.BYTE, (byte) 1);
        data.set(VAMPIRE, PersistentDataType.BYTE, (byte) 1);
    }

    /** Takes the curse away: no longer a vampire nor a bearer (the Stand stays). */
    public static void cure(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(BEARER);
        data.remove(VAMPIRE);
    }

    /** The player's Stand, or null. */
    public static StandType stand(Player player) {
        return StandType.byKey(player.getPersistentDataContainer().get(STAND, PersistentDataType.STRING));
    }

    public static void setStand(Player player, StandType type) {
        if (type == null) {
            player.getPersistentDataContainer().remove(STAND);
        } else {
            player.getPersistentDataContainer().set(STAND, PersistentDataType.STRING, type.key());
        }
    }
}
