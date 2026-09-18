# ⚙️ Zona Técnica

La **Zona Técnica** es el área de la wiki destinada a desarrolladores y colaboradores. Reúne toda la documentación que describe el **funcionamiento explícito del código** de MultiverseCreatures y su **estructura de organización interna**: cómo está dividido el proyecto, qué convenciones se siguen y cómo se prueban los cambios antes de publicarlos.

> Si solo juegas con el plugin, puedes ignorar esta sección. Si piensas extender el proyecto o reportar bugs con contexto técnico, empieza por aquí.

## 📄 Páginas

- [Arquitectura](./Architecture.md) — Estructura del código (`src/main/java`), subsistemas, convenciones obligatorias y guía para añadir contenido nuevo.
- [Tests](./Tests.md) — El framework de pruebas (JUnit 5), cómo ejecutarlas y qué verifica cada clase de test.

## 🧭 Cómo navegar

1. Empieza en **Arquitectura** para entender el mapa del código y los patrones que todo colaborador debe respetar.
2. Antes de tocar código, revisa la sección de **Tests** para saber qué comportamiento ya está cubierto y qué romperías si cambias la lógica.

---

## 🗺️ Composición del proyecto (resumen)

| Subsistema | Paquete | Responsabilidad |
|---|---|---|
| Punto de entrada | `com.Chagui68.MultiverseCreatures` | `onEnable`/`onDisable`, registro de recetas, listeners e instancias de todos los sistemas |
| Comandos | `com.Chagui68.commands` | Ejecutor y tab-completer de `/msc` |
| Entidades (mobs, minijefes, jefes) | `com.Chagui68.entities` | Criaturas personalizadas, jefes con framework de ataques, minijefes, enrutador de spawns |
| Objetos y recetas | `com.Chagui68.items` | Armaduras, armas, componentes, alimentos, reliquias y registro de recetas |
| Eventos de Bukkit | `com.Chagui68.listener` | Un manejador de eventos por sistema (objeto, jefe, dimensión, armadura...) |
| Música NBS | `com.Chagui68.music` | Reproducción de canciones NBS, discos y jukebox |
| Rituales y dimensión | `com.Chagui68.ritual` | Estructuras de ritual, invocaciones y la dimensión privada del jefe |
| Utilidades | `com.Chagui68.utils` | `ItemBuilder`, `MscEntityUtils`, políticas de mundo |

Para el detalle completo (con el árbol de directorios real y las convenciones de código) consulta [Arquitectura](./Architecture.md).