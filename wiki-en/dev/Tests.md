# 🧪 Tests

This page documents **how plugin changes are tested** and **what each suite verifies**. The tests only cover high-risk pure logic and math (bosses and rituals are easily broken when touched); there is no real server dependency.

## ⚙️ Framework & execution

- **JUnit 5 (Jupiter)** — dependency `org.junit.jupiter:junit-jupiter:5.11.4` (`test` scope).
- **Maven Surefire 3.5.2** — runs the tests automatically during the `test` phase.
- **Java 21** — same compiler as the main code.
- **Headless**: no Paper/Purpur server is booted. Bukkit classes that get touched (e.g. `World`) are simulated with `java.lang.reflect.Proxy`, or `Location` objects with a `null` world are used to exercise only the arithmetic.
- **SnakeYAML** (shipped with `purpur-api`) parses `config.yml` and `plugin.yml` in `ConfigFilesGuardTest`, so an invalid indent fails the suite instead of the server start.
- **Test support** (`testsupport/ProjectPaths`, `testsupport/LimbGeometry`) is the harness the guards share: it finds the project by walking up from the working directory (a source-reading guard used to read `src/main/java` from wherever Maven was started, which found nothing and passed vacuously), names a file by path segments with one failure mode, and reads a limb's second joint out of the export instead of trusting the code that hardcodes it.

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

All 47 files live in `src/test/java/com/Chagui68/`, mirroring the package of the class they exercise.

### `utils/MscEntityUtilsHealthTest` — Boss virtual health
Covers the health math in `utils/MscEntityUtils`:
- **`calculateSafeHealth`** — Clamps the requested health to the server limit (attribute cap, default 1024 on Paper) to avoid `IllegalArgumentException`. Verifies real boss cases: ArmorStandBoss (3200), NIX (450), Frost Golem (200); clamps to a minimum of **0.1** (prevents instant death on spawn) and protects against negative input.
- **`calculateVirtualProgress`** — Clamps the boss-bar progress to [0.0, 1.0] (including `0/0` = 0).
- **`calculateScaledPhysicalHealth`** — Converts **virtual** health (e.g. 3200) into the real physical health stored on the entity (scaled to its physical max), with 0 → death.
- **`clampHitboxScale`** — Every boss's stand scale is clamped to `0.25`–`8.0`: the boundaries themselves stay usable, `0`, negatives, `NaN` and both infinities fall back to a plain stand (`1.0`), a huge value caps at `8.0`, and the four scales the bosses actually ship (`1.0`, `1.2`, `1.9`, `7.5`) pass through untouched.

### `utils/MscGeometryOverlayTest` — Geometry drawn in the world
- The hitbox overlay is drawn along the **twelve edges** of the box: each edge runs on exactly one axis, they add up to four of each side length and they meet at exactly the eight corners — an outline missing an edge would hide the gap the audit is looking for. A degenerate (empty) box still draws instead of throwing.
- A joint is placed in the **same frame the display pieces use** (`yaw + 180`): a stand facing north mirrors the model point, a quarter turn maps it onto the other axis, and no rotation ever changes its height or its distance from the stand.
- The joints the command actually hands over (Kinger's, NIX's and Jack Star's real pivot constants, **elbows and knees included**) all sit inside a body: within the stand's height, near the body axis and on its own plane.

### `utils/DisplaySuitTest` — The suit every dressed boss wears
- An adoption recognises a piece only when its **suit, piece and owner tags all agree**: a piece of another boss, another piece of the same suit, or a suit tag without the rest is never taken.
- The search skips entities that are not displays, returns nothing when there is nothing to adopt, and ignores a missing world or location.
- The suit is built in **one place**: a source guard fails if any of the three dressed bosses builds its own head from the skin texture, zeroes its own display settings or forgets to adopt/remove its pieces.

### `utils/MscLeftoversTest` — Startup sweep of attack props
- The props of an attack that a restart cut short are removed at enable (orbiting shields, the planted shield holder, the lance ring, the wing panels, the triangle seal, the mirror copies, Kinger's bullets), because their attack object is gone and nothing else would ever remove them.
- The list is tested from the other side too: **creatures** (`MSC_FrostGolem`, the summons), **suit pieces** (each boss adopts its own) and **hand-placed markers** (`MSC_Dummy`, `MSC_SealMarker`) are never swept.
- The sweep only touches the entities it recognises — it does not unload the world around them — and a missing world is ignored instead of throwing.

### `utils/MscBossBarTest` — Who sees a boss bar
- A boss bar is a packet per player, not a world object, so the viewer list has to be revisited: a player who **logs in later** gets the bar and one who already had it is never added twice.
- A viewer who **logs out** or moves to another world stops seeing the bar; a distance-limited bar (Jack Star's) swaps its viewers for the players inside the range instead of accumulating them.
- A missing bar, world or location is ignored instead of throwing, so a bar handed over mid-teardown cannot break the ticker.

### `utils/MscLimbTest` — The second joint of a limb
- The joint between two export segments is **halfway between their centres**, so nothing has to be assumed about how big a piece is and a re-export moves the joint with it; a still limb rests exactly where the export puts it, second joint and all.
- Over the whole range a walk can reach, the lower segment keeps its exact distance from the second joint — it cannot come away from the limb — and stays below that joint instead of folding up through the thigh.
- A **knee** folds only on the back half of the swing and an **elbow** only on the forward half, both by a documented share of the parent's swing (`1.5×`, capped at `1.2` rad ≈ 69°) and always in the direction the limb is already going, so a joint adds to the swing instead of leading or cancelling it.

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
- Drives the real `commands/CommandMenu` helpers (not a local copy): `clampPage` clamps any requested page into `[1, totalPages]` (negative inputs, `0`, and above the maximum), `pageCount` always covers every line with at least one page, and `pageSlice` returns exactly one non-overlapping window per page.
- Category prefixes keep the legacy `&6&lLabel&8:` / `   &e• &fitem` formatting.
- The `spawn`, `give`, and `attack` titles and help lines exist for every valid page (1–3, 1–4 and 1–4 respectively), and the self-paginated `dummy` and `seal` menus still fit in two pages of 12 lines.

### `commands/SpawnCatalogueTest` — `/msc spawn` data table
- The three help pages are byte-for-byte the text the command printed before the table was extracted.
- Every alias resolves back to its own type, aliases are unique, lowercase and free of blanks, and the legacy shortcuts (`army`, `rogue`, `flame`, …) are all offered by tab completion.
- Success/failure messages match the old per-branch wording (`Spawned Military Zombie Horse trap!` / `Failed to spawn trap.`).
- Spawnable-but-undocumented kinds (JackStar) stay out of the help menu, and the Jack boss answers to **exactly one alias** (`jack`): the retired `jackstar`/`arquitecto`/`systemarchitect` shortcuts no longer resolve from commands nor appear in tab completion.

### `commands/GiveCatalogueTest` — `/msc give` data table
- The four help pages are byte-for-byte the text the command printed before.
- Every item named in the help text is a real, givable alias (grouped lines like `reaperessence &8/ &evoidessence` included), and unknown aliases return `null` instead of throwing.
- Aliases are unique and lowercase, and every entry declares an item factory.

### `commands/AttackCatalogueTest` — `/msc attack` data table
- The four help pages are byte-for-byte the text the command printed before.
- Tab completion names are the documented attacks, unique and lowercase; every entry sits on a page that exists and shares the `&7` body colour.

### `commands/AttackRegistryCoherenceTest` — the five attack lists stay in agreement
- Reads the attack sources and `ArmorStandBoss` and checks that every attack class is registered in `initAttacks()` and answers to the name derived from its class (with the documented exceptions `executionsweep`, `soultethers`, `runemines`).
- The aerial/ground sets match the folders the classes live in, no attack sits in both sets, and the ranged/defensive ones stay in neither.
- Every name quoted by the random rotation arrays is a registered attack.
- The help catalogue and the sources are the same set of names, so nothing is advertised that cannot run and nothing runnable stays hidden.

### `commands/MscKillFilterTest` — `/msc kill` predicates
- `MSC_`-prefixed scoreboard tags identify a plugin entity; the legacy untagged names (Mahoraga, Garou, Bone Shield, …) still count; vanilla mobs are left alone.
- The type filter matches tags with `-`/`_` stripped and falls back to a name substring; `null`/blank types never match.

### `commands/DummyAttackPreviewTest` — Dummy attack preview
- Every attack documented in `AttackCatalogue` is one the dummy accepts, in any case, and an unknown name is refused instead of quietly running something else; `random` picks from that same list, reproducibly.
- Tab completion and the dummy help both offer the preview, and every preview page renders exactly the attacks documented on it, driven through the real `CommandMenu` with a recording sender.
- The promise itself: `MscEntityUtils.damageBy` refuses a hit from an entity tagged as a performing dummy — checked with a proxy victim that records the `damage` calls it receives — while an untagged attacker is not treated as a preview. The opposite direction cannot run headless, because `DamageType.GENERIC` only resolves against a live registry.

### `entities/boss/NixDamageCapTest` — NIX damage cap
- Nix can never lose more than `entities.nix-executioner.max-damage-per-hit` (default **100**) from a single hit: anything above is clamped, anything below passes through untouched, and the cap never *inflates* a hit.
- `0` (or any non-positive value) disables the limit, which is the documented way back to the old unbounded behaviour.
- Pins the health-pool arithmetic down: a 10 000-damage burst leaves 350 of 450 HP, four capped hits leave 50, the fifth finishes the boss.

### `entities/boss/PenetratingDamageTest` — Sentinel penetrating damage
- **Armour is credited back**: the engine folds `ARMOR`, `MAGIC` (Protection enchantments) and `RESISTANCE` into the event damage, and `unmitigated` undoes those three so a 22-damage cleave survives full netherite. Shield blocking is deliberately *not* credited back, and the result never goes negative.
- `penetratingDamage` keeps Resistance partially effective: the boss ignores `penetrating-resistance-pierce` (default **0.2**) of the potion's reduction, so Resistance I blocks 16% instead of 20% (a 10 hit deals 8.4), `0.0` leaves the potion fully effective and `1.0` ignores it entirely. Mitigation is 20% per level and caps at 100% (Resistance V).
- Out-of-range pierce values are clamped, a hit can never grow past the raw damage, and the per-hit cap (`max-damage-dealt`, 15) is applied before Resistance so the potion can never raise it.

### `entities/boss/PenetratingHitTest` — `/msc debug` snapshot
- The immutable `PenetratingHit` record is the single place the whole pipeline runs (credit armour/Protection/Resistance back, cap, pierce Resistance), so the figures `/msc debug` prints cannot drift from the live handler: a full-netherite 22-damage cleave comes back as 12.6, the cap is applied before Resistance, and a hit absorbed by armour ends at 0 instead of a negative or `NaN`.
- Resistance is reported as a one-based level (amplifier 2 → level 3), and the snapshotted age is elapsed time clamped at zero, so a backwards clock cannot produce a negative age.

### `entities/boss/SentinelDefenseTest` — Sentinel incoming damage
- Pins the boss's whole defensive stack, now extracted from its event handler into `SentinelDefense`: shield seal ×0.5, healing circle ×0.8, stone skin ×0.5, reflect barrier ×0.7, the absorb shield spending its health, and the `max-damage-per-hit` cap applied **last**.
- Checks the traps the inline version hid: the defences cannot raise a hit above the cap (`200 → 40 → capped 30`), a hit exactly at the cap is not marked as capped, the reflect barrier returns 30% of the *reduced* hit and the cap does not shrink what it returns, and an invulnerable boss produces no cap step at all.
- The `steps` trace is asserted byte-for-byte because `/msc debug` prints it, and a sweep over every defence combination proves no defence can enlarge a hit or return a negative value.

### `commands/DebugReportTest` — `/msc debug` rendering
- Pins the rendered lines without a sender: a penetrating hit lists its event damage, the credited-back armour/Protection/Resistance, the through-armour total, the cap, the pierce, the Resistance level and the final value; a `DEALT` sample pairs the attack's intended damage with what the player actually took; a `TAKEN` sample spells out the cap or load-balancer split between the hit and what it cost.
- An empty mechanic note is skipped instead of leaving a dangling separator, and the age line is driven by an injected clock so the output is deterministic.

### `entities/boss/BossDamageLogTest` — `/msc debug` registry
- Keeps one sample per player, boss and direction: a newer sample replaces the older one in its slot, and `samplesFor` returns them ordered by `BossId` then direction regardless of insert order, so the report cannot reshuffle between runs.
- Players are tracked independently, `forget` clears one without touching another, and null records are ignored rather than throwing.
- `forgetBoss` drops one boss's samples for every player in a single sweep and leaves the other bosses intact — the same call the log's own listener runs when a boss stand leaves the world.

### `entities/boss/JackResilienceTest` — Jack Star incoming damage
- Every hit resolves through one door: an explicit dodge takes nothing, anything else is split.
- The split **conserves the hit** (`toBoss + sharedTotal == incoming`) across a sweep of damage values and party sizes, so a tuning change cannot quietly delete or duplicate damage.
- A landed hit **always reaches the boss** whatever the party size, which is what keeps Jack Star damageable at all times; a roll exactly on the dodge chance still lands.
- The compressed (demoted) form raises the configured chance to 0.45, and the boundary value keeps the configured one.

### `entities/boss/JackModelTest` — Jack Star model geometry
- The eleven parts are pinned against the **in-game reference model**: every part's height and depth match within a millimetre and all eleven sit on one shared X axis, so the body cannot drift apart one part at a time.
- The model is **centred on the hitbox** (spine and `CENTER.x` at zero) instead of carrying the reference's whole-body X offset, with the head above the torso above the legs, the head top near two blocks and the feet off the ground.
- Left and right limbs are mirrored, each **joint sits on the same side as the limb it drives** (a swapped hip used to swing a leg around the opposite hip), and a swinging limb keeps its X and never detaches from its joint.
- A sweep proves no two parts share a place, that shape shifting scales translations and part scales together, and that every part stays inside the stand's hitbox.
- Both arms and both legs were exported in two segments, so the model folds them: each **elbow and knee sits where the export leaves the biggest gap between the two segments** (the code's joint is compared against that independently derived one), only the lower half of a limb follows that joint while the upper half stays rigid, and the **whole walk stays inside the stand's hitbox**.

### `entities/boss/NixModelTest` — NIX model geometry
- The 27 parts are pinned against the **exported model**: every translation matches and the whole body shares one X axis, so no part can drift on its own.
- The model is **centred on the hitbox** (`NixModel.baseTranslation` puts the spine at zero instead of the export's `+0.066`), and `CENTER` is the **midpoint of the exported extents** rather than the mean of the 27 parts, which any part added or removed would pull around.
- Left and right limbs are mirrored, each **joint sits on the same side as the limb it drives** and on that limb's own axis, and a swinging limb keeps its X and never detaches from its joint.
- A sweep proves no two parts share a place, and the **hitbox test** keeps `MODEL_HITBOX_SCALE` covering the whole rest pose (0.95 wide, 3.75 tall) while staying within 0.05 of the smallest scale the model needs — the old literal `2.0` kept 1.8 blocks of empty box above the head.
- A source guard keeps the parts from lagging again (`setTeleportDuration`/`setInterpolationDuration`/`setDisplayWidth`/`setDisplayHeight` all zero, configured in one place) and requires a reload to **adopt** the parts it already has instead of spawning a second, overlapping body.
- The numbered pieces are **not** in stacking order, so each **elbow and knee is compared against the split the export actually shows**: the single `_4` piece sits above the joint, the other five fold below it, and the joint itself is where the gap is — the code's answer is never taken on trust. The whole walk, elbows bending included, then has to stay inside the stand's hitbox.

### `entities/NixModelKinematicsTest` — NIX kinematic model (27 parts)
- The model has **exactly 27** `ItemDisplay` parts (Blockbench export).
- Rigid kinematic hierarchy: arms and legs with **6** segments each (so they rotate as rigid bodies around shoulder/hip joints), head, upper and lower torso with **1** each.
- Every 16-float matrix decomposes into a finite offset, strictly positive scale, and finite quaternion; profile name and `base64` texture are non-blank.
- The model's horizontal center is finite.

### `entities/KingerOptimizationAndKinematicsTest` — Kinger model + optimization
- **15** parts (Blockbench spec) with the same matrix-integrity guarantees (16 floats, finite offset/scale/quaternion) and a finite `CENTER`.
- **Display sync throttling**: stationary with no animation syncs every 3 ticks (**30** of 90, ~66% reduction); while moving or animating it syncs **every tick** (90 of 90). This validates the `!isIdle || tickCount % 3 == 0` rule.

### `entities/KingerModelTest` — Kinger model geometry
- The fifteen suit pieces are pinned against the **exported model**: every translation matches, the trunk and the legs share one Z axis, and each half of a leg is stacked on its own X so a swinging leg cannot split sideways.
- `CENTER` is the **torso axis** (the midpoint of the two torso pieces) instead of the mean of the fifteen anchors or the bounding-box midpoint, which the arms pull 0.03 and 0.08 blocks forward; the trunk, the legs and the head all sit on that axis, and re-centring never touches a height.
- Every piece belongs to a **limb group**: the pieces of a group keep their distances while swinging, no piece slides sideways or flies off its joint, and no two pieces share a place.
- A limb has **two joints where the export has two segments and one where it does not**: the shin folds at the knee the export leaves between the thigh and the shin (compared against the biggest gap in the leg's own geometry), the thigh and the boot plate stay rigid, Kinger's one-piece arms expose no second joint at all, and a still boss (or one whose swing is on the forward half of the step) folds nothing.
- The **hitbox test** keeps `MODEL_HITBOX_SCALE` covering the whole rest pose (0.5 wide, 1.975 tall) while staying within 0.1 of the 0.94 the geometry strictly needs — the old literal `2.0` doubled the box in every direction and swallowed swings at thin air.
- A second hitbox test **walks the whole gait**: a step bends the knee and carries the shin further from the axis than the rest pose does, so every piece is checked over a full cycle of `walkSwing`. The deepest step takes the shin about four centimetres past the stand's 0.5-wide box, which is the documented price of a knee that bends enough to be visible; that slack is pinned so it cannot grow unnoticed.
- Every piece's tag is unique and carries its owner, so an adoption cannot mix two pieces up; a source guard keeps the pieces from lagging (`setTeleportDuration`/`setInterpolationDuration`/`setInterpolationDelay`/`setDisplayWidth`/`setDisplayHeight` all zero, configured in one place), requires a reload to **adopt** the suit it already has instead of spawning a second, overlapping one and to **rebuild that boss's boss bar** (with the virtual health its progress is read from), and keeps the animation going through `KingerModel.compose` rather than a per-piece transform.

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

### `entities/boss/BossArenaGroundRecoveryTest` — Ground recovery
- `findFloorY` returns the floor altitude, and **`NaN`** — not the boss's own Y — when the scan reaches nothing. That distinction is the fix: the old convenience method made "standing on the floor" and "nothing underneath" the same value, so a grounded boss stopped attacking forever.
- `getGroundY` keeps its documented fallback of returning the current Y, pinned down so the two behaviours cannot drift back together.
- `ringOffsets` starts at the origin, contains every offset within the radius exactly once and never goes back towards the origin — the nearest usable column must always win.
- `findUsableColumn` prefers the boss's column, walks outward when it is void, respects the search radius and reports `null` when nothing is usable so the caller can try the target column and then the world spawn.

### `utils/MscWorldPolicyTest` — World allowlist
- An **empty (or missing) allowlist means every world**, which is what `config.yml` documents. The implementation used to treat it as a hardcoded list of five world names, so a server with a custom world name silently got no conversions — and its periodic recount deleted any MSC creature it found there.
- A populated list restricts, with case and surrounding blanks normalized.
- The plugin's own worlds (`boss_dimension`, `drakes_bosses`) stay allowed even behind an explicit allowlist.

### `commands/CommandPermissionTest` — `/msc` permission rule
- Holding `commands.permission` is enough to run `/msc`, with or without OP.
- `commands.op-only: true` keeps operators working when they lack the node; setting it to `false` means only the node counts.
- A plain player with no node and no OP is denied.
- A subcommand with no configured node stays open to whoever passed the main gate, while one pinned under `commands.subcommand-permissions` needs its node on top of it.

### `entities/HeadSlimeImmunityTest` — Head Slime gelatin immunity
- The immunity window is a deadline per player, so eating a second gelatin **extends** it instead of the older scheduled removal ending it early, and nothing outlives the window after a logout.
- An expired window is dropped on access, and `clearAllImmunity()` is called from `onDisable`.

### `entities/boss/SentinelPhaseTest` — Sentinel phase ladder
- The ladder **reproduces the old hardcoded comparison chain exactly**: a sweep of health fractions from −5% to 105% is checked against the `> 0.8 / > 0.6 / > 0.4 / > 0.2` chain the boss used to inline, plus the exact boundaries (at precisely 80% health the boss is already in phase 1), nonsense inputs, and that the phase only ever grows as health drops.
- `sanitizeThresholds` drops thresholds outside `(0, 1]` and non-finite ones, sorts the rest highest first, collapses duplicates (they would be a zero-width phase), keeps `1.0`, returns an immutable list, and falls back to the defaults when a config edit leaves nothing usable.
- The generated boss bar titles are asserted **byte for byte** against the five strings the old switch held, for the default five phases and for a rescaled three-phase ladder — including that a phase past the end cannot emit a negative number of squares.
- Bar colours follow the phases, and a longer ladder reuses the last colour of the palette instead of falling back to red.

### `entities/boss/SentinelHitboxTest` — The Obsidian Sentinel's own stand
- The Sentinel is not a suit on an invisible stand, it **is** the scaled stand, so one number is at once the size of the model and the box players hit: `MODEL_HITBOX_SCALE` is pinned at `7.5` (about fourteen blocks of warrior), inside the range the clamp allows and wide enough to be hit.
- The scale is read from `armor-stand-boss.hitbox-scale` and **clamped**, never a literal inside `trySpawn` again — the code that sets it from a bare `7.5` is a failure.
- The two numbers of the arrival pentagram (its duration and its radius) are named constants the seal call passes, so the timing and the arena size are not buried in a call site.
- The stand the fight is hit through is configured in **exactly one place**: arms on, no base plate, no gravity, invulnerable off, persistent, named and tagged — each of those is asserted to appear once in the source, because a second spawn path forgetting one would produce a boss the fight cannot be won against.

### `utils/MscTextTest` — Item and mob name parity
- Every helper that builds item names, lore, flavour quotes and the `✦ … ✦` footers is serialised back with `LegacyComponentSerializer.legacySection()` and compared to the exact `ChatColor` string it replaced, so the migration cannot shift a space, a colour code or a bold flag unnoticed.
- Covers the mid-sentence colour switches (`rich`), the empty spacer lines (`blank`), the colourless names (`plain`) and the argument validation of `rich`.
- Pins the legacy rule that a **colour code clears bold**: a bold prefix followed by another colour stays bold only on the prefix, which is why the Garou name tag is built as two siblings instead of a decorated parent. A child would inherit the bold, and the test keeps that trap visible.
- `plainText` is the counterpart used to **compare** a name rather than show it, so it must strip every code and return an empty string for a nameless entity.

### `ConfigFilesGuardTest` — Resource contract (`config.yml` / `plugin.yml`)
- Parses both resources with SnakeYAML, so a broken indent or a lost section fails the build instead of the server start.
- Scans `src/main/java` for quoted config paths and fails if any of them is missing from `config.yml`. Nothing else enforced the header's "all paths match the code" promise: the Nullshear Edge handler read five `items.nullshear-edge.*` keys that were not in the file, and two Excalibur passive keys were documented but hardcoded, both falling back to code defaults silently.
- Asserts `plugin.yml` keeps the command, the `msc.admin` node (the same one `commands.permission` declares), the `msc.admin.bypass` node used by the boss-dimension handlers, and a `usage` line listing every sub-command.
- Checks the player-facing knobs stay sane: the Sentinels `phase-thresholds` descend inside `(0, 1]`, defence durations are at least one tick, `no-player-despawn-ticks` allows `0`, and every toggleable mob keeps its `enabled` flag.
- A self-test proves the literal scanner reports dotted literals outside comments and ignores the ones inside them.
- Asserts `commands.subcommand-permissions` exists as an **empty map** by default: the documented escape hatch must not disappear silently, and the shipped config must not restrict anything by surprise.
- Asserts **every** boss ships the hitbox scale its geometry test proves is right (`kinger.hitbox-scale` 1.0, `nix-executioner.hitbox-scale` 1.9, `jackstar-architect.hitbox-scale` 1.2, `armor-stand-boss.hitbox-scale` 7.5 — the Sentinel's own body), inside the 0.25–8 range a hand-edited value gets clamped to — the knob and the geometry tests have to agree out of the box.

### `utils/LegacyNameApiGuardTest` — Migration guard
- Reads `src/main/java` and fails if any file goes back to the deprecated String name APIs (`setDisplayName`, `setLore`, `setItemName`, `setCustomName`, `getDisplayName`, `getCustomName`). Those methods still compile and still work, so an item written the old way would otherwise only show up as a subtly wrong tooltip.
- Matches inside comments are ignored, and the scan asserts it walked the whole source set so it cannot pass vacuously.
- A second test feeds the detector a sample with all six APIs plus a commented-out one, proving the guard catches exactly what it is looking for.

### `utils/MscLogTest` — Reported failures
- The twenty-one catch blocks that used to swallow their exception (`catch (Exception ignored) { }`) now report through `utils/MscLog`; this suite drives it with a capturing `Handler` and asserts the plugin logger is actually asked to print.
- A tolerated failure is logged at `FINE` and an actionable one at `WARNING`, always with the context, the exception's simple name and its message — a `NumberFormatException` keeps the offending input.
- An exception without a message is still named (`java.lang.IllegalStateException`), a null one reports `unknown error` instead of throwing, and `init(null)` keeps the previous logger so the startup order cannot silence the plugin.

### `utils/SilentCatchGuardTest` — No silent catch blocks
- A source guard: it strips comments, strings and chars (keeping line numbers), finds every `catch` clause under `src/main/java` and **fails if any body is left blank**, with an empty allow-list and a floor of 40 catches so the scanner cannot pass vacuously.
- It is what keeps the logging change from being undone one block at a time: a swallowed exception is invisible in review, a blank catch body is not.

## 🗃️ Where they run

Tests execute during Maven's **`test` phase** (Surefire). They need no server or network: just the JDK 21 and the dependencies declared in `pom.xml`.

They also run in CI: `.github/workflows/verify.yml` runs `mvn verify` on every push to `main` and on every pull request. Publishing to Modrinth (`modrinth-publish.yml`) waits for that job, because its own build step uses `-DskipTests`.

> When touching health/boss logic, ritual structures, or the NIX/Kinger kinematic models, run the full suite with `mvn test` to make sure you are not introducing regressions.