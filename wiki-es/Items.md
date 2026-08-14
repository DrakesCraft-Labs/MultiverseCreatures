# 🎒 Objetos

Esta página cubre los objetos varios distintivos (Corona del Rey Helado, Garras de Mantis, Linterna de Wirt, Cabeza del Stand de Dio), objetos de utilidad (Mina Militar) y alimentos (Galleta de Scooby, Gelatina de Head Slime).

Todos los objetos usan `ItemBuilder` y etiquetas PDC `msc_<objeto>` únicas. Usa **`/msc give <objeto>`** para obtenerlos (solo OP).

---

## 🧊 Corona del Rey Helado (Hora de Aventura — Ooo)

> "Gunter, ¿por qué tienes que ser así?"

Una corona de invierno eterno que otorga dominio sobre el hielo y la nieve.

| Estadística | Valor |
|---|---|
| Material | HORN_CORAL_FAN |
| Ranura | Mano principal O mano secundaria (no puede colocarse como casco de armorstand) |
| Pasiva de combate | El portador recibe **×0.8 de daño** (reducción del 20%) + inmune a la causa de daño FREEZE |

### Habilidades

| Control | Habilidad | Cooldown |
|---|---|---|
| **Click derecho** (sin agacharse) | **Lanzamiento de Bloque de Nieve** — Selecciona un bloque de nieve/hielo dentro de 5 bloques (visual elevado). En un segundo click derecho, apunta a una LivingEntity dentro de 30 bloques para **lanzar el bloque** como proyectil `FallingBlock` (velocidad balística, gravedad 0.08, máx. 100 ticks). | 10 s por lanzamiento |
| **Click derecho** (agachado) | **Ventisca** — AoE en expansión de 5 segundos hasta un radio de 8 bloques. 3 de daño/tick + Lentitud II + Oscuridad II (40t) + empuje hacia afuera + congela el agua cercana a HIELO. | 60 s |
| **Click izquierdo** | **Alternar Camino de Hielo** — Caminar sobre el agua la convierte en `FROSTED_ICE` (área 3×3, 30% de probabilidad para bloques adyacentes). Partículas: SNOWFLAKE. | Sin cooldown |

**Efectos del Lanzamiento de Bloque de Nieve al impactar (12 de daño + knockback +0.3 Y, congela el agua cercana a HIELO):**
- Bloque de nieve → Lentitud II + Debilidad I (5 s)
- Bloque de hielo → Lentitud I + Debilidad I + Náusea I (5 s)

Sonidos: BLOCK_SNOW_STEP, BLOCK_NOTE_BLOCK_CHIME, ENTITY_SNOW_GOLEM_SHOOT, BLOCK_GLASS_BREAK, ENTITY_ELDER_GUARDIAN_CURSE, ENTITY_GENERIC_EXTINGUISH_FIRE, WEATHER_RAIN_ABOVE, CLOUD.

- **Give:** `/msc give icecrown` (alias `crown`)
- **Intercambio:** del Comerciante Multiversal por 48 Estrellas del Nether + 64 Hielo Azul
- **Tema:** Hora de Aventura (Rey Helado, Gunter)

---

## 🦗 Garras de Mantis (Hollow Knight — Hallownest)

> "Los señores mantis observan desde arriba."

Garras forjadas con la seda y el hierro de Deepnest.

| Estadística | Valor |
|---|---|
| Material | SHEARS |
| Pasiva 1 | **Agarrarse a las paredes** — Shift mientras te agachas + en el aire + contra una pared → caída lenta (velocidad vertical limitada a -0.1) + Salto Mejorado II (40 t) para aterrizar con seguridad |
| Pasiva 2 | **Salto de pared** — Espacio estando en el aire junto a una pared → velocidad Y = 0.55 (`WALL_JUMP_VERTICAL`), resetea la distancia de caída + sonido sweep_attack + partículas CRIT |
| Restricción | No puede romper bloques mientras la sostiene |
| Irrompible | Sí (oculto) |
| Modelo personalizado | 1002 |
| Implementación del salto de pared | Un manejador de paquetes Netty personalizado intercepta `ServerboundPlayerInputPacket` para detectar entradas de salto de flanco ascendente. |

- **Give:** `/msc give mantisclaws` (alias `claws`)
- **Intercambio:** del Comerciante Multiversal por 16 Lingotes de Hierro + 8 Cuerdas
- **Tema:** Hollow Knight (Deepnest, Señores Mantis)

---

## 🔮 Linterna de Wirt (Khand)

> "La llama no conoce el invierno."

Una linterna que guarda un alma perdida.

| Estadística | Valor |
|---|---|
| Material | SOUL_LANTERN |
| Ranura | Mano principal O secundaria |
| Tarea pasiva | Cada 20 ticks mientras se sostiene: <br>• Aplica Visión Nocturna I (100 t, ambient=false, overwrite=false) <br>• **Repele** a todas las entidades vivas no jugador dentro de 12 bloques (`REPEL_RADIUS`): velocidad hacia afuera (+0.3 Y, máx. 0.6) + limpia su objetivo si es el portador + 30% de probabilidad de generar SOUL_FIRE_FLAME |
| Etiqueta PDC | `msc_wirts_lantern` |
| Eventos | `EntityTargetEvent` cancelado si el objetivo es el portador · `EntityDamageEvent` cancelado a menos que el atacante sea un jugador (el portador es inmune al daño de no jugadores) · `EntityDamageByEntityEvent` cancelado si la víctima es un no jugador (el portador no puede dañar mobs) · click derecho + manipulación de armorstand cancelados (la lámpara es puramente protectora) |

- **Give:** `/msc give wirtslantern` (alias `lantern`)
- **Intercambio:** del Comerciante Multiversal por 32 Arena de Almas + 16 Tierra de Almas
- **Tema:** Khand (linterna protectora estilo Diablo Tristram)

---

## 🌟 Cabeza del Stand de Dio (JoJo's Bizarre Adventure)

> "Za Warudo! Toki wo tomare!"

La manifestación del poder de The World.

| Estadística | Valor |
|---|---|
| Material | PLAYER_HEAD (skin Base64 personalizada del Stand de Dio, nombre de perfil `"Dio_Stand"` con UUID aleatorio) |
| Modelo personalizado | 1001 |
| No se puede colocar ni usar como casco | Sí (eventos de colocación + ranura 39 cancelados) |

### Habilidades

| Disparador | Habilidad |
|---|---|
| **Pasiva (objeto en cualquier parte del inventario)** | Se invoca un ArmorStand "Stand" invulnerable (`MSC_PlayerDioStand`) detrás del jugador con armadura dorada + la Cabeza del Stand de Dio. Sigue al jugador cada tick. Se elimina al salir/soltar. |
| **Click derecho (mientras se sostiene)** | **THE WORLD: CONGELACIÓN** — llama a `FreezeAbility.freezeInArea(player, freeze-radius, freeze-duration)` para congelar a todos los jugadores cercanos. Omite a los jugadores que sostienen su propia Cabeza del Stand de Dio. Título + sonido. |
| **Ataque (click izquierdo con la cabeza en la mano)** | Cancela el daño normal, aplica `dio-stand.stand-damage` (por defecto 4.0) directamente vía `setHealth` (puñetazo THE WORLD). Activa una animación de Puñetazo del Stand — alterna poses de brazo izquierdo/derecho cada `stand-interval-ticks` (3 t) durante `stand-duration-ticks` (100 t) + partículas CRIT + sonido ENTITY_PLAYER_ATTACK_STRONG. |

### Config

| Clave | Valor por defecto |
|---|---|
| `dio-stand.cooldown-ms` | 120000 (2 min) |
| `dio-stand.freeze-radius` | 50 |
| `dio-stand.freeze-duration-ticks` | 100 |
| `dio-stand.stand-duration-ticks` | 100 |
| `dio-stand.stand-interval-ticks` | 3 |
| `dio-stand.stand-damage` | 4.0 |
| Cooldown interno del puñetazo | 15 s |

Probabilidad de drop del DioBoss: `dio-boss.drop-chance` (10%).

- **Give:** `/msc give diostand` (alias `dio`)
- **Tema:** JoJo's Bizarre Adventure (DIO + The World)

---

## 🧨 Mina Militar (Military)

> "Un paso es todo lo que se necesita."

Un explosivo crafteado auto-camuflado para parecer el terreno circundante.

| Estadística | Valor |
|---|---|
| Material | TNT |
| Camuflaje | Se determina automáticamente por el bloque inferior, o el bloque más común dentro de un radio de 3 bloques. Excluye: bedrock, barreras, pancartas, carteles, puertas, trampillas, camas, cajas de shulker, aditamentos de redstone, plantas, alfombras, velas, huevos, spawners, etc. |
| Disparadores de detonación | Un jugador no creativo/volador **pisa el bloque superior**; **romper** el bloque; **click derecho** sobre el bloque; o que otra explosión elimine la entrada |
| Potencia de explosión | 4.0 (rompe bloques) |
| Etiqueta PDC | `msc_military_mine` |

- **Give:** `/msc give militarymine` (alias `mine`)
- **Crafteo:** requiere **Componente Militar** (dropeado por las unidades de ZombieHorseTrap)
- **Tema:** Military (cadena de ZombieHorseTrap)

---

## 🍪 Alimentos

### Galleta de Scooby (Mystery Inc.)

> "Scooby-Dooby-Doo... ¡Esto sabe a valor!"

| Estadística | Valor |
|---|---|
| Material | COOKIE |
| Alimento | 2 · Saturación 0.4 |
| **Efecto al consumir** | **Resistencia VI** (amplificador 5) durante 10 segundos (200 ticks) |
| Etiqueta PDC | `msc_scooby_cookie` |

**Lore:** *"Una galleta misteriosa que pulsa con energía de otro mundo."*

- **Give:** `/msc give scoobycookie` (alias `cookie`)
- **Intercambio:** del Comerciante Multiversal (5 por 20 Diamantes)
- **Tema:** Mystery Inc. (Scooby-Doo)

---

### Gelatina de Head Slime (Reino Slime)

> "¡Babosa pero satisfactoria!"

Una gelatina elástica, temblorosa y extrañamente sabrosa.

| Estadística | Valor |
|---|---|
| Material | MAGENTA_GLAZED_TERRACOTTA |
| Alimento | 4 · Saturación 2.4 |
| **Efecto de click derecho** | Añade el UUID del usuario al conjunto `HeadSlime.immunePlayers` durante **10 segundos** (200 ticks). Se renderiza un anillo de partículas de bruja alrededor del jugador inmune; cualquier Head Slime adherido se desprende automáticamente. |
| Etiqueta PDC | `msc_head_slime_gelatin` |

- **Give:** `/msc give headslimegelatin` (alias `gelatin`)
- **Crafteo:** de **Corazón de Head Slime** (dropeado por Head Slime)
- **Tema:** Reino Slime