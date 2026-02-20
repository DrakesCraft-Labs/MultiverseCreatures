# 🐾 MultiverseCreatures - Scooby Update

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.6-brightgreen.svg)](https://www.spigotmc.org/)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.java.com/)

A Minecraft plugin that adds themed creatures and items, starting with the Scooby-Doo universe.

---

## 🚀 Features

### 👤 Shaggy (Wandering Trader)
- Randomly spawns instead of a normal Wandering Trader (30% chance).
- Features a custom name and unique look.
- **Exclusive Trades**: Exchange Diamonds for the legendary Scooby Cookies.

### 🍪 Scooby Cookies
- Custom items with unique persistent metadata.
- **Consumption Effect**:
  - Upon eating, the player receives **Resistance VI** (Invincibility).
  - The effect lasts for **10 seconds**.

### 🏠 System
- Intelligent entity spawn management to prevent interference with vanilla mechanics.

---

## 📋 Requirements

### Server
- **Minecraft**: 1.20.6
- **Server Software**: Spigot or Paper 1.20.6
- **Java**: 21 or higher

---

## 📦 Installation

### For Server Administrators
1. **Download the Plugin**
   - Download the latest `.jar` from [GitHub Releases](https://github.com/Chagui68/MultiverseCreatures/releases).
   - Place the file in your server's `/plugins/` folder.

2. **Restart**
   - Restart the server to apply changes.

### For Developers
1. **Build from Source**
   - Ensure you have Maven installed.
   - Run `mvn clean package`.
   - The generated file will be in the `target` folder.
+