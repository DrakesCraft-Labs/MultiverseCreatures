# ⚙️ Developer Zone

The **Developer Zone** is the wiki area intended for developers and contributors. It gathers all the documentation that describes the **explicit inner workings of MultiverseCreatures' code** and its **internal organization structure**: how the project is laid out, which conventions must be followed, and how changes are tested before release.

> If you only play with the plugin, you can skip this section. If you plan to extend the project or report bugs with technical context, this is the place to start.

## 📄 Pages

- [Architecture](./Architecture.md) — Source layout (`src/main/java`), subsystems, mandatory conventions, and a guide for adding new content.
- [Tests](./Tests.md) — The testing framework (JUnit 5), how to run the suite, and what every test class verifies.

## 🧭 How to navigate

1. Start with **Architecture** to understand the code map and the patterns every contributor must respect.
2. Before touching any code, review the **Tests** section to know which behavior is already covered and what you could break by changing that logic.

---

## 🗺️ Project composition (summary)

| Subsystem | Package | Responsibility |
|---|---|---|
| Entry point | `com.Chagui68.MultiverseCreatures` | `onEnable`/`onDisable`, recipe registration, listener and instance registration for every subsystem |
| Commands | `com.Chagui68.commands` | `/msc` command executor and tab completer |
| Entities (mobs, minibosses, bosses) | `com.Chagui68.entities` | Custom creatures, bosses with an attack framework, minibosses, spawn router |
| Items & recipes | `com.Chagui68.items` | Armor, weapons, components, food, relics and recipe registration |
| Bukkit events | `com.Chagui68.listener` | One event handler per system (item, boss, dimension, armor...) |
| NBS music | `com.Chagui68.music` | NBS song playback, discs and jukebox |
| Rituals & dimension | `com.Chagui68.ritual` | Ritual structures, invocations and the private boss dimension |
| Utilities | `com.Chagui68.utils` | `ItemBuilder`, `MscEntityUtils`, world policies |

For the full detail (the real directory tree and the code conventions) check [Architecture](./Architecture.md).