# 🛠️ Comandos

Todos los comandos usan la raíz **`/msc`**. **Permiso:** `msc.admin` (OP del servidor por defecto). `msc.admin.bypass` (también OP) omite las reglas de construcción y teletransporte de la dimensión protegida del jefe.

```
/msc                           Muestra el menú de ayuda estructurado y categorizado
/msc spawn <tipo>              Invoca un mob, jefe, strike force militar, etc. (/msc spawn help [1-3])
/msc give <objeto> [cantidad] [jugador]  Entrega armas legendarias, armaduras o componentes (/msc give help [1-4])
/msc seal <patrón> [plano]     Renderiza un patrón de sello de partículas
/msc dummy ...                 Invoca / posa / anima dummies y previsualiza ataques
/msc attack <nombre> [rango]   Dispara un ataque o mecánica del ArmorStandBoss (/msc attack help [1-4])
/msc music <play|stop|list|disc>  Reproduce / detiene canciones NBS, obtén un disco de jukebox
/msc dimtp <mundo>             Teletransporta entre mundos
/msc cleanstands [mundo]       Elimina todos los armor stands relacionados con MSC
/msc kill [tipo|all] [radio]   Purga criaturas de MSC de forma segura
/msc debug [jugador]           Desglosa el daño de cada jefe hacia y desde un jugador
/msc reload                    Recarga config.yml y sincroniza entidades y jefes
```

Cada comando se detalla abajo.

Los alias que aceptan `spawn`, `give` y `attack` — y los que ofrece el autocompletado — se declaran en una sola tabla por comando (`SpawnCatalogue`, `GiveCatalogue` y `AttackCatalogue`): la misma tabla alimenta el ejecutor, el menú de ayuda y el tab-complete.

---

## /msc spawn <tipo>

Invoca una sola entidad (o una formación táctica) en la ubicación del ejecutor. Los siguientes tipos son compatibles (alias entre paréntesis):

| Tipo | Entidad |
|---|---|
| `merchant` | Comerciante Multiversal ("Shaggy" Comerciante Errante) |
| `mahoraga` | Minijefe Mahoraga |
| `kinger` | Minijefe Kinger |
| `garou` | Minijefe Garou [Hero Hunter] |
| `nix` (`executioner`, `nixelverdugo`) | NIX - El Verdugo (jefe con modelo custom de 27 piezas) |
| `armorstand` (`armorstandboss`) | EL CENTINELA DE OBSIDIANA, jefe final |
| `jack` | JACK STAR — El Arquitecto del Sistema (5 fases, 3 vidas) |
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
| `warlord` | Orcish Warlord (furia berserker) |
| `disctrader` | Disc Trader — aldeano bibliotecario que vende discos de música |

Los detalles de cada entidad viven en [Jefes](./Bosses.md) y [Criaturas](./Creatures.md).

---

## /msc give <objeto> [cantidad] [jugador|@a|@p|@r|@s]

La cantidad por defecto es 1 y puede ser de 1 a 64. Sin destino el objeto va al ejecutor; si se indica, acepta el nombre exacto de un jugador o un selector (`@e` se rechaza a propósito).

### Armas

| Objeto | Alias |
|---|---|
| `excalibur` | `sword` |
| `cindergreatsword` | `greatsword` |
| `nullshearedge` | `nullshear` |
| `soulreapscythe` | `scythe` |
| `venomfang` | `dagger` |
| `aetherpullshot` | `pullshot` |
| `skyfiretalisman` | `talisman` |
| `sentinelgrimoire` | `grimoire` |
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
| `starcore` | `star` |
| `militarycomponent` | `component` |
| `swordmold` | `mold` |
| `headslimeheart` | `heart` |
| `chaosorb` | — |
| `chaospowder` | — |
| `chaosfragment` | — |
| `chaoscore` | — |
| `condensedchaosorb` | `condensed` |
| `enderfragment` | `ender` |
| `frostheart` | `frost` |
| `magmacore` | `magma` |
| `obsidianshard` | `shard` |
| `reaperessence` | `reaper` |
| `reinforcedbone` | `bone` |
| `reinforcedboneblock` | — |
| `bonemarrow` | `marrow` |
| `ossifiedplate` | `plate` |
| `moltenmarrow` | — |
| `shadowcloak` | `cloak` |
| `stormcrystal` | `storm` |
| `venomgland` | `venom` |
| `voidessence` | `void` |
| `wheelessence` | `whelessence` |

### Catalizadores de jefe y bloques nucleares

| Objeto | Alias |
|---|---|
| `wheelcore` | — |
| `moltenwheelcore` | `moltenwheel` |
| `refinedwheelcore` | `refinedwheel` |
| `reapercore` | — |
| `sentinelcore` | `sentinel` |
| `endercore` | — |
| `multiversalcore` | `multiverse` |
| `compressedgoldblock` | `goldblock` |
| `refinednetherite` | — |
| `moltennetherite` | `molten` |
| `executionerwarrant` | `warrant`, `deathwarrant` |
| `architectkernel` | `kernel`, `architect` |

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
| `attack <ataque\|random>` | Previsualiza un ataque real del Centinela en el dummy |
| `attack list [page]` | Lista cada ataque que el dummy puede hacer |

**Animaciones:** `flyup`, `land`, `airslam`, `shieldseal`, `healingcircle` (`heal`), `rain`, `pentagram`, `trianglecall` (`triangle`)

**Previsualización de ataques:** `attack <ataque|random>` hace que el dummy ejecute un ataque real del Centinela — el mismo objeto de ataque que corre el jefe, con su coreografía, partículas y sellos — así se puede revisar una animación en un servidor de pruebas sin armar una pelea. Nada de lo que golpee el dummy puede dañar a nadie: todos los ataques dañan por un único helper, y ahí se rechaza a un dummy en actuación. Los efectos y el empuje siguen ocurriendo, así que mira desde un paso atrás. `attack list [page]` lista todos los nombres, y el autocompletado también los ofrece.

---

## /msc attack <nombre> [rango]

Dispara un ataque, defensa o mecánica de transición de fase del ArmorStandBoss por nombre. Encuentra el jefe más cercano dentro de `rango` bloques (por defecto `aggro-range` = 50) y lo ejecuta.

### Ataques de suelo (18)

`groundslam`, `groundshatter`, `shieldbash`, `lancestorm`, `earthpillar`, `chaingrapple`, `warstomp`, `armorspikes`, `vortexpull`, `mirrorimage`, `doombeam`, `lanceflurry`, `whirlwindslash`, `executionsweep`, `obsidianspire`, `earthmaw`, `shadowstep`, `runeward`

### Ataques aéreos (16)

`starfall`, `aerialrush`, `sonicboom`, `lightningstorm`, `gravitywell`, `crossslash`, `novaburst`, `darkorb`, `windcutter`, `heavenlyjudgment`, `rainoflances`, `airslam`, `hoverbarrage` (alias `crossbarrage`), `eclipsefall`, `bladering`, `obsidianwings`

### Ataques a distancia (15)

`lancesnipe`, `meteorstorm`, `voidbeam`, `frostlance`, `lightningspear`, `shadowvolley`, `chainlightning`, `crystalbarrage`, `arcaneorb`, `voidrift`, `arcanemissiles`, `spiritbeam`, `soultethers`, `plaguebrand`, `runemines`

### Transiciones de fase

`phaserage`, `phasebarrier`, `phasestorm`, `phasedespair`

### Estados defensivos

`stoneskin`, `reflectbarrier`, `absorbshield`

### Mecánicas y varios

`trianglecall`, `flyup`, `land`, `shieldseal`, `heal`, `reset`

La lista completa y los detalles están en la [página de Jefes](./Bosses.md).

---

## /msc debug [jugador]

Diagnostica el daño de los jefes. Sin argumento apunta al jugador al que estás mirando (hasta 30 bloques); también puedes indicar un nombre, la única forma de usarlo desde la consola.

El informe tiene una sección por jefe (el Obsidian Sentinel, Nix y Jack Star) y lee la **muestra más reciente** de ese jugador en ambos sentidos, así que deja que el jefe le golpee (o golpéale tú) una vez antes:

- **DEALT to player** — el ataque, el daño que pedía (`Intended`) y lo que el jugador recibió de verdad (`Applied`), tras su armadura y efectos.
- **TAKEN from player** — el golpe tal como llegó (`Hit`), la mecánica propia del jefe (`cap N`, el reparto del load balancer, los multiplicadores de defensa, un esquive…) y el daño que el jefe aplicó.

La entrada `DEALT` del Sentinel es el **desglose penetrante** (daño del evento, armadura/Protección/Resistencia devueltas, el cap por golpe y la perforación de Resistencia), porque todos sus golpes pasan por ese pipeline. La entrada `TAKEN` de Nix muestra su cap `max-damage-per-hit`; la de Jack Star, el reparto del Load Balancer.

```
OBSIDIAN SENTINEL
  ▸ DEALT to player · penetrating
      Event : 3.52 · armour : -17.60 · protection : 0.00 · resistance : -0.88
      Through armour : 22.00 · cap : 15.00 · pierce : 20% · Resistance : level 1
      Final damage dealt : 12.60
  ▸ TAKEN from player · Incoming hit
      Hit : 120.00 · stone skin ×0.5, cap 50.0 · Applied : 50.00

NIX - THE EXECUTIONER
  ▸ DEALT to player · Guillotine Cleave
      Intended : 22.00 · Applied : 9.90
  ▸ TAKEN from player · Incoming hit
      Hit : 300.00 · cap 100.0 · Applied : 100.00

JACK STAR - THE SYSTEM ARCHITECT
  ▸ DEALT to player · Three-Slash
      Intended : 16.00 · Applied : 8.40
  ▸ TAKEN from player · Incoming hit
      Hit : 134.00 · load balancer: 46.9 shared · Applied : 87.10
```

Las secciones sin datos se omiten; si aún no te ha tocado ningún jefe el comando lo dice en vez de mostrar un desglose vacío. Las muestras son transitorias: las de un jugador se pierden al desconectarse y las de un jefe al morir o despawnear, así que el informe siempre refleja la sesión actual. Los caps y multiplicadores están en `entities.<jefe>.*` en `config.yml`; mira [Instalación](./Installation.md) y [Jefes](./Bosses.md) para saber qué hace cada opción.

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
| *tus propios nodos* | — | Puertas opcionales por subcomando, declaradas en `commands.subcommand-permissions` |

Aún no hay permisos por objeto o por mob. Los administradores del servidor pueden restringir el comando detrás de un plugin de permisos (p. ej. LuckPerms) dando `msc.admin` solo al personal de confianza.

`commands.subcommand-permissions` asigna a cada subcomando un nodo extra. Un subcomando listado ahí necesita ese nodo **además de** `msc.admin`; lo que no aparezca, o esté en blanco, queda abierto para quien haya pasado la puerta principal, así que el `{}` que se envía no cambia nada:

```yaml
commands:
  permission: "msc.admin"
  op-only: true
  subcommand-permissions:
    debug: msc.debug
    attack: msc.attack
```