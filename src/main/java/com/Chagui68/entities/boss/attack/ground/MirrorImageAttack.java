package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Mirror Image: three spectral copies of the Sentinel peel away from it, each one a full-size
 * silhouette drawn in violet light. They lunge at the players with their spears and burst when they
 * reach them.
 */
public class MirrorImageAttack extends ChoreographedAttack.Ground {

    private static final int SPLIT = 16;
    private static final int RUSH = 26;
    private static final int IMAGES = 3;
    private static final double BURST = 4;

    public MirrorImageAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.mirror-image-damage", 10.0);
        Vector origin = stage.feet();
        float yaw = stage.yaw();
        List<Victim> victims = stage.victims();
        Vector[] positions = new Vector[IMAGES];
        Vector[] goals = new Vector[IMAGES];
        float[] headings = new float[IMAGES];
        boolean[] burst = new boolean[IMAGES];
        for (int i = 0; i < IMAGES; i++) {
            double angle = Math.toRadians(-yaw) + Math.PI / 2 + (i - 1) * 1.2;
            positions[i] = origin.clone();
            Vector side = Shapes.heading(angle).multiply(7);
            goals[i] = victims.isEmpty() ? origin.clone().add(side.clone().multiply(2.5))
                    : victims.get(i % victims.size()).position();
            Vector to = goals[i].clone().subtract(origin);
            headings[i] = (float) Math.toDegrees(Math.atan2(-to.getX(), to.getZ()));
            positions[i] = origin.clone().add(side);
        }
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, SPLIT, Poses.SPREAD, Ease.IN_OUT);
        t.at(0, () -> fx.sound(origin, Sfx.ILLUSIONER_MIRROR, 2.5f, 0.6f));
        t.span(0, SPLIT, (tick, p) -> {
            for (int i = 0; i < IMAGES; i++) {
                Vector at = origin.clone().add(positions[i].clone().subtract(origin).multiply(Ease.at(Ease.OUT, p)));
                silhouette(stage, at, yaw, Poses.SPREAD, fx.dust(Palette.mix(Palette.SPECTRAL, Palette.AMETHYST, p), 1.6f), 0.7);
                if (tick % 2 == 0) Telegraph.circle(stage, goals[i], BURST, p);
            }
            fx.cloud(Particle.REVERSE_PORTAL, stage.body().chest(), 6, 2, 0.05);
        });

        t.span(SPLIT, SPLIT + RUSH, (tick, p) -> {
            for (int i = 0; i < IMAGES; i++) {
                if (burst[i]) continue;
                Vector to = goals[i].clone().subtract(positions[i]).setY(0);
                double distance = to.length();
                Vector previous = positions[i].clone();
                if (distance > 0.5) positions[i].add(to.normalize().multiply(Math.min(distance, 1.6)));
                positions[i].setY(stage.floorY(positions[i].getX(), positions[i].getY(), positions[i].getZ()));
                silhouette(stage, positions[i], headings[i], Poses.THRUST, fx.dust(Palette.AMETHYST, 1.6f), 0.7);
                fx.line(previous.clone().add(new Vector(0, 3, 0)), positions[i].clone().add(new Vector(0, 3, 0)), 0.6,
                        fx.fade(Palette.SPECTRAL, Palette.VOID_DEEP, 1.4f));
                boolean arrived = distance < 2 || tick == RUSH - 1;
                Area blast = Area.cylinder(positions[i], BURST, 2, 8);
                if (arrived || !stage.victimsIn(Area.cylinder(positions[i], 2, 2, 8)).isEmpty()) {
                    burst[i] = true;
                    Vector core = positions[i].clone().add(new Vector(0, 3, 0));
                    fx.flash(core, Palette.AMETHYST);
                    fx.draw(Shapes.sphere(core, 3, 50), fx.dust(Palette.SPECTRAL, 2.0f));
                    fx.burst(core, Particle.END_ROD, 30, 0.6);
                    fx.sound(core, Sfx.GLASS_BREAK, 2f, 0.6f);
                    fx.sound(core, Sfx.EXPLODE, 1.5f, 1.1f);
                    Vector at = positions[i].clone();
                    stage.hit(blast, damage, victim -> {
                        victim.effect(Affliction.BLINDNESS, 30, 0);
                        Vector away = victim.position().subtract(at).setY(0);
                        if (away.lengthSquared() > 1e-6) away.normalize();
                        victim.push(away.multiply(0.6).setY(0.5));
                    });
                }
            }
            if (tick % 6 == 0) fx.sound(stage.feet(), Sfx.PHANTOM_SWOOP, 1.5f, 0.7f);
        });
        recover(t, stage, SPLIT + 4, SPLIT + 20, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "mirrorimage";
    }
}
