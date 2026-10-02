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
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Rune Ward: the spear is planted and a turning rune circle burns into the floor near the target.
 * It outlives the cast: every two seconds it pulses, filling its ring with light and hurting whoever
 * is still inside. The fight carries on around it.
 */
public class RuneWardAttack extends ChoreographedAttack.Ground {

    private static final int PLANT = 18;
    private static final int PULSES = 5;
    private static final int PERIOD = 40;
    private static final double RADIUS = 7;

    public RuneWardAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.rune-ward-damage", 8.0);
        Vector center = stage.onGround(aimAt(stage, stage.target(), 12));
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, PLANT - 4, Poses.SPEAR_OVERHEAD, Ease.IN_OUT);
        tween(t, stage, PLANT - 4, PLANT, Poses.SPEAR_OVERHEAD, Poses.KNEEL, Ease.OUT_BACK);
        t.span(0, PLANT, (tick, p) -> {
            Vector tip = stage.body().spearTip();
            if (tick % 2 == 0) fx.line(tip, center.clone().add(new Vector(0, 0.5, 0)), 1.0, fx.dust(Palette.GOLD, 1.2f));
            drawWard(stage, center, p, tick);
        });
        t.at(0, () -> fx.sound(center, Sfx.ENCHANT, 2.5f, 0.6f));
        t.at(PLANT, () -> {
            fx.flash(center.clone().add(new Vector(0, 1, 0)), Palette.GOLD);
            fx.sound(center, Sfx.BEACON_ACTIVATE, 2.5f, 0.8f);
        });
        recover(t, stage, PLANT + 6, PLANT + 22, Poses.GUARD);

        int end = PLANT + PULSES * PERIOD;
        t.span(PLANT, end, (tick, p) -> {
            int phase = tick % PERIOD;
            drawWard(stage, center, 1, tick);
            if (phase >= PERIOD - 14 && phase % 2 == 0) Telegraph.circle(stage, center, RADIUS, (phase - (PERIOD - 14)) / 13.0);
            if (phase == PERIOD - 1) {
                fx.draw(Shapes.helix(center, RADIUS * 0.9, 6, 1.5, 60, tick), fx.dust(Palette.HOLY, 2.0f));
                fx.ring(center.clone().add(new Vector(0, 0.3, 0)), RADIUS, 0.5, 0, fx.particle(Particle.END_ROD));
                fx.cloud(Particle.FIREWORK, center.clone().add(new Vector(0, 2, 0)), 20, RADIUS * 0.5, 0.05);
                fx.sound(center, Sfx.BELL, 2f, 1.2f);
                fx.sound(center, Sfx.BLAZE_SHOOT, 1.5f, 1.4f);
                stage.hit(Area.cylinder(center, RADIUS, 1, 6), damage, victim -> {
                    victim.push(new Vector(0, 0.7, 0));
                    victim.effect(Affliction.GLOWING, 60, 0);
                });
            }
        });
        t.at(end, () -> {
            fx.burst(center.clone().add(new Vector(0, 1, 0)), Particle.END_ROD, 40, 0.5);
            fx.sound(center, Sfx.BEACON_DEACTIVATE, 2f, 0.8f);
        });
        return t;
    }

    /** The ward: two rings, a pentagram and runes turning in the band between them. */
    private static void drawWard(Stage stage, Vector center, double progress, int tick) {
        if (tick % 2 != 0) return;
        Fx fx = stage.fx();
        Vector c = center.clone().add(new Vector(0, 0.15, 0));
        double r = RADIUS * Ease.at(Ease.OUT, progress);
        if (r < 0.5) return;
        fx.ring(c, r, 0.6, tick * 0.02, fx.dust(Palette.GOLD, 1.3f));
        fx.ring(c, r * 0.82, 0.8, -tick * 0.03, fx.dust(Palette.MOLTEN, 1.0f));
        fx.draw(Shapes.star(c, r * 0.8, 5, 2, tick * 0.02, 0.6, Shapes.FLAT_U, Shapes.FLAT_V), fx.dust(Palette.GOLD, 1.1f));
        for (Vector rune : Shapes.circle(c, r * 0.91, 10, Shapes.FLAT_U, Shapes.FLAT_V, -tick * 0.04)) {
            fx.cloud(Particle.ENCHANT, rune.clone().add(new Vector(0, 0.4, 0)), 2, 0.1, 0.4);
        }
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return PLANT + 22;
    }

    @Override
    public String getName() {
        return "runeward";
    }
}
