# 🐉 Jefes

MultiverseCreatures incluye **un jefe final** y múltiples y formidables **minijefes y jefes con modelos personalizados**. Todos los jefes se invocan con `/msc spawn <tipo>` (solo OP) y tienen salud/daño/cooldowns configurables en `config.yml`.

---

## 🛡️ EL CENTINELA DE OBSIDIANA — Jefe final

Un ArmorStand animado gigante de escala 7.5×. El clímax del plugin. A diferencia de los jefes vestidos, el Centinela **es** el stand, así que su escala es a la vez el tamaño del modelo y la caja que golpean los jugadores.

| Estadística | Valor por defecto |
|---|---|
| Salud | `armor-stand-boss.health` (por defecto 3200) |
| Hitbox | `armor-stand-boss.hitbox-scale` (7.5) — escala del propio stand del jefe: tamaño del modelo y hitbox a la vez, unos catorce bloques de guerrero (acotado a 0.25–8) |
| Barra de jefe | `SEGMENTED_6`, roja → azul a lo largo de las fases (ver `phase-thresholds`) |
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

La escalera es datos, no código: `armor-stand-boss.phase-thresholds` contiene las fracciones de salud en las que empieza cada fase siguiente, de mayor a menor. El número de fases es uno más que las entradas de esa lista, y es también cuántos cuadrados muestra el título de la barra de jefe — uno por fase, rojo para las que quedan y gris para las ya gastadas. Las entradas fuera de `(0, 1]` se descartan y los duplicados se colapsan, así que una errata no puede dejar al jefe con una sola fase.

### Comportamiento de la IA

- **Modo suelo** elige entre Círculo de Curación (<40% HP, 25%), Vuelo (15%), Sello de Escudo (35%), Ataque de Suelo (55%), Bombardeo Flotante (por defecto).
- **Modo vuelo** ejecuta ataques aéreos aleatorios cada 80 ticks; aterriza con AirSlam cuando se han realizado ≥10 ataques únicos.
- **Estados defensivos** (aleatorios, solo por debajo del 50% de HP, en el suelo): **Piel de Piedra** (×0.5 daño recibido), **Barrera Reflectante** (×0.7 daño + 30% reflejado), **Escudo Absorbente** (absorbedor de 100 HP que visualmente cambia de azul a rojo). Sus duraciones salen de `defense-duration-stone-skin-ticks` (200), `defense-duration-reflect-barrier-ticks` (160) y `defense-duration-absorb-shield-ticks` (300).
- **Recuperación de suelo** — un jefe en modo suelo solo ataca mientras `isOnGround` es cierto. Si se queda sin bloque sólido debajo (vacío, agua, un agujero, un borde), flotaba en silencio para siempre. Tras `ground-recovery-grace-ticks` (40) ticks sin suelo se teletransporta a la columna más cercana con piso y espacio libre, prefiriendo la zona de su objetivo actual y recurriendo al spawn del mundo si no encuentra nada, y reanuda el ataque con los cooldowns reiniciados.
- **Despawn** — sin nadie en un radio de 100 bloques el jefe sigue peleando durante `no-player-despawn-ticks` (200, ~10 s) antes de despawnear y limpiar sus tareas, sellos, música y barra de jefe. Ponlo a `0` para que el jefe se vaya en cuanto la arena se vacíe.
- **Daño penetrante** — con `penetrating-damage: true` los golpes del jefe ignoran armadura y encantamientos de Protección (se reaplican como daño `OUT_OF_WORLD`, con cap de `max-damage-dealt` (15) por golpe). La Resistencia solo se perfora *en parte*: `penetrating-resistance-pierce: 0.2` hace que el jefe ignore el 20% de la mitigación de la poción, así que un jugador con Resistencia I (20% de reducción) sigue bloqueando el 16% del golpe. `0.0` deja la Resistencia totalmente efectiva y `1.0` la ignora por completo. Usa `/msc debug [jugador]` tras un golpe para ver el desglose completo (daño del evento, las reducciones devueltas, la perforación aplicada y el valor final); el mismo comando también informa de lo que Nix y Jack Star hacen y reciben de ese jugador.

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

### Registro de ataques — 55 ataques en total

Todos los ataques son clases que extienden `BossAttackBase` bajo `entities/boss/attack/<aerial|ground|ranged|defensive>/`, registrados en `ArmorStandBoss.initAttacks()` y despachados polimórficamente vía `attackRegistry.get(name).execute(instance)`. Activa cualquiera manualmente:

```
/msc attack <nombre-del-ataque> [rango]
```

Los 55 nombres los lista `/msc attack help` (cuatro páginas, una por categoría) y los ofrece el autocompletado. `/msc attack` también acepta las mecánicas de arriba (`flyup`, `land`, `heal`, `reset`, las cuatro transiciones `phase*`) y el alias heredado `crossbarrage` de `hoverbarrage`.

| Suelo (18) | Aéreos (16) | A distancia (15) | Defensivos (6) |
|---|---|---|---|
| groundslam | starfall | lancesnipe | stoneskin |
| groundshatter | aerialrush | meteorstorm | reflectbarrier |
| shieldbash | sonicboom | voidbeam | absorbshield |
| lancestorm | lightningstorm | frostlance | shieldseal |
| earthpillar | gravitywell | lightningspear | healingcircle |
| chaingrapple | crossslash | shadowvolley | trianglecall |
| warstomp | novaburst | chainlightning |  |
| armorspikes | darkorb | crystalbarrage |  |
| vortexpull | windcutter | arcaneorb |  |
| mirrorimage | heavenlyjudgment | voidrift |  |
| doombeam | rainoflances | arcanemissiles |  |
| lanceflurry | airslam | spiritbeam |  |
| whirlwindslash | hoverbarrage (crossbarrage) | soultethers |  |
| executionsweep | eclipsefall | plaguebrand |  |
| obsidianspire | bladering | runemines |  |
| earthmaw | obsidianwings |  |  |
| shadowstep |  |  |  |
| runeward |  |  |  |

Objetivos adicionales de `/msc attack` para **mecánicas y transiciones de fase**: `flyup`, `land`, `heal`, `reset`, `phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`.

### Segunda tanda — diez ataques más y cómo reconocerlos

Cada uno de los diez añadidos está animado alrededor de una seña inequívoca, para que el jugador pueda nombrar el ataque desde el primer segundo del preaviso. Los overrides de daño viven bajo `entities.armor-stand-boss.*-damage`.

| Ataque | Categoría | Seña de la animación | Clave de daño (por defecto) |
|---|---|---|---|
| `obsidianspire` | Suelo | Los dos brazos suben rectos sobre la cabeza y el cuerpo se echa atrás, y luego una línea de pilares volcánicos erupciona uno a uno hacia delante. Cada pilar lanza a su víctima **hacia arriba**, y la Cruz del Verdugo se dibuja a lo largo del recorrido. | `obsidian-spire-damage` (11) |
| `earthmaw` | Suelo | Se agacha mientras dos filas de dientes se abren sobre un cono frontal de 34°, el suelo se agrieta con un Sello de Seísmo y entonces las filas **se cierran de golpe** y arrastran al centro a todo el que pillan dentro. | `earth-maw-damage` (9) |
| `shadowstep` | Suelo | Se encorva con los brazos cruzados, un pentagrama se lo traga y **desaparece** — el único ataque que se teletransporta. Reaparece 1,35 bloques por detrás del objetivo con un segundo sello y gira 360° para apuñalarlo. | `shadow-step-damage` (15) |
| `runeward` | Suelo | Un **lanzamiento de rodillas** dentro de un pentagrama grande, y después planta una runa de amatista que se queda atrás. Cada 25 ticks la runa late y empuja un anillo de runas (Debilidad + Lentitud) durante 150 ticks antes de romperse. | `rune-ward-damage` (4) |
| `eclipsefall` | Aéreos | Los dos brazos suben rectos mientras un **disco oscuro** se forma tres bloques por encima, rodeado por un pentagrama. El disco cae con estela de cometa y detona como una onda negra que ciega a los que están cerca. | `eclipse-fall-damage` (16) |
| `bladering` | Aéreos | Ocho lanzas de netherita aparecen en un **anillo visible orbitando su cuerpo** bajo un sello de alas. Gira con el anillo, lo ensancha y lanza las lanzas una a una con vuelo teledirigido. | `blade-ring-damage` (7) |
| `obsidianwings` | Aéreos | Cuatro paneles de alas le crecen en los hombros y se despliegan. Tres **aleteos** bajan los paneles y lanzan anillos de esquirlas de obsidiana hacia fuera; después las alas se pliegan y ambas golpean a la vez en una onda expansiva. | `obsidian-wings-damage` (8), `obsidian-wings-slam-damage` (15) |
| `soultethers` | A distancia | Los brazos suben sobre la cabeza y las brasas de alma de todos los jugadores cercanos drenan hacia su pecho. Una **atadura luminosa queda dibujada** de su pecho hasta cuatro jugadores mientras los arrastra y los drena, y al final tira con fuerza y los reúne de golpe. | `soul-tether-damage` (3), `soul-tether-snap-damage` (13) |
| `plaguebrand` | A distancia | Abre los brazos con las palmas hacia arriba y un círculo verde enfermizo se cierra sobre la víctima elegida. La marca cae como un estallido de esporas, drena a su huésped cada segundo y **salta a quien esté a su lado**; su cabeza sigue al jugador marcado tras el impacto. | `plague-brand-hit-damage` (9), `plague-brand-damage` (4) |
| `runemines` | A distancia | Un barrido bajo del brazo por el suelo bajo un triángulo rúnico, y luego seis runas salen **lanzadas alrededor del objetivo**. Tardan 25 ticks en armarse (apagadas, planas), se vuelven violetas y laten, y estallan hacia arriba cuando alguien las pisa — o se apagan solas a los 140 ticks. | `rune-mine-damage` (8) |

Las rotaciones aleatorias también los usan: `obsidianspire`, `earthmaw`, `shadowstep` y `runeward` en las tablas de suelo, `eclipsefall`, `bladering` y `obsidianwings` en las aéreas, y `soultethers`, `plaguebrand` y `runemines` en la de distancia. Los tres a distancia disparan tanto en suelo como en vuelo; los otros siete exigen el estado correspondiente.

### Drops

1000 XP al morir, más el broadcast del título "THE OBSIDIAN SENTINEL / Has been defeated!". Rayo + sonido de muerte de wither al morir.

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
| Mejora de **Diamante** de Tinker en el arma empuñada (`mahoraga.ignore-diamond-mod`) | **Siempre ignorada** — el reflejo y la cancelación de cada golpe nunca aplican |
| Espada Tinker con material **Infinity Singularity** (`mahoraga.infinity-weapon-adaptation`) | Mahoraga solo recibe **1 de daño** por golpe |
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

Un rey de ajedrez viviente: un ArmorStand invisible vestido con un traje de 15 piezas ItemDisplay (base, piernas, torso, cuello, cinturón, brazos, cabeza y adorno) que camina, pelea y persigue jugadores como una pieza de ajedrez cobrada vida. El stand no lleva escala — una hitbox normal de 0.5 × 1.975 bloques, ajustada al traje en vez de la doblada anterior — y las piezas se agrupan en extremidades, así que una pierna gira desde su cadera y la espinilla sigue al muslo en lugar de girar cada pieza sobre su propia ancla — y la **rodilla se dobla con el paso**, porque la espinilla articula sobre una segunda articulación a medio camino entre el muslo y la espinilla exportados en vez de quedarse congelada contra el muslo. El paso está calibrado a esa caja: incluso el paso más profundo deja la espinilla doblada sobre el stand, así que un espadazo apuntado a la pierna nunca pasa por el aire.

| Estadística | Valor por defecto |
|---|---|
| Salud | `kinger.health` (120) |
| Hitbox | `kinger.hitbox-scale` (1.0) — tamaño del stand invisible por el que se golpea el traje; 1.0 es un stand normal y ya cubre todo el modelo (acotado a 0.25–8) |
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
- **Animaciones**: balanceo al caminar, preparación de golpe cuerpo a cuerpo y poses de lanzamiento a distancia en las piezas del traje. Cada pieza pertenece a un grupo de extremidad rígido que gira alrededor de una única articulación (cadera, hombro, cintura o cuello), así que el traje no se desmonta al moverse.

**Barra de jefe:** barra púrpura "Kinger", siempre actualizada con la salud actual.

**Persistencia:** etiquetado `MSC_Kinger`, por lo que sobrevive a recargas del plugin y se retoma al iniciar — al recargar se vuelven a enganchar las piezas que ya había creado, en vez de construir un segundo cuerpo superpuesto, y se reconstruye su barra de jefe.

**Muerte:** elimina todos los displays del traje y transmite uno de los `kinger.death-messages` temáticos de ajedrez ("checked by the King", "knocked off the board", "lost the game"...).

---

## 🪓 NIX - El Verdugo

Un verdugo colosal e implacable construido a partir de un **modelo personalizado de 27 piezas ItemDisplay** utilizando cabezas de jugador con texturas y transformaciones matriciales. NIX posee IA avanzada, movimiento fluido de extremidades mediante animaciones procedurales con cuaterniones JOML y brutales mecánicas de ejecución.

| Estadística | Valor por defecto |
|---|---|
| Salud | `nix-executioner.health` (450.0) |
| Hitbox | `nix-executioner.hitbox-scale` (1.9) — tamaño del stand invisible por el que se golpea el traje (acotado a 0.25–8) |
| Rango de agresión | `nix-executioner.aggro-range` (28.0 bloques) |
| Velocidad de movimiento | `nix-executioner.move-speed` (0.30) |
| Rango cuerpo a cuerpo / Daño de tajo | `nix-executioner.melee-range` (3.5) · `nix-executioner.cleave-damage` (22.0) |
| Rango de atracción con cadenas | `nix-executioner.chain-range` (24.0 bloques) |
| Cap de daño recibido | `nix-executioner.max-damage-per-hit` (100.0 por golpe; `0` desactiva el cap) |
| Cooldowns | cuerpo a cuerpo 20 ticks · cadenas 80 ticks |
| Ritual de Invocación | **El Cadalso del Verdugo** en la Boss Dimension (sacrificando `Sentencia de Muerte`) · `/msc spawn nix` (OP) |

### Habilidades y Mecánicas

- **Tajo de Guillotina (Cuerpo a cuerpo en área):**
  Al estar a distancia de golpe, Nix levanta ambos brazos y descarga un tajo descendente aplastante. Inflige `cleave-damage` (22) en un radio frontal de 3.2 bloques, empuja a los jugadores y les aplica **Wither II (Sangrado)** y **Lentitud II**.
- **Cadenas del Juicio (Atracción a distancia):**
  Cuando un objetivo intenta huir (a entre 5 y 24 bloques de distancia), Nix lanza cadenas de hierro espectrales (`Sound.BLOCK_CHAIN_PLACE`) que aprisionan a la víctima, atrayéndola con violencia hacia él e infligiéndole **Oscuridad** y **Lentitud III**.
- **Frenesí de Ejecución (Pasiva):**
  Cuando la salud del jugador objetivo cae por debajo del **25%**, Nix entra en frenesí de ejecución: su velocidad de movimiento aumenta un +30%, sus ojos emiten partículas de polvo carmesí y el compás de sus zancadas se acelera.
- **Animaciones Procedurales del Modelo:**
  Las 27 piezas (Cabeza, Torso Superior, Pelvis, Brazo Derecho de 6 piezas, Brazo Izquierdo de 6 piezas, Pierna Derecha de 6 piezas, Pierna Izquierda de 6 piezas) cuentan con contra-rotaciones de marcha sincronizadas, preparación de ataques y seguimiento del cabeceo de la mirada del jugador. Siguen a un **ArmorStand invisible** (`MSC_NixBoss`) que carga la vida real y la hitbox, y las articulaciones viven en `NixModel` (hombros en x = ±0.3514, caderas en ∓0.1171, cuello en 1.650, torso en 1.171), así que cada extremidad gira sobre su propia articulación. Cada brazo y cada pierna es una pila de dos segmentos, así que los **codos y las rodillas también se pliegan**: la pieza `_4` lleva la articulación y las cinco piezas de debajo articulan sobre ella al caminar y durante el tajo de cleave (los codos se flexionan en la preparación y se extienden en el golpe, mientras las rodillas flexan en una ligera sentadilla). El export queda 0.066 bloques fuera de la columna, así que las piezas se recentran sobre la hitbox, y el stand se escala 1.9 para que su caja (0.95 de ancho, 3.75 de alto) cubra todo el modelo en vez de dejar la cabeza fuera de una caja vanilla.

**Barra de jefe:** Barra segmentada de color rojo oscuro que muestra `NIX - El Verdugo` con niebla y cielo oscurecido.

**Persistencia:** Etiquetado `MSC_NixBoss` y `MSC_NixPart`, y cada pieza lleva además su propia etiqueta más una de propietario del stand al que pertenece — una recarga **adopta** las piezas que el jefe vivo ya tiene en vez de crear un segundo cuerpo encima, y las huérfanas se limpian.

**Muerte:** Desencadena truenos, sonido de muerte de wither, una explosión de partículas carmesí, suelta 450 XP y muestra un título de condena finalizada a los jugadores cercanos.

---

## 👨‍💻 JACK STAR — El Arquitecto del Sistema

Cinco fases, tres vidas y un cuerpo construido con once cabezas de skin.

### Estadísticas del jefe

| Campo | Valor |
|---|---|
| Vida | `jackstar-architect.health` (700.0) |
| Hitbox | `jackstar-architect.hitbox-scale` (1.2) — tamaño del stand invisible por el que se golpea el traje (acotado a 0.25–8) |
| Vidas | 3 — las dos primeras "muertes" ejecutan un reinicio **Watchdog** que restaura el 50% de la vida, y la última el 40% |
| Fases | 1 >80% · 2 >60% · 3 >40% · 4 >20% · 5 (kernel panic) ≤20% |
| Daño | `melee-damage` (16) · `slam-damage` (20) · `sigkill-damage` (35) |
| Defensas | esquive Ultra Instinct `dodge-chance` (0.22) · `packet-loss-chance` (0.25) descarta proyectiles · muros cortafuegos, firejail y telarañas |
| Agro / velocidad / alcance | `aggro-range` (32.0) · `move-speed` (0.32) · `melee-range` (3.8) |
| Ritual de invocación | Ritual **The System Architect** en la Dimensión del Jefe · `/msc spawn jack` (OP) |

### Modelo

Once cabezas de skin (`ItemDisplay`, etiqueta `msc_jackstar_part`) forman la cabeza, el torso y dos segmentos por brazo y pierna. Siguen a un **ArmorStand invisible** (`msc_jackstar_boss`) que carga la vida real y la hitbox, así que el cuerpo visible es lo que apuntan los jugadores mientras el stand lleva la contabilidad. Las articulaciones viven en `JackModel` (hombros en x = ±0.35, caderas en ∓0.12, cuello en 1.87) y cada extremidad gira sobre su propia articulación, con contra-rotaciones al caminar. Los dos brazos y las dos piernas se exportaron en dos segmentos, así que los **codos y las rodillas se pliegan además de ese balanceo**: el antebrazo y la espinilla articulan sobre su propia articulación al caminar y durante tajos cuerpo a cuerpo (los codos articulan en el arco de tajo y las rodillas flexan en pose de combate). Las piezas se recentran sobre la hitbox, que es lo que hace que el cuerpo coincida con el stand en vez de desplazarse casi un bloque hacia un lado.

### Subprocesos

Tres segundos después de aparecer, y de nuevo en cada cambio de fase — cinco veces como máximo — Jack Star invoca a otro jefe a 14 bloques: Garou, Mahoraga, Chaos Mage, Obsidian Guard, Soul Reaper o NIX, elegido al azar hasta que uno acepte. Él se queda en el campo todo el tiempo: un subproceso es presión extra, **nunca un escudo**, así que sigue peleando y sigue recibiendo daño mientras esté vivo.

### Daño recibido

Los golpes pasan por una sola puerta: primero el esquive **Ultra Instinct** (0.22, elevado a 0.45 en forma comprimida) y después el **Load Balancer**, que deja el 65% en el jefe y reparte el 35% entre cada jugador no creativo en 14 bloques. Un golpe que acierta siempre llega al jefe; `/msc debug` imprime el golpe previsto, el reparto y el valor aplicado.

**Botín:** 950 XP y el `ArchitectKernel`, con un título final para cada jugador en 60 bloques.