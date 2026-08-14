package com.Chagui68.entities.boss.attack;

import com.Chagui68.entities.BossInstance;

public interface BossAttack {
    void execute(BossInstance instance);

    String getName();
}
