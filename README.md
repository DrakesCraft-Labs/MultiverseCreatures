# MultiverseCreatures

Advanced procedural world generation and themed entity plugin for Spigot/Paper **1.21+**.

MultiverseCreatures (v5.0.DEV) transforms your Minecraft server by introducing a custom modular terrain generator, unique biomes with NMS injection, and high-performance procedural population.

## 🌍 World Generation Features

The plugin implements a custom **MSCTerraGenerator** that provides full control over terrain shape, biome distribution, and resource population.

- **Modular Terrain:** High-performance noise sampling and slant calculation for smooth, natural-looking landscapes.
- **Custom Biome System:**
  - **NMS Injection:** Biomes are injected directly into the server registry for maximum compatibility.
  - **Thematic Visuals:** Customizable fog, water, sky, and grass colors via `biomes.yml`.
  - **Biome HUD:** Real-time Action Bar display showing the current custom biome name.
- **Procedural Flora & Structures:**
  - Support for **.json**, **.schem**, and **.litematic** schematics.
  - Custom tree registry for varied, realistic forest generation.
  - Precise control over ore distribution and vein clustering.

### 🏞️ Active Biome: Mountain Valley
- 🌿 **Mountain Valley:** Lush, emerald-rich valleys with custom procedural trees and unique resource distribution.

---

## 🐲 Bosses & Special Entities

### 🌌 Sovereign of the End (The Dragon)
The Ender Dragon has been completely reworked into a multi-phase boss with massive health and unique abilities.

- **Dynamic Health & Naming:** 2000 HP with 4 distinct boss phases and unique names.
- **Ability Roulette:** Every 60 seconds, the dragon rolls for a random powerful ability.
- **Phase-Based Passives:**
  - ⚪ **White:** Phase Shift (Teleportation on heavy hits).
  - 🟢 **Green:** Siphon Life (Healing on hit).
  - 🔵 **Blue:** Static Discharge (Lightning knockback).
  - 🟡 **Yellow:** Solar Flare (Blinding).
  - 🔴 **Red:** Shadow Armor (30% Damage Reduction).
- **Abilities:** Meteor Showers, Abyssal Screams, Void Beams, and Corrupting Breath.

### 🧔 Shaggy & Themed Items
- **Shaggy NPC:** A Wandering Trader replacement with custom AI and unique trades.
- **Excalibur Sword:** Rare legendary trade granting **Strength III**.
- **Scooby Cookies:** Custom food providing **Resistance VI** for 10 seconds.

---

## 🛠️ Commands

All plugin interactions are handled via the `/msc` command (**Permission:** `msc.admin`).

| Command | Description |
|---|---|
| `/msc spawn <shaggy/dragon>` | Spawns a custom themed entity. |
| `/msc reload` | Reloads `biomes.yml` and re-initializes world generators. |
| `/msc check` | Performs a system diagnostic (Registry audit & World audit). |
| `/msc damage <amount>` | Applies damage to the nearest custom dragon (for testing). |
| `/msc ability <name>` | Forces the dragon to perform a specific ability. |
| `/msc phase <color>` | Forces a specific boss phase color. |
| `/msc chests` | Spawns reward chests behind the exit portal. |

---

## ⚙️ Configuration

The plugin uses two main configuration files:

1. **`config.yml`**: General settings, including the entity skin system.
2. **`biomes.yml`**: Detailed biome definitions including terrain altitude, flora, and resource percentage.

### Example Biome entry in `biomes.yml`:
```yaml
abyssal_plains:
  display_name: "§1Abyssal Plains"
  base_biome: "DEEP_OCEAN"
  water_color: 16711904
  terrain:
    base_height: 32
    amplitude: 12
  resources:
    ores:
      - material: DIAMOND_ORE
        chance: 0.01
```

---

## 🚀 Requirements

- **Server:** Paper or Spigot **1.21.1+**
- **Java:** **21** or higher.
- **Optional Dependencies:**
  - `LibsDisguises`: Required for custom player skin rendering on entities.

## 🔨 Build

To build the project from source using Maven:

```bash
mvn clean package -DskipTests
```
The output jar will be located in the `target/` directory as `MultiverseCreatures-v5.0.DEV.jar`.

