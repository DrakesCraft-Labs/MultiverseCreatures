# 🧪 Componentes de Crafteo

El sistema de botín del plugin es intencionadamente **simple y temático**: cada mob dropea un ingrediente único, y esos ingredientes se usan para craftear los objetos legendarios. Los drops ocurren al morir el mob y respetan un `drop-chance` (en `config.yml`) que se tira por cada muerte.

Fuentes de drop — para cada componente, el mob y la probabilidad:

| Componente | Mob de origen | Probabilidad de drop | Etiqueta temática |
|---|---|---|---|
| **Esencia de Rueda** `§f§lWheel Essence` (NETHERITE_SCRAP) | Mahoraga | 75% | Multiverso (JJK) |
| **Orbe del Caos** `§d§lChaos Orb` (NETHER_STAR) | Mago del Caos | 60% | Multiverso |
| **Fragmento Ender** `§3§lEnder Fragment` (ENDER_PEARL) | Caballero Ender | 55% | Multiverso |
| **Corazón de Escarcha** `§b§lFrost Heart` (BLUE_ICE) | Gólem de Escarcha | 75% | Multiverso |
| **Núcleo de Magma** `§6§lMagma Core` (MAGMA_CREAM) | Elemental de Llama | 60% | Multiverso |
| **Cristal de Tormenta** `§e§lStorm Crystal` (QUARTZ) | Invocador de Tormentas | 60% | Multiverso |
| **Glándula de Veneno** `§2§lVenom Gland` (SPIDER_EYE) | Bruja de Veneno | 60% | Multiverso |
| **Esencia del Vacío** `§5§lVoid Essence` (ENDER_EYE) | Void Crawler | 50% | Multiverso |
| **Esencia de Segador** `§0§lReaper Essence` (SOUL_LANTERN) | Segador de Almas | 60% | Multiverso |
| **Hueso Reforzado** `§f§lReinforced Bone` (BONE) | Bone Shield | 80% | Multiverso |
| **Médula de Hueso** `§f§lBone Marrow` (BONE_MEAL) | crafteado de **Hueso Reforzado** + Bloques de Redstone + Verruga del Nether | — | Multiverso |
| **Placa Osificada** `§f§lOssified Plate` (CALCITE) | crafteada de **Médula de Hueso** + Calcita + Diamante | — | Multiverso |
| **Médula Fundida** `§6§lMolten Marrow` (REDSTONE) | **Alto Horno SOLO**: 1 Placa Osificada (100 ticks, 0.5 XP) | — | Multiverso |
| **Fragmento de Capa de las Sombras** `§8§lShadow Cloak Fragment` (BLACK_WOOL) | Shadow Rogue | 50% | Multiverso |
| **Fragmento de Obsidiana** `§8§lObsidian Shard` (OBSIDIAN) | Guardia de Obsidiana | 85% | Multiverso |
| **Corazón de Head Slime** `§a§lHead Slime Heart` (SLIME_BALL) | Head Slime | siempre (100%) | Reino Slime |
| **Componente Militar** `§a§lMilitary Component` (GUNPOWDER) | cada unidad de ZombieHorseTrap | 30% (`zombie-horse-trap.military-component-drop-chance`) | Military |
| **Núcleo Estelar** `§e§lStar Core` (NETHER_STAR) | especial / "de una entidad superior" | — | Multiverso |
| **Núcleo de Rueda** `§6§lWheel Core` (MUSIC_DISC_OTHERSIDE) | crafteado de **Esencia de Rueda** + Bloque de Diamante + Estrella del Nether; **debe fundirse en un Alto Horno** | — | Multiverso (JJK) |
| **Núcleo de Rueda Fundido** `§6§lMolten Wheel Core` (BLAZE_POWDER) | **Alto Horno**: 1 Núcleo de Rueda (100 ticks, 0.5 XP) | — | Multiverso (JJK) |
| **Netherita Fundida** `§8§lMolten Netherite` (ANCIENT_DEBRIS) | **Alto Horno**: 1 Netherita Refinada (100 ticks, 0.5 XP) | — | Multiverso |
| **Núcleo de Rueda Refinado** `§6§lRefined Wheel Core` (MUSIC_DISC_OTHERSIDE) | crafteado de **Núcleo de Rueda Fundido** + **Netherita Fundida** | — | Multiverso (JJK) |
| **Núcleo de Segador** `§0§lReaper Core` (WITHER_ROSE) | crafteado de **Esencia de Segador** + Arena de Almas + Estrella del Nether | — | Multiverso |
| **Netherita Refinada** `§8§lRefined Netherite` (NETHERITE_INGOT) | crafteada de **4 Núcleo Estelar** (esquinas) + **4 Fragmento de Netherita** (lados) + **Bloque de Oro Comprimido** (centro, 9 Bloques de Oro) | — | Multiverso |

Cada componente es solo un ingrediente etiquetado `msc_<nombre>` — **no hace nada por sí solo**, pero es necesario para craftear el objeto legendario correspondiente.

---

## Cadenas de crafteo (botín → objeto)

```
Mahoraga ─┬─► Esencia de Rueda ────────► Rueda de Ocho Manos (casco)
Mago del Caos ─► Orbe del Caos ────────► Forja del Caos (herramienta de re-forja)
Caballero Ender ─► Fragmento Ender ────► Aether Pullshot (tridente)
Gólem de Escarcha ─► Corazón de Escarcha ► Corazón de Escarcha (mano secundaria)
Elemental de Llama ─► Núcleo de Magma ─► Gran Espada de Ascuas
Invocador de Tormentas ─► Cristal de Tormenta ► Talismán Skyfire
Bruja de Veneno ─► Glándula de Veneno ─► Venomfang (daga)
Void Crawler ─► Esencia del Vacío ─────► Filo Nullshear
Segador de Almas ─► Esencia de Segador ► Guadaña Soulreap
Bone Shield ─► Hueso Reforzado ─► [Médula de Hueso ─► Placa Osificada ─► *Alto Horno (solo)* Médula Fundida] ─► Marrow Aegis (escudo)
Shadow Rogue ─► Capa de las Sombras ───► Manto Veilwalker (mano secundaria)
Guardia de Obsidiana ─► Fragmento de Obsidiana ► Bastión de Obsidiana (set de 4 piezas)
Head Slime ─► Corazón de Head Slime ───► Gelatina de Head Slime (alimento)
ZombieHorseTrap ─► Componente Militar ► Mina Militar (TNT camuflado)
Entidad superior ─► Núcleo Estelar ─────► Excalibur (y más allá...)
Mahoraga ─► Esencia de Rueda ─► Núcleo de Rueda ─► [Alto Horno] Núcleo Fundido ─► Núcleo de Rueda Refinado ─► Rueda de Ocho Manos (casco)
Netherita Refinada ─► [Alto Horno] Netherita Fundida ─► (mezclada con Núcleo de Rueda Fundido)
Segador de Almas ─► Esencia de Segador ─► Núcleo de Segador ─► Guadaña Soulreap
Guardia de Obsidiana ─► Fragmento de Obsidiana + Netherita Refinada (4 Núcleo Estelar + 4 Fragmento + Bloque de Oro Comprimido) ─► Bastión de Obsidiana
```

---

## Comando give (solo pruebas)

Puedes darte cualquier componente directamente para probar:

```
/msc give chaosorb
/msc give enderfragment    (alias: ender)
/msc give frostheart        (alias: frost)
/msc give magmacore         (alias: magma)
/msc give obsidianshard     (alias: shard)
/msc give reaperessence     (alias: reaper)
/msc give reinforcedbone    (alias: bone)
/msc give bonemarrow        (alias: marrow)
/msc give ossifiedplate     (alias: plate)
/msc give moltenmarrow
/msc give shadowcloak      (alias: cloak)
/msc give stormcrystal      (alias: storm)
/msc give venomgland        (alias: venom)
/msc give voidessence       (alias: void)
/msc give wheelessence      (alias: whelessence)
/msc give headslimeheart    (alias: heart)
/msc give militarycomponent (alias: component)
/msc give starcore          (alias: star)
/msc give wheelcore
/msc give moltenwheelcore   (alias: moltenwheel)
/msc give moltennetherite   (alias: molten)
/msc give refinedwheelcore  (alias: refinedwheel)
/msc give reapercore
/msc give refinednetherite
```

Ver [Comandos](./Commands.md) para la referencia completa.