import colorsys
import json
import math
import os
import struct
import zlib

BAYER4 = [[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]
LIGHT = (-0.62, -0.78)


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def _shift_hue(h, toward, amount):
    d = ((toward - h + 0.5) % 1.0) - 0.5
    if abs(d) <= amount:
        return toward % 1.0
    return (h + math.copysign(amount, d)) % 1.0


def ramp(base, n=6, base_index=3, value_step=0.16, hue_step=0.022, sat_step=0.07, alpha=255):
    r, g, b = [v / 255.0 for v in base[:3]]
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    out = []
    for i in range(n):
        t = i - base_index
        if t < 0:
            hh = _shift_hue(h, 0.70, hue_step * -t)
            ss = min(1.0, s + sat_step * -t)
            vv = max(0.05, v * (1 + value_step * 1.3 * t))
        elif t > 0:
            hh = _shift_hue(h, 0.14, hue_step * t)
            ss = max(0.0, s - sat_step * 0.8 * t)
            vv = min(1.0, v + (1 - v) * 0.22 * t + v * value_step * 0.6 * t)
        else:
            hh, ss, vv = h, s, v
        rr, gg, bb = colorsys.hsv_to_rgb(hh, ss, vv)
        out.append((int(rr * 255 + 0.5), int(gg * 255 + 0.5), int(bb * 255 + 0.5), alpha))
    return out


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t + 0.5) for i in range(4))


def rect(x0, y0, x1, y1):
    return {(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)}


def ellipse(cx, cy, rx, ry):
    out = set()
    for y in range(int(math.floor(cy - ry)) - 1, int(math.ceil(cy + ry)) + 2):
        for x in range(int(math.floor(cx - rx)) - 1, int(math.ceil(cx + rx)) + 2):
            dx = (x + 0.5 - cx) / max(rx, 0.01)
            dy = (y + 0.5 - cy) / max(ry, 0.01)
            if dx * dx + dy * dy <= 1.0:
                out.add((x, y))
    return out


def poly(points):
    out = set()
    ys = [p[1] for p in points]
    for y in range(int(math.floor(min(ys))), int(math.ceil(max(ys))) + 1):
        yc = y + 0.5
        xs = []
        n = len(points)
        for i in range(n):
            x0, y0 = points[i]
            x1, y1 = points[(i + 1) % n]
            if (y0 <= yc < y1) or (y1 <= yc < y0):
                xs.append(x0 + (yc - y0) * (x1 - x0) / (y1 - y0))
        xs.sort()
        for i in range(0, len(xs) - 1, 2):
            for x in range(int(math.ceil(xs[i] - 0.5)), int(math.floor(xs[i + 1] - 0.5)) + 1):
                out.add((x, y))
    return out


def line(x0, y0, x1, y1):
    out = set()
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx + dy
    x, y = x0, y0
    while True:
        out.add((x, y))
        if x == x1 and y == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x += sx
        if e2 <= dx:
            err += dx
            y += sy
    return out


def thick(points, width):
    out = set(points)
    r = width / 2.0
    for (x, y) in points:
        for oy in range(-int(r) - 1, int(r) + 2):
            for ox in range(-int(r) - 1, int(r) + 2):
                if ox * ox + oy * oy <= r * r:
                    out.add((x + ox, y + oy))
    return out


def shift(mask, dx, dy):
    return {(x + dx, y + dy) for (x, y) in mask}


class Sprite:
    def __init__(self, w=16, h=16):
        self.w, self.h = w, h
        self.px = [None] * (w * h)
        self.ramp_at = [None] * (w * h)
        self.no_outline = set()

    def copy(self):
        s = Sprite(self.w, self.h)
        s.px = list(self.px)
        s.ramp_at = list(self.ramp_at)
        s.no_outline = set(self.no_outline)
        return s

    def inside(self, x, y):
        return 0 <= x < self.w and 0 <= y < self.h

    def get(self, x, y):
        return self.px[y * self.w + x] if self.inside(x, y) else None

    def put(self, x, y, c, r=None):
        if self.inside(x, y) and c is not None:
            i = y * self.w + x
            if c[3] < 255 and self.px[i] is not None:
                c = mix(self.px[i], (c[0], c[1], c[2], 255), c[3] / 255.0)
            self.px[i] = c
            if r is not None:
                self.ramp_at[i] = r

    def clear(self, mask):
        for (x, y) in mask:
            if self.inside(x, y):
                self.px[y * self.w + x] = None
                self.ramp_at[y * self.w + x] = None

    def flat(self, mask, c, r=None):
        for (x, y) in mask:
            self.put(x, y, c, r)

    def shade(self, mask, r, lo=1, hi=None, light=LIGHT, dome=0.55, mode="sphere", dither=True, bias=0.0):
        if not mask:
            return
        hi = len(r) - 2 if hi is None else hi
        xs = [p[0] for p in mask]
        ys = [p[1] for p in mask]
        cx = (min(xs) + max(xs)) / 2.0
        cy = (min(ys) + max(ys)) / 2.0
        rad = max(max(xs) - min(xs), max(ys) - min(ys), 1) / 2.0 + 0.5
        dist = {}
        frontier = []
        for p in mask:
            x, y = p
            if any((x + ox, y + oy) not in mask for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                dist[p] = 1
                frontier.append(p)
        while frontier:
            nxt = []
            for (x, y) in frontier:
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    q = (x + ox, y + oy)
                    if q in mask and q not in dist:
                        dist[q] = dist[(x, y)] + 1
                        nxt.append(q)
            frontier = nxt
        maxd = max(dist.values())
        lx, ly = light
        for p in mask:
            x, y = p
            if mode == "cyl_x":
                d = -((x + 0.5 - cx) * lx) / rad
            elif mode == "cyl_y":
                d = -((y + 0.5 - cy) * ly) / rad
            elif mode == "flat":
                d = 0.0
            else:
                d = -((x + 0.5 - cx) * lx + (y + 0.5 - cy) * ly) / rad
            dom = (dist[p] - 1) / max(1, maxd - 1) if maxd > 1 else 0.5
            v = (1 - dome) * (0.5 + 0.5 * max(-1.0, min(1.0, d))) + dome * dom + bias
            v = max(0.0, min(1.0, v))
            f = lo + v * (hi - lo)
            base = int(math.floor(f))
            frac = f - base
            if dither:
                thr = (BAYER4[y % 4][x % 4] + 0.5) / 16.0
                idx = base + (1 if frac > thr else 0)
            else:
                idx = int(round(f))
            idx = max(lo, min(hi, idx))
            self.put(x, y, r[idx], r)

    def outline(self, selective=True, outside=True, color=None):
        add = []
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y * self.w + x] is not None:
                    continue
                best = None
                lit = False
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    qx, qy = x + ox, y + oy
                    if self.inside(qx, qy) and self.px[qy * self.w + qx] is not None and (qx, qy) not in self.no_outline:
                        r = self.ramp_at[qy * self.w + qx]
                        best = r if r is not None else best
                        if best is None:
                            best = [self.px[qy * self.w + qx]] * 6
                        if ox > 0 or oy > 0:
                            lit = True
                if best is not None:
                    c = color or (best[1] if (selective and lit) else best[0])
                    add.append((x, y, c))
        for (x, y, c) in add:
            self.px[y * self.w + x] = c

    def inner_line(self, mask, r, idx=1):
        for (x, y) in mask:
            if self.get(x, y) is not None:
                self.put(x, y, r[idx], r)

    def spec(self, x, y, c=(255, 255, 248, 255)):
        if self.get(x, y) is not None:
            self.put(x, y, c)

    def pixels(self):
        return list(self.px)


def _png(path, rows, w, h):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            c = rows[y * w + x]
            raw.extend(c if c is not None else (0, 0, 0, 0))

    def chunk(kind, data):
        c = struct.pack(">I", len(data)) + kind + data
        return c + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def save(path, frames, frametime=None, frame_order=None):
    if not isinstance(frames, list):
        frames = [frames]
    w, h = frames[0].w, frames[0].h
    strip = []
    for f in frames:
        strip.extend(f.pixels())
    _png(path, strip, w, h * len(frames))
    meta = path + ".mcmeta"
    if len(frames) > 1:
        anim = {"frametime": frametime or 2, "interpolate": False}
        if frame_order:
            anim["frames"] = frame_order
        with open(meta, "w", encoding="utf-8") as f:
            json.dump({"animation": anim}, f, indent=2)
            f.write("\n")
    elif os.path.exists(meta):
        os.remove(meta)


def contact_sheet(path, named, scale=8, cols=8):
    cell = 16 * scale + 8
    rows = (len(named) + cols - 1) // cols
    W = cols * cell * 2
    H = rows * cell
    out = [(0, 0, 0, 255)] * (W * H)
    backgrounds = [(0x8B, 0x8B, 0x8B, 255), (0x10, 0x1C, 0x22, 255)]
    for i, (name, frames) in enumerate(named):
        if not isinstance(frames, list):
            frames = [frames]
        sprite = frames[0]
        col = i % cols
        row = i // cols
        for b, bg in enumerate(backgrounds):
            ox = (col * 2 + b) * cell + 4
            oy = row * cell + 4
            for y in range(16 * scale):
                for x in range(16 * scale):
                    out[(oy + y) * W + ox + x] = bg
            for y in range(16):
                for x in range(16):
                    c = sprite.px[y * 16 + x]
                    if c is None:
                        continue
                    for yy in range(scale):
                        base = (oy + y * scale + yy) * W + ox + x * scale
                        for xx in range(scale):
                            out[base + xx] = mix(bg, (c[0], c[1], c[2], 255), c[3] / 255.0)
    _png(path, out, W, H)
