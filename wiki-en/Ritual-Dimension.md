# 🌌 Ritual Dimension

The **Ritual Dimension** (world `boss_dimension`) is a private, boss-only world where the **Obsidian Sentinel** is fought. It is a scorched battlefield about 1500 × 1500 blocks across: a paved arena in the middle of a wasteland of lava rivers and lakes, craters, obsidian spikes, ruins and the bones of something huge, closed in by a jagged obsidian range, under an eternal red sky. No weather, no daylight cycle and no natural mob spawning. The dimension is generated automatically the first time a player enters it.

> The dimension is heavily restricted on purpose: players cannot place or break blocks, and almost all commands are blocked (only `/say`, `/me`, `/help` and `/?` work — even `/msc dimtp` is blocked inside). Admins bypass these restrictions with the `msc.admin.bypass` permission.

---

## 🏛️ The terrain

```
r ≤ 48       the arena: perfectly flat paved floor at y=40, a crying-obsidian sigil of rings
             and spokes, a chiselled rim; nothing ever stands on it
r ≤ 92       the approach: paving rubble thinning out into the wasteland, gentle enough to walk
r ≤ 110      no lava, crater or structure: the fight always starts on safe ground
beyond       the wasteland, in three kinds of ground:
               soul valleys       soul sand and soul soil with blue soul fire
               scorched plains    blackstone and basalt with magma patches (they burn)
               burning fields     netherrack with fire
             hazards: lava rivers and lakes (surface at y=34), craters (some flooded),
             obsidian and basalt spikes, ruined colonnades, giant ribcages
edge ≥ 630   a jagged range of banded obsidian, basalt and blackstone, peaks up to ~y=150
```

Lava only ever fills a column up to y=34, and every column below that level is filled the same way, so a lava surface always meets more lava or rock and never runs anywhere. The world border (`boss-dimension.size`, 1500 by default, 100 - 1500) sits just beyond the range.

**Regenerating.** The plugin leaves a `msc-generator.txt` file in the world folder naming the generator that built it. A dimension built by an older generator is deleted and rebuilt once, on the next start. `boss-dimension.reset-on-load: true` forces a rebuild; set it back to false afterwards. Both destroy everything built in that world.

---

## 🕯️ Entering: the Ritual Structure

To enter the dimension you must build and light the **Ritual Structure** in the overworld.

### Layout (7×7, ground level)

```
. S S S S S .        S = polished blackstone stairs (border)
S S C K C S S        C = chiseled polished blackstone
S C O B O C S        O = crying obsidian
S K B X B K S        B = polished blackstone bricks
S C O B O C S        K = cracked polished blackstone bricks
S S C K C S S        X = obsidian (center)
. S S S S S .
```

- The **center block** must be **obsidian**.
- On the **second layer** (1 block above the ground), place **12 candles** in a ring around the inside of the border:

```
. . c c c . .        candles (c) on layer y+1:
. c . . . c .        3 on the top edge (z=1, x=2-4)
. c . . . c .        2 on each side (x=1 and x=5, z=2-4)
. c . . . c .        3 on the bottom edge (z=5, x=2-4)
. . c c c . .
```

### Activation

1. Build the structure and place all 12 candles.
2. Light **every candle** with flint & steel, a fire charge, or another candle item.
3. Red/blue fire circle particles and portal particles appear around the structure.
4. After **~5 seconds**, any player standing inside the circle (radius 5 from the center) is teleported to the Ritual Dimension (they get a short blindness effect — *"There is no escape."*).

> Only one ritual can be active per world at a time. If the structure is broken or the candles go out, the ritual stops.

---

## ⚔️ Invoking the boss: the Invocation Circle

Once inside the dimension, the **Obsidian Sentinel** must be invoked manually.

### Layout (5×5, red candle ring)

```
_ R R R _        (ring of 12 red candles — 3 per edge — empty inside)
R _ _ _ R        center: empty — drop the Echo Shard here
R _ _ _ R
R _ _ _ R
_ R R R _
```

1. Place **12 red candles** in a 5×5 ring (the corners and edges of a square, leaving the center empty).
2. Light **all** of them with flint & steel or a fire charge.
3. A flaming **pentagram** animation spawns in the middle while the invocation is active.
4. **Drop an Echo Shard** (`echo_shard`) into the center of the circle.
5. The shard is consumed, the candles extinguish, and the **Obsidian Sentinel** awakens at the center.

> Killing the Sentinel drops a **Sentinel Core** (configurable chance, `armor-stand-boss.sentinel-core-drop-chance`, default 100%) — a key ingredient for the **Sentinel Grimoire** and other apex recipes.

---

## 🪓 Invoking NIX: The Executioner's Scaffold

**NIX - The Executioner** can strictly only be summoned inside the **Boss Dimension** (`boss_dimension`). Players must build **The Executioner's Scaffold** and make a blood sacrifice on the anvil.

> **Coordinates are relative**: they mark each block's position relative to the **south-west corner of the 5×5** (your origin point). The structure does not need to be built at any fixed spot in the world — the plugin detects the pattern anywhere.
>
> **The floor is not checked**: the plugin only validates the structure blocks themselves, never the ground below or the surroundings. You can build it on crying obsidian, dirt, or anything else.

### Layout (5×5 footprint)

```
P . . . P        P = Corner Gallows (3 blocks tall)
. . c . .        c = Red Candle (lit)
. c A c .        A = Central Anvil (The Executioner's Block)
. . c . .
P . . . P
```

### Required Materials
- **1 Central Anvil** (`anvil`, `chipped_anvil`, or `damaged_anvil`) at the center: `(2, 0, 2)`.
- **4 Red Candles** (`red_candle`) on ground level, one on each side of the anvil:
  - North `(2, 0, 1)`, South `(2, 0, 3)`, West `(1, 0, 2)`, East `(3, 0, 2)`.
- **4 Corner Gallows**: at `(0, 0)`, `(4, 0)`, `(0, 4)`, and `(4, 4)`. Each gallows is **3 blocks tall**:
  - `Y=0` — Base: Polished Blackstone Bricks, Polished Blackstone, Deepslate Bricks, Polished Deepslate, Crying Obsidian, or Iron Block.
  - `Y=1` — Chain: `iron_chain` or any block whose name ends in `chain`.
  - `Y=2` — Skull: Skeleton, Wither Skeleton, Player, or Zombie head (regular or wall variant).

### Invocation Procedure
1. Build the structure on any surface inside `boss_dimension`.
2. Light all **4 red candles** with flint & steel or fire charge.
3. **Activate the ritual**: right-click any of the red candles (with flint & steel in hand or with any item). That is when the plugin checks that the structure is complete and all 4 candles are lit.
4. **Active Invocation Effect** (while it lasts):
   - Blood-red particle lines (`#8B0000`) connect the 4 skull gallows to the central anvil.
   - Ominous chain rattling sounds echo periodically while dark smoke rises from the anvil.
   - If you break the structure or the candles go out, the ritual **cancels** and you must start over.
5. **The Sacrificial Offering**: drop the item onto the central anvil (within **3 blocks** of it, at floor height):
   - An **`Executioner's Warrant`** (via `/msc give warrant` or a crafting recipe).
   - *(Alternative offerings accepted: a `Netherite Axe` or a `Wither Skeleton Skull`)*.
6. **Awakening**:
   - The sacrifice is consumed.
   - Red lightning strikes the anvil with a heavy guillotine impact sound (`Sound.BLOCK_ANVIL_LAND`).
   - The candles extinguish and **NIX - The Executioner** materializes above the anvil in combat stance!

> While NIX is active inside the Boss Dimension, block placing, block breaking, and combat-evading commands are locked for all non-admin players.

---

## ⏱️ Invoking DIO: The World's Throne

**DIO** can only be summoned inside the **Boss Dimension** (`boss_dimension`): the candles and the offering do nothing anywhere else. Coordinates are relative to the south-west corner of the 5×5, and the floor is not checked.

### Layout (5×5 footprint)

```
S . . . S        S = Corner pillar (3 blocks tall)
. . c . .        c = Yellow Candle (lit)
. c G c .        G = Gold Block (the throne)
. . c . .
S . . . S
```

### Required Materials
- **1 Gold Block** at the centre `(2, 0, 2)`: the throne.
- **4 Yellow Candles** (`yellow_candle`) next to it: North `(2, 0, 1)`, South `(2, 0, 3)`, West `(1, 0, 2)`, East `(3, 0, 2)`.
- **4 Corner pillars** at `(0, 0)`, `(4, 0)`, `(0, 4)` and `(4, 4)`, each **3 blocks tall**:
  - `Y=0` — Gold Block.
  - `Y=1` — Emerald Block.
  - `Y=2` — any skull or head (Player, Skeleton, Wither Skeleton or Zombie; standing or on a wall). The World's head works too.

### Invocation Procedure
1. Build the throne anywhere inside `boss_dimension`.
2. Light the **4 yellow candles** and right-click one of them.
3. **The throne wakes**: golden and green light runs from the four heads to the throne, and a golden clock face appears above it, its hand moving once a second with a ticking sound. Breaking the structure or putting a candle out cancels it.
4. **The offering**: drop a **Clock** on the throne (within **3 blocks**).
5. **ZA WARUDO**: the clock is consumed, a shell of stopped time bursts out of the throne and everyone within 20 blocks is held for a moment. Two and a half seconds later, lightning strikes and **DIO** stands on the throne with The World behind him.

> Only one invocation per world at a time, and the throne does not wake while DIO is already alive there. While DIO fights, the Boss Dimension's build and command locks apply as for the other bosses.

---

## 🏛️ DrakesBosses gods: the Pantheon Altar

With **DrakesBosses** installed (soft dependency), its gods can also be summoned inside `boss_dimension`. Without DrakesBosses the altar does nothing. DrakesBosses spawns the god with its own stats, skills, loot and rewards.

### Layout (5×5)

```
P . . . P        P = Corner pillar (2 blocks tall)
. . c . .        c = Pantheon candle (lit)
. c A c .        A = Altar core
. . c . .
P . . . P
```

| Pantheon | Candles | Core `(2, 0, 2)` | Pillars |
|---|---|---|---|
| **Olimpo** | `white_candle` | `chiseled_quartz_block` | `quartz_pillar` |
| **Asgard** | `green_candle` | `chiseled_deepslate` | `spruce_log` |
| **Duat** | `orange_candle` | `chiseled_sandstone` | `smooth_sandstone` |
| **el Vacío** | `purple_candle` | `end_stone_bricks` | `purpur_pillar` |

### Offerings (defaults, configurable under `drakes-bosses.offerings`)

| Pantheon | God ← offering |
|---|---|
| Olimpo | Zeus ← `lightning_rod` · Poseidón ← `heart_of_the_sea` · Hades ← `wither_skeleton_skull` · Ares ← `netherite_sword` · Artemisa ← `spectral_arrow` · Prometeo ← `fire_charge` · Circe ← `amethyst_shard` · Polifemo ← `fermented_spider_eye` · Kratos ← `netherite_axe` · Tifón ← `magma_block` · Hidra ← `prismarine_shard` · Cerbero ← `bone_block` |
| Asgard | Thor ← `iron_block` · Odín ← `gold_block` · Loki ← `ender_pearl` · Heimdall ← `blaze_rod` |
| Duat | Ra ← `golden_carrot` · Isis ← `feather` · Anubis ← `rotten_flesh` · Set ← `redstone_block` |
| el Vacío | Coloso del End ← `echo_shard` · Garou Cósmico ← `nether_star` · Dios Corrupto ← `totem_of_undying` · Wither Storm ← `wither_rose` · Dragón Ancestral ← `dragon_breath` · Jax ← `lantern` |

### Procedure
1. Build the pantheon's altar anywhere in `boss_dimension` (coordinates relative to the south-west corner; the floor is not checked).
2. Light the **4 candles** and right-click one. The altar wakes: light from the pillars, a turning pentagram on the ground, and chat lists the offerings it takes.
3. **Drop (Q) the god's offering** on the core (within **3 blocks**). **One** item is consumed; the rest of the stack stays on the ground. An offering from another pantheon is refused and kept.
4. After `drakes-bosses.arrival-delay-ticks` (40 by default) lightning strikes and the god appears on the core. If DrakesBosses cannot spawn it (e.g. Jax disabled), the offering is given back.

> One altar per world at a time, and it will not wake while another boss fights in the dimension. While the god lives, the building and command locks apply. A god with no player within 64 blocks for `drakes-bosses.no-player-despawn-seconds` (300 by default; 0 = never) is sent away so it cannot lock the dimension.

---

## 🚪 Leaving

There is no teleport command available inside the dimension — the only way out is the **same ritual used to enter**:

1. Build the **Ritual Structure** (the 7×7 polished blackstone layout with 12 white candles described above) inside the dimension.
2. Light **all 12 candles**.
3. After ~5 seconds, players standing inside the circle are teleported back to the **overworld spawn**.

> If the plugin is disabled/reloaded with players inside, everyone is sent back to the overworld spawn automatically.

---

## 🧰 Technical notes

- World name: `boss_dimension` (created on first entry, unloaded on plugin disable).
- Spawn point: `0.5, 41, 0.5`, the centre of the arena.
- Game rules: no daylight cycle, no weather cycle, no mob spawning, immediate respawn, no advancement announcements.
- The sky is forced red via a biome override; `boss-dimension.red-sky: false` leaves it alone (the tint is applied to a vanilla biome, so it is global).
- Config: `boss-dimension.red-sky`, `boss-dimension.size` and `boss-dimension.reset-on-load`.
- Relevant classes: `BossDimensionManager`, `Wasteland`, `WastelandGenerator`, `BossInvocationManager`, `RitualManager`, `RitualStructure`, `BossInvocationStructure`, `PantheonInvocationManager`, `PantheonAltarStructure`, `PantheonGod` — see [Architecture](./dev/Architecture.md) and [Tests](./dev/Tests.md).
