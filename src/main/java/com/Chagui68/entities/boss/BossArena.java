package com.Chagui68.entities.boss;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

/**
 * Consultas sobre el terreno y los jugadores alrededor de un jefe.
 *
 * POR QUE ESTA APARTE
 *
 * Estos metodos vivian dentro de ArmorStandBoss, una clase de mas de dos mil lineas. No usaban
 * nada del jefe: solo miran un mundo, una ubicacion o un jugador. Eso los ata a un jefe concreto
 * sin motivo, y es justo lo que impedia que otro jefe reutilizara los 42 ataques ya escritos.
 *
 * Al ser estaticos y sin estado, cualquier jefe puede llamarlos, y ademas se pueden probar solos.
 *
 * QUE CUENTA COMO JUGADOR VALIDO
 *
 * En todas partes se descartan igual los muertos, los de creativo y los espectadores. Estaba
 * repetido en cinco sitios con la misma condicion copiada; aqui se decide una sola vez, para que
 * no pueda quedar la mitad actualizada.
 */
public final class BossArena {

    private BossArena() {}

    /** Si a este jugador le afectan los ataques del jefe. */
    public static boolean esObjetivoValido(Player p) {
        return !p.isDead()
                && p.getGameMode() != GameMode.CREATIVE
                && p.getGameMode() != GameMode.SPECTATOR;
    }

    /** Los jugadores del mundo a los que el jefe puede atacar. */
    public static List<Player> getValidPlayers(World world) {
        List<Player> result = new ArrayList<>();
        if (world == null) {
            return result;
        }
        for (Player p : world.getPlayers()) {
            if (esObjetivoValido(p)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Los jugadores validos dentro de un radio.
     *
     * El radio va al cuadrado a proposito: los ataques comparan distancias muchas veces por tick
     * y la raiz cuadrada es lo caro de la operacion.
     */
    public static List<Player> getValidPlayersNear(Location center, double radiusSq) {
        List<Player> result = new ArrayList<>();
        if (center == null || center.getWorld() == null) {
            return result;
        }
        for (Player p : center.getWorld().getPlayers()) {
            if (esObjetivoValido(p) && p.getLocation().distanceSquared(center) <= radiusSq) {
                result.add(p);
            }
        }
        return result;
    }

    /** Cuantos jugadores validos hay dentro del radio. */
    public static int countPlayersInRange(Location center, double radius) {
        return getValidPlayersNear(center, radius * radius).size();
    }

    /** El jugador valido mas cercano dentro del alcance, o null si no hay ninguno. */
    public static Player findNearestPlayer(Location center, double range) {
        Player nearest = null;
        double nearestDistSq = range * range;
        for (Player p : getValidPlayers(center.getWorld())) {
            double distSq = p.getLocation().distanceSquared(center);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }

    /** Distancia al jugador valido mas cercano, o Double.MAX_VALUE si no hay ninguno. */
    public static double getNearestPlayerDistance(Location loc) {
        Player nearest = findNearestPlayer(loc, Double.MAX_VALUE);
        return nearest == null ? Double.MAX_VALUE : nearest.getLocation().distance(loc);
    }

    /**
     * Empuja al jugador hacia arriba.
     *
     * Si ya viene subiendo no se le toca: encadenar impulsos manda al jugador a la estratosfera y
     * lo mata de caida, que no es lo que pretende ningun ataque.
     */
    public static void launchPlayer(Player p, double y) {
        if (p.getVelocity().getY() > 0.1) {
            return;
        }
        p.setVelocity(p.getVelocity().setY(y));
    }

    /** La altura del primer bloque solido por debajo, o la propia altura si no hay ninguno. */
    public static double getGroundY(Location loc, double maxScan) {
        for (double dy = 1; dy <= maxScan; dy++) {
            if (loc.clone().subtract(0, dy, 0).getBlock().getType().isSolid()) {
                return loc.getY() - dy + 1;
            }
        }
        return loc.getY();
    }

    /** Si la entidad esta pisando suelo solido. */
    public static boolean isOnGround(BossPuppet stand) {
        return stand.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    /** El jugador valido mas cercano dentro del alcance de agresion, o null. */
    public static Player detectTarget(BossPuppet stand, double aggroRange) {
        return findNearestPlayer(stand.getLocation(), aggroRange);
    }
}
