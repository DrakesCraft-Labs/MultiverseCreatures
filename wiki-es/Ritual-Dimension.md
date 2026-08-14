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
- Clases relevantes: `BossDimensionManager`, `BossInvocationManager`, `RitualManager`, `RitualStructure`, `BossInvocationStructure` — ver [Arquitectura](./Architecture.md).