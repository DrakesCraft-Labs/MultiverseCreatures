package com.Chagui68.entities.boss;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.List;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.BossInstance;

/**
 * Lo que un ataque necesita de su jefe.
 *
 * POR QUE EXISTE
 *
 * Los 42 ataques del plugin estaban escritos contra ArmorStandBoss, la clase concreta del
 * Centinela de Obsidiana. Funcionan perfectamente, pero al depender del tipo concreto no los
 * puede usar ningun otro jefe: para reaprovechar un ataque habia que copiarlo y cambiarle el
 * tipo, que es como acaban divergiendo dos copias del mismo codigo.
 *
 * Con esta interfaz en medio, cualquier jefe que la implemente reutiliza los 42 tal cual. Ese es
 * el objetivo: que todos los jefes del servidor compartan la misma logica de ataque y solo se
 * diferencien en sus valores y su puesta en escena.
 *
 * QUE ENTRA Y QUE NO
 *
 * Aqui esta solo lo generico: quien hay cerca, donde esta el suelo, como se empuja a un jugador,
 * como se resetea la pose. Lo propio del Centinela -- el pentagrama del cielo, la lanza de
 * netherita, los tiempos de su escudo -- se queda en ArmorStandBoss, porque un jefe distinto no
 * tiene por que tener nada de eso.
 */
public interface BossHost {

    /** El plugin, para programar tareas y leer configuracion. */
    MultiverseCreatures getPlugin();

    /** Devuelve al jefe a su pose de reposo despues de un ataque. */
    void resetBossPose(BossInstance instance);

    /** Los jugadores del mundo a los que este jefe puede atacar. */
    default List<Player> getValidPlayers(World world) {
        return BossArena.getValidPlayers(world);
    }

    /** Los jugadores validos dentro de un radio, dado al cuadrado. */
    default List<Player> getValidPlayersNear(Location center, double radiusSq) {
        return BossArena.getValidPlayersNear(center, radiusSq);
    }

    /** Empuja a un jugador hacia arriba. */
    default void launchPlayer(Player p, double y) {
        BossArena.launchPlayer(p, y);
    }

    /** La altura del suelo bajo una ubicacion. */
    default double getGroundY(Location loc, double maxScan) {
        return BossArena.getGroundY(loc, maxScan);
    }

    /** Si la entidad del jefe esta pisando suelo. */
    default boolean isOnGround(BossPuppet stand) {
        return BossArena.isOnGround(stand);
    }

    /** Cuantos jugadores validos hay en el radio. */
    default int countPlayersInRange(Location center, double radius) {
        return BossArena.countPlayersInRange(center, radius);
    }

    /** El jugador valido mas cercano, o null. */
    default Player findNearestPlayer(Location center, double range) {
        return BossArena.findNearestPlayer(center, range);
    }

    /** El objetivo del jefe segun su alcance de agresion, o null. */
    Player detectTarget(BossPuppet stand);

    /** Onda expansiva desde un punto. La comparten varios ataques de suelo. */
    void spawnShockwaveWave(World world, Location center, double maxRadius);

    /**
     * Daño de los sellos y del bombardeo aereo.
     *
     * Llevan valor por defecto porque un jefe nuevo no tiene por que usar ninguno de los dos
     * ataques que los consultan; el Centinela los sobrescribe con lo que diga su configuracion.
     */
    default double getSealDamage() {
        return 6.0;
    }

    default double getHoverBarrageDamage() {
        return 4.0;
    }
}
