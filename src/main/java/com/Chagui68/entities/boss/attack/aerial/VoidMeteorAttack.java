package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Missile;
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
import org.joml.Quaternionf;

import java.util.List;

/**
 * Void Meteor: hovering, the Sentinel lifts both arms and pulls a boulder of obsidian together out
 * of nothing above its head, wrapped in a churning shell of void. It hurls it at its target; the
 * meteor crashes down, sends a shockwave rolling out across the floor and leaves a crater of void
 * that blinds and saps whoever lingers in it.
 */
public class VoidMeteorAttack extends ChoreographedAttack.Aerial {

    private static final int FORM = 36;
    private static final int FLIGHT = 22;
    private static final int CRATER = 70;
    private static final double CRATER_RADIUS = 5.5;

    public VoidMeteorAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Victim target = stage.target();
        if (target == null) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.void-meteor-damage", 18.0);
        Timeline t = new Timeline();
        List<Prop> props = props(t);
        Prop[] rock = new Prop[1];
        Vector[] landing = {stage.onGround(target.position())};
        Vector[] crater = new Vector[1];
        double[] spin = {0};

        // Form: both arms up, the boulder growing over the head.
        tweenTo(t, stage, 0, 14, Poses.CAST_SKY, Ease.OUT_BACK);
        t.at(0, () -> {
            fx.sound(stage.feet(), Sfx.END_PORTAL_SPAWN, 2f, 0.6f);
            rock[0] = stage.block(Material.CRYING_OBSIDIAN, above(stage), 0.2f, new Quaternionf());
            rock[0].glow(Palette.AMETHYST);
            props.add(rock[0]);
        });
        t.span(1, FORM, (tick, p) -> {
            spin[0] += 0.12;
            Vector core = above(stage);
            float size = (float) (0.2 + 3.3 * Ease.at(Ease.OUT, p));
            rock[0].moveTo(core.clone().subtract(new Vector(0, size / 2, 0)), 1);
            rock[0].reshape(size, new Quaternionf().rotationXYZ((float) spin[0], (float) spin[0] * 0.7f, 0.3f), 1);
            fx.draw(Shapes.sphere(core, size * 0.9 + 0.6, 22), fx.dust(Palette.VOID_DEEP, 2.0f).sometimes(0.7));
            fx.ring(core, size + 1.2, 0.7, spin[0] * 2, fx.dust(Palette.AMETHYST, 1.2f));
            if (tick % 3 == 0) fx.gather(core, 9, 6, Palette.VOID, 12);
            fx.cloud(Particle.REVERSE_PORTAL, core, 3, size * 0.6, 0.05);
            landing[0] = stage.onGround(target.position());
            if (tick % 2 == 0) Telegraph.circle(stage, landing[0], CRATER_RADIUS + 3, p * 0.8);
        });
        t.at(FORM - 10, () -> fx.sound(stage.feet(), Sfx.WARDEN_SONIC_CHARGE, 2.5f, 0.5f));

        // Throw.
        tween(t, stage, FORM, FORM + 4, Poses.CAST_SKY, Poses.THROW.withLeftArm(-60, 0, -20), Ease.OUT_BACK);
        t.at(FORM, () -> {
            Vector from = above(stage);
            Vector velocity = landing[0].clone().subtract(from).multiply(1.0 / FLIGHT);
            fx.sound(from, Sfx.WITHER_SHOOT, 3f, 0.5f);
            fx.sound(from, Sfx.DRAGON_SHOOT, 2f, 0.6f);
            Missile meteor = new Missile(from, velocity, 2.5)
                    .carrying(rock[0])
                    .look((at, dir, age) -> {
                        fx.draw(Shapes.sphere(at, 2.6, 18), fx.dust(Palette.VOID_DEEP, 2.2f).sometimes(0.6));
                        fx.cloud(Particle.SOUL_FIRE_FLAME, at, 6, 1.4, 0.05);
                        fx.trail(at, at.clone().subtract(dir.clone().multiply(6)), Palette.AMETHYST, 8);
                        fx.cloud(Particle.LARGE_SMOKE, at.clone().subtract(dir.clone().multiply(2)), 3, 0.8, 0.02);
                        Telegraph.circle(stage, landing[0], CRATER_RADIUS + 3, 0.8 + 0.2 * Math.min(1, age / (double) FLIGHT));
                    })
                    .onHit(victim -> stage.damage(victim, damage))
                    .onBurst(at -> {
                        crater[0] = stage.onGround(at);
                        rock[0].remove();
                        Vector mid = crater[0].clone().add(new Vector(0, 1, 0));
                        fx.impact(mid, Palette.VOID, 4);
                        fx.burst(mid, Particle.EXPLOSION_EMITTER, 1, 0);
                        fx.burst(mid, Particle.SQUID_INK, 50, 0.5);
                        fx.sound(mid, Sfx.EXPLODE, 3f, 0.5f);
                        fx.sound(mid, Sfx.MACE_SMASH_GROUND, 3f, 0.5f);
                        for (int i = 0; i < 14; i++) {
                            Vector out = Shapes.heading(i * Math.PI / 7).multiply(0.35).setY(0.7);
                            stage.debris(mid, out, i % 2 == 0 ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN, 26);
                        }
                        stage.hit(Area.cylinder(crater[0], CRATER_RADIUS, 1, 5), damage, victim -> {
                            victim.fling(Shapes.flat(victim.position().subtract(crater[0])).normalize().multiply(1.2).setY(1.0));
                            victim.effect(Affliction.DARKNESS, 60, 0);
                        });
                    });
            fly(t, stage, FORM + 1, FLIGHT + 10, meteor);
        });
        // Shockwave and the void crater left behind, both from wherever the meteor came down.
        int land = FORM + 1 + FLIGHT;
        java.util.Set<java.util.UUID> struck = new java.util.HashSet<>();
        t.span(land, land + 14, (tick, p) -> {
            if (crater[0] == null) return;
            double radius = CRATER_RADIUS + p * 12;
            fx.draw(Shapes.ring(crater[0], radius, 0.8, tick), fx.dust(Palette.AMETHYST, 2f).and(fx.crumble(Material.OBSIDIAN, 1, 0.2).sometimes(0.4)));
            for (Victim victim : stage.victimsIn(Area.ring(crater[0], radius - 1.2, radius + 1.2, 1.5))) {
                if (!struck.add(victim.id())) continue;
                stage.damage(victim, damage * 0.4);
                victim.push(new Vector(0, 0.6, 0));
            }
        });
        t.span(land, land + CRATER, (tick, p) -> {
            if (crater[0] == null) return;
            double radius = CRATER_RADIUS * (p > 0.85 ? (1 - p) / 0.15 : 1);
            if (tick % 2 == 0) {
                fx.disc(crater[0].clone().add(new Vector(0, 0.1, 0)), radius, 0.9, fx.dust(Palette.VOID_DEEP, 1.8f));
                fx.ring(crater[0].clone().add(new Vector(0, 0.2, 0)), radius, 0.6, -tick * 0.1, fx.dust(Palette.AMETHYST, 1.3f));
            }
            fx.cloud(Particle.REVERSE_PORTAL, crater[0].clone().add(new Vector(0, 0.5, 0)), 3, radius * 0.6, 0.02);
            if (tick % 10 == 0) {
                stage.hit(Area.cylinder(crater[0], radius, 1, 3), damage * 0.15, victim -> {
                    victim.effect(Affliction.BLINDNESS, 30, 0);
                    victim.effect(Affliction.WEAKNESS, 40, 0);
                });
            }
        });
        tweenTo(t, stage, FORM + 8, FORM + 24, Poses.HOVER, Ease.IN_OUT);
        return t;
    }

    private static Vector above(Stage stage) {
        return stage.body().head().add(new Vector(0, 5, 0));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return FORM + 24;
    }

    @Override
    public String getName() {
        return "voidmeteor";
    }
}
