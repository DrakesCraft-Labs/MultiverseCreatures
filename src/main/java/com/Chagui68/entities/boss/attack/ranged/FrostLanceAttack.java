package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Frost Lance: a lance of packed ice forms in the Sentinel's free hand, frost crawling over it, and is
 * flung at the target. It shatters into shards and leaves a ring of frost on the floor.
 */
public class FrostLanceAttack extends ChoreographedAttack.Ranged {

    private static final int FORM = 18;
    private static final double SPEED = 2.1;
    private static final Pose RAISED = Poses.GUARD.withLeftArm(-165, 20, -10).withHead(-10, 0, 0);
    private static final Pose HURL = Poses.GUARD.withLeftArm(-60, -10, 0).withBody(10, 15, 0);

    public FrostLanceAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.8);
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] lance = new Prop[1];

        tweenTo(t, stage, 0, FORM, RAISED, Ease.IN_OUT);
        t.at(2, () -> {
            Vector hand = stage.body().leftHand();
            lance[0] = stage.block(Material.PACKED_ICE, hand, 0.6f, Prop.pointing(target.chest().subtract(hand)));
            lance[0].resize(0.6f, 0.2f, Prop.pointing(target.chest().subtract(hand)), 0);
            lance[0].glow(Palette.FROST);
            props.add(lance[0]);
        });
        t.span(3, FORM, (tick, p) -> {
            Vector hand = stage.body().leftHand().add(new Vector(0, 1, 0));
            Vector dir = target.chest().subtract(hand);
            lance[0].moveTo(hand, 1);
            lance[0].resize(0.6f, (float) (0.2 + 5 * p), Prop.pointing(dir), 1);
            fx.draw(Shapes.helix(hand.clone().subtract(new Vector(0, 1, 0)), 1.2, 3, 2, 14, tick * 0.4), fx.dust(Palette.ICE, 1.2f));
            fx.cloud(Particle.SNOWFLAKE, hand, 3, 0.6, 0.02);
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.GLASS_BREAK, 1.5f, 0.5f));
        t.at(FORM - 6, () -> fx.sound(stage.feet(), Sfx.AMETHYST_CHIME, 2f, 0.5f));

        tween(t, stage, FORM, FORM + 4, RAISED, HURL, Ease.OUT_BACK);
        t.at(FORM + 1, () -> {
            Vector from = stage.body().leftHand().add(new Vector(0, 1, 0));
            Vector velocity = target.chest().subtract(from).normalize().multiply(SPEED);
            lance[0].resize(0.6f, 5f, Prop.pointing(velocity), 1);
            fx.sound(from, Sfx.TRIDENT_THROW, 2.5f, 1.4f);
            Missile missile = new Missile(from, velocity, 1.5)
                    .homing(target, 0.04)
                    .carrying(lance[0])
                    .look((at, dir, age) -> {
                        fx.line(at, at.clone().subtract(dir.clone().multiply(3)), 0.5, fx.fade(Palette.ICE, Palette.FROST, 1.2f));
                        fx.cloud(Particle.SNOWFLAKE, at, 2, 0.3, 0.01);
                    })
                    .onHit(victim -> {
                        stage.damage(victim, damage);
                        victim.effect(Affliction.SLOWNESS, 80, 3);
                        victim.effect(Affliction.MINING_FATIGUE, 80, 1);
                    })
                    .onBurst(at -> {
                        fx.flash(at, Palette.ICE);
                        fx.burst(at, Particle.SNOWFLAKE, 50, 0.5);
                        for (int i = 0; i < 6; i++) {
                            stage.debris(at, Vector.getRandom().subtract(new Vector(0.5, 0, 0.5)).multiply(0.5).setY(0.4), Material.PACKED_ICE, 20);
                        }
                        Vector floor = stage.onGround(at);
                        fx.ring(floor, 3.5, 0.4, 0, fx.dust(Palette.ICE, 1.6f).and(fx.particle(Particle.SNOWFLAKE)));
                        fx.sound(at, Sfx.GLASS_BREAK, 2.5f, 0.7f);
                        stage.hit(Area.cylinder(floor, 3.5, 1, 3), damage * 0.4,
                                victim -> victim.effect(Affliction.SLOWNESS, 60, 2));
                    });
            fly(t, stage, FORM + 2, 36, missile);
        });
        recover(t, stage, FORM + 8, FORM + 22, Poses.GUARD);
        t.hold(FORM + 40);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return FORM + 22;
    }

    @Override
    public String getName() {
        return "frostlance";
    }
}
