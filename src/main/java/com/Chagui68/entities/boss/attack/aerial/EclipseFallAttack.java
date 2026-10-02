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
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Eclipse Fall: a black sun with a burning corona swells in the sky over the target, blotting it
 * out, then drops. Darkness spreads from where it lands.
 */
public class EclipseFallAttack extends ChoreographedAttack.Aerial {

    private static final int SWELL = 32;
    private static final int DROP = 8;
    private static final double RADIUS = 6.5;
    private static final double HEIGHT = 22;

    public EclipseFallAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.eclipse-fall-damage", 16.0);
        Vector ground = stage.onGround(aimAt(stage, stage.target(), 10));
        Vector sky = ground.clone().add(new Vector(0, HEIGHT, 0));
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, SWELL, Poses.CAST_SKY, Ease.OUT);
        t.at(0, () -> fx.sound(sky, Sfx.WITHER_SPAWN, 1.5f, 0.6f));
        t.span(0, SWELL, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, ground, RADIUS, p);
            drawEclipse(fx, sky, RADIUS * Ease.at(Ease.OUT, p), tick);
            fx.line(stage.body().rightHand().midpoint(stage.body().leftHand()), sky, 1.4, fx.dust(Palette.EMBER, 1.0f).sometimes(0.5));
        });
        t.span(SWELL, SWELL + DROP, (tick, p) -> {
            Vector at = sky.clone().add(ground.clone().subtract(sky).multiply(Ease.at(Ease.IN, p)));
            drawEclipse(fx, at, RADIUS, SWELL + tick);
            fx.cloud(Particle.LARGE_SMOKE, at.clone().add(new Vector(0, 2, 0)), 10, RADIUS * 0.5, 0.02);
        });
        t.at(SWELL, () -> fx.sound(sky, Sfx.DRAGON_SHOOT, 3f, 0.4f));
        int impact = SWELL + DROP;
        t.at(impact, () -> {
            fx.flash(ground.clone().add(new Vector(0, 1, 0)), Palette.EMBER);
            fx.impact(ground.clone().add(new Vector(0, 1, 0)), Palette.VOID_DEEP, 5);
            fx.flatBurst(ground, Particle.FLAME, 50, 0.7);
            fx.burst(ground.clone().add(new Vector(0, 2, 0)), Particle.SQUID_INK, 60, 0.6);
            fx.sound(ground, Sfx.EXPLODE, 3f, 0.4f);
            fx.sound(ground, Sfx.WARDEN_SONIC_BOOM, 2f, 0.5f);
            stage.hit(Area.cylinder(ground, RADIUS, 1, 6), damage, victim -> {
                victim.effect(Affliction.DARKNESS, 100, 0);
                victim.ignite(80);
                victim.fling(new Vector(0, 1.0, 0));
            });
        });
        shockwave(t, stage, impact, 12, ground, RADIUS * 2.2, Palette.VOID, damage * 0.3, victim -> victim.effect(Affliction.DARKNESS, 60, 0));
        tweenTo(t, stage, impact + 2, impact + 16, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    /** A black disc edged by a corona of flame and flickering spikes of light. */
    private static void drawEclipse(Fx fx, Vector center, double radius, int tick) {
        if (radius < 0.3) return;
        fx.disc(center, radius * 0.85, 0.9, fx.dust(Palette.VOID_DEEP, 2.4f));
        fx.ring(center, radius, 0.6, tick * 0.05, fx.dust(Palette.EMBER, 2.0f));
        fx.ring(center, radius * 1.12, 0.8, -tick * 0.08, fx.dust(Palette.GOLD, 1.4f).sometimes(0.6));
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6 + tick * 0.03;
            double flare = radius * (1.15 + 0.35 * Math.abs(Math.sin(tick * 0.3 + i)));
            fx.line(Shapes.onCircle(center, radius, angle, Shapes.FLAT_U, Shapes.FLAT_V),
                    Shapes.onCircle(center, flare, angle, Shapes.FLAT_U, Shapes.FLAT_V), 0.5, fx.dust(Palette.MOLTEN, 1.3f));
        }
    }

    @Override
    public String getName() {
        return "eclipsefall";
    }
}
