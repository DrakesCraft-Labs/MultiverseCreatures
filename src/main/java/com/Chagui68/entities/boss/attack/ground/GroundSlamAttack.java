package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.BossInstance.ShieldState;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Aegis Judgment (the Sentinel's signature, run on its own clock rather than drawn from a pool): it
 * hurls its shield into the sky, where the shield turns and casts a burning pentagram on the floor
 * under every player. When the spear comes down, columns of light fall from the shield into every
 * pentagram, and the shield drops back into the Sentinel's hand.
 */
public class GroundSlamAttack extends ChoreographedAttack.Ground {

    private static final int THROW = 18;
    private static final int MARK = 40;
    private static final int RETURN = 20;
    private static final double HEIGHT = 22;
    private static final double RADIUS = 4;

    public GroundSlamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return super.ready(instance) && instance.shieldState == ShieldState.NORMAL;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 1.0);
        BossInstance instance = stage.instance();
        ItemStack[] saved = new ItemStack[1];
        if (instance != null) {
            instance.shieldState = ShieldState.PLANTED;
            EntityEquipment equipment = instance.stand.getEquipment();
            ItemStack held = equipment == null ? null : equipment.getItemInOffHand();
            if (held != null && held.getType() == Material.SHIELD) {
                saved[0] = held.clone();
                equipment.setItemInOffHand(null);
            }
        }
        Vector sky = stage.feet().add(new Vector(0, HEIGHT, 0));
        List<Vector> marks = new ArrayList<>();
        for (Victim victim : stage.victims()) marks.add(stage.onGround(victim.position()));
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] shield = new Prop[1];
        double[] spin = {0};

        tweenTo(t, stage, 0, THROW, Poses.SPEAR_RAISED.withLeftArm(-175, 0, -20), Ease.OUT_BACK);
        t.at(3, () -> {
            shield[0] = stage.item(Material.SHIELD, stage.body().leftHand(), 1f, new org.joml.Quaternionf());
            shield[0].glow(Palette.GOLD);
            props.add(shield[0]);
            fx.sound(stage.feet(), Sfx.ZOMBIE_IRON_DOOR, 2.5f, 0.6f);
        });
        t.at(4, () -> {
            shield[0].moveTo(sky, THROW - 4);
            fx.trail(stage.body().leftHand(), sky, Palette.GOLD, THROW - 4);
        });
        t.span(4, THROW + MARK + RETURN, (tick, p) -> {
            if (shield[0] == null) return;
            spin[0] += 0.25;
            float size = (float) Math.min(9, 1 + tick * 0.5);
            shield[0].reshape(size, new org.joml.Quaternionf().rotationX((float) Math.PI / 2).rotateZ((float) spin[0]), 1);
        });

        t.span(THROW, THROW + MARK, (tick, p) -> {
            fx.ring(sky, 6, 0.8, spin[0] * 0.2, fx.dust(Palette.GOLD, 1.6f));
            fx.draw(Shapes.star(sky.clone().subtract(new Vector(0, 0.5, 0)), 5, 5, 2, -spin[0] * 0.1, 0.6, Shapes.FLAT_U, Shapes.FLAT_V),
                    fx.dust(Palette.MOLTEN, 1.2f));
            if (tick % 2 != 0) return;
            for (Vector mark : marks) {
                Telegraph.circle(stage, mark, RADIUS, p);
                fx.draw(Shapes.star(mark.clone().add(new Vector(0, 0.2, 0)), RADIUS * 0.9, 5, 2, tick * 0.05, 0.4, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.dust(Palette.EMBER, 1.1f));
                fx.line(sky, mark, 2.0, fx.dust(Palette.GOLD, 0.8f).sometimes(0.5));
            }
        });
        t.at(THROW, () -> fx.sound(sky, Sfx.BELL_RESONATE, 2.5f, 0.6f));
        tween(t, stage, THROW + MARK - 10, THROW + MARK - 2, Poses.SPEAR_RAISED, Poses.SPEAR_OVERHEAD, Ease.IN);
        tween(t, stage, THROW + MARK - 2, THROW + MARK + 2, Poses.SPEAR_OVERHEAD, Poses.SPEAR_SLAM, Ease.OUT_BACK);

        int judgment = THROW + MARK;
        t.at(judgment, () -> {
            if (instance != null) instance.shieldState = ShieldState.SLAM_DONE;
            Vector impact = spearGround(stage);
            fx.impact(impact.clone().add(new Vector(0, 1, 0)), Palette.GOLD, 3);
            fx.sound(impact, Sfx.MACE_SMASH_GROUND, 3f, 0.6f);
            fx.sound(sky, Sfx.TRIDENT_THUNDER, 3f, 0.6f);
            for (Vector mark : marks) {
                fx.beam(sky, mark, Palette.HOLY, Palette.GOLD, 2.5);
                for (int i = 0; i < 6; i++) {
                    Vector edge = mark.clone().add(Shapes.heading(i * Math.PI / 3).multiply(RADIUS * 0.7));
                    fx.line(edge, edge.clone().add(new Vector(0, 12, 0)), 0.6, fx.dust(Palette.HOLY, 2.0f));
                }
                fx.impact(mark.clone().add(new Vector(0, 0.8, 0)), Palette.EMBER, 2.5);
                fx.flatBurst(mark, Particle.FLAME, 20, 0.4);
                stage.hit(Area.cylinder(mark, RADIUS, 1, 6), damage, victim -> victim.push(new Vector(0, 0.6, 0)));
            }
        });
        t.at(judgment + 4, () -> {
            if (shield[0] != null) shield[0].moveTo(stage.body().leftHand(), RETURN - 4);
        });
        t.at(judgment + RETURN, () -> {
            if (shield[0] != null) shield[0].remove();
            fx.sound(stage.feet(), Sfx.SHIELD_BLOCK, 2f, 1.4f);
            fx.burst(stage.body().leftHand(), Particle.END_ROD, 20, 0.3);
        });
        t.onFinish(() -> {
            if (instance == null) return;
            instance.shieldState = ShieldState.NORMAL;
            EntityEquipment equipment = instance.stand.getEquipment();
            ItemStack shieldItem = saved[0] != null ? saved[0] : new ItemStack(Material.SHIELD);
            if (equipment != null && equipment.getItemInOffHand().getType() == Material.AIR && !instance.shieldSealActive) {
                equipment.setItemInOffHand(shieldItem);
            }
        });
        recover(t, stage, judgment + 8, judgment + RETURN + 4, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "groundslam";
    }
}
