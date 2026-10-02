package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Arcane Orb: a large, slow orb wrapped in turning rings is pushed out from the Sentinel's hands. It
 * hunts its target patiently and detonates in a wide sphere when it reaches anyone.
 */
public class ArcaneOrbAttack extends ChoreographedAttack.Ranged {

    private static final int FORM = 24;
    private static final double SPEED = 0.55;
    private static final double SIZE = 1.8;
    private static final double BLAST = 5.5;

    public ArcaneOrbAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 1.0);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, FORM, Poses.CHANNEL, Ease.IN_OUT);
        gather(t, stage, 0, FORM, () -> core(stage), 6, Palette.AMETHYST);
        t.span(0, FORM, (tick, p) -> drawOrb(fx, core(stage), SIZE * p, tick));
        t.at(0, () -> fx.sound(stage.feet(), Sfx.ILLUSIONER_CAST, 2f, 0.6f));

        tween(t, stage, FORM, FORM + 4, Poses.CHANNEL, Poses.CAST_FORWARD, Ease.OUT_BACK);
        t.at(FORM + 1, () -> {
            Vector from = core(stage);
            fx.sound(from, Sfx.EVOKER_CAST, 2f, 0.5f);
            Missile orb = new Missile(from, target.chest().subtract(from).normalize().multiply(SPEED), SIZE + 0.4)
                    .homing(target, 0.08)
                    .look((at, dir, age) -> {
                        drawOrb(fx, at, SIZE, age);
                        if (age % 2 == 0) fx.line(at, at.clone().subtract(dir.clone().multiply(5)), 0.6, fx.fade(Palette.SPECTRAL, Palette.VOID, 1.0f));
                        if (age % 20 == 0) fx.sound(at, Sfx.BEACON_POWER, 1f, 1.6f);
                    })
                    .onBurst(at -> {
                        fx.flash(at, Palette.SPECTRAL);
                        fx.draw(Shapes.sphere(at, BLAST, 90), fx.fade(Palette.SPECTRAL, Palette.VOID, 2.2f));
                        fx.burst(at, Particle.END_ROD, 50, 0.8);
                        fx.burst(at, Particle.REVERSE_PORTAL, 40, 1.0);
                        fx.sound(at, Sfx.EXPLODE, 2.5f, 0.9f);
                        fx.sound(at, Sfx.GLASS_BREAK, 2f, 0.5f);
                        stage.hit(Area.sphere(at, BLAST), damage, victim -> {
                            Vector away = victim.position().subtract(at);
                            if (away.lengthSquared() > 1e-6) away.normalize();
                            victim.fling(away.multiply(1.2).setY(0.7));
                        });
                    });
            fly(t, stage, FORM + 2, 120, orb);
        });
        recover(t, stage, FORM + 8, FORM + 22, Poses.GUARD);
        t.hold(FORM + 124);
        return t;
    }

    private static void drawOrb(Fx fx, Vector at, double size, int age) {
        if (size < 0.2) return;
        fx.draw(Shapes.sphere(at, size * 0.6, 14), fx.dust(Palette.SPECTRAL, 2.0f));
        double spin = age * 0.25;
        Vector a = new Vector(Math.cos(spin), 0.4, Math.sin(spin)).normalize();
        Vector b = new Vector(-Math.sin(spin * 0.7), 1, Math.cos(spin * 0.7)).normalize();
        Vector[] ringA = Shapes.planeAxes(a);
        Vector[] ringB = Shapes.planeAxes(b);
        fx.draw(Shapes.circle(at, size, 16, ringA[0], ringA[1], spin), fx.dust(Palette.AMETHYST, 1.4f));
        fx.draw(Shapes.circle(at, size * 1.15, 16, ringB[0], ringB[1], -spin), fx.dust(Palette.VOID, 1.4f));
    }

    private static Vector core(Stage stage) {
        return stage.body().rightHand().midpoint(stage.body().leftHand()).add(stage.forward().multiply(2));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return FORM + 22;
    }

    @Override
    public String getName() {
        return "arcaneorb";
    }
}
