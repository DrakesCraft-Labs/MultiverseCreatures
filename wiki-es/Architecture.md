# 🏗️ Arquitectura

Esta página es para desarrolladores que quieran extender MultiverseCreatures. Explica la estructura, los subsistemas principales y las convenciones que debes seguir.

> Si no eres un colaborador, puedes dejar de leer — pero si lo eres, las convenciones de abajo son **obligatorias** para el código nuevo.

---

## Estructura de fuentes

```
src/main/java/com/Chagui68/
├── MultiverseCreatures.java          Punto de entrada del plugin: onEnable/onDisable, registro de recetas + listeners
├── ability/                           Habilidades de jugador (FreezeAbility)
├── commands/                          Ejecutor del comando /msc + tab completer
│   └── MSCCommand.java
├── entities/
│   ├── boss/                          ArmorStandBoss + framework de ataques (EL CENTINELA DE OBSIDIANA)
│   │   ├── ArmorStandBoss.java        Clase del jefe: spawn, fases, escudo, barra, ticker de IA, registro de ataques
│   │   ├── MagicSealListener.java      Renderizado de sellos de partículas (NO es un Listener; lo consume el jefe)
│   │   ├── BossInstance.java           Estructura de estado del jefe por instancia
│   │   └── attack/
│   │       ├── BossAttack.java              Interfaz: execute(BossInstance), getName()
│   │       ├── BossAttackBase.java          Base abstracta: helpers de boss/plugin/random/sealDamage
│   │       ├── aerial/                      13 ataques aéreos (starfall, airslam, ...)
│   │       ├── ground/                      11 ataques de suelo (shieldbash, groundslam, ...)
│   │       └── ranged/                      12 ataques a distancia (meteorstorm, spiritbeam, ...)
│   ├── miniboss/                      DioBoss.java, Mahoraga.java
│   ├── Kinger.java                    ♟️ minijefe pieza de ajedrez (traje de ArmorStand + ItemDisplay)
│   ├── DiscTrader.java                Aldeano bibliotecario que vende discos de música
│   └── handler/
│       └── MobHandler.java            Enrutador de spawns naturales (registrado externamente)
├── items/
│   ├── armor/                         EightHandledWheel, ObsidianBastion
│   ├── components/                    16 ingredientes de crafteo (VoidEssence, MagmaCore, ...)
│   ├── dio/                           DioStandHead
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
└── utils/
    ├── ItemBuilder.java               Builder fluido para ItemStacks (lore, etiquetas PDC, encantamientos)
    └── MscEntityUtils.java            setAttribute, spawnTagged, permanentFireResistance,
                                       isValidTarget, handleDeath — utilidades compartidas de mobs
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

Todas las entidades personalizadas reciben una etiqueta de scoreboard `MSC_<nombre>` (p. ej. `MSC_ObsidianGuard`, `MSC_DioStand`, `MSC_ArmorBossSummoned`). La regla de fuego amigo MSC de Mahoraga y las protecciones de invocación del jefe dependen de estas etiquetas: cualquier evento de daño entre dos entidades etiquetadas `MSC_*` se cancela.

### 6. Manejadores de paquetes

`MantisClawsHandler` registra un manejador de paquetes Netty para interceptar `ServerboundPlayerInputPacket` — necesario para detectar entradas de salto de "flanco ascendente" para el salto de pared. El manejador se inyecta en el canal del jugador al unirse y se elimina al salir. Cualquier mecánica nueva que requiera detección de flanco de entradas brutas debe seguir este patrón.

---

## Añadir contenido nuevo

| Para añadir... | Pasos |
|---|---|
| **Objeto nuevo** | 1. Crea una clase en `items/<categoría>/` usando `ItemBuilder`. Expón `public static final ItemStack` + `NamespacedKey KEY`.<br>2. Registra la receta en `MultiverseCreatures.registerRecipes()`.<br>3. Crea un `listener/XxxHandler implements Listener` para su comportamiento de click derecho/izquierdo/consumo (usa `MscEntityUtils.isCreativeOrSpectator` para los controles de modo de juego).<br>4. Registra el manejador en `MultiverseCreatures.onEnable()`: `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |
| **Mob nuevo** | 1. Crea una clase en `entities/<...>/` que implemente `Listener`. Usa `MscEntityUtils.spawnTagged/setAttribute/handleDeath`.<br>2. Auto-regístrate en el constructor.<br>3. Instánciala una vez en `MultiverseCreatures.onEnable()` para que esté viva y reciba eventos.<br>4. (Opcional) Registra una ruta de reemplazo de spawn dentro de `MobHandler`. |
| **Ataque de jefe nuevo** | 1. Crea una clase que extienda `BossAttackBase` en `entities/boss/attack/<aerial\|ground\|ranged>/` devolviendo un `getName()` único.<br>2. Delega la lógica compartida vía `boss.<helper>()` (la clase base ya expone `boss`, `plugin`, un `Random`, `sealDamage(...)`, etc.).<br>3. Regístrala en `ArmorStandBoss.initAttacks()` con `registerAttack(new XxxAttack(this))`. Sin editar el código de despacho. |
| **Manejador de herramienta/arma nuevo** | 1. Crea `listener/XxxHandler implements Listener`.<br>2. Usa `MscEntityUtils.isCreativeOrSpectator` para los controles de modo de juego.<br>3. Regístralo en `MultiverseCreatures.onEnable()` vía `getServer().getPluginManager().registerEvents(new XxxHandler(this), this)`. |

---

## Estilo de código

- **Sin comentarios en el código** — los nombres y la estructura deben auto-documentarse.
- **Construcción fluida de objetos** — siempre vía `ItemBuilder`, nunca `new ItemStack(...) + ItemMeta` inline.
- **Etiquetas PDC para identificación** — nunca compares por nombre visible.
- **`AttributeModifier` moderno** — solo el constructor `NamespacedKey`.
- **Prefijo MSC** — toda entidad personalizada recibe una etiqueta de scoreboard `MSC_<Nombre>`.

---

## Compilación

```bash
mvn clean package -DskipTests
```

Salida: `target/MultiverseCreatures-v${project.version}.jar`

Dependencias (todas `provided` por Paper/Purpur en tiempo de ejecución):
- `org.purpurmc.purpur:purpur-api:1.21.11-R0.1-SNAPSHOT`
- `org.joml:joml:1.10.5` (3D Display Entities)
- `io.netty:netty-all:4.1.82.Final` (intercepción de paquetes)
- `com.google.code.gson:gson:2.10.1` (análisis de JSON de esquemas)
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
| Habilidad de jugador | `ability/FreezeAbility.java` |
| Intercepción de paquetes | `listener/MantisClawsHandler.java` |
| Motor de música | `music/NBSSong.java`, `music/MusicManager.java`, `music/MusicDisc.java` |
| Discos de jukebox | `listener/misc/DiscJukeboxHandler.java`, `entities/DiscTrader.java` |