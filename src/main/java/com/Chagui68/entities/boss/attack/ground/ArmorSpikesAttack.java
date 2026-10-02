package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
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

import java.util.List;

/**
 * Armor Spikes: the Sentinel curls in, its armour glowing hotter, then obsidian spikes burst out of
 * its body in every direction and slam into whoever stood close.
 */
public class ArmorSpikesAttack extends ChoreographedAttack.Ground {

    private static final int CURL = 18;
    private static final int SPIKES = 18;
    private static final double REACH = 9;
    private static final Pose CURLED = Poses.CAST_GROUND.withRightArm(-30, -40, 10).withLeftArm(-30, 40, -10);

    public ArmorSpikesAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.9);
        Vector feet = stage.feet();
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, CURL, CURLED, Ease.IN);
        t.span(0, CURL, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, feet, REACH, p);
            Vector chest = stage.body().chest();
            fx.draw(Shapes.sphere(chest, 2.5 + (1 - p) * 3, 30), fx.dust(Palette.mix(Palette.VOID, Palette.EMBER, p), 1.6f));
            if (tick % 4 == 0) fx.gather(chest, 6, 6, Palette.EMBER, 8);
        });
        t.at(0, () -> fx.sound(feet, Sfx.WARDEN_HEARTBEAT, 2.5f, 0.6f));
        t.at(10, () -> fx.sound(feet, Sfx.WARDEN_HEARTBEAT, 2.5f, 0.8f));

        tween(t, stage, CURL, CURL + 3, CURLED, Poses.SPREAD, Ease.OUT_BACK);
        t.at(CURL, () -> {
            Vector chest = stage.body().chest();
            for (int i = 0; i < SPIKES; i++) {
                double angle = 2 * Math.PI * i / SPIKES;
                double rise = (i % 3 - 1) * 0.35;
                Vector dir = Shapes.heading(angle).setY(rise).normalize();
                Vector base = chest.clone().add(dir.clone().multiply(1.5)).subtract(new Vector(0, 1.5 + (i % 2) * 2, 0));
                Prop spike = stage.block(i % 4 == 0 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN, base, 0.7f, Prop.pointing(dir));
                spike.resize(0.7f, 0.1f, Prop.pointing(dir), 0);
                props.add(spike);
            }
            fx.flash(chest, Palette.EMBER);
            fx.burst(chest, Particle.LAVA, 30, 0.8);
            fx.sound(feet, Sfx.WITHER_BREAK_BLOCK, 3f, 0.5f);
            fx.sound(feet, Sfx.EXPLODE, 2f, 0.8f);
        });
        t.at(CURL + 1, () -> {
            for (int i = 0; i < props.size(); i++) {
                Prop spike = props.get(i);
                double angle = 2 * Math.PI * i / SPIKES;
                double rise = (i % 3 - 1) * 0.35;
                Vector dir = Shapes.heading(angle).setY(rise).normalize();
                spike.resize(0.7f, (float) (REACH - 2 + (i % 2) * 2), Prop.pointing(dir), 3);
            }
        });
        t.at(CURL + 3, () -> stage.hit(Area.cylinder(feet, REACH, 1, 10), damage, victim -> {
            Vector away = victim.position().subtract(feet).setY(0);
            if (away.lengthSquared() > 1e-6) away.normalize();
            victim.fling(away.multiply(1.4).setY(0.7));
            fx.impact(victim.chest(), Palette.EMBER, 1.5);
        }));
        t.span(CURL + 3, CURL + 30, (tick, p) -> {
            if (tick % 3 == 0) fx.ring(feet.clone().add(new Vector(0, 0.3, 0)), REACH, 1.2, tick,
                    fx.particle(Particle.SMALL_FLAME).sometimes(0.4));
        });
        t.at(CURL + 30, () -> {
            for (Prop spike : props) spike.reshape(0.1f, new org.joml.Quaternionf(), 8);
            fx.sound(feet, Sfx.AMETHYST_BREAK, 2f, 0.5f);
        });
        recover(t, stage, CURL + 22, CURL + 40, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "armorspikes";
    }
}
