# 🐉 Bosses

MultiverseCreatures includes **one final boss** and **three minibosses**. All bosses are spawned via `/msc spawn <type>` (OP-only) and have configurable health/damage/cooldowns in `config.yml`.

---

## 🛡️ THE OBSIDIAN SENTINEL — Final Boss

A gigantic 7.5×-scale animated ArmorStand. The climax of the plugin.

| Stat | Default |
|---|---|
| Health | `armor-stand-boss.health` (default 500) |
| Boss bar | `SEGMENTED_6`, red → blue across 5 phases |
| Music | `Undertale — Megalovania` (60-block range, stops on death) |
| Equipment | Full Netherite (Amethyst/Silence trim) + Netherite Lance + unbreakable Shield |
| Summon | `/msc spawn armorstand` (alias `armorstandboss`) |

### Phases (transitions happen at HP thresholds)

| Phase | HP % | Transition effect |
|---|---|---|
| 0 — Red | >80% | Rage: knockback + Weakness I to nearby players, large pentagram seal |
| 1 — Purple | >60% | Barrier: invulnerable 100t, heals +30 HP, celestial seal |
| 2 — Yellow | >40% | Storm: 15 lightning strikes over 12-block radius, Slowness II + Weakness II |
| 3 — Green | >20% | Despair: invulnerable 80t, AoE damage ×1.5 + Darkness II + Blindness I + Slowness III |
| 4 — Blue | ≤20% | final phase |

### AI behaviour

- **Ground mode** chooses between HealingCircle (<40% HP, 25%), FlyUp (15%), ShieldSeal (35%), GroundAttack (55%), HoverBarrage (default).
- **Flying mode** executes random aerial attacks every 80 ticks; lands via AirSlam when ≥10 unique attacks have been performed.
- **Defense states** (random, only below 50% HP, on ground): **Stone Skin** (×0.5 dmg taken), **Reflect Barrier** (×0.7 dmg + 30% reflect), **Absorb Shield** (100-HP absorber that visually shifts blue → red).

### Special mechanics

- **Plant Shield / Ground Slam** — plants the shield as an ItemDisplay (7.5 scale), performs delayed GroundSlam, retrieves later.
- **Shield Seal** — hemispherical dust+END_ROD shield sphere for 200 ticks, ×0.7 incoming damage, with 12 orbiting ItemDisplay shields.
- **Healing Circle** — 35-tick cast, green circle, heals up to 5% max HP over 200 ticks, ×0.8 dmg taken while active.
- **Hover Barrage ("CrossBarrage")** — rises to y+15, traces an X-shape, fires X-beams that explode for `hover-barrage-damage` (12) + knockback.
- **Triangle Call** — spawns magic triangle seal + reinforcements (scales with player count):
  - Air mode: Infernal Ghast + Night Stalker Phantom (carrying Sniper Skeleton with Power V Infinity bow).
  - Ground mode: War Beast Ravager (300 HP, 24 dmg) carrying a Dark Priest Evoker (40 HP, Speed I).
  - Summons are `MSC_ArmorBossSummoned` tagged; friendly-fire between the boss and its summons is disabled.
- **Sky Pentagram** — per-player pentagram seals 30 blocks above, exploding in a column after 80 ticks — `seal-damage` (15) within 6-block radius + knockup.
- **Shockwave Rings** — 10 expanding rings, ground damage falls off with distance, knockup, FallingBlock debris.

### Attack registry — 33 attacks total

All attacks are classes extending `BossAttackBase` under `entities/boss/attack/<aerial|ground|ranged>/`, registered in `ArmorStandBoss.initAttacks()` and dispatched polymorphically via `attackRegistry.get(name).execute(instance)`. Trigger any one manually:

```
/msc attack <attack-name> [range]
```

| Ground (11) | Aerial (13) | Ranged (12) |
|---|---|---|
| groundslam | starfall | lancesnipe |
| groundshatter | aerialrush | meteorstorm |
| shieldbash | sonicboom | voidbeam |
| lancestorm | lightningstorm | frostlance |
| earthpillar | gravitywell | lightningspear |
| chaingrapple | crossslash | shadowvolley |
| warstomp | novaburst | chainlightning |
| armorspikes | darkorb | crystalbarrage |
| vortexpull | windcutter | arcaneorb |
| mirrorimage | heavenlyjudgment | voidrift |
| doombeam | rainoflances | arcanemissiles |
|  | airslam | spiritbeam |
|  | hoverbarrage (crossbarrage) |  |

Additional `/msc attack` targets for **mechanics & phase transitions**: `trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`, `phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`, `stoneskin`, `reflectbarrier`, `absorbshield`.

### Drops

1000 XP on death, plus the "THE OBSIDIAN SENTINEL / Has been defeated!" title broadcast. Lightning + wither-death sound on death.

---

## 🌟 Dio Brando — Miniboss (JoJo's Bizarre Adventure)

| Stat | Default |
|---|---|
| Health | `dio-boss.health` (300) |
| Attack Damage | `dio-boss.damage` (10) |
| Cooldown | `dio-boss.cooldown-ms` (120 s) |
| Spawn | `/msc spawn dio` |
| Natural spawn | `dio-boss.spawn-chance` (0.5%) — replaces Zombies (not from spawners) |

**Appearance:** Zombie with a custom Dio player-head helmet (base64 skin texture), gold armor with Netherite/Emerald trims (Vex/Silence/Ward patterns). `MaximumNoDamageTicks = 0` (no i-frames).

**The Stand:** An invulnerable ArmorStand (`MSC_DioStand`) hovers behind Dio, wearing Dio Stand Head + gold armor.

### Abilities

- **THE WORLD: FREEZING** — if no fleeing player is in range, freezes all players within `freeze-radius` (50) for `freeze-duration-ticks` (100) using `FreezeAbility`. Players are locked via Slowness 255 + Jump Boost 128 (position-locked, head-rotation allowed). Title `"THE WORLD: FREEZING · Time has stopped!"`. After the duration, deals `freeze-damage` (10) to all players within `freeze-damage-radius` (30). A 24-sword ItemDisplay animation (glowing IRON_SWORDs) surrounds each frozen player and flies through them over ~15 ticks.
- **THE WORLD: TELEPORT** — targets the furthest player outside `teleport-inner-radius` (25), teleports Dio 2 blocks behind them, applies Darkness I + Slowness I (100 ticks), boosts attack speed to 100, and triggers a **Stand Punch**: 3-tick alternating arm pose animation on the Stand ArmorStand with CRIT particles + strong-attack sound at the target location.
- **On every melee hit** Dio also triggers a Stand Punch animation at the damaged player.

**Drops:** `dio-boss.drop-chance` (10%) to drop **Dio Stand Head**. 500 XP. Lightning + wither-death sound on death; Stand ArmorStand is removed.

---

## ⚙️ Mahoraga — Miniboss (Jujutsu Kaisen)

"Eight-Handled Sword Divergent Sila Divine General" — adaptation made manifest.

| Stat | Default |
|---|---|
| Health | 250 |
| Spawn | `/msc spawn mahoraga` |
| Natural spawn | `mahoraga.spawn-chance` (2%) — replaces Zombies |

**Adaptation logic (per-tick scan of target's armor & weapons):**

| Target's attribute | Mahoraga gains |
|---|---|
| Diamond/Netherite armor + Protection levels | scaled **Attack Damage** bonus |
| Total Protection > 5 (Diamond/Netherite) | **Strength** amplifier = total/5 |
| Max Sharpness on any weapon ≥ 5 | **Resistance** amplifier (max 3) = floor(Sharp/5 − 1) |
| Knockback enchantments | **Knockback Resistance** = totalKnockback × 0.3 |
| Target > 4 blocks away | **Speed I** for 30 ticks |
| Target close | Speed removed |

Outfit: white stained-glass helmet + white leather armor (unbreakable).

**MSC friendly-fire protection:** if both damager and target carry any `MSC_` scoreboard tag, the damage event is cancelled — Mahoraga cannot harm other MSC mobs (and vice versa).

**Drops:** 75% chance **Wheel Essence**. 150 XP. Custom player-death messages via `mahoraga.death-messages`.

---

## ♟️ Kinger — Miniboss (The Amazing Digital Circus)

A living chess king: an invisible 2.0-scale ArmorStand dressed in a 15-piece ItemDisplay suit (base, legs, torso, collar, belt, arms, head and ornament) that walks, fights and tracks players like a chess piece come to life.

| Stat | Default |
|---|---|
| Health | `kinger.health` (120) |
| Aggro range | `kinger.aggro-range` (25 blocks) |
| Move speed | `kinger.move-speed` (0.32) |
| Melee range / radius / damage | `kinger.melee-range` (3) · `kinger.melee-radius` (3.5) · `kinger.melee-damage` (8) |
| Ranged range / damage | `kinger.ranged-range` (30) · `kinger.ranged-damage` (6) |
| Cooldowns | melee 25 ticks · ranged 45 ticks |
| Spawn | `/msc spawn kinger` — **or** place an ArmorStand |
| ArmorStand replacement | `kinger.spawn-on-armorstand-chance` (0.01 = 1% of placed ArmorStands become Kinger; set to 0 to disable) — respects `kinger.enabled` |

### AI behaviour

- **Chases** the nearest player within aggro range (walks at `move-speed`, snaps to the ground) and **faces** the target while tracking its head pitch.
- **Melee** (≤3 blocks): purple dust + smoke burst, `melee-damage` to all players within the melee radius, with a 1.3-velocity knockback.
- **Ranged** (>3 and ≤30 blocks): fires a **ShulkerBullet** from the right hand (`MSC_KingerBullet`) with a shulker shoot sound.
- **Animations**: walking sway, melee wind-up and ranged cast poses on the suit parts.

**Boss bar:** purple "Kinger" bar, always updated with current health.

**Persistence:** tagged `MSC_Kinger`, so it survives plugin reloads and is picked up again on startup.

**Death:** removes all suit displays and broadcasts one of the chess-themed `kinger.death-messages` ("checked by the King", "knocked off the board", "lost the game"...).
