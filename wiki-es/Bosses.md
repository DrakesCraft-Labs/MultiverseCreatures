# 🐉 Jefes

MultiverseCreatures incluye **un jefe final** y **tres minijefes**. Todos los jefes se invocan con `/msc spawn <tipo>` (solo OP) y tienen salud/daño/cooldowns configurables en `config.yml`.

---

## 🛡️ EL CENTINELA DE OBSIDIANA — Jefe final

Un ArmorStand animado gigante de escala 7.5×. El clímax del plugin.

| Estadística | Valor por defecto |
|---|---|
| Salud | `armor-stand-boss.health` (por defecto 500) |
| Barra de jefe | `SEGMENTED_6`, roja → azul a lo largo de 5 fases |
| Música | `Undertale — Megalovania` (radio de 60 bloques, se detiene al morir) |
| Equipamiento | Netherita completa (trim Amatista/Silencio) + Lanza de Netherita + Escudo irrompible |
| Invocación | `/msc spawn armorstand` (alias `armorstandboss`) |

### Fases (las transiciones ocurren según el umbral de HP)

| Fase | % HP | Efecto de transición |
|---|---|---|
| 0 — Roja | >80% | Ira: knockback + Debilidad I a los jugadores cercanos, sello de pentagrama grande |
| 1 — Púrpura | >60% | Barrera: invulnerable 100t, cura +30 HP, sello celestial |
| 2 — Amarilla | >40% | Tormenta: 15 rayos en un radio de 12 bloques, Lentitud II + Debilidad II |
| 3 — Verde | >20% | Desesperación: invulnerable 80t, daño AoE ×1.5 + Oscuridad II + Ceguera I + Lentitud III |
| 4 — Azul | ≤20% | fase final |

### Comportamiento de la IA

- **Modo suelo** elige entre Círculo de Curación (<40% HP, 25%), Vuelo (15%), Sello de Escudo (35%), Ataque de Suelo (55%), Bombardeo Flotante (por defecto).
- **Modo vuelo** ejecuta ataques aéreos aleatorios cada 80 ticks; aterriza con AirSlam cuando se han realizado ≥10 ataques únicos.
- **Estados defensivos** (aleatorios, solo por debajo del 50% de HP, en el suelo): **Piel de Piedra** (×0.5 daño recibido), **Barrera Reflectante** (×0.7 daño + 30% reflejado), **Escudo Absorbente** (absorbedor de 100 HP que visualmente cambia de azul a rojo).

### Mecánicas especiales

- **Escudo Plantado / Ground Slam** — planta el escudo como ItemDisplay (escala 7.5), realiza un GroundSlam retrasado y lo recupera después.
- **Sello de Escudo** — esfera protectora hemisférica de partículas dust+END_ROD durante 200 ticks, ×0.7 daño entrante, con 12 escudos ItemDisplay en órbita.
- **Círculo de Curación** — lanzamiento de 35 ticks, círculo verde, cura hasta el 5% de la HP máxima en 200 ticks, ×0.8 daño recibido mientras está activo.
- **Bombardeo Flotante ("CrossBarrage")** — sube a y+15, traza una forma de X, dispara rayos X que explotan infligiendo `hover-barrage-damage` (12) + knockback.
- **Llamada del Triángulo** — invoca un sello de triángulo mágico + refuerzos (escala con el número de jugadores):
  - Modo aéreo: Ghast Infernal + Fantasma Acechador Nocturno (que lleva un Esqueleto Francotirador con arco Power V / Infinity).
  - Modo suelo: Bestia de Guerra Ravager (300 HP, 24 de daño) que lleva un Evocador Sacerdote Oscuro (40 HP, Velocidad I).
  - Las invocaciones llevan la etiqueta `MSC_ArmorBossSummoned`; el fuego amigo entre el jefe y sus invocaciones está desactivado.
- **Pentagrama del Cielo** — sellos de pentagrama por jugador 30 bloques por encima, que explotan en una columna tras 80 ticks — `seal-damage` (15) en un radio de 6 bloques + empuje hacia arriba.
- **Anillos de Onda Expansiva** — 10 anillos en expansión, daño de suelo que decae con la distancia, empuje hacia arriba, escombros de FallingBlock.

### Registro de ataques — 33 ataques en total

Todos los ataques son clases que extienden `BossAttackBase` bajo `entities/boss/attack/<aerial|ground|ranged>/`, registrados en `ArmorStandBoss.initAttacks()` y despachados polimórficamente vía `attackRegistry.get(name).execute(instance)`. Activa cualquiera manualmente:

```
/msc attack <nombre-del-ataque> [rango]
```

| Suelo (11) | Aéreos (13) | A distancia (12) |
|---|---|---|
| groundslam | starfall | lancesnipe |
| groundshatter | aerialrush | meteorstorm |
| shieldbash | sonicboom | voidbeam |
| lancestorm | lightningstorm | frostlance |
| earthpillar | gravitywell | lightningspear |
| chaingrapple | crossslash | shadowvolley |
| warstomp | novaburst | chainlightning |
| armorspikes | darkorb | crystalbarrage |
| vortexpull | windcutter | arcaneorb |
| mirrorimage | heavenlyjudgment | voidrift |
| doombeam | rainoflances | arcanemissiles |
|  | airslam | spiritbeam |
|  | hoverbarrage (crossbarrage) |  |

Objetivos adicionales de `/msc attack` para **mecánicas y transiciones de fase**: `trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`, `phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`, `stoneskin`, `reflectbarrier`, `absorbshield`.

### Drops

1000 XP al morir, más el broadcast del título "THE OBSIDIAN SENTINEL / Has been defeated!". Rayo + sonido de muerte de wither al morir.

---

## 🌟 Dio Brando — Minijefe (JoJo's Bizarre Adventure)

| Estadística | Valor por defecto |
|---|---|
| Salud | `dio-boss.health` (300) |
| Daño de ataque | `dio-boss.damage` (10) |
| Cooldown | `dio-boss.cooldown-ms` (120 s) |
| Invocación | `/msc spawn dio` |
| Spawn natural | `dio-boss.spawn-chance` (0.5%) — reemplaza Zombies (no de spawners) |

**Apariencia:** Zombie con un casco de cabeza de jugador personalizado de Dio (textura de skin base64), armadura dorada con trims de Netherita/Esmeralda (patrones Vex/Silence/Ward). `MaximumNoDamageTicks = 0` (sin i-frames).

**El Stand:** Un ArmorStand invulnerable (`MSC_DioStand`) que flota detrás de Dio, con la Cabeza del Stand de Dio + armadura dorada.

### Habilidades

- **THE WORLD: CONGELACIÓN** — si no hay ningún jugador huyendo en rango, congela a todos los jugadores dentro de `freeze-radius` (50) durante `freeze-duration-ticks` (100) usando `FreezeAbility`. Los jugadores quedan bloqueados con Lentitud 255 + Salto Mejorado 128 (posición bloqueada, rotación de cabeza permitida). Título `"THE WORLD: FREEZING · Time has stopped!"`. Tras la duración, inflige `freeze-damage` (10) a todos los jugadores dentro de `freeze-damage-radius` (30). Una animación de 24 espadas ItemDisplay (IRON_SWORD brillantes) rodea a cada jugador congelado y lo atraviesa en ~15 ticks.
- **THE WORLD: TELEPORT** — apunta al jugador más lejano fuera de `teleport-inner-radius` (25), teletransporta a Dio 2 bloques detrás de él, aplica Oscuridad I + Lentitud I (100 ticks), aumenta la velocidad de ataque a 100 y activa un **Puñetazo del Stand**: animación de 3 ticks alternando poses de brazos en el ArmorStand del Stand con partículas CRIT + sonido de ataque fuerte en la ubicación del objetivo.
- **En cada golpe cuerpo a cuerpo** Dio también activa una animación de Puñetazo del Stand sobre el jugador dañado.

**Drops:** `dio-boss.drop-chance` (10%) de dropear la **Cabeza del Stand de Dio**. 500 XP. Rayo + sonido de muerte de wither al morir; el ArmorStand del Stand se elimina.

---

## ⚙️ Mahoraga — Minijefe (Jujutsu Kaisen)

"Divino General de la Espada de Ocho Manos Divergente Sila" — la adaptación hecha realidad.

| Estadística | Valor por defecto |
|---|---|
| Salud | 250 |
| Invocación | `/msc spawn mahoraga` |
| Spawn natural | `mahoraga.spawn-chance` (2%) — reemplaza Zombies |

**Lógica de adaptación (escaneo por tick de la armadura y armas del objetivo):**

| Atributo del objetivo | Mahoraga gana |
|---|---|
| Armadura de Diamante/Netherita + niveles de Protección | bonificación de **Daño de Ataque** escalada |
| Armadura de Slimefun / Tinker (`mahoraga.slimefun-adaptation`, dependencia suave) | bonificación de **Daño de Ataque** por nivel del material (0.3 blando → 3.0 Singularidades/Infinity) |
| Set completo de Mail Links de Infinity Singularity (`mahoraga.instakill-infinity-armor`) | **Muerte instantánea** — atraviesa el trait "Infinite Defence" (daño = 1) |
| Mejora de **Diamante** de Tinker en el arma empuñada (`mahoraga.ignore-diamond-mod`) | **30%** de probabilidad de ignorar el reflejo y la cancelación de cada golpe |
| Protección total > 5 (Diamante/Netherita) | amplificador de **Fuerza** = total/5 |
| Nitidez **o Castigo** totales máximos en cualquier arma (5 niveles por rango) | nivel de **Resistencia** (máx. 4) = floor((Nitidez + Castigo)/5) |
| Encantamientos de Knockback | **Resistencia al Knockback** = totalKnockback × 0.3 |
| Objetivo a > 4 bloques | **Velocidad I** durante 30 ticks |
| Objetivo cercano | Se elimina la Velocidad |

Atuendo: casco de vidrio blanco + armadura de cuero blanca (irrompible).

**Protección de fuego amigo MSC:** si tanto el atacante como el objetivo llevan cualquier etiqueta de scoreboard `MSC_`, el evento de daño se cancela — Mahoraga no puede dañar a otros mobs MSC (y viceversa).

**Drops:** 75% de probabilidad de **Esencia de Rueda**. 150 XP. Mensajes de muerte de jugador personalizados vía `mahoraga.death-messages`.

---

## ♟️ Kinger — Minijefe (The Amazing Digital Circus)

Un rey de ajedrez viviente: un ArmorStand invisible de escala 2.0 vestido con un traje de 15 piezas ItemDisplay (base, piernas, torso, cuello, cinturón, brazos, cabeza y adorno) que camina, pelea y persigue jugadores como una pieza de ajedrez cobrada vida.

| Estadística | Valor por defecto |
|---|---|
| Salud | `kinger.health` (120) |
| Rango de agresión | `kinger.aggro-range` (25 bloques) |
| Velocidad de movimiento | `kinger.move-speed` (0.32) |
| Rango / radio / daño cuerpo a cuerpo | `kinger.melee-range` (3) · `kinger.melee-radius` (3.5) · `kinger.melee-damage` (8) |
| Rango / daño a distancia | `kinger.ranged-range` (30) · `kinger.ranged-damage` (6) |
| Cooldowns | cuerpo a cuerpo 25 ticks · a distancia 45 ticks |
| Invocación | `/msc spawn kinger` — **o** coloca un ArmorStand |
| Reemplazo de ArmorStand | `kinger.spawn-on-armorstand-chance` (0.01 = 1% de los ArmorStands colocados se convierten en Kinger; pon 0 para desactivar) — respeta `kinger.enabled` |

### Comportamiento de la IA

- **Persigue** al jugador más cercano dentro del rango de agresión (camina a `move-speed`, se ancla al suelo) y **mira** al objetivo mientras sigue su inclinación de cabeza.
- **Cuerpo a cuerpo** (≤3 bloques): ráfaga de partículas púrpuras + humo, `melee-damage` a todos los jugadores dentro del radio cuerpo a cuerpo, con knockback de velocidad 1.3.
- **A distancia** (>3 y ≤30 bloques): dispara una **ShulkerBullet** desde la mano derecha (`MSC_KingerBullet`) con sonido de disparo de shulker.
- **Animaciones**: balanceo al caminar, preparación de golpe cuerpo a cuerpo y poses de lanzamiento a distancia en las piezas del traje.

**Barra de jefe:** barra púrpura "Kinger", siempre actualizada con la salud actual.

**Persistencia:** etiquetado `MSC_Kinger`, por lo que sobrevive a recargas del plugin y se retoma al iniciar.

**Muerte:** elimina todos los displays del traje y transmite uno de los `kinger.death-messages` temáticos de ajedrez ("checked by the King", "knocked off the board", "lost the game"...).