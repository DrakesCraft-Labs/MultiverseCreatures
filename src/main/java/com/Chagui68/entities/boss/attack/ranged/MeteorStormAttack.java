package com.Chagui68.entities.boss.attack.ranged;

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
 * Meteor Storm: the Sentinel calls down the sky. Burning circles open on the floor, and blocks of
 * magma come screaming in at an angle, trailing fire and smoke, one after another.
 */
public class MeteorStormAttack extends ChoreographedAttack.Ranged {

    private static final int CALL = 20;
    private static final int METEORS = 9;
    private static final int GAP = 4;
    private static final int FALL = 14;
    private static final double RADIUS = 3.5;

    public MeteorStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        double damage = seal(stage, 0.7);
        Vector feet = stage.feet();
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        List<Vector> spots = new ArrayList<>();
        for (Victim victim : stage.victims()) {
            if (spots.size() >= METEORS / 2) break;
            spots.add(stage.onGround(victim.position()));
        }
        while (spots.size() < METEORS) {
            double angle = stage.random().nextDouble() * Math.PI * 2;
            spots.add(stage.onGround(feet.clone().add(Shapes.heading(angle).multiply(8 + stage.random().nextDouble() * 18))));
        }
        Vector incoming = Shapes.heading(stage.random().nextDouble() * Math.PI * 2).multiply(0.6).setY(-1).normalize();

        tweenTo(t, stage, 0, CALL, Poses.CAST_SKY, Ease.OUT);
        t.span(0, CALL, (tick, p) -> {
            Vector hands = stage.body().rightHand().midpoint(stage.body().leftHand());
            fx.line(hands, hands.clone().add(new Vector(0, 25, 0)), 1.0, fx.dust(Palette.EMBER, 1.6f).sometimes(0.6));
            fx.ring(feet.clone().add(new Vector(0, 30, 0)), 6 + p * 10, 1.2, tick * 0.1,
                    fx.particle(Particle.LARGE_SMOKE).and(fx.dust(Palette.EMBER, 2.0f)).sometimes(0.5));
        });
        t.at(0, () -> fx.sound(feet, Sfx.BLAZE_AMBIENT, 3f, 0.4f));
        t.at(8, () -> fx.sound(feet, Sfx.WITHER_SPAWN, 1.5f, 1.4f));

        for (int i = 0; i < METEORS; i++) {
            Vector spot = spots.get(i);
            int land = CALL + FALL + i * GAP;
            int start = land - FALL;
            Vector sky = spot.clone().subtract(incoming.clone().multiply(36));
            Prop[] rock = new Prop[1];
            t.span(start - 12, land, (tick, p) -> {
                if (tick % 2 == 0) Telegraph.circle(stage, spot, RADIUS, Math.min(1, tick / (FALL + 11.0)));
            });
            t.at(start, () -> {
                rock[0] = stage.block(Material.MAGMA_BLOCK, sky, 2.6f, new org.joml.Quaternionf().rotationXYZ(0.4f, 0.7f, 0.2f));
                props.add(rock[0]);
                fx.sound(sky, Sfx.GHAST_SHOOT, 2f, 0.5f);
            });
            t.span(start, land, (tick, p) -> {
                Vector at = sky.clone().add(spot.clone().subtract(sky).multiply(Ease.at(Ease.IN, p)));
                if (rock[0] != null) rock[0].moveTo(at, 1);
                fx.cloud(Particle.FLAME, at, 6, 0.8, 0.03);
                fx.cloud(Particle.LARGE_SMOKE, at.clone().subtract(incoming.clone().multiply(2)), 4, 0.7, 0.02);
                fx.dust(Palette.MOLTEN, 2.4f, 0.8, 3).at(at);
            });
            t.at(land, () -> {
                if (rock[0] != null) rock[0].remove();
                fx.impact(spot.clone().add(new Vector(0, 1, 0)), Palette.EMBER, 3.5);
                fx.flatBurst(spot, Particle.FLAME, 24, 0.4);
                fx.draw(Shapes.ring(spot, RADIUS, 0.5, 0), fx.crumble(stage.groundMaterial(spot), 3, 0.3));
                for (int k = 0; k < 4; k++) {
                    Vector up = Shapes.heading(k * Math.PI / 2 + 0.4).multiply(0.25).setY(0.6);
                    stage.debris(spot.clone().add(new Vector(0, 0.5, 0)), up, Material.MAGMA_BLOCK, 24);
                }
                fx.sound(spot, Sfx.EXPLODE, 2.5f, 0.7f);
                stage.hit(Area.cylinder(spot, RADIUS, 1, 4), damage, victim -> {
                    victim.ignite(80);
                    Vector away = victim.position().subtract(spot).setY(0);
                    if (away.lengthSquared() > 1e-6) away.normalize();
                    victim.push(away.multiply(0.7).setY(0.6));
                });
            });
        }
        int end = CALL + FALL + METEORS * GAP;
        recover(t, stage, end - 4, end + 12, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "meteorstorm";
    }
}
