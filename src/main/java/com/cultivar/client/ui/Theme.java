package com.cultivar.client.ui;

public final class Theme {
    public static final int BACKDROP = 0xFF060B0E;
    public static final int GLASS_TOP = 0xFF16262D;
    public static final int GLASS_BOTTOM = 0xFF0B151A;
    public static final int GLASS_EDGE = 0xFF2A454D;
    public static final int GLASS_EDGE_DARK = 0xFF050A0C;
    public static final int WELL = 0xFF071014;
    public static final int WELL_EDGE = 0xFF1F3A42;
    public static final int GRID = 0x146FE0C8;
    public static final int TEXT = 0xFFDCF2EE;
    public static final int TEXT_DIM = 0xFF8DB2AD;
    public static final int TEXT_MUTED = 0xFF4F716E;
    public static final int GOOD = 0xFF5CE08A;
    public static final int WARN = 0xFFF2B84A;
    public static final int BAD = 0xFFF2585A;
    public static final int INFO = 0xFF5AB8F2;
    public static final int MUTANT = 0xFFC77DFF;
    public static final int WILD = 0xFF8FD86A;

    public static final int ACCENT_LAB = 0xFF3FE0C8;
    public static final int ACCENT_MUTAGEN = 0xFFB86BFF;
    public static final int ACCENT_COMPOST = 0xFFE7A445;
    public static final int ACCENT_CLIMATE = 0xFF58B8FF;
    public static final int ACCENT_BANK = 0xFF7BE07B;
    public static final int ACCENT_CARD = 0xFFE9D27A;
    public static final int ACCENT_BLIGHT = 0xFFE0703F;

    private Theme() {
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int withAlpha(int color, float a) {
        int alpha = Math.round(Math.max(0f, Math.min(1f, a)) * 255f);
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    public static int fade(int color, float a) {
        int alpha = Math.round((color >>> 24) * Math.max(0f, Math.min(1f, a)));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int from, int to, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * k);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * k);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * k);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int shade(int color, float k) {
        int r = Math.min(255, Math.round(((color >> 16) & 0xFF) * k));
        int g = Math.min(255, Math.round(((color >> 8) & 0xFF) * k));
        int b = Math.min(255, Math.round((color & 0xFF) * k));
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int quality(float t) {
        if (t >= 0.5f) {
            return lerp(WARN, GOOD, (t - 0.5f) * 2f);
        }
        return lerp(BAD, WARN, t * 2f);
    }

    public static int hsv(float h, float s, float v) {
        float hh = (h - (float) Math.floor(h)) * 6f;
        int i = (int) hh;
        float f = hh - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        float r;
        float g;
        float b;
        switch (i) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return 0xFF000000 | (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
    }
}
