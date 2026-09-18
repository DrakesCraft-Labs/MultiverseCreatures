# 🌌 Dimensión del Ritual

La **Dimensión del Ritual** (mundo `boss_dimension`) es un mundo privado, solo para el jefe, donde se pelea contra el **Centinela de Obsidiana**. Es un mundo de vacío: un suelo de bedrock con 5 capas de obsidiana llorosa, un cielo rojo eterno, sin clima, sin ciclo de día/noche y sin spawns de mobs naturales. La dimensión se genera automáticamente la primera vez que un jugador entra en ella.

> La dimensión está restringida a propósito **mientras el jefe está activo**: durante la pelea, los jugadores no pueden colocar ni romper bloques, y casi todos los comandos están bloqueados (solo `/say`, `/me`, `/help`, `/?` y `/msc dimtp` funcionan). Con el Centinela de Obsidiana inactivo, los jugadores vuelven a poder construir (p. ej. montar la Estructura del Ritual de salida) y usar todos los comandos con normalidad. Los administradores evitan las restricciones con el permiso `msc.admin.bypass`.

---

## 🕯️ Entrar: la Estructura del Ritual

Para entrar en la dimensión debes construir y encender la **Estructura del Ritual** en el overworld.

### Distribución (7×7, a nivel del suelo)

```
. S S S S S .        S = escaleras de piedra negra pulida (borde)
S S C K C S S        C = piedra negra pulida cincelada
S C O B O C S        O = obsidiana llorosa
S K B X B K S        B = ladrillos de piedra negra pulida
S C O B O C S        K = ladrillos de piedra negra pulida agrietados
S S C K C S S        X = obsidiana (centro)
. S S S S S .
```

- El **bloque central** debe ser **obsidiana**.
- En la **segunda capa** (1 bloque por encima del suelo), coloca **12 velas** en un anillo alrededor del interior del borde:

```
. . c c c . .        velas (c) en la capa y+1:
. c . . . c .        3 en el borde superior (z=1, x=2-4)
. c . . . c .        2 en cada lado (x=1 y x=5, z=2-4)
. c . . . c .        3 en el borde inferior (z=5, x=2-4)
. . c c c . .
```

### Activación

1. Construye la estructura y coloca las 12 velas.
2. Enciende **cada vela** con pedernal y acero, una carga de fuego u otro objeto de vela.
3. Aparecen partículas de círculo de fuego rojo/azul y partículas de portal alrededor de la estructura.
4. Tras **~5 segundos**, cualquier jugador dentro del círculo (radio 5 desde el centro) es teletransportado a la Dimensión del Ritual (recibe un efecto breve de ceguera — *"There is no escape."*).

> Solo puede haber un ritual activo por mundo a la vez. Si la estructura se rompe o las velas se apagan, el ritual se detiene.

---

## ⚔️ Invocar al jefe: el Círculo de Invocación

Una vez dentro de la dimensión, el **Centinela de Obsidiana** debe invocarse manualmente.

### Distribución (5×5, anillo de velas rojas)

```
_ R R R _        (anillo de 12 velas rojas — 3 por borde — interior vacío)
R _ _ _ R        centro: vacío — suelta el Fragmento de Eco aquí
R _ _ _ R
R _ _ _ R
_ R R R _
```

1. Coloca **12 velas rojas** en un anillo de 5×5 (las esquinas y bordes de un cuadrado, dejando el centro vacío).
2. Enciende **todas** con pedernal y acero o una carga de fuego.
3. Una animación de **pentagrama** en llamas aparece en el medio mientras la invocación está activa.
4. **Suelta un Fragmento de Eco** (`echo_shard`) en el centro del círculo.
5. El fragmento se consume, las velas se apagan y el **Centinela de Obsidiana** despierta en el centro.

> Matar al Centinela dropea un **Núcleo Centinela** (probabilidad configurable, `armor-stand-boss.sentinel-core-drop-chance`, por defecto 100%) — un ingrediente clave para el **Grimorio Centinela** y otras recetas cumbre.

---

## 🪓 Invocación de NIX: El Cadalso del Verdugo

**NIX - El Verdugo** solo puede ser invocado de forma exclusiva dentro de la **Dimensión del Jefe** (`boss_dimension`). Los jugadores deben erigir **El Cadalso del Verdugo** y ofrecer un sacrificio de sangre sobre el yunque.

> **Las coordenadas son relativas**: marcan la posición de cada bloque respecto a la **esquina suroeste del 5×5** (tu punto de origen). No hay que construirlo en un lugar fijo del mundo: el ritual detecta el patrón en cualquier sitio.
>
> **El suelo no es específico**: el plugin no comprueba el bloque del suelo ni los alrededores, solo los bloques de la estructura. Puedes montarlo sobre obsidiana llorosa, tierra, lo que sea.

### Distribución (Huella 5×5)

```
P . . . P        P = Poste de Cadalso (3 bloques de alto)
. . c . .        c = Vela Roja (encendida)
. c Y c .        Y = Yunque Central (El Tajo de Decapitación)
. . c . .
P . . . P
```

### Materiales Requeridos
- **1 Yunque Central** (`anvil`, `chipped_anvil` o `damaged_anvil`) en el centro: `(2, 0, 2)`.
- **4 Velas Rojas** (`red_candle`) a nivel del suelo, una a cada lado del yunque:
  - Norte `(2, 0, 1)`, Sur `(2, 0, 3)`, Oeste `(1, 0, 2)`, Este `(3, 0, 2)`.
- **4 Postes de Cadalso en las Esquinas**: `(0, 0)`, `(4, 0)`, `(0, 4)` y `(4, 4)`. Cada poste son **3 bloques de alto**:
  - `Y=0` — Base: ladrillos de piedra negra pulida, piedra negra pulida, ladrillos de pizarra profunda, pizarra pulida, obsidiana llorosa o bloque de hierro.
  - `Y=1` — Cadena: `iron_chain` o cualquier bloque con nombre que termine en `chain`.
  - `Y=2` — Calavera: de esqueleto, wither, jugador o zombie (normal o de pared).

### Procedimiento de Invocación
1. Construye el cadalso en cualquier superficie de la `boss_dimension`.
2. Enciende las **4 velas rojas** con un mechero o carga ígnea.
3. **Activa el ritual**: haz clic derecho sobre cualquiera de las velas rojas (con el mechero en la mano o con cualquier objeto). Será entonces cuando el plugin compruebe que la estructura está completa y las 4 velas encendidas.
4. **Efecto de Invocación Activa** (mientras dura):
   - Rayos y partículas de sangre carmesí (`#8B0000`) se conectan desde las 4 calaveras de los postes hacia el yunque central.
   - Suenan cadenas pesadas a intervalos mientras una densa humareda oscura brota del yunque.
   - Si rompes la estructura o las velas se apagan, el ritual se **cancela** y debes empezar de nuevo.
5. **La Ofrenda de Sangre**: suelta el objeto sobre el yunque central (a menos de **3 bloques** de él y a la altura del suelo):
   - Una **`Sentencia de Muerte`** (`Executioner's Warrant`, con `/msc give warrant` o crafteable).
   - *(También se aceptan como ofrendas alternativas un `Hacha de Netherita` o una `Calavera de Wither Skeleton`)*.
6. **Aparición**:
   - El sacrificio es consumido al instante.
   - Un rayo carmesí azota el yunque con un impacto ensordecedor de guillotina (`Sound.BLOCK_ANVIL_LAND`).
   - Las velas se apagan y **NIX - El Verdugo** se materializa sobre el yunque desatando el combate.

> Mientras NIX esté activo dentro de la Dimensión del Jefe, la colocación y rotura de bloques, así como los comandos de escape, permanecen bloqueados.

---

## 🚪 Salir

Con el jefe inactivo puedes salir directamente con **`/msc dimtp`** (los comandos solo se bloquean durante la pelea). Si quieres salir por medios del juego, o si el jefe sigue activo, la única salida es **el mismo ritual usado para entrar**:

1. Construye la **Estructura del Ritual** (la distribución 7×7 de piedra negra pulida con 12 velas blancas descrita arriba) dentro de la dimensión.
2. Enciende **las 12 velas**.
3. Tras ~5 segundos, los jugadores dentro del círculo son teletransportados de vuelta al **spawn del overworld**.

> Si el plugin se desactiva/recarga con jugadores dentro, todos son enviados de vuelta al spawn del overworld automáticamente.

---

## 🧰 Notas técnicas

- Nombre del mundo: `boss_dimension` (se crea en la primera entrada, se descarga al desactivar el plugin).
- Punto de spawn: `0.5, 10, 0.5` (sobre el suelo de obsidiana llorosa).
- Reglas del mundo: sin ciclo de día/noche, sin ciclo de clima, sin spawns de mobs, reaparición inmediata, sin anuncios de avances.
- El cielo se fuerza a rojo vía un override de bioma.
- Clases relevantes: `BossDimensionManager`, `BossInvocationManager`, `RitualManager`, `RitualStructure`, `BossInvocationStructure` — ver [Arquitectura](./dev/Architecture.md).