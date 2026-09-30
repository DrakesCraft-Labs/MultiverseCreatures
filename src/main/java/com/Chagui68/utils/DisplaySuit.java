package com.Chagui68.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Transformation;

import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

/**
 * The body every dressed boss wears: the armoured suit of player-head pieces that Jack Star, NIX and
 * Kinger walk around in.
 *
 * WHY IT EXISTS
 *
 * The three of them had grown the same code three times — build the head from the base64 skin, apply
 * the rest transform, zero the interpolation and the display box, tag the piece with its own name and
 * with its owner's id, hunt for an orphaned piece before spawning a new one, and delete the suit when
 * the boss dies — and they had drifted apart: one searched a narrower box, one tagged the owner in a
 * different place, and only two of them had learned that a display with a real bounding box swallows
 * the player's swings. What each boss actually needs to decide is only which piece belongs to which
 * limb and how that limb moves; everything else lives here now, so a fourth dressed boss gets the
 * whole audit for free.
 *
 * The three tags are the whole ownership model:
 *
 * <ul>
 *   <li>{@code suit} — marks the piece as part of a suit at all, and the damage listener uses it to
 *       route a hit on a piece to its stand;</li>
 *   <li>{@code piece} — which piece of the suit it is, so an adoption never mixes two of them;</li>
 *   <li>{@code owner} — which boss it belongs to, so a reload continues the body it already has
 *       instead of building a second, overlapping one.</li>
 * </ul>
 */
public final class DisplaySuit {

    /** The three tags that identify one piece of one boss's suit in the world. */
    public record SuitTags(String suit, String piece, String owner) {

        public boolean matches(Entity entity) {
            return entity.getScoreboardTags().contains(suit)
                    && entity.getScoreboardTags().contains(piece)
                    && entity.getScoreboardTags().contains(owner);
        }
    }

    private DisplaySuit() {
    }

    /** The player head a piece is drawn as, decoded from the exported skin texture. */
    public static ItemStack head(String profileName, String base64Texture, String owner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            try {
                String json = new String(Base64.getDecoder().decode(base64Texture));
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                String url = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), profileName);
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(url));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (Exception e) {
                MscLog.warn("could not build the " + owner + " head of " + profileName, e);
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    /**
     * Applies everything a piece needs, so a freshly spawned and an adopted piece end up identical.
     *
     * <p>Every interpolation value is zero because the pieces are placed on the stand's exact
     * position every tick: letting the client smooth a teleport the server already snapped is what
     * makes a suit trail behind the invisible hitbox it is supposed to cover.
     *
     * <p>Both display dimensions are zero because a display's width and height double as its bounding
     * box: a piece with a real box is picked by the client instead of the armour stand, and a display
     * takes no damage, so a swing aimed at the body would be lost and the boss would look unkillable.
     */
    public static void configure(ItemDisplay display, ItemStack head, Transformation restPose, SuitTags tags) {
        display.setItemStack(head);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        display.setBillboard(Display.Billboard.FIXED);
        display.setTransformation(restPose);
        display.setTeleportDuration(0);
        display.setInterpolationDuration(0);
        display.setInterpolationDelay(0);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setDisplayWidth(0.0f);
        display.setDisplayHeight(0.0f);
        display.setInvulnerable(false);
        display.setGravity(false);
        display.setSilent(true);
        display.setPersistent(true);
        display.addScoreboardTag(tags.suit());
        display.addScoreboardTag(tags.piece());
        display.addScoreboardTag(tags.owner());
    }

    /** Spawns one piece at the suit's root and configures it in a single place. */
    public static ItemDisplay spawn(Location root, ItemStack head, Transformation restPose, SuitTags tags) {
        ItemDisplay display = (ItemDisplay) root.getWorld().spawnEntity(root, org.bukkit.entity.EntityType.ITEM_DISPLAY);
        configure(display, head, restPose, tags);
        return display;
    }

    /**
     * Looks for this piece of this boss near the stand, without loading anything new.
     *
     * <p>The search box is deliberately generous in height and tight around the body: a suit piece
     * sits within a couple of blocks of the stand even at the top of the model, and a wider sweep
     * would risk adopting a neighbour's piece.
     */
    public static ItemDisplay find(World world, Location center, SuitTags tags) {
        if (world == null || center == null) return null;
        for (Entity entity : world.getNearbyEntities(center, 6.0, 8.0, 6.0)) {
            if (entity instanceof ItemDisplay display && tags.matches(display)) {
                return display;
            }
        }
        return null;
    }

    /** Removes every piece of a suit whose boss is gone. */
    public static void remove(World world, Collection<UUID> pieces) {
        for (UUID id : pieces) {
            Entity entity = (world != null) ? world.getEntity(id) : Bukkit.getEntity(id);
            if (entity != null) entity.remove();
        }
    }
}
