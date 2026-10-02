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

import java.util.ArrayList;
import java.util.List;

/**
 * Starfall: hanging in the air, the Sentinel lifts its arms and the sky answers with falling stars.
 * Each lands in a golden circle that warns of it, leaving a burst of light and a scorched star.
 */
public class StarfallAttack extends ChoreographedAttack.Aerial {

    private static final int CALL = 14;
    private static final int STARS = 12;
    private static final int GAP = 3;
    private static final int FALL = 16;
    private static final double RADIUS = 3;

    public StarfallAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.5);
        Vector feet = stage.feet();
        Vector below = stage.onGround(feet);
        List<Vector> spots = new ArrayList<>();
        List<Victim> victims = stage.victims();
        for (int i = 0; i < STARS; i++) {
            Vector around = victims.isEmpty() ? below : victims.get(i % victims.size()).position();
            double spread = i < victims.size() ? 0 : 3 + stage.random().nextDouble() * 9;
            spots.add(stage.onGround(around.clone().add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(spread))));
        }
        Vector slant = new Vector(0.35, -1, 0.2).normalize();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CALL, Poses.CAST_SKY, Ease.OUT);
        t.at(0, () -> fx.sound(feet, Sfx.AMETHYST_CHIME, 3f, 0.5f));
        t.span(0, CALL, (tick, p) -> {
            Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
            fx.draw(Shapes.sphere(hands, 1 + p * 2, 16), fx.dust(Palette.GOLD, 1.4f));
            fx.cloud(Particle.FIREWORK, hands.clone().add(new Vector(0, 6, 0)), 3, 3, 0.02);
        });

        for (int i = 0; i < STARS; i++) {
            Vector spot = spots.get(i);
            int land = CALL + FALL + i * GAP;
            Vector sky = spot.clone().subtract(slant.clone().multiply(40));
            t.span(land - FALL - 8, land, (tick, p) -> {
                if (tick % 2 == 0) Telegraph.circle(stage, spot, RADIUS, Math.min(1, tick / (FALL + 7.0)));
            });
            t.span(land - FALL, land, (tick, p) -> {
                Vector at = sky.clone().add(spot.clone().subtract(sky).multiply(Ease.at(Ease.IN, p)));
                fx.draw(Shapes.sphere(at, 0.7, 10), fx.dust(Palette.HOLY, 2.0f));
                fx.line(at, at.clone().subtract(slant.clone().multiply(5)), 0.4, fx.fade(Palette.GOLD, Palette.EMBER, 1.4f));
                fx.cloud(Particle.END_ROD, at, 2, 0.2, 0.02);
            });
            t.at(land, () -> {
                fx.flash(spot.clone().add(new Vector(0, 1, 0)), Palette.GOLD);
                fx.burst(spot.clone().add(new Vector(0, 0.5, 0)), Particle.FIREWORK, 30, 0.4);
                fx.draw(Shapes.star(spot.clone().add(new Vector(0, 0.15, 0)), RADIUS, 5, 2, stage.random().nextDouble() * 6, 0.35,
                        Shapes.FLAT_U, Shapes.FLAT_V), fx.dust(Palette.GOLD, 1.4f));
                fx.sound(spot, Sfx.EXPLODE, 1.5f, 1.3f);
                fx.sound(spot, Sfx.AMETHYST_CHIME, 1.5f, 1.4f);
                stage.hit(Area.cylinder(spot, RADIUS, 1, 4), damage, victim -> victim.push(new Vector(0, 0.5, 0)));
            });
        }
        int end = CALL + FALL + STARS * GAP;
        tweenTo(t, stage, end - 4, end + 10, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    @Override
    public String getName() {
        return "starfall";
    }
}
