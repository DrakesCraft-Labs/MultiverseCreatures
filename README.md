<div align="center">

<img src="docs/banner.svg" alt="MultiverseCreatures" width="100%">

# ✦ MultiverseCreatures ✦

### Themed creatures, bosses & legendary items pulled from across the multiverse

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21%2B-7C4DFF?logo=minecraft&logoColor=white)](https://modrinth.com/plugin/multiversecreatures)
[![Purpur](https://img.shields.io/badge/Purpur-1.21.11-FFA000?logo=purpur)](https://purpurmc.org/)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](./LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/gJCViBEN?label=Modrinth%20Downloads&logo=modrinth&color=2DD2A4)](https://modrinth.com/plugin/multiversecreatures)

**A Paper/Purpur/Spigot plugin that brings adaptive bosses, signature weapons, and themed mobs inspired by JoJo, Jujutsu Kaisen, Hollow Knight, Adventure Time, Arthurian legend, Half-Life, Scooby-Doo, Diablo and more.**

[📥 Download on Modrinth](https://modrinth.com/plugin/multiversecreatures) ·
[📖 Full Wiki](wiki-en/Home.md) ·
[🐛 Report Issues](https://github.com/Chagui68/MultiverseCreatures/issues)

</div>

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `play.drakescraft.net` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rR7FbfCt9Y)
> * 🌐 **Web & Guía**: [drakescraft.net](https://drakescraft.net) — 🛒 **Tienda**: [tienda.drakescraft.net](https://tienda.drakescraft.net)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

---

## 🎲 What is MultiverseCreatures?

MultiverseCreatures is a content plugin for **Minecraft 1.21+** that turns a server into a multiverse playground. Every vanilla mob spawn has a chance to be replaced with a themed custom creature; each one has its own AI, drops specialized crafting ingredients, and is the source of a legendary item referencing a different universe.

### ✨ Core Highlights

- **🛡️ One Final Boss** — *THE OBSIDIAN SENTINEL*, a fully animated 5-phase ArmorStand boss with **33 unique attacks** (13 aerial · 11 ground · 12 ranged), boss bar, megalovania music, magic seals, summoned reinforcements, and defensive states.
- **🧠 Adaptive Bosses** — *Mahoraga* reads your **full inventory every tick** and evolves its stats in real time (Sharpness → Resistance, Protection → Strength, Knockback → knockback immunity, distance → Speed).
- **🌌 Themed Minibosses** — *Dio Brando* (JoJo) with an invulnerable Stand ArmorStand that casts **THE WORLD: FREEZING**, freezing every nearby player and locking their movement.
- **⚔️ Legendary Weapons** — *Excalibur*, *Cinder Greatsword*, *Nullshear Edge*, *Soulreap Scythe*, *Aether Pullshot*, *Skyfire Talisman* — each with passive + active abilities, cooldowns, and themed lore.
- **🔮 Themed Relics** — *Ice King's Crown* (ice projectile + blizzard + ice-path), *Mantis Claws* (wall-cling + wall-jump via packet interception), *Wirt's Lantern* (mob repel + Night Vision + immunity), *Dio's Stand Head* (summoned Stand with time-freeze attack).
- **🪖 Armor Sets** — *Eight-Handled Wheel* (damage-cause adaptation), *Obsidian Bastion* (4-piece set bonus with +40% Health, knockback immunity, fire/lava immunity), plus off-hand relics *Marrow Aegis* (damage reflect), *Veilwalker Mantle* (stealth + backstab) and *Frost Heart* (chill aura + Frost Walker).
- **🎒 Themed Food & Utility** — *Scooby Cookie* (Resistance VI), *Head Slime Gelatin* (Head Slime immunity), *Military Mine* (auto-camouflaged TNT).
- **🌿 Natural-spawn replacements** for Zombies, Skeletons, Creepers, Spiders, Witches, Blazes, Iron Golems, Endermen, Wither Skeletons, Evokers, Slimes — each with its own chance, fully configurable.
- **🐎 Full-Moon Military Ambush** — a rare 0.1% chance during a Full Moon to spawn a ZombieHorseTrap that unleashes a 5-unit undead army.
- **🛒 Multiverse Merchant** — 30% of Wandering Trader spawns become "Shaggy" with custom multiverse trades.
- **🎵 NBS Music Engine** — custom music playback (ships Megalovania as the boss theme).
- **🔮 Ritual Structures & Private Boss Dimension** — scaffolding for future boss invocation arcs.
- **⚙️ Fully Configurable** — every spawn chance, boss stat, item cooldown, death message and effect amplifier lives in `config.yml`.

---

## 📖 Documentation Wiki

The project has a complete documentation site built into the repository. It covers every boss, mob, item, command and config knob in detail — all generated from the source code.

**🔗 Browse the full wiki on GitHub:** https://github.com/Chagui68/MultiverseCreatures/tree/main/wiki

| Page | Topic |
|------|-------|
| [Home](wiki-en/Home.md) | Overview + featured themes |
| [Bosses](wiki-en/Bosses.md) | THE OBSIDIAN SENTINEL · Dio Brando · Mahoraga |
| [Creatures](wiki-en/Creatures.md) | 14 natural-spawn replacement mobs + ZombieHorseTrap army |
| [Weapons](wiki-en/Weapons.md) | Excalibur, Cinder Greatsword, Nullshear Edge, Soulreap Scythe, Aether Pullshot, Skyfire Talisman, Chaos Forge |
| [Armor-and-Relics](wiki-en/Armor-and-Relics.md) | Eight-Handled Wheel, Obsidian Bastion set, off-hand relics (Marrow Aegis, Veilwalker Mantle, Frost Heart) |
| [Items](wiki-en/Items.md) | Ice King's Crown, Mantis Claws, Wirt's Lantern, Dio's Stand Head, Military Mine, Scooby Cookie, Head Slime Gelatin |
| [Components](wiki-en/Components.md) | The 16 mob-drop crafting ingredients + the loot → item chains |
| [Commands](wiki-en/Commands.md) | Full `/msc` reference (spawn, give, seal, dummy, attack, music, dimtp, cleanstands) |
| [Architecture](wiki-en/Architecture.md) | Code structure, conventions and how to extend the plugin |
| [Installation](wiki-en/Installation.md) | Step-by-step install, config.yml guide, troubleshooting |

---

## 🧩 Extending: reusing the 42 boss attacks

The boss attacks live behind a small interface so they are **not tied to one boss**.

```java
public interface BossHost {
    MultiverseCreatures getPlugin();
    void resetBossPose(BossInstance instance);
    Player detectTarget(ArmorStand stand);
    void spawnShockwaveWave(World world, Location center, double maxRadius);
    // plus defaults: getValidPlayers, getValidPlayersNear, launchPlayer,
    // getGroundY, isOnGround, countPlayersInRange, findNearestPlayer
}
```

Any boss that implements `BossHost` can reuse the attack classes as they are. **39 of the 42 are
host-agnostic**; the remaining three reach for something only THE OBSIDIAN SENTINEL has — the
netherite lance, the shield timings and the sky pentagram — and cast explicitly, with a comment
saying so.

Terrain and player queries live in `BossArena` as stateless helpers, so they can be called from
anywhere and tested on their own. The "is this player a valid target?" rule (skip dead, creative
and spectator) is decided in exactly one place.

> **Heads-up if you are writing a non-ArmorStand boss:** the attacks animate the boss through
> ArmorStand poses — `setHeadPose`, `setBodyPose`, arm poses — over 250 calls across the 42
> classes. A mob-based boss can reuse an attack's *effect* (damage, particles, projectiles) but
> not its choreography. Splitting each attack into "effect" and "animation" is the natural next
> step if that is needed.

---

## 📦 Installation

1. Download the latest `.jar` from the [Modrinth page](https://modrinth.com/plugin/multiversecreatures).
2. Drop it into your server's `plugins/` folder.
3. Start the server once to generate the default `config.yml`.
4. (Optional) Edit `plugins/MultiverseCreatures/config.yml` to tune spawn chances, cooldowns, boss stats, death messages, and effect amplifiers.
5. Restart and enjoy. Use `/msc spawn <type>` to summon anything, or wait for natural spawns to be replaced.

> **Requirements:** Paper / Purpur / Spigot **1.21+** (built against `purpur-api 1.21.11`) · **Java 21+**

A full install guide lives in the [Installation wiki page](wiki-en/Installation.md).

---

## 🚀 Build from Source

```bash
git clone https://github.com/Chagui68/MultiverseCreatures.git
cd MultiverseCreatures
mvn clean package -DskipTests
```

Output JAR will be at `target/MultiverseCreatures-v<version>.jar`.

---

## 🛠️ Commands (Quick Reference)

All interactions use the `/msc` command. **Permission:** `msc.admin` (server OP by default).

```
/msc spawn <type>      Summon a mob/boss/merchant at your location
/msc give <item> [n]   Obtain an item (amount 1–64)
/msc seal <pattern>    Render a particle seal pattern
/msc dummy ...         Spawn/pose/an animation ArmorStand dummies
/msc attack <name>     Trigger an ArmorStandBoss attack/mechanic
/msc music <play|stop> Play or stop NBS songs
/msc dimtp <world>     Teleport across worlds
/msc cleanstands        Remove all MSC-related armor stands
```

Full breakdown (alias tables, all spawn types, giveable items, attack names, seal patterns, dummy animations) is in the [Commands wiki page](wiki-en/Commands.md).

---

## 🗺️ Project Structure

```
src/main/java/com/Chagui68/
├── MultiverseCreatures.java      
├── ability/                     
├── commands/                     
├── entities/
│   ├── boss/                    
│   │   ├── attack/{aerial,ground,ranged}/  
│   │   └── MagicSealListener / BossInstance
│   ├── miniboss/                
│   └── handler/               
├── items/
│   ├── armor/ components/ food/ dio/
│   ├── misc/                     
│   │   └── offhand/              
│   └── weapons/{melee,ranged,magic}/
├── listener/                   
├── music/                       
├── ritual/                       
└── utils/                      

wiki/                             
├── Home.md
├── Bosses.md
├── Creatures.md
├── Weapons.md
├── Armor-and-Relics.md
├── Items.md
├── Components.md
├── Commands.md
├── Architecture.md
└── Installation.md
```

The `wiki/` folder is **pure Markdown documentation** and is excluded from the Maven build — it lives only on GitHub for reference and is never bundled into the plugin JAR.

---

## 📜 License

[GPL-3.0](./LICENSE) — MultiverseCreatures is open source.

---

<div align="center">

**Made with ☕ and a love of the multiverse.**

If you enjoy the plugin, leave a ⭐ on GitHub and a ❤ on [Modrinth](https://modrinth.com/plugin/multiversecreatures)!

</div>

## ⚖️ Upstream Attribution & License / Licencia y Créditos

- **Original Project / Upstream**: Slimefun4 Community Addon.
- **Port & Maintenance**: DrakesCraft Labs team (Compatibility for Paper / Purpur 1.21.11).
- **License**: GPL-3.0 / MIT.
- **Source Code**: [GitHub Repository](https://github.com/DrakesCraft-Labs/MultiverseCreatures)
- **Support & Issues**: [GitHub Issues](https://github.com/DrakesCraft-Labs/MultiverseCreatures/issues) | [Discord](https://discord.gg/rR7FbfCt9Y)

*This project is an open-source derivative work maintained by DrakesCraft Labs under the terms of its original license. All original assets and concepts belong to their respective creators.*
