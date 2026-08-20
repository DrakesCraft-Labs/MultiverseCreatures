# 🧟 Creatures

All creatures below **replace natural spawns** of vanilla mobs with a configurable chance. Roll routing lives in `MobHandler` (`entities/handler/MobHandler.java`). Every value is configurable in `config.yml`. Every creature can also be summoned with `/msc spawn <type>` (OP-only).

---

## 👻 Head Slime (Half-Life)

A parasitic slime that leaps onto its target and **attaches as a passenger** to the target's head.

| Target | Effect while attached |
|---|---|
| **Player** | Blindness II + Slowness I + periodic true damage (`damage-per-interval` every `damage-interval-ticks`); auto-detaches after `max-attach-ticks` (200). Detaches if the player takes damage. |
| **MSC mob** (buff mode) | Every `buff-interval-ticks` (40 t): Strength II + Speed II + Resistance I + Fire Resistance. Re-targets the mob to nearest player. Per-mob interactions: doubles creeper explosion radius, makes skeletons fire 3 crit arrows, gives Shadow Rogue invisibility, gives Obsidian Guard absorption, etc. |
| **Creeper with attached Head Slime** | Explosion deals **24.0 true damage** scaled by distance to players (instead of the creeper's normal damage). |

Will not attach to Mahoraga or a creature already wearing one.

**Immunity system:** players holding Head Slime Gelatin get pushed into `immunePlayers` for 10s; witch particle ring renders around immune players and the slime auto-detaches.

- **Natural spawn:** `head-slime.spawn-chance` (10%) of Slime spawns
- **Drops:** Head Slime Heart (always)
- **Command:** `/msc spawn headslime`

---

## 💥 Creeper Jr.

A swarm of three tiny fast creepers that leap-fuse and deal **true damage** (OUT_OF_WORLD, bypasses armor).

| Stat | Default |
|---|---|
| Scale | 0.6× normal creeper |
| Speed | 0.5 (vs vanilla 0.2 — much faster) |
| Explosion radius | 2 (vs vanilla 3) |
| Fuse ticks | 25 (very short) |
| True-damage max | 12 (scales by distance from center) |
| Block damage | None (yield = 0) |

On explosion knockback is applied to nearby non-player entities, and other Creeper Jr. instances in the area are forced to drop their target.

- **Natural spawn:** `creeper-jr.spawn-chance` (15%) — spawns in trio
- **Drops:** none special
- **Command:** `/msc spawn creeperjr`
- **Death messages:** from `creeper-jr.death-messages`, `%player%` replaced

---

## 🐴 ZombieHorseTrap (Military Army — Full Moon Event)

A rare Full-Moon trap. During `MoonPhase.FULL_MOON`, 0.1% of natural zombie spawns become a **Military Zombie Horse** that wanders; when a (non-creative/spectator) player comes within 6 blocks, it despawns and spawns a coordinated 5-unit army:

| Unit | Tag | Stats & Behaviour |
|---|---|---|
| **Zombie Tank** (center) | `MSC_ZombieTank` | Zombie scale 1.5, 350 HP, 10 dmg, full iron armor + lime leather helmet. Permanent Resistance I + Slowness I + Fire Resistance. Takes 50% reduced projectile damage. Death enables Duelist ranged positioning AI. |
| **Skeleton Duelist** (×2) | `MSC_Duelist` | Skeleton 50 HP, chainmail + purple leather helmet. Holds "Duelist Bow" (Flame I / Power III) for ranged and switches to "Duelist Sword" (Sharp III / Knock II) at ≤6 blocks. Maintains flank positions behind tank while alive. |
| **Zombie Lancer** (mounted) | `MSC_Lancer` on `MSC_LancerHorse` | Zombie with iron armor + grey leather helmet holding "Iron Lance". Horse: Resistance I + Speed III + Fire Resistance. On horse death the Lancer gets permanent Speed III + Strength I. |
| **Army Camel** (×2) | `MSC_ArmyCamel` with riders | Camel (Husk): Speed II + Resistance II + Fire Resistance. Rider 1 — `MSC_CamelZombie` (Zombie, orange helmet, copper armor, **Diamond Lance**) Resistance I + Fire Resistance. Rider 2 — `MSC_CamelSkeleton` (Bogged, Power II / Punch II bow). On camel death riders gain Speed (and zombie Resistance / skeleton Speed II). |
| **Sniper Skeleton** (rearguard) | `MSC_Sniper` | Wither Skeleton 40 HP, full green leather armor, "Sniper Bow" (Power V / Infinity). **Predicts player movement** (velocity + gravity arc) and fires crit arrows every 30 ticks at up to 50 blocks. Arrows apply Wither I (100t) + Weakness I (100t). |

**Commands:**
- Full army: `/msc spawn zombietrap` (alias `army`)
- Individual units: `/msc spawn tank`, `duelist`, `lancer`, `camel`, `sniper`

**Drops:** each of Tank/Duelist/Lancer/CamelZombie/CamelSkeleton/Sniper has a 30% chance (`zombie-horse-trap.military-component-drop-chance`) to drop a **Military Component**.

---

## ⛑️ Obsidian Guard — Heavy Tank Zombie

Full-netherite heavy tank zombie.

| Stat | Value |
|---|---|
| Base | Zombie (adult, scale 1.8) |
| Health | 300 |
| Damage | 8 |
| Speed | 0.15 (slow) |
| Armor | Full Netherite + Obsidian helmet + Netherite Sword ("Obsidian Blade", Sharp III / Knock II) |
| Permanent buffs | Resistance II, Fire Resistance, full Knockback Resistance |

**Abilities:**
- **Taunt** (cd 100): targets all players within 20 blocks → Slowness II + Weakness I (60 ticks) + "Face me!" chat message + iron-door sound.
- **Self-Heal** (when HP < 100 AND cd > 200): Regeneration III 60 ticks + heart particles.
- **Melee hit:** Weakness II (60t) + Slowness II (40t) to player.

- **Natural spawn:** `obsidian-guard.spawn-chance` (2%) of Zombie spawns
- **Drops:** 85% chance **Obsidian Shard** + 100 XP
- **Command:** `/msc spawn obsidianguard` (alias `obsidian`)

---

## 🌌 Ender Knight

Enderman-themed knight with a "Ender Blade" Diamond Sword (Sharp V, Knock II).

| Stat | Value |
|---|---|
| Base | Enderman |
| Health | 120 |
| Damage | 14 |
| Speed | 0.3 |
| Follow Range | 30 |

**Abilities (vs players):**
- **Ender Pull** (dist 5–25, cd 60): pulls the target toward the knight (velocity), 4 dmg + portal/end-rod particles + enderman scream.
- **Ender Rush** (dist >8, cd 40): teleports behind the target and backstabs for 8 dmg + Slowness II (60t) + dual portal particle bursts.
- **Levitate Touch** (dist <4): Levitation I (20t). Melee also applies Levitation I (30t).

- **Natural spawn:** `ender-knight.spawn-chance` (4%) of Enderman spawns
- **Drops:** 55% **Ender Fragment** + 70 XP
- **Command:** `/msc spawn enderknight` (alias `ender`)

---

## ❄️ Frost Golem

Iron Golem reimagined as a winter guardian. Wears aqua leather chestplate.

| Stat | Value |
|---|---|
| Base | Iron Golem |
| Health | 200 |
| Damage | 12 |
| Speed | 0.18 |

**Abilities:**
- **Ice Aura** (dist < 8, cd 40): 16-point ring of light-blue DUST + SNOWFLAKE particles, Slowness IV (60t) + Weakness I (60t) + 4 dmg.
- **Freeze Beam** (dist 5–20, cd 80): a beam of DUST + SNOWFLAKE from golem to target, 10 dmg + Slowness VI (100t) + Jump Boost -4 (lock to ground) (100t).
- **Melee:** Slowness III (80t) + Weakness II (80t).
- Immune to Slowness / Weakness / Jump Boost applied to itself.

**Craftable build:** place a Carved Pumpkin or Jack o'Lantern on 2 Packed/Blue Ice with Ice/Packed/Blue Ice "arms" left and right (consumed on activation).

- **Natural spawn:** `frost-golem.spawn-chance` (8%) of Iron Golem spawns
- **Drops:** 75% **Frost Heart** + 80 XP
- **Command:** `/msc spawn frostgolem` (alias `frost`)

---

## 🔥 Flame Elemental

A blaze with homing meteor projectiles.

| Stat | Value |
|---|---|
| Base | Blaze |
| Health | 80 |
| Speed | 0.25 |

**Abilities:**
- **Meteor projectile** (cd 60, dist 3–20): homing meteor from 1.5 blocks above the elemental. Emits FLAME, SMOKE, LAVA, orange DUST. On direct hit (dist<2): 100 fire ticks + 12 dmg + explosion. On impact/timeout: 80 fire ticks + 6 dmg to players within 3 blocks.
- When target within 5 blocks: adds 20 fire ticks + 3 dmg if elemental is on fire. Melee ignites player (80 ticks).

- **Natural spawn:** `flame-elemental.spawn-chance` (10%) of Blaze spawns
- **Drops:** 60% **Magma Core** + 40 XP
- **Command:** `/msc spawn flameelemental` (alias `flame`)

---

## ⚡ Storm Caller

A witch that bends lightning.

| Stat | Value |
|---|---|
| Base | Witch |
| Health | 60 |
| Speed | 0.28 |

**Abilities:**
- **Lightning Strike** (cd 60): two lightning bolts near target (random ±4 offset, snapped to highest block). Players within 4 blocks of a strike: 10 dmg + Slowness III (40t) + thunder sound.
- **Storm Cloud** (cd 100): a rotating DUST (grey-blue) + CLOUD particle cloud 5 blocks above target, then 4 lightning strikes over a 10-block area: 8 dmg + Slowness II (60t).

Roll priority: **Storm Caller rolls first** when a natural Witch spawns; if it fails, Venom Witch rolls.

- **Natural spawn:** `storm-caller.spawn-chance` (4%) of Witch spawns
- **Drops:** 60% **Storm Crystal** + 50 XP
- **Command:** `/msc spawn stormcaller` (alias `storm`)

---

## 🧪 Venom Witch

A poison-specialist witch.

| Stat | Value |
|---|---|
| Base | Witch |
| Health | 50 |
| Speed | 0.25 |

**Abilities:**
- **Toxic Cloud** (cd 80): AreaEffectCloud radius 3.5, 100 ticks, green 0x66FF00, with Poison III (60t) + Wither II (40t) + Slowness II (60t).
- **Debuff Bolt** (cd 50): Poison II (100t) + Weakness II (100t) + Blindness I (40t) + 4 dmg + witch particles.

- **Natural spawn:** `venom-witch.spawn-chance` (5%) of Witch spawns (only if Storm Caller roll fails)
- **Drops:** 60% **Venom Gland** + 30 XP
- **Command:** `/msc spawn venomwitch` (alias `venom`)

---

## ⚰️ Soul Reaper

A wither-skeleton scythe wielder with lifesteal.

| Stat | Value |
|---|---|
| Base | Wither Skeleton |
| Health | 100 |
| Speed | 0.28 |
| Follow Range | 30 |
| Weapon | Netherite Axe ("Soul Reaper's Scythe", Sharp V / Fire Aspect II) |

**Abilities:**
- **Soul Drain** (dist <8, cd 60): 10 dmg, heals self for 50% of damage dealt, applies Wither III (80t) + Hunger III (80t). Stream of END_ROD + PORTAL particles from target to self + wither-shoot sound.
- **Melee lifesteal**: heals for 30% of final damage. Applies Wither II (100t).

- **Natural spawn:** `soul-reaper.spawn-chance` (5%) of Wither Skeleton spawns
- **Drops:** 60% **Reaper Essence** + 60 XP
- **Command:** `/msc spawn soulreaper` (alias `reaper`)

---

## 🌀 Chaos Mage

An evoker that casts one of 6 random chaotic spells every 50+ ticks.

| Stat | Value |
|---|---|
| Base | Evoker |
| Health | 70 |
| Speed | 0.25 |

**Spells (random 0–5):**
0. Summons a "Chaos Vex" (`MSC_ChaosVex` tag) as passenger.
1. Ring of fire + ignites target (80t fire) + 8 dmg.
2. Summons 3 Creepers around itself.
3. Random debuff (Poison/Wither/Slowness/Weakness/Blindness/Hunger/Levitation/Darkness, amp 2, 120t) + 5 dmg + witch particles.
4. Fires 4 SmallFireballs in cardinal directions (yield 0).
5. Swaps positions with target; teleports + applies Nausea I (100t) + portal particles + enderman sound.

- **Natural spawn:** `chaos-mage.spawn-chance` (6%) of Evoker spawns
- **Drops:** 60% **Chaos Orb** + 50 XP
- **Command:** `/msc spawn chaosmage` (alias `chaos`)

---

## 🕳️ Void Crawler

A phase-shifting ambush spider.

| Stat | Value |
|---|---|
| Base | Spider |
| Health | 80 |
| Speed | 0.35 (fastest spider-type) |

**Abilities:**
- **Phase Teleport** (dist >4, cd 50): if stuck in solid block, teleports to nearby open space; otherwise 10% chance per tick to randomly teleport. Portal particles at both ends.
- **Poison Burst** (dist <6, cd 60): 12-point ring of dark-purple DUST (0x8800AA) + WITCH particles; Poison III (100t) + Wither II (60t) + 6 dmg.
- **Melee:** Poison II (80t) + Wither I (40t).

- **Natural spawn:** `void-crawler.spawn-chance` (7%) of Spider spawns
- **Drops:** 50% **Void Essence** + 35 XP
- **Command:** `/msc spawn voidcrawler` (alias `void`)

---

## 🗡️ Shadow Rogue

A fast skeleton assassin.

| Stat | Value |
|---|---|
| Base | Skeleton |
| Health | 60 |
| Speed | 0.35 |
| Weapon | Netherite Sword ("Shadow Blade", Sharp IV / Knock I) + black stained-glass helmet |

**Abilities:**
- **Shadow Teleport** (dist² >25, cd 60): teleports 2 blocks behind the target + portal particles at both endpoints + enderman sound; re-locks target.
- **Backstab** (dist² <9, cd 40): only triggers if the target faces away (dot > 0.7). Deals **18 dmg**, knockback, Blindness II (60t) + Slowness III (60t). CRIT + SWEEP_ATTACK particles + crit sound.

- **Natural spawn:** `shadow-rogue.spawn-chance` (5%) of Skeleton spawns
- **Drops:** 50% **Shadow Cloak Fragment** + 30 XP
- **Command:** `/msc spawn shadowrogue` (alias `rogue`)

---

## 🦴 Bone Shield

A defensive skeleton with a recharging bone wall.

| Stat | Value |
|---|---|
| Base | Skeleton |
| Health | 120 |
| Speed | 0.2 |
| Shield HP | 30 (recharges after 100 ticks when depleted) |
| Off-hand | Shield (auto re-equipped if lost) |

**Abilities:**
- **Passive shield:** reduces incoming damage by 60% (absorbs up to shield HP), bone-block particles on block + sound.
- **Counter:** when blocking, reflects 3.0 damage to attacking players + CRIT particles.
- Orbital DUST (0xEEEEEE) particles around the skeleton in an 8-point ring.

- **Natural spawn:** `bone-shield.spawn-chance` (6%) of Skeleton spawns (rolled alongside Shadow Rogue)
- **Drops:** 80% **Reinforced Bone** + 40 XP
- **Command:** `/msc spawn boneshield` (alias `bone`)

---

## 🛒 Multiverse Merchant ("Shaggy")

A Wandering Trader replacement — 30% (`SHAGGY_CHANCE`) of trader spawns become "Multiverse Merchant" with **custom trades from across the multiverse**:

| Item | Cost | Uses |
|---|---|---|
| 5× Scooby Cookie | 20 Diamonds | 999 |
| Excalibur | 16 Star Cores + 32 Netherite Ingots | 1 |
| Ice King's Crown | 48 Nether Stars + 64 Blue Ice | 1 |
| Wirt's Lantern | 32 Soul Sand + 16 Soul Soil | 1 |
| Mantis Claws | 16 Iron Ingots + 8 String | 999 |

- **Command:** `/msc spawn merchant`

---

## Spawn Summary Table

| Vanilla mob | MSC replacement | Default chance |
|---|---|---|
| Zombie (Full Moon) | ZombieHorseTrap (army) | 0.1% |
| Zombie | Mahoraga | 2% |
| Zombie | Obsidian Guard | 2% |
| Slime | Head Slime | 10% |
| Creeper | Creeper Jr. (×3) | 15% |
| Skeleton | Shadow Rogue | 5% |
| Skeleton | Bone Shield | 6% |
| Blaze | Flame Elemental | 10% |
| Iron Golem | Frost Golem | 8% |
| Spider | Void Crawler | 7% |
| Witch | Storm Caller (rolls first) | 4% |
| Witch | Venom Witch (rolls second) | 5% |
| Wither Skeleton | Soul Reaper | 5% |
| Evoker | Chaos Mage | 6% |
| Enderman | Ender Knight | 4% |
| Wandering Trader | Multiverse Merchant | 30% |
