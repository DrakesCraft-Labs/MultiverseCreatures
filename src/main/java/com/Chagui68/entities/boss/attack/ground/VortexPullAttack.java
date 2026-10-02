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
 * Vortex Pull: the Sentinel channels a whirlpool of void across the floor. Spiral arms turn inwards
 * and drag everyone towards it while the core darkens, until it implodes.
 */
public class VortexPullAttack extends ChoreographedAttack.Ground {

    private static final int CHANNEL = 12;
    private static final int PULL = 50;
    private static final double RADIUS = 16;
    private static final double CORE = 5;

    public VortexPullAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double tickDamage = stage.config("entities.armor-stand-boss.vortex-pull-tick-damage", 2.0);
        double finalDamage = stage.config("entities.armor-stand-boss.vortex-pull-final-damage", 12.0);
        Vector center = stage.onGround(stage.feet().add(stage.forward().multiply(6)));
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CHANNEL, Poses.CHANNEL, Ease.IN_OUT);
        t.at(0, () -> fx.sound(center, Sfx.CONDUIT_ACTIVATE, 2.5f, 0.5f));
        t.span(0, CHANNEL, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, center, CORE, p);
            Telegraph.ring(stage, center, RADIUS - 0.5, RADIUS, p);
        });

        t.span(CHANNEL, CHANNEL + PULL, (tick, p) -> {
            double spin = tick * 0.18;
            // Four spiral arms winding into the core.
            for (int arm = 0; arm < 4; arm++) {
                for (double r = RADIUS; r > 1; r -= 0.9) {
                    double angle = spin + arm * Math.PI / 2 + (RADIUS - r) * 0.22;
                    Vector point = stage.onGround(center.clone().add(Shapes.heading(angle).multiply(r)));
                    fx.dust(Palette.mix(Palette.VOID, Palette.VOID_DEEP, 1 - r / RADIUS), 1.2f + (float) (1 - r / RADIUS)).at(point.add(new Vector(0, 0.2, 0)));
                }
            }
            fx.draw(Shapes.sphere(center.clone().add(new Vector(0, 1.5, 0)), 1 + p * 1.5, 24), fx.dust(Palette.VOID_DEEP, 2.0f));
            fx.cloud(Particle.REVERSE_PORTAL, center.clone().add(new Vector(0, 1.5, 0)), 8, 1.5, 0.05);
            Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
            if (tick % 2 == 0) fx.line(hands, center.clone().add(new Vector(0, 1.5, 0)), 0.8, fx.dust(Palette.AMETHYST, 1.2f));
            for (Victim victim : stage.victimsIn(Area.cylinder(center, RADIUS, 3, 8))) {
                Vector in = center.clone().subtract(victim.position()).setY(0);
                double distance = in.length();
                if (distance > 0.6) victim.push(in.normalize().multiply(0.05 + 0.08 * p).add(Shapes.heading(spin).multiply(0.02)));
                if (tick % 10 == 0 && distance < CORE) stage.damage(victim, tickDamage);
            }
            if (tick % 10 == 0) fx.sound(center, Sfx.WARDEN_HEARTBEAT, 2f, 0.5f + (float) p);
        });

        int burst = CHANNEL + PULL;
        tween(t, stage, burst - 3, burst + 2, Poses.CHANNEL, Poses.SPREAD, Ease.OUT_BACK);
        t.at(burst, () -> {
            Vector core = center.clone().add(new Vector(0, 1.5, 0));
            fx.flash(core, Palette.AMETHYST);
            fx.burst(core, Particle.REVERSE_PORTAL, 80, 1.2);
            fx.burst(core, Particle.SQUID_INK, 30, 0.5);
            fx.draw(Shapes.sphere(core, 4, 60), fx.dust(Palette.AMETHYST, 2.4f));
            fx.sound(core, Sfx.WARDEN_SONIC_BOOM, 2f, 0.6f);
            fx.sound(core, Sfx.EXPLODE, 2f, 0.6f);
            stage.hit(Area.cylinder(center, CORE + 1, 2, 6), finalDamage, victim -> {
                victim.fling(new Vector(0, 1.3, 0));
                victim.effect(Affliction.DARKNESS, 60, 0);
            });
        });
        shockwave(t, stage, burst, 12, center, RADIUS * 0.7, Palette.AMETHYST, finalDamage * 0.3, null);
        recover(t, stage, burst + 6, burst + 22, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "vortexpull";
    }
}
