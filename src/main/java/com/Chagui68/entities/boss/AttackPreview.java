package com.Chagui68.entities.boss;

import org.bukkit.entity.Entity;

/**
 * Attack preview: a boss attack runs on a pose dummy so its animation can be watched on a test
 * server, without a fight and without damage.
 *
 * <p>WHY IT EXISTS
 *
 * <p>The Sentinel's attacks could only be seen by standing next to the real boss and letting it hit
 * you, which is a poor way to review an animation and a worse one on a test server. The dummy
 * already existed to pose the model; the preview turns it into the actor, so the same attack object
 * the boss runs plays out in front of an admin.
 *
 * <p>HOW IT STAYS HARMLESS
 *
 * <p>Attacks are reused whole — choreography, particles, sounds, telegraphs — because a preview of
 * something else would be useless. Only the damage is refused, and it is refused at the single door
 * every attack damages through, {@code MscEntityUtils.damageBy}, which asks this class whether the
 * attacker is performing.
 *
 * <p>What that leaves: potion effects, knockback and the fire the two burning attacks spread still
 * play, because they are part of the animation. Nobody dies of them, which is the point of a
 * preview, but standing in the middle of a meteor shower is still going to feel like a meteor
 * shower.
 */
public final class AttackPreview {

    /**
     * Tag carried by an entity that acts out attacks for show.
     *
     * <p>The dummy is that entity: it is an ArmorStand with no AI of its own, and nothing in the
     * plugin ever expects it to deal damage.
     */
    public static final String TAG = "MSC_Dummy";

    private AttackPreview() {
    }

    /** Whether this attacker is a preview dummy, whose hits are part of the show rather than a fight. */
    public static boolean isActor(Entity attacker) {
        return attacker != null && attacker.getScoreboardTags().contains(TAG);
    }
}
