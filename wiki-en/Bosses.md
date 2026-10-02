# 🐉 Bosses

MultiverseCreatures includes **one final boss** and multiple formidable **minibosses and custom-modeled bosses**. All bosses are spawned via `/msc spawn <type>` (OP-only) and have configurable health/damage/cooldowns in `config.yml`.

---

## 🛡️ THE OBSIDIAN SENTINEL — Final Boss

A gigantic 7.5×-scale animated ArmorStand. The climax of the plugin. Unlike the dressed bosses, the Sentinel **is** the stand, so its scale is at once the size of the model and the box players hit.

| Stat | Default |
|---|---|
| Health | `armor-stand-boss.health` (default 3200) |
| Hitbox | `armor-stand-boss.hitbox-scale` (7.5) — scale of the boss's own stand: model size and hitbox at once, about fourteen blocks of warrior (clamped to 0.25–8) |
| Boss bar | `SEGMENTED_6`, red → blue across the phases (see `phase-thresholds`) |
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

The ladder is data, not code: `armor-stand-boss.phase-thresholds` holds the health fractions at which each next phase begins, highest first. The number of phases is one more than the entries in that list, and it is also how many squares the boss bar title shows — one per phase, red for the phases still to come and grey for the ones already spent. Entries outside `(0, 1]` are dropped and duplicates collapsed, so a typo cannot leave the boss with a single phase.

### AI behaviour

- **Ground mode** chooses between HealingCircle (<40% HP, 25%), FlyUp (15%), ShieldSeal (35%), GroundAttack (55%), HoverBarrage (default).
- **Flying mode** throws an aerial attack 50 ticks after the previous one has finished; after five different ones (and 10–20 s in the air) it lands or dives down with AirSlam, and never stays up more than 40 s.
- **One attack at a time** — while an attack is animating the AI neither turns him nor starts the next one; the cooldowns only count while he is free, so a long attack never eats the pause after it.
- **Defense states** (random, only below 50% HP, on ground): **Stone Skin** (×0.5 dmg taken), **Reflect Barrier** (×0.7 dmg + 30% reflect), **Absorb Shield** (100-HP absorber that visually shifts blue → red). Their durations come from `defense-duration-stone-skin-ticks` (200), `defense-duration-reflect-barrier-ticks` (160) and `defense-duration-absorb-shield-ticks` (300).
- **Ground recovery** — a grounded boss only attacks while `isOnGround` is true. If it ends up with no solid block under it (void, water, a hole, a cliff edge), it hovers silently forever. After `ground-recovery-grace-ticks` (40) without ground it teleports to the nearest column with a floor and headroom, preferring the area around its current target and falling back to the world spawn, then resumes attacking with its cooldowns reset.
- **Despawn** — with nobody inside a 100-block radius the boss keeps fighting for `no-player-despawn-ticks` (200, ~10 s) before it despawns and cleans up its tasks, seals, music and boss bar. Set it to `0` to remove the boss as soon as the arena empties.
- **Penetrating damage** — with `penetrating-damage: true` the boss's own hits bypass armour and Protection enchantments (they are re-applied as `OUT_OF_WORLD` damage, capped at `max-damage-dealt` (15) per hit). Resistance is only *partially* pierced: `penetrating-resistance-pierce: 0.2` makes the boss ignore 20% of the potion's mitigation, so a player with Resistance I (20% reduction) still blocks 16% of the hit. `0.0` leaves Resistance fully effective, `1.0` ignores it entirely. Use `/msc debug [player]` after a hit to see the whole breakdown (event damage, the reductions credited back, the pierce applied and the final value); the same command also reports what Nix and Jack Star deal to and take from that player.

### Special mechanics

- **Aegis Judgment** (`groundslam`) — on its own clock, outside the pools: he hurls his shield into the sky, where it turns and casts a burning pentagram under every player for two seconds; when his spear comes down, columns of light fall from the shield into every pentagram — `seal-damage` (15) in a 4-block radius + knock-up — and the shield drops back into his hand.
- **Wings** — burning wings built like real ones: a dark bone that leaves his back between the shoulder blades and rises through an elbow and a wrist, long flight feathers hanging from it that glow from red to orange towards their tips, and a shorter row of coverts. They beat slowly, the whole wing turning about its root.
- **Shield Seal** — six great shields of light wheel around him at chest height for 200 ticks, ×0.7 incoming damage; they fold back into one at the end.
- **Healing Circle** — he kneels in a circle of green runes for 200 ticks with threads of light rising into him, healing 0.15% a tick up to 3% of his max HP. Hurting him inside the circle is the counter.
- **Hover Barrage ("CrossBarrage")** — rises if grounded and fires volleys of X-shaped beams from the air, `hover-barrage-damage` (12) + knockback.
- **Triangle Call** — he plants the spear and raises two standing seals of fire; columns of light come down through them and reinforcements step out (scales with player count):
  - Air mode: Infernal Ghast + Night Stalker Phantom (carrying Sniper Skeleton with Power V Infinity bow).
  - Ground mode: War Beast Ravager (300 HP, 24 dmg) carrying a Dark Priest Evoker (40 HP, Speed I).
  - Summons are `MSC_ArmorBossSummoned` tagged; friendly-fire between the boss and its summons is disabled.

### Animated attacks

Every attack is a **choreography**: a telegraph on the floor that says *where* (red to yellow as it heats up), a wind-up of the body that says *when*, the blow, and a recovery back to the guard. The Sentinel's arms, legs, head and body move through real poses computed from the armor stand model, so the spear tip, the shield face and the hands are where the effects come from. While an attack plays, it owns the body: the AI does not turn him or start another attack until it is done. Props (shields, spears, obsidian pillars, meteors) are display entities tagged `MSC_AttackProp` and are removed when the attack ends or the server restarts.

### Attack registry — 61 attacks total

All attacks are classes extending `ChoreographedAttack` under `entities/boss/attack/<aerial|ground|ranged|defensive>/`, registered in `ArmorStandBoss.initAttacks()` and dispatched polymorphically via `attackRegistry.get(name).execute(instance)`. Trigger any one manually:

```
/msc attack <attack-name> [range]
```

All 61 names are listed by `/msc attack help` (four pages, one per category) and offered by tab completion. `/msc attack` also accepts the mechanics above (`flyup`, `land`, `heal`, `reset`, the four `phase*` transitions) plus `hoverbarrage`'s legacy alias `crossbarrage`.

| Ground (21) | Aerial (18) | Ranged (16) | Defensive (6) |
|---|---|---|---|
| groundslam | starfall | lancesnipe | stoneskin |
| groundshatter | aerialrush | meteorstorm | reflectbarrier |
| shieldbash | sonicboom | voidbeam | absorbshield |
| lancestorm | lightningstorm | frostlance | shieldseal |
| earthpillar | gravitywell | lightningspear | healingcircle |
| chaingrapple | crossslash | shadowvolley | trianglecall |
| warstomp | novaburst | chainlightning |  |
| armorspikes | darkorb | crystalbarrage |  |
| vortexpull | windcutter | arcaneorb |  |
| mirrorimage | heavenlyjudgment | voidrift |  |
| doombeam | rainoflances | arcanemissiles |  |
| lanceflurry | airslam | spiritbeam |  |
| whirlwindslash | hoverbarrage (crossbarrage) | soultethers |  |
| executionsweep | eclipsefall | plaguebrand |  |
| obsidianspire | bladering | runemines |  |
| earthmaw | obsidianwings |  |  |
| shadowstep | voidmeteor | obsidianprison |  |
| runeward | phantomlegion |  |  |
| sunderingcharge |  |  |  |
| spearcyclone |  |  |  |
| cataclysm |  |  |  |

Additional `/msc attack` targets for **mechanics & phase transitions**: `flyup`, `land`, `heal`, `reset`, `phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`.

### Second wave — ten more attacks and how to read them

Each of the ten additions is animated around one unmistakable tell, so a player can name the attack from the first second of the wind-up. Damage overrides live under `entities.armor-stand-boss.*-damage`.

| Attack | Category | Animation signature | Damage key (default) |
|---|---|---|---|
| `obsidianspire` | Ground | Both arms rise straight overhead and the body leans back, then a line of volcanic pillars erupts one by one along the facing. Each pillar throws its victim **upwards**, and an Executioner Cross is drawn along the path. | `obsidian-spire-damage` (11) |
| `earthmaw` | Ground | Kneeling crouch while two rows of teeth open over a 34° frontal cone, cracked ground from a Quake Seal, then the rows **snap shut** and drag everyone caught inside to the centre. | `earth-maw-damage` (9) |
| `shadowstep` | Ground | He hunches with his arms crossed, a pentagram swallows him and he is **gone** — the only teleporting attack. He reappears 1.35 blocks behind the target through a second sigil and spins 360° into a backstab burst. | `shadow-step-damage` (15) |
| `runeward` | Ground | A **kneeling cast** inside a large pentagram, then he plants an amethyst rune that stays behind. Every 25 ticks the rune throbs and pushes a ring of runes outward (Weakness + Slowness) for 150 ticks before shattering. | `rune-ward-damage` (4) |
| `eclipsefall` | Aerial | Both arms lift straight up while a **dark disc** forms three blocks overhead, ringed by a pentagram. The disc then falls with a comet trail and detonates as a black shockwave that blinds everyone nearby. | `eclipse-fall-damage` (16) |
| `bladering` | Aerial | Eight netherite lances materialise in a **visible ring orbiting his body** under a wing seal. He spins with the ring, then widens it and launches the lances one by one with homing flight. | `blade-ring-damage` (7) |
| `obsidianwings` | Aerial | Four wing panels grow out of his shoulders and unfold. Three **wing beats** sweep the panels down and throw expanding rings of obsidian shards outward; the wings then snap shut and both slam down together into a shockwave. | `obsidian-wings-damage` (8), `obsidian-wings-slam-damage` (15) |
| `soultethers` | Ranged | Both arms rise overhead and soul embers drain out of every nearby player into his chest. A glowing **tether stays drawn** from his chest to up to four players while they are dragged in and drained, and at the end it snaps taut and reels everyone in hard. | `soul-tether-damage` (3), `soul-tether-snap-damage` (13) |
| `plaguebrand` | Ranged | He spreads both arms with the palms open and a sickly green circle closes on the chosen victim. The mark lands as a spore burst, drains its host every second and **jumps to anyone standing next to them**; his head keeps tracking the marked player after the hit. | `plague-brand-hit-damage` (9), `plague-brand-damage` (4) |
| `runemines` | Ranged | A low arm sweep across the ground under a runic triangle, then six runes are **flung out around the target**. They arm for 25 ticks (dim, flat) before turning violet and pulsing, and burst upward when stepped on — or fade out on their own after 140 ticks. | `rune-mine-damage` (8) |

The random rotations pick them up as well: `obsidianspire`, `earthmaw`, `shadowstep` and `runeward` in the ground tables, `eclipsefall`, `bladering` and `obsidianwings` in the aerial ones, and `soultethers`, `plaguebrand` and `runemines` in the ranged table. The three ranged attacks fire both on the ground and in the air; the seven others require the matching state.

### Third wave — six more attacks

| Attack | Category | Animation signature | Damage key (default) |
|---|---|---|---|
| `sunderingcharge` | Ground | Spear point dropped to the floor, weight back, the lane glowing ahead; then a sprint that **rips a molten fissure** behind the tip. At the end a rising cut throws a fan of obsidian blades out of the ground, and a moment later the whole fissure erupts in fire. | `sundering-charge-damage` (14) |
| `spearcyclone` | Ground | He whirls the spear overhead faster and faster inside a growing spiral of wind, then sweeps it down: a **cyclone** rolls after the target, dragging and lifting anyone near it, and bursts at the end of its run. | `spear-cyclone-damage` (4 per hit, ×3 on the burst) |
| `cataclysm` | Ground | Ultimate: he drives the spear into the earth and three bands of the floor glow, with **safe rings** of dark ground between them. From the inside out each band bursts in walls of fire and obsidian spikes. Stand in the gaps. | `cataclysm-damage` (20) |
| `voidmeteor` | Aerial | Both arms raised while a boulder of crying obsidian forms over his head in a shell of void; he hurls it at the target, it crashes down with a shockwave and leaves a **void crater** that blinds and weakens. | `void-meteor-damage` (18) |
| `phantomlegion` | Aerial | Four **spectral copies** of himself climb out of the floor around the target. One after another each marks its line and lunges straight through the ring; the last two strike together. | `phantom-legion-damage` (11) |
| `obsidianprison` | Ranged | He points the spear at each player: a rune circle opens under them, then a **cage of obsidian spikes** bursts up around it, leaning in, and collapses on itself. Leave the circle before the bars close. | `obsidian-prison-damage` (16) |

They are in the random rotations too: `sunderingcharge` in the medium and far ground tables, `spearcyclone` and `cataclysm` in the close and medium ones, `voidmeteor` and `phantomlegion` in the aerial tables and `obsidianprison` in the ranged one.

### Drops

1000 XP on death, plus the "THE OBSIDIAN SENTINEL / Has been defeated!" title broadcast. Lightning + wither-death sound on death.

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
| Slimefun / Tinker armor (`mahoraga.slimefun-adaptation`, soft-dependency) | **Attack Damage** bonus by material tier (0.3 soft → 3.0 Singularities/Infinity) |
| Full Infinity Singularity Mail Links set (`mahoraga.instakill-infinity-armor`) | **Instant kill** — pierces the "Infinite Defence" trait (damage = 1) |
| Tinker **Diamond** modification on the held weapon (`mahoraga.ignore-diamond-mod`) | **Always ignored** — the reflect-and-cancel of each hit never applies |
| Tinker sword with **Infinity Singularity** material (`mahoraga.infinity-weapon-adaptation`) | Mahoraga only takes **1 damage** per hit |
| Total Protection > 5 (Diamond/Netherite) | **Strength** amplifier = total/5 |
| Max Sharpness **or Smite** total on any weapon (5 levels per rank) | **Resistance** level (max 4) = floor((Sharpness + Smite)/5) |
| Knockback enchantments | **Knockback Resistance** = totalKnockback × 0.3 |
| Target > 4 blocks away | **Speed I** for 30 ticks |
| Target close | Speed removed |

Outfit: white stained-glass helmet + white leather armor (unbreakable).

**MSC friendly-fire protection:** if both damager and target carry any `MSC_` scoreboard tag, the damage event is cancelled — Mahoraga cannot harm other MSC mobs (and vice versa).

**Drops:** 75% chance **Wheel Essence**. 150 XP. Custom player-death messages via `mahoraga.death-messages`.

---

## ♟️ Kinger — Miniboss (The Amazing Digital Circus)

A living chess king: an invisible ArmorStand dressed in a 15-piece ItemDisplay suit (base, legs, torso, collar, belt, arms, head and ornament) that walks, fights and tracks players like a chess piece come to life. The stand is unscaled — a plain 0.5 × 1.975-block hitbox, sized to the suit instead of the old doubled one — and the pieces are grouped into limbs, so a leg swings from its hip and the shin follows the thigh rather than every piece turning on its own anchor — and the **knee bends with the step**, because the shin hinges on a second joint halfway between the exported thigh and shin instead of staying frozen against the thigh. The stride is calibrated to that box: even the deepest step keeps the folded shin over the stand, so a sword swing aimed at the leg never passes through empty air.

| Stat | Default |
|---|---|
| Health | `kinger.health` (120) |
| Hitbox | `kinger.hitbox-scale` (1.0) — size of the invisible stand the suit is hit through; 1.0 is a plain stand and already covers the whole model (clamped to 0.25–8) |
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
- **Animations**: walking sway, melee wind-up and ranged cast poses on the suit parts. Each piece belongs to a rigid limb group that revolves around one joint (hip, shoulder, waist or neck), so the suit stays in one piece while it moves.

**Boss bar:** purple "Kinger" bar, always updated with current health.

**Persistence:** tagged `MSC_Kinger`, so it survives plugin reloads and is picked up again on startup — a reload reattaches the suit it already spawned instead of building a second, overlapping body, and rebuilds its boss bar.

**Death:** removes all suit displays and broadcasts one of the chess-themed `kinger.death-messages` ("checked by the King", "knocked off the board", "lost the game"...).

---

## 🪓 NIX - The Executioner

A towering, ruthless executioner constructed from a custom **27-piece ItemDisplay model** using specialized player skins and matrix transformations. NIX possesses advanced AI, smooth limb movement via procedural JOML quaternion animations, and deadly execution mechanics.

| Stat | Default |
|---|---|
| Health | `nix-executioner.health` (450.0) |
| Hitbox | `nix-executioner.hitbox-scale` (1.9) — size of the invisible stand the suit is hit through (clamped to 0.25–8) |
| Aggro range | `nix-executioner.aggro-range` (28.0 blocks) |
| Move speed | `nix-executioner.move-speed` (0.30) |
| Melee range / Cleave damage | `nix-executioner.melee-range` (3.5) · `nix-executioner.cleave-damage` (22.0) |
| Chain pull range | `nix-executioner.chain-range` (24.0 blocks) |
| Damage cap taken | `nix-executioner.max-damage-per-hit` (100.0 per hit; `0` disables the cap) |
| Cooldowns | melee 20 ticks · chain pull 80 ticks |
| Summon Ritual | **The Executioner's Scaffold** in Boss Dimension (sacrificing `Executioner's Warrant`) · `/msc spawn nix` (OP) |

### Abilities & Mechanics

- **Guillotine Cleave (Melee AOE):**
  When within melee reach, Nix winds up both arms and delivers a crushing downward cleave. Deals `cleave-damage` (22) in a 3.2-block frontal radius, knocks players back, and inflicts **Wither II (Bleed)** and **Slowness II**.
- **Chains of Judgment (Ranged Pull):**
  When a target tries to flee (between 5 and 24 blocks away), Nix casts spectral iron chains (`Sound.BLOCK_CHAIN_PLACE`) that bind the victim, pulling them violently toward Nix while inflicting **Darkness** and **Slowness III**.
- **Blood Harvest (Signature):**
  He flings both arms out to the sides while blood gathers in his hands and a warning ring closes on the floor, then whirls three full turns with blades of blood trailing from his hands. Every revolution cuts everyone within 4.5 blocks for `harvest-damage` (9) with Wither and drags them in; it ends in a ring of blood.
- **Gallows Leap (Signature):**
  A deep crouch with the arms thrown back while the landing spot glows under the target, then a leap of up to 18 blocks with both arms raised overhead. He lands with a two-handed slam: `gallows-damage` (18) within 4 blocks plus a knock-up, and a shockwave that hits for half on its way out.
- **Condemnation (Signature):**
  He raises his right arm and points at his victims: a gallows of particles stands over every player within aggro range (up to four), the blade trembling at the top and a red circle on the floor. 1.5 seconds later the arm chops down and every blade falls — `condemn-damage` (20), Wither II and Slowness for whoever is still in the circle.
  One signature move every `special-cooldown-ticks` (160) + up to 2 s, picked by distance; while one plays it owns the body.
- **The Executioner's Axe:**
  Nix carries a netherite axe in his right hand that follows his forearm and elbow. The basic attack is a guillotine chop: the axe goes up over his head and comes down in front of him, and the hit lands as the axe falls (60% of the swing). His head follows the player's eyes from his own instead of staring at the floor.
- **Execution Frenzy (Passive):**
  When target player health drops below **25%**, Nix enters an execution frenzy: movement speed increases by +30%, eyes emit crimson dust particles, and walking stride tempo accelerates.
- **Procedural Model Animations:**
  All 27 pieces (Head, Upper Torso, Lower Pelvis, 6-part Right Arm, 6-part Left Arm, 6-part Right Leg, 6-part Left Leg) feature synchronized walking counter-rotations, attack windups, and player-tracking head pitch. They follow an **invisible armour stand** (`MSC_NixBoss`) that carries the real health pool and the hitbox, and the joints live in `NixModel` (shoulders at x = ±0.3514, hips at ∓0.1171, neck at 1.650, torso at 1.171), so every limb swings around its own joint. Each arm and leg is a stack of two segments, so the **elbows and knees fold as well**: the `_4` piece carries the joint and the five pieces below it hinge on it while walking as well as during cleave swings (elbows bend during windup and snap straight on chop, with knees flexing into a squat). The export sits 0.066 blocks off the spine, so the parts are re-centred on the hitbox, and the stand is scaled 1.9 so its box (0.95 wide, 3.75 tall) covers the whole model instead of leaving the head outside a vanilla box.

**Boss bar:** Dark Red segmented bar displaying `NIX - The Executioner` with fog and darkened skies.

**Persistence:** Tagged `MSC_NixBoss` and `MSC_NixPart`, and every part also carries its own tag plus an owner tag for the stand it belongs to — a reload **adopts** the parts a live boss already has instead of building a second body on top, and orphans are cleaned up.

**Death:** Triggers lightning thunder, wither death sounds, a bloody particle explosion, drops 450 XP, and announces an execution end title to nearby players.

---

## 👨‍💻 JACK STAR — The System Architect

Five phases, three lives and a body built out of eleven skin heads.

### Boss stats

| Field | Value |
|---|---|
| Health | `jackstar-architect.health` (700.0) |
| Hitbox | `jackstar-architect.hitbox-scale` (1.2) — size of the invisible stand the suit is hit through (clamped to 0.25–8) |
| Lives | 3 — the first two "deaths" run a **Watchdog** reboot that restores 50% HP, the last one 40% |
| Phases | 1 >80% · 2 >60% · 3 >40% · 4 >20% · 5 (kernel panic) ≤20% |
| Damage | `melee-damage` (16) · `slam-damage` (20) · `sigkill-damage` (35) |
| Defences | `dodge-chance` (0.22) Ultra Instinct dodge · `packet-loss-chance` (0.25) discards projectiles · firewall, firejail and cobweb builds |
| Aggro / speed / reach | `aggro-range` (32.0) · `move-speed` (0.32) · `melee-range` (3.8) |
| Summon Ritual | **The System Architect** ritual in the Boss Dimension · `/msc spawn jack` (OP) |

### Model

Eleven skin heads (`ItemDisplay`, tag `msc_jackstar_part`) form the head, the torso and two segments per arm and leg. They follow an **invisible armour stand** (`msc_jackstar_boss`) that carries the real health pool and the hitbox, so the visible body is what players aim at while the stand keeps the bookkeeping. The joints live in `JackModel` (shoulders at x = ±0.35, hips at ∓0.12, neck at 1.87) and every limb swings around its own joint, with counter-rotations while walking. Both arms and both legs are exported in two segments, so the **elbows and knees fold on top of that swing**: the forearm and shin hinge on their own joint while walking and during sword slashes (elbows folding through the swing arc and knees sinking into a combat stance). The parts are re-centred on the hitbox, which is why the body lines up with the stand instead of drifting most of a block to the side.

### Signature moves

One every `special-cooldown-ticks` (200) + up to 2 s, 30% sooner from phase 4, picked by distance and announced in the arena chat as a line of code. While one plays it owns the body and the regular routine waits.

| Move | Animation signature | Damage key (default) |
|---|---|---|
| **fork()** | He cocks his right arm back with a spinning wireframe cube in his hand, the left hand aiming, and throws it. Every time the cube lands it bursts and **forks into two** that hop sideways, three generations deep: 1 + 2 + 4 explosions, each with its landing ring shown in advance. | `fork-bomb-damage` (10; ×0.6 for the forks) |
| **Binary Rain** | Both hands raised, fingers typing at the sky while a sheet of green code scrolls over the arena. Glowing cells light up under and ahead of the players and a falling **1 or 0** crashes into each one. | `binary-rain-damage` (8) |
| **Stack Overflow** | A low sprinting stance with both blades swept back, then **four dashing cuts** through the target, each one pushed as a "[ ]" frame that stays drawn on the floor. When the stack is full it overflows: every frame detonates along its line in reverse order. | `stack-overflow-damage` (14; half on the dash itself) |

### Subprocesses

Three seconds after spawning, and again on every phase change — five times at most — Jack Star summons another boss 14 blocks away: Garou, Mahoraga, Chaos Mage, Obsidian Guard, Soul Reaper or NIX, drawn at random until one accepts. He stays on the field throughout: a subprocess is extra pressure, **never a shield**, so he keeps fighting and keeps taking damage while one is alive.

### Damage taken

Hits pass one door: the **Ultra Instinct** dodge first (0.22, raised to 0.45 in compressed form), then the **Load Balancer**, which leaves 65% on the boss and shares 35% among every non-creative player within 14 blocks. A landed hit always reaches the boss; `/msc debug` prints the intended hit, the split and the value applied.

**Drops:** 950 XP and the `ArchitectKernel`, with a final title for every player within 60 blocks.

---

## ⏱️ DIO — JoJo's Bizarre Adventure

DIO walks in menacingly (ゴゴゴ letters drift up around him) with his Stand **The World** floating behind his right shoulder. DIO is a visible armor stand in his yellow Part 3 outfit with his own face; The World is a larger stand with its own head, gold armour with emerald trim and a golden aura. Health is virtual, like Nix and Jack Star. Summoned at **The World's Throne** in the Boss Dimension (see [Ritual Dimension](Ritual-Dimension)) or with `/msc spawn dio` (OP).

| Field | Value |
|---|---|
| Health | `dio-brando.health` (900) — below 50% he enrages ("WRYYYY!"): shorter pauses, longer time stop, more knives |
| Aggro / speed | `aggro-range` (32) · `move-speed` (0.26) |
| Damage cap taken | `max-damage-per-hit` (100) |

| Attack | What happens | Damage key (default) |
|---|---|---|
| **ZA WARUDO** | The World rises with its arms spread and a sphere of stopped time sweeps out `time-stop-radius` (40) blocks. For `time-stop-ticks` (100) players cannot move, attack, use items or shoot; mobs and projectiles freeze too, and he counts the seconds ("1-byō keika..."). He throws knives that stop in the air in a ring around everyone. "Toki wa ugokidasu": time moves again and every knife flies. Cooldown `time-stop-cooldown-ticks` (700). | `knife-damage` (5) per knife |
| **MUDA MUDA MUDA** | The World rushes up to 10 blocks in and buries the target under a barrage of fists, then a last two-fisted "MUDAAA!" that launches them. | `barrage-damage` (2.5 every 3 ticks) · `barrage-finisher-damage` (14) |
| **ROAD ROLLER DA!** | He leaps high over the target, a road roller appears and falls with him on its roof; The World pounds it into the ground ("MUDA MUDA") and it explodes. The landing ring is shown the whole time. | `road-roller-damage` (26; half on the explosion) |
| **Knife fan** | Knives drawn behind his head and thrown in a fan of 7 (9 enraged). | `knife-damage` (5) |
| **Space Ripper Stingy Eyes** | His eyes glow red, then two jets of pressurised fluid sweep across the arena. | `eye-beam-damage` (5 every 3 ticks) |
| **The World's punch** | His basic attack at close range: a heavy punch with knockback. | `punch-damage` (12) |

He greets anyone who walks up to him ("Oh? You're approaching me?"). His knives, the road roller and the menacing letters are display props removed when an attack ends, the boss dies or the server restarts; `/msc kill` removes DIO and The World.
