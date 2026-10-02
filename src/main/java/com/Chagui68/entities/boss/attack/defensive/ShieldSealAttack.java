package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.ChoreographedAttack;
import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Prop;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Stage;
import com.Chagui68.entities.boss.fx.Timeline;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Shield Seal: the Sentinel lets go of its shield and it multiplies — six great shields of light
 * wheel around it at chest height, faces out, halving every hit it takes. When the seal ends they
 * fold back into one and return to its hand.
 */
public class ShieldSealAttack extends ChoreographedAttack {

    private static final int CAST = 24;
    private static final int HOLD = 200;
    private static final int SHIELDS = 6;
    private static final double RADIUS = 7;

    public ShieldSealAttack(BossHost boss) {
        super(boss);
    }

    @Override
    protected boolean ready(BossInstance instance) {
        return !instance.shieldSealActive && !instance.isFlying;
    }

    @Override
    public Timeline choreograph(Stage stage) {
        Fx fx = stage.fx();
        BossInstance instance = stage.instance();
        if (instance != null) {
            instance.shieldSealActive = true;
            instance.shieldSealTimer = 0;
            EntityEquipment equipment = instance.stand.getEquipment();
            ItemStack held = equipment == null ? null : equipment.getItemInOffHand();
            if (held != null && held.getType() == Material.SHIELD) {
                instance.shieldSealSavedShield = held.clone();
                equipment.setItemInOffHand(null);
            } else {
                instance.shieldSealSavedShield = null;
            }
        }
        Timeline t = new Timeline();
        List<Prop> shields = props(t);
        double[] spin = {0};

        tweenTo(t, stage, 0, CAST, Poses.SPREAD.withLeftArm(-150, 0, -30), Ease.OUT);
        t.at(0, () -> fx.sound(stage.feet(), Sfx.ILLUSIONER_CAST, 2.5f, 0.8f));
        t.at(4, () -> {
            for (int i = 0; i < SHIELDS; i++) {
                Prop shield = stage.item(Material.SHIELD, stage.body().leftHand(), 0.5f, new org.joml.Quaternionf());
                shield.glow(Palette.FROST);
                shields.add(shield);
            }
        });
        t.span(5, CAST + HOLD, (tick, p) -> {
            double out = Math.min(1, tick / (double) (CAST - 5));
            spin[0] += 0.04 + out * 0.03;
            Vector chest = stage.body().chest();
            for (int i = 0; i < shields.size(); i++) {
                double angle = spin[0] + 2 * Math.PI * i / SHIELDS;
                Vector at = Shapes.onCircle(chest, RADIUS * Ease.at(Ease.OUT_BACK, out), angle, Shapes.FLAT_U, Shapes.FLAT_V)
                        .add(new Vector(0, Math.sin(tick * 0.1 + i) * 0.6, 0));
                shields.get(i).moveTo(at, 1);
                // Each shield faces outwards from the boss.
                float yaw = (float) (-angle + Math.PI / 2);
                shields.get(i).reshape((float) (0.5 + 4.5 * out), new org.joml.Quaternionf().rotationY(yaw), 1);
                if (tick % 4 == i % 4) fx.dust(Palette.ICE, 1.2f, 0.4, 2).at(at);
            }
            if (tick % 3 == 0) fx.ring(chest, RADIUS, 0.9, -spin[0], fx.dust(Palette.FROST, 1.0f).sometimes(0.6));
            if (instance != null) instance.shieldSealTimer = Math.max(0, tick - CAST);
        });
        t.at(CAST, () -> {
            fx.flash(stage.body().chest(), Palette.FROST);
            fx.sound(stage.feet(), Sfx.SHIELD_BLOCK, 3f, 1.6f);
            fx.sound(stage.feet(), Sfx.BEACON_ACTIVATE, 2f, 1.0f);
        });
        tweenTo(t, stage, CAST, CAST + 14, Poses.SHIELD_WALL.withLeftArm(-60, 0, -40), Ease.IN_OUT);

        int end = CAST + HOLD;
        t.at(end, () -> {
            Vector chest = stage.body().chest();
            for (Prop shield : shields) {
                shield.moveTo(stage.body().leftHand(), 8);
                shield.reshape(0.5f, new org.joml.Quaternionf(), 8);
            }
            fx.burst(chest, Particle.END_ROD, 40, 0.6);
            fx.sound(chest, Sfx.SHIELD_BLOCK, 2.5f, 0.7f);
        });
        t.onFinish(() -> {
            if (instance == null) return;
            instance.shieldSealActive = false;
            instance.shieldSealTimer = 0;
            EntityEquipment equipment = instance.stand.getEquipment();
            if (equipment != null && instance.shieldSealSavedShield != null
                    && equipment.getItemInOffHand().getType() == Material.AIR) {
                equipment.setItemInOffHand(instance.shieldSealSavedShield);
            }
            instance.shieldSealSavedShield = null;
        });
        recover(t, stage, end, end + 10, Poses.GUARD);
        return t;
    }

    @Override
    public int lockTicks(Timeline timeline) {
        return CAST + 14;
    }

    @Override
    public String getName() {
        return "shieldseal";
    }
}
