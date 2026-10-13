package com.cultivar.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class Instruments {
    public static final float MICRO = 0.62f;

    private Instruments() {
    }

    public static void section(Canvas c, Font font, Component label, float x, float y, float w, int accent) {
        c.text(font, label, x, y, MICRO, Theme.fade(accent, 0.85f));
        float lx = x + font.width(label) * MICRO + 3;
        if (lx < x + w) {
            c.rectH(lx, y + 2.5f, x + w - lx, 0.7f, Theme.fade(accent, 0.5f), Theme.fade(accent, 0f));
        }
    }

    public static void well(Canvas c, float x, float y, float w, float h) {
        c.rect(x, y, w, h, Theme.WELL);
        c.rectV(x, y, w, Math.min(4, h), 0x66000000, 0x00000000);
        c.frame(x, y, w, h, 0.7f, Theme.WELL_EDGE);
    }

    public static void bar(Canvas c, float x, float y, float w, float h, float value, int color, float time) {
        float v = Anim.clamp01(value);
        well(c, x - 1, y - 1, w + 2, h + 2);
        float fw = w * v;
        if (fw > 0.3f) {
            c.rectV(x, y, fw, h, Theme.shade(color, 1.15f), Theme.shade(color, 0.65f));
            c.rectV(x, y, fw, h * 0.45f, 0x40FFFFFF, 0x00FFFFFF);
            float sweep = (time * 0.7f) % 1.4f - 0.2f;
            float sx = x + sweep * w;
            if (sx > x && sx < x + fw) {
                c.glow(true);
                c.rectH(Math.max(x, sx - 8), y, Math.min(8, sx - x), h, 0x00FFFFFF, Theme.withAlpha(0xFFFFFF, 0.25f));
                c.glow(false);
            }
            c.bloom(x + fw, y + h / 2f, h * 1.8f + 2, Theme.fade(color, 0.7f));
            c.rect(x + fw - 0.8f, y, 0.8f, h, 0xCCFFFFFF);
        }
        for (int i = 1; i < 10; i++) {
            c.rect(x + w * i / 10f, y + h - Math.min(2f, h * 0.4f), 0.5f, Math.min(2f, h * 0.4f), 0x50000000);
        }
    }

    public static void segments(Canvas c, float x, float y, float w, float h, int n, float lit, int color) {
        float gap = 1f;
        float sw = (w - gap * (n - 1)) / n;
        for (int i = 0; i < n; i++) {
            float sx = x + i * (sw + gap);
            float on = Anim.clamp01(lit - i);
            c.rect(sx, y, sw, h, 0xFF0C181C);
            if (on > 0f) {
                c.rect(sx, y, sw, h, Theme.fade(color, 0.25f + 0.75f * on));
                c.rect(sx, y, sw, Math.max(0.6f, h * 0.3f), Theme.fade(0xFFFFFFFF, 0.25f * on));
                c.bloom(sx + sw / 2f, y + h / 2f, Math.max(sw, h) * 0.9f, Theme.fade(color, 0.25f * on));
            }
        }
    }

    public static void ring(Canvas c, float cx, float cy, float r, float thick, float value, int color, float time) {
        float v = Anim.clamp01(value);
        float start = (float) (-Math.PI / 2);
        c.arc(cx, cy, r - thick / 2, r + thick / 2, 0, (float) (Math.PI * 2), 0xFF0D1A1F, 0xFF12232A);
        if (v > 0.001f) {
            float end = start + (float) (Math.PI * 2) * v;
            c.arcSweep(cx, cy, r - thick / 2, r + thick / 2, start, end, Theme.shade(color, 0.5f), color);
            float hx = cx + Canvas.cos(end) * r;
            float hy = cy + Canvas.sin(end) * r;
            c.bloom(hx, hy, thick * 2.2f, Theme.fade(color, 0.6f + 0.3f * Anim.pulse(time, 1.5f)));
        }
        for (int i = 0; i < 24; i++) {
            float a = start + (float) (Math.PI * 2) * i / 24;
            float r0 = r + thick / 2 + 1;
            float r1 = r0 + (i % 6 == 0 ? 2.5f : 1.2f);
            c.line(cx + Canvas.cos(a) * r0, cy + Canvas.sin(a) * r0, cx + Canvas.cos(a) * r1, cy + Canvas.sin(a) * r1, 0.6f, 0x705A8A90);
        }
    }

    public static float chip(Canvas c, Font font, Component text, float x, float y, int color, boolean filled) {
        float w = font.width(text) * MICRO + 6;
        float h = 7;
        if (filled) {
            c.round(x, y, w, h, 2, Theme.fade(color, 0.85f));
            c.text(font, text, x + 3, y + 1.6f, MICRO, 0xFF0A1418);
        } else {
            c.round(x, y, w, h, 2, Theme.fade(color, 0.16f));
            c.frame(x, y, w, h, 0.6f, Theme.fade(color, 0.7f));
            c.text(font, text, x + 3, y + 1.6f, MICRO, color);
        }
        return w;
    }

    public static void led(Canvas c, float x, float y, int color, boolean on, float time) {
        if (on) {
            float p = 0.75f + 0.25f * Anim.pulse(time, 1.2f);
            c.bloom(x, y, 6, Theme.fade(color, 0.55f * p));
            c.disc(x, y, 1.8f, Theme.shade(color, 1.3f), color);
        } else {
            c.disc(x, y, 1.8f, 0xFF1C2A2E, 0xFF10191C);
        }
    }

    public static void brackets(Canvas c, float x, float y, float w, float h, float len, float t, int color) {
        c.rect(x, y, len, t, color);
        c.rect(x, y, t, len, color);
        c.rect(x + w - len, y, len, t, color);
        c.rect(x + w - t, y, t, len, color);
        c.rect(x, y + h - t, len, t, color);
        c.rect(x, y + h - len, t, len, color);
        c.rect(x + w - len, y + h - t, len, t, color);
        c.rect(x + w - t, y + h - len, t, len, color);
    }

    public static void scanlines(Canvas c, float x, float y, float w, float h, float time) {
        for (float yy = y; yy < y + h; yy += 2f) {
            c.rect(x, yy, w, 0.5f, 0x12000000);
        }
        float band = y + ((time * 22f) % (h + 20)) - 10;
        c.glow(true);
        c.rectV(x, band - 6, w, 6, 0x00FFFFFF, Theme.withAlpha(0xFFFFFF, 0.035f));
        c.rectV(x, band, w, 6, Theme.withAlpha(0xFFFFFF, 0.035f), 0x00FFFFFF);
        c.glow(false);
    }
}
