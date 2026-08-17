# 🏠 MultiverseCreatures Wiki — Inicio

Bienvenido a la wiki de **MultiverseCreatures**! Esta es la documentación completa del plugin. Empieza aquí para obtener una visión general y luego explora las páginas dedicadas a jefes, criaturas, objetos y configuración.

> **Enlaces rápidos:** [Jefes](./Bosses.md) · [Criaturas](./Creatures.md) · [Armas](./Weapons.md) · [Armaduras y Reliquias](./Armor-and-Relics.md) · [Objetos](./Items.md) · [Componentes](./Components.md) · [Música](./Music.md) · [Dimensión del Ritual](./Ritual-Dimension.md) · [Comandos](./Commands.md) · [Arquitectura](./Architecture.md) · [Instalación](./Installation.md)

---

## ✨ ¿Qué es MultiverseCreatures?

Un plugin para servidores **Paper/Purpur/Spigot 1.21+** que añade:

- 1 jefe final con 5 fases y 33 ataques
- 3 minijefes (Dio, Mahoraga, Kinger)
- 14 criaturas temáticas que reemplazan los spawns naturales
- 1 raro evento de ejército militar en Luna Llena
- 1 reemplazo del Comerciante Errante con intercambios personalizados
- 11 armas, 3 piezas de armadura, 3 reliquias de mano secundaria, 4 objetos varios, 2 alimentos
- 16 componentes de crafteo que dropean los mobs
- Motor de música NBS personalizado + estructuras de ritual

Cada spawn de mob vanilla tiene una probabilidad configurable de ser reemplazado por su contraparte MSC; cada contraparte tiene su propia IA, dropea ingredientes de crafteo especializados y da acceso a objetos legendarios temáticos.

---

## 🎨 Temas representados

| Universo / Tema | Contenido |
|---|---|
| **JoJo's Bizarre Adventure** | Boss Dio Brando · objeto Cabeza del Stand de Dio · congelación temporal "THE WORLD" |
| **Jujutsu Kaisen** | Minijefe Mahoraga · casco Rueda de Ocho Manos |
| **Hollow Knight** | Garras de Mantis (agarre de pared + salto de pared) |
| **Hora de Aventura** | Corona del Rey Helado (nieve/ventisca/camino de hielo) |
| **Leyenda artúrica** | Excalibur (rayo Llama Solar) |
| **Half-Life** | Head Slime (parásito estilo headcrab) |
| **Scooby-Doo / Mystery Inc.** | Galleta de Scooby · Comerciante Multiversal "Shaggy" |
| **Diablo** | Linterna de Wirt (linterna que repele mobs) |
| **Original / Multiverso** | Centinela de Obsidiana, Guardia de Obsidiana, Void Crawler, Caballero Ender, Brujas de Tormenta/Veneno, Segador de Almas, Mago del Caos, etc. |

---

## 🚀 Inicio rápido

1. Instala el JAR en `plugins/` (ver [Instalación](./Installation.md)).
2. Arranca una vez para generar `config.yml`.
3. Ajusta las probabilidades de spawn, cooldowns y estadísticas del jefe como desees.
4. Usa `/msc spawn <tipo>` para invocar entidades para pruebas, o simplemente juega — los spawns naturales serán reemplazados.

---

## 📚 Páginas

- [Jefes](./Bosses.md) — El Centinela de Obsidiana, Dio Brando, Mahoraga
- [Dimensión del Ritual](./Ritual-Dimension.md) — El mundo privado del jefe: cómo entrar e invocar al Centinela
- [Criaturas](./Creatures.md) — Todos los mobs que reemplazan spawns naturales + el ejército ZombieHorseTrap
- [Armas](./Weapons.md) — Excalibur, Gran Espada de Ascuas, Filo Nullshear, Guadaña Soulreap, Aether Pullshot, Talismán Skyfire, Forja del Caos
- [Armaduras y Reliquias](./Armor-and-Relics.md) — Rueda de Ocho Manos, Bastión de Obsidiana, reliquias de mano secundaria
- [Objetos](./Items.md) — Corona del Rey Helado, Garras de Mantis, Linterna de Wirt, Cabeza del Stand de Dio, Mina Militar, Galleta de Scooby, Gelatina de Head Slime
- [Componentes](./Components.md) — Los 16 drops de crafteo y qué mob proporciona cada uno
- [Música](./Music.md) — Canciones NBS incluidas, discos de jukebox y el Disc Trader, créditos de canciones
- [Recetas](./Recipes.md) — Todas las recetas de crafteo personalizadas (formas, ingredientes, niveles)
- [Comandos](./Commands.md) — Referencia completa de `/msc` (spawn, give, seal, dummy, attack, music, dimtp, cleanstands)
- [Arquitectura](./Architecture.md) — Estructura del código, convenciones y cómo ampliar el proyecto
- [Instalación](./Installation.md) — Instalación paso a paso, requisitos, solución de problemas