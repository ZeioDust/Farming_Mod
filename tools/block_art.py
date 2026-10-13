import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixel import (BAYER4, Sprite, ellipse, hexc, line, mix, poly, ramp, rect, save, thick)

LAB = ramp(hexc("D4D8DC"), base_index=3, value_step=0.08, hue_step=0.008, sat_step=0.02)
METAL = ramp(hexc("8A96A0"), value_step=0.15)
DARK = ramp(hexc("3A444C"), value_step=0.25)
PLANK = ramp(hexc("9A6C40"))
PLANK_DARK = ramp(hexc("6A4428"))
BRASS = ramp(hexc("C8962E"), value_step=0.2)
COPPER = ramp(hexc("B86A3E"))
GLASS = ramp(hexc("8CC8D8"))
SOIL = ramp(hexc("5E4028"))
SOIL_DRY = ramp(hexc("8E8A80"), value_step=0.1)
LEAF = ramp(hexc("3E8A30"))
LEAF_DARK = ramp(hexc("2A5E24"))
BARK = ramp(hexc("5A4028"))
TEAL = ramp(hexc("2E9C8C"))
VIOLET = ramp(hexc("9A4FE0"), value_step=0.2)
AMBERR = ramp(hexc("D88A1E"), value_step=0.2)
ICE = ramp(hexc("9ED8F0"), value_step=0.1)
WATER = ramp(hexc("3C7CD0"), value_step=0.2)
HAZARD_Y = hexc("E8C020")
HAZARD_K = hexc("26282A")
GENE_COLORS = ["E0B040", "6FBF4F", "60A8E0", "D08A40", "F0E070", "B070C0", "8A6A40", "C05050"]


def noise(x, y, seed):
    h = ((x % 16) * 73856093) ^ ((y % 16) * 19349663) ^ (seed * 83492791)
    h = (h ^ (h >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65536.0


def smooth_noise(x, y, seed, cell=4):
    gx, gy = x / cell, y / cell
    x0, y0 = int(math.floor(gx)), int(math.floor(gy))
    fx, fy = gx - x0, gy - y0
    n = 16 // cell

    def v(ix, iy):
        return noise((ix % n) * cell, (iy % n) * cell, seed)

    a = v(x0, y0) + (v(x0 + 1, y0) - v(x0, y0)) * fx
    b = v(x0, y0 + 1) + (v(x0 + 1, y0 + 1) - v(x0, y0 + 1)) * fx
    return a + (b - a) * fy


def textured(s, mask, r, lo, hi, seed, grain=0.5, cell=4):
    for (x, y) in mask:
        v = (1 - grain) * smooth_noise(x, y, seed, cell) + grain * noise(x, y, seed + 7)
        f = lo + v * (hi - lo)
        base = int(math.floor(f))
        idx = base + (1 if (f - base) > (BAYER4[y % 4][x % 4] + 0.5) / 16.0 else 0)
        s.put(x, y, r[max(lo, min(hi, idx))], r)


def panel(r, seed=1, rivets=True, frame=True):
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), r, 2, 3, seed, grain=0.35)
    if frame:
        for i in range(16):
            s.put(i, 0, r[4])
            s.put(0, i, r[4])
            s.put(i, 15, r[1])
            s.put(15, i, r[1])
        s.put(0, 15, r[2])
        s.put(15, 0, r[2])
        for i in range(1, 15):
            s.put(i, 1, r[3])
            s.put(1, i, r[3])
            s.put(i, 14, r[2])
            s.put(14, i, r[2])
    if rivets:
        for (x, y) in ((2, 2), (12, 2), (2, 12), (12, 12)):
            s.put(x, y, r[5])
            s.put(x + 1, y + 1, r[1])
    return s


def inset(s, x0, y0, x1, y1, r, fill_idx=1):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            s.put(x, y, r[fill_idx])
    for x in range(x0, x1 + 1):
        s.put(x, y0, r[0])
        s.put(x, y1 + 1, r[4])
    for y in range(y0, y1 + 1):
        s.put(x0, y, r[0])
        s.put(x1 + 1, y, r[4])


def raised(s, x0, y0, x1, y1, r):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            s.put(x, y, r[3])
    for x in range(x0, x1 + 1):
        s.put(x, y0, r[4])
        s.put(x, y1, r[1])
    for y in range(y0, y1 + 1):
        s.put(x0, y, r[4])
        s.put(x1, y, r[1])
    s.put(x1, y0, r[3])
    s.put(x0, y1, r[2])


def vents(s, x0, y0, x1, y1, r, step=2):
    for y in range(y0, y1 + 1, step):
        for x in range(x0, x1 + 1):
            s.put(x, y, r[0])
            s.put(x, y + 1, r[4])


def grille(s, x0, y0, x1, y1, r):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if (x + y) % 2 == 0:
                s.put(x, y, r[0])
            else:
                s.put(x, y, r[2])


def glass_window(s, x0, y0, x1, y1, frame_r, tint=None):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if tint:
                s.put(x, y, tint)
    for x in range(x0 - 1, x1 + 2):
        s.put(x, y0 - 1, frame_r[1])
        s.put(x, y1 + 1, frame_r[4])
    for y in range(y0 - 1, y1 + 2):
        s.put(x0 - 1, y, frame_r[1])
        s.put(x1 + 1, y, frame_r[4])
    for (dx, a) in ((1, 150), (3, 90)):
        for k in range(y1 - y0 + 1):
            x = x0 + dx + k
            y = y1 - k
            if x0 <= x <= x1 and y0 <= y <= y1 and k < 3:
                s.put(x, y, (255, 255, 255, a))


def led(s, x, y, color, on=True):
    c = hexc(color)
    s.put(x, y, c if on else mix(c, (30, 30, 30, 255), 0.7))
    if on and y > 0 and s.get(x, y - 1) is not None:
        s.put(x, y - 1, mix(s.get(x, y - 1), c, 0.35))


def hazard(s, x0, y0, x1, y1, phase=0):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            s.put(x, y, HAZARD_Y if ((x + y + phase) // 2) % 2 == 0 else HAZARD_K)


def bottom(r, seed):
    s = panel(r, seed, rivets=False)
    for (x, y) in ((1, 1), (13, 1), (1, 13), (13, 13)):
        s.flat(rect(x, y, x + 1, y + 1), r[0])
    return s


def genome_lab_front(frame=0, frames=8):
    s = panel(LAB, 3)
    s.flat(rect(2, 2, 13, 2), TEAL[3])
    inset(s, 3, 4, 12, 9, DARK, 1)
    for y in range(5, 10):
        t = (y + frame) / 4.0 * math.pi
        a = 7.5 + math.sin(t) * 3.2
        b = 7.5 - math.sin(t) * 3.2
        s.put(int(round(a)), y, hexc("5CE8A0"))
        s.put(int(round(b)), y, hexc("3FA8E0"))
        if (y + frame) % 2 == 0:
            for x in range(min(int(round(a)), int(round(b))) + 1, max(int(round(a)), int(round(b)))):
                s.put(x, y, ramp(hexc(GENE_COLORS[(y + frame) % 8]))[2])
    inset(s, 4, 11, 8, 12, DARK, 0)
    s.put(6, 11, hexc("C9A46E"))
    led(s, 11, 12, "5CE88A", frame % 4 < 2)
    led(s, 12, 12, "F2B84A", True)
    return s


def lab_side(seed, accent="2E9C8C"):
    s = panel(LAB, seed)
    vents(s, 4, 9, 11, 12, LAB)
    a = ramp(hexc(accent))
    s.flat(rect(2, 4, 13, 4), a[3])
    s.flat(rect(2, 5, 13, 5), a[1])
    return s


def lab_top(seed):
    s = panel(LAB, seed)
    inset(s, 5, 5, 10, 10, LAB, 2)
    s.flat(rect(6, 6, 9, 9), DARK[2])
    s.put(6, 6, DARK[4])
    return s


def seed_bank_front():
    s = panel(LAB, 5, rivets=False)
    for row in range(3):
        for col in range(3):
            x0 = 1 + col * 5
            y0 = 1 + row * 5
            raised(s, x0, y0, x0 + 4, y0 + 4, LAB)
            label = ramp(hexc(GENE_COLORS[(row * 3 + col) % 8]))
            s.flat(rect(x0 + 1, y0 + 1, x0 + 3, y0 + 1), label[3])
            s.put(x0 + 2, y0 + 3, BRASS[4])
            s.put(x0 + 2, y0 + 4, BRASS[1])
    return s


def seed_vault_drawer_front():
    s = panel(LAB, 6, rivets=False)
    for y0 in (1, 8):
        raised(s, 1, y0, 14, y0 + 6, LAB)
        s.flat(rect(4, y0 + 1, 11, y0 + 2), hexc("F4EFE0"))
        s.flat(rect(5, y0 + 2, 9, y0 + 2), ramp(hexc("3A4650"))[3])
        s.flat(rect(5, y0 + 4, 10, y0 + 4), BRASS[4])
        s.flat(rect(5, y0 + 5, 10, y0 + 5), BRASS[1])
    return s


def mutagen_front(frame=0, frames=8):
    s = panel(LAB, 7)
    port = ellipse(8, 6.5, 4.6, 4.6)
    inner = ellipse(8, 6.5, 3.4, 3.4)
    s.shade(port - inner, METAL, lo=1, hi=5, dome=0.4)
    level = 4.5
    for (x, y) in inner:
        if y >= level:
            v = VIOLET[2 + ((y - 5) % 2)] if y > level else VIOLET[5]
            s.put(x, y, v)
        else:
            s.put(x, y, DARK[1])
    for k, (bx, ph) in enumerate(((7, 0), (9, 3), (8, 6))):
        by = 9 - ((frame + ph) % frames) * 5 // frames
        if (bx, by) in inner and by >= level:
            s.put(bx, by, VIOLET[5])
    s.put(6, 4, (255, 255, 255, 200))
    s.put(6, 5, (255, 255, 255, 120))
    hazard(s, 2, 12, 13, 13, phase=0)
    return s


def mutagen_side():
    s = panel(LAB, 8)
    for x0 in (4, 10):
        s.flat(rect(x0, 2, x0 + 1, 13), GLASS[4])
        s.flat(rect(x0, 7, x0 + 1, 13), VIOLET[3])
        s.put(x0, 7, VIOLET[5])
        s.flat(rect(x0 - 1, 2, x0 + 2, 2), METAL[2])
        s.flat(rect(x0 - 1, 13, x0 + 2, 13), METAL[2])
    return s


def thermal_front():
    s = panel(METAL, 9)
    inset(s, 3, 3, 12, 9, ICE, 3)
    for (x, y) in line(4, 4, 11, 8) | line(4, 8, 11, 4) | line(7, 4, 8, 8):
        s.put(x, y, ICE[5])
    s.put(7, 6, (255, 255, 255, 255))
    for i, x in enumerate(range(4, 12, 2)):
        led(s, x, 12, "6AC8F8", i < 3)
    return s


def thermal_side():
    s = panel(METAL, 10)
    for y in range(3, 13, 2):
        s.flat(rect(2, y, 13, y), METAL[5])
        s.flat(rect(2, y + 1, 13, y + 1), METAL[1])
    return s


def fan_top(r, seed):
    s = panel(r, seed)
    ring = ellipse(8, 8, 5.6, 5.6)
    s.flat(ring, r[1])
    for (x, y) in ring:
        a = math.atan2(y + 0.5 - 8, x + 0.5 - 8)
        if int((a + math.pi) / (math.pi / 3)) % 2 == 0:
            s.put(x, y, r[0])
    s.flat(ellipse(8, 8, 1.6, 1.6), r[4])
    for (x, y) in ellipse(8, 8, 5.6, 5.6) - ellipse(8, 8, 4.8, 4.8):
        s.put(x, y, r[4] if (x + y) < 16 else r[1])
    return s


def climate_front(frame=0, frames=4):
    s = panel(LAB, 11)
    dial = ellipse(5.5, 6, 3.6, 3.6)
    s.flat(dial, hexc("F4F0E4"))
    for (x, y) in ellipse(5.5, 6, 3.6, 3.6) - ellipse(5.5, 6, 2.8, 2.8):
        s.put(x, y, METAL[2] if (x + y) > 11 else METAL[4])
    ang = -2.2 + frame * 0.25
    s.flat(line(5, 6, int(round(5 + math.cos(ang) * 2.4)), int(round(6 + math.sin(ang) * 2.4))), hexc("C83A3A"))
    s.put(5, 6, DARK[1])
    for (x, y, c) in ((3, 5, "5AA8FF"), (5, 3, "5CE0B0"), (8, 5, "F2585A")):
        s.put(x, y, hexc(c))
    inset(s, 10, 3, 13, 9, DARK, 1)
    for i, x in enumerate(range(11, 14)):
        hgt = 2 + ((i + frame) % 4)
        for k in range(hgt):
            s.put(x, 9 - k, hexc("5CE88A"))
    for x in (4, 7, 10):
        raised(s, x, 11, x + 2, 12, METAL)
    led(s, 13, 12, "5CE88A", frame % 2 == 0)
    return s


def planks(seed, gaps=False):
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), PLANK, 2, 4, seed, grain=0.6, cell=8)
    for y in (3, 7, 11, 15):
        s.flat(rect(0, y, 15, y), PLANK[1] if not gaps else SOIL[0])
    for (x, y) in ((5, 1), (11, 5), (3, 9), (9, 13)):
        s.put(x, y, PLANK[1])
    return s


def wood_frame(s):
    for i in range(16):
        s.put(i, 0, PLANK_DARK[4])
        s.put(0, i, PLANK_DARK[4])
        s.put(i, 15, PLANK_DARK[1])
        s.put(15, i, PLANK_DARK[1])
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        s.put(x, y, METAL[4])


def compost_front():
    s = planks(12, gaps=True)
    wood_frame(s)
    raised(s, 4, 9, 11, 14, PLANK_DARK)
    s.flat(rect(5, 10, 10, 13), SOIL[2])
    for (x, y) in ((6, 11), (9, 12), (7, 13)):
        s.put(x, y, LEAF[4])
    s.put(4, 10, BRASS[4])
    s.put(4, 13, BRASS[4])
    return s


def compost_side():
    s = planks(13, gaps=True)
    wood_frame(s)
    return s


def compost_top():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), SOIL, 1, 4, 14, grain=0.6, cell=4)
    for (x, y, c) in ((4, 5, LEAF[4]), (10, 3, LEAF[3]), (7, 11, ramp(hexc("C8A040"))[3]), (12, 9, LEAF[4]), (3, 12, SOIL[5])):
        s.put(x, y, c)
    for i in range(16):
        for (x, y) in ((i, 0), (i, 1), (0, i), (1, i), (i, 14), (i, 15), (14, i), (15, i)):
            s.put(x, y, PLANK[3] if (x < 2 or y < 2) else PLANK[1])
    return s


def crate_corners(s):
    for (x, y) in ((0, 0), (1, 0), (0, 1), (14, 0), (15, 0), (15, 1), (0, 14), (0, 15), (1, 15), (15, 14), (14, 15), (15, 15)):
        s.put(x, y, METAL[3])


def collector_front():
    s = planks(15)
    crate_corners(s)
    inset(s, 4, 9, 11, 13, DARK, 1)
    for (x, y, c) in ((5, 12, "E8C838"), (7, 11, "F0A030"), (9, 12, "8CB64C"), (10, 11, "E8C838"), (6, 13, "C9A24A")):
        s.put(x, y, hexc(c))
    s.flat(rect(5, 4, 10, 6), hexc("F1EAD6"))
    s.flat(rect(6, 5, 9, 5), ramp(hexc("3A4650"))[3])
    return s


def collector_side():
    s = planks(16)
    crate_corners(s)
    return s


def collector_top():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), PLANK_DARK, 1, 2, 17)
    produce = [ramp(hexc(c)) for c in ("E8C838", "F0A030", "8CB64C", "C9A24A", "A27450", "E89AC4")]
    for i in range(22):
        x = 2 + int(noise(i, 3, 5) * 12)
        y = 2 + int(noise(i, 9, 5) * 12)
        r = produce[i % len(produce)]
        s.put(x, y, r[3])
        s.put(x + 1, y, r[4])
        s.put(x, y + 1, r[2])
    for i in range(16):
        for (x, y) in ((i, 0), (0, i)):
            s.put(x, y, PLANK[4])
        for (x, y) in ((i, 15), (15, i)):
            s.put(x, y, PLANK[1])
    crate_corners(s)
    return s


def spreader_front():
    s = panel(METAL, 18)
    disc = ellipse(8, 8, 4.6, 4.6)
    s.shade(disc, DARK, lo=1, hi=4, dome=0.3)
    for k in range(4):
        a = k * math.pi / 2 + 0.4
        s.flat(line(8, 8, int(round(8 + math.cos(a) * 4)), int(round(8 + math.sin(a) * 4))), METAL[4])
    s.put(8, 8, BRASS[5])
    for (x, y, c) in ((3, 13, "6FD06F"), (12, 13, "E07A5A"), (13, 12, "B08AE0")):
        s.put(x, y, hexc(c))
    return s


def spreader_top():
    s = panel(METAL, 19)
    inset(s, 3, 3, 12, 12, METAL, 1)
    for i in range(26):
        x = 4 + int(noise(i, 1, 9) * 8)
        y = 4 + int(noise(i, 7, 9) * 8)
        s.put(x, y, hexc(("6FD06F", "E07A5A", "B08AE0", "8A6A40")[i % 4]))
    return s


def metal_side(seed):
    s = panel(METAL, seed)
    vents(s, 4, 4, 11, 7, METAL)
    s.flat(rect(3, 11, 12, 11), METAL[1])
    s.flat(rect(3, 12, 12, 12), METAL[4])
    return s


def pollination_front(active=False, frame=0, frames=4):
    s = panel(METAL, 20)
    if active:
        inset(s, 3, 3, 12, 11, ramp(hexc("F2D24A")), 4)
        for (x, y) in ellipse(7.5, 7, 3.5, 3.0):
            s.put(x, y, hexc("FFF4B0"))
        s.put(7, 6, (255, 255, 255, 255))
    else:
        inset(s, 3, 3, 12, 11, METAL, 2)
        s.flat(rect(4, 7, 11, 7), METAL[1])
    for x in range(3, 13):
        s.put(x, 2, HAZARD_Y if (x // 2) % 2 == 0 else HAZARD_K)
    led(s, 3, 13, "F2C42C", (not active) or frame % 2 == 0)
    led(s, 12, 13, "F2C42C", (not active) or frame % 2 == 1)
    return s


def landing_top():
    s = panel(METAL, 21)
    for (x, y) in ellipse(8, 8, 5.5, 5.5) - ellipse(8, 8, 4.5, 4.5):
        s.put(x, y, HAZARD_Y)
    for (x, y) in ellipse(8, 8, 2.2, 2.2) - ellipse(8, 8, 1.2, 1.2):
        s.put(x, y, HAZARD_Y)
    for (x, y) in ((8, 1), (8, 14), (1, 8), (14, 8)):
        s.put(x, y, HAZARD_Y)
    return s


def hopper_front(powder, level):
    s = panel(METAL, 22)
    inset(s, 5, 2, 10, 13, DARK, 1)
    fill_rows = int(round(level / 8 * 11))
    pr = ramp(hexc(powder), base_index=3, value_step=0.1)
    for y in range(13 - fill_rows + 1, 14):
        for x in range(6, 11):
            s.put(x, y, pr[3 + ((x + y) % 2)])
    if fill_rows > 0:
        for x in range(6, 11):
            s.put(x, 13 - fill_rows + 1, pr[5])
    for y in range(3, 14, 2):
        s.put(11, y, METAL[5])
    glass_window(s, 6, 3, 10, 13, METAL)
    return s


def hopper_top(powder):
    s = panel(METAL, 23)
    for (x, y) in ellipse(8, 8, 5.2, 5.2):
        s.put(x, y, METAL[1])
    pr = ramp(hexc(powder), base_index=3, value_step=0.1)
    textured(s, ellipse(8, 8.5, 4.0, 3.6), pr, 2, 4, 3)
    return s


def humidity_front(humidifier=True):
    s = panel(LAB, 24 if humidifier else 25)
    grille(s, 3, 2, 12, 7, LAB)
    tank = ramp(hexc("5AA8F0" if humidifier else "E0A040"))
    inset(s, 4, 9, 11, 13, DARK, 1)
    for y in range(11, 14):
        for x in range(5, 12):
            s.put(x, y, tank[3 if y > 11 else 4])
    glass_window(s, 5, 10, 11, 13, LAB)
    led(s, 13, 9, "5AA8F0" if humidifier else "F2B84A", True)
    return s


def humidity_top(humidifier=True):
    s = panel(LAB, 26)
    if humidifier:
        ring = ellipse(8, 8, 3.6, 3.6)
        s.shade(ring, METAL, lo=1, hi=4, dome=0.4)
        s.flat(ellipse(8, 8, 2.0, 2.0), hexc("8CC8F0"))
        s.put(7, 7, hexc("E8F8FF"))
    else:
        grille(s, 3, 3, 12, 12, LAB)
    return s


def grow_bottom():
    s = panel(DARK, 27, rivets=False)
    for y in range(2, 14, 3):
        for x in range(2, 14, 3):
            warm = ((x + y) // 3) % 3
            c = hexc(("FFF2C8", "F07AB0", "8AA8FF")[warm])
            s.put(x, y, c)
            s.put(x + 1, y, mix(c, (255, 255, 255, 255), 0.5))
            s.put(x, y + 1, mix(c, (40, 40, 40, 255), 0.3))
            s.put(x + 1, y + 1, c)
    return s


def grow_side():
    s = panel(METAL, 28)
    s.flat(rect(1, 12, 14, 14), hexc("FFF2C8"))
    s.flat(rect(1, 12, 14, 12), hexc("FFFFFF"))
    vents(s, 3, 3, 12, 8, METAL)
    return s


def grow_top():
    s = panel(METAL, 29)
    for x in range(2, 14, 2):
        s.flat(rect(x, 2, x, 13), METAL[5])
        s.flat(rect(x + 1, 2, x + 1, 13), METAL[1])
    return s


def soil_top(r, seed, moist=False, framed=False):
    s = Sprite()
    lo, hi = (0, 3) if moist else (1, 4)
    textured(s, rect(0, 0, 15, 15), r, lo, hi, seed, grain=0.55, cell=4)
    for y in range(0, 16, 4):
        for x in range(16):
            s.put(x, y, r[max(0, lo - 0) if moist else 1])
            s.put(x, (y + 1) % 16, r[hi])
    for (x, y) in ((3, 2), (11, 6), (6, 10), (13, 14)):
        s.put(x, y, r[5] if not moist else r[3])
    if moist:
        for (x, y) in ((5, 3), (12, 11)):
            s.put(x, y, (170, 200, 230, 255))
    if framed:
        for i in range(16):
            for (x, y, c) in ((i, 0, PLANK[4]), (0, i, PLANK[4]), (i, 15, PLANK[1]), (15, i, PLANK[1])):
                s.put(x, y, c)
        for (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
            s.put(x, y, METAL[3])
    return s


def soil_side(r, seed, framed=False):
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), r, 1, 4, seed, grain=0.6, cell=4)
    for (x, y) in ((2, 4), (9, 9), (13, 3), (6, 13)):
        s.put(x, y, ramp(hexc("8A8A80"))[3])
    s.flat(rect(0, 0, 15, 0), r[4])
    if framed:
        s.flat(rect(0, 0, 15, 2), PLANK[3])
        s.flat(rect(0, 2, 15, 2), PLANK[1])
    return s


def depleted():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), SOIL_DRY, 1, 4, 30, grain=0.5, cell=4)
    for pts in ((2, 3, 6, 6), (6, 6, 5, 10), (9, 2, 12, 5), (12, 5, 14, 9), (8, 12, 11, 14)):
        for (x, y) in line(*pts):
            s.put(x, y, SOIL_DRY[0])
    return s


def greenhouse_pane():
    s = Sprite()
    for y in range(16):
        for x in range(16):
            s.put(x, y, (214, 240, 246, 70))
    frame = ramp(hexc("E8ECEA"), base_index=4, value_step=0.05)
    for i in range(16):
        s.put(i, 0, frame[5])
        s.put(0, i, frame[5])
        s.put(i, 15, frame[2])
        s.put(15, i, frame[2])
    for (dx, a) in ((3, 170), (4, 110), (7, 90)):
        for k in range(1, 15):
            x, y = dx + k, 15 - k
            if 1 <= x <= 14 and 1 <= y <= 14 and k < 9 - dx // 2:
                s.put(x, y, (255, 255, 255, a))
    return s


def isolation_screen():
    s = Sprite()
    mesh = ramp(hexc("E6E2D6"), base_index=4, value_step=0.05)
    for y in range(16):
        for x in range(16):
            if x % 3 == 0 or y % 3 == 0:
                s.put(x, y, mesh[3 if (x + y) % 2 else 4])
    for i in range(16):
        for (x, y, c) in ((i, 0, PLANK[4]), (0, i, PLANK[4]), (i, 15, PLANK[1]), (15, i, PLANK[1])):
            s.put(x, y, c)
    return s


def burn_pan(lit=False, frame=0, frames=4):
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), DARK, 2, 4, 31)
    for i in range(16):
        s.put(i, 0, DARK[4])
        s.put(0, i, DARK[4])
        s.put(i, 15, DARK[1])
        s.put(15, i, DARK[1])
    bed = rect(3, 3, 12, 12)
    if lit:
        ember = ramp(hexc("E8601C"), value_step=0.25)
        for (x, y) in bed:
            v = noise(x + frame * 5, y + frame * 3, 32)
            s.put(x, y, ember[1 + int(v * 4.99)])
        for (x, y) in ((5, 6), (9, 4), (7, 10), (11, 9)):
            if (x + frame) % 3:
                s.put(x, y, hexc("FFE680"))
    else:
        textured(s, bed, ramp(hexc("4A4440")), 1, 3, 33)
        for (x, y) in ((5, 6), (9, 9)):
            s.put(x, y, ramp(hexc("8A8680"))[3])
    return s


def irrigation(fed=False, frame=0, frames=8):
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), COPPER, 2, 3, 34, grain=0.3)
    for i in range(16):
        for k, idx in ((6, 4), (9, 1)):
            s.put(i, k, COPPER[idx])
            s.put(k, i, COPPER[idx])
    for i in range(16):
        for k in (7, 8):
            for (x, y) in ((i, k), (k, i)):
                if fed:
                    w = WATER[3 if ((i + frame) // 2) % 2 == 0 else 4]
                    if (i + frame) % 8 == 0:
                        w = WATER[5]
                    s.put(x, y, w)
                else:
                    s.put(x, y, COPPER[0])
    for b in (2, 13):
        s.flat(rect(b, 5, b, 10), BRASS[4])
        s.flat(rect(5, b, 10, b), BRASS[4])
    return s


def sprinkler_side():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), COPPER, 2, 4, 35)
    for y in (4, 10):
        s.flat(rect(0, y, 15, y), BRASS[4])
        s.flat(rect(0, y + 1, 15, y + 1), BRASS[1])
    return s


def sprinkler_top():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), METAL, 2, 4, 36)
    for (x, y) in ellipse(8, 8, 4.5, 4.5) - ellipse(8, 8, 3.5, 3.5):
        s.put(x, y, METAL[1])
    for a in range(6):
        ang = a * math.pi / 3
        x = int(round(8 + math.cos(ang) * 4))
        y = int(round(8 + math.sin(ang) * 4))
        s.put(x, y, WATER[4])
    s.put(8, 8, BRASS[5])
    return s


def trial_post():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), PLANK, 2, 4, 37, grain=0.7, cell=8)
    for x in range(0, 16, 4):
        s.flat(rect(x, 0, x, 15), PLANK[1])
    return s


def trial_plate():
    s = Sprite()
    plate = ramp(hexc("F1EAD6"), base_index=4, value_step=0.04)
    textured(s, rect(0, 0, 15, 15), plate, 3, 4, 38)
    for i in range(16):
        s.put(i, 0, plate[5])
        s.put(i, 15, plate[1])
        s.put(0, i, plate[5])
        s.put(15, i, plate[1])
    for (y, w) in ((4, 10), (7, 7), (10, 9)):
        s.flat(rect(3, y, 3 + w, y), ramp(hexc("3A4650"))[3])
    s.flat(rect(12, 9, 13, 10), LEAF[4])
    return s


def bark_side():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), BARK, 1, 4, 39, grain=0.4, cell=4)
    for x0 in (1, 5, 9, 13):
        for y in range(16):
            x = x0 + (1 if (y // 5 + x0) % 2 else 0)
            s.put(x % 16, y, BARK[0])
            s.put((x + 1) % 16, y, BARK[4] if y % 3 else BARK[2])
    for (x, y) in ((3, 6), (11, 12), (7, 2)):
        s.put(x, y, ramp(hexc("6A8A3A"))[3])
    return s


def bark_top():
    s = Sprite()
    wood = ramp(hexc("A27A48"))
    textured(s, rect(0, 0, 15, 15), wood, 2, 4, 40, grain=0.3)
    for r in (1.8, 3.6, 5.4):
        for (x, y) in ellipse(7.5, 7.5, r, r) - ellipse(7.5, 7.5, r - 0.9, r - 0.9):
            s.put(x, y, wood[1])
    for i in range(16):
        for (x, y) in ((i, 0), (0, i), (i, 15), (15, i)):
            s.put(x, y, BARK[2])
    return s


def leafmass():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), LEAF_DARK, 0, 3, 41, grain=0.5, cell=4)
    for i in range(14):
        cx = int(noise(i, 2, 42) * 16)
        cy = int(noise(i, 5, 42) * 16)
        for (x, y) in ellipse(cx + 0.5, cy + 0.5, 1.6, 1.1):
            s.put(x % 16, y % 16, LEAF[3 if (x + y) % 2 else 4])
        s.put(cx % 16, (cy - 1) % 16, LEAF[5])
    return s


def vine():
    s = Sprite()
    for sx in (3, 8, 12):
        for y in range(16):
            x = sx + (1 if (y // 4) % 2 else 0)
            s.put(x, y, LEAF[2])
            s.put(x + 1, y, LEAF[3])
            if y % 4 == 1:
                for (lx, ly) in ellipse(x + 2.5, y + 0.5, 1.4, 0.9):
                    s.put(lx, ly, LEAF[4])
    return s


def breach_frame():
    s = Sprite()
    stone = ramp(hexc("5A6A40"))
    textured(s, rect(0, 0, 15, 15), stone, 1, 4, 43, grain=0.5)
    for i in range(16):
        s.put(i, 0, stone[4])
        s.put(0, i, stone[4])
        s.put(i, 15, stone[1])
        s.put(15, i, stone[1])
    inset(s, 4, 4, 11, 11, stone, 1)
    s.shade(ellipse(8, 8, 2.8, 2.8), AMBERR, lo=2, hi=5, dome=0.6)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        s.put(x, y, BRASS[4])
    return s


def breach(frame=0, frames=8):
    s = Sprite()
    for y in range(16):
        for x in range(16):
            v = smooth_noise(x, y + frame * 2, 44, 4) * 0.6 + smooth_noise(x + frame, y, 45, 8) * 0.4
            g = ramp(hexc("4EA050"), value_step=0.2)[1 + int(v * 4.99)]
            s.put(x, y, (g[0], g[1], g[2], 150 + int(v * 60)))
    return s


PAPER_B = ramp(hexc("E6DFCC"), base_index=3, value_step=0.05)
SLATE = ramp(hexc("2A3436"), value_step=0.12)
FELT = ramp(hexc("2E6A44"), value_step=0.12)
GOLD_B = ramp(hexc("E0B030"), value_step=0.2)


def exchange_front(frame=0, frames=4):
    s = planks(70)
    crate_corners(s)
    inset(s, 2, 2, 13, 9, SLATE, 1)
    heights = [3, 5, 4, 6, 2, 5]
    for i, h in enumerate(heights):
        h = max(1, min(6, h + ((frame + i) % frames == 0) - ((frame + i * 2) % frames == 1)))
        x = 3 + i * 2
        col = hexc("7BE07B") if i % 2 == 0 else hexc("F2C442")
        for y in range(9 - h + 1, 10):
            s.put(x, y, col if y > 9 - h + 1 else mix(col, (255, 255, 255, 255), 0.4))
    inset(s, 4, 11, 11, 13, DARK, 1)
    for (x, y, c) in ((5, 12, "E8C838"), (7, 12, "F0A030"), (8, 13, "8CB64C"), (10, 12, "E89AC4"), (6, 13, "C9A24A")):
        s.put(x, y, hexc(c))
    return s


def exchange_top():
    s = planks(81)
    crate_corners(s)
    s.flat(rect(1, 7, 14, 8), ramp(hexc("3E9A44"))[3])
    s.flat(rect(1, 9, 14, 9), ramp(hexc("3E9A44"))[1])
    raised(s, 6, 6, 9, 10, BRASS)
    s.put(7, 8, BRASS[0])
    s.put(8, 8, BRASS[0])
    return s


def board_front():
    s = planks(71)
    wood_frame(s)
    for (x0, y0, x1, y1, seed) in ((2, 2, 7, 8, 0), (8, 3, 13, 9, 1), (4, 10, 11, 14, 2)):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                s.put(x, y, PAPER_B[4] if (x + y + seed) % 5 else PAPER_B[3])
        if y1 + 1 < 15:
            for x in range(x0, x1 + 1):
                s.put(x, y1 + 1, PLANK_DARK[1])
        for y in range(y0 + 2, y1, 2):
            for x in range(x0 + 1, x1 - (y % 3)):
                s.put(x, y, ramp(hexc("3A4650"))[3])
        s.put((x0 + x1) // 2, y0, BRASS[4])
    for (x, y) in ((8, 13), (9, 14), (10, 13), (11, 12)):
        s.put(x, y, hexc("3E9A44"))
    return s


def board_top():
    s = planks(72)
    wood_frame(s)
    return s


def processing_front(frame=0, frames=4):
    s = panel(METAL, 73)
    s.flat(rect(2, 2, 13, 2), AMBERR[3])
    inset(s, 3, 4, 12, 10, DARK, 1)
    for cx in (5.5, 9.5):
        roll = ellipse(cx + 0.5, 7.5, 2.4, 2.4)
        for (x, y) in roll:
            if 3 <= x <= 12 and 4 <= y <= 10:
                s.put(x, y, METAL[2])
        a = frame / frames * math.pi / 2 * (1 if cx < 7 else -1)
        for k in range(2):
            ang = a + k * math.pi / 2
            x = int(round(cx + 0.5 + math.cos(ang) * 1.6))
            y = int(round(7.5 + math.sin(ang) * 1.6))
            s.put(x, y, METAL[4])
            s.put(int(round(cx + 0.5 - math.cos(ang) * 1.6)), int(round(7.5 - math.sin(ang) * 1.6)), METAL[4])
    glass_window(s, 3, 4, 12, 10, METAL)
    inset(s, 5, 12, 10, 13, DARK, 0)
    s.flat(rect(6, 13, 9, 13), AMBERR[2])
    led(s, 12, 12, "F2B84A", frame % 2 == 0)
    return s


def processing_top():
    s = panel(METAL, 74)
    inset(s, 4, 4, 11, 11, DARK, 1)
    for (x, y) in ((5, 5), (6, 6), (7, 7), (8, 8), (9, 9), (10, 10)):
        s.put(x, y, DARK[3])
    s.flat(rect(6, 6, 9, 9), DARK[0])
    for (x, y, c) in ((7, 6, "E8C838"), (8, 7, "8CB64C"), (6, 8, "F0A030")):
        s.put(x, y, hexc(c))
    return s


def editor_front(frame=0, frames=8):
    s = panel(LAB, 75)
    s.flat(rect(2, 2, 13, 2), VIOLET[3])
    inset(s, 2, 4, 13, 10, DARK, 1)
    locus = 4 + (frame // 2) % 8
    for x in range(3, 14):
        t = (x + frame * 0.5) / 3.0 * math.pi
        a = 7 + math.sin(t) * 2.2
        b = 7 - math.sin(t) * 2.2
        s.put(x, int(round(a)), hexc("5CE8A0"))
        s.put(x, int(round(b)), hexc("3FA8E0"))
        if x == locus:
            for y in range(min(int(round(a)), int(round(b))), max(int(round(a)), int(round(b))) + 1):
                s.put(x, y, VIOLET[5])
    inset(s, 4, 12, 7, 13, DARK, 0)
    s.put(5, 12, hexc("3FE0C8"))
    s.put(6, 12, hexc("3FE0C8"))
    led(s, 11, 12, "C77DFF", frame % 4 < 2)
    led(s, 12, 12, "5CE88A", True)
    return s


def bench_front():
    s = planks(76)
    wood_frame(s)
    raised(s, 3, 3, 12, 10, PLANK_DARK)
    for (x, y) in ellipse(8.0, 6.5, 2.8, 2.8):
        s.put(x, y, GOLD_B[2])
    for (x, y) in ellipse(8.0, 6.5, 1.9, 1.9):
        s.put(x, y, GOLD_B[4])
    s.put(7, 5, GOLD_B[5])
    s.put(9, 8, GOLD_B[1])
    for i, (w, c) in enumerate(((9, "D08A4E"), (7, "C9D3DA"), (5, "F2C94C"))):
        for x in range(3, 3 + w):
            s.put(x, 12 + i, hexc(c))
    return s


def bench_top():
    s = Sprite()
    textured(s, rect(0, 0, 15, 15), FELT, 1, 3, 77, grain=0.4)
    for i in range(16):
        for (x, y) in ((i, 0), (0, i)):
            s.put(x, y, PLANK[4])
        for (x, y) in ((i, 15), (15, i)):
            s.put(x, y, PLANK[1])
    s.flat(rect(3, 4, 9, 11), PAPER_B[4])
    for y in (6, 8, 10):
        s.flat(rect(4, y, 8, y), ramp(hexc("3A4650"))[3])
    for (x, y) in ellipse(11.5, 10.5, 1.6, 1.6):
        s.put(x, y, GOLD_B[4])
    return s


def anim(fn, frames, **kw):
    return [fn(frame=i, frames=frames, **kw) for i in range(frames)]


def all_blocks():
    out = [
        ("genome_lab_front", anim(genome_lab_front, 8), 4), ("genome_lab_side", lab_side(40), None),
        ("genome_lab_top", lab_top(41), None),
        ("seed_bank_front", seed_bank_front(), None), ("seed_bank_side", lab_side(42, "5A7AC0"), None),
        ("seed_bank_top", lab_top(43), None),
        ("seed_vault_drawer_front", seed_vault_drawer_front(), None),
        ("mutagen_chamber_front", anim(mutagen_front, 8), 3), ("mutagen_chamber_side", mutagen_side(), None),
        ("mutagen_chamber_top", fan_top(LAB, 44), None),
        ("thermal_shock_unit_front", thermal_front(), None), ("thermal_shock_unit_side", thermal_side(), None),
        ("thermal_shock_unit_top", fan_top(METAL, 45), None),
        ("climate_controller_front", anim(climate_front, 4), 10), ("climate_controller_side", lab_side(46, "5AB8F2"), None),
        ("climate_controller_top", humidity_top(True), None),
        ("compost_bin_front", compost_front(), None), ("compost_bin_side", compost_side(), None), ("compost_bin_top", compost_top(), None),
        ("harvest_collector_front", collector_front(), None), ("harvest_collector_side", collector_side(), None),
        ("harvest_collector_top", collector_top(), None),
        ("nutrient_spreader_front", spreader_front(), None), ("nutrient_spreader_side", metal_side(47), None),
        ("nutrient_spreader_top", spreader_top(), None),
        ("pollination_unit_front", pollination_front(False), None),
        ("pollination_unit_front_active", [pollination_front(True, frame=i) for i in range(4)], 5),
        ("pollination_unit_side", metal_side(48), None), ("pollination_unit_top", landing_top(), None),
        ("humidifier_front", humidity_front(True), None), ("humidifier_side", lab_side(49, "5AA8F0"), None),
        ("humidifier_top", humidity_top(True), None),
        ("dehumidifier_front", humidity_front(False), None), ("dehumidifier_side", lab_side(50, "E0A040"), None),
        ("dehumidifier_top", humidity_top(False), None),
        ("grow_light_bottom", grow_bottom(), None), ("grow_light_side", grow_side(), None), ("grow_light_top", grow_top(), None),
        ("lime_hopper_top", hopper_top("ECECE6"), None), ("sulfur_hopper_top", hopper_top("E8D23A"), None),
        ("hopper_side", metal_side(51), None),
        ("lab_bottom", bottom(LAB, 52), None), ("metal_bottom", bottom(METAL, 53), None), ("wood_bottom", bottom(PLANK_DARK, 54), None),
        ("managed_soil_top", soil_top(SOIL, 60), None), ("managed_soil_top_moist", soil_top(SOIL, 60, moist=True), None),
        ("managed_soil_side", soil_side(SOIL, 61), None),
        ("breeding_plot_top", soil_top(SOIL, 62, framed=True), None),
        ("breeding_plot_top_moist", soil_top(SOIL, 62, moist=True, framed=True), None),
        ("breeding_plot_side", soil_side(SOIL, 63, framed=True), None),
        ("depleted_soil", depleted(), None),
        ("greenhouse_pane", greenhouse_pane(), None), ("isolation_screen", isolation_screen(), None),
        ("burn_pan", burn_pan(False), None), ("burn_pan_lit", [burn_pan(True, frame=i) for i in range(4)], 3),
        ("irrigation_line", irrigation(False), None), ("irrigation_line_fed", anim(irrigation, 8, fed=True), 2),
        ("sprinkler", sprinkler_side(), None), ("sprinkler_top", sprinkler_top(), None),
        ("trial_marker", trial_post(), None), ("trial_marker_plate", trial_plate(), None),
        ("canopy_bark_side", bark_side(), None), ("canopy_bark_top", bark_top(), None),
        ("canopy_leafmass", leafmass(), None), ("wild_vine_block", vine(), None),
        ("canopy_portal_frame", breach_frame(), None), ("canopy_portal", anim(breach, 8), 3),
        ("produce_exchange_front", anim(exchange_front, 4), 8), ("produce_exchange_side", collector_side(), None),
        ("produce_exchange_top", exchange_top(), None),
        ("contract_board_front", board_front(), None), ("contract_board_side", compost_side(), None),
        ("contract_board_top", board_top(), None),
        ("processing_station_front", anim(processing_front, 4), 3), ("processing_station_side", metal_side(78), None),
        ("processing_station_top", processing_top(), None),
        ("gene_editor_front", anim(editor_front, 8), 3), ("gene_editor_side", lab_side(79, "9A4FE0"), None),
        ("gene_editor_top", lab_top(80), None),
        ("show_bench_front", bench_front(), None), ("show_bench_side", compost_side(), None),
        ("show_bench_top", bench_top(), None),
    ]
    for kind, powder in (("lime", "ECECE6"), ("sulfur", "E8D23A")):
        for level in range(9):
            out.append((f"{kind}_hopper_front_{level}", hopper_front(powder, level), None))
    return out


RETIRED = ["genome_lab", "seed_bank", "seed_vault_drawer", "mutagen_chamber", "thermal_shock_unit", "climate_controller", "compost_bin",
           "harvest_collector", "nutrient_spreader", "pollination_unit", "pollination_unit_active", "humidifier", "dehumidifier",
           "grow_light", "lime_hopper", "sulfur_hopper"]


def write_all(texture_dir):
    for name, frames, frametime in all_blocks():
        save(os.path.join(texture_dir, "block", name + ".png"), frames, frametime)
    for name in RETIRED:
        path = os.path.join(texture_dir, "block", name + ".png")
        if os.path.exists(path):
            os.remove(path)
    return len(all_blocks())


def preview(path):
    from pixel import contact_sheet
    contact_sheet(path, [(n, f) for n, f, _ in all_blocks()], cols=10)


if __name__ == "__main__":
    if len(sys.argv) >= 3 and sys.argv[1] == "--sheet":
        preview(sys.argv[2])
        print("sheet written")
