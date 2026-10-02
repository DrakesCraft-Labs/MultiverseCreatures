package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.BossInstance.DefenseState;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.SentinelBody;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Function;

/**
 * Stone Skin: the Sentinel braces and the floor rises to armour it — slabs of stone tear loose from
 * the ground, fly up and lock onto its chest, shoulders and legs. While they hold it takes half
 * damage; when the defence ends they crack and fall away.
 */
public class StoneSkinAttack extends ChoreographedAttack {

    private static final int GATHER = 24;
    private static final int MAX = 400;

    /** Where each plate sits on the body. */
    private static final List<Function<SentinelBody.Anatomy, Vector>> MOUNTS = List.of(
            SentinelBody.Anatomy::chest, SentinelBody.Anatomy::rightShoulder, SentinelBody.Anatomy::leftShoulder,
            b -> b.rightHand().midpoint(b.rightShoulder()), b -> b.leftHand().midpoint(b.leftShoulder()),
            b -> b.rightFoot().midpoint(b.chest()), b -> b.leftFoot().midpoint(b.chest()), SentinelBody.Anatomy::head);

    public StoneSkinAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return instance.activeDefense == DefenseState.NONE && boss.isOnGround(instance.stand);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        BossInstance instance = stage.instance();
        if (instance != null) {
            instance.activeDefense = DefenseState.STONE_SKIN;
            instance.defenseTimer = 0;
        }
        Vector feet = stage.feet();
        Timeline t = new Timeline();
        List<Prop> plates = props(t);
        Vector[] from = new Vector[MOUNTS.size()];

        tweenTo(t, stage, 0, GATHER, Poses.SHIELD_WALL.withRightArm(-30, 30, 20).withLeftArm(-30, -30, -20), Ease.IN_OUT);
        t.at(0, () -> {
            for (int i = 0; i < MOUNTS.size(); i++) {
                from[i] = stage.onGround(feet.clone().add(Shapes.heading(i * 2 * Math.PI / MOUNTS.size()).multiply(9)));
                Prop plate = stage.block(i % 2 == 0 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE_TILES, from[i], 1.8f,
                        new org.joml.Quaternionf().rotationXYZ(i, i * 0.5f, 0));
                plates.add(plate);
                fx.crumble(stage.groundMaterial(from[i]), 12, 1).at(from[i]);
            }
            fx.sound(feet, Sfx.WITHER_BREAK_BLOCK, 2f, 0.6f);
        });
        t.span(1, GATHER, (tick, p) -> {
            SentinelBody.Anatomy body = stage.body();
            for (int i = 0; i < plates.size(); i++) {
                Vector mount = MOUNTS.get(i).apply(body);
                double lift = Math.sin(Math.PI * p) * 4;
                Vector at = from[i].clone().add(mount.clone().subtract(from[i]).multiply(Ease.at(Ease.IN_OUT, p))).add(new Vector(0, lift, 0));
                plates.get(i).moveTo(at, 1);
                if (tick % 2 == 0) fx.crumble(Material.POLISHED_BLACKSTONE, 2, 0.3).at(at);
            }
        });
        t.at(GATHER, () -> {
            fx.flash(stage.body().chest(), Palette.STONE);
            fx.burst(stage.body().chest(), Particle.CLOUD, 30, 0.4);
            fx.sound(feet, Sfx.ANVIL_LAND, 2.5f, 0.5f);
            fx.sound(feet, Sfx.IRON_GOLEM_ATTACK, 2f, 0.5f);
        });
        recover(t, stage, GATHER, GATHER + 14, Poses.GUARD);

        // The plates ride the body until the defence ends.
        int[] end = {MAX};
        t.span(GATHER, MAX, (tick, p) -> {
            boolean active = instance == null ? tick < 200 : instance.activeDefense == DefenseState.STONE_SKIN;
            if (!active) {
                if (end[0] == MAX) end[0] = GATHER + tick;
                return;
            }
            SentinelBody.Anatomy body = stage.body();
            for (int i = 0; i < plates.size(); i++) {
                Vector at = MOUNTS.get(i).apply(body);
                plates.get(i).moveTo(at, 1);
                if (tick % 20 == i) fx.crumble(Material.DEEPSLATE_TILES, 3, 0.4).at(at);
            }
            if (tick % 6 == 0) fx.ring(stage.onGround(stage.feet()).add(new Vector(0, 0.2, 0)), 2.5, 0.6, tick, fx.dust(Palette.STONE, 1.4f));
        });
        t.span(GATHER, MAX, (tick, p) -> {
            if (GATHER + tick != end[0]) return;
            for (Prop plate : plates) {
                fx.crumble(Material.POLISHED_BLACKSTONE, 10, 0.6).at(plate.position());
                stage.debris(plate.position(), new Vector(stage.random().nextGaussian() * 0.15, 0.2, stage.random().nextGaussian() * 0.15),
                        Material.POLISHED_BLACKSTONE, 20);
                plate.remove();
            }
            fx.sound(stage.feet(), Sfx.WITHER_BREAK_BLOCK, 2f, 0.9f);
            t.stop();
        });
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return GATHER + 14;
    }

    @Override
    public String getName() {
        return "stoneskin";
    }
}
