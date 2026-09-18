package com.Chagui68.entities.boss.attack;

import java.util.Random;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.BossHost;

/**
 * Common base for boss attacks.
 *
 * Attacks depend on the BossHost interface rather than a concrete boss class, allowing all
 * attack behaviors to be reused across different bosses.
 */
public abstract class BossAttackBase implements BossAttack {

    protected final BossHost boss;
    protected final MultiverseCreatures plugin;
    protected final Random random = new Random();
    protected final double sealDamage;
    protected final double hoverBarrageDamage;

    public BossAttackBase(BossHost boss) {
        this.boss = boss;
        this.plugin = boss.getPlugin();
        this.sealDamage = boss.getSealDamage();
        this.hoverBarrageDamage = boss.getHoverBarrageDamage();
    }
}
