import json
import os
import random
import struct
import zlib

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "cultivar")
TEX = os.path.join(ROOT, "textures")
MODELS = os.path.join(ROOT, "models", "block")

SPECIES = {
    "wheatline": ((0x6E, 0x9A, 0x3C), (0xF0, 0xE8, 0xB0), (0xD8, 0xB8, 0x50)),
    "maize": ((0x5A, 0x9A, 0x38), (0xE8, 0xD8, 0x80), (0xE8, 0xC8, 0x38)),
    "soybean": ((0x6A, 0xA0, 0x48), (0xE8, 0xD8, 0xF0), (0x9C, 0xC0, 0x60)),
    "clover": ((0x48, 0x98, 0x40), (0xE0, 0xA0, 0xC8), (0x58, 0xA8, 0x48)),
    "tuber": ((0x5E, 0x8E, 0x3E), (0xC0, 0x90, 0xE0), (0x9A, 0x70, 0x4A)),
    "brassica": ((0x4A, 0x88, 0x80), (0xF0, 0xE0, 0x60), (0x5A, 0x98, 0x90)),
    "gourd": ((0x60, 0x90, 0x38), (0xF0, 0xC0, 0x40), (0xF0, 0xA0, 0x30)),
    "fiber_flax": ((0x6A, 0x96, 0x4C), (0x60, 0x80, 0xE0), (0xC8, 0xB8, 0x88)),
    "graft_union": ((0x58, 0x84, 0x3A), (0xE8, 0xD0, 0x90), (0xB0, 0x8A, 0x50)),
}


def save_png(path, pixels, width, height):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            r, g, b, a = pixels[y * width + x]
            raw.extend((r & 255, g & 255, b & 255, a & 255))

    def chunk(kind, data):
        c = struct.pack(">I", len(data)) + kind + data
        return c + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


class Canvas:
    def __init__(self, w=16, h=16, fill=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.p = [fill] * (w * h)

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            if len(c) == 3:
                c = (c[0], c[1], c[2], 255)
            self.p[y * self.w + x] = c

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def outline(self, x0, y0, x1, y1, c):
        for x in range(x0, x1 + 1):
            self.set(x, y0, c)
            self.set(x, y1, c)
        for y in range(y0, y1 + 1):
            self.set(x0, y, c)
            self.set(x1, y, c)

    def save(self, *parts):
        save_png(os.path.join(TEX, *parts) + ".png", self.p, self.w, self.h)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3])


def noise_fill(cv, base, var, rng, alpha=255):
    for y in range(cv.h):
        for x in range(cv.w):
            f = 1.0 + (rng.random() - 0.5) * var
            r, g, b = shade(base, f)
            cv.set(x, y, (r, g, b, alpha))


def crop_texture(name, leaf, flower, produce, stage):
    rng = random.Random(f"{name}:{stage}")
    cv = Canvas()
    height = [3, 5, 8, 10, 12, 13, 14][stage]
    stem = shade(leaf, 0.75)
    for sx in (3, 7, 11, 14) if stage >= 2 else (5, 10):
        top = 15 - height + rng.randint(0, 1)
        for y in range(top, 16):
            cv.set(sx, y, stem)
        for y in range(top + 1, 16, 3):
            side = -1 if (y + sx) % 2 else 1
            cv.set(sx + side, y, leaf)
            if stage >= 2:
                cv.set(sx + 2 * side, y - 1, shade(leaf, 1.15))
        if stage == 3:
            cv.set(sx, top - 1, shade(leaf, 1.3))
        if stage == 4:
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 0)):
                cv.set(sx + dx, top + dy, flower)
            cv.set(sx, top, (0xF8, 0xF0, 0x60))
        if stage == 5:
            cv.set(sx, top, shade(produce, 0.8))
            cv.set(sx + 1, top + 1, shade(produce, 0.7))
        if stage == 6:
            if name in ("gourd", "tuber"):
                gx = sx - 1
                cv.rect(gx, 13, gx + 2, 15, produce)
                cv.set(gx + 1, 12, shade(leaf, 0.7))
            else:
                cv.rect(sx - 1, top, sx + 1, top + 2, produce)
                cv.set(sx, top - 1, shade(produce, 1.15))
    return cv


def crops():
    for name, (leaf, flower, produce) in SPECIES.items():
        for stage in range(7):
            crop_texture(name, leaf, flower, produce, stage).save("block", f"{name}_stage{stage}")
    rng = random.Random(7)
    blight = Canvas()
    for i in range(46):
        x, y = rng.randint(0, 15), rng.randint(2, 15)
        blight.set(x, y, (0x5A + rng.randint(-10, 10), 0x4A, 0x2A, 255))
        if rng.random() < 0.4:
            blight.set(x + 1, y, (0xC8, 0xC0, 0xA8, 255))
    blight.save("block", "blight_marker")
    bag = Canvas()
    noise_fill(bag, (0xE8, 0xDC, 0xC0), 0.12, random.Random(8))
    for y in range(0, 16, 4):
        for x in range(16):
            bag.set(x, y, (0xC8, 0xB8, 0x98, 255))
    bag.save("block", "isolation_bag_overlay")


def blocks():
    import block_art
    return block_art.write_all(TEX)


def items():
    import item_art
    return item_art.write_all(TEX)


def entities():
    rng = random.Random(51)
    for name, base, accent in (("pollinator_drone", (0xE4, 0xE8, 0xE8), (0xF0, 0xC8, 0x20)),
                               ("harvester_cart", (0x8A, 0x64, 0x3C), (0x9A, 0xA2, 0xA8)),
                               ("soil_probe_rover", (0x9A, 0xA2, 0xA8), (0x40, 0x40, 0x44))):
        cv = Canvas(64, 32)
        noise_fill(cv, base, 0.08, rng)
        for x in range(0, 64, 8):
            for y in range(32):
                cv.set(x, y, (*shade(base, 0.7), 255))
        for y in range(0, 32, 8):
            for x in range(64):
                cv.set(x, y, (*shade(base, 0.7), 255))
        cv.rect(0, 14, 63, 20, (*accent, 255))
        cv.rect(40, 0, 63, 6, (*shade(accent, 0.8), 255))
        cv.save("entity", name)
    for name, color, alpha in (("spore_drift", (0x8A, 0x7A, 0x50), 255), ("mist_cloud", (0xE8, 0xF4, 0xFF), 255)):
        cv = Canvas(32, 32)
        for y in range(32):
            for x in range(32):
                d = ((x - 15.5) ** 2 + (y - 15.5) ** 2) ** 0.5
                if d < 15 and rng.random() < (1.0 - d / 15) * 0.9:
                    a = int(alpha * (1.0 - d / 15))
                    cv.set(x, y, (*shade(color, 0.9 + rng.random() * 0.2), a))
        cv.save("entity", name)
    jet = Canvas()
    for y in range(16):
        for x in range(16):
            if 5 <= x <= 10:
                cv_a = 200 if 6 <= x <= 9 else 110
                jet.set(x, y, (0x90, 0xC8, 0xF0, cv_a))
    jet.save("entity", "water_jet")


def particles():
    defs = {
        "pollen_mote": ((0xF8, 0xE0, 0x50), 2),
        "spore_speck": ((0x7A, 0x6A, 0x40), 2),
        "water_droplet": ((0x70, 0xB0, 0xF0), 2),
        "mist_wisp": ((0xF0, 0xF8, 0xFF), 3),
        "soil_crumb": ((0x6A, 0x48, 0x2C), 2),
    }
    for name, (color, size) in defs.items():
        for frame in range(2):
            cv = Canvas(8, 8)
            s = size + frame
            off = (8 - s) // 2
            cv.rect(off, off, off + s - 1, off + s - 1, (*color, 255))
            cv.set(off, off, (*shade(color, 1.2), 255))
            cv.save("particle", f"{name}_{frame}")


def write_model(name, data):
    os.makedirs(MODELS, exist_ok=True)
    with open(os.path.join(MODELS, name + ".json"), "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def plane_x(x, top, tex):
    return {"from": [x, -1, 0], "to": [x, top, 16], "shade": False,
            "faces": {"west": {"uv": [0, 0, 16, 16], "texture": tex}, "east": {"uv": [16, 0, 0, 16], "texture": tex}}}


def plane_z(z, top, tex):
    return {"from": [0, -1, z], "to": [16, top, z], "shade": False,
            "faces": {"north": {"uv": [0, 0, 16, 16], "texture": tex}, "south": {"uv": [16, 0, 0, 16], "texture": tex}}}


def crop_parent(vigor, density, tex="#crop"):
    top = [11, 15, 21][vigor]
    positions = {0: [8], 1: [4, 12], 2: [3, 8, 13]}[density]
    elements = [plane_x(p, top, tex) for p in positions] + [plane_z(p, top, tex) for p in positions]
    return elements


def models():
    for vigor in range(3):
        for density in range(3):
            write_model(f"crop_v{vigor}_d{density}", {
                "ambientocclusion": False,
                "textures": {"particle": "#crop"},
                "elements": crop_parent(vigor, density)})
        write_model(f"blight_marker_v{vigor}", {
            "ambientocclusion": False,
            "textures": {"particle": "cultivar:block/blight_marker", "crop": "cultivar:block/blight_marker"},
            "elements": crop_parent(vigor, 1)})
    box = lambda f, t, tex: {"from": f, "to": t, "faces": {d: {"texture": tex} for d in ("north", "south", "east", "west", "up", "down")}}
    write_model("isolation_bag_overlay", {
        "ambientocclusion": False,
        "textures": {"particle": "cultivar:block/isolation_bag_overlay", "bag": "cultivar:block/isolation_bag_overlay"},
        "elements": [box([3, 0, 3], [13, 14, 13], "#bag")]})
    write_model("template_pan", {
        "parent": "block/block",
        "textures": {"particle": "#pan"},
        "elements": [box([1, 0, 1], [15, 2, 15], "#pan"), box([1, 2, 1], [15, 6, 2], "#pan"), box([1, 2, 14], [15, 6, 15], "#pan"),
                     box([1, 2, 2], [2, 6, 14], "#pan"), box([14, 2, 2], [15, 6, 14], "#pan")]})
    write_model("template_pipe", {
        "parent": "block/block",
        "textures": {"particle": "#pipe"},
        "elements": [box([0, 0, 6], [16, 4, 10], "#pipe"), box([6, 0, 0], [10, 4, 16], "#pipe")]})
    write_model("template_sprinkler", {
        "parent": "block/block",
        "textures": {"particle": "#side"},
        "elements": [box([5, 0, 5], [11, 6, 11], "#side"),
                     {"from": [3, 6, 3], "to": [13, 8, 13],
                      "faces": {"up": {"texture": "#top"}, "down": {"texture": "#side"}, "north": {"texture": "#side"},
                                "south": {"texture": "#side"}, "east": {"texture": "#side"}, "west": {"texture": "#side"}}},
                     box([7, 8, 7], [9, 10, 9], "#side")]})
    write_model("template_post", {
        "parent": "block/block",
        "textures": {"particle": "#post"},
        "elements": [box([7, 0, 7], [9, 12, 9], "#post"), box([3, 9, 7.5], [13, 14, 8.5], "#plate")]})
    write_model("template_portal", {
        "textures": {"particle": "#portal"},
        "elements": [{"from": [0, 0, 0], "to": [16, 12, 16],
                      "faces": {d: {"texture": "#portal"} for d in ("north", "south", "east", "west", "up")}}]})


if __name__ == "__main__":
    crops()
    blocks()
    items()
    import item_art
    item_art.write_logo(os.path.join(ROOT, "..", "..", "cultivar_logo.png"))
    entities()
    particles()
    models()
    count = sum(len(files) for _, _, files in os.walk(TEX))
    print(f"Wrote {count} textures and parent models under {ROOT}")
