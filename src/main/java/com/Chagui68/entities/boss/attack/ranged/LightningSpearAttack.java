package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Bolts;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Lightning Spear: the spear is raised and the sky answers, a bolt striking its tip and leaving it
 * crackling. Then it is hurled: where it lands, lightning falls, and arcs jump to everyone close by.
 */
public class LightningSpearAttack extends ChoreographedAttack.Ranged {

    private static final int RAISE = 14;
    private static final int CHARGED = 10;
    private static final double SPEED = 2.4;
    private static final double ARC_RANGE = 9;

    public LightningSpearAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.9);
        Vector[] aim = {target.chest()};
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, RAISE, Poses.SPEAR_RAISED, Ease.OUT);
        t.at(RAISE - 2, () -> {
            Vector tip = stage.body().spearTip();
            Bolts.bolt(fx, tip.clone().add(new Vector(2, 30, -1)), tip, stage.random());
            stage.lightning(stage.feet().add(stage.body().right().multiply(-3)));
            fx.flash(tip, Palette.ICE);
            fx.sound(tip, Sfx.LIGHTNING_THUNDER, 2.5f, 0.8f);
        });
        t.span(RAISE - 2, RAISE + CHARGED, (tick, p) -> {
            Vector tip = stage.body().spearTip();
            Vector hand = stage.body().rightHand();
            if (tick % 2 == 0) Bolts.bolt(fx, hand, tip, stage.random());
            fx.cloud(Particle.ELECTRIC_SPARK, tip, 6, 0.6, 0.1);
            if (tick < CHARGED) aim[0] = target.chest();
        });
        tween(t, stage, RAISE + CHARGED - 6, RAISE + CHARGED, Poses.SPEAR_RAISED, Poses.THROW_COIL, Ease.IN_OUT);
        t.span(RAISE, RAISE + CHARGED, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, stage.onGround(aim[0]), ARC_RANGE * 0.4, p);
        });

        int release = RAISE + CHARGED;
        tween(t, stage, release, release + 4, Poses.THROW_COIL, Poses.THROW, Ease.OUT_BACK);
        t.at(release + 1, () -> {
            Vector from = stage.body().spearTip();
            Vector velocity = aim[0].clone().subtract(from).normalize().multiply(SPEED);
            Prop lance = spear(stage, Material.NETHERITE_SPEAR, from, velocity, 5f);
            lance.glow(Palette.STORM);
            props.add(lance);
            fx.sound(from, Sfx.TRIDENT_THROW, 3f, 0.7f);
            Missile missile = new Missile(from, velocity, 1.6)
                    .carrying(lance)
                    .look((at, dir, age) -> {
                        Bolts.bolt(fx, at.clone().subtract(dir.clone().multiply(4)), at, stage.random());
                        fx.cloud(Particle.ELECTRIC_SPARK, at, 4, 0.3, 0.05);
                    })
                    .onHit(victim -> stage.damage(victim, damage))
                    .onBurst(at -> {
                        Vector floor = stage.onGround(at);
                        stage.lightning(floor);
                        fx.impact(at, Palette.STORM, 2.5);
                        fx.sound(at, Sfx.TRIDENT_THUNDER, 3f, 1f);
                        // The strike arcs out to everyone near it.
                        for (Victim near : stage.victimsIn(Area.sphere(at, ARC_RANGE))) {
                            Bolts.bolt(fx, at, near.chest(), stage.random());
                            stage.damage(near, damage * 0.5);
                            near.effect(Affliction.SLOWNESS, 40, 1);
                            near.push(new Vector(0, 0.4, 0));
                        }
                    });
            fly(t, stage, release + 2, 30, missile);
        });
        recover(t, stage, release + 8, release + 22, Poses.GUARD);
        t.hold(release + 34);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return RAISE + CHARGED + 22;
    }

    @Override
    public String getName() {
        return "lightningspear";
    }
}
