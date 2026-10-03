"""
Cuts a full player skin into the eleven head skins of the plugin's head model.

The eleven-head humanoid is a player model cut in pieces, one pixel of skin being 1/16 of a head
of scale 0.937:

    head      the skin's head, as it is
    chest     the top 4 rows of the body          belly     its bottom 8 rows
    upper arm the top 4 rows of an arm            forearm   its bottom 8 rows
    thigh     the top 4 rows of a leg             shin      its bottom 8 rows

A head skin has six 8x8 faces, so every face of a piece is stretched to 8x8; the piece is drawn
squashed by the same amount (the chest is half as tall as the head), which puts every pixel back
where the skin had it. The outer layer (jacket, sleeves, trousers) becomes the head's outer
layer.

    python tools/stand-skins/split.py      reads tools/stand-skins/source/<name>.png
                                           writes tools/stand-skins/out/<name>/<nn>_<piece>.png
"""

import json
from pathlib import Path
from PIL import Image

HERE = Path(__file__).parent
SOURCE = HERE / "source"
OUT = HERE / "out"

PIECES = ["head", "chest", "belly", "arm_r_upper", "arm_r_lower", "arm_l_upper", "arm_l_lower",
          "leg_r_upper", "leg_r_lower", "leg_l_upper", "leg_l_lower"]

# Where each face of a head skin goes: base layer, and the outer layer 32 pixels to the right.
HEAD_FACES = {"right": (0, 8), "front": (8, 8), "left": (16, 8), "back": (24, 8), "top": (8, 0), "bottom": (16, 0)}

# Each box of the player model: its corner in the skin and its size in pixels (width, height, depth).
# The faces of a box sit around its corner the way Minecraft lays every box out:
#   top (d, 0, w, d), bottom (d + w, 0, w, d),
#   right (0, d, d, h), front (d, d, w, h), left (d + w, d, d, h), back (2d + w, d, w, h)
BOXES = {
    "body": ((16, 16), (8, 12, 4), (16, 32)),
    "arm_r": ((40, 16), (4, 12, 4), (40, 32)),
    "arm_l": ((32, 48), (4, 12, 4), (48, 48)),
    "leg_r": ((0, 16), (4, 12, 4), (0, 32)),
    "leg_l": ((16, 48), (4, 12, 4), (0, 48)),
}


def faces(skin, corner, size):
    """The six faces of a box as images, keyed by name."""
    (x, y), (w, h, d) = corner, size
    rect = {"top": (x + d, y, w, d), "bottom": (x + d + w, y, w, d),
            "right": (x, y + d, d, h), "front": (x + d, y + d, w, h),
            "left": (x + d + w, y + d, d, h), "back": (x + 2 * d + w, y + d, w, h)}
    return {name: skin.crop((rx, ry, rx + rw, ry + rh)) for name, (rx, ry, rw, rh) in rect.items()}


def rows(face, start, count):
    return face.crop((0, start, face.width, start + count))


def piece_faces(box, upper):
    """The faces of the upper (4 rows) or lower (8 rows) segment of a box."""
    start, count = (0, 4) if upper else (4, 8)
    cut = {name: rows(box[name], start, count) for name in ("right", "front", "left", "back")}
    # The top of the upper segment is the box's top; the bottom of the lower one its bottom. The
    # faces where the two segments meet are never seen; they repeat the row next to them.
    cut["top"] = box["top"] if upper else rows(box["front"], 4, 1)
    cut["bottom"] = rows(box["front"], 3, 1) if upper else box["bottom"]
    return cut


def head_skin(base, outer):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for name, (fx, fy) in HEAD_FACES.items():
        img.paste(base[name].resize((8, 8), Image.NEAREST), (fx, fy))
        if outer is not None:
            layer = outer[name].resize((8, 8), Image.NEAREST)
            img.paste(layer, (fx + 32, fy), layer)
    return img


def split(skin):
    """The eleven head skins of a player skin, in the plugin's piece order."""
    skin = skin.convert("RGBA")
    head = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    head.paste(skin.crop((0, 0, 64, 16)), (0, 0))
    out = {"head": head}
    for name, (corner, size, outer_corner) in BOXES.items():
        base = faces(skin, corner, size)
        outer = faces(skin, outer_corner, size)
        for upper, suffix in ((True, "upper"), (False, "lower")):
            key = {"body": "chest" if upper else "belly"}.get(name, f"{name}_{suffix}")
            out[key] = head_skin(piece_faces(base, upper), piece_faces(outer, upper))
    return [out[piece] for piece in PIECES]


def main():
    manifest_path = OUT / "manifest.json"
    manifest = json.loads(manifest_path.read_text()) if manifest_path.exists() else {}
    for source in sorted(SOURCE.glob("*.png")):
        name = source.stem
        folder = OUT / name
        folder.mkdir(parents=True, exist_ok=True)
        files = []
        for index, (piece, img) in enumerate(zip(PIECES, split(Image.open(source)))):
            path = folder / f"{index:02d}_{piece}.png"
            img.save(path)
            files.append(f"{name}/{path.name}")
        manifest[name] = files
        print("split", name)
    manifest_path.write_text(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
