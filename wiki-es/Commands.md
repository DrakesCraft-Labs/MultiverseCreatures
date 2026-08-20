# 🛠️ Comandos

Todos los comandos usan la raíz **`/msc`**. **Permiso:** `msc.admin` (OP del servidor por defecto).

```
/msc spawn <tipo>              Invoca un mob, jefe, comerciante, etc.
/msc give <objeto> [cantidad]  Date un objeto (1–64)
/msc seal <patrón> [plano]     Renderiza un patrón de sello de partículas
/msc dummy ...                 Invoca / posa / anima ArmorStands de prueba
/msc attack <nombre> [rango]   Dispara un ataque/mecánica del ArmorStandBoss
/msc music <play|stop|list|disc>  Reproduce / detiene canciones NBS, obtén un disco de jukebox
/msc dimtp <mundo>             Teletransporta entre mundos
/msc cleanstands               Elimina todos los armor stands relacionados con MSC
```

Cada comando se detalla abajo.

---

## /msc spawn <tipo>

Invoca una sola entidad (o una formación táctica) en la ubicación del ejecutor. Los siguientes tipos son compatibles (alias entre paréntesis):

| Tipo | Entidad |
|---|---|
| `merchant` | Comerciante Multiversal ("Shaggy" Comerciante Errante) |
| `mahoraga` | Minijefe Mahoraga |
| `kinger` | Minijefe Kinger |
| `armorstand` (`armorstandboss`) | EL CENTINELA DE OBSIDIANA, jefe final |
| `creeperjr` | Creeper Jr. (×3 — aparece en trío) |
| `headslime` | Head Slime |
| `zombietrap` (`army`) | Trampa de Caballo Zombie Militar (emboscada de ejército completo de 5 unidades) |
| `tank` | Zombie Tank (unidad única) |
| `duelist` | Skeleton Duelist militar |
| `lancer` | Zombie Lancer + ZombieHorse |
| `camel` | Camel del Ejército con jinetes |
| `sniper` | Skeleton Sniper |
| `boneshield` (`bone`) | Bone Shield |
| `chaosmage` (`chaos`) | Mago del Caos |
| `enderknight` (`ender`) | Caballero Ender |
| `flameelemental` (`flame`) | Elemental de Llama |
| `frostgolem` (`frost`) | Gólem de Escarcha |
| `obsidianguard` (`obsidian`) | Guardia de Obsidiana |
| `shadowrogue` (`rogue`) | Shadow Rogue |
| `soulreaper` (`reaper`) | Segador de Almas |
| `stormcaller` (`storm`) | Invocador de Tormentas |
| `venomwitch` (`venom`) | Bruja de Veneno |
| `voidcrawler` (`void`) | Void Crawler |
| `disctrader` | Disc Trader — aldeano bibliotecario que vende discos de música |

Los detalles de cada entidad viven en [Jefes](./Bosses.md) y [Criaturas](./Creatures.md).

---

## /msc give <objeto> [cantidad]

La cantidad por defecto es 1 y puede ser de 1 a 64.

### Armas

| Objeto | Alias |
|---|---|
| `excalibur` | `sword` |
| `cindergreatsword` | `greatsword` |
| `nullshearedge` | `nullshear` |
| `soulreapscythe` | `scythe` |
| `aetherpullshot` | `pullshot` |
| `skyfiretalisman` | `talisman` |
| `chaosforge` | — |

### Armaduras y reliquias

| Objeto | Alias |
|---|---|
| `eighthandledwheel` | `wheel` |
| `obsidianbastionhelmet` | `bastionhelmet` |
| `obsidianbastionchestplate` | `bastionchestplate` |
| `obsidianbastionleggings` | `bastionleggings` |
| `obsidianbastionboots` | `bastionboots` |
| `marrowaegis` | `aegis` |
| `veilwalkermantle` | `mantle` |
| `frostheartoffhand` | `frostoffhand` |

### Objetos varios

| Objeto | Alias |
|---|---|
| `icecrown` | `crown` |
| `mantisclaws` | `claws` |
| `wirtslantern` | `lantern` |
| `militarymine` | `mine` |
| `scoobycookie` | `cookie` |
| `headslimegelatin` | `gelatin` |

### Componentes

| Objeto | Alias |
|---|---|
| `scoobycookie` (arriba) | `cookie` |
| `starcore` | `star` |
| `militarycomponent` | `component` |
| `headslimeheart` | `heart` |
| `chaosorb` | — |
| `enderfragment` | `ender` |
| `frostheart` | `frost` |
| `magmacore` | `magma` |
| `obsidianshard` | `shard` |
| `reaperessence` | `reaper` |
| `reinforcedbone` | `bone` |
| `bonemarrow` | `marrow` |
| `ossifiedplate` | `plate` |
| `moltenmarrow` | — |
| `shadowcloak` | `cloak` |
| `stormcrystal` | `storm` |
| `venomgland` | `venom` |
| `voidessence` | `void` |
| `wheelessence` | `whelessence` |

> Nota: algunos alias se solapan (`bone` = componente Hueso Reforzado, pero `bone` **también** es el alias de spawn de Bone Shield). El contexto (spawn vs give) los desambigua.

---

## /msc seal <patrón> [plano]

Renderiza un patrón de sello de partículas alrededor del ejecutor. El motor de lanzamiento de hechizos falso usado por el jefe Centinela de Obsidiana; se ofrece como juguete creativo para administradores de servidor.

**Patrones:**

```
pentagram   triangle / runic   celestial   circle   ring
star        floating / shield  wings       wings2
vortex      quake              divine      storm
```

**Planos** (opcional):

- `horizontal` (`h` / `xz`) — por defecto, dibujado en el plano del suelo
- `vertical-north` (`vertical` / `v` / `xy`) — dibujado en el plano X-Y (mirando al norte)
- `vertical-east` (`ez` / `yz`) — dibujado en el plano Y-Z (mirando al este)

---

## /msc dummy ...

Manipula un ArmorStand de prueba usado para posar/vista previa de contenido. Útil para diseñar animaciones del jefe sin ejecutar la pelea completa del boss.

| Subcomando | Comportamiento |
|---|---|
| `spawn` | Invoca un dummy nuevo en tu ubicación |
| `remove` | Elimina el dummy |
| `set <parte> <x> <y> <z>` | Establece la pose de una parte del cuerpo |
| `<parte> <eje> <grados>` | Rota una parte del cuerpo sobre un eje |

**Partes:** `rightarm`, `leftarm`, `body`, `head`, `rightleg`, `leftleg`
**Ejes:** `x` / `pitch`, `y` / `yaw`, `z` / `roll`

| Subcomando | Comportamiento |
|---|---|
| `wings` / `wings2` / `nowings` | Alterna presets de poses de alas |
| `animate <anim>` | Reproduce una animación preset con nombre |

**Animaciones:** `flyup`, `land`, `airslam`, `shieldseal`, `healingcircle` (`heal`), `rain`, `pentagram`, `trianglecall` (`triangle`)

---

## /msc attack <nombre> [rango]

Dispara un ataque, defensa o mecánica de transición de fase del ArmorStandBoss por nombre. Encuentra el jefe más cercano dentro de `rango` bloques (por defecto `aggro-range` = 50) y lo ejecuta.

### Ataques de suelo (11)

`groundslam`, `groundshatter`, `shieldbash`, `lancestorm`, `earthpillar`, `chaingrapple`, `warstomp`, `armorspikes`, `vortexpull`, `mirrorimage`, `doombeam`

### Ataques aéreos (13)

`starfall`, `aerialrush`, `sonicboom`, `lightningstorm`, `gravitywell`, `crossslash`, `novaburst`, `darkorb`, `windcutter`, `heavenlyjudgment`, `rainoflances`, `airslam`, `hoverbarrage` (alias `crossbarrage`)

### Ataques a distancia (12)

`lancesnipe`, `meteorstorm`, `voidbeam`, `frostlance`, `lightningspear`, `shadowvolley`, `chainlightning`, `crystalbarrage`, `arcaneorb`, `voidrift`, `arcanemissiles`, `spiritbeam`

### Transiciones de fase

`phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`

### Estados defensivos

`stoneskin`, `reflectbarrier`, `absorbshield`

### Mecánicas y varios

`trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`

La lista completa y los detalles están en la [página de Jefes](./Bosses.md).

---

## /msc music <play|stop|list|disc> [canción] [loop]

Reproduce cualquier archivo `.nbs` de `plugins/MultiverseCreatures/music/`. Las canciones se reproducen vía el `MusicManager` (paquetes de protocolo note-block-stub) para todos los jugadores cercanos dentro de un radio configurable.

```
/msc music list                Lista todas las canciones de la carpeta de música
/msc music play Undertale-Megalovania true   Reproduce (loop=true)
/msc music stop                Detiene la canción actual
/msc music disc Megalovania    Date el disco de jukebox de una canción
```

`/msc music disc <canción>` te da el disco de jukebox correspondiente — insértalo en un jukebox para reproducir la canción, click derecho con la mano vacía para expulsarlo. Ver [Música](./Music.md) para las canciones incluidas, créditos y el Disc Trader.

---

## /msc dimtp <mundo>

Teletransporta al ejecutor entre mundos/dimensiones. Se usa para probar el andamiaje de la dimensión del jefe y para saltar rápidamente entre overworld/nether/the_end.

---

## /msc cleanstands

Itera todos los mundos y elimina cada ArmorStand cuya etiqueta de scoreboard empiece por `MSC_`. Útil para limpiar después de una pelea de jefe o un crash durante una batalla. **Limpia los compañeros Stand del jefe, los ItemDisplays invocados y las plantillas de jefe aéreo muertas u obsoletas.**

---

## Permisos

| Permiso | Por defecto | Descripción |
|---|---|---|
| `msc.admin` | Solo OP | Requerido para TODOS los subcomandos de `/msc` |

Aún no hay permisos por objeto o por mob. Los administradores del servidor pueden restringir el comando detrás de un plugin de permisos (p. ej. LuckPerms) dando `msc.admin` solo al personal de confianza.