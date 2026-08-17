# 🏗️ Architecture

This page is for developers who want to extend MultiverseCreatures. It explains the layout, the major subsystems, and the conventions you must follow.

> If you are not a contributor, you can stop reading — but if you are, the conventions below are **mandatory** for new code.

---

## Source layout

```
src/main/java/com/Chagui68/
├── MultiverseCreatures.java          Plugin entrypoint: onEnable/onDisable, recipe + listener registration
├── ability/                           Player abilities (FreezeAbility)
├── commands/                          /msc command executor + tab completer
│   └── MSCCommand.java
├── entities/
│   ├── boss/                          ArmorStandBoss + attack framework (THE OBSIDIAN SENTINEL)
│   │   ├── ArmorStandBoss.java        Boss class: spawn, phases, shield, bar, AI ticker, attack registry
│   │   ├── MagicSealListener.java      Particle seal rendering (NOT a Listener; consumed by the boss)
│   │   ├── BossInstance.java           Per-instance boss state struct
│   │   └── attack/
│   │       ├── BossAttack.java              Interface: execute(BossInstance), getName()
│   │       ├── BossAttackBase.java          Abstract base: boss/plugin/random/sealDamage helpers
│   │       ├── aerial/                      13 air attacks (starfall, airslam, ...)
│   │       ├── ground/                      11 ground attacks (shieldbash, groundslam, ...)
│   │       └── ranged/                      12 ranged attacks (meteorstorm, spiritbeam, ...)
│   ├── miniboss/                      DioBoss.java, Mahoraga.java
│   ├── Kinger.java                    ♟️ chess-piece miniboss (ArmorStand + ItemDisplay suit)
│   ├── DiscTrader.java                Librarian villager selling music discs
│   └── handler/
│       └── MobHandler.java            Natural-spawn router (registered externally)
├── items/
│   ├── armor/                         EightHandledWheel, ObsidianBastion
│   ├── components/                    16 crafting ingredients (VoidEssence, MagmaCore, ...)
│   ├── dio/                           DioStandHead
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
└── utils/
    ├── ItemBuilder.java               Fluent builder for ItemStacks (lore, PDC tags, enchants)
    └── MscEntityUtils.java            setAttribute, spawnTagged, permanentFireResistance,
                                       isValidTarget, handleDeath — shared mob utilities
```

---

## Architectural conventions

### 1. Boss attacks — polymorphic registry

Every attack of THE OBSIDIAN SENTINEL lives as its own class extending `BossAttackBase`, located under `entities/boss/attack/{aerial,ground,ranged}/`. They are registered in `ArmorStandBoss.initAttacks()` and dispatched polymorphically via:

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

All custom entities receive an `MSC_<name>` scoreboard tag (e.g. `MSC_ObsidianGuard`, `MSC_DioStand`, `MSC_ArmorBossSummoned`). Mahoraga's MSC-friendly-fire rule and the boss's summon protections both rely on these tags: any damage event between two `MSC_*`-tagged entities is cancelled.

### 6. Packet handlers

`MantisClawsHandler` registers a Netty packet handler to intercept `ServerboundPlayerInputPacket` — needed to detect "rising edge" jump inputs for the wall-jump. The handler is injected into the player's channel on join and removed on quit. Any new mechanic requiring raw input edge detection should follow this pattern.

---

## Adding new content

| To add... | Steps |
|---|---|
| **New item** | 1. Create a class under `items/<category>/` using `ItemBuilder`. Expose `public static final ItemStack` + `NamespacedKey KEY`.<br>2. Register the recipe in `MultiverseCreatures.registerRecipes()`.<br>3. Create a `listener/XxxHandler implements Listener` for its right/left-click/consume behaviour (use `MscEntityUtils.isCreativeOrSpectator` for game-mode guards).<br>4. Register the handler in `MultiverseCreatures.onEnable()`: `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **New mob** | 1. Create a class under `entities/<...>/` implementing `Listener`. Use `MscEntityUtils.spawnTagged/setAttribute/handleDeath`.<br>2. Self-register in the constructor.<br>3. Instantiate it once in `MultiverseCreatures.onEnable()` so it's alive to receive events.<br>4. (Optional) Register a spawn replacement route inside `MobHandler`. |
| **New boss attack** | 1. Create a class extending `BossAttackBase` under `entities/boss/attack/<aerial\|ground\|ranged>/` returning a unique `getName()`.<br>2. Delegate shared logic via `boss.<helper>()` (the base class already exposes `boss`, `plugin`, a `Random`, `sealDamage(...)`, etc.).<br>3. Register it in `ArmorStandBoss.initAttacks()` with `registerAttack(new XxxAttack(this))`. No edits to dispatch code needed. |
| **New tool/weapon handler** | 1. Create `listener/XxxHandler implements Listener`.<br>2. Use `MscEntityUtils.isCreativeOrSpectator` for game-mode guards.<br>3. Register it in `MultiverseCreatures.onEnable()` via `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |

---

## Code style

- **No comments in code** — names and structure must self-document.
- **Fluent item construction** — always via `ItemBuilder`, never `new ItemStack(...) + ItemMeta` inline.
- **PDC tags for identification** — never compare by display name.
- **Modern `AttributeModifier`** — `NamespacedKey` constructor only.
- **MSC prefix** — every custom entity gets an `MSC_<Name>` scoreboard tag.

---

## Build

```bash
mvn clean package -DskipTests
```

Output: `target/MultiverseCreatures-v${project.version}.jar`

Dependencies (all `provided` by Paper/Purpur at runtime):
- `org.purpurmc.purpur:purpur-api:1.21.11-R0.1-SNAPSHOT`
- `org.joml:joml:1.10.5` (3D Display Entities)
- `io.netty:netty-all:4.1.82.Final` (packet interception)
- `com.google.code.gson:gson:2.10.1` (schematic JSON parsing)
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
| Player ability | `ability/FreezeAbility.java` |
| Packet interception | `listener/MantisClawsHandler.java` |
| Music engine | `music/NBSSong.java`, `music/MusicManager.java`, `music/MusicDisc.java` |
| Jukebox discs | `listener/misc/DiscJukeboxHandler.java`, `entities/DiscTrader.java` |
