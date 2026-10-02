package com.Chagui68.entities.boss.attack.aerial;

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
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Sonic Boom: the Sentinel draws a breath of sound into its chest, rings tightening around it, then
 * releases it down a straight line at its target. The aim locks a moment before it fires.
 */
public class SonicBoomAttack extends ChoreographedAttack.Aerial {

    private static final int CHARGE = 26;
    private static final int LOCK = 6;
    private static final double RANGE = 36;

    public SonicBoomAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 1.1);
        Vector[] aim = {target.chest()};
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CHARGE, Poses.ROAR.withHead(10, 0, 0), Ease.IN_OUT);
        t.span(0, CHARGE, (tick, p) -> {
            Vector chest = stage.body().chest();
            if (tick < CHARGE - LOCK) aim[0] = target.chest();
            Vector dir = aim[0].clone().subtract(chest).normalize();
            Vector[] axes = Shapes.planeAxes(dir);
            for (int ring = 0; ring < 3; ring++) {
                double r = (1 - p) * 6 + ring * 1.2;
                fx.draw(Shapes.circle(chest.clone().add(dir.clone().multiply(2 + ring * 1.5)), r, 14, axes[0], axes[1], tick * 0.2),
                        fx.dust(Palette.mix(Palette.SOUL, Palette.ICE, p), 1.3f));
            }
            if (tick % 3 == 0) fx.line(chest, chest.clone().add(dir.clone().multiply(RANGE)), 1.5,
                    fx.dust(tick >= CHARGE - LOCK ? Palette.WARNING : Palette.SOUL, 0.9f));
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.WARDEN_SONIC_CHARGE, 3f, 0.7f));

        t.at(CHARGE, () -> {
            Vector chest = stage.body().chest();
            Vector dir = aim[0].clone().subtract(chest).normalize();
            Vector end = chest.clone().add(dir.clone().multiply(RANGE));
            for (Vector point : Shapes.line(chest, end, 1.2)) {
                fx.sink().particle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0, null);
            }
            Vector[] axes = Shapes.planeAxes(dir);
            for (Vector point : Shapes.line(chest, end, 4)) {
                fx.draw(Shapes.circle(point, 1.5, 10, axes[0], axes[1], 0), fx.dust(Palette.SOUL, 1.4f));
            }
            fx.sound(chest, Sfx.WARDEN_SONIC_BOOM, 3f, 0.8f);
            stage.hit(Area.segment(chest, end, 2.4), damage, victim -> {
                victim.fling(dir.clone().multiply(2.0).setY(0.6));
                victim.effect(Affliction.NAUSEA, 60, 0);
            });
        });
        tween(t, stage, CHARGE, CHARGE + 3, Poses.ROAR, Poses.SPREAD, Ease.OUT_BACK);
        tweenTo(t, stage, CHARGE + 6, CHARGE + 20, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    @Override
    public String getName() {
        return "sonicboom";
    }
}
