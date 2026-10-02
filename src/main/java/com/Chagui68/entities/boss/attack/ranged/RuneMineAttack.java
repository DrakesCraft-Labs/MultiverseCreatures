package com.Chagui68.entities.boss.attack.ranged;

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
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune Mines: the Sentinel sweeps its arm and flings glowing runes across the field. Each one lands,
 * draws itself into the floor and arms; stepping into an armed rune sets it off, and any left at the
 * end go off on their own, one after another.
 */
public class RuneMineAttack extends ChoreographedAttack.Ranged {

    private static final int SWEEP = 16;
    private static final int MINES = 7;
    private static final int FLIGHT = 14;
    private static final int ARM = 20;
    private static final int LIFE = 180;
    private static final double TRIGGER = 2.2;
    private static final double BLAST = 3.8;

    public RuneMineAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.rune-mine-damage", 9.0);
        Vector feet = stage.feet();
        List<Vector> spots = new ArrayList<>();
        for (Victim victim : stage.victims()) {
            if (spots.size() >= 3) break;
            Vector near = victim.position().add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(2.5));
            spots.add(stage.onGround(near));
        }
        while (spots.size() < MINES) {
            double angle = stage.random().nextDouble() * Math.PI * 2;
            spots.add(stage.onGround(feet.clone().add(Shapes.heading(angle).multiply(7 + stage.random().nextDouble() * 14))));
        }
        boolean[] gone = new boolean[MINES];
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, SWEEP / 2, Poses.SWING_BACK.withLeftArm(-90, 60, 0), Ease.IN_OUT);
        tweenTo(t, stage, SWEEP / 2, SWEEP, Poses.SWING_THROUGH.withLeftArm(-90, -60, 0), Ease.OUT);
        t.at(SWEEP / 2, () -> fx.sound(feet, Sfx.ENCHANT, 2f, 0.7f));
        recover(t, stage, SWEEP + 4, SWEEP + 18, Poses.GUARD);

        for (int i = 0; i < MINES; i++) {
            int index = i;
            Vector spot = spots.get(i);
            int thrown = SWEEP / 2 + i;
            int landed = thrown + FLIGHT;
            int armed = landed + ARM;
            t.span(thrown, landed, (tick, p) -> {
                Vector from = stage.body().leftHand();
                Vector at = from.clone().add(spot.clone().subtract(from).multiply(p)).add(new Vector(0, Math.sin(Math.PI * p) * 6, 0));
                fx.draw(Shapes.sphere(at, 0.5, 8), fx.dust(Palette.GOLD, 1.5f));
                fx.cloud(Particle.ENCHANT, at, 3, 0.2, 0.5);
            });
            t.span(landed, landed + LIFE, (tick, p) -> {
                if (gone[index]) return;
                boolean isArmed = tick >= ARM;
                Vector c = spot.clone().add(new Vector(0, 0.15, 0));
                if (tick % 2 == 0) {
                    double r = Math.min(TRIGGER, TRIGGER * tick / 8.0);
                    fx.ring(c, r, 0.45, tick * 0.1, fx.dust(isArmed ? Palette.WARNING : Palette.GOLD, 1.1f));
                    fx.draw(Shapes.star(c, r * 0.8, 3, 1, -tick * 0.1, 0.4, Shapes.FLAT_U, Shapes.FLAT_V),
                            fx.dust(isArmed ? Palette.EMBER : Palette.MOLTEN, 0.9f));
                }
                if (isArmed && tick % 20 == 0) fx.sound(spot, Sfx.BELL, 0.6f, 1.8f);
                boolean stepped = isArmed && !stage.victimsIn(Area.cylinder(spot, TRIGGER, 1, 3)).isEmpty();
                boolean expired = tick == LIFE - 1 - (MINES - index) * 4;
                if (stepped || expired) {
                    gone[index] = true;
                    fx.impact(spot.clone().add(new Vector(0, 0.8, 0)), Palette.EMBER, 2.5);
                    fx.draw(Shapes.helix(spot, 1.5, 5, 1.5, 30, 0), fx.dust(Palette.GOLD, 1.6f));
                    fx.sound(spot, Sfx.EXPLODE, 2f, 1.1f);
                    stage.hit(Area.cylinder(spot, BLAST, 1, 4), damage, victim -> {
                        victim.fling(new Vector(0, 1.0, 0));
                        victim.ignite(40);
                    });
                }
            });
            if (i == 0) t.at(armed, () -> fx.sound(spot, Sfx.BEACON_ACTIVATE, 1.5f, 1.8f));
        }
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SWEEP + 18;
    }

    @Override
    public String getName() {
        return "runemines";
    }
}
