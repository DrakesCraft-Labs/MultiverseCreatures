# 🏹 La Flecha y los Stands (JoJo's Bizarre Adventure)

La Flecha elige a quién darle un Stand… y a quién matar. La sangre de DIO te vuelve portador de
Stand: la Flecha ya no puede matarte y siempre despierta tu Stand.

```
Archer of the Arrow ──(2% de sus disparos)──► la Flecha
DIO ──► Vampire Blood ──► Unstable Blood ──► Bearer's Elixir ──► vampiro + portador de Stand
portador ──► la Flecha siempre despierta un Stand
sin Elixir ──► 70% muerte (marcado indigno) / 30% despierta un Stand
```

---

## 💀 Arquero de la Flecha

Un esqueleto con corona de oro y abrigo morado que reemplaza esqueletos naturales.

| Campo | Valor (config `entities.arrow-skeleton`) |
|---|---|
| Aparición | `spawn-chance` 0.04 de los esqueletos naturales (× `spawn-rate-multiplier`) |
| Vida / velocidad | `health` 50 · `speed` 0.28 |
| Arco | Poder III; la corona y el abrigo no caen |
| La Flecha | `stand-arrow-chance` **0.02**: 2 de cada 100 disparos |
| Muerte | `death-chance` **0.7** |
| Spawn manual | `/msc spawn arrowskeleton` (alias `archer`, `standarrow`) |

La Flecha brilla en dorado, deja una estela, suena una campana al dispararla y no se puede
recoger. Al atravesar a un jugador:

| Resultado | Cuándo | Qué pasa |
|---|---|---|
| **Stand** | Bebió el Bearer's Elixir (portador) | Nunca muere: siempre despierta un Stand al azar según su rareza |
| **Muerte** | 70%, si no es portador | Muere sin importar armadura ni tótem; mensaje de muerte propio (3 variantes en `arrow-death-messages`). Queda marcado como **indigno** |
| **Stand** | 30%, si no es portador | Sobrevive y despierta un Stand al azar según su rareza |
| **Rechazado** | Indigno y no portador | La Flecha lo atraviesa sin efecto: no tendrá Stand hasta beber el Bearer's Elixir, que borra la marca |
| **Resonancia** | Ya tiene un Stand | La Flecha se desvanece; ni lo mata ni le da otro |

---

## 🩸 DIO y la Vampire Blood

DIO suelta **1–2 Vampire Blood** al morir (`entities.dio-brando.vampire-blood-min/max`). Es el
único componente del plugin que acepta el soporte de pociones:

| Paso | Base | Ingrediente | Resultado |
|---|---|---|---|
| 1 | Poción Rara | **Vampire Blood** | Unstable Blood |
| 2 | Unstable Blood | Rosa del Wither | **Bearer's Elixir** |

Las dos pociones no tienen tipo vanilla, así que ninguna receta vanilla las puede transformar.

### Beber el Bearer's Elixir

- Te conviertes en **portador de Stand**: si la Flecha te elige, tu Stand despierta.
- Te conviertes en **vampiro** (se guarda en el jugador, sobrevive a reinicios y muertes):
  - ☀ **El sol te quema**: de día y bajo el cielo abierto recibes `vampire.sun-damage` (2) de
    **daño verdadero** cada segundo. Va directo a la vida: ignora armadura, Resistencia,
    encantamientos, corazones de absorción y tótems. Un techo te protege; la lluvia también
    (`rain-protects`).
  - Si el sol te mata aparece una muerte propia, una de estas tres (`vampire.sun-death-messages`):
    - *%player% se convirtió en cenizas bajo el sol*
    - *%player% olvidó que el amanecer no perdona a los hijos de la noche*
    - *%player% quiso desafiar al sol como DIO... y el sol ganó*
  - 🌙 **La noche es tuya**: Fuerza I y Velocidad I de noche, visión nocturna siempre y robo de
    vida del 15% en los golpes cuerpo a cuerpo (`vampire.lifesteal`).

---

## 「」 Los Stands

| Stand | Rareza | Habilidades |
|---|---|---|
| Hermit Purple | 30 | **F**: Fotografía espiritual — hace brillar a todo ser en 48 bloques y señala al jugador más cercano |
| Magician's Red | 25 | **F**: Crossfire Hurricane — abanico de 3 cruces de fuego (6 de daño, sin incendiar bloques) |
| Crazy Diamond | 18 | **F**: Restauración — cura 8 de vida al jugador que miras (o a ti) y repara la mitad de lo que sostiene |
| Killer Queen | 14 | **Pasiva**: Primera Bomba · **F o `/stand sha`**: Sheer Heart Attack |
| Star Platinum | 10 | **F**: ORA ORA (ráfaga) · **Agachado + clic izquierdo**: detiene el tiempo 1,5 s |
| The World | 3 | **F**: MUDA MUDA (ráfaga) · **Agachado + clic izquierdo**: ZA WARUDO, 3 s |

- **Agachado + F** (cambiar de mano) invoca o retira el Stand. Flota tras tu hombro derecho con
  un aura de su color y da un paso al frente para las ráfagas.
- La rareza, los daños, radios y enfriamientos están en `stands.<stand>` de config.yml.
- Al **detener el tiempo** se congelan jugadores, mobs y proyectiles del radio. Los jefes y los
  usuarios de Star Platinum o The World siguen moviéndose.

### 💣 Killer Queen

- **Primera Bomba (pasiva)**: cada jugador que golpeas recibe además una explosión
  (`explosion-damage` 4, una por `explosion-cooldown-ms` 1500). `include-mobs: true` también hace
  estallar a los mobs; `require-summoned: true` exige tener el Stand invocado.
- **Sheer Heart Attack** (`/stand sha`, o F con Killer Queen invocado): se abre un menú con la
  cabeza de cada jugador conectado. Al elegir uno sale un pequeño tanque con cara de calavera:
  - Es **inmortal**: está hecho de displays, no tiene vida ni caja de golpe.
  - Es **lento** (`sha-speed` 0,12 bloques por tick) y rueda por el suelo subiendo escalones.
  - Si no puede avanzar (un muro, un acantilado, un techo) **atraviesa los bloques** hacia su
    objetivo y vuelve a rodar en cuanto encuentra suelo.
  - **Mantiene cargado el chunk** en el que está, así que nunca se detiene por falta de jugadores.
  - Al alcanzar a su objetivo **explota** (`sha-damage` 18, sin romper bloques).
  - Si el objetivo **se desconecta, desaparece**. Si está en otro mundo, espera donde está.
  - Uno a la vez, enfriamiento `sha-cooldown-seconds` 300.

---

## 🧱 Modelos de los Stands

Todos los Stands con cuerpo (Star Platinum, The World, Magician's Red, Crazy Diamond, Killer
Queen) y el propio DIO se dibujan con **once cabezas de jugador texturizadas** (cabeza, pecho,
abdomen, brazos, antebrazos, muslos y espinillas); el armor stand de DIO ahora solo es su hitbox.
Los modelos van en el jar (`stands/<nombre>.txt`, formato `/summon` de BDEngine, hechos a partir de
skins con `tools/stand-skins`): el plugin lee cada uno tal cual, lo pone de pie y lo anima con su propio esqueleto (respiración, la
cabeza siguiendo a su usuario, codos y rodillas que se doblan, la ráfaga y cada movimiento de
The World de DIO).

Un servidor puede reemplazar cualquiera: pon un export `/summon` de BDEngine, pegado tal cual, en
`plugins/MultiverseCreatures/stands/<nombre>.txt` (`star-platinum.txt`, `dio-brando.txt`, ...) y
usa `/msc reload`. Un archivo que no se pueda leer se avisa en la consola y se usa el modelo
incluido. El `README.txt` de la carpeta lo repite. Hermit Purple no tiene cuerpo: sigue siendo un
espiral de enredaderas con espinas alrededor del brazo de su usuario.

## ⚔️ Armas de los jefes

| Arma | Receta | Habilidades |
|---|---|---|
| **Executioner's Guillotine** (hacha de netherita) | `E C E / · A · / · W ·` — 2 Executioner's Edge, cadena de hierro, hacha de netherita, Orden del Verdugo | Sentencia (+40% a objetivos bajo el 30%), Sangrado (Wither I), Cadenas del Juicio (clic derecho), Caída de la Guillotina (shift + clic derecho) |
| **Architect's Deployer** (arco) | `· E · / N K N / · B ·` — fragmento de eco, 2 lingotes de netherita, Architect Kernel, arco | Paquetes guiados (+20%), sudo rm -rf (shift + clic izquierdo, haz de 40 bloques), Failover (bajo el 30% de vida, saltas tras el atacante) |

NIX suelta **1 Executioner's Edge** siempre y un segundo con un 35% (`edge-drop-chance`,
`edge-bonus-chance`). JACKSTAR sigue soltando su **Architect Kernel**, que además de invocarlo
ahora es el núcleo del Architect's Deployer.

---

## ⌨️ Comandos

| Comando | Quién | Qué hace |
|---|---|---|
| `/stand` | todos (`msc.stand`) | Tu Stand, si eres portador o vampiro, y tus habilidades |
| `/stand summon` · `/stand ability [1\|2]` · `/stand sha` | todos | Lo mismo que las teclas |
| `/stand give <jugador> [stand]` | `msc.admin` | Despierta un Stand (al azar si no se indica) |
| `/stand remove <jugador>` | `msc.admin` | Quita el Stand |
| `/stand vampire <jugador> <on\|off>` | `msc.admin` | Da o cura la sangre de DIO |
| `/stand arrow <jugador>` | `msc.admin` | Atraviesa al jugador con la Flecha (prueba) |

`/msc give` incluye `vampireblood`, `unstableblood`, `bearerelixir`, `executioneredge`,
`executionerguillotine` y `architectdeployer`.
