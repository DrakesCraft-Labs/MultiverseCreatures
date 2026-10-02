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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Doom Beam: blood-red energy gathers between the Sentinel's hands, then a beam tears out and sweeps
 * across the cone in front of it, scorching a line on the floor. Ducking behind it, or outrunning the
 * sweep, is the way out.
 */
public class DoomBeamAttack extends ChoreographedAttack.Ground {

    private static final int CHARGE = 30;
    private static final int SWEEP = 34;
    private static final double LENGTH = 30;
    private static final double IMPACT = 15;
    private static final double HALF_ARC = Math.toRadians(40);

    public DoomBeamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.45);
        Vector feet = stage.feet();
        Vector forward = stage.forward();
        double base = Math.atan2(forward.getZ(), forward.getX());
        Map<UUID, Integer> lastHit = new HashMap<>();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CHARGE, Poses.CHANNEL, Ease.IN_OUT);
        gather(t, stage, 0, CHARGE, () -> core(stage), 6, Palette.BLOOD);
        t.span(0, CHARGE, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.cone(stage, feet, forward, HALF_ARC, LENGTH, p);
            fx.draw(Shapes.sphere(core(stage), 0.6 + p * 1.4, 20), fx.fade(Palette.BLOOD, Palette.VOID_DEEP, 2.0f));
        });
        t.at(0, () -> fx.sound(feet, Sfx.WARDEN_SONIC_CHARGE, 3f, 0.5f));
        t.at(CHARGE - 10, () -> fx.sound(feet, Sfx.BEACON_POWER, 2.5f, 0.5f));

        t.span(CHARGE, CHARGE + SWEEP, (tick, p) -> {
            double angle = base - HALF_ARC + 2 * HALF_ARC * Ease.at(Ease.IN_OUT, p);
            Vector heading = Shapes.heading(angle);
            Vector from = core(stage);
            // The beam strikes the floor partway out and the fire runs on along the ground from there,
            // so the whole lane burns at the height a player stands at.
            Vector impact = stage.onGround(feet.clone().add(heading.clone().multiply(IMPACT))).add(new Vector(0, 0.4, 0));
            Vector end = stage.onGround(feet.clone().add(heading.clone().multiply(LENGTH))).add(new Vector(0, 0.4, 0));
            fx.beam(from, impact, Palette.HOLY, Palette.BLOOD, 1.4);
            fx.line(from, impact, 1.6, fx.particle(Particle.SONIC_BOOM).sometimes(0.15));
            fx.impact(impact, Palette.BLOOD, 1);
            fx.line(feet.clone().add(heading.clone().multiply(3)).setY(impact.getY()), end, 0.5,
                    fx.particle(Particle.FLAME).and(fx.dust(Palette.BLOOD, 1.4f)).and(fx.particle(Particle.LARGE_SMOKE).sometimes(0.15)));
            fx.cloud(Particle.LAVA, impact, 2, 0.5, 0);
            if (tick % 4 == 0) fx.sound(impact, Sfx.FIRECHARGE, 1.5f, 0.5f);
            Area lane = Area.segment(feet.clone().add(heading.clone().multiply(3)), end.clone(), 2.2)
                    .or(Area.segment(from, impact, 2.0));
            for (Victim victim : stage.victimsIn(lane)) {
                Integer last = lastHit.get(victim.id());
                if (last != null && tick - last < 6) continue;
                lastHit.put(victim.id(), tick);
                stage.damage(victim, damage);
                victim.ignite(80);
                victim.effect(Affliction.WITHER, 60, 1);
                victim.push(heading.clone().multiply(0.5).setY(0.3));
            }
        });
        t.at(CHARGE, () -> fx.sound(feet, Sfx.WARDEN_SONIC_BOOM, 3f, 0.4f));
        recover(t, stage, CHARGE + SWEEP, CHARGE + SWEEP + 16, Poses.GUARD);
        return t;
    }

    /** The point between the hands where the beam is born. */
    private static Vector core(Stage stage) {
        return stage.body().rightHand().midpoint(stage.body().leftHand()).add(stage.forward().multiply(1.2));
    }

    @Override
    public String getName() {
        return "doombeam";
    }
}
