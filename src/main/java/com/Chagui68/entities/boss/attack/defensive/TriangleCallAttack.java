package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * Triangle Call: the Sentinel plants its spear and raises two standing seals of fire, one on each
 * side. Columns of light come down through them and its reinforcements step out: war beasts ridden
 * by dark priests on the ground, ghasts and phantom-riding snipers in the air. One extra set for
 * every three players.
 */
public class TriangleCallAttack extends ChoreographedAttack {

    private static final int RAISE = 24;
    private static final int SEAL = 80;
    private static final double SIDE = 7;
    private static final double SIZE = 4.5;

    public TriangleCallAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return !instance.triangleCallActive;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        BossInstance instance = stage.instance();
        boolean airborne = instance != null && instance.isFlying;
        if (instance != null) instance.triangleCallActive = true;
        int sets = 1 + stage.victims().size() / 3;
        Vector feet = stage.feet();
        Vector right = stage.body().right();
        Vector forward = stage.forward();
        Vector[] seals = {
                feet.clone().add(right.clone().multiply(SIDE)).add(new Vector(0, SIZE + 1, 0)),
                feet.clone().subtract(right.clone().multiply(SIDE)).add(new Vector(0, SIZE + 1, 0))};
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, RAISE, Poses.SPEAR_RAISED.withLeftArm(-90, 0, -80), Ease.OUT);
        t.at(0, () -> fx.sound(feet, Sfx.EVOKER_PREPARE_SUMMON, 3f, 0.5f));
        t.span(0, RAISE + SEAL, (tick, p) -> {
            double grown = Math.min(1, tick / (double) RAISE);
            for (Vector seal : seals) {
                // A standing triangle facing forward, turning slowly, inside a ring.
                fx.draw(Shapes.star(seal, SIZE * grown, 3, 1, Math.PI / 2 + tick * 0.03, 0.35, right, Shapes.UP),
                        fx.dust(Palette.EMBER, 1.6f));
                fx.draw(Shapes.circle(seal, SIZE * 1.1 * grown, 30, right, Shapes.UP, -tick * 0.02), fx.dust(Palette.MOLTEN, 1.2f));
                fx.draw(Shapes.star(seal, SIZE * 0.5 * grown, 3, 1, -Math.PI / 2 - tick * 0.05, 0.4, right, Shapes.UP),
                        fx.dust(Palette.GOLD, 1.0f));
                if (tick % 3 == 0) fx.cloud(Particle.FLAME, seal, 3, SIZE * 0.4, 0.02);
            }
        });
        t.at(RAISE, () -> {
            for (Vector seal : seals) {
                Vector base = stage.onGround(seal);
                fx.line(base, base.clone().add(new Vector(0, 30, 0)), 0.5, fx.dust(Palette.HOLY, 2.2f));
                fx.flash(seal, Palette.EMBER);
                fx.flatBurst(base, Particle.FLAME, 30, 0.5);
            }
            fx.sound(feet, Sfx.WITHER_SPAWN, 1.5f, 1.2f);
            stage.onServer(world -> {
                for (int s = 0; s < 2; s++) {
                    Vector at = s == 0 ? seals[0] : seals[1];
                    for (int i = 0; i < sets; i++) {
                        Vector offset = forward.clone().multiply(i * 3.0);
                        if (airborne) summonAir(world, at.clone().add(offset).add(new Vector(0, i * 3, 0)));
                        else summonGround(world, stage.onGround(at.clone().add(offset)));
                    }
                }
            });
        });
        recover(t, stage, RAISE + 6, RAISE + 22, airborne ? Poses.HOVER : Poses.GUARD);
        t.onFinish(() -> {
            if (instance != null) instance.triangleCallActive = false;
        });
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return RAISE + 22;
    }

    // ------------------------------------------------------------------ reinforcements

    private void summonGround(World world, Vector at) {
        Location location = at.toLocation(world);
        Ravager ravager = world.spawn(location, Ravager.class, r -> prepare(r, MscText.title(DARK_RED, "War Beast"), 300.0));
        AttributeInstance damage = ravager.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(24.0);
        target(ravager, location);
        Evoker evoker = world.spawn(location, Evoker.class, e -> prepare(e, MscText.title(LIGHT_PURPLE, "Dark Priest"), 40.0));
        evoker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
        ravager.addPassenger(evoker);
    }

    private void summonAir(World world, Vector at) {
        Location location = at.toLocation(world);
        Ghast ghast = world.spawn(location, Ghast.class, g -> prepare(g, MscText.title(RED, "Infernal Ghast"), 20.0));
        target(ghast, location);
        Location higher = location.clone().add(0, 4, 0);
        Phantom phantom = world.spawn(higher, Phantom.class, p -> prepare(p, MscText.title(DARK_PURPLE, "Night Stalker"), 30.0));
        target(phantom, higher);
        WitherSkeleton sniper = world.spawn(higher, WitherSkeleton.class, s -> prepare(s, MscText.title(DARK_GREEN, "Sniper Skeleton"), 40.0));
        AttributeInstance follow = sniper.getAttribute(Attribute.FOLLOW_RANGE);
        if (follow != null) follow.setBaseValue(40.0);
        sniper.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment equipment = sniper.getEquipment();
        if (equipment != null) {
            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta meta = bow.getItemMeta();
            if (meta != null) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.POWER, 5, true);
                meta.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY, 1, true);
                meta.itemName(Component.text("Sniper Bow"));
                bow.setItemMeta(meta);
            }
            equipment.setItemInMainHand(bow);
            equipment.setItemInMainHandDropChance(0);
        }
        phantom.addPassenger(sniper);
    }

    private static void prepare(LivingEntity entity, Component name, double health) {
        entity.customName(name);
        entity.setCustomNameVisible(true);
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.addScoreboardTag("MSC_ArmorBossSummoned");
        AttributeInstance max = entity.getAttribute(Attribute.MAX_HEALTH);
        if (max != null) max.setBaseValue(health);
        entity.setHealth(health);
    }

    private void target(org.bukkit.entity.Mob mob, Location near) {
        Player player = boss.findNearestPlayer(near, 100);
        if (player != null) mob.setTarget(player);
    }

    @Override
    public String getName() {
        return "trianglecall";
    }
}
