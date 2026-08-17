# 🎵 Music

The plugin ships with its own **NBS (Open Note Block Studio) music engine** plus a set of bundled songs. Songs live in `plugins/MultiverseCreatures/music/` as `.nbs` files and can be played on demand with `/msc music play <song> [loop]`.

## 📀 Music discs & the Disc Trader

Every bundled song has a matching **music disc**. Discs behave exactly like vanilla discs — their only function is to be inserted into a **jukebox**:

1. Right-click a jukebox holding a disc → the song starts playing (NBS engine, 32-block radius, loops).
2. Right-click the jukebox again with an **empty hand** → the disc pops out and the song stops.
3. Breaking the jukebox also stops the song; the disc drops normally.

Get a disc with `/msc music disc <song>` or buy them from the **Disc Trader**:

- A **librarian villager** named `Disc Trader`, spawnable with `/msc spawn disctrader` or found naturally: **5%** of naturally spawned villagers become one (`disc-trader.spawn-chance` in `config.yml`, default `0.05`, scaled by `general.spawn-rate-multiplier`).
- Sells **one disc per loaded song** (16 emeralds each) through the standard villager trading GUI.

## 📻 Bundled songs

| File | Credit |
|---|---|
| `Birdbrain.nbs` | "Birdbrain" by IndyGirlfriend is licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/o6QNLePeEN |
| `Bohemian Rhapsody.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |
| `DECO_27 -- ???.nbs` | "DECO*27 -- ???" by dotCrhinos is licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/Zu8YzlyBLi |
| `Ievan Polkka - Hatsune Miku.nbs` | "Ievan Polkka - Hatsune Miku" by AyaSapphire is licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/3t6Sk2kdMA |
| `Mesmerizer (???????).nbs` | "Mesmerizer (???????)" by ripestarvn. All rights reserved. https://noteblock.world/song/Sf4kcdC3nj |
| `one last kiss.nbs` | "one last kiss." by XinYueLovely is licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/Pc6S1voDI8 |
| `Triple Baka (Ft. Hatsune Miku, Kasane Teto & Akita Neru).nbs` | "Triple Baka (Ft. Hatsune Miku, Kasane Teto & Akita Neru)" by posvendasgerente is licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0) https://noteblock.world/song/CX755xs78z |
| `Undertale-Megalovania.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |
| `Undertale-Bonetrousle.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |
| `Turkish_March.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |
| `linkin_park-burn_it_down.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |
| `Giorno_Giovana_theme.nbs` | Unknown author<br>Source: https://github.com/nickg2/NBSsongs |

Credits follow the author files shipped inside the `Authors` folder of the song bundle. Songs without an author file are credited as **Unknown author** with source [NBSsongs](https://github.com/nickg2/NBSsongs).