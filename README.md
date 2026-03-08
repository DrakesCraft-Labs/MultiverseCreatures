# MultiverseCreatures

Plugin for custom themed creatures and items on Spigot/Paper **1.20.1 - 1.20.6**.

## Main Features

### 🌌 Sovereign of the End (The Dragon)
The Ender Dragon has been completely reworked into a multi-phase boss with massive health and unique abilities.

- **Dynamic Health & Naming:** 5000 HP with 4 distinct boss phases and names.
- **Ability Roulette:** Every 60 seconds, the dragon rolls for a random powerful ability.
- **Damage Cap:** Maximum of 200 damage per hit to prevent instant kills.
- **Phase-Based Passives:**
  - ⚪ **White Phase (Phase Shift):** Chance to teleport away on heavy hits.
  - 🟢 **Green Phase (Siphon Life):** Heals 0.5% max health when damaging players.
  - 🔵 **Blue Phase (Static Discharge):** Strikes attackers with lightning and knockback.
  - 🟡 **Yellow Phase (Solar Flare):** Chance to blind players on hit.
  - 🔴 **Red Phase (Shadow Armor):** 30% flat damage reduction.
- **Special Abilities:**
  - **💥 Abyssal Dash:** Charges at players with explosive force.
  - **⚡ Lightning Storm:** Constant lightning strikes across the arena.
  - **🟣 Void Corruption:** The floor (End Stone) damages players standing on it.
  - **🦇 Shadow Enemies:** Spawns "Void Shadows" (Bats) that blind and darken players.
  - **🔊 Abyssal Scream:** Massive wave that launches players into the air.
  - **☄️ Meteor Shower:** Meteors rain from the sky targeting player positions.
  - **🌫️ Abyssal Mist:** Blinds and darkens everyone in the world.
  - **⚛️ Void Beam:** Devastating beam attack.

### 🧔 Shaggy & Themed Items
- **Shaggy:** Wandering Trader replacement (30% chance) with custom AI and trades.
- **Excalibur Sword:** Rare trade that grants **Strength III** while held.
- **Scooby Cookies:** Custom food that grants **Resistance VI** for 10 seconds.
- **Player Skin System:** Entities can render as player skins using `LibsDisguises`.

## Configuration
File: `plugins/MultiverseCreatures/config.yml`

```yaml
skins:
  enabled: false
  source: fixed                # fixed | nearest-player | random-online-player | entity-name
  fixed-name: Notch
  nearest-player-max-distance: 64.0
```

## Requirements
- **Minecraft Server:** Paper/Spigot 1.20.1 - 1.20.6
- **Java:** 21+
- **Optional:** `LibsDisguises` (required for player skin rendering - **Not compatible with 1.20.1**)

## Build
```bash
mvn clean package -DskipTests
```
Output jar: `target/MultiverseCreatures-v1.0.jar`
