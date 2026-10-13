package com.cultivar.client.ui;

import com.cultivar.client.ui.Icons.Icon;
import org.jspecify.annotations.Nullable;

public final class Chassis {
    public static final int HEADER = 16;

    private Chassis() {
    }

    public static void backdrop(Canvas c, float x, float y, float w, float h, int accent, float time) {
        float breathe = 0.10f + 0.05f * Anim.pulse(time, 0.25f);
        c.haloRect(x, y, w, h, 18, Theme.fade(accent, breathe));
        c.shadowRect(x, y, w, h, 10, 0x90000000);
    }

    public static void panel(Canvas c, float x, float y, float w, float h, int accent, @Nullable Icon icon, boolean working, float time,
                             float age) {
        c.round(x - 1, y - 1, w + 2, h + 2, 4, Theme.GLASS_EDGE_DARK);
        c.round(x, y, w, h, 3, Theme.GLASS_EDGE);
        c.rectV(x + 1, y + 1, w - 2, h - 2, Theme.GLASS_TOP, Theme.GLASS_BOTTOM);
        for (float gx = x + 8; gx < x + w - 1; gx += 8) {
            c.rect(gx, y + HEADER, 0.5f, h - HEADER - 1, Theme.GRID);
        }
        for (float gy = y + HEADER + 8; gy < y + h - 1; gy += 8) {
            c.rect(x + 1, gy, w - 2, 0.5f, Theme.GRID);
        }
        for (int i = 0; i < (int) (w * h / 90); i++) {
            float px = x + 2 + Anim.hash(i * 7 + 1) * (w - 4);
            float py = y + 2 + Anim.hash(i * 13 + 5) * (h - 4);
            c.rect(px, py, 0.6f, 0.6f, Anim.hash(i) > 0.5f ? 0x0CFFFFFF : 0x18000000);
        }
        c.rectV(x + 1, y + 1, w - 2, HEADER - 1, Theme.lerp(Theme.GLASS_TOP, Theme.shade(accent, 0.35f), 0.35f), Theme.GLASS_TOP);
        c.rectH(x + 1, y + HEADER - 1, (w - 2) / 2, 0.8f, Theme.fade(accent, 0f), Theme.fade(accent, 0.8f));
        c.rectH(x + w / 2, y + HEADER - 1, (w - 2) / 2, 0.8f, Theme.fade(accent, 0.8f), Theme.fade(accent, 0f));
        c.glow(true);
        c.rectV(x + 1, y + HEADER - 1, w - 2, 4, Theme.fade(accent, 0.22f), Theme.fade(accent, 0f));
        c.glow(false);
        c.rect(x + 2, y + 1, w - 4, 0.6f, 0x40FFFFFF);
        if (icon != null) {
            Icons.draw(c, icon, x + 10, y + 8, 9, accent);
        }
        Instruments.led(c, x + w - 9, y + 8, working ? Theme.GOOD : Theme.shade(accent, 0.5f), true, time * (working ? 3f : 0.6f));
        Instruments.scanlines(c, x + 1, y + HEADER, w - 2, h - HEADER - 1, time);
        float grow = Anim.easeOutBack(age / 0.45f);
        Instruments.brackets(c, x - 2, y - 2, w + 4, h + 4, 4 + 6 * grow, 1.2f, Theme.fade(accent, 0.9f));
        c.glow(true);
        Instruments.brackets(c, x - 3, y - 3, w + 6, h + 6, 5 + 6 * grow, 2.4f, Theme.fade(accent, 0.25f));
        c.glow(false);
    }

    public static void opening(Canvas c, float x, float y, float w, float h, int accent, float age) {
        float t = age / 0.5f;
        if (t >= 1f) {
            return;
        }
        float sweepY = y + h * Anim.easeOutCubic(t);
        float bottom = y + h;
        if (sweepY < bottom) {
            c.rect(x, sweepY, w, bottom - sweepY, Theme.fade(Theme.GLASS_BOTTOM, 0.98f));
        }
        c.glow(true);
        c.rectV(x - 6, sweepY - 10, w + 12, 10, Theme.fade(accent, 0f), Theme.fade(accent, 0.55f * (1 - t)));
        c.rect(x - 6, sweepY - 0.5f, w + 12, 1.2f, Theme.withAlpha(0xFFFFFF, 0.9f * (1 - t)));
        c.glow(false);
    }
}
