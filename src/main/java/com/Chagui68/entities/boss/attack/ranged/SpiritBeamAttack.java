package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Affliction;
import com.Chagui68.entities.boss.fx.Area;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Spirit Beam: the Sentinel looks up and calls a column of spectral light down on each player in
 * turn. A ring of light marks the spot, then the beam lands from high above and stays for a moment.
 */
public class SpiritBeamAttack extends ChoreographedAttack.Ranged {

    private static final int CALL = 16;
    private static final int WARN = 18;
    private static final int GAP = 8;
    private static final int BEAM = 12;
    private static final double RADIUS = 2.8;

    public SpiritBeamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public Timeline choreograph(Stage stage) {
        List<Vector> spots = new ArrayList<>();
        for (Victim victim : stage.victims()) {
            if (spots.size() >= 5) break;
            spots.add(stage.onGround(victim.position()));
        }
        if (spots.isEmpty()) return null;
        Fx fx = stage.fx();
        double damage = seal(stage, 0.8);
        Timeline t = new Timeline();

        tweenTo(t, stage, 0, CALL, Poses.CAST_SKY, Ease.OUT);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.BEACON_ACTIVATE, 2.5f, 1.2f));
        t.span(0, CALL + spots.size() * GAP + WARN, (tick, p) -> {
            if (tick % 2 == 0) fx.line(stage.body().head(), stage.body().head().add(new Vector(0, 30, 0)), 1.2,
                    fx.dust(Palette.HOLY, 1.4f).sometimes(0.6));
        });

        for (int i = 0; i < spots.size(); i++) {
            Vector spot = spots.get(i);
            int warn = CALL + i * GAP;
            int strike = warn + WARN;
            t.span(warn, strike, (tick, p) -> {
                if (tick % 2 == 0) Telegraph.circle(stage, spot, RADIUS, p);
                fx.line(spot.clone().add(new Vector(0, 40 - 30 * p, 0)), spot.clone().add(new Vector(0, 40, 0)), 1.0,
                        fx.dust(Palette.HOLY, 1.0f).sometimes(0.5));
            });
            t.span(strike, strike + BEAM, (tick, p) -> {
                double radius = RADIUS * (1 - 0.6 * p);
                for (int k = 0; k < 6; k++) {
                    Vector offset = Shapes.heading(k * Math.PI / 3 + tick * 0.3).multiply(radius * 0.7);
                    fx.line(spot.clone().add(offset), spot.clone().add(offset).add(new Vector(0, 40, 0)), 1.2,
                            fx.dust(Palette.HOLY, 2.0f));
                }
                fx.line(spot, spot.clone().add(new Vector(0, 40, 0)), 0.6, fx.dust(Palette.SPECTRAL, 2.4f));
                fx.ring(spot, radius, 0.4, tick * 0.5, fx.particle(Particle.END_ROD));
                if (tick == 0) {
                    fx.flash(spot.clone().add(new Vector(0, 1, 0)), Palette.HOLY);
                    fx.sound(spot, Sfx.BEACON_POWER, 2.5f, 1.6f);
                    fx.sound(spot, Sfx.TOTEM_USE, 1f, 1.4f);
                    stage.hit(Area.cylinder(spot, RADIUS, 1, 40), damage, victim -> {
                        victim.effect(Affliction.GLOWING, 80, 0);
                        victim.effect(Affliction.WEAKNESS, 60, 0);
                    });
                }
            });
        }
        int end = CALL + spots.size() * GAP + WARN + BEAM;
        recover(t, stage, end - 4, end + 12, Poses.GUARD);
        return t;
    }

    @Override
    public String getName() {
        return "spiritbeam";
    }
}
