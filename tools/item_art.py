import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixel import (Sprite, contact_sheet, ellipse, hexc, line, mix, poly, ramp, rect, save, shift, thick)

KRAFT = ramp(hexc("C9A46E"))
PAPER = ramp(hexc("E6DFCC"), base_index=3, value_step=0.05)
CREAM = ramp(hexc("F1EAD6"), base_index=4, value_step=0.04)
STEEL = ramp(hexc("8E9BA6"), value_step=0.2)
BRASS = ramp(hexc("C8962E"), value_step=0.2)
COPPER = ramp(hexc("C06A3C"))
WOOD = ramp(hexc("8E5E36"))
DARKWOOD = ramp(hexc("5C3A22"))
LEAF = ramp(hexc("4E9A34"))
LEAF_PALE = ramp(hexc("7CB84A"))
SOIL = ramp(hexc("5A3E26"))
BURLAP = ramp(hexc("A88A5A"))
TEAL = ramp(hexc("1F6B66"))
GOLD = ramp(hexc("D8AE48"), value_step=0.22)
VIOLET = ramp(hexc("9A4FE0"))
AMBER = ramp(hexc("D88A1E"), value_step=0.2)
RUST = ramp(hexc("B0582C"))
TWINE = ramp(hexc("A0783E"))
INK = ramp(hexc("3A4650"))
WHITE = (255, 255, 250, 255)
GLASS_EDGE = ramp(hexc("6E8C96"))


def glass_tint(sprite, mask, alpha=70):
    for (x, y) in mask:
        sprite.put(x, y, (196, 232, 240, alpha), GLASS_EDGE)


GLYPHS = {
    "N": ["1001", "1101", "1011", "1001", "1001"],
    "P": ["1110", "1001", "1001", "1110", "1000"],
    "K": ["1001", "1010", "1100", "1010", "1001"],
}


def glyph(s, ch, x, y, c):
    for j, row in enumerate(GLYPHS[ch]):
        for i, bit in enumerate(row):
            if bit == "1":
                s.put(x + i, y + j, c)


def motif(s, species, ox, oy):
    if species == "wheatline":
        s.flat(line(ox + 3, oy + 5, ox + 3, oy + 1), LEAF[2])
        for i, (dx, dy) in enumerate(((2, 0), (4, 1), (2, 2), (4, 3), (3, -1))):
            s.put(ox + dx, oy + dy, ramp(hexc("D8B04C"))[4 if i % 2 == 0 else 3])
    elif species == "maize":
        s.shade(ellipse(ox + 3.5, oy + 2.5, 1.6, 2.6), ramp(hexc("E8C030")), lo=2, hi=4, dome=0.2)
        s.flat({(ox + 1, oy + 4), (ox + 1, oy + 3), (ox + 5, oy + 4), (ox + 6, oy + 3)}, LEAF[3])
    elif species == "soybean":
        pod = thick(line(ox + 1, oy + 4, ox + 5, oy + 1), 1.6)
        s.shade(pod, ramp(hexc("8DB850")), lo=2, hi=4, dome=0.3)
        for (dx, dy) in ((2, 3), (3, 2), (4, 1)):
            s.put(ox + dx, oy + dy, ramp(hexc("B8D878"))[4])
    elif species == "clover":
        for (dx, dy) in ((2, 1), (4, 1), (3, 3)):
            s.shade(ellipse(ox + dx + 0.5, oy + dy + 0.5, 1.1, 1.1), LEAF, lo=2, hi=4, dome=0.1)
        s.put(ox + 3, oy + 2, hexc("F0B8D8"))
        s.put(ox + 3, oy + 5, LEAF[1])
    elif species == "tuber":
        s.shade(ellipse(ox + 3, oy + 3, 2.6, 1.8), ramp(hexc("A47A4E")), lo=1, hi=4, dome=0.3)
        s.put(ox + 2, oy + 3, ramp(hexc("A47A4E"))[1])
        s.put(ox + 3, oy + 0, LEAF[4])
        s.put(ox + 4, oy + 1, LEAF[3])
    elif species == "brassica":
        s.shade(ellipse(ox + 3, oy + 2.8, 2.4, 2.2), ramp(hexc("4E9C90")), lo=1, hi=4, dome=0.4)
        s.put(ox + 3, oy + 2, ramp(hexc("4E9C90"))[5])
        s.put(ox + 2, oy + 3, ramp(hexc("4E9C90"))[2])
    elif species == "gourd":
        s.shade(ellipse(ox + 3, oy + 3.2, 2.4, 1.8), ramp(hexc("E8902A")), lo=1, hi=4, dome=0.3)
        s.put(ox + 3, oy + 3, ramp(hexc("E8902A"))[2])
        s.put(ox + 3, oy + 0, WOOD[3])
        s.put(ox + 3, oy + 1, WOOD[2])
    elif species == "fiber_flax":
        s.flat(line(ox + 3, oy + 5, ox + 3, oy + 2), LEAF[2])
        blue = ramp(hexc("5C7EE0"))
        for (dx, dy) in ((2, 1), (4, 1), (3, 0), (2, 2), (4, 2)):
            s.put(ox + dx, oy + dy, blue[4 if dy == 0 else 3])
        s.put(ox + 3, oy + 1, hexc("F4E070"))


SPECIES_BAND = {"wheatline": "C49A3A", "maize": "D8A820", "soybean": "6E9C3C", "clover": "C86AA0", "tuber": "8A5E38",
                "brassica": "3E8A80", "gourd": "D87A22", "fiber_flax": "4A6AC8"}


def seed_packet(species):
    s = Sprite()
    body = rect(3, 3, 12, 14)
    s.shade(body, KRAFT, lo=1, hi=4, mode="cyl_x", dome=0.15)
    for x in range(3, 13):
        s.put(x, 2, KRAFT[4], KRAFT)
        s.put(x, 3, KRAFT[2], KRAFT)
    for x in range(3, 13, 2):
        s.put(x, 1, KRAFT[3], KRAFT)
    label = rect(4, 5, 11, 11)
    s.flat(label, CREAM[4], CREAM)
    s.flat({(x, 5) for x in range(4, 12)}, CREAM[5], CREAM)
    s.flat({(11, y) for y in range(5, 12)}, CREAM[2], CREAM)
    s.flat({(x, 11) for x in range(4, 12)}, CREAM[2], CREAM)
    motif(s, species, 5, 6)
    band = ramp(hexc(SPECIES_BAND[species]))
    s.shade(rect(3, 12, 12, 13), band, lo=2, hi=4, mode="cyl_x", dome=0.0)
    s.put(5, 13, band[5])
    s.put(8, 12, band[5])
    s.put(10, 13, band[5])
    s.flat({(x, 14) for x in range(3, 13)}, KRAFT[1], KRAFT)
    s.spec(3, 4, KRAFT[5])
    s.spec(3, 5, KRAFT[5])
    s.outline()
    return s


def wheatline_sheaf():
    s = Sprite()
    straw = ramp(hexc("C9A24A"))
    grain = ramp(hexc("E0B44A"), value_step=0.18)
    ears = [((6, 15), (3, 7)), ((8, 15), (8, 6)), ((10, 15), (13, 7))]
    for (a, b) in ears:
        s.flat(line(a[0], a[1], b[0], b[1]), straw[2], straw)
    for (a, b) in ears:
        tx, ty = b
        lean = -1 if a[0] > b[0] else (1 if a[0] < b[0] else 0)
        for k in range(5):
            y = ty - 5 + k
            x = tx + (lean if k < 2 else 0)
            s.put(x - 1, y, grain[4 if k % 2 == 0 else 2], grain)
            s.put(x + 1, y, grain[2 if k % 2 == 0 else 4], grain)
            s.put(x, y, grain[3], grain)
        s.put(tx + lean - 1, ty - 7, straw[4], straw)
        s.put(tx + lean + 1, ty - 7, straw[4], straw)
        s.put(tx + lean, ty - 6, straw[3], straw)
    twine = rect(5, 11, 11, 12)
    s.shade(twine, TWINE, lo=1, hi=4, mode="cyl_y", dome=0.0)
    s.put(11, 13, TWINE[2], TWINE)
    s.put(12, 14, TWINE[3], TWINE)
    s.outline()
    s.spec(8, 1)
    s.spec(3, 2)
    return s


def maize_cob():
    s = Sprite()
    kern = ramp(hexc("ECC23A"), value_step=0.18)
    cob = thick(line(5, 11, 10, 4), 4.2)
    for (x, y) in cob:
        u = x + y
        v = x - y
        lvl = 4 if (u + v // 2) % 2 == 0 else 3
        d = -((x - 7.5) * -0.62 + (y - 7.5) * -0.78) / 6.0
        lvl += 1 if d > 0.35 else (-1 if d < -0.3 else 0)
        if (v % 3) == 0:
            lvl -= 1
        s.put(x, y, kern[max(1, min(5, lvl))], kern)
    husk = ramp(hexc("7EAE48"))
    s.shade(poly([(1.5, 14.5), (4.5, 8.5), (7.5, 10.5), (6.5, 13.5), (3.5, 15.5)]), husk, lo=1, hi=4, dome=0.3)
    s.shade(poly([(5.5, 15.5), (9.5, 9.5), (11.5, 10.5), (9.5, 14.5)]), ramp(hexc("6A9E3E")), lo=1, hi=4, dome=0.3)
    s.flat(line(3, 13, 6, 10), husk[5], husk)
    silk = ramp(hexc("E8D890"))
    for (x, y) in ((11, 2), (12, 1), (12, 3), (13, 2)):
        s.put(x, y, silk[4], silk)
    s.outline()
    s.spec(8, 5)
    s.spec(9, 6, kern[5])
    return s


def soybean_pod():
    s = Sprite()
    pod = ramp(hexc("8CB64C"))
    body = set()
    pts = [(3, 12), (5, 10), (7, 8), (9, 7), (11, 5), (12, 3)]
    for i in range(len(pts) - 1):
        body |= thick(line(pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1]), 3.4)
    s.shade(body, pod, lo=1, hi=4, dome=0.45)
    for (cx, cy) in ((5.5, 10.5), (8.0, 8.0), (10.5, 5.5)):
        s.shade(ellipse(cx, cy, 1.5, 1.5), pod, lo=3, hi=5, dome=0.6)
    s.flat(line(4, 13, 13, 4), pod[2], pod)
    for (x, y) in ((2, 11), (6, 7), (9, 4), (14, 6), (12, 9)):
        if s.get(x, y) is None:
            s.put(x, y, (220, 236, 190, 150))
    s.flat(line(12, 3, 13, 1), WOOD[3], WOOD)
    s.outline()
    s.spec(5, 9)
    s.spec(8, 7)
    s.spec(10, 4)
    return s


def clover_bundle():
    s = Sprite()
    s.flat(line(8, 15, 6, 9), LEAF[2], LEAF)
    s.flat(line(8, 15, 10, 9), LEAF[2], LEAF)
    s.flat(line(8, 15, 8, 6), LEAF[1], LEAF)
    for (cx, cy) in ((4.5, 9.5), (11.5, 10.5)):
        for (ox, oy) in ((-1.4, -1.2), (1.4, -1.2), (0, 1.3)):
            s.shade(ellipse(cx + ox, cy + oy, 1.5, 1.5), LEAF_PALE, lo=1, hi=4, dome=0.35)
        s.put(int(cx), int(cy), LEAF[1], LEAF)
    flower = ramp(hexc("E89AC4"), value_step=0.14)
    head = ellipse(8.5, 4.5, 3.2, 3.0)
    s.shade(head, flower, lo=1, hi=5, dome=0.45)
    for (x, y) in head:
        if (x + y * 2) % 3 == 0 and s.get(x, y) == flower[3]:
            s.put(x, y, flower[2], flower)
    s.outline()
    s.spec(7, 3)
    return s


def tuber():
    s = Sprite()
    skin = ramp(hexc("A27450"))
    body = ellipse(8.5, 9.5, 5.6, 4.4) | ellipse(5.0, 7.5, 3.0, 3.0) | ellipse(11.5, 12.0, 2.8, 2.2)
    s.shade(body, skin, lo=1, hi=4, dome=0.5)
    for (x, y) in ((6, 9), (10, 8), (9, 12), (12, 11), (4, 7)):
        s.put(x, y, skin[1], skin)
        s.put(x + 1, y - 1, skin[4], skin)
    s.flat({(4, 4), (4, 3), (5, 2)}, LEAF[3], LEAF)
    s.put(3, 2, LEAF_PALE[4], LEAF_PALE)
    s.put(6, 2, LEAF[4], LEAF)
    for (x, y) in ((7, 13), (13, 9), (5, 11)):
        s.put(x, y, SOIL[2], SOIL)
    s.outline()
    s.spec(6, 6)
    s.spec(9, 7, skin[5])
    return s


def brassica_head():
    s = Sprite()
    outer = ramp(hexc("5E9A6A"))
    leafr = ramp(hexc("86BE6E"))
    heart = ramp(hexc("C4E29A"), value_step=0.1)
    s.shade(ellipse(3.5, 11.5, 3.2, 2.8), outer, lo=1, hi=4, dome=0.35)
    s.shade(ellipse(12.5, 11.5, 3.2, 2.8), outer, lo=1, hi=4, dome=0.35)
    head = ellipse(8.0, 8.5, 5.8, 5.4)
    s.shade(head, leafr, lo=1, hi=4, dome=0.5)
    s.shade(ellipse(7.5, 7.5, 3.0, 2.8), heart, lo=2, hi=5, dome=0.6)
    for (cx, cy, rr) in ((8.0, 9.5, 4.3), (8.0, 8.0, 2.6)):
        for (x, y) in ellipse(cx, cy, rr, rr * 0.9) - ellipse(cx, cy + 0.6, rr - 0.9, rr * 0.9 - 0.9):
            if y > cy - 0.5 and (x, y) in head:
                s.put(x, y, leafr[1], leafr)
    for pts in ((8, 9, 4, 12), (8, 9, 12, 12), (8, 10, 8, 13)):
        for (x, y) in line(*pts):
            if (x, y) in head:
                s.put(x, y, heart[4], heart)
    s.outline()
    s.spec(6, 5)
    s.spec(7, 5, heart[5])
    return s


def gourd():
    s = Sprite()
    rind = ramp(hexc("E88C28"), value_step=0.17)
    body = ellipse(8.0, 11.0, 5.4, 4.2) | ellipse(8.0, 6.5, 3.2, 3.0)
    s.shade(body, rind, lo=1, hi=4, dome=0.5)
    for rx in (5, 8, 11):
        for (x, y) in body:
            if x == rx and s.get(x, y) is not None and y > 7:
                s.put(x, y, rind[2], rind)
    s.flat({(8, 3), (8, 2), (9, 1), (10, 1)}, WOOD[3], WOOD)
    s.shade(ellipse(5.0, 3.5, 2.2, 1.3), LEAF, lo=2, hi=4, dome=0.2)
    s.outline()
    s.spec(6, 5)
    s.spec(5, 9)
    s.spec(6, 9, rind[5])
    return s


def flax_fiber():
    s = Sprite()
    fib = ramp(hexc("DCCDA0"), value_step=0.1)
    for i, x in enumerate(range(4, 12)):
        strands = line(x, 7, int(round(2.5 + i * 1.6)), 1) | line(x, 9, int(round(3.5 + i * 1.3)), 14)
        for (px, py) in strands:
            s.put(px, py, fib[2 + (i * 7) % 3], fib)
    band = ramp(hexc("4A6AC8"))
    s.shade(rect(4, 7, 11, 9), band, lo=1, hi=4, mode="cyl_y", dome=0.0)
    s.put(11, 8, band[5], band)
    s.outline()
    s.spec(6, 3)
    return s


GENE_COLORS = ["E0B040", "6FBF4F", "60A8E0", "D08A40", "F0E070", "B070C0", "8A6A40", "C05050"]


def genome_card(frame=0, frames=8):
    s = Sprite()
    card = rect(1, 3, 14, 12)
    s.shade(card, CREAM, lo=3, hi=5, dome=0.0, mode="cyl_y")
    s.flat(rect(1, 3, 14, 4), TEAL[3], TEAL)
    s.flat({(x, 3) for x in range(1, 15)}, TEAL[4], TEAL)
    s.put(13, 4, GOLD[4], GOLD)
    widths = [(3, 2), (2, 3), (3, 3), (1, 3), (2, 2), (3, 1), (2, 3), (3, 2)]
    for i in range(4):
        y = 6 + i * 1 + (i // 2)
        for half in range(2):
            gi = i * 2 + half
            a, b = widths[gi]
            col = ramp(hexc(GENE_COLORS[gi]))
            x0 = 2 + half * 4
            s.flat({(x0 + k, y) for k in range(a)}, col[3], col)
    for i, x in enumerate(range(10, 14)):
        h = 4 if i % 2 == 0 else 3
        for y in range(6, 6 + h):
            s.put(x, y, INK[2 + (i % 2)], INK)
    for x in range(2, 14):
        hue = ((x + frame) % 6)
        c = [hexc("8FE0D8"), hexc("A8C8F8"), hexc("D8B0F0"), hexc("F0D8A0"), hexc("B8F0B0"), hexc("90D8F8")][hue]
        s.put(x, 11, c, CREAM)
    s.outline()
    sx = -2 + frame * 18 // frames
    for y in range(3, 13):
        for x in (sx, sx + 1):
            if s.get(x, y) is not None and 1 <= x <= 14:
                s.put(x, y, (255, 255, 255, 120 if x == sx else 70))
    return s


def soil_probe(frame=0):
    s = Sprite()
    shaft = thick(line(7, 8, 13, 14), 1.6)
    s.shade(shaft, STEEL, lo=1, hi=4, dome=0.5)
    s.shade(thick(line(13, 14, 14, 15), 1.2), COPPER, lo=2, hi=5, dome=0.3)
    head = rect(1, 1, 8, 7)
    s.shade(head, ramp(hexc("3C4A52")), lo=1, hi=4, dome=0.2)
    s.flat({(x, 1) for x in range(1, 9)}, ramp(hexc("3C4A52"))[4])
    screen = rect(2, 2, 6, 4)
    lcd = ramp(hexc("6AD878"))
    s.flat(screen, lcd[1], lcd)
    bars = [(2, 3), (3, 2), (4, 3), (5, 1), (6, 2)]
    for i, (x, hgt) in enumerate(bars):
        hh = 1 + ((hgt + frame + i) % 3)
        for k in range(hh):
            s.put(x, 4 - k, lcd[4], lcd)
    led = hexc("F05050") if frame % 2 == 0 else hexc("6A2A2A")
    s.put(7, 6, led)
    s.put(2, 6, ramp(hexc("3C4A52"))[5])
    s.put(4, 6, ramp(hexc("3C4A52"))[5])
    s.outline()
    s.spec(8, 9)
    return s


def hand_lens(frame=0, frames=8):
    s = Sprite()
    handle = thick(line(9, 10, 14, 15), 2.2)
    s.shade(handle, WOOD, lo=1, hi=4, dome=0.5)
    s.shade(thick(line(9, 9, 10, 10), 2.4), BRASS, lo=2, hi=5, dome=0.4)
    ring = ellipse(6.5, 6.5, 5.2, 5.2) - ellipse(6.5, 6.5, 3.6, 3.6)
    s.shade(ring, BRASS, lo=1, hi=5, dome=0.4)
    glass = ellipse(6.5, 6.5, 3.6, 3.6)
    lens = ramp(hexc("9ED6E8"))
    s.shade(glass, lens, lo=2, hi=4, dome=0.6)
    s.outline()
    g = (frame * 2) % (frames * 2) - 3
    for (x, y) in glass:
        if x - y == g or x - y == g + 1:
            s.put(x, y, (255, 255, 255, 200 if x - y == g else 110))
    s.spec(4, 4)
    s.spec(5, 4)
    return s


def pollination_brush():
    s = Sprite()
    s.shade(thick(line(2, 14, 8, 8), 1.8), WOOD, lo=1, hi=4, dome=0.5)
    s.shade(thick(line(8, 8, 10, 6), 2.2), BRASS, lo=2, hi=5, dome=0.3)
    tuft = ellipse(11.8, 4.2, 3.2, 2.8)
    bristle = ramp(hexc("EEE2B8"))
    s.shade(tuft, bristle, lo=2, hi=5, dome=0.5)
    pollen = ramp(hexc("F2C42C"))
    for (x, y) in ((12, 2), (13, 4), (11, 3), (14, 3), (12, 5)):
        s.put(x, y, pollen[3 + (x % 2)], pollen)
    s.outline()
    for (x, y, a) in ((15, 1, 220), (14, 0, 160), (15, 6, 140)):
        s.put(x, y, (pollen[4][0], pollen[4][1], pollen[4][2], a))
    return s


def isolation_bag():
    s = Sprite()
    body = poly([(4.5, 6.5), (11.5, 6.5), (13.5, 14.5), (2.5, 14.5)])
    s.shade(body, PAPER, lo=1, hi=4, mode="cyl_x", dome=0.3)
    for (x0, x1) in ((5, 4), (8, 8), (11, 12)):
        for (px, py) in line(x0, 8, x1, 14):
            if (px, py) in body:
                s.put(px, py, PAPER[2], PAPER)
    for (x, y) in rect(6, 9, 10, 12):
        s.put(x, y, (150, 170, 160, 255) if (x + y) % 2 else PAPER[4], PAPER)
    s.shade(poly([(5.5, 3.5), (10.5, 3.5), (11.5, 6.5), (4.5, 6.5)]), PAPER, lo=2, hi=4, mode="cyl_x", dome=0.0)
    for x in (6, 8, 10):
        s.put(x, 4, PAPER[1], PAPER)
    string = ramp(hexc("C83E3E"))
    s.flat(rect(4, 6, 11, 6), string[3], string)
    s.flat({(12, 6), (13, 7), (13, 8), (12, 5)}, string[4], string)
    s.outline()
    s.spec(4, 9)
    return s


def grafting_knife():
    s = Sprite()
    s.shade(thick(line(1, 14, 6, 9), 2.6), DARKWOOD, lo=1, hi=4, dome=0.5)
    for (x, y) in ((3, 12), (5, 10)):
        s.put(x, y, BRASS[5], BRASS)
    s.shade(thick(line(6, 9, 7, 8), 2.6), BRASS, lo=2, hi=4, dome=0.2)
    blade = poly([(6.5, 7.5), (10.5, 3.5), (13.5, 1.5), (15.0, 2.5), (14.5, 5.0), (13.0, 5.0), (11.0, 7.0), (8.5, 9.5)])
    s.shade(blade, STEEL, lo=1, hi=5, dome=0.45)
    s.flat(line(8, 7, 13, 2), STEEL[5], STEEL)
    s.flat({(14, 5), (13, 6)}, STEEL[1], STEEL)
    s.outline()
    s.spec(14, 2)
    return s


def field_manual(frame=0, frames=8):
    s = Sprite()
    cover = rect(2, 1, 12, 14)
    s.shade(cover, TEAL, lo=1, hi=4, dome=0.25, mode="cyl_x")
    s.shade(rect(13, 2, 13, 14), CREAM, lo=2, hi=4, mode="flat")
    for y in range(3, 14, 2):
        s.put(13, y, CREAM[2], CREAM)
    s.shade(rect(2, 1, 3, 14), GOLD, lo=1, hi=4, mode="cyl_x", dome=0.0)
    for (x, y) in ((11, 1), (12, 1), (12, 2), (11, 14), (12, 14), (12, 13)):
        s.put(x, y, GOLD[4], GOLD)
    for y in range(4, 12):
        t = (y - 4) / 7.0 * math.pi * 1.5
        a = 7.5 + math.sin(t) * 2.2
        b = 7.5 - math.sin(t) * 2.2
        s.put(int(round(a)), y, GOLD[4], GOLD)
        s.put(int(round(b)), y, ramp(hexc("6FE0D0"))[3])
        if y % 2 == 0:
            for x in range(min(int(round(a)), int(round(b))) + 1, max(int(round(a)), int(round(b)))):
                s.put(x, y, TEAL[5], TEAL)
    ribbon = ramp(hexc("C83A3A"))
    s.flat({(9, 14), (9, 15), (10, 15)}, ribbon[3], ribbon)
    s.outline()
    gy = 4 + (frame % frames)
    if gy <= 11:
        for x in range(4, 12):
            c = s.get(x, gy)
            if c is not None and c[0] > 150:
                s.put(x, gy, WHITE)
    s.spec(4, 2)
    return s


def field_notebook():
    s = Sprite()
    s.shade(rect(3, 1, 12, 14), ramp(hexc("4E7E3A")), lo=1, hi=4, mode="cyl_x", dome=0.2)
    s.shade(rect(13, 2, 13, 14), CREAM, lo=2, hi=4, mode="flat")
    s.shade(rect(10, 1, 10, 14), ramp(hexc("2A2E30")), lo=2, hi=3, mode="flat")
    s.flat(rect(5, 4, 8, 6), CREAM[4], CREAM)
    s.flat({(5, 5), (6, 5), (7, 5)}, CREAM[2], CREAM)
    pencil = ramp(hexc("E8B830"))
    s.shade(thick(line(1, 12, 9, 4), 1.4), pencil, lo=2, hi=4, dome=0.3)
    s.flat({(9, 4), (10, 3)}, WOOD[4], WOOD)
    s.put(11, 2, INK[1], INK)
    s.flat({(1, 12), (0, 13)}, ramp(hexc("E88A9A"))[3])
    s.outline()
    return s


def pedigree_sheet():
    s = Sprite()
    sheet = poly([(2.5, 1.5), (11.5, 1.5), (13.5, 3.5), (13.5, 14.5), (2.5, 14.5)])
    s.shade(sheet, CREAM, lo=3, hi=5, dome=0.0, mode="cyl_x")
    s.flat({(12, 2), (12, 3), (13, 3)}, CREAM[2], CREAM)
    s.flat({(x, 3) for x in range(4, 10)}, INK[3], INK)
    nodes = {(8, 5): 0, (5, 8): 1, (11, 8): 2, (4, 12): 3, (7, 12): 4, (10, 12): 5, (12, 12): 6}
    links = [((8, 5), (5, 8)), ((8, 5), (11, 8)), ((5, 8), (4, 12)), ((5, 8), (7, 12)), ((11, 8), (10, 12)), ((11, 8), (12, 12))]
    for a, b in links:
        s.flat(line(a[0], a[1] + 1, b[0], b[1] - 1), INK[3], INK)
    for (x, y), i in nodes.items():
        c = ramp(hexc(GENE_COLORS[i]))
        s.put(x, y, c[3], c)
    s.outline()
    return s


def trial_tag():
    s = Sprite()
    plate = rect(3, 2, 12, 8)
    s.shade(plate, CREAM, lo=3, hi=5, mode="cyl_y", dome=0.2)
    s.shade(poly([(6.5, 8.5), (9.5, 8.5), (8.5, 14.5), (7.5, 14.5)]), CREAM, lo=2, hi=4, mode="cyl_x", dome=0.0)
    s.flat({(x, 4) for x in range(5, 11)}, INK[3], INK)
    s.flat({(x, 6) for x in range(5, 9)}, INK[3], INK)
    s.put(10, 6, LEAF[4], LEAF)
    s.put(11, 5, LEAF[4], LEAF)
    s.put(4, 3, ramp(hexc("C04848"))[3])
    s.outline()
    return s


def burlap_sack(content, content_ramp, specks=()):
    s = Sprite()
    body = poly([(3.5, 6.5), (12.5, 6.5), (13.5, 14.5), (2.5, 14.5)])
    s.shade(body, BURLAP, lo=1, hi=4, dome=0.35)
    for (x, y) in body:
        if (x * 3 + y) % 4 == 0:
            s.put(x, y, mix(s.get(x, y), BURLAP[1], 0.5), BURLAP)
    s.shade(rect(3, 5, 12, 6), BURLAP, lo=2, hi=5, mode="cyl_y", dome=0.0)
    mound = ellipse(8, 5.0, 4.6, 2.6)
    s.shade(mound, content_ramp, lo=1, hi=4, dome=0.5)
    for (x, y, c) in specks:
        s.put(x, y, c)
    s.outline()
    return s


def compost():
    leaf = LEAF_PALE
    return burlap_sack("compost", SOIL, ((6, 3, leaf[3]), (10, 4, leaf[4]), (8, 2, SOIL[5]), (5, 5, SOIL[5])))


def lime_powder():
    s = Sprite()
    bag = rect(4, 3, 12, 13)
    s.shade(bag, KRAFT, lo=1, hi=4, mode="cyl_x", dome=0.2)
    for x in range(4, 13, 2):
        s.put(x, 2, KRAFT[3], KRAFT)
    s.flat(rect(6, 6, 10, 9), CREAM[5], CREAM)
    s.flat({(x, 7) for x in range(7, 10)}, ramp(hexc("5A9AC0"))[3])
    s.flat({(x, 8) for x in range(7, 9)}, ramp(hexc("5A9AC0"))[2])
    lime = ramp(hexc("ECECE8"), base_index=4, value_step=0.06, hue_step=0.0, sat_step=0.0)
    pile = ellipse(5.0, 13.0, 4.2, 2.6)
    s.shade(pile, lime, lo=1, hi=5, dome=0.45)
    for (x, y) in ((10, 14), (11, 15), (9, 15)):
        s.put(x, y, lime[4], lime)
    s.outline()
    s.spec(4, 4)
    return s


def sulfur_powder():
    s = Sprite()
    jar = rect(4, 4, 11, 14)
    s.flat(jar, (176, 208, 214, 255), GLASS_EDGE)
    crystals = ramp(hexc("E8D23A"), value_step=0.18)
    fill = rect(5, 8, 10, 13)
    s.shade(fill, crystals, lo=1, hi=4, dome=0.3)
    for (x, y) in ((6, 9), (8, 10), (9, 12), (5, 12), (7, 8)):
        s.put(x, y, crystals[5], crystals)
    glass_tint(s, jar, 60)
    s.shade(rect(4, 2, 11, 3), WOOD, lo=1, hi=4, mode="cyl_y", dome=0.0)
    s.outline()
    s.flat({(5, 5), (5, 6), (5, 7)}, (255, 255, 255, 200))
    return s


def nutrient_bag(letter, color):
    s = Sprite()
    r = ramp(hexc(color))
    body = poly([(3.5, 3.5), (12.5, 3.5), (12.5, 14.5), (3.5, 14.5)])
    s.shade(body, r, lo=1, hi=4, mode="cyl_x", dome=0.3)
    for x in range(3, 13):
        s.put(x, 3, r[4] if x % 2 else r[2], r)
    s.flat({(x, 2) for x in range(4, 12, 2)}, r[3], r)
    label = rect(5, 6, 10, 12)
    s.flat(label, CREAM[5], CREAM)
    s.flat({(10, y) for y in range(6, 13)}, CREAM[3], CREAM)
    s.flat({(x, 12) for x in range(5, 11)}, CREAM[3], CREAM)
    glyph(s, letter, 6, 7, r[1])
    s.outline()
    s.spec(4, 5)
    return s


def mutagen_vial(frame=0, frames=8):
    s = Sprite()
    liquid = ramp(hexc("A04CE8"), value_step=0.2)
    inner = rect(6, 8, 9, 13) | {(7, 14), (8, 14)}
    s.shade(inner, liquid, lo=2, hi=5, mode="cyl_x", dome=0.3, bias=0.05 * math.sin(frame / frames * 2 * math.pi))
    for k, (bx, phase) in enumerate(((7, 0), (8, 3), (6, 5))):
        by = 13 - ((frame + phase) % frames) * 6 // frames
        if 8 <= by <= 13:
            s.put(bx, by, liquid[5])
    s.flat({(x, 8) for x in range(6, 10)}, liquid[5], liquid)
    tube = rect(5, 4, 10, 13) | {(6, 14), (7, 15), (8, 15), (9, 14)}
    glass_tint(s, tube, 70)
    s.shade(rect(6, 1, 9, 3), ramp(hexc("A87850")), lo=1, hi=4, mode="cyl_x", dome=0.0)
    s.outline()
    s.flat({(6, 5), (6, 6), (6, 7)}, (255, 255, 255, 210))
    s.put(9, 10, (255, 255, 255, 120))
    return s


def blight_sample(frame=0, frames=8):
    s = Sprite()
    tube = rect(6, 4, 9, 13) | {(7, 14), (8, 14)}
    mass = ramp(hexc("A8582E"))
    s.shade(rect(6, 11, 9, 13) | {(7, 14), (8, 14)}, mass, lo=1, hi=4, dome=0.4)
    for k in range(5):
        a = frame / frames * 2 * math.pi + k * 1.256
        x = int(round(7.5 + math.cos(a) * 1.3))
        y = int(round(8 + math.sin(a * 0.7 + k) * 2.2))
        s.put(x, y, ramp(hexc("E0A060"))[3 + (k % 2)])
    glass_tint(s, tube, 75)
    cap = rect(5, 1, 10, 3)
    s.shade(cap, ramp(hexc("E8C020")), lo=2, hi=4, mode="cyl_x", dome=0.0)
    for x in range(5, 11):
        if x % 2 == 0:
            s.put(x, 2, ramp(hexc("2A2A2A"))[3])
    s.outline()
    s.flat({(6, 5), (6, 6), (6, 7)}, (255, 255, 255, 210))
    return s


def canopy_resin(frame=0, frames=8):
    s = Sprite()
    drop = ellipse(8.0, 10.0, 4.6, 4.6) | poly([(8.0, 1.5), (11.6, 8.0), (4.4, 8.0)])
    s.shade(drop, AMBER, lo=1, hi=5, dome=0.55, light=(0.55, 0.7))
    s.shade(ellipse(7.0, 9.0, 2.2, 2.5), AMBER, lo=3, hi=5, dome=0.6)
    s.put(9, 11, AMBER[2], AMBER)
    s.put(10, 12, AMBER[1], AMBER)
    s.outline()
    s.spec(6, 7)
    s.spec(6, 8)
    s.spec(7, 5, AMBER[5])
    a = frame / frames * 2 * math.pi
    gx = int(round(8 + math.cos(a) * 3.2))
    gy = int(round(10 + math.sin(a) * 3.2))
    if s.get(gx, gy) is not None:
        s.put(gx, gy, (255, 248, 220, 230))
    return s


def canopy_key(frame=0, frames=8):
    s = Sprite()
    s.shade(thick(line(7, 7, 14, 14), 1.8), BRASS, lo=1, hi=4, dome=0.45)
    s.shade(rect(9, 12, 10, 14), BRASS, lo=2, hi=4, mode="cyl_x", dome=0.0)
    s.shade(rect(11, 14, 12, 15), BRASS, lo=2, hi=4, mode="cyl_x", dome=0.0)
    bow = ellipse(5.0, 5.0, 4.4, 4.4) - ellipse(5.0, 5.0, 2.4, 2.4)
    s.shade(bow, BRASS, lo=1, hi=5, dome=0.4)
    core = ellipse(5.0, 5.0, 2.4, 2.4)
    pulse = 0.12 * math.sin(frame / frames * 2 * math.pi)
    s.shade(core, ramp(hexc("E0701A"), value_step=0.22), lo=1, hi=5, dome=0.7, bias=pulse)
    s.outline()
    s.spec(4, 4)
    s.spec(3, 2)
    return s


def wild_seed():
    s = Sprite()
    husk = ramp(hexc("B48A4A"))
    dark = ramp(hexc("6A4A32"))
    wing = ramp(hexc("D8C08A"), value_step=0.1)
    s.shade(poly([(3.5, 3.5), (8.5, 6.5), (7.5, 11.5), (3.0, 9.5)]), husk, lo=1, hi=4, dome=0.5)
    for (x, y) in line(4, 5, 6, 10):
        s.put(x, y, dark[3], dark)
    s.shade(ellipse(10.5, 11.0, 2.6, 2.4), dark, lo=1, hi=5, dome=0.55)
    s.shade(poly([(9.5, 2.5), (14.5, 1.5), (13.5, 4.5), (10.5, 6.5)]), wing, lo=1, hi=4, dome=0.3)
    s.shade(ellipse(10.0, 6.0, 1.6, 1.4), husk, lo=2, hi=4, dome=0.4)
    for (x, y) in line(11, 5, 14, 2):
        s.put(x, y, wing[2], wing)
    s.flat({(6, 12), (6, 13), (5, 14)}, LEAF[3], LEAF)
    s.flat({(7, 13), (4, 14)}, LEAF_PALE[4], LEAF_PALE)
    s.outline()
    s.spec(4, 6)
    s.spec(9, 10)
    s.spec(10, 9, dark[5])
    return s


OIL = ramp(hexc("D8B428"), value_step=0.18)
CERAMIC = ramp(hexc("D8D0C0"), base_index=3, value_step=0.08)
BRINE = ramp(hexc("A8C870"), value_step=0.12)
PUREE = ramp(hexc("E88A2A"), value_step=0.16)
LINEN = ramp(hexc("D8CCA8"), value_step=0.08)
BLUEPRINT = ramp(hexc("2E5EA8"), value_step=0.14)
CYAN = ramp(hexc("3FE0C8"), value_step=0.2)
FILM = ramp(hexc("8ED8D0"), value_step=0.1)


def green_manure():
    clover = ramp(hexc("58A848"))
    return burlap_sack("green", LEAF, ((5, 3, clover[5]), (7, 2, LEAF_PALE[4]), (10, 3, clover[4]), (9, 5, LEAF[5]), (6, 5, SOIL[3])))


def field_meal():
    s = Sprite()
    sack = poly([(4.5, 3.5), (11.5, 3.5), (12.5, 13.5), (3.5, 13.5)])
    s.shade(sack, KRAFT, lo=1, hi=4, mode="cyl_x", dome=0.25)
    s.shade(rect(4, 2, 11, 3), KRAFT, lo=2, hi=5, mode="cyl_y", dome=0.0)
    for x in range(4, 12, 2):
        s.put(x, 3, TWINE[2], TWINE)
    s.flat(rect(5, 6, 10, 10), CREAM[5], CREAM)
    s.flat({(7, 7), (8, 7), (7, 8), (8, 8), (8, 9)}, ramp(hexc("C8A040"))[3])
    s.flat({(6, 8), (9, 7)}, ramp(hexc("C8A040"))[4])
    meal = ramp(hexc("E8D8A8"), base_index=4, value_step=0.06)
    s.shade(ellipse(12.0, 14.0, 3.4, 1.8), meal, lo=1, hi=5, dome=0.45)
    s.outline()
    s.spec(5, 5)
    return s


def soy_oil(frame=0, frames=8):
    s = Sprite()
    neck = rect(7, 2, 9, 4)
    body = poly([(5.5, 5.5), (10.5, 5.5), (12.5, 8.5), (12.5, 14.5), (3.5, 14.5), (3.5, 8.5)])
    s.flat(body | neck, (176, 208, 214, 255), GLASS_EDGE)
    level = 9 + (1 if frame % frames >= frames // 2 else 0)
    oil = {(x, y) for (x, y) in body if y >= level}
    s.shade(oil, OIL, lo=1, hi=5, mode="cyl_x", dome=0.35)
    mx = 4 + (frame * 9 // frames) % 9
    if (mx, level) in oil:
        s.put(mx, level, OIL[5], OIL)
    glass_tint(s, body | neck, 55)
    s.shade(rect(7, 1, 9, 1), WOOD, lo=2, hi=4, mode="cyl_x", dome=0.0)
    s.flat(rect(5, 11, 10, 12), CREAM[5], CREAM)
    s.flat({(6, 11), (7, 12), (8, 11), (9, 12)}, OIL[2])
    s.outline()
    s.flat({(4, 9), (4, 10), (4, 11)}, (255, 255, 255, 190))
    return s


def linen_canvas():
    s = Sprite()
    roll = rect(2, 5, 13, 11)
    s.shade(roll, LINEN, lo=1, hi=5, mode="cyl_y", dome=0.3)
    for (x, y) in roll:
        if (x + y) % 2 == 0:
            s.put(x, y, mix(s.get(x, y), LINEN[2], 0.35), LINEN)
    end = ellipse(13.5, 8.0, 1.8, 3.6)
    s.shade(end, LINEN, lo=2, hi=5, dome=0.5)
    s.flat({(13, 7), (14, 8), (13, 9)}, LINEN[1], LINEN)
    s.shade(poly([(2.5, 11.5), (8.5, 11.5), (7.5, 14.5), (1.5, 14.5)]), LINEN, lo=2, hi=4, dome=0.2)
    for x in (5, 10):
        s.flat({(x, y) for y in range(5, 12)}, TWINE[3], TWINE)
    s.outline()
    s.spec(4, 6)
    return s


def starch():
    s = Sprite()
    bowl = poly([(2.5, 9.5), (13.5, 9.5), (11.5, 14.5), (4.5, 14.5)])
    s.shade(bowl, CERAMIC, lo=1, hi=5, mode="cyl_x", dome=0.3)
    s.flat({(x, 11) for x in range(4, 13)}, ramp(hexc("5A8AC0"))[3])
    white = ramp(hexc("F4F2EC"), base_index=4, value_step=0.05, hue_step=0.0, sat_step=0.0)
    s.shade(ellipse(8.0, 8.5, 5.4, 3.4), white, lo=1, hi=5, dome=0.55)
    s.shade(thick(line(10, 7, 14, 2), 1.0), STEEL, lo=2, hi=5, dome=0.3)
    s.shade(ellipse(10.0, 7.5, 1.6, 1.2), STEEL, lo=2, hi=5, dome=0.4)
    s.outline()
    s.spec(6, 7)
    return s


def bioplastic():
    s = Sprite()
    for i, (ox, oy) in enumerate(((2, 8), (4, 5), (6, 2))):
        sheet = poly([(ox + 0.5, oy + 0.5), (ox + 8.5, oy + 0.5), (ox + 7.5, oy + 6.5), (ox - 0.5, oy + 6.5)])
        s.clear(sheet)
        s.shade(sheet, FILM, lo=1 + i, hi=5, dome=0.15)
        for (x, y) in sheet:
            if x == ox + 1 + (y - oy) // 3:
                s.put(x, y, (240, 255, 252, 220), FILM)
    s.outline()
    s.spec(7, 3)
    s.spec(8, 3)
    return s


def pickled_brassica():
    s = Sprite()
    jar = rect(3, 4, 12, 14)
    s.flat(jar, (176, 208, 214, 255), GLASS_EDGE)
    s.shade(rect(4, 6, 11, 13), BRINE, lo=2, hi=4, dome=0.2)
    leaf = ramp(hexc("5A9890"))
    for (cx, cy, rx, ry) in ((6.0, 9.0, 2.4, 1.8), (9.5, 11.0, 2.4, 1.8), (7.5, 12.5, 2.6, 1.4), (9.0, 7.5, 2.0, 1.6)):
        s.shade(ellipse(cx, cy, rx, ry), leaf, lo=1, hi=4, dome=0.5)
    for (x, y) in ((6, 9), (9, 11), (9, 7)):
        s.put(x, y, LEAF_PALE[5], LEAF_PALE)
    glass_tint(s, jar, 60)
    s.shade(rect(3, 2, 12, 3), STEEL, lo=2, hi=5, mode="cyl_y", dome=0.0)
    s.outline()
    s.flat({(4, 5), (4, 6), (4, 7), (4, 8)}, (255, 255, 255, 190))
    return s


def gourd_puree():
    s = Sprite()
    bowl = poly([(1.5, 8.5), (14.5, 8.5), (12.5, 13.5), (3.5, 13.5)])
    s.shade(bowl, CERAMIC, lo=1, hi=5, mode="cyl_x", dome=0.3)
    s.shade(rect(5, 14, 10, 14), CERAMIC, lo=1, hi=3, mode="cyl_x", dome=0.0)
    s.shade(ellipse(8.0, 8.5, 6.4, 2.4), PUREE, lo=1, hi=5, dome=0.5)
    s.flat({(6, 8), (7, 8), (10, 9)}, PUREE[5], PUREE)
    s.shade(thick(line(10, 7, 14, 1), 1.0), WOOD, lo=2, hi=5, dome=0.3)
    s.outline()
    s.spec(4, 9)
    return s


def biofuel_cake():
    s = Sprite()
    cake = ramp(hexc("5E6A30"), value_step=0.14)
    top = poly([(2.5, 6.5), (9.5, 3.5), (14.5, 6.5), (7.5, 9.5)])
    front = poly([(2.5, 6.5), (7.5, 9.5), (7.5, 13.5), (2.5, 10.5)])
    side = poly([(7.5, 9.5), (14.5, 6.5), (14.5, 10.5), (7.5, 13.5)])
    s.shade(top, cake, lo=3, hi=5, dome=0.15)
    s.shade(front, cake, lo=1, hi=3, dome=0.1)
    s.shade(side, cake, lo=0, hi=2, dome=0.1)
    clover = ramp(hexc("58A848"))
    for (x, y) in ((5, 6), (8, 5), (11, 6), (9, 7), (4, 9), (6, 11), (10, 10), (12, 9)):
        if s.get(x, y) is not None:
            s.put(x, y, clover[3] if (x + y) % 2 else AMBER[2], cake)
    s.outline()
    return s


def ration_pack():
    s = Sprite()
    parcel = rect(2, 5, 13, 13)
    s.shade(parcel, LINEN, lo=1, hi=4, mode="cyl_x", dome=0.25)
    for (x, y) in parcel:
        if (x * 2 + y) % 3 == 0:
            s.put(x, y, mix(s.get(x, y), LINEN[1], 0.3), LINEN)
    s.shade(rect(2, 4, 13, 5), LINEN, lo=3, hi=5, mode="cyl_y", dome=0.0)
    for y in range(4, 14):
        s.put(8, y, TWINE[3], TWINE)
    for x in range(2, 14):
        s.put(x, 9, TWINE[3], TWINE)
    s.flat({(7, 3), (9, 3), (6, 2), (10, 2)}, TWINE[4], TWINE)
    tag = poly([(9.5, 10.5), (14.5, 10.5), (14.5, 14.5), (9.5, 14.5)])
    s.clear(tag)
    s.flat(tag, CREAM[5], CREAM)
    s.flat({(11, 12), (12, 11), (12, 12), (13, 13)}, LEAF[3])
    s.outline()
    s.spec(3, 6)
    return s


def edit_reagent(frame=0, frames=8):
    s = Sprite()
    ampoule = poly([(6.5, 3.5), (9.5, 3.5), (11.5, 7.5), (11.5, 14.5), (4.5, 14.5), (4.5, 7.5)])
    tip = rect(7, 1, 8, 3)
    s.flat(ampoule | tip, (176, 208, 214, 255), GLASS_EDGE)
    liquid = {(x, y) for (x, y) in ampoule if y >= 8}
    pulse = 0.15 * math.sin(frame / frames * 2 * math.pi)
    s.shade(liquid, CYAN, lo=1, hi=5, mode="cyl_x", dome=0.4, bias=pulse)
    for y in range(8, 14):
        ph = (y + frame) * 0.9
        xa = int(round(8 + math.sin(ph) * 1.6))
        xb = int(round(8 - math.sin(ph) * 1.6))
        s.put(xa, y, (240, 255, 250, 255), CYAN)
        s.put(xb, y, VIOLET[4], VIOLET)
    glass_tint(s, ampoule | tip, 50)
    s.flat({(7, 4)}, (255, 255, 255, 220))
    s.outline()
    s.flat({(5, 8), (5, 9), (5, 10)}, (255, 255, 255, 190))
    return s


def gene_editor_blueprint():
    s = Sprite()
    sheet = poly([(1.5, 3.5), (12.5, 3.5), (12.5, 13.5), (1.5, 13.5)])
    s.shade(sheet, BLUEPRINT, lo=2, hi=4, dome=0.1)
    pale = (196, 220, 248, 255)
    for x in range(2, 13, 3):
        for y in range(4, 14):
            if s.get(x, y) is not None:
                s.put(x, y, mix(s.get(x, y), pale, 0.25), BLUEPRINT)
    for (x, y) in (line(4, 6, 10, 6) | line(4, 6, 4, 11) | line(10, 6, 10, 11) | line(4, 11, 10, 11)):
        s.put(x, y, pale, BLUEPRINT)
    for y in range(7, 11):
        s.put(6 + (y % 2), y, pale, BLUEPRINT)
        s.put(8 - (y % 2), y, (150, 190, 240, 255), BLUEPRINT)
    s.shade(rect(12, 3, 14, 13), BLUEPRINT, lo=1, hi=5, mode="cyl_x", dome=0.2)
    s.outline()
    return s


def ribbon(metal):
    s = Sprite()
    tails = ramp(hexc({"bronze": "8A3A2A", "silver": "2E4E8A", "gold": "2E7A4A"}[metal]))
    m = {"bronze": ramp(hexc("C07A40"), value_step=0.18), "silver": ramp(hexc("C8D0D8"), value_step=0.14),
         "gold": ramp(hexc("E0B030"), value_step=0.2)}[metal]
    s.shade(poly([(5.5, 8.5), (8.5, 8.5), (6.5, 15.5), (3.5, 13.5)]), tails, lo=1, hi=4, dome=0.3)
    s.shade(poly([(7.5, 8.5), (10.5, 8.5), (12.5, 13.5), (9.5, 15.5)]), tails, lo=1, hi=4, dome=0.3)
    rosette = set()
    for i in range(12):
        a = i / 12 * 2 * math.pi
        rosette |= ellipse(8 + math.cos(a) * 4.2, 6.5 + math.sin(a) * 4.2, 1.8, 1.8)
    s.shade(rosette, tails, lo=2, hi=5, dome=0.4)
    s.shade(ellipse(8.0, 6.5, 3.6, 3.6), m, lo=1, hi=5, dome=0.6)
    s.flat({(7, 5), (8, 5), (7, 6)}, m[5], m)
    s.outline()
    s.spec(6, 4)
    return s


def logo():
    s = Sprite(32, 32)
    badge = ellipse(16.0, 16.0, 15.5, 15.5)
    s.shade(badge, ramp(hexc("1F4A44"), value_step=0.1), lo=1, hi=4, dome=0.35)
    rim = badge - ellipse(16.0, 16.0, 14.0, 14.0)
    s.shade(rim, BRASS, lo=1, hi=5, dome=0.4)
    for y in range(5, 26):
        t = y / 4.2
        a = int(round(16 + math.sin(t) * 6))
        b = int(round(16 - math.sin(t) * 6))
        s.put(a, y, CYAN[3] if y % 2 else CYAN[2], CYAN)
        s.put(b, y, VIOLET[3] if y % 2 else VIOLET[2], VIOLET)
        if y % 3 == 0:
            for x in range(min(a, b) + 1, max(a, b)):
                s.put(x, y, mix(s.get(x, y), CYAN[1], 0.5), CYAN)
    s.shade(ellipse(16.0, 26.5, 9.5, 3.6), SOIL, lo=1, hi=4, dome=0.5)
    s.shade(thick(line(16, 25, 16, 13), 1.2), LEAF, lo=2, hi=4, mode="cyl_x", dome=0.2)
    s.shade(poly([(15.5, 18.5), (9.5, 13.5), (6.5, 15.5), (11.5, 19.5)]), LEAF_PALE, lo=1, hi=5, dome=0.5)
    s.shade(poly([(16.5, 15.5), (22.5, 9.5), (25.5, 11.5), (20.5, 16.5)]), LEAF, lo=1, hi=5, dome=0.5)
    s.shade(ellipse(16.0, 11.5, 2.2, 2.6), LEAF_PALE, lo=2, hi=5, dome=0.6)
    s.outline()
    s.spec(10, 14)
    s.spec(22, 11)
    s.spec(9, 6)
    return s


def write_logo(path, scale=4):
    from pixel import _png
    sprite = logo()
    w, h = sprite.w * scale, sprite.h * scale
    rows = []
    for y in range(h):
        for x in range(w):
            c = sprite.get(x // scale, y // scale)
            rows.append(c if c is not None else (0, 0, 0, 0))
    _png(path, rows, w, h)


def animated(fn, frames):
    return [fn(frame=i, frames=frames) for i in range(frames)]


def all_items():
    out = []
    for species in ("wheatline", "maize", "soybean", "clover", "tuber", "brassica", "gourd", "fiber_flax"):
        out.append((f"{species}_seed_packet", seed_packet(species), None))
    out += [
        ("wheatline_sheaf", wheatline_sheaf(), None),
        ("maize_cob", maize_cob(), None),
        ("soybean_pod", soybean_pod(), None),
        ("clover_bundle", clover_bundle(), None),
        ("tuber", tuber(), None),
        ("brassica_head", brassica_head(), None),
        ("gourd", gourd(), None),
        ("flax_fiber", flax_fiber(), None),
        ("genome_card", animated(genome_card, 8), 3),
        ("soil_probe", [soil_probe(frame=i) for i in range(4)], 6),
        ("hand_lens", animated(hand_lens, 10), 2),
        ("pollination_brush", pollination_brush(), None),
        ("isolation_bag", isolation_bag(), None),
        ("grafting_knife", grafting_knife(), None),
        ("field_manual", animated(field_manual, 12), 2),
        ("field_notebook", field_notebook(), None),
        ("pedigree_sheet", pedigree_sheet(), None),
        ("trial_tag", trial_tag(), None),
        ("compost", compost(), None),
        ("lime_powder", lime_powder(), None),
        ("sulfur_powder", sulfur_powder(), None),
        ("nutrient_blend_n", nutrient_bag("N", "3E8AD0"), None),
        ("nutrient_blend_p", nutrient_bag("P", "D05A44"), None),
        ("nutrient_blend_k", nutrient_bag("K", "9A5AC8"), None),
        ("mutagen_vial", animated(mutagen_vial, 8), 3),
        ("blight_sample", animated(blight_sample, 12), 3),
        ("canopy_resin", animated(canopy_resin, 12), 3),
        ("canopy_key", animated(canopy_key, 10), 3),
        ("wild_seed", wild_seed(), None),
        ("green_manure", green_manure(), None),
        ("field_meal", field_meal(), None),
        ("soy_oil", animated(soy_oil, 8), 4),
        ("linen_canvas", linen_canvas(), None),
        ("starch", starch(), None),
        ("bioplastic", bioplastic(), None),
        ("pickled_brassica", pickled_brassica(), None),
        ("gourd_puree", gourd_puree(), None),
        ("biofuel_cake", biofuel_cake(), None),
        ("ration_pack", ration_pack(), None),
        ("edit_reagent", animated(edit_reagent, 8), 3),
        ("gene_editor_blueprint", gene_editor_blueprint(), None),
        ("ribbon_bronze", ribbon("bronze"), None),
        ("ribbon_silver", ribbon("silver"), None),
        ("ribbon_gold", ribbon("gold"), None),
    ]
    return out


def write_all(texture_dir):
    for name, frames, frametime in all_items():
        save(os.path.join(texture_dir, "item", name + ".png"), frames if isinstance(frames, list) else frames, frametime)
    return len(all_items())


if __name__ == "__main__":
    if len(sys.argv) >= 3 and sys.argv[1] == "--sheet":
        contact_sheet(sys.argv[2], [(n, f) for n, f, _ in all_items()])
        print("sheet written")
