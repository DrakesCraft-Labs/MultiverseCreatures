package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Bolts;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain Lightning: crackling rings mark every player in reach, then a bolt leaves the spear and
 * jumps from one marked player to the next, nearest first, losing strength with each jump.
 */
public class ChainLightningAttack extends ChoreographedAttack.Ranged {

    private static final int MARK = 20;
    private static final int JUMP = 3;
    private static final int MAX_JUMPS = 5;

    public ChainLightningAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        List<Victim> chain = order(stage);
        if (chain.isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.75);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, MARK, Poses.SPEAR_RAISED, Ease.OUT);
        t.span(0, MARK, (tick, p) -> {
            Vector tip = stage.body().spearTip();
            fx.cloud(Particle.ELECTRIC_SPARK, tip, 5, 0.5, 0.1);
            if (tick % 3 == 0) Bolts.bolt(fx, stage.body().rightHand(), tip, stage.random());
            for (Victim victim : chain) {
                fx.draw(Shapes.circle(victim.chest(), 1.2, 10, Shapes.FLAT_U, Shapes.FLAT_V, tick * 0.4),
                        fx.dust(Palette.mix(Palette.STORM, Palette.ICE, p), 1.2f));
            }
        });
        t.at(0, () -> fx.sound(stage.feet(), Sfx.BEACON_POWER, 2f, 1.6f));
        t.at(MARK - 6, () -> fx.sound(stage.feet(), Sfx.TRIDENT_THUNDER, 2f, 1.4f));

        for (int i = 0; i < chain.size(); i++) {
            int index = i;
            double hitDamage = damage * Math.pow(0.8, i);
            t.span(MARK + i * JUMP, MARK + i * JUMP + JUMP, (tick, p) -> {
                Vector from = index == 0 ? stage.body().spearTip() : chain.get(index - 1).chest();
                Bolts.bolt(fx, from, chain.get(index).chest(), stage.random());
                if (tick == 0) {
                    Victim victim = chain.get(index);
                    stage.damage(victim, hitDamage);
                    victim.effect(Affliction.SLOWNESS, 30, 2);
                    fx.flash(victim.chest(), Palette.ICE);
                    fx.burst(victim.chest(), Particle.ELECTRIC_SPARK, 20, 0.4);
                    fx.sound(victim.position(), Sfx.LIGHTNING_IMPACT, 1.5f, 1.2f + index * 0.1f);
                }
            });
        }
        int end = MARK + chain.size() * JUMP;
        tween(t, stage, MARK, MARK + 4, Poses.SPEAR_RAISED, Poses.CAST_FORWARD, Ease.OUT);
        recover(t, stage, end + 2, end + 16, Poses.GUARD);
        return t;
    }

    /** The chain's order: the nearest player to the boss, then each next nearest to the last. */
    private static List<Victim> order(Stage stage) {
        List<Victim> left = new ArrayList<>(stage.victims());
        List<Victim> chain = new ArrayList<>();
        Vector from = stage.feet();
        while (!left.isEmpty() && chain.size() < MAX_JUMPS) {
            Victim nearest = null;
            for (Victim victim : left) {
                if (nearest == null || victim.position().distanceSquared(from) < nearest.position().distanceSquared(from)) {
                    nearest = victim;
                }
            }
            if (!chain.isEmpty() && nearest.position().distance(from) > 16) break;
            chain.add(nearest);
            left.remove(nearest);
            from = nearest.position();
        }
        return chain;
    }

    @Override
    public String getName() {
        return "chainlightning";
    }
}
