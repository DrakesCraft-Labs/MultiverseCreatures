# Stand skins → head models

The Stands and DIO are drawn in game with eleven textured player heads (the BDEngine humanoid:
head, chest, belly, upper arms, forearms, thighs, shins). This folder turns player skins into
those models.

| Step | Command | What it does |
|---|---|---|
| 1 | `python tools/stand-skins/split.py` | Cuts every full skin in `source/<name>.png` into its 11 head skins, in `out/<name>/` |
| (1b) | `python tools/stand-skins/paint.py` | Paints head skins by hand-written pixel art, for a Stand with no skin (Magician's Red) |
| 2 | `mvn test` then open `tools/stand-viewer/` | Preview every model in 3D, with the real skins ("Painted skins") |
| 3 | `MINESKIN_API_KEY=... python tools/stand-skins/mineskin.py <name>...` | Uploads the head skins to MineSkin (unlisted) and writes `src/main/resources/stands/<name>.txt` |

The `<name>` is a Stand key (`star-platinum`, `killer-queen`, `crazy-diamond`, `magicians-red`) or
`dio-brando` for DIO himself. A model file can also be a BDEngine `/summon` export pasted as it is;
servers can override any of them in `plugins/MultiverseCreatures/stands/`.

To run the viewer: `python -m http.server 8765` from the repository root, then open
`http://localhost:8765/tools/stand-viewer/`.
