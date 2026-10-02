package com.Chagui68.entities.boss.attack.ground;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Whirlwind Slash: the Sentinel winds its spear back and spins twice, the blade tracing a ring of
 * wind that drags players inwards, then plants its feet and finishes with one wide cut.
 */
public class WhirlwindSlashAttack extends ChoreographedAttack.Ground {

    private static final int WIND = 14;
    private static final int SPIN = 30;
    private static final int TURNS = 2;
    private static final double RADIUS = 10;

    public WhirlwindSlashAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double spinDamage = stage.config("entities.armor-stand-boss.whirlwind-slash-secondary-damage", 4.0);
        double finishDamage = stage.config("entities.armor-stand-boss.whirlwind-slash-damage", 10.0);
        Vector feet = stage.feet();
        float startYaw = stage.yaw();
        Map<UUID, Integer> lastHit = new HashMap<>();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, WIND, Poses.SWING_BACK, Ease.IN_BACK);
        t.span(0, WIND, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, feet, RADIUS, p);
            fx.cloud(Particle.CLOUD, stage.body().spearTip(), 2, 0.3, 0.02);
        });
        t.at(0, () -> fx.sound(feet, Sfx.BREEZE_SHOOT, 2f, 0.5f));

        t.span(WIND, WIND + SPIN, (tick, p) -> {
            float yaw = startYaw - (float) (360.0 * TURNS * Ease.at(Ease.IN_OUT, p));
            Vector look = feet.clone().add(new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))));
            stage.face(look);
            stage.pose(Poses.SWING_THROUGH.withRightArm(-22, 20, 15));
            Vector tip = stage.body().spearTip();
            double angle = Math.atan2(tip.getZ() - feet.getZ(), tip.getX() - feet.getX());
            // The blade's wake: an arc of wind behind the tip.
            fx.draw(Shapes.arc(feet.clone().add(new Vector(0, tip.getY() - feet.getY(), 0)), RADIUS * 0.85,
                            angle, angle + 1.2, 12, Shapes.FLAT_U, Shapes.FLAT_V),
                    fx.dust(Palette.ICE, 1.6f).and(fx.particle(Particle.SWEEP_ATTACK).sometimes(0.2)));
            fx.ring(feet.clone().add(new Vector(0, 0.5, 0)), RADIUS, 1.4, tick * 0.4, fx.particle(Particle.CLOUD).sometimes(0.5));
            if (tick % 5 == 0) fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 2f, 0.6f + (float) p * 0.4f);
            for (Victim victim : stage.victimsIn(Area.cylinder(feet, RADIUS, 1, 8))) {
                Vector in = feet.clone().subtract(victim.position()).setY(0);
                if (in.lengthSquared() > 4) victim.push(in.normalize().multiply(0.07));
                Integer last = lastHit.get(victim.id());
                if (last == null || tick - last >= 10) {
                    lastHit.put(victim.id(), tick);
                    stage.damage(victim, spinDamage);
                }
            }
        });

        int cut = WIND + SPIN;
        t.at(cut, () -> stage.face(feet.clone().add(new Vector(-Math.sin(Math.toRadians(startYaw)), 0, Math.cos(Math.toRadians(startYaw))))));
        tween(t, stage, cut, cut + 6, Poses.SWING_BACK, Poses.SWING_THROUGH, Ease.OUT);
        t.at(cut + 3, () -> {
            Vector forward = stage.forward();
            double base = Math.atan2(forward.getZ(), forward.getX());
            Vector center = feet.clone().add(new Vector(0, 2.5, 0));
            fx.crescent(center, RADIUS * 0.6, RADIUS * 1.2, base - 1.3, base + 1.3, Shapes.FLAT_U, Shapes.FLAT_V,
                    fx.dust(Palette.HOLY, 2.0f), fx.dust(Palette.ICE, 1.4f));
            fx.sound(center, Sfx.PLAYER_ATTACK_SWEEP, 3f, 0.5f);
            fx.sound(center, Sfx.WIND_CHARGE_BURST, 2f, 0.7f);
            stage.hit(Area.cone(feet, forward, 1.3, RADIUS * 1.25, 7), finishDamage, victim -> {
                victim.fling(victim.position().subtract(feet).setY(0).normalize().multiply(1.5).setY(0.6));
                fx.impact(victim.chest(), Palette.ICE, 1.5);
            });
        });
        recover(t, stage, cut + 10, cut + 26, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "whirlwindslash";
    }
}
