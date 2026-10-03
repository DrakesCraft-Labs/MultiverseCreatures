"""
Turns the eleven head skins of a model into textures Minecraft can show, and writes the model the
plugin loads.

A player head only shows a skin that Mojang hosts, so every head skin made by split.py or
paint.py is uploaded once through MineSkin (https://mineskin.org), which returns its texture.
The textures are cached in out/<name>/textures.json, so only new or changed skins are uploaded
again. When all eleven of a model are ready, the model is written to
src/main/resources/stands/<name>.txt, in the same format as a BDEngine export, which the plugin
reads like The World's.

    set MINESKIN_API_KEY=...            (a free key from https://account.mineskin.org)
    python tools/stand-skins/mineskin.py star-platinum killer-queen crazy-diamond dio-brando

The skins are uploaded as unlisted.
"""

import hashlib
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid
from pathlib import Path

HERE = Path(__file__).parent
OUT = HERE / "out"
MODELS = HERE.parent.parent / "src" / "main" / "resources" / "stands"
API = "https://api.mineskin.org/v2/generate"
AGENT = "MultiverseCreatures-StandSkins/1.0"

# The canonical eleven-head humanoid, in the frame BDEngine exports in (it looks towards -z, its
# right hand on +x): where the top of each head sits and its scale. Same order as split.py.
S = 0.937
H = S / 2
LAYOUT = [
    ((0.0, 4 * H, 0.0), (S, S, S)),            # head
    ((0.0, 3 * H, 0.0), (S, H, H)),            # chest
    ((0.0, 2.5 * H, 0.0), (S, S, H)),          # belly
    ((0.75 * H, 3 * H, 0.0), (H, H, H)),       # right upper arm
    ((0.75 * H, 2.5 * H, 0.0), (H, S, H)),     # right forearm
    ((-0.75 * H, 3 * H, 0.0), (H, H, H)),      # left upper arm
    ((-0.75 * H, 2.5 * H, 0.0), (H, S, H)),    # left forearm
    ((0.25 * H, 1.5 * H, 0.0), (H, H, H)),     # right thigh
    ((0.25 * H, H, 0.0), (H, S, H)),           # right shin
    ((-0.25 * H, 1.5 * H, 0.0), (H, H, H)),    # left thigh
    ((-0.25 * H, H, 0.0), (H, S, H)),          # left shin
]


def multipart(fields, file_name, data):
    boundary = uuid.uuid4().hex
    body = bytearray()
    for key, value in fields.items():
        body += f"--{boundary}\r\nContent-Disposition: form-data; name=\"{key}\"\r\n\r\n{value}\r\n".encode()
    body += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{file_name}\"\r\n"
             "Content-Type: image/png\r\n\r\n").encode()
    body += data + f"\r\n--{boundary}--\r\n".encode()
    return bytes(body), f"multipart/form-data; boundary={boundary}"


def upload(key, path, label):
    body, content_type = multipart({"variant": "classic", "visibility": "unlisted", "name": label[:20]},
                                   path.name, path.read_bytes())
    request = urllib.request.Request(API, data=body, method="POST", headers={
        "Authorization": f"Bearer {key}", "User-Agent": AGENT, "Content-Type": content_type,
        "Accept": "application/json"})
    while True:
        try:
            with urllib.request.urlopen(request, timeout=120) as response:
                reply = json.loads(response.read())
            break
        except urllib.error.HTTPError as error:
            text = error.read().decode(errors="replace")
            if error.code == 429:
                wait = 5.0
                try:
                    wait = max(1.0, json.loads(text)["rateLimit"]["next"]["relative"] / 1000)
                except (KeyError, ValueError, TypeError):
                    pass
                print(f"  rate limited, waiting {wait:.1f} s")
                time.sleep(wait)
                continue
            raise SystemExit(f"MineSkin refused {path.name}: HTTP {error.code} {text}")
    value = reply["skin"]["texture"]["data"]["value"]
    delay = reply.get("rateLimit", {}).get("next", {}).get("relative", 0) / 1000
    return value, delay


def build(name, key):
    folder = OUT / name
    files = sorted(folder.glob("[0-9][0-9]_*.png"))
    if len(files) != 11:
        raise SystemExit(f"{name}: expected 11 head skins in {folder}, found {len(files)}")
    cache_path = folder / "textures.json"
    cache = json.loads(cache_path.read_text()) if cache_path.exists() else {}
    values = []
    for index, path in enumerate(files):
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        entry = cache.get(path.name)
        if entry and entry["sha256"] == digest:
            values.append(entry["value"])
            continue
        print(f"{name}: uploading {path.name}")
        value, delay = upload(key, path, f"msc-{name}-{index}")
        cache[path.name] = {"sha256": digest, "value": value}
        cache_path.write_text(json.dumps(cache, indent=2))
        values.append(value)
        time.sleep(max(delay, 1.0))
    write_model(name, values)


def write_model(name, values):
    passengers = []
    for value, ((x, y, z), (sx, sy, sz)) in zip(values, LAYOUT):
        matrix = [sx, 0, 0, x, 0, sy, 0, y, 0, 0, sz, z, 0, 0, 0, 1]
        numbers = ",".join(f"{m:.10g}f" for m in matrix)
        passengers.append('{id:"minecraft:item_display",item:{id:"minecraft:player_head",Count:1,components:'
                          '{"minecraft:profile":{properties:[{name:"textures",value:"' + value + '"}]}}},'
                          'item_display:"none",transformation:[' + numbers + ']}')
    MODELS.mkdir(parents=True, exist_ok=True)
    target = MODELS / f"{name}.txt"
    target.write_text("/summon block_display ~-0.5 ~-0.5 ~-0.5 {Passengers:[" + ",".join(passengers) + "]}\n",
                      encoding="utf-8")
    print(f"{name}: model written to {target}")


def main():
    key = os.environ.get("MINESKIN_API_KEY")
    if not key:
        raise SystemExit("Set MINESKIN_API_KEY to a MineSkin API key first.")
    names = sys.argv[1:] or [p.name for p in sorted(OUT.iterdir()) if p.is_dir()]
    for name in names:
        build(name, key)


if __name__ == "__main__":
    main()
