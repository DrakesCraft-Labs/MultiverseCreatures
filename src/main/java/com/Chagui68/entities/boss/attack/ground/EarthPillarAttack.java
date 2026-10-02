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
import com.Chagui68.entities.boss.fx.Victim;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Earth Pillar: the Sentinel crouches with its hands over the floor; the ground cracks and glows
 * under every player, then obsidian pillars burst out and throw them into the air.
 */
public class EarthPillarAttack extends ChoreographedAttack.Ground {

    private static final int CROUCH = 14;
    private static final int WARN = 20;
    private static final double RADIUS = 2.6;
    private static final int MAX_PILLARS = 6;

    public EarthPillarAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.8);
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        List<Vector> spots = new ArrayList<>();
        for (Victim victim : stage.victims()) {
            if (spots.size() >= MAX_PILLARS) break;
            spots.add(stage.onGround(victim.position()));
        }
        if (spots.isEmpty()) spots.add(stage.onGround(stage.feet().add(stage.forward().multiply(12))));

        tweenTo(t, stage, 0, CROUCH, Poses.CAST_GROUND, Ease.IN_OUT);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.EVOKER_CAST, 2f, 0.5f));
        t.span(0, CROUCH + WARN, (tick, p) -> {
            for (Vector hand : new Vector[]{stage.body().rightHand(), stage.body().leftHand()}) {
                fx.line(hand, stage.onGround(hand), 0.5, fx.dust(Palette.MOLTEN, 1.2f).sometimes(0.5));
            }
            if (tick % 2 != 0) return;
            double progress = Math.min(1, (double) tick / (CROUCH + WARN));
            for (Vector spot : spots) {
                Telegraph.circle(stage, spot, RADIUS, progress);
                // Cracks running out from the spot as it charges.
                for (int i = 0; i < 4; i++) {
                    Vector dir = Shapes.heading(i * Math.PI / 2 + tick * 0.1);
                    fx.line(spot, spot.clone().add(dir.multiply(RADIUS * progress)), 0.4,
                            fx.dust(Palette.EMBER, 1.0f));
                }
                fx.crumble(stage.groundMaterial(spot), 3, RADIUS * 0.5).at(spot);
            }
        });
        t.at(CROUCH + WARN - 6, () -> fx.sound(stage.feet(), Sfx.RESPAWN_ANCHOR_CHARGE, 2f, 0.5f));

        int burst = CROUCH + WARN;
        for (Vector spot : spots) {
            pillar(t, stage, burst, spot, Material.OBSIDIAN, 2.4f, 8, 3, 34, props);
            pillar(t, stage, burst + 1, spot.clone().add(new Vector(1.6, 0, 0.8)), Material.CRYING_OBSIDIAN, 1.2f, 5, 3, 32, props);
            t.at(burst + 1, () -> {
                fx.burst(spot.clone().add(new Vector(0, 1, 0)), Particle.LAVA, 12, 0.6);
                fx.cloud(Particle.LARGE_SMOKE, spot, 20, 1.5, 0.08);
                fx.sound(spot, Sfx.WITHER_BREAK_BLOCK, 2f, 0.6f);
                fx.sound(spot, Sfx.EXPLODE, 1.5f, 0.9f);
                stage.hit(Area.cylinder(spot, RADIUS + 0.4, 1, 3), damage,
                        victim -> victim.fling(new Vector(0, 1.6, 0)));
            });
        }
        tween(t, stage, burst - 2, burst + 2, Poses.CAST_GROUND, Poses.CAST_SKY, Ease.OUT_BACK);
        recover(t, stage, burst + 10, burst + 26, Poses.GUARD);
        t.hold(burst + 52);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return CROUCH + WARN + 26;
    }

    @Override
    public String getName() {
        return "earthpillar";
    }
}
