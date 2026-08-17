# ⚔️ Armas

Todas las armas son irrompibles y usan una etiqueta de datos persistente `msc_<objeto>`. Cada una tiene un ItemStack construido con `ItemBuilder` con lore y está registrada con una receta personalizada en `MultiverseCreatures.registerRecipes()`. El comportamiento vive en un `listener/<Nombre>Handler.java`.

Usa **`/msc give <objeto>`** (solo OP) para obtener cualquier arma durante las pruebas. Ver [Comandos](./Commands.md) para la lista completa de give.

---

## 🗡️ Cuerpo a cuerpo

### Excalibur (Avalon — leyenda artúrica)

> "Quien sostenga esta espada, si es digno, poseerá el poder del propio Sol."

| Estadística | Valor |
|---|---|
| Material | NETHERITE_SWORD |
| Pasiva | Otorga **Fuerza III** mientras se sostiene |
| Activa | **Llama Solar** — Click derecho dispara un rayo en la dirección de la mirada |
| Rango del rayo | `excalibur.solar-flare.range` (20) |
| Radio del rayo | 1.5 |
| Daño | 12 + 100 ticks de fuego + Ceguera I (60t) + knockback (1.5 horiz / 0.8 vert) |
| Cooldown | `excalibur.solar-flare.cooldown-ms` (15 s) |
| Partículas | FLAME, SOUL_FIRE_FLAME, END_ROD, ELECTRIC_SPARK |
| Sonidos | ENTITY_LIGHTNING_BOLT_THUNDER, BLOCK_BEACON_POWER_SELECT |

- **Give:** `/msc give excalibur` (alias `sword`)
- **Intercambio:** del Comerciante Multiversal por 16 Núcleos Estelares + 32 Lingotes de Netherita
- **Tema:** Avalon (artúrico)

---

### Gran Espada de Ascuas

> "Donde cae, el mundo arde."

Una hoja de netherita a dos manos forjada con el corazón de un Elemental de Llama.

| Estadística | Valor |
|---|---|
| Material | NETHERITE_SWORD |
| Pasiva 1 | **A dos manos**: no puede combinarse con ningún objeto de mano secundaria (el intercambio se cancela) |
| Pasiva 2 | Los enemigos golpeados arden (Aspecto Ígneo II) |
| Pasiva 3 | El portador obtiene **Resistencia al Fuego** mientras la sostiene |
| Activa | **Golpe de Ascuas** — Click derecho, AoE de radio 5 bloques |
| Daño del golpe | 12 + prende fuego a los enemigos durante 4 s + knockback (0.6 Y) |
| Cooldown del golpe | `SLAM_COOLDOWN_MS` (10 s) |
| Partículas | EXPLOSION, FLAME, LAVA |

- **Give:** `/msc give cindergreatsword` (alias `greatsword`)
- **Tema:** Multiverso (cadena de drop del Elemental de Llama vía Núcleo de Magma)

---

### Filo Nullshear

> "No está ahí, y sin embargo está."

Una hoja que corta la costura entre el mundo y la nada que hay detrás.

| Estadística | Valor |
|---|---|
| Material | NETHERITE_SWORD |
| Pasiva 1 | Cada golpe inflige **30%** del daño como **daño del vacío** (ignora armadura) |
| Pasiva 2 | Golpear al aire libre tiene **10%** de probabilidad de aplicar **Oscuridad** (5 s) |
| Activa | **Parpadeo del Vacío** — Shift + Click derecho |
| Rango del parpadeo | 30 bloques |
| Cooldown del parpadeo | 20 s |
| Partículas | PORTAL en ambos extremos |

- **Give:** `/msc give nullshearedge` (alias `nullshear`)
- **Tema:** Multiverso (cadena del Void Crawler vía Esencia del Vacío)

---

### Guadaña Soulreap

> "Cada alma hace la hoja más pesada, y sin embargo al portador más ligero."

Una guadaña curva de acero del vacío que zumba con el lamento de los no segados.

| Estadística | Valor |
|---|---|
| Material | NETHERITE_HOE |
| Pasiva | Cada golpe drena **4 HP** y cura al portador **2 HP** |
| Contador de almas | Cada golpe recoge un alma (3 almas al matar) — almacenado vía PDC `msc_soulreap_counter` |
| Activa | **Cosecha** — activación pasiva |
| Activación de Cosecha | Tras recolectar **10 almas** |
| Duración de Cosecha | 10 segundos |
| Efectos de Cosecha | Daño ×2, mejor robo de vida, aura de almas (Fuerza I + REAP_DAMAGE_MULTIPLIER = 2.0) |
| Sonidos | ENTITY_WITHER_SPAWN al activarse |

- **Give:** `/msc give soulreapscythe` (alias `scythe`)
- **Tema:** Multiverso (cadena del Segador de Almas vía Esencia de Segador)

---

### Venomfang

> "Una gota puede disolver la determinación de un hombre..."

Una daga destilada del veneno corrosivo de una Bruja de Veneno.

| Estadística | Valor |
|---|---|
| Material | IRON_SWORD |
| Pasiva | Cada golpe aplica **Veneno I** (5 s) y **Wither I** (4 s) |
| Partículas / Sonido | ITEM_SLIME, ENTITY_SPIDER_AMBIENT |
| Irrompible | Sí |

- **Give:** `/msc give venomfang` (alias `dagger`)
- **Crafteo:** de **Glándula de Veneno** (dropeada por la Bruja de Veneno) + Bloque de Oro + Molde de Espada + Palo
- **Tema:** Multiverso (cadena de la Bruja de Veneno vía Glándula de Veneno)

---

## 🏹 A distancia

### Aether Pullshot

> "Una correa no de cuerda, sino de distancia negada."

Un tridente forjado con un Fragmento Ender, encordado con una correa de espacio desgastado.

| Estadística | Valor |
|---|---|
| Material | TRIDENT |
| Encantamiento | Lealtad III (siempre vuelve) |
| Activa | **Tirón Aether** — Click derecho sobre entidad |
| Rango | 40 bloques |
| Duración del tirón | 3 s (`PULL_DURATION_TICKS` = 60) |
| Velocidad del tirón | 0.5 (+0.2 Y) |
| Daño inicial | 6 (al golpear) |
| Daño final | 10 (cuando el objetivo llega a 2 bloques de ti) — con partículas EXPLOSION + sonido ENTITY_GENERIC_EXPLODE |
| Cooldown | 30 s |
| Partículas | PORTAL (inicial), EXPLOSION (final) |

- **Give:** `/msc give aetherpullshot` (alias `pullshot`)
- **Tema:** Multiverso (cadena del Caballero Ender vía Fragmento Ender)

---

## 🔮 Herramientas mágicas

### Talismán Skyfire

> "La tormenta responde, incluso cuando el cielo calla."

Un amuleto de cobre que zumba con la ira latente de un Invocador de Tormentas.

| Estadística | Valor |
|---|---|
| Material | COPPER_INGOT |
| Activa | **Golpe Skyfire** — Click derecho sobre bloque |
| Rango del golpe | 50 bloques de distancia |
| Radio del golpe | 3 bloques |
| Daño del golpe | 8 + aturdimiento breve + knockback (0.6 Y) |
| Cooldown | 10 s |
| Pasiva | El portador es **inmune al daño de rayos** mientras lo sostiene |
| Partículas / Sonido | FLASH, ENTITY_LIGHTNING_BOLT_THUNDER |

- **Give:** `/msc give skyfiretalisman` (alias `talisman`)
- **Tema:** Multiverso (cadena del Invocador de Tormentas vía Cristal de Tormenta)

---

### Forja del Caos

> "En el orbe, todas las posibilidades; en la mano, solo una."

Un yunque portátil imbuido de entropía. No puede crear — solo retorcer lo que ya está escrito sobre un objeto.

| Estadística | Valor |
|---|---|
| Material | ANVIL |
| Activa | **Re-forjar** — Click derecho con la Forja en la mano secundaria y el objeto encantado en la mano principal |
| Efecto | Cada encantamiento existente del objetivo sube **+1 nivel** (máx. 30) |
| Restricciones | Solo objetos que ya tengan encantamientos · cada objeto solo puede re-forjarse una vez (marca PDC `msc_chaos_reforged`) · consume **1 Orbe del Caos** del inventario |
| Marca de re-forja | Añade `§4§o⟡ Reforged by Chaos ⟡` al lore del objetivo |
| Partículas / Sonidos | ENCHANT, BLOCK_ENCHANTMENT_TABLE_USE |

- **Give:** `/msc give chaosforge`
- **Tema:** Multiverso (cadena del Mago del Caos vía Orbe del Caos)

---

### Grimorio Centinela

> "Todo universo responde a quien lo lee."

Un tomo prohibido encuadernado con el cuero de un Centinela caído. Contiene 8 páginas de hechizos, cada una con su propio sello original. `Shift + Click derecho` cambia la página (la barra de acción muestra la selección), `Click derecho` lanza el hechizo actual.

| Página | Hechizo | Efecto | Cooldown |
|---|---|---|---|
| 1 | 🔥 Pentagrama Ardiente | Pentagrama de fuego vertical apuntando a tu objetivo · 10 de daño + quema en 3.5 bloques | 8 s |
| 2 | 🗡️ Lluvia de Lanzas | Sello de triángulo rúnico + lluvia de lanzas luminosas · 12 de daño en 3 pulsos | 7 s |
| 3 | ⚡ Juicio Divino | Sello divino + 3 rayos · 18 de daño en total | 10 s |
| 4 | ❌ Marca del Verdugo | X roja de verdugo sobre el objetivo · explota tras 2.5 s (14 de daño + empuje + ceguera) | 10 s |
| 5 | 🌀 Vórtice Singular | Sello de vórtice que atrae a los enemigos · 8 de daño | 15 s |
| 6 | 🌋 Terremoto | Sello sísmico · 10 de daño + lanzamiento al aire | 9 s |
| 7 | 🛡️ Baluarte Celestial | Sello celestial · Absorción (4 corazones) + Resistencia | 20 s |
| 8 | ✨ Aura Centinela | Aura de invulnerabilidad · inmune al daño durante 3.5 s | 45 s |

- **Give:** `/msc give sentinelgrimoire` (alias `grimoire`)
- **Receta:** LIBRO ×4 + Núcleo Multiversal ×2 + Núcleo Centinela (ver Recipes.md)
- **Config:** daños/cooldowns bajo `grimoire:` en config.yml