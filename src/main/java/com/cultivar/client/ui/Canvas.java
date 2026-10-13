package com.cultivar.client.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Arrays;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

public final class Canvas {
    private static final float TAU = (float) (Math.PI * 2);
    private final GuiGraphicsExtractor g;
    private float[] xy = new float[1024];
    private int[] colors = new int[512];
    private int vertices;
    private boolean glow;
    private float alpha = 1f;
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    public Canvas(GuiGraphicsExtractor g) {
        this.g = g;
        resetBounds();
    }

    public GuiGraphicsExtractor g() {
        return g;
    }

    public Canvas alpha(float a) {
        this.alpha = Anim.clamp01(a);
        return this;
    }

    public float alpha() {
        return alpha;
    }

    public Canvas glow(boolean on) {
        if (on != glow) {
            flush();
            glow = on;
        }
        return this;
    }

    public void flush() {
        if (vertices == 0) {
            return;
        }
        int x0 = (int) Math.floor(minX);
        int y0 = (int) Math.floor(minY);
        int x1 = (int) Math.ceil(maxX);
        int y1 = (int) Math.ceil(maxY);
        ScreenRectangle rect = new ScreenRectangle(x0, y0, Math.max(1, x1 - x0), Math.max(1, y1 - y0));
        ScreenRectangle scissor = g.peekScissorStack();
        ScreenRectangle bounds = scissor != null ? scissor.intersection(rect) : rect;
        if (bounds != null) {
            RenderPipeline pipeline = glow ? UiPipelines.GUI_GLOW : RenderPipelines.GUI;
            g.submitGuiElementRenderState(new Mesh(pipeline, Arrays.copyOf(xy, vertices * 2), Arrays.copyOf(colors, vertices), vertices,
                    scissor, bounds));
        }
        vertices = 0;
        resetBounds();
    }

    public void scissor(int x0, int y0, int x1, int y1) {
        flush();
        g.enableScissor(x0, y0, x1, y1);
    }

    public void endScissor() {
        flush();
        g.disableScissor();
    }

    private void resetBounds() {
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        maxX = -Float.MAX_VALUE;
        maxY = -Float.MAX_VALUE;
    }

    private void emit(float tx, float ty, int color) {
        if (vertices * 2 + 2 > xy.length) {
            xy = Arrays.copyOf(xy, xy.length * 2);
            colors = Arrays.copyOf(colors, colors.length * 2);
        }
        xy[vertices * 2] = tx;
        xy[vertices * 2 + 1] = ty;
        colors[vertices] = alpha < 1f ? Theme.fade(color, alpha) : color;
        vertices++;
        minX = Math.min(minX, tx);
        minY = Math.min(minY, ty);
        maxX = Math.max(maxX, tx);
        maxY = Math.max(maxY, ty);
    }

    public void quad(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2, float x3, float y3, int c3) {
        Matrix3x2f m = g.pose();
        float ax = m.m00 * x0 + m.m10 * y0 + m.m20;
        float ay = m.m01 * x0 + m.m11 * y0 + m.m21;
        float bx = m.m00 * x1 + m.m10 * y1 + m.m20;
        float by = m.m01 * x1 + m.m11 * y1 + m.m21;
        float cx = m.m00 * x2 + m.m10 * y2 + m.m20;
        float cy = m.m01 * x2 + m.m11 * y2 + m.m21;
        float dx = m.m00 * x3 + m.m10 * y3 + m.m20;
        float dy = m.m01 * x3 + m.m11 * y3 + m.m21;
        float area = (ax * by - bx * ay) + (bx * cy - cx * by) + (cx * dy - dx * cy) + (dx * ay - ax * dy);
        if (area <= 0f) {
            emit(ax, ay, c0);
            emit(bx, by, c1);
            emit(cx, cy, c2);
            emit(dx, dy, c3);
        } else {
            emit(ax, ay, c0);
            emit(dx, dy, c3);
            emit(cx, cy, c2);
            emit(bx, by, c1);
        }
    }

    public void tri(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2) {
        quad(x0, y0, c0, x1, y1, c1, x2, y2, c2, x2, y2, c2);
    }

    public void rect(float x, float y, float w, float h, int color) {
        quad(x, y, color, x, y + h, color, x + w, y + h, color, x + w, y, color);
    }

    public void rectV(float x, float y, float w, float h, int top, int bottom) {
        quad(x, y, top, x, y + h, bottom, x + w, y + h, bottom, x + w, y, top);
    }

    public void rectH(float x, float y, float w, float h, int left, int right) {
        quad(x, y, left, x, y + h, left, x + w, y + h, right, x + w, y, right);
    }

    public void frame(float x, float y, float w, float h, float t, int color) {
        rect(x, y, w, t, color);
        rect(x, y + h - t, w, t, color);
        rect(x, y + t, t, h - 2 * t, color);
        rect(x + w - t, y + t, t, h - 2 * t, color);
    }

    public void line(float x0, float y0, float x1, float y1, float width, int color) {
        line(x0, y0, x1, y1, width, color, color);
    }

    public void line(float x0, float y0, float x1, float y1, float width, int c0, int c1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return;
        }
        float nx = -dy / len * width * 0.5f;
        float ny = dx / len * width * 0.5f;
        quad(x0 + nx, y0 + ny, c0, x0 - nx, y0 - ny, c0, x1 - nx, y1 - ny, c1, x1 + nx, y1 + ny, c1);
    }

    public void polyline(float[] points, int count, float width, int color) {
        for (int i = 0; i + 1 < count; i++) {
            line(points[i * 2], points[i * 2 + 1], points[i * 2 + 2], points[i * 2 + 3], width, color);
        }
    }

    public void dashed(float x0, float y0, float x1, float y1, float width, float dash, float gap, float phase, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return;
        }
        float step = dash + gap;
        float start = -((phase % step) + step) % step;
        for (float s = start; s < len; s += step) {
            float a = Math.max(0f, s);
            float b = Math.min(len, s + dash);
            if (b > a) {
                line(x0 + dx * a / len, y0 + dy * a / len, x0 + dx * b / len, y0 + dy * b / len, width, color);
            }
        }
    }

    public void disc(float cx, float cy, float r, int center, int edge) {
        int segments = segmentsFor(r);
        for (int i = 0; i < segments; i++) {
            float a0 = TAU * i / segments;
            float a1 = TAU * (i + 1) / segments;
            quad(cx, cy, center, cx + cos(a0) * r, cy + sin(a0) * r, edge, cx + cos(a1) * r, cy + sin(a1) * r, edge, cx, cy, center);
        }
    }

    public void arc(float cx, float cy, float r0, float r1, float a0, float a1, int inner, int outer) {
        int segments = Math.max(2, (int) Math.ceil(Math.abs(a1 - a0) / TAU * segmentsFor(r1)));
        for (int i = 0; i < segments; i++) {
            float s0 = a0 + (a1 - a0) * i / segments;
            float s1 = a0 + (a1 - a0) * (i + 1) / segments;
            quad(cx + cos(s0) * r0, cy + sin(s0) * r0, inner, cx + cos(s0) * r1, cy + sin(s0) * r1, outer,
                    cx + cos(s1) * r1, cy + sin(s1) * r1, outer, cx + cos(s1) * r0, cy + sin(s1) * r0, inner);
        }
    }

    public void arcSweep(float cx, float cy, float r0, float r1, float a0, float a1, int from, int to) {
        int segments = Math.max(2, (int) Math.ceil(Math.abs(a1 - a0) / TAU * segmentsFor(r1)));
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            float s0 = a0 + (a1 - a0) * t0;
            float s1 = a0 + (a1 - a0) * t1;
            int c0 = Theme.lerp(from, to, t0);
            int c1 = Theme.lerp(from, to, t1);
            quad(cx + cos(s0) * r0, cy + sin(s0) * r0, c0, cx + cos(s0) * r1, cy + sin(s0) * r1, c0,
                    cx + cos(s1) * r1, cy + sin(s1) * r1, c1, cx + cos(s1) * r0, cy + sin(s1) * r0, c1);
        }
    }

    public void ring(float cx, float cy, float r, float width, int color) {
        arc(cx, cy, r - width * 0.5f, r + width * 0.5f, 0f, TAU, color, color);
    }

    public void bloom(float cx, float cy, float r, int color) {
        boolean was = glow;
        glow(true);
        disc(cx, cy, r, color, color & 0x00FFFFFF);
        glow(was);
    }

    public void haloRect(float x, float y, float w, float h, float spread, int color) {
        boolean was = glow;
        glow(true);
        int clear = color & 0x00FFFFFF;
        rectV(x, y - spread, w, spread, clear, color);
        rectV(x, y + h, w, spread, color, clear);
        rectH(x - spread, y, spread, h, clear, color);
        rectH(x + w, y, spread, h, color, clear);
        corner(x, y, spread, (float) Math.PI, color, clear);
        corner(x + w, y, spread, (float) (Math.PI * 1.5), color, clear);
        corner(x + w, y + h, spread, 0f, color, clear);
        corner(x, y + h, spread, (float) (Math.PI * 0.5), color, clear);
        glow(was);
    }

    public void shadowRect(float x, float y, float w, float h, float spread, int color) {
        boolean was = glow;
        glow(false);
        int clear = color & 0x00FFFFFF;
        rectV(x, y - spread, w, spread, clear, color);
        rectV(x, y + h, w, spread, color, clear);
        rectH(x - spread, y, spread, h, clear, color);
        rectH(x + w, y, spread, h, color, clear);
        corner(x, y, spread, (float) Math.PI, color, clear);
        corner(x + w, y, spread, (float) (Math.PI * 1.5), color, clear);
        corner(x + w, y + h, spread, 0f, color, clear);
        corner(x, y + h, spread, (float) (Math.PI * 0.5), color, clear);
        glow(was);
    }

    private void corner(float cx, float cy, float r, float start, int center, int edge) {
        int segments = 6;
        for (int i = 0; i < segments; i++) {
            float a0 = start + (float) (Math.PI * 0.5) * i / segments;
            float a1 = start + (float) (Math.PI * 0.5) * (i + 1) / segments;
            quad(cx, cy, center, cx + cos(a0) * r, cy + sin(a0) * r, edge, cx + cos(a1) * r, cy + sin(a1) * r, edge, cx, cy, center);
        }
    }

    public void round(float x, float y, float w, float h, float r, int color) {
        float rr = Math.min(r, Math.min(w, h) * 0.5f);
        rect(x + rr, y, w - 2 * rr, h, color);
        rect(x, y + rr, rr, h - 2 * rr, color);
        rect(x + w - rr, y + rr, rr, h - 2 * rr, color);
        corner(x + rr, y + rr, rr, (float) Math.PI, color, color);
        corner(x + w - rr, y + rr, rr, (float) (Math.PI * 1.5), color, color);
        corner(x + w - rr, y + h - rr, rr, 0f, color, color);
        corner(x + rr, y + h - rr, rr, (float) (Math.PI * 0.5), color, color);
    }

    public void fan(float cx, float cy, float[] points, int count, int center, int edge) {
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            quad(cx, cy, center, points[i * 2], points[i * 2 + 1], edge, points[j * 2], points[j * 2 + 1], edge, cx, cy, center);
        }
    }

    public void stripes(float x, float y, float w, float h, float period, float phase, int a, int b) {
        rect(x, y, w, h, b);
        float half = period * 0.5f;
        float offset = ((phase % period) + period) % period;
        for (float s = -h - period + offset; s < w; s += period) {
            float l0 = Math.max(x, x + s);
            float l1 = Math.min(x + w, x + s + half);
            if (l1 <= x || l0 >= x + w) {
                continue;
            }
            float bx0 = x + s;
            float bx1 = x + s + half;
            quad(clamp(bx0 + h, x, x + w), y, a, clamp(bx0, x, x + w), y + h, a, clamp(bx1, x, x + w), y + h, a,
                    clamp(bx1 + h, x, x + w), y, a);
        }
    }

    private boolean textVisible() {
        return alpha > 0.04f;
    }

    public void text(Font font, Component text, float x, float y, int color) {
        text(font, text, x, y, color, false);
    }

    public void text(Font font, Component text, float x, float y, int color, boolean shadow) {
        if (!textVisible()) {
            return;
        }
        flush();
        g.text(font, text, Math.round(x), Math.round(y), Theme.fade(color, alpha), shadow);
    }

    public void text(Font font, String text, float x, float y, int color) {
        text(font, Component.literal(text), x, y, color, false);
    }

    public void text(Font font, Component text, float x, float y, float scale, int color) {
        if (!textVisible()) {
            return;
        }
        flush();
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.text(font, text, 0, 0, Theme.fade(color, alpha), false);
        g.pose().popMatrix();
    }

    public void centered(Font font, Component text, float cx, float y, int color) {
        text(font, text, cx - font.width(text) / 2f, y, color, false);
    }

    public void centered(Font font, Component text, float cx, float y, float scale, int color) {
        text(font, text, cx - font.width(text) * scale / 2f, y, scale, color);
    }

    public void right(Font font, Component text, float rx, float y, int color) {
        text(font, text, rx - font.width(text), y, color, false);
    }

    public void right(Font font, Component text, float rx, float y, float scale, int color) {
        text(font, text, rx - font.width(text) * scale, y, scale, color);
    }

    public void glowText(Font font, Component text, float x, float y, int color, int glowColor) {
        int w = font.width(text);
        float cy = y + 3.5f;
        for (int i = 0; i <= 4; i++) {
            bloom(x + w * i / 4f, cy, 7, Theme.fade(glowColor, 0.10f));
        }
        int edge = Theme.fade(glowColor, 0.55f);
        text(font, text, x - 1, y, edge, false);
        text(font, text, x + 1, y, edge, false);
        text(font, text, x, y - 1, edge, false);
        text(font, text, x, y + 1, edge, false);
        text(font, text, x, y, color, false);
    }

    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    public static float sin(float a) {
        return (float) Math.sin(a);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int segmentsFor(float r) {
        return Math.max(12, Math.min(96, (int) (r * 2.2f)));
    }
}
