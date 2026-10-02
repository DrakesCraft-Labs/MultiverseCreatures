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
 * Wind Cutter: two wide sweeps of the spear cut blades of wind out of the air; they drop to the
 * floor and race across it in a fan, low enough to jump, each one a wall of grey wind.
 */
public class WindCutterAttack extends ChoreographedAttack.Aerial {

    private static final int WIND = 12;
    private static final int BLADES = 5;
    private static final int TRAVEL = 26;
    private static final double RANGE = 34;
    private static final double SPREAD = Math.toRadians(60);

    public WindCutterAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.5);
        Vector origin = stage.onGround(stage.feet());
        Vector forward = stage.forward();
        double base = Math.atan2(forward.getZ(), forward.getX());
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WIND, Poses.SWING_BACK, Ease.IN_BACK);
        t.span(0, WIND, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.cone(stage, origin, forward, SPREAD, RANGE, p);
            fx.cloud(Particle.CLOUD, stage.body().spearTip(), 2, 0.4, 0.02);
        });
        t.at(0, () -> fx.sound(origin, Sfx.BREEZE_SHOOT, 2.5f, 0.6f));
        tween(t, stage, WIND, WIND + 5, Poses.SWING_BACK, Poses.SWING_THROUGH, Ease.OUT);
        tween(t, stage, WIND + 7, WIND + 12, Poses.SWING_THROUGH, Poses.SWING_BACK, Ease.OUT);

        for (int i = 0; i < BLADES; i++) {
            double angle = base - SPREAD + 2 * SPREAD * i / (BLADES - 1);
            Vector dir = Shapes.heading(angle);
            Vector side = new Vector(-dir.getZ(), 0, dir.getX());
            int start = WIND + 3 + (i % 2) * 7;
            Set<UUID> struck = new HashSet<>();
            t.at(start, () -> fx.sound(origin, Sfx.WIND_CHARGE_BURST, 1.5f, 0.8f + (float) Math.random() * 0.4f));
            t.span(start, start + TRAVEL, (tick, p) -> {
                Vector center = stage.onGround(origin.clone().add(dir.clone().multiply(3 + (RANGE - 3) * p)));
                double half = 2.5 + p * 2;
                for (double s = -half; s <= half; s += 0.5) {
                    double curve = (1 - (s / half) * (s / half)) * 1.2;
                    Vector at = center.clone().add(side.clone().multiply(s)).add(dir.clone().multiply(curve));
                    fx.dust(Palette.ICE, 1.4f).at(at.clone().add(new Vector(0, 0.4, 0)));
                    fx.dust(Palette.STONE, 1.8f).sometimes(0.5).at(at.clone().add(new Vector(0, 1.0, 0)));
                }
                fx.cloud(Particle.GUST, center.clone().add(new Vector(0, 0.6, 0)), 1, half * 0.4, 0);
                for (Victim victim : stage.victimsIn(Area.segment(center.clone().subtract(side.clone().multiply(half)),
                        center.clone().add(side.clone().multiply(half)), 1.3))) {
                    if (!struck.add(victim.id())) continue;
                    stage.damage(victim, damage);
                    victim.push(dir.clone().multiply(1.2).setY(0.3));
                }
            });
        }
        tweenTo(t, stage, WIND + 14, WIND + 28, Poses.HOVER, Ease.IN_OUT);
        t.hold(WIND + 10 + TRAVEL + 2);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return WIND + 28;
    }

    @Override
    public String getName() {
        return "windcutter";
    }
}
