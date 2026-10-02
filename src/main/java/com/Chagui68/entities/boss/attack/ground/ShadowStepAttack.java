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

/**
 * Shadow Step: the Sentinel dissolves into a column of void smoke while a sigil opens on the far side
 * of its target, then steps out of it already swinging. The sigil shows where it will come out.
 */
public class ShadowStepAttack extends ChoreographedAttack.Ground {

    private static final int SINK = 16;
    private static final double BEHIND = 6;

    public ShadowStepAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.shadow-step-damage", 14.0);
        Vector from = stage.feet();
        Vector past = Shapes.flat(target.position().subtract(from));
        Vector exit = stage.onGround(target.position().add(past.clone().multiply(BEHIND))).subtract(new Vector(0, 0.15, 0));
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, SINK, Poses.CAST_GROUND, Ease.IN);
        t.span(0, SINK, (tick, p) -> {
            Vector chest = stage.body().chest();
            fx.draw(Shapes.helix(from, 2.5 * (1 - p) + 1, 14, 3, 30, tick * 0.4), fx.dust(Palette.VOID_DEEP, 2.0f));
            fx.cloud(Particle.SQUID_INK, chest, 6, 1.5, 0.02);
            fx.cloud(Particle.REVERSE_PORTAL, chest, 10, 2, 0.1);
            if (tick % 2 == 0) {
                Telegraph.circle(stage, exit, 3, p);
                fx.draw(Shapes.star(exit.clone().add(new Vector(0, 0.2, 0)), 3, 5, 2, tick * 0.2, 0.4, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.dust(Palette.AMETHYST, 1.2f));
            }
        });
        t.at(0, () -> fx.sound(from, Sfx.ENDERMAN_TELEPORT, 2f, 0.5f));
        t.at(SINK - 4, () -> fx.sound(exit, Sfx.SOUL_ESCAPE, 2.5f, 0.6f));

        t.at(SINK, () -> {
            fx.cloud(Particle.LARGE_SMOKE, from.clone().add(new Vector(0, 5, 0)), 40, 2, 4, 0.03);
            stage.moveTo(exit);
            stage.face(target.position());
            fx.draw(Shapes.helix(exit, 3, 14, 2, 40, 0), fx.dust(Palette.AMETHYST, 2.0f));
            fx.burst(exit.clone().add(new Vector(0, 6, 0)), Particle.REVERSE_PORTAL, 60, 1.0);
            fx.sound(exit, Sfx.ENDERMAN_TELEPORT, 2.5f, 0.7f);
        });
        tween(t, stage, SINK, SINK + 3, Poses.CAST_GROUND, Poses.SWING_BACK, Ease.OUT);
        tween(t, stage, SINK + 3, SINK + 7, Poses.SWING_BACK, Poses.SWING_THROUGH, Ease.OUT);
        t.at(SINK + 5, () -> {
            Vector feet = stage.feet();
            Vector forward = stage.forward();
            double base = Math.atan2(forward.getZ(), forward.getX());
            fx.crescent(feet.clone().add(new Vector(0, 3, 0)), 4, 10, base - 1.2, base + 1.2, Shapes.FLAT_U, Shapes.FLAT_V,
                    fx.dust(Palette.SPECTRAL, 2.0f), fx.fade(Palette.VOID, Palette.VOID_DEEP, 1.6f));
            fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 3f, 0.5f);
            stage.hit(Area.cone(feet, forward, 1.2, 11, 7), damage, victim -> {
                victim.effect(Affliction.BLINDNESS, 40, 0);
                victim.fling(forward.clone().multiply(1.3).setY(0.5));
                fx.impact(victim.chest(), Palette.VOID, 1.5);
            });
        });
        recover(t, stage, SINK + 12, SINK + 28, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "shadowstep";
    }
}
