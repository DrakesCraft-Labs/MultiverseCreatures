package com.Chagui68.entities.boss.attack.ranged;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Void Beam: a dark sphere grows between the Sentinel's hands, then a beam of void pours out of it
 * and follows the target a step behind. Outrunning its turn is how to escape it.
 */
public class VoidBeamAttack extends ChoreographedAttack.Ranged {

    private static final int CHARGE = 24;
    private static final int FIRE = 44;
    private static final double TURN = 0.12;
    private static final double RANGE = 40;

    public VoidBeamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.3);
        Vector[] aim = new Vector[1];
        Map<UUID, Integer> lastHit = new HashMap<>();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CHARGE, Poses.CAST_FORWARD, Ease.IN_OUT);
        gather(t, stage, 0, CHARGE, () -> core(stage), 7, Palette.VOID);
        t.span(0, CHARGE, (tick, p) -> {
            Vector core = core(stage);
            fx.draw(Shapes.sphere(core, 0.5 + p * 1.6, 24), fx.fade(Palette.VOID, Palette.VOID_DEEP, 2.2f));
            fx.cloud(Particle.REVERSE_PORTAL, core, 4, 0.6, 0.05);
            // Aiming line, faint, so the player sees what it will lock on to.
            if (tick % 3 == 0) fx.line(core, target.chest(), 1.4, fx.dust(Palette.AMETHYST, 0.8f));
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.WARDEN_SONIC_CHARGE, 2.5f, 0.6f));

        t.span(CHARGE, CHARGE + FIRE, (tick, p) -> {
            Vector core = core(stage);
            Vector want = target.chest().subtract(core).normalize();
            aim[0] = aim[0] == null ? want : aim[0].clone().multiply(1 - TURN).add(want.multiply(TURN)).normalize();
            Vector end = core.clone().add(aim[0].clone().multiply(RANGE));
            // Stop the beam where it meets the floor.
            for (Vector point : Shapes.line(core, end, 1.0)) {
                if (point.getY() <= stage.floorY(point.getX(), point.getY(), point.getZ())) {
                    end = point;
                    break;
                }
            }
            fx.beam(core, end, Palette.SPECTRAL, Palette.VOID, 1.6);
            Vector[] axes = Shapes.planeAxes(aim[0]);
            for (Vector point : Shapes.line(core, end, 3.0)) {
                fx.draw(Shapes.circle(point, 0.9, 6, axes[0], axes[1], tick * 0.5 + point.length()), fx.dust(Palette.VOID_DEEP, 1.2f));
            }
            fx.impact(end, Palette.VOID, 1);
            if (tick % 5 == 0) fx.sound(core, Sfx.BEACON_POWER, 1.5f, 0.5f);
            for (Victim victim : stage.victimsIn(Area.segment(core, end, 1.8))) {
                Integer last = lastHit.get(victim.id());
                if (last != null && tick - last < 6) continue;
                lastHit.put(victim.id(), tick);
                stage.damage(victim, damage);
                victim.effect(Affliction.DARKNESS, 40, 0);
                victim.push(aim[0].clone().multiply(0.25));
            }
        });
        t.at(CHARGE, () -> fx.sound(stage.feet(), Sfx.WARDEN_SONIC_BOOM, 2.5f, 0.6f));
        recover(t, stage, CHARGE + FIRE, CHARGE + FIRE + 14, Poses.GUARD);
        return t;
    }

    private static Vector core(Stage stage) {
        return stage.body().rightHand().midpoint(stage.body().leftHand()).add(stage.forward().multiply(1.5));
    }

    @Override
    public String getName() {
        return "voidbeam";
    }
}
