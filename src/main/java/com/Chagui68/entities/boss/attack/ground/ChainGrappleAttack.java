package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Chain Grapple: the free hand flings a burning chain at the target. If it bites, the target is
 * reeled in hand over hand and met with a spear thrust that throws them back out.
 */
public class ChainGrappleAttack extends ChoreographedAttack.Ground {

    private static final int AIM = 14;
    private static final int THROW = 8;
    private static final int REEL = 18;
    private static final Pose FLING = Poses.GUARD.withLeftArm(-95, 10, 0).withBody(0, -15, 0).withHead(5, 0, 0);
    private static final Pose HAUL = Poses.THRUST_COIL.withLeftArm(-30, 0, -20);
    private static final Color IRON = Color.fromRGB(0x9A9AA6);

    public ChainGrappleAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double hookDamage = stage.config("entities.armor-stand-boss.chain-grapple-damage", 2.0) * 3;
        double thrustDamage = seal(stage, 0.8);
        Vector aimed = target.position();
        Victim[] hooked = new Victim[1];
        Vector[] chainHead = new Vector[1];
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, AIM, FLING, Ease.IN_OUT);
        t.span(0, AIM, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, stage.feet(), aimed, 3, p);
            Vector hand = stage.body().leftHand();
            fx.draw(Shapes.helix(hand.clone().subtract(new Vector(0, 1, 0)), 0.8, 2, 2, 12, tick * 0.5),
                    fx.dust(Palette.EMBER, 1.2f));
        });
        t.at(AIM - 4, () -> fx.sound(stage.feet(), Sfx.CHAIN_BREAK, 2f, 0.5f));

        // The chain flies out link by link.
        t.span(AIM, AIM + THROW, (tick, p) -> {
            Vector hand = stage.body().leftHand();
            Vector to = target.chest();
            chainHead[0] = hand.clone().add(to.clone().subtract(hand).multiply(Math.min(1, (tick + 1.0) / THROW)));
            drawChain(fx, hand, chainHead[0], tick);
            fx.cloud(Particle.FLAME, chainHead[0], 3, 0.2, 0.02);
            if (tick == THROW - 1 && chainHead[0].distance(target.chest()) < 4) {
                hooked[0] = target;
                stage.damage(target, hookDamage);
                target.effect(Affliction.SLOWNESS, 40, 2);
                fx.impact(target.chest(), Palette.EMBER, 1.5);
                fx.sound(target.position(), Sfx.CHAIN_BREAK, 2f, 1.2f);
            }
        });

        tweenTo(t, stage, AIM + THROW, AIM + THROW + REEL, HAUL, Ease.IN);
        t.span(AIM + THROW, AIM + THROW + REEL, (tick, p) -> {
            if (hooked[0] == null) return;
            Vector hand = stage.body().leftHand();
            Vector victimAt = hooked[0].chest();
            drawChain(fx, hand, victimAt, tick);
            Vector pull = stage.feet().add(stage.forward().multiply(5)).subtract(hooked[0].position());
            if (pull.lengthSquared() > 4) hooked[0].fling(pull.normalize().multiply(0.9).setY(0.15));
            if (tick % 4 == 0) fx.sound(victimAt, Sfx.CHAIN_BREAK, 1f, 0.8f);
        });

        int stab = AIM + THROW + REEL;
        tween(t, stage, stab, stab + 3, HAUL, Poses.THRUST, Ease.OUT_BACK);
        t.at(stab + 2, () -> {
            Vector tip = stage.body().spearTip();
            fx.line(stage.body().rightHand(), tip.clone().add(stage.forward().multiply(3)), 0.3,
                    fx.dust(Palette.HOLY, 1.6f).and(fx.particle(Particle.CRIT)));
            fx.sound(tip, Sfx.PLAYER_ATTACK_STRONG, 2f, 0.5f);
            Area front = Area.cone(stage.feet(), stage.forward(), Math.toRadians(35), 12, 6);
            stage.hit(front, thrustDamage, victim -> {
                victim.fling(stage.forward().multiply(1.8).setY(1.0));
                fx.impact(victim.chest(), Palette.HOLY, 2);
            });
        });
        recover(t, stage, stab + 8, stab + 24, Poses.GUARD);
        return t;
    }

    private static void drawChain(Fx fx, Vector from, Vector to, int tick) {
        int link = 0;
        for (Vector point : Shapes.line(from, to, 0.45)) {
            fx.dust(link % 2 == 0 ? IRON : Palette.EMBER, link % 2 == 0 ? 1.4f : 0.9f).at(point);
            link++;
        }
    }

    @Override
    public String getName() {
        return "chaingrapple";
    }
}
