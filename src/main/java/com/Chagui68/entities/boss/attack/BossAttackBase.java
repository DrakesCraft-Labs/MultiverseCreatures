package com.Chagui68.entities.boss.attack;

import java.util.Random;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.BossHost;

/**
 * Base comun de los ataques.
 *
 * POR QUE DEPENDE DE BossHost Y NO DE ArmorStandBoss
 *
 * Antes recibia la clase concreta del Centinela de Obsidiana. Los ataques funcionaban, pero
 * quedaban atados a ese jefe: para que otro los usara habia que copiarlos y cambiarles el tipo,
 * y a partir de ahi las dos copias se separan.
 *
 * Apuntando a la interfaz, los 42 ataques valen para cualquier jefe que la implemente. De los 42
 * solo dos necesitan algo propio del Centinela, y esos hacen la conversion explicita.
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
