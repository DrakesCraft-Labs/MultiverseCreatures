# 🪖 Armaduras y Reliquias

Esta página cubre las piezas de armadura y las reliquias de mano secundaria. Todos los objetos son irrompibles y usan `ItemBuilder` + etiquetas de datos persistentes `msc_<objeto>`. Cada armadura/handler se auto-registra vía `MultiverseCreatures.onEnable()`.

---

## 🪖 Armaduras

### Rueda de Ocho Manos (Jujutsu Kaisen — Mahoraga)

> "Lo que se adapta no puede romperse; lo que se rompe no puede volver."

Una corona tallada de un fragmento de la Rueda de Ocho Manos que una vez giró contra todo daño.

| Estadística | Valor |
|---|---|
| Material | NETHERITE_HELMET |
| Cargas | 8 (`MAX_CHARGES = 8`) |
| Regeneración de cargas | 1 carga por cada `CHARGE_REGEN_TICKS` (200 ticks = 10 s); la regeneración empieza cuando se consume la primera carga |
| Activa (pasiva) | **Adaptación** — al recibir daño, consume 1 carga para volverse **inmune a esa causa de daño durante 8 segundos** |
| Cooldown de bloqueo | 15 s por causa de daño (`BLOCK_COOLDOWN_MS`); varios tipos de daño en el mismo tick generan efectos de inmunidad independientes |
| Partículas / Sonido | END_ROD, BLOCK_BEACON_ACTIVATE |
| Irrompible | Sí |

La rueda **aprende el tipo de daño que recibes** y luego se adapta. Las diferentes causas de daño tienen cooldowns independientes.

- **Give:** `/msc give eighthandledwheel` (alias `wheel`)
- **Crafteo:** Núcleo de Rueda Refinado (Esencia de Rueda → Núcleo de Rueda → *Alto Horno* → Núcleo de Rueda Fundido + Netherita Fundida → Núcleo de Rueda Refinado) + 4 Bloques de Netherita (ver [Recetas](./Recipes.md))
- **Tema:** Jujutsu Kaisen (drop de Mahoraga → Esencia de Rueda)

---

### Bastión de Obsidiana (set de 4 piezas de Netherita)

> "Más negro que la noche, más duro que la determinación."

| Pieza | Material |
|---|---|
| Yelmo | NETHERITE_HELMET |
| Peto | NETHERITE_CHESTPLATE |
| Grebas | NETHERITE_LEGGINGS |
| Botas | NETHERITE_BOOTS |

Las 4 piezas vienen pre-encantadas con **Protección IV · Protección contra Explosiones IV · Espinas II · Irrompibilidad III**.

### Bono de set completo

| Efecto | Valor |
|---|---|
| Salud Máxima | **+40%** (`MAX_HEALTH_BONUS = 0.4` AttributeModifier) |
| Resistencia al Knockback | **1.0** (inmunidad completa) |
| Inmunidad a Fuego / Lava | Sí (se cancelan las causas de daño `FIRE`, `LAVA`, `FIRE_TICK`) |
| Penalización de velocidad de movimiento | **-20%** (`SPEED_PENALTY = 0.2` AttributeModifier) |

El bono de set se re-comprueba al entrar/salir/romper el objeto y transmite mensajes de chat "set bonus activated/lost". Implementación de referencia para modificadores de set: comprobación idempotente `getModifier(key)` antes de añadir, `removeModifier(key)` al limpiar — sin necesidad de mapas por jugador.

- **Give:** `/msc give obsidianbastionhelmet` (y `chestplate` / `leggings` / `boots`) — alias `bastionhelmet` etc.
- **Tema:** Multiverso (cadena del Guardia de Obsidiana vía Fragmento de Obsidiana)

---

## 🛡️ Reliquias de mano secundaria

Las tres reliquias de mano secundaria solo funcionan en la ranura de mano secundaria y usan etiquetas `msc_<objeto>` únicas.

### Marrow Aegis

> "La arquitectura de la muerte, preservada en médula."

Un escudo tallado de hueso reforzado.

| Estadística | Valor |
|---|---|
| Material | SHIELD |
| Pasiva | **Un bloqueo exitoso refleja el 50%** del daño entrante como **daño verdadero** (`REFLECT_FRACTION = 0.5`) al atacante |
| Bono de bloqueo | Otorga **Resistencia II** + **Fuerza I** durante 5 segundos en un bloqueo exitoso |
| Cooldown de recarga | 15 s (`RECHARGE_COOLDOWN_MS`) |
| Partículas | ITEM_SHIELD_BLOCK, CRIT |
| Irrompible | Sí |

- **Give:** `/msc give marrowaegis` (alias `aegis`)
- **Crafteo:** cadena de 3 componentes — 8× Hueso Reforzado → **Médula de Hueso** → 4× Médula + Calcita + Diamante → **Placa Osificada** → *Alto Horno (solo)* → **Médula Fundida** → Marrow Aegis (ver [Recetas](./Recipes.md))
- **Tema:** Multiverso (cadena del Bone Shield vía Hueso Reforzado)

---

### Manto Veilwalker

> "El tiempo se detiene donde piso, y el mundo olvida mi nombre."

Un reloj de bolsillo cronomántico arrancado de la sombra de un Rogue. Su tic-tac dobla la luz y el tiempo.

| Estadística | Valor |
|---|---|
| Material | CLOCK |
| Activa | **Paso a Través** — Click derecho al aire |
| Duración del sigilo | 10 s (Invisibilidad + Velocidad I) |
| Cooldown del sigilo | 30 s |
| Pasiva | El primer golpe desde el sigilo inflige **+50%** de daño (`BACKSTAB_DAMAGE_MULTIPLIER = 1.5`) — la apuñalada por la espalda elimina el sigilo + muestra el mensaje "Backstab! +N% damage" |
| Sonido | ENTITY_ENDERMAN_TELEPORT |
| Etiquetas PDC | `msc_veilwalker_mantle`, etiqueta de sigilo `MSC_VeilMantle_Stealth` |
| Irrompible | Sí |

- **Give:** `/msc give veilwalkermantle` (alias `mantle`)
- **Tema:** Multiverso (cadena del Shadow Rogue vía Fragmento de Capa de las Sombras)

---

### Corazón de Escarcha (mano secundaria)

> "Late una vez por siglo, y el invierno lo sigue."

Un núcleo congelado pulsante del pecho de un Gólem de Escarcha. Solo la mano secundaria puede estabilizar su frío infinito.

| Estadística | Valor |
|---|---|
| Material | LIGHT_BLUE_DYE |
| Pasiva (solo activa en mano secundaria) | Los atacantes cuerpo a cuerpo son congelados: **Lentitud II** + **Debilidad I** durante 3 s |
| Aura | Los enemigos dentro de **4 bloques** son ralentizados |
| Bono para el portador | Otorga **Paso de Escarcha I** mientras se sostiene |
| Partículas / Sonido | SNOWFLAKE, ENTITY_PLAYER_HURT_FREEZE |
| Irrompible | Sí |

- **Give:** `/msc give frostheartoffhand` (alias `frostoffhand`)
- **Tema:** Multiverso (cadena del Gólem de Escarcha vía componente Corazón de Escarcha)