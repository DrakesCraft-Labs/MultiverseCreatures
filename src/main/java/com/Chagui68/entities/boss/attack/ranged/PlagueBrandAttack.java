package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Plague Brand: a festering sigil is thrown at the target and burns over their head. Whoever stands
 * close to a branded player catches the brand too, so the players have to scatter. When it ripens,
 * every brand bursts into a cloud of plague.
 */
public class PlagueBrandAttack extends ChoreographedAttack.Ranged {

    private static final int THROW = 14;
    private static final int RIPEN = 80;
    private static final double SPREAD = 4.5;
    private static final double CLOUD = 4;

    public PlagueBrandAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double hitDamage = stage.config("entities.armor-stand-boss.plague-brand-hit-damage", 4.0);
        double burstDamage = stage.config("entities.armor-stand-boss.plague-brand-damage", 10.0);
        Map<UUID, Victim> branded = new LinkedHashMap<>();
        int[] brandedAt = {-1};
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, THROW, Poses.THROW_COIL.withRightArm(-12, 0, 8).withLeftArm(-160, -40, 0), Ease.IN_OUT);
        t.span(0, THROW, (tick, p) -> {
            Vector hand = stage.body().leftHand();
            fx.draw(Shapes.sphere(hand, 0.5 + p, 12), fx.fade(Palette.PLAGUE, Palette.ASH, 1.6f));
            fx.cloud(Particle.SPORE_BLOSSOM_AIR, hand, 2, 0.5, 0);
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.WITHER_SHOOT, 1.5f, 0.6f));
        tween(t, stage, THROW, THROW + 4, Poses.THROW_COIL.withRightArm(-12, 0, 8).withLeftArm(-160, -40, 0),
                Poses.GUARD.withLeftArm(-50, 10, 0), Ease.OUT);
        t.at(THROW + 1, () -> {
            Vector from = stage.body().leftHand();
            Missile glob = new Missile(from, target.chest().subtract(from).normalize().multiply(1.3), 1.4)
                    .homing(target, 0.2)
                    .look((at, dir, age) -> {
                        fx.draw(Shapes.sphere(at, 0.6, 10), fx.dust(Palette.PLAGUE, 1.6f));
                        fx.cloud(Particle.SNEEZE, at, 2, 0.2, 0.01);
                    })
                    .onHit(victim -> {
                        stage.damage(victim, hitDamage);
                        brand(victim, branded, fx);
                        if (brandedAt[0] < 0) brandedAt[0] = 0;
                    })
                    .onBurst(at -> fx.burst(at, Particle.SNEEZE, 20, 0.2));
            fly(t, stage, THROW + 2, 40, glob);
        });
        recover(t, stage, THROW + 6, THROW + 20, Poses.GUARD);

        int start = THROW + 2;
        t.span(start, start + 40 + RIPEN, (tick, p) -> {
            if (branded.isEmpty()) return;
            brandedAt[0]++;
            double ripeness = Math.min(1, brandedAt[0] / (double) RIPEN);
            for (Victim victim : branded.values().toArray(new Victim[0])) {
                Vector over = victim.eyes().add(new Vector(0, 1.2, 0));
                fx.draw(Shapes.star(over, 0.8, 5, 2, tick * 0.15, 0.25, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.dust(Palette.mix(Palette.PLAGUE, Palette.BLOOD, ripeness), 1.0f));
                fx.ring(victim.position().add(new Vector(0, 0.1, 0)), SPREAD, 0.9, tick * 0.1,
                        fx.dust(Palette.PLAGUE, 0.8f).sometimes(0.5));
                // The brand jumps to anyone standing too close.
                for (Victim near : stage.victimsIn(Area.cylinder(victim.position(), SPREAD, 2, 4))) {
                    if (branded.containsKey(near.id())) continue;
                    brand(near, branded, fx);
                    fx.line(victim.chest(), near.chest(), 0.5, fx.dust(Palette.PLAGUE, 1.2f));
                    fx.sound(near.position(), Sfx.WITHER_SHOOT, 1f, 1.6f);
                }
            }
            if (brandedAt[0] == RIPEN) {
                for (Victim victim : branded.values()) {
                    Vector at = victim.chest();
                    fx.draw(Shapes.sphere(at, CLOUD, 60), fx.fade(Palette.PLAGUE, Palette.ASH, 2.4f));
                    fx.cloud(Particle.SNEEZE, at, 40, CLOUD * 0.5, 0.05);
                    fx.sound(at, Sfx.EXPLODE, 1.5f, 1.3f);
                    stage.hit(Area.sphere(at, CLOUD), burstDamage, hit -> {
                        hit.effect(Affliction.POISON, 100, 1);
                        hit.effect(Affliction.HUNGER, 200, 1);
                    });
                }
                branded.clear();
            }
        });
        return t;
    }

    private static void brand(Victim victim, Map<UUID, Victim> branded, Fx fx) {
        branded.put(victim.id(), victim);
        fx.burst(victim.chest(), Particle.SNEEZE, 12, 0.15);
        victim.effect(Affliction.GLOWING, 100, 0);
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return THROW + 20;
    }

    @Override
    public String getName() {
        return "plaguebrand";
    }
}
