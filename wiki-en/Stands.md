# 🏹 The Arrow and the Stands (JoJo's Bizarre Adventure)

The Arrow chooses who gets a Stand… and who dies. DIO's blood makes you a Stand bearer: the
Arrow can no longer kill you and always awakens your Stand.

```
Archer of the Arrow ──(2% of its shots)──► the Arrow
DIO ──► Vampire Blood ──► Unstable Blood ──► Bearer's Elixir ──► vampire + Stand bearer
bearer ──► the Arrow always awakens a Stand
no Elixir ──► 70% death (marked unworthy) / 30% a Stand awakens
```

---

## 💀 Archer of the Arrow 

A skeleton in a golden crown and a purple coat that replaces natural skeletons.

| Field | Value (config `entities.arrow-skeleton`) |
|---|---|
| Spawn | `spawn-chance` 0.04 of natural skeletons (× `spawn-rate-multiplier`) |
| Health / speed | `health` 50 · `speed` 0.28 |
| Bow | Power III; crown and coat never drop |
| The Arrow | `stand-arrow-chance` **0.02**: 2 shots in 100 |
| Death | `death-chance` **0.7** |
| Manual spawn | `/msc spawn arrowskeleton` (aliases `archer`, `standarrow`) |

The Arrow glows gold, leaves a trail, rings a bell when fired and cannot be picked up. When it
pierces a player:

| Outcome | When | What happens |
|---|---|---|
| **Stand** | Drank the Bearer's Elixir (bearer) | Never dies: a Stand always awakens, rolled by rarity |
| **Death** | 70%, not a bearer | Dies whatever their armour or totem; its own death messages (3 in `arrow-death-messages`). Marked **unworthy** |
| **Stand** | 30%, not a bearer | Survives and a Stand awakens, rolled by rarity |
| **Rejected** | Unworthy, not a bearer | The Arrow passes through: no Stand until they drink the Bearer's Elixir, which lifts the mark |
| **Resonance** | Already a Stand user | The Arrow fades: it neither kills nor gives another |

---

## 🩸 DIO and Vampire Blood

DIO drops **1–2 Vampire Blood** when he falls (`entities.dio-brando.vampire-blood-min/max`). It is
the only component of the plugin a brewing stand accepts:

| Step | Base | Ingredient | Result |
|---|---|---|---|
| 1 | Awkward Potion | **Vampire Blood** | Unstable Blood |
| 2 | Unstable Blood | Wither Rose | **Bearer's Elixir** |

Neither potion has a vanilla type, so no vanilla recipe can turn them into something else.

### Drinking the Bearer's Elixir

- You become a **Stand bearer**: if the Arrow chooses you, your Stand awakens.
- You become a **vampire** (stored in the player: it survives restarts and deaths):
  - ☀ **The sun burns you**: in daytime under the open sky you take `vampire.sun-damage` (2)
    **true damage** every second. It goes straight to your health past armour, Resistance,
    enchantments, absorption hearts and totems. A roof protects you; so does rain
    (`rain-protects`).
  - Dying to the sun shows its own death message, one of three (`vampire.sun-death-messages`).
  - 🌙 **The night is yours**: Strength I and Speed I at night, night vision always and 15%
    lifesteal on melee hits (`vampire.lifesteal`).

---

## 「」 The Stands

| Stand | Rarity | Abilities |
|---|---|---|
| Hermit Purple | 30 | **F**: Spirit Photography — every living thing within 48 blocks glows; the nearest player is named |
| Magician's Red | 25 | **F**: Crossfire Hurricane — a fan of 3 burning ankhs (6 damage, no block fire) |
| Crazy Diamond | 18 | **F**: Restoration — heals 8 to the player you look at (or you) and mends half of what they hold |
| Killer Queen | 14 | **Passive**: First Bomb · **F or `/stand sha`**: Sheer Heart Attack |
| Star Platinum | 10 | **F**: ORA ORA barrage · **Sneak + left click**: stops time 1.5 s |
| The World | 3 | **F**: MUDA MUDA barrage · **Sneak + left click**: ZA WARUDO, 3 s |

- **Sneak + F** (swap hands) summons or sends back the Stand. It floats behind the right shoulder
  with an aura in its colour and steps forward for barrages.
- Rarity, damage, radius and cooldowns live under `stands.<stand>` in config.yml.
- **Stopping time** freezes players, mobs and projectiles in the radius. Bosses and Star Platinum
  or The World users keep moving.

### 💣 Killer Queen

- **First Bomb (passive)**: every player you hit also takes an explosion (`explosion-damage` 4,
  one per `explosion-cooldown-ms` 1500). `include-mobs: true` blows up mobs too;
  `require-summoned: true` needs the Stand out.
- **Sheer Heart Attack** (`/stand sha`, or F with Killer Queen out): a menu with the head of every
  online player opens. Pick one and a small skull-faced tank rolls out:
  - **Immortal**: made of displays, it has no health and no hitbox.
  - **Slow** (`sha-speed` 0.12 blocks per tick); it rolls along the ground, climbing steps.
  - When it cannot go on (a wall, a cliff, a ceiling) it **phases through the blocks** towards its
    target and rolls again as soon as there is ground.
  - It **keeps its chunk loaded**, so it never stops for lack of players.
  - It **explodes** on reaching its target (`sha-damage` 18, no block damage).
  - If the target **leaves the server, it vanishes**. In another world, it waits where it is.
  - One at a time, `sha-cooldown-seconds` 300.

---

## ⚔️ Boss weapons

| Weapon | Recipe | Abilities |
|---|---|---|
| **Executioner's Guillotine** (netherite axe) | `E C E / · A · / · W ·` — 2 Executioner's Edges, iron chain, netherite axe, Executioner's Warrant | Sentence (+40% below 30% health), Bleed (Wither I), Chains of Judgment (right click), Guillotine Drop (sneak + right click) |
| **Architect's Deployer** (bow) | `· E · / N K N / · B ·` — echo shard, 2 netherite ingots, Architect Kernel, bow | Guided packets (+20%), sudo rm -rf (sneak + left click, 40 block beam), Failover (below 30% health you jump behind the attacker) |

NIX always drops **1 Executioner's Edge** and a second one 35% of the time (`edge-drop-chance`,
`edge-bonus-chance`). JACKSTAR still drops his **Architect Kernel**, which besides summoning him is
now the core of the Deployer.

---

## ⌨️ Commands

| Command | Who | What it does |
|---|---|---|
| `/stand` | everybody (`msc.stand`) | Your Stand, whether you are a bearer or a vampire, and your abilities |
| `/stand summon` · `/stand ability [1\|2]` · `/stand sha` | everybody | Same as the keys |
| `/stand give <player> [stand]` | `msc.admin` | Awakens a Stand (random when not named) |
| `/stand remove <player>` | `msc.admin` | Takes the Stand away |
| `/stand vampire <player> <on\|off>` | `msc.admin` | Gives or cures DIO's blood |
| `/stand arrow <player>` | `msc.admin` | Pierces the player with the Arrow (testing) |

`/msc give` includes `vampireblood`, `unstableblood`, `bearerelixir`, `executioneredge`,
`executionerguillotine` and `architectdeployer`.
