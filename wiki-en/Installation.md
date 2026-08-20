# 📦 Installation

A step-by-step guide to installing and configuring MultiverseCreatures.

---

## 1. Requirements

| Requirement | Version |
|---|---|
| Minecraft server software | **Paper / Purpur / Spigot 1.21+** |
| (Recommended) | Purpur 1.21.11+ (the plugin is built against `purpur-api 1.21.11`) |
| Java | **21 or higher** |

The plugin uses 1.21-specific APIs (transformation display entities, modern AttributeModifier, persistent data containers with NamespacedKey) and **will not load on 1.20 or earlier**.

---

## 2. Install

1. Download the latest `MultiverseCreatures-vX.Y.Z.jar` from one of:
   - The [Modrinth page](https://modrinth.com/plugin/multiversecreatures) (recommended)
   - The [GitHub Releases](https://github.com/Chagui68/MultiverseCreatures/releases) page
2. Stop your server.
3. Drop the JAR into `plugins/`.
4. Start the server once — this generates the default `plugins/MultiverseCreatures/config.yml` and the resource subfolders (`music/`, `schematics/`, `structures/`).
5. (Optional) Edit `config.yml` to your liking. The defaults are reasonable, but power users will want to tune spawn chances, boss health, item cooldowns, and death messages.
6. Restart and play!

---

## 3. Configuration (`config.yml`)

Every plugin setting lives in `plugins/MultiverseCreatures/config.yml`. The default config is shipped with sane values; the most useful knobs are:

### Spawn chances

Each natural-spawn replacement has its own chance key:

```yaml
mahoraga:
  spawn-chance: 0.02          # 2%
obsidian-guard:
  spawn-chance: 0.02          # 2%
creeper-jr:
  spawn-chance: 0.15          # 15% (spawns in trio)
head-slime:
  spawn-chance: 0.10          # 10%
shadow-rogue:
  spawn-chance: 0.05
bone-shield:
  spawn-chance: 0.06
flame-elemental:
  spawn-chance: 0.10
frost-golem:
  spawn-chance: 0.08
void-crawler:
  spawn-chance: 0.07
storm-caller:
  spawn-chance: 0.04
venom-witch:
  spawn-chance: 0.05
soul-reaper:
  spawn-chance: 0.05
chaos-mage:
  spawn-chance: 0.06
ender-knight:
  spawn-chance: 0.04
zombie-horse-trap:
  full-moon-spawn-chance: 0.001   # 0.1% during Full Moon
```

### Boss stats

```yaml
armor-stand-boss:
  health: 500.0
  aggro-range: 50
  seal-damage: 15.0
  hover-barrage-damage: 12.0
```

### Item cooldowns & amplifiers

```yaml
excalibur:
  solar-flare:
    range: 20
    cooldown-ms: 15000

ice-king-crown:
  launch-cooldown-ms: 10000
  blizzard-cooldown-ms: 60000
```

### Death messages

Every lethal mob has a `<mob>.death-messages` list; on player death, a random entry is shown with `%player%` replaced:

```yaml
creeper-jr:
  death-messages:
    - "%player% was blown up by Creeper Jr."
    - "%player% got too close to a tiny creeper."
```

---

## 4. Custom music

Drop any `.nbs` file into `plugins/MultiverseCreatures/music/`. The plugin ships with 12 songs (including `Undertale-Megalovania.nbs`, used as the Obsidian Sentinel boss theme). You can:

- Replace the boss theme by replacing `Undertale-Megalovania.nbs`.
- Get the jukebox disc of any song with `/msc music disc <song>` or from the **Disc Trader** villager (`/msc spawn disctrader`). Discs work in a jukebox like vanilla discs — see [Music](./Music.md) for the full song list and credits.

Use **NBS format 5+**, single-instrument survival note-block samples work best for realistic in-world playback.

---

## 5. Server performance notes

- Most mobs add a per-tick AI task scoped to themselves. As long as you keep mob counts reasonable, the cost is similar to vanilla (the plugin uses Bukkit's `BukkitRunnable` and `PersistentDataContainer` rather than NMS).
- The Obsidian Sentinel has the most expensive AI; if you run more than one at a time on a low-spec server you may see tick cost climb — only run the boss when needed.
- All `MSC_*` ArmorStands are cleaned up by `/msc cleanstands` if a fight crashes or the server restarts midway.

---

## 6. Permissions

Add a permission manager (e.g. LuckPerms) and assign:

```
msc.admin   → trustworthy staff who can use /msc
```

There is no per-item or per-spawn permission yet — the entire `/msc` command is gated by `msc.admin` (defaults to OP).

---

## 7. Troubleshooting

| Symptom | Fix |
|---|---|
| Plugin doesn't load | Check server log for "UnsupportedClassVersionError" — you need Java 21+ on your server JVM. |
| Mobs don't spawn | Keep `CreatureSpawnEvent` reasons in mind: the plugin only replaces `NATURAL`, `SPAWNER_EGG`, `REINFORCEMENTS`, and `SPAWNER` spawns. Mob-farm spawner spawns are affected; egg-based custom spawns may not be. |
| Items don't work | Make sure you're holding the actual MSC item (with `msc_*` PDC tags). Items renamed or cloned in creative may not be tagged. Use `/msc give <item>` to obtain valid copies. |
| Boss is invincible | If the boss is mid-phase-transition (Barrier/Despair), it is briefly invulnerable — wait a few seconds. |
| Stands won't despawn after a crash | Run `/msc cleanstands` once. This scrubs every `MSC_*`-tagged ArmorStand in all worlds. |
| Custom music won't play | Confirm the `.nbs` file is in `plugins/MultiverseCreatures/music/` and the file extension is lowercase `.nbs`. |

---

## 8. Compatibility & security

- **100% Bukkit API** — no NMS accesses; safe across Paper / Purpur / Spigot 1.21+.
- The plugin uses one custom Netty channel handler (Mantis Claws wall-jump input detection). It's injected on join, removed on quit, and is resilient to disconnects.
- No external database; all plugin state is in-memory or per-world PDC.
- No telemetry; the plugin makes no outbound connections.

---

## 9. Useful links

- [Modrinth page](https://modrinth.com/plugin/multiversecreatures)
- [GitHub](https://github.com/Chagui68/MultiverseCreatures)
- [Issue tracker](https://github.com/Chagui68/MultiverseCreatures/issues)
- [Full wiki home](./Home.md)
