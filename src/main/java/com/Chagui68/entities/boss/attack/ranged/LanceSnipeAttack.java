package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
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
 * Lance Snipe: the spear is drawn back like a javelin while the line of fire is marked on the floor,
 * then a spectral copy is hurled at the target, trailing gold, and bursts where it strikes.
 */
public class LanceSnipeAttack extends ChoreographedAttack.Ranged {

    private static final int AIM = 18;
    private static final double SPEED = 2.6;

    public LanceSnipeAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 1.0);
        Vector[] aim = {target.chest()};
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, AIM, Poses.THROW_COIL, Ease.IN_OUT);
        t.span(0, AIM, (tick, p) -> {
            if (tick < AIM - 4) aim[0] = target.chest();
            Vector hand = stage.body().rightHand();
            if (tick % 2 == 0) Telegraph.line(stage, stage.feet(), aim[0].clone().setY(stage.feet().getY()), 2.5, p);
            fx.draw(Shapes.sphere(stage.body().spearTip(), 0.3 + p * 0.8, 10), fx.dust(Palette.GOLD, 1.4f));
            if (tick % 3 == 0) fx.gather(stage.body().spearTip(), 3, 4, Palette.HOLY, 6);
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.TRIDENT_RIPTIDE, 2f, 0.5f));

        tween(t, stage, AIM, AIM + 4, Poses.THROW_COIL, Poses.THROW, Ease.OUT_BACK);
        t.at(AIM + 1, () -> {
            Vector from = stage.body().spearTip();
            Vector velocity = aim[0].clone().subtract(from).normalize().multiply(SPEED);
            Prop lance = spear(stage, Material.NETHERITE_SPEAR, from, velocity, 5f);
            lance.glow(Palette.GOLD);
            props.add(lance);
            fx.sound(from, Sfx.TRIDENT_THROW, 3f, 0.5f);
            fx.flash(from, Palette.GOLD);
            Missile missile = new Missile(from, velocity, 1.6)
                    .carrying(lance)
                    .look((at, dir, age) -> {
                        fx.line(at, at.clone().subtract(dir.clone().multiply(4)), 0.4, fx.fade(Palette.HOLY, Palette.GOLD, 1.4f));
                        fx.cloud(Particle.END_ROD, at, 1, 0.1, 0);
                    })
                    .onHit(victim -> {
                        stage.damage(victim, damage);
                        victim.fling(velocity.clone().normalize().multiply(1.2).setY(0.5));
                    })
                    .onBurst(at -> {
                        fx.impact(at, Palette.GOLD, 2.5);
                        fx.draw(Shapes.ring(at, 3, 0.4, 0), fx.dust(Palette.HOLY, 1.8f));
                        fx.sound(at, Sfx.TRIDENT_THUNDER, 2f, 1.2f);
                    });
            fly(t, stage, AIM + 2, 30, missile);
        });
        recover(t, stage, AIM + 8, AIM + 22, Poses.GUARD);
        t.hold(AIM + 34);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return AIM + 22;
    }

    @Override
    public String getName() {
        return "lancesnipe";
    }
}
