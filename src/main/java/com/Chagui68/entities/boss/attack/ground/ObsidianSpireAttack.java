package com.Chagui68.entities.boss.attack.ground;

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
 * Obsidian Spire: the spear is brought down on the floor and a line of obsidian spires erupts from
 * the impact towards the target, one after another, each a little taller than the last.
 */
public class ObsidianSpireAttack extends ChoreographedAttack.Ground {

    private static final int WIND = 16;
    private static final int SPIRES = 10;
    private static final double SPACING = 3.2;

    public ObsidianSpireAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.obsidian-spire-damage", 12.0);
        Vector feet = stage.feet();
        Vector dir = Shapes.flat(aimAt(stage, stage.target(), 20).subtract(feet));
        Vector first = feet.clone().add(dir.clone().multiply(5));
        Vector last = first.clone().add(dir.clone().multiply(SPACING * (SPIRES - 1)));
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, WIND, Poses.SPEAR_OVERHEAD, Ease.IN_OUT);
        t.span(0, WIND, (tick, p) -> {
            if (tick % 2 == 0) Telegraph.line(stage, first, last, 3.4, p);
            fx.draw(Shapes.sphere(stage.body().spearTip(), 0.5 + p, 10), fx.dust(Palette.VOID, 1.5f));
        });
        t.at(0, () -> fx.sound(feet, Sfx.EVOKER_CAST, 2f, 0.6f));

        tween(t, stage, WIND, WIND + 4, Poses.SPEAR_OVERHEAD, Poses.SPEAR_SLAM, Ease.OUT_BACK);
        t.at(WIND + 3, () -> {
            Vector hit = spearGround(stage);
            fx.impact(hit.clone().add(new Vector(0, 0.5, 0)), Palette.VOID, 2.5);
            fx.sound(hit, Sfx.MACE_SMASH_GROUND, 2.5f, 0.7f);
        });
        for (int i = 0; i < SPIRES; i++) {
            int at = WIND + 4 + i * 2;
            Vector spot = stage.onGround(first.clone().add(dir.clone().multiply(SPACING * i))).subtract(new Vector(0, 0.15, 0));
            double height = 4 + i * 0.6;
            pillar(t, stage, at, spot, i % 3 == 2 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN, 1.8f, height, 2, 30 - i, props);
            t.at(at + 1, () -> {
                fx.cloud(Particle.REVERSE_PORTAL, spot.clone().add(new Vector(0, height * 0.5, 0)), 10, 0.6, 0.05);
                fx.sound(spot, Sfx.POINTED_DRIPSTONE_LAND, 2f, 0.5f);
                stage.hit(Area.cylinder(spot, 2.2, 1, height), damage, victim -> {
                    victim.fling(dir.clone().multiply(0.4).setY(1.3));
                    fx.impact(victim.chest(), Palette.VOID, 1.2);
                });
            });
        }
        recover(t, stage, WIND + 12, WIND + 28, Poses.GUARD);
        t.hold(WIND + 4 + SPIRES * 2 + 50);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return WIND + 30;
    }

    @Override
    public String getName() {
        return "obsidianspire";
    }
}
