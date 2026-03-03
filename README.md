# MultiverseCreatures
**Developed by: Chagui68**

A custom plugin for Spigot/Paper 1.20.6 that adds themed creatures, items with special abilities, and an advanced dynamic skin system.

## 🚀 Key Features

### 1. Unique Items System
The plugin uses `PersistentDataContainer` to identify its items, ensuring that abilities work even if the item's name is changed.

*   **Excalibur Sword:** 
    *   **Obtainment:** Rare trade with Shaggy.
    *   **Ability:** Grants constant **Strength II** while held in the main hand.
    *   **Logic:** Uses a clock (Scheduler) that checks the held item every second, ensuring the effect is never lost.
*   **Scooby Cookies:**
    *   **Effect:** When consumed, they grant **Resistance VI** for 10 seconds.
    *   **Usage:** Ideal for critical combat situations.

### 2. Creatures and Mobs
*   **Shaggy (Wandering Trader):**
    *   Has a **30% chance** of replacing a conventional Wandering Trader upon spawning.
    *   Features custom trades requiring high-value materials (Diamonds, Netherite).
    *   Internally tagged with the scoreboard tag `MSC_Shaggy` for tracking.

## 🎭 Skin System (LibsDisguises Integration)
One of the technical highlights of this plugin is its dynamic skin service (`PlayerSkinDisguiseService`).

### Technical Implementation
The plugin integrates **LibsDisguises** intelligently using **Java Reflection**:
*   **Optionality:** The plugin DOES NOT require LibsDisguises to start. If the plugin is missing, it simply disables skins and continues to function normally without console errors.
*   **Independence:** By using Reflection, we don't depend directly on the LibsDisguises JAR in the build environment, making it more flexible.

### Viewing Modes (config.yml)
You can configure where the "Shaggy" mob gets its skin from:
*   `fixed`: Uses a fixed skin (e.g., Notch).
*   `nearest-player`: Copies the skin of the nearest player at the moment of spawning.
*   `random-online-player`: Chooses the skin of a random player currently online.
*   `entity-name`: Uses the entity's custom name as the skin name.

## 🛠 System Requirements
*   **Minecraft Version:** Paper/Spigot 1.20.6
*   **Java:** 21 or higher.
*   **Optional Dependency:** [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises.81/) (Recommended for the full visual experience).

## 🔨 Building
To build the project manually:
```bash
mvn clean package -DskipTests
```
The resulting file can be found at `target/MultiverseCreatures-v1.0.jar`.

---
*This project is owned by Chagui68. All rights reserved.*
