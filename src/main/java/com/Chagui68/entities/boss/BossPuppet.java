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
 * The body of a boss, whether an ArmorStand or a normal creature.
 *
 * WHY IT EXISTS
 *
 * The 42 plugin attacks animate the boss by moving head, body and arms. There are ~285
 * pose calls spread across all classes, and poses only exist on an ArmorStand.
 * That tied attacks to that boss type: DrakesCraft bosses are living creatures — Blaze,
 * Enderman, Evoker, Giant — and a zombie has no left-arm pose.
 *
 * The alternative was splitting each attack into two halves, effect and choreography,
 * touching 42 files. This achieves the same without touching any: the attack still
 * asks for the pose, and here we decide whether there is someone to apply it to.
 *
 * WHAT HAPPENS WITH A CREATURE
 *
 * Poses are silently ignored, which is the only honest response: there is no way to
 * bend a zombie's arm. Everything else — position, health, attributes, equipment,
 * teleport — works the same, and so does the whole attack effect: damage, particles,
 * projectiles and knockback. A creature boss reuses the full attack except choreography.
 *
 * Head rotation is approximated by rotating the entity, because on a creature that is
 * visible and several attacks use it to telegraph where they aim.
 */
public final class BossPuppet {

    private final LivingEntity entidad;
    private final ArmorStand stand;

    public BossPuppet(LivingEntity entidad) {
        this.entidad = entidad;
        this.stand = entidad instanceof ArmorStand ? (ArmorStand) entidad : null;
    }

    /** The real entity, for code that needs the concrete type. */
    public LivingEntity entidad() {
        return entidad;
    }

    /** The ArmorStand, or null if this boss is a creature. */
    public ArmorStand armorStand() {
        return stand;
    }

    /** Whether this boss supports poses. Attacks don't need to check; for diagnostics. */
    public boolean tienePoses() {
        return stand != null;
    }

    // --- Works on any entity --------------------------------------------------------

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

    // --- Poses: real on ArmorStand, ignored on creature ----------------------------

    /**
     * Current poses.
     *
     * On a creature they return zero angle instead of failing: several attacks read the pose
     * to interpolate from it, and returning zero keeps the animation at its starting point
     * instead of breaking the whole attack.
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
        // On a creature there is no head pose, but rotating it is visible and several attacks
        // use it to telegraph aim. Approximated via entity rotation.
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
