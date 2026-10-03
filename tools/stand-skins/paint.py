"""
Paints the player-head skins of every Stand for the eleven-head model (the same skeleton as The
World's BDEngine export): head, chest, belly, upper arms, forearms, thighs and shins.

Each head uses the top 16 rows of a 64x64 skin: six 8x8 faces (right, front, left, back on row 8,
top and bottom on row 0) and the same six again, from x = 32, as the outer layer that floats a
little above the head and gives depth (hair, pads, buckles).

Faces are written as 8 strings of 8 characters. A character is a colour of the Stand's palette:
1-5 are its body from darkest to lightest, a b c its first accent (pads, bands), p q r its second
(cloth, plates), u v the aura its shins fade into, and the rest are named per Stand. '.' is
transparent (outer layer only).

    python tools/stand-skins/paint.py      writes tools/stand-skins/out/<stand>/<nn>_<piece>.png
"""

from pathlib import Path
from PIL import Image

OUT = Path(__file__).parent / "out"

# Order of the pieces in the plugin's HeadModel: head, chest, belly, right arm (upper, fore),
# left arm (upper, fore), right leg (thigh, shin), left leg (thigh, shin).
PIECES = ["head", "chest", "belly", "arm_r_upper", "arm_r_lower", "arm_l_upper", "arm_l_lower",
          "leg_r_upper", "leg_r_lower", "leg_l_upper", "leg_l_lower"]

FACES = {"right": (0, 8), "front": (8, 8), "left": (16, 8), "back": (24, 8), "top": (8, 0), "bottom": (16, 0)}

BLANK = ["........"] * 8


def fill(c):
    return [c * 8] * 8


def mirror(face):
    return [row[::-1] for row in face]


def shaded(edge, mid, light=None):
    """A plain side: darker edges, lighter middle."""
    light = light or mid
    return [edge + mid * 2 + light * 2 + mid * 2 + edge] * 8


# ------------------------------------------------------------------ shared anatomy

def body_template():
    """The athletic frame every Stand shares. Stands override what makes them themselves."""
    side = shaded("2", "3", "4")
    t = {}
    t["chest"] = {
        "front": ["23444432",
                  "34555543",
                  "45552555",
                  "45542455",
                  "34422443",
                  "22322322",
                  "33344333",
                  "23344332"],
        "back": ["23333332",
                 "33433433",
                 "34433443",
                 "33433433",
                 "23333332",
                 "22333322",
                 "23333332",
                 "22222222"],
        "right": side, "left": side,
        "top": ["23444432"] * 8, "bottom": fill("2"),
    }
    t["belly"] = {
        "front": ["23542452",
                  "24432442",
                  "23542452",
                  "24432442",
                  "23542452",
                  "abccccba",
                  "aaaaaaaa",
                  "pqrrrrqp"],
        "back": ["23333332",
                 "23344332",
                 "23333332",
                 "22333322",
                 "23333332",
                 "abbbbbba",
                 "aaaaaaaa",
                 "ppqqqqpp"],
        "right": ["23443332"] * 5 + ["abbbbbba", "aaaaaaaa", "pqqqqqqp"],
        "left": ["23334432"] * 5 + ["abbbbbba", "aaaaaaaa", "pqqqqqqp"],
        "top": fill("3"), "bottom": fill("p"),
    }
    upper_arm = {
        "front": ["34444443", "34555543", "34544543", "33444433", "23444432", "23344332", "22333322", "22333322"],
        "back": ["23333332", "23444332", "23333332", "22333322", "22333322", "22333322", "22222222", "12222221"],
        "right": side, "left": side, "top": fill("4"), "bottom": fill("2"),
    }
    forearm = {
        "front": ["23444432", "23455432", "23444432", "22344322", "abccccba", "aaaaaaaa", "34545453", "23333332"],
        "back": ["22333322", "23333332", "22333322", "22333322", "abbbbbba", "aaaaaaaa", "33333333", "22222222"],
        "right": ["22333322"] * 4 + ["abbbbbba", "aaaaaaaa", "33444433", "22333322"],
        "left": ["22333322"] * 4 + ["abbbbbba", "aaaaaaaa", "33444433", "22333322"],
        "top": fill("3"), "bottom": ["33333333", "34444443", "34444443", "34444443",
                                     "34444443", "34444443", "34444443", "33333333"],
    }
    thigh = {
        "front": ["23444432", "23455432", "23455432", "23444432", "23444432", "22344322", "22333322", "12333321"],
        "back": ["22333322", "23333332", "23333332", "22333322", "22333322", "22333322", "22222222", "12222221"],
        "right": side, "left": side, "top": fill("3"), "bottom": fill("2"),
    }
    shin = {
        "front": ["abccccba", "aabbbbaa", "23444432", "23444432", "u334433u", "uv3333vu", "uvvvvvvu", "uuuuuuuu"],
        "back": ["22333322", "22333322", "23333332", "23333332", "u233332u", "uv2222vu", "uvvvvvvu", "uuuuuuuu"],
        "right": ["aabbbbaa", "a222222a", "22333322", "22333322", "u233332u", "uv2222vu", "uvvvvvvu", "uuuuuuuu"],
        "left": ["aabbbbaa", "a222222a", "22333322", "22333322", "u233332u", "uv2222vu", "uvvvvvvu", "uuuuuuuu"],
        "top": fill("3"), "bottom": fill("u"),
    }
    t["arm_r_upper"] = upper_arm
    t["arm_l_upper"] = mirrored(upper_arm)
    t["arm_r_lower"] = forearm
    t["arm_l_lower"] = mirrored(forearm)
    t["leg_r_upper"] = thigh
    t["leg_l_upper"] = mirrored(thigh)
    t["leg_r_lower"] = shin
    t["leg_l_lower"] = mirrored(shin)
    # Outer layers: shoulder guards over the upper arms, a rim on each band and knee guard.
    pad = {"top": ["bccccccb", "cccccccc", "cccccccc", "cccccccc", "cccccccc", "cccccccc", "cccccccc", "bccccccb"],
           "front": ["bccccccb", "abbbbbba", "aaaaaaaa"] + ["........"] * 5,
           "back": ["bccccccb", "abbbbbba", "aaaaaaaa"] + ["........"] * 5,
           "right": ["bccccccb", "abbbbbba", "aaaaaaaa"] + ["........"] * 5,
           "left": ["bccccccb", "abbbbbba", "aaaaaaaa"] + ["........"] * 5}
    t["outer"] = {"arm_r_upper": pad, "arm_l_upper": mirrored(pad),
                  "arm_r_lower": band(4), "arm_l_lower": band(4),
                  "leg_r_lower": band(0), "leg_l_lower": band(0)}
    return t


def band(row):
    """A rim floating over one row of every side: the edge of a bracer or a knee guard."""
    face = ["........"] * 8
    face[row] = "bccccccb"
    return {"front": face, "back": face, "right": face, "left": face}


def mirrored(piece):
    out = {}
    for name, face in piece.items():
        if name in ("right", "left"):
            out["left" if name == "right" else "right"] = mirror(face)
        else:
            out[name] = mirror(face)
    return out


def overlay(base, extra):
    """Writes the non '.' characters of extra over base."""
    return ["".join(e if e != "." else b for b, e in zip(rb, re)) for rb, re in zip(base, extra)]


# ------------------------------------------------------------------ the Stands

STANDS = {}


def star_platinum():
    palette = {"1": "3a2f63", "2": "4f4383", "3": "6a5ca6", "4": "8577c2", "5": "a497dc",
               "a": "9c6a12", "b": "d39b22", "c": "f2c84b", "p": "6e1218", "q": "9b1d24", "r": "c52f35",
               "u": "2e2152", "v": "4b3a86", "k": "0e0b14", "h": "2a2238", "w": "f4f4f4", "e": "2fd0e0"}
    t = body_template()
    t["head"] = {
        "front": ["kkkkkkkk",
                  "kbccccbk",
                  "k444444k",
                  "4kk44kk4",
                  "4we44ew4",
                  "34444443",
                  "33322333",
                  "23333332"],
        "right": ["kkkkkkkk", "kkkkbccb", "kkkk4444", "kkk43444", "kkk43344", "kkk44444", "kkkk3444", "kkkk3333"],
        "back": ["kkhkkhkk", "kbbbbbbk", "khkkkhkk", "kkkhkkkk", "khkkkkhk", "kkkhkkhk", "khkkhkkk", "kkkkkkkk"],
        "top": ["kkhkkhkk", "khkkkkhk", "kkkhhkkk", "khkkkkhk", "kkkhkkkk", "khkkhkkk", "kkkkkkhk", "kkhkkkkk"],
        "bottom": fill("3"),
    }
    t["head"]["left"] = mirror(t["head"]["right"])
    t["outer"]["head"] = {
        "front": ["kkkkkkkk", "k......k"] + ["........"] * 6,
        "top": ["kkkkkkkk", "kkhkkhkk", "kkkkkkkk", "khkkkkhk", "kkkkkkkk", "kkkhkkkk", "kkkkkkkk", "kkkkkkkk"],
        "back": ["kkkkkkkk", "khkkkkhk", "kkkkkkkk", "kkkhkkkk", "kkkkkkkk", "khkkhkkk", "k.kk.kk.", ".k..k..k"],
        "right": ["kkkkkkkk", "kkkkk...", "kkkk....", "kkk.....", "kkk.....", "kkk.....", "kk......", "k......."],
        "left": mirror(["kkkkkkkk", "kkkkk...", "kkkk....", "kkk.....", "kkk.....", "kkk.....", "kk......", "k......."]),
    }
    # The red scarf round the neck and its tail down the back.
    t["chest"]["front"][0] = "2rrqqrr2"
    t["chest"]["front"][1] = "3qrrrrq3"
    t["chest"]["back"][0] = "rrrrrrrr"
    t["chest"]["back"][1] = "23rqqr32"
    t["chest"]["back"][2] = "34rqqr43"
    t["chest"]["top"] = ["2rrrrrr2"] * 8
    t["outer"]["chest"] = {"front": ["..rrrr..", "...qq..."] + ["........"] * 6,
                           "back": ["rrrrrrrr", "..rqqr..", "..qrrq..", "...qq..."] + ["........"] * 4}
    # Gold belt, red loincloth.
    t["belly"]["front"][7] = "33rqqr33"
    t["outer"]["belly"] = {"front": ["........"] * 5 + ["...cc...", "..rrrr..", "..qrrq.."],
                           "back": ["........"] * 6 + ["..pqqp..", "..pqqp.."]}
    return palette, t


def magicians_red():
    palette = {"1": "4a170f", "2": "6d2414", "3": "8f331a", "4": "b44a22", "5": "d3682e",
               "a": "8a5a10", "b": "c9922a", "c": "f0c050", "p": "6d1a0a", "q": "a83212", "r": "e0561c",
               "u": "5a1206", "v": "a8300c", "k": "1a0a06", "e": "f7c51f", "y": "e3a72f", "Y": "b07818",
               "f": "ffb43a", "F": "ff6a10"}
    t = body_template()
    t["head"] = {
        "front": ["rrqrrqrr",
                  "q455554q",
                  "3kk44kk3",
                  "3ee44ee3",
                  "34yyyy43",
                  "3yYccYy3",
                  "33yYYy33",
                  "233YY332"],
        "right": ["qrrrqrrq", "rrqq4554", "rq334444", "qq334ek4", "q3333444", "33333444", "33333344", "22233333"],
        "back": ["rqrrqrrq", "qrrqqrrq", "rqrrrrqr", "qrqrrqrq", "rrqrrqrr", "qrrqqrrq", "3qrrrrq3", "23qrrq32"],
        "top": ["rrqrrqrr", "rqrrrrqr", "qrrqqrrq", "rrqrrqrr", "rqrrrrqr", "qrrqqrrq", "rrqrrqrr", "rqrrrrqr"],
        "bottom": fill("2"),
    }
    t["head"]["left"] = mirror(t["head"]["right"])
    t["outer"]["head"] = {
        "front": ["rr.rr.rr", "........", "........", "........", "..yyyy..", ".yYYYYy.", "..yYYy..", "...YY..."],
        "top": ["rrrrrrrr", "rqrrrrqr", "rrqrrqrr", "rrrrrrrr", "rqrrrrqr", "rrrrrrrr", "rrqrrqrr", "rrrrrrrr"],
        "back": ["rrrrrrrr", "qrrrrrrq", "rrqrrqrr", "qrrrrrrq", "..rqqr..", "..qrrq..", "...rr...", "...qq..."],
        "right": ["rrrrrr..", "rrq.....", "rq......", "q.......", "........", "........", "........", "........"],
        "left": mirror(["rrrrrr..", "rrq.....", "rq......", "q.......", "........", "........", "........", "........"]),
    }
    # The golden ankh on a feathered chest.
    t["chest"]["front"][0] = "rqrrrrqr"
    t["outer"]["chest"] = {"front": ["........", "...cc...", "..c..c..", "...cc...", ".cccccc.", "...cc...",
                                     "...bb...", "........"]}
    # Flames round the wrists instead of bands, a skirt of feathers.
    for side in ("arm_r_lower", "arm_l_lower"):
        for face in ("front", "back", "right", "left"):
            t[side][face][4] = "FfFffFfF"
            t[side][face][5] = "fFffFFfF"
        t["outer"][side] = {f: ["........"] * 3 + ["F.f..F.f", "fFfFFfFf", ".f..f..F"] + ["........"] * 2
                            for f in ("front", "back", "right", "left")}
    t["belly"]["front"][7] = "rqrrrrqr"
    t["outer"]["belly"] = {"front": ["........"] * 6 + ["rqr..rqr", "q.rqqr.q"],
                           "back": ["........"] * 6 + ["rqrrrrqr", "q.rqqr.q"],
                           "right": ["........"] * 6 + ["rqrrrrqr", "q.r..r.q"],
                           "left": ["........"] * 6 + ["rqrrrrqr", "q.r..r.q"]}
    # Feathers on the shoulders instead of gold guards.
    feathers = {"top": fill("r"), "front": ["rqrrqrrq", "qrrqqrrq", "r.qr.rq."] + ["........"] * 5}
    feathers["back"] = feathers["front"]
    feathers["right"] = ["rqrrqrrq", "qrrqqrrq", "rrqrrqrr", "q.rq.rq.", ".q..q..q"] + ["........"] * 3
    feathers["left"] = feathers["right"]
    t["outer"]["arm_r_upper"] = feathers
    t["outer"]["arm_l_upper"] = mirrored(feathers)
    return palette, t


def crazy_diamond():
    palette = {"1": "5a1f3d", "2": "8a2f5c", "3": "b84a7f", "4": "dc6fa0", "5": "f39cc3",
               "a": "1b5f9c", "b": "2f8fd6", "c": "79c6ff", "p": "8a8f96", "q": "c4c9cf", "r": "f1f4f6",
               "u": "6a2347", "v": "9a3a6a", "k": "120a10", "w": "ffffff", "e": "3fb6ff", "x": "7fe8ff", "X": "d0fbff"}
    t = body_template()
    t["head"] = {
        "front": ["qrrrrrrq",
                  "qrrbbrrq",
                  "4qr44rq4",
                  "4kk44kk4",
                  "4we44ew4",
                  "q444444q",
                  "rrbqqbrr",
                  "qrrrrrrq"],
        "right": ["qrrrrrrq", "qrrrrrqq", "qqrr4444", "qqr43444", "qqr43344", "qqr44444", "bbbbbbbr", "qqrrrrrr"],
        "back": ["qrrrrrrq", "rrrrrrrr", "rqrbbrqr", "rrbbbbrr", "rqrbbrqr", "rrrrrrrr", "bbbbbbbb", "qrrrrrrq"],
        "top": ["qrrr4rrq", "rrrr4rrr", "rrr44rrr", "rrrr4rrr", "rrrr4rrr", "rrr44rrr", "rrrr4rrr", "qrrr4rrq"],
        "bottom": fill("q"),
    }
    t["head"]["left"] = mirror(t["head"]["right"])
    t["outer"]["head"] = {
        "top": ["...44...", "...45...", "...44...", "...45...", "...44...", "...45...", "...44...", "...44..."],
        "front": ["...44...", "........", "........", "........", "........", "........", ".b....b.", "........"],
    }
    # The diamond on the chest, the hearts on the shoulders, white plates on the belly.
    t["outer"]["chest"] = {"front": ["........", "........", "...XX...", "..XxxX..", "..xxxx..", "...xx...",
                                     "........", "........"]}
    heart = ["........", ".bb..bb.", "bccbbccb", "bccccccb", ".bccccb.", "..bccb..", "...bb...", "........"]
    t["outer"]["arm_r_upper"]["front"] = heart
    t["outer"]["arm_l_upper"]["front"] = heart
    t["outer"]["belly"] = {"front": ["........"] * 5 + ["...bb...", "..bccb..", "...bb..."]}
    t["outer"]["leg_r_lower"]["front"] = [".b....b.", "bcb..bcb", ".b....b.", "........", "........",
                                          "........", "........", "........"]
    t["outer"]["leg_l_lower"]["front"] = t["outer"]["leg_r_lower"]["front"]
    return palette, t


def killer_queen():
    palette = {"1": "6a4a52", "2": "9a7a82", "3": "c4a4aa", "4": "e2c6c6", "5": "f6e2e0",
               "a": "4a2a40", "b": "6e3c5c", "c": "96557e", "p": "9a3a6a", "q": "c45a8c", "r": "e888b6",
               "u": "5a2a5a", "v": "8a4a86", "k": "120a10", "w": "ffffff", "e": "121212",
               "s": "f4f0e8", "S": "a8a29a", "m": "c8ccd2"}
    t = body_template()
    t["head"] = {
        "front": ["444bb444",
                  "4q4bb4q4",
                  "44444444",
                  "4bb44bb4",
                  "4we44ew4",
                  "34444443",
                  "334bb433",
                  "23333332"],
        "right": ["44444444", "4444q444", "44444444", "43444444", "433444bb", "44444444", "34444444", "33333333"],
        "back": ["444bb444", "444bb444", "44444444", "444bb444", "44444444", "444bb444", "34444443", "33333333"],
        "top": ["444bb444", "444bb444", "444bb444", "444bb444", "444bb444", "444bb444", "444bb444", "444bb444"],
        "bottom": fill("3"),
    }
    t["head"]["left"] = mirror(t["head"]["right"])
    # Cat ears on the corners of the skull.
    t["outer"]["head"] = {
        "front": ["4r....r4", "44....44"] + ["........"] * 6,
        "top": ["44....44", "4r....r4"] + ["........"] * 6,
        "right": ["......44", "......4r"] + ["........"] * 6,
        "left": ["44......", "r4......"] + ["........"] * 6,
    }
    # Pink shoulder guards studded with steel, a skull buckle, the skull on the right hand.
    studs = ["bmccmccmb"[:8], "abbbbbba", "aaaaaaaa"] + ["........"] * 5
    for side in ("arm_r_upper", "arm_l_upper"):
        pad = t["outer"][side]
        for face in ("front", "back", "right", "left"):
            pad[face] = ["rmrrmrrm", "qrrrrrrq", "pqqqqqqp"] + ["........"] * 5
        pad["top"] = ["rrrrrrrr", "rmrrrrmr", "rrrrrrrr", "rrrmmrrr", "rrrmmrrr", "rrrrrrrr", "rmrrrrmr", "rrrrrrrr"]
    del studs
    t["chest"]["front"][2] = "35b55b53"
    t["chest"]["front"][3] = "345b4b43"
    t["outer"]["belly"] = {"front": ["........"] * 5 + ["...ss...", "..sSSs..", "...ss..."]}
    t["outer"]["arm_r_lower"]["front"] = ["........"] * 5 + ["..ss....", ".sSSs...", "..ss...."]
    for side in ("arm_r_lower", "arm_l_lower"):
        for face in ("front", "back", "right", "left"):
            t[side][face][4] = "ambmmbma"
    return palette, t


STANDS = {"star-platinum": star_platinum, "magicians-red": magicians_red,
          "crazy-diamond": crazy_diamond, "killer-queen": killer_queen}


# ------------------------------------------------------------------ painting

def hex_rgba(h):
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def paint_face(img, palette, origin, face, stand, piece, name):
    ox, oy = origin
    for y, row in enumerate(face):
        if len(row) != 8 or len(face) != 8:
            raise ValueError(f"{stand} {piece} {name}: faces are 8x8, row {y} is '{row}'")
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch not in palette:
                raise ValueError(f"{stand} {piece} {name}: '{ch}' is not in the palette")
            img.putpixel((ox + x, oy + y), hex_rgba(palette[ch]))


def paint(stand, maker):
    palette, t = maker()
    folder = OUT / stand
    folder.mkdir(parents=True, exist_ok=True)
    files = []
    for index, piece in enumerate(PIECES):
        img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        for name, origin in FACES.items():
            paint_face(img, palette, origin, t[piece][name], stand, piece, name)
            outer = t["outer"].get(piece, {}).get(name)
            if outer:
                paint_face(img, palette, (origin[0] + 32, origin[1]), outer, stand, piece, "outer " + name)
        path = folder / f"{index:02d}_{piece}.png"
        img.save(path)
        files.append(f"{stand}/{path.name}")
    return files


def main():
    import json
    manifest = {}
    for stand, maker in STANDS.items():
        manifest[stand] = paint(stand, maker)
    (OUT / "manifest.json").write_text(json.dumps(manifest, indent=2))
    print("painted", ", ".join(manifest))


if __name__ == "__main__":
    main()
