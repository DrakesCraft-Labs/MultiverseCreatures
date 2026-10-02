package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
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
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Obsidian Prison: the Sentinel points its spear at each player in turn and a circle of runes
 * opens under their feet. A moment later a cage of obsidian spikes bursts up around the circle,
 * leaning inwards, and then the cage collapses on itself in a spray of shards. Getting out of the
 * circle before the bars close is the whole trick; whoever is still inside when it falls is crushed.
 */
public class ObsidianPrisonAttack extends ChoreographedAttack.Ranged {

    private static final int AIM = 16;
    private static final int WARN = 22;
    private static final int RISE = 6;
    private static final int HOLD = 26;
    private static final int BARS = 10;
    private static final double RADIUS = 3.5;
    private static final int MAX_CAGES = 4;

    public ObsidianPrisonAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        if (stage.victims().isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = stage.config("entities.armor-stand-boss.obsidian-prison-damage", 16.0);
        List<Victim> victims = stage.victims();
        int cages = Math.min(MAX_CAGES, victims.size());
        List<Vector> centers = new ArrayList<>();
        for (int i = 0; i < cages; i++) centers.add(stage.onGround(victims.get(i).position()));
        Timeline t = new Timeline();
        List<Prop> props = props(t);

        tweenTo(t, stage, 0, AIM, Poses.CAST_FORWARD.withRightArm(-95, 0, 5), Ease.OUT_BACK);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.EVOKER_PREPARE_SUMMON, 2.5f, 0.7f));
        t.span(0, AIM, (tick, p) -> {
            for (Vector center : centers) {
                if (tick % 3 == 0) fx.line(stage.body().spearTip(), center.clone().add(new Vector(0, 1, 0)), 1.2,
                        fx.dust(Palette.AMETHYST, 0.9f).sometimes(0.5));
            }
            fx.draw(Shapes.sphere(stage.body().spearTip(), 0.3 + p * 0.6, 10), fx.dust(Palette.VOID, 1.4f));
        });

        for (int c = 0; c < cages; c++) {
            Vector center = centers.get(c);
            int open = AIM + c * 3;
            int close = open + WARN;
            int fall = close + RISE + HOLD;
            List<Prop> bars = new ArrayList<>();
            t.at(open, () -> {
                fx.sound(center, Sfx.AMETHYST_CHIME, 2f, 0.6f);
                fx.flash(center.clone().add(new Vector(0, 0.5, 0)), Palette.AMETHYST);
            });
            t.span(open, close, (tick, p) -> {
                if (tick % 2 != 0) return;
                Telegraph.circle(stage, center, RADIUS, p);
                fx.draw(Shapes.star(center.clone().add(new Vector(0, 0.15, 0)), RADIUS * 0.85, 5, 2, tick * 0.06, 0.45,
                        Shapes.FLAT_U, Shapes.FLAT_V), fx.dust(Palette.AMETHYST, 1.1f));
                for (int i = 0; i < BARS; i++) {
                    Vector foot = barFoot(center, i);
                    fx.cloud(Particle.REVERSE_PORTAL, foot, 1, 0.1, 0.02);
                }
            });
            // The bars burst up, leaning in over the circle.
            t.at(close, () -> {
                for (int i = 0; i < BARS; i++) {
                    Vector foot = stage.onGround(barFoot(center, i));
                    double angle = i * Math.PI * 2 / BARS;
                    Quaternionf lean = lean(angle, 0.35f);
                    Prop bar = stage.block(i % 2 == 0 ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN, foot, 0.6f, lean);
                    bar.resize(0.6f, 0.05f, lean, 0);
                    bar.resize(0.6f, 5.5f, lean, RISE);
                    bars.add(bar);
                    props.add(bar);
                    fx.crumble(stage.groundMaterial(foot), 8, 0.3).at(foot);
                }
                fx.sound(center, Sfx.POINTED_DRIPSTONE_LAND, 3f, 0.5f);
                fx.sound(center, Sfx.WITHER_BREAK_BLOCK, 2f, 0.8f);
                fx.flatBurst(center, Particle.CLOUD, 20, 0.3);
                // Rising bars knock back anyone standing right on the edge.
                stage.hit(Area.ring(center, RADIUS - 0.8, RADIUS + 0.8, 3), damage * 0.3,
                        victim -> victim.push(Shapes.flat(victim.position().subtract(center)).normalize().multiply(0.6).setY(0.5)));
            });
            t.span(close + RISE, fall, (tick, p) -> {
                // Inside, the air thickens: slowing runes and a tightening ring.
                if (tick % 3 == 0) {
                    fx.ring(center.clone().add(new Vector(0, 1 + p * 3, 0)), RADIUS * (1 - p * 0.6), 0.6, tick * 0.2,
                            fx.dust(Palette.VOID, 1.3f));
                    fx.cloud(Particle.PORTAL, center.clone().add(new Vector(0, 1.5, 0)), 4, RADIUS * 0.5, 0.3);
                }
                if (tick % 10 == 0) {
                    stage.hit(Area.cylinder(center, RADIUS - 0.5, 1, 4), 0, victim -> victim.effect(Affliction.SLOWNESS, 15, 2));
                    fx.sound(center, Sfx.WARDEN_HEARTBEAT, 1.5f, 1.2f);
                }
            });
            // The cage falls in.
            t.at(fall - 4, () -> {
                for (int i = 0; i < bars.size(); i++) bars.get(i).resize(0.6f, 5.5f, lean(i * Math.PI * 2 / BARS, 0.95f), 4);
            });
            t.at(fall, () -> {
                Vector mid = center.clone().add(new Vector(0, 1.2, 0));
                for (Prop bar : bars) {
                    fx.crumble(Material.OBSIDIAN, 14, 0.5).at(bar.position().clone().add(new Vector(0, 1, 0)));
                    Vector out = Shapes.flat(bar.position().subtract(center)).normalize().multiply(0.25).setY(0.5);
                    stage.debris(bar.position().clone().add(new Vector(0, 1, 0)), out, Material.CRYING_OBSIDIAN, 20);
                    bar.remove();
                }
                bars.clear();
                fx.impact(mid, Palette.AMETHYST, 2.5);
                fx.burst(mid, Particle.PORTAL, 40, 0.6);
                fx.sound(mid, Sfx.GLASS_BREAK, 3f, 0.5f);
                fx.sound(mid, Sfx.AMETHYST_BREAK, 3f, 0.6f);
                stage.hit(Area.cylinder(center, RADIUS, 1, 5), damage, victim -> {
                    victim.effect(Affliction.SLOWNESS, 60, 2);
                    victim.effect(Affliction.MINING_FATIGUE, 80, 1);
                });
            });
        }
        recover(t, stage, AIM + 10, AIM + 26, Poses.GUARD);
        return t;
    }

    /** Tilts a bar standing on the circle at {@code angle} in towards the centre by {@code tilt} radians. */
    private static Quaternionf lean(double angle, float tilt) {
        return new Quaternionf().rotationAxis(tilt, (float) -Math.sin(angle), 0, (float) Math.cos(angle));
    }

    private static Vector barFoot(Vector center, int i) {
        return center.clone().add(Shapes.heading(i * Math.PI * 2 / BARS).multiply(RADIUS));
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return AIM + 26;
    }

    @Override
    public String getName() {
        return "obsidianprison";
    }
}
