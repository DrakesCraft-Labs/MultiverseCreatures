# 📦 Instalación

Una guía paso a paso para instalar y configurar MultiverseCreatures.

---

## 1. Requisitos

| Requisito | Versión |
|---|---|
| Software de servidor de Minecraft | **Paper / Purpur / Spigot 1.21+** |
| (Recomendado) | Purpur 1.21.11+ (el plugin está compilado contra `purpur-api 1.21.11`) |
| Java | **21 o superior** |

El plugin usa APIs específicas de 1.21 (entidades de display con transformación, AttributeModifier moderno, contenedores de datos persistentes con NamespacedKey) y **no cargará en 1.20 o versiones anteriores**.

---

## 2. Instalar

1. Descarga el último `MultiverseCreatures-vX.Y.Z.jar` desde una de estas opciones:
   - La página de [Modrinth](https://modrinth.com/plugin/multiversecreatures) (recomendado)
   - La página de [GitHub Releases](https://github.com/Chagui68/MultiverseCreatures/releases)
2. Detén tu servidor.
3. Coloca el JAR en `plugins/`.
4. Arranca el servidor una vez — esto genera el `plugins/MultiverseCreatures/config.yml` por defecto y las subcarpetas de recursos (`music/`, `schematics/`, `structures/`).
5. (Opcional) Edita `config.yml` a tu gusto. Los valores por defecto son razonables, pero los usuarios avanzados querrán ajustar las probabilidades de spawn, la salud del jefe, los cooldowns de objetos y los mensajes de muerte.
6. Reinicia y ¡a jugar!

---

## 3. Configuración (`config.yml`)

Todos los ajustes del plugin viven en `plugins/MultiverseCreatures/config.yml`. El config por defecto viene con valores sensatos; los ajustes más útiles son:

### Probabilidades de spawn

Cada reemplazo de spawn natural tiene su propia clave de probabilidad:

```yaml
dio-boss:
  spawn-chance: 0.005         # 0.5% de los spawns de zombie
mahoraga:
  spawn-chance: 0.02          # 2%
obsidian-guard:
  spawn-chance: 0.02          # 2%
creeper-jr:
  spawn-chance: 0.15          # 15% (aparece en trío)
head-slime:
  spawn-chance: 0.10          # 10%
shadow-rogue:
  spawn-chance: 0.05
bone-shield:
  spawn-chance: 0.06
flame-elemental:
  spawn-chance: 0.10
frost-golem:
  spawn-chance: 0.08
void-crawler:
  spawn-chance: 0.07
storm-caller:
  spawn-chance: 0.04
venom-witch:
  spawn-chance: 0.05
soul-reaper:
  spawn-chance: 0.05
chaos-mage:
  spawn-chance: 0.06
ender-knight:
  spawn-chance: 0.04
zombie-horse-trap:
  full-moon-spawn-chance: 0.001   # 0.1% durante la Luna Llena
```

### Estadísticas del jefe

```yaml
armor-stand-boss:
  health: 500.0
  aggro-range: 50
  seal-damage: 15.0
  hover-barrage-damage: 12.0

dio-boss:
  health: 300.0
  damage: 10.0
  cooldown-ms: 120000
  spawn-chance: 0.005
  drop-chance: 0.10
  freeze-radius: 50
  freeze-duration-ticks: 100
  freeze-damage: 10.0
  freeze-damage-radius: 30
  teleport-inner-radius: 25
  teleport-darkness-duration: 100
  teleport-slowness-duration: 100
```

### Cooldowns y amplificadores de objetos

```yaml
excalibur:
  solar-flare:
    range: 20
    cooldown-ms: 15000

ice-king-crown:
  launch-cooldown-ms: 10000
  blizzard-cooldown-ms: 60000

dio-stand:
  cooldown-ms: 120000
  freeze-radius: 50
  freeze-duration-ticks: 100
  stand-duration-ticks: 100
  stand-interval-ticks: 3
  stand-damage: 4.0
```

### Mensajes de muerte

Cada mob letal tiene una lista `<mob>.death-messages`; al morir un jugador, se muestra una entrada aleatoria con `%player%` reemplazado:

```yaml
creeper-jr:
  death-messages:
    - "%player% was blown up by Creeper Jr."
    - "%player% got too close to a tiny creeper."
```

---

## 4. Música personalizada

Coloca cualquier archivo `.nbs` en `plugins/MultiverseCreatures/music/`. El plugin incluye 12 canciones (incluyendo `Undertale-Megalovania.nbs`, usada como tema del jefe Centinela de Obsidiana). Puedes:

- Reemplazar el tema del jefe sustituyendo `Undertale-Megalovania.nbs`.
- Obtener el disco de jukebox de cualquier canción con `/msc music disc <canción>` o del **Disc Trader** (aldeano) (`/msc spawn disctrader`). Los discos funcionan en un jukebox como los discos vanilla — ver [Música](./Music.md) para la lista completa de canciones y créditos.

Usa **formato NBS 5+**; las muestras de note-block de supervivencia de un solo instrumento funcionan mejor para la reproducción realista en el mundo.

---

## 5. Notas de rendimiento del servidor

- La mayoría de mobs añaden una tarea de IA por tick limitada a sí mismos. Mientras mantengas recuentos de mobs razonables, el coste es similar al vanilla (el plugin usa `BukkitRunnable` y `PersistentDataContainer` de Bukkit en lugar de NMS).
- El Centinela de Obsidiana tiene la IA más costosa; si ejecutas más de uno a la vez en un servidor de bajas especificaciones puedes ver subir el coste por tick — solo ejecuta el jefe cuando sea necesario.
- Todos los ArmorStands `MSC_*` se limpian con `/msc cleanstands` si una pelea falla o el servidor se reinicia a mitad.

---

## 6. Permisos

Añade un gestor de permisos (p. ej. LuckPerms) y asigna:

```
msc.admin   → personal de confianza que puede usar /msc
```

Aún no existe un permiso por objeto o por spawn — todo el comando `/msc` está controlado por `msc.admin` (por defecto solo OP).

---

## 7. Solución de problemas

| Síntoma | Solución |
|---|---|
| El plugin no carga | Revisa el log del servidor buscando "UnsupportedClassVersionError" — necesitas Java 21+ en la JVM del servidor. |
| Los mobs no aparecen | Ten en cuenta las razones de `CreatureSpawnEvent`: el plugin solo reemplaza spawns `NATURAL`, `SPAWNER_EGG`, `REINFORCEMENTS` y `SPAWNER`. Los spawns de granjas de mobs se ven afectados; los spawns personalizados con huevos pueden no verse afectados. |
| Los objetos no funcionan | Asegúrate de tener el objeto MSC real (con etiquetas PDC `msc_*`). Los objetos renombrados o clonados en creativo pueden no estar etiquetados. Usa `/msc give <objeto>` para obtener copias válidas. |
| El jefe es invencible | Si el jefe está en plena transición de fase (Barrera/Desesperación), es brevemente invulnerable — espera unos segundos. |
| Los Stands no desaparecen tras un crash | Ejecuta `/msc cleanstands` una vez. Esto limpia todos los ArmorStands etiquetados `MSC_*` en todos los mundos. |
| La música personalizada no suena | Confirma que el archivo `.nbs` está en `plugins/MultiverseCreatures/music/` y que la extensión es `.nbs` en minúsculas. |

---

## 8. Compatibilidad y seguridad

- **100% API de Bukkit** — sin accesos NMS; seguro en Paper / Purpur / Spigot 1.21+.
- El plugin usa un manejador de canal Netty personalizado (detección de entrada de salto de pared de las Garras de Mantis). Se inyecta al entrar, se elimina al salir y es resiliente ante desconexiones.
- Sin base de datos externa; todo el estado del plugin está en memoria o en PDC por mundo.
- Sin telemetría; el plugin no hace conexiones salientes.

---

## 9. Enlaces útiles

- [Página de Modrinth](https://modrinth.com/plugin/multiversecreatures)
- [GitHub](https://github.com/Chagui68/MultiverseCreatures)
- [Rastreador de incidencias](https://github.com/Chagui68/MultiverseCreatures/issues)
- [Inicio de la wiki completa](./Home.md)