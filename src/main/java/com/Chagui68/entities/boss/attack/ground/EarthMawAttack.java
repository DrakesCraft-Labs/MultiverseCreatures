package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Earth Maw: the ground around the target splits into two rows of teeth that rise and snap shut
 * like a jaw. Getting out of the circle before it closes is the counter.
 */
public class EarthMawAttack extends ChoreographedAttack.Ground {

    private static final int WARN = 22;
    private static final int TEETH = 7;
    private static final double RADIUS = 6;

    public EarthMawAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.earth-maw-damage", 16.0);
        Vector center = stage.onGround(aimAt(stage, stage.target(), 14));
        Vector across = Shapes.flat(center.clone().subtract(stage.feet()));
        Vector along = new Vector(-across.getZ(), 0, across.getX());
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        List<Prop> left = new ArrayList<>();
        List<Prop> right = new ArrayList<>();

        tweenTo(t, stage, 0, WARN, Poses.CAST_GROUND, Ease.IN_OUT);
        t.span(0, WARN, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, center, RADIUS, p);
            fx.line(stage.body().rightHand(), center.clone().add(new Vector(0, 0.5, 0)), 1.2,
                    fx.dust(Palette.MOLTEN, 1.0f).sometimes(0.5));
            fx.crumble(stage.groundMaterial(center), 4, RADIUS * 0.6).at(center);
        });
        t.at(0, () -> fx.sound(center, Sfx.RAVAGER_ROAR, 2f, 0.5f));

        // Two rows of teeth, one on each side of the circle.
        t.at(WARN, () -> {
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < TEETH; i++) {
                    double offset = (i - (TEETH - 1) / 2.0) * 1.7;
                    Vector base = stage.onGround(center.clone().add(along.clone().multiply(offset)).add(across.clone().multiply(side * RADIUS)));
                    Vector lean = across.clone().multiply(-side * 0.25).add(new Vector(0, 1, 0)).normalize();
                    Prop tooth = stage.block(i % 2 == 0 ? Material.BONE_BLOCK : Material.BASALT, base.subtract(new Vector(0, 0.3, 0)), 1.0f, Prop.pointing(lean));
                    tooth.resize(1.0f, 0.1f, Prop.pointing(lean), 0);
                    props.add(tooth);
                    (side < 0 ? left : right).add(tooth);
                }
            }
            fx.sound(center, Sfx.WITHER_BREAK_BLOCK, 2f, 0.6f);
        });
        t.at(WARN + 1, () -> {
            for (int i = 0; i < props.size(); i++) {
                int side = i < TEETH ? -1 : 1;
                Vector lean = across.clone().multiply(-side * 0.25).add(new Vector(0, 1, 0)).normalize();
                props.get(i).resize(1.0f, (float) (3.5 + (i % 3) * 0.8), Prop.pointing(lean), 5);
            }
        });
        // The jaw snaps: both rows tip inwards over the circle.
        int snap = WARN + 8;
        t.at(snap, () -> {
            for (int i = 0; i < props.size(); i++) {
                int side = i < TEETH ? -1 : 1;
                Vector bite = across.clone().multiply(-side).add(new Vector(0, 0.35, 0)).normalize();
                props.get(i).resize(1.0f, (float) (RADIUS + 0.5), Prop.pointing(bite), 3);
            }
            fx.sound(center, Sfx.EVOKER_FANGS, 3f, 0.5f);
        });
        t.at(snap + 3, () -> {
            fx.draw(Shapes.ring(center, RADIUS * 0.5, 0.6, 0), fx.crumble(Material.BONE_BLOCK, 4, 0.4));
            fx.burst(center.clone().add(new Vector(0, 1.5, 0)), Particle.CRIT, 30, 0.6);
            fx.sound(center, Sfx.BONE_BREAK, 2.5f, 0.6f);
            stage.hit(Area.cylinder(center, RADIUS, 1, 4), damage, victim -> {
                victim.effect(Affliction.SLOWNESS, 80, 3);
                victim.push(new Vector(0, 0.4, 0));
            });
        });
        t.at(snap + 30, () -> {
            for (Prop tooth : props) tooth.reshape(0.1f, new org.joml.Quaternionf(), 10);
            fx.draw(Shapes.ring(center, RADIUS, 0.8, 0), fx.crumble(Material.BASALT, 3, 0.4));
        });
        recover(t, stage, WARN + 4, WARN + 20, Poses.GUARD);
        t.hold(snap + 42);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return WARN + 20;
    }

    @Override
    public String getName() {
        return "earthmaw";
    }
}
