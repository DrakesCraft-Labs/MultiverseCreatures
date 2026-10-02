# 🏗️ Architecture

This page is for developers who want to extend MultiverseCreatures. It explains the layout, the major subsystems, and the conventions you must follow.

> If you are not a contributor, you can stop reading — but if you are, the conventions below are **mandatory** for new code.

---

## Source layout

```
src/main/java/com/Chagui68/
├── MultiverseCreatures.java          Plugin entrypoint: onEnable/onDisable, recipe + listener registration
├── commands/                          /msc command executor + tab completer
│   ├── MSCCommand.java                 Dispatcher: permission gate, sub-command routing, tab completion
│   ├── CommandMenu.java                Every rendered menu + the pagination maths
│   ├── SpawnCatalogue.java             Spawnable entities: aliases, messages, help pages
│   ├── GiveCatalogue.java              Givable items: aliases, item factories, help pages
│   ├── AttackCatalogue.java            Attack names + help pages
│   ├── DummyStudio.java                Pose-dummy subsystem (spawn, poses, wings, animations, attack preview)
│   ├── SealStudio.java                 Particle-seal patterns, planes and hand-drawn shapes
│   └── MscKillFilter.java              Pure "is this one of ours?" predicates for /msc kill
├── entities/
│   ├── boss/                          ArmorStandBoss + attack framework (THE OBSIDIAN SENTINEL)
│   │   ├── ArmorStandBoss.java        Boss class: spawn, phases, shield, bar, AI ticker, attack registry
│   │   ├── MagicSealListener.java      Particle seal rendering: walks the shapes below and paints them
│   │   ├── seal/                       Pure seal geometry, tested without a server
│   │   │   ├── SealPlane.java               Where a flat shape lands: XZ lies down, XY/YZ stand
│   │   │   ├── SealGeometry.java            Circles, pentagrams, stars, spokes and their densities
│   │   │   ├── WingGeometry.java            The two wing profiles as a function of pose and frame
│   │   │   └── SealPoint.java / WingPoint.java  Plane and world points
│   │   ├── BossInstance.java           Per-instance boss state struct
│   │   ├── AttackPreview.java          Marker that lets a dummy act out attacks without damaging
│   │   └── attack/
│   │       ├── BossAttack.java              Interface: execute(BossInstance), getName()
│   │       ├── BossAttackBase.java          Abstract base: boss/plugin/random/sealDamage helpers
│   │       ├── ChoreographedAttack.java     Base of every Sentinel attack: telegraph → wind-up → blow → recovery
│   │       ├── aerial/                      18 air attacks (starfall, voidmeteor, ...)
│   │       ├── ground/                      21 ground attacks (shieldbash, cataclysm, ...)
│   │       ├── ranged/                      16 ranged attacks (meteorstorm, obsidianprison, ...)
│   │       └── defensive/                   6 defences (stoneskin, shieldseal, ...)
│   │   ├── fx/                          The choreography kit, testable without a server:
│   │   │   ├── Timeline.java / Stage.java   Tick-by-tick script and the world it plays in (LiveStage on a server)
│   │   │   ├── Pose.java / Poses.java / SentinelBody.java  Armor-stand poses and where hands, spear and shield end up
│   │   │   ├── Fx.java / Shapes.java / Palette.java        Particle brushes, geometry and the colour palette
│   │   │   └── Telegraph.java / Area.java / Missile.java / Prop.java  Warnings, hit volumes, projectiles, display props
│   │   ├── NixMoves.java / JackMoves.java  Pose tables of NIX's and JackStar's signature moves
│   ├── miniboss/                      Mahoraga.java
│   ├── Kinger.java                    ♟️ chess-piece miniboss (ArmorStand + ItemDisplay suit)
│   ├── KingerModel.java               Kinger's part geometry: pivots, second joint, walk cycle
│   ├── DiscTrader.java                Librarian villager selling music discs
│   └── handler/
│       └── MobHandler.java            Natural-spawn router (registered externally)
├── items/
│   ├── armor/                         EightHandledWheel, ObsidianBastion
│   ├── components/                    16 crafting ingredients (VoidEssence, MagmaCore, ...)
│   ├── food/                          HeadSlimeGelatin, ScoobyCookie
│   ├── misc/
│   │   ├── IceCrown, MantisClaws, MilitaryMine, WirtsLantern
│   │   └── offhand/                   FrostHeartOffhand, MarrowAegis, VeilwalkerMantle
│   └── weapons/
│       ├── magic/                     ChaosForge, SkyfireTalisman
│       ├── melee/                     CinderGreatsword, Excalibur, NullshearEdge, SoulreapScythe
│       └── ranged/                    AetherPullshot
├── listener/                          Bukkit event handlers (one per item/boss/relic system)
├── music/                             NBS song playback: NBSSong, MusicManager, MusicDisc,
│                                      DiscJukeboxHandler (jukebox discs)
├── ritual/                             Ritual structures & private boss dimension
│   ├── BossDimensionManager.java       Creates, configures and unloads boss_dimension
│   ├── BossDimensionSky.java           Red sky and fog (biome special-effects override)
│   ├── RitualStructure.java            Overworld entry ritual (7×7) and its candle checks
│   ├── BossInvocationStructure.java    Sentinel invocation circle (5×5, red candles)
│   ├── NixInvocationStructure.java     NIX's scaffold (5×5, anvil and gallows)
│   ├── JackInvocationStructure.java    JACKSTAR's terminal (5×5, core and rods)
│   └── terrain/                        The battlefield: Wasteland (shape), WastelandGenerator, TerrainNoise
└── utils/
    ├── ItemBuilder.java               Fluent builder for ItemStacks (lore, PDC tags, enchants)
    ├── MscEntityUtils.java            setAttribute, spawnTagged, permanentFireResistance,
    │                                  isValidTarget, handleDeath — shared mob utilities
    ├── MscLimb.java                   Two-segment limb kinematics (swing, fold, knee, elbow)
    ├── DisplaySuit.java               The ItemDisplay suit the dressed bosses wear
    ├── MscBossBar.java                Boss-bar bookkeeping: who sees which bar, and when it goes
    ├── MscLeftovers.java               Startup sweep of attack props left in the world
    ├── MscGeometryOverlay.java        Draws a model's joints, or replays its walk (/msc debug geometry [walk])
    └── MscText/MscLog/MscWorldPolicy   Text helpers, reported failures, world allowlist

src/main/resources/
├── plugin.yml                         command, permission nodes and usage line
└── config.yml                         every knob the code reads (contract checked by ConfigFilesGuardTest)
```

---

## Architectural conventions

### 1. Boss attacks — polymorphic registry

Every attack of THE OBSIDIAN SENTINEL lives as its own class extending `ChoreographedAttack` (itself a `BossAttackBase`), located under `entities/boss/attack/{aerial,ground,ranged,defensive}/`. An attack only writes a `Timeline` against a `Stage`: poses, particles, props and hit volumes per tick. On a server `LiveStage` plays it and locks the body for its length; in the tests `RecordingStage` plays the same script offline. They are registered in `ArmorStandBoss.initAttacks()` and dispatched polymorphically via:

```java
attackRegistry.get(name).execute(instance);
```

There are three random selectors (`executeRandomAerialAttack`, `executeRandomGroundAttack`, `executeRangedAttack`) and the `/msc attack <name>` switch — all dispatch through the same registry. **Adding a new attack becomes "create class + `registerAttack(new XxxAttack(this))`"** — no edits to dispatch code needed.

### 2. Mobs — self-registering Listener pattern

Each custom mob class implements `Listener` and self-registers in its own constructor:

```java
public XxxMob(MultiverseCreatures plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin);
    // ... spawn-time setup ...
}
```

**`MobHandler` is the exception** — it's externally registered by `MultiverseCreatures.onEnable()` because it routes natural `CreatureSpawnEvent`s to the right mob class.

### 3. Items — fluent ItemBuilder

All `ItemStack` construction goes through `utils/ItemBuilder` (fluent API):

```java
public static final ItemStack ITEM = ItemBuilder.of(Material.NETHERITE_SWORD)
        .name("§6Excalibur")
        .lore("§7The legendary blade of kings,",
              "§7forged from a fallen star's heart.")
        .tagged(KEY)              // attaches msc_<item> PDC tag
        .unbreakable()
        .customModelData(1003)
        .build();
public static final NamespacedKey KEY =
        new NamespacedKey("multiversecreatures", "msc_excalibur_sword");
```

Persistent data tags use `PersistentDataType.INTEGER` with a `NamespacedKey("multiversecreatures", "msc_<item>")` per item — **never** compare items by display name; always check the PDC key.

### 4. Attribute modifiers — modern namespaced constructor

Use the modern `AttributeModifier(NamespacedKey, double, Operation)` constructor — **NOT** the deprecated UUID-based one. `ObsidianBastionHandler` is the reference implementation for armor set bonuses:

- Idempotent `getAttribute(key)` check before adding a modifier (`getModifier(key) == null`)
- `getAttribute(key).removeModifier(key)` on cleanup

This pattern means you **don't need per-player modifier maps** — the attribute API manages re-application for you.

### 5. MSC tagging & friendly-fire

All custom entities receive an `MSC_<name>` scoreboard tag (e.g. `MSC_ObsidianGuard`, `MSC_ArmorBossSummoned`). Mahoraga's MSC-friendly-fire rule and the boss's summon protections both rely on these tags: any damage event between two `MSC_*`-tagged entities is cancelled.

### 6. Packet handlers

`MantisClawsHandler` registers a Netty packet handler to intercept `ServerboundPlayerInputPacket` — needed to detect "rising edge" jump inputs for the wall-jump. The handler is injected into the player's channel on join and removed on quit. Any new mechanic requiring raw input edge detection should follow this pattern.

### 7. The boss dimension terrain

`ritual/terrain/Wasteland` describes the battlefield as a pure function of the column and the world seed, and `WastelandGenerator` only copies each column into the chunk. A chunk never asks another chunk anything, so borders always match and generation runs in parallel.

- **Every vanilla stage stays off.** `shouldGenerateNoise()` returning true makes the server generate ordinary overworld terrain *before* the plugin's own blocks; the coliseum generator of 2.5 did exactly that, which is why its arena came out mixed with hills, water and stone.
- **The arena is a contract.** It is flat at y=40 and nothing stands on it or within 40 blocks of it, and no step around it is taller than one block: the bosses' ground queries, their walking and the seal placement depend on that.
- **Lava cannot run.** Lava fills columns up to one level, `LAVA_Y`, so it always meets more lava or rock.
- **An old world is rebuilt once.** A world that already exists is loaded as it is, so `BossDimensionManager` leaves `msc-generator.txt` in the world folder and rebuilds a dimension whose marker is missing or names another generator.

### 8. Scheduled tasks — a handle, or a cancel of its own

Every repeating task is one of two shapes, and `SchedulerHandleGuardTest` fails the build over a third:

- **A long-lived loop keeps its handle.** A ticker that lives with the plugin (one per custom mob, one per item aura, the population recount) is stored in a `BukkitTask` field and cancelled by `stopTasks()` from `onDisable`. Starting one twice cancels the previous task first: two loops walking the same `Map` is the bug this shape exists to make impossible.
- **A short-lived effect cancels itself or is handed over.** An attack or seal sequence calls `cancel()` inside its own runnable when its timer ends, or returns the `BukkitRunnable` so the boss can cancel it early (`BossInstance.flyTask`, `…shieldSealTask`). The guard reads the handle through its declaration, including `instance.field = new BukkitRunnable()` declared in another class.
- **Never a bare `new BukkitRunnable() { … }.runTaskTimer(…)`.** Bukkit cancels a plugin's tasks on disable, so nothing survives forever; the problem is that the plugin itself can no longer stop it — a reload or a second `startTicker()` would leave two loops behind, which is how twenty-four of them were found.

### 9. Magic seals — a pure shape, a thin brush

`MagicSealListener` paints particles; `entities/boss/seal/` decides where they go. The split is load-bearing, and `SealOrchestrationGuardTest` holds it down:

- **The geometry is pure.** `SealPlane`, `SealPoint`, `SealGeometry` and `WingGeometry` never import a server type, so a circle, a pentagram or a flap is tested with plain numbers. A shape has an invariant — a chord length, a radius band, a mirror symmetry — and the seal tests assert the invariant instead of a screenshot.
- **The listener walks, it does not compute.** One shared `repeat(...)` helper schedules every seal and the file does no trigonometry at all: adding a seal is picking a shape from `SealGeometry` and a color. The guard fails on a second scheduling site or a stray `Math.cos`.
- **Densities live with the shape.** Sample counts and scales (`pentagramSamples`, `celestialRadii`, the vertical 1.3×) are geometry, so a seal cannot quietly lose the floor that keeps a small star legible. Three helpers no seal called (`drawOuterRing`, `baseY`, `spawnFlameAura`) and a duplicated pentagram chord went away with the split.

---

## Adding new content

| To add... | Steps |
|---|---|
| **New item** | 1. Create a class under `items/<category>/` using `ItemBuilder`. Expose `public static final ItemStack` + `NamespacedKey KEY`.<br>2. Register the recipe in `MultiverseCreatures.registerRecipes()`.<br>3. Create a `listener/XxxHandler implements Listener` for its right/left-click/consume behaviour (use `MscEntityUtils.isCreativeOrSpectator` for game-mode guards).<br>4. Register the handler in `MultiverseCreatures.onEnable()`: `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **New mob** | 1. Create a class under `entities/<...>/` implementing `Listener`. Use `MscEntityUtils.spawnTagged/setAttribute/handleDeath`.<br>2. Self-register in the constructor.<br>3. Instantiate it once in `MultiverseCreatures.onEnable()` so it's alive to receive events.<br>4. (Optional) Register a spawn replacement route inside `MobHandler`. |
| **New boss attack** | 1. Create a class extending `ChoreographedAttack.Ground`, `.Aerial` or `.Ranged` under `entities/boss/attack/<aerial\|ground\|ranged>/` returning a unique `getName()`, and write its `choreograph(Stage)`.<br>2. Register it in `ArmorStandBoss.initAttacks()` with `registerAttack(new XxxAttack(this))`, add it to a `SentinelAttackPool` table and the aerial/ground name set. `ChoreographyTest` picks it up on its own and plays it offline. |
| **New tool/weapon handler** | 1. Create `listener/XxxHandler implements Listener`.<br>2. Use `MscEntityUtils.isCreativeOrSpectator` for game-mode guards.<br>3. Register it in `MultiverseCreatures.onEnable()` via `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **New `/msc` alias, item or attack** | 1. Add one entry to `commands/SpawnCatalogue`, `commands/GiveCatalogue` or `commands/AttackCatalogue`: the executor, the help pages and the tab completer all read that single table.<br>2. An attack still needs its class registered in `ArmorStandBoss.initAttacks()` and its `getName()` copied into the catalogue entry, plus into the aerial/ground name sets that gate when it is allowed to fire. |
| **New config option** | 1. Read it with a default (`config.getInt("path.to.key", fallback)`) so existing configs keep working.<br>2. Declare it in `config.yml` — `ConfigFilesGuardTest` fails the build when a key is read by the code but missing from the file. |

---

## Code style

- **Comments explain *why*, never *what*** — names and structure must self-document; a comment earns its place when it records a constraint, a legacy quirk or a non-obvious ordering (`SentinelPhase` and the `/msc` catalogues document their data that way).
- **Fluent item construction** — always via `ItemBuilder`, never `new ItemStack(...) + ItemMeta` inline.
- **PDC tags for identification** — never compare by display name.
- **Modern `AttributeModifier`** — `NamespacedKey` constructor only.
- **MSC prefix** — every custom entity gets an `MSC_<Name>` scoreboard tag.

---

## Build

```bash
mvn verify                  # compile + unit tests + shaded jar (what CI runs)
mvn clean package -DskipTests  # build only, no tests
```

Output: `target/MultiverseCreatures-v${project.version}.jar` (the shaded plugin jar).

Dependencies (all `provided` by Paper/Purpur at runtime except Gson, which is shaded):
- `org.purpurmc.purpur:purpur-api:1.21.11-R0.1-SNAPSHOT`
- `org.joml:joml:1.10.9` (3D Display Entities)
- `io.netty:netty-transport:4.2.18.Final` (packet interception)
- `com.google.code.gson:gson:2.14.0` (schematic JSON parsing, shaded as `com/Chagui68/libs/gson`)
- `com.google.code.findbugs:jsr305:3.0.2`
- `org.jetbrains:annotations:24.0.1`

---

## Where to look for examples

| Pattern | Reference file |
|---|---|
| Boss attack class | `entities/boss/attack/ground/GroundSlamAttack.java` |
| Item + handler | `items/weapons/melee/Excalibur.java` + `listener/ItemCombatHandler.java` |
| Armor set bonus | `items/armor/ObsidianBastion.java` + `listener/ObsidianBastionHandler.java` |
| Mob spawn routing | `entities/handler/MobHandler.java` |
| Packet interception | `listener/MantisClawsHandler.java` |
| Music engine | `music/NBSSong.java`, `music/MusicManager.java`, `music/MusicDisc.java` |
| Jukebox discs | `listener/misc/DiscJukeboxHandler.java`, `entities/DiscTrader.java` |
