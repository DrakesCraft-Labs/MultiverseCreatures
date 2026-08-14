# 🎵 Música

El plugin incluye su propio **motor de música NBS (Open Note Block Studio)** además de un conjunto de canciones incluidas. Las canciones viven en `plugins/MultiverseCreatures/music/` como archivos `.nbs` y se pueden reproducir a demanda con `/msc music play <canción> [loop]`.

## 📀 Discos de música y el Disc Trader

Cada canción incluida tiene un **disco de música** equivalente. Los discos se comportan exactamente igual que los discos vanilla — su única función es insertarse en un **jukebox**:

1. Click derecho en un jukebox con un disco → la canción empieza a sonar (motor NBS, radio de 32 bloques, en bucle).
2. Click derecho en el jukebox de nuevo **con la mano vacía** → el disco sale y la canción se detiene.
3. Romper el jukebox también detiene la canción; el disco cae con normalidad.

Obtén un disco con `/msc music disc <canción>` o cómpralos al **Disc Trader**:

- Un **aldeano bibliotecario** llamado `Disc Trader`, invocable con `/msc spawn disctrader` o encontrable de forma natural: el **5%** de los aldeanos spawnados naturalmente se convierten en uno (`disc-trader.spawn-chance` en `config.yml`, por defecto `0.05`, escalado por `general.spawn-rate-multiplier`).
- Vende **un disco por canción cargada** (16 esmeraldas cada uno) a través de la GUI de comercio estándar de aldeanos.

## 📻 Canciones incluidas

| Archivo | Crédito |
|---|---|
| `Birdbrain.nbs` | "Birdbrain" de IndyGirlfriend está licenciada bajo CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/o6QNLePeEN |
| `Bohemian Rhapsody.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |
| `DECO_27 -- ???.nbs` | "DECO*27 -- ???" de dotCrhinos está licenciada bajo CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/Zu8YzlyBLi |
| `Ievan Polkka - Hatsune Miku.nbs` | "Ievan Polkka - Hatsune Miku" de AyaSapphire está licenciada bajo CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/3t6Sk2kdMA |
| `Mesmerizer (???????).nbs` | "Mesmerizer (???????)" de ripestarvn. Todos los derechos reservados. https://noteblock.world/song/Sf4kcdC3nj |
| `one last kiss.nbs` | "one last kiss." de XinYueLovely está licenciada bajo CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/Pc6S1voDI8 |
| `Triple Baka (Ft. Hatsune Miku, Kasane Teto & Akita Neru).nbs` | "Triple Baka (Ft. Hatsune Miku, Kasane Teto & Akita Neru)" de posvendasgerente está licenciada bajo CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/CX755xs78z |
| `Undertale-Megalovania.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |
| `Undertale-Bonetrousle.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |
| `Turkish_March.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |
| `linkin_park-burn_it_down.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |
| `Giorno_Giovana_theme.nbs` | Autor desconocido<br>Fuente: https://github.com/nickg2/NBSsongs |

Los créditos siguen los archivos de autor incluidos en la carpeta `Authors` del paquete de canciones. Las canciones sin archivo de autor se acreditan como **Autor desconocido** con fuente [NBSsongs](https://github.com/nickg2/NBSsongs).