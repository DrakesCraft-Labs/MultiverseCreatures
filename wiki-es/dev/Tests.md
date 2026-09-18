# 🧪 Tests

Esta página documenta **cómo se prueban** los cambios del plugin y **qué verifica cada suite**. Los tests solo cubren lógica pura y matemática de alto riesgo (los jefes y rituales se rompen fácilmente si se tocan); no existe un servidor real como dependencia.

## ⚙️ Framework y ejecución

- **JUnit 5 (Jupiter)** — dependencia `org.junit.jupiter:junit-jupiter:5.11.4` (scope `test`).
- **Maven Surefire 3.5.2** — ejecuta los tests automáticamente en la fase `test`.
- **Java 21** — mismo compilador que el código principal.
- **Headless**: no se arranca un servidor Paper/Purpur. Las clases de Bukkit que se tocan (p. ej. `World`) se simulan con `java.lang.reflect.Proxy` o se usan objetos `Location` con mundo `null` para ejercitar solo la aritmética.

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

Los 12 archivos viven en `src/test/java/com/Chagui68/` reflejando el paquete de la clase que prueban.

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
- La lógica de paginación estrecha cualquier página pedida al rango `[1, totalPages]` (entradas negativas, `0`, y por encima del máximo).
- Los subencabezados de `spawn`, `give` y `attack` existen para todas las páginas válidas (1–3, 1–4 y 1–4 respectivamente).

### `entities/NixModelKinematicsTest` — Modelo cinemático de NIX (27 partes)
- El modelo tiene **exactamente 27** partes `ItemDisplay` (export de Blockbench).
- La jerarquía cinemática es rígida: brazos y piernas con **6** segmentos cada uno (para rotar como cuerpo rígido en hombro/cadera), cabeza, torso superior e inferior con **1**.
- Cada matriz de 16 floats se descompone en offset finito, escala estrictamente positiva y cuaternión finito; nombre de perfil y textura `base64` no vacíos.
- El centro horizontal del modelo es finito.

### `entities/KingerOptimizationAndKinematicsTest` — Modelo de Kinger + optimización
- **15** partes (spec de Blockbench) con las mismas garantías de integridad de matrices (16 floats, offset/scale/quaternion finitos) y `CENTER` finito.
- **Throttling de sync de displays**: parado y sin animación sincroniza cada 3 ticks (**30** de 90, ~66% menos); moviéndose o en animación sincroniza **a cada tick** (90 de 90). Esto valida la regla `!isIdle || tickCount % 3 == 0`.

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

## 🗃️ Dónde se corren

Los tests se ejecutan en la **fase `test` de Maven** (Surefire). No requieren servidor ni red: solo el JDK 21 y las dependencias del `pom.xml`.

> Al tocar código relacionado con salud/jefes, estructuras de ritual o el modelo cinemático de NIX/Kinger, ejecuta la suite completa con `mvn test` para asegurarte de no introducir regresiones.