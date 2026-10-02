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
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Void Rift: the Sentinel tears the air open where its target stands. A standing slit of void widens,
 * dragging everything nearby towards it, then snaps shut with a blast.
 */
public class VoidRiftAttack extends ChoreographedAttack.Ranged {

    private static final int TEAR = 16;
    private static final int OPEN = 40;
    private static final double PULL = 11;
    private static final double BLAST = 5;

    public VoidRiftAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 1.0);
        Vector base = stage.onGround(aimAt(stage, stage.target(), 14));
        Vector center = base.clone().add(new Vector(0, 3.5, 0));
        Vector across = Shapes.flat(base.clone().subtract(stage.feet()));
        Vector side = new Vector(-across.getZ(), 0, across.getX());
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, TEAR, Poses.CAST_FORWARD.withRightArm(-95, -40, 0), Ease.IN_BACK);
        t.span(0, TEAR, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, base, BLAST, p);
            fx.line(center.clone().subtract(new Vector(0, 3 * p, 0)), center.clone().add(new Vector(0, 3 * p, 0)), 0.3,
                    fx.dust(Palette.SPECTRAL, 1.2f));
        });
        t.at(0, () -> fx.sound(center, Sfx.TRIAL_SPAWNER_OMINOUS, 2.5f, 0.6f));
        t.at(TEAR, () -> {
            fx.flash(center, Palette.AMETHYST);
            fx.sound(center, Sfx.END_PORTAL_SPAWN, 1.5f, 1.4f);
        });

        t.span(TEAR, TEAR + OPEN, (tick, p) -> {
            double width = 1.6 * Math.sin(Math.PI * Math.min(1, p * 1.2));
            // A standing ellipse: the edge in bright violet, the inside in deep void.
            for (double a = 0; a < 2 * Math.PI; a += 0.12) {
                Vector edge = center.clone().add(side.clone().multiply(Math.cos(a) * width)).add(new Vector(0, Math.sin(a) * 4, 0));
                fx.dust(Palette.AMETHYST, 1.6f).at(edge);
            }
            for (double y = -3.5; y <= 3.5; y += 0.5) {
                double half = width * Math.sqrt(Math.max(0, 1 - (y / 4) * (y / 4)));
                for (double s = -half + 0.3; s < half; s += 0.5) {
                    fx.dust(Palette.VOID_DEEP, 2.0f).at(center.clone().add(side.clone().multiply(s)).add(new Vector(0, y, 0)));
                }
            }
            fx.cloud(Particle.REVERSE_PORTAL, center, 10, 2, 0.2);
            if (tick % 2 == 0) fx.gather(center, PULL, 4, Palette.VOID, 12);
            for (Victim victim : stage.victimsIn(Area.cylinder(base, PULL, 2, 8))) {
                Vector in = center.clone().subtract(victim.chest());
                if (in.lengthSquared() > 1) victim.push(in.normalize().multiply(0.09));
            }
            if (tick % 10 == 0) fx.sound(center, Sfx.WARDEN_HEARTBEAT, 1.5f, 1.2f);
        });

        int close = TEAR + OPEN;
        t.at(close, () -> {
            fx.flash(center, Palette.SPECTRAL);
            fx.draw(Shapes.sphere(center, BLAST, 70), fx.fade(Palette.AMETHYST, Palette.VOID_DEEP, 2.2f));
            fx.burst(center, Particle.REVERSE_PORTAL, 80, 1.4);
            fx.sound(center, Sfx.EXPLODE, 2.5f, 0.6f);
            fx.sound(center, Sfx.WARDEN_SONIC_BOOM, 1.5f, 1.2f);
            stage.hit(Area.sphere(center, BLAST + 1).or(Area.cylinder(base, BLAST, 1, 7)), damage, victim -> {
                victim.effect(Affliction.NAUSEA, 80, 0);
                Vector away = victim.position().subtract(base).setY(0);
                if (away.lengthSquared() > 1e-6) away.normalize();
                victim.fling(away.multiply(1.1).setY(0.8));
            });
        });
        recover(t, stage, TEAR + 4, TEAR + 20, Poses.GUARD);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return TEAR + 20;
    }

    @Override
    public String getName() {
        return "voidrift";
    }
}
