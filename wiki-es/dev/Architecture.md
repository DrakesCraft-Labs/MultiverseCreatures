# 🏗️ Arquitectura

Esta página es para desarrolladores que quieran extender MultiverseCreatures. Explica la estructura, los subsistemas principales y las convenciones que debes seguir.

> Si no eres un colaborador, puedes dejar de leer — pero si lo eres, las convenciones de abajo son **obligatorias** para el código nuevo.

---

## Estructura de fuentes

```
src/main/java/com/Chagui68/
├── MultiverseCreatures.java          Punto de entrada del plugin: onEnable/onDisable, registro de recetas + listeners
├── commands/                          Ejecutor del comando /msc + tab completer
│   ├── MSCCommand.java                 Dispatcher: permiso, enrutado de subcomandos, autocompletado
│   ├── CommandMenu.java                Todos los menús renderizados + la aritmética de paginación
│   ├── SpawnCatalogue.java             Entidades spawneables: alias, mensajes, páginas de ayuda
│   ├── GiveCatalogue.java              Ítems entregables: alias, fábricas de ítem, páginas de ayuda
│   ├── AttackCatalogue.java            Nombres de ataques + páginas de ayuda
│   ├── DummyStudio.java                Subsistema del muñeco de poses (spawn, poses, alas, animaciones, previsualización de ataques)
│   ├── SealStudio.java                 Patrones de sellos de partículas, planos y formas dibujadas a mano
│   └── MscKillFilter.java              Predicados puros de "¿esto es nuestro?" para /msc kill
├── entities/
│   ├── boss/                          ArmorStandBoss + framework de ataques (EL CENTINELA DE OBSIDIANA)
│   │   ├── ArmorStandBoss.java        Clase del jefe: spawn, fases, escudo, barra, ticker de IA, registro de ataques
│   │   ├── MagicSealListener.java      Renderizado de sellos de partículas: recorre las formas de abajo y las pinta
│   │   ├── seal/                       Geometría pura de sellos, testeada sin servidor
│   │   │   ├── SealPlane.java               Dónde cae una forma plana: XZ se acuesta, XY/YZ se levantan
│   │   │   ├── SealGeometry.java            Círculos, pentagramas, estrellas, radios y sus densidades
│   │   │   ├── WingGeometry.java            Los dos perfiles de ala según pose y fotograma
│   │   │   └── SealPoint.java / WingPoint.java  Puntos de plano y de mundo
│   │   ├── BossInstance.java           Estructura de estado del jefe por instancia
│   │   ├── AttackPreview.java          Marca que deja al dummy actuar ataques sin dañar
│   │   └── attack/
│   │       ├── BossAttack.java              Interfaz: execute(BossInstance), getName()
│   │       ├── BossAttackBase.java          Base abstracta: helpers de boss/plugin/random/sealDamage
│   │       ├── aerial/                      13 ataques aéreos (starfall, airslam, ...)
│   │       ├── ground/                      11 ataques de suelo (shieldbash, groundslam, ...)
│   │       └── ranged/                      12 ataques a distancia (meteorstorm, spiritbeam, ...)
│   ├── miniboss/                      Mahoraga.java
│   ├── Kinger.java                    ♟️ minijefe pieza de ajedrez (traje de ArmorStand + ItemDisplay)
│   ├── KingerModel.java               Geometría de las piezas de Kinger: pivotes, segunda articulación, ciclo de caminar
│   ├── DiscTrader.java                Aldeano bibliotecario que vende discos de música
│   └── handler/
│       └── MobHandler.java            Enrutador de spawns naturales (registrado externamente)
├── items/
│   ├── armor/                         EightHandledWheel, ObsidianBastion
│   ├── components/                    16 ingredientes de crafteo (VoidEssence, MagmaCore, ...)
│   ├── food/                          HeadSlimeGelatin, ScoobyCookie
│   ├── misc/
│   │   ├── IceCrown, MantisClaws, MilitaryMine, WirtsLantern
│   │   └── offhand/                   FrostHeartOffhand, MarrowAegis, VeilwalkerMantle
│   └── weapons/
│       ├── magic/                     ChaosForge, SkyfireTalisman
│       ├── melee/                     CinderGreatsword, Excalibur, NullshearEdge, SoulreapScythe
│       └── ranged/                    AetherPullshot
├── listener/                          Manejadores de eventos de Bukkit (uno por sistema de objeto/jefe/reliquia)
├── music/                             Reproducción de canciones NBS: NBSSong, MusicManager, MusicDisc,
│                                      DiscJukeboxHandler (discos de jukebox)
├── ritual/                             Estructuras de ritual & dimensión privada del jefe
│   ├── BossDimensionManager.java       Crea, configura y descarga boss_dimension
│   ├── BossDimensionSky.java           Cielo y niebla rojos (override de efectos del bioma)
│   ├── RitualStructure.java            Ritual de entrada del overworld (7×7) y sus velas
│   ├── BossInvocationStructure.java    Círculo de invocación del Centinela (5×5, velas rojas)
│   ├── NixInvocationStructure.java     Andamio de NIX (5×5, yunque y horcas)
│   ├── JackInvocationStructure.java    Terminal de JACKSTAR (5×5, núcleo y pararrayos)
│   └── terrain/                        Generación de terreno de boss_dimension
│       ├── BossArenaGenerator.java     ChunkGenerator: etapas, sumidero de chunk, bioma, spawn
│       ├── ArenaShape.java             Forma pura: arena, terrazas, páramo, cañones
│       ├── ArenaNoise.java             Hash y campos de ruido deterministas
│       ├── ArenaPalette.java           Estratos, pavimento luminoso, set de materiales de la plaza
│       ├── ArenaLandmarks.java         Monolitos, arcos, portales, pozos, agujas, esquirlas
│       └── TerrainSink.java            Destino de escritura: coordenadas de mundo, filtrado por chunk
└── utils/
    ├── ItemBuilder.java               Builder fluido para ItemStacks (lore, etiquetas PDC, encantamientos)
    ├── MscEntityUtils.java            setAttribute, spawnTagged, permanentFireResistance,
    │                                  isValidTarget, handleDeath — utilidades compartidas de mobs
    ├── MscLimb.java                   Cinemática de extremidades de dos segmentos (swing, fold, knee, elbow)
    ├── DisplaySuit.java               El traje de ItemDisplay que visten los jefes
    ├── MscBossBar.java                Contabilidad de barras de jefe: quién ve cuál y cuándo se va
    ├── MscLeftovers.java              Barrido de arranque de props de ataque dejados en el mundo
    ├── MscGeometryOverlay.java        Dibuja las articulaciones de un modelo o reproduce su caminar (/msc debug geometry [walk])
    └── MscText/MscLog/MscWorldPolicy   Texto, fallos reportados, lista de mundos permitidos

src/main/resources/
├── plugin.yml                         comando, nodos de permiso y línea de usage
└── config.yml                         cada ajuste que lee el código (contrato verificado por ConfigFilesGuardTest)
```

---

## Convenciones arquitectónicas

### 1. Ataques del jefe — registro polimórfico

Cada ataque del CENTINELA DE OBSIDIANA vive como su propia clase que extiende `BossAttackBase`, ubicada en `entities/boss/attack/{aerial,ground,ranged}/`. Se registran en `ArmorStandBoss.initAttacks()` y se despachan polimórficamente vía:

```java
attackRegistry.get(name).execute(instance);
```

Hay tres selectores aleatorios (`executeRandomAerialAttack`, `executeRandomGroundAttack`, `executeRangedAttack`) y el switch de `/msc attack <nombre>` — todos despachan a través del mismo registro. **Añadir un ataque nuevo pasa a ser "crear clase + `registerAttack(new XxxAttack(this))`"** — sin editar el código de despacho.

### 2. Mobs — patrón Listener auto-registrado

Cada clase de mob personalizada implementa `Listener` y se auto-registra en su propio constructor:

```java
public XxxMob(MultiverseCreatures plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin);
    // ... setup de spawn ...
}
```

**`MobHandler` es la excepción** — lo registra externamente `MultiverseCreatures.onEnable()` porque enruta `CreatureSpawnEvent`s naturales a la clase de mob correcta.

### 3. Objetos — ItemBuilder fluido

Toda la construcción de `ItemStack` pasa por `utils/ItemBuilder` (API fluida):

```java
public static final ItemStack ITEM = ItemBuilder.of(Material.NETHERITE_SWORD)
        .name("§6Excalibur")
        .lore("§7The legendary blade of kings,",
              "§7forged from a fallen star's heart.")
        .tagged(KEY)              // adjunta la etiqueta PDC msc_<item>
        .unbreakable()
        .customModelData(1003)
        .build();
public static final NamespacedKey KEY =
        new NamespacedKey("multiversecreatures", "msc_excalibur_sword");
```

Las etiquetas de datos persistentes usan `PersistentDataType.INTEGER` con un `NamespacedKey("multiversecreatures", "msc_<item>")` por objeto — **nunca** compares objetos por nombre visible; comprueba siempre la clave PDC.

### 4. Modificadores de atributos — constructor moderno con namespace

Usa el constructor moderno `AttributeModifier(NamespacedKey, double, Operation)` — **NO** el obsoleto basado en UUID. `ObsidianBastionHandler` es la implementación de referencia para bonos de set de armadura:

- Comprobación idempotente de `getAttribute(key)` antes de añadir un modificador (`getModifier(key) == null`)
- `getAttribute(key).removeModifier(key)` al limpiar

Este patrón significa que **no necesitas mapas de modificadores por jugador** — la API de atributos gestiona la re-aplicación por ti.

### 5. Etiquetado MSC & fuego amigo

Todas las entidades personalizadas reciben una etiqueta de scoreboard `MSC_<nombre>` (p. ej. `MSC_ObsidianGuard`, `MSC_ArmorBossSummoned`). La regla de fuego amigo MSC de Mahoraga y las protecciones de invocación del jefe dependen de estas etiquetas: cualquier evento de daño entre dos entidades etiquetadas `MSC_*` se cancela.

### 6. Manejadores de paquetes

`MantisClawsHandler` registra un manejador de paquetes Netty para interceptar `ServerboundPlayerInputPacket` — necesario para detectar entradas de salto de "flanco ascendente" para el salto de pared. El manejador se inyecta en el canal del jugador al unirse y se elimina al salir. Cualquier mecánica nueva que requiera detección de flanco de entradas brutas debe seguir este patrón.

### 7. El terreno de la dimensión del jefe

La dimensión del ritual la genera `ritual/terrain/`, y su forma es una función pura de las coordenadas del mundo y la semilla. No es una decisión de estilo, es lo que hace que el mundo funcione:

- **Ningún chunk puede mirar a otro.** `ArenaShape`/`ArenaNoise` responden "cuánto mide esta columna" solo con las coordenadas, así que el borde de un chunk siempre encaja con el de su vecino, en cualquier orden de generación. Nunca leas el `Random` que el servidor pasa a `generateNoise`: cambia entre pasadas y deja costuras.
- **La forma se testea, el pegamento no.** Todo lo que la pelea necesita — el suelo plano en y=5, el volumen de juego vacío, las terrazas que nadie puede escalar, los cañones quedándose lejos, el sellado de bedrock — es una función pura comprobada por `ArenaShapeTest` / `BossArenaTerrainTest`. La única parte sin test es `BossArenaGenerator.ChunkSink`, y es delgada a propósito: escribe coordenadas de mundo en el chunk actual y descarta todo lo demás.
- **La plaza es un contrato, no un adorno.** `ArenaPalette.PLAZA_PAVEMENT` tiene que seguir siendo un subconjunto de lo que aceptan `JackInvocationStructure.isValidBase` y `NixInvocationStructure.isValidBase`, o los jugadores no podrán montar un ritual en el spawn. El test de terreno cruza ambas listas, así que cambiar cualquiera de las dos rompe el build.
- **Nada se alza dentro de la arena.** Todo el espectáculo está en el pavimento y al otro lado de la muralla; las consultas de suelo de los jefes, la colocación del sello y el respaldo de recuperación de terreno se ajustaron sobre una llanura plana y vacía.
- **Las ruinas también son por columna.** `ArenaLandmarks` decide desde un índice de celda, así que una ruina a caballo entre dos chunks la dibujan ambos sin saber nada el uno del otro, y el sumidero descarta la mitad que cae fuera.

### 8. Tareas programadas — un handle, o un cancel propio

Toda tarea repetida tiene una de dos formas, y `SchedulerHandleGuardTest` rompe el build ante una tercera:

- **Un bucle de vida larga conserva su handle.** Un ticker que vive con el plugin (uno por mob propio, uno por aura de objeto, el recuento de población) se guarda en un campo `BukkitTask` y lo cancela `stopTasks()` desde `onDisable`. Arrancarlo dos veces cancela antes la tarea anterior: dos bucles recorriendo el mismo `Map` es el fallo que esta forma existe para hacer imposible.
- **Un efecto corto se autocancela o se entrega.** Un ataque o una secuencia de sellos llama a `cancel()` dentro de su propio runnable cuando termina su temporizador, o devuelve el `BukkitRunnable` para que el jefe pueda cancelarlo antes (`BossInstance.flyTask`, `…shieldSealTask`). La guardia lee el handle a través de su declaración, incluido `instance.campo = new BukkitRunnable()` declarado en otra clase.
- **Nunca un `new BukkitRunnable() { … }.runTaskTimer(…)` desnudo.** Bukkit cancela las tareas del plugin al desactivarlo, así que ninguna sobrevive para siempre; el problema es que el plugin ya no puede pararla — un reload o un segundo `startTicker()` dejarían dos bucles detrás, que es como aparecieron veinticuatro.

### 9. Sellos mágicos — una forma pura, un pincel fino

`MagicSealListener` pinta partículas; `entities/boss/seal/` decide dónde van. La división es estructural, y `SealOrchestrationGuardTest` la sostiene:

- **La geometría es pura.** `SealPlane`, `SealPoint`, `SealGeometry` y `WingGeometry` nunca importan un tipo del servidor, así que un círculo, un pentagrama o un aleteo se prueban con números. Una forma tiene un invariante — longitud de cuerda, banda de radio, simetría especular — y los tests de sellos comprueban el invariante en vez de una captura.
- **El listener recorre, no calcula.** Un único helper `repeat(...)` agenda todos los sellos y el archivo no hace trigonometría: añadir un sello es elegir una forma de `SealGeometry` y un color. La guardia falla ante un segundo punto de agenda o un `Math.cos` suelto.
- **Las densidades viven con la forma.** Los conteos de muestras y las escalas (`pentagramSamples`, `celestialRadii`, el 1.3× vertical) son geometría, así un sello no puede perder en silencio el suelo que mantiene legible una estrella pequeña. Tres helpers que ningún sello llamaba (`drawOuterRing`, `baseY`, `spawnFlameAura`) y una cuerda duplicada del pentagrama desaparecieron con la división.

---

## Añadir contenido nuevo

| Para añadir... | Pasos |
|---|---|
| **Objeto nuevo** | 1. Crea una clase en `items/<categoría>/` usando `ItemBuilder`. Expón `public static final ItemStack` + `NamespacedKey KEY`.<br>2. Registra la receta en `MultiverseCreatures.registerRecipes()`.<br>3. Crea un `listener/XxxHandler implements Listener` para su comportamiento de click derecho/izquierdo/consumo (usa `MscEntityUtils.isCreativeOrSpectator` para los controles de modo de juego).<br>4. Registra el manejador en `MultiverseCreatures.onEnable()`: `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **Mob nuevo** | 1. Crea una clase en `entities/<...>/` que implemente `Listener`. Usa `MscEntityUtils.spawnTagged/setAttribute/handleDeath`.<br>2. Auto-regístrate en el constructor.<br>3. Instánciala una vez en `MultiverseCreatures.onEnable()` para que esté viva y reciba eventos.<br>4. (Opcional) Registra una ruta de reemplazo de spawn dentro de `MobHandler`. |
| **Ataque de jefe nuevo** | 1. Crea una clase que extienda `BossAttackBase` en `entities/boss/attack/<aerial\|ground\|ranged>/` devolviendo un `getName()` único.<br>2. Delega la lógica compartida vía `boss.<helper>()` (la clase base ya expone `boss`, `plugin`, un `Random`, `sealDamage(...)`, etc.).<br>3. Regístrala en `ArmorStandBoss.initAttacks()` con `registerAttack(new XxxAttack(this))`. Sin editar el código de despacho. |
| **Manejador de herramienta/arma nuevo** | 1. Crea `listener/XxxHandler implements Listener`.<br>2. Usa `MscEntityUtils.isCreativeOrSpectator` para los controles de modo de juego.<br>3. Regístralo en `MultiverseCreatures.onEnable()` vía `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **Alias, objeto o ataque nuevo de `/msc`** | 1. Añade una entrada en `commands/SpawnCatalogue`, `commands/GiveCatalogue` o `commands/AttackCatalogue`: el ejecutor, las páginas de ayuda y el autocompletado leen esa única tabla.<br>2. Un ataque igual necesita su clase registrada en `ArmorStandBoss.initAttacks()`, su `getName()` copiado en la entrada del catálogo y el nombre añadido a los sets aéreo/suelo que gobiernan cuándo puede dispararse. |
| **Opción de configuración nueva** | 1. Léela con valor por defecto (`config.getInt("ruta.a.clave", fallback)`) para que las configs existentes sigan funcionando.<br>2. Declárala en `config.yml`: `ConfigFilesGuardTest` falla el build cuando el código lee una clave que no está en el archivo. |

---

## Estilo de código

- **Los comentarios explican el *porqué*, nunca el *qué*** — los nombres y la estructura deben auto-documentarse; un comentario se gana su lugar cuando registra una restricción, una rareza heredada o un orden no obvio (`SentinelPhase` y los catálogos de `/msc` documentan sus datos así).
- **Construcción fluida de objetos** — siempre vía `ItemBuilder`, nunca `new ItemStack(...) + ItemMeta` inline.
- **Etiquetas PDC para identificación** — nunca compares por nombre visible.
- **`AttributeModifier` moderno** — solo el constructor `NamespacedKey`.
- **Prefijo MSC** — toda entidad personalizada recibe una etiqueta de scoreboard `MSC_<Nombre>`.

---

## Compilación

```bash
mvn verify                     # compila + tests + jar sombreado (lo que corre CI)
mvn clean package -DskipTests  # solo compilar, sin tests
```

Salida: `target/MultiverseCreatures-v${project.version}.jar` (el jar del plugin sombreado).

Dependencias (todas `provided` por Paper/Purpur en tiempo de ejecución salvo Gson, que va sombreado):
- `org.purpurmc.purpur:purpur-api:1.21.11-R0.1-SNAPSHOT`
- `org.joml:joml:1.10.9` (3D Display Entities)
- `io.netty:netty-transport:4.2.18.Final` (intercepción de paquetes)
- `com.google.code.gson:gson:2.14.0` (análisis de JSON de esquemas, sombreado como `com/Chagui68/libs/gson`)
- `com.google.code.findbugs:jsr305:3.0.2`
- `org.jetbrains:annotations:24.0.1`

---

## Dónde buscar ejemplos

| Patrón | Archivo de referencia |
|---|---|
| Clase de ataque del jefe | `entities/boss/attack/ground/GroundSlamAttack.java` |
| Objeto + manejador | `items/weapons/melee/Excalibur.java` + `listener/ItemCombatHandler.java` |
| Bono de set de armadura | `items/armor/ObsidianBastion.java` + `listener/ObsidianBastionHandler.java` |
| Enrutado de spawn de mobs | `entities/handler/MobHandler.java` |
| Intercepción de paquetes | `listener/MantisClawsHandler.java` |
| Motor de música | `music/NBSSong.java`, `music/MusicManager.java`, `music/MusicDisc.java` |
| Discos de jukebox | `listener/misc/DiscJukeboxHandler.java`, `entities/DiscTrader.java` |