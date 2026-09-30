# 🛠️ Commands

All commands use the **`/msc`** root. **Permission:** `msc.admin` (server OP by default). `msc.admin.bypass` (also OP) skips the protected boss-dimension build and teleport rules.

```
/msc                           Show structured, categorized help menu
/msc spawn <type>              Summon a mob, boss, military strike, etc. (/msc spawn help [1-3])
/msc give <item> [amount] [player]  Give legendary weapons, armor or components (/msc give help [1-4])
/msc seal <pattern> [plane]    Render a particle seal pattern
/msc dummy ...                 Spawn / pose / animate dummies, preview boss attacks
/msc attack <name> [range]     Trigger an ArmorStandBoss attack/mechanic (/msc attack help [1-4])
/msc music <play|stop|list|disc>  Play / stop NBS songs, get a jukebox disc
/msc dimtp <world>             Teleport across worlds
/msc cleanstands [world]       Remove all MSC-related armor stands (optionally by world)
/msc kill [type|all] [radius]  Safely kill/purge MSC custom creatures
/msc debug [player]            Break down each boss's damage to and from a player
/msc reload                    Reload config.yml and sync entities and bosses
```

Each command is detailed below.

The aliases accepted by `spawn`, `give` and `attack` — and the ones offered by tab completion — are declared in a single table per command (`SpawnCatalogue`, `GiveCatalogue` and `AttackCatalogue`): the same table feeds the executor, the help menu and the completer.

---

## /msc spawn <type>

Summons a single entity (or a tactical formation) at the executor's location. The following types are supported (aliases in parentheses):

| Type | Entity |
|---|---|
| `merchant` | Multiverse Merchant ("Shaggy" Wandering Trader) |
| `mahoraga` | Mahoraga miniboss |
| `kinger` | Kinger miniboss |
| `garou` | Garou [Hero Hunter] miniboss |
| `nix` (`executioner`, `nixelverdugo`) | NIX - The Executioner (custom 27-part model boss) |
| `armorstand` (`armorstandboss`) | THE OBSIDIAN SENTINEL final boss |
| `jack` | JACK STAR — The System Architect (5 phases, 3 lives) |
| `creeperjr` | Creeper Jr. (×3 — spawns in trio) |
| `headslime` | Head Slime |
| `zombietrap` (`army`) | Military Zombie Horse trap (full 5-unit army ambush) |
| `tank` | Zombie Tank (single unit) |
| `duelist` | Military Skeleton Duelist |
| `lancer` | Zombie Lancer + ZombieHorse |
| `camel` | Army Camel with riders |
| `sniper` | Sniper Skeleton |
| `boneshield` (`bone`) | Bone Shield |
| `chaosmage` (`chaos`) | Chaos Mage |
| `enderknight` (`ender`) | Ender Knight |
| `flameelemental` (`flame`) | Flame Elemental |
| `frostgolem` (`frost`) | Frost Golem |
| `obsidianguard` (`obsidian`) | Obsidian Guard |
| `shadowrogue` (`rogue`) | Shadow Rogue |
| `soulreaper` (`reaper`) | Soul Reaper |
| `stormcaller` (`storm`) | Storm Caller |
| `venomwitch` (`venom`) | Venom Witch |
| `voidcrawler` (`void`) | Void Crawler |
| `warlord` | Orcish Warlord (berserker rage) |
| `disctrader` | Disc Trader — librarian villager selling music discs |

Details for each entity live in [Bosses](./Bosses.md) and [Creatures](./Creatures.md).

---

## /msc give <item> [amount] [player|@a|@p|@r|@s]

Amount defaults to 1 and can be 1–64. Without a target the item goes to the executor; otherwise it accepts an exact player name or a selector (`@e` is rejected on purpose).

### Weapons

| Item | Aliases |
|---|---|
| `excalibur` | `sword` |
| `cindergreatsword` | `greatsword` |
| `nullshearedge` | `nullshear` |
| `soulreapscythe` | `scythe` |
| `venomfang` | `dagger` |
| `aetherpullshot` | `pullshot` |
| `skyfiretalisman` | `talisman` |
| `sentinelgrimoire` | `grimoire` |
| `chaosforge` | — |

### Armor & relics

| Item | Aliases |
|---|---|
| `eighthandledwheel` | `wheel` |
| `obsidianbastionhelmet` | `bastionhelmet` |
| `obsidianbastionchestplate` | `bastionchestplate` |
| `obsidianbastionleggings` | `bastionleggings` |
| `obsidianbastionboots` | `bastionboots` |
| `marrowaegis` | `aegis` |
| `veilwalkermantle` | `mantle` |
| `frostheartoffhand` | `frostoffhand` |

### Misc items

| Item | Aliases |
|---|---|
| `icecrown` | `crown` |
| `mantisclaws` | `claws` |
| `wirtslantern` | `lantern` |
| `militarymine` | `mine` |
| `scoobycookie` | `cookie` |
| `headslimegelatin` | `gelatin` |

### Components

| Item | Aliases |
|---|---|
| `starcore` | `star` |
| `militarycomponent` | `component` |
| `swordmold` | `mold` |
| `headslimeheart` | `heart` |
| `chaosorb` | — |
| `chaospowder` | — |
| `chaosfragment` | — |
| `chaoscore` | — |
| `condensedchaosorb` | `condensed` |
| `enderfragment` | `ender` |
| `frostheart` | `frost` |
| `magmacore` | `magma` |
| `obsidianshard` | `shard` |
| `reaperessence` | `reaper` |
| `reinforcedbone` | `bone` |
| `reinforcedboneblock` | — |
| `bonemarrow` | `marrow` |
| `ossifiedplate` | `plate` |
| `moltenmarrow` | — |
| `shadowcloak` | `cloak` |
| `stormcrystal` | `storm` |
| `venomgland` | `venom` |
| `voidessence` | `void` |
| `wheelessence` | `whelessence` |

### Boss catalysts & core blocks

| Item | Aliases |
|---|---|
| `wheelcore` | — |
| `moltenwheelcore` | `moltenwheel` |
| `refinedwheelcore` | `refinedwheel` |
| `reapercore` | — |
| `sentinelcore` | `sentinel` |
| `endercore` | — |
| `multiversalcore` | `multiverse` |
| `compressedgoldblock` | `goldblock` |
| `refinednetherite` | — |
| `moltennetherite` | `molten` |
| `executionerwarrant` | `warrant`, `deathwarrant` |
| `architectkernel` | `kernel`, `architect` |

> Note: some aliases overlap (`bone` = Reinforced Bone component, but `bone` is **also** the spawn alias for Bone Shield). Context (spawn vs give) disambiguates.

---

## /msc seal <pattern> [plane]

Renders a particle seal pattern around the executor. The fake spell-casting engine used by the Obsidian Sentinel boss; provided as a creative toy for server admins.

**Patterns:**

```
pentagram   triangle / runic   celestial   circle   ring
star        floating / shield  wings       wings2
vortex      quake              divine      storm
```

**Planes** (optional):

- `horizontal` (`h` / `xz`) — default, drawn on the ground plane
- `vertical-north` (`vertical` / `v` / `xy`) — drawn on the X-Y (north-facing) plane
- `vertical-east` (`ez` / `yz`) — drawn on the Y-Z (east-facing) plane

---

## /msc dummy ...

Manipulates an ArmorStand dummy used for posing/content preview. Useful for designing boss animations without running the full boss fight.

| Subcommand | Behaviour |
|---|---|
| `spawn` | Spawn a fresh dummy at your location |
| `remove` | Remove the dummy |
| `set <part> <x> <y> <z>` | Set pose of a body part |
| `<part> <axis> <degrees>` | Rotate a body part on an axis |

**Parts:** `rightarm`, `leftarm`, `body`, `head`, `rightleg`, `leftleg`
**Axes:** `x` / `pitch`, `y` / `yaw`, `z` / `roll`

| Subcommand | Behaviour |
|---|---|
| `wings` / `wings2` / `nowings` | Toggle wing-pose presets |
| `animate <anim>` | Play a named preset animation |
| `attack <attack\|random>` | Preview a real Sentinel attack on the dummy |
| `attack list [page]` | List every attack the dummy can perform |

**Animations:** `flyup`, `land`, `airslam`, `shieldseal`, `healingcircle` (`heal`), `rain`, `pentagram`, `trianglecall` (`triangle`)

**Attack preview:** `attack <attack|random>` makes the dummy perform a real Sentinel attack — the same attack object the boss runs, with its choreography, particles and seals — so an animation can be reviewed on a test server without starting a fight. Nothing the dummy lands can damage anyone: every attack damages through one helper, and a performing dummy is refused there. Effects and knockback still play, so watch from a step back. `attack list [page]` lists every name, and tab completion offers them too.

---

## /msc attack <name> [range]

Triggers an ArmorStandBoss attack, defense, or phase-transition mechanic by name. Finds the nearest boss within `range` blocks (default `aggro-range` = 50) and executes.

### Ground attacks (18)

`groundslam`, `groundshatter`, `shieldbash`, `lancestorm`, `earthpillar`, `chaingrapple`, `warstomp`, `armorspikes`, `vortexpull`, `mirrorimage`, `doombeam`, `lanceflurry`, `whirlwindslash`, `executionsweep`, `obsidianspire`, `earthmaw`, `shadowstep`, `runeward`

### Aerial attacks (16)

`starfall`, `aerialrush`, `sonicboom`, `lightningstorm`, `gravitywell`, `crossslash`, `novaburst`, `darkorb`, `windcutter`, `heavenlyjudgment`, `rainoflances`, `airslam`, `hoverbarrage` (alias `crossbarrage`), `eclipsefall`, `bladering`, `obsidianwings`

### Ranged attacks (15)

`lancesnipe`, `meteorstorm`, `voidbeam`, `frostlance`, `lightningspear`, `shadowvolley`, `chainlightning`, `crystalbarrage`, `arcaneorb`, `voidrift`, `arcanemissiles`, `spiritbeam`, `soultethers`, `plaguebrand`, `runemines`

### Phase transitions

`phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`

### Defensive states

`stoneskin`, `reflectbarrier`, `absorbshield`

### Mechanics & misc

`trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`

The full list and details are on the [Bosses wiki page](./Bosses.md).

---

## /msc debug [player]

Diagnoses boss damage. Without an argument it targets the player you are looking at (within 30 blocks); you can also name a player, which is the only way to use it from the console.

The report has one section per boss (the Obsidian Sentinel, Nix and Jack Star) and reads the **most recent sample** for that player in both directions, so let the boss hit them (or hit the boss) once first:

- **DEALT to player** — the attack, the damage it asked for (`Intended`) and what the player actually took (`Applied`), after their armour and effects.
- **TAKEN from player** — the hit as it arrived (`Hit`), the boss's own mechanic (`cap N`, the load-balancer split, the defence multipliers, a dodge…), and the damage the boss applied.

The Sentinel's `DEALT` entry is the **penetrating breakdown** (event damage, the armour/Protection/Resistance credited back, the per-hit cap, the Resistance pierce), because every hit it lands goes through that pipeline. Nix's `TAKEN` entry shows its `max-damage-per-hit` cap; Jack Star's shows the Load Balancer split.

```
OBSIDIAN SENTINEL
  ▸ DEALT to player · penetrating
      Event : 3.52 · armour : -17.60 · protection : 0.00 · resistance : -0.88
      Through armour : 22.00 · cap : 15.00 · pierce : 20% · Resistance : level 1
      Final damage dealt : 12.60
  ▸ TAKEN from player · Incoming hit
      Hit : 120.00 · stone skin ×0.5, cap 50.0 · Applied : 50.00

NIX - THE EXECUTIONER
  ▸ DEALT to player · Guillotine Cleave
      Intended : 22.00 · Applied : 9.90
  ▸ TAKEN from player · Incoming hit
      Hit : 300.00 · cap 100.0 · Applied : 100.00

JACK STAR - THE SYSTEM ARCHITECT
  ▸ DEALT to player · Three-Slash
      Intended : 16.00 · Applied : 8.40
  ▸ TAKEN from player · Incoming hit
      Hit : 134.00 · load balancer: 46.9 shared · Applied : 87.10
```

Sections with nothing recorded are skipped; if no boss has touched the player yet the command says so instead of printing an empty breakdown. The samples are transient: a player's are dropped when they disconnect and a boss's when it dies or despawns, so the report always reflects the current session. The caps and multipliers live in `entities.<boss>.*` in `config.yml`; see [Installation](./Installation.md) and [Bosses](./Bosses.md) for what each knob does.

---

## /msc music <play|stop|list|disc> [song] [loop]

Plays any `.nbs` file from `plugins/MultiverseCreatures/music/`. Songs are played via the `MusicManager` (note-block-stub protocol packets) to all nearby players within a configurable radius.

```
/msc music list                List all songs in the music folder
/msc music play Undertale-Megalovania true   Play (loop=true)
/msc music stop                 Stop current song
/msc music disc Megalovania     Give yourself the jukebox disc of a song
```

`/msc music disc <song>` gives the matching jukebox disc — insert it in a jukebox to play the song, right-click with an empty hand to eject it. See [Music](./Music.md) for the bundled songs, credits and the Disc Trader.

---

## /msc dimtp <world>

Teleports the executor across worlds/dimensions. Used for testing the boss dimension scaffolding and for quickly jumping between overworld/nether/the_end.

---

## /msc cleanstands

Iterates all worlds and removes every ArmorStand whose scoreboard tag starts with `MSC_`. Useful to clean up after a boss fight or a crash during a battle. **Cleans up the boss's Stand companions, summoned ItemDisplays, dead or stale air-boss templates.**

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `msc.admin` | OP only | Required for ALL `/msc` subcommands |
| *your own nodes* | — | Optional per-subcommand gates, declared under `commands.subcommand-permissions` |

There are no per-item or per-mob permissions yet. Server admins can gate the command behind a permission plugin (e.g. LuckPerms) by giving `msc.admin` only to trusted staff.

`commands.subcommand-permissions` maps a subcommand to one extra node. A subcommand named there needs its node **on top of** `msc.admin`; anything absent or blank stays open to whoever passed the main gate, so the shipped `{}` changes nothing:

```yaml
commands:
  permission: "msc.admin"
  op-only: true
  subcommand-permissions:
    debug: msc.debug
    attack: msc.attack
```
