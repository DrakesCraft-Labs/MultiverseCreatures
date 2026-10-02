package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * War Stomp: the Sentinel lifts a leg while the floor around it glows, then brings it down and three
 * shockwaves roll out. Each wave hits once; jumping it is the counter.
 */
public class WarStompAttack extends ChoreographedAttack {

    private static final int LIFT = 22;
    private static final double[] WAVES = {14, 20, 26};

    public WarStompAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return !instance.isFlying;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        double damage = stage.config("entities.armor-stand-boss.war-stomp-damage", 8.0);
        Vector center = stage.feet();
        Fx fx = stage.fx();
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, LIFT, Poses.STOMP_LIFT, Ease.IN_OUT);
        t.span(0, LIFT, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.circle(stage, center, WAVES[0], p);
            if (tick % 4 == 0) {
                for (int i = 1; i < WAVES.length; i++) Telegraph.ring(stage, center, WAVES[i] - 1, WAVES[i], p);
            }
            Vector foot = stage.body().rightFoot();
            fx.cloud(Particle.SMALL_FLAME, foot, 2, 0.4, 0.02);
            fx.draw(com.Chagui68.entities.boss.fx.Shapes.ring(foot, 1.2 + p, 0.5, tick * 0.4),
                    fx.dust(Palette.mix(Palette.MOLTEN, Palette.EMBER, p), 1.4f));
        });
        t.at(0, () -> fx.sound(center, Sfx.WARDEN_ROAR, 2.5f, 0.6f));
        t.at(12, () -> fx.sound(center, Sfx.ANVIL_LAND, 1.5f, 0.5f));

        tween(t, stage, LIFT, LIFT + 4, Poses.STOMP_LIFT, Poses.STOMP_DOWN, Ease.OUT_BACK);
        t.at(LIFT + 2, () -> {
            Vector foot = stage.onGround(stage.body().rightFoot());
            fx.impact(foot.clone().add(new Vector(0, 1, 0)), Palette.MOLTEN, 4);
            fx.flatBurst(foot, Particle.CLOUD, 48, 0.9);
            fx.flatBurst(foot, Particle.FLAME, 24, 0.5);
            fx.sound(center, Sfx.EXPLODE, 3f, 0.5f);
            fx.sound(center, Sfx.MACE_SMASH_GROUND, 2f, 0.4f);
        });
        for (int i = 0; i < WAVES.length; i++) {
            double waveDamage = damage * (1.0 - i * 0.2);
            shockwave(t, stage, LIFT + 2 + i * 6, 16 + i * 2, center, WAVES[i],
                    i == 0 ? Palette.EMBER : Palette.mix(Palette.EMBER, Palette.ASH, i / 2.0), waveDamage, victim -> {
                        Vector away = victim.position().subtract(center).setY(0);
                        if (away.lengthSquared() > 1e-6) away.normalize();
                        victim.push(away.multiply(0.8).setY(0.6));
                        victim.effect(Affliction.SLOWNESS, 50, 1);
                    });
        }
        recover(t, stage, LIFT + 18, LIFT + 34, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "warstomp";
    }
}
