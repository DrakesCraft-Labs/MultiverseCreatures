package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Healing Circle: the Sentinel goes down on one knee inside a circle of green runes and draws life up
 * out of the ground — threads of light rising into it — healing a little every tick for ten seconds.
 * Interrupting it means getting inside the circle and hurting it while it kneels.
 */
public class HealingCircleAttack extends ChoreographedAttack {

    private static final int KNEEL = 30;
    private static final int CHANNEL = 200;
    private static final double RADIUS = 6;
    private static final double MAX_HEAL = 0.03;
    private static final double HEAL_PER_TICK = 0.0015;
    private static final Color LIFE = Color.fromRGB(0x5CFF7A);
    private static final Color LIFE_DEEP = Color.fromRGB(0x1E8C3A);

    public HealingCircleAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return !instance.healingCircleActive && !instance.isFlying;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        BossInstance instance = stage.instance();
        if (instance != null) {
            instance.healingCircleActive = true;
            instance.healingCircleTimer = 0;
            instance.healingCircleHealed = 0;
        }
        Vector ground = stage.onGround(stage.feet());
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, KNEEL, Poses.KNEEL.withLeftArm(-60, 0, -30).withHead(-10, 0, 0), Ease.IN_OUT);
        t.at(0, () -> fx.sound(ground, Sfx.ILLUSIONER_MIRROR, 2f, 1.2f));
        t.span(0, KNEEL + CHANNEL, (tick, p) -> {
            double open = Math.min(1, tick / (double) KNEEL);
            if (tick % 2 == 0) {
                Vector c = ground.clone().add(new Vector(0, 0.15, 0));
                fx.ring(c, RADIUS * open, 0.5, tick * 0.02, fx.dust(LIFE, 1.3f));
                fx.ring(c, RADIUS * 0.75 * open, 0.6, -tick * 0.03, fx.dust(LIFE_DEEP, 1.1f));
                fx.draw(Shapes.star(c, RADIUS * 0.75 * open, 6, 2, tick * 0.02, 0.5, Shapes.FLAT_U, Shapes.FLAT_V), fx.dust(LIFE, 0.9f));
            }
            if (tick < KNEEL) return;
            // Threads of life rising out of the circle into the body.
            Vector chest = stage.body().chest();
            for (int i = 0; i < 3; i++) {
                Vector from = ground.clone().add(Shapes.heading(stage.random().nextDouble() * 6.28).multiply(RADIUS * stage.random().nextDouble()));
                fx.trail(from, chest, LIFE, 14);
            }
            fx.cloud(Particle.HAPPY_VILLAGER, chest, 2, 2, 0);
            if (tick % 12 == 0) fx.cloud(Particle.HEART, chest.clone().add(new Vector(0, 3, 0)), 2, 2, 0);
            if (tick % 40 == 0) fx.sound(ground, Sfx.BEACON_POWER, 1.5f, 1.6f);
            heal(instance);
        });
        t.at(KNEEL, () -> {
            fx.flash(ground.clone().add(new Vector(0, 1, 0)), LIFE);
            fx.sound(ground, Sfx.ENCHANT, 2.5f, 0.8f);
        });
        t.onFinish(() -> {
            if (instance == null) return;
            instance.healingCircleActive = false;
            instance.healingCircleTimer = 0;
        });
        recover(t, stage, KNEEL + CHANNEL, KNEEL + CHANNEL + 18, Poses.GUARD);
        return t;
    }

    /** Heals 0.15% of max health a tick, never past 3% in total nor past full health. */
    private static void heal(BossInstance instance) {
        if (instance == null) return;
        BossPuppet body = instance.stand;
        double max = body.getMaxHealth();
        double room = Math.min(max * MAX_HEAL - instance.healingCircleHealed, max - body.getHealth());
        double amount = Math.min(max * HEAL_PER_TICK, room);
        if (amount <= 0) return;
        body.setHealth(body.getHealth() + amount);
        instance.healingCircleHealed += amount;
        instance.healingCircleTimer++;
        if (instance.bossBar != null) {
            instance.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(body.getHealth(), max));
        }
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return timeline.length();
    }

    @Override
    public String getName() {
        return "healingcircle";
    }
}
