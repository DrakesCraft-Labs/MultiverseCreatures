# 🛠️ Commands

All commands use the **`/msc`** root. **Permission:** `msc.admin` (server OP by default).

```
/msc spawn <type>              Summon a mob, boss, merchant, etc.
/msc give <item> [amount]      Give yourself an item (1–64)
/msc seal <pattern> [plane]    Render a particle seal pattern
/msc dummy ...                 Spawn / pose / animate ArmorStand dummies
/msc attack <name> [range]     Trigger an ArmorStandBoss attack/mechanic
/msc music <play|stop|list|disc>  Play / stop NBS songs, get a jukebox disc
/msc dimtp <world>             Teleport across worlds
/msc cleanstands               Remove all MSC-related armor stands
```

Each command is detailed below.

---

## /msc spawn <type>

Summons a single entity (or a tactical formation) at the executor's location. The following types are supported (aliases in parentheses):

| Type | Entity |
|---|---|
| `merchant` | Multiverse Merchant ("Shaggy" Wandering Trader) |
| `dio` | Dio Brando boss |
| `mahoraga` | Mahoraga miniboss |
| `kinger` | Kinger miniboss |
| `armorstand` (`armorstandboss`) | THE OBSIDIAN SENTINEL final boss |
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
| `disctrader` | Disc Trader — librarian villager selling music discs |

Details for each entity live in [Bosses](./Bosses.md) and [Creatures](./Creatures.md).

---

## /msc give <item> [amount]

Amount defaults to 1 and can be 1–64.

### Weapons

| Item | Aliases |
|---|---|
| `excalibur` | `sword` |
| `cindergreatsword` | `greatsword` |
| `nullshearedge` | `nullshear` |
| `soulreapscythe` | `scythe` |
| `aetherpullshot` | `pullshot` |
| `skyfiretalisman` | `talisman` |
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
| `diostand` | `dio` |
| `militarymine` | `mine` |
| `scoobycookie` | `cookie` |
| `headslimegelatin` | `gelatin` |

### Components

| Item | Aliases |
|---|---|
| `scoobycookie` (above) | `cookie` |
| `starcore` | `star` |
| `militarycomponent` | `component` |
| `headslimeheart` | `heart` |
| `chaosorb` | — |
| `enderfragment` | `ender` |
| `frostheart` | `frost` |
| `magmacore` | `magma` |
| `obsidianshard` | `shard` |
| `reaperessence` | `reaper` |
| `reinforcedbone` | `bone` |
| `bonemarrow` | `marrow` |
| `ossifiedplate` | `plate` |
| `moltenmarrow` | — |
| `shadowcloak` | `cloak` |
| `stormcrystal` | `storm` |
| `venomgland` | `venom` |
| `voidessence` | `void` |
| `wheelessence` | `whelessence` |

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

**Animations:** `flyup`, `land`, `airslam`, `shieldseal`, `healingcircle` (`heal`), `rain`, `pentagram`, `trianglecall` (`triangle`)

---

## /msc attack <name> [range]

Triggers an ArmorStandBoss attack, defense, or phase-transition mechanic by name. Finds the nearest boss within `range` blocks (default `aggro-range` = 50) and executes.

### Ground attacks (11)

`groundslam`, `groundshatter`, `shieldbash`, `lancestorm`, `earthpillar`, `chaingrapple`, `warstomp`, `armorspikes`, `vortexpull`, `mirrorimage`, `doombeam`

### Aerial attacks (13)

`starfall`, `aerialrush`, `sonicboom`, `lightningstorm`, `gravitywell`, `crossslash`, `novaburst`, `darkorb`, `windcutter`, `heavenlyjudgment`, `rainoflances`, `airslam`, `hoverbarrage` (alias `crossbarrage`)

### Ranged attacks (12)

`lancesnipe`, `meteorstorm`, `voidbeam`, `frostlance`, `lightningspear`, `shadowvolley`, `chainlightning`, `crystalbarrage`, `arcaneorb`, `voidrift`, `arcanemissiles`, `spiritbeam`

### Phase transitions

`phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`

### Defensive states

`stoneskin`, `reflectbarrier`, `absorbshield`

### Mechanics & misc

`trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`

The full list and details are on the [Bosses wiki page](./Bosses.md).

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

Iterates all worlds and removes every ArmorStand whose scoreboard tag starts with `MSC_`. Useful to clean up after a boss fight or a crash during a battle. **Cleans up the boss's Stand companions, summoned ItemDisplays, dead or stale air-boss templates, and player Dio Stands.**

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `msc.admin` | OP only | Required for ALL `/msc` subcommands |

There are no per-item or per-mob permissions yet. Server admins can gate the command behind a permission plugin (e.g. LuckPerms) by giving `msc.admin` only to trusted staff.
