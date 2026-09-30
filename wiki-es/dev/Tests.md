# 🧪 Tests

Esta página documenta **cómo se prueban** los cambios del plugin y **qué verifica cada suite**. Los tests solo cubren lógica pura y matemática de alto riesgo (los jefes y rituales se rompen fácilmente si se tocan); no existe un servidor real como dependencia.

## ⚙️ Framework y ejecución

- **JUnit 5 (Jupiter)** — dependencia `org.junit.jupiter:junit-jupiter:5.11.4` (scope `test`).
- **Maven Surefire 3.5.2** — ejecuta los tests automáticamente en la fase `test`.
- **Java 21** — mismo compilador que el código principal.
- **Headless**: no se arranca un servidor Paper/Purpur. Las clases de Bukkit que se tocan (p. ej. `World`) se simulan con `java.lang.reflect.Proxy` o se usan objetos `Location` con mundo `null` para ejercitar solo la aritmética.
- **SnakeYAML** (viene con `purpur-api`) parsea `config.yml` y `plugin.yml` en `ConfigFilesGuardTest`, así una indentación inválida falla en la suite y no al arrancar el servidor.

Comandos:

```bash
mvn test                     # Ejecuta todos los tests
mvn clean package -DskipTests  # Compila el JAR saltando los tests
```

Para ejecutar una clase concreta:

```bash
mvn test -Dtest=NixInvocationStructureTest
```

## 📋 Inventario de tests

Los 40 archivos viven en `src/test/java/com/Chagui68/` reflejando el paquete de la clase que prueban.

### `utils/MscEntityUtilsHealthTest` — Salud virtual de los jefes
Cubre la aritmética de salud de `utils/MscEntityUtils`:
- **`calculateSafeHealth`** — Ajusta la salud pedida al límite del servidor (cap de atributo, por defecto 1024 en Paper) evitando `IllegalArgumentException`. Verifica casos de jefes reales: ArmorStandBoss (3200), NIX (450), Frost Golem (200); clampa a mínimo **0.1** (evita muerte instantánea al aparecer) y protege de entradas negativas.
- **`calculateVirtualProgress`** — Clampa el progreso de la boss bar entre 0.0 y 1.0 (incluye `0/0` = 0).
- **`calculateScaledPhysicalHealth`** — Convierte la salud **virtual** (p. ej. 3200) a la salud física real almacenada en la entidad (escalada a su máximo físico), con 0 → muerte.

### `ritual/RitualStructureTest` — Ritual de entrada (overworld, 7×7)
- Centro del ritual en `(3, 0, 3)` con radio 5.
- **Exactamente 12 velas** en la capa `Y=1`, ninguna en el centro.

### `ritual/NixInvocationStructureTest` — El Cadalso del Verdugo (5×5)
- 4 velas rojas, cada una a **distancia Manhattan 1** del yunque `(2, 0, 2)` y en el suelo (`Y=0`).
- 4 postes de cadalso formando el cuadrado 5×5 en las esquinas.
- `getAnvilLocation` → `origen + (2.5, 0.5, 2.5)`.
- Partículas de las horcas elevadas a `Y + 1.8` (altura de calavera).
- `containsCandle` acepta solo las 4 posiciones de vela y rechaza centro, esquinas y fuera de la estructura.

### `ritual/BossInvocationStructureTest` — Círculo de invocación del Centinela (5×5)
- Centro en `(2, 0, 2)` (donde se suelta el Fragmento de Eco).
- `containsCandle` valida el anillo de 12 velas rojas y rechaza el centro y el exterior.

### `commands/CommandHelpPaginationTest` — Paginación de `/msc`
- Ejercita los helpers reales de `commands/CommandMenu` (ya no una copia local): `clampPage` estrecha cualquier página pedida al rango `[1, totalPages]` (entradas negativas, `0`, y por encima del máximo), `pageCount` siempre cubre todas las líneas con al menos una página, y `pageSlice` devuelve exactamente una ventana por página, sin solapes.
- Los prefijos de categoría conservan el formato legacy `&6&lLabel&8:` / `   &e• &fitem`.
- Los títulos y líneas de `spawn`, `give` y `attack` existen para todas las páginas válidas (1–3, 1–4 y 1–4 respectivamente), y los menús autopaginados `dummy` y `seal` siguen cabiendo en dos páginas de 12 líneas.

### `commands/SpawnCatalogueTest` — Tabla de datos de `/msc spawn`
- Las tres páginas de ayuda son, byte a byte, el texto que imprimía el comando antes de extraer la tabla.
- Cada alias resuelve a su propio tipo, los alias son únicos, en minúsculas y sin vacíos, y los atajos legacy (`army`, `rogue`, `flame`, …) se ofrecen en el autocompletado.
- Los mensajes de éxito/fallo conservan el texto de cada rama antigua (`Spawned Military Zombie Horse trap!` / `Failed to spawn trap.`).
- Los tipos spawneables pero indocumentados (JackStar) quedan fuera del menú de ayuda, y el jefe Jack responde a **un único alias** (`jack`): los atajos retirados `jackstar`/`arquitecto`/`systemarchitect` ya no se resuelven por comandos ni aparecen en el autocompletado.

### `commands/GiveCatalogueTest` — Tabla de datos de `/msc give`
- Las cuatro páginas de ayuda son, byte a byte, el texto que imprimía el comando antes.
- Todo ítem nombrado en la ayuda es un alias real entregable (incluidas las líneas agrupadas como `reaperessence &8/ &evoidessence`), y un alias desconocido devuelve `null` en lugar de lanzar.
- Los alias son únicos y en minúsculas, y cada entrada declara su fábrica de ítem.

### `commands/AttackCatalogueTest` — Tabla de datos de `/msc attack`
- Las cuatro páginas de ayuda son, byte a byte, el texto que imprimía el comando antes.
- Los nombres del autocompletado son los ataques documentados, únicos y en minúsculas; cada entrada está en una página existente y comparte el color de cuerpo `&7`.

### `commands/AttackRegistryCoherenceTest` — las cinco listas de ataques siguen de acuerdo
- Lee las fuentes de los ataques y `ArmorStandBoss` y comprueba que cada clase de ataque está registrada en `initAttacks()` y responde al nombre derivado de su clase (con las excepciones documentadas `executionsweep`, `soultethers`, `runemines`).
- Los conjuntos aéreo/suelo coinciden con las carpetas donde viven las clases, ningún ataque está en ambos conjuntos, y los de distancia/defensivos no están en ninguno.
- Todos los nombres citados por los arrays de rotación aleatoria son ataques registrados.
- El catálogo de ayuda y las fuentes son el mismo conjunto de nombres, así que no se anuncia nada que no pueda ejecutarse ni queda oculto nada ejecutable.

### `commands/MscKillFilterTest` — Predicados de `/msc kill`
- Las etiquetas de scoreboard con prefijo `MSC_` identifican a una entidad del plugin; los nombres legacy sin etiqueta (Mahoraga, Garou, Bone Shield, …) siguen contando; los mobs vanilla quedan intactos.
- El filtro por tipo compara etiquetas con `-`/`_` eliminados y cae al nombre como subcadena; un tipo `null` o vacío nunca coincide.

### `commands/DummyAttackPreviewTest` — Previsualización de ataques en el dummy
- Cada ataque documentado en `AttackCatalogue` es uno que el dummy acepta, en cualquier caso, y un nombre desconocido se rechaza en vez de ejecutar otra cosa en silencio; `random` elige de esa misma lista, de forma reproducible.
- El autocompletado y la ayuda del dummy ofrecen la previsualización, y cada página de la vista previa renderiza exactamente los ataques documentados en ella, ejecutada por el `CommandMenu` real con un sender que graba.
- La promesa en sí: `MscEntityUtils.damageBy` rechaza un golpe de una entidad marcada como dummy en actuación — comprobado con una víctima proxy que graba las llamadas a `damage` — mientras que un atacante sin marca no cuenta como previsualización. La dirección contraria no puede correr headless, porque `DamageType.GENERIC` solo resuelve contra un registro vivo.

### `entities/boss/NixDamageCapTest` — Cap de daño de NIX
- Nix nunca puede perder más de `entities.nix-executioner.max-damage-per-hit` (por defecto **100**) en un solo golpe: lo que supera el cap se recorta, lo que queda por debajo pasa intacto y el cap nunca *infla* un golpe.
- `0` (o cualquier valor no positivo) desactiva el límite, que es la forma documentada de volver al comportamiento sin tope.
- Fija la aritmética del pool de vida: una ráfaga de 10 000 deja 350 de 450 HP, cuatro golpes con cap dejan 50 y el quinto termina con el jefe.

### `entities/boss/PenetratingDamageTest` — Daño penetrante del Centinela
- **La armadura se devuelve**: el motor ya ha aplicado `ARMOR`, `MAGIC` (encantamientos de Protección) y `RESISTANCE` al daño del evento, y `unmitigated` deshace esos tres para que un tajo de 22 sobreviva a netherite completo. El bloqueo con escudo *no* se devuelve a propósito y el resultado nunca es negativo.
- `penetratingDamage` mantiene la Resistencia parcialmente efectiva: el jefe ignora `penetrating-resistance-pierce` (por defecto **0.2**) de la reducción de la poción, así que Resistencia I bloquea el 16% en lugar del 20% (un golpe de 10 quita 8.4), `0.0` deja la poción totalmente efectiva y `1.0` la ignora por completo. La mitigación es del 20% por nivel y se topa al 100% (Resistencia V).
- Los valores de perforación fuera de rango se recortan, un golpe nunca puede superar el daño bruto y el cap por golpe (`max-damage-dealt`, 15) se aplica antes de la Resistencia, así que la poción nunca puede subirlo.

### `entities/boss/PenetratingHitTest` — Snapshot de `/msc debug`
- El record inmutable `PenetratingHit` es el único sitio donde se ejecuta el pipeline completo (devolver armadura/Protección/Resistencia, aplicar el cap y perforar la Resistencia), así que las cifras que imprime `/msc debug` no pueden desviarse del manejador real: un tajo de 22 con netherite completo vuelve como 12.6, el cap se aplica antes de la Resistencia y un golpe absorbido por la armadura termina en 0, no en un negativo ni `NaN`.
- La Resistencia se reporta como nivel de base uno (amplificador 2 → nivel 3) y la edad del snapshot es tiempo transcurrido recortado a cero, así que un reloj hacia atrás no puede producir una edad negativa.

### `entities/boss/SentinelDefenseTest` — Daño entrante del Centinela
- Fija todo el stack defensivo del jefe, ahora extraído de su manejador de eventos a `SentinelDefense`: sello de escudo ×0.5, círculo de curación ×0.8, piel de piedra ×0.5, barrera reflectante ×0.7, el escudo de absorción gastando su vida y el cap `max-damage-per-hit` aplicado **al final**.
- Comprueba las trampas que escondía la versión inline: las defensas no pueden subir un golpe por encima del cap (`200 → 40 → cap 30`), un golpe justo en el cap no se marca como capeado, la barrera reflectante devuelve el 30% del golpe *reducido* y el cap no recorta lo que devuelve, y un jefe invulnerable no genera ningún paso de cap.
- La traza `steps` se comprueba byte a byte porque `/msc debug` la imprime, y un barrido por todas las combinaciones de defensa demuestra que ninguna puede agrandar un golpe ni devolver un valor negativo.

### `commands/DebugReportTest` — Render de `/msc debug`
- Fija las líneas renderizadas sin sender: un golpe penetrante lista su daño del evento, la armadura/Protección/Resistencia devueltas, el total tras la armadura, el cap, la perforación, el nivel de Resistencia y el valor final; una muestra `DEALT` empareja el daño deseado del ataque con lo que el jugador recibió; una muestra `TAKEN` detalla el cap o el reparto del load balancer entre el golpe y lo que costó.
- Una nota de mecánica vacía se omite en vez de dejar un separador suelto, y la línea de edad se controla con un reloj inyectado para que la salida sea determinista.

### `entities/boss/BossDamageLogTest` — Registro de `/msc debug`
- Mantiene una muestra por jugador, jefe y sentido: una muestra nueva sustituye a la anterior en su hueco, y `samplesFor` las devuelve ordenadas por `BossId` y luego por sentido sin importar el orden de inserción, así que el informe no puede reordenarse entre ejecuciones.
- Los jugadores se siguen por separado, `forget` limpia uno sin tocar otro, y los registros nulos se ignoran en vez de lanzar.
- `forgetBoss` elimina las muestras de un jefe para todos los jugadores en una sola pasada y deja intactos los otros jefes: es la misma llamada que hace el listener de la propia bitácora cuando el soporte de un jefe sale del mundo.

### `entities/boss/JackResilienceTest` — Daño entrante de Jack Star
- Cada golpe se resuelve por una sola puerta: un esquive explícito no quita nada, cualquier otro golpe se reparte.
- El reparto **conserva el golpe** (`toBoss + sharedTotal == incoming`) en un barrido de valores y tamaños de grupo, así que un ajuste de balance no puede borrar ni duplicar daño en silencio.
- Un golpe que acierta **siempre llega al jefe**, sea cual sea el tamaño del grupo: es lo que mantiene a Jack Star recibiendo daño en todo momento; una tirada exactamente en la probabilidad de esquive sigue acierta.
- La forma comprimida (degradada) sube la probabilidad configurada a 0.45, y el valor límite conserva la configurada.

### `entities/boss/JackModelTest` — Geometría del modelo de Jack Star
- Las once piezas quedan fijadas contra el **modelo de referencia del juego**: la altura y la profundidad de cada pieza coinciden al milímetro y las once comparten un único eje X, así que el cuerpo no puede descolocarse pieza a pieza.
- El modelo queda **centrado en la hitbox** (columna y `CENTER.x` a cero) en vez de arrastrar el desplazamiento global en X de la referencia, con la cabeza por encima del torso y este por encima de las piernas, la coronilla cerca de los dos bloques y los pies separados del suelo.
- Las extremidades izquierda y derecha están espejadas, cada **articulación está del mismo lado que la extremidad que mueve** (una cadera intercambiada hacía girar una pierna sobre la cadera opuesta), y una extremidad que gira conserva su X y nunca se desprende de su articulación.
- Un barrido demuestra que no hay dos piezas en el mismo sitio, que el cambio de escala escala a la vez traslaciones y escalas de pieza, y que cada pieza cabe dentro de la hitbox del stand.

### `entities/boss/NixModelTest` — Geometría del modelo de NIX
- Las 27 piezas quedan fijadas contra el **modelo exportado**: cada traslación coincide y todo el cuerpo comparte un único eje X, así que ninguna pieza puede desviarse por su cuenta.
- El modelo queda **centrado en la hitbox** (`NixModel.baseTranslation` deja la columna en cero en vez del `+0.066` del export), y `CENTER` es el **punto medio de los extremos exportados** y no la media de las 27 piezas, que cualquier pieza añadida o quitada arrastraría.
- Las extremidades izquierda y derecha están espejadas, cada **articulación está del mismo lado que la extremidad que mueve** y sobre su propio eje, y una extremidad que gira conserva su X y nunca se desprende de su articulación.
- Un barrido demuestra que no hay dos piezas en el mismo sitio, y el **test de la hitbox** mantiene `MODEL_HITBOX_SCALE` cubriendo toda la pose de reposo (0.95 de ancho, 3.75 de alto) sin alejarse más de 0.05 de la escala mínima que el modelo necesita — el literal `2.0` anterior dejaba 1.8 bloques de caja vacía sobre la cabeza.
- Una guardia de fuentes impide que las piezas vuelvan a retrasarse (`setTeleportDuration`/`setInterpolationDuration`/`setDisplayWidth`/`setDisplayHeight` a cero, configurados en un solo sitio) y exige que un recargue **adopte** las piezas que ya tiene en vez de crear un segundo cuerpo superpuesto.

### `entities/NixModelKinematicsTest` — Modelo cinemático de NIX (27 partes)
- El modelo tiene **exactamente 27** partes `ItemDisplay` (export de Blockbench).
- La jerarquía cinemática es rígida: brazos y piernas con **6** segmentos cada uno (para rotar como cuerpo rígido en hombro/cadera), cabeza, torso superior e inferior con **1**.
- Cada matriz de 16 floats se descompone en offset finito, escala estrictamente positiva y cuaternión finito; nombre de perfil y textura `base64` no vacíos.
- El centro horizontal del modelo es finito.

### `entities/KingerOptimizationAndKinematicsTest` — Modelo de Kinger + optimización
- **15** partes (spec de Blockbench) con las mismas garantías de integridad de matrices (16 floats, offset/scale/quaternion finitos) y `CENTER` finito.
- **Throttling de sync de displays**: parado y sin animación sincroniza cada 3 ticks (**30** de 90, ~66% menos); moviéndose o en animación sincroniza **a cada tick** (90 de 90). Esto valida la regla `!isIdle || tickCount % 3 == 0`.

### `entities/KingerModelTest` — Geometría del modelo de Kinger
- Las quince piezas del traje quedan fijadas contra el **modelo exportado**: cada traslación coincide, el tronco y las piernas comparten un único eje Z, y cada mitad de pierna está apilada sobre su propio eje X, así que una pierna que gira no puede partirse de lado.
- `CENTER` es el **eje del torso** (el punto medio de las dos piezas del torso) y no la media de las quince anclas ni el punto medio del bbox, que los brazos arrastran 0.03 y 0.08 bloques hacia delante; el tronco, las piernas y la cabeza quedan sobre ese eje, y el recentrado nunca toca una altura.
- Cada pieza pertenece a un **grupo de extremidad rígido** con una única articulación: la espinilla gira con el muslo en vez de hacerlo sobre su propia rodilla, las piezas de un grupo conservan sus distancias al girar, ninguna se desliza de lado ni se desprende de su articulación, y no hay dos piezas en el mismo sitio.
- El **test de la hitbox** mantiene `MODEL_HITBOX_SCALE` cubriendo toda la pose de reposo (0.5 de ancho, 1.975 de alto) sin alejarse más de 0.1 de la escala 0.94 que la geometría necesita — el literal `2.0` anterior duplicaba la caja en todas direcciones y se tragaba golpes al aire.
- La etiqueta de cada pieza es única y lleva su propietario, así que una adopción no puede confundir dos piezas; una guardia de fuentes impide que las piezas vuelvan a retrasarse (`setTeleportDuration`/`setInterpolationDuration`/`setInterpolationDelay`/`setDisplayWidth`/`setDisplayHeight` a cero, configurados en un solo sitio), exige que un recargue **adopte** el traje que ya tiene en vez de crear un segundo superpuesto y que **reconstruya la barra de jefe** de ese jefe (con la salud virtual de la que se lee su progreso), y mantiene la animación pasando por `KingerModel.compose` en lugar de una transformación por pieza.

### `listener/bossdimension/BossDimensionGuardLogicTest` — Guardia de la dimensión del jefe
- Los manejadores de eventos hacen **short-circuit**: en mundos que no son la `boss_dimension` nunca se evalúa la comprobación de permiso (solo se evalúa dentro del mundo del jefe).

### `entities/handler/MobHandlerRecountTest` — Cooldown del tope de población
- `MobHandler.puedeRecontar` permite el recontado **por mundo** y respeta un cooldown de fallo independiente por mundo: `world` con cooldown hasta `160000` no re-cuenta en `159999` pero sí en `160000`; `world_nether` no se bloquea por el cooldown de `world`.

### `entities/EnderKnightWorldGuardTest` — Teleport del Caballero Ender
- `EnderKnight.sharesWorld` solo devuelve `true` si los mundos coinciden; rechaza identidad de mundo `null`. Garantiza que los cálculos de distancia del tira-tira solo ocurran dentro del mismo mundo.

### `entities/DistanceOptimizationEquivalenceTest` — Optimización distancia²
- El predicado `distanceSquared < threshold²` es **equivalente** a `distance < threshold` (Euclídeo) para todos los umbrales usados en el código (30, 25, 35, 20, 8, 6, 5, 4, 3, 2, 1.8 bloques), incluida la frontera (justo a menos/más de 0.001).
- El daño con caída por distancia calculado desde `sqrt(distancia²)` equivale al calculado con distancia directa (tolerancia 1e-9) para las fases Storm, Despair y AirSlam del Centinela.
- Los objetivos fuera de rango se descartan **antes** de calcular ninguna raíz cuadrada.

### `entities/boss/ShockwaveAndCombatOptimizationTest` — Ondas de choque y combate
- Fórmulas de la onda: el empuje vertical (`Y`) queda acotado en `[0.4, 0.7]` y el multiplicador de daño en `[0.4, 1.0]` para radios 0–30.
- **`hitInThisRing` (deduplicación)**: varios alts de partículas adyacentes del mismo anillo caen dentro del radio de impacto del jugador, pero con la deduplicación el jugador recibe daño **exactamente una vez por anillo**.
- **Throttling de NIX**: parado sincroniza cada 3 ticks (30 de 90); durante el cleave o las cadenas sincroniza a cada tick (90 de 90).

### `entities/boss/BossArenaGroundRecoveryTest` — Recuperación de suelo
- `findFloorY` devuelve la altura del piso, y **`NaN`** — no la `Y` propia del jefe — cuando el escaneo no alcanza nada. Esa distinción es el arreglo: antes "estar apoyado en el piso" y "no haber nada debajo" eran el mismo valor, así que un jefe en modo suelo dejaba de atacar para siempre.
- `getGroundY` conserva su comportamiento documentado de devolver la `Y` actual, fijado por test para que los dos comportamientos no vuelvan a confundirse.
- `ringOffsets` empieza en el origen, contiene cada desplazamiento del radio exactamente una vez y nunca retrocede hacia el origen: la columna usable más cercana debe ganar siempre.
- `findUsableColumn` prefiere la columna del propio jefe, camina hacia fuera si está vacía, respeta el radio de búsqueda y devuelve `null` cuando no hay nada usable, para que el llamante pueda probar la columna del objetivo y después el spawn del mundo.

### `utils/MscWorldPolicyTest` — Lista blanca de mundos
- Una lista **vacía (o ausente) significa todos los mundos**, que es lo que documenta `config.yml`. La implementación la trataba como una lista fija de cinco nombres, así que un servidor con un mundo de nombre propio no tenía conversiones — y su recuento periódico borraba cualquier criatura MSC que encontrara ahí.
- Una lista con contenido restringe, normalizando mayúsculas y espacios sobrantes.
- Los mundos propios del plugin (`boss_dimension`, `drakes_bosses`) siguen permitidos incluso con una lista explícita.

### `commands/CommandPermissionTest` — Regla de permisos de `/msc`
- Tener `commands.permission` basta para usar `/msc`, con OP o sin él.
- `commands.op-only: true` mantiene operativos a los operadores que no tienen el nodo; `false` hace que solo cuente el nodo.
- Un jugador normal sin nodo y sin OP queda rechazado.
- Un subcomando sin nodo configurado queda abierto para quien pasó la puerta principal, mientras que uno fijado en `commands.subcommand-permissions` necesita su nodo además de la principal.

### `entities/HeadSlimeImmunityTest` — Inmunidad de la gelatina
- La ventana de inmunidad es una fecha límite por jugador, así que comer una segunda gelatina la **extiende** en vez de que el removal programado anterior la corte antes, y nada sobrevive a la ventana tras un logout.
- Una ventana expirada se descarta al consultarla, y `clearAllImmunity()` se invoca desde `onDisable`.

### `entities/boss/SentinelPhaseTest` — Escalera de fases del Centinela
- La escalera **reproduce exactamente la cadena de comparaciones hardcodeada**: un barrido de fracciones de salud de −5% a 105% se compara contra la cadena `> 0.8 / > 0.6 / > 0.4 / > 0.2` que el jefe tenía en línea, más los bordes exactos (a 80% justos el jefe ya está en la fase 1), entradas sin sentido, y que la fase solo crece cuando la salud baja.
- `sanitizeThresholds` descarta umbrales fuera de `(0, 1]` y no finitos, ordena el resto de mayor a menor, colapsa duplicados (serían una fase de ancho cero), conserva el `1.0`, devuelve una lista inmutable, y recurre a los valores por defecto cuando una edición del config no deja nada usable.
- Los títulos generados de la barra de jefe se afirman **byte a byte** contra los cinco strings que tenía el switch, para las cinco fases por defecto y para una escalera reescalada de tres fases — incluyendo que una fase más allá del final no puede emitir cuadrados negativos.
- Los colores de la barra siguen a las fases, y una escalera más larga reusa el último color de la paleta en vez de caer a rojo.

### `utils/MscTextTest` — Paridad de nombres de items y mobs
- Cada helper que arma nombres de items, lore, frases de sabor y los pies `✦ … ✦` se serializa de vuelta con `LegacyComponentSerializer.legacySection()` y se compara con el string `ChatColor` exacto al que reemplazó, así la migración no puede mover un espacio, un código de color ni una negrita sin que se note.
- Cubre los cambios de color a mitad de línea (`rich`), las líneas vacías separadoras (`blank`), los nombres sin color (`plain`) y la validación de argumentos de `rich`.
- Fija la regla legacy de que **un código de color limpia la negrita**: un prefijo en negrita seguido de otro color queda en negrita solo en el prefijo, y por eso el nombre de Garou se arma como dos hermanos y no como padre decorado. Un hijo heredaría la negrita, y el test deja esa trampa a la vista.
- `plainText` es la contraparte que sirve para **comparar** un nombre en vez de mostrarlo, así que debe quitar todo código y devolver string vacío para una entidad sin nombre.

### `ConfigFilesGuardTest` — Contrato de recursos (`config.yml` / `plugin.yml`)
- Parsea ambos recursos con SnakeYAML, así una indentación rota o una sección perdida fallan en el build y no al arrancar el servidor.
- Escanea `src/main/java` buscando rutas de config entre comillas y falla si alguna no existe en `config.yml`. Nada hacía cumplir la promesa del encabezado ("all paths match the code"): el handler del Nullshear Edge leía cinco claves `items.nullshear-edge.*` que no estaban en el archivo, y dos claves del pasivo de Excalibur estaban documentadas pero hardcodeadas, cayendo ambas silenciosamente a los valores por defecto del código.
- Verifica que `plugin.yml` conserve el comando, el nodo `msc.admin` (el mismo que declara `commands.permission`), el nodo `msc.admin.bypass` que usan los handlers de la dimensión del jefe, y una línea `usage` que liste todos los subcomandos.
- Comprueba que los ajustes visibles para el jugador sigan sanos: los `phase-thresholds` del Centinela descienden dentro de `(0, 1]`, las duraciones de defensa duran al menos un tick, `no-player-despawn-ticks` admite `0`, y cada mob conmutable conserva su flag `enabled`.
- Un autotest prueba que el escáner de literales reporta los literales con punto fuera de comentarios e ignora los que están dentro.
- Verifica que `commands.subcommand-permissions` exista como **mapa vacío** por defecto: la puerta documentada no debe desaparecer en silencio, y la config que se envía no debe restringir nada por sorpresa.

### `utils/LegacyNameApiGuardTest` — Guardia de la migración
- Lee `src/main/java` y falla si algún archivo vuelve a las APIs String deprecadas de nombre (`setDisplayName`, `setLore`, `setItemName`, `setCustomName`, `getDisplayName`, `getCustomName`). Esos métodos siguen compilando y funcionando, así que un item escrito a la vieja usanza solo se notaría como un tooltip sutilmente mal.
- Las coincidencias dentro de comentarios se ignoran, y el escaneo verifica que recorrió todo el sourceset para no pasar de forma vacua.
- Un segundo test le da al detector una muestra con las seis APIs más una comentada, probando que la guardia detecta exactamente lo que busca.

### `utils/MscLogTest` — Fallos reportados
- Los veintiún bloques `catch` que se tragaban su excepción (`catch (Exception ignored) { }`) ahora reportan por `utils/MscLog`; esta suite lo maneja con un `Handler` capturador y comprueba que de verdad se pide al logger del plugin que imprima.
- Un fallo tolerado se registra en `FINE` y uno accionable en `WARNING`, siempre con el contexto, el nombre simple de la excepción y su mensaje — un `NumberFormatException` conserva la entrada ofensiva.
- Una excepción sin mensaje igualmente se nombra (`java.lang.IllegalStateException`), una nula reporta `unknown error` en vez de lanzar, e `init(null)` conserva el logger anterior, así que el orden de arranque no puede silenciar el plugin.

### `utils/SilentCatchGuardTest` — Sin bloques catch silenciosos
- Guardia de fuentes: recorta comentarios, strings y chars (conservando las líneas), localiza cada cláusula `catch` bajo `src/main/java` y **falla si algún cuerpo queda en blanco**, con una lista de permitidos vacía y un suelo de 40 catches para que el escáner no pase de forma vacua.
- Es lo que impide deshacer el cambio de logging bloque a bloque: una excepción tragada es invisible en una revisión, pero un cuerpo de `catch` vacío no.

## 🗃️ Dónde se corren

Los tests se ejecutan en la **fase `test` de Maven** (Surefire). No requieren servidor ni red: solo el JDK 21 y las dependencias del `pom.xml`.

También corren en CI: `.github/workflows/verify.yml` ejecuta `mvn verify` en cada push a `main` y en cada pull request. La publicación a Modrinth (`modrinth-publish.yml`) espera a ese job, porque su propio paso de compilación usa `-DskipTests`.

> Al tocar código relacionado con salud/jefes, estructuras de ritual o el modelo cinemático de NIX/Kinger, ejecuta la suite completa con `mvn test` para asegurarte de no introducir regresiones.