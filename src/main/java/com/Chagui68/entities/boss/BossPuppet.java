package com.Chagui68.entities.boss;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.EulerAngle;

/**
 * El cuerpo de un jefe, sea un ArmorStand o una criatura normal.
 *
 * POR QUE EXISTE
 *
 * Los 42 ataques del plugin animan al jefe moviendole la cabeza, el cuerpo y los brazos. Son unas
 * 285 llamadas a poses repartidas por todas las clases, y las poses solo existen en un ArmorStand.
 * Eso ataba los ataques a ese tipo de jefe: los de DrakesCraft son criaturas vivas -- Blaze,
 * Enderman, Evoker, Gigante -- y un zombi no tiene pose de brazo izquierdo.
 *
 * La alternativa era partir cada ataque en dos mitades, efecto y coreografia, tocando 42 ficheros.
 * Esto hace lo mismo sin tocar ninguno: el ataque sigue pidiendo la pose, y aqui se decide si hay
 * a quien ponersela.
 *
 * QUE PASA CON UNA CRIATURA
 *
 * Las poses se ignoran en silencio, que es la unica respuesta honesta: no hay forma de doblarle el
 * brazo a un zombi. Todo lo demas -- posicion, vida, atributos, equipo, teletransporte -- funciona
 * igual, y con ello el efecto entero del ataque: el daño, las particulas, los proyectiles y los
 * empujones. Un jefe criatura reutiliza el ataque completo salvo su coreografia.
 *
 * La rotacion de la cabeza sí se aproxima girando la entidad, porque en una criatura eso sí se ve
 * y varios ataques la usan para telegrafiar hacia donde apuntan.
 */
public final class BossPuppet {

    private final LivingEntity entidad;
    private final ArmorStand stand;

    public BossPuppet(LivingEntity entidad) {
        this.entidad = entidad;
        this.stand = entidad instanceof ArmorStand ? (ArmorStand) entidad : null;
    }

    /** La entidad real, para lo que necesite el tipo concreto. */
    public LivingEntity entidad() {
        return entidad;
    }

    /** El ArmorStand, o null si este jefe es una criatura. */
    public ArmorStand armorStand() {
        return stand;
    }

    /** Si este jefe se puede posar. Los ataques no necesitan preguntarlo; es para diagnostico. */
    public boolean tienePoses() {
        return stand != null;
    }

    // --- Lo que funciona en cualquier entidad ---------------------------------------------

    public Location getLocation() {
        return entidad.getLocation();
    }

    public World getWorld() {
        return entidad.getWorld();
    }

    public boolean isValid() {
        return entidad.isValid();
    }

    public boolean isDead() {
        return entidad.isDead();
    }

    public boolean teleport(Location l) {
        return entidad.teleport(l);
    }

    public AttributeInstance getAttribute(Attribute a) {
        return entidad.getAttribute(a);
    }

    public UUID getUniqueId() {
        return entidad.getUniqueId();
    }

    public double getHealth() {
        return entidad.getHealth();
    }

    public void setHealth(double h) {
        entidad.setHealth(h);
    }

    public Set<String> getScoreboardTags() {
        return entidad.getScoreboardTags();
    }

    public EntityEquipment getEquipment() {
        return entidad.getEquipment();
    }

    public void setCustomName(String n) {
        entidad.setCustomName(n);
    }

    public void setCustomNameVisible(boolean v) {
        entidad.setCustomNameVisible(v);
    }

    public void setInvulnerable(boolean v) {
        entidad.setInvulnerable(v);
    }

    public void setGravity(boolean v) {
        entidad.setGravity(v);
    }

    public void setPersistent(boolean v) {
        entidad.setPersistent(v);
    }

    public void setRemoveWhenFarAway(boolean v) {
        entidad.setRemoveWhenFarAway(v);
    }

    public void setMaximumNoDamageTicks(int t) {
        entidad.setMaximumNoDamageTicks(t);
    }

    public void remove() {
        entidad.remove();
    }

    public void addScoreboardTag(String tag) {
        entidad.addScoreboardTag(tag);
    }

    public void setCanPickupItems(boolean v) {
        entidad.setCanPickupItems(v);
    }

    public void setCollidable(boolean v) {
        entidad.setCollidable(v);
    }

    public void damage(double cantidad) {
        entidad.damage(cantidad);
    }

    public void damage(double cantidad, org.bukkit.entity.Entity origen) {
        entidad.damage(cantidad, origen);
    }

    // --- Poses: reales en un ArmorStand, ignoradas en una criatura -------------------------

    /**
     * Las poses actuales.
     *
     * En una criatura devuelven el angulo cero en vez de fallar: varios ataques leen la pose para
     * interpolar desde ella, y devolver cero deja la animacion en su sitio de partida en lugar de
     * romper el ataque entero.
     */
    public EulerAngle getHeadPose() {
        return stand != null ? stand.getHeadPose() : EulerAngle.ZERO;
    }

    public EulerAngle getBodyPose() {
        return stand != null ? stand.getBodyPose() : EulerAngle.ZERO;
    }

    public EulerAngle getLeftArmPose() {
        return stand != null ? stand.getLeftArmPose() : EulerAngle.ZERO;
    }

    public EulerAngle getRightArmPose() {
        return stand != null ? stand.getRightArmPose() : EulerAngle.ZERO;
    }


    public void setHeadPose(EulerAngle a) {
        if (stand != null) {
            stand.setHeadPose(a);
            return;
        }
        // En una criatura no hay pose de cabeza, pero girarla sí se ve y varios ataques la usan
        // para telegrafiar hacia donde apuntan. Se aproxima con la rotacion de la entidad.
        Location l = entidad.getLocation();
        l.setYaw((float) Math.toDegrees(a.getY()));
        l.setPitch((float) Math.toDegrees(a.getX()));
        entidad.setRotation(l.getYaw(), l.getPitch());
    }

    public void setBodyPose(EulerAngle a) {
        if (stand != null) {
            stand.setBodyPose(a);
        }
    }

    public void setLeftArmPose(EulerAngle a) {
        if (stand != null) {
            stand.setLeftArmPose(a);
        }
    }

    public void setRightArmPose(EulerAngle a) {
        if (stand != null) {
            stand.setRightArmPose(a);
        }
    }

    public void setLeftLegPose(EulerAngle a) {
        if (stand != null) {
            stand.setLeftLegPose(a);
        }
    }

    public void setRightLegPose(EulerAngle a) {
        if (stand != null) {
            stand.setRightLegPose(a);
        }
    }

    public void setSmall(boolean v) {
        if (stand != null) {
            stand.setSmall(v);
        }
    }

    public void setArms(boolean v) {
        if (stand != null) {
            stand.setArms(v);
        }
    }

    public void setBasePlate(boolean v) {
        if (stand != null) {
            stand.setBasePlate(v);
        }
    }

    public void setMarker(boolean v) {
        if (stand != null) {
            stand.setMarker(v);
        }
    }
}
