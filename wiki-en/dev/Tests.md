# 🧪 Tests

This page documents **how plugin changes are tested** and **what each suite verifies**. The tests only cover high-risk pure logic and math (bosses and rituals are easily broken when touched); there is no real server dependency.

## ⚙️ Framework & execution

- **JUnit 5 (Jupiter)** — dependency `org.junit.jupiter:junit-jupiter:5.11.4` (`test` scope).
- **Maven Surefire 3.5.2** — runs the tests automatically during the `test` phase.
- **Java 21** — same compiler as the main code.
- **Headless**: no Paper/Purpur server is booted. Bukkit classes that get touched (e.g. `World`) are simulated with `java.lang.reflect.Proxy`, or `Location` objects with a `null` world are used to exercise only the arithmetic.

Commands:

```bash
mvn test                     # Runs the whole suite
mvn clean package -DskipTests  # Builds the JAR skipping tests
```

To run one class:

```bash
mvn test -Dtest=NixInvocationStructureTest
```

## 📋 Test inventory

All 12 files live in `src/test/java/com/Chagui68/`, mirroring the package of the class they exercise.

### `utils/MscEntityUtilsHealthTest` — Boss virtual health
Covers the health math in `utils/MscEntityUtils`:
- **`calculateSafeHealth`** — Clamps the requested health to the server limit (attribute cap, default 1024 on Paper) to avoid `IllegalArgumentException`. Verifies real boss cases: ArmorStandBoss (3200), NIX (450), Frost Golem (200); clamps to a minimum of **0.1** (prevents instant death on spawn) and protects against negative input.
- **`calculateVirtualProgress`** — Clamps the boss-bar progress to [0.0, 1.0] (including `0/0` = 0).
- **`calculateScaledPhysicalHealth`** — Converts **virtual** health (e.g. 3200) into the real physical health stored on the entity (scaled to its physical max), with 0 → death.

### `ritual/RitualStructureTest` — Entry ritual (overworld, 7×7)
- Ritual center at `(3, 0, 3)` with radius 5.
- **Exactly 12 candles** on layer `Y=1`, none at the center.

### `ritual/NixInvocationStructureTest` — The Executioner's Scaffold (5×5)
- 4 red candles, each at **Manhattan distance 1** from the anvil at `(2, 0, 2)` and on the ground layer (`Y=0`).
- 4 corner gallows forming the 5×5 bounding square at the corners.
- `getAnvilLocation` → `origin + (2.5, 0.5, 2.5)`.
- Gallows particles elevated to `Y + 1.8` (skull height).
- `containsCandle` accepts only the 4 candle positions and rejects the center, the corners, and anything outside the structure.

### `ritual/BossInvocationStructureTest` — Sentinel invocation circle (5×5)
- Center at `(2, 0, 2)` (where the Echo Shard is dropped).
- `containsCandle` validates the 12-candle ring and rejects the center and the outside.

### `commands/CommandHelpPaginationTest` — `/msc` pagination
- The pagination logic clamps any requested page into `[1, totalPages]` (negative inputs, `0`, and above the maximum).
- The `spawn`, `give`, and `attack` subheaders exist for every valid page (1–3, 1–4 and 1–4 respectively).

### `entities/NixModelKinematicsTest` — NIX kinematic model (27 parts)
- The model has **exactly 27** `ItemDisplay` parts (Blockbench export).
- Rigid kinematic hierarchy: arms and legs with **6** segments each (so they rotate as rigid bodies around shoulder/hip joints), head, upper and lower torso with **1** each.
- Every 16-float matrix decomposes into a finite offset, strictly positive scale, and finite quaternion; profile name and `base64` texture are non-blank.
- The model's horizontal center is finite.

### `entities/KingerOptimizationAndKinematicsTest` — Kinger model + optimization
- **15** parts (Blockbench spec) with the same matrix-integrity guarantees (16 floats, finite offset/scale/quaternion) and a finite `CENTER`.
- **Display sync throttling**: stationary with no animation syncs every 3 ticks (**30** of 90, ~66% reduction); while moving or animating it syncs **every tick** (90 of 90). This validates the `!isIdle || tickCount % 3 == 0` rule.

### `listener/bossdimension/BossDimensionGuardLogicTest` — Boss dimension guard
- Event handlers **short-circuit**: events outside the `boss_dimension` world never evaluate the permission check (it is only evaluated inside the boss world).

### `entities/handler/MobHandlerRecountTest` — Population cap cooldown
- `MobHandler.puedeRecontar` allows re-counting **per world**, respecting an independent failure cooldown per world: `world` with cooldown until `160000` does not re-count at `159999` but does at `160000`; `world_nether` is not blocked by `world`'s cooldown.

### `entities/EnderKnightWorldGuardTest` — Ender Knight teleport
- `EnderKnight.sharesWorld` only returns `true` when the worlds match; a `null` world identity is rejected. Guarantees the pull-distance math only happens within the same world.

### `entities/DistanceOptimizationEquivalenceTest` — Squared-distance optimization
- The `distanceSquared < threshold²` predicate is **equivalent** to Euclidean `distance < threshold` for every threshold used in the code (30, 25, 35, 20, 8, 6, 5, 4, 3, 2, 1.8 blocks), including the boundary (just ±0.001).
- Distance-dropoff damage computed from `sqrt(dist²)` equals the value computed with direct distance (tolerance 1e-9) for the Sentinel's Storm, Despair, and AirSlam phases.
- Out-of-range targets are discarded **before** any square root is computed.

### `entities/boss/ShockwaveAndCombatOptimizationTest` — Shockwaves and combat
- Shockwave formulas: the vertical knockback (`Y`) stays bounded in `[0.4, 0.7]` and the damage multiplier in `[0.4, 1.0]` for radii 0–30.
- **`hitInThisRing` (deduplication)**: several adjacent ring particles fall inside the player's impact radius, but with deduplication the player takes damage **exactly once per ring**.
- **NIX throttling**: stationary syncs every 3 ticks (30 of 90); during a cleave or chain animation it syncs every tick (90 of 90).

## 🗃️ Where they run

Tests execute during Maven's **`test` phase** (Surefire). They need no server or network: just the JDK 21 and the dependencies declared in `pom.xml`.

> When touching health/boss logic, ritual structures, or the NIX/Kinger kinematic models, run the full suite with `mvn test` to make sure you are not introducing regressions.