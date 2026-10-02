package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Telegraph;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Heavenly Judgment: a seal of gold opens in the sky over the target and a colossal sword of light
 * lowers out of it, point down. It hangs, then falls. The blade lands in the centre of the seal; the
 * shockwave of light carries further.
 */
public class HeavenlyJudgmentAttack extends ChoreographedAttack.Aerial {

    private static final int SUMMON = 34;
    private static final int DROP = 5;
    private static final double INNER = 5;
    private static final double OUTER = 11;
    private static final double HEIGHT = 26;

    public HeavenlyJudgmentAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 1.3);
        Vector ground = stage.onGround(aimAt(stage, stage.target(), 10));
        Vector sky = ground.clone().add(new Vector(0, HEIGHT, 0));
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] sword = new Prop[1];

        tweenTo(t, stage, 0, SUMMON, Poses.SPEAR_RAISED, Ease.OUT);
        t.at(0, () -> fx.sound(sky, Sfx.BELL_RESONATE, 3f, 0.8f));
        t.at(6, () -> {
            sword[0] = spear(stage, Material.GOLDEN_SWORD, sky.clone().add(new Vector(0, 10, 0)), new Vector(0, -1, 0), 0.5f);
            sword[0].glow(Palette.GOLD);
            props.add(sword[0]);
        });
        t.at(7, () -> {
            sword[0].reshape(18f, diagonal(new Vector(0, -1, 0)), 20);
            sword[0].moveTo(sky, 20);
        });
        t.span(0, SUMMON + DROP, (tick, p) -> {
            if (tick % 2 == 0) {
                Telegraph.circle(stage, ground, INNER, Math.min(1, (double) tick / SUMMON));
                Telegraph.ring(stage, ground, OUTER - 0.5, OUTER, Math.min(1, (double) tick / SUMMON));
            }
            double grown = Math.min(1, tick / 12.0);
            Vector seal = sky.clone().add(new Vector(0, 6, 0));
            fx.ring(seal, 9 * grown, 0.8, tick * 0.04, fx.dust(Palette.GOLD, 1.8f));
            fx.ring(seal, 7 * grown, 0.9, -tick * 0.06, fx.dust(Palette.HOLY, 1.4f));
            fx.draw(Shapes.star(seal, 7 * grown, 8, 3, tick * 0.04, 0.8, Shapes.FLAT_U, Shapes.FLAT_V), fx.dust(Palette.GOLD, 1.2f));
            if (tick % 2 == 0) fx.line(stage.body().spearTip(), seal, 1.5, fx.dust(Palette.HOLY, 1.0f).sometimes(0.5));
            fx.cloud(Particle.END_ROD, sky, 3, 3, 0.02);
        });
        t.at(SUMMON - 10, () -> fx.sound(sky, Sfx.BEACON_POWER, 3f, 0.5f));

        t.at(SUMMON, () -> {
            if (sword[0] != null) sword[0].moveTo(ground.clone().add(new Vector(0, 5, 0)), DROP);
            fx.sound(sky, Sfx.TRIDENT_THROW, 3f, 0.4f);
        });
        t.span(SUMMON, SUMMON + DROP, (tick, p) -> fx.line(sky, ground, 1.0, fx.dust(Palette.HOLY, 2.0f).sometimes(0.7)));
        int impact = SUMMON + DROP;
        t.at(impact, () -> {
            fx.flash(ground.clone().add(new Vector(0, 2, 0)), Palette.HOLY);
            fx.impact(ground.clone().add(new Vector(0, 1, 0)), Palette.GOLD, 5);
            for (int i = 0; i < 8; i++) {
                Vector beam = ground.clone().add(Shapes.heading(i * Math.PI / 4).multiply(INNER));
                fx.line(beam, beam.clone().add(new Vector(0, 18, 0)), 0.6, fx.dust(Palette.HOLY, 2.0f));
            }
            fx.sound(ground, Sfx.EXPLODE, 3f, 0.5f);
            fx.sound(ground, Sfx.MACE_SMASH_GROUND, 3f, 0.6f);
            fx.sound(ground, Sfx.TOTEM_USE, 2f, 0.8f);
            stage.hit(Area.cylinder(ground, INNER, 1, 8), damage, victim -> victim.fling(new Vector(0, 1.4, 0)));
        });
        shockwave(t, stage, impact, 14, ground, OUTER, Palette.GOLD, damage * 0.4, victim -> victim.push(new Vector(0, 0.6, 0)));
        t.at(impact + 30, () -> {
            if (sword[0] != null) sword[0].reshape(0.1f, diagonal(new Vector(0, -1, 0)), 12);
            fx.burst(ground.clone().add(new Vector(0, 6, 0)), Particle.END_ROD, 60, 0.6);
        });
        tweenTo(t, stage, impact + 4, impact + 18, Poses.HOVER, Ease.IN_OUT);
        t.hold(impact + 44);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return SUMMON + DROP + 18;
    }

    @Override
    public String getName() {
        return "heavenlyjudgment";
    }
}
