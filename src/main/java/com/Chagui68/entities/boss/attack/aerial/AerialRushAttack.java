package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Aerial Rush: the Sentinel draws back in the air, spear levelled, then dives along the marked lane
 * through its target and sweeps back up to its height, a wake of wind behind it.
 */
public class AerialRushAttack extends ChoreographedAttack.Aerial {

    private static final int COIL = 16;
    private static final int DIVE = 10;
    private static final int CLIMB = 14;

    public AerialRushAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.9);
        Vector start = stage.feet();
        double height = start.getY() - stage.floorY(start.getX(), start.getY(), start.getZ());
        Vector aim = stage.onGround(target.position());
        Vector dir = Shapes.flat(aim.clone().subtract(start));
        Vector through = stage.onGround(aim.clone().add(dir.clone().multiply(8))).add(new Vector(0, 0.5, 0));
        Vector rest = through.clone().add(dir.clone().multiply(10)).add(new Vector(0, height, 0));
        Set<UUID> struck = new HashSet<>();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, COIL, Poses.THRUST_COIL.withRightLeg(25, 0, 0).withLeftLeg(35, 0, 0), Ease.IN_BACK);
        t.span(0, COIL, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, stage.onGround(start), through, 6, p);
            stage.moveTo(start.clone().subtract(dir.clone().multiply(2 * Ease.at(Ease.OUT, p))).add(new Vector(0, p, 0)));
            fx.cloud(Particle.CLOUD, stage.body().chest(), 2, 1.5, 0.02);
        });
        t.at(0, () -> fx.sound(start, Sfx.PHANTOM_SWOOP, 2.5f, 0.5f));

        tween(t, stage, COIL, COIL + 3, Poses.THRUST_COIL, Poses.DIVE.withRightArm(-20, 0, 0), Ease.OUT);
        Vector[] from = new Vector[1];
        t.span(COIL, COIL + DIVE, (tick, p) -> {
            if (from[0] == null) from[0] = stage.feet();
            Vector before = stage.feet();
            Vector at = from[0].clone().add(through.clone().subtract(from[0]).multiply(Ease.at(Ease.IN, p)));
            stage.moveTo(at);
            fx.line(before.clone().add(new Vector(0, 4, 0)), at.clone().add(new Vector(0, 4, 0)), 0.5,
                    fx.dust(Palette.ICE, 1.8f, 1.5, 2).and(fx.particle(Particle.GUST).sometimes(0.15)));
            fx.cloud(Particle.SWEEP_ATTACK, stage.body().spearTip(), 2, 0.5, 0);
            for (Victim victim : stage.victimsIn(Area.segment(before, at.clone().add(new Vector(0, 4, 0)), 4.5))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                victim.fling(dir.clone().multiply(1.6).setY(0.9));
                fx.impact(victim.chest(), Palette.ICE, 2);
            }
            if (tick == DIVE - 1) {
                fx.flatBurst(stage.onGround(at), Particle.CLOUD, 40, 0.8);
                fx.sound(at, Sfx.WIND_CHARGE_BURST, 3f, 0.6f);
            }
        });
        t.at(COIL + 2, () -> fx.sound(stage.feet(), Sfx.TRIDENT_RIPTIDE, 3f, 0.6f));

        tweenTo(t, stage, COIL + DIVE, COIL + DIVE + CLIMB, Poses.HOVER, Ease.IN_OUT);
        t.span(COIL + DIVE, COIL + DIVE + CLIMB, (tick, p) -> {
            stage.moveTo(through.clone().add(rest.clone().subtract(through).multiply(Ease.at(Ease.OUT, p))));
            fx.cloud(Particle.CLOUD, stage.feet(), 3, 1, 0.05);
        });
        t.at(COIL + DIVE + CLIMB, () -> stage.face(target.position()));
        return t;
    }

    @Override
    public String getName() {
        return "aerialrush";
    }
}
