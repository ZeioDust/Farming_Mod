package com.cultivar.client.ui;

public final class Icons {
    public enum Icon { PLUS, MINUS, LEFT, RIGHT, UP, DOWN, CROSS, COPY, REFRESH, DEPOSIT, CHECK, DROP, LIGHT, SHIELD, HELIX, SEED, PAPER, FLASK, THERMO, SEARCH, GRID, WIND, BOOK, SPORE, SPLIT, PORTAL }

    private Icons() {
    }

    public static void draw(Canvas c, Icon icon, float cx, float cy, float s, int color) {
        float h = s * 0.5f;
        float w = Math.max(1f, s * 0.14f);
        switch (icon) {
            case PLUS -> {
                c.rect(cx - h * 0.8f, cy - w / 2, h * 1.6f, w, color);
                c.rect(cx - w / 2, cy - h * 0.8f, w, h * 1.6f, color);
            }
            case MINUS -> c.rect(cx - h * 0.8f, cy - w / 2, h * 1.6f, w, color);
            case LEFT -> chevron(c, cx, cy, h, w, (float) Math.PI, color);
            case RIGHT -> chevron(c, cx, cy, h, w, 0f, color);
            case UP -> chevron(c, cx, cy, h, w, (float) (-Math.PI / 2), color);
            case DOWN -> chevron(c, cx, cy, h, w, (float) (Math.PI / 2), color);
            case CROSS -> {
                c.line(cx - h * 0.7f, cy - h * 0.7f, cx + h * 0.7f, cy + h * 0.7f, w, color);
                c.line(cx - h * 0.7f, cy + h * 0.7f, cx + h * 0.7f, cy - h * 0.7f, w, color);
            }
            case COPY -> {
                c.frame(cx - h * 0.85f, cy - h * 0.85f, h * 1.2f, h * 1.2f, w * 0.8f, Theme.fade(color, 0.6f));
                c.frame(cx - h * 0.35f, cy - h * 0.35f, h * 1.2f, h * 1.2f, w * 0.8f, color);
            }
            case REFRESH -> {
                c.arc(cx, cy, h * 0.55f, h * 0.55f + w, (float) (-Math.PI * 0.2), (float) (Math.PI * 1.45), color, color);
                float ax = cx + Canvas.cos((float) (-Math.PI * 0.2)) * (h * 0.6f);
                float ay = cy + Canvas.sin((float) (-Math.PI * 0.2)) * (h * 0.6f);
                c.tri(ax - h * 0.35f, ay - h * 0.1f, color, ax + h * 0.35f, ay - h * 0.1f, color, ax, ay + h * 0.4f, color);
            }
            case DEPOSIT -> {
                c.rect(cx - w / 2, cy - h * 0.9f, w, h * 0.9f, color);
                c.tri(cx - h * 0.45f, cy - h * 0.1f, color, cx + h * 0.45f, cy - h * 0.1f, color, cx, cy + h * 0.35f, color);
                c.rect(cx - h * 0.85f, cy + h * 0.55f, h * 1.7f, w, color);
                c.rect(cx - h * 0.85f, cy + h * 0.1f, w, h * 0.5f, color);
                c.rect(cx + h * 0.85f - w, cy + h * 0.1f, w, h * 0.5f, color);
            }
            case CHECK -> {
                c.line(cx - h * 0.7f, cy, cx - h * 0.2f, cy + h * 0.55f, w * 1.2f, color);
                c.line(cx - h * 0.2f, cy + h * 0.55f, cx + h * 0.75f, cy - h * 0.6f, w * 1.2f, color);
            }
            case DROP -> {
                c.disc(cx, cy + h * 0.25f, h * 0.55f, color, color);
                c.tri(cx - h * 0.5f, cy + h * 0.05f, color, cx + h * 0.5f, cy + h * 0.05f, color, cx, cy - h * 0.85f, color);
            }
            case LIGHT -> {
                c.disc(cx, cy, h * 0.38f, color, color);
                for (int i = 0; i < 8; i++) {
                    float a = (float) (Math.PI * 2 * i / 8);
                    c.line(cx + Canvas.cos(a) * h * 0.55f, cy + Canvas.sin(a) * h * 0.55f, cx + Canvas.cos(a) * h * 0.9f,
                            cy + Canvas.sin(a) * h * 0.9f, w * 0.8f, color);
                }
            }
            case SHIELD -> {
                c.quad(cx - h * 0.75f, cy - h * 0.75f, color, cx - h * 0.75f, cy + h * 0.05f, color, cx, cy + h * 0.9f, color,
                        cx, cy - h * 0.9f, color);
                c.quad(cx + h * 0.75f, cy - h * 0.75f, Theme.shade(color, 0.7f), cx, cy - h * 0.9f, Theme.shade(color, 0.7f),
                        cx, cy + h * 0.9f, Theme.shade(color, 0.7f), cx + h * 0.75f, cy + h * 0.05f, Theme.shade(color, 0.7f));
            }
            case HELIX -> {
                int steps = 10;
                for (int i = 0; i < steps; i++) {
                    float t0 = i / (float) steps;
                    float t1 = (i + 1) / (float) steps;
                    float y0 = cy - h + t0 * s;
                    float y1 = cy - h + t1 * s;
                    float lean0 = (t0 - 0.5f) * h * 0.5f;
                    float lean1 = (t1 - 0.5f) * h * 0.5f;
                    float a0 = Canvas.sin(t0 * 9.42f) * h * 0.55f;
                    float a1 = Canvas.sin(t1 * 9.42f) * h * 0.55f;
                    c.line(cx + lean0 + a0, y0, cx + lean1 + a1, y1, w, color);
                    c.line(cx + lean0 - a0, y0, cx + lean1 - a1, y1, w, Theme.fade(color, 0.55f));
                    if (i % 2 == 1) {
                        c.line(cx + lean0 - a0, y0, cx + lean0 + a0, y0, w * 0.55f, Theme.fade(color, 0.7f));
                    }
                }
            }
            case SEED -> {
                c.disc(cx, cy, h * 0.6f, color, Theme.shade(color, 0.7f));
                c.line(cx - h * 0.25f, cy - h * 0.25f, cx + h * 0.25f, cy + h * 0.25f, w * 0.7f, Theme.shade(color, 0.5f));
            }
            case PAPER -> {
                c.quad(cx - h * 0.6f, cy - h * 0.85f, color, cx - h * 0.6f, cy + h * 0.85f, color, cx + h * 0.6f, cy + h * 0.85f, color,
                        cx + h * 0.6f, cy - h * 0.4f, color);
                c.tri(cx + h * 0.15f, cy - h * 0.85f, Theme.shade(color, 0.6f), cx + h * 0.15f, cy - h * 0.4f, Theme.shade(color, 0.6f),
                        cx + h * 0.6f, cy - h * 0.4f, Theme.shade(color, 0.6f));
                c.quad(cx - h * 0.6f, cy - h * 0.85f, color, cx - h * 0.6f, cy - h * 0.4f, color, cx + h * 0.15f, cy - h * 0.4f, color,
                        cx + h * 0.15f, cy - h * 0.85f, color);
                for (int i = 0; i < 3; i++) {
                    c.rect(cx - h * 0.4f, cy - h * 0.1f + i * h * 0.3f, h * 0.8f, w * 0.6f, Theme.shade(color, 0.55f));
                }
            }
            case FLASK -> {
                c.rect(cx - h * 0.18f, cy - h * 0.9f, h * 0.36f, h * 0.7f, color);
                c.quad(cx - h * 0.18f, cy - h * 0.2f, color, cx - h * 0.75f, cy + h * 0.85f, color, cx + h * 0.75f, cy + h * 0.85f, color,
                        cx + h * 0.18f, cy - h * 0.2f, color);
            }
            case THERMO -> {
                c.round(cx - h * 0.2f, cy - h * 0.9f, h * 0.4f, h * 1.3f, h * 0.2f, color);
                c.disc(cx, cy + h * 0.55f, h * 0.38f, color, color);
            }
            case SEARCH -> {
                c.ring(cx - h * 0.15f, cy - h * 0.15f, h * 0.5f, w, color);
                c.line(cx + h * 0.2f, cy + h * 0.2f, cx + h * 0.8f, cy + h * 0.8f, w * 1.3f, color);
            }
            case WIND -> {
                for (int i = 0; i < 3; i++) {
                    float yy = cy - h * 0.55f + i * h * 0.55f;
                    float len = h * (i == 1 ? 1.6f : 1.2f);
                    c.line(cx - h * 0.8f, yy, cx - h * 0.8f + len, yy, w, color);
                    c.arc(cx - h * 0.8f + len, yy - h * 0.2f, h * 0.2f - w / 2, h * 0.2f + w / 2, (float) (-Math.PI / 2), (float) (Math.PI / 2),
                            color, color);
                }
            }
            case BOOK -> {
                c.quad(cx - h * 0.85f, cy - h * 0.7f, color, cx - h * 0.85f, cy + h * 0.75f, color, cx - w / 2, cy + h * 0.9f, color,
                        cx - w / 2, cy - h * 0.55f, color);
                c.quad(cx + w / 2, cy - h * 0.55f, Theme.shade(color, 0.75f), cx + w / 2, cy + h * 0.9f, Theme.shade(color, 0.75f),
                        cx + h * 0.85f, cy + h * 0.75f, Theme.shade(color, 0.75f), cx + h * 0.85f, cy - h * 0.7f, Theme.shade(color, 0.75f));
                for (int i = 0; i < 3; i++) {
                    float yy = cy - h * 0.3f + i * h * 0.3f;
                    c.rect(cx - h * 0.7f, yy, h * 0.5f, w * 0.5f, Theme.shade(color, 0.45f));
                    c.rect(cx + h * 0.2f, yy, h * 0.5f, w * 0.5f, Theme.shade(color, 0.4f));
                }
            }
            case SPORE -> {
                for (int i = 0; i < 7; i++) {
                    float a = (float) (Math.PI * 2 * i / 7);
                    float rr = h * (i % 2 == 0 ? 0.65f : 0.4f);
                    c.disc(cx + Canvas.cos(a) * rr, cy + Canvas.sin(a) * rr, h * 0.16f, color, color);
                }
                c.disc(cx, cy, h * 0.22f, color, color);
            }
            case SPLIT -> {
                c.line(cx, cy + h * 0.9f, cx, cy, w, color);
                c.line(cx, cy, cx - h * 0.6f, cy - h * 0.7f, w, color);
                c.line(cx, cy, cx + h * 0.6f, cy - h * 0.7f, w, color);
                c.disc(cx - h * 0.6f, cy - h * 0.7f, h * 0.2f, color, color);
                c.disc(cx + h * 0.6f, cy - h * 0.7f, h * 0.2f, Theme.MUTANT, Theme.MUTANT);
            }
            case PORTAL -> {
                for (int i = 0; i < 8; i++) {
                    float a = (float) (Math.PI * 2 * i / 8);
                    c.rect(cx + Canvas.cos(a) * h * 0.7f - h * 0.15f, cy + Canvas.sin(a) * h * 0.7f - h * 0.15f, h * 0.3f, h * 0.3f, color);
                }
                c.disc(cx, cy, h * 0.35f, Theme.fade(color, 0.6f), Theme.fade(color, 0.1f));
            }
            case GRID -> {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        c.rect(cx - h * 0.8f + i * h * 0.6f, cy - h * 0.8f + j * h * 0.6f, h * 0.4f, h * 0.4f, color);
                    }
                }
            }
        }
    }

    private static void chevron(Canvas c, float cx, float cy, float h, float w, float angle, int color) {
        float ca = Canvas.cos(angle);
        float sa = Canvas.sin(angle);
        float tipX = cx + ca * h * 0.45f;
        float tipY = cy + sa * h * 0.45f;
        float bx = cx - ca * h * 0.35f;
        float by = cy - sa * h * 0.35f;
        float px = -sa * h * 0.7f;
        float py = ca * h * 0.7f;
        c.line(bx + px, by + py, tipX, tipY, w * 1.2f, color);
        c.line(bx - px, by - py, tipX, tipY, w * 1.2f, color);
    }
}
