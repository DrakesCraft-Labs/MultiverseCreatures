package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Nova Burst: the Sentinel folds in on itself in the air, blazing brighter, then throws its arms wide
 * and a shell of light expands from it in every direction. It hits once as it passes; the floor
 * shows how far it will reach.
 */
public class NovaBurstAttack extends ChoreographedAttack.Aerial {

    private static final int CURL = 22;
    private static final int EXPAND = 14;
    private static final double REACH = 22;

    public NovaBurstAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.7);
        Vector ground = stage.onGround(stage.feet());
        Set<UUID> struck = new HashSet<>();
        Vector[] core = new Vector[1];
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CURL, Poses.CAST_GROUND.withRightArm(-60, -50, 0).withLeftArm(-60, 50, 0), Ease.IN);
        t.span(0, CURL, (tick, p) -> {
            Vector chest = stage.body().chest();
            if (tick % 2 == 0) Telegraph.circle(stage, ground, REACH * 0.8, p);
            fx.draw(Shapes.sphere(chest, 4 - p * 2.5, 30), fx.fade(Palette.HOLY, Palette.GOLD, 2.0f));
            if (tick % 3 == 0) fx.gather(chest, 10, 8, Palette.GOLD, 10);
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.BEACON_ACTIVATE, 2.5f, 0.6f));
        t.at(CURL - 8, () -> fx.sound(stage.feet(), Sfx.RESPAWN_ANCHOR_CHARGE, 2.5f, 1.2f));

        tween(t, stage, CURL, CURL + 3, Poses.CAST_GROUND, Poses.SPREAD, Ease.OUT_BACK);
        t.at(CURL, () -> {
            core[0] = stage.body().chest();
            fx.flash(core[0], Palette.HOLY);
            fx.sound(core[0], Sfx.EXPLODE, 3f, 0.6f);
            fx.sound(core[0], Sfx.TOTEM_USE, 1.5f, 0.6f);
        });
        t.span(CURL, CURL + EXPAND, (tick, p) -> {
            double radius = 2 + REACH * Ease.at(Ease.OUT, p);
            int points = (int) Math.min(220, radius * radius * 0.6);
            fx.draw(Shapes.sphere(core[0], radius, points), fx.fade(Palette.HOLY, Palette.EMBER, 2.2f).sometimes(0.8));
            fx.ring(stage.onGround(core[0]).add(new Vector(0, 0.4, 0)), Math.min(radius, REACH), 0.8, 0,
                    fx.particle(Particle.FLAME).sometimes(0.5));
            for (Victim victim : stage.victims()) {
                double d = victim.chest().distance(core[0]);
                if (d > radius + 1.5 || d < radius - 4 || !struck.add(victim.id())) continue;
                stage.damage(victim, damage);
                Vector away = victim.chest().subtract(core[0]);
                if (away.lengthSquared() > 1e-6) away.normalize();
                victim.fling(away.multiply(1.3).setY(0.6));
                victim.ignite(60);
                victim.effect(Affliction.BLINDNESS, 20, 0);
            }
        });
        tweenTo(t, stage, CURL + EXPAND, CURL + EXPAND + 14, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    @Override
    public String getName() {
        return "novaburst";
    }
}
